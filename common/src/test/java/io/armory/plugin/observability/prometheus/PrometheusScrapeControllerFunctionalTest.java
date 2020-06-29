/*
 * Copyright 2020 Armory, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.armory.plugin.observability.prometheus;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import io.armory.plugin.observability.promethus.PrometheusScrapeController;
import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Timer;
import io.micrometer.prometheus.MutatedPrometheusMeterRegistry;
import io.micrometer.prometheus.PrometheusConfig;
import io.prometheus.client.CollectorRegistry;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

/**
 * These tests are more functional than unit, because I don't really have the time to reverse
 * engineer the Prometheus registry to mock everything properly.
 */
public class PrometheusScrapeControllerFunctionalTest {

  CollectorRegistry collectorRegistry;
  MutatedPrometheusMeterRegistry registry;

  @Mock Clock clock;

  PrometheusScrapeController sut;

  @Before
  public void before() {
    initMocks(this);
    collectorRegistry = new CollectorRegistry();
    sut = new PrometheusScrapeController(collectorRegistry);

    registry =
        new MutatedPrometheusMeterRegistry(PrometheusConfig.DEFAULT, collectorRegistry, clock);
    var now = Instant.parse("2020-05-26T00:00:00.000Z");
    var later = now.plusSeconds(2);
    when(clock.monotonicTime()).thenReturn(Duration.ofMillis(now.toEpochMilli()).toNanos());
    var sample = Timer.start(registry);
    when(clock.monotonicTime()).thenReturn(Duration.ofMillis(later.toEpochMilli()).toNanos());
    sample.stop(
        registry.timer("http.request", List.of(Tag.of("response", "200"), Tag.of("uri", "/foo"))));
  }

  @Test
  public void test_that_scrape_returns_the_expected_serialized_response() throws Exception {
    var expectedScrapeResource =
        this.getClass()
            .getClassLoader()
            .getResource("io/armory/plugin/observability/prometheus/expected-scrape.txt");
    var expectedContent = Files.readString(Path.of(expectedScrapeResource.toURI()));

    var responseEntity = sut.scrape(null);
    assertTrue(responseEntity.getStatusCode().is2xxSuccessful());
    //noinspection ConstantConditions
    assertEquals(
        "text/plain;version=0.0.4;charset=utf-8",
        responseEntity.getHeaders().getContentType().toString());
    assertEquals(expectedContent, responseEntity.getBody());
  }

  @Test
  public void test_that_the_prometheus_registry_can_handle_tags_that_are_sometimes_absent()
      throws Exception {
    var expectedScrapeResource =
        this.getClass()
            .getClassLoader()
            .getResource(
                "io/armory/plugin/observability/prometheus/expected-scrape-with-2-counters-different-tags.txt");

    var expectedContent = Files.readString(Path.of(expectedScrapeResource.toURI()));

    var tagsWithMissingTag = List.of(Tag.of("hostname", "localhost"));

    var fullCollectionOfTags =
        List.of(Tag.of("hostname", "localhost"), Tag.of("optionalExtraMetadata", "my-cool-value"));

    registry.counter("foo", tagsWithMissingTag).increment();
    registry.counter("foo", fullCollectionOfTags).increment();
    var responseEntity = sut.scrape(null);
    assertTrue(responseEntity.getStatusCode().is2xxSuccessful());
    //noinspection ConstantConditions
    assertEquals(
        "text/plain;version=0.0.4;charset=utf-8",
        responseEntity.getHeaders().getContentType().toString());
    // use length since, order is non-deterministic
    assertEquals(expectedContent.length(), responseEntity.getBody().length());
  }
}
