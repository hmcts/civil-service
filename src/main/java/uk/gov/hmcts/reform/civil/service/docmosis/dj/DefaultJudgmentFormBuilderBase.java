package uk.gov.hmcts.reform.civil.service.docmosis.dj;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.enums.RepaymentFrequencyDJ;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.docmosis.common.Party;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.utils.AddressUtils;
import uk.gov.hmcts.reform.civil.utils.InterestCalculator;
import uk.gov.hmcts.reform.civil.utils.MonetaryConversions;

import java.math.BigDecimal;
import java.util.Optional;

@RequiredArgsConstructor
public abstract class DefaultJudgmentFormBuilderBase {

    private final InterestCalculator interestCalculator;
    private final JudgmentAmountsCalculator judgmentAmountsCalculator;
    private final OrganisationService organisationService;

    protected Party getApplicantOrgDetails(OrganisationPolicy organisationPolicy) {

        return Optional.ofNullable(organisationPolicy)
            .map(OrganisationPolicy::getOrganisation)
            .map(Organisation::getOrganisationID)
            .map(organisationService::findOrganisationById)
            .flatMap(value -> value.map(o -> Party.builder()
                .name(o.getName())
                .primaryAddress(AddressUtils.getAddress(o.getContactInformation().get(0)))
                .build())).orElse(null);
    }

    protected Party getPartyDetails(uk.gov.hmcts.reform.civil.model.Party party) {
        return Party.builder()
            .name(party.getPartyName())
            .primaryAddress(party.getPrimaryAddress())
            .build();
    }

    protected BigDecimal getClaimFee(CaseData caseData) {
        return judgmentAmountsCalculator.getClaimFee(caseData);
    }

    @NotNull
    protected BigDecimal getDebtAmount(CaseData caseData) {
        BigDecimal debtAmount = judgmentAmountsCalculator.getDebtAmount(caseData).setScale(2);
        return debtAmount;
    }

    protected String getRepaymentString(RepaymentFrequencyDJ repaymentFrequency, boolean isWelsh) {
        switch (repaymentFrequency) {
            case ONCE_ONE_WEEK : return isWelsh ? "pob wythnos" : "each week";
            case ONCE_ONE_MONTH: return isWelsh ? "pob mis" : "each month";
            case ONCE_TWO_WEEKS: return isWelsh  ? "pob 2 wythnos" : "every 2 weeks";
            default:
                return null;
        }
    }

    protected String getRepaymentFrequency(RepaymentFrequencyDJ repaymentFrequencyDJ, boolean isWelsh) {
        switch (repaymentFrequencyDJ) {
            case ONCE_ONE_WEEK : return isWelsh ?  "yr wythnos" : "per week";
            case ONCE_ONE_MONTH: return isWelsh ?  "y mis" : "per month";
            case ONCE_TWO_WEEKS: return isWelsh ?  "pob 2 wythnos" : "every 2 weeks";
            default:
                return null;
        }
    }

    protected String getInstallmentAmount(String amount) {
        var regularRepaymentAmountPennies = new BigDecimal(amount);
        return String.valueOf(MonetaryConversions.penniesToPounds(regularRepaymentAmountPennies));
    }
}
