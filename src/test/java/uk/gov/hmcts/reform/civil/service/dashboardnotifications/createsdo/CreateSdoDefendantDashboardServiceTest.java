package uk.gov.hmcts.reform.civil.service.dashboardnotifications.createsdo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Mediation;
import uk.gov.hmcts.reform.civil.model.mediation.MediationDocumentsType;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.utils.DashboardDecisionHelper;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.utils.ElementUtils;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.AllocatedTrack.FAST_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.mediation.MediationUnsuccessfulReason.NOT_CONTACTABLE_CLAIMANT_ONE;
import static uk.gov.hmcts.reform.civil.enums.mediation.MediationUnsuccessfulReason.NOT_CONTACTABLE_DEFENDANT_ONE;
import static uk.gov.hmcts.reform.civil.model.mediation.MediationDocumentsType.NON_ATTENDANCE_STATEMENT;

@ExtendWith(MockitoExtension.class)
class CreateSdoDefendantDashboardServiceTest {

    private static final String AUTH_TOKEN = "Bearer";
    private static final List<MediationDocumentsType> MEDIATION_NON_ATTENDANCE_OPTION = List.of(NON_ATTENDANCE_STATEMENT);

    @Mock
    private DashboardScenariosService dashboardScenariosService;
    @Mock
    private DashboardNotificationsParamsMapper mapper;
    @Mock
    private CreateSdoDashboardDate createSdoDashboardDate;
    @Mock
    private FeatureToggleService featureToggleService;
    @Mock
    private DashboardDecisionHelper dashboardDecisionHelper;

    @InjectMocks
    private CreateSdoDefendantDashboardService createSdoDefendantDashboardService;

    @Test
    void shouldRecordScenarioInSDO_whenInvoked() {
        CaseData caseData = CaseDataBuilder.builder().atStateTrialReadyCheck().build();
        caseData.setOrderSDODocumentDJCollection(List.of(
            ElementUtils.element(new CaseDocument().setDocumentLink(
                new Document().setDocumentBinaryUrl("urlDirectionsOrder")))));
        caseData.setRespondent1Represented(YesOrNo.NO);

        HashMap<String, Object> scenarioParams = new HashMap<>();
        scenarioParams.put("orderDocument", "urlDirectionsOrder");

        when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
        when(dashboardDecisionHelper.isDashBoardEnabledForCase(caseData)).thenReturn(true);

        createSdoDefendantDashboardService.notifySdoCreated(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            AUTH_TOKEN,
            "Scenario.AAA6.CP.OrderMade.Defendant",
            caseData.getCcdCaseReference().toString(),
            ScenarioRequestParams.builder().params(scenarioParams).build()
        );
    }

    @Test
    void shouldNotRecordScenarioInSDO_whenInvoked_ifLipVLipDisabled() {
        CaseData caseData = CaseDataBuilder.builder().atStateTrialReadyCheck().build();
        caseData.setOrderSDODocumentDJCollection(List.of(
            ElementUtils.element(new CaseDocument().setDocumentLink(
                new Document().setDocumentBinaryUrl("urlDirectionsOrder")))));
        caseData.setRespondent1Represented(YesOrNo.NO);

        HashMap<String, Object> scenarioParams = new HashMap<>();
        scenarioParams.put("orderDocument", "urlDirectionsOrder");

        when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);
        when(featureToggleService.isLipVLipEnabled()).thenReturn(false);

