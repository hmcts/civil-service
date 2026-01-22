package uk.gov.hmcts.reform.civil.service.dashboardnotifications.citizenhearingfeepayment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.FeeType;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.PaymentDetails;
import uk.gov.hmcts.reform.civil.model.citizenui.FeePaymentOutcomeDetails;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.PaymentStatus.SUCCESS;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_HEARING_FEE_PAID_CLAIMANT;

@ExtendWith(MockitoExtension.class)
class CitizenHearingFeePaymentDashboardServiceTest {

    private static final String AUTH_TOKEN = "Bearer token";
    private static final Long CASE_REFERENCE = 123456789L;

    @Mock
    private DashboardScenariosService dashboardScenariosService;
    @Mock
    private DashboardNotificationsParamsMapper mapper;

    private CitizenHearingFeePaymentDashboardService service;

    @BeforeEach
    void setUp() {
        service = new CitizenHearingFeePaymentDashboardService(dashboardScenariosService, mapper);
        lenient().when(mapper.mapCaseDataToParams(any(CaseData.class))).thenReturn(new HashMap<>());
    }

    @Test
    void shouldRecordScenarioWhenHearingFeePaymentSuccessful() {
        CaseData caseData = baseCaseData();
        caseData.setHearingFeePaymentDetails(PaymentDetails.builder().status(SUCCESS).build());
        HashMap<String, Object> params = new HashMap<>();
        params.put("status", "success");
        when(mapper.mapCaseDataToParams(caseData)).thenReturn(params);

        service.notifyCitizenHearingFeePayment(caseData, AUTH_TOKEN);

        ArgumentCaptor<ScenarioRequestParams> captor = ArgumentCaptor.forClass(ScenarioRequestParams.class);
        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_HEARING_FEE_PAID_CLAIMANT.getScenario()),
            eq(CASE_REFERENCE.toString()),
            captor.capture()
        );
        assertThat(captor.getValue().getParams()).isEqualTo(params);
    }

    @Test
    void shouldRecordScenarioWhenHwfRemissionNotGranted() {
        CaseData caseData = baseCaseData();
        caseData.setHwfFeeType(FeeType.HEARING);
        caseData.setFeePaymentOutcomeDetails(
            FeePaymentOutcomeDetails.builder()
                .hwfFullRemissionGrantedForHearingFee(YesOrNo.NO)
                .build()
        );

        service.notifyCitizenHearingFeePayment(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_HEARING_FEE_PAID_CLAIMANT.getScenario()),
            eq(CASE_REFERENCE.toString()),
            any(ScenarioRequestParams.class)
        );
    }

    @Test
    void shouldNotRecordScenarioWhenApplicantRepresented() {
        CaseData caseData = baseCaseData();
        caseData.setApplicant1Represented(YesOrNo.YES);
        caseData.setHearingFeePaymentDetails(PaymentDetails.builder().status(SUCCESS).build());

        service.notifyCitizenHearingFeePayment(caseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardScenariosService);
    }

    @Test
    void shouldNotRecordScenarioWhenNoTriggeringEvent() {
        CaseData caseData = baseCaseData();

        service.notifyCitizenHearingFeePayment(caseData, AUTH_TOKEN);

        verifyNoInteractions(dashboardScenariosService);
    }

    private CaseData baseCaseData() {
        return CaseDataBuilder.builder()
            .ccdCaseReference(CASE_REFERENCE)
            .applicant1Represented(YesOrNo.NO)
            .build();
    }
}
