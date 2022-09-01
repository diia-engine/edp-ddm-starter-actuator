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

package com.epam.digital.data.platform.starter.actuator.readinessprobe.config;

import com.epam.digital.data.platform.starter.actuator.readinessprobe.KafkaHealthCheckTopicCreator;
import com.epam.digital.data.platform.starter.actuator.readinessprobe.KafkaHealthIndicator;
import org.apache.kafka.clients.admin.AdminClient;
import org.springframework.boot.actuate.autoconfigure.health.ConditionalOnEnabledHealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.function.Supplier;

@Configuration
@ConditionalOnClass(KafkaTemplate.class)
@ConditionalOnEnabledHealthIndicator("kafka")
public class ActuatorKafkaConfig {

  @Bean
  public KafkaHealthIndicator kafkaHealthIndicator(KafkaTemplate<String, String> kafkaTemplate) {
    return new KafkaHealthIndicator(kafkaTemplate);
  }

  @Bean
  public KafkaHealthCheckTopicCreator kafkaHealthCheckTopicCreator(
      Supplier<AdminClient> adminClientFactory) {
    return new KafkaHealthCheckTopicCreator(adminClientFactory);
  }
}
