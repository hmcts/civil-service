package uk.gov.hmcts.reform.civil.handler.callback;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.camunda.bpm.engine.RuntimeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.ContactInformationUpdatedCallbackHandler;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.ContactDetailsUpdatedEvent;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CONTACT_INFORMATION_UPDATED;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@ExtendWith(MockitoExtension.class)
class ContactInformationUpdatedCallbackHandlerTest extends BaseCallbackHandlerTest {

    @Mock
    private RuntimeService runTimeService;

    private ContactInformationUpdatedCallbackHandler handler;

    @BeforeEach
    void setup() {
        ObjectMapper objectMapper = new ObjectMapper();
        handler = new ContactInformationUpdatedCallbackHandler(runTimeService, objectMapper);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldClearContactDetailsUpdatedEventFieldFromCaseData_andSetExpectedCamundaVars(boolean submittedByCaseworker) {
        String processId = "process-id";
        CaseData caseData = CaseData.builder()
            .businessProcess(BusinessProcess.builder().processInstanceId(processId).build())
            .contactDetailsUpdatedEvent(
                ContactDetailsUpdatedEvent.builder()
                    .summary("Summary")
                    .description("Description")
                    .submittedByCaseworker(submittedByCaseworker ? YES : NO)
                    .build()
            ).build();

        CallbackParams params = callbackParamsOf(caseData, CONTACT_INFORMATION_UPDATED, ABOUT_TO_SUBMIT);

        AboutToStartOrSubmitCallbackResponse result = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        verify(runTimeService).setVariable(processId, "submittedByCaseworker", submittedByCaseworker);
        assertNull(result.getData().get("contactDetailsUpdatedEvent"));
    }

    @Test
    void shouldReturnExpectedCamundaActivityId() {
        CaseData caseData = CaseData.builder().build();
        handler.camundaActivityId(callbackParamsOf(caseData, CONTACT_INFORMATION_UPDATED, ABOUT_TO_SUBMIT));
    }

}
