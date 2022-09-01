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
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
class UrlAvailabilityCheckerTest {

  @Mock
  private RestTemplate restTemplate;
  @InjectMocks
  private UrlAvailabilityChecker urlAvailabilityChecker;

  @Test
  void shouldReturnNoServicesIfAllRequestsSuccessful() {
    when(restTemplate.exchange("stub", HttpMethod.GET, null, String.class))
        .thenReturn(successfulResponse());

    var result = urlAvailabilityChecker.getDownServices(Set.of("stub"));

    assertThat(result.size()).isZero();
  }

  @Test
  void shouldReturnServicesHostnameIfRequestUnsuccessful() {
    when(restTemplate.exchange("stub", HttpMethod.GET, null, String.class))
        .thenReturn(unsuccessfulResponse());

    var result = urlAvailabilityChecker.getDownServices(Set.of("stub"));

    assertThat(result).hasSize(1)
        .contains("stub");
  }

  @Test
  void shouldReturnServicesHostnameIfRequestUnsuccessfulIoException() {
    when(restTemplate.exchange("stub", HttpMethod.GET, null, String.class))
        .thenThrow(new ResourceAccessException("", new IOException()));

    var result = urlAvailabilityChecker.getDownServices(Set.of("stub"));

    assertThat(result).hasSize(1)
        .contains("stub");
  }

  private ResponseEntity<String> successfulResponse() {
    return new ResponseEntity<>(null, HttpStatus.OK);
  }

  private ResponseEntity<String> unsuccessfulResponse() {
    return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
