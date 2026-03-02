package uk.gov.hmcts.reform.civil.service.dashboardnotifications.defendantresponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dq.Language;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
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
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AA6_DEFENDANT_RESPONSE_PAY_BY_INSTALLMENTS_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DEFENDANT_ADMIT_AND_PAID_PARTIAL_ALREADY_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DEFENDANT_ADMIT_PAY_INSTALLMENTS_ORG_COM_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DEFENDANT_FULL_ADMIT_PAY_IMMEDIATELY_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DEFENDANT_FULL_OR_PART_ADMIT_PAY_SET_DATE_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DEFENDANT_FULL_OR_PART_ADMIT_PAY_SET_DATE_ORG_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DEFENDANT_NOC_MOVES_OFFLINE_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DEFENDANT_PART_ADMIT_PAY_IMMEDIATELY_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DEFENDANT_RESPONSE_BILINGUAL_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DEFENDANT_RESPONSE_BILINGUAL_WELSH_ENABLED_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DEFENDANT_RESPONSE_FULLDISPUTE_MULTI_INT_FAST_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DEFENDANT_RESPONSE_FULL_DEFENCE_ALREADY_PAID_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DEFENDANT_RESPONSE_FULL_DEFENCE_FULL_DISPUTE_CLAIMANT_CARM;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DEFENDANT_RESPONSE_FULL_DEFENCE_FULL_DISPUTE_MEDIATION_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DEF_RESPONSE_FULL_DEFENCE_FULL_DISPUTE_REFUSED_MEDIATION_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_ENGLISH_DEFENDANT_RESPONSE_BILINGUAL_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPLICATION_AVAILABLE_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPLICATION_INITIATE_APPLICATION_INACTIVE_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_NOTICE_AAA6_DEF_LR_RESPONSE_FULL_DEFENCE_COUNTERCLAIM_CLAIMANT;

@ExtendWith(MockitoExtension.class)
class DefendantResponseClaimantDashboardServiceTest {

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
    private DefendantResponseClaimantDashboardService service;

    @BeforeEach
    void setUp() {
        lenient().when(mapper.mapCaseDataToParams(any())).thenReturn(new HashMap<>());
    }

    private CaseData mockBaseCaseData(long caseId) {
        CaseData caseData = mock(CaseData.class);
        when(caseData.getCcdCaseReference()).thenReturn(caseId);
        when(caseData.isRespondentResponseBilingual()).thenReturn(false);
        lenient().when(caseData.isClaimantBilingual()).thenReturn(false);
        when(caseData.isApplicant1NotRepresented()).thenReturn(true);
        lenient().when(caseData.nocApplyForLiPDefendant()).thenReturn(false);
        lenient().when(caseData.getGeneralApplications()).thenReturn(null);
        lenient().when(caseData.isApplicantLiP()).thenReturn(true);
        return caseData;
    }

    @Test
    void shouldRecordEnglishScenarioWhenEligible() {
        CaseData caseData = CaseDataBuilder.builder()
            .ccdCaseReference(1234L)
            .applicant1Represented(YesOrNo.NO)
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
            .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY)
            .respondent1(new Party().setType(Party.Type.INDIVIDUAL))
            .build();

