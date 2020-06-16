package io.armory.plugin.observability.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ArmoryEnvironmentMetadata {
  private final String pluginVersion;
  private final String applicationName;
  private final String armoryAppVersion;
  private final String ossAppVersion;
  private final String spinnakerRelease;
}
