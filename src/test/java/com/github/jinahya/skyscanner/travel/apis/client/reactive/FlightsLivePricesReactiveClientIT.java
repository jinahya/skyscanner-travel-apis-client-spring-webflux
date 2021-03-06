package com.github.jinahya.skyscanner.travel.apis.client.reactive;

/*-
 * #%L
 * skyscanner-travel-apis-client-spring
 * %%
 * Copyright (C) 2020 Jinahya, Inc.
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.sun.javafx.scene.control.skin.VirtualFlow;
import lombok.extern.slf4j.Slf4j;
import net.skyscanner.api.partners.apiservices.pricing.v1_0.FlightsLivePricesResultPollingRequest;
import net.skyscanner.api.partners.apiservices.pricing.v1_0.FlightsLivePricesSessionCreationRequest;
import net.skyscanner.api.partners.apiservices.reference.v1_0.Country;
import net.skyscanner.api.partners.apiservices.reference.v1_0.Currency;
import net.skyscanner.api.partners.apiservices.reference.v1_0.Locale;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.EnabledIf;
import reactor.core.publisher.DirectProcessor;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import static com.github.jinahya.skyscanner.travel.apis.client.Application.SYSTEM_PROPERTY_NAME_API_KEY;
import static org.assertj.core.api.Assertions.assertThat;

@EnabledIf("#{systemProperties['" + SYSTEM_PROPERTY_NAME_API_KEY + "'] != null}")
@SpringBootTest
@Slf4j
class FlightsLivePricesReactiveClientIT extends SkyscannerTravelApisReactiveClientIT<FlightsLivePricesReactiveClient> {

    FlightsLivePricesReactiveClientIT() {
        super(FlightsLivePricesReactiveClient.class);
    }

    // -----------------------------------------------------------------------------------------------------------------
    @Test
    void testFlightsLivePrices() {
        final LocalDate today = LocalDate.now();
        final FlightsLivePricesSessionCreationRequest sessionCreationRequest
                = FlightsLivePricesSessionCreationRequest.builder()
                .country("KR")
                .currency("KRW")
                .locale("ko-KR")
                .originPlace("ICN-sky")
                .destinationPlace("LOND-sky")
                .outboundDate(today.plus(10, ChronoUnit.DAYS))
                .inboundDate(today.plus(12, ChronoUnit.DAYS))
                .adults(1)
                .build();
        final String location = clientInstance().createSession(sessionCreationRequest).block();
        log.debug("location: {}", location);
        assertThat(location).isNotEmpty();
        final FlightsLivePricesResultPollingRequest resultPollingRequest
                = FlightsLivePricesResultPollingRequest.builder()
                .build();
        clientInstance()
                .pollResult(location, resultPollingRequest)
                .doOnNext(r -> {
                    log.debug("response.hashCode: {}", String.format("0x%08x", r.hashCode()));
                    log.debug("response.status: {}", r.getStatus());
                    log.debug("response.itineraries.size: {}", r.getItineraries().size());
                    assertThat(validator().validate(r)).isEmpty();
                })
                .filter(r -> {
                    final boolean empty = r.getItineraries().isEmpty();
                    if (empty) {
                        log.debug("filtering... empty");
                    }
                    return !empty;
                })
                .blockLast();
    }
}
