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
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.helper.DashboardNotificationHelper;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.helper.DashboardTasksHelper;
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
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_SDO_DRAWN_PRE_CASE_PROGRESSION;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CP_ORDER_MADE_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CP_SDO_MADE_BY_LA_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DEFENDANT_NOTICE_OF_CHANGE_CLAIM_REMAINS_ONLINE_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_MEDIATION_UNSUCCESSFUL_TRACK_CHANGE_CLAIMANT_CARM;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_MEDIATION_UNSUCCESSFUL_TRACK_CHANGE_CLAIMANT_WITHOUT_UPLOAD_FILES_CARM;
import static uk.gov.hmcts.reform.civil.model.mediation.MediationDocumentsType.NON_ATTENDANCE_STATEMENT;

@ExtendWith(MockitoExtension.class)
class CreateSdoClaimantDashboardServiceTest {

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
    private DashboardNotificationHelper dashboardDecisionHelper;
    @Mock
    private DashboardTasksHelper dashboardTasksHelper;

    @InjectMocks
    private CreateSdoClaimantDashboardService createSdoClaimantDashboardService;

    @Test
    void shouldRecordScenarioInSdo_whenInvoked() {
        CaseData caseData = CaseDataBuilder.builder().atStateTrialReadyCheck().build();
        Document document = new Document();
        document.setDocumentBinaryUrl("urlDirectionsOrder");
        CaseDocument caseDocument = new CaseDocument();
        caseDocument.setDocumentLink(document);
        caseData.setOrderSDODocumentDJCollection(List.of(ElementUtils.element(caseDocument)));
        caseData.setApplicant1Represented(YesOrNo.NO);

        HashMap<String, Object> scenarioParams = new HashMap<>();
        scenarioParams.put("orderDocument", "urlDirectionsOrder");

        when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
        when(dashboardDecisionHelper.isDashBoardEnabledForCase(caseData)).thenReturn(true);

        createSdoClaimantDashboardService.notifySdoCreated(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            AUTH_TOKEN,
            SCENARIO_AAA6_CP_ORDER_MADE_CLAIMANT.getScenario(),
            caseData.getCcdCaseReference().toString(),
            ScenarioRequestParams.builder().params(scenarioParams).build()
        );
        verify(dashboardTasksHelper).makeTasksInactiveForClaimant(caseData);
    }

    @Test
    void shouldNotRecordScenarioInSDO_whenInvoked_ifLipVLipDisabled() {
        CaseData caseData = CaseDataBuilder.builder().atStateTrialReadyCheck().build();
        caseData.setOrderSDODocumentDJCollection(List.of(
            ElementUtils.element(new CaseDocument().setDocumentLink(
                new Document().setDocumentBinaryUrl("urlDirectionsOrder")))));
        caseData.setApplicant1Represented(YesOrNo.NO);

        HashMap<String, Object> scenarioParams = new HashMap<>();
        scenarioParams.put("orderDocument", "urlDirectionsOrder");

        when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);
        when(featureToggleService.isLipVLipEnabled()).thenReturn(false);

