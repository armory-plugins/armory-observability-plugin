/*
 * Copyright 2020 Armory, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.armory.plugin.observability.service;

import static io.armory.plugin.observability.filters.ArmoryRecommendedFilters.ARMORY_RECOMMENDED_FILTERS;

import io.armory.plugin.observability.model.PluginConfig;
import io.armory.plugin.observability.model.PluginMetricsConfig;
import io.micrometer.core.instrument.config.MeterFilter;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 * https://micrometer.io/docs/concepts#_denyaccept_meters
 *
 * <p>A service that will provide our filters and transformers to configure / customize metrics to
 * be more efficient for metrics platforms that care about the number of unique MTS and or DPM.
 */
@Slf4j
public class MeterFilterService {

  private final PluginMetricsConfig metricsConfig;

  public MeterFilterService(PluginConfig pluginConfig) {
    metricsConfig = pluginConfig.getMetrics();
  }

  /**
   * TODO, pattern for impl TBD.
   *
   * @return The list of enabled filters.
   */
  public List<MeterFilter> getMeterFilters() {
    if (metricsConfig.isArmoryRecommendedFiltersEnabled()) {
      return ARMORY_RECOMMENDED_FILTERS;
    }
    return List.of();
  }
}
