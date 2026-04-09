package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertasks.setapplicantresponsedeadlinespec;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.dq.Expert;
import uk.gov.hmcts.reform.civil.model.dq.Experts;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;

import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.model.dq.Expert.fromSmallClaimExpertDetails;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

@Component
@Slf4j
public class Respondent1ExpertsCaseDataUpdaters implements ExpertsAndWitnessesCaseDataUpdater {

    @Override
    public CaseData update(CaseData caseData) {
        log.info("Updating Respondent1ExpertsCaseData for caseId: {}", caseData.getCcdCaseReference());

        if (caseData.getRespondent1DQ() != null) {
            if (YES.equals(caseData.getResponseClaimExpertSpecRequired())
                    && caseData.getRespondent1DQ().getSmallClaimExperts() != null) {
                log.info("Setting respondent1DQExperts with expert details for caseId: {}", caseData.getCcdCaseReference());
                Expert expert = fromSmallClaimExpertDetails(caseData.getRespondent1DQ().getSmallClaimExperts());
                Experts experts = new Experts();
                experts.setExpertRequired(caseData.getResponseClaimExpertSpecRequired());
                experts.setDetails(wrapElements(expert));
                Respondent1DQ respondent1DQ = caseData.getRespondent1DQ();
                respondent1DQ.setRespondent1DQExperts(experts);
                caseData.setRespondent1DQ(respondent1DQ);
            } else if (NO.equals(caseData.getResponseClaimExpertSpecRequired())) {
                log.info("Setting respondent1DQExperts with expertRequired as NO for caseId: {}", caseData.getCcdCaseReference());
                Experts experts = new Experts();
                experts.setExpertRequired(caseData.getResponseClaimExpertSpecRequired());
                Respondent1DQ respondent1DQ = caseData.getRespondent1DQ();
                respondent1DQ.setRespondent1DQExperts(experts);
                caseData.setRespondent1DQ(respondent1DQ);
            }
        }
        return caseData;
    }
}
