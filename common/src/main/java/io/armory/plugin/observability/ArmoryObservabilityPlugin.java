package io.armory.plugin.observability;

import com.netflix.spinnaker.kork.plugins.api.spring.PrivilegedSpringPlugin;
import io.armory.plugin.observability.model.PluginConfig;
import io.armory.plugin.observability.newrelic.NewRelicRegistrySupplier;
import io.armory.plugin.observability.promethus.PrometheusRegistrySupplier;
import io.armory.plugin.observability.promethus.PrometheusScrapeController;
import io.armory.plugin.observability.registry.AddDefaultTagsRegistryCustomizer;
import io.armory.plugin.observability.registry.AddFiltersRegistryCustomizer;
import io.armory.plugin.observability.registry.ArmoryObservabilityCompositeRegistry;
import io.armory.plugin.observability.service.MeterFilterService;
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

      // Prometheus Beans
      registerBean(beanDefinitionFor(CollectorRegistry.class), registry);
      registerBean(beanDefinitionFor(PrometheusRegistrySupplier.class), registry);
      registerBean(beanDefinitionFor(PrometheusScrapeController.class), registry);

      // New Relic Bean
      registerBean(beanDefinitionFor(NewRelicRegistrySupplier.class), registry);

      // Composite Registry
      registerBean(primaryBeanDefinitionFor(ArmoryObservabilityCompositeRegistry.class), registry);
    } catch (Exception e) {
      throw new PluginRuntimeException("Failed to register Armory Observability Plugin beans", e);
    }
  }
}
