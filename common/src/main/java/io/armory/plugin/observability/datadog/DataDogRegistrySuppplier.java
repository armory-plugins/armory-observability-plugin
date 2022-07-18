package io.armory.plugin.observability.datadog;

import io.armory.plugin.observability.model.PluginConfig;
import io.armory.plugin.observability.model.PluginMetricsDatadogConfig;
import io.armory.plugin.observability.model.PluginMetricsNewRelicConfig;
import io.armory.plugin.observability.registry.RegistryConfigWrapper;
import io.armory.plugin.observability.service.TagsService;
import io.micrometer.core.instrument.Clock;
import io.prometheus.client.CollectorRegistry;

import java.util.function.Supplier;


public class DataDogRegistrySuppplier implements Supplier<RegistryConfigWrapper> {

    private final PluginMetricsDatadogConfig datadogConfig;
    private final CollectorRegistry collectorRegistry;
    private final Clock clock;

    public DataDogRegistrySuppplier (PluginConfig pluginConfig, CollectorRegistry collectorRegistry, Clock clock) {
        datadogConfig = pluginConfig.getMetrics().getDatadog();
        this.collectorRegistry = collectorRegistry;

    }
}
