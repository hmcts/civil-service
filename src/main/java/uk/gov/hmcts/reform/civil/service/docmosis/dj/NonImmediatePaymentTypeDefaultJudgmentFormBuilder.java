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
                                                             OrganisationService organisationService,
                                                             DjWelshTextService djWelshTextService) {
        super(interestCalculator, judgmentAmountsCalculator, organisationService, djWelshTextService);
    }

    public DefaultJudgmentForm getDefaultJudgmentForm(CaseData caseData,
                                                      uk.gov.hmcts.reform.civil.model.Party respondent,
                                                      String event,
                                                      boolean addReferenceOfSecondRes) {

        return super.getDefaultJudgmentForm(caseData, respondent, event, addReferenceOfSecondRes)
            .copy()
            .setPaymentPlan(caseData.getPaymentTypeSelection().name())
            .setPayByDate(Objects.isNull(caseData.getPaymentSetDate()) ? null :
                DateFormatHelper.formatLocalDate(caseData.getPaymentSetDate(), DateFormatHelper.DATE))
            .setRepaymentFrequency(Objects.isNull(caseData.getRepaymentFrequency()) ? null : getRepaymentFrequency(caseData.getRepaymentFrequency(),
                                                                                                                false))
            .setPaymentStr(Objects.isNull(caseData.getRepaymentFrequency()) ? null : getRepaymentString(caseData.getRepaymentFrequency(),
                                                                                                     false))
            .setInstallmentAmount(Objects.isNull(caseData.getRepaymentSuggestion()) ? null : getInstallmentAmount(caseData.getRepaymentSuggestion()))
            .setRepaymentDate(Objects.isNull(caseData.getRepaymentDate()) ? null :
                DateFormatHelper.formatLocalDate(caseData.getRepaymentDate(), DateFormatHelper.DATE));
    }
}
