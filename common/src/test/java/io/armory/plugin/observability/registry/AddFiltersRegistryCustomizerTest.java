package io.armory.plugin.observability.registry;

import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

import io.armory.plugin.observability.service.MeterFilterService;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.config.MeterFilter;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

public class AddFiltersRegistryCustomizerTest {

  @Mock MeterFilterService meterFilterService;

  @Mock MeterRegistry registry;

  @Mock MeterRegistry.Config config;

  AddFiltersRegistryCustomizer sut;

  @Before
  public void before() {
    initMocks(this);
    sut = new AddFiltersRegistryCustomizer(meterFilterService);
    when(registry.config()).thenReturn(config);
  }

  @Test
  public void test_that_customize_adds_the_enabled_filters_to_the_registry() throws Exception {
    var denyAllFilter = MeterFilter.deny(id -> true);
    when(meterFilterService.getMeterFilters()).thenReturn(List.of(denyAllFilter));
    sut.customize(registry);
    verify(config, times(1)).meterFilter(denyAllFilter);
  }
}
