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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

@RequiredArgsConstructor
public abstract class DefaultJudgmentFormBuilderBase {

    private final InterestCalculator interestCalculator;
    private final JudgmentAmountsCalculator judgmentAmountsCalculator;
    private final OrganisationService organisationService;
    private final DjWelshTextService djWelshTextService;

    protected Party getApplicantOrgDetails(OrganisationPolicy organisationPolicy) {

        return Optional.ofNullable(organisationPolicy)
            .map(OrganisationPolicy::getOrganisation)
            .map(Organisation::getOrganisationID)
            .map(organisationService::findOrganisationById)
            .flatMap(value -> value.map(o -> new Party()
                .setName(o.getName())
                .setPrimaryAddress(AddressUtils.getAddress(o.getContactInformation().get(0))))).orElse(null);
    }

    protected Party getPartyDetails(uk.gov.hmcts.reform.civil.model.Party party) {
        return new Party()
            .setName(party.getPartyName())
            .setPrimaryAddress(party.getPrimaryAddress());
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
        return djWelshTextService.getRepaymentString(repaymentFrequency, isWelsh);
    }

    protected String getRepaymentFrequency(RepaymentFrequencyDJ repaymentFrequencyDJ, boolean isWelsh) {
        return djWelshTextService.getRepaymentFrequency(repaymentFrequencyDJ, isWelsh);
    }

    protected String getDateInWelsh(LocalDate dateToConvert) {
        return djWelshTextService.getDateInWelsh(dateToConvert);
    }

    protected String getInstallmentAmount(String amount) {
        return djWelshTextService.getInstallmentAmount(amount);
    }
}
