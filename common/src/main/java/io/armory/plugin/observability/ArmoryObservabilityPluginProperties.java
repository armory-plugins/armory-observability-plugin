package io.armory.plugin.observability;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties("spinnaker.extensibility.plugins.armory.observability-plugin.config")
public class ArmoryObservabilityPluginProperties {

    String customerEnvId;
    String customerEnvName;
    String customerName;

    boolean meterRegistryFiltersDisabled;
    boolean defaultTagsDisabled;
}
