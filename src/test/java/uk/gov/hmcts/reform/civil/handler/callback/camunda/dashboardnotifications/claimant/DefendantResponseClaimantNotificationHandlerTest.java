package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.client.DashboardApiClient;
import uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.RespondToClaim;
import uk.gov.hmcts.reform.civil.model.RespondToClaimAdmitPartLRspec;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import java.util.stream.Stream;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_CLAIMANT_DASHBOARD_NOTIFICATION_FOR_DEFENDANT_RESPONSE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AA6_DEFENDANT_RESPONSE_PAY_BY_INSTALLMENTS_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DEFENDANT_FULL_ADMIT_PAY_IMMEDIATELY_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DEFENDANT_ADMIT_AND_PAID_PARTIAL_ALREADY_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DEFENDANT_FULL_OR_PART_ADMIT_PAY_SET_DATE_ORG_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DEFENDANT_FULL_OR_PART_ADMIT_PAY_SET_DATE_CLAIMANT;

@ExtendWith(MockitoExtension.class)
public class DefendantResponseClaimantNotificationHandlerTest extends BaseCallbackHandlerTest {

    @InjectMocks
    private DefendantResponseClaimantNotificationHandler handler;

    @Mock
    private DashboardApiClient dashboardApiClient;

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
            when(featureToggleService.isDashboardServiceEnabled()).thenReturn(true);
        }

        @ParameterizedTest
        @MethodSource("defendantTypeAndScenarioArguments")
        public void configureDashboardNotificationsForDefendantResponseForPartAdmitPayByDate(Enum partyType, DashboardScenarios dashboardScenario) {

            Map<String, Object> params = new HashMap<>();

            when(dashboardNotificationsParamsMapper.mapCaseDataToParams(any())).thenReturn(params);
            when(dashboardApiClient.recordScenario(any(), any(), anyString(), any())).thenReturn(ResponseEntity.of(
                Optional.empty()));

            LocalDate admitPaymentDeadline = OffsetDateTime.now().toLocalDate();
            CaseData caseData = CaseDataBuilder.builder().atStateRespondentPartAdmissionSpec().build()
                .toBuilder()
                .legacyCaseReference("reference")
                .ccdCaseReference(Long.valueOf(8723L))
                .applicant1Represented(YesOrNo.NO)
                .respondent1(Party.builder()
                        .type(Party.Type.valueOf(partyType.name())).build())
                .respondToClaimAdmitPartLRspec(RespondToClaimAdmitPartLRspec
                                                   .builder()
                                                   .whenWillThisAmountBePaid(admitPaymentDeadline)
                                                   .build())
                .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE)
                .respondToAdmittedClaimOwingAmountPounds(new BigDecimal(1000))
                .build();

            CallbackParams callbackParams = CallbackParamsBuilder.builder()
                .of(ABOUT_TO_SUBMIT, caseData)
                .build();

            handler.handle(callbackParams);

            verify(dashboardApiClient, times(1)).recordScenario(
                caseData.getCcdCaseReference().toString(),
                    dashboardScenario.getScenario(),
                "BEARER_TOKEN",
                ScenarioRequestParams.builder().params(params).build()
            );
        }

        @ParameterizedTest
        @MethodSource("defendantTypeAndScenarioArguments")
        public void configureDashboardNotificationsForDefendantResponseForFullAdmitPayByDate(Enum partyType, DashboardScenarios dashboardScenario) {

            Map<String, Object> params = new HashMap<>();

            when(dashboardNotificationsParamsMapper.mapCaseDataToParams(any())).thenReturn(params);
            when(dashboardApiClient.recordScenario(any(), any(), anyString(), any())).thenReturn(ResponseEntity.of(
                Optional.empty()));

            LocalDate admitPaymentDeadline = OffsetDateTime.now().toLocalDate();
            CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullAdmissionSpec().build()
                .toBuilder()
                .legacyCaseReference("reference")
                .ccdCaseReference(Long.valueOf(23055L))
                .applicant1Represented(YesOrNo.NO)
                .respondent1(Party.builder()
                        .type(Party.Type.valueOf(partyType.name())).build())
                .respondToClaimAdmitPartLRspec(RespondToClaimAdmitPartLRspec
                                                   .builder()
                                                   .whenWillThisAmountBePaid(admitPaymentDeadline)
                                                   .build())
                .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE)
                .totalClaimAmount(new BigDecimal(1000))
                .build();

            CallbackParams callbackParams = CallbackParamsBuilder.builder()
                .of(ABOUT_TO_SUBMIT, caseData)
                .build();

            handler.handle(callbackParams);

            verify(dashboardApiClient, times(1)).recordScenario(
                caseData.getCcdCaseReference().toString(),
                    dashboardScenario.getScenario(),
                "BEARER_TOKEN",
                ScenarioRequestParams.builder().params(params).build()
            );
        }

        @Test
        public void configureDashboardNotificationsForDefendantResponseForFullAdmitImmediatelyClaimant() {

            Map<String, Object> params = new HashMap<>();

            when(dashboardNotificationsParamsMapper.mapCaseDataToParams(any())).thenReturn(params);
            LocalDate admitPaymentDeadline = OffsetDateTime.now().toLocalDate();

            CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullAdmissionSpec().build()
                .toBuilder()
                .legacyCaseReference("reference")
                .ccdCaseReference(Long.valueOf(1234L))
                .applicant1Represented(YesOrNo.NO)
                .respondToClaimAdmitPartLRspec(RespondToClaimAdmitPartLRspec
                                                   .builder()
                                                   .whenWillThisAmountBePaid(admitPaymentDeadline)
                                                   .build())
                .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY)
                .totalClaimAmount(new BigDecimal(1000))
                .build();

            CallbackParams callbackParams = CallbackParamsBuilder.builder()
                .of(ABOUT_TO_SUBMIT, caseData)
                .build();

            handler.handle(callbackParams);

            verify(dashboardApiClient, times(1)).recordScenario(
                caseData.getCcdCaseReference().toString(),
                SCENARIO_AAA6_DEFENDANT_FULL_ADMIT_PAY_IMMEDIATELY_CLAIMANT.getScenario(),
                "BEARER_TOKEN",
                ScenarioRequestParams.builder().params(params).build()
            );
        }

        @Test
        public void configureDashboardNotificationsForDefendantResponseForFullDefencePaidPartialClaimant() {
            //given
            Map<String, Object> params = new HashMap<>();

            when(dashboardNotificationsParamsMapper.mapCaseDataToParams(any())).thenReturn(params);

            String caseId = "12345673";
            LocalDate paymentDate = OffsetDateTime.now().toLocalDate().minusDays(5);
            CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullDefenceSpec().build()
                .toBuilder()
                .legacyCaseReference("reference")
                .ccdCaseReference(Long.valueOf(caseId))
                .applicant1Represented(YesOrNo.NO)
                .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
                .respondToClaim(RespondToClaim.builder()
                                    .howMuchWasPaid(new BigDecimal(1000))
                                    .whenWasThisAmountPaid(paymentDate)
                                    .build())
                .totalClaimAmount(new BigDecimal(1500))
                .build();

            CallbackParams callbackParams = CallbackParamsBuilder.builder()
                .of(ABOUT_TO_SUBMIT, caseData)
                .build();
            //when
            handler.handle(callbackParams);
            //then
            verify(dashboardApiClient, times(1)).recordScenario(
                caseData.getCcdCaseReference().toString(),
                SCENARIO_AAA6_DEFENDANT_ADMIT_AND_PAID_PARTIAL_ALREADY_CLAIMANT.getScenario(),
                "BEARER_TOKEN",
                ScenarioRequestParams.builder().params(params).build()
            );
        }

        @Test
        public void configureDashboardNotificationsForDefendantResponseForPartAdmitPaidPartialClaimant() {
            //given
            Map<String, Object> params = new HashMap<>();

            when(dashboardNotificationsParamsMapper.mapCaseDataToParams(any())).thenReturn(params);

            String caseId = "12345674";
            LocalDate paymentDate = OffsetDateTime.now().toLocalDate().minusDays(5);
            CaseData caseData = CaseDataBuilder.builder().atStateRespondentPartAdmissionSpec().build()
                .toBuilder()
                .legacyCaseReference("reference")
                .ccdCaseReference(Long.valueOf(caseId))
                .applicant1Represented(YesOrNo.NO)
                .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
                .respondToAdmittedClaim(RespondToClaim.builder()
                                            .howMuchWasPaid(new BigDecimal(1000))
                                            .whenWasThisAmountPaid(paymentDate)
                                            .build())
                .totalClaimAmount(new BigDecimal(1500))
                .build();

            CallbackParams callbackParams = CallbackParamsBuilder.builder()
                .of(ABOUT_TO_SUBMIT, caseData)
                .build();
            //when
            handler.handle(callbackParams);
            //then
            verify(dashboardApiClient, times(1)).recordScenario(
                caseData.getCcdCaseReference().toString(),
                SCENARIO_AAA6_DEFENDANT_ADMIT_AND_PAID_PARTIAL_ALREADY_CLAIMANT.getScenario(),
                "BEARER_TOKEN",
                ScenarioRequestParams.builder().params(params).build()
            );
        }

        @Test
        public void configureDashboardNotificationsForDefendantResponseForFullAdmitInstallmentsClaimant() {

            Map<String, Object> params = new HashMap<>();

            when(dashboardNotificationsParamsMapper.mapCaseDataToParams(any())).thenReturn(params);
            when(dashboardApiClient.recordScenario(any(), any(), anyString(), any())).thenReturn(ResponseEntity.of(
                    Optional.empty()));

            CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullAdmissionSpec().build()
                    .toBuilder()
                    .legacyCaseReference("reference")
                    .ccdCaseReference(Long.valueOf(1234L))
                    .applicant1Represented(YesOrNo.NO)
                    .respondent1(Party.builder().type(Party.Type.INDIVIDUAL).build())
                    .respondToClaimAdmitPartLRspec(RespondToClaimAdmitPartLRspec
                            .builder()
                            .build())
                    .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.SUGGESTION_OF_REPAYMENT_PLAN)
                    .totalClaimAmount(new BigDecimal(1000))
                    .build();

            CallbackParams callbackParams = CallbackParamsBuilder.builder()
                    .of(ABOUT_TO_SUBMIT, caseData)
                    .build();

            handler.handle(callbackParams);

            verify(dashboardApiClient, times(1)).recordScenario(
                    caseData.getCcdCaseReference().toString(),
                    SCENARIO_AA6_DEFENDANT_RESPONSE_PAY_BY_INSTALLMENTS_CLAIMANT.getScenario(),
                    "BEARER_TOKEN",
                    ScenarioRequestParams.builder().params(params).build()
            );
        }

        private static Stream<Arguments> defendantTypeAndScenarioArguments() {
            return Stream.of(
                    Arguments.of(Party.Type.ORGANISATION, SCENARIO_AAA6_DEFENDANT_FULL_OR_PART_ADMIT_PAY_SET_DATE_ORG_CLAIMANT),
                    Arguments.of(Party.Type.COMPANY, SCENARIO_AAA6_DEFENDANT_FULL_OR_PART_ADMIT_PAY_SET_DATE_ORG_CLAIMANT),
                    Arguments.of(Party.Type.INDIVIDUAL, SCENARIO_AAA6_DEFENDANT_FULL_OR_PART_ADMIT_PAY_SET_DATE_CLAIMANT),
                    Arguments.of(Party.Type.SOLE_TRADER, SCENARIO_AAA6_DEFENDANT_FULL_OR_PART_ADMIT_PAY_SET_DATE_CLAIMANT)
            );
        }
    }
}
