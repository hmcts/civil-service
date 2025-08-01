package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimant;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.callback.DashboardCallbackHandler;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_CLAIMANT_DASHBOARD_NOTIFICATION_FOR_DEFENDANT_RESPONSE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AA6_DEFENDANT_RESPONSE_PAY_BY_INSTALLMENTS_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DEFENDANT_ADMIT_AND_PAID_PARTIAL_ALREADY_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DEFENDANT_ADMIT_PAY_INSTALLMENTS_ORG_COM_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DEFENDANT_FULL_ADMIT_PAY_IMMEDIATELY_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DEFENDANT_FULL_OR_PART_ADMIT_PAY_SET_DATE_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DEFENDANT_FULL_OR_PART_ADMIT_PAY_SET_DATE_ORG_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DEFENDANT_NOC_MOVES_OFFLINE_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DEFENDANT_PART_ADMIT_PAY_IMMEDIATELY_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DEFENDANT_RESPONSE_FULLDISPUTE_MULTI_INT_FAST_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DEFENDANT_RESPONSE_FULL_DEFENCE_ALREADY_PAID_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DEFENDANT_RESPONSE_FULL_DEFENCE_FULL_DISPUTE_CLAIMANT_CARM;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DEFENDANT_RESPONSE_FULL_DEFENCE_FULL_DISPUTE_MEDIATION_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DEF_RESPONSE_FULL_DEFENCE_FULL_DISPUTE_REFUSED_MEDIATION_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPLICATION_AVAILABLE_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPLICATION_INITIATE_APPLICATION_INACTIVE_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_NOTICE_AAA6_DEF_LR_RESPONSE_FULL_DEFENCE_COUNTERCLAIM_CLAIMANT;

