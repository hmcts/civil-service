package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.defendant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.enums.PaymentFrequencyLRspec;
import uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.RepaymentPlanLRspec;
import uk.gov.hmcts.reform.civil.model.RespondToClaimAdmitPartLRspec;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_DEFENDANT_DASHBOARD_NOTIFICATION_FOR_DEFENDANT_RESPONSE;
import static uk.gov.hmcts.reform.civil.constants.SpecJourneyConstantLRSpec.DISPUTES_THE_CLAIM;
import static uk.gov.hmcts.reform.civil.constants.SpecJourneyConstantLRSpec.HAS_PAID_THE_AMOUNT_CLAIMED;
import static uk.gov.hmcts.reform.civil.enums.AllocatedTrack.SMALL_CLAIM;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DEFENDANT_ADMIT_PAY_BY_SET_DATE_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DEFENDANT_ADMIT_PAY_IMMEDIATELY_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DEFENDANT_ADMIT_PAY_INSTALLMENTS_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DEFENDANT_ADMIT_PAY_INSTALMENT_COMPANY_ORGANISATION_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DEFENDANT_ALREADY_PAID;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DEFENDANT_FULL_DEFENCE_FULL_DISPUTE_MEDIATION;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DEFENDANT_FULL_DEFENCE_NO_MEDIATION_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DEFENDANT_RESPONSE_FULL_DEFENCE_FULL_DISPUTE_DEFENDANT_CARM;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DEFENDANT_RESPONSE_FULL_DEFENCE_FULL_DISPUTE_MULTI_INT_FAST_DEFENDANT;

@ExtendWith(MockitoExtension.class)
class DefendantResponseDefendantNotificationHandlerTest extends BaseCallbackHandlerTest {

    @InjectMocks
    private DefendantResponseDefendantNotificationHandler handler;

    @Mock
    private DashboardScenariosService dashboardScenariosService;

    @Mock
    private DashboardNotificationsParamsMapper dashboardNotificationsParamsMapper;

    @Mock
    private FeatureToggleService featureToggleService;

    public static final String TASK_ID = "GenerateDefendantDashboardNotificationDefendantResponse";

    @Test
    void handleEventsReturnsTheExpectedCallbackEvent() {
        assertThat(handler.handledEvents()).contains(CREATE_DEFENDANT_DASHBOARD_NOTIFICATION_FOR_DEFENDANT_RESPONSE);
    }

