package org.springframework.cloud.openfeign.support;

import feign.RequestTemplate;
import feign.codec.EncodeException;
import feign.codec.Encoder;
import feign.form.spring.SpringFormEncoder;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.BeansException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.GenericHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Compatibility shim for legacy HMCTS clients expecting the old SpringEncoder constructor.
 * Supports both Spring Cloud OpenFeign 5 and older constructor signatures.
 * TODO(DTSCCI-3888): Delete this shim when all dependent libraries use Spring Cloud OpenFeign's
 * native SpringEncoder for Boot 4 and no reflective/legacy constructor fallback is required.
 */
public class SpringEncoder implements Encoder {

    private final SpringFormEncoder springFormEncoder;
    private final FeignEncoderProperties encoderProperties;
    private final ObjectProvider<FeignHttpMessageConverters> convertersProvider;
    private final ObjectFactory<?> legacyConvertersFactory;

    public SpringEncoder(ObjectProvider<FeignHttpMessageConverters> converters) {
        this(new SpringFormEncoder(), new FeignEncoderProperties(), converters);
    }

    public SpringEncoder(SpringFormEncoder springFormEncoder,
                         FeignEncoderProperties encoderProperties,
                         ObjectProvider<FeignHttpMessageConverters> converters) {
        this.springFormEncoder = springFormEncoder;
        this.encoderProperties = encoderProperties;
        this.convertersProvider = converters;
        this.legacyConvertersFactory = null;
    }

    public SpringEncoder(SpringFormEncoder springFormEncoder,
                         ObjectFactory<?> converters,
                         FeignEncoderProperties encoderProperties,
                         ObjectProvider<?> ignoredCustomizerProvider) {
        this.springFormEncoder = springFormEncoder;
        this.encoderProperties = encoderProperties;
        this.convertersProvider = null;
        this.legacyConvertersFactory = converters;
    }

    public SpringEncoder(ObjectFactory<?> converters) {
        this.springFormEncoder = new SpringFormEncoder();
        this.encoderProperties = new FeignEncoderProperties();
        this.convertersProvider = null;
        this.legacyConvertersFactory = converters;
    }

    @Override
    public void encode(Object requestBody, Type bodyType, RequestTemplate request) throws EncodeException {
        if (requestBody == null) {
            request.body(new byte[0], StandardCharsets.UTF_8);
            return;
        }

        MediaType contentType = resolveContentType(request.headers());
        if (isFormRelated(contentType)) {
            springFormEncoder.encode(requestBody, bodyType, request);
            return;
        }

        List<HttpMessageConverter<?>> converters = resolveConverters();
        for (HttpMessageConverter<?> converter : converters) {
            if (!canWrite(converter, requestBody, bodyType, contentType)) {
                continue;
            }
            try {
                FeignOutputMessage output = new FeignOutputMessage();
                write(converter, requestBody, bodyType, contentType, output);
                output.getHeaders().forEach((k, v) -> request.header(k, v.toArray(String[]::new)));
                request.body(output.bodyAsBytes(), StandardCharsets.UTF_8);
                return;
            } catch (IOException ex) {
                throw new EncodeException("Error converting request body", ex);
            }
        }

        new Encoder.Default().encode(requestBody, bodyType, request);
    }

    private List<HttpMessageConverter<?>> resolveConverters() {
        if (convertersProvider != null) {
            try {
                FeignHttpMessageConverters feignConverters = convertersProvider.getObject();
                if (feignConverters != null) {
                    return withJacksonSerializerCompatibility(feignConverters.getConverters());
                }
            } catch (BeansException ex) {
                // Fall back to default encoder when converter beans are not present in slim test contexts.
            }
        }
        if (legacyConvertersFactory != null) {
            try {
                Object legacy = legacyConvertersFactory.getObject();
                if (legacy != null) {
                    Method getConverters = legacy.getClass().getMethod("getConverters");
                    Object value = getConverters.invoke(legacy);
                    if (value instanceof List<?> items) {
                        List<HttpMessageConverter<?>> converted = new ArrayList<>();
                        for (Object item : items) {
                            if (item instanceof HttpMessageConverter<?> converter) {
                                converted.add(converter);
                            }
                        }
                        return withJacksonSerializerCompatibility(converted);
                    }
                }
            } catch (BeansException ex) {
                // Fall back to default encoder when converter beans are not present in slim test contexts.
            } catch (ReflectiveOperationException ex) {
                // Fall back to default encoder when legacy converter container type is unavailable.
            }
        }
        return withJacksonSerializerCompatibility(defaultConverters());
    }

