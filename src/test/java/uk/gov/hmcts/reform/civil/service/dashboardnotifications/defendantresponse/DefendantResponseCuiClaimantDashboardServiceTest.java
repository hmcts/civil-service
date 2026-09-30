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

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DefendantResponseCuiClaimantDashboardServiceTest {

    private static final String AUTH_TOKEN = "Bearer";

    @Mock
    private DefendantResponseClaimantDashboardService claimantDashboardService;
    @Mock
    private DefendantResponseWelshClaimantDashboardService welshClaimantDashboardService;

    @InjectMocks
    private DefendantResponseCuiClaimantDashboardService dashboardService;

    @Test
    void shouldDelegateToClaimantDashboardServiceWhenClaimantIsNotBilingual() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setClaimantBilingualLanguagePreference(Language.ENGLISH.toString());

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
    void shouldDelegateToWelshClaimantDashboardServiceWhenClaimantBilingual() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setClaimantBilingualLanguagePreference(Language.WELSH.toString());

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
