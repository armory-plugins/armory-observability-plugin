package io.armory.plugin.observability.registrycustomizer;

import io.armory.plugin.observability.model.PluginConfig;
import io.armory.plugin.observability.model.PluginMetricsConfig;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.config.MeterFilter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import static io.micrometer.core.instrument.config.MeterFilter.deny;

/**
 * https://micrometer.io/docs/concepts#_denyaccept_meters
 *
 * A registry customizer that will add our filters and transformers to configure / customize metrics to be
 * more efficient for metrics platforms that care about the number of unique MTS and or DPM.
 */
@Slf4j
public class MetricFiltersMeterRegistryCustomizer extends RegistryCustomizer {

    /**
     * This may be too primitive but, it can be a place to start.
     *
     * Denies metrics that contain the word "percentile" in their name.
     */
    private static final MeterFilter BLOCK_METRICS_THAT_HAVE_PERCENTILE_IN_NAME =
            deny(id -> id.getName().toLowerCase().contains("percentile"));

    public static List<MeterFilter> METER_FILTERS = List.of(
            BLOCK_METRICS_THAT_HAVE_PERCENTILE_IN_NAME
    );

    private final PluginMetricsConfig metricsConfig;

    public MetricFiltersMeterRegistryCustomizer(PluginConfig metricsConfig) {
        this.metricsConfig = metricsConfig.getMetrics();
    }

    @Override

    public void doCustomize(MeterRegistry registry) {
        if (!metricsConfig.isMeterRegistryFiltersDisabled()) {
            log.info("Adding meter filters");
            METER_FILTERS.forEach(meterFilter -> registry.config().meterFilter(meterFilter));
        }
    }
}
