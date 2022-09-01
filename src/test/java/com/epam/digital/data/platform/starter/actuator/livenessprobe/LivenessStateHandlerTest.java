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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.availability.ApplicationAvailability;
import org.springframework.boot.availability.AvailabilityChangeEvent;
import org.springframework.context.ApplicationEventPublisher;

import java.util.function.Predicate;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LivenessStateHandlerTest {

  private static final int TEST_FAILURE_THRESHOLD = 2;

  private LivenessStateHandler livenessStateHandler;

  @Mock
  private ApplicationEventPublisher applicationEventPublisher;
  @Mock
  private ApplicationAvailability applicationAvailability;

  @BeforeEach
  void beforeEach() {
    livenessStateHandler = new LivenessStateHandler(
            TEST_FAILURE_THRESHOLD, applicationEventPublisher, applicationAvailability);
  }

  @Test
  void expectNeverChangeStateWhenUnhealthyAmountUnderThreshold() {
    int response = 500;
    Predicate<Integer> unhealthyCondition = resp -> resp == 500;
    livenessStateHandler.handleResponse(response, unhealthyCondition);

    verify(applicationEventPublisher, never()).publishEvent(any());
    verify(applicationAvailability, never()).getState(any());
  }

  @Test
  void expectChangeStateToBrokenWhenUnhealthyOverThreshold() {
    int response = 500;
    Predicate<Integer> unhealthyCondition = resp -> resp == 500;
    livenessStateHandler.handleResponse(response, unhealthyCondition);
    livenessStateHandler.handleResponse(response, unhealthyCondition);

    ArgumentCaptor<AvailabilityChangeEvent<LivenessResponseBasedState>> changeAvailabilityEventCaptor =
            ArgumentCaptor.forClass(AvailabilityChangeEvent.class);
    verify(applicationEventPublisher).publishEvent(changeAvailabilityEventCaptor.capture());
    assertThat(changeAvailabilityEventCaptor.getValue().getState())
        .isEqualTo(LivenessResponseBasedState.BROKEN);

    verify(applicationAvailability, never()).getState(any());
  }

  @Test
  void expectChangeStateFromBrokenToCorrectWhenSuccess() {
    when(applicationAvailability.getState(any(), any()))
        .thenReturn(LivenessResponseBasedState.BROKEN);

    int response = 501;
    Predicate<Integer> unhealthyCondition = resp -> resp == 500;
    livenessStateHandler.handleResponse(response, unhealthyCondition);

    ArgumentCaptor<AvailabilityChangeEvent<LivenessResponseBasedState>> changeAvailabilityEventCaptor = ArgumentCaptor.forClass(AvailabilityChangeEvent.class);
    verify(applicationEventPublisher).publishEvent(changeAvailabilityEventCaptor.capture());
    assertThat(changeAvailabilityEventCaptor.getValue().getState())
        .isEqualTo(LivenessResponseBasedState.CORRECT);
  }
}