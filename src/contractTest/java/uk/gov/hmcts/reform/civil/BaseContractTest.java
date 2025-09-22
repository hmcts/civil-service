package uk.gov.hmcts.reform.civil;

import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONException;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;

@ExtendWith(PactConsumerTestExt.class)
@ActiveProfiles("integration-test")
@SpringBootTest
public class BaseContractTest {

    protected static final String AUTHORIZATION_HEADER = "Authorization";
    protected static final String SERVICE_AUTHORIZATION_HEADER = "ServiceAuthorization";
    protected static final String AUTHORIZATION_TOKEN = "Bearer some-access-token";
    protected static final String SERVICE_AUTH_TOKEN = "someServiceAuthToken";

    @Autowired
    protected ObjectMapper objectMapper;

    protected String createJsonObject(Object obj) throws JSONException, IOException {
        return objectMapper.writeValueAsString(obj);
    }
}
