package uk.gov.hmcts.reform.civil.service.docmosis.caseprogression.helpers;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.docmosis.casepogression.JudgeFinalOrderForm;

import static java.util.Objects.nonNull;

@Component
public class CaseInfoPopulator {

    public JudgeFinalOrderForm populateCaseInfo(JudgeFinalOrderForm form, CaseData caseData) {
        return form.setCaseNumber(caseData.getCcdCaseReference().toString())
            .setLegacyNumber(caseData.getLegacyCaseReference())
            .setClaimant1Name(caseData.getApplicant1().getPartyName())
            .setClaimant2Name(nonNull(caseData.getApplicant2()) ? caseData.getApplicant2().getPartyName() : null)
            .setDefendant1Name(caseData.getRespondent1().getPartyName())
            .setDefendant2Name(nonNull(caseData.getRespondent2()) ? caseData.getRespondent2().getPartyName() : null)
            .setClaimantNum(nonNull(caseData.getApplicant2()) ? "Claimant 1" : "Claimant")
            .setDefendantNum(nonNull(caseData.getRespondent2()) ? "Defendant 1" : "Defendant")
            .setCaseName(caseData.getCaseNameHmctsInternal())
            .setClaimantReference(nonNull(caseData.getSolicitorReferences())
                                   ? caseData.getSolicitorReferences().getApplicantSolicitor1Reference() : null)
            .setDefendantReference(nonNull(caseData.getSolicitorReferences())
                                    ? caseData.getSolicitorReferences().getRespondentSolicitor1Reference() : null);
    }
}
