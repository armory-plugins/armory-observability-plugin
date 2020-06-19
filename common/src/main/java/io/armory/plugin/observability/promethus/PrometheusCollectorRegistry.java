package io.armory.plugin.observability.promethus;

import io.prometheus.client.CollectorRegistry;
import org.springframework.stereotype.Component;

/** Wrapper class so that is can be created in plugin and constructor injected. */
@Component
public class PrometheusCollectorRegistry extends CollectorRegistry {

  public PrometheusCollectorRegistry() {
    super(true);
  }
}
