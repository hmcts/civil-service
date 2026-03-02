package uk.gov.hmcts.reform.civil.service.dashboardnotifications.defendantresponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import java.util.HashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DEFENDANT_ADMIT_PAY_IMMEDIATELY_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DEFENDANT_ADMIT_PAY_INSTALLMENTS_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DEFENDANT_ADMIT_PAY_INSTALMENT_COMPANY_ORGANISATION_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DEFENDANT_ALREADY_PAID;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DEFENDANT_FULL_DEFENCE_FULL_DISPUTE_MEDIATION;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DEFENDANT_FULL_DEFENCE_NO_MEDIATION_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DEFENDANT_FULL_OR_PART_ADMIT_PAY_SET_DATE_ORG_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DEFENDANT_RESPONSE_FULL_DEFENCE_FULL_DISPUTE_DEFENDANT_CARM;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DEFENDANT_RESPONSE_FULL_DEFENCE_FULL_DISPUTE_MULTI_INT_FAST_DEFENDANT;

@ExtendWith(MockitoExtension.class)
class DefendantResponseDefendantDashboardServiceTest {

    private static final String AUTH_TOKEN = "BEARER";

    @Mock
    private DashboardScenariosService dashboardScenariosService;
    @Mock
    private DashboardNotificationsParamsMapper mapper;
    @Mock
    private FeatureToggleService featureToggleService;

    @InjectMocks
    private DefendantResponseDefendantDashboardService service;

    private final HashMap<String, Object> params = new HashMap<>();

    @BeforeEach
    void setupMapper() {
        when(mapper.mapCaseDataToParams(any())).thenReturn(params);
    }

    private CaseData mockBaseCaseData(long caseId) {
        CaseData caseData = mock(CaseData.class);
        when(caseData.getCcdCaseReference()).thenReturn(caseId);
        when(caseData.isRespondent1NotRepresented()).thenReturn(true);
        lenient().when(caseData.isRespondentResponseFullDefence()).thenReturn(false);
        lenient().when(caseData.isClaimBeingDisputed()).thenReturn(false);
        lenient().when(caseData.isSmallClaim()).thenReturn(true);
        lenient().when(caseData.isPayByInstallment()).thenReturn(false);
        lenient().when(caseData.isPartAdmitClaimSpec()).thenReturn(false);
        lenient().when(caseData.isFullAdmitClaimSpec()).thenReturn(false);
        lenient().when(caseData.isPayBySetDate()).thenReturn(false);
        lenient().when(caseData.isFullAdmitPayImmediatelyClaimSpec()).thenReturn(false);
        lenient().when(caseData.isPartAdmitPayImmediatelyClaimSpec()).thenReturn(false);
        lenient().when(caseData.hasDefendantAgreedToFreeMediation()).thenReturn(false);
        lenient().when(caseData.getRespondent1()).thenReturn(new Party().setType(Party.Type.INDIVIDUAL));
        return caseData;
    }

