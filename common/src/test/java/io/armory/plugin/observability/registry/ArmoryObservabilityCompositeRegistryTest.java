package io.armory.plugin.observability.registry;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

import io.armory.plugin.observability.service.MeterFilterService;
import io.armory.plugin.observability.service.TagsService;
import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.logging.LoggingMeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;

public class ArmoryObservabilityCompositeRegistryTest {

  @Mock TagsService tagsService;

  @Mock MeterFilterService meterFilterService;

  @Before
  public void before() {
    initMocks(this);
  }

  @Test
  public void test_that_ArmoryObservabilityCompositeRegistry_uses_supplied_registries() {
    var registry = new LoggingMeterRegistry();
    var sut =
        new ArmoryObservabilityCompositeRegistry(
            Clock.SYSTEM, List.of(() -> registry), List.of(), tagsService, meterFilterService);

    assertEquals(1, sut.getRegistries().size());
    assertEquals(registry, sut.getRegistries().toArray()[0]);
  }

  @Test
  public void
      test_that_ArmoryObservabilityCompositeRegistry_uses_a_simple_registry_when_no_registries_are_enabled() {
    var sut =
        new ArmoryObservabilityCompositeRegistry(
            Clock.SYSTEM, List.of(() -> null), List.of(), tagsService, meterFilterService);
    assertEquals(1, sut.getRegistries().size());
    assertEquals(SimpleMeterRegistry.class, sut.getRegistries().toArray()[0].getClass());
  }

  @Test
  public void
      test_that_ArmoryObservabilityCompositeRegistry_calls_the_registry_customizers_for_each_enabled_registry() {
    var customizer = mock(MeterRegistryCustomizer.class);
    var registry = new LoggingMeterRegistry();
    var registry2 = new SimpleMeterRegistry();
    new ArmoryObservabilityCompositeRegistry(
        Clock.SYSTEM,
        List.of(() -> registry, () -> registry2),
        List.of(customizer),
        tagsService,
        meterFilterService);

    verify(customizer, times(2)).customize(any());
  }
}
