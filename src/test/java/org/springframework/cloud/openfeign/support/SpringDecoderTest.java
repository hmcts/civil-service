package org.springframework.cloud.openfeign.support;

import feign.Request;
import feign.Response;
import feign.codec.DecodeException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SpringDecoderTest {

    @Test
    void shouldDecodeJsonUsingDefaultConvertersWhenProviderReturnsNull() throws Exception {
        ObjectProvider<FeignHttpMessageConverters> provider = mockFeignConvertersProvider();
        when(provider.getObject()).thenReturn(null);
        SpringDecoder decoder = new SpringDecoder(provider);

        Response response = jsonResponse(200, "{\"value\":\"ok\"}");

        Object decoded = decoder.decode(response, (Type) Map.class);

        assertThat(decoded).isInstanceOfSatisfying(
            Map.class,
            value -> assertThat(((Map<?, ?>) value).get("value")).isEqualTo("ok")
        );
    }

    @Test
    void shouldDecodeJsonUsingLegacyConvertersConstructor() throws Exception {
        ObjectFactory<?> legacyFactory =
            () -> new HttpMessageConverters(List.of(new MappingJackson2HttpMessageConverter()));
        ObjectProvider<?> ignoredCustomizerProvider = mockAnyObjectProvider();
        SpringDecoder decoder = new SpringDecoder(legacyFactory, ignoredCustomizerProvider);

        Response response = jsonResponse(200, "{\"name\":\"civil\"}");

        Object decoded = decoder.decode(response, (Type) Map.class);

        assertThat(decoded).isInstanceOfSatisfying(
            Map.class,
            value -> assertThat(((Map<?, ?>) value).get("name")).isEqualTo("civil")
        );
    }

    @Test
    void shouldReturnSameResponseWhenTypeIsResponseClass() throws Exception {
        ObjectProvider<FeignHttpMessageConverters> provider = mockFeignConvertersProvider();
        when(provider.getObject()).thenReturn(null);
        SpringDecoder decoder = new SpringDecoder(provider);
        Response response = jsonResponse(200, "{\"value\":\"ok\"}");

        Object decoded = decoder.decode(response, Response.class);

        assertThat(decoded).isSameAs(response);
    }

    @Test
    void shouldReturnNullForNoContentStatus() throws Exception {
        ObjectProvider<FeignHttpMessageConverters> provider = mockFeignConvertersProvider();
        when(provider.getObject()).thenReturn(null);
        SpringDecoder decoder = new SpringDecoder(provider);
        Response response = jsonResponse(204, "");

        Object decoded = decoder.decode(response, String.class);

        assertThat(decoded).isNull();
    }

    @Test
    void shouldWrapRestClientExceptionAsDecodeException() {
        ObjectProvider<FeignHttpMessageConverters> provider = mockFeignConvertersProvider();
        when(provider.getObject()).thenThrow(new NoSuchBeanDefinitionException("not found"));
        SpringDecoder decoder = new SpringDecoder(provider);
        Response response = jsonResponse(200, "not-json");

        assertThatThrownBy(() -> decoder.decode(response, Map.class))
            .isInstanceOf(DecodeException.class);
    }

    private static Response jsonResponse(int status, String body) {
        Request request = Request.create(
            Request.HttpMethod.POST,
            "http://localhost/test",
            Map.of(),
            new byte[0],
            StandardCharsets.UTF_8,
            null
        );
        return Response.builder()
            .request(request)
            .status(status)
            .reason("reason")
            .headers(Map.of("Content-Type", List.of("application/json")))
            .body(body, StandardCharsets.UTF_8)
            .build();
    }

    @SuppressWarnings("unchecked")
    private static ObjectProvider<FeignHttpMessageConverters> mockFeignConvertersProvider() {
        return (ObjectProvider<FeignHttpMessageConverters>) mock(ObjectProvider.class);
    }

    @SuppressWarnings("unchecked")
    private static ObjectProvider<Object> mockAnyObjectProvider() {
        return (ObjectProvider<Object>) mock(ObjectProvider.class);
    }
}
