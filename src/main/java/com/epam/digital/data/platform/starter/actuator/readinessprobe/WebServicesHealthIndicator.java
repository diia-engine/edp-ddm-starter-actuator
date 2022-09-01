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

import java.util.Set;

import com.epam.digital.data.platform.starter.actuator.readinessprobe.config.ReadinessServicesConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class WebServicesHealthIndicator implements HealthIndicator {

  private final Logger log = LoggerFactory.getLogger(WebServicesHealthIndicator.class);
  private final UrlAvailabilityChecker urlAvailabilityChecker;
  private final Set<String> services;

  public WebServicesHealthIndicator(
          ReadinessServicesConfig servicesConfig, UrlAvailabilityChecker urlAvailabilityChecker) {
    this.services = servicesConfig.getServices();
    this.urlAvailabilityChecker = urlAvailabilityChecker;
  }

  @Override
  public Health health() {
    var downResponses = urlAvailabilityChecker.getDownServices(services);

    if (downResponses.isEmpty()) {
      return Health.up().build();
    }

    log.error("WebServicesHealthIndicator failed: {}", downResponses);
    return Health.down()
        .withDetail("SERVICES_DOWN", downResponses)
        .build();
  }
}
