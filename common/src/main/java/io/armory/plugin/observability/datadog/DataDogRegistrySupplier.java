package io.armory.plugin.observability.datadog;

import io.armory.plugin.observability.model.PluginConfig;
import io.armory.plugin.observability.model.PluginMetricsDatadogConfig;
import io.armory.plugin.observability.registry.RegistryConfigWrapper;
import io.micrometer.core.ipc.http.HttpUrlConnectionSender;
import io.micrometer.datadog.DatadogMeterRegistry;
import org.jetbrains.annotations.NotNull;
import java.time.Duration;
import java.util.function.Supplier;


public class DataDogRegistrySupplier implements Supplier<RegistryConfigWrapper> {

    private final PluginMetricsDatadogConfig datadogConfig;
    protected HttpUrlConnectionSender sender;

    public DataDogRegistrySupplier (@NotNull PluginConfig pluginConfig) {
        datadogConfig = pluginConfig.getMetrics().getDatadog();
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
