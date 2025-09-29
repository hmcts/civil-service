package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CallbackType;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.service.notification.defendantresponse.caseoffline.applicant.CaseHandledOffLineApplicantSolicitorNotifierFactory;
import uk.gov.hmcts.reform.civil.service.notification.defendantresponse.caseoffline.applicant.CaseHandledOfflineApplicantSolicitorNotifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.DefendantResponseCaseHandedOfflineApplicantNotificationHandler.TASK_ID_APPLICANT1;

@ExtendWith(MockitoExtension.class)
class DefendantResponseCaseHandedOfflineApplicantNotificationHandlerTest extends BaseCallbackHandlerTest {

    @Mock
    CaseHandledOfflineApplicantSolicitorNotifier notifier;
    @Mock
    private CaseHandledOffLineApplicantSolicitorNotifierFactory factory;
    @InjectMocks
    private DefendantResponseCaseHandedOfflineApplicantNotificationHandler handler;

    @Test
    void shouldReturnCorrectCamundaActivityId_whenInvoked() {

        assertThat(handler.camundaActivityId(CallbackParamsBuilder.builder().request(CallbackRequest.builder()
                                                                                         .eventId("NOTIFY_APPLICANT_SOLICITOR1_FOR_CASE_HANDED_OFFLINE").build()).build()))
            .isEqualTo(TASK_ID_APPLICANT1);
    }

    @Test
    void shouldCallNotifier() {
        when(factory.getCaseHandledOfflineSolicitorNotifier(any())).thenReturn(notifier);

        handler.handle(CallbackParams.builder().type(CallbackType.ABOUT_TO_SUBMIT).caseData(CaseData.builder().build()).build());

        verify(notifier).notifyApplicantSolicitorForCaseHandedOffline(any());
    }
}
