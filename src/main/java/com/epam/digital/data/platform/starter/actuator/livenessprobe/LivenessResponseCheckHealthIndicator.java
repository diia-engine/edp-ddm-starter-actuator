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

import org.springframework.boot.actuate.availability.AvailabilityStateHealthIndicator;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.availability.ApplicationAvailability;
import org.springframework.stereotype.Component;

@Component
public class LivenessResponseCheckHealthIndicator extends AvailabilityStateHealthIndicator {

  public LivenessResponseCheckHealthIndicator(ApplicationAvailability availability) {
    super(availability, LivenessResponseBasedState.class,
        statusMappings -> {
          statusMappings.add(LivenessResponseBasedState.CORRECT, Status.UP);
          statusMappings.add(LivenessResponseBasedState.BROKEN, Status.DOWN);
        });
  }
}
