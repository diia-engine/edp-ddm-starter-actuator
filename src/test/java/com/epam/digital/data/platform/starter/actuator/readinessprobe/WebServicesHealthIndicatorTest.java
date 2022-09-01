/*
 * Copyright 2021 EPAM Systems.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.digital.data.platform.starter.actuator.readinessprobe;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Set;

import com.epam.digital.data.platform.starter.actuator.readinessprobe.config.ReadinessServicesConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.Status;

@ExtendWith(MockitoExtension.class)
class WebServicesHealthIndicatorTest {

  @Mock
  private UrlAvailabilityChecker urlAvailabilityChecker;
  @Mock
  private ReadinessServicesConfig readinessServicesConfig;

  private WebServicesHealthIndicator webServicesHealthIndicator;

  @BeforeEach
  void setUp() {
    when(readinessServicesConfig.getServices()).thenReturn(Collections.emptySet());
    webServicesHealthIndicator =
        new WebServicesHealthIndicator(readinessServicesConfig, urlAvailabilityChecker);
  }

  @Test
  void shouldReportDownStatusIfOneServiceIsDown() {
    when(urlAvailabilityChecker.getDownServices(any())).thenReturn(downServices());

    var health = webServicesHealthIndicator.health();

    assertThat(health.getStatus()).isEqualTo(Status.DOWN);
    assertThat(health.getDetails()).containsEntry("SERVICES_DOWN", Set.of("example.gov.ua"));
  }

  @Test
  void shouldReportUpStatusIfNoServiceIsDown() {
    when(urlAvailabilityChecker.getDownServices(any())).thenReturn(Collections.emptySet());

    var health = webServicesHealthIndicator.health();

    assertThat(health.getStatus()).isEqualTo(Status.UP);
  }

  private Set<String> downServices() {
    return Set.of("example.gov.ua");
  }
}
