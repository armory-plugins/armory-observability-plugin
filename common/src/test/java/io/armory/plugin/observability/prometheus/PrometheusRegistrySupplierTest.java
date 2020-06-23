package io.armory.plugin.observability.prometheus;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.MockitoAnnotations.initMocks;

import io.armory.plugin.observability.model.PluginConfig;
import io.armory.plugin.observability.model.PluginMetricsConfig;
import io.armory.plugin.observability.model.PluginMetricsPrometheusConfig;
import io.armory.plugin.observability.promethus.PrometheusRegistrySupplier;
import io.micrometer.core.instrument.Clock;
import io.prometheus.client.CollectorRegistry;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

public class PrometheusRegistrySupplierTest {

  PluginMetricsPrometheusConfig prometheusConfig;

  @Mock CollectorRegistry collectorRegistry;

  @Mock Clock clock;

  PrometheusRegistrySupplier sut;

  @Before
  public void before() {
    initMocks(this);
    var pluginConfig = new PluginConfig();
    var metricsConfig = new PluginMetricsConfig();
    pluginConfig.setMetrics(metricsConfig);
    prometheusConfig = PluginMetricsPrometheusConfig.builder().build();
    metricsConfig.setPrometheus(prometheusConfig);
    sut = new PrometheusRegistrySupplier(pluginConfig, collectorRegistry, clock);
  }

  @Test
  public void test_that_get_returns_null_if_prometheus_is_not_enabled() {
    prometheusConfig.setEnabled(false);
    assertNull(sut.get());
  }

  @Test
  public void test_that_get_returns_a_prometheus_registry_if_prometheus_is_enabled() {
    prometheusConfig.setEnabled(true);
    var actual = sut.get();
    assertNotNull(actual);
  }
}
