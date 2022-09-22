package io.armory.plugin.observability.datadog;

import io.armory.plugin.observability.model.PluginMetricsDatadogConfig;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import org.pf4j.PluginRuntimeException;

public class DataDogRegistryConfig implements io.micrometer.datadog.DatadogConfig{
    private final PluginMetricsDatadogConfig datadogConfig;
    public DataDogRegistryConfig (PluginMetricsDatadogConfig datadogConfig){
        this.datadogConfig = datadogConfig;
    }

    @Override
    public String get(String key) {
        return null; // NOOP, source config from the PluginConfig that is injected
    }

    @Override
    public String apiKey() {
        return Optional.ofNullable(datadogConfig.getApiKey())
                .orElseThrow(() ->
                        new PluginRuntimeException(
                                "The datadog API key is a required plugin config property"));
    }

    @Override
    public String applicationKey() { return datadogConfig.getApplicationKey(); }

    @Override
    public String uri() { return datadogConfig.getUri(); }

}
