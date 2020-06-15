package io.armory.plugin.observability.meterregistrycustomizer;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.armory.plugin.observability.model.ArmoryEnvironmentMetadata;
import io.armory.plugin.observability.model.PluginConfig;
import io.armory.plugin.observability.model.PluginMetricsConfig;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.boot.info.BuildProperties;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;

/**
 * A registry customizer that will add the default tags that enable observability best practices for Armory.
 */
@Slf4j
public class DefaultTagsMeterRegistryCustomizer implements MeterRegistryCustomizer<MeterRegistry> {

    private static final String SPRING_BOOT_BUILD_PROPERTIES_PATH = "META-INF/build-info.properties";

    protected final PluginMetricsConfig metricsConfig;
    private final String springInjectedApplicationName;

    public DefaultTagsMeterRegistryCustomizer(PluginConfig metricsConfig,
                                              @Value("${spring.application.name:#{null}}") String springInjectedApplicationName) {

        this.metricsConfig = metricsConfig.getMetrics();
        this.springInjectedApplicationName = springInjectedApplicationName;
    }

    protected ArmoryEnvironmentMetadata getEnvironmentMetadata(BuildProperties buildProperties) {

        String resolvedApplicationName = ofNullable(buildProperties.getName())
                .or(() -> ofNullable(springInjectedApplicationName))
                .orElse("UNKNOWN");

        return ArmoryEnvironmentMetadata.builder()
                .applicationName(resolvedApplicationName)
                .armoryAppVersion(buildProperties.getVersion())
                .ossAppVersion(buildProperties.get("ossVersion"))
                .spinnakerRelease(buildProperties.get("spinnakerRelease"))
                .build();
    }

    /**
     * @return Map of environment metadata that we will use as the default tags, with all null/empty values stripped.
     */
    protected Map<String, String> getDefaultTagsAsFilteredMap(ArmoryEnvironmentMetadata environmentMetadata) {
        Map<String, String> tags = new HashMap<>(metricsConfig.getAdditionalTags());

        tags.put("applicationName", environmentMetadata.getApplicationName());   // clouddriver
        tags.put("armoryAppVersion", environmentMetadata.getArmoryAppVersion()); // 2.1.0
        tags.put("ossAppVersion", environmentMetadata.getOssAppVersion());       // 0.22.1
        tags.put("spinnakerRelease", environmentMetadata.getSpinnakerRelease()); // 2.19.8
        tags.put("hostname", System.getenv("HOSTNAME"));

        // If KUBERNETES_SERVICE_HOST and KUBERNETES_SERVICE_PORT is set, lets assume we are in a K8s environment
        ofNullable(System.getenv("KUBERNETES_SERVICE_HOST"))
                .ifPresent(host -> ofNullable(System.getenv("KUBERNETES_SERVICE_PORT"))
                        .ifPresent(port -> tags.put("k8sVersion", fetchKubernetesVersion(host, port))));

        return tags.entrySet().stream()
                .filter(it -> (it.getValue() != null && !it.getValue().strip().equals("")))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /**
     * Loads the the build properties Spring boot metadata object. Normally you would get this auto injected into your configuration.
     * Since this is a simple plugin, we can just read the file and load the props.
     * <p>
     * If the file is not present, because the plugin is being loaded into OSS Spinnaker for example, the props will all be null
     *
     * Duplicates the logic from https://github.com/spring-projects/spring-boot/blob/28e1b90735a57eb637690a4a029b462f8a3eafee/spring-boot-project/spring-boot-autoconfigure/src/main/java/org/springframework/boot/autoconfigure/info/ProjectInfoAutoConfiguration.java#L68-L86
     *
     * @return build-related information such as group and artifact.
     */
    protected BuildProperties getBuildProperties(String propertiesPath) {
        var buildInfoPrefix = "build.";
        var buildInfoProperties = new Properties();
        try (var is = this.getClass().getClassLoader().getResourceAsStream(propertiesPath)) {
            var rawProperties = new Properties();
            rawProperties.load(is);
            rawProperties.stringPropertyNames().stream()
                    .filter(propName -> propName.startsWith(buildInfoPrefix))
                    .forEach(propName -> buildInfoProperties.put(propName.substring(buildInfoPrefix.length()), rawProperties.get(propName)));
        } catch (Exception e) {
            log.warn("You can ignore the following warning if you are not running an Armory Wrapper Spinnaker Service for Spinnaker >= 2.19");
            log.warn("Failed to load META-INF/build-info.properties, msg: {}", e.getMessage());
        }
        return new BuildProperties(buildInfoProperties);
    }

    /**
     * This method will attempt to fetch the K8s Git Version from the version endpoint.
     *
     * @return the git version of the K8s cluster.
     */
    protected String fetchKubernetesVersion(String host, String port) {
        log.info("Fetching version data from the K8s service api with 5 second timeout");
        try {
            var client = new OkHttpClient.Builder()
                    .callTimeout(1, TimeUnit.SECONDS)
                    .readTimeout(1, TimeUnit.SECONDS)
                    .connectTimeout(1, TimeUnit.SECONDS)
                    .hostnameVerifier((hostname, session) -> true) // ignore hostnames for K8s API
                    .build();
            var response = client.newCall(new Request.Builder()
                    .url(String.format("https://%s:%s/version", host, port)).build()).execute();

            if (!response.isSuccessful()) {
                throw new RuntimeException("Failed to get version metadata from K8s API status code: " + response.code());
            }

            @SuppressWarnings("ConstantConditions")
            Map<String, Object> versionData = new ObjectMapper()
                    .readValue(response.body().bytes(), new TypeReference<HashMap<String, Object>>() {
                    });
            return String.valueOf(versionData.get("gitVersion"));
        } catch (Exception e) {
            log.warn("Failed to fetch version data from the K8s service API, msg: " + e.getMessage());
            return null;
        }
    }

    protected List<Tag> getDefaultTags(Map<String, String> tags) {
        return tags.entrySet().stream()
                .map(tag -> {
                    log.info("Adding default tag {}: {} to default tags list.", tag.getKey(), tag.getValue());
                    return Tag.of(tag.getKey(), tag.getValue());
                })
                .collect(Collectors.toList());
    }

    protected String getPropertiesPath() {
        return SPRING_BOOT_BUILD_PROPERTIES_PATH;
    }

    @Override
    public void customize(MeterRegistry registry) {
        if (!metricsConfig.isDefaultTagsDisabled()) {
            var propertiesPath = getPropertiesPath();
            var buildProperties = getBuildProperties(propertiesPath);
            var environmentMetadata = getEnvironmentMetadata(buildProperties);
            var tagsAsMap = getDefaultTagsAsFilteredMap(environmentMetadata);
            var defaultTags = getDefaultTags(tagsAsMap);
            registry.config().commonTags(defaultTags);
        }
    }
}
