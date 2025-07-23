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
public class Respondent2ExpertsCaseDataUpdater implements ExpertsAndWitnessesCaseDataUpdater {

    @Override
    public void update(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedData) {
        log.info("Updating Respondent2ExpertsCaseData for caseId: {}", caseData.getCcdCaseReference());

        if (caseData.getRespondent2DQ() != null) {
            if (YES.equals(caseData.getResponseClaimExpertSpecRequired2())
                    && caseData.getRespondent2DQ().getSmallClaimExperts() != null) {
                log.debug("Setting respondent2DQExperts with expert details for caseId: {}", caseData.getCcdCaseReference());
                Expert expert = fromSmallClaimExpertDetails(caseData.getRespondent2DQ().getSmallClaimExperts());
                updatedData.respondent2DQ(
                        updatedData.build().getRespondent2DQ().toBuilder()
                                .respondent2DQExperts(Experts.builder()
                                        .expertRequired(caseData.getResponseClaimExpertSpecRequired2())
                                        .details(wrapElements(expert))
                                        .build())
                                .build());
            } else if (NO.equals(caseData.getResponseClaimExpertSpecRequired2())) {
                log.debug("Setting respondent2DQExperts with expertRequired as NO for caseId: {}", caseData.getCcdCaseReference());
                updatedData.respondent2DQ(
                        updatedData.build().getRespondent2DQ().toBuilder()
                                .respondent2DQExperts(Experts.builder()
                                        .expertRequired(caseData.getResponseClaimExpertSpecRequired2())
                                        .build())
                                .build());
            }
        }
    }
}
