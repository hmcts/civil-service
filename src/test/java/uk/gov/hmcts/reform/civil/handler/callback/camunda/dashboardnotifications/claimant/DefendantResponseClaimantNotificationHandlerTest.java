package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.constants.SpecJourneyConstantLRSpec;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.ChangeOfRepresentation;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.RespondToClaim;
import uk.gov.hmcts.reform.civil.model.RespondToClaimAdmitPartLRspec;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.genapplication.GeneralApplication;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
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
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_CLAIMANT_DASHBOARD_NOTIFICATION_FOR_DEFENDANT_RESPONSE;
import static uk.gov.hmcts.reform.civil.constants.SpecJourneyConstantLRSpec.DISPUTES_THE_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.AllocatedTrack.SMALL_CLAIM;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AA6_DEFENDANT_RESPONSE_PAY_BY_INSTALLMENTS_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DEFENDANT_ADMIT_AND_PAID_PARTIAL_ALREADY_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DEFENDANT_FULL_ADMIT_PAY_IMMEDIATELY_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DEFENDANT_FULL_OR_PART_ADMIT_PAY_SET_DATE_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DEFENDANT_FULL_OR_PART_ADMIT_PAY_SET_DATE_ORG_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DEFENDANT_NOC_MOVES_OFFLINE_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DEFENDANT_RESPONSE_FULLDISPUTE_MULTI_INT_FAST_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DEFENDANT_RESPONSE_FULL_DEFENCE_ALREADY_PAID_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DEFENDANT_RESPONSE_FULL_DEFENCE_FULL_DISPUTE_CLAIMANT_CARM;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DEFENDANT_RESPONSE_FULL_DEFENCE_FULL_DISPUTE_MEDIATION_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPLICATION_AVAILABLE_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPLICATION_INITIATE_APPLICATION_INACTIVE_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_NOTICE_AAA6_DEF_LR_RESPONSE_FULL_DEFENCE_COUNTERCLAIM_CLAIMANT;

@ExtendWith(MockitoExtension.class)
class DefendantResponseClaimantNotificationHandlerTest extends BaseCallbackHandlerTest {

    @InjectMocks
    private DefendantResponseClaimantNotificationHandler handler;

    @Mock
    private DashboardScenariosService dashboardScenariosService;

    @Mock
    private DashboardNotificationsParamsMapper dashboardNotificationsParamsMapper;

    @Mock
    private FeatureToggleService featureToggleService;

    public static final String TASK_ID = "GenerateClaimantDashboardNotificationDefendantResponse";

    @Test
    void handleEventsReturnsTheExpectedCallbackEvent() {
        assertThat(handler.handledEvents()).contains(CREATE_CLAIMANT_DASHBOARD_NOTIFICATION_FOR_DEFENDANT_RESPONSE);
    }

    @Test
    void shouldReturnCorrectCamundaActivityId_whenInvoked() {
        assertThat(handler.camundaActivityId(
            CallbackParamsBuilder.builder()
                .request(CallbackRequest.builder()
                             .eventId(CREATE_CLAIMANT_DASHBOARD_NOTIFICATION_FOR_DEFENDANT_RESPONSE.name())
                             .build())
                .build()))
            .isEqualTo(TASK_ID);
    }

    @Nested
    class AboutToSubmitCallback {
        @BeforeEach
        void setup() {
            when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
        }

        @ParameterizedTest
        @MethodSource("defendantTypeAndScenarioArguments")
        void configureDashboardNotificationsForDefendantResponseForPartAdmitPayByDate(Enum partyType, DashboardScenarios dashboardScenario) {

            HashMap<String, Object> params = new HashMap<>();

            when(dashboardNotificationsParamsMapper.mapCaseDataToParams(any())).thenReturn(params);
            when(featureToggleService.isLipVLipEnabled()).thenReturn(true);

            CaseData caseData = CaseDataBuilder.builder().atStateRespondentPartAdmissionSpec().build();
            caseData.setLegacyCaseReference("reference");
            caseData.setCcdCaseReference(8723L);
            caseData.setApplicant1Represented(YesOrNo.NO);
            Party party = new Party();
            party.setType(Party.Type.valueOf(partyType.name()));
            caseData.setRespondent1(party);
            LocalDate admitPaymentDeadline = OffsetDateTime.now().toLocalDate();
            RespondToClaimAdmitPartLRspec respondToClaimAdmitPartLRspec = new RespondToClaimAdmitPartLRspec();
            respondToClaimAdmitPartLRspec.setWhenWillThisAmountBePaid(admitPaymentDeadline);
            caseData.setRespondToClaimAdmitPartLRspec(respondToClaimAdmitPartLRspec);
            caseData.setDefenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE);
            caseData.setRespondToAdmittedClaimOwingAmountPounds(new BigDecimal(1000));

