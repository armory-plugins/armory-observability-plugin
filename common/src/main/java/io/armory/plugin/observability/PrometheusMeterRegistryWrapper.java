package io.armory.plugin.observability;

import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;

public class PrometheusMeterRegistryWrapper extends PrometheusMeterRegistry {

    public PrometheusMeterRegistryWrapper(ArmoryObservabilityPluginProperties pluginProperties) {
        this(new PrometheusConfigWrapper(pluginProperties), pluginProperties);
    }

    public PrometheusMeterRegistryWrapper(PrometheusConfig config, ArmoryObservabilityPluginProperties pluginProperties) {
        super(config);
    }
}
