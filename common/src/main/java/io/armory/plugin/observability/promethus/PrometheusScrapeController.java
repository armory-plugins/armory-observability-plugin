package io.armory.plugin.observability.promethus;

import io.prometheus.client.exporter.common.TextFormat;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

/**
 * Port of PrometheusScrapeEndpoint but rather than being an actuator endpoint that won't work with plugins,
 * it will be a rest controller bean that we can register.
 *
 * See: https://github.com/spring-projects/spring-boot/blob/cd1baf18fe9ec71c11d7d131d6f1a417ec0c00e2/spring-boot-project/spring-boot-actuator/src/main/java/org/springframework/boot/actuate/metrics/export/prometheus/PrometheusScrapeEndpoint.java
 */
@Slf4j
@RestController()
@RequestMapping("/armory-observability/metrics")
public class PrometheusScrapeController {

    private final PrometheusCollectorRegistryWrapper collectorRegistry;

    public PrometheusScrapeController(PrometheusCollectorRegistryWrapper collectorRegistry) {
        this.collectorRegistry = collectorRegistry;
    }

    @GetMapping()
    public ResponseEntity<String> scrape() {
        try {
            Writer writer = new StringWriter();
            TextFormat.write004(writer, this.collectorRegistry.metricFamilySamples());
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.set("Content-Type", TextFormat.CONTENT_TYPE_004);
            return new ResponseEntity<>(writer.toString(), responseHeaders, HttpStatus.OK);
        }
        catch (IOException ex) {
            // This actually never happens since StringWriter::write() doesn't throw any
            // IOException
            throw new RuntimeException("Writing metrics failed", ex);
        }
    }
}
