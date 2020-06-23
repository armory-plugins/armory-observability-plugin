package io.armory.plugin.observability.registry;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import io.armory.plugin.observability.service.TagsService;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

public class AddDefaultTagsRegistryCustomizerTest {

  @Mock TagsService tagsService;

  @Mock MeterRegistry registry;

  @Mock MeterRegistry.Config config;

  AddDefaultTagsRegistryCustomizer sut;

  @Before
  public void before() {
    initMocks(this);
    sut = new AddDefaultTagsRegistryCustomizer(tagsService);
    when(registry.config()).thenReturn(config);
  }

  @Test
  public void test_that_customize_adds_the_tags_to_the_registry_common_tag_config() {
    var tags = List.of(Tag.of("FOO", "BAR"));
    when(tagsService.getDefaultTags()).thenReturn(tags);
    sut.customize(registry);
    verify(config, times(1)).commonTags(tags);
  }
}
