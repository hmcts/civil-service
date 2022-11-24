package uk.gov.hmcts.reform.civil.handler.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.event.EvidenceUploadNotificationEvent;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.EvidenceUploadApplicantNotificationHandler;
import uk.gov.hmcts.reform.civil.notification.EvidenceUploadRespondentNotificationHandler;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDetailsBuilder;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class EvidenceUploadNotificationEventHandlerTest {

    @Mock
    private CoreCaseDataService coreCaseDataService;

    @Mock
    private CaseDetailsConverter caseDetailsConverter;
    @Mock
    private EvidenceUploadApplicantNotificationHandler applicantNotificationHandler;
    @Mock
    private EvidenceUploadRespondentNotificationHandler respondentNotificationHandler;
    @InjectMocks
    private EvidenceUploadNotificationEventHandler handler;

    @Test
    void shouldNotifyAllSolicitors() {
        Long caseId = 1633357679902210L;
        CaseData caseData = new CaseDataBuilder().atStateClaimDraft()
            .build();
        CaseDetails caseDetails = CaseDetailsBuilder.builder().data(caseData).build();
        when(coreCaseDataService.getCase(caseId)).thenReturn(caseDetails);
        when(caseDetailsConverter.toCaseData(caseDetails)).thenReturn(caseData);
        EvidenceUploadNotificationEvent event = new EvidenceUploadNotificationEvent(caseId);
        handler.sendEvidenceUploadNotification(event);

        verify(applicantNotificationHandler).notifyApplicantEvidenceUpload(caseData);
        verify(respondentNotificationHandler).notifyRespondentEvidenceUpload(caseData, false);
        verify(respondentNotificationHandler).notifyRespondentEvidenceUpload(caseData, true);
    }

    @Test
    void shouldContinueRespondentNotificationIfApplicantFailed() {
        Long caseId = 1633357679902210L;
        CaseData caseData = new CaseDataBuilder().atStateClaimDraft()
            .build();
        CaseDetails caseDetails = CaseDetailsBuilder.builder().data(caseData).build();
        when(coreCaseDataService.getCase(caseId)).thenReturn(caseDetails);
        when(caseDetailsConverter.toCaseData(caseDetails)).thenReturn(caseData);
        doThrow(new RuntimeException()).when(applicantNotificationHandler).notifyApplicantEvidenceUpload(caseData);
        EvidenceUploadNotificationEvent event = new EvidenceUploadNotificationEvent(caseId);
        handler.sendEvidenceUploadNotification(event);

        verify(respondentNotificationHandler).notifyRespondentEvidenceUpload(caseData, true);
        verify(respondentNotificationHandler).notifyRespondentEvidenceUpload(caseData, false);
    }

    @Test
    void shouldContinueRespondent2IfRespondent1Failed() {
        Long caseId = 1633357679902210L;
        CaseData caseData = new CaseDataBuilder().atStateClaimDraft()
            .build();
        CaseDetails caseDetails = CaseDetailsBuilder.builder().data(caseData).build();
        when(coreCaseDataService.getCase(caseId)).thenReturn(caseDetails);
        when(caseDetailsConverter.toCaseData(caseDetails)).thenReturn(caseData);
        doThrow(new RuntimeException()).when(respondentNotificationHandler).notifyRespondentEvidenceUpload(caseData,
                                                                                                           true);
        EvidenceUploadNotificationEvent event = new EvidenceUploadNotificationEvent(caseId);
        handler.sendEvidenceUploadNotification(event);

        verify(applicantNotificationHandler).notifyApplicantEvidenceUpload(caseData);
        verify(respondentNotificationHandler).notifyRespondentEvidenceUpload(caseData, false);
    }

}
