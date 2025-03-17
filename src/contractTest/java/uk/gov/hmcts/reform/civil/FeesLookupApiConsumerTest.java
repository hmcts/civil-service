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
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.ClaimValue;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.genapplication.GAApplicationType;
import uk.gov.hmcts.reform.civil.model.genapplication.GAInformOtherParty;
import uk.gov.hmcts.reform.civil.model.genapplication.GARespondentOrderAgreement;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.FeesService;
import uk.gov.hmcts.reform.civil.service.GeneralAppFeesService;
import uk.gov.hmcts.reform.civil.service.hearings.HearingFeesService;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.service.FeesClientService.EVENT_ISSUE;
import static uk.gov.hmcts.reform.civil.service.FeesClientService.MONEY_CLAIM;

@PactTestFor(providerName = "feeRegister_lookUp")
@MockServerConfig(hostInterface = "localhost", port = "6662")
@TestPropertySource(properties = "fees.api.url=http://localhost:6662")
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
    private HearingFeesService hearingFeesService;

    @Autowired
    private FeesService feesService;

    @Autowired
    private GeneralAppFeesService generalAppFeesService;

    @MockBean
    private FeatureToggleService featureToggleService;

    @Pact(consumer = "civil_service")
    public RequestResponsePact getFeeForHearingSmallClaims(PactDslWithProvider builder) throws JSONException {
        return buildLookupFeeWithAmountPact(
            builder,
            "A request for civil-service small claims hearing fees",
            CMC_SERVICE,
            JURISDICTION_CIVIL,
            JURISDICTION_CC,
            CHANNEL,
            HEARING_EVENT,
            SMALL_CLAIMS_KEYWORD,
            "1000",
            new BigDecimal(50.00),
            "FEE0440"
        );
    }

    @Pact(consumer = "civil_service")
    public RequestResponsePact getFeeForHearingFastTrackClaims(PactDslWithProvider builder) throws JSONException {
        return buildLookupFeeWithAmountPact(
            builder,
            "A request for civil-service fast track hearing fees",
            CMC_SERVICE,
            JURISDICTION_CIVIL,
            JURISDICTION_CIVIL,
            CHANNEL,
            HEARING_EVENT,
            FAST_TRACK_KEYWORD,
            "1000",
            new BigDecimal(60.00),
            "FEE0441"
        );
    }

    @Pact(consumer = "civil_service")
    public RequestResponsePact getFeeForHearingMultiClaims(PactDslWithProvider builder) throws JSONException {
        return buildLookupFeeWithAmountPact(
            builder,
            "A request for civil-service multi track hearing fees",
            CMC_SERVICE,
            JURISDICTION_CIVIL,
            JURISDICTION_CIVIL,
            CHANNEL,
            HEARING_EVENT,
            MULTI_TRACK_KEYWORD,
            "1000",
            new BigDecimal(70.00),
            "FEE0442"
        );
    }

    @Pact(consumer = "civil_service")
    public RequestResponsePact getFeeForMoneyClaim(PactDslWithProvider builder) throws JSONException {
        return buildLookupFeeWithAmountPact(
            builder,
            "A request for civil-service money claims fees",
            CMC_SERVICE,
            JURISDICTION_CIVIL,
            JURISDICTION_CC,
            CHANNEL,
            EVENT_ISSUE,
            MONEY_CLAIM,
            "1000.00",
            new BigDecimal(80.00),
            "FEE0443"
        );
    }

    @Pact(consumer = "civil_service")
    public RequestResponsePact getFeeForMoneyClaimWithoutKeyword(PactDslWithProvider builder) throws JSONException {
        return buildLookupFeeWithoutKeywordPact(
            builder,
            "A request for civil-service money claims fees without the keyword feature",
            CMC_SERVICE,
            JURISDICTION_CIVIL,
            JURISDICTION_CC,
            CHANNEL,
            EVENT_ISSUE,
            "1000.00",
            new BigDecimal(90),
            "FEE0444"
        );
    }

    @Pact(consumer = "civil_service")
    public RequestResponsePact getFeeForGAWithNotice(PactDslWithProvider builder) throws JSONException {
        return buildLookupFeePact(
            builder,
            "A request for general application with notice fee",
            SERVICE_GENERAL,
            JURISDICTION_CIVIL,
            JURISDICTION_CIVIL,
            CHANNEL,
            GENERAL_APP_EVENT,
            WITH_NOTICE_KEYWORD,
            new BigDecimal(100.00),
            "FEE0445"
        );
    }

    @Pact(consumer = "civil_service")
    public RequestResponsePact getFeeForConsentWithOrWithout(PactDslWithProvider builder) throws JSONException {
        return buildLookupFeePact(
            builder,
            "A request for general application consent with or without notice fee",
            SERVICE_GENERAL,
            JURISDICTION_CIVIL,
            JURISDICTION_CIVIL,
            CHANNEL,
            GENERAL_APP_EVENT,
            CONSENT_WITHWITHOUT_NOTICE_KEYWORD,
            new BigDecimal(110.00),
            "FEE0446"
        );
    }

    @Pact(consumer = "civil_service")
    public RequestResponsePact getFeeForAppToVaryOrSuspend(PactDslWithProvider builder) throws JSONException {
        return buildLookupFeePact(
            builder,
            "A request for general application consent with or without notice fee",
            "other",
            JURISDICTION_CIVIL,
            JURISDICTION_CIVIL,
            CHANNEL,
            "miscellaneous",
            APPN_TO_VARY_KEYWORD,
            new BigDecimal(120.00),
            "FEE0447"
        );
    }

    @Test
    @PactTestFor(pactMethod = "getFeeForHearingSmallClaims")
    public void verifyFeeForHearingSmallClaims() {

        Fee fee =
            hearingFeesService.getFeeForHearingSmallClaims(new BigDecimal(1000));
        assertThat(fee.getCode(), is(equalTo("FEE0440")));
        assertThat(fee.getCalculatedAmountInPence(), is(equalTo(new BigDecimal(5000))));
    }

    @Test
    @PactTestFor(pactMethod = "getFeeForHearingFastTrackClaims")
    public void verifyFeeForHearingFastTrackClaims() {

        Fee fee =
            hearingFeesService.getFeeForHearingFastTrackClaims(
                new BigDecimal(1000)
            );

        assertThat(fee.getCode(), is(equalTo("FEE0441")));
        assertThat(fee.getCalculatedAmountInPence(), is(equalTo(new BigDecimal(6000))));
    }

    @Test
    @PactTestFor(pactMethod = "getFeeForHearingMultiClaims")
    public void verifyFeeForHearingMultiClaims() {

        Fee fee =
            hearingFeesService.getFeeForHearingMultiClaims(
                new BigDecimal(1000)
            );
        assertThat(fee.getCode(), is(equalTo("FEE0442")));
        assertThat(fee.getCalculatedAmountInPence(), is(equalTo(new BigDecimal(7000))));
    }

    @Test
    @PactTestFor(pactMethod = "getFeeForMoneyClaimWithoutKeyword")
    public void verifyFeeForMoneyClaimWithoutKeyword() {
        when(featureToggleService.isFeatureEnabled("fee-keywords-enable")).thenReturn(false);
        Fee fee = feesService.getFeeDataByClaimValue(
            ClaimValue.builder().statementOfValueInPennies(new BigDecimal(100000)).build()
        );
        assertThat(fee.getCode(), is(equalTo("FEE0444")));
        assertThat(fee.getCalculatedAmountInPence(), is(equalTo(new BigDecimal(9000))));
    }

    @Test
    @PactTestFor(pactMethod = "getFeeForMoneyClaim")
    public void verifyFeeForMoneyClaim() {
        when(featureToggleService.isFeatureEnabled("fee-keywords-enable")).thenReturn(true);
        when(featureToggleService.isLipVLipEnabled()).thenReturn(false);
        Fee fee = feesService.getFeeDataByClaimValue(
            ClaimValue.builder().statementOfValueInPennies(new BigDecimal(100000)).build()
        );
        assertThat(fee.getCode(), is(equalTo("FEE0443")));
        assertThat(fee.getCalculatedAmountInPence(), is(equalTo(new BigDecimal(8000))));
    }

    @Test
    @PactTestFor(pactMethod = "getFeeForGAWithNotice")
    public void verifyFeeForGAWithNotice() {
        Fee fee = generalAppFeesService.getFeeForGA(
            CaseData.builder().generalAppRespondentAgreement(GARespondentOrderAgreement.builder().hasAgreed(YesOrNo.NO).build())
                .generalAppType(
                    GAApplicationType.builder().types(List.of(GeneralApplicationTypes.SET_ASIDE_JUDGEMENT))
                        .build())
                .build());
        assertThat(fee.getCode(), is(equalTo("FEE0445")));
        assertThat(fee.getCalculatedAmountInPence(), is(equalTo(new BigDecimal(10000))));
    }

    @Test
    @PactTestFor(pactMethod = "getFeeForConsentWithOrWithout")
    public void verifyFeeForConsentWithOrWithout() {
        Fee fee = generalAppFeesService.getFeeForGA(
            CaseData.builder().generalAppType(
                    GAApplicationType.builder().types(List.of(GeneralApplicationTypes.SETTLE_BY_CONSENT))
                        .build())
                .build());
        assertThat(fee.getCode(), is(equalTo("FEE0446")));
        assertThat(fee.getCalculatedAmountInPence(), is(equalTo(new BigDecimal(11000))));
    }

    @Test
    @PactTestFor(pactMethod = "getFeeForAppToVaryOrSuspend")
    public void verifyFeeForAppToVaryOrSuspend() {

        Fee fee = generalAppFeesService.getFeeForGA(
            CaseData.builder().generalAppType(
                    GAApplicationType.builder().types(List.of(GeneralApplicationTypes.VARY_PAYMENT_TERMS_OF_JUDGMENT))
                        .build())
                .build());
        assertThat(fee.getCode(), is(equalTo("FEE0447")));
        assertThat(fee.getCalculatedAmountInPence(), is(equalTo(new BigDecimal(12000))));
    }

    private RequestResponsePact buildLookupFeeWithAmountPact(PactDslWithProvider builder,
                                                             String uponReceiving,
                                                             String service,
                                                             String jurisdiction1,
                                                             String jurisdiction2,
                                                             String channel,
                                                             String event,
                                                             String keyword,
                                                             String claimAmount,
                                                             BigDecimal feeAmount,
                                                             String feeCode) {
        return builder
            .uponReceiving(uponReceiving)
            .path(ENDPOINT)
            .method(HttpMethod.GET.toString())
            .matchQuery("service", service, service)
            .matchQuery("jurisdiction1", jurisdiction1, jurisdiction1)
            .matchQuery("jurisdiction2", jurisdiction2, jurisdiction2)
            .matchQuery("channel", channel, channel)
            .matchQuery("event", event, event)
            .matchQuery("keyword", keyword, keyword)
            .matchQuery("amount_or_volume", claimAmount, claimAmount)
            .willRespondWith()
            .matchHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .body(buildFeesResponseBody(feeCode, feeAmount))
            .status(HttpStatus.SC_OK)
            .toPact();
    }

    private RequestResponsePact buildLookupFeePact(PactDslWithProvider builder,
                                                   String uponReceiving,
                                                   String service,
                                                   String jurisdiction1,
                                                   String jurisdiction2,
                                                   String channel,
                                                   String event,
                                                   String keyword,
                                                   BigDecimal feeAmount,
                                                   String feeCode) {
        return builder
            .uponReceiving(uponReceiving)
            .path(ENDPOINT)
            .method(HttpMethod.GET.toString())
            .matchQuery("service", service, service)
            .matchQuery("jurisdiction1", jurisdiction1, jurisdiction1)
            .matchQuery("jurisdiction2", jurisdiction2, jurisdiction2)
            .matchQuery("channel", channel, channel)
            .matchQuery("event", event, event)
            .matchQuery("keyword", keyword, keyword)
            .willRespondWith()
            .matchHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .body(buildFeesResponseBody(feeCode, feeAmount))
            .status(HttpStatus.SC_OK)
            .toPact();
    }

    private RequestResponsePact buildLookupFeeWithoutKeywordPact(PactDslWithProvider builder,
                                                                 String uponReceiving,
                                                                 String service,
                                                                 String jurisdiction1,
                                                                 String jurisdiction2,
                                                                 String channel,
                                                                 String event,
                                                                 String claimAmount,
                                                                 BigDecimal feeAmount,
                                                                 String feeCode) {
        return builder
            .uponReceiving(uponReceiving)
            .path(ENDPOINT)
            .method(HttpMethod.GET.toString())
            .matchQuery("service", service, service)
            .matchQuery("jurisdiction1", jurisdiction1, jurisdiction1)
            .matchQuery("jurisdiction2", jurisdiction2, jurisdiction2)
            .matchQuery("channel", channel, channel)
            .matchQuery("event", event, event)
            .matchQuery("amount_or_volume", claimAmount, claimAmount)
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
