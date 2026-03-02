package uk.gov.hmcts.reform.civil.service.dashboardnotifications.claimissue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.FeeType;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.FeePaymentOutcomeDetails;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import java.util.HashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_APPLICATIONS_TO_THE_COURT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIM_ISSUE_HWF_PHONE_PAYMENT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIM_ISSUE_RESPONSE_AWAIT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_MESSAGES_TO_THE_COURT;

@ExtendWith(MockitoExtension.class)
class CreateClaimAfterPaymentApplicantDashboardServiceTest {

    private static final String AUTH_TOKEN = "BEARER";

    @Mock
    private DashboardScenariosService dashboardScenariosService;
    @Mock
    private DashboardNotificationsParamsMapper mapper;
    @Mock
    private FeatureToggleService featureToggleService;

    @InjectMocks
    private CreateClaimAfterPaymentApplicantDashboardService service;

    @BeforeEach
    void setup() {
        when(mapper.mapCaseDataToParams(any())).thenReturn(new HashMap<>());
    }

    @Test
    void shouldRecordBaseAndExtraScenariosWhenEligible() {
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
        when(featureToggleService.isDashboardEnabledForCase(any())).thenReturn(true);
        when(featureToggleService.isLipQueryManagementEnabled(any())).thenReturn(true);

        FeePaymentOutcomeDetails feeOutcome = new FeePaymentOutcomeDetails();
        feeOutcome.setHwfFullRemissionGrantedForClaimIssue(YesOrNo.NO);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCcdCaseReference(1234L);
        caseData.setApplicant1Represented(YesOrNo.NO);
        caseData.setHwfFeeType(FeeType.CLAIMISSUED);
        caseData.setFeePaymentOutcomeDetails(feeOutcome);

        service.notifyClaimant(caseData, AUTH_TOKEN);

        ScenarioRequestParams params = ScenarioRequestParams.builder().params(new HashMap<>()).build();
        verify(dashboardScenariosService).recordScenarios(
            AUTH_TOKEN,
            SCENARIO_AAA6_CLAIM_ISSUE_RESPONSE_AWAIT.getScenario(),
            "1234",
            params
        );
        verify(dashboardScenariosService).recordScenarios(
            AUTH_TOKEN,
            SCENARIO_AAA6_APPLICATIONS_TO_THE_COURT.getScenario(),
            "1234",
            params
        );
        verify(dashboardScenariosService).recordScenarios(
            AUTH_TOKEN,
            SCENARIO_AAA6_MESSAGES_TO_THE_COURT.getScenario(),
            "1234",
            params
        );
        verify(dashboardScenariosService).recordScenarios(
            AUTH_TOKEN,
            SCENARIO_AAA6_CLAIM_ISSUE_HWF_PHONE_PAYMENT.getScenario(),
            "1234",
            params
        );
    }

    @Test
    void shouldNotRecordAnythingWhenLipVLipDisabled() {
        when(featureToggleService.isLipVLipEnabled()).thenReturn(false);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCcdCaseReference(1234L);

        service.notifyClaimant(caseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardScenariosService);
    }

    @Test
    void shouldNotRecordAnythingWhenDashboardDisabled() {
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
        when(featureToggleService.isDashboardEnabledForCase(any())).thenReturn(false);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCcdCaseReference(1234L);
        caseData.setApplicant1Represented(YesOrNo.NO);

        service.notifyClaimant(caseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardScenariosService);
    }

    @Test
    void shouldNotRecordAnythingWhenNotLipCase() {
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
        when(featureToggleService.isDashboardEnabledForCase(any())).thenReturn(true);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCcdCaseReference(1234L);
        caseData.setApplicant1Represented(YesOrNo.YES);
        caseData.setRespondent1Represented(YesOrNo.YES);

        service.notifyClaimant(caseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardScenariosService);
    }

    @Test
    void shouldNotRecordAnythingWhenApplicantRepresentedEvenIfLipCase() {
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
        when(featureToggleService.isDashboardEnabledForCase(any())).thenReturn(true);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCcdCaseReference(1234L);
        caseData.setApplicant1Represented(YesOrNo.YES);
        caseData.setRespondent1Represented(YesOrNo.NO);

        service.notifyClaimant(caseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardScenariosService);
    }
}
