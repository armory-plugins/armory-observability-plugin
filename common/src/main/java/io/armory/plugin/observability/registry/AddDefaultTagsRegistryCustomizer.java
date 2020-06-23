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

import io.armory.plugin.observability.service.TagsService;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;

@Slf4j
public class AddDefaultTagsRegistryCustomizer implements MeterRegistryCustomizer<MeterRegistry> {

  private final TagsService tagsService;

  public AddDefaultTagsRegistryCustomizer(TagsService tagsService) {
    this.tagsService = tagsService;
  }

  @Override
  public void customize(MeterRegistry registry) {
    log.info("Adding default tags to registry: {}", registry.getClass().getSimpleName());
    registry.config().commonTags(tagsService.getDefaultTags());
  }
}
