package io.armory.plugin.observability.service;

import io.armory.plugin.observability.registry.MeterRegistrySupplier;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.context.ApplicationContext;

public class MeterRegistryService {

  private final List<MeterRegistrySupplier> meterRegistrySuppliers;

  public MeterRegistryService(ApplicationContext appContext) {
    var regSuppliers = appContext.getBeanNamesForType(MeterRegistrySupplier.class);
    meterRegistrySuppliers =
        Arrays.stream(regSuppliers)
            .map(beanName -> appContext.getBean(beanName, MeterRegistrySupplier.class))
            .collect(Collectors.toList());
  }

  public List<MeterRegistrySupplier> getMeterRegistrySuppliers() {
    return meterRegistrySuppliers;
  }
}
