package uk.gov.hmcts.reform.civil;

import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
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
import uk.gov.hmcts.reform.civil.model.FeeLookupResponseDto;

import java.math.BigDecimal;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static uk.gov.hmcts.reform.civil.service.FeesClientService.EVENT_ISSUE;
import static uk.gov.hmcts.reform.civil.service.FeesClientService.MONEY_CLAIM;

@PactTestFor(providerName = "feeRegister_lookUp")
@MockServerConfig(hostInterface = "localhost", port = "6665")
public class FeesLookupApiConsumerTest extends BaseContractTest {

    public static final String ENDPOINT = "/fees-register/fees/lookup";
    public static final String CHANNEL = "default";
    public static final String HEARING_EVENT = "hearing";
    public static final String CMC_SERVICE = "civil money claims";
    public static final String SMALL_CLAIMS_KEYWORD = "HearingSmallClaims";
    public static final String FAST_TRACK_KEYWORD = "FastTrackHrg";
    public static final String MULTI_TRACK_KEYWORD = "MultiTrackHrg";
    private static final String GENERAL_APP_EVENT = "general application";
    private static final String WITH_NOTICE_KEYWORD = "GAOnNotice";
    private static final String CONSENT_WITHWITHOUT_NOTICE_KEYWORD = "GeneralAppWithoutNotice";
    private static final String APPN_TO_VARY_KEYWORD = "AppnToVaryOrSuspend";
    public static final String SERVICE_GENERAL = "general";
    public static final String JURISDICTION_CIVIL = "civil";
    public static final String JURISDICTION_CC = "county court";

    @Autowired
    private FeesApiClient feesApiClient;

    @Pact(consumer = "civil-service")
    public RequestResponsePact getFeeForMoneyClaimWithoutKeyword(PactDslWithProvider builder) throws JSONException {
        return buildLookupFeeRequestWithoutKeywordResponsePact(builder, JURISDICTION_CC, "1000",
                                                               new BigDecimal(80.00), "FEE0033",
                                                               "Money Claims Fees exists for Civil",
                                                               "a request for money claims fees without keyword",
                                                               EVENT_ISSUE
        );
    }

    @Pact(consumer = "civil-service")
    public RequestResponsePact getFeeForMoneyClaim(PactDslWithProvider builder) throws JSONException {
        return buildLookupFeeRequestWithAmountResponsePact(builder, JURISDICTION_CIVIL, MONEY_CLAIM, "1000",
                                                           new BigDecimal(80.00), "FEE0023",
                                                           "Money Claims Fees exists for Civil",
                                                           "a request for money claims fees", EVENT_ISSUE
        );
    }

    @Pact(consumer = "civil-service")
    public RequestResponsePact getFeeForGAWithNotice(PactDslWithProvider builder) throws JSONException {
        return buildGenAppFeeRequestResponsePact(builder, "a request for GA with notice",
                                                 WITH_NOTICE_KEYWORD, GENERAL_APP_EVENT, SERVICE_GENERAL,
                                                 new BigDecimal(10.00), "FEE0011"
        );
    }

    @Pact(consumer = "civil-service")
    public RequestResponsePact getFeeForConsentWithOrWithout(PactDslWithProvider builder) throws JSONException {
        return buildGenAppFeeRequestResponsePact(builder, "a request for GA Consent with or without notice",
                                                 CONSENT_WITHWITHOUT_NOTICE_KEYWORD, GENERAL_APP_EVENT, SERVICE_GENERAL,
                                                 new BigDecimal(20.00), "FEE0012"
        );
    }

    @Pact(consumer = "civil-service")
    public RequestResponsePact getFeeForAppToVaryOrSuspend(PactDslWithProvider builder) throws JSONException {
        return buildGenAppFeeRequestResponsePact(builder, "a request for GA App to vary or to suspend",
                                                 APPN_TO_VARY_KEYWORD, "miscellaneous", "other",
                                                 new BigDecimal(30.00), "FEE0013"
        );
    }

    @Pact(consumer = "civil-service")
    public RequestResponsePact getFeeForHearingSmallClaims(PactDslWithProvider builder) throws JSONException {
        return buildHearingFeeRequestResponsePact(builder, JURISDICTION_CC, SMALL_CLAIMS_KEYWORD,
                                                  "1000", new BigDecimal(50.00), "FEE0001",
                                                  "Hearing Fees exist for Civil",
                                                  "a request for civil money claims fees"
        );
    }

    @Pact(consumer = "civil-service")
    public RequestResponsePact getFeeForHearingFastTrackClaims(PactDslWithProvider builder) throws JSONException {
        return buildHearingFeeRequestResponsePact(builder, JURISDICTION_CIVIL, FAST_TRACK_KEYWORD, "1000",
                                                  new BigDecimal(70.00), "FEE0002", "Hearing Fees exists for Civil",
                                                  "a request for fast track claims fees"
        );
    }

