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
import uk.gov.hmcts.reform.civil.client.FeesApiClient;
import uk.gov.hmcts.reform.civil.model.Fee2Dto;

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
@MockServerConfig(hostInterface = "localhost", port = "6666")
public class FeesRangeGroupApiConsumerTest extends BaseContractTest {

    public static final String ENDPOINT = "/fees-register/fees";

    @Autowired
    private FeesApiClient feesApiClient;

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
        Fee2Dto[] fee = feesApiClient.findRangeGroup(
            CMC_SERVICE,
            JURISDICTION_CIVIL,
            JURISDICTION_CC,
            CHANNEL,
            EVENT_ISSUE
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
                .object("applicantType", applicantType ->
                    applicantType
                        .date("creationTime", "yyyy-MM-dd'T'HH:mm:ss'Z'")
                        .date("lastUpdated", "yyyy-MM-dd'T'HH:mm:ss'Z'")
                        .stringType("name", "name"))
                .object("channelType", channelTypeDto ->
                    channelTypeDto
                        .date("creationTime", "yyyy-MM-dd'T'HH:mm:ss'Z'")
                        .date("lastUpdated", "yyyy-MM-dd'T'HH:mm:ss'Z'")
                        .stringType("name", "name"))
                .stringType("code", "code")
                .object("currentVersion", feeVersionDto ->
                    getFeeVersionDto(feeVersionDto))
                .object("eventType", eventType -> eventType
                    .date("creationTime", "yyyy-MM-dd'T'HH:mm:ss'Z'")
                    .date("lastUpdated", "yyyy-MM-dd'T'HH:mm:ss'Z'")
                    .stringType("name", "name"))
                .stringType("feeType", "FEETYPE")
                .minArrayLike("feeVersions", 1, feeVersions -> getFeeVersionDto(feeVersions))
                .object("jurisdiction1", jurisdiction1 ->
                    jurisdiction1
                        .stringType("name", "name"))
                .object("jurisdiction2", jurisdiction2 ->
                    jurisdiction2
                        .stringType("name", "name"))
                .stringType("keyword", "keyword")
                .object("matchingVersion", feeVersionDto ->
                    getFeeVersionDto(feeVersionDto))
                .numberType("maxRange", "maxRange")
                .numberType("minRange", "minRange")
                .stringType("rangeUnit", "rangeUnit")
                .object("serviceType", serviceType -> serviceType
                    .date("creationTime", "yyyy-MM-dd'T'HH:mm:ss'Z'")
                    .date("lastUpdated", "yyyy-MM-dd'T'HH:mm:ss'Z'")
                    .stringType("name", "name"))
                .booleanType("unspecifiedClaimAmount")
            )).build();
    }

    private static LambdaDslObject getFeeVersionDto(LambdaDslObject feeVersionDto) {
        return feeVersionDto
            .stringType("approvedBy", "approvedBy")
            .stringType("author", "author")
            .stringType("description", "description")
            .stringType("direction", "direction")
            .object("flatAmount", flatAmount ->
                flatAmount
                    .numberType("amount"))
            .stringType("memoLine", "memoLine")
            .stringType("naturalAccountCode", "naturalAccountCode")
            .object("percentageAmount", percentageAmount -> percentageAmount
                .numberType("percentage"))
            .stringType("siRefId", "siRefId")
            .stringType("status", "status")
            .stringType("statutoryInstrument", "statutoryInstrument")
            .date("validFrom", "yyyy-MM-dd'T'HH:mm:ss'Z'")
            .date("validTo", "yyyy-MM-dd'T'HH:mm:ss'Z'")
            .numberType("version")
            .object("volumeAmount", volumeAmount -> volumeAmount
                .numberType("amount"));
    }
}
