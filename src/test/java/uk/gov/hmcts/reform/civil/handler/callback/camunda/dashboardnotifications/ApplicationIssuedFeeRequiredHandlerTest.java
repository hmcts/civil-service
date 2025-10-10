package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.genapplication.GAApplicationType;
import uk.gov.hmcts.reform.civil.model.genapplication.GAHearingDateGAspec;
import uk.gov.hmcts.reform.civil.model.genapplication.GAPbaDetails;
import uk.gov.hmcts.reform.civil.model.genapplication.GARespondentOrderAgreement;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.GeneralAppFeesService;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_DASHBOARD_NOTIFICATION_FOR_GA_APPLICANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPLICATION_SUBMITTED_APPLICANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPS_APPLICATION_FEE_REQUIRED_APPLICANT;

@ExtendWith(MockitoExtension.class)
public class ApplicationIssuedFeeRequiredHandlerTest extends BaseCallbackHandlerTest {

    @Mock
    private DashboardScenariosService dashboardScenariosService;
    @Mock
    private DashboardNotificationsParamsMapper mapper;
    @Mock
    private FeatureToggleService featureToggleService;
    @Mock
    private GeneralAppFeesService generalAppFeesService;
    @InjectMocks
    private ApplicationIssuedFeeRequiredHandler handler;

    @Test
    void handleEventsReturnsTheExpectedCallbackEvent() {
        assertThat(handler.handledEvents()).contains(CREATE_DASHBOARD_NOTIFICATION_FOR_GA_APPLICANT);
    }

    @Test
    void shouldReturnCorrectCamundaActivityId_whenInvoked() {
        assertThat(handler.camundaActivityId(CallbackParams.builder().build()))
            .isEqualTo("default");
    }

    @Nested
    class AboutToSubmitCallback {

        @Test
        void shouldRecordApplicationIssueFeeRequiredScenarioWhenInvoked() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().withNoticeCaseData();
            caseData = caseData.toBuilder()
                .parentCaseReference(caseData.getCcdCaseReference().toString())
                .isGaApplicantLip(YesOrNo.YES)
                .parentClaimantIsApplicant(YesOrNo.YES)
                .generalAppPBADetails(GAPbaDetails.builder()
                                          .fee(Fee.builder()
                                                   .calculatedAmountInPence(new BigDecimal(100000))
                                                   .build())
                                          .build())
                .build();

            HashMap<String, Object> scenarioParams = new HashMap<>();
            when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
            when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(CREATE_DASHBOARD_NOTIFICATION_FOR_GA_APPLICANT.name())
                    .build()
            ).build();

            handler.handle(params);
            verify(dashboardScenariosService).recordScenarios(
                    "BEARER_TOKEN",
                    SCENARIO_AAA6_GENERAL_APPS_APPLICATION_FEE_REQUIRED_APPLICANT.getScenario(),
                    caseData.getCcdCaseReference().toString(),
                    ScenarioRequestParams.builder().params(scenarioParams).build()
            );
        }

        @Test
        void shouldRecordApplicationSubmittedScenarioWhenInvokedForFreeApplication() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().withNoticeCaseData();
            caseData = caseData.toBuilder()
                .parentCaseReference(caseData.getCcdCaseReference().toString())
                .generalAppRespondentAgreement(GARespondentOrderAgreement.builder().hasAgreed(YesOrNo.YES).build())
                .generalAppType(GAApplicationType.builder().types(List.of(GeneralApplicationTypes.ADJOURN_HEARING)).build())
                .isGaApplicantLip(YesOrNo.YES)
                .generalAppHearingDate(GAHearingDateGAspec.builder().hearingScheduledDate(LocalDate.now().minusDays(20)).build())
                .parentClaimantIsApplicant(YesOrNo.YES)
                .generalAppPBADetails(GAPbaDetails.builder()
                                          .fee(Fee.builder()
                                                   .calculatedAmountInPence(new BigDecimal(0))
                                                   .build())
                                          .build())
                .build();

            HashMap<String, Object> scenarioParams = new HashMap<>();
            when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
            when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);
            when(generalAppFeesService.isFreeApplication(any())).thenReturn(true);

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(CREATE_DASHBOARD_NOTIFICATION_FOR_GA_APPLICANT.name())
                    .build()
            ).build();

            handler.handle(params);
            verify(dashboardScenariosService).recordScenarios(
                    "BEARER_TOKEN",
                    SCENARIO_AAA6_GENERAL_APPLICATION_SUBMITTED_APPLICANT.getScenario(),
                    caseData.getCcdCaseReference().toString(),
                    ScenarioRequestParams.builder().params(scenarioParams).build()
            );
        }

        @Test
        void shouldNotRecordApplicationIssueFeeRequiredScenarioWhenGaFlagIsDisable() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().withNoticeCaseData();
            caseData = caseData.toBuilder()
                .parentCaseReference(caseData.getCcdCaseReference().toString())
                .isGaApplicantLip(YesOrNo.YES)
                .parentClaimantIsApplicant(YesOrNo.YES)
                .generalAppPBADetails(GAPbaDetails.builder()
                                          .fee(Fee.builder()
                                                   .calculatedAmountInPence(new BigDecimal(100000))
                                                   .build())
                                          .build())
                .build();

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(CREATE_DASHBOARD_NOTIFICATION_FOR_GA_APPLICANT.name())
                    .build()
            ).build();

            handler.handle(params);
            verifyNoInteractions(dashboardScenariosService);
        }

    }
}
