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

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.availability.ApplicationAvailability;
import org.springframework.boot.availability.AvailabilityChangeEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class LivenessStateHandler {

  private final Integer livenessProbeFailureThreshold;

  private final ApplicationEventPublisher applicationEventPublisher;
  private final ApplicationAvailability applicationAvailability;

  private final AtomicInteger unhealthyConsecutiveRequestsCounter = new AtomicInteger(0);

  public LivenessStateHandler(
      @Value("${probes.liveness.failureThreshold:10}") Integer livenessProbeFailureThreshold,
      ApplicationEventPublisher applicationEventPublisher,
      ApplicationAvailability applicationAvailability) {
    this.livenessProbeFailureThreshold = livenessProbeFailureThreshold;
    this.applicationEventPublisher = applicationEventPublisher;
    this.applicationAvailability = applicationAvailability;
  }

  public <T> void handleResponse(T response, Predicate<T> unhealthyResponseCondition) {
    if (unhealthyResponseCondition.test(response)) {
      if (unhealthyConsecutiveRequestsCounter.incrementAndGet() >= livenessProbeFailureThreshold) {
        AvailabilityChangeEvent.publish(
            applicationEventPublisher, this, LivenessResponseBasedState.BROKEN);
      }
    } else {
      unhealthyConsecutiveRequestsCounter.set(0);
      LivenessResponseBasedState currState = applicationAvailability.getState(
              LivenessResponseBasedState.class, LivenessResponseBasedState.BROKEN);
      if (LivenessResponseBasedState.BROKEN.equals(currState)) {
        AvailabilityChangeEvent.publish(
            applicationEventPublisher, this, LivenessResponseBasedState.CORRECT);
      }
    }
  }
}
