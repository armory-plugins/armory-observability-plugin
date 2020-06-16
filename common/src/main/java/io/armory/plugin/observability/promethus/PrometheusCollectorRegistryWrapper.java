package io.armory.plugin.observability.promethus;

import io.prometheus.client.CollectorRegistry;

/** Wrapper class so that is can be created in plugin and constructor injected. */
public class PrometheusCollectorRegistryWrapper extends CollectorRegistry {

  public PrometheusCollectorRegistryWrapper() {
    super(true);
  }
}
