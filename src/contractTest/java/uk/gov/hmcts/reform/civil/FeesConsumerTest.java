package uk.gov.hmcts.reform.civil;

import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit.MockServerConfig;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.PactSpecVersion;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.hmcts.reform.civil.config.HearingFeeConfiguration;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.service.hearings.HearingFeesService;
import uk.gov.hmcts.reform.fees.client.model.FeeLookupResponseDto;

import javax.ws.rs.HttpMethod;
import java.math.BigDecimal;
import java.net.URI;
import java.util.HashMap;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

@ExtendWith(PactConsumerTestExt.class)
@PactTestFor(providerName = "feeRegister_lookUp")
@MockServerConfig(hostInterface = "localhost", port = "6666")
public class FeesConsumerTest {

    public static final String ENDPOINT = "/fees-register/fees/lookup";
    public static final String CHANNEL = "default";
    public static final String HEARING_EVENT = "hearing";
    public static final String JURISDICTION1 = "civil";
    public static final String HEARING_SERVICE = "civil money claims";
    public static final String SMALL_CLAIMS_KEYWORD = "HearingSmallClaims";
    public static final String FAST_TRACK_KEYWORD = "FastTrackHrg";
    public static final String MULTI_TRACK_KEYWORD = "MultiTrackHrg";
    private static final String GENERAL_APP_EVENT = "general application";
    private static final String WITH_NOTICE_KEYWORD = "GAOnNotice";
    private static final String CONSENT_WITHWITHOUT_NOTICE_KEYWORD = "GeneralAppWithoutNotice";
    private static final String APPN_TO_VARY_KEYWORD = "AppnToVaryOrSuspend";
    public static final String SERVICE_GENERAL = "general";
    public static final String JURISDICTION_CIVIL = "civil";

