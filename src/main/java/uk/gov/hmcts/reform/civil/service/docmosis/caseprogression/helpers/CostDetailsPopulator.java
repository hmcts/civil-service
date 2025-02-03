package uk.gov.hmcts.reform.civil.service.docmosis.caseprogression.helpers;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.finalorders.CostEnums;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.docmosis.casepogression.JudgeFinalOrderForm;
import uk.gov.hmcts.reform.civil.model.docmosis.casepogression.JudgeFinalOrderForm.JudgeFinalOrderFormBuilder;
import uk.gov.hmcts.reform.civil.utils.MonetaryConversions;

import java.time.LocalDate;

import static java.lang.String.format;
import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@Component
public class CostDetailsPopulator {

    public JudgeFinalOrderForm.JudgeFinalOrderFormBuilder populateCostsDetails(JudgeFinalOrderFormBuilder builder, CaseData caseData) {
        return builder.costSelection(caseData.getAssistedOrderCostList().name())
            .costsReservedText(nonNull(caseData.getAssistedOrderCostsReserved())
                                   ? caseData.getAssistedOrderCostsReserved().getDetailsRepresentationText() : null)
            .bespokeCostText(nonNull(caseData.getAssistedOrderCostsBespoke())
                                 ? caseData.getAssistedOrderCostsBespoke().getBesPokeCostDetailsText() : null)
            .summarilyAssessed(getSummarilyAssessed(caseData))
            .summarilyAssessedDate(getSummarilyAssessedDate(caseData))
            .detailedAssessment(getDetailedAssessment(caseData))
            .interimPayment(getInterimPayment(caseData))
            .interimPaymentDate(getInterimPaymentDate(caseData))
            .qcosProtection(getQcosProtection(caseData))
            .costsProtection(caseData.getPublicFundingCostsProtection().equals(YES) ? "true" : null);
    }

    private String getSummarilyAssessed(CaseData caseData) {
        return nonNull(caseData.getAssistedOrderMakeAnOrderForCosts())
            && nonNull(caseData.getAssistedOrderMakeAnOrderForCosts().getMakeAnOrderForCostsList())
            && caseData.getAssistedOrderMakeAnOrderForCosts().getAssistedOrderClaimantDefendantFirstDropdown().equals(
            CostEnums.COSTS)
            ? populateSummarilyAssessedText(caseData) : null;
    }

    public String populateSummarilyAssessedText(CaseData caseData) {
        if (caseData.getAssistedOrderMakeAnOrderForCosts().getMakeAnOrderForCostsList().equals(CostEnums.CLAIMANT)) {
            return format(
                "The claimant shall pay the defendant's costs (both fixed and summarily assessed as appropriate) "
                    + "in the sum of £%s. Such sum shall be paid by 4pm on",
                MonetaryConversions.penniesToPounds(caseData.getAssistedOrderMakeAnOrderForCosts().getAssistedOrderCostsFirstDropdownAmount()));
        } else {
            return format(
                "The defendant shall pay the claimant's costs (both fixed and summarily assessed as appropriate) "
                    + "in the sum of £%s. Such sum shall be paid by 4pm on",
                MonetaryConversions.penniesToPounds(caseData.getAssistedOrderMakeAnOrderForCosts().getAssistedOrderCostsFirstDropdownAmount()));
        }
    }

    private LocalDate getSummarilyAssessedDate(CaseData caseData) {
        return nonNull(caseData.getAssistedOrderMakeAnOrderForCosts())
            && nonNull(caseData.getAssistedOrderMakeAnOrderForCosts().getMakeAnOrderForCostsList())
            && caseData.getAssistedOrderMakeAnOrderForCosts().getAssistedOrderClaimantDefendantFirstDropdown().equals(
            CostEnums.COSTS)
            ? caseData.getAssistedOrderMakeAnOrderForCosts().getAssistedOrderCostsFirstDropdownDate() : null;
    }

    private String getDetailedAssessment(CaseData caseData) {
        return nonNull(caseData.getAssistedOrderMakeAnOrderForCosts())
            && nonNull(caseData.getAssistedOrderMakeAnOrderForCosts().getMakeAnOrderForCostsList())
            && caseData.getAssistedOrderMakeAnOrderForCosts().getAssistedOrderClaimantDefendantFirstDropdown().equals(
            CostEnums.SUBJECT_DETAILED_ASSESSMENT)
            ? populateDetailedAssessmentText(caseData) : null;
    }

    private String getInterimPayment(CaseData caseData) {
        return nonNull(caseData.getAssistedOrderMakeAnOrderForCosts())
            && nonNull(caseData.getAssistedOrderMakeAnOrderForCosts().getMakeAnOrderForCostsList())
            && caseData.getAssistedOrderMakeAnOrderForCosts().getAssistedOrderClaimantDefendantFirstDropdown().equals(
            CostEnums.SUBJECT_DETAILED_ASSESSMENT)
            && caseData.getAssistedOrderMakeAnOrderForCosts().getAssistedOrderAssessmentSecondDropdownList2().equals(
            CostEnums.YES)
            ? populateInterimPaymentText(caseData) : null;
    }

    private LocalDate getInterimPaymentDate(CaseData caseData) {
        return nonNull(caseData.getAssistedOrderMakeAnOrderForCosts())
            && nonNull(caseData.getAssistedOrderMakeAnOrderForCosts().getMakeAnOrderForCostsList())
            && caseData.getAssistedOrderMakeAnOrderForCosts().getAssistedOrderClaimantDefendantFirstDropdown().equals(
            CostEnums.SUBJECT_DETAILED_ASSESSMENT)
            ? caseData.getAssistedOrderMakeAnOrderForCosts().getAssistedOrderAssessmentThirdDropdownDate() : null;
    }

    private String getQcosProtection(CaseData caseData) {
        return nonNull(caseData.getAssistedOrderMakeAnOrderForCosts())
            && nonNull(caseData.getAssistedOrderMakeAnOrderForCosts().getMakeAnOrderForCostsYesOrNo())
            && caseData.getAssistedOrderMakeAnOrderForCosts().getMakeAnOrderForCostsYesOrNo().equals(
            YES) ? "true" : null;
    }

    public String populateDetailedAssessmentText(CaseData caseData) {
        String standardOrIndemnity;
        if (caseData.getAssistedOrderMakeAnOrderForCosts().getAssistedOrderAssessmentSecondDropdownList1().equals(
            CostEnums.INDEMNITY_BASIS)) {
            standardOrIndemnity = "on the indemnity basis if not agreed";
        } else {
            standardOrIndemnity = "on the standard basis if not agreed";
        }

        if (caseData.getAssistedOrderMakeAnOrderForCosts().getMakeAnOrderForCostsList().equals(CostEnums.CLAIMANT)) {
            return format(
                "The claimant shall pay the defendant's costs to be subject to a detailed assessment %s",
                standardOrIndemnity
            );
        }
        return format(
            "The defendant shall pay the claimant's costs to be subject to a detailed assessment %s",
            standardOrIndemnity
        );
    }

    public String populateInterimPaymentText(CaseData caseData) {
        return format(
            "An interim payment of £%s on account of costs shall be paid by 4pm on ",
            MonetaryConversions.penniesToPounds(caseData.getAssistedOrderMakeAnOrderForCosts().getAssistedOrderAssessmentThirdDropdownAmount()));
    }

}
