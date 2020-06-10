package io.armory.plugin.observability;

import com.sun.net.httpserver.HttpServer;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import lombok.extern.slf4j.Slf4j;
import com.netflix.spinnaker.kork.plugins.api.spring.PrivilegedSpringPlugin;
import org.pf4j.PluginWrapper;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class ArmoryObservabilityPlugin extends PrivilegedSpringPlugin {

    private static final int PROMETHEUS_PORT = 8484;
    private static final String PROMETHEUS_PATH = "/prometheus";

    private final PrometheusMeterRegistry prometheusMeterRegistry;
    private final ExecutorService executorService;

    public ArmoryObservabilityPlugin(PluginWrapper wrapper) {
        super(wrapper);
        this.prometheusMeterRegistry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
        this.executorService = Executors.newSingleThreadExecutor();
    }

    @Override
    public void registerBeanDefinitions(BeanDefinitionRegistry registry) {
        try {
            registry.registerBeanDefinition("prometheusRegistry",
                    BeanDefinitionBuilder.genericBeanDefinition(MeterRegistry.class, this::prometheusRegistry)
                            .setPrimary(true)
                            .getBeanDefinition()
            );

            registerBean(beanDefinitionFor(ArmoryObservabilityPluginProperties.class), registry);
            registerBean(beanDefinitionFor(DefaultTagsMeterRegistryCustomizer.class), registry);
            registerBean(beanDefinitionFor(MetricFiltersMeterRegistryCustomizer.class), registry);
        } catch (Exception e) {
            log.error("Failed to register Armory Metrics Plugin beans", e);
        }
    }

    @Override
    public void start() {
        startPrometheusScrapeEndpoint();
        log.info("The Armory Metrics Plugin has started");
    }

    @Override
    public void stop() {
        executorService.shutdownNow();
        log.info("The Armory Metrics Plugin has stopped");
    }

    private MeterRegistry prometheusRegistry() {
        return this.prometheusMeterRegistry;
    }

    /**
     * TODO figure out how to wire up actuator endpoint
     * https://micrometer.io/docs/registry/prometheus#_configuring
     */
    private void startPrometheusScrapeEndpoint() {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(PROMETHEUS_PORT), 0);
            server.createContext(PROMETHEUS_PATH, httpExchange -> {
                String response = this.prometheusMeterRegistry.scrape();
                httpExchange.sendResponseHeaders(200, response.getBytes().length);
                try (OutputStream os = httpExchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
            });
            executorService.submit(server::start);
            log.info("Prometheus Scrape Endpoint on port listing on port: {}, with path: {}", PROMETHEUS_PORT, PROMETHEUS_PATH);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