        createSdoDefendantDashboardService.notifySdoCreated(caseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardScenariosService);
    }

    @Test
    void shouldNotRecordScenarioInSDO_whenInvoked_ifDashboardDisabled() {
        CaseData caseData = CaseDataBuilder.builder().atStateTrialReadyCheck().build();
        caseData.setOrderSDODocumentDJCollection(List.of(
            ElementUtils.element(new CaseDocument().setDocumentLink(
                new Document().setDocumentBinaryUrl("urlDirectionsOrder")))));
        caseData.setRespondent1Represented(YesOrNo.NO);

        HashMap<String, Object> scenarioParams = new HashMap<>();
        scenarioParams.put("orderDocument", "urlDirectionsOrder");

        when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
        when(dashboardDecisionHelper.isDashBoardEnabledForCase(caseData)).thenReturn(false);

        createSdoDefendantDashboardService.notifySdoCreated(caseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardScenariosService);
    }

    @Test
    void shouldNotRecordScenarioInSDO_whenRespondentRepresented() {
        CaseData caseData = CaseDataBuilder.builder().atStateTrialReadyCheck().build();
        caseData.setOrderSDODocumentDJCollection(List.of(
            ElementUtils.element(new CaseDocument().setDocumentLink(
                new Document().setDocumentBinaryUrl("urlDirectionsOrder")))));
        caseData.setRespondent1Represented(YesOrNo.YES);

        HashMap<String, Object> scenarioParams = new HashMap<>();
        scenarioParams.put("orderDocument", "urlDirectionsOrder");

        when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);

        createSdoDefendantDashboardService.notifySdoCreated(caseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardScenariosService);
    }

    @Test
    void shouldRecordScenarioInSDO_whenInvokedMediationUnsuccessfulCarmWithoutUploadDocuments() {
        CaseData caseData = CaseDataBuilder.builder().atStateTrialReadyCheck().build();
        caseData.setOrderSDODocumentDJCollection(List.of(
            ElementUtils.element(new CaseDocument().setDocumentLink(
                new Document().setDocumentBinaryUrl("urlDirectionsOrder")))));
        caseData.setRespondent1Represented(YesOrNo.NO);
        caseData.setResponseClaimTrack(FAST_CLAIM.name());
        caseData.setTotalClaimAmount(BigDecimal.valueOf(999));
        caseData.setMediation(new Mediation()
                                  .setMediationUnsuccessfulReasonsMultiSelect(List.of(
                                      NOT_CONTACTABLE_CLAIMANT_ONE,
                                      NOT_CONTACTABLE_DEFENDANT_ONE
                                  )));

        HashMap<String, Object> scenarioParams = new HashMap<>();
        scenarioParams.put("orderDocument", "urlDirectionsOrder");

        when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
        when(dashboardDecisionHelper.isDashBoardEnabledForCase(caseData)).thenReturn(true);
        when(dashboardDecisionHelper.isCarmApplicableCase(any())).thenReturn(true);
        when(dashboardDecisionHelper.isMediationUnsuccessfulReasonEqualToNotContactableDefendantOne(any()))
            .thenReturn(true);
        when(dashboardDecisionHelper.hasTrackChanged(any())).thenReturn(true);
        when(dashboardDecisionHelper.hasUploadDocuments(any())).thenReturn(false);

        createSdoDefendantDashboardService.notifySdoCreated(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            AUTH_TOKEN,
            "Scenario.AAA6.MediationUnsuccessfulWithoutUploadDocuments.TrackChange.CARM.Defendant",
            caseData.getCcdCaseReference().toString(),
            ScenarioRequestParams.builder().params(scenarioParams).build()
        );
    }

    @Test
    void shouldRecordMediationUnsuccessfulScenarioInSDO_whenMediationUnsuccessfulReasonEqualToNotContactableDefendantOneIsFalse() {
        CaseData caseData = CaseDataBuilder.builder().atStateTrialReadyCheck()
            .uploadMediationByDocumentTypes(MEDIATION_NON_ATTENDANCE_OPTION)
            .build();
        caseData.setOrderSDODocumentDJCollection(List.of(
            ElementUtils.element(new CaseDocument().setDocumentLink(
                new Document().setDocumentBinaryUrl("urlDirectionsOrder")))));
        caseData.setRespondent1Represented(YesOrNo.NO);
        caseData.setResponseClaimTrack(FAST_CLAIM.name());
        caseData.setTotalClaimAmount(BigDecimal.valueOf(999));
        caseData.setMediation(new Mediation()
                                  .setMediationUnsuccessfulReasonsMultiSelect(List.of(
                                      NOT_CONTACTABLE_CLAIMANT_ONE,
                                      NOT_CONTACTABLE_DEFENDANT_ONE
                                  )));

        HashMap<String, Object> scenarioParams = new HashMap<>();
        scenarioParams.put("orderDocument", "urlDirectionsOrder");

        when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
        when(dashboardDecisionHelper.isDashBoardEnabledForCase(caseData)).thenReturn(true);
        when(dashboardDecisionHelper.isCarmApplicableCase(any())).thenReturn(true);
        when(dashboardDecisionHelper.isMediationUnsuccessfulReasonEqualToNotContactableDefendantOne(any()))
            .thenReturn(false);

        createSdoDefendantDashboardService.notifySdoCreated(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService, never()).recordScenarios(
            AUTH_TOKEN,
            "Scenario.AAA6.MediationUnsuccessful.TrackChange.CARM.Defendant",
            caseData.getCcdCaseReference().toString(),
            ScenarioRequestParams.builder().params(scenarioParams).build()
        );

        verify(dashboardScenariosService).recordScenarios(
            AUTH_TOKEN,
            "Scenario.AAA6.CP.OrderMade.Defendant",
            caseData.getCcdCaseReference().toString(),
            ScenarioRequestParams.builder().params(scenarioParams).build()
        );
    }

    @Test
    void shouldRecordMediationUnsuccessfulScenarioInSDO_whenHasTrackChangedIsFalse() {
        CaseData caseData = CaseDataBuilder.builder().atStateTrialReadyCheck()
            .uploadMediationByDocumentTypes(MEDIATION_NON_ATTENDANCE_OPTION)
            .build();
        caseData.setOrderSDODocumentDJCollection(List.of(
            ElementUtils.element(new CaseDocument().setDocumentLink(
                new Document().setDocumentBinaryUrl("urlDirectionsOrder")))));
        caseData.setRespondent1Represented(YesOrNo.NO);
        caseData.setResponseClaimTrack(FAST_CLAIM.name());
        caseData.setTotalClaimAmount(BigDecimal.valueOf(999));
        caseData.setMediation(new Mediation()
                                  .setMediationUnsuccessfulReasonsMultiSelect(List.of(
                                      NOT_CONTACTABLE_CLAIMANT_ONE,
                                      NOT_CONTACTABLE_DEFENDANT_ONE
                                  )));

        HashMap<String, Object> scenarioParams = new HashMap<>();
        scenarioParams.put("orderDocument", "urlDirectionsOrder");

        when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
        when(dashboardDecisionHelper.isDashBoardEnabledForCase(caseData)).thenReturn(true);
        when(dashboardDecisionHelper.isCarmApplicableCase(any())).thenReturn(true);
        when(dashboardDecisionHelper.isMediationUnsuccessfulReasonEqualToNotContactableDefendantOne(any()))
            .thenReturn(true);
        when(dashboardDecisionHelper.hasTrackChanged(any())).thenReturn(false);

        createSdoDefendantDashboardService.notifySdoCreated(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService, never()).recordScenarios(
            AUTH_TOKEN,
            "Scenario.AAA6.MediationUnsuccessful.TrackChange.CARM.Defendant",
            caseData.getCcdCaseReference().toString(),
            ScenarioRequestParams.builder().params(scenarioParams).build()
        );

        verify(dashboardScenariosService).recordScenarios(
            AUTH_TOKEN,
            "Scenario.AAA6.CP.OrderMade.Defendant",
            caseData.getCcdCaseReference().toString(),
            ScenarioRequestParams.builder().params(scenarioParams).build()
        );
    }

    @Test
    void shouldRecordScenarioInSDO_whenInvokedMediationUnsuccessfulCarmWithUploadDocuments() {
        CaseData caseData = CaseDataBuilder.builder().atStateTrialReadyCheck()
            .uploadMediationByDocumentTypes(MEDIATION_NON_ATTENDANCE_OPTION)
            .build();
        caseData.setOrderSDODocumentDJCollection(List.of(
            ElementUtils.element(new CaseDocument().setDocumentLink(
                new Document().setDocumentBinaryUrl("urlDirectionsOrder")))));
        caseData.setRespondent1Represented(YesOrNo.NO);
        caseData.setResponseClaimTrack(FAST_CLAIM.name());
        caseData.setTotalClaimAmount(BigDecimal.valueOf(999));
        caseData.setMediation(new Mediation()
                                  .setMediationUnsuccessfulReasonsMultiSelect(List.of(
                                      NOT_CONTACTABLE_CLAIMANT_ONE,
                                      NOT_CONTACTABLE_DEFENDANT_ONE
                                  )));

        HashMap<String, Object> scenarioParams = new HashMap<>();
        scenarioParams.put("orderDocument", "urlDirectionsOrder");

        when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
        when(dashboardDecisionHelper.isDashBoardEnabledForCase(caseData)).thenReturn(true);
        when(dashboardDecisionHelper.isCarmApplicableCase(any())).thenReturn(true);
        when(dashboardDecisionHelper.isMediationUnsuccessfulReasonEqualToNotContactableDefendantOne(any()))
            .thenReturn(true);
        when(dashboardDecisionHelper.hasTrackChanged(any())).thenReturn(true);
        when(dashboardDecisionHelper.hasUploadDocuments(any())).thenReturn(true);

        createSdoDefendantDashboardService.notifySdoCreated(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            AUTH_TOKEN,
            "Scenario.AAA6.MediationUnsuccessful.TrackChange.CARM.Defendant",
            caseData.getCcdCaseReference().toString(),
            ScenarioRequestParams.builder().params(scenarioParams).build()
        );
    }

    @Test
    void shouldNotRecordScenarioInSDO_whenInvokedMediationUnsuccessfulCarmFastClaim() {
        CaseData caseData = CaseDataBuilder.builder().atStateTrialReadyCheck().build();
        caseData.setOrderSDODocumentDJCollection(List.of(
            ElementUtils.element(new CaseDocument().setDocumentLink(
                new Document().setDocumentBinaryUrl("urlDirectionsOrder")))));
        caseData.setRespondent1Represented(YesOrNo.NO);
        caseData.setResponseClaimTrack(FAST_CLAIM.name());
        caseData.setTotalClaimAmount(BigDecimal.valueOf(11111111));
        caseData.setMediation(new Mediation()
                                  .setMediationUnsuccessfulReasonsMultiSelect(List.of(
                                      NOT_CONTACTABLE_CLAIMANT_ONE,
                                      NOT_CONTACTABLE_DEFENDANT_ONE
                                  )));

        HashMap<String, Object> scenarioParams = new HashMap<>();
        scenarioParams.put("orderDocument", "urlDirectionsOrder");

        when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
        when(dashboardDecisionHelper.isDashBoardEnabledForCase(caseData)).thenReturn(true);
        when(dashboardDecisionHelper.isCarmApplicableCase(any())).thenReturn(false);
        when(dashboardDecisionHelper.isSDODrawnPreCPRelease(any())).thenReturn(true);

        createSdoDefendantDashboardService.notifySdoCreated(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService, never()).recordScenarios(
            AUTH_TOKEN,
            "Scenario.AAA6.MediationUnsuccessful.TrackChange.CARM.Defendant",
            caseData.getCcdCaseReference().toString(),
            ScenarioRequestParams.builder().params(scenarioParams).build()
        );

        verify(dashboardScenariosService).recordScenarios(
            AUTH_TOKEN,
            "Scenario.AAA6.ClaimantIntent.SDODrawn.PreCaseProgression.Defendant",
            caseData.getCcdCaseReference().toString(),
            ScenarioRequestParams.builder().params(scenarioParams).build()
        );
    }

    @Test
    void shouldRecordScenarioInSDOPreCPRelease_whenInvoked() {
        CaseData caseData = CaseDataBuilder.builder().atStateTrialReadyCheck().build();
        caseData.setOrderSDODocumentDJCollection(List.of(
            ElementUtils.element(new CaseDocument().setDocumentLink(
                new Document().setDocumentBinaryUrl("urlDirectionsOrder")))));
        caseData.setRespondent1Represented(YesOrNo.NO);

        HashMap<String, Object> scenarioParams = new HashMap<>();
        scenarioParams.put("orderDocument", "urlDirectionsOrder");

        when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
        when(dashboardDecisionHelper.isDashBoardEnabledForCase(caseData)).thenReturn(true);
        when(dashboardDecisionHelper.isSDODrawnPreCPRelease(any())).thenReturn(true);

        createSdoDefendantDashboardService.notifySdoCreated(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            AUTH_TOKEN,
            "Scenario.AAA6.ClaimantIntent.SDODrawn.PreCaseProgression.Defendant",
            caseData.getCcdCaseReference().toString(),
            ScenarioRequestParams.builder().params(scenarioParams).build()
        );
    }

    @Test
    void shouldRecordScenarioInSdoLegalAdviser_whenInvoked() {
        CaseData caseData = CaseDataBuilder.builder().atStateTrialReadyCheck().build();
        caseData.setOrderSDODocumentDJCollection(List.of(
            ElementUtils.element(new CaseDocument().setDocumentLink(
                new Document().setDocumentBinaryUrl("urlDirectionsOrder")))));
        caseData.setRespondent1Represented(YesOrNo.NO);

        HashMap<String, Object> scenarioParams = new HashMap<>();
        scenarioParams.put("orderDocument", "urlDirectionsOrder");

        when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
        when(dashboardDecisionHelper.isDashBoardEnabledForCase(caseData)).thenReturn(true);

        createSdoDefendantDashboardService.notifySdoCreated(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            AUTH_TOKEN,
            "Scenario.AAA6.CP.OrderMade.Defendant",
            caseData.getCcdCaseReference().toString(),
            ScenarioRequestParams.builder().params(scenarioParams).build()
        );
    }

    @Test
    void shouldRecordScenarioInSdoLegalAdviser_IfIsEligibleForReconsideration() {
        HashMap<String, Object> scenarioParams = new HashMap<>();
        scenarioParams.put("orderDocument", "urlDirectionsOrder");
        CaseData caseData = CaseDataBuilder.builder().atAllFinalOrdersIssuedCheck().build();
        caseData.setRespondent1Represented(YesOrNo.NO);
        caseData.setIsReferToJudgeClaim(null);

        when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
        when(dashboardDecisionHelper.isDashBoardEnabledForCase(caseData)).thenReturn(true);
        when(dashboardDecisionHelper.isEligibleForReconsideration(any())).thenReturn(true);

        createSdoDefendantDashboardService.notifySdoCreated(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            AUTH_TOKEN,
            "Scenario.AAA6.CP.SDOMadebyLA.Defendant",
            caseData.getCcdCaseReference().toString(),
            ScenarioRequestParams.builder().params(scenarioParams).build()
        );
    }

    @Test
    void shouldNotRecordScenarioInSdoLegalAdviser_IfIsNotEligibleForReconsideration() {
        HashMap<String, Object> scenarioParams = new HashMap<>();
        scenarioParams.put("orderDocument", "urlDirectionsOrder");
        CaseData caseData = CaseDataBuilder.builder().atAllFinalOrdersIssuedCheck().build();
        caseData.setRespondent1Represented(YesOrNo.NO);
        caseData.setIsReferToJudgeClaim(null);

        when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
        when(dashboardDecisionHelper.isDashBoardEnabledForCase(caseData)).thenReturn(true);
        when(dashboardDecisionHelper.isEligibleForReconsideration(any())).thenReturn(false);

        createSdoDefendantDashboardService.notifySdoCreated(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService, never()).recordScenarios(
            AUTH_TOKEN,
            "Scenario.AAA6.CP.SDOMadebyLA.Defendant",
            caseData.getCcdCaseReference().toString(),
            ScenarioRequestParams.builder().params(scenarioParams).build()
        );

        verify(dashboardScenariosService).recordScenarios(
            AUTH_TOKEN,
            "Scenario.AAA6.CP.OrderMade.Defendant",
            caseData.getCcdCaseReference().toString(),
            ScenarioRequestParams.builder().params(scenarioParams).build()
        );
    }

    @Test
    void shouldNotRecordScenarioInSdoLegalAdviser_IfIsReferToJudgeClaim() {
        HashMap<String, Object> scenarioParams = new HashMap<>();
        scenarioParams.put("orderDocument", "urlDirectionsOrder");
        CaseData caseData = CaseDataBuilder.builder().atAllFinalOrdersIssuedCheck().build();
        caseData.setRespondent1Represented(YesOrNo.NO);
        caseData.setIsReferToJudgeClaim(YesOrNo.YES);

        when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
        when(dashboardDecisionHelper.isDashBoardEnabledForCase(caseData)).thenReturn(true);
        when(dashboardDecisionHelper.isEligibleForReconsideration(any())).thenReturn(true);

        createSdoDefendantDashboardService.notifySdoCreated(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService, never()).recordScenarios(
            AUTH_TOKEN,
            "Scenario.AAA6.CP.SDOMadebyLA.Defendant",
            caseData.getCcdCaseReference().toString(),
            ScenarioRequestParams.builder().params(scenarioParams).build()
        );

        verify(dashboardScenariosService).recordScenarios(
            AUTH_TOKEN,
            "Scenario.AAA6.CP.OrderMade.Defendant",
            caseData.getCcdCaseReference().toString(),
            ScenarioRequestParams.builder().params(scenarioParams).build()
        );
    }
}
