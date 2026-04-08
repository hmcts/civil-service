package uk.gov.hmcts.reform.civil.service.dashboardnotifications.defendantresponse;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.dq.Language;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.RespondentLiPResponse;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefendantResponseCuiClaimantDashboardServiceTest {

    private static final String AUTH_TOKEN = "Bearer";

    @Mock
    private DefendantResponseClaimantDashboardService claimantDashboardService;
    @Mock
    private DefendantResponseWelshClaimantDashboardService welshClaimantDashboardService;
    @Mock
    private FeatureToggleService featureToggleService;

    @InjectMocks
    private DefendantResponseCuiClaimantDashboardService dashboardService;

    @Test
    void shouldDelegateToClaimantDashboardServiceWhenWelshNotEnabledForMainCaseAndIsClaimantBilingual() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setClaimantBilingualLanguagePreference(Language.WELSH.toString());
        when(featureToggleService.isWelshEnabledForMainCase()).thenReturn(false);

        dashboardService.notifyDefendantResponse(caseData, AUTH_TOKEN);

        verify(claimantDashboardService).notifyDefendantResponse(caseData, AUTH_TOKEN);
    }

    @Test
    void shouldDelegateToClaimantDashboardServiceWhenWelshEnabledForMainCaseAndIsClaimantNotBilingual() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setClaimantBilingualLanguagePreference(Language.ENGLISH.toString());
        when(featureToggleService.isWelshEnabledForMainCase()).thenReturn(true);

        dashboardService.notifyDefendantResponse(caseData, AUTH_TOKEN);

        verify(claimantDashboardService).notifyDefendantResponse(caseData, AUTH_TOKEN);
    }

    @Test
    void shouldDelegateToClaimantDashboardServiceWhenRespondentIsNotBilingual() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCaseDataLiP(
            new CaseDataLiP().setRespondent1LiPResponse(
                new RespondentLiPResponse().setRespondent1ResponseLanguage(Language.ENGLISH.toString())));

        dashboardService.notifyDefendantResponse(caseData, AUTH_TOKEN);

        verify(claimantDashboardService).notifyDefendantResponse(caseData, AUTH_TOKEN);
    }

    @Test
    void shouldDelegateToWelshClaimantDashboardServiceWhenWelshIsEnabledForMainCaseAndClaimantBilingual() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setClaimantBilingualLanguagePreference(Language.WELSH.toString());
        when(featureToggleService.isWelshEnabledForMainCase()).thenReturn(true);

        dashboardService.notifyDefendantResponse(caseData, AUTH_TOKEN);

        verify(welshClaimantDashboardService).notifyDefendantResponse(caseData, AUTH_TOKEN);
    }

    @Test
    void shouldDelegateToWelshClaimantDashboardServiceWhenRespondentIsBilingual() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCaseDataLiP(
            new CaseDataLiP().setRespondent1LiPResponse(
                new RespondentLiPResponse().setRespondent1ResponseLanguage(Language.WELSH.toString())));

        dashboardService.notifyDefendantResponse(caseData, AUTH_TOKEN);

        verify(welshClaimantDashboardService).notifyDefendantResponse(caseData, AUTH_TOKEN);
    }

}
