package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertasks.setapplicantresponsedeadlinespec;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;

@Component
@Slf4j
public class Respondent1WitnessesCaseDataUpdater implements ExpertsAndWitnessesCaseDataUpdater {

    @Override
    public CaseData update(CaseData caseData) {
        log.info("Updating Respondent1WitnessesCaseData for caseId: {}", caseData.getCcdCaseReference());

        if (caseData.getRespondent1DQWitnessesSmallClaim() != null) {
            log.info("Setting respondent1DQWitnesses with small claim witnesses for caseId: {}", caseData.getCcdCaseReference());
            Respondent1DQ respondent1DQ = caseData.getRespondent1DQ();
            respondent1DQ.setRespondent1DQWitnesses(caseData.getRespondent1DQWitnessesSmallClaim());
            caseData.setRespondent1DQ(respondent1DQ);
        }
        return caseData;
    }
}
