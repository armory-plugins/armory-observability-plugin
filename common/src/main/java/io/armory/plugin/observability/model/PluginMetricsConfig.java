package io.armory.plugin.observability.model;

import lombok.Data;

import java.util.Map;

@Data
public class PluginMetricsConfig {
    private Map<String, String> additionalTags = Map.of();

    private PluginMetricsPrometheusConfig prometheus = new PluginMetricsPrometheusConfig();

    private boolean meterRegistryFiltersDisabled;
    private boolean defaultTagsDisabled;
}