            CallbackParams callbackParams = CallbackParamsBuilder.builder()
                .of(ABOUT_TO_SUBMIT, caseData)
                .build();

            handler.handle(callbackParams);

            verify(dashboardScenariosService).recordScenarios(
                "BEARER_TOKEN",
                dashboardScenario.getScenario(),
                caseData.getCcdCaseReference().toString(),
                ScenarioRequestParams.builder().params(params).build()
            );
        }

        @ParameterizedTest
        @MethodSource("defendantTypeAndScenarioArguments")
        void configureDashboardNotificationsForDefendantResponseForFullAdmitPayByDate(Enum partyType, DashboardScenarios dashboardScenario) {
            HashMap<String, Object> params = new HashMap<>();
            when(dashboardNotificationsParamsMapper.mapCaseDataToParams(any())).thenReturn(params);
            when(featureToggleService.isLipVLipEnabled()).thenReturn(true);

            CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullAdmissionSpec().build();
            caseData.setLegacyCaseReference("reference");
            caseData.setCcdCaseReference(23055L);
            caseData.setApplicant1Represented(YesOrNo.NO);
            Party party = new Party();
            party.setType(Party.Type.valueOf(partyType.name()));
            caseData.setRespondent1(party);
            LocalDate admitPaymentDeadline = OffsetDateTime.now().toLocalDate();
            RespondToClaimAdmitPartLRspec respondToClaimAdmitPartLRspec = new RespondToClaimAdmitPartLRspec();
            respondToClaimAdmitPartLRspec.setWhenWillThisAmountBePaid(admitPaymentDeadline);
            caseData.setRespondToClaimAdmitPartLRspec(respondToClaimAdmitPartLRspec);
            caseData.setDefenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE);
            caseData.setTotalClaimAmount(new BigDecimal(1000));

            CallbackParams callbackParams = CallbackParamsBuilder.builder()
                .of(ABOUT_TO_SUBMIT, caseData)
                .build();

