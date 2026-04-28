package uk.gov.hmcts.reform.civil.service.docmosis.dj.nondivergent;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.helpers.DateFormatHelper;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.docmosis.common.Party;
import uk.gov.hmcts.reform.civil.model.docmosis.dj.DefaultJudgmentForm;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.service.docmosis.dj.DefaultJudgmentFormBuilderBase;
import uk.gov.hmcts.reform.civil.service.docmosis.dj.DjWelshTextService;
import uk.gov.hmcts.reform.civil.service.docmosis.dj.JudgmentAmountsCalculator;
import uk.gov.hmcts.reform.civil.utils.InterestCalculator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

import static uk.gov.hmcts.reform.civil.utils.JudgmentOnlineUtils.getApplicant1Details;
import static uk.gov.hmcts.reform.civil.utils.JudgmentOnlineUtils.getApplicants;
import static uk.gov.hmcts.reform.civil.utils.JudgmentOnlineUtils.getApplicantSolicitorRef;
import static uk.gov.hmcts.reform.civil.utils.JudgmentOnlineUtils.getRespondent1Details;
import static uk.gov.hmcts.reform.civil.utils.JudgmentOnlineUtils.getRespondent1SolicitorRef;
import static uk.gov.hmcts.reform.civil.utils.JudgmentOnlineUtils.getRespondent2Details;
import static uk.gov.hmcts.reform.civil.utils.JudgmentOnlineUtils.getRespondent2SolicitorRef;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getRespondent1NameWithLitigiousFriend;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getRespondent2NameWithLitigiousFriend;

@Component
public class NonDivergentSpecDefaultJudgementFormBuilder extends DefaultJudgmentFormBuilderBase {

    private static final String RESPONDENT_1 = "respondent1";

    @Autowired
    public NonDivergentSpecDefaultJudgementFormBuilder(InterestCalculator interestCalculator,
                                                       JudgmentAmountsCalculator judgmentAmountsCalculator,
                                                       OrganisationService organisationService,
                                                       DjWelshTextService djWelshTextService) {
        super(interestCalculator, judgmentAmountsCalculator, organisationService, djWelshTextService);
    }

    public DefaultJudgmentForm getDefaultJudgmentForm(CaseData caseData, String partyType) {
        BigDecimal debtAmount = getDebtAmount(caseData);
        BigDecimal cost = getClaimFee(caseData);

        DefaultJudgmentForm defaultJudgmentForm = new DefaultJudgmentForm();
        defaultJudgmentForm
            .setCaseNumber(caseData.getLegacyCaseReference())
            .setCcdCaseReference(String.valueOf(caseData.getLegacyCaseReference()))
            .setFormText("No response,")
            .setApplicant(getApplicants(caseData))
            .setRespondent(getRespondentLROrLipDetails(caseData, partyType))
            .setDebt(debtAmount.toString())
            .setCosts(cost.toString())
            .setTotalCost(debtAmount.add(cost).setScale(2).toString())
            .setApplicantReference(getApplicantSolicitorRef(caseData))
            .setRespondentReference(getRespondent1SolicitorRef(caseData))
            .setRespondent1Name(getRespondent1NameWithLitigiousFriend(caseData))
            .setRespondent2Name(Objects.isNull(caseData.getRespondent2()) ? null : getRespondent2NameWithLitigiousFriend(caseData))
            .setRespondent1Ref(getRespondent1SolicitorRef(caseData))
            .setRespondent2Ref(getRespondent2SolicitorRef(caseData))
            .setClaimantLR(getClaimantLipOrLRDetailsForPaymentAddress(caseData))
            .setApplicantDetails(getClaimantLipOrLRDetailsForPaymentAddress(caseData))
            .setPaymentPlan(caseData.getPaymentTypeSelection().name())
            .setPayByDate(Objects.isNull(caseData.getPaymentSetDate()) ? null :
                           DateFormatHelper.formatLocalDate(caseData.getPaymentSetDate(), DateFormatHelper.DATE))
            .setRepaymentFrequency(Objects.isNull(caseData.getRepaymentFrequency()) ? null : getRepaymentFrequency(
                caseData.getRepaymentFrequency(), false))
            .setPaymentStr(Objects.isNull(caseData.getRepaymentFrequency()) ? null : getRepaymentString(
                caseData.getRepaymentFrequency(), false))
            .setWelshRepaymentFrequency(Objects.isNull(caseData.getRepaymentFrequency()) ? null : getRepaymentFrequency(
                caseData.getRepaymentFrequency(), true))
            .setWelshPaymentStr(Objects.isNull(caseData.getRepaymentFrequency()) ? null : getRepaymentString(
                caseData.getRepaymentFrequency(), true))
            .setCurrentDateInWelsh(getDateInWelsh(LocalDate.now()))
            .setWelshPayByDate(Objects.isNull(caseData.getPaymentSetDate()) ?  null : getDateInWelsh(caseData.getPaymentSetDate()))
            .setWelshRepaymentDate(Objects.isNull(caseData.getRepaymentDate()) ? null :
                                    getDateInWelsh(caseData.getRepaymentDate()))
            .setInstallmentAmount(Objects.isNull(caseData.getRepaymentSuggestion()) ? null : getInstallmentAmount(caseData.getRepaymentSuggestion()))
            .setRepaymentDate(Objects.isNull(caseData.getRepaymentDate()) ? null :
                DateFormatHelper.formatLocalDate(caseData.getRepaymentDate(), DateFormatHelper.DATE));
        return defaultJudgmentForm;
    }

    private Party getRespondentLROrLipDetails(CaseData caseData, String partyType) {
        boolean respondentOne = RESPONDENT_1.equals(partyType);

        if (isRespondentLip(caseData, respondentOne)) {
            return getRespondentLipDetails(caseData, respondentOne);
        }

        return getRespondentOrganisationDetails(caseData, respondentOne);
    }

    private boolean isRespondentLip(CaseData caseData, boolean respondentOne) {
        return respondentOne ? caseData.isRespondent1LiP() : caseData.isRespondent2LiP();
    }

    private Party getRespondentLipDetails(CaseData caseData, boolean respondentOne) {
        return respondentOne ? getRespondent1Details(caseData) : getRespondent2Details(caseData);
    }

    private Party getRespondentOrganisationDetails(CaseData caseData, boolean respondentOne) {
        if (respondentOne && caseData.getRespondent1OrganisationPolicy() != null) {
            return getApplicantOrgDetails(caseData.getRespondent1OrganisationPolicy());
        }
        if (!respondentOne && caseData.getRespondent2OrganisationPolicy() != null) {
            return getApplicantOrgDetails(caseData.getRespondent2OrganisationPolicy());
        }
        return null;
    }

    private Party getClaimantLipOrLRDetailsForPaymentAddress(CaseData caseData) {
        if (caseData.isApplicantLiP()) {
            return getApplicant1Details(caseData);
        } else {
            if (caseData.getApplicant1OrganisationPolicy() != null) {
                return getApplicantOrgDetails(caseData.getApplicant1OrganisationPolicy());
            } else {
                return null;
            }
        }
    }
}
