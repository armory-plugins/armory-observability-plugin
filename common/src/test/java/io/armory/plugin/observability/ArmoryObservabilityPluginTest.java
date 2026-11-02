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

package io.armory.plugin.observability;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.pf4j.PluginRuntimeException;
import org.pf4j.PluginWrapper;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;

@ExtendWith(MockitoExtension.class)
public class ArmoryObservabilityPluginTest {

  @Mock PluginWrapper pluginWrapper;

  @Mock BeanDefinitionRegistry registry;

  ArmoryObservabilityPlugin sut;

  @BeforeEach
  public void before() {
    sut = new ArmoryObservabilityPlugin(pluginWrapper);
  }

  @Test
  public void test_that_registerBeanDefinitions_registers_some_beans() {
    sut.registerBeanDefinitions(registry);
    verify(registry, atLeast(1)).registerBeanDefinition(any(), any());
  }

  @Test
  public void test_that_registerBeanDefinitions_throws_PRE_when_bean_registration_fails() {
    doThrow(new BeanDefinitionStoreException("error"))
        .when(registry)
        .registerBeanDefinition(any(), any());
    assertThrows(
        PluginRuntimeException.class, () -> sut.registerBeanDefinitions(registry));
  }
}