@Service
public class DefendantResponseClaimantNotificationHandler extends DashboardCallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(CREATE_CLAIMANT_DASHBOARD_NOTIFICATION_FOR_DEFENDANT_RESPONSE);
    public static final String TASK_ID = "GenerateClaimantDashboardNotificationDefendantResponse";

    public DefendantResponseClaimantNotificationHandler(DashboardScenariosService dashboardScenariosService,
                                                        DashboardNotificationsParamsMapper mapper,
                                                        FeatureToggleService featureToggleService) {
        super(dashboardScenariosService, mapper, featureToggleService);
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return TASK_ID;
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    @Override
    public boolean shouldRecordScenario(CaseData caseData) {
        return caseData.isApplicant1NotRepresented();
    }

    @Override
    public String getScenario(CaseData caseData) {
        if (caseData.getRespondent1ClaimResponseTypeForSpec() == null) {
            return null;
        }
        return switch (caseData.getRespondent1ClaimResponseTypeForSpec()) {
            case FULL_DEFENCE -> getFullDefenceScenario(caseData);
            case FULL_ADMISSION -> getFullAdmissionScenario(caseData);
            case PART_ADMISSION -> getPartAdmissionScenario(caseData);
            case COUNTER_CLAIM -> getCounterClaimScenario(caseData);
            default -> null;
        };
    }

    private String getCounterClaimScenario(CaseData caseData) {
        if (caseData.isLipvLROneVOne() && featureToggleService.isDefendantNoCOnlineForCase(caseData)) {
            return SCENARIO_NOTICE_AAA6_DEF_LR_RESPONSE_FULL_DEFENCE_COUNTERCLAIM_CLAIMANT.getScenario();
        }
        return null;
    }

    @Override
    public Map<String, Boolean> getScenarios(CaseData caseData) {
        return Map.of(
            SCENARIO_AAA6_GENERAL_APPLICATION_INITIATE_APPLICATION_INACTIVE_CLAIMANT.getScenario(),
            featureToggleService.isGaForLipsEnabled() && RespondentResponseTypeSpec.COUNTER_CLAIM.equals(
                caseData.getRespondent1ClaimResponseTypeForSpec()) && caseData.isApplicant1NotRepresented(),
            SCENARIO_AAA6_GENERAL_APPLICATION_AVAILABLE_CLAIMANT.getScenario(),
            featureToggleService.isGaForLipsEnabled() && caseData.getGeneralApplications().size() > 0
                && ((RespondentResponseTypeSpec.COUNTER_CLAIM.equals(caseData.getRespondent1ClaimResponseTypeForSpec())
                && caseData.isApplicant1NotRepresented()) || caseData.nocApplyForLiPDefendant())
        );
    }

    private String getPartAdmissionScenario(CaseData caseData) {
        if (caseData.nocApplyForLiPDefendant()) {
            return SCENARIO_AAA6_DEFENDANT_NOC_MOVES_OFFLINE_CLAIMANT.getScenario();
        } else if (caseData.isPayByInstallment()) {
            return getScenarioForPayByInstallmentBasedOnRespondentPartyType(caseData);
        } else if (caseData.isPayBySetDate()) {
            return getScenarioForPayBySetDateBasedOnRespondentPartyType(caseData);
        } else if (caseData.isPayImmediately()) {
            return SCENARIO_AAA6_DEFENDANT_PART_ADMIT_PAY_IMMEDIATELY_CLAIMANT.getScenario();
        }
        return defendantResponseStatesPaid(caseData);
    }

    private String getFullAdmissionScenario(CaseData caseData) {
        if (caseData.nocApplyForLiPDefendant()) {
            return SCENARIO_AAA6_DEFENDANT_NOC_MOVES_OFFLINE_CLAIMANT.getScenario();
        } else if (caseData.isPayByInstallment()) {
            return getScenarioForPayByInstallmentBasedOnRespondentPartyType(caseData);
        } else if (caseData.isPayBySetDate()) {
            return getScenarioForPayBySetDateBasedOnRespondentPartyType(caseData);
        }
        return SCENARIO_AAA6_DEFENDANT_FULL_ADMIT_PAY_IMMEDIATELY_CLAIMANT.getScenario();
    }

    private String getFullDefenceScenario(CaseData caseData) {
        if (caseData.isClaimBeingDisputed()) {
            if (isCarmApplicable(caseData)) {
                return SCENARIO_AAA6_DEFENDANT_RESPONSE_FULL_DEFENCE_FULL_DISPUTE_CLAIMANT_CARM.getScenario();
            } else if (caseData.hasDefendantAgreedToFreeMediation() && caseData.isSmallClaim()) {
                return SCENARIO_AAA6_DEFENDANT_RESPONSE_FULL_DEFENCE_FULL_DISPUTE_MEDIATION_CLAIMANT.getScenario();
            } else if (caseData.hasDefendantNotAgreedToFreeMediation() && caseData.isSmallClaim()) {
                return SCENARIO_AAA6_DEF_RESPONSE_FULL_DEFENCE_FULL_DISPUTE_REFUSED_MEDIATION_CLAIMANT.getScenario();
            } else if (!caseData.isSmallClaim()) {
                return SCENARIO_AAA6_DEFENDANT_RESPONSE_FULLDISPUTE_MULTI_INT_FAST_CLAIMANT.getScenario();
            }
        }

        return defendantResponseStatesPaid(caseData);
    }

    private boolean isCarmApplicable(CaseData caseData) {
        return getFeatureToggleService().isCarmEnabledForCase(caseData)
            && caseData.isSmallClaim();
    }

    private String defendantResponseStatesPaid(CaseData caseData) {
        return caseData.isPaidFullAmount()
            ? SCENARIO_AAA6_DEFENDANT_RESPONSE_FULL_DEFENCE_ALREADY_PAID_CLAIMANT.getScenario()
            : SCENARIO_AAA6_DEFENDANT_ADMIT_AND_PAID_PARTIAL_ALREADY_CLAIMANT.getScenario();
    }

    private String getScenarioForPayByInstallmentBasedOnRespondentPartyType(CaseData caseData) {
        return caseData.getRespondent1().isCompanyOROrganisation()
            ? SCENARIO_AAA6_DEFENDANT_ADMIT_PAY_INSTALLMENTS_ORG_COM_CLAIMANT.getScenario()
            : SCENARIO_AA6_DEFENDANT_RESPONSE_PAY_BY_INSTALLMENTS_CLAIMANT.getScenario();
    }

    private String getScenarioForPayBySetDateBasedOnRespondentPartyType(CaseData caseData) {
        return caseData.getRespondent1().isCompanyOROrganisation()
            ? SCENARIO_AAA6_DEFENDANT_FULL_OR_PART_ADMIT_PAY_SET_DATE_ORG_CLAIMANT.getScenario()
            : SCENARIO_AAA6_DEFENDANT_FULL_OR_PART_ADMIT_PAY_SET_DATE_CLAIMANT.getScenario();
    }
}
