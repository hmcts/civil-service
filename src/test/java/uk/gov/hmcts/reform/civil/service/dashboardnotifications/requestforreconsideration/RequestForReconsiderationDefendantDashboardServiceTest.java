package uk.gov.hmcts.reform.civil.service.dashboardnotifications.requestforreconsideration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import java.util.HashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CP_REQUEST_FOR_RECONSIDERATION_REQUESTED_BY_OTHER_PARTY_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CP_REQUEST_FOR_RECONSIDERATION_REQUESTED_BY_OTHER_PARTY_DEFENDANT_RECIPIENT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CP_REQUEST_FOR_RECONSIDERATION_REQUESTED_BY_OTHER_PARTY_DELETE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CP_REQUEST_FOR_RECONSIDERATION_REQUESTED_BY_OTHER_PARTY_LR_RECIPIENT_DELETE;

@ExtendWith(MockitoExtension.class)
class RequestForReconsiderationDefendantDashboardServiceTest {

    @InjectMocks
    private RequestForReconsiderationDefendantDashboardService handler;
    @Mock
    private DashboardScenariosService dashboardScenariosService;
    @Mock
    private DashboardNotificationsParamsMapper dashboardNotificationsParamsMapper;

    public static final String TASK_ID = "CreateNotificationRequestForReconsiderationDefendant";

    @Test
    void configureDashboardNotificationsForDefendantRequest() {

        HashMap<String, Object> params = new HashMap<>();

        when(dashboardNotificationsParamsMapper.mapCaseDataToParams(any())).thenReturn(params);

        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build();
        caseData.setRespondent1Represented(YesOrNo.NO);

        handler.notifyRequestForReconsideration(caseData, "BEARER_TOKEN");

        verify(dashboardScenariosService).recordScenarios(
            "BEARER_TOKEN",
            SCENARIO_AAA6_CP_REQUEST_FOR_RECONSIDERATION_REQUESTED_BY_OTHER_PARTY_DEFENDANT.getScenario(),
            caseData.getCcdCaseReference().toString(),
            new ScenarioRequestParams(params)
        );
    }

    @Test
    void configureDashboardNotificationsForDefendantRequestOtherPartyLR() {

        HashMap<String, Object> params = new HashMap<>();

        when(dashboardNotificationsParamsMapper.mapCaseDataToParams(any())).thenReturn(params);

        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build();
        caseData.setRespondent1Represented(YesOrNo.YES);

        handler.notifyRequestForReconsideration(caseData, "BEARER_TOKEN");

        verify(dashboardScenariosService).recordScenarios(
            "BEARER_TOKEN",
            SCENARIO_AAA6_CP_REQUEST_FOR_RECONSIDERATION_REQUESTED_BY_OTHER_PARTY_DELETE.getScenario(),
            caseData.getCcdCaseReference().toString(),
            new ScenarioRequestParams(params)
        );
    }

    @Test
    void configureDashboardNotificationsForDefendantRequestOtherPartyLRSecondRequest() {

        HashMap<String, Object> params = new HashMap<>();

        when(dashboardNotificationsParamsMapper.mapCaseDataToParams(any())).thenReturn(params);

        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build();
        caseData.setRespondent1Represented(YesOrNo.YES);
        caseData.setOrderRequestedForReviewDefendant(YesOrNo.YES);
        caseData.setOrderRequestedForReviewClaimant(YesOrNo.YES);

        handler.notifyRequestForReconsideration(caseData, "BEARER_TOKEN");

        verify(dashboardScenariosService).recordScenarios(
            "BEARER_TOKEN",
            SCENARIO_AAA6_CP_REQUEST_FOR_RECONSIDERATION_REQUESTED_BY_OTHER_PARTY_LR_RECIPIENT_DELETE.getScenario(),
            caseData.getCcdCaseReference().toString(),
            new ScenarioRequestParams(params)
        );
    }

    @Test
    void configureDashboardNotificationsForDefendantRequestSecondRequest() {

        HashMap<String, Object> params = new HashMap<>();

        when(dashboardNotificationsParamsMapper.mapCaseDataToParams(any())).thenReturn(params);

        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build();
        caseData.setRespondent1Represented(YesOrNo.NO);
        caseData.setOrderRequestedForReviewDefendant(YesOrNo.YES);
        caseData.setOrderRequestedForReviewClaimant(YesOrNo.YES);

        handler.notifyRequestForReconsideration(caseData, "BEARER_TOKEN");

        verify(dashboardScenariosService).recordScenarios(
            "BEARER_TOKEN",
            SCENARIO_AAA6_CP_REQUEST_FOR_RECONSIDERATION_REQUESTED_BY_OTHER_PARTY_DEFENDANT_RECIPIENT.getScenario(),
            caseData.getCcdCaseReference().toString(),
            new ScenarioRequestParams(params)
        );
    }

    @ParameterizedTest
    @CsvSource({"YES,NO", "NO,YES", "NO,NO"})
    void shouldUseOrdinaryDeletionForRepresentedRecipientUntilBothPartiesRequestReview(YesOrNo claimantReview,
                                                                                     YesOrNo defendantReview) {
        HashMap<String, Object> params = new HashMap<>();
        when(dashboardNotificationsParamsMapper.mapCaseDataToParams(any())).thenReturn(params);
        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build();
        caseData.setRespondent1Represented(YesOrNo.YES);
        caseData.setOrderRequestedForReviewClaimant(claimantReview);
        caseData.setOrderRequestedForReviewDefendant(defendantReview);

        handler.notifyRequestForReconsideration(caseData, "BEARER_TOKEN");

        verify(dashboardScenariosService).recordScenarios("BEARER_TOKEN",
            SCENARIO_AAA6_CP_REQUEST_FOR_RECONSIDERATION_REQUESTED_BY_OTHER_PARTY_DELETE.getScenario(),
            caseData.getCcdCaseReference().toString(), new ScenarioRequestParams(params));
    }
}
