package io.armory.plugin.observability.promethus;

import com.sun.net.httpserver.HttpServer;
import io.armory.plugin.observability.model.ArmoryObservabilityPluginProperties;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * https://micrometer.io/docs/registry/prometheus#_configuring
 */
@Slf4j
public class PrometheusScrapeEndpoint {

    public PrometheusScrapeEndpoint(ArmoryObservabilityPluginProperties pluginProperties,
                                    PrometheusMeterRegistryWrapper registry) {

        var prometheusConfig = pluginProperties.getPrometheus();
        var port = prometheusConfig.getScrapePort();
        var path = prometheusConfig.getPath();

        ExecutorService executorService = Executors.newSingleThreadExecutor();

        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
            server.createContext(path, httpExchange -> {
                String response = registry.scrape();
                httpExchange.sendResponseHeaders(200, response.getBytes().length);
                try (OutputStream os = httpExchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
            });
            executorService.submit(server::start);
            log.info("Prometheus Scrape Endpoint on port listing on port: {}, with path: {}", port, path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
