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
  private boolean whiteListedMetrics = false;
  private boolean defaultTagsDisabled = false;
  //TOOD: Move to a whitelisted string instead of hardcoded list
  //private List<String> filtersToAllow = new ArrayList<String>();
}
