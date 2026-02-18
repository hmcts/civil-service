package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.uploadhearingdocuments;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskContext;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.uploadhearingdocuments.UploadHearingDocumentsClaimantService;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UploadHearingDocumentsClaimantDashboardTaskTest {

    @Mock
    private UploadHearingDocumentsClaimantService uploadHearingDocumentsClaimantService;

    @Mock
    private DashboardTaskContext context;

    @InjectMocks
    private UploadHearingDocumentsClaimantDashboardTask uploadHearingDocumentsClaimantDashboardTask;

    @Test
    void shouldDelegateToUploadHearingDocumentsClaimantDashboardTask() {
        CaseData caseData = CaseData.builder().build();
        String authToken = "token";

        when(context.caseData()).thenReturn(caseData);
        when(context.authToken()).thenReturn(authToken);

        uploadHearingDocumentsClaimantDashboardTask.execute(context);

        verify(uploadHearingDocumentsClaimantService).notifyUploadHearingDocuments(caseData, authToken);
        verifyNoMoreInteractions(uploadHearingDocumentsClaimantService);
    }
}
