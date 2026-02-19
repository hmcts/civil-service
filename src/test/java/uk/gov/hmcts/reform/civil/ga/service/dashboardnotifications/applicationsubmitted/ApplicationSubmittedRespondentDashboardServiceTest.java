package uk.gov.hmcts.reform.civil.ga.service.dashboardnotifications.applicationsubmitted;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.ga.client.DashboardApiClient;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.service.GaDashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.model.genapplication.GAInformOtherParty;
import uk.gov.hmcts.reform.civil.model.genapplication.GAUrgencyRequirement;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPLICATION_SUBMITTED_NONURGENT_RESPONDENT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPLICATION_SUBMITTED_URGENT_RESPONDENT;

@ExtendWith(MockitoExtension.class)
class ApplicationSubmittedRespondentDashboardServiceTest {

    private static final String AUTH_TOKEN = "auth-token";

    @Mock
    private DashboardApiClient dashboardApiClient;

    @Mock
    private GaDashboardNotificationsParamsMapper mapper;

    @InjectMocks
    private ApplicationSubmittedRespondentDashboardService service;

    @Test
    void shouldRecordNonUrgentScenarioWhenWithNotice() {
        GeneralApplicationCaseData caseData = baseCase()
            .toBuilder()
            .generalAppInformOtherParty(GAInformOtherParty.builder().isWithNotice(YesOrNo.YES).build())
            .generalAppUrgencyRequirement(GAUrgencyRequirement.builder().generalAppUrgency(YesOrNo.NO).build())
            .build();

        assertScenarioRecorded(caseData, SCENARIO_AAA6_GENERAL_APPLICATION_SUBMITTED_NONURGENT_RESPONDENT.getScenario());
    }

    @Test
    void shouldRecordUrgentScenarioWhenWithNoticeAndUrgent() {
        GeneralApplicationCaseData caseData = baseCase()
            .toBuilder()
            .generalAppInformOtherParty(GAInformOtherParty.builder().isWithNotice(YesOrNo.YES).build())
            .generalAppUrgencyRequirement(GAUrgencyRequirement.builder().generalAppUrgency(YesOrNo.YES).build())
            .build();

        assertScenarioRecorded(caseData, SCENARIO_AAA6_GENERAL_APPLICATION_SUBMITTED_URGENT_RESPONDENT.getScenario());
    }

    @Test
    void shouldRecordNonUrgentScenarioWhenWithConsent() {
        GeneralApplicationCaseData caseData = baseCase()
            .toBuilder()
            .generalAppConsentOrder(YesOrNo.YES)
            .generalAppUrgencyRequirement(GAUrgencyRequirement.builder().generalAppUrgency(YesOrNo.NO).build())
            .build();

        assertScenarioRecorded(caseData, SCENARIO_AAA6_GENERAL_APPLICATION_SUBMITTED_NONURGENT_RESPONDENT.getScenario());
    }

    @Test
    void shouldRecordUrgentScenarioWhenWithConsentAndUrgent() {
        GeneralApplicationCaseData caseData = baseCase()
            .toBuilder()
            .generalAppConsentOrder(YesOrNo.YES)
            .generalAppUrgencyRequirement(GAUrgencyRequirement.builder().generalAppUrgency(YesOrNo.YES).build())
            .build();

        assertScenarioRecorded(caseData, SCENARIO_AAA6_GENERAL_APPLICATION_SUBMITTED_URGENT_RESPONDENT.getScenario());
    }

    @Test
    void shouldNotRecordScenarioWhenWithoutNoticeOrConsent() {
        GeneralApplicationCaseData caseData = baseCase()
            .toBuilder()
            .generalAppInformOtherParty(GAInformOtherParty.builder().isWithNotice(YesOrNo.NO).build())
            .generalAppConsentOrder(YesOrNo.NO)
            .build();

        service.notifyApplicationSubmitted(caseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardApiClient);
    }

    private GeneralApplicationCaseData baseCase() {
        return GeneralApplicationCaseData.builder()
            .ccdCaseReference(789012L)
            .isGaRespondentOneLip(YesOrNo.YES)
            .isMultiParty(YesOrNo.NO)
            .generalAppInformOtherParty(GAInformOtherParty.builder().isWithNotice(YesOrNo.NO).build())
            .generalAppConsentOrder(YesOrNo.NO)
            .build();
    }

    private void assertScenarioRecorded(GeneralApplicationCaseData caseData, String scenario) {
        HashMap<String, Object> params = new HashMap<>();
        when(mapper.mapCaseDataToParams(caseData)).thenReturn(params);

        service.notifyApplicationSubmitted(caseData, AUTH_TOKEN);

        verify(dashboardApiClient).recordScenario(
            caseData.getCcdCaseReference().toString(),
            scenario,
            AUTH_TOKEN,
            ScenarioRequestParams.builder().params(params).build()
        );
    }

    @Test
    void shouldRecordScenario_true_whenRespondentOneLipYes() {
        GeneralApplicationCaseData caseData = GeneralApplicationCaseData.builder()
            .isGaRespondentOneLip(YesOrNo.YES)
            .build();
        assertTrue(service.shouldRecordScenario(caseData));
    }

    @Test
    void shouldRecordScenario_false_whenSinglePartyAndRespondentOneLipNo() {
        GeneralApplicationCaseData caseData = GeneralApplicationCaseData.builder()
            .isMultiParty(YesOrNo.NO)
            .isGaRespondentOneLip(YesOrNo.NO)
            .build();
        assertFalse(service.shouldRecordScenario(caseData));
    }

    @Test
    void shouldRecordScenario_false_whenSinglePartyAndRespondentOneLipNull() {
        GeneralApplicationCaseData caseData = GeneralApplicationCaseData.builder()
            .isMultiParty(YesOrNo.NO)
            .build();
        assertFalse(service.shouldRecordScenario(caseData));
    }

    @Test
    void shouldRecordScenario_true_whenMultiPartyAndRespondentTwoLipYes() {
        GeneralApplicationCaseData caseData = GeneralApplicationCaseData.builder()
            .isMultiParty(YesOrNo.YES)
            .isGaRespondentOneLip(YesOrNo.NO)
            .isGaRespondentTwoLip(YesOrNo.YES)
            .build();
        assertTrue(service.shouldRecordScenario(caseData));
    }

    @Test
    void shouldRecordScenario_false_whenMultiPartyAndRespondentTwoLipNoAndRespondentOneLipNo() {
        GeneralApplicationCaseData caseData = GeneralApplicationCaseData.builder()
            .isMultiParty(YesOrNo.YES)
            .isGaRespondentOneLip(YesOrNo.NO)
            .isGaRespondentTwoLip(YesOrNo.NO)
            .build();
        assertFalse(service.shouldRecordScenario(caseData));
    }

    @Test
    void shouldRecordScenario_true_whenMultiPartyRespondentOneLipYesEvenIfRespondentTwoLipNo() {
        GeneralApplicationCaseData caseData = GeneralApplicationCaseData.builder()
            .isMultiParty(YesOrNo.YES)
            .isGaRespondentOneLip(YesOrNo.YES)
            .isGaRespondentTwoLip(YesOrNo.NO)
            .build();
        assertTrue(service.shouldRecordScenario(caseData));
    }
}
