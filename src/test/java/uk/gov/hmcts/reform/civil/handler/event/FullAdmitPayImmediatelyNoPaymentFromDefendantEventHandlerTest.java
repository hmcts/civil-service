package uk.gov.hmcts.reform.civil.handler.event;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.config.SystemUpdateUserConfiguration;
import uk.gov.hmcts.reform.civil.event.FullAdmitPayImmediatelyNoPaymentFromDefendantEvent;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.RespondToClaimAdmitPartLRspec;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDetailsBuilder;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.utils.DateUtils;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CLAIMANT_RESPONSE_FA_IMMEDIATE_CCJ;

@ExtendWith(SpringExtension.class)
class FullAdmitPayImmediatelyNoPaymentFromDefendantEventHandlerTest {

    @Mock
    private UserService userService;
    @Mock
    private CoreCaseDataService coreCaseDataService;
    @Mock
    private DashboardScenariosService dashboardScenariosService;
    @Mock
    private DashboardNotificationsParamsMapper mapper;
    @Mock
    private SystemUpdateUserConfiguration userConfig;
    @Mock
    private CaseDetailsConverter caseDetailsConverter;
    @Mock
    private FeatureToggleService featureToggleService;
    @InjectMocks
    private FullAdmitPayImmediatelyNoPaymentFromDefendantEventHandler handler;

    static final Long CASE_ID = 1111111111111111L;
    static final String AUTH_TOKEN = "mock_token";

    @BeforeEach
    void init() {
        when(userService.getAccessToken(anyString(), anyString())).thenReturn(AUTH_TOKEN);
        when(userConfig.getUserName()).thenReturn("");
        when(userConfig.getPassword()).thenReturn("");
    }

    @Test
    void shouldCreateClaimantDashboardNotifications() {
        LocalDate whenWillThisAmountBePaid = LocalDate.now().plusDays(5);
        CaseData caseData = new CaseDataBuilder().atStateClaimDraft().build();
        CaseData updated = caseData.toBuilder()
            .ccdCaseReference(CASE_ID)
            .totalClaimAmount(BigDecimal.valueOf(124.67))
            .respondToClaimAdmitPartLRspec(RespondToClaimAdmitPartLRspec.builder()
                .whenWillThisAmountBePaid(whenWillThisAmountBePaid)
                .build())
            .build();
        CaseDetails caseDetails = CaseDetailsBuilder.builder().data(updated).build();
        when(coreCaseDataService.getCase(CASE_ID)).thenReturn(caseDetails);
        when(caseDetailsConverter.toCaseData(caseDetails)).thenReturn(updated);
        when(featureToggleService.isJudgmentOnlineLive()).thenReturn(false);

        HashMap<String, Object> scenarioParams = new HashMap<>();
        scenarioParams.put("ccdCaseReference", CASE_ID);
        scenarioParams.put("fullAdmitPayImmediatelyPaymentAmount", "£124.67");
        scenarioParams.put("responseToClaimAdmitPartPaymentDeadline", DateUtils.formatDate(whenWillThisAmountBePaid));

        when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);

        handler.createClaimantDashboardScenario(new FullAdmitPayImmediatelyNoPaymentFromDefendantEvent(CASE_ID));

        verify(dashboardScenariosService).recordScenarios(
            AUTH_TOKEN,
            DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_FULL_ADMIT_CLAIMANT.getScenario(),
            CASE_ID.toString(),
            ScenarioRequestParams.builder().params(scenarioParams).build()
        );
    }

    @Test
    void shouldTriggerFullAdmitPayImmediatelyCCJEvent() {
        when(featureToggleService.isJudgmentOnlineLive()).thenReturn(true);
        handler.createClaimantDashboardScenario(new FullAdmitPayImmediatelyNoPaymentFromDefendantEvent(CASE_ID));

        verify(coreCaseDataService).triggerEvent(CASE_ID, CLAIMANT_RESPONSE_FA_IMMEDIATE_CCJ);
    }
}
