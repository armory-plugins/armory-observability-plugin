package io.armory.plugin.observability;

import io.prometheus.client.CollectorRegistry;
import org.springframework.boot.actuate.metrics.export.prometheus.PrometheusScrapeEndpoint;

public class PrometheusScrapeEndpointImpl extends PrometheusScrapeEndpoint {

    public PrometheusScrapeEndpointImpl() {
        super(new CollectorRegistry());
    }
}
