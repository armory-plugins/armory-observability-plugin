package io.armory.plugin.observability;

import com.netflix.spinnaker.kork.plugins.api.spring.PrivilegedSpringPlugin;
import io.armory.plugin.observability.model.PluginConfig;
import io.armory.plugin.observability.promethus.PrometheusCollectorRegistry;
import io.armory.plugin.observability.promethus.PrometheusRegistry;
import io.armory.plugin.observability.promethus.PrometheusScrapeController;
import io.armory.plugin.observability.service.MeterFilterService;
import io.armory.plugin.observability.service.TagsService;
import lombok.extern.slf4j.Slf4j;
import org.pf4j.PluginWrapper;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;

@Slf4j
public class ArmoryObservabilityPlugin extends PrivilegedSpringPlugin {

  public ArmoryObservabilityPlugin(PluginWrapper wrapper) {
    super(wrapper);
  }

  @Override
  public void registerBeanDefinitions(BeanDefinitionRegistry registry) {
    try {
      registerBean(beanDefinitionFor(PluginConfig.class), registry);
      registerBean(beanDefinitionFor(TagsService.class), registry);
      registerBean(beanDefinitionFor(MeterFilterService.class), registry);
      registerBean(beanDefinitionFor(PrometheusCollectorRegistry.class), registry);
      registerBean(primaryBeanDefinitionFor(PrometheusRegistry.class), registry);
      registerBean(beanDefinitionFor(PrometheusScrapeController.class), registry);
    } catch (Exception e) {
      log.error("Failed to register Armory Metrics Plugin beans", e);
    }
  }

  @Override
  public void start() {
    log.info("The Armory Metrics Plugin has started");
  }

  @Override
  public void stop() {
    log.info("The Armory Metrics Plugin has stopped");
  }
}
