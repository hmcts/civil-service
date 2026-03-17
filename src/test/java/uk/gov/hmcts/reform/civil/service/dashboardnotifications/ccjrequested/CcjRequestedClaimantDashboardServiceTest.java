package uk.gov.hmcts.reform.civil.service.dashboardnotifications.ccjrequested;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.RespondToClaimAdmitPartLRspec;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_CCJ_REQUESTED_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_REQUESTED_CCJ_CLAIMANT;

@ExtendWith(MockitoExtension.class)
class CcjRequestedClaimantDashboardServiceTest {

    private static final String AUTH_TOKEN = "BEARER";

    @Mock
    private DashboardScenariosService dashboardScenariosService;
    @Mock
    private DashboardNotificationsParamsMapper mapper;
    @Mock
    private FeatureToggleService featureToggleService;

    @InjectMocks
    private CcjRequestedClaimantDashboardService service;

    @BeforeEach
    void setup() {
        when(mapper.mapCaseDataToParams(any())).thenReturn(new HashMap<>());
    }

    @Test
    void shouldRecordCcjRequestedScenarioWhenEligible() {
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);

        CaseDocument caseDocument = new CaseDocument();
        caseDocument.setDocumentType(DocumentType.DEFAULT_JUDGMENT);
        caseDocument.setCreatedDatetime(LocalDateTime.now());
        Element<CaseDocument> element = new Element<>();
        element.setValue(caseDocument);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCcdCaseReference(1234L);
        caseData.setApplicant1Represented(YesOrNo.NO);
        caseData.setDefaultJudgmentDocuments(List.of(element));

        service.notifyClaimant(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_CLAIMANT_INTENT_CCJ_REQUESTED_CLAIMANT.getScenario()),
            eq("1234"),
            any(ScenarioRequestParams.class)
        );
    }

    @Test
    void shouldRecordRequestedCcjScenarioWhenEligibleButNoDefaultJudgment() {
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCcdCaseReference(1234L);
        caseData.setApplicant1Represented(YesOrNo.NO);
        caseData.setDefaultJudgmentDocuments(List.of());

        service.notifyClaimant(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_CLAIMANT_INTENT_REQUESTED_CCJ_CLAIMANT.getScenario()),
            eq("1234"),
            any(ScenarioRequestParams.class)
        );
    }

    @Test
    void shouldRecordCcjRequestedScenarioWhenPaymentDateBeforeToday() {
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);

        RespondToClaimAdmitPartLRspec respondToClaimAdmitPart = new RespondToClaimAdmitPartLRspec();
        respondToClaimAdmitPart.setWhenWillThisAmountBePaid(LocalDate.now().minusDays(1));

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCcdCaseReference(1234L);
        caseData.setApplicant1Represented(YesOrNo.NO);
        caseData.setRespondToClaimAdmitPartLRspec(respondToClaimAdmitPart);
        caseData.setRespondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION);
        caseData.setDefenceAdmitPartPaymentTimeRouteRequired(
            RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY);
        caseData.setDefaultJudgmentDocuments(List.of());

        service.notifyClaimant(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_CLAIMANT_INTENT_CCJ_REQUESTED_CLAIMANT.getScenario()),
            eq("1234"),
            any(ScenarioRequestParams.class)
        );
    }

    @Test
    void shouldRecordRequestedCcjScenarioWhenPaymentDateBeforeTodayButNotImmediate() {
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);

        RespondToClaimAdmitPartLRspec respondToClaimAdmitPart = new RespondToClaimAdmitPartLRspec();
        respondToClaimAdmitPart.setWhenWillThisAmountBePaid(LocalDate.now().minusDays(1));

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCcdCaseReference(1234L);
        caseData.setApplicant1Represented(YesOrNo.NO);
        caseData.setRespondToClaimAdmitPartLRspec(respondToClaimAdmitPart);
        caseData.setRespondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION);
        caseData.setDefenceAdmitPartPaymentTimeRouteRequired(
            RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE);
        caseData.setDefaultJudgmentDocuments(List.of());

        service.notifyClaimant(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_CLAIMANT_INTENT_REQUESTED_CCJ_CLAIMANT.getScenario()),
            eq("1234"),
            any(ScenarioRequestParams.class)
        );
    }

    @Test
    void shouldRecordCcjRequestedScenarioWhenRepaymentSummaryPresent() {
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCcdCaseReference(1234L);
        caseData.setApplicant1Represented(YesOrNo.NO);
        caseData.setDefaultJudgmentDocuments(List.of());
        caseData.setRepaymentSummaryObject("summary");

        service.notifyClaimant(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_CLAIMANT_INTENT_CCJ_REQUESTED_CLAIMANT.getScenario()),
            eq("1234"),
            any(ScenarioRequestParams.class)
        );
    }

    @Test
    void shouldRecordRequestedCcjScenarioWhenPaymentDateAfterToday() {
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);

        RespondToClaimAdmitPartLRspec respondToClaimAdmitPart = new RespondToClaimAdmitPartLRspec();
        respondToClaimAdmitPart.setWhenWillThisAmountBePaid(LocalDate.now().plusDays(1));

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCcdCaseReference(1234L);
        caseData.setApplicant1Represented(YesOrNo.NO);
        caseData.setRespondToClaimAdmitPartLRspec(respondToClaimAdmitPart);
        caseData.setRespondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION);
        caseData.setDefenceAdmitPartPaymentTimeRouteRequired(
            RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY);
        caseData.setDefaultJudgmentDocuments(List.of());

        service.notifyClaimant(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_CLAIMANT_INTENT_REQUESTED_CCJ_CLAIMANT.getScenario()),
            eq("1234"),
            any(ScenarioRequestParams.class)
        );
    }

    @Test
    void shouldRecordRequestedCcjScenarioWhenDocumentTypeIsNotDefaultJudgment() {
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);

        CaseDocument caseDocument = new CaseDocument();
        caseDocument.setDocumentType(DocumentType.SEALED_CLAIM);
        caseDocument.setCreatedDatetime(LocalDateTime.now());
        Element<CaseDocument> element = new Element<>();
        element.setValue(caseDocument);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCcdCaseReference(1234L);
        caseData.setApplicant1Represented(YesOrNo.NO);
        caseData.setDefaultJudgmentDocuments(List.of(element));

        service.notifyClaimant(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_CLAIMANT_INTENT_REQUESTED_CCJ_CLAIMANT.getScenario()),
            eq("1234"),
            any(ScenarioRequestParams.class)
        );
    }

    @Test
    void shouldNotRecordWhenToggleDisabled() {
        when(featureToggleService.isLipVLipEnabled()).thenReturn(false);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCcdCaseReference(1234L);
        caseData.setApplicant1Represented(YesOrNo.NO);
        caseData.setDefaultJudgmentDocuments(List.of());

        service.notifyClaimant(caseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardScenariosService);
    }

    @Test
    void shouldNotRecordWhenApplicantIsRepresented() {
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setApplicant1Represented(YesOrNo.YES);
        caseData.setDefaultJudgmentDocuments(List.of());

        service.notifyClaimant(caseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardScenariosService);
    }
}
