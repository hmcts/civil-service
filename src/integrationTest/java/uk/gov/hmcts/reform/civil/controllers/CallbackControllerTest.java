package uk.gov.hmcts.reform.civil.controllers;

import com.microsoft.applicationinsights.TelemetryClient;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.mock.mockito.MockBean;

import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.BaseIntegrationTest;
import uk.gov.hmcts.reform.civil.sampledata.CaseDetailsBuilder;

import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_CLAIM;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.INITIATE_GENERAL_APPLICATION;

public class CallbackControllerTest extends BaseIntegrationTest {

    private static final String CALLBACK_URL = "/cases/callbacks/{callback-type}";
    private static final String CALLBACK_PAGE_ID_URL = "/cases/callbacks/{callback-type}/{page-id}";
    private static final String CALLBACK_VERSION_URL = "/cases/callbacks/version/{version}/{callback-type}";
    private static final String CALLBACK_VERSION_PAGE_ID_URL =
        "/cases/callbacks/version/{version}/{callback-type}/{page-id}";
    private static final String VALID_CALLBACK_TYPE = "mid";
    private static final String VALID_CALLBACK_TYPE2 = "about-to-start";
    private static final String VALID_CALLBACK_TYPE_VERSION = "about-to-start";
    private static final String INVALID_CALLBACK_TYPE = "invalid-callback-type";
    private static final String VALID_PAGE_ID = "start-claim";
    private static final String INVALID_PAGE_ID = "invalid-page-id";
    private static final String V1 = "V_1";
    private static final String INVALID_VERSION = "b2";

    private CallbackRequest callbackRequest;

    @MockBean
    private TelemetryClient telemetryClient;

    @BeforeEach
    void setup() {
        callbackRequest = CallbackRequest.builder()
            .eventId(CREATE_CLAIM.name())
            .caseDetails(CaseDetailsBuilder.builder().atStateAwaitingRespondentAcknowledgement().build())
            .build();
    }

