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

package io.armory.plugin.observability.service;

import static io.armory.plugin.observability.service.TagsService.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import com.netflix.spinnaker.kork.version.VersionResolver;
import io.armory.plugin.observability.model.ArmoryEnvironmentMetadata;
import io.armory.plugin.observability.model.PluginConfig;
import io.micrometer.core.instrument.Tag;
import java.util.Map;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.boot.info.BuildProperties;

public class TagsServiceTest {

  private static final String MOCK_APPLICATION_NAME = "mock-application-name";
  private static final String MOCK_PLUGIN_VER = "1.0.0";
  private TagsService sut;

  @Mock private VersionResolver versionResolver;

  PluginConfig pluginConfig;

  @Before
  public void before() {
    initMocks(this);
    pluginConfig = new PluginConfig();
    sut = new TagsService(pluginConfig, versionResolver, MOCK_APPLICATION_NAME);
  }

  @Test
  public void test_that_getBuildProperties_loads_the_props_file_properly() {
    BuildProperties props = sut.getBuildProperties("build-info.properties");
    assertEquals(props.getName(), "clouddriver");
    assertEquals(props.getVersion(), "2.21.0");
    assertEquals(props.get("armoryVersion"), "2.21.0");
    assertEquals(props.get("spinnakerRelease"), "1.20.5");
    assertEquals(props.get("ossVersion"), "6.9.2-20200606020017");
  }

  @Test
  public void
      test_that_getBuildProperties_loads_the_props_file_properly_when_extra_props_are_preset() {
    BuildProperties props = sut.getBuildProperties("build-info-with-extra.properties");
    assertEquals(props.getName(), "clouddriver");
    assertEquals(props.getVersion(), "2.21.0");
    assertEquals(props.get("armoryVersion"), "2.21.0");
    assertEquals(props.get("spinnakerRelease"), "1.20.5");
    assertEquals(props.get("ossVersion"), "6.9.2-20200606020017");
  }

  @Test
  public void test_that_getBuildProperties_doesnt_error_when_build_info_file_missing() {
    BuildProperties props = sut.getBuildProperties("i-do-not-exist.properties");
    assertNotNull(props);
    assertNull(props.getName());
    assertNull(props.getVersion());
    assertNull(props.get("armoryVersion"));
    assertNull(props.get("spinnakerRelease"));
    assertNull(props.get("ossVersion"));
  }

  @Test
  public void test_that_getDefaultTagsAsFilteredMap_filters_null_and_empty_values() {
    var res =
        sut.getDefaultTagsAsFilteredMap(
            ArmoryEnvironmentMetadata.builder()
                .applicationName("foo")
                .ossAppVersion(null)
                .armoryAppVersion("")
                .build());

    assertEquals(2, res.size());
    assertEquals(res.get(SPIN_SVC), "foo");
    assertEquals(res.get(LIB), LIB_NAME);
  }

  @Test
  public void test_that_getDefaultTags_converts_map_to_list() {
    var res = sut.getDefaultTags(Map.of("foo", "bar"));

    assertEquals(1, res.size());
    assertEquals(res.get(0), Tag.of("foo", "bar"));
  }

  @Test
  public void test_that_getEnvironmentMetadata_reads_app_name_from_build_props() {
    sut = new TagsService(pluginConfig, versionResolver, null);
    var buildProps = mock(BuildProperties.class);
    when(buildProps.getName()).thenReturn("foo");
    var metadata = sut.getEnvironmentMetadata(buildProps, MOCK_PLUGIN_VER);
    assertEquals("foo", metadata.getApplicationName());
  }

  @Test
  public void test_that_getEnvironmentMetadata_uses_spring_app_name_props_if_build_props_null() {
    var buildProps = mock(BuildProperties.class);
    when(buildProps.getName()).thenReturn(null);
    var metadata = sut.getEnvironmentMetadata(buildProps, MOCK_PLUGIN_VER);
    assertEquals(MOCK_APPLICATION_NAME, metadata.getApplicationName());
  }

  @Test
  public void test_that_getEnvironmentMetadata_lastly_defaults_to_unknown() {
    var sut = new TagsService(new PluginConfig(), versionResolver, null);
    var buildProps = mock(BuildProperties.class);
    when(buildProps.getName()).thenReturn(null);
    var metadata = sut.getEnvironmentMetadata(buildProps, MOCK_PLUGIN_VER);
    assertEquals("UNKNOWN", metadata.getApplicationName());
  }

  @Test
  public void test_that_additional_tags_are_added() {
    sut.metricsConfig.setAdditionalTags(
        Map.of("foo", "bar", "someValue", "12345", SPINNAKER_RELEASE, "I will be ignored"));
    var res =
        sut.getDefaultTagsAsFilteredMap(
            ArmoryEnvironmentMetadata.builder().spinnakerRelease("I take precedence").build());

    assertEquals(4, res.size());
    assertEquals("bar", res.get("foo"));
    assertEquals("12345", res.get("someValue"));
    assertEquals("I take precedence", res.get(SPINNAKER_RELEASE));
    assertEquals(res.get(LIB), LIB_NAME);
  }

  @Test
  public void test_that_oss_version_resolver_is_used_when_armory_env_oss_version_is_null() {
    var appName = "foo";
    var version = UUID.randomUUID().toString();
    var env = ArmoryEnvironmentMetadata.builder().applicationName(appName).build();
    when(versionResolver.resolve(appName)).thenReturn(version);
    var tags = sut.getDefaultTagsAsFilteredMap(env);
    assertEquals(version, tags.get("version"));
  }

  @Test
  public void test_that_getPluginVersion_can_parse_properties() {
    var version = sut.getPluginVersion(PLUGIN_PROPERTIES_PATH);
    assertEquals("foo", version);
  }

  @Test
  public void test_that_getPluginVersion_returns_null_if_no_props_found() {
    var version = sut.getPluginVersion("i-do-not-exist");
    assertNull(version);
  }

  @Test
  public void test_that_getDefaultTags_returns_some_tags_even_when_no_props_are_loaded() {
    var tags = sut.getDefaultTags();
    assertNotNull(tags);
    assertTrue(tags.size() > 0);
  }
}
