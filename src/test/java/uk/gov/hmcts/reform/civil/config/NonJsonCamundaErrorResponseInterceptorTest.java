package uk.gov.hmcts.reform.civil.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.io.entity.ByteArrayEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.message.BasicClassicHttpResponse;
import org.camunda.bpm.client.impl.EngineRestExceptionDto;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class NonJsonCamundaErrorResponseInterceptorTest {

    private final NonJsonCamundaErrorResponseInterceptor interceptor = new NonJsonCamundaErrorResponseInterceptor();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void rewritesHtmlErrorPageIntoParseableEngineRestExceptionDto() throws Exception {
        ClassicHttpResponse response = response(502, new StringEntity(
            "<html><head><title>502 Bad Gateway</title></head><body><h1>502 Bad Gateway</h1></body></html>",
            ContentType.TEXT_HTML));

        interceptor.process(response, null, null);

        EngineRestExceptionDto dto = parse(response);
        assertThat(dto.getType()).isEqualTo(NonJsonCamundaErrorResponseInterceptor.SYNTHETIC_TYPE);
        assertThat(dto.getMessage()).contains("502 Bad Gateway").endsWith("(HTTP 502)");
        assertThat(response.getEntity().getContentType()).isEqualTo(ContentType.APPLICATION_JSON.toString());
    }

    @Test
    void rewritesPlainTextGatewayError() throws Exception {
        ClassicHttpResponse response = response(504, new StringEntity("Gateway Timeout", ContentType.TEXT_PLAIN));

        interceptor.process(response, null, null);

        assertThat(parse(response).getMessage()).isEqualTo("Gateway Timeout (HTTP 504)");
    }

    @Test
    void rewritesEmptyErrorBodyWithAFallbackMessage() throws Exception {
        ClassicHttpResponse response = response(503, new StringEntity("", ContentType.TEXT_PLAIN));

        interceptor.process(response, null, null);

        assertThat(parse(response).getMessage()).isEqualTo("non-JSON error response (HTTP 503)");
    }

    @Test
    void leavesAJsonErrorBodyUntouched() throws Exception {
        String json = "{\"type\":\"RestException\",\"message\":\"engine says no\"}";
        ClassicHttpResponse response = response(500, new StringEntity(json, ContentType.APPLICATION_JSON));

        interceptor.process(response, null, null);

        EngineRestExceptionDto dto = parse(response);
        assertThat(dto.getType()).isEqualTo("RestException");
        assertThat(dto.getMessage()).isEqualTo("engine says no");
    }

    @Test
    void preservesJsonErrorBodiesLargerThanEightKilobytes() throws Exception {
        String message = "x".repeat(9000);
        String json = "{\"type\":\"RestException\",\"message\":\"" + message + "\"}";
        ClassicHttpResponse response = response(500, new StringEntity(json, ContentType.APPLICATION_JSON));

        interceptor.process(response, null, null);

        String actual = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
        assertThat(actual).isEqualTo(json);
        EngineRestExceptionDto dto = objectMapper.readValue(actual, EngineRestExceptionDto.class);
        assertThat(dto.getType()).isEqualTo("RestException");
        assertThat(dto.getMessage()).isEqualTo(message);
    }

    @Test
    void leavesSuccessResponsesUntouched() throws Exception {
        HttpEntity original = new ByteArrayEntity("[]".getBytes(StandardCharsets.UTF_8), ContentType.APPLICATION_JSON);
        ClassicHttpResponse response = response(200, original);

        interceptor.process(response, null, null);

        assertThat(response.getEntity()).isSameAs(original);
    }

    @Test
    void toleratesAnErrorResponseWithNoBody() throws Exception {
        ClassicHttpResponse response = response(503, null);

        interceptor.process(response, null, null);

        assertThat(response.getEntity()).isNull();
    }

    @Test
    void ignoresResponsesThatAreNotClassicHttpResponses() {
        HttpResponse nonClassicResponse = mock(HttpResponse.class);

        interceptor.process(nonClassicResponse, null, null);

        verifyNoInteractions(nonClassicResponse);
    }

    @Test
    void leavesOriginalEntityWhenTheBodyCannotBeBuffered() throws Exception {
        HttpEntity explodingEntity = mock(HttpEntity.class);
        when(explodingEntity.getContent()).thenThrow(new IOException("boom"));
        ClassicHttpResponse response = response(502, explodingEntity);

        interceptor.process(response, null, null);

        assertThat(response.getEntity()).isSameAs(explodingEntity);
    }

    @Test
    void leavesOriginalEntityWhenTheContentStreamIsNull() throws Exception {
        HttpEntity entityWithNoStream = mock(HttpEntity.class);
        when(entityWithNoStream.getContent()).thenReturn(null);
        ClassicHttpResponse response = response(502, entityWithNoStream);

        interceptor.process(response, null, null);

        assertThat(response.getEntity()).isSameAs(entityWithNoStream);
    }

    @Test
    void treatsWhitespacePrefixedJsonArraysAsJson() throws Exception {
        String json = "  \n[1,2,3]";
        ClassicHttpResponse response = response(500, new StringEntity(json, ContentType.TEXT_PLAIN));

        interceptor.process(response, null, null);

        String actual = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
        assertThat(actual).isEqualTo(json);
        assertThat(response.getEntity().getContentType()).isEqualTo(ContentType.APPLICATION_JSON.toString());
    }

    @Test
    void truncatesLongNonJsonBodiesInTheMessageSnippet() throws Exception {
        String longBody = "x".repeat(250);
        ClassicHttpResponse response = response(502, new StringEntity(longBody, ContentType.TEXT_PLAIN));

        interceptor.process(response, null, null);

        String message = parse(response).getMessage();
        assertThat(message).startsWith("x".repeat(200) + "...").endsWith("(HTTP 502)");
    }

    private static ClassicHttpResponse response(int code, HttpEntity entity) {
        BasicClassicHttpResponse response = new BasicClassicHttpResponse(code);
        response.setEntity(entity);
        return response;
    }

    private EngineRestExceptionDto parse(ClassicHttpResponse response) throws Exception {
        return objectMapper.readValue(
            EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8), EngineRestExceptionDto.class);
    }
}
