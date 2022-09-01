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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.Status;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.SettableListenableFuture;

@ExtendWith(MockitoExtension.class)
class KafkaHealthIndicatorTest {

  @Mock
  private KafkaTemplate<String, String> kafkaTemplate;

  private KafkaHealthIndicator kafkaHealthIndicator;
  private SettableListenableFuture<SendResult<String, String>> future;

  @BeforeEach
  void setUp() {
    kafkaHealthIndicator = new KafkaHealthIndicator(kafkaTemplate);
    future = new SettableListenableFuture<>();
    future.set(new SendResult<>(null, null));
  }

  @Test
  void shouldReturnUpStatusWhenKafkaMessageSentAndGotBack() {
    when(kafkaTemplate.send(any(), any())).thenReturn(future);

    var health = kafkaHealthIndicator.health();

    assertThat(health.getStatus()).isEqualTo(Status.UP);
  }

  @Test
  void shouldReturnDownStatusWhenKafkaResultedInException() {
    when(kafkaTemplate.send(any(), any())).thenThrow(new RuntimeException());

    var health = kafkaHealthIndicator.health();

    assertThat(health.getStatus()).isEqualTo(Status.DOWN);
  }
}
