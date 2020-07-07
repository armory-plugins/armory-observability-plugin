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

package io.armory.plugin.observability.registry;

import io.armory.plugin.observability.service.MeterCustomizerService;
import io.armory.plugin.observability.service.MeterFilterService;
import io.armory.plugin.observability.service.MeterRegistryService;
import io.armory.plugin.observability.service.TagsService;
import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.composite.CompositeMeterRegistry;
import io.micrometer.core.instrument.simple.SimpleConfig;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

/**
 * This is the registry that Micrometer/Spectator will use. It will collect all of the enabled
 * registries, if none are enabled it will default to a Simple Registry w/ default settings.
 */
@Slf4j
public class ArmoryObservabilityCompositeRegistry extends CompositeMeterRegistry {

  public ArmoryObservabilityCompositeRegistry(
      Clock clock,
      MeterRegistryService meterRegistryService,
      MeterCustomizerService meterCustomizerService,
      TagsService tagsService,
      MeterFilterService meterFilterService) {

    this(
        clock,
        ((Supplier<List<MeterRegistry>>)
                () -> {
                  List<MeterRegistry> enabledRegistries =
                      meterRegistryService.getMeterRegistrySuppliers().stream()
                          .map(MeterRegistrySupplier::get)
                          .filter(Objects::nonNull)
                          .collect(Collectors.toList());

                  // If none of the registries that this plugin provides are enabled, we will
                  // default to the simple registry and assume that Spectator with the spinnaker
                  // monitoring daemon will be used.
                  if (enabledRegistries.size() == 0) {
                    log.warn(
                        "None of the supported Armory Observability Plugin registries where enabled defaulting a Simple Meter Registry which Spectator will use.");
                    var simple = new SimpleMeterRegistry(SimpleConfig.DEFAULT, clock);
                    simple.config().commonTags(tagsService.getDefaultTags());
                    meterFilterService
                        .getMeterFilters()
                        .forEach(meterFilter -> simple.config().meterFilter(meterFilter));
                    enabledRegistries = List.of(simple);
                  }
                  return enabledRegistries;
                })
            .get());

    this.getRegistries()
        .forEach(
            meterRegistry ->
                meterCustomizerService
                    .getRegistryCustomizers()
                    .forEach(registryCustomizer -> registryCustomizer.customize(meterRegistry)));
  }

  public ArmoryObservabilityCompositeRegistry(Clock clock, Iterable<MeterRegistry> registries) {
    super(clock, registries);
  }
}
