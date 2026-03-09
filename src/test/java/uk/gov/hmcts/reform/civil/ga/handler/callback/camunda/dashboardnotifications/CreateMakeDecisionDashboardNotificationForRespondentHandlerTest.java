package uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.dashboardnotifications;

import org.junit.jupiter.api.Nested;
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
import uk.gov.hmcts.reform.civil.ga.client.DashboardApiClient;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.ga.enums.MakeAppAvailableCheckGAspec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeDecisionOption;
import uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeMakeAnOrderOption;
import uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeRequestMoreInfoOption;
import uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeWrittenRepresentationsOptions;
import uk.gov.hmcts.reform.civil.ga.handler.GeneralApplicationBaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.sampledata.GeneralApplicationCaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAHearingNoticeApplication;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAHearingNoticeDetail;
import uk.gov.hmcts.reform.civil.model.genapplication.GAInformOtherParty;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAJudicialDecision;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAJudicialMakeAnOrder;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAJudicialRequestMoreInfo;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAJudicialWrittenRepresentations;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAMakeApplicationAvailableCheck;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.ga.service.GaDashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_APPLICANT_DASHBOARD_NOTIFICATION_FOR_MAKE_DECISION;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_RESPONDENT_DASHBOARD_NOTIFICATION_FOR_MAKE_DECISION;
import static uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeDecisionOption.MAKE_AN_ORDER;
import static uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeMakeAnOrderOption.APPROVE_OR_EDIT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPLICATION_HEARING_SCHEDULED_RESPONDENT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPLICATION_JUDGE_UNCLOAK_RESPONDENT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPLICATION_ORDER_MADE_RESPONDENT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPLICATION_REQUEST_MORE_INFO_RESPONDENT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPLICATION_WRITTEN_REPRESENTATION_REQUIRED_RESPONDENT;

@ExtendWith(MockitoExtension.class)
public class CreateMakeDecisionDashboardNotificationForRespondentHandlerTest extends GeneralApplicationBaseCallbackHandlerTest {

    @Mock
    private DashboardApiClient dashboardApiClient;
    @Mock
    private GaDashboardNotificationsParamsMapper mapper;
    @Mock
    private FeatureToggleService featureToggleService;
    @InjectMocks
    private CreateMakeDecisionDashboardNotificationForRespondentHandler handler;

    private final List<MakeAppAvailableCheckGAspec> makeAppAvailableCheck = List.of(MakeAppAvailableCheckGAspec.CONSENT_AGREEMENT_CHECKBOX);

    private final GAMakeApplicationAvailableCheck gaMakeApplicationAvailableCheck = new GAMakeApplicationAvailableCheck()
        .setMakeAppAvailableCheck(makeAppAvailableCheck);

    @Test
    void handleEventsReturnsTheExpectedCallbackEvent() {
        assertThat(handler.handledEvents()).contains(CREATE_RESPONDENT_DASHBOARD_NOTIFICATION_FOR_MAKE_DECISION);
    }

    @Test
    void shouldReturnCorrectCamundaActivityId_whenInvoked() {
        assertThat(handler.camundaActivityId(new CallbackParams()))
            .isEqualTo("default");
    }

    @Nested
    class AboutToSubmitCallback {

        @Test
        void shouldRecordMoreInfoRequiredRespondentScenarioWhenInvoked() {
            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().atStateClaimDraft().withNoticeCaseData();
            caseData = caseData.copy()
                .parentCaseReference(caseData.getCcdCaseReference().toString())
                .isGaApplicantLip(YesOrNo.YES)
                .parentClaimantIsApplicant(YesOrNo.YES)
                .judicialDecisionRequestMoreInfo(new GAJudicialRequestMoreInfo().setDeadlineForMoreInfoSubmission(
                    LocalDateTime.now().plusDays(5)))
                .ccdState(CaseState.APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION)
                .build();

            HashMap<String, Object> scenarioParams = new HashMap<>();
            when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(CREATE_RESPONDENT_DASHBOARD_NOTIFICATION_FOR_MAKE_DECISION.name())
                    .build()
            ).build();

