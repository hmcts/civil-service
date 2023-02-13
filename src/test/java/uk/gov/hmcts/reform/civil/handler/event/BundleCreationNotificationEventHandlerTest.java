package uk.gov.hmcts.reform.civil.handler.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.event.BundleCreationTriggerEvent;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.BundleCreationApplicantNotificationHandler;
import uk.gov.hmcts.reform.civil.notification.BundleCreationRespondentNotificationHandler;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDetailsBuilder;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class BundleCreationNotificationEventHandlerTest {

    @Mock
    private CoreCaseDataService coreCaseDataService;

    @Mock
    private CaseDetailsConverter caseDetailsConverter;
    @Mock
    private BundleCreationApplicantNotificationHandler applicantNotificationHandler;
    @Mock
    private BundleCreationRespondentNotificationHandler respondentNotificationHandler;
    @InjectMocks
    private BundleCreationTriggerEventHandler handler;

    @Test
    void shouldNotifyAllSolicitors() {
        //given: case details
        Long caseId = 1633357679902210L;
        CaseData caseData = new CaseDataBuilder().atStateClaimDraft()
            .build();
        CaseDetails caseDetails = CaseDetailsBuilder.builder().data(caseData).build();
        when(coreCaseDataService.getCase(caseId)).thenReturn(caseDetails);
        when(caseDetailsConverter.toCaseData(caseDetails)).thenReturn(caseData);
        BundleCreationTriggerEvent event = new BundleCreationTriggerEvent(caseId);
        //when: Evidence upload Notification handler is called
        handler.sendBundleCreationTriggerNotification(event);
        //then: Should call notification handler for all solicitors
        verify(applicantNotificationHandler).notifyApplicantBundleCreation(caseData);
        verify(respondentNotificationHandler).notifyRespondentBundleCreation(caseData, false);
        verify(respondentNotificationHandler).notifyRespondentBundleCreation(caseData, true);
    }

    @Test
    void shouldContinueRespondentNotificationIfApplicantFailed() {
        //given: case details
        Long caseId = 1633357679902210L;
        CaseData caseData = new CaseDataBuilder().atStateClaimDraft()
            .build();
        CaseDetails caseDetails = CaseDetailsBuilder.builder().data(caseData).build();
        //when: Exception is thrown from applicant notification handler
        when(coreCaseDataService.getCase(caseId)).thenReturn(caseDetails);
        when(caseDetailsConverter.toCaseData(caseDetails)).thenReturn(caseData);
        doThrow(new RuntimeException()).when(applicantNotificationHandler).notifyApplicantBundleCreation(caseData);
        BundleCreationTriggerEvent event = new BundleCreationTriggerEvent(caseId);
        handler.sendBundleCreationTriggerNotification(event);
        //then: Respondent handler should be called for both solicitors
        verify(respondentNotificationHandler).notifyRespondentBundleCreation(caseData, true);
        verify(respondentNotificationHandler).notifyRespondentBundleCreation(caseData, false);
    }

    @Test
    void shouldContinueRespondent2IfRespondent1Failed() {
        //given: case details
        Long caseId = 1633357679902210L;
        CaseData caseData = new CaseDataBuilder().atStateClaimDraft()
            .build();
        CaseDetails caseDetails = CaseDetailsBuilder.builder().data(caseData).build();
        //when: Exception is thrown for repondent1 notification handler
        when(coreCaseDataService.getCase(caseId)).thenReturn(caseDetails);
        when(caseDetailsConverter.toCaseData(caseDetails)).thenReturn(caseData);
        doThrow(new RuntimeException()).when(respondentNotificationHandler).notifyRespondentBundleCreation(caseData,
                                                                                                           true);
        BundleCreationTriggerEvent event = new BundleCreationTriggerEvent(caseId);
        handler.sendBundleCreationTriggerNotification(event);
        //then: Applicant handler and Respondent handler (for respondent2) should be called
        verify(applicantNotificationHandler).notifyApplicantBundleCreation(caseData);
        verify(respondentNotificationHandler).notifyRespondentBundleCreation(caseData, false);
    }
}
