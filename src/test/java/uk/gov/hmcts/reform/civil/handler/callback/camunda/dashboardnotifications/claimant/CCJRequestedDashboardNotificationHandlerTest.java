package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimant;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.RespondToClaimAdmitPartLRspec;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_DASHBOARD_NOTIFICATION_FOR_CCJ_REQUEST_FOR_APPLICANT1;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_CCJ_REQUESTED_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_REQUESTED_CCJ_CLAIMANT;

@ExtendWith(MockitoExtension.class)
class CCJRequestedDashboardNotificationHandlerTest extends BaseCallbackHandlerTest {

    @InjectMocks
    private CCJRequestedDashboardNotificationHandler handler;

    @Mock
    private DashboardScenariosService dashboardScenariosService;

    @Mock
    private DashboardNotificationsParamsMapper dashboardNotificationsParamsMapper;

    @Mock
    private FeatureToggleService featureToggleService;

    public static final String TASK_ID = "GenerateDashboardNotificationClaimantIntentCCJRequestedForApplicant1";

    HashMap<String, Object> params = new HashMap<>();

    @Test
    void handleEventsReturnsTheExpectedCallbackEvent() {
        assertThat(handler.handledEvents()).contains(CREATE_DASHBOARD_NOTIFICATION_FOR_CCJ_REQUEST_FOR_APPLICANT1);
    }

    @Test
    void shouldReturnCorrectCamundaActivityId_whenInvoked() {
        assertThat(handler.camundaActivityId(
            CallbackParamsBuilder.builder()
                .request(CallbackRequest.builder()
                             .eventId(CREATE_DASHBOARD_NOTIFICATION_FOR_CCJ_REQUEST_FOR_APPLICANT1.name())
                             .build())
                .build()))
            .isEqualTo(TASK_ID);
    }

    @Test
    void createDashboardNotificationsWhenDJSubmitted() {

        params.put("ccdCaseReference", "123");
        params.put("defaultRespondTime", "4pm");
        params.put("responseDeadline", "11 March 2024");

        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
        when(dashboardNotificationsParamsMapper.mapCaseDataToParams(any())).thenReturn(params);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setLegacyCaseReference("reference");
        caseData.setCcdCaseReference(1234L);
        caseData.setApplicant1Represented(YesOrNo.NO);
        LocalDateTime dateTime = LocalDate.of(2020, Month.JANUARY, 18).atStartOfDay();
        caseData.setRespondent1ResponseDeadline(dateTime);
        CaseDocument caseDocument = new CaseDocument();
        caseDocument.setDocumentType(DocumentType.DEFAULT_JUDGMENT);
        caseDocument.setCreatedDatetime(LocalDateTime.now());
        Element<CaseDocument> element = new Element<>();
        element.setValue(caseDocument);
        List<Element<CaseDocument>> elements = List.of(element);
        caseData.setDefaultJudgmentDocuments(elements);

        CallbackParams callbackParams = CallbackParamsBuilder.builder()
            .of(ABOUT_TO_SUBMIT, caseData)
            .build();

        handler.handle(callbackParams);
        verify(dashboardScenariosService).recordScenarios(
            "BEARER_TOKEN",
            SCENARIO_AAA6_CLAIMANT_INTENT_CCJ_REQUESTED_CLAIMANT.getScenario(),
            caseData.getCcdCaseReference().toString(),
            ScenarioRequestParams.builder().params(params).build()
        );
    }

    @Test
    void createDashboardNotificationsWhenDJSubmittedForLipvLR() {

        params.put("ccdCaseReference", "123");
        params.put("defaultRespondTime", "4pm");
        params.put("responseDeadline", "11 March 2024");

        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
        when(dashboardNotificationsParamsMapper.mapCaseDataToParams(any())).thenReturn(params);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setLegacyCaseReference("reference");
        caseData.setCcdCaseReference(1234L);
        caseData.setApplicant1Represented(YesOrNo.NO);
        LocalDateTime dateTime = LocalDate.of(2020, Month.JANUARY, 18).atStartOfDay();
        caseData.setRespondent1ResponseDeadline(dateTime);
        caseData.setRepaymentSummaryObject("Test String");

        CallbackParams callbackParams = CallbackParamsBuilder.builder()
            .of(ABOUT_TO_SUBMIT, caseData)
            .build();

        handler.handle(callbackParams);
        verify(dashboardScenariosService).recordScenarios(
            "BEARER_TOKEN",
            SCENARIO_AAA6_CLAIMANT_INTENT_CCJ_REQUESTED_CLAIMANT.getScenario(),
            caseData.getCcdCaseReference().toString(),
            ScenarioRequestParams.builder().params(params).build()
        );
    }

