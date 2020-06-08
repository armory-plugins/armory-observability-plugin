package io.armory.plugin.metrics.configuration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.armory.plugin.metrics.model.ArmoryEnvironmentMetadata;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static io.armory.plugin.metrics.MeterFilters.METER_FILTERS;

@Slf4j
@Configuration
public class MetricsConfiguration {

    private final ArmoryEnvironmentMetadata armoryEnvironmentMetadata;

    public MetricsConfiguration(BuildProperties buildProperties,
                                @Value("${armory.metrics.customerEnvId:#{null}}") String customerEnvId,
                                @Value("${armory.metrics.customerEnvName:#{null}}") String customerEnvName,
                                @Value("${armory.metrics.customerName:#{null}}") String customerName) {

        log.info("Initializing Armory Metrics for managed monitoring, alerting and KPIs");
        armoryEnvironmentMetadata = ArmoryEnvironmentMetadata.builder()
                .applicationName(buildProperties.getName())
                .armoryAppVersion(buildProperties.getVersion())
                .ossAppVersion(buildProperties.get("ossVersion"))
                .spinnakerRelease(buildProperties.get("spinnakerRelease"))
                .customerEnvId(customerEnvId)
                .customerEnvName(customerEnvName)
                .customerName(customerName)
                .build();
    }

    @Bean(name = "armoryMetricsDefaultTags")
    Map<String, String> defaultTags() {

        var tags = new HashMap<>(Map.of(
            "applicationName", armoryEnvironmentMetadata.getApplicationName(),   // clouddriver
            "armoryAppVersion", armoryEnvironmentMetadata.getArmoryAppVersion(), // 2.1.0
            "ossAppVersion", armoryEnvironmentMetadata.getOssAppVersion(),       // 0.22.1
            "spinnakerVersion", armoryEnvironmentMetadata.getSpinnakerRelease(), // 2.19.8
            "customerEnvName", armoryEnvironmentMetadata.getCustomerEnvName(),   // prod
            "customerName", armoryEnvironmentMetadata.getCustomerName()          // armory
        ));

        // If KUBERNETES_SERVICE_HOST is set, lets assume we are in a K8s environment
        Optional.ofNullable(System.getenv("KUBERNETES_SERVICE_HOST"))
                .ifPresent(host -> {
                    fetchKubernetesVersion(host)
                            .ifPresent(version -> tags.put("k8sVersion", version));
                    Optional.ofNullable(System.getenv("HOSTNAME"))
                            .ifPresent(hostname -> tags.put("podName", hostname));
                });
        return tags;
    }

    @Bean
    MeterRegistryCustomizer<MeterRegistry> addDefaultTags(@Qualifier("armoryMetricsDefaultTags") Map<String, String> defaultTags) {
        return meterRegistry -> {
            List<Tag> tags = defaultTags.entrySet().stream()
                    .filter(it -> (it.getValue() != null && !it.getValue().strip().equals(""))) // filter out tags with no values
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
     */
    @Bean
    MeterRegistryCustomizer<MeterRegistry> addMetricFilters() {
        return meterRegistry -> METER_FILTERS.forEach(meterFilter -> meterRegistry.config().meterFilter(meterFilter));
    }

    /**
     * This method will attempt to fetch the K8s Git Version from the version endpoint
     */
    private Optional<String> fetchKubernetesVersion(String host) {
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
            return Optional.ofNullable(String.valueOf(versionData.get("gitVersion")));
        } catch (Exception e) {
            log.error("Failed to fetch version data from the K8s service API", e);
            return Optional.empty();
        }
    }
}
