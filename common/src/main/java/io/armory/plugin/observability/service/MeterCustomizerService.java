package io.armory.plugin.observability.service;

import io.armory.plugin.observability.registry.RegistryCustomizer;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.context.ApplicationContext;

public class MeterCustomizerService {

  private final List<RegistryCustomizer> registryCustomizers;

  public MeterCustomizerService(ApplicationContext appContext) {
    var meterRegistryCustomizerBeans = appContext.getBeanNamesForType(RegistryCustomizer.class);
    registryCustomizers =
        Arrays.stream(meterRegistryCustomizerBeans)
            .map(beanName -> appContext.getBean(beanName, RegistryCustomizer.class))
            .collect(Collectors.toList());
  }

  public List<RegistryCustomizer> getRegistryCustomizers() {
    return registryCustomizers;
  }
}
