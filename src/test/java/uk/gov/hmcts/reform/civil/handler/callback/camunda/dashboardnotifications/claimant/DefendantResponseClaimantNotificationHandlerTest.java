package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.client.DashboardApiClient;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.RespondToClaim;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_CLAIMANT_DASHBOARD_NOTIFICATION_FOR_DEFENDANT_RESPONSE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DEFENDANT_ADMIT_AND_PAID_PARTIAL_ALREADY_CLAIMANT;

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

    @BeforeEach
    void setup() {
        when(featureToggleService.isDashboardServiceEnabled()).thenReturn(true);
    }

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

    @Test
    public void configureDashboardNotificationsForDefendantResponseForFullDefencePaidPartcialClaimant() {
        //given
        Map<String, Object> params = new HashMap<>();

        when(dashboardNotificationsParamsMapper.mapCaseDataToParams(any())).thenReturn(params);
        LocalDate admitPaymentDeadline = OffsetDateTime.now().toLocalDate();

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
    public void configureDashboardNotificationsForDefendantResponseForPartAdmitPaidPartcialClaimant() {
        //given
        Map<String, Object> params = new HashMap<>();

        when(dashboardNotificationsParamsMapper.mapCaseDataToParams(any())).thenReturn(params);
        LocalDate admitPaymentDeadline = OffsetDateTime.now().toLocalDate();

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
}
