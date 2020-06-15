package io.armory.plugin.observability.model;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties("spinnaker.extensibility.plugins.armory.observability-plugin.config")
public class PluginConfig {
    PluginMetricsConfig metrics = new PluginMetricsConfig();
}
