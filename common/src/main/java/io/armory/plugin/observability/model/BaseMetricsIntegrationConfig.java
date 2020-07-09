package io.armory.plugin.observability.model;

import lombok.Data;

@Data
public abstract class BaseMetricsIntegrationConfig {
  private MeterRegistryConfig meterRegistryConfig = new MeterRegistryConfig();
}
