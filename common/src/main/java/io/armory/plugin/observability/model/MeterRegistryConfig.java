package io.armory.plugin.observability.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeterRegistryConfig {
  private boolean armoryRecommendedFiltersEnabled = false;
  private boolean defaultTagsDisabled = false;
}
