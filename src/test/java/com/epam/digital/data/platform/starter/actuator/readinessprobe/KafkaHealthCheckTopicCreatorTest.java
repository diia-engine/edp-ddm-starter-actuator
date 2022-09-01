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

import com.epam.digital.data.platform.starter.actuator.readinessprobe.KafkaHealthCheckTopicCreator.CreateKafkaTopicException;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.ListTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.errors.TopicExistsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static com.epam.digital.data.platform.starter.actuator.readinessprobe.KafkaConstants.KAFKA_HEALTH_TOPIC;
import static org.apache.kafka.common.config.TopicConfig.RETENTION_MS_CONFIG;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KafkaHealthCheckTopicCreatorTest {

  private static final String RETENTION_MS = Long.toString(1000L);

  private Set<String> existedTopics;
  private KafkaHealthCheckTopicCreator instance;

  @Mock
  private AdminClient adminClient;
  @Mock
  private KafkaFuture<Void> createTopicsFuture;
  @Mock
  private KafkaFuture<Set<String>> listTopicsFuture;
  @Mock
  private CreateTopicsResult createTopicsResult;
  @Mock
  private ListTopicsResult listTopicsResult;
  @Captor
  private ArgumentCaptor<Set<NewTopic>> setArgumentCaptor;

  @BeforeEach
  void setup() {
    instance = new KafkaHealthCheckTopicCreator(() -> adminClient);
    existedTopics = new HashSet<>();
    existedTopics.add("some-topic");
    existedTopics.add("another-topic");
  }

  @Test
  void shouldCreateTopic() throws Exception {
    customizeAdminClientMock(existedTopics);
    when(createTopicsResult.values())
            .thenReturn(Collections.singletonMap(KAFKA_HEALTH_TOPIC, createTopicsFuture));
    when(adminClient.createTopics(anyCollection())).thenReturn(createTopicsResult);

    instance.createKafkaTopic();

    verify(adminClient).createTopics(setArgumentCaptor.capture());
    var resultSet = setArgumentCaptor.getValue();

    assertEquals(1, resultSet.size());

    var newTopic = resultSet.stream().findFirst().get();

    assertEquals(KAFKA_HEALTH_TOPIC, newTopic.name());
    assertEquals(1, newTopic.configs().size());
    assertEquals(RETENTION_MS, newTopic.configs().get(RETENTION_MS_CONFIG));
  }

  @Test
  void shouldNotCreateTopic() throws Exception {
    customizeAdminClientMock(existedTopics);
    existedTopics.add(KAFKA_HEALTH_TOPIC);

    instance.createKafkaTopic();

    verify(adminClient, never()).createTopics(setArgumentCaptor.capture());
  }

  @Test
  void shouldThrowExceptionWhenCannotConnectToKafka() {
    when(adminClient.listTopics()).thenThrow(new CreateKafkaTopicException("any", null));

    assertThrows(CreateKafkaTopicException.class, () -> instance.createKafkaTopic());
  }

  @Test
  void shouldThrowExceptionWhenNonSuccessTopicCreation() throws Exception {
    customizeAdminClientMock(existedTopics);
    when(adminClient.createTopics(anyCollection())).thenReturn(createTopicsResult);
    when(createTopicsResult.values())
        .thenReturn(Collections.singletonMap(KAFKA_HEALTH_TOPIC, createTopicsFuture));
    when(createTopicsFuture.get(anyLong(), any(TimeUnit.class))).thenThrow(new RuntimeException());

    assertThrows(CreateKafkaTopicException.class, () -> instance.createKafkaTopic());
  }

  @Test
  void shouldNotFailIfTopicExistsException() throws Exception {
    customizeAdminClientMock(existedTopics);
    when(createTopicsResult.values())
            .thenReturn(Collections.singletonMap(KAFKA_HEALTH_TOPIC, createTopicsFuture));
    when(adminClient.createTopics(anyCollection())).thenReturn(createTopicsResult);
    when(createTopicsFuture.get(anyLong(), any(TimeUnit.class)))
            .thenThrow(new RuntimeException(new TopicExistsException("")));

    instance.createKafkaTopic();
  }

  private void customizeAdminClientMock(Set<String> topics) throws Exception {
    when(adminClient.listTopics()).thenReturn(listTopicsResult);
    when(listTopicsResult.names()).thenReturn(listTopicsFuture);
    doReturn(topics).when(listTopicsFuture).get(anyLong(), any(TimeUnit.class));
  }
}
