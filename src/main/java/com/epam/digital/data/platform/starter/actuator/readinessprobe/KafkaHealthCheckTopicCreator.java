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
import static com.epam.digital.data.platform.starter.actuator.readinessprobe.KafkaConstants.RETENTION_MS;
import static com.epam.digital.data.platform.starter.actuator.readinessprobe.KafkaConstants.TOPIC_CREATION_TIMEOUT;
import static org.apache.kafka.common.config.TopicConfig.RETENTION_MS_CONFIG;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import javax.annotation.PostConstruct;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.actuate.autoconfigure.health.ConditionalOnEnabledHealthIndicator;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnEnabledHealthIndicator("kafka")
public class KafkaHealthCheckTopicCreator {

  private static final int NUM_PARTITIONS = 1;
  private static final short REPLICATION_FACTOR = 1;

  private final Supplier<AdminClient> actuatorKafkaAdminClientFactory;

  public KafkaHealthCheckTopicCreator(Supplier<AdminClient> actuatorKafkaAdminClientFactory) {
    this.actuatorKafkaAdminClientFactory = actuatorKafkaAdminClientFactory;
  }

  @PostConstruct
  public void createKafkaTopic() {
    try (var adminClient = actuatorKafkaAdminClientFactory.get()) {
      if (!isTopicExist(adminClient)) {
        create(adminClient);
      }
    }
  }

  private boolean isTopicExist(AdminClient adminClient) {
    try {
      return adminClient.listTopics()
          .names()
          .get(TOPIC_CREATION_TIMEOUT, TimeUnit.SECONDS)
          .contains(KAFKA_HEALTH_TOPIC);
    } catch (Exception e) {
      throw new CreateKafkaTopicException("Failed to retrieve existing kafka topics", e);
    }
  }

  private void create(AdminClient adminClient) {
    var createTopicsResult = adminClient.createTopics(getConfiguredHealthTopics());
    try {
      createTopicsResult.all().get(TOPIC_CREATION_TIMEOUT, TimeUnit.SECONDS);
    } catch (Exception e) {
      throw new CreateKafkaTopicException("Failed to create a kafka topic", e);
    }
  }

  private Collection<NewTopic> getConfiguredHealthTopics() {
    var newTopic = new NewTopic(KAFKA_HEALTH_TOPIC, NUM_PARTITIONS, REPLICATION_FACTOR);
    newTopic.configs(Map.of(RETENTION_MS_CONFIG, Long.toString(RETENTION_MS)));
    return Collections.singleton(newTopic);
  }

  static class CreateKafkaTopicException extends RuntimeException {

    public CreateKafkaTopicException(String message, Exception e) {
      super(message, e);
    }
  }
}