    private HearingFeesService hearingFeesService = new HearingFeesService(
        new RestTemplate(),
        new HearingFeeConfiguration("http://localhost:6666", ENDPOINT, HEARING_SERVICE, JURISDICTION1,
                                    "county court", JURISDICTION_CIVIL, CHANNEL, HEARING_EVENT,
                                    "hearing", FAST_TRACK_KEYWORD, MULTI_TRACK_KEYWORD,
                                    SMALL_CLAIMS_KEYWORD
        )
    );

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
        return buildHearingFeeRequestResponsePact(builder, "county court", SMALL_CLAIMS_KEYWORD,
                                                  "1000", new BigDecimal(50.00), "FEE0001",
                                                  "Hearing Fees exist for Civil",
                                                  "a request for civil money claims fees"
        );
    }

    @Pact(consumer = "civil-service")
    public RequestResponsePact getFeeForHearingFastTrackClaims(PactDslWithProvider builder) throws JSONException {
        return buildHearingFeeRequestResponsePact(builder, JURISDICTION1, FAST_TRACK_KEYWORD, "1000",
                                                  new BigDecimal(70.00), "FEE0002", "Hearing Fees exists for Civil",
                                                  "a request for fast track claims fees"
        );
    }

    @Pact(consumer = "civil-service")
    public RequestResponsePact getFeeForHearingMultiClaims(PactDslWithProvider builder) throws JSONException {
        return buildHearingFeeRequestResponsePact(builder, JURISDICTION1, MULTI_TRACK_KEYWORD, "1000",
                                                  new BigDecimal(80.00), "FEE0003", "Hearing Fees exists for Civil",
                                                  "a request for multi track claims fees"
        );
    }

    @Test
    @PactTestFor(pactMethod = "getFeeForGAWithNotice")
    public void verifyFeeForGAWithNotice() {

        FeeLookupResponseDto fee = getFeeForGA(WITH_NOTICE_KEYWORD, GENERAL_APP_EVENT, SERVICE_GENERAL);
        assertThat(fee.getCode(), is(equalTo("FEE0011")));
        assertThat(fee.getFeeAmount(), is(equalTo(new BigDecimal(10.00))));
    }

    @Test
    @PactTestFor(pactMethod = "getFeeForConsentWithOrWithout")
    public void verifyFeeForConsentWithOrWithout() {

        FeeLookupResponseDto fee = getFeeForGA(CONSENT_WITHWITHOUT_NOTICE_KEYWORD, GENERAL_APP_EVENT, SERVICE_GENERAL);
        assertThat(fee.getCode(), is(equalTo("FEE0012")));
        assertThat(fee.getFeeAmount(), is(equalTo(new BigDecimal(20.00))));
    }

    @Test
    @PactTestFor(pactMethod = "getFeeForAppToVaryOrSuspend")
    public void verifyFeeForAppToVaryOrSuspend() {

        FeeLookupResponseDto fee = getFeeForGA(APPN_TO_VARY_KEYWORD, "miscellaneous", "other");
        assertThat(fee.getCode(), is(equalTo("FEE0013")));
        assertThat(fee.getFeeAmount(), is(equalTo(new BigDecimal(30.00))));
    }

    @Test
    @PactTestFor(pactMethod = "getFeeForHearingSmallClaims")
    public void verifyFeeForHearingSmallClaims() {

        Fee fee = hearingFeesService
            .getFeeForHearingSmallClaims(new BigDecimal(1000));
        assertThat(fee.getCode(), is(equalTo("FEE0001")));
        assertThat(fee.getCalculatedAmountInPence(), is(equalTo(new BigDecimal(5000))));
    }

    @Test
    @PactTestFor(pactMethod = "getFeeForHearingFastTrackClaims")
    public void verifyFeeForHearingFastTrackClaims() {

        Fee fee = hearingFeesService
            .getFeeForHearingFastTrackClaims(new BigDecimal(1000));
        assertThat(fee.getCode(), is(equalTo("FEE0002")));
        assertThat(fee.getCalculatedAmountInPence(), is(equalTo(new BigDecimal(7000))));
    }

    @Test
    @PactTestFor(pactMethod = "getFeeForHearingMultiClaims")
    public void verifyFeeForHearingMultiClaims() {

        Fee fee = hearingFeesService
            .getFeeForHearingMultiClaims(new BigDecimal(1000));
        assertThat(fee.getCode(), is(equalTo("FEE0003")));
        assertThat(fee.getCalculatedAmountInPence(), is(equalTo(new BigDecimal(8000))));
    }

    private RequestResponsePact buildGenAppFeeRequestResponsePact(PactDslWithProvider builder, String uponReceiving,
                                                                  String keyword, String event, String service,
                                                                  BigDecimal feeAmount, String feeCode) {
        return builder
            .given("General Application fees exist")
            .uponReceiving(uponReceiving)
            .path(ENDPOINT)
            .method(HttpMethod.GET)
            .matchQuery("channel", CHANNEL, CHANNEL)
            .matchQuery("event", event, event)
            .matchQuery("jurisdiction1", JURISDICTION1, JURISDICTION1)
            .matchQuery("jurisdiction2", JURISDICTION_CIVIL, JURISDICTION_CIVIL)
            .matchQuery("service", service, service)
            .matchQuery("keyword", keyword, keyword)
            .willRespondWith()
            .matchHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .body(buildFeesResponseBody(feeCode, feeAmount))
            .status(HttpStatus.SC_OK)
            .toPact();
    }

    private FeeLookupResponseDto getFeeForGA(String keyword, String event, String service) {

        String queryURL = "http://localhost:6666" + ENDPOINT;
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(queryURL)
            .queryParam("channel", CHANNEL)
            .queryParam("event", event)
            .queryParam("jurisdiction1", JURISDICTION1)
            .queryParam("jurisdiction2", JURISDICTION_CIVIL)
            .queryParam("service", service)
            .queryParam("keyword", keyword);

        URI uri;
        FeeLookupResponseDto feeLookupResponseDto;
        try {
            uri = builder.buildAndExpand(new HashMap<>()).toUri();


            RestTemplate restTemplate = new RestTemplate();
            feeLookupResponseDto = restTemplate.getForObject(uri, FeeLookupResponseDto.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        if (feeLookupResponseDto == null || feeLookupResponseDto.getFeeAmount() == null) {
            throw new RuntimeException("No Fees returned by fee-service while creating General Application");
        }
        return feeLookupResponseDto;
    }

    private RequestResponsePact buildHearingFeeRequestResponsePact(PactDslWithProvider builder, String jurisdiction2,
                                                                   String keyword, String amount, BigDecimal feeAmount,
                                                                   String feeCode, String given, String uponReceiving) {
        return builder
            .given(given)
            .uponReceiving(uponReceiving)
            .path(ENDPOINT)
            .method(HttpMethod.GET)
            .matchQuery("channel", CHANNEL, CHANNEL)
            .matchQuery("event", HEARING_EVENT, HEARING_EVENT)
            .matchQuery("jurisdiction1", JURISDICTION1, JURISDICTION1)
            .matchQuery("jurisdiction2", jurisdiction2, jurisdiction2)
            .matchQuery("service", HEARING_SERVICE, HEARING_SERVICE)
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
