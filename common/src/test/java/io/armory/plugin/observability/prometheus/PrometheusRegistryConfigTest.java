package io.armory.plugin.observability.prometheus;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import io.armory.plugin.observability.model.PluginMetricsPrometheusConfig;
import io.armory.plugin.observability.promethus.PrometheusRegistryConfig;
import java.time.Duration;
import org.junit.Before;
import org.junit.Test;

public class PrometheusRegistryConfigTest {

  PluginMetricsPrometheusConfig prometheusConfig;

  PrometheusRegistryConfig sut;

  @Before
  public void before() {
    prometheusConfig = new PluginMetricsPrometheusConfig();
    sut = new PrometheusRegistryConfig(prometheusConfig);
  }

  @Test
  public void test_that_PrometheusRegistryConfig_proxies_PluginMetricsPrometheusConfig() {
    var step = Duration.ofSeconds(30);
    prometheusConfig.setDescriptions(true);
    prometheusConfig.setStepInSeconds((int) step.toSeconds());
    assertEquals(step, sut.step());
    assertEquals(prometheusConfig.isDescriptions(), sut.descriptions());
    assertNull(sut.get("foo"));
  }
}