            handler.handle(callbackParams);
            verify(dashboardScenariosService).recordScenarios(
                "BEARER_TOKEN",
                dashboardScenario.getScenario(),
                caseData.getCcdCaseReference().toString(),
                ScenarioRequestParams.builder().params(params).build()
            );
        }

        @Test
        void configureDashboardNotificationsForDefendantResponseForFullAdmitImmediatelyClaimant() {

            HashMap<String, Object> params = new HashMap<>();

            when(dashboardNotificationsParamsMapper.mapCaseDataToParams(any())).thenReturn(params);

            CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullAdmissionSpec().build();
            caseData.setLegacyCaseReference("reference");
            caseData.setCcdCaseReference(1234L);
            caseData.setApplicant1Represented(YesOrNo.NO);
            LocalDate admitPaymentDeadline = OffsetDateTime.now().toLocalDate();
            RespondToClaimAdmitPartLRspec respondToClaimAdmitPartLRspec = new RespondToClaimAdmitPartLRspec();
            respondToClaimAdmitPartLRspec.setWhenWillThisAmountBePaid(admitPaymentDeadline);
            caseData.setRespondToClaimAdmitPartLRspec(respondToClaimAdmitPartLRspec);
            caseData.setDefenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY);
            caseData.setTotalClaimAmount(new BigDecimal(1000));

            CallbackParams callbackParams = CallbackParamsBuilder.builder()
                .of(ABOUT_TO_SUBMIT, caseData)
                .build();

            handler.handle(callbackParams);
            verify(dashboardScenariosService).recordScenarios(
                "BEARER_TOKEN",
                SCENARIO_AAA6_DEFENDANT_FULL_ADMIT_PAY_IMMEDIATELY_CLAIMANT.getScenario(),
                caseData.getCcdCaseReference().toString(),
                ScenarioRequestParams.builder().params(params).build()
            );
        }

        @Test
        void configureDashboardNotificationsForDefendantResponseForFullDefencePaidPartialClaimant() {
            //given
            HashMap<String, Object> params = new HashMap<>();

            when(dashboardNotificationsParamsMapper.mapCaseDataToParams(any())).thenReturn(params);

            String caseId = "12345673";
            CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullDefenceSpec().build();
            caseData.setLegacyCaseReference("reference");
            caseData.setCcdCaseReference(Long.valueOf(caseId));
            caseData.setApplicant1Represented(YesOrNo.NO);
            caseData.setRespondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE);
            caseData.setResponseClaimTrack(SMALL_CLAIM.name());
            RespondToClaim respondToClaim = new RespondToClaim();
            respondToClaim.setHowMuchWasPaid(new BigDecimal(1000));
            LocalDate paymentDate = OffsetDateTime.now().toLocalDate().minusDays(5);
            respondToClaim.setWhenWasThisAmountPaid(paymentDate);
            caseData.setRespondToClaim(respondToClaim);
            caseData.setTotalClaimAmount(new BigDecimal(1500));

            CallbackParams callbackParams = CallbackParamsBuilder.builder()
                .of(ABOUT_TO_SUBMIT, caseData)
                .build();
            //when
            handler.handle(callbackParams);
            //then
            verify(dashboardScenariosService).recordScenarios(
                "BEARER_TOKEN",
                SCENARIO_AAA6_DEFENDANT_ADMIT_AND_PAID_PARTIAL_ALREADY_CLAIMANT.getScenario(),
                caseData.getCcdCaseReference().toString(),
                ScenarioRequestParams.builder().params(params).build()
            );
        }

        @Test
        void configureDashboardNotificationsForDefendantResponseForFullDefenceCounterClaimantForLipVsLr() {
            //given
            HashMap<String, Object> params = new HashMap<>();

            when(dashboardNotificationsParamsMapper.mapCaseDataToParams(any())).thenReturn(params);
            when(featureToggleService.isDefendantNoCOnlineForCase(any())).thenReturn(true);
            CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullDefenceSpec().build();
            caseData.setLegacyCaseReference("reference");
            caseData.setCcdCaseReference(Long.valueOf("12345673"));
            caseData.setApplicant1Represented(YesOrNo.NO);
            caseData.setRespondent1Represented(YesOrNo.YES);
            caseData.setRespondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.COUNTER_CLAIM);
            caseData.setResponseClaimTrack(SMALL_CLAIM.name());
            caseData.setTotalClaimAmount(new BigDecimal(1500));
            caseData.setGeneralApplications(List.of(new Element<>(null, new GeneralApplication())));

            CallbackParams callbackParams = CallbackParamsBuilder.builder()
                .of(ABOUT_TO_SUBMIT, caseData)
                .build();
            //when
            handler.handle(callbackParams);
            //then
            verify(dashboardScenariosService).recordScenarios(
                "BEARER_TOKEN",
                SCENARIO_NOTICE_AAA6_DEF_LR_RESPONSE_FULL_DEFENCE_COUNTERCLAIM_CLAIMANT.getScenario(),
                caseData.getCcdCaseReference().toString(),
                ScenarioRequestParams.builder().params(params).build()
            );
            verify(dashboardScenariosService).recordScenarios(
                "BEARER_TOKEN",
                SCENARIO_AAA6_GENERAL_APPLICATION_AVAILABLE_CLAIMANT.getScenario(),
                caseData.getCcdCaseReference().toString(),
                ScenarioRequestParams.builder().params(params).build()
            );
            verify(dashboardScenariosService).recordScenarios(
                "BEARER_TOKEN",
                SCENARIO_AAA6_GENERAL_APPLICATION_INITIATE_APPLICATION_INACTIVE_CLAIMANT.getScenario(),
                caseData.getCcdCaseReference().toString(),
                ScenarioRequestParams.builder().params(params).build()
            );
        }

        @Test
        void shouldNotTriggerForDashboardNotificationsForDefendantResponseForFullDefenceCounterClaimantForLrVsLr() {
            //given
            CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullDefenceSpec().build();
            caseData.setLegacyCaseReference("reference");
            caseData.setCcdCaseReference(Long.valueOf("12345678"));
            caseData.setApplicant1Represented(YesOrNo.YES);
            caseData.setRespondent1Represented(YesOrNo.YES);
            caseData.setRespondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.COUNTER_CLAIM);
            caseData.setResponseClaimTrack(SMALL_CLAIM.name());
            caseData.setTotalClaimAmount(new BigDecimal(1500));

            CallbackParams callbackParams = CallbackParamsBuilder.builder()
                .of(ABOUT_TO_SUBMIT, caseData)
                .build();
            //when
            handler.handle(callbackParams);
            //then
            HashMap<String, Object> params = new HashMap<>();
            verify(dashboardScenariosService, never()).recordScenarios(
                "BEARER_TOKEN",
                SCENARIO_NOTICE_AAA6_DEF_LR_RESPONSE_FULL_DEFENCE_COUNTERCLAIM_CLAIMANT.getScenario(),
                caseData.getCcdCaseReference().toString(),
                ScenarioRequestParams.builder().params(params).build()
            );
        }

        @Test
        void configureDashboardNotificationsForDefendantResponseForPartAdmitPaidPartialClaimant() {
            //given
            HashMap<String, Object> params = new HashMap<>();
            String caseId = "12345674";
            when(dashboardNotificationsParamsMapper.mapCaseDataToParams(any())).thenReturn(params);

            CaseData caseData = CaseDataBuilder.builder().atStateRespondentPartAdmissionSpec().build();
            caseData.setLegacyCaseReference("reference");
            caseData.setCcdCaseReference(Long.valueOf(caseId));
            caseData.setApplicant1Represented(YesOrNo.NO);
            caseData.setRespondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION);
            RespondToClaim respondToClaim = new RespondToClaim();
            respondToClaim.setHowMuchWasPaid(new BigDecimal(1000));
            LocalDate paymentDate = OffsetDateTime.now().toLocalDate().minusDays(5);
            respondToClaim.setWhenWasThisAmountPaid(paymentDate);
            caseData.setRespondToAdmittedClaim(respondToClaim);
            caseData.setTotalClaimAmount(new BigDecimal(1500));

            CallbackParams callbackParams = CallbackParamsBuilder.builder()
                .of(ABOUT_TO_SUBMIT, caseData)
                .build();
            //when
            handler.handle(callbackParams);
            //then
            verify(dashboardScenariosService).recordScenarios(
                "BEARER_TOKEN",
                SCENARIO_AAA6_DEFENDANT_ADMIT_AND_PAID_PARTIAL_ALREADY_CLAIMANT.getScenario(),
                caseData.getCcdCaseReference().toString(),
                ScenarioRequestParams.builder().params(params).build()
            );
        }

        @Test
        void configureDashboardNotificationsForDefendantResponseForFullAdmitInstallmentsClaimant() {
            //given
            HashMap<String, Object> params = new HashMap<>();
            String caseId = "12345675";
            when(dashboardNotificationsParamsMapper.mapCaseDataToParams(any())).thenReturn(params);

            CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullAdmissionSpec().build();
            caseData.setLegacyCaseReference("reference");
            caseData.setCcdCaseReference(Long.valueOf(caseId));
            caseData.setApplicant1Represented(YesOrNo.NO);
            caseData.setRespondent1(new Party().setType(Party.Type.INDIVIDUAL));
            caseData.setRespondToClaimAdmitPartLRspec(new RespondToClaimAdmitPartLRspec());
            caseData.setDefenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.SUGGESTION_OF_REPAYMENT_PLAN);
            caseData.setTotalClaimAmount(new BigDecimal(1000));

            CallbackParams callbackParams = CallbackParamsBuilder.builder()
                .of(ABOUT_TO_SUBMIT, caseData)
                .build();

            handler.handle(callbackParams);
            verify(dashboardScenariosService).recordScenarios(
                "BEARER_TOKEN",
                SCENARIO_AA6_DEFENDANT_RESPONSE_PAY_BY_INSTALLMENTS_CLAIMANT.getScenario(),
                caseData.getCcdCaseReference().toString(),
                ScenarioRequestParams.builder().params(params).build()
            );
        }

        @ParameterizedTest
        @EnumSource(value = AllocatedTrack.class, mode = EnumSource.Mode.EXCLUDE, names = {"SMALL_CLAIM"})
        void configureDashboardNotificationsForDefendantResponseFullDefenseFastTackForClaimant(AllocatedTrack track) {
            HashMap<String, Object> params = new HashMap<>();
            when(dashboardNotificationsParamsMapper.mapCaseDataToParams(any())).thenReturn(params);
            when(featureToggleService.isLipVLipEnabled()).thenReturn(true);

            CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullDefenceSpec().build();
            caseData.setLegacyCaseReference("reference");
            caseData.setCcdCaseReference(1234L);
            caseData.setApplicant1Represented(YesOrNo.NO);
            caseData.setRespondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE);
            caseData.setDefenceRouteRequired(DISPUTES_THE_CLAIM);
            Party party = new Party();
            party.setType(Party.Type.INDIVIDUAL);
            caseData.setRespondent1(party);
            caseData.setResponseClaimTrack(track.name());

            CallbackParams callbackParams = CallbackParamsBuilder.builder()
                .of(ABOUT_TO_SUBMIT, caseData)
                .build();

            handler.handle(callbackParams);
            verify(dashboardScenariosService).recordScenarios(
                "BEARER_TOKEN",
                SCENARIO_AAA6_DEFENDANT_RESPONSE_FULLDISPUTE_MULTI_INT_FAST_CLAIMANT.getScenario(),
                caseData.getCcdCaseReference().toString(),
                ScenarioRequestParams.builder().params(params).build()
            );
        }

        @Test
        void configureDashboardNotificationsForDefendantResponseForFullDefenceFullDisputeMediationClaimant() {
            HashMap<String, Object> params = new HashMap<>();
            when(dashboardNotificationsParamsMapper.mapCaseDataToParams(any())).thenReturn(params);
            CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullDefenceSpec().build();
            caseData.setLegacyCaseReference("reference");
            caseData.setCcdCaseReference(1234L);
            caseData.setApplicant1Represented(YesOrNo.NO);
            caseData.setResponseClaimTrack(SMALL_CLAIM.name());
            caseData.setRespondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE);
            caseData.setDefenceRouteRequired(SpecJourneyConstantLRSpec.DISPUTES_THE_CLAIM);
            caseData.setResponseClaimMediationSpecRequired(YesOrNo.YES);
            caseData.setResponseClaimTrack(SMALL_CLAIM.name());

            CallbackParams callbackParams = CallbackParamsBuilder.builder()
                .of(ABOUT_TO_SUBMIT, caseData)
                .build();

            handler.handle(callbackParams);
            verify(dashboardScenariosService).recordScenarios(
                "BEARER_TOKEN",
                SCENARIO_AAA6_DEFENDANT_RESPONSE_FULL_DEFENCE_FULL_DISPUTE_MEDIATION_CLAIMANT.getScenario(),
                caseData.getCcdCaseReference().toString(),
                ScenarioRequestParams.builder().params(params).build()
            );
        }

        @Test
        void shouldRecordScenario_whenInvoked() {
            CaseData caseData = CaseDataBuilder.builder().atStateTrialReadyCheck().build();
            caseData.setRespondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION);
            caseData.setDefenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY);
            caseData.setApplicant1Represented(YesOrNo.NO);
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(CREATE_CLAIMANT_DASHBOARD_NOTIFICATION_FOR_DEFENDANT_RESPONSE.name()).build()
            ).build();

            when(featureToggleService.isLipVLipEnabled()).thenReturn(true);

            HashMap<String, Object> scenarioParams = new HashMap<>();

            handler.handle(params);
            verify(dashboardScenariosService).recordScenarios(
                "BEARER_TOKEN",
                "Scenario.AAA6.DefResponse.PartAdmit.PayImmediately.Claimant",
                caseData.getCcdCaseReference().toString(),
                ScenarioRequestParams.builder().params(scenarioParams).build()
            );
        }

        private static Stream<Arguments> defendantTypeAndScenarioArguments() {
            return Stream.of(
                Arguments.of(
                    Party.Type.ORGANISATION,
                    SCENARIO_AAA6_DEFENDANT_FULL_OR_PART_ADMIT_PAY_SET_DATE_ORG_CLAIMANT
                ),
                Arguments.of(Party.Type.COMPANY, SCENARIO_AAA6_DEFENDANT_FULL_OR_PART_ADMIT_PAY_SET_DATE_ORG_CLAIMANT),
                Arguments.of(Party.Type.INDIVIDUAL, SCENARIO_AAA6_DEFENDANT_FULL_OR_PART_ADMIT_PAY_SET_DATE_CLAIMANT),
                Arguments.of(Party.Type.SOLE_TRADER, SCENARIO_AAA6_DEFENDANT_FULL_OR_PART_ADMIT_PAY_SET_DATE_CLAIMANT)
            );
        }
    }

    @Test
    void configureDashboardNotificationsForDefendantResponseForFullAdmitFullPaidClaimant() {
        HashMap<String, Object> params = new HashMap<>();
        when(dashboardNotificationsParamsMapper.mapCaseDataToParams(any())).thenReturn(params);
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);

        CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullDefenceSpec().build();
        caseData.setLegacyCaseReference("reference");
        caseData.setCcdCaseReference(1234L);
        caseData.setApplicant1Represented(YesOrNo.NO);
        Party party = new Party();
        party.setType(Party.Type.INDIVIDUAL);
        caseData.setRespondent1(party);
        caseData.setRespondToClaimAdmitPartLRspec(new RespondToClaimAdmitPartLRspec());
        caseData.setRespondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE);
        caseData.setResponseClaimTrack(SMALL_CLAIM.name());
        RespondToClaim respondToClaim = new RespondToClaim();
        respondToClaim.setHowMuchWasPaid(new BigDecimal(100000));
        caseData.setRespondToClaim(respondToClaim);
        caseData.setTotalClaimAmount(new BigDecimal(1000));

        CallbackParams callbackParams = CallbackParamsBuilder.builder()
            .of(ABOUT_TO_SUBMIT, caseData)
            .build();

        handler.handle(callbackParams);
        verify(dashboardScenariosService).recordScenarios(
            "BEARER_TOKEN",
            SCENARIO_AAA6_DEFENDANT_RESPONSE_FULL_DEFENCE_ALREADY_PAID_CLAIMANT.getScenario(),
            caseData.getCcdCaseReference().toString(),
            ScenarioRequestParams.builder().params(params).build()
        );
    }

    @Test
    void configureDashboardNotificationsForDefendantResponseForPartAdmitAfterNocClaimant() {
        HashMap<String, Object> params = new HashMap<>();
        when(dashboardNotificationsParamsMapper.mapCaseDataToParams(any())).thenReturn(params);
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);

        CaseData caseData = CaseDataBuilder.builder().atStateRespondentPartAdmissionSpec().build();
        caseData.setCcdState(CaseState.PROCEEDS_IN_HERITAGE_SYSTEM);
        caseData.setChangeOfRepresentation(new ChangeOfRepresentation());
        caseData.setLegacyCaseReference("reference");
        caseData.setCcdCaseReference(1234L);
        caseData.setApplicant1Represented(YesOrNo.NO);
        Party party = new Party();
        party.setType(Party.Type.INDIVIDUAL);
        caseData.setRespondent1(party);
        caseData.setRespondent1(party);
        caseData.setResponseClaimTrack(SMALL_CLAIM.name());
        RespondToClaim respondToClaim = new RespondToClaim();
        respondToClaim.setHowMuchWasPaid(new BigDecimal(100000));
        caseData.setRespondToClaim(respondToClaim);
        caseData.setGeneralApplications(List.of(new Element<>(null, new GeneralApplication())));
        caseData.setTotalClaimAmount(new BigDecimal(1000));

        CallbackParams callbackParams = CallbackParamsBuilder.builder()
            .of(ABOUT_TO_SUBMIT, caseData)
            .build();

        handler.handle(callbackParams);
        verify(dashboardScenariosService).recordScenarios(
            "BEARER_TOKEN",
            SCENARIO_AAA6_DEFENDANT_NOC_MOVES_OFFLINE_CLAIMANT.getScenario(),
            caseData.getCcdCaseReference().toString(),
            ScenarioRequestParams.builder().params(params).build()
        );

        verify(dashboardScenariosService).recordScenarios(
            "BEARER_TOKEN",
            SCENARIO_AAA6_GENERAL_APPLICATION_AVAILABLE_CLAIMANT.getScenario(),
            caseData.getCcdCaseReference().toString(),
            ScenarioRequestParams.builder().params(params).build()
        );
    }

    @Test
    void configureDashboardNotificationsForDefendantResponseForFullAdmitAfterNocClaimant() {
        HashMap<String, Object> params = new HashMap<>();
        when(dashboardNotificationsParamsMapper.mapCaseDataToParams(any())).thenReturn(params);
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);

        CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullAdmissionSpec().build();
        caseData.setCcdState(CaseState.PROCEEDS_IN_HERITAGE_SYSTEM);
        caseData.setChangeOfRepresentation(new ChangeOfRepresentation());
        caseData.setLegacyCaseReference("reference");
        caseData.setCcdCaseReference(1234L);
        caseData.setApplicant1Represented(YesOrNo.NO);
        Party party = new Party();
        party.setType(Party.Type.INDIVIDUAL);
        caseData.setRespondent1(party);
        caseData.setResponseClaimTrack(SMALL_CLAIM.name());
        RespondToClaim respondToClaim = new RespondToClaim();
        respondToClaim.setHowMuchWasPaid(new BigDecimal(100000));
        caseData.setRespondToClaim(respondToClaim);
        caseData.setTotalClaimAmount(new BigDecimal(1000));

        CallbackParams callbackParams = CallbackParamsBuilder.builder()
            .of(ABOUT_TO_SUBMIT, caseData)
            .build();

        handler.handle(callbackParams);
        verify(dashboardScenariosService).recordScenarios(
            "BEARER_TOKEN",
            SCENARIO_AAA6_DEFENDANT_NOC_MOVES_OFFLINE_CLAIMANT.getScenario(),
            caseData.getCcdCaseReference().toString(),
            ScenarioRequestParams.builder().params(params).build()
        );
    }

    @Test
    void configureDashboardNotificationsForDefendantResponseFullDefenceDisputeAllClaimantCarm() {
        HashMap<String, Object> params = new HashMap<>();
        when(dashboardNotificationsParamsMapper.mapCaseDataToParams(any())).thenReturn(params);
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
        when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(true);

        CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullDefenceSpec().build();
        caseData.setLegacyCaseReference("reference");
        caseData.setCcdCaseReference(1234L);
        caseData.setApplicant1Represented(YesOrNo.NO);
        Party party = new Party();
        party.setType(Party.Type.INDIVIDUAL);
        caseData.setRespondent1(party);
        caseData.setRespondToClaimAdmitPartLRspec(new RespondToClaimAdmitPartLRspec());
        caseData.setRespondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE);
        caseData.setDefenceRouteRequired(DISPUTES_THE_CLAIM);
        caseData.setResponseClaimTrack(SMALL_CLAIM.name());
        RespondToClaim respondToClaim = new RespondToClaim();
        respondToClaim.setHowMuchWasPaid(new BigDecimal(100000));
        caseData.setRespondToClaim(respondToClaim);
        caseData.setTotalClaimAmount(new BigDecimal(1000));

        CallbackParams callbackParams = CallbackParamsBuilder.builder()
            .of(ABOUT_TO_SUBMIT, caseData)
            .build();

        handler.handle(callbackParams);

        verify(dashboardScenariosService).recordScenarios(
            "BEARER_TOKEN",
            SCENARIO_AAA6_DEFENDANT_RESPONSE_FULL_DEFENCE_FULL_DISPUTE_CLAIMANT_CARM.getScenario(),
            caseData.getCcdCaseReference().toString(),
            ScenarioRequestParams.builder().params(params).build()
        );
    }

    @Test
    void willNotConfigureDashboardNotificationsForDefendantResponseWhenRespondent1ClaimResponseTypeForSpecIsNull() {
        //given
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
        CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullDefenceSpec().build();
        caseData.setLegacyCaseReference("reference");
        caseData.setCcdCaseReference(12345673L);
        caseData.setApplicant1Represented(YesOrNo.NO);
        caseData.setResponseClaimTrack(SMALL_CLAIM.name());
        caseData.setRespondent1ClaimResponseTypeForSpec(null);
        RespondToClaim respondToClaim = new RespondToClaim();
        respondToClaim.setHowMuchWasPaid(new BigDecimal(1000));
        LocalDate paymentDate = OffsetDateTime.now().toLocalDate().minusDays(5);
        respondToClaim.setWhenWasThisAmountPaid(paymentDate);
        caseData.setRespondToClaim(respondToClaim);
        caseData.setTotalClaimAmount(new BigDecimal(1500));

        CallbackParams callbackParams = CallbackParamsBuilder.builder()
            .of(ABOUT_TO_SUBMIT, caseData)
            .build();
        //when
        handler.handle(callbackParams);
        //then
        verifyNoInteractions(dashboardScenariosService);
    }
}
