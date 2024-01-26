package uk.gov.hmcts.reform.civil.handler.callback;

import org.camunda.bpm.engine.RuntimeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.ContactInformationUpdatedCallbackHandler;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.ContactDetailsUpdatedEvent;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CONTACT_INFORMATION_UPDATED;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@SpringBootTest(classes = {
    ContactInformationUpdatedCallbackHandler.class,
    JacksonAutoConfiguration.class,
    CaseDetailsConverter.class,
})
class ContactInformationUpdatedCallbackHandlerTest extends BaseCallbackHandlerTest {

    @MockBean
    private RuntimeService runTimeService;
    @Autowired
    private ContactInformationUpdatedCallbackHandler handler;

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