    @Test
    void createDashboardNotificationsWhenJBASubmitted() {

        params.put("ccdCaseReference", "123");
        params.put("defaultRespondTime", "4pm");

        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
        when(dashboardNotificationsParamsMapper.mapCaseDataToParams(any())).thenReturn(params);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setLegacyCaseReference("reference");
        caseData.setCcdCaseReference(1234L);
        caseData.setApplicant1Represented(YesOrNo.NO);
        caseData.setRespondent1ResponseDeadline(LocalDateTime.now());
        caseData.setCcdState(CaseState.AWAITING_APPLICANT_INTENTION);
        caseData.setRespondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION);
        caseData.setDefenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY);
        RespondToClaimAdmitPartLRspec respondToClaimAdmitPartLRspec = new RespondToClaimAdmitPartLRspec();
        respondToClaimAdmitPartLRspec.setWhenWillThisAmountBePaid(LocalDate.now().minusDays(1));
        caseData.setRespondToClaimAdmitPartLRspec(respondToClaimAdmitPartLRspec);

        CallbackParams callbackParams = CallbackParamsBuilder.builder()
            .of(ABOUT_TO_SUBMIT, caseData)
            .build();

        handler.handle(callbackParams);
        verify(dashboardScenariosService).recordScenarios(
            "BEARER_TOKEN",
            SCENARIO_AAA6_CLAIMANT_INTENT_CCJ_REQUESTED_CLAIMANT.getScenario(),
            caseData.getCcdCaseReference().toString(),
            ScenarioRequestParams.builder().params(params).build()
        );
    }

    @ParameterizedTest
    @MethodSource("provideDefendantSignSettlementData")
    void createDashboardNotificationsWhenDefendantBreakSSA(CaseData caseData) {

        params.put("claimantRepaymentPlanDecision", "accepted");
        params.put("claimantRepaymentPlanDecisionCy", "derbyn");
        params.put("respondent1PartyName", "Mr Defendant Guy");

        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
        when(dashboardNotificationsParamsMapper.mapCaseDataToParams(any())).thenReturn(params);

        CallbackParams callbackParams = CallbackParamsBuilder.builder()
            .of(ABOUT_TO_SUBMIT, caseData)
            .build();

        handler.handle(callbackParams);
        verify(dashboardScenariosService).recordScenarios(
            "BEARER_TOKEN",
            SCENARIO_AAA6_CLAIMANT_INTENT_REQUESTED_CCJ_CLAIMANT.getScenario(),
            caseData.getCcdCaseReference().toString(),
            ScenarioRequestParams.builder().params(params).build()
        );
    }

    static Stream<Arguments> provideDefendantSignSettlementData() {

        CaseData defendantRejectedSSA = CaseDataBuilder.builder().build();
        defendantRejectedSSA.setLegacyCaseReference("reference");
        defendantRejectedSSA.setCcdCaseReference(1234L);
        defendantRejectedSSA.setApplicant1Represented(YesOrNo.NO);
        defendantRejectedSSA.setRespondent1ResponseDeadline(LocalDateTime.now());
        CaseDataLiP caseDataLiP = new CaseDataLiP();
        caseDataLiP.setRespondentSignSettlementAgreement(YesOrNo.NO);
        defendantRejectedSSA.setCaseDataLiP(caseDataLiP);
        defendantRejectedSSA.setCcdState(CaseState.AWAITING_APPLICANT_INTENTION);

        CaseData defendantNotRespondedToSSA = CaseDataBuilder.builder().build();
        defendantNotRespondedToSSA.setLegacyCaseReference("reference");
        defendantNotRespondedToSSA.setApplicant1Represented(YesOrNo.NO);
        defendantNotRespondedToSSA.setCcdCaseReference(1234L);
        defendantNotRespondedToSSA.setRespondent1ResponseDeadline(LocalDateTime.now());
        defendantNotRespondedToSSA.setRespondent1RespondToSettlementAgreementDeadline(LocalDateTime.of(2024, 3, 1, 12, 0, 0));
        defendantNotRespondedToSSA.setCcdState(CaseState.AWAITING_APPLICANT_INTENTION);

        CaseData defendantBreakSSA = CaseDataBuilder.builder().build();
        defendantBreakSSA.setLegacyCaseReference("reference");
        defendantBreakSSA.setCcdCaseReference(1234L);
        defendantBreakSSA.setApplicant1Represented(YesOrNo.NO);
        defendantBreakSSA.setRespondent1ResponseDeadline(LocalDateTime.now());
        defendantBreakSSA.setRespondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION);
        defendantBreakSSA.setDefenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY);
        RespondToClaimAdmitPartLRspec respondToClaimAdmitPartLRspec = new RespondToClaimAdmitPartLRspec();
        respondToClaimAdmitPartLRspec.setWhenWillThisAmountBePaid(LocalDate.now().minusDays(1));
        defendantBreakSSA.setRespondToClaimAdmitPartLRspec(respondToClaimAdmitPartLRspec);
        defendantBreakSSA.setCcdState(CaseState.AWAITING_APPLICANT_INTENTION);

        return Stream.of(
            Arguments.of(defendantRejectedSSA),
            Arguments.of(defendantNotRespondedToSSA),
            Arguments.of(defendantBreakSSA)
        );

    }

}
