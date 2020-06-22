package io.armory.plugin.observability.registry;

import io.micrometer.core.instrument.MeterRegistry;
import java.util.function.Supplier;

/**
 * Interface wrapping Supplier<MeterRegistry> so that we can inject collections of the
 * implementations via Spring.
 */
public interface RegistrySupplier extends Supplier<MeterRegistry> {}
