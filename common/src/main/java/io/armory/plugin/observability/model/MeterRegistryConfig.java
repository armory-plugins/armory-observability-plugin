package io.armory.plugin.observability.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeterRegistryConfig {
  private boolean armoryRecommendedFiltersEnabled = false;
  private boolean defaultTagsDisabled = false;

  private List<String> excludedMetricsPrefix;

}