        createSdoClaimantDashboardService.notifySdoCreated(caseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardScenariosService);
        verify(dashboardTasksHelper).makeTasksInactiveForClaimant(caseData);
    }

    @Test
    void shouldNotRecordScenarioInSDO_whenInvoked_ifDashboardDisabled() {
        CaseData caseData = CaseDataBuilder.builder().atStateTrialReadyCheck().build();
        caseData.setOrderSDODocumentDJCollection(List.of(
            ElementUtils.element(new CaseDocument().setDocumentLink(
                new Document().setDocumentBinaryUrl("urlDirectionsOrder")))));
        caseData.setApplicant1Represented(YesOrNo.NO);

        HashMap<String, Object> scenarioParams = new HashMap<>();
        scenarioParams.put("orderDocument", "urlDirectionsOrder");

        when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);

        createSdoClaimantDashboardService.notifySdoCreated(caseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardScenariosService);
        verify(dashboardTasksHelper).makeTasksInactiveForClaimant(caseData);
    }

    @Test
    void shouldNotRecordScenarioInSDO_whenRespondentRepresented() {
        CaseData caseData = CaseDataBuilder.builder().atStateTrialReadyCheck().build();
        caseData.setOrderSDODocumentDJCollection(List.of(
            ElementUtils.element(new CaseDocument().setDocumentLink(
                new Document().setDocumentBinaryUrl("urlDirectionsOrder")))));
        caseData.setApplicant1Represented(YesOrNo.YES);

        HashMap<String, Object> scenarioParams = new HashMap<>();
        scenarioParams.put("orderDocument", "urlDirectionsOrder");

        when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);

        createSdoClaimantDashboardService.notifySdoCreated(caseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardScenariosService);
        verify(dashboardTasksHelper).makeTasksInactiveForClaimant(caseData);
    }

    @Test
    void shouldRecordScenarioInSdo_whenInvokedForMediationUnsuccessfulCarmWithoutUploadDocuments() {
        CaseData caseData = CaseDataBuilder.builder().atStateTrialReadyCheck().build();
        Document document = new Document();
        document.setDocumentBinaryUrl("urlDirectionsOrder");
        CaseDocument caseDocument = new CaseDocument();
        caseDocument.setDocumentLink(document);
        caseData.setOrderSDODocumentDJCollection(List.of(ElementUtils.element(caseDocument)));
        caseData.setApplicant1Represented(YesOrNo.NO);
        caseData.setResponseClaimTrack(FAST_CLAIM.name());
        caseData.setTotalClaimAmount(BigDecimal.valueOf(999));
        Mediation mediation = new Mediation();
        mediation.setMediationUnsuccessfulReasonsMultiSelect(List.of(
            NOT_CONTACTABLE_CLAIMANT_ONE,
            NOT_CONTACTABLE_DEFENDANT_ONE
        ));
        caseData.setMediation(mediation);

        HashMap<String, Object> scenarioParams = new HashMap<>();
        scenarioParams.put("orderDocument", "urlDirectionsOrder");

        when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
        when(dashboardDecisionHelper.isDashBoardEnabledForCase(caseData)).thenReturn(true);
        when(dashboardDecisionHelper.isCarmApplicableCase(caseData)).thenReturn(true);
        when(dashboardDecisionHelper.isMediationUnsuccessfulReasonEqualToNotContactableClaimantOne(caseData))
            .thenReturn(true);
        when(dashboardDecisionHelper.hasTrackChanged(caseData)).thenReturn(true);
        when(dashboardDecisionHelper.hasUploadDocuments(caseData)).thenReturn(false);

        createSdoClaimantDashboardService.notifySdoCreated(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            AUTH_TOKEN,
            SCENARIO_AAA6_MEDIATION_UNSUCCESSFUL_TRACK_CHANGE_CLAIMANT_WITHOUT_UPLOAD_FILES_CARM.getScenario(),
            caseData.getCcdCaseReference().toString(),
            ScenarioRequestParams.builder().params(scenarioParams).build()
        );
        verifyNoInteractions(dashboardTasksHelper);
    }

    @Test
    void shouldRecordScenarioInSdo_whenInvokedForMediationUnsuccessfulCarmWithUploadDocuments() {
        CaseData caseData = CaseDataBuilder.builder().atStateTrialReadyCheck()
            .uploadMediationByDocumentTypes(MEDIATION_NON_ATTENDANCE_OPTION).build();
        Document document = new Document();
        document.setDocumentBinaryUrl("urlDirectionsOrder");
        CaseDocument caseDocument = new CaseDocument();
        caseDocument.setDocumentLink(document);
        caseData.setOrderSDODocumentDJCollection(List.of(ElementUtils.element(caseDocument)));
        caseData.setApplicant1Represented(YesOrNo.NO);
        caseData.setResponseClaimTrack(FAST_CLAIM.name());
        caseData.setTotalClaimAmount(BigDecimal.valueOf(999));
        Mediation mediation = new Mediation();
        mediation.setMediationUnsuccessfulReasonsMultiSelect(List.of(
            NOT_CONTACTABLE_CLAIMANT_ONE,
            NOT_CONTACTABLE_DEFENDANT_ONE
        ));
        caseData.setMediation(mediation);

        HashMap<String, Object> scenarioParams = new HashMap<>();
        scenarioParams.put("orderDocument", "urlDirectionsOrder");

        when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
        when(dashboardDecisionHelper.isDashBoardEnabledForCase(caseData)).thenReturn(true);
        when(dashboardDecisionHelper.isCarmApplicableCase(caseData)).thenReturn(true);
        when(dashboardDecisionHelper.isMediationUnsuccessfulReasonEqualToNotContactableClaimantOne(caseData))
            .thenReturn(true);
        when(dashboardDecisionHelper.hasTrackChanged(caseData)).thenReturn(true);
        when(dashboardDecisionHelper.hasUploadDocuments(caseData)).thenReturn(true);

        createSdoClaimantDashboardService.notifySdoCreated(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            AUTH_TOKEN,
            SCENARIO_AAA6_MEDIATION_UNSUCCESSFUL_TRACK_CHANGE_CLAIMANT_CARM.getScenario(),
            caseData.getCcdCaseReference().toString(),
            ScenarioRequestParams.builder().params(scenarioParams).build()
        );
        verifyNoInteractions(dashboardTasksHelper);
    }

    @Test
    void shouldRecordScenarioInSdoPreCPRelease_whenInvoked() {
        CaseData caseData = CaseDataBuilder.builder().atStateTrialReadyCheck().build();
        caseData.setApplicant1Represented(YesOrNo.NO);

        HashMap<String, Object> scenarioParams = new HashMap<>();
        scenarioParams.put("orderDocument", "urlDirectionsOrder");

        when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
        when(dashboardDecisionHelper.isDashBoardEnabledForCase(caseData)).thenReturn(true);
        when(dashboardDecisionHelper.isSDODrawnPreCPRelease(caseData)).thenReturn(true);

        createSdoClaimantDashboardService.notifySdoCreated(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            AUTH_TOKEN,
            SCENARIO_AAA6_CLAIMANT_SDO_DRAWN_PRE_CASE_PROGRESSION.getScenario(),
            caseData.getCcdCaseReference().toString(),
            ScenarioRequestParams.builder().params(scenarioParams).build()
        );
        verifyNoInteractions(dashboardTasksHelper);
    }

    @Test
    void shouldRecordScenarioInSdoLegalAdviser_whenInvoked() {
        HashMap<String, Object> scenarioParams = new HashMap<>();
        scenarioParams.put("orderDocument", "urlDirectionsOrder");

        CaseData caseData = CaseDataBuilder.builder().atStateTrialReadyCheck().build();
        caseData.setResponseClaimTrack("SMALL_CLAIM");
        caseData.setTotalClaimAmount(BigDecimal.valueOf(500));
        caseData.setApplicant1Represented(YesOrNo.NO);
        caseData.setSpecRespondent1Represented(YesOrNo.NO);
        caseData.setRespondent1Represented(YesOrNo.NO);

        when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
        when(dashboardDecisionHelper.isDashBoardEnabledForCase(caseData)).thenReturn(true);
        when(dashboardDecisionHelper.isEligibleForReconsideration(caseData)).thenReturn(true);

        createSdoClaimantDashboardService.notifySdoCreated(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            AUTH_TOKEN,
            SCENARIO_AAA6_CP_SDO_MADE_BY_LA_CLAIMANT.getScenario(),
            caseData.getCcdCaseReference().toString(),
            ScenarioRequestParams.builder().params(scenarioParams).build()
        );
        verify(dashboardTasksHelper).makeTasksInactiveForClaimant(caseData);
    }

    @Test
    void shouldRecordScenarioInSdoLegalAdviser_whenInvokedForNOC() {
        HashMap<String, Object> scenarioParams = new HashMap<>();
        scenarioParams.put("orderDocument", "urlDirectionsOrder");

        CaseData caseData = CaseDataBuilder.builder().atStateTrialReadyCheck().build();
        caseData.setResponseClaimTrack("SMALL_CLAIM");
        caseData.setSpecRespondent1Represented(YesOrNo.YES);
        caseData.setTotalClaimAmount(BigDecimal.valueOf(500));
        caseData.setApplicant1Represented(YesOrNo.NO);

        when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
        when(dashboardDecisionHelper.isDashBoardEnabledForCase(caseData)).thenReturn(true);
        when(dashboardDecisionHelper.isEligibleForReconsideration(caseData)).thenReturn(true);

        createSdoClaimantDashboardService.notifySdoCreated(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            AUTH_TOKEN,
            SCENARIO_AAA6_DEFENDANT_NOTICE_OF_CHANGE_CLAIM_REMAINS_ONLINE_CLAIMANT.getScenario(),
            caseData.getCcdCaseReference().toString(),
            ScenarioRequestParams.builder().params(scenarioParams).build()
        );

        verify(dashboardScenariosService).recordScenarios(
            AUTH_TOKEN,
            SCENARIO_AAA6_CP_SDO_MADE_BY_LA_CLAIMANT.getScenario(),
            caseData.getCcdCaseReference().toString(),
            ScenarioRequestParams.builder().params(scenarioParams).build()
        );
        verify(dashboardTasksHelper).makeTasksInactiveForClaimant(caseData);
    }

    @Test
    void shouldNotRecordScenarioInSdoLegalAdviser_whenInvokedWithFeatureToggleDisabled() {
        HashMap<String, Object> scenarioParams = new HashMap<>();
        scenarioParams.put("orderDocument", "urlDirectionsOrder");

        CaseData caseData = CaseDataBuilder.builder().atStateTrialReadyCheck().build();
        caseData.setResponseClaimTrack("SMALL_CLAIM");
        caseData.setTotalClaimAmount(BigDecimal.valueOf(500));
        caseData.setApplicant1Represented(YesOrNo.NO);

        when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
        when(dashboardDecisionHelper.isDashBoardEnabledForCase(caseData)).thenReturn(true);
        when(dashboardDecisionHelper.isEligibleForReconsideration(caseData)).thenReturn(false);
        when(dashboardDecisionHelper.isSDODrawnPreCPRelease(caseData)).thenReturn(true);

        createSdoClaimantDashboardService.notifySdoCreated(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService, never()).recordScenarios(
            AUTH_TOKEN,
            SCENARIO_AAA6_CP_SDO_MADE_BY_LA_CLAIMANT.getScenario(),
            caseData.getCcdCaseReference().toString(),
            ScenarioRequestParams.builder().params(scenarioParams).build()
        );

        verify(dashboardScenariosService).recordScenarios(
            AUTH_TOKEN,
            SCENARIO_AAA6_CLAIMANT_SDO_DRAWN_PRE_CASE_PROGRESSION.getScenario(),
            caseData.getCcdCaseReference().toString(),
            ScenarioRequestParams.builder().params(scenarioParams).build()
        );
        verifyNoInteractions(dashboardTasksHelper);
    }
}
