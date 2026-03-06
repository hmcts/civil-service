package org.springframework.cloud.openfeign.support;

import feign.FeignException;
import feign.Response;
import feign.Util;
import feign.codec.DecodeException;
import feign.codec.Decoder;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.HttpMessageConverterExtractor;
import org.springframework.web.client.RestClientException;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Compatibility shim for dependencies still calling the legacy SpringDecoder constructor
 * shape while running with Spring Cloud OpenFeign 5.
 * TODO(DTSCCI-3888): Remove once all transitive clients use Boot 4/OpenFeign 5 APIs.
 */
public class SpringDecoder implements Decoder {

    private final ObjectProvider<FeignHttpMessageConverters> convertersProvider;
    private final ObjectFactory<?> legacyConvertersFactory;

    public SpringDecoder(ObjectProvider<FeignHttpMessageConverters> converters) {
        this.convertersProvider = converters;
        this.legacyConvertersFactory = null;
    }

    public SpringDecoder(ObjectFactory<?> converters, ObjectProvider<?> ignoredCustomizerProvider) {
        this.convertersProvider = null;
        this.legacyConvertersFactory = converters;
    }

    @Override
    public Object decode(Response response, Type type) throws IOException, FeignException {
        if (type == Response.class) {
            return response;
        }
        if (response.status() == 404 || response.status() == 204) {
            return Util.emptyValueOf(type);
        }
        if (response.body() == null) {
            return null;
        }

        HttpMessageConverterExtractor<?> extractor =
            new HttpMessageConverterExtractor<>(type, resolveConverters());
        try {
            return extractor.extractData(new FeignResponseAdapter(response));
        } catch (RestClientException ex) {
            throw new DecodeException(response.status(), ex.getMessage(), response.request(), ex);
        }
    }

    private List<HttpMessageConverter<?>> resolveConverters() {
        if (convertersProvider != null) {
            try {
                FeignHttpMessageConverters feignConverters = convertersProvider.getObject();
                if (feignConverters != null) {
                    return feignConverters.getConverters();
                }
            } catch (BeansException ex) {
                // Fall through to legacy/default converters.
            }
        }

        if (legacyConvertersFactory != null) {
            try {
                Object legacy = legacyConvertersFactory.getObject();
                if (legacy != null) {
                    Object value = legacy.getClass().getMethod("getConverters").invoke(legacy);
                    if (value instanceof List<?> items) {
                        List<HttpMessageConverter<?>> converted = new ArrayList<>();
                        for (Object item : items) {
                            if (item instanceof HttpMessageConverter<?> converter) {
                                converted.add(converter);
                            }
                        }
                        if (!converted.isEmpty()) {
                            return converted;
                        }
                    }
                }
            } catch (ReflectiveOperationException | BeansException ex) {
                // Fall through to default converters.
            }
        }

        return List.of(
            new ByteArrayHttpMessageConverter(),
            new StringHttpMessageConverter(StandardCharsets.UTF_8),
            new MappingJackson2HttpMessageConverter()
        );
    }

    private static final class FeignResponseAdapter implements ClientHttpResponse {

        private final Response response;

        private FeignResponseAdapter(Response response) {
            this.response = response;
        }

        @Override
        public HttpStatusCode getStatusCode() {
            return HttpStatusCode.valueOf(response.status());
        }

        @Override
        public String getStatusText() {
            return response.reason();
        }

        @Override
        public void close() {
            if (response.body() != null) {
                try {
                    response.body().close();
                } catch (IOException ignored) {
                    // Ignore close failures to match typical ClientHttpResponse behavior.
                }
            }
        }

        @Override
        public InputStream getBody() throws IOException {
            return response.body() == null ? InputStream.nullInputStream() : response.body().asInputStream();
        }

        @Override
        public HttpHeaders getHeaders() {
            HttpHeaders headers = new HttpHeaders();
            for (Map.Entry<String, java.util.Collection<String>> entry : response.headers().entrySet()) {
                headers.put(entry.getKey(), new ArrayList<>(entry.getValue()));
            }
            return headers;
        }
    }
}
