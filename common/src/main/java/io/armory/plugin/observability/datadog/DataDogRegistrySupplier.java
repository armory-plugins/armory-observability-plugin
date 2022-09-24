package io.armory.plugin.observability.datadog;

import io.armory.plugin.observability.model.PluginConfig;
import io.armory.plugin.observability.model.PluginMetricsDatadogConfig;
import io.armory.plugin.observability.registry.RegistryConfigWrapper;
import io.micrometer.core.instrument.Clock;
import io.micrometer.datadog.DatadogMeterRegistry;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;


public class DataDogRegistrySupplier implements Supplier<RegistryConfigWrapper> {

    private final PluginMetricsDatadogConfig datadogConfig;
    private final DatadogMeterRegistry collectorRegistry;
    private final Clock clock;

    public DataDogRegistrySupplier (@NotNull PluginConfig pluginConfig, DatadogMeterRegistry collectorRegistry, Clock clock) {
        datadogConfig = pluginConfig.getMetrics().getDatadog();
        this.collectorRegistry = collectorRegistry;
        this.clock = clock;
    }

    @Override
    public RegistryConfigWrapper get() {
      if (!datadogConfig.isEnabled()) {
        return null;
      }
      var config = new DataDogRegistryConfig(datadogConfig);
      var registry = DatadogMeterRegistry.builder(config).build();
      registry.start();
        return RegistryConfigWrapper.builder()
                .meterRegistry(registry)
                .build();
    }
}
