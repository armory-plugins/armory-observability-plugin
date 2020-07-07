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

package io.armory.plugin.observability;

import com.netflix.spinnaker.kork.plugins.api.spring.PrivilegedSpringPlugin;
import io.armory.plugin.observability.model.PluginConfig;
import io.armory.plugin.observability.newrelic.NewRelicRegistrySupplier;
import io.armory.plugin.observability.promethus.PrometheusRegistrySupplier;
import io.armory.plugin.observability.promethus.PrometheusScrapeEndpoint;
import io.armory.plugin.observability.registry.AddDefaultTagsRegistryCustomizer;
import io.armory.plugin.observability.registry.AddFiltersRegistryCustomizer;
import io.armory.plugin.observability.registry.ArmoryObservabilityCompositeRegistry;
import io.armory.plugin.observability.service.MeterCustomizerService;
import io.armory.plugin.observability.service.MeterFilterService;
import io.armory.plugin.observability.service.MeterRegistryService;
import io.armory.plugin.observability.service.TagsService;
import io.prometheus.client.CollectorRegistry;
import lombok.extern.slf4j.Slf4j;
import org.pf4j.PluginRuntimeException;
import org.pf4j.PluginWrapper;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;

/** Main entry point into the Armory Observability Plugin. */
@Slf4j
public class ArmoryObservabilityPlugin extends PrivilegedSpringPlugin {

  public ArmoryObservabilityPlugin(PluginWrapper wrapper) {
    super(wrapper);
  }

  @Override
  public void registerBeanDefinitions(BeanDefinitionRegistry registry) {
    try {
      // Core plugin beans
      registerBean(beanDefinitionFor(PluginConfig.class), registry);
      registerBean(beanDefinitionFor(TagsService.class), registry);
      registerBean(beanDefinitionFor(AddDefaultTagsRegistryCustomizer.class), registry);
      registerBean(beanDefinitionFor(MeterFilterService.class), registry);
      registerBean(beanDefinitionFor(AddFiltersRegistryCustomizer.class), registry);
      registerBean(beanDefinitionFor(MeterCustomizerService.class), registry);
      registerBean(beanDefinitionFor(MeterRegistryService.class), registry);

      // Prometheus Beans
      registerBean(beanDefinitionFor(CollectorRegistry.class), registry);
      registerBean(beanDefinitionFor(PrometheusRegistrySupplier.class), registry);
      registerBean(beanDefinitionFor(PrometheusScrapeEndpoint.class), registry);

      // New Relic Bean
      registerBean(beanDefinitionFor(NewRelicRegistrySupplier.class), registry);

      // Composite Registry
      registerBean(primaryBeanDefinitionFor(ArmoryObservabilityCompositeRegistry.class), registry);
    } catch (Exception e) {
      throw new PluginRuntimeException("Failed to register Armory Observability Plugin beans", e);
    }
  }
}
