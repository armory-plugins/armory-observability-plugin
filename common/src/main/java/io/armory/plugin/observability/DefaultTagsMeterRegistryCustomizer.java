package io.armory.plugin.observability;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * A registry customizer that will add the default tags that enable observability best practices for Armory.
 */
@Slf4j
public class DefaultTagsMeterRegistryCustomizer implements MeterRegistryCustomizer<MeterRegistry> {

    private final ArmoryEnvironmentMetadata environmentMetadata;
    private final ArmoryObservabilityPluginProperties pluginProperties;
    private final String springInjectedApplicationName;

    public DefaultTagsMeterRegistryCustomizer(ArmoryObservabilityPluginProperties pluginProperties,
                                              @Value("${spring.application.name:#{null}}") String springInjectedApplicationName) {

        this.pluginProperties = pluginProperties;
        this.springInjectedApplicationName = springInjectedApplicationName;

        var buildProperties = getBuildProperties();

        environmentMetadata = ArmoryEnvironmentMetadata.builder()
                .applicationName(buildProperties.getName())
                .armoryAppVersion(buildProperties.getVersion())
                .ossAppVersion(buildProperties.get("ossVersion"))
                .spinnakerRelease(buildProperties.get("spinnakerRelease"))
                .customerEnvId(pluginProperties.getCustomerEnvId())
                .customerEnvName(pluginProperties.getCustomerEnvName())
                .customerName(pluginProperties.getCustomerName())
                .build();
    }

    /**
     * @return Map of environment metadata that we will use as the default tags, with all null/empty values stripped.
     */
    private Map<String, String> getDefaultTagsAsFilteredMap() {
        Map<String, String> tags = new HashMap<>();
        String resolvedApplicationName = Optional
                .ofNullable(environmentMetadata.getApplicationName())
                .or(() -> Optional.ofNullable(springInjectedApplicationName))
                .orElse("UNKNOWN");

        tags.put("applicationName", resolvedApplicationName);                    // clouddriver
        tags.put("armoryAppVersion", environmentMetadata.getArmoryAppVersion()); // 2.1.0
        tags.put("ossAppVersion", environmentMetadata.getOssAppVersion());       // 0.22.1
        tags.put("spinnakerVersion", environmentMetadata.getSpinnakerRelease()); // 2.19.8
        tags.put("customerEnvId", environmentMetadata.getCustomerEnvId());       // e0fb0422-aa8e-11ea-bb37-0242ac130002
        tags.put("customerEnvName", environmentMetadata.getCustomerEnvName());   // prod
        tags.put("customerName", environmentMetadata.getCustomerName());         // armory

        // If KUBERNETES_SERVICE_HOST is set, lets assume we are in a K8s environment
        Optional.ofNullable(System.getenv("KUBERNETES_SERVICE_HOST"))
                .ifPresent(host -> {
                    tags.put("k8sVersion", fetchKubernetesVersion(host));
                    tags.put("podName", System.getenv("HOSTNAME"));
                });

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
     * @return build-related information such as group and artifact.
     */
    private BuildProperties getBuildProperties() {
        var buildInfoProperties = new Properties();
        try (var is = this.getClass().getClassLoader().getResourceAsStream("META-INF/build-info.properties")) {
            buildInfoProperties.load(is);
        } catch (Exception e) {
            log.warn("You can ignore the following warning if you are not running an Armory Wrapper Spinnaker Service for Spinnaker >= 2.19");
            log.warn("Failed to load META-INF/build-info.properties, msg: {}", e.getMessage());
        }
        return new BuildProperties(buildInfoProperties);
    }

    /**
     * This method will attempt to fetch the K8s Git Version from the version endpoint
     *
     * @return the git version of the K8s cluster.
     */
    private String fetchKubernetesVersion(String host) {
        log.info("Fetching version data from the K8s service api with 5 second timeout");
        try {
            var client = new OkHttpClient.Builder()
                    .callTimeout(5, TimeUnit.SECONDS)
                    .readTimeout(5, TimeUnit.SECONDS)
                    .connectTimeout(5, TimeUnit.SECONDS)
                    .build();
            var response = client.newCall(new Request.Builder()
                    .url(String.format("http:%s/version", host)).build()).execute();

            @SuppressWarnings("ConstantConditions")
            Map<String, Object> versionData = new ObjectMapper()
                    .readValue(response.body().bytes(), new TypeReference<HashMap<String, Object>>() {
                    });
            return String.valueOf(versionData.get("gitVersion"));
        } catch (Exception e) {
            log.warn("Failed to fetch version data from the K8s service API", e);
            return null;
        }
    }

    private List<Tag> getDefaultTags() {
        return getDefaultTagsAsFilteredMap().entrySet().stream()
                .map(tag -> {
                    log.info("Adding default tag {}: {} to default tags list.", tag.getKey(), tag.getValue());
                    return io.micrometer.core.instrument.Tag.of(tag.getKey(), tag.getValue());
                })
                .collect(Collectors.toList());
    }

    @Override
    public void customize(MeterRegistry registry) {
        if (!pluginProperties.defaultTagsDisabled) {
            registry.config().commonTags(getDefaultTags());
        }
    }
}