    @ParameterizedTest
    @ValueSource(strings = {"about-to-start", "mid", "about-to-submit"})
    @SneakyThrows
    void shouldReturnValidationErrorForMalformedDate(String callbackType) {
        callbackRequest.getCaseDetails().getData().put("hearingDate", "--00");

        doPost(BEARER_TOKEN, callbackRequest, CALLBACK_URL, callbackType)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.errors.length()").value(1))
            .andExpect(jsonPath("$.errors[0]", containsString("hearingDate")));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Yes", "No"})
    @SneakyThrows
    void shouldRejectObservedGaHearingDateRegardlessOfVisibility(String hearingYesOrNo) {
        // The logs establish the malformed date, but not the controlling answer at failure.
        callbackRequest.getCaseDetails().getData().put("generalAppHearingDetails",
            Map.of("hearingYesorNo", hearingYesOrNo, "hearingDate", "-10-20"));
        var request = CallbackRequest.builder().eventId(INITIATE_GENERAL_APPLICATION.name())
            .caseDetails(callbackRequest.getCaseDetails()).build();

        doPost(BEARER_TOKEN, request, CALLBACK_URL, "about-to-submit")
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.errors.length()").value(1))
            .andExpect(jsonPath("$.errors[0]", containsString("generalAppHearingDetails.hearingDate")))
            .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    @SneakyThrows
    void shouldReturnValidationErrorForMalformedPreviousDate() {
        var previous = CaseDetailsBuilder.builder().atStateAwaitingRespondentAcknowledgement().build();
        previous.getData().put("hearingDate", "-02-02");
        callbackRequest = CallbackRequest.builder().eventId(CREATE_CLAIM.name())
            .caseDetails(callbackRequest.getCaseDetails()).caseDetailsBefore(previous).build();

        doPost(BEARER_TOKEN, callbackRequest, CALLBACK_URL, "about-to-submit")
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.errors[0]", containsString("hearingDate")));
    }

    @ParameterizedTest
    @ValueSource(strings = {"2026-09-09", "2026-09-09T00:00:00.000Z"})
    @SneakyThrows
    void shouldAcceptStockDateFormatsThroughHttp(String date) {
        callbackRequest.getCaseDetails().getData().put("hearingDate", date);
        callbackRequest.getCaseDetails().getData().put("respondent1DQHearingSmallClaim",
            Map.of("smallClaimUnavailableDate", List.of(Map.of("value",
                Map.of("date", date, "fromDate", date, "toDate", date)))));

        doPost(BEARER_TOKEN, callbackRequest, CALLBACK_PAGE_ID_URL, "mid", "start-claim")
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.errors").doesNotExist())
            .andExpect(jsonPath("$.data.hearingDate").value("2026-09-09"))
            .andExpect(jsonPath("$.data.respondent1DQHearingSmallClaim.smallClaimUnavailableDate[0].value.date")
                .value("2026-09-09"))
            .andExpect(jsonPath("$.data.respondent1DQHearingSmallClaim.smallClaimUnavailableDate[0].value.fromDate")
                .value("2026-09-09"))
            .andExpect(jsonPath("$.data.respondent1DQHearingSmallClaim.smallClaimUnavailableDate[0].value.toDate")
                .value("2026-09-09"));
    }

    @Test
    @SneakyThrows
    void shouldReturnNotFoundWhenCallbackHandlerIsNotImplemented() {
        doPost(BEARER_TOKEN, callbackRequest, CALLBACK_URL, "invalid-callback-type")
            .andExpect(status().isNotFound());
    }

    @Test
    @SneakyThrows
    void shouldReturnOkWithValidCallbackType() {
        doPost(BEARER_TOKEN, callbackRequest, CALLBACK_URL, VALID_CALLBACK_TYPE2)
            .andExpect(status().isOk());
    }

    @Test
    @SneakyThrows
    void shouldReturnOkWithValidCallbackTypeAndPageId() {
        doPost(BEARER_TOKEN, callbackRequest, CALLBACK_PAGE_ID_URL, VALID_CALLBACK_TYPE, VALID_PAGE_ID)
            .andExpect(status().isOk());
    }

    @Test
    @SneakyThrows
    void shouldReturnNotFoundWithInvalidCallbackTypeButValidPage() {
        doPost(BEARER_TOKEN, callbackRequest, CALLBACK_PAGE_ID_URL, INVALID_CALLBACK_TYPE, VALID_PAGE_ID)
            .andExpect(status().isNotFound());
    }

    @Test
    @SneakyThrows
    void shouldReturnNotFoundWithValidCallbackTypeButInvalidPage() {
        doPost(BEARER_TOKEN, callbackRequest, CALLBACK_PAGE_ID_URL, VALID_CALLBACK_TYPE, INVALID_PAGE_ID)
            .andExpect(status().isNotFound());
    }

    @Test
    @SneakyThrows
    void shouldReturnNotFoundWithInvalidCallbackTypeAndPage() {
        doPost(BEARER_TOKEN, callbackRequest, CALLBACK_PAGE_ID_URL, INVALID_CALLBACK_TYPE, INVALID_PAGE_ID)
            .andExpect(status().isNotFound());
    }

    @Test
    @SneakyThrows
    void shouldReturnOkWithValidCallbackTypeAndVersion() {
        doPost(BEARER_TOKEN, callbackRequest, CALLBACK_VERSION_URL, V1, VALID_CALLBACK_TYPE_VERSION)
            .andExpect(status().isOk());
    }

    @Test
    @SneakyThrows
    void shouldReturnBadRequestWithInvalidVersion() {
        doPost(BEARER_TOKEN, callbackRequest, CALLBACK_VERSION_URL, INVALID_VERSION, VALID_CALLBACK_TYPE)
            .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void shouldReturnNotFoundWithValidVersionAndInvalidType() {
        doPost(BEARER_TOKEN, callbackRequest, CALLBACK_VERSION_URL, V1, INVALID_CALLBACK_TYPE)
            .andExpect(status().isNotFound());
    }

    @Test
    @SneakyThrows
    void shouldReturnOkWithAllValidParams() {
        doPost(BEARER_TOKEN, callbackRequest, CALLBACK_VERSION_PAGE_ID_URL, V1, VALID_CALLBACK_TYPE, VALID_PAGE_ID)
            .andExpect(status().isOk());
    }

    @Test
    @SneakyThrows
    void shouldReturnBadRequestWithInvalidVersionOnly() {
        doPost(BEARER_TOKEN, callbackRequest, CALLBACK_VERSION_PAGE_ID_URL, INVALID_VERSION, VALID_CALLBACK_TYPE, VALID_PAGE_ID)
            .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void shouldReturnBadRequestWithNullBody() {
        doPost(BEARER_TOKEN, null, CALLBACK_PAGE_ID_URL, VALID_CALLBACK_TYPE, VALID_PAGE_ID)
            .andExpect(status().isBadRequest());
    }
}
