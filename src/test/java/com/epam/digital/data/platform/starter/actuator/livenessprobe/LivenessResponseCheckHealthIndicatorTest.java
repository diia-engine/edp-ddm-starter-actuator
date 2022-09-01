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

package com.epam.digital.data.platform.starter.actuator.livenessprobe;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.availability.ApplicationAvailability;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LivenessResponseCheckHealthIndicatorTest {

  private LivenessResponseCheckHealthIndicator livenessResponseCheckHealthIndicator;

  @Mock
  private ApplicationAvailability applicationAvailability;

  @Test
  void expectCustomStateIsMappedCorrectlyToHealth() {
    when(applicationAvailability.getState(any())).thenReturn(LivenessResponseBasedState.CORRECT);
    livenessResponseCheckHealthIndicator =
        new LivenessResponseCheckHealthIndicator(applicationAvailability);

    Health actual = livenessResponseCheckHealthIndicator.health();

    assertThat(actual.getStatus()).isEqualTo(Status.UP);
  }
}
