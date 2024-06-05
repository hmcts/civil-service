package uk.gov.hmcts.reform.civil;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.Pact;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.model.RequestResponsePact;
import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.civil.config.HearingFeeConfiguration;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.service.hearings.HearingFeesService;

import java.math.BigDecimal;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

@ExtendWith(PactConsumerTestExt.class)
@PactTestFor(providerName = "feeRegister_lookUp", port = "6666")
public class FeesConsumerTest {

    public static final String ENDPOINT = "/fees-register/fees/lookup";
    public static final String CHANNEL = "default";
    public static final String HEARING_EVENT = "hearing";
    public static final String JURISDICTION1 = "civil";
    public static final String SERVICE = "civil money claims";
    public static final String SMALL_CLAIMS_KEYWORD = "HearingSmallClaims";
    public static final String FAST_TRACK_KEYWORD = "FastTrackHrg";
    public static final String MULTI_TRACK_KEYWORD = "MultiTrackHrg";

    private HearingFeesService hearingFeesService = new HearingFeesService(
        new RestTemplate(),
        new HearingFeeConfiguration("http://localhost:6666", ENDPOINT, SERVICE, JURISDICTION1,
                                    "county court", "civil", CHANNEL, HEARING_EVENT,
                                    "hearing", FAST_TRACK_KEYWORD, MULTI_TRACK_KEYWORD,
                                    SMALL_CLAIMS_KEYWORD
        )
    );

    @Pact(consumer = "civil-service")
    public RequestResponsePact getFeeForHearingSmallClaims(PactDslWithProvider builder) throws JSONException {
        return buildRequestResponsePact(builder, "county court", SMALL_CLAIMS_KEYWORD,
                                        "1000", new BigDecimal(50.00), "FEE0001",
                                        "Hearing Fees exist for Civil",
                                        "a request for civil money claims fees"
        );

    }

    @Pact(consumer = "civil-service")
    public RequestResponsePact getFeeForHearingFastTrackClaims(PactDslWithProvider builder) throws JSONException {
        return buildRequestResponsePact(builder, JURISDICTION1, FAST_TRACK_KEYWORD, "1000",
                                        new BigDecimal(70.00), "FEE0002", "Hearing Fees exist for Civil",
                                        "a request for fast track claims fees"
        );
    }

    @Pact(consumer = "civil-service")
    public RequestResponsePact getFeeForHearingMultiClaims(PactDslWithProvider builder) throws JSONException {
        return buildRequestResponsePact(builder, JURISDICTION1, MULTI_TRACK_KEYWORD, "1000",
                                        new BigDecimal(80.00), "FEE0003", "Hearing Fees exist for Civil",
                                        "a request for multi track claims fees"
        );
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


    private RequestResponsePact buildRequestResponsePact(PactDslWithProvider builder, String jurisdiction2,
                                                         String keyword, String amount, BigDecimal feeAmount,
                                                         String feeCode, String given, String uponReceiving) {
        return builder
            .given(given)
            .uponReceiving(uponReceiving)
            .path(ENDPOINT)
            .method("GET")
            .matchQuery("channel", CHANNEL, CHANNEL)
            .matchQuery("event", HEARING_EVENT, HEARING_EVENT)
            .matchQuery("jurisdiction1", JURISDICTION1, JURISDICTION1)
            .matchQuery("jurisdiction2", jurisdiction2, jurisdiction2)
            .matchQuery("service", SERVICE, SERVICE)
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
