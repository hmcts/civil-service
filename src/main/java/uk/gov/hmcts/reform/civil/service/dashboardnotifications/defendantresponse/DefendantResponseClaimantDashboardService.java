package uk.gov.hmcts.reform.civil.service.dashboardnotifications.defendantresponse;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardScenarioService;
import uk.gov.hmcts.reform.dashboard.services.DashboardNotificationService;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import java.time.LocalDateTime;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AA6_DEFENDANT_RESPONSE_PAY_BY_INSTALLMENTS_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DEFENDANT_ADMIT_AND_PAID_PARTIAL_ALREADY_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DEFENDANT_ADMIT_PAY_INSTALLMENTS_ORG_COM_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DEFENDANT_FULL_ADMIT_PAY_IMMEDIATELY_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DEFENDANT_FULL_OR_PART_ADMIT_PAY_SET_DATE_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DEFENDANT_FULL_OR_PART_ADMIT_PAY_SET_DATE_ORG_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DEFENDANT_NOC_MOVES_OFFLINE_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DEFENDANT_PART_ADMIT_PAY_IMMEDIATELY_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DEFENDANT_RESPONSE_BILINGUAL_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DEFENDANT_RESPONSE_BILINGUAL_WELSH_ENABLED_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DEFENDANT_RESPONSE_FULLDISPUTE_MULTI_INT_FAST_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DEFENDANT_RESPONSE_FULL_DEFENCE_ALREADY_PAID_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DEFENDANT_RESPONSE_FULL_DEFENCE_FULL_DISPUTE_CLAIMANT_CARM;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DEFENDANT_RESPONSE_FULL_DEFENCE_FULL_DISPUTE_MEDIATION_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_ENGLISH_DEFENDANT_RESPONSE_BILINGUAL_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPLICATION_AVAILABLE_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPLICATION_INITIATE_APPLICATION_INACTIVE_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DEF_RESPONSE_FULL_DEFENCE_FULL_DISPUTE_REFUSED_MEDIATION_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_NOTICE_AAA6_DEF_LR_RESPONSE_FULL_DEFENCE_COUNTERCLAIM_CLAIMANT;
import static uk.gov.hmcts.reform.civil.service.dashboardnotifications.defendantresponse.DefendantResponseScenarioHelper.isCarmApplicable;
import static uk.gov.hmcts.reform.civil.service.dashboardnotifications.defendantresponse.DefendantResponseScenarioHelper.scenarioForRespondentPartyType;

@Service
public class DefendantResponseClaimantDashboardService extends DashboardScenarioService {

    private static final String DJ_NOTIFICATION = "Notice.AAA6.DefResponse.ResponseTimeElapsed.Claimant";

    private final FeatureToggleService featureToggleService;
    private final DashboardNotificationService dashboardNotificationService;

    public DefendantResponseClaimantDashboardService(DashboardScenariosService dashboardScenariosService,
                                                     DashboardNotificationsParamsMapper mapper,
                                                     FeatureToggleService featureToggleService,
                                                     DashboardNotificationService dashboardNotificationService) {
        super(dashboardScenariosService, mapper);
        this.featureToggleService = featureToggleService;
        this.dashboardNotificationService = dashboardNotificationService;
    }

    public void notifyDefendantResponse(CaseData caseData, String authToken) {
        recordScenario(caseData, authToken);
    }

    @Override
    protected String getScenario(CaseData caseData) {
        if (isBilingualFlow(caseData)) {
            return getBilingualScenario(caseData);
        }

        return switch (caseData.getRespondent1ClaimResponseTypeForSpec()) {
            case null -> null;
            case FULL_DEFENCE -> getFullDefenceScenario(caseData);
            case FULL_ADMISSION -> getFullAdmissionScenario(caseData);
            case PART_ADMISSION -> getPartAdmissionScenario(caseData);
            case COUNTER_CLAIM -> getCounterClaimScenario(caseData);
        };
    }

    @Override
    protected Map<String, Boolean> getScenarios(CaseData caseData) {
        if (isBilingualFlow(caseData)) {
            return Map.of();
        }

        boolean counterClaimForLip = RespondentResponseTypeSpec.COUNTER_CLAIM
            .equals(caseData.getRespondent1ClaimResponseTypeForSpec())
            && caseData.isApplicant1NotRepresented();

        boolean generalApplicationAvailable = hasGeneralApplications(caseData)
            && (counterClaimForLip || caseData.nocApplyForLiPDefendant());

        return Map.of(
            SCENARIO_AAA6_GENERAL_APPLICATION_INITIATE_APPLICATION_INACTIVE_CLAIMANT.getScenario(),
            counterClaimForLip,
            SCENARIO_AAA6_GENERAL_APPLICATION_AVAILABLE_CLAIMANT.getScenario(),
            generalApplicationAvailable
        );
    }

    @Override
    protected boolean shouldRecordScenario(CaseData caseData) {
        if (isBilingualFlow(caseData)) {
            return caseData.isApplicantLiP();
        }

        return caseData.isApplicant1NotRepresented();
    }

