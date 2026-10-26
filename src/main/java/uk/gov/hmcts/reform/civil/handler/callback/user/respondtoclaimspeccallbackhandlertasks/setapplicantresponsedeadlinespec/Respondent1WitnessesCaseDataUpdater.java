package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertasks.setapplicantresponsedeadlinespec;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Witnesses;

import static uk.gov.hmcts.reform.civil.utils.WitnessUtils.preservePartyIdsFromExisting;

@Component
@Slf4j
public class Respondent1WitnessesCaseDataUpdater implements ExpertsAndWitnessesCaseDataUpdater {

    @Override
    public CaseData update(CaseData caseData) {
        log.info("Updating Respondent1WitnessesCaseData for caseId: {}", caseData.getCcdCaseReference());

        if (caseData.getRespondent1DQWitnessesSmallClaim() != null && caseData.getRespondent1DQ() != null) {
            log.info("Setting respondent1DQWitnesses with small claim witnesses for caseId: {}", caseData.getCcdCaseReference());
            Respondent1DQ respondent1DQ = caseData.getRespondent1DQ();
            Witnesses smallClaimWitnesses = caseData.getRespondent1DQWitnessesSmallClaim();
            preservePartyIdsFromExisting(smallClaimWitnesses, respondent1DQ.getWitnesses());
            respondent1DQ.setRespondent1DQWitnesses(smallClaimWitnesses);
            caseData.setRespondent1DQ(respondent1DQ);
        }
        return caseData;
    }
}
