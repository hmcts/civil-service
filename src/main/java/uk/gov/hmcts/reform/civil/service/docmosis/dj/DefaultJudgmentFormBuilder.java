package uk.gov.hmcts.reform.civil.service.docmosis.dj;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.docmosis.dj.DefaultJudgmentForm;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.utils.InterestCalculator;

import java.math.BigDecimal;
import java.util.Objects;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GENERATE_DJ_FORM_SPEC;
import static uk.gov.hmcts.reform.civil.utils.JudgmentOnlineUtils.getApplicant;

@Component
public class DefaultJudgmentFormBuilder extends DefaultJudgmentFormBuilderBase implements StandardDefaultJudgmentBuilder {

    @Autowired
    public DefaultJudgmentFormBuilder(InterestCalculator interestCalculator, JudgmentAndSettlementAmountsCalculator judgmentAndSettlementAmountsCalculator,
                                      OrganisationService organisationService) {
        super(interestCalculator, judgmentAndSettlementAmountsCalculator, organisationService);
    }

    public DefaultJudgmentForm getDefaultJudgmentForm(CaseData caseData,
                                                      uk.gov.hmcts.reform.civil.model.Party respondent,
                                                      String event,
                                                      boolean addReferenceOfSecondRes) {
        BigDecimal debtAmount = event.equals(GENERATE_DJ_FORM_SPEC.name())
            ? getDebtAmount(caseData) : BigDecimal.ZERO;
        BigDecimal cost = event.equals(GENERATE_DJ_FORM_SPEC.name())
            ? getClaimFee(caseData) : BigDecimal.ZERO;

        if (event.equals(GENERATE_DJ_FORM_SPEC.name()) && debtAmount.signum() < 1) {
            cost = cost.add(debtAmount);
            debtAmount = BigDecimal.ZERO;
        }

        String respReference = null;
        if (caseData.getSolicitorReferences() != null) {
            respReference = addReferenceOfSecondRes ? caseData.getSolicitorReferences()
                .getRespondentSolicitor1Reference() : null;
        }

        return DefaultJudgmentForm.builder()
            .caseNumber(caseData.getLegacyCaseReference())
            .formText("No response,")
            .applicant(getApplicant(caseData.getApplicant1(), caseData.getApplicant2()))
            .respondent(getPartyDetails(respondent))
            .claimantLR(getApplicantOrgDetails(caseData.getApplicant1OrganisationPolicy())
            )
            .debt(debtAmount.toString())
            .costs(cost.toString())
            .totalCost(debtAmount.add(cost).setScale(2).toString())
            .applicantReference(Objects.isNull(caseData.getSolicitorReferences())
                ? null : caseData.getSolicitorReferences()
                .getApplicantSolicitor1Reference())
            .respondentReference(Objects.isNull(caseData.getSolicitorReferences())
                ? null : respReference).build();
    }

}
