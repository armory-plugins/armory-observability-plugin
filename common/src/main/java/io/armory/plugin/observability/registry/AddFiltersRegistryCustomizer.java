package io.armory.plugin.observability.registry;

import io.armory.plugin.observability.service.MeterFilterService;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;

@Slf4j
public class AddFiltersRegistryCustomizer implements MeterRegistryCustomizer<MeterRegistry> {

  private final MeterFilterService meterFilterService;

  public AddFiltersRegistryCustomizer(MeterFilterService meterFilterService) {
    this.meterFilterService = meterFilterService;
  }

  @Override
  public void customize(MeterRegistry registry) {
    log.info("Adding Meter Filters to registry: {}", registry.getClass().getSimpleName());
    meterFilterService
        .getMeterFilters()
        .forEach(meterFilter -> registry.config().meterFilter(meterFilter));
  }
}