        service.notifyDefendantResponse(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_DEFENDANT_FULL_ADMIT_PAY_IMMEDIATELY_CLAIMANT.getScenario()),
            eq("1234"),
            any(ScenarioRequestParams.class)
        );
        verifyNoInteractions(dashboardNotificationService);
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
    void shouldRecordGeneralApplicationScenariosForCounterClaimLipCase() {
        CaseData caseData = CaseDataBuilder.builder()
            .ccdCaseReference(9012L)
            .applicant1Represented(YesOrNo.NO)
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.COUNTER_CLAIM)
            .getGeneralApplicationWithStrikeOut("respondent1")
            .build();

        service.notifyDefendantResponse(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_GENERAL_APPLICATION_INITIATE_APPLICATION_INACTIVE_CLAIMANT.getScenario()),
            eq("9012"),
            any(ScenarioRequestParams.class)
        );
        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_GENERAL_APPLICATION_AVAILABLE_CLAIMANT.getScenario()),
            eq("9012"),
            any(ScenarioRequestParams.class)
        );
        verify(dashboardNotificationService, never()).deleteByNameAndReferenceAndCitizenRole(any(), any(), any());
    }

    @Test
    void shouldReturnOfflineScenarioForFullAdmissionWhenNoc() {
        CaseData caseData = mockBaseCaseData(3010L);
        when(caseData.getRespondent1ClaimResponseTypeForSpec()).thenReturn(RespondentResponseTypeSpec.FULL_ADMISSION);
        when(caseData.nocApplyForLiPDefendant()).thenReturn(true);

        service.notifyDefendantResponse(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_DEFENDANT_NOC_MOVES_OFFLINE_CLAIMANT.getScenario()),
            eq("3010"),
            any(ScenarioRequestParams.class)
        );
    }

    @Test
    void shouldUseOrganisationInstallmentScenarioForPartAdmission() {
        CaseData caseData = mockBaseCaseData(3001L);
        when(caseData.getRespondent1ClaimResponseTypeForSpec()).thenReturn(RespondentResponseTypeSpec.PART_ADMISSION);
        when(caseData.isPayByInstallment()).thenReturn(true);
        when(caseData.getRespondent1()).thenReturn(new Party().setType(Party.Type.COMPANY));

        service.notifyDefendantResponse(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_DEFENDANT_ADMIT_PAY_INSTALLMENTS_ORG_COM_CLAIMANT.getScenario()),
            eq("3001"),
            any(ScenarioRequestParams.class)
        );
    }

    @Test
    void shouldUseIndividualInstallmentScenarioForPartAdmission() {
        CaseData caseData = mockBaseCaseData(3007L);
        when(caseData.getRespondent1ClaimResponseTypeForSpec()).thenReturn(RespondentResponseTypeSpec.PART_ADMISSION);
        when(caseData.isPayByInstallment()).thenReturn(true);
        when(caseData.getRespondent1()).thenReturn(new Party().setType(Party.Type.INDIVIDUAL));

        service.notifyDefendantResponse(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AA6_DEFENDANT_RESPONSE_PAY_BY_INSTALLMENTS_CLAIMANT.getScenario()),
            eq("3007"),
            any(ScenarioRequestParams.class)
        );
    }

    @Test
    void shouldUseSetDateScenarioForIndividualFullAdmission() {
        CaseData caseData = mockBaseCaseData(3002L);
        when(caseData.getRespondent1ClaimResponseTypeForSpec()).thenReturn(RespondentResponseTypeSpec.FULL_ADMISSION);
        when(caseData.isPayBySetDate()).thenReturn(true);
        when(caseData.getRespondent1()).thenReturn(new Party().setType(Party.Type.INDIVIDUAL));

        service.notifyDefendantResponse(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_DEFENDANT_FULL_OR_PART_ADMIT_PAY_SET_DATE_CLAIMANT.getScenario()),
            eq("3002"),
            any(ScenarioRequestParams.class)
        );
    }

    @Test
    void shouldUseSetDateScenarioForOrganisationPartAdmission() {
        CaseData caseData = mockBaseCaseData(3008L);
        when(caseData.getRespondent1ClaimResponseTypeForSpec()).thenReturn(RespondentResponseTypeSpec.PART_ADMISSION);
        when(caseData.isPayBySetDate()).thenReturn(true);
        when(caseData.getRespondent1()).thenReturn(new Party().setType(Party.Type.COMPANY));

        service.notifyDefendantResponse(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_DEFENDANT_FULL_OR_PART_ADMIT_PAY_SET_DATE_ORG_CLAIMANT.getScenario()),
            eq("3008"),
            any(ScenarioRequestParams.class)
        );
    }

    @Test
    void shouldUsePayImmediatelyScenarioForPartAdmission() {
        CaseData caseData = mockBaseCaseData(3009L);
        when(caseData.getRespondent1ClaimResponseTypeForSpec()).thenReturn(RespondentResponseTypeSpec.PART_ADMISSION);
        when(caseData.isPayImmediately()).thenReturn(true);

        service.notifyDefendantResponse(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_DEFENDANT_PART_ADMIT_PAY_IMMEDIATELY_CLAIMANT.getScenario()),
            eq("3009"),
            any(ScenarioRequestParams.class)
        );
    }

    @Test
    void shouldUsePartialAlreadyPaidScenarioWhenPartPaymentAcknowledged() {
        CaseData caseData = mockBaseCaseData(3011L);
        when(caseData.getRespondent1ClaimResponseTypeForSpec()).thenReturn(RespondentResponseTypeSpec.PART_ADMISSION);
        when(caseData.isPayByInstallment()).thenReturn(false);
        when(caseData.isPayBySetDate()).thenReturn(false);
        when(caseData.isPayImmediately()).thenReturn(false);
        when(caseData.isPaidFullAmount()).thenReturn(false);

        service.notifyDefendantResponse(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_DEFENDANT_ADMIT_AND_PAID_PARTIAL_ALREADY_CLAIMANT.getScenario()),
            eq("3011"),
            any(ScenarioRequestParams.class)
        );
    }

    @Test
    void shouldSkipAdditionalScenariosWhenBilingual() {
        CaseData caseData = mock(CaseData.class);
        when(caseData.isRespondentResponseBilingual()).thenReturn(true);
        Map<String, Boolean> scenarios = ReflectionTestUtils.invokeMethod(service, "getScenarios", caseData);
        assertThat(scenarios).isEmpty();
    }

    @Test
    void shouldRequireLipForBilingualScenario() {
        CaseData caseData = CaseDataBuilder.builder()
            .applicant1Represented(YesOrNo.NO)
            .caseDataLip(caseDataLiPWithResponseLanguage(Language.BOTH))
            .build();
        boolean result = ReflectionTestUtils.invokeMethod(service, "shouldRecordScenario", caseData);
        assertThat(result).isTrue();

        CaseData representedCase = caseData.toBuilder()
            .applicant1Represented(YesOrNo.YES)
            .build();
        result = ReflectionTestUtils.invokeMethod(service, "shouldRecordScenario", representedCase);
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

    @Test
    void shouldUseAlreadyPaidScenarioWhenDefenceSaysPaid() {
        CaseData caseData = mockBaseCaseData(3003L);
        when(caseData.getRespondent1ClaimResponseTypeForSpec()).thenReturn(RespondentResponseTypeSpec.FULL_DEFENCE);
        when(caseData.isClaimBeingDisputed()).thenReturn(false);
        when(caseData.isPaidFullAmount()).thenReturn(true);

        service.notifyDefendantResponse(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_DEFENDANT_RESPONSE_FULL_DEFENCE_ALREADY_PAID_CLAIMANT.getScenario()),
            eq("3003"),
            any(ScenarioRequestParams.class)
        );
    }

    @Test
    void shouldUseCounterClaimScenarioWhenNoCOnline() {
        CaseData caseData = mockBaseCaseData(3004L);
        when(caseData.getRespondent1ClaimResponseTypeForSpec()).thenReturn(RespondentResponseTypeSpec.COUNTER_CLAIM);
        when(caseData.isLipvLROneVOne()).thenReturn(true);
        when(featureToggleService.isDefendantNoCOnlineForCase(caseData)).thenReturn(true);

        service.notifyDefendantResponse(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_NOTICE_AAA6_DEF_LR_RESPONSE_FULL_DEFENCE_COUNTERCLAIM_CLAIMANT.getScenario()),
            eq("3004"),
            any(ScenarioRequestParams.class)
        );
    }

    @Test
    void shouldUseRefusedMediationScenarioForSmallClaims() {
        CaseData caseData = mockBaseCaseData(3005L);
        when(caseData.getRespondent1ClaimResponseTypeForSpec()).thenReturn(RespondentResponseTypeSpec.FULL_DEFENCE);
        when(caseData.isClaimBeingDisputed()).thenReturn(true);
        when(caseData.isSmallClaim()).thenReturn(true);
        when(caseData.hasDefendantNotAgreedToFreeMediation()).thenReturn(true);

        service.notifyDefendantResponse(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_DEF_RESPONSE_FULL_DEFENCE_FULL_DISPUTE_REFUSED_MEDIATION_CLAIMANT.getScenario()),
            eq("3005"),
            any(ScenarioRequestParams.class)
        );
    }

    @Test
    void shouldUseMediationScenarioWhenSmallClaimAndMediationAccepted() {
        CaseData caseData = mockBaseCaseData(3012L);
        when(caseData.getRespondent1ClaimResponseTypeForSpec()).thenReturn(RespondentResponseTypeSpec.FULL_DEFENCE);
        when(caseData.isClaimBeingDisputed()).thenReturn(true);
        when(caseData.isSmallClaim()).thenReturn(true);
        when(caseData.hasDefendantAgreedToFreeMediation()).thenReturn(true);
        when(featureToggleService.isCarmEnabledForCase(caseData)).thenReturn(false);

        service.notifyDefendantResponse(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_DEFENDANT_RESPONSE_FULL_DEFENCE_FULL_DISPUTE_MEDIATION_CLAIMANT.getScenario()),
            eq("3012"),
            any(ScenarioRequestParams.class)
        );
    }

    @Test
    void shouldUseCarmScenarioWhenEnabledAndSmallClaim() {
        CaseData caseData = mockBaseCaseData(3013L);
        when(caseData.getRespondent1ClaimResponseTypeForSpec()).thenReturn(RespondentResponseTypeSpec.FULL_DEFENCE);
        when(caseData.isClaimBeingDisputed()).thenReturn(true);
        when(caseData.isSmallClaim()).thenReturn(true);
        when(featureToggleService.isCarmEnabledForCase(caseData)).thenReturn(true);

        service.notifyDefendantResponse(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_DEFENDANT_RESPONSE_FULL_DEFENCE_FULL_DISPUTE_CLAIMANT_CARM.getScenario()),
            eq("3013"),
            any(ScenarioRequestParams.class)
        );
    }

    @Test
    void shouldUseMultiTrackScenarioWhenNotSmallClaim() {
        CaseData caseData = mockBaseCaseData(3006L);
        when(caseData.getRespondent1ClaimResponseTypeForSpec()).thenReturn(RespondentResponseTypeSpec.FULL_DEFENCE);
        when(caseData.isClaimBeingDisputed()).thenReturn(true);
        when(caseData.isSmallClaim()).thenReturn(false);

        service.notifyDefendantResponse(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_DEFENDANT_RESPONSE_FULLDISPUTE_MULTI_INT_FAST_CLAIMANT.getScenario()),
            eq("3006"),
            any(ScenarioRequestParams.class)
        );
    }

    @Test
    void shouldNotRecordScenarioWhenRespondent1ClaimResponseTypeForSpecIsNull() {
        CaseData caseData = CaseDataBuilder.builder()
            .ccdCaseReference(1234L)
            .applicant1Represented(YesOrNo.NO)
            .respondent1ClaimResponseTypeForSpec(null)
            .build();

        service.notifyDefendantResponse(caseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardScenariosService);
    }

    private CaseDataLiP caseDataLiPWithResponseLanguage(Language language) {
        RespondentLiPResponse response = new RespondentLiPResponse();
        response.setRespondent1ResponseLanguage(language.toString());
        return new CaseDataLiP().setRespondent1LiPResponse(response);
    }
}
