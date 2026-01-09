package uk.gov.hmcts.reform.civil.service.dashboardnotifications.caseproceedsoffline;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ApplicationsProceedOfflineNotificationServiceTest {

    @Mock
    private ApplicationsProceedOfflineClaimantDashboardService claimantService;
    @Mock
    private ApplicationsProceedOfflineDefendantDashboardService defendantService;

    @InjectMocks
    private ApplicationsProceedOfflineNotificationService service;

    private static CaseData baseCaseData() {
        return CaseDataBuilder.builder().build().toBuilder()
            .ccdState(CaseState.PROCEEDS_IN_HERITAGE_SYSTEM)
            .ccdCaseReference(1234L)
            .build();
    }

    @Test
    void shouldDelegateToClaimantServiceWhenEnabled() {

        service.notifyClaimant(baseCaseData(), "token");

        verify(claimantService).notify(baseCaseData(), "token");
        verify(defendantService, never()).notify(any(), any());
    }

    @Test
    void shouldDelegateToDefendantServiceWhenEnabled() {

        service.notifyDefendant(baseCaseData(), "token");

        verify(defendantService).notify(baseCaseData(), "token");
        verify(claimantService, never()).notify(any(), any());
    }

    @Test
    void shouldSkipWhenCaseNotOffline() {

        CaseData nonProceed = baseCaseData().toBuilder().ccdState(CaseState.All_FINAL_ORDERS_ISSUED).build();

        service.notifyClaimant(nonProceed, "token");
        service.notifyDefendant(nonProceed, "token");

        verify(claimantService, never()).notify(any(), any());
        verify(defendantService, never()).notify(any(), any());
    }

}
