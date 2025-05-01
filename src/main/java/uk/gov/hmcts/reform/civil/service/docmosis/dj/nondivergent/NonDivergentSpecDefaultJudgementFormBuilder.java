package uk.gov.hmcts.reform.civil.service.docmosis.dj.nondivergent;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.helpers.DateFormatHelper;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.docmosis.common.Party;
import uk.gov.hmcts.reform.civil.model.docmosis.dj.DefaultJudgmentForm;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.service.docmosis.dj.DefaultJudgmentFormBuilderBase;
import uk.gov.hmcts.reform.civil.service.docmosis.dj.JudgmentAmountsCalculator;
import uk.gov.hmcts.reform.civil.utils.InterestCalculator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

import static uk.gov.hmcts.reform.civil.utils.JudgmentOnlineUtils.getApplicant;
import static uk.gov.hmcts.reform.civil.utils.JudgmentOnlineUtils.getApplicantSolicitorRef;
import static uk.gov.hmcts.reform.civil.utils.JudgmentOnlineUtils.getRespondent1SolicitorRef;
import static uk.gov.hmcts.reform.civil.utils.JudgmentOnlineUtils.getRespondent2SolicitorRef;

@Component
public class NonDivergentSpecDefaultJudgementFormBuilder extends DefaultJudgmentFormBuilderBase {

    private static final String RESPONDENT_1 = "respondent1";

    @Autowired
    public NonDivergentSpecDefaultJudgementFormBuilder(InterestCalculator interestCalculator,
                                                       JudgmentAmountsCalculator judgmentAmountsCalculator,
                                                       OrganisationService organisationService) {
        super(interestCalculator, judgmentAmountsCalculator, organisationService);
    }

    public DefaultJudgmentForm getDefaultJudgmentForm(CaseData caseData, String partyType) {
        BigDecimal debtAmount = getDebtAmount(caseData);
        BigDecimal cost = getClaimFee(caseData);

        DefaultJudgmentForm.DefaultJudgmentFormBuilder builder = DefaultJudgmentForm.builder();
        builder
            .caseNumber(caseData.getLegacyCaseReference())
            .formText("No response,")
            .applicant(getApplicant(caseData.getApplicant1(), caseData.getApplicant2()))
            .respondent(getRespondentLROrLipDetails(caseData, partyType))
            .debt(debtAmount.toString())
            .costs(cost.toString())
            .totalCost(debtAmount.add(cost).setScale(2).toString())
            .applicantReference(getApplicantSolicitorRef(caseData))
            .respondentReference(getRespondent1SolicitorRef(caseData))
            .respondent1Name(caseData.getRespondent1().getPartyName())
            .respondent2Name(Objects.isNull(caseData.getRespondent2()) ? null : caseData.getRespondent2().getPartyName())
            .respondent1Ref(getRespondent1SolicitorRef(caseData))
            .respondent2Ref(getRespondent2SolicitorRef(caseData))
            .claimantLR(getClaimantLipOrLRDetailsForPaymentAddress(caseData))
            .applicantDetails(getClaimantLipOrLRDetailsForPaymentAddress(caseData))
            .paymentPlan(caseData.getPaymentTypeSelection().name())
            .payByDate(Objects.isNull(caseData.getPaymentSetDate()) ? null :
                           DateFormatHelper.formatLocalDate(caseData.getPaymentSetDate(), DateFormatHelper.DATE))
            .repaymentFrequency(Objects.isNull(caseData.getRepaymentFrequency()) ? null : getRepaymentFrequency(
                caseData.getRepaymentFrequency(), false))
            .paymentStr(Objects.isNull(caseData.getRepaymentFrequency()) ? null : getRepaymentString(
                caseData.getRepaymentFrequency(), false))
            .welshRepaymentFrequency(Objects.isNull(caseData.getRepaymentFrequency()) ? null : getRepaymentFrequency(
                caseData.getRepaymentFrequency(), true))
            .welshPaymentStr(Objects.isNull(caseData.getRepaymentFrequency()) ? null : getRepaymentString(
                caseData.getRepaymentFrequency(), true))
            .currentDateInWelsh(getDateInWelsh(LocalDate.now()))
            .welshPayByDate(Objects.isNull(caseData.getPaymentSetDate()) ?  null : getDateInWelsh(caseData.getPaymentSetDate()))
            .welshRepaymentDate(Objects.isNull(caseData.getRepaymentDate()) ? null :
                                    getDateInWelsh(caseData.getRepaymentDate()))
            .installmentAmount(Objects.isNull(caseData.getRepaymentSuggestion()) ? null : getInstallmentAmount(caseData.getRepaymentSuggestion()))
            .repaymentDate(Objects.isNull(caseData.getRepaymentDate()) ? null :
                DateFormatHelper.formatLocalDate(caseData.getRepaymentDate(), DateFormatHelper.DATE));
        return builder.build();
    }

    private Party getRespondentLROrLipDetails(CaseData caseData, String partyType) {
        if (partyType.equals(RESPONDENT_1)) {
            if (caseData.isRespondent1LiP()) {
                return getPartyDetails(caseData.getRespondent1());
            } else {
                if (caseData.getRespondent1OrganisationPolicy() != null) {
                    return getApplicantOrgDetails(caseData.getRespondent1OrganisationPolicy());
                } else {
                    return null;
                }
            }
        } else {
            if (caseData.isRespondent2LiP()) {
                return getPartyDetails(caseData.getRespondent2());
            } else {
                if (caseData.getRespondent2OrganisationPolicy() != null) {
                    return getApplicantOrgDetails(caseData.getRespondent2OrganisationPolicy());
                } else {
                    return null;
                }
            }
        }
    }

    private Party getClaimantLipOrLRDetailsForPaymentAddress(CaseData caseData) {
        if (caseData.isApplicantLiP()) {
            return getPartyDetails(caseData.getApplicant1());
        } else {
            if (caseData.getApplicant1OrganisationPolicy() != null) {
                return getApplicantOrgDetails(caseData.getApplicant1OrganisationPolicy());
            } else {
                return null;
            }
        }
    }
}