    @Test
    void shouldReturnCorrectCamundaActivityId_whenInvoked() {
        assertThat(handler.camundaActivityId(
            CallbackParamsBuilder.builder()
                .request(CallbackRequest.builder()
                             .eventId(CREATE_DEFENDANT_DASHBOARD_NOTIFICATION_FOR_DEFENDANT_RESPONSE.name())
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

        @Test
        void configureDashboardNotificationsForDefendantResponseForPartAdmitImmediately() {
            //given
            HashMap<String, Object> params = new HashMap<>();

            when(dashboardNotificationsParamsMapper.mapCaseDataToParams(any())).thenReturn(params);
            LocalDate admitPaymentDeadline = OffsetDateTime.now().toLocalDate();

            CaseData caseData = CaseDataBuilder.builder().atStateRespondentPartAdmissionSpec().build();
            caseData.setLegacyCaseReference("reference");
            caseData.setCcdCaseReference(1234L);
            caseData.setRespondent1Represented(YesOrNo.NO);
            caseData.setRespondToClaimAdmitPartLRspec(new RespondToClaimAdmitPartLRspec(admitPaymentDeadline));
            caseData.setDefenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY);
            caseData.setRespondToAdmittedClaimOwingAmountPounds(new BigDecimal(1000));

            CallbackParams callbackParams = CallbackParamsBuilder.builder()
                .of(ABOUT_TO_SUBMIT, caseData)
                .build();
            //when
            handler.handle(callbackParams);
            //then
            verify(dashboardScenariosService).recordScenarios(
                "BEARER_TOKEN",
                SCENARIO_AAA6_DEFENDANT_ADMIT_PAY_IMMEDIATELY_DEFENDANT.getScenario(),
                caseData.getCcdCaseReference().toString(),
                ScenarioRequestParams.builder().params(params).build()
            );
        }

        @Test
        void configureDashboardNotificationsForDefendantResponseForFullAdmitImmediately() {
            //given
            HashMap<String, Object> params = new HashMap<>();

            when(dashboardNotificationsParamsMapper.mapCaseDataToParams(any())).thenReturn(params);
            LocalDate admitPaymentDeadline = OffsetDateTime.now().toLocalDate();

            CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullAdmissionSpec().build();
            caseData.setLegacyCaseReference("reference");
            caseData.setCcdCaseReference(1234L);
            caseData.setRespondent1Represented(YesOrNo.NO);
            caseData.setRespondToClaimAdmitPartLRspec(new RespondToClaimAdmitPartLRspec(admitPaymentDeadline));
            caseData.setDefenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY);
            caseData.setTotalClaimAmount(new BigDecimal(1000));

            CallbackParams callbackParams = CallbackParamsBuilder.builder()
                .of(ABOUT_TO_SUBMIT, caseData)
                .build();
            //when
            handler.handle(callbackParams);
            //then
            verify(dashboardScenariosService).recordScenarios(
                "BEARER_TOKEN",
                SCENARIO_AAA6_DEFENDANT_ADMIT_PAY_IMMEDIATELY_DEFENDANT.getScenario(),
                caseData.getCcdCaseReference().toString(),
                ScenarioRequestParams.builder().params(params).build()
            );
        }

        @Test
        void configureDashboardNotificationsForDefendantResponseForPartAdmitInstalmentCompanyOrganisation() {

            HashMap<String, Object> params = new HashMap<>();

            when(dashboardNotificationsParamsMapper.mapCaseDataToParams(any())).thenReturn(params);
            String caseId = "12345671";
            LocalDate firstRepaymentDate = OffsetDateTime.now().toLocalDate();

            CaseData caseData = CaseDataBuilder.builder().atStateRespondentPartAdmissionSpec().build();
            caseData.setLegacyCaseReference("reference");
            caseData.setCcdCaseReference(Long.valueOf(caseId));
            caseData.setRespondent1(PartyBuilder.builder().company().build());
            caseData.setRespondent1Represented(YesOrNo.NO);
            caseData.setRespondent1RepaymentPlan(new RepaymentPlanLRspec()
                .setFirstRepaymentDate(firstRepaymentDate)
                .setPaymentAmount(new BigDecimal(1000))
                .setRepaymentFrequency(PaymentFrequencyLRspec.ONCE_ONE_WEEK));
            caseData.setDefenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.SUGGESTION_OF_REPAYMENT_PLAN);
            caseData.setRespondToAdmittedClaimOwingAmountPounds(new BigDecimal(1000));

            CallbackParams callbackParams = CallbackParamsBuilder.builder()
                .of(ABOUT_TO_SUBMIT, caseData)
                .build();

            handler.handle(callbackParams);
            verify(dashboardScenariosService).recordScenarios(
                "BEARER_TOKEN",
                SCENARIO_AAA6_DEFENDANT_ADMIT_PAY_INSTALMENT_COMPANY_ORGANISATION_DEFENDANT.getScenario(),
                caseData.getCcdCaseReference().toString(),
                ScenarioRequestParams.builder().params(params).build()
            );
        }

        @Test
        void configureDashboardNotificationsForDefendantResponseForFullAdmitInstalmentCompanyOrganisation() {

            HashMap<String, Object> params = new HashMap<>();

            when(dashboardNotificationsParamsMapper.mapCaseDataToParams(any())).thenReturn(params);
            String caseId = "12345672";
            LocalDate firstRepaymentDate = OffsetDateTime.now().toLocalDate();

            CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullAdmissionSpec().build();
            caseData.setLegacyCaseReference("reference");
            caseData.setCcdCaseReference(Long.valueOf(caseId));
            caseData.setRespondent1(PartyBuilder.builder().company().build());
            caseData.setRespondent1Represented(YesOrNo.NO);
            caseData.setRespondent1RepaymentPlan(new RepaymentPlanLRspec()
                .setFirstRepaymentDate(firstRepaymentDate)
                .setPaymentAmount(new BigDecimal(1000))
                .setRepaymentFrequency(PaymentFrequencyLRspec.ONCE_ONE_WEEK));
            caseData.setTotalClaimAmount(new BigDecimal(1000));
            caseData.setDefenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.SUGGESTION_OF_REPAYMENT_PLAN);

            CallbackParams callbackParams = CallbackParamsBuilder.builder()
                .of(ABOUT_TO_SUBMIT, caseData)
                .build();

            handler.handle(callbackParams);
            verify(dashboardScenariosService).recordScenarios(
                "BEARER_TOKEN",
                SCENARIO_AAA6_DEFENDANT_ADMIT_PAY_INSTALMENT_COMPANY_ORGANISATION_DEFENDANT.getScenario(),
                caseData.getCcdCaseReference().toString(),
                ScenarioRequestParams.builder().params(params).build()
            );
        }

