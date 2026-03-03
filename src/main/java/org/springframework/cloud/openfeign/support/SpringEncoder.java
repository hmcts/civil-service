package org.springframework.cloud.openfeign.support;

import feign.RequestTemplate;
import feign.codec.EncodeException;
import feign.codec.Encoder;
import feign.form.spring.SpringFormEncoder;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.GenericHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Compatibility shim for legacy HMCTS clients expecting the old SpringEncoder constructor.
 * Supports both Spring Cloud OpenFeign 5 and older constructor signatures.
 */
public class SpringEncoder implements Encoder {

    private final SpringFormEncoder springFormEncoder;
    private final FeignEncoderProperties encoderProperties;
    private final ObjectProvider<FeignHttpMessageConverters> convertersProvider;
    private final ObjectFactory<HttpMessageConverters> legacyConvertersFactory;

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

    public SpringEncoder(ObjectFactory<HttpMessageConverters> converters) {
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
            FeignHttpMessageConverters feignConverters = convertersProvider.getObject();
            if (feignConverters != null) {
                return feignConverters.getConverters();
            }
        }
        if (legacyConvertersFactory != null) {
            HttpMessageConverters legacy = legacyConvertersFactory.getObject();
            if (legacy != null) {
                return legacy.getConverters();
            }
        }
        return List.of();
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
