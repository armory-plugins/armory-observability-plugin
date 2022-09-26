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

package io.armory.plugin.observability.model;

import com.netflix.spinnaker.kork.plugins.api.spring.ExposeToApp;
import io.micrometer.core.instrument.Clock;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties("spinnaker.extensibility.plugins.armory.observability-plugin.config")
@Slf4j
public class PluginConfig  {
    PluginMetricsConfig metrics = new PluginMetricsConfig();

    @Bean
    @ExposeToApp
    public Clock micrometerClock() {

        log.info("Bean clock created");
        return Clock.SYSTEM;

    }
}
