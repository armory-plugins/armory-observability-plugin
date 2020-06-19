package io.armory.plugin.observability;

import io.armory.plugin.observability.model.PluginConfig;
import io.armory.plugin.observability.promethus.PrometheusCollectorRegistry;
import io.armory.plugin.observability.promethus.PrometheusRegistry;
import io.armory.plugin.observability.promethus.PrometheusScrapeController;
import io.armory.plugin.observability.service.MeterFilterService;
import io.armory.plugin.observability.service.TagsService;
import io.micrometer.core.instrument.Clock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.autoconfigure.metrics.MetricsAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Slf4j
@Configuration
@EnableConfigurationProperties
@Import({
  PluginConfig.class,
  TagsService.class,
  MeterFilterService.class,
  PrometheusCollectorRegistry.class,
  PrometheusRegistry.class,
  PrometheusScrapeController.class
})
@AutoConfigureBefore(MetricsAutoConfiguration.class)
public class PluginConfiguration {

  @Bean
  @ConditionalOnMissingBean
  public Clock micrometerClock() {
    return Clock.SYSTEM;
  }
}
