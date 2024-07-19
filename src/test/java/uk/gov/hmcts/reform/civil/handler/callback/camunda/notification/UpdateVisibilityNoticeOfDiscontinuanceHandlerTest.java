package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import org.camunda.bpm.engine.RuntimeService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.settlediscontinue.ConfirmOrderGivesPermission;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;

@SpringBootTest(classes = {
    UpdateVisibilityNoticeOfDiscontinuanceHandler.class,
    JacksonAutoConfiguration.class
})
class UpdateVisibilityNoticeOfDiscontinuanceHandlerTest extends BaseCallbackHandlerTest {

    @MockBean
    private RuntimeService runTimeService;

    @Autowired
    private UpdateVisibilityNoticeOfDiscontinuanceHandler handler;

    private static final String processId = "process-id";

    @Nested
    class AboutToSubmitCallback {

        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        void shouldUpdateCamundaVariables_whenInvoked(Boolean toggleState) {
            //Given
            CaseData caseData = CaseDataBuilder.builder()
                .businessProcess(BusinessProcess.builder().processInstanceId(processId).build()).build();
            caseData.setConfirmOrderGivesPermission(
                toggleState ? ConfirmOrderGivesPermission.YES : ConfirmOrderGivesPermission.NO);

            CallbackParams params = CallbackParams.builder()
                .caseData(caseData)
                .type(ABOUT_TO_SUBMIT)
                .request(CallbackRequest.builder()
                             .eventId(CaseEvent.UPDATE_VISIBILITY_NOTICE_OF_DISCONTINUANCE.name())
                             .build())
                .build();
            //When
            handler.handle(params);
            //Then
            verify(runTimeService).setVariable(processId, "discontinuanceValidationSuccess", toggleState);
        }
    }

}
