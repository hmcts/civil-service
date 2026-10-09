package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertasks.setapplicantresponsedeadlinespec;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.dq.Respondent2DQ;
import uk.gov.hmcts.reform.civil.model.dq.Witnesses;

import static uk.gov.hmcts.reform.civil.utils.WitnessUtils.preservePartyIdsFromExisting;

@Component
@Slf4j
public class Respondent2WitnessesCaseDataUpdater implements ExpertsAndWitnessesCaseDataUpdater {

    @Override
    public CaseData update(CaseData caseData) {
        log.info("Updating Respondent2WitnessesCaseData for caseId: {}", caseData.getCcdCaseReference());

        if (caseData.getRespondent2DQWitnessesSmallClaim() != null) {
            log.info("Setting respondent2DQWitnesses with small claim witnesses for caseId: {}", caseData.getCcdCaseReference());
            Respondent2DQ respondent2DQ = caseData.getRespondent2DQ();
            Witnesses smallClaimWitnesses = caseData.getRespondent2DQWitnessesSmallClaim();
            preservePartyIdsFromExisting(smallClaimWitnesses, respondent2DQ != null ? respondent2DQ.getWitnesses() : null);
            respondent2DQ.setRespondent2DQWitnesses(smallClaimWitnesses);
            caseData.setRespondent2DQ(respondent2DQ);
        }
        return caseData;
    }
}
