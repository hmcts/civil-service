package uk.gov.hmcts.reform.civil.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.ByteArrayEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.message.BasicClassicHttpResponse;
import org.camunda.bpm.client.impl.EngineRestExceptionDto;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

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
