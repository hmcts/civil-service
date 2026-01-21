package uk.gov.hmcts.reform.civil.service.dashboardnotifications.uploadhearingdocuments;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocationCivil;
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
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CP_HEARING_DOCUMENTS_UPLOAD_DEFENDANT;

@ExtendWith(MockitoExtension.class)
class UploadHearingDocumentsDefendantServiceTest {

    private static final Long CASE_ID = 12349988L;
    private static final String AUTH_TOKEN = "Bearer";
    private static final String BASE_LOCATION = "00002";
    private ScenarioRequestParams scenarioRequestParams;

    @Mock
    private DashboardScenariosService dashboardScenariosService;
    @Mock
    private DashboardNotificationsParamsMapper mapper;
    @Mock
    private FeatureToggleService featureToggleService;

    @InjectMocks
    private UploadHearingDocumentsDefendantService uploadHearingDocumentsDefendantService;

    @BeforeEach
    void setup() {
        HashMap<String, Object> params = new HashMap<>();
        params.put("ccdCaseReference", CASE_ID.toString());
        scenarioRequestParams = ScenarioRequestParams.builder().params(params).build();
        when(mapper.mapCaseDataToParams(any())).thenReturn(params);
    }

    @Test
    void shouldCreateDashboardNotifications_ifRespondent1NotRepresented() {

        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
        when(featureToggleService.isCaseProgressionEnabledAndLocationWhiteListed(any())).thenReturn(true);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setRespondent1Represented(YesOrNo.NO);
        caseData.setCcdCaseReference(CASE_ID);
        caseData.setCcdState(CaseState.CASE_PROGRESSION);
        caseData.setCaseManagementLocation(new CaseLocationCivil().setBaseLocation(BASE_LOCATION));

        uploadHearingDocumentsDefendantService.notifyBundleUpdated(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            AUTH_TOKEN,
            SCENARIO_AAA6_CP_HEARING_DOCUMENTS_UPLOAD_DEFENDANT.getScenario(),
            caseData.getCcdCaseReference().toString(),
            scenarioRequestParams
        );
    }

    @Test
    void shouldNotCreateDashboardNotifications_ifRespondent1Represented() {

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setRespondent1Represented(YesOrNo.YES);
        caseData.setCcdCaseReference(CASE_ID);
        caseData.setCcdState(CaseState.CASE_PROGRESSION);
        caseData.setCaseManagementLocation(new CaseLocationCivil().setBaseLocation(BASE_LOCATION));

        uploadHearingDocumentsDefendantService.notifyBundleUpdated(caseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardScenariosService);
    }

    @Test
    void shouldNotCreateDashboardNotifications_ifCcdStateNotCaseProgression() {

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setRespondent1Represented(YesOrNo.NO);
        caseData.setCcdCaseReference(CASE_ID);
        caseData.setCcdState(CaseState.CASE_ISSUED);
        caseData.setCaseManagementLocation(new CaseLocationCivil().setBaseLocation(BASE_LOCATION));

        uploadHearingDocumentsDefendantService.notifyBundleUpdated(caseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardScenariosService);
    }

    @Test
    void shouldCreateDashboardNotificationsAfterNroChangesAndWelshEnabledForMainCase() {

        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
        when(featureToggleService.isCaseProgressionEnabledAndLocationWhiteListed(any())).thenReturn(false);
        when(featureToggleService.isWelshEnabledForMainCase()).thenReturn(true);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setRespondent1Represented(YesOrNo.NO);
        caseData.setCcdCaseReference(CASE_ID);
        caseData.setCcdState(CaseState.CASE_PROGRESSION);
        caseData.setCaseManagementLocation(new CaseLocationCivil().setBaseLocation(BASE_LOCATION));

        uploadHearingDocumentsDefendantService.notifyBundleUpdated(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            AUTH_TOKEN,
            SCENARIO_AAA6_CP_HEARING_DOCUMENTS_UPLOAD_DEFENDANT.getScenario(),
            caseData.getCcdCaseReference().toString(),
            scenarioRequestParams
        );
    }

    @Test
    void shouldCreateDashboardNotificationsAfterNroChangesAndWelshNotEnabledForMainCase() {

        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
        when(featureToggleService.isCaseProgressionEnabledAndLocationWhiteListed(any())).thenReturn(false);
        when(featureToggleService.isWelshEnabledForMainCase()).thenReturn(false);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setRespondent1Represented(YesOrNo.NO);
        caseData.setCcdCaseReference(CASE_ID);
        caseData.setCcdState(CaseState.CASE_PROGRESSION);
        caseData.setCaseManagementLocation(new CaseLocationCivil().setBaseLocation(BASE_LOCATION));

        uploadHearingDocumentsDefendantService.notifyBundleUpdated(caseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardScenariosService);
    }

    @Test
    void shouldNotCreateDashboardNotifications_ifNotLipVLipEnabled() {

        when(featureToggleService.isLipVLipEnabled()).thenReturn(false);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setRespondent1Represented(YesOrNo.NO);
        caseData.setCcdCaseReference(CASE_ID);
        caseData.setCcdState(CaseState.CASE_PROGRESSION);
        caseData.setCaseManagementLocation(new CaseLocationCivil().setBaseLocation(BASE_LOCATION));

        uploadHearingDocumentsDefendantService.notifyBundleUpdated(caseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardScenariosService);
    }
}
