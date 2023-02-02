package uk.gov.hmcts.reform.civil.handler.callback.camunda.caseevents;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.verification.VerificationMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.GeneralApplicationDetailsBuilder;
import uk.gov.hmcts.reform.civil.service.GenAppStateHelperService;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.APPLICATION_OFFLINE_UPDATE_CLAIM;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    ApplicationOfflineUpdateClaimCallbackHandler.class, JacksonAutoConfiguration.class
})
class ApplicationOfflineUpdateClaimCallbackHandlerTest extends BaseCallbackHandlerTest {

    @Autowired
    private ApplicationOfflineUpdateClaimCallbackHandler handler;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private GenAppStateHelperService helperService;

    private static final String APPLICATION_PROCEEDS_OFFLINE_DESCRIPTION = "Proceeds In Heritage";
    private static final VerificationMode ONCE = Mockito.times(1);

    @BeforeEach
    void prepare() {
        ReflectionTestUtils.setField(handler, "objectMapper", new ObjectMapper().registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS));
    }

    @Test
    void handleEventsReturnsTheExpectedCallbackEvent() {
        assertThat(handler.handledEvents()).contains(APPLICATION_OFFLINE_UPDATE_CLAIM);
    }

    @Test
    public void callHelperServiceToUpdateApplicationDetailsInClaimWhenGeneralApplicationsPresent() {
        CaseData caseData = GeneralApplicationDetailsBuilder.builder()
            .getTestCaseDataWithDetails(CaseData.builder().build(),
                                        true,
                                        true,
                                        true, true,
                                        getOriginalStatusOfGeneralApplication_test1()
            );
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

        when(helperService.updateApplicationDetailsInClaim(
            any(),
            eq(APPLICATION_PROCEEDS_OFFLINE_DESCRIPTION),
            eq(GenAppStateHelperService.RequiredState.APPLICATION_PROCEEDS_OFFLINE)
        ))
            .thenReturn(caseData);

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        assertThat(response.getErrors()).isNull();
        verify(helperService, ONCE).updateApplicationDetailsInClaim(
            caseData,
            APPLICATION_PROCEEDS_OFFLINE_DESCRIPTION,
            GenAppStateHelperService.RequiredState.APPLICATION_PROCEEDS_OFFLINE
        );
        verifyNoMoreInteractions(helperService);
    }

    @Test
    public void noCallToHelperServiceToUpdateApplicationDetailsInClaimWhenNoGeneralApplicationsPresent() {
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
    public void returnErrorIfHelperServiceThrowsErrors() {
        CaseData caseData = GeneralApplicationDetailsBuilder.builder()
            .getTestCaseDataWithDetails(CaseData.builder().build(),
                                        true,
                                        true,
                                        true, true,
                                        getOriginalStatusOfGeneralApplication_test1()
            );
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

        when(helperService.updateApplicationDetailsInClaim(any(), any(), any()))
            .thenThrow(new RuntimeException("Some Error"));

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        assertThat(response.getErrors()).isNotNull();
        assertThat(response.getErrors())
            .contains("Error occurred while updating claim with application status: " + "Some Error");
        verify(helperService, ONCE).updateApplicationDetailsInClaim(
            caseData,
            APPLICATION_PROCEEDS_OFFLINE_DESCRIPTION,
            GenAppStateHelperService.RequiredState.APPLICATION_PROCEEDS_OFFLINE
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
        latestStatus.put("1112", "Application Closed");

        return latestStatus;
    }
}

