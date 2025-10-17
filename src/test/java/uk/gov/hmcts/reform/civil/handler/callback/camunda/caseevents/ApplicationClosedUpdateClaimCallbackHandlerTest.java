package uk.gov.hmcts.reform.civil.handler.callback.camunda.caseevents;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.GeneralApplicationDetailsBuilder;
import uk.gov.hmcts.reform.civil.service.GenAppStateHelperService;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.APPLICATION_CLOSED_UPDATE_CLAIM;

@ExtendWith(MockitoExtension.class)
class ApplicationClosedUpdateClaimCallbackHandlerTest extends BaseCallbackHandlerTest {

    @Mock
    private GenAppStateHelperService helperService;

    private ApplicationClosedUpdateClaimCallbackHandler handler;
    private ObjectMapper objectMapper;

    private static final String APPLICATION_CLOSED_DESCRIPTION = "Application Closed";

    @BeforeEach
    void prepare() {
        objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .registerModule(new Jdk8Module());
        handler = new ApplicationClosedUpdateClaimCallbackHandler(helperService, objectMapper);
    }

    @Test
    void handleEventsReturnsTheExpectedCallbackEvent() {
        assertThat(handler.handledEvents()).contains(APPLICATION_CLOSED_UPDATE_CLAIM);
    }

    @Test
    void callHelperServiceToUpdateApplicationDetailsInClaimWhenGeneralApplicationsPresent() {
        CaseData caseData = GeneralApplicationDetailsBuilder.builder()
            .getTestCaseDataWithDetails(CaseData.builder().build(),
                                        true,
                                        true,
                                        true, true,
                                        getOriginalStatusOfGeneralApplication_test1()
            );
        GeneralApplicationCaseData expectedGa = toGaCaseData(caseData);
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

        when(helperService.updateApplicationDetailsInClaim(
            any(GeneralApplicationCaseData.class),
            eq(APPLICATION_CLOSED_DESCRIPTION),
            eq(GenAppStateHelperService.RequiredState.APPLICATION_CLOSED)
        ))
            .thenReturn(caseData);

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        assertThat(response.getErrors()).isNull();
        verify(helperService, times(1)).updateApplicationDetailsInClaim(
            expectedGa,
            APPLICATION_CLOSED_DESCRIPTION,
            GenAppStateHelperService.RequiredState.APPLICATION_CLOSED
        );
        verifyNoMoreInteractions(helperService);
    }

    @Test
    void noCallToHelperServiceToUpdateApplicationDetailsInClaimWhenNoGeneralApplicationsPresent() {
        CaseData caseData = GeneralApplicationDetailsBuilder.builder()
            .getTestCaseDataWithDetails(CaseData.builder().build(),
                                        false,
                                        false,
                                        false, false,
                                        Map.of()
            );
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        assertThat(response.getErrors()).isNull();

        verifyNoInteractions(helperService);
    }

    @Test
    void returnErrorIfHelperServiceThrowsErrors() {
        CaseData caseData = GeneralApplicationDetailsBuilder.builder()
            .getTestCaseDataWithDetails(CaseData.builder().build(),
                                        true,
                                        true,
                                        true, true,
                                        getOriginalStatusOfGeneralApplication_test1()
            );
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

        when(helperService.updateApplicationDetailsInClaim(any(GeneralApplicationCaseData.class), any(), any()))
            .thenThrow(new RuntimeException("Some Error"));

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        assertThat(response.getErrors()).isNotNull();
        assertThat(response.getErrors())
            .contains("Error occurred while updating claim with application status: " + "Some Error");
        verify(helperService, times(1)).updateApplicationDetailsInClaim(
            toGaCaseData(caseData),
            APPLICATION_CLOSED_DESCRIPTION,
            GenAppStateHelperService.RequiredState.APPLICATION_CLOSED
        );
        verifyNoMoreInteractions(helperService);
    }

    @Test
    void callHelperServiceWithGaCaseDataWhenOnlyGaPayloadPresent() {
        CaseData caseData = GeneralApplicationDetailsBuilder.builder()
            .getTestCaseDataWithDetails(CaseData.builder().build(),
                true,
                true,
                true, true,
                getOriginalStatusOfGeneralApplication_test1()
            );
        GeneralApplicationCaseData gaCaseData = toGaCaseData(caseData);
        CallbackParams params = gaCallbackParamsOf(gaCaseData, ABOUT_TO_SUBMIT);

        when(helperService.updateApplicationDetailsInClaim(
            any(GeneralApplicationCaseData.class),
            eq(APPLICATION_CLOSED_DESCRIPTION),
            eq(GenAppStateHelperService.RequiredState.APPLICATION_CLOSED)
        )).thenReturn(caseData);

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        assertThat(response.getErrors()).isNull();
        verify(helperService, times(1)).updateApplicationDetailsInClaim(
            eq(gaCaseData),
            eq(APPLICATION_CLOSED_DESCRIPTION),
            eq(GenAppStateHelperService.RequiredState.APPLICATION_CLOSED)
        );
        verifyNoMoreInteractions(helperService);
    }

    private Map<String, String> getOriginalStatusOfGeneralApplication_test1() {
        Map<String, String> latestStatus = new HashMap<>();
        latestStatus.put("1234", "Application Submitted - Awaiting Judicial Decision");
        latestStatus.put("2345", "Order Made");
        latestStatus.put("3456", "Awaiting Respondent Response");
        latestStatus.put("4567", "Directions Order Made");
        latestStatus.put("5678", "Awaiting Written Representations");
        latestStatus.put("6789", "Additional Information Require");
        latestStatus.put("7890", "Application Dismissed");
        latestStatus.put("8910", "Proceeds In Heritage");
        latestStatus.put("1011", "Listed for a Hearing");

        return latestStatus;
    }
}
