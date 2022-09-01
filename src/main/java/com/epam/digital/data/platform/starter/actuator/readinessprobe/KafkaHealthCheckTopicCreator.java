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
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.errors.TopicExistsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KafkaHealthCheckTopicCreator {

  private static final int NUM_PARTITIONS = 1;
  private static final short REPLICATION_FACTOR = 1;

  private final Logger log = LoggerFactory.getLogger(KafkaHealthCheckTopicCreator.class);

  private final Supplier<AdminClient> adminClientFactory;

  public KafkaHealthCheckTopicCreator(Supplier<AdminClient> adminClientFactory) {
    this.adminClientFactory = adminClientFactory;
  }

  @PostConstruct
  public void createKafkaTopic() {
    try (var adminClient = adminClientFactory.get()) {
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
    createTopicsResult.values().forEach(this::handleTopicCreationResult);
  }

  private void handleTopicCreationResult(String topicName, KafkaFuture<Void> future) {
    try {
      future.get(TOPIC_CREATION_TIMEOUT, TimeUnit.SECONDS);
    } catch (Exception e) {
      if (e.getCause() instanceof TopicExistsException) {
        log.warn("Topic {} was in missing topics list, but now exists", topicName);
      } else {
        throw new CreateKafkaTopicException(
            String.format("Failed to create topic %s in %d sec", topicName, TOPIC_CREATION_TIMEOUT),
            e);
      }
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
