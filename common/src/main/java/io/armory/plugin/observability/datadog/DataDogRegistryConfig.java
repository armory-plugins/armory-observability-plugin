package io.armory.plugin.observability.datadog;

import io.armory.plugin.observability.model.PluginMetricsDatadogConfig;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import org.pf4j.PluginRuntimeException;

public class DataDogRegistryConfig implements io.micrometer.datadog.DatadogConfig{
    private final DataDogRegistryConfig datadogConfig;
    public DataDogRegistryConfig (PluginMetricsDatadogConfig datadogConfig){
        this.datadogConfig = datadogConfig;
    }

    @Override
    public String get(String key) {
        return null; // NOOP, source config from the PluginConfig that is injected
    }

    @Override
    public String apiKey() {
        return Optional.ofNullable(datadogConfig.apiKey())
                .orElseThrow(() ->
                        new PluginRuntimeException(
                                "The datadog API key is a required plugin config property"));
    }

    @Override
    public String applicationKey() { return datadogConfig.applicationKey(); }

    @Override
    public String uri() { return datadogConfig.uri(); }

    @Override
    public String hosTag() { return datadogConfig.hosTag(); }

    @Override
    public boolean descriptions() { return datadogConfig.descriptions(); }
}
