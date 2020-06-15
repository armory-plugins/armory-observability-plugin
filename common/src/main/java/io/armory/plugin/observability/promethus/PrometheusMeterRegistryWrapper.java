package io.armory.plugin.observability.promethus;

import io.armory.plugin.observability.model.PluginConfig;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;

public class PrometheusMeterRegistryWrapper extends PrometheusMeterRegistry {

    public PrometheusMeterRegistryWrapper(PluginConfig pluginProperties) {
        this(new PrometheusConfigWrapper(pluginProperties), pluginProperties);
    }

    public PrometheusMeterRegistryWrapper(PrometheusConfig config, PluginConfig pluginProperties) {
        super(config);
    }
}
