package uk.gov.hmcts.reform.civil.service.dashboardnotifications.ccjrequested;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.RespondToClaimAdmitPartLRspec;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardScenarioService;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_CCJ_REQUESTED_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_REQUESTED_CCJ_CLAIMANT;

@Service
public class CcjRequestedClaimantDashboardService extends DashboardScenarioService {

    private final FeatureToggleService featureToggleService;

    public CcjRequestedClaimantDashboardService(DashboardScenariosService dashboardScenariosService,
                                                DashboardNotificationsParamsMapper mapper,
                                                FeatureToggleService featureToggleService) {
        super(dashboardScenariosService, mapper);
        this.featureToggleService = featureToggleService;
    }

    public void notifyClaimant(CaseData caseData, String authToken) {
        recordScenario(caseData, authToken);
    }

    @Override
    protected String getScenario(CaseData caseData) {
        if (claimantSubmitsEitherCcjOrDjWithoutSettlementAgreement(caseData)) {
            return SCENARIO_AAA6_CLAIMANT_INTENT_CCJ_REQUESTED_CLAIMANT.getScenario();
        }
        return SCENARIO_AAA6_CLAIMANT_INTENT_REQUESTED_CCJ_CLAIMANT.getScenario();
    }

    @Override
    protected boolean shouldRecordScenario(CaseData caseData) {
        return featureToggleService.isLipVLipEnabled()
            && YesOrNo.NO.equals(caseData.getApplicant1Represented());
    }

    private boolean claimantSubmitsEitherCcjOrDjWithoutSettlementAgreement(CaseData caseData) {
        LocalDate whenWillThisAmountBePaid =
            Optional.ofNullable(caseData.getRespondToClaimAdmitPartLRspec())
                .map(RespondToClaimAdmitPartLRspec::getWhenWillThisAmountBePaid)
                .orElse(null);

        return (nonNull(whenWillThisAmountBePaid)
            && whenWillThisAmountBePaid.isBefore(LocalDate.now())
            && caseData.isFullAdmitPayImmediatelyClaimSpec())
            || ((!caseData.getDefaultJudgmentDocuments().isEmpty() && caseData.getDefaultJudgmentDocuments().stream()
            .map(Element::getValue)
            .anyMatch(doc -> doc.getDocumentType().equals(DocumentType.DEFAULT_JUDGMENT)))
            || (Objects.nonNull(caseData.getRepaymentSummaryObject())));
    }
}
