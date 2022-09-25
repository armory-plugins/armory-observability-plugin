package io.armory.plugin.observability.datadog;

import io.armory.plugin.observability.model.PluginConfig;
import io.armory.plugin.observability.model.PluginMetricsDatadogConfig;
import io.armory.plugin.observability.registry.RegistryConfigWrapper;
import io.micrometer.core.instrument.Clock;
import io.micrometer.core.ipc.http.HttpUrlConnectionSender;
import io.micrometer.datadog.DatadogMeterRegistry;
import org.jetbrains.annotations.NotNull;
import io.micrometer.core.ipc.http.HttpSender;

import java.time.Duration;
import java.util.function.Supplier;


public class DataDogRegistrySupplier implements Supplier<RegistryConfigWrapper> {

    private final PluginMetricsDatadogConfig datadogConfig;
    //private final DatadogMeterRegistry collectorRegistry;
    protected HttpUrlConnectionSender sender;
    private final Clock clock;

    public DataDogRegistrySupplier (@NotNull PluginConfig pluginConfig, Clock clock) {
        datadogConfig = pluginConfig.getMetrics().getDatadog();
        this.clock = clock;
        this.sender =  new HttpUrlConnectionSender(Duration.ofSeconds(datadogConfig.getConnectDurationSeconds()),Duration.ofSeconds(datadogConfig.getReadDurationSeconds()));
    }

    @Override
    public RegistryConfigWrapper get() {
      if (!datadogConfig.isEnabled()) {
        return null;
      }
      var config = new DataDogRegistryConfig(datadogConfig);
      var registry = DatadogMeterRegistry.builder(config).httpClient(sender).build();

        return RegistryConfigWrapper.builder()
                .meterRegistry(registry)
                .meterRegistryConfig(datadogConfig.getMeterRegistryConfig())
                .build();
    }
}