        @Test
        void configureDashboardNotificationsForDefendantResponseForPartAdmitAlreadyPaid() {
            //given
            HashMap<String, Object> params = new HashMap<>();

            when(dashboardNotificationsParamsMapper.mapCaseDataToParams(any())).thenReturn(params);

            CaseData caseData = CaseDataBuilder.builder().atStateRespondentPartAdmissionSpec().build();
            caseData.setLegacyCaseReference("reference");
            caseData.setCcdCaseReference(1234L);
            caseData.setRespondent1Represented(YesOrNo.NO);
            caseData.setRespondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION);
            caseData.setSpecDefenceAdmittedRequired(YesOrNo.YES);

            CallbackParams callbackParams = CallbackParamsBuilder.builder()
                .of(ABOUT_TO_SUBMIT, caseData)
                .build();
            //when
            handler.handle(callbackParams);
            //then
            verify(dashboardScenariosService).recordScenarios(
                "BEARER_TOKEN",
                SCENARIO_AAA6_DEFENDANT_ALREADY_PAID.getScenario(),
                caseData.getCcdCaseReference().toString(),
                ScenarioRequestParams.builder().params(params).build()
            );
        }

        @Test
        void configureDashboardNotificationsForDefendantResponseForFullAdmitAlreadyPaid() {
            //given
            HashMap<String, Object> params = new HashMap<>();

            when(dashboardNotificationsParamsMapper.mapCaseDataToParams(any())).thenReturn(params);

            CaseData caseData = CaseDataBuilder.builder().atStateRespondentPartAdmissionSpec().build();
            caseData.setLegacyCaseReference("reference");
            caseData.setCcdCaseReference(1234L);
            caseData.setRespondent1Represented(YesOrNo.NO);
            caseData.setRespondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE);
            caseData.setDefenceRouteRequired(HAS_PAID_THE_AMOUNT_CLAIMED);

            CallbackParams callbackParams = CallbackParamsBuilder.builder()
                .of(ABOUT_TO_SUBMIT, caseData)
                .build();
            //when
            handler.handle(callbackParams);
            //then
            verify(dashboardScenariosService).recordScenarios(
                "BEARER_TOKEN",
                SCENARIO_AAA6_DEFENDANT_ALREADY_PAID.getScenario(),
                caseData.getCcdCaseReference().toString(),
                ScenarioRequestParams.builder().params(params).build()
            );
        }

        @Test
        void configureDashboardNotificationsForDefendantResponseForPartAdmitBySetDate() {
            //given
            HashMap<String, Object> params = new HashMap<>();

            when(dashboardNotificationsParamsMapper.mapCaseDataToParams(any())).thenReturn(params);

            LocalDate admitPaymentDeadline = OffsetDateTime.now().toLocalDate();

            CaseData caseData = CaseDataBuilder.builder().atStateRespondentPartAdmissionSpec().build();
            caseData.setLegacyCaseReference("reference");
            caseData.setCcdCaseReference(1234L);
            caseData.setRespondent1Represented(YesOrNo.NO);
            caseData.setRespondToClaimAdmitPartLRspec(new RespondToClaimAdmitPartLRspec(admitPaymentDeadline));
            caseData.setDefenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE);
            caseData.setRespondToAdmittedClaimOwingAmountPounds(new BigDecimal(1000));

            CallbackParams callbackParams = CallbackParamsBuilder.builder()
                .of(ABOUT_TO_SUBMIT, caseData)
                .build();
            //when
            handler.handle(callbackParams);
            //then
            verify(dashboardScenariosService).recordScenarios(
                "BEARER_TOKEN",
                SCENARIO_AAA6_DEFENDANT_ADMIT_PAY_BY_SET_DATE_DEFENDANT.getScenario(),
                caseData.getCcdCaseReference().toString(),
                ScenarioRequestParams.builder().params(params).build()
            );
        }

        @Test
        void configureDashboardNotificationsForDefendantResponseForFullAdmitBySetDate() {
            //given
            HashMap<String, Object> params = new HashMap<>();

            when(dashboardNotificationsParamsMapper.mapCaseDataToParams(any())).thenReturn(params);

            LocalDate admitPaymentDeadline = OffsetDateTime.now().toLocalDate();

            CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullAdmissionSpec().build();
            caseData.setLegacyCaseReference("reference");
            caseData.setCcdCaseReference(1234L);
            caseData.setRespondent1Represented(YesOrNo.NO);
            caseData.setRespondToClaimAdmitPartLRspec(new RespondToClaimAdmitPartLRspec(admitPaymentDeadline));
            caseData.setDefenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE);
            caseData.setTotalClaimAmount(new BigDecimal(1000));

            CallbackParams callbackParams = CallbackParamsBuilder.builder()
                .of(ABOUT_TO_SUBMIT, caseData)
                .build();
            //when
            handler.handle(callbackParams);
            //then
            verify(dashboardScenariosService).recordScenarios(
                "BEARER_TOKEN",
                SCENARIO_AAA6_DEFENDANT_ADMIT_PAY_BY_SET_DATE_DEFENDANT.getScenario(),
                caseData.getCcdCaseReference().toString(),
                ScenarioRequestParams.builder().params(params).build()
            );
        }

        @Test
        void configureDashboardNotificationsForDefendantResponseForFullAdmitInstallments() {
            //given
            HashMap<String, Object> params = new HashMap<>();

            when(dashboardNotificationsParamsMapper.mapCaseDataToParams(any())).thenReturn(params);
            LocalDate admitPaymentDeadline = OffsetDateTime.now().toLocalDate();

            CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullAdmissionSpec().build();
            caseData.setLegacyCaseReference("reference");
            caseData.setCcdCaseReference(1234L);
            caseData.setRespondent1Represented(YesOrNo.NO);
            caseData.setRespondToClaimAdmitPartLRspec(new RespondToClaimAdmitPartLRspec(admitPaymentDeadline));
            caseData.setDefenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.SUGGESTION_OF_REPAYMENT_PLAN);
            caseData.setTotalClaimAmount(new BigDecimal(1000));

            CallbackParams callbackParams = CallbackParamsBuilder.builder()
                .of(ABOUT_TO_SUBMIT, caseData)
                .build();
            //when
            handler.handle(callbackParams);
            //then
            verify(dashboardScenariosService).recordScenarios(
                "BEARER_TOKEN",
                SCENARIO_AAA6_DEFENDANT_ADMIT_PAY_INSTALLMENTS_DEFENDANT.getScenario(),
                caseData.getCcdCaseReference().toString(),
                ScenarioRequestParams.builder().params(params).build()
            );
        }

        @Test
        void configureDashboardNotificationsForDefendantResponseForDefenceNoMediation() {
            //given
            HashMap<String, Object> params = new HashMap<>();
            when(dashboardNotificationsParamsMapper.mapCaseDataToParams(any())).thenReturn(params);

            when(featureToggleService.isLipVLipEnabled()).thenReturn(true);

            CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullAdmissionSpec().build();
            caseData.setLegacyCaseReference("reference");
            caseData.setCcdCaseReference(1234L);
            caseData.setRespondent1Represented(YesOrNo.NO);
            caseData.setRespondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE);
            caseData.setResponseClaimMediationSpecRequired(YesOrNo.NO);
            caseData.setTotalClaimAmount(new BigDecimal(1000));

            CallbackParams callbackParams = CallbackParamsBuilder.builder()
                .of(ABOUT_TO_SUBMIT, caseData)
                .build();

            //when
            handler.handle(callbackParams);
            verify(dashboardScenariosService).recordScenarios(
                "BEARER_TOKEN",
                SCENARIO_AAA6_DEFENDANT_FULL_DEFENCE_NO_MEDIATION_DEFENDANT.getScenario(),
                caseData.getCcdCaseReference().toString(),
                ScenarioRequestParams.builder().params(params).build()
            );
        }

        void configureDashboardNotificationsForDefendantResponseForFullDefenceDisputeAllWithMediation() {
            //given
            HashMap<String, Object> params = new HashMap<>();

            when(dashboardNotificationsParamsMapper.mapCaseDataToParams(any())).thenReturn(params);

            when(featureToggleService.isLipVLipEnabled()).thenReturn(true);

            CaseData caseData = CaseDataBuilder.builder().atStateRespondentPartAdmissionSpec().build();
            caseData.setLegacyCaseReference("reference");
            caseData.setCcdCaseReference(530012L);
            caseData.setRespondent1Represented(YesOrNo.NO);
            caseData.setRespondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE);
            caseData.setDefenceRouteRequired(DISPUTES_THE_CLAIM);
            caseData.setResponseClaimMediationSpecRequired(YesOrNo.YES);

            CallbackParams callbackParams = CallbackParamsBuilder.builder()
                .of(ABOUT_TO_SUBMIT, caseData)
                .build();
            //when
            handler.handle(callbackParams);
            //then
            verify(dashboardScenariosService).recordScenarios(
                "BEARER_TOKEN",
                SCENARIO_AAA6_DEFENDANT_FULL_DEFENCE_FULL_DISPUTE_MEDIATION.getScenario(),
                caseData.getCcdCaseReference().toString(),
                ScenarioRequestParams.builder().params(params).build()
            );
        }

        @ParameterizedTest
        @EnumSource(value = AllocatedTrack.class, mode = EnumSource.Mode.EXCLUDE, names = {"SMALL_CLAIM"})
        void configureDashboardNotificationsForDefendantResponseForFullDefenceDisputeAllFastTrack(AllocatedTrack track) {
            //given
            HashMap<String, Object> params = new HashMap<>();

            when(dashboardNotificationsParamsMapper.mapCaseDataToParams(any())).thenReturn(params);

            when(featureToggleService.isLipVLipEnabled()).thenReturn(true);

            CaseData caseData = CaseDataBuilder.builder().atStateRespondentPartAdmissionSpec().build();
            caseData.setLegacyCaseReference("reference");
            caseData.setCcdCaseReference(1234L);
            caseData.setRespondent1Represented(YesOrNo.NO);
            caseData.setRespondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE);
            caseData.setDefenceRouteRequired(DISPUTES_THE_CLAIM);
            caseData.setResponseClaimTrack(track.name());

            CallbackParams callbackParams = CallbackParamsBuilder.builder()
                .of(ABOUT_TO_SUBMIT, caseData)
                .build();
            //when
            handler.handle(callbackParams);
            //then
            verify(dashboardScenariosService).recordScenarios(
                "BEARER_TOKEN",
                SCENARIO_AAA6_DEFENDANT_RESPONSE_FULL_DEFENCE_FULL_DISPUTE_MULTI_INT_FAST_DEFENDANT.getScenario(),
                caseData.getCcdCaseReference().toString(),
                ScenarioRequestParams.builder().params(params).build()
            );
        }

        @Test
        void configureDashboardNotificationsForDefendantResponseForFullDefenceDisputeAllSmallTrackCarm() {
            //given
            HashMap<String, Object> params = new HashMap<>();

            when(dashboardNotificationsParamsMapper.mapCaseDataToParams(any())).thenReturn(params);

            when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
            when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(true);

            CaseData caseData = CaseDataBuilder.builder().atStateRespondentPartAdmissionSpec().build();
            caseData.setLegacyCaseReference("reference");
            caseData.setCcdCaseReference(Long.valueOf(1234L));
            caseData.setRespondent1Represented(YesOrNo.NO);
            caseData.setRespondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE);
            caseData.setDefenceRouteRequired(DISPUTES_THE_CLAIM);
            caseData.setResponseClaimTrack(SMALL_CLAIM.name());

            CallbackParams callbackParams = CallbackParamsBuilder.builder()
                .of(ABOUT_TO_SUBMIT, caseData)
                .build();
            //when
            handler.handle(callbackParams);
            //then
            verify(dashboardScenariosService).recordScenarios(
                "BEARER_TOKEN",
                SCENARIO_AAA6_DEFENDANT_RESPONSE_FULL_DEFENCE_FULL_DISPUTE_DEFENDANT_CARM.getScenario(),
                caseData.getCcdCaseReference().toString(),
                ScenarioRequestParams.builder().params(params).build()
            );
        }
    }
}