            handler.handle(params);
            verify(dashboardApiClient).recordScenario(
                caseData.getCcdCaseReference().toString(),
                SCENARIO_AAA6_GENERAL_APPLICATION_REQUEST_MORE_INFO_RESPONDENT.getScenario(),
                "BEARER_TOKEN",
                ScenarioRequestParams.builder().params(scenarioParams).build()
            );
        }

        @Test
        void shouldRecordMoreInfoRequiredRespondentScenarioWhenAppIsWithoutNoticeInvoked() {
            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().atStateClaimDraft().withoutNoticeCaseData();
            caseData = caseData.copy()
                .parentCaseReference(caseData.getCcdCaseReference().toString())
                .isGaApplicantLip(YesOrNo.YES)
                .parentClaimantIsApplicant(YesOrNo.YES)
                .judicialDecisionRequestMoreInfo(new GAJudicialRequestMoreInfo().setRequestMoreInfoOption(
                    GAJudgeRequestMoreInfoOption.REQUEST_MORE_INFORMATION).setDeadlineForMoreInfoSubmission(
                    LocalDateTime.now().plusDays(5)))
                .ccdState(CaseState.APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION)
                .build();

            HashMap<String, Object> scenarioParams = new HashMap<>();
            when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(CREATE_RESPONDENT_DASHBOARD_NOTIFICATION_FOR_MAKE_DECISION.name())
                    .build()
            ).build();

