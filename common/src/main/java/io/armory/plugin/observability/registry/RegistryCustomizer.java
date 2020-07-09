package io.armory.plugin.observability.registry;

import io.armory.plugin.observability.model.MeterRegistryConfig;
import io.micrometer.core.instrument.MeterRegistry;

@FunctionalInterface
public interface RegistryCustomizer {

  /**
   * Customize the given {@code registry}.
   *
   * @param registry the registry to customize
   * @param meterRegistryConfig the filter/tag config for the registry/integration being customized.
   */
  void customize(MeterRegistry registry, MeterRegistryConfig meterRegistryConfig);
}
