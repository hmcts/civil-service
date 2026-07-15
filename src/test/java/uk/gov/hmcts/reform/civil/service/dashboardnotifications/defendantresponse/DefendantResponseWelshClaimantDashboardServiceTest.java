package uk.gov.hmcts.reform.civil.service.dashboardnotifications.defendantresponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dq.Language;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.RespondentLiPResponse;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;
import uk.gov.hmcts.reform.dashboard.services.DashboardNotificationService;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import java.time.LocalDateTime;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DEFENDANT_RESPONSE_BILINGUAL_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DEFENDANT_RESPONSE_BILINGUAL_WELSH_ENABLED_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_ENGLISH_DEFENDANT_RESPONSE_BILINGUAL_CLAIMANT;

@ExtendWith(MockitoExtension.class)
class DefendantResponseWelshClaimantDashboardServiceTest {

    private static final String AUTH_TOKEN = "BEARER";

    @Mock
    private DashboardScenariosService dashboardScenariosService;
    @Mock
    private DashboardNotificationService dashboardNotificationService;
    @Mock
    private DashboardNotificationsParamsMapper mapper;
    @Mock
    private FeatureToggleService featureToggleService;

    @InjectMocks
    private DefendantResponseWelshClaimantDashboardService service;

    @BeforeEach
    void setUp() {
        lenient().when(mapper.mapCaseDataToParams(any())).thenReturn(new HashMap<>());
    }

    @Test
    void shouldDeleteDjNotificationWhenWelshEnabledAndDeadlinePassed() {
        when(featureToggleService.isWelshEnabledForMainCase()).thenReturn(true);

        CaseData caseData = CaseDataBuilder.builder()
            .ccdCaseReference(5678L)
            .applicant1Represented(YesOrNo.NO)
            .caseDataLip(caseDataLiPWithResponseLanguage(Language.WELSH))
            .claimantBilingualLanguagePreference(Language.WELSH.toString())
            .respondent1ResponseDeadline(LocalDateTime.now().minusDays(1))
            .build();

        service.notifyDefendantResponse(caseData, AUTH_TOKEN);

        verify(dashboardNotificationService).deleteByNameAndReferenceAndCitizenRole(
            "Notice.AAA6.DefResponse.ResponseTimeElapsed.Claimant",
            "5678",
            "CLAIMANT"
        );
        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_DEFENDANT_RESPONSE_BILINGUAL_WELSH_ENABLED_CLAIMANT.getScenario()),
            eq("5678"),
            any(ScenarioRequestParams.class)
        );
    }

    @Test
    void shouldRecordBilingualScenarioWhenClaimIssueBilingualAndToggleEnabled() {
        when(featureToggleService.isWelshEnabledForMainCase()).thenReturn(true);

        CaseData caseData = CaseDataBuilder.builder()
            .ccdCaseReference(4321L)
            .applicant1Represented(YesOrNo.NO)
            .claimantBilingualLanguagePreference(Language.WELSH.toString())
            .build();

        service.notifyDefendantResponse(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_ENGLISH_DEFENDANT_RESPONSE_BILINGUAL_CLAIMANT.getScenario()),
            eq("4321"),
            any(ScenarioRequestParams.class)
        );
        verify(dashboardNotificationService, never()).deleteByNameAndReferenceAndCitizenRole(any(), any(), any());
    }

    @Test
    void shouldRecordBilingualScenarioWhenWelshDisabled() {
        CaseData caseData = CaseDataBuilder.builder()
            .ccdCaseReference(4322L)
            .applicant1Represented(YesOrNo.NO)
            .caseDataLip(caseDataLiPWithResponseLanguage(Language.WELSH))
            .build();

        service.notifyDefendantResponse(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_DEFENDANT_RESPONSE_BILINGUAL_CLAIMANT.getScenario()),
            eq("4322"),
            any(ScenarioRequestParams.class)
        );
    }

    @Test
    void shouldNotRecordBilingualScenarioWhenClaimantNotLip() {
        CaseData caseData = CaseDataBuilder.builder()
            .ccdCaseReference(4323L)
            .applicant1Represented(YesOrNo.YES)
            .caseDataLip(caseDataLiPWithResponseLanguage(Language.BOTH))
            .build();

        service.notifyDefendantResponse(caseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardScenariosService);
    }

    @Test
    void shouldRequireLipForBilingualScenario() {
        CaseData caseData = CaseDataBuilder.builder()
            .applicant1Represented(YesOrNo.NO)
            .caseDataLip(caseDataLiPWithResponseLanguage(Language.BOTH))
            .build();
        boolean result = Boolean.TRUE.equals(ReflectionTestUtils.invokeMethod(
            service,
            "shouldRecordScenario",
            caseData
        ));
        assertThat(result).isTrue();

        CaseData representedCase = caseData.toBuilder()
            .applicant1Represented(YesOrNo.YES)
            .build();
        result = Boolean.TRUE.equals(ReflectionTestUtils.invokeMethod(
            service,
            "shouldRecordScenario",
            representedCase
        ));
        assertThat(result).isFalse();
    }

    @Test
    void shouldNotCleanupDjNotificationWhenNotBilingual() {
        CaseData caseData = CaseDataBuilder.builder()
            .applicant1Represented(YesOrNo.NO)
            .claimantBilingualLanguagePreference(Language.ENGLISH.toString())
            .build();
        ReflectionTestUtils.invokeMethod(service, "beforeRecordScenario", caseData, AUTH_TOKEN);
        verifyNoInteractions(dashboardNotificationService);
    }

    private CaseDataLiP caseDataLiPWithResponseLanguage(Language language) {
        RespondentLiPResponse response = new RespondentLiPResponse();
        response.setRespondent1ResponseLanguage(language.toString());
        return new CaseDataLiP().setRespondent1LiPResponse(response);
    }
}
