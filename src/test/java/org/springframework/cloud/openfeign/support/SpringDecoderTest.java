package org.springframework.cloud.openfeign.support;

import feign.Request;
import feign.Response;
import feign.codec.DecodeException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.core.io.Resource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
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
    void shouldDecodePdfAsResourceUsingLegacyConvertersConstructorDefaults() throws Exception {
        ObjectFactory<?> legacyFactory = () -> new HttpMessageConverters(List.of());
        ObjectProvider<?> ignoredCustomizerProvider = mockAnyObjectProvider();
        SpringDecoder decoder = new SpringDecoder(legacyFactory, ignoredCustomizerProvider);
        Response response = binaryResponse(200, "application/pdf", "pdf-content".getBytes(StandardCharsets.UTF_8));

        Object decoded = decoder.decode(response, Resource.class);

        assertThat(decoded).isInstanceOfSatisfying(
            Resource.class,
            resource -> assertThat(readResource(resource)).isEqualTo("pdf-content".getBytes(StandardCharsets.UTF_8))
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

    @Test
    void shouldReturnNullWhenResponseBodyIsNull() throws Exception {
        SpringDecoder decoder = new SpringDecoder(mockFeignConvertersProvider());
        Response response = responseWithoutBody(200);

        Object decoded = decoder.decode(response, Map.class);

        assertThat(decoded).isNull();
    }

    @Test
    void shouldUseProvidedFeignConvertersWhenAvailable() throws Exception {
        ObjectProvider<FeignHttpMessageConverters> provider = mockFeignConvertersProvider();
        FeignHttpMessageConverters converters = mock(FeignHttpMessageConverters.class);
        when(converters.getConverters()).thenReturn(List.of(new MappingJackson2HttpMessageConverter()));
        when(provider.getObject()).thenReturn(converters);
        SpringDecoder decoder = new SpringDecoder(provider);

        Object decoded = decoder.decode(jsonResponse(200, "{\"x\":\"y\"}"), (Type) Map.class);

        assertThat(decoded).isInstanceOfSatisfying(
            Map.class,
            value -> assertThat(((Map<?, ?>) value).get("x")).isEqualTo("y")
        );
    }

    @Test
    void shouldFallbackToDefaultConvertersWhenLegacyFactoryHasNoGetConvertersMethod() throws Exception {
        ObjectFactory<?> legacyFactory = Object::new;
        SpringDecoder decoder = new SpringDecoder(legacyFactory, mockAnyObjectProvider());

        Object decoded = decoder.decode(jsonResponse(200, "{\"k\":\"v\"}"), (Type) Map.class);

        assertThat(decoded).isInstanceOfSatisfying(
            Map.class,
            value -> assertThat(((Map<?, ?>) value).get("k")).isEqualTo("v")
        );
    }

    @Test
    void shouldFallbackToDefaultConvertersWhenLegacyFactoryThrowsBeansException() throws Exception {
        ObjectFactory<?> legacyFactory = () -> {
            throw new NoSuchBeanDefinitionException("legacy converters missing");
        };
        SpringDecoder decoder = new SpringDecoder(legacyFactory, mockAnyObjectProvider());

        Object decoded = decoder.decode(jsonResponse(200, "{\"fallback\":true}"), (Type) Map.class);

        assertThat(decoded).isInstanceOfSatisfying(
            Map.class,
            value -> assertThat(((Map<?, ?>) value).get("fallback")).isEqualTo(true)
        );
    }

    @Test
    void shouldExposeResponseAdapterStatusTextHeadersAndCloseBody() throws Exception {
        TestBody body = new TestBody(new ByteArrayInputStream("{}".getBytes(StandardCharsets.UTF_8)), false);
        Response response = responseWithCustomBody(200, "custom-reason", body);
        ClientHttpResponse adapter = createAdapter(response);

        assertThat(adapter.getStatusText()).isEqualTo("custom-reason");
        assertThat(adapter.getHeaders().get("Content-Type")).containsExactly("application/json");
        assertThat(adapter.getBody()).isNotNull();
        adapter.close();

        assertThat(body.closed).isTrue();
    }

    @Test
    void shouldHandleAdapterBodyNullAndCloseIOException() throws Exception {
        ClientHttpResponse nullBodyAdapter = createAdapter(responseWithoutBody(200));
        InputStream body = nullBodyAdapter.getBody();
        assertThat(body.readAllBytes()).isEmpty();

        ClientHttpResponse failingCloseAdapter = createAdapter(
            responseWithCustomBody(200, "reason", new TestBody(new ByteArrayInputStream(new byte[0]), true))
        );
        failingCloseAdapter.close();
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

    private static Response responseWithoutBody(int status) {
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
            .build();
    }

    private static Response responseWithCustomBody(int status, String reason, Response.Body body) {
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
            .reason(reason)
            .headers(Map.of("Content-Type", List.of("application/json")))
            .body(body)
            .build();
    }

    private static Response binaryResponse(int status, String contentType, byte[] body) {
        Request request = Request.create(
            Request.HttpMethod.GET,
            "http://localhost/document",
            Map.of(),
            null,
            StandardCharsets.UTF_8,
            null
        );
        return Response.builder()
            .request(request)
            .status(status)
            .reason("reason")
            .headers(Map.of("Content-Type", List.of(contentType)))
            .body(body)
            .build();
    }

    private static byte[] readResource(Resource resource) {
        try {
            return resource.getContentAsByteArray();
        } catch (IOException exception) {
            throw new AssertionError(exception);
        }
    }

    private static ClientHttpResponse createAdapter(Response response) throws Exception {
        Class<?> adapterClass = Class.forName("org.springframework.cloud.openfeign.support.SpringDecoder$FeignResponseAdapter");
        Constructor<?> constructor = adapterClass.getDeclaredConstructor(Response.class);
        constructor.setAccessible(true);
        return (ClientHttpResponse) constructor.newInstance(response);
    }

    @SuppressWarnings("unchecked")
    private static ObjectProvider<FeignHttpMessageConverters> mockFeignConvertersProvider() {
        return (ObjectProvider<FeignHttpMessageConverters>) mock(ObjectProvider.class);
    }

    @SuppressWarnings("unchecked")
    private static ObjectProvider<Object> mockAnyObjectProvider() {
        return (ObjectProvider<Object>) mock(ObjectProvider.class);
    }

    private static final class TestBody implements Response.Body {

        private final InputStream inputStream;
        private final boolean failOnClose;
        private boolean closed;

        private TestBody(InputStream inputStream, boolean failOnClose) {
            this.inputStream = inputStream;
            this.failOnClose = failOnClose;
        }

        @Override
        public Integer length() {
            return null;
        }

        @Override
        public boolean isRepeatable() {
            return false;
        }

        @Override
        public InputStream asInputStream() {
            return inputStream;
        }

        @Override
        public java.io.Reader asReader(java.nio.charset.Charset charset) {
            return new java.io.InputStreamReader(inputStream, charset);
        }

        @Override
        public void close() throws IOException {
            closed = true;
            if (failOnClose) {
                throw new IOException("close failed");
            }
            inputStream.close();
        }
    }
}
