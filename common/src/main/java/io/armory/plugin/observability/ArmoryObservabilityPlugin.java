package io.armory.plugin.observability;

import com.netflix.spinnaker.kork.plugins.api.spring.PrivilegedSpringPlugin;
import io.armory.plugin.observability.model.PluginConfig;
import io.armory.plugin.observability.promethus.PrometheusMeterRegistryWrapper;
import io.armory.plugin.observability.promethus.PrometheusScrapeEndpoint;
import io.armory.plugin.observability.registrycustomizer.DefaultTagsMeterRegistryCustomizer;
import io.armory.plugin.observability.registrycustomizer.MetricFiltersMeterRegistryCustomizer;
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
      registerBean(primaryBeanDefinitionFor(PrometheusMeterRegistryWrapper.class), registry);
      registerBean(beanDefinitionFor(PrometheusScrapeEndpoint.class), registry);
      registerBean(beanDefinitionFor(PluginConfig.class), registry);
      registerBean(beanDefinitionFor(DefaultTagsMeterRegistryCustomizer.class), registry);
      registerBean(beanDefinitionFor(MetricFiltersMeterRegistryCustomizer.class), registry);
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
