package io.armory.plugin.observability.promethus;

import io.armory.plugin.observability.model.ArmoryObservabilityPluginProperties;
import io.armory.plugin.observability.model.PluginPrometheusConfig;
import io.micrometer.prometheus.PrometheusConfig;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

public class PrometheusConfigWrapper implements PrometheusConfig {

    private final PluginPrometheusConfig prometheusConfig;

    PrometheusConfigWrapper(ArmoryObservabilityPluginProperties pluginProperties) {
        prometheusConfig = pluginProperties.getPrometheus();
    }

    @Override
    public String get(String key) {
        return null;
    }

    @Override
    public boolean descriptions() {
        return prometheusConfig.isDescriptions();
    }

    @Override
    public Duration step() {
        return Duration.of(prometheusConfig.getStepInSeconds(), ChronoUnit.SECONDS);
    }
}
