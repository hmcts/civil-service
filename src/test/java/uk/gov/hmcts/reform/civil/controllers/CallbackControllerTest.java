package uk.gov.hmcts.reform.civil.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.callback.CallbackHandlerFactory;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CallbackVersion;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.service.http.HttpResponseHeadersService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class CallbackControllerTest {

    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private final CallbackHandlerFactory factory = mock(CallbackHandlerFactory.class);
    private final HttpResponseHeadersService headers = mock(HttpResponseHeadersService.class);
    private final CallbackController controller = new CallbackController(factory, new CaseDetailsConverter(mapper), headers);

    @ParameterizedTest
    @ValueSource(strings = {"--18", "--00", "--14", "2026 -09-09", "--08", "20226-10-12", "2026-`10-19",
        "--11", "-09-28/09/2026", "--26", "--02", "-11-03", "-02-02"})
    void shouldRejectMalformedDateWithoutDispatchingOrChangingData(String date) {
        CaseDetails details = details(Map.of("hearingDate", date));
        var response = invoke(details, null);

        assertThat(response.getErrors()).hasSize(1);
        assertThat(response.getErrors().getFirst()).contains("hearingDate", "Invalid date");
        assertThat(details.getData()).containsEntry("hearingDate", date);
        verifyNoInteractions(factory, headers);
    }

    @ParameterizedTest
    @ValueSource(strings = {"generalAppUrgencyRequirement", "generalAppHearingDetails",
        "respondent1DQHearingSmallClaim", "resp1MediationAvailability"})
    void shouldRejectNestedMalformedDates(String parent) {
        Map<String, Object> nested = switch (parent) {
            case "generalAppUrgencyRequirement" -> Map.of("urgentAppConsiderationDate", "--08");
            case "generalAppHearingDetails" -> Map.of("trialDateFrom", "-11-03");
            case "respondent1DQHearingSmallClaim" -> Map.of("smallClaimUnavailableDate",
                List.of(Map.of("value", Map.of("date", "--00"))));
            default -> Map.of("unavailableDatesForMediation",
                List.of(Map.of("value", Map.of("fromDate", "--14"))));
        };
        assertThat(invoke(details(Map.of(parent, nested)), null).getErrors()).hasSize(1);
        verifyNoInteractions(factory);
    }

    @Test
    void shouldRejectMalformedGeneralApplicationDate() {
        CaseDetails details = CaseDetails.builder().id(1234567890123456L).caseTypeId("GENERALAPPLICATION")
            .data(Map.of("generalAppUrgencyRequirement", Map.of("urgentAppConsiderationDate", "2026-`10-19")))
            .build();
        assertThat(invoke(details, null).getErrors().getFirst()).contains("urgentAppConsiderationDate");
        verifyNoInteractions(factory);
    }

    @Test
    void shouldRejectMalformedDateInPreviousData() {
        assertThat(invoke(details(Map.of()), details(Map.of("hearingDate", "--00"))).getErrors()).hasSize(1);
        verifyNoInteractions(factory);
    }

    @ParameterizedTest
    @ValueSource(strings = {"2026-09-09", "2026-09-09T00:00:00.000Z"})
    void shouldPreserveStockDateParsing(String date) {
        when(factory.dispatch(any())).thenReturn(AboutToStartOrSubmitCallbackResponse.builder().build());

        assertThat(invoke(details(Map.of("hearingDate", date)), null).getErrors()).isNullOrEmpty();
        verify(factory).dispatch(any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"2026-09-09", "2026-09-09T00:00:00.000Z"})
    void shouldRetainUnavailableDatesFromCui(String date) {
        when(factory.dispatch(any())).thenReturn(AboutToStartOrSubmitCallbackResponse.builder().build());
        invoke(details(Map.of("respondent1DQHearingSmallClaim", Map.of("smallClaimUnavailableDate",
            List.of(Map.of("value", Map.of("date", date, "fromDate", date, "toDate", date)))))), null);

        ArgumentCaptor<CallbackParams> params = ArgumentCaptor.forClass(CallbackParams.class);
        verify(factory).dispatch(params.capture());
        var unavailable = ((CaseData) params.getValue().getCaseData()).getRespondent1DQ().getRespondent1DQHearingSmallClaim()
            .getSmallClaimUnavailableDate().getFirst().getValue();
        assertThat(unavailable.getDate()).isEqualTo(LocalDate.of(2026, 9, 9));
        assertThat(unavailable.getFromDate()).isEqualTo(unavailable.getDate());
        assertThat(unavailable.getToDate()).isEqualTo(unavailable.getDate());
    }

    @Test
    void shouldAcceptDateFieldsFromSavedGaSnapshot() {
        // Date-related subset of the supplied saved GA; this is not the failed callback payload.
        var details = CaseDetails.builder().id(1234567890123456L).caseTypeId("GENERALAPPLICATION")
            .data(Map.of("generalAppHearingDetails", Map.of(
                "hearingYesorNo", "No", "trialRequiredYesOrNo", "Yes", "trialDateFrom", "2026-10-20"),
                "generalAppUrgencyRequirement", Map.of(
                    "generalAppUrgency", "Yes", "urgentAppConsiderationDate", "2026-10-20")))
            .build();
        when(factory.dispatch(any())).thenReturn(AboutToStartOrSubmitCallbackResponse.builder().build());

        assertThat(invoke(details, null).getErrors()).isNullOrEmpty();
        ArgumentCaptor<CallbackParams> params = ArgumentCaptor.forClass(CallbackParams.class);
        verify(factory).dispatch(params.capture());
        var hearing = params.getValue().getGeneralApplicationCaseData().getGeneralAppHearingDetails();
        assertThat(hearing.getHearingDate()).isNull();
        assertThat(hearing.getTrialDateFrom()).isEqualTo(LocalDate.of(2026, 10, 20));
    }

    @Test
    void shouldKeepSubmittedFailuresVisible() {
        CallbackRequest request = CallbackRequest.builder()
            .caseDetails(details(Map.of("hearingDate", "--00"))).build();
        Optional<CallbackVersion> version = Optional.empty();
        Optional<String> pageId = Optional.empty();

        assertThatThrownBy(() -> controller.callback(null, "test", "submitted", request, version, pageId))
            .isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(factory);
    }

    @Test
    void shouldNotHideUnrelatedConversionErrors() {
        CaseDetails caseDetails = details(Map.of("hearingDate", Map.of("bad", "value")));

        assertThatThrownBy(() -> invoke(caseDetails, null))
            .isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(factory);
    }

    private AboutToStartOrSubmitCallbackResponse invoke(CaseDetails current, CaseDetails previous) {
        return (AboutToStartOrSubmitCallbackResponse) controller.callback(
            null, "test", "about-to-submit",
            CallbackRequest.builder().caseDetails(current).caseDetailsBefore(previous).build(),
            Optional.empty(), Optional.empty());
    }

    private CaseDetails details(Map<String, Object> data) {
        return CaseDetails.builder().id(1234567890123456L).caseTypeId("CIVIL").data(data).build();
    }
}
