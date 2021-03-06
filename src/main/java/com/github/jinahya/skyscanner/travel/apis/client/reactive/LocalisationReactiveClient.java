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

import com.fasterxml.jackson.core.JsonParser;
import lombok.extern.slf4j.Slf4j;
import net.skyscanner.api.partners.apiservices.reference.v1_0.Country;
import net.skyscanner.api.partners.apiservices.reference.v1_0.Currency;
import net.skyscanner.api.partners.apiservices.reference.v1_0.Locale;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.Disposable;
import reactor.core.publisher.DirectProcessor;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.Collection;

import static com.github.jinahya.skyscanner.travel.apis.client.utils.JsonParserUtils.parseWrappedArrayInDocument;
import static com.github.jinahya.skyscanner.travel.apis.client.utils.ResponseSpecUtils.pipeBodyAndAccept;
import static java.nio.channels.Channels.newInputStream;

/**
 * A client related to <a href="https://skyscanner.github.io/slate/#localisation">Localisation</a>.
 *
 * @author Jin Kwon &lt;onacit_at_gmail.com&gt;
 */
@Validated
@Component
@Slf4j
public class LocalisationReactiveClient extends SkyscannerTravelApisReactiveClient {

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Retrieves locales.
     *
     * @param sink a flux sink to which parsed elements are pushed.
     * @return a mono to block.
     * @see <a href="https://skyscanner.github.io/slate/#locales">Locales</a>
     */
    @NonNull
    public Mono<Void> retrieveLocales(@NotNull final FluxSink<? super Locale> sink) {
        final WebClient.ResponseSpec response = webClient()
                .get()
                .uri(b -> b.pathSegment("reference", "v1.0", "locales").build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve();
        return pipeBodyAndAccept(
                response,
                c -> {
                    try {
                        try (JsonParser parser = objectMapper().createParser(newInputStream(c))) {
                            parseWrappedArrayInDocument(parser, Locale.class, sink::next);
                            sink.complete();
                        }
                    } catch (final IOException ioe) {
                        sink.error(ioe);
                    }
                }
        );
    }

    /**
     * Retrieves locales and adds to specified collection.
     *
     * @param collection the collection to which retrieved locales are added.
     * @return specified collection.
     * @see #retrieveCurrencies(FluxSink)
     */
    @NonNull
    public <T extends Collection<? super Locale>> T retrieveLocales(@NotNull final T collection) {
        final DirectProcessor<Locale> processor = DirectProcessor.create();
        final Disposable disposable = processor.subscribe(collection::add);
        retrieveLocales(processor.sink()).block();
        return collection;
    }

    // -----------------------------------------------------------------------------------------------------------------
    /**
     * Retrieves currencies.
     *
     * @param sink a flux sink to which parsed elements are pushed.
     * @return a mono to block.
     * @see <a href="https://skyscanner.github.io/slate/#currencies">Currencies</a>
     */
    @NonNull
    public Mono<Void> retrieveCurrencies(@NotNull final FluxSink<? super Currency> sink) {
        final WebClient.ResponseSpec response = webClient().get()
                .uri(b -> b.pathSegment("reference", "v1.0", "currencies").build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve();
        return pipeBodyAndAccept(
                response,
                c -> {
                    try {
                        try (JsonParser parser = objectMapper().createParser(newInputStream(c))) {
                            parseWrappedArrayInDocument(parser, Currency.class, sink::next);
                            sink.complete();
                        }
                    } catch (final IOException ioe) {
                        sink.error(ioe);
                    }
                }
        );
    }

    /**
     * Retrieves market countries.
     *
     * @param locale a locale of preferred language of the result.
     * @param sink   a flux sink to which parsed elements are pushed.
     * @return a mono to block
     * @see <a href="https://skyscanner.github.io/slate/#markets">Markets</a>
     */
    @NonNull
    public Mono<Void> retrieveMarkets(@NotBlank final String locale, @NotNull final FluxSink<? super Country> sink) {
        final WebClient.ResponseSpec response = webClient().get()
                .uri(b -> b.pathSegment("reference", "v1.0", "countries", locale).build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve();
        return pipeBodyAndAccept(
                response,
                c -> {
                    try {
                        try (JsonParser parser = objectMapper().createParser(newInputStream(c))) {
                            parseWrappedArrayInDocument(parser, Country.class, sink::next);
                            sink.complete();
                        }
                    } catch (final IOException ioe) {
                        sink.error(ioe);
                    }
                }
        );
    }
}
