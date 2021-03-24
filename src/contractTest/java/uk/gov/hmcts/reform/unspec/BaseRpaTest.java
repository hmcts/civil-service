package uk.gov.hmcts.reform.unspec;

import au.com.dius.pact.consumer.ConsumerPactBuilder;
import au.com.dius.pact.consumer.PactTestRun;
import au.com.dius.pact.consumer.PactVerificationResult;
import au.com.dius.pact.model.MockProviderConfig;
import au.com.dius.pact.model.RequestResponsePact;
import io.restassured.RestAssured;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;

import java.util.Map;

import static au.com.dius.pact.consumer.ConsumerPactRunnerKt.runConsumerTest;

@Slf4j
public abstract class BaseRpaTest {

    protected static final String VERSION = "1.0.0";
    protected static final String PATH = "/fake-endpoint";
    protected static final String CONSUMER = "civil";
    protected static final String PROVIDER = "rpa_api";

    protected PactVerificationResult getPactVerificationResult(String payload, String description) {
        Map<String, String> headers = Map.of("Content-Type", "application/json",
                                             "title", description,
                                             "version", VERSION);
        RequestResponsePact pact = preparePact(description, payload, headers);
        PactTestRun pactTestRun = preparePactTestRun(payload, headers);

        return runPactTest(pact, pactTestRun);
    }

    protected RequestResponsePact preparePact(String description, String body, Map<String, String> headers) {
        // @formatter:off
        return ConsumerPactBuilder
            .consumer(CONSUMER)
            .hasPactWith(PROVIDER)
            .uponReceiving(description)
                .path(PATH)
                .method("POST")
                .body(body)
                .headers(headers)
            .willRespondWith()
                .status(200)
            .toPact();
        // @formatter:on
    }

    protected PactVerificationResult runPactTest(RequestResponsePact pact, PactTestRun pactTestRun) {
        MockProviderConfig config = MockProviderConfig.createDefault();
        PactVerificationResult result = runConsumerTest(pact, config, pactTestRun);

        if (result instanceof PactVerificationResult.Error) {
            throw new RuntimeException(((PactVerificationResult.Error) result).getError());
        }
        return result;
    }

    protected PactTestRun preparePactTestRun(final String body, final Map<String, String> headers) {
        // @formatter:off
        return mockServer -> RestAssured
            .given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
            .when()
                .headers(headers)
                .body(body)
                .post(mockServer.getUrl() + PATH)
            .then()
                .statusCode(200);
        // @formatter:on
    }
}
