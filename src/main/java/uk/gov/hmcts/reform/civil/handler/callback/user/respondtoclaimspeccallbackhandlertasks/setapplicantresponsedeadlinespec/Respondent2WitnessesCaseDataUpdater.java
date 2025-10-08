package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertasks.setapplicantresponsedeadlinespec;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;

@Component
@Slf4j
public class Respondent2WitnessesCaseDataUpdater implements ExpertsAndWitnessesCaseDataUpdater {

    @Override
    public void update(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedData) {
        log.info("Updating Respondent2WitnessesCaseData for caseId: {}", caseData.getCcdCaseReference());

        if (caseData.getRespondent2DQWitnessesSmallClaim() != null) {
            log.debug("Setting respondent2DQWitnesses with small claim witnesses for caseId: {}", caseData.getCcdCaseReference());
            updatedData.respondent2DQ(
                    updatedData.build().getRespondent2DQ().toBuilder()
                            .respondent2DQWitnesses(caseData.getRespondent2DQWitnessesSmallClaim())
                            .build());
        }
    }
}
