package uk.gov.hmcts.reform.civil.ga.service.dashboardnotifications.applicationresponded;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.service.DocUploadDashboardNotificationService;
import uk.gov.hmcts.reform.civil.ga.service.GaForLipService;
import uk.gov.hmcts.reform.civil.model.genapplication.GAApplicationType;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApplicationRespondedDashboardServiceTest {

    private static final String AUTH_TOKEN = "auth-token";

    @Mock
    private DocUploadDashboardNotificationService dashboardNotificationService;

    @Mock
    private GaForLipService gaForLipService;

    @InjectMocks
    private ApplicationRespondedDashboardService service;

    @Test
    void shouldCreateResponseNotificationsForLipAppAndRespWhenNotVaryPaymentTerms() {
        GeneralApplicationCaseData caseData = buildCase(false);
        givenLipFlags(true, true);

        service.notifyApplicationResponded(caseData, AUTH_TOKEN);

        verifyResponseNotifications(caseData, true, true);
    }

    @Test
    void shouldCreateResponseNotificationOnlyForApplicantWhenNotVaryPaymentTermsAndOnlyApplicantLip() {
        GeneralApplicationCaseData caseData = buildCase(false);
        givenLipFlags(true, false);

        service.notifyApplicationResponded(caseData, AUTH_TOKEN);

        verifyResponseNotifications(caseData, true, false);
    }

    @Test
    void shouldCreateResponseNotificationOnlyForRespondentWhenNotVaryPaymentTermsAndOnlyRespondentLip() {
        GeneralApplicationCaseData caseData = buildCase(false);
        givenLipFlags(false, true);

        service.notifyApplicationResponded(caseData, AUTH_TOKEN);

        verifyResponseNotifications(caseData, false, true);
    }

    @Test
    void shouldNotCreateResponseNotificationsWhenNotVaryPaymentTermsAndNoLip() {
        GeneralApplicationCaseData caseData = buildCase(false);
        givenLipFlags(false, false);

        service.notifyApplicationResponded(caseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardNotificationService);
    }

    @Test
    void shouldCreateOfflineNotificationsForLipAppAndRespWhenVaryPaymentTerms() {
        GeneralApplicationCaseData caseData = buildCase(true);
        givenLipFlags(true, true);

        service.notifyApplicationResponded(caseData, AUTH_TOKEN);

        verifyOfflineNotifications(caseData, true, true);
    }

    @Test
    void shouldCreateOfflineNotificationOnlyForApplicantWhenVaryPaymentTermsAndOnlyApplicantLip() {
        GeneralApplicationCaseData caseData = buildCase(true);
        givenLipFlags(true, false);

        service.notifyApplicationResponded(caseData, AUTH_TOKEN);

        verifyOfflineNotifications(caseData, true, false);
    }

    @Test
    void shouldCreateOfflineNotificationOnlyForRespondentWhenVaryPaymentTermsAndOnlyRespondentLip() {
        GeneralApplicationCaseData caseData = buildCase(true);
        givenLipFlags(false, true);

        service.notifyApplicationResponded(caseData, AUTH_TOKEN);

        verifyOfflineNotifications(caseData, false, true);
    }

    @Test
    void shouldNotCreateOfflineNotificationsWhenVaryPaymentTermsAndNoLip() {
        GeneralApplicationCaseData caseData = buildCase(true);
        givenLipFlags(false, false);

        service.notifyApplicationResponded(caseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardNotificationService);
    }

    private void givenLipFlags(boolean applicantLip, boolean respondentLip) {
        when(gaForLipService.isLipApp(any())).thenReturn(applicantLip);
        when(gaForLipService.isLipResp(any())).thenReturn(respondentLip);
    }

    private GeneralApplicationCaseData buildCase(boolean varyPaymentTerms) {
        return GeneralApplicationCaseData.builder()
            .parentClaimantIsApplicant(varyPaymentTerms ? YesOrNo.NO : YesOrNo.YES)
            .generalAppType(GAApplicationType.builder()
                                .types(List.of(varyPaymentTerms
                                                   ? GeneralApplicationTypes.VARY_PAYMENT_TERMS_OF_JUDGMENT
                                                   : GeneralApplicationTypes.SUMMARY_JUDGEMENT))
                                .build())
            .build();
    }

    private void verifyResponseNotifications(GeneralApplicationCaseData caseData,
                                             boolean expectApplicant,
                                             boolean expectRespondent) {
        verify(dashboardNotificationService, times(expectApplicant ? 1 : 0))
            .createResponseDashboardNotification(
                eq(caseData),
                eq(DocUploadDashboardNotificationService.APPLICANT),
                eq(AUTH_TOKEN)
            );
        verify(dashboardNotificationService, times(expectRespondent ? 1 : 0))
            .createResponseDashboardNotification(
                eq(caseData),
                eq(DocUploadDashboardNotificationService.RESPONDENT),
                eq(AUTH_TOKEN)
            );
        verify(dashboardNotificationService, never())
            .createOfflineResponseDashboardNotification(any(), any(), anyString());
        verifyNoMoreInteractions(dashboardNotificationService);
    }

    private void verifyOfflineNotifications(GeneralApplicationCaseData caseData,
                                            boolean expectApplicant,
                                            boolean expectRespondent) {
        verify(dashboardNotificationService, times(expectApplicant ? 1 : 0))
            .createOfflineResponseDashboardNotification(
                eq(caseData),
                eq(DocUploadDashboardNotificationService.APPLICANT),
                eq(AUTH_TOKEN)
            );
        verify(dashboardNotificationService, times(expectRespondent ? 1 : 0))
            .createOfflineResponseDashboardNotification(
                eq(caseData),
                eq(DocUploadDashboardNotificationService.RESPONDENT),
                eq(AUTH_TOKEN)
            );
        verify(dashboardNotificationService, never())
            .createResponseDashboardNotification(any(), any(), anyString());
        verifyNoMoreInteractions(dashboardNotificationService);
    }
}