    @Pact(consumer = "civil-service")
    public RequestResponsePact getFeeForHearingMultiClaims(PactDslWithProvider builder) throws JSONException {
        return buildHearingFeeRequestResponsePact(builder, JURISDICTION_CC, MULTI_TRACK_KEYWORD, "1000",
                                                  new BigDecimal(80.00), "FEE0003", "Hearing Fees exists for Civil",
                                                  "a request for multi track claims fees"
        );
    }

    @Test
    @PactTestFor(pactMethod = "getFeeForMoneyClaimWithoutKeyword")
    public void verifyFeeForMoneyClaimWithoutKeyword() {
        FeeLookupResponseDto fee = feesApiClient.lookupFeeWithoutKeyword(
            CMC_SERVICE,
            JURISDICTION_CIVIL,
            JURISDICTION_CC,
            CHANNEL,
            EVENT_ISSUE,
            new BigDecimal(1000)
        );
        assertThat(fee.getCode(), is(equalTo("FEE0033")));
        assertThat(fee.getFeeAmount(), is(equalTo(new BigDecimal(80.00))));
    }

    @Test
    @PactTestFor(pactMethod = "getFeeForMoneyClaim")
    public void verifyFeeForMoneyClaim() {
        FeeLookupResponseDto fee = feesApiClient.lookupFeeWithAmount(
            CMC_SERVICE,
            JURISDICTION_CIVIL,
            JURISDICTION_CIVIL,
            CHANNEL,
            EVENT_ISSUE,
            MONEY_CLAIM,
            new BigDecimal(1000)
        );
        assertThat(fee.getCode(), is(equalTo("FEE0023")));
        assertThat(fee.getFeeAmount(), is(equalTo(new BigDecimal(80.00))));
    }

    @Test
    @PactTestFor(pactMethod = "getFeeForGAWithNotice")
    public void verifyFeeForGAWithNotice() {

        FeeLookupResponseDto fee = feesApiClient.lookupFee(
            SERVICE_GENERAL,
            JURISDICTION_CIVIL,
            JURISDICTION_CIVIL,
            CHANNEL,
            GENERAL_APP_EVENT,
            WITH_NOTICE_KEYWORD
        );
        assertThat(fee.getCode(), is(equalTo("FEE0011")));
        assertThat(fee.getFeeAmount(), is(equalTo(new BigDecimal(10.00))));
    }

    @Test
    @PactTestFor(pactMethod = "getFeeForConsentWithOrWithout")
    public void verifyFeeForConsentWithOrWithout() {
        FeeLookupResponseDto fee = feesApiClient.lookupFee(
            SERVICE_GENERAL,
            JURISDICTION_CIVIL,
            JURISDICTION_CIVIL,
            CHANNEL,
            GENERAL_APP_EVENT,
            CONSENT_WITHWITHOUT_NOTICE_KEYWORD
        );
        assertThat(fee.getCode(), is(equalTo("FEE0012")));
        assertThat(fee.getFeeAmount(), is(equalTo(new BigDecimal(20.00))));
    }

    @Test
    @PactTestFor(pactMethod = "getFeeForAppToVaryOrSuspend")
    public void verifyFeeForAppToVaryOrSuspend() {

        FeeLookupResponseDto fee = feesApiClient.lookupFee(
            "other",
            JURISDICTION_CIVIL,
            JURISDICTION_CIVIL,
            CHANNEL,
            "miscellaneous",
            APPN_TO_VARY_KEYWORD
        );
        assertThat(fee.getCode(), is(equalTo("FEE0013")));
        assertThat(fee.getFeeAmount(), is(equalTo(new BigDecimal(30.00))));
    }

    @Test
    @PactTestFor(pactMethod = "getFeeForHearingSmallClaims")
    public void verifyFeeForHearingSmallClaims() {

        FeeLookupResponseDto fee =
            feesApiClient.lookupFeeWithAmount(
                CMC_SERVICE,
                JURISDICTION_CIVIL,
                JURISDICTION_CC,
                CHANNEL,
                HEARING_EVENT,
                SMALL_CLAIMS_KEYWORD,
                new BigDecimal(1000)
            );
        assertThat(fee.getCode(), is(equalTo("FEE0001")));
        assertThat(fee.getFeeAmount(), is(equalTo(new BigDecimal(50))));
    }

    @Test
    @PactTestFor(pactMethod = "getFeeForHearingFastTrackClaims")
    public void verifyFeeForHearingFastTrackClaims() {

        FeeLookupResponseDto fee =
            feesApiClient.lookupFeeWithAmount(
                CMC_SERVICE,
                JURISDICTION_CIVIL,
                JURISDICTION_CIVIL,
                CHANNEL,
                HEARING_EVENT,
                FAST_TRACK_KEYWORD,
                new BigDecimal(1000)
            );

        assertThat(fee.getCode(), is(equalTo("FEE0002")));
        assertThat(fee.getFeeAmount(), is(equalTo(new BigDecimal(70))));
    }

