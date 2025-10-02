package uk.gov.hmcts.reform.civil.service.docmosis.sealedclaim.helpers;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.StatementOfTruth;
import uk.gov.hmcts.reform.civil.model.docmosis.sealedclaim.SealedClaimResponseFormForSpec;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Respondent2DQ;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

@Component
public class StatementOfTruthPopulator {

    private final FeatureToggleService featureToggleService;

    public StatementOfTruthPopulator(FeatureToggleService featureToggleService) {
        this.featureToggleService = featureToggleService;
    }

    public void populateStatementOfTruthDetails(SealedClaimResponseFormForSpec.SealedClaimResponseFormForSpecBuilder builder, CaseData caseData) {

        StatementOfTruth statementOfTruth = null;

        if (!isRespondent2(caseData)) {
            Respondent1DQ dq = caseData.getRespondent1DQ();
            if (dq != null && dq.getRespondent1DQStatementOfTruth() != null) {
                statementOfTruth = dq.getRespondent1DQStatementOfTruth();
            }
        } else {
            Respondent2DQ dq = caseData.getRespondent2DQ();
            if (dq != null && dq.getRespondent2DQStatementOfTruth() != null) {
                statementOfTruth = dq.getRespondent2DQStatementOfTruth();
            }
        }

        builder.statementOfTruth(statementOfTruth)
            .allocatedTrack(caseData.getResponseClaimTrack())
            .responseType(caseData.getRespondentClaimResponseTypeForSpecGeneric())
            .checkCarmToggle(featureToggleService.isCarmEnabledForCase(caseData))
            .mediation(caseData.getResponseClaimMediationSpecRequired());
    }

    private boolean isRespondent2(CaseData caseData) {
        return (caseData.getRespondent2ResponseDate() != null)
            && (caseData.getRespondent1ResponseDate() == null
            || caseData.getRespondent2ResponseDate().isAfter(caseData.getRespondent1ResponseDate()));
    }
}
