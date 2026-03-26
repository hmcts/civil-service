package uk.gov.hmcts.reform.civil.consumer;

import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONException;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;

@ExtendWith(PactConsumerTestExt.class)
@ActiveProfiles({"integration-test", "contract-test"})
@SpringBootTest
public class BaseContractTest {

    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String SERVICE_AUTHORIZATION_HEADER = "ServiceAuthorization";
    public static final String DATA_STORE_URL_HEADER = "Data-Store-Url";
    public static final String ROLE_ASSIGNMENT_URL_HEADER = "Role-Assignment-Url";
    public static final String AUTHORIZATION_TOKEN = "Bearer some-access-token";
    public static final String SERVICE_AUTH_TOKEN = "someServiceAuthToken";
    public static final String DATA_STORE_URL = "http://data-store-url";
    public static final String ROLE_ASSIGNMENT_URL = "http://role-assignment-url";

    @Autowired
    protected ObjectMapper objectMapper;

    protected String createJsonObject(Object obj) throws JSONException, IOException {
        return objectMapper.writeValueAsString(obj);
    }
}
