package io.armory.plugin.observability.datadog;

import io.armory.plugin.observability.model.PluginMetricsDatadogConfig;
import org.pf4j.PluginRuntimeException;

import java.util.Optional;

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

    @Override public boolean descriptions() { return false; }

    @Override public String hostTag() {return "hostTag"; }
    
    @Override
    public Duration step() {
        return Duration.of(datadogConfig.getStepInSeconds(), ChronoUnit.SECONDS);
    }
}
