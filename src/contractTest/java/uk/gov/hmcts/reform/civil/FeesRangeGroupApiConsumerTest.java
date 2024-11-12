package uk.gov.hmcts.reform.civil;

import au.com.dius.pact.consumer.dsl.DslPart;
import au.com.dius.pact.consumer.dsl.LambdaDslObject;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit.MockServerConfig;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.reform.civil.config.FeesConfiguration;
import uk.gov.hmcts.reform.civil.model.Fee2Dto;
import uk.gov.hmcts.reform.civil.service.FeesClientService;

import static au.com.dius.pact.consumer.dsl.LambdaDsl.newJsonArray;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static uk.gov.hmcts.reform.civil.FeesLookupApiConsumerTest.CHANNEL;
import static uk.gov.hmcts.reform.civil.FeesLookupApiConsumerTest.CMC_SERVICE;
import static uk.gov.hmcts.reform.civil.FeesLookupApiConsumerTest.JURISDICTION_CC;
import static uk.gov.hmcts.reform.civil.FeesLookupApiConsumerTest.JURISDICTION_CIVIL;
import static uk.gov.hmcts.reform.civil.service.FeesClientService.EVENT_ISSUE;

@PactTestFor(providerName = "feeRegister_rangeGroup")
@MockServerConfig(hostInterface = "localhost", port = "6661")
@TestPropertySource(properties = "fees.api.url=http://localhost:6661")
public class FeesRangeGroupApiConsumerTest extends BaseContractTest {

    public static final String ENDPOINT = "/fees-register/fees";

    @Autowired
    private FeesClientService feesClientService;
    @Autowired
    private FeesConfiguration feesConfiguration;

    @Pact(consumer = "civil-service")
    public RequestResponsePact getRangeGroupFees(PactDslWithProvider builder) throws JSONException {
        return buildLookupRangeGroupFeesResponsePact(
            builder,
            "Money Claims Fees exists for Civil",
            "a request for range group fees"
        );
    }

    @Test
    @PactTestFor(pactMethod = "getRangeGroupFees")
    public void verifyRangeGroupFees() {
        Fee2Dto[] fee = feesClientService.findRangeGroup(feesConfiguration.getChannel(), feesConfiguration.getEvent()
        );
        assertThat(fee[0].getCode(), is(equalTo("code")));
    }

    private RequestResponsePact buildLookupRangeGroupFeesResponsePact(PactDslWithProvider builder,
                                                                      String given,
                                                                      String uponReceiving) {
        return builder
            .given(given)
            .uponReceiving(uponReceiving)
            .path(ENDPOINT)
            .method(HttpMethod.GET.toString())
            .matchQuery("service", CMC_SERVICE, CMC_SERVICE)
            .matchQuery("jurisdiction1", JURISDICTION_CIVIL, JURISDICTION_CIVIL)
            .matchQuery("jurisdiction2", JURISDICTION_CC, JURISDICTION_CC)
            .matchQuery("channel", CHANNEL, CHANNEL)
            .matchQuery("event", EVENT_ISSUE, EVENT_ISSUE)
            .matchQuery("feeVersionStatus", "approved", "approved")
            .willRespondWith()
            .matchHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .body(buildRangeGroupFeesResponseBody())
            .status(HttpStatus.SC_OK)
            .toPact();
    }

    private DslPart buildRangeGroupFeesResponseBody() {
        return newJsonArray(response -> response
            .object(feeDto -> feeDto
                .object("applicant_type", applicantType ->
                    applicantType
                        .stringType("name", "name"))
                .object("channel_type", channelTypeDto ->
                    channelTypeDto
                        .stringType("name", "name"))
                .stringType("code", "code")
                .object("current_version", feeVersionDto ->
                    getFeeVersionDto(feeVersionDto))
                .object("event_type", eventType -> eventType
                    .stringType("name", "name"))
                .stringType("fee_type", "FEETYPE")
                .minArrayLike("fee_versions", 1, feeVersions -> getFeeVersionDto(feeVersions))
                .object("jurisdiction1", jurisdiction1 ->
                    jurisdiction1
                        .stringType("name", "name"))
                .object("jurisdiction2", jurisdiction2 ->
                    jurisdiction2
                        .stringType("name", "name"))
                .stringType("keyword", "keyword")
                .object("matching_version", feeVersionDto ->
                    getFeeVersionDto(feeVersionDto))
                .numberType("max_range", "maxRange")
                .numberType("min_range", "minRange")
                .stringType("range_unit", "rangeUnit")
                .object("service_type", serviceType -> serviceType
                    .stringType("name", "name"))
                .booleanType("unspecified_claim_amount")
            )).build();
    }

    private static LambdaDslObject getFeeVersionDto(LambdaDslObject feeVersionDto) {
        return feeVersionDto
            .stringType("approvedBy", "approvedBy")
            .stringType("author", "author")
            .stringType("description", "description")
            .stringType("direction", "direction")
            .object("flat_amount", flatAmount ->
                flatAmount
                    .numberType("amount"))
            .stringType("memo_line", "memoLine")
            .stringType("natural_account_code", "naturalAccountCode")
            .object("percentage_amount", percentageAmount -> percentageAmount
                .numberType("percentage"))
            .stringType("si_ref_id", "siRefId")
            .stringType("status", "status")
            .stringType("statutory_instrument", "statutoryInstrument")
            .stringMatcher("valid_from",
                "^(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{1,6}\\+\\d{2}:\\d{2})$",
                "2015-03-09T00:00:00.000+00:00")
            .stringMatcher("valid_to",
                "^(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{1,6}\\+\\d{2}:\\d{2})$",
                "2022-03-09T00:00:00.000+00:00")
            .numberType("version");
    }
}
