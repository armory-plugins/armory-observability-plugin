package io.armory.plugin.observability.promethus;

import io.prometheus.client.CollectorRegistry;

/** Wrapper class so that is can be created in plugin and constructor injected. */
public class PrometheusCollectorRegistry extends CollectorRegistry {

  public PrometheusCollectorRegistry() {
    super(true);
  }
}
