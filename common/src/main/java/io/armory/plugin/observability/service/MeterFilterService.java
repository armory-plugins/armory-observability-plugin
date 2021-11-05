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

import io.armory.plugin.observability.filters.Filters;
import io.armory.plugin.observability.model.MeterRegistryConfig;
import io.micrometer.core.instrument.config.MeterFilter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import static io.armory.plugin.observability.filters.Filters.DENY_CONTROLLER_INVOCATIONS_METRICS;

/**
 * https://micrometer.io/docs/concepts#_denyaccept_meters
 *
 * <p>A service that will provide our filters and transformers to configure / customize metrics to
 * be more efficient for metrics platforms that care about the number of unique MTS and or DPM.
 */
@Slf4j
public class MeterFilterService {

  /**
   * TODO, pattern for impl TBD.
   *
   * @return The list of enabled filters.
   * @param meterRegistryConfig
   */
  public List<MeterFilter> getMeterFilters(MeterRegistryConfig meterRegistryConfig) {
    if (meterRegistryConfig.isArmoryRecommendedFiltersEnabled()) {
      log.info("Armory Recommended filters are enabled returning those");
      return List.of(DENY_CONTROLLER_INVOCATIONS_METRICS);
    }
    if (meterRegistryConfig.isWhiteListedMetrics()) {
      log.info("Curated set of whitelist filters is enabled... rejecting all others");
      return List.of(Filters.WHITELIST_ONLY_FILTER);
    }
    return List.of();
  }
}
