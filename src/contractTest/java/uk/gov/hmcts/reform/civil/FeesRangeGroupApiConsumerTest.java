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

import java.math.BigDecimal;

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

    @Pact(consumer = "civil_service")
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
        assertThat(fee[0].getCode(), is(equalTo("FEE0209")));
        assertThat(fee[0].getFeeType(), is(equalTo("FEETYPE")));
        assertThat(fee[0].getKeyword(), is(equalTo("MoneyClaim")));
        assertThat(fee[0].getMinRange(), is(equalTo(BigDecimal.valueOf(100))));
        assertThat(fee[0].getMaxRange(), is(equalTo(BigDecimal.valueOf(100))));
        assertThat(fee[0].getRangeUnit(), is(equalTo("GBP")));
        assertThat(fee[0].getUnspecifiedClaimAmount(), is(equalTo(true)));
        assertThat(fee[0].getFeeVersions().get(0).getVersion(), is(equalTo(2)));
        assertThat(fee[0].getFeeVersions().get(0).getAuthor(), is(equalTo("124756")));
        assertThat(fee[0].getFeeVersions().get(0).getApprovedBy(), is(equalTo("39907")));
        assertThat(fee[0].getFeeVersions().get(0).getDescription(), is(equalTo("Counter Claim - 5000.01 up to 10000 GBP")));
        assertThat(fee[0].getFeeVersions().get(0).getStatus(), is(equalTo("approved")));
        assertThat(fee[0].getFeeVersions().get(0).getValidFrom().toString(), is(equalTo("2015-03-09")));
        assertThat(fee[0].getFeeVersions().get(0).getValidTo().toString(), is(equalTo("2022-03-09")));
        assertThat(fee[0].getFeeVersions().get(0).getFlatAmount().getAmount(), is(equalTo(BigDecimal.valueOf(455))));
        assertThat(fee[0].getFeeVersions().get(0).getMemoLine(), is(equalTo("RECEIPT OF FEES - Civil issue money")));
        assertThat(fee[0].getFeeVersions().get(0).getStatutoryInstrument(), is(equalTo("2014 No 874")));
        assertThat(fee[0].getFeeVersions().get(0).getSiRefId(), is(equalTo("1.1h")));
        assertThat(fee[0].getFeeVersions().get(0).getNaturalAccountCode(), is(equalTo("4481102133")));
        assertThat(fee[0].getFeeVersions().get(0).getPercentageAmount().getPercentage(), is(equalTo(BigDecimal.valueOf(5))));
        assertThat(fee[0].getFeeVersions().get(0).getDirection(), is(equalTo("enhanced")));
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
                        .stringValue("name", "all"))
                .object("channel_type", channelTypeDto ->
                    channelTypeDto
                        .stringValue("name", "default"))
                .stringValue("code", "FEE0209")
                .object("current_version", feeVersionDto ->
                    getFeeVersionDto(feeVersionDto))
                .object("event_type", eventType -> eventType
                    .stringValue("name", "issue"))
                .stringValue("fee_type", "FEETYPE")
                .minArrayLike("fee_versions", 1, feeVersions -> getFeeVersionDto(feeVersions))
                .object("jurisdiction1", jurisdiction1 ->
                    jurisdiction1
                        .stringValue("name", "civil"))
                .object("jurisdiction2", jurisdiction2 ->
                    jurisdiction2
                        .stringValue("name", "county court"))
                .stringValue("keyword", "MoneyClaim")
                .object("matching_version", feeVersionDto ->
                    getFeeVersionDto(feeVersionDto))
                .numberType("max_range", 100)
                .numberType("min_range", 100)
                .stringValue("range_unit", "GBP")
                .object("service_type", serviceType -> serviceType
                    .stringValue("name", "civil money claims"))
                .booleanType("unspecified_claim_amount")
            )).build();
    }

    private static LambdaDslObject getFeeVersionDto(LambdaDslObject feeVersionDto) {

        return feeVersionDto
            .stringType("approvedBy", "39907")
            .stringType("author", "124756")
            .stringType("description", "Counter Claim - 5000.01 up to 10000 GBP")
            .stringType("direction", "enhanced")
            .object("flat_amount", flatAmount ->
                flatAmount
                    .numberType("amount", 455))
            .stringType("memo_line", "RECEIPT OF FEES - Civil issue money")
            .stringType("natural_account_code", "4481102133")
            .object("percentage_amount", percentageAmount -> percentageAmount
                .numberType("percentage", 5))
            .stringType("si_ref_id", "1.1h")
            .stringType("status", "approved")
            .stringType("statutory_instrument", "2014 No 874")
                .date("valid_from", "2015-03-09")
                .date("valid_to", "2022-03-09")
            .numberType("version", 2);
    }
}
