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
  @Builder.Default private boolean armoryRecommendedFiltersEnabled = false;
  @Builder.Default private boolean defaultTagsDisabled = false;
}
