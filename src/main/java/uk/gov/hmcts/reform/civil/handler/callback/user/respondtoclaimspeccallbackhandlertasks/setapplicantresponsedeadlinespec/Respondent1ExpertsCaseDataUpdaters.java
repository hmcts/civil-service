package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertasks.setapplicantresponsedeadlinespec;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.dq.Expert;
import uk.gov.hmcts.reform.civil.model.dq.Experts;

import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.model.dq.Expert.fromSmallClaimExpertDetails;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

@Component
public class Respondent1ExpertsCaseDataUpdaters implements ExpertsAndWitnessesCaseDataUpdater {

    @Override
    public void update(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedData) {
        if (caseData.getRespondent1DQ() != null) {
            if (YES.equals(caseData.getResponseClaimExpertSpecRequired())
                    && caseData.getRespondent1DQ().getSmallClaimExperts() != null) {
                Expert expert = fromSmallClaimExpertDetails(caseData.getRespondent1DQ().getSmallClaimExperts());
                updatedData.respondent1DQ(
                        updatedData.build().getRespondent1DQ().toBuilder()
                                .respondent1DQExperts(Experts.builder()
                                        .expertRequired(caseData.getResponseClaimExpertSpecRequired())
                                        .details(wrapElements(expert))
                                        .build())
                                .build());
            } else if (NO.equals(caseData.getResponseClaimExpertSpecRequired())) {
                updatedData.respondent1DQ(
                        updatedData.build().getRespondent1DQ().toBuilder()
                                .respondent1DQExperts(Experts.builder()
                                        .expertRequired(caseData.getResponseClaimExpertSpecRequired())
                                        .build())
                                .build());
            }
        }
    }
}
