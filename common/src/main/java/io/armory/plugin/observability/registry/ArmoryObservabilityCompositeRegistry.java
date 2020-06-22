package io.armory.plugin.observability.registry;

import io.armory.plugin.observability.newrelic.NewRelicRegistrySupplier;
import io.armory.plugin.observability.promethus.PrometheusRegistrySupplier;
import io.armory.plugin.observability.service.MeterFilterService;
import io.armory.plugin.observability.service.TagsService;
import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.composite.CompositeMeterRegistry;
import io.micrometer.core.instrument.simple.SimpleConfig;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ArmoryObservabilityCompositeRegistry extends CompositeMeterRegistry {

  public ArmoryObservabilityCompositeRegistry(
      Clock clock,
      PrometheusRegistrySupplier prometheusRegistrySupplier,
      NewRelicRegistrySupplier newRelicRegistrySupplier,
      TagsService tagsService,
      MeterFilterService meterFilterService) {

    this(
        clock,
        ((Supplier<List<MeterRegistry>>)
                () -> {
                  List<MeterRegistry> enabledRegistries =
                      List.of(prometheusRegistrySupplier, newRelicRegistrySupplier).stream()
                          .map(Supplier::get)
                          .filter(Objects::nonNull)
                          .collect(Collectors.toList());

                  // If none of the extra registries that this plugin provides are enabled, we will
                  // default to the simple registry
                  // and assume that the spinnaker monitoring daemon will be used.
                  if (enabledRegistries.size() == 0) {
                    log.warn(
                        "None of the supported Armory Observability Plugin registries where enabled defaulting a Simple Meter Registry which Spectator will use.");
                    var simple = new SimpleMeterRegistry(SimpleConfig.DEFAULT, clock);
                    simple.config().commonTags(tagsService.getDefaultTags());
                    meterFilterService
                        .getMeterFilters()
                        .forEach(meterFilter -> simple.config().meterFilter(meterFilter));
                    enabledRegistries = List.of(simple);
                  }
                  return enabledRegistries;
                })
            .get());
  }

  public ArmoryObservabilityCompositeRegistry(Clock clock, Iterable<MeterRegistry> registries) {
    super(clock, registries);
  }
}