    @Test
    @PactTestFor(pactMethod = "getFeeForHearingMultiClaims")
    public void verifyFeeForHearingMultiClaims() {

        FeeLookupResponseDto fee =
            feesApiClient.lookupFeeWithAmount(
                CMC_SERVICE,
                JURISDICTION_CIVIL,
                JURISDICTION_CC,
                CHANNEL,
                HEARING_EVENT,
                MULTI_TRACK_KEYWORD,
                new BigDecimal(1000)
            );
        assertThat(fee.getCode(), is(equalTo("FEE0003")));
        assertThat(fee.getFeeAmount(), is(equalTo(new BigDecimal(80))));
    }

    private RequestResponsePact buildGenAppFeeRequestResponsePact(PactDslWithProvider builder, String uponReceiving,
                                                                  String keyword, String event, String service,
                                                                  BigDecimal feeAmount, String feeCode) {
        return builder
            .given("General Application fees exist")
            .uponReceiving(uponReceiving)
            .path(ENDPOINT)
            .method(HttpMethod.GET.toString())
            .matchQuery("channel", CHANNEL, CHANNEL)
            .matchQuery("event", event, event)
            .matchQuery("jurisdiction1", JURISDICTION_CIVIL, JURISDICTION_CIVIL)
            .matchQuery("jurisdiction2", JURISDICTION_CIVIL, JURISDICTION_CIVIL)
            .matchQuery("service", service, service)
            .matchQuery("keyword", keyword, keyword)
            .willRespondWith()
            .matchHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .body(buildFeesResponseBody(feeCode, feeAmount))
            .status(HttpStatus.SC_OK)
            .toPact();
    }

    private RequestResponsePact buildLookupFeeRequestWithoutKeywordResponsePact(PactDslWithProvider builder,
                                                                                String jurisdiction2,
                                                                                String amount,
                                                                                BigDecimal feeAmount,
                                                                                String feeCode,
                                                                                String given,
                                                                                String uponReceiving,
                                                                                String event) {
        return builder
            .given(given)
            .uponReceiving(uponReceiving)
            .path(ENDPOINT)
            .method(HttpMethod.GET.toString())
            .matchQuery("channel", CHANNEL, CHANNEL)
            .matchQuery("event", event, event)
            .matchQuery("jurisdiction1", JURISDICTION_CIVIL, JURISDICTION_CIVIL)
            .matchQuery("jurisdiction2", jurisdiction2, jurisdiction2)
            .matchQuery("service", CMC_SERVICE, CMC_SERVICE)
            .matchQuery("amount_or_volume", amount, amount)
            .willRespondWith()
            .matchHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .body(buildFeesResponseBody(feeCode, feeAmount))
            .status(HttpStatus.SC_OK)
            .toPact();
    }

    private RequestResponsePact buildLookupFeeRequestWithAmountResponsePact(PactDslWithProvider builder, String jurisdiction2,
                                                                            String keyword, String amount, BigDecimal feeAmount,
                                                                            String feeCode, String given, String uponReceiving,
                                                                            String event) {
        return builder
            .given(given)
            .uponReceiving(uponReceiving)
            .path(ENDPOINT)
            .method(HttpMethod.GET.toString())
            .matchQuery("channel", CHANNEL, CHANNEL)
            .matchQuery("event", event, event)
            .matchQuery("jurisdiction1", JURISDICTION_CIVIL, JURISDICTION_CIVIL)
            .matchQuery("jurisdiction2", jurisdiction2, jurisdiction2)
            .matchQuery("service", CMC_SERVICE, CMC_SERVICE)
            .matchQuery("keyword", keyword, keyword)
            .matchQuery("amount_or_volume", amount, amount)
            .willRespondWith()
            .matchHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .body(buildFeesResponseBody(feeCode, feeAmount))
            .status(HttpStatus.SC_OK)
            .toPact();
    }

    private RequestResponsePact buildHearingFeeRequestResponsePact(PactDslWithProvider builder, String jurisdiction2,
                                                                   String keyword, String amount, BigDecimal feeAmount,
                                                                   String feeCode, String given, String uponReceiving) {
        return builder
            .given(given)
            .uponReceiving(uponReceiving)
            .path(ENDPOINT)
            .method(HttpMethod.GET.toString())
            .matchQuery("channel", CHANNEL, CHANNEL)
            .matchQuery("event", HEARING_EVENT, HEARING_EVENT)
            .matchQuery("jurisdiction1", JURISDICTION_CIVIL, JURISDICTION_CIVIL)
            .matchQuery("jurisdiction2", jurisdiction2, jurisdiction2)
            .matchQuery("service", CMC_SERVICE, CMC_SERVICE)
            .matchQuery("keyword", keyword, keyword)
            .matchQuery("amount_or_volume", amount, amount)
            .willRespondWith()
            .matchHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .body(buildFeesResponseBody(feeCode, feeAmount))
            .status(HttpStatus.SC_OK)
            .toPact();
    }

    private PactDslJsonBody buildFeesResponseBody(String feeCode, BigDecimal feeAmount) {
        return new PactDslJsonBody()
            .stringType("code", feeCode)
            .stringType("description", "Fee Description")
            .numberType("version", 1)
            .decimalType("fee_amount", feeAmount);
    }
}
