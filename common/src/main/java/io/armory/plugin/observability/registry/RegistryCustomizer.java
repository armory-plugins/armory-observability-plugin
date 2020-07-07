package io.armory.plugin.observability.registry;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;

public abstract class RegistryCustomizer implements MeterRegistryCustomizer<MeterRegistry> {}