            handler.handle(params);
            verify(dashboardApiClient).recordScenario(
                caseData.getCcdCaseReference().toString(),
                SCENARIO_AAA6_GENERAL_APPLICATION_REQUEST_MORE_INFO_RESPONDENT.getScenario(),
                "BEARER_TOKEN",
                ScenarioRequestParams.builder().params(scenarioParams).build()
            );
        }

        @Test
        void shouldRecordApplicationUncloakedRespondentScenarioWhenAppIsWithoutNoticeInvoked() {
            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().atStateClaimDraft().withoutNoticeCaseData();
            caseData = caseData.copy()
                .parentCaseReference(caseData.getCcdCaseReference().toString())
                .isGaApplicantLip(YesOrNo.YES)
                .parentClaimantIsApplicant(YesOrNo.YES)
                .applicationIsUncloakedOnce(YesOrNo.YES)
                .makeAppVisibleToRespondents(gaMakeApplicationAvailableCheck)
                .build();

            HashMap<String, Object> scenarioParams = new HashMap<>();
            when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(CREATE_RESPONDENT_DASHBOARD_NOTIFICATION_FOR_MAKE_DECISION.name())
                    .build()
            ).build();

            handler.handle(params);
            verify(dashboardApiClient).recordScenario(
                caseData.getCcdCaseReference().toString(),
                SCENARIO_AAA6_GENERAL_APPLICATION_JUDGE_UNCLOAK_RESPONDENT.getScenario(),
                "BEARER_TOKEN",
                ScenarioRequestParams.builder().params(scenarioParams).build()
            );
        }

        @Test
        void shouldNotRecordApplicationUncloakedRespondentScenarioWhenAppIsWithNoticeInvoked() {
            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().atStateClaimDraft().withNoticeCaseData();
            caseData = caseData.copy()
                .parentCaseReference(caseData.getCcdCaseReference().toString())
                .isGaApplicantLip(YesOrNo.YES)
                .parentClaimantIsApplicant(YesOrNo.YES)
                .applicationIsUncloakedOnce(YesOrNo.NO)
                .ccdState(CaseState.APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION)
                .makeAppVisibleToRespondents(gaMakeApplicationAvailableCheck)
                .generalAppInformOtherParty(GAInformOtherParty.builder().isWithNotice(YesOrNo.YES).build())
                .build();

            HashMap<String, Object> scenarioParams = new HashMap<>();
            when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(CREATE_RESPONDENT_DASHBOARD_NOTIFICATION_FOR_MAKE_DECISION.name())
                    .build()
            ).build();

            handler.handle(params);
            verifyNoInteractions(dashboardApiClient);
        }

        @Test
        void shouldNotRecordApplicationUncloakedRespondentScenarioWhenAppIsNotUncloakedInvoked() {
            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().atStateClaimDraft().withNoticeCaseData();
            caseData = caseData.copy()
                .parentCaseReference(caseData.getCcdCaseReference().toString())
                .isGaApplicantLip(YesOrNo.YES)
                .parentClaimantIsApplicant(YesOrNo.YES)
                .applicationIsUncloakedOnce(YesOrNo.NO)
                .makeAppVisibleToRespondents(gaMakeApplicationAvailableCheck)
                .generalAppInformOtherParty(GAInformOtherParty.builder().isWithNotice(YesOrNo.NO).build())
                .ccdState(CaseState.APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION)
                .build();

            HashMap<String, Object> scenarioParams = new HashMap<>();
            when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(CREATE_RESPONDENT_DASHBOARD_NOTIFICATION_FOR_MAKE_DECISION.name())
                    .build()
            ).build();

            handler.handle(params);
            verifyNoInteractions(dashboardApiClient);
        }

        @Test
        void shouldRecordRequestWrittenRepresentationsRespondentScenarioWhenInvoked() {
            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().atStateClaimDraft().withNoticeCaseData();
            caseData = caseData.copy()
                .ccdState(CaseState.APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION)
                .parentCaseReference(caseData.getCcdCaseReference().toString())
                .judicialDecisionMakeAnOrderForWrittenRepresentations(new GAJudicialWrittenRepresentations())
                .judicialDecision(new GAJudicialDecision().setDecision(GAJudgeDecisionOption.MAKE_ORDER_FOR_WRITTEN_REPRESENTATIONS))
                .generalAppInformOtherParty(GAInformOtherParty.builder().isWithNotice(YesOrNo.YES).build())
                .build();

            HashMap<String, Object> scenarioParams = new HashMap<>();
            when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(CREATE_RESPONDENT_DASHBOARD_NOTIFICATION_FOR_MAKE_DECISION.name())
                    .build()
            ).build();

            handler.handle(params);
            verify(dashboardApiClient).recordScenario(
                caseData.getCcdCaseReference().toString(),
                SCENARIO_AAA6_GENERAL_APPLICATION_WRITTEN_REPRESENTATION_REQUIRED_RESPONDENT.getScenario(),
                "BEARER_TOKEN",
                ScenarioRequestParams.builder().params(scenarioParams).build()
            );
        }

        @Test
        void shouldNotRecordRequestWrittenRepresentationsRespondentScenarioWhenAppIsNotUncloakedInvoked() {
            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().atStateClaimDraft().withNoticeCaseData();
            caseData = caseData.copy()
                .parentCaseReference(caseData.getCcdCaseReference().toString())
                .judicialDecisionMakeAnOrderForWrittenRepresentations(new GAJudicialWrittenRepresentations())
                .judicialDecision(new GAJudicialDecision().setDecision(GAJudgeDecisionOption.MAKE_ORDER_FOR_WRITTEN_REPRESENTATIONS))
                .generalAppInformOtherParty(GAInformOtherParty.builder().isWithNotice(YesOrNo.NO).build())
                .ccdState(CaseState.APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION)
                .build();

            HashMap<String, Object> scenarioParams = new HashMap<>();
            when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(CREATE_RESPONDENT_DASHBOARD_NOTIFICATION_FOR_MAKE_DECISION.name())
                    .build()
            ).build();

            handler.handle(params);
            verify(dashboardApiClient).recordScenario(
                caseData.getCcdCaseReference().toString(),
                SCENARIO_AAA6_GENERAL_APPLICATION_WRITTEN_REPRESENTATION_REQUIRED_RESPONDENT.getScenario(),
                "BEARER_TOKEN",
                ScenarioRequestParams.builder().params(scenarioParams).build()
            );
        }

        @Test
        void shouldRecordRequestMoreInfoRespondentScenarioWhenOptionIsRequestMoreInformation() {
            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().atStateClaimDraft().withNoticeCaseData();
            caseData = caseData.copy()
                .ccdState(CaseState.APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION)
                .parentCaseReference(caseData.getCcdCaseReference().toString())
                .judicialDecisionRequestMoreInfo(new GAJudicialRequestMoreInfo()
                                                     .setRequestMoreInfoOption(GAJudgeRequestMoreInfoOption.REQUEST_MORE_INFORMATION))
                .build();

            HashMap<String, Object> scenarioParams = new HashMap<>();
            when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(CREATE_RESPONDENT_DASHBOARD_NOTIFICATION_FOR_MAKE_DECISION.name())
                    .build()
            ).build();

            handler.handle(params);
            verify(dashboardApiClient).recordScenario(
                caseData.getCcdCaseReference().toString(),
                SCENARIO_AAA6_GENERAL_APPLICATION_REQUEST_MORE_INFO_RESPONDENT.getScenario(),
                "BEARER_TOKEN",
                ScenarioRequestParams.builder().params(scenarioParams).build()
            );
        }

        @Test
        void shouldNotRecordRequestMoreInfoRespondentScenarioWhenOptionIsSendToOtherParty() {
            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().atStateClaimDraft().withNoticeCaseData();
            caseData = caseData.copy()
                .ccdState(CaseState.APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION)
                .parentCaseReference(caseData.getCcdCaseReference().toString())
                .judicialDecisionRequestMoreInfo(new GAJudicialRequestMoreInfo()
                                                     .setRequestMoreInfoOption(GAJudgeRequestMoreInfoOption.SEND_APP_TO_OTHER_PARTY))
                .build();

            HashMap<String, Object> scenarioParams = new HashMap<>();
            when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(CREATE_RESPONDENT_DASHBOARD_NOTIFICATION_FOR_MAKE_DECISION.name())
                    .build()
            ).build();

            handler.handle(params);
            verifyNoInteractions(dashboardApiClient);
        }

    }

    @Nested
    class AboutToSubmitCallbackForHearingScheduled {

        @Test
        void shouldRecordHearingDateRequiredRespondentScenarioWhenInvoked() {
            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().atStateClaimDraft().withNoticeCaseData();
            caseData = caseData.copy()
                .parentCaseReference(caseData.getCcdCaseReference().toString())
                .isGaApplicantLip(YesOrNo.YES)
                .parentClaimantIsApplicant(YesOrNo.YES)
                .ccdState(CaseState.LISTING_FOR_A_HEARING)
                .judicialDecision(new GAJudicialDecision().setDecision(GAJudgeDecisionOption.LIST_FOR_A_HEARING))
                .gaHearingNoticeApplication(new GAHearingNoticeApplication())
                .gaHearingNoticeDetail(new GAHearingNoticeDetail())
                .generalAppInformOtherParty(GAInformOtherParty.builder().isWithNotice(YesOrNo.YES).build())
                .build();

            HashMap<String, Object> scenarioParams = new HashMap<>();
            when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(CREATE_RESPONDENT_DASHBOARD_NOTIFICATION_FOR_MAKE_DECISION.name())
                    .build()
            ).build();

            handler.handle(params);
            verify(dashboardApiClient).recordScenario(
                caseData.getCcdCaseReference().toString(),
                SCENARIO_AAA6_GENERAL_APPLICATION_HEARING_SCHEDULED_RESPONDENT.getScenario(),
                "BEARER_TOKEN",
                ScenarioRequestParams.builder().params(scenarioParams).build()
            );
        }

        @Test
        void shouldRecordMoreInfoRequiredRespondentScenarioWhenAppIsWithoutNoticeInvoked() {
            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().atStateClaimDraft().withNoticeCaseData();
            caseData = caseData.copy()
                .parentCaseReference(caseData.getCcdCaseReference().toString())
                .isGaApplicantLip(YesOrNo.YES)
                .parentClaimantIsApplicant(YesOrNo.YES)
                .ccdState(CaseState.LISTING_FOR_A_HEARING)
                .judicialDecision(new GAJudicialDecision().setDecision(GAJudgeDecisionOption.LIST_FOR_A_HEARING))
                .gaHearingNoticeApplication(new GAHearingNoticeApplication())
                .gaHearingNoticeDetail(new GAHearingNoticeDetail())
                .generalAppInformOtherParty(GAInformOtherParty.builder().isWithNotice(YesOrNo.NO).build())
                .build();

            HashMap<String, Object> scenarioParams = new HashMap<>();
            when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(CREATE_RESPONDENT_DASHBOARD_NOTIFICATION_FOR_MAKE_DECISION.name())
                    .build()
            ).build();

            handler.handle(params);
            verify(dashboardApiClient).recordScenario(
                caseData.getCcdCaseReference().toString(),
                SCENARIO_AAA6_GENERAL_APPLICATION_HEARING_SCHEDULED_RESPONDENT.getScenario(),
                "BEARER_TOKEN",
                ScenarioRequestParams.builder().params(scenarioParams).build()
            );
        }

        @ParameterizedTest
        @MethodSource("provideOrderType")
        void shouldRecordOrderMadeRespondentScenarioWhenInvoked_isWIthNoticeApplication(GAJudgeDecisionOption decisionOption, GAJudgeMakeAnOrderOption orderOption) {
            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().atStateClaimDraft().withNoticeCaseData();
            caseData = caseData.copy()
                .parentCaseReference(caseData.getCcdCaseReference().toString())
                .isGaApplicantLip(YesOrNo.YES)
                .parentClaimantIsApplicant(YesOrNo.YES)
                .generalAppInformOtherParty(GAInformOtherParty.builder().isWithNotice(YesOrNo.YES).build())
                .judicialDecision(new GAJudicialDecision().setDecision(decisionOption))
                .ccdState(CaseState.APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION)
                .judicialDecisionMakeOrder(new GAJudicialMakeAnOrder().setMakeAnOrder(orderOption)).build();

            HashMap<String, Object> scenarioParams = new HashMap<>();
            when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(CREATE_RESPONDENT_DASHBOARD_NOTIFICATION_FOR_MAKE_DECISION.name())
                    .build()
            ).build();

            handler.handle(params);
            verify(dashboardApiClient).recordScenario(
                caseData.getCcdCaseReference().toString(),
                SCENARIO_AAA6_GENERAL_APPLICATION_ORDER_MADE_RESPONDENT.getScenario(),
                "BEARER_TOKEN",
                ScenarioRequestParams.builder().params(scenarioParams).build()
            );
        }

        @ParameterizedTest
        @MethodSource("provideOrderType")
        void shouldRecordOrderMadeApplicantScenarioWhenInvoked_isWIthNoticeApplication(
            GAJudgeDecisionOption decisionOption, GAJudgeMakeAnOrderOption orderOption) {
            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().atStateClaimDraft().withNoticeCaseData();
            caseData = caseData.copy()
                .parentCaseReference(caseData.getCcdCaseReference().toString())
                .isGaApplicantLip(YesOrNo.YES)
                .parentClaimantIsApplicant(YesOrNo.YES)
                .generalAppInformOtherParty(GAInformOtherParty.builder().isWithNotice(YesOrNo.YES).build())
                .judicialDecision(new GAJudicialDecision().setDecision(decisionOption))
                .ccdState(CaseState.ADDITIONAL_RESPONSE_TIME_EXPIRED)
                .judicialDecisionMakeOrder(new GAJudicialMakeAnOrder()
                                               .setMakeAnOrder(orderOption)).build();

            HashMap<String, Object> scenarioParams = new HashMap<>();
            when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(CREATE_RESPONDENT_DASHBOARD_NOTIFICATION_FOR_MAKE_DECISION.name())
                    .build()
            ).build();

            handler.handle(params);
            verify(dashboardApiClient).recordScenario(
                caseData.getCcdCaseReference().toString(),
                SCENARIO_AAA6_GENERAL_APPLICATION_ORDER_MADE_RESPONDENT.getScenario(),
                "BEARER_TOKEN",
                ScenarioRequestParams.builder().params(scenarioParams).build()
            );
        }

        @Test
        void shouldRecordOrderMadeApplicantScenarioWhenInvoked_isWIthNoticeApplication_whenStateIsNotCorrect() {
            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().atStateClaimDraft().withNoticeCaseData();
            caseData = caseData.copy()
                .parentCaseReference(caseData.getCcdCaseReference().toString())
                .isGaApplicantLip(YesOrNo.YES)
                .parentClaimantIsApplicant(YesOrNo.YES)
                .generalAppInformOtherParty(GAInformOtherParty.builder().isWithNotice(YesOrNo.YES).build())
                .judicialDecision(new GAJudicialDecision().setDecision(
                    MAKE_AN_ORDER
                ))
                .ccdState(CaseState.AWAITING_RESPONDENT_ACKNOWLEDGEMENT)
                .judicialDecisionMakeOrder(new GAJudicialMakeAnOrder()
                                               .setMakeAnOrder(APPROVE_OR_EDIT)).build();

            HashMap<String, Object> scenarioParams = new HashMap<>();
            when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(CREATE_RESPONDENT_DASHBOARD_NOTIFICATION_FOR_MAKE_DECISION.name())
                    .build()
            ).build();

            handler.handle(params);
            verify(dashboardApiClient, never()).recordScenario(
                caseData.getCcdCaseReference().toString(),
                SCENARIO_AAA6_GENERAL_APPLICATION_ORDER_MADE_RESPONDENT.getScenario(),
                "BEARER_TOKEN",
                ScenarioRequestParams.builder().params(scenarioParams).build()
            );
        }

        private static Stream<Arguments> provideOrderType() {
            return Stream.of(
                Arguments.of(MAKE_AN_ORDER, APPROVE_OR_EDIT),
                Arguments.of(MAKE_AN_ORDER, GAJudgeMakeAnOrderOption.DISMISS_THE_APPLICATION),
                Arguments.of(MAKE_AN_ORDER, GAJudgeMakeAnOrderOption.GIVE_DIRECTIONS_WITHOUT_HEARING),
                Arguments.of(GAJudgeDecisionOption.FREE_FORM_ORDER, null),
                Arguments.of(GAJudgeDecisionOption.LIST_FOR_A_HEARING, null)
            );
        }

        @Test
        void shouldRecordRequestWrittenRepresentationsApplicantScenarioWhenInvokedForUncloakedApplication() {
            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().atStateClaimDraft().withoutNoticeCaseData();
            caseData = caseData.copy()
                .ccdState(CaseState.APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION)
                .parentCaseReference(caseData.getCcdCaseReference().toString())
                .applicationIsUncloakedOnce(YesOrNo.YES)
                .judicialDecisionMakeAnOrderForWrittenRepresentations(new GAJudicialWrittenRepresentations().setWrittenOption(
                    GAJudgeWrittenRepresentationsOptions.CONCURRENT_REPRESENTATIONS))
                .judicialDecision(new GAJudicialDecision().setDecision(GAJudgeDecisionOption.MAKE_ORDER_FOR_WRITTEN_REPRESENTATIONS))
                .build();

            HashMap<String, Object> scenarioParams = new HashMap<>();
            when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(CREATE_APPLICANT_DASHBOARD_NOTIFICATION_FOR_MAKE_DECISION.name())
                    .build()
            ).build();

            handler.handle(params);
            verify(dashboardApiClient).recordScenario(
                caseData.getCcdCaseReference().toString(),
                SCENARIO_AAA6_GENERAL_APPLICATION_WRITTEN_REPRESENTATION_REQUIRED_RESPONDENT.getScenario(),
                "BEARER_TOKEN",
                ScenarioRequestParams.builder().params(scenarioParams).build()
            );
        }
    }
}
