package uk.gov.hmcts.reform.civil.handler.event;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.config.SystemUpdateUserConfiguration;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.RespondToClaimAdmitPartLRspec;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDetailsBuilder;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.utils.DateUtils;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class FullAdmitPayImmediatelyNoPaymentFromDefendantEventProcessorTest {

    @Mock
    private UserService userService;
    @Mock
    private CoreCaseDataService coreCaseDataService;
    @Mock
    private DashboardScenarioTransactionalService dashboardScenariosService;
    @Mock
    private DashboardNotificationsParamsMapper mapper;
    @Mock
    private SystemUpdateUserConfiguration userConfig;
    @Mock
    private CaseDetailsConverter caseDetailsConverter;
    @InjectMocks
    private FullAdmitPayImmediatelyNoPaymentFromDefendantProcessor handler;

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
            .respondToClaimAdmitPartLRspec(new RespondToClaimAdmitPartLRspec()
                .setWhenWillThisAmountBePaid(whenWillThisAmountBePaid)
            )
            .build();
        CaseDetails caseDetails = CaseDetailsBuilder.builder().data(updated).build();
        when(coreCaseDataService.getCase(CASE_ID)).thenReturn(caseDetails);
        when(caseDetailsConverter.toCaseData(caseDetails)).thenReturn(updated);

        HashMap<String, Object> scenarioParams = new HashMap<>();
        scenarioParams.put("ccdCaseReference", CASE_ID);
        scenarioParams.put("fullAdmitPayImmediatelyPaymentAmount", "£124.67");
        scenarioParams.put("responseToClaimAdmitPartPaymentDeadline", DateUtils.formatDate(whenWillThisAmountBePaid));

        when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);

        handler.createClaimantDashboardScenario(CASE_ID);

        verify(dashboardScenariosService).createScenario(
            AUTH_TOKEN,
            DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_FULL_ADMIT_CLAIMANT,
            CASE_ID.toString(),
            ScenarioRequestParams.builder().params(scenarioParams).build()
        );
    }
}