    private static List<HttpMessageConverter<?>> defaultConverters() {
        MappingJackson2HttpMessageConverter jacksonConverter = new MappingJackson2HttpMessageConverter();
        jacksonConverter.getObjectMapper().configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        return List.of(
            new ByteArrayHttpMessageConverter(),
            new StringHttpMessageConverter(StandardCharsets.UTF_8),
            jacksonConverter
        );
    }

    private static List<HttpMessageConverter<?>> withJacksonSerializerCompatibility(List<HttpMessageConverter<?>> converters) {
        if (converters == null) {
            return List.of();
        }
        converters.stream()
            .filter(MappingJackson2HttpMessageConverter.class::isInstance)
            .map(MappingJackson2HttpMessageConverter.class::cast)
            .forEach(converter -> {
                converter.getObjectMapper().configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
                converter.getObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL);
            });
        return converters;
    }

    private static MediaType resolveContentType(Map<String, Collection<String>> headers) {
        Collection<String> contentTypeValues = headers.get(HttpHeaders.CONTENT_TYPE);
        if (contentTypeValues == null || contentTypeValues.isEmpty()) {
            return null;
        }
        return MediaType.parseMediaType(contentTypeValues.iterator().next());
    }

    private static boolean canWrite(HttpMessageConverter<?> converter, Object body, Type bodyType, MediaType contentType) {
        Class<?> bodyClass = body.getClass();
        if (converter instanceof GenericHttpMessageConverter<?> generic) {
            return generic.canWrite(bodyType, bodyClass, contentType);
        }
        return converter.canWrite(bodyClass, contentType);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void write(HttpMessageConverter converter,
                              Object body,
                              Type bodyType,
                              MediaType contentType,
                              FeignOutputMessage output) throws IOException {
        if (converter instanceof GenericHttpMessageConverter<?> generic) {
            ((GenericHttpMessageConverter) generic).write(body, bodyType, contentType, output);
            return;
        }
        converter.write(body, contentType, output);
    }

    private static boolean isFormRelated(MediaType contentType) {
        if (contentType == null) {
            return false;
        }
        return MediaType.APPLICATION_FORM_URLENCODED.includes(contentType)
            || MediaType.MULTIPART_FORM_DATA.includes(contentType)
            || MediaType.MULTIPART_MIXED.includes(contentType)
            || MediaType.MULTIPART_RELATED.includes(contentType);
    }

    @SuppressWarnings("unused")
    protected boolean binaryContentType(FeignOutputMessage outputMessage) {
        if (encoderProperties.isCharsetFromContentType()) {
            return false;
        }
        MediaType contentType = outputMessage.getHeaders().getContentType();
        return contentType != null
            && (MediaType.APPLICATION_OCTET_STREAM.includes(contentType)
            || MediaType.IMAGE_JPEG.includes(contentType)
            || MediaType.IMAGE_PNG.includes(contentType)
            || MediaType.IMAGE_GIF.includes(contentType));
    }

    private static final class FeignOutputMessage implements HttpOutputMessage {

        private final HttpHeaders headers = new HttpHeaders();
        private final ByteArrayOutputStream body = new ByteArrayOutputStream();

        @Override
        public OutputStream getBody() {
            return body;
        }

        @Override
        public HttpHeaders getHeaders() {
            return headers;
        }

        byte[] bodyAsBytes() {
            return body.toByteArray();
        }
    }
}