    @Override
    protected void beforeRecordScenario(CaseData caseData, String authToken) {
        if (!isBilingualFlow(caseData)) {
            return;
        }

        if (featureToggleService.isWelshEnabledForMainCase()
            && caseData.getRespondent1ResponseDeadline() != null
            && caseData.getRespondent1ResponseDeadline().isBefore(LocalDateTime.now())
            && caseData.getCcdCaseReference() != null) {
            dashboardNotificationService.deleteByNameAndReferenceAndCitizenRole(
                DJ_NOTIFICATION,
                caseData.getCcdCaseReference().toString(),
                CLAIMANT_ROLE
            );
        }
    }

    private String getBilingualScenario(CaseData caseData) {
        if (caseData.isRespondentResponseBilingual()) {
            if (featureToggleService.isWelshEnabledForMainCase()) {
                return SCENARIO_AAA6_DEFENDANT_RESPONSE_BILINGUAL_WELSH_ENABLED_CLAIMANT.getScenario();
            }
            return SCENARIO_AAA6_DEFENDANT_RESPONSE_BILINGUAL_CLAIMANT.getScenario();
        }
        return SCENARIO_AAA6_ENGLISH_DEFENDANT_RESPONSE_BILINGUAL_CLAIMANT.getScenario();
    }

    private String getCounterClaimScenario(CaseData caseData) {
        if (caseData.isLipvLROneVOne() && featureToggleService.isDefendantNoCOnlineForCase(caseData)) {
            return SCENARIO_NOTICE_AAA6_DEF_LR_RESPONSE_FULL_DEFENCE_COUNTERCLAIM_CLAIMANT.getScenario();
        }
        return null;
    }

    private String getPartAdmissionScenario(CaseData caseData) {
        if (caseData.nocApplyForLiPDefendant()) {
            return SCENARIO_AAA6_DEFENDANT_NOC_MOVES_OFFLINE_CLAIMANT.getScenario();
        } else if (caseData.isPayByInstallment()) {
            return scenarioForRespondentPartyType(
                caseData,
                SCENARIO_AAA6_DEFENDANT_ADMIT_PAY_INSTALLMENTS_ORG_COM_CLAIMANT.getScenario(),
                SCENARIO_AA6_DEFENDANT_RESPONSE_PAY_BY_INSTALLMENTS_CLAIMANT.getScenario()
            );
        } else if (caseData.isPayBySetDate()) {
            return scenarioForRespondentPartyType(
                caseData,
                SCENARIO_AAA6_DEFENDANT_FULL_OR_PART_ADMIT_PAY_SET_DATE_ORG_CLAIMANT.getScenario(),
                SCENARIO_AAA6_DEFENDANT_FULL_OR_PART_ADMIT_PAY_SET_DATE_CLAIMANT.getScenario()
            );
        } else if (caseData.isPayImmediately()) {
            return SCENARIO_AAA6_DEFENDANT_PART_ADMIT_PAY_IMMEDIATELY_CLAIMANT.getScenario();
        }
        return defendantResponseStatesPaid(caseData);
    }

    private String getFullAdmissionScenario(CaseData caseData) {
        if (caseData.nocApplyForLiPDefendant()) {
            return SCENARIO_AAA6_DEFENDANT_NOC_MOVES_OFFLINE_CLAIMANT.getScenario();
        } else if (caseData.isPayByInstallment()) {
            return scenarioForRespondentPartyType(
                caseData,
                SCENARIO_AAA6_DEFENDANT_ADMIT_PAY_INSTALLMENTS_ORG_COM_CLAIMANT.getScenario(),
                SCENARIO_AA6_DEFENDANT_RESPONSE_PAY_BY_INSTALLMENTS_CLAIMANT.getScenario()
            );
        } else if (caseData.isPayBySetDate()) {
            return scenarioForRespondentPartyType(
                caseData,
                SCENARIO_AAA6_DEFENDANT_FULL_OR_PART_ADMIT_PAY_SET_DATE_ORG_CLAIMANT.getScenario(),
                SCENARIO_AAA6_DEFENDANT_FULL_OR_PART_ADMIT_PAY_SET_DATE_CLAIMANT.getScenario()
            );
        }
        return SCENARIO_AAA6_DEFENDANT_FULL_ADMIT_PAY_IMMEDIATELY_CLAIMANT.getScenario();
    }

    private String getFullDefenceScenario(CaseData caseData) {
        if (caseData.isClaimBeingDisputed()) {
            if (isCarmApplicable(featureToggleService, caseData)) {
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

    private String defendantResponseStatesPaid(CaseData caseData) {
        return caseData.isPaidFullAmount()
            ? SCENARIO_AAA6_DEFENDANT_RESPONSE_FULL_DEFENCE_ALREADY_PAID_CLAIMANT.getScenario()
            : SCENARIO_AAA6_DEFENDANT_ADMIT_AND_PAID_PARTIAL_ALREADY_CLAIMANT.getScenario();
    }

    private boolean hasGeneralApplications(CaseData caseData) {
        return caseData.getGeneralApplications() != null && !caseData.getGeneralApplications().isEmpty();
    }

    private boolean isBilingualFlow(CaseData caseData) {
        return caseData.isRespondentResponseBilingual()
            || (featureToggleService.isWelshEnabledForMainCase() && caseData.isClaimantBilingual());
    }
}
