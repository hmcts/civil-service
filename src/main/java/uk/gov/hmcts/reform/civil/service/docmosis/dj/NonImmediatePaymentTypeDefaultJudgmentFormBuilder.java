package uk.gov.hmcts.reform.civil.service.docmosis.dj;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.helpers.DateFormatHelper;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.docmosis.dj.DefaultJudgmentForm;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.utils.InterestCalculator;

import java.util.Objects;

@Component
public class NonImmediatePaymentTypeDefaultJudgmentFormBuilder extends DefaultJudgmentFormBuilder implements StandardDefaultJudgmentBuilder {

    @Autowired
    public NonImmediatePaymentTypeDefaultJudgmentFormBuilder(InterestCalculator interestCalculator,
                                                             JudgmentAmountsCalculator judgmentAmountsCalculator,
                                                             OrganisationService organisationService) {
        super(interestCalculator, judgmentAmountsCalculator, organisationService);
    }

    public DefaultJudgmentForm getDefaultJudgmentForm(CaseData caseData,
                                                      uk.gov.hmcts.reform.civil.model.Party respondent,
                                                      String event,
                                                      boolean addReferenceOfSecondRes) {

        return super.getDefaultJudgmentForm(caseData, respondent, event, addReferenceOfSecondRes)
            .toBuilder().paymentPlan(caseData.getPaymentTypeSelection().name())
            .payByDate(Objects.isNull(caseData.getPaymentSetDate()) ? null :
                DateFormatHelper.formatLocalDate(caseData.getPaymentSetDate(), DateFormatHelper.DATE))
            .repaymentFrequency(Objects.isNull(caseData.getRepaymentFrequency()) ? null : getRepaymentFrequency(caseData.getRepaymentFrequency()))
            .paymentStr(Objects.isNull(caseData.getRepaymentFrequency()) ? null : getRepaymentString(caseData.getRepaymentFrequency()))
            .installmentAmount(Objects.isNull(caseData.getRepaymentSuggestion()) ? null : getInstallmentAmount(caseData.getRepaymentSuggestion()))
            .repaymentDate(Objects.isNull(caseData.getRepaymentDate()) ? null :
                DateFormatHelper.formatLocalDate(caseData.getRepaymentDate(), DateFormatHelper.DATE))
            .build();
    }
}