    @Test
    void shouldRecordScenarioWhenEligible() {
        CaseData caseData = mockBaseCaseData(1234L);
        when(caseData.isRespondentResponseFullDefence()).thenReturn(true);
        when(caseData.isClaimBeingDisputed()).thenReturn(true);
        when(caseData.isSmallClaim()).thenReturn(true);
        when(featureToggleService.isCarmEnabledForCase(caseData)).thenReturn(true);

        service.notifyDefendantResponse(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_DEFENDANT_RESPONSE_FULL_DEFENCE_FULL_DISPUTE_DEFENDANT_CARM.getScenario()),
            eq("1234"),
            any()
        );
    }

    @Test
    void shouldRecordSetDateScenarioForOrganisations() {
        CaseData caseData = mockBaseCaseData(2001L);
        when(caseData.isPayBySetDate()).thenReturn(true);
        when(caseData.getRespondent1()).thenReturn(new Party().setType(Party.Type.COMPANY));

        service.notifyDefendantResponse(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_DEFENDANT_FULL_OR_PART_ADMIT_PAY_SET_DATE_ORG_DEFENDANT.getScenario()),
            eq("2001"),
            any()
        );
    }

    @Test
    void shouldRecordAlreadyPaidScenario() {
        CaseData caseData = mockBaseCaseData(2002L);
        when(caseData.isRespondentResponseFullDefence()).thenReturn(true);
        when(caseData.hasDefendantPaidTheAmountClaimed()).thenReturn(true);

        service.notifyDefendantResponse(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_DEFENDANT_ALREADY_PAID.getScenario()),
            eq("2002"),
            any()
        );
    }

    @Test
    void shouldRecordMultiTrackScenario() {
        CaseData caseData = mockBaseCaseData(2003L);
        when(caseData.isRespondentResponseFullDefence()).thenReturn(true);
        when(caseData.isClaimBeingDisputed()).thenReturn(true);
        when(caseData.isSmallClaim()).thenReturn(false);

        service.notifyDefendantResponse(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_DEFENDANT_RESPONSE_FULL_DEFENCE_FULL_DISPUTE_MULTI_INT_FAST_DEFENDANT.getScenario()),
            eq("2003"),
            any()
        );
    }

    @Test
    void shouldRecordImmediatePaymentScenario() {
        CaseData caseData = mockBaseCaseData(2004L);
        when(caseData.isFullAdmitPayImmediatelyClaimSpec()).thenReturn(true);

        service.notifyDefendantResponse(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_DEFENDANT_ADMIT_PAY_IMMEDIATELY_DEFENDANT.getScenario()),
            eq("2004"),
            any()
        );
    }

    @Test
    void shouldRecordNoMediationScenario() {
        CaseData caseData = mockBaseCaseData(2005L);
        when(caseData.isRespondentResponseFullDefence()).thenReturn(true);
        when(caseData.hasDefendantAgreedToFreeMediation()).thenReturn(false);

        service.notifyDefendantResponse(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_DEFENDANT_FULL_DEFENCE_NO_MEDIATION_DEFENDANT.getScenario()),
            eq("2005"),
            any()
        );
    }

    @Test
    void shouldRecordInstallmentScenarioForCompany() {
        CaseData caseData = mockBaseCaseData(2006L);
        when(caseData.isPayByInstallment()).thenReturn(true);
        when(caseData.isPartAdmitClaimSpec()).thenReturn(true);
        when(caseData.getRespondent1()).thenReturn(new Party().setType(Party.Type.COMPANY));

        service.notifyDefendantResponse(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_DEFENDANT_ADMIT_PAY_INSTALMENT_COMPANY_ORGANISATION_DEFENDANT.getScenario()),
            eq("2006"),
            any()
        );
    }

    @Test
    void shouldRecordInstallmentScenarioForIndividual() {
        CaseData caseData = mockBaseCaseData(2007L);
        when(caseData.isPayByInstallment()).thenReturn(true);
        when(caseData.isFullAdmitClaimSpec()).thenReturn(true);
        when(caseData.getRespondent1()).thenReturn(new Party().setType(Party.Type.INDIVIDUAL));

        service.notifyDefendantResponse(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_DEFENDANT_ADMIT_PAY_INSTALLMENTS_DEFENDANT.getScenario()),
            eq("2007"),
            any()
        );
    }

    @Test
    void shouldRecordMediationScenario() {
        CaseData caseData = mockBaseCaseData(2008L);
        when(caseData.isRespondentResponseFullDefence()).thenReturn(true);
        when(caseData.isClaimBeingDisputed()).thenReturn(true);
        when(caseData.hasDefendantAgreedToFreeMediation()).thenReturn(true);

        service.notifyDefendantResponse(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO_AAA6_DEFENDANT_FULL_DEFENCE_FULL_DISPUTE_MEDIATION.getScenario()),
            eq("2008"),
            any()
        );
    }

    @Test
    void shouldNotRecordScenarioWhenNotEligible() {
        CaseData caseData = mockBaseCaseData(2009L);
        when(caseData.isRespondent1NotRepresented()).thenReturn(false);
        when(caseData.isRespondentResponseFullDefence()).thenReturn(true);

        service.notifyDefendantResponse(caseData, AUTH_TOKEN);

        verify(dashboardScenariosService, never()).recordScenarios(any(), any(), any(), any());
    }
}
