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

import static java.util.Optional.ofNullable;

import com.netflix.spinnaker.kork.version.VersionResolver;
import io.armory.plugin.observability.model.ArmoryEnvironmentMetadata;
import io.armory.plugin.observability.model.PluginConfig;
import io.armory.plugin.observability.model.PluginMetricsConfig;
import io.micrometer.core.instrument.Tag;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.info.BuildProperties;

/**
 * Service that will collect common metadata and allow for Micrometer to use this metadata as common
 * tags.
 */
@Slf4j
public class TagsService {

  private static final String SPRING_BOOT_BUILD_PROPERTIES_PATH = "META-INF/build-info.properties";
  protected static final String PLUGIN_PROPERTIES_PATH =
      "io/armory/plugin/observability/build.properties";

  protected final PluginMetricsConfig metricsConfig;
  private final VersionResolver versionResolver;
  private final String springInjectedApplicationName;

  public TagsService(
      PluginConfig metricsConfig,
      VersionResolver versionResolver,
      @Value("${spring.application.name:#{null}}") String springInjectedApplicationName) {

    this.metricsConfig = metricsConfig.getMetrics();
    this.versionResolver = versionResolver;
    this.springInjectedApplicationName = springInjectedApplicationName;
  }

  private String trimToNull(String string) {
    if (string == null) {
      return null;
    }
    var trimmed = string.strip();
    return trimmed.isEmpty() ? null : trimmed;
  }

  protected ArmoryEnvironmentMetadata getEnvironmentMetadata(
      BuildProperties buildProperties, String pluginVersion) {

    String resolvedApplicationName =
        ofNullable(trimToNull(springInjectedApplicationName))
            .or(() -> ofNullable(trimToNull(buildProperties.getName())))
            .orElse("UNKNOWN");

    return ArmoryEnvironmentMetadata.builder()
        .pluginVersion(pluginVersion)
        .applicationName(resolvedApplicationName)
        .armoryAppVersion(buildProperties.getVersion())
        .ossAppVersion(buildProperties.get("ossVersion"))
        .spinnakerRelease(buildProperties.get("spinnakerRelease"))
        .build();
  }

  /**
   * @return Map of environment metadata that we will use as the default tags, with all null/empty
   *     values stripped.
   */
  protected Map<String, String> getDefaultTagsAsFilteredMap(
      ArmoryEnvironmentMetadata environmentMetadata) {
    Map<String, String> tags = new HashMap<>(metricsConfig.getAdditionalTags());

    ofNullable(environmentMetadata.getOssAppVersion())
        .or(() -> ofNullable(versionResolver.resolve(environmentMetadata.getApplicationName())))
        .ifPresent(version -> tags.put("version", version));

    tags.put("lib", "aop");
    tags.put("libVer", environmentMetadata.getPluginVersion());
    tags.put("spinSvc", environmentMetadata.getApplicationName());
    tags.put("armSpinSvcVer", environmentMetadata.getArmoryAppVersion());
    tags.put("ossSpinSvcVer", environmentMetadata.getOssAppVersion());
    tags.put("spinnakerRelease", environmentMetadata.getSpinnakerRelease());
    tags.put("hostname", System.getenv("HOSTNAME"));

    return tags.entrySet().stream()
        .filter(it -> (it.getValue() != null && !it.getValue().strip().equals("")))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  /**
   * Loads the the build properties Spring boot metadata object. Normally you would get this auto
   * injected into your configuration. Since this is a simple plugin, we can just read the file and
   * load the props.
   *
   * <p>If the file is not present, because the plugin is being loaded into OSS Spinnaker for
   * example, the props will all be null
   *
   * <p>Duplicates the logic from
   * https://github.com/spring-projects/spring-boot/blob/28e1b90735a57eb637690a4a029b462f8a3eafee/spring-boot-project/spring-boot-autoconfigure/src/main/java/org/springframework/boot/autoconfigure/info/ProjectInfoAutoConfiguration.java#L68-L86
   *
   * @return build-related information such as group and artifact.
   */
  protected BuildProperties getBuildProperties(String propertiesPath) {
    var buildInfoPrefix = "build.";
    var buildInfoProperties = new Properties();
    try (var is = this.getClass().getClassLoader().getResourceAsStream(propertiesPath)) {
      var rawProperties = new Properties();
      rawProperties.load(is);
      rawProperties.stringPropertyNames().stream()
          .filter(propName -> propName.startsWith(buildInfoPrefix))
          .forEach(
              propName ->
                  buildInfoProperties.put(
                      propName.substring(buildInfoPrefix.length()), rawProperties.get(propName)));
    } catch (Exception e) {
      log.warn(
          "You can ignore the following warning if you are not running an Armory Wrapper Spinnaker Service for Spinnaker >= 2.19");
      log.warn("Failed to load META-INF/build-info.properties, msg: {}", e.getMessage());
    }
    return new BuildProperties(buildInfoProperties);
  }

  /** @return the plugin version */
  protected String getPluginVersion(String path) {
    try (var is = this.getClass().getClassLoader().getResourceAsStream(path)) {
      var properties = new Properties();
      properties.load(is);
      return properties.getProperty("version");
    } catch (Exception e) {
      log.warn(
          "Failed to load the plugin version build props file, returning null msg: {}",
          e.getMessage());
      return null;
    }
  }

  protected List<Tag> getDefaultTags(Map<String, String> tags) {
    return tags.entrySet().stream()
        .map(
            tag -> {
              log.info(
                  "Adding default tag {}: {} to default tags list.", tag.getKey(), tag.getValue());
              return Tag.of(tag.getKey(), tag.getValue());
            })
        .collect(Collectors.toList());
  }

  protected String getPropertiesPath() {
    return SPRING_BOOT_BUILD_PROPERTIES_PATH;
  }

  protected String getPluginPropertiesPath() {
    return PLUGIN_PROPERTIES_PATH;
  }

  public List<Tag> getDefaultTags() {
    if (metricsConfig.isDefaultTagsDisabled()) {
      return List.of();
    }
    var springbootPropertiesPath = getPropertiesPath();
    var buildProperties = getBuildProperties(springbootPropertiesPath);
    var pluginPropertiesPath = getPluginPropertiesPath();
    var pluginVersion = getPluginVersion(pluginPropertiesPath);
    var environmentMetadata = getEnvironmentMetadata(buildProperties, pluginVersion);
    var tagsAsMap = getDefaultTagsAsFilteredMap(environmentMetadata);
    return getDefaultTags(tagsAsMap);
  }
}
