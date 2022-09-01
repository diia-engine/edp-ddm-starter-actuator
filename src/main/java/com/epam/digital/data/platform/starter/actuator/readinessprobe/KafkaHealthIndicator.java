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

import static com.epam.digital.data.platform.starter.actuator.readinessprobe.KafkaConstants.KAFKA_HEALTH_TOPIC;
import static com.epam.digital.data.platform.starter.actuator.readinessprobe.KafkaConstants.RESPONSE_TIMEOUT;

import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.kafka.core.KafkaTemplate;

public class KafkaHealthIndicator implements HealthIndicator {

  private final Logger log = LoggerFactory.getLogger(KafkaHealthIndicator.class);

  private final KafkaTemplate<String, String> kafkaTemplate;

  private static final String KAFKA_HEALTH_MESSAGE = "Check";

  public KafkaHealthIndicator(KafkaTemplate<String, String> kafkaTemplate) {
    this.kafkaTemplate = kafkaTemplate;
  }

  @Override
  public Health health() {
    try {
      kafkaTemplate.send(KAFKA_HEALTH_TOPIC, KAFKA_HEALTH_MESSAGE)
          .get(RESPONSE_TIMEOUT, TimeUnit.MILLISECONDS);
    } catch (Exception e) {
      log.error("KafkaHealthIndicator failed", e);
      return Health.down()
              .withException(e)
              .build();
    }
    return Health.up().build();
  }
}
