package io.armory.plugin.observability;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import io.prometheus.client.CollectorRegistry;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.boot.actuate.metrics.export.prometheus.PrometheusScrapeEndpoint;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static io.armory.plugin.observability.MeterFilters.METER_FILTERS;

@Slf4j
@Configuration
@EnableConfigurationProperties
public class PluginConfiguration {

    @Bean
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    ArmoryEnvironmentMetadata environmentMetadata(ArmoryObservabilityPluginProperties pluginProperties) {

        var buildProperties = getBuildProperties();

        return ArmoryEnvironmentMetadata.builder()
                .applicationName(buildProperties.getName())
                .armoryAppVersion(buildProperties.getVersion())
                .ossAppVersion(buildProperties.get("ossVersion"))
                .spinnakerRelease(buildProperties.get("spinnakerRelease"))
                .customerName(pluginProperties.getCustomerName())
                .customerEnvName(pluginProperties.getCustomerEnvName())
                .customerEnvId(pluginProperties.getCustomerEnvId())
                .build();
    }

    /**
     * @return Map of environment metadata that we will use as the default tags, with all null/empty values stripped.
     */
    @Bean
    Map<String, String> defaultTags(ArmoryEnvironmentMetadata environmentMetadata) {
        Map<String, String> tags = new HashMap<>();
        tags.put("applicationName", environmentMetadata.getApplicationName());   // clouddriver
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
     * @return A registry customizer that will add the default tags that enable observability best practices for Armory.
     */
    @Bean
    public MeterRegistryCustomizer<MeterRegistry> defaultTagsMeterRegistryCustomizer(Map<String, String> defaultTags) {
        log.info("Enabling default Armory observability metric tags");
        return meterRegistry -> {
            List<Tag> tags = defaultTags.entrySet().stream()
                    .map(tag -> {
                        log.info("Adding default tag {}: {} to registry", tag.getKey(), tag.getValue());
                        return Tag.of(tag.getKey(), tag.getValue());
                    })
                    .collect(Collectors.toList());
            meterRegistry.config().commonTags(tags);
        };
    }

    /**
     * https://micrometer.io/docs/concepts#_denyaccept_meters
     *
     * @return A registry customizer that will add our filters and transformers to configure / customize metrics to be
     * more efficient for metrics platforms that care about the number of unique MTS and or DPM.
     */
    @Bean
    public MeterRegistryCustomizer<MeterRegistry> metricFiltersMeterRegistryCustomizer() {
        log.info("Enabling default Armory observability meter registry filters");
        return meterRegistry -> METER_FILTERS.forEach(meterFilter -> meterRegistry.config().meterFilter(meterFilter));
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
                    .readValue(response.body().bytes(), new TypeReference<HashMap<String, Object>>() {});
            return String.valueOf(versionData.get("gitVersion"));
        } catch (Exception e) {
            log.warn("Failed to fetch version data from the K8s service API", e);
            return null;
        }
    }

    /**
     * Loads the the build properties Spring boot metadata object. Normally you would get this auto injected into your configuration.
     * Since this is a simple plugin, we can just read the file and load the props.
     *
     * If the file is not present, because the plugin is being loaded into OSS Spinnaker for example, the props will all be null
     *
     * @return build-related information such as group and artifact.
     */
    private BuildProperties getBuildProperties() {
        var buildInfoProperties = new Properties();
        try(var is = this.getClass().getClassLoader().getResourceAsStream("META-INF/build-info.properties")) {
            buildInfoProperties.load(is);
        } catch (Exception e) {
            log.warn("You can ignore the following warning if you are not running an Armory Wrapper Spinnaker Service for Spinnaker >= 2.19");
            log.warn("Failed to load META-INF/build-info.properties", e);
        }
        return new BuildProperties(buildInfoProperties);
    }

    @Primary
    @Bean
    public MeterRegistry prometheusRegistry() {
        return new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
    }

    @Bean
    public PrometheusScrapeEndpoint prometheusEndpoint() {
        return new PrometheusScrapeEndpoint(new CollectorRegistry(true));
    }
}
