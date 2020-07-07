package io.armory.plugin.observability.registry;

import io.micrometer.core.instrument.MeterRegistry;

public interface MeterRegistrySupplier {
  MeterRegistry get();
}
