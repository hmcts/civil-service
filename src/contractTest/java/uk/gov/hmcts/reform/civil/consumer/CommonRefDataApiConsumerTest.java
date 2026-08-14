package uk.gov.hmcts.reform.civil.consumer;

import au.com.dius.pact.consumer.dsl.DslPart;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit.MockServerConfig;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.civil.crd.model.CategorySearchResult;
import uk.gov.hmcts.reform.civil.service.CategoryService;

import static au.com.dius.pact.consumer.dsl.LambdaDsl.newJsonBody;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

@PactTestFor(providerName = "rd_commondata_api")
@MockServerConfig(hostInterface = "localhost", port = "6682")
@TestPropertySource(properties = "rd_commondata.api.url=http://localhost:6682")
public class CommonRefDataApiConsumerTest extends BaseContractTest {

    private static final String CATEGORY_ID = "HearingChannel";
    private static final String SERVICE_ID = "AAA7";

    @Autowired
    private CategoryService categoryService;

    @MockBean
    private AuthTokenGenerator authTokenGenerator;

    @BeforeEach
    void setUp() {
        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTH_TOKEN);
    }

    @Pact(consumer = "civil_service")
    public RequestResponsePact getListOfValuesCategory(PactDslWithProvider builder) {
        return builder
            .uponReceiving("a common ref data list of values category request")
            .path("/refdata/commondata/lov/categories/" + CATEGORY_ID)
            .headers(SERVICE_AUTHORIZATION_HEADER, SERVICE_AUTH_TOKEN, AUTHORIZATION_HEADER, AUTHORIZATION_TOKEN)
            .method(HttpMethod.GET.toString())
            .matchQuery("serviceId", "AAA6|AAA7", SERVICE_ID)
            .willRespondWith()
            .matchHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .body(buildCategoryResponse())
            .status(HttpStatus.SC_OK)
            .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "getListOfValuesCategory")
    public void verifyGetListOfValuesCategory() {
        CategorySearchResult response = categoryService
            .findCategoryByCategoryIdAndServiceId(AUTHORIZATION_TOKEN, CATEGORY_ID, SERVICE_ID)
            .orElseThrow();

        assertThat(response.getCategories().size(), is(equalTo(1)));
        assertThat(response.getCategories().get(0).getKey(), is(equalTo("INTER")));
    }

    private DslPart buildCategoryResponse() {
        return newJsonBody(root -> root.minArrayLike("list_of_values", 1, 1, category -> category
            .stringValue("active_flag", "Y")
            .stringValue("category_key", CATEGORY_ID)
            .stringValue("key", "INTER")
            .numberValue("lov_order", 1)
            .stringValue("value_en", "In person")
            .stringValue("value_cy", "Wyneb yn wyneb")
            .minArrayLike("child_nodes", 0, 0, child -> {
            }))).build();
    }
}
