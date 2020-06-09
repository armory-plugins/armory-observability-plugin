package io.armory.plugin.observability;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A registry customizer that will add the default tags that enable observability best practices for Armory.
 */
@Slf4j
@Component
public class DefaultTagsMeterRegistryCustomizer implements MeterRegistryCustomizer<PrometheusMeterRegistry> {

    private final Map<String, String> defaultTags;

    public DefaultTagsMeterRegistryCustomizer(Map<String, String> defaultTags) {
        this.defaultTags = defaultTags;
    }

    @Override
    public void customize(PrometheusMeterRegistry registry) {
        List<Tag> tags = defaultTags.entrySet().stream()
                .map(tag -> {
                    log.info("Adding default tag {}: {} to registry", tag.getKey(), tag.getValue());
                    return Tag.of(tag.getKey(), tag.getValue());
                })
                .collect(Collectors.toList());
        registry.config().commonTags(tags);
    }
}
