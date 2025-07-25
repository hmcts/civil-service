package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertasks.setapplicantresponsedeadlinespec;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.dq.Expert;
import uk.gov.hmcts.reform.civil.model.dq.Experts;

import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.model.dq.Expert.fromSmallClaimExpertDetails;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

@Component
@Slf4j
public class Respondent1ExpertsCaseDataUpdaters implements ExpertsAndWitnessesCaseDataUpdater {

    @Override
    public void update(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedData) {
        log.info("Updating Respondent1ExpertsCaseData for caseId: {}", caseData.getCcdCaseReference());

        if (caseData.getRespondent1DQ() != null) {
            if (YES.equals(caseData.getResponseClaimExpertSpecRequired())
                    && caseData.getRespondent1DQ().getSmallClaimExperts() != null) {
                log.debug("Setting respondent1DQExperts with expert details for caseId: {}", caseData.getCcdCaseReference());
                Expert expert = fromSmallClaimExpertDetails(caseData.getRespondent1DQ().getSmallClaimExperts());
                updatedData.respondent1DQ(
                        updatedData.build().getRespondent1DQ().toBuilder()
                                .respondent1DQExperts(Experts.builder()
                                        .expertRequired(caseData.getResponseClaimExpertSpecRequired())
                                        .details(wrapElements(expert))
                                        .build())
                                .build());
            } else if (NO.equals(caseData.getResponseClaimExpertSpecRequired())) {
                log.debug("Setting respondent1DQExperts with expertRequired as NO for caseId: {}", caseData.getCcdCaseReference());
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
