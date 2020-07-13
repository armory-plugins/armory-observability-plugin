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

package io.armory.plugin.observability.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PluginMetricsNewRelicConfig extends BaseMetricsIntegrationConfig {
  // New Relic Specific Settings
  @Builder.Default private boolean enabled = false;
  @Builder.Default private String apiKey = null;
  @Builder.Default private String uri = "https://metric-api.newrelic.com/";
  @Builder.Default private boolean enableAuditMode = false;

  // Push Registry Settings
  @Builder.Default private int stepInSeconds = 30;
  @Builder.Default private int numThreads = 2;
  @Builder.Default private int batchSize = 10000;
}
