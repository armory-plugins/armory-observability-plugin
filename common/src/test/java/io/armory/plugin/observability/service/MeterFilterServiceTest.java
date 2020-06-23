package io.armory.plugin.observability.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import io.armory.plugin.observability.model.PluginConfig;
import org.junit.Before;
import org.junit.Test;

public class MeterFilterServiceTest {

  MeterFilterService sut;

  @Before
  public void before() {
    sut = new MeterFilterService(new PluginConfig());
  }

  @Test
  public void test_that_getMeterFilters_returns_emtpy_list_because_it_is_not_implemented() {
    var filters = sut.getMeterFilters();
    assertNotNull(filters);
    assertEquals(0, filters.size());
  }
}
