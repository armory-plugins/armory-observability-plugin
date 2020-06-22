package io.armory.plugin.observability.registry;

import io.armory.plugin.observability.service.TagsService;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;

@Slf4j
public class AddDefaultTagsRegistryCustomizer implements MeterRegistryCustomizer<MeterRegistry> {

  private final TagsService tagsService;

  public AddDefaultTagsRegistryCustomizer(TagsService tagsService) {
    this.tagsService = tagsService;
  }

  @Override
  public void customize(MeterRegistry registry) {
    log.info("Adding default tags to registry: {}", registry.getClass().getSimpleName());
    registry.config().commonTags(tagsService.getDefaultTags());
  }
}
