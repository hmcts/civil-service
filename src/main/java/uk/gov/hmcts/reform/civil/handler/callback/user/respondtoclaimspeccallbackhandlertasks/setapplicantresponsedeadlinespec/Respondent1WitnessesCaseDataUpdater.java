package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertasks.setapplicantresponsedeadlinespec;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;

@Component
@Slf4j
public class Respondent1WitnessesCaseDataUpdater implements ExpertsAndWitnessesCaseDataUpdater {

    @Override
    public void update(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedData) {
        log.info("Updating Respondent1WitnessesCaseData for caseId: {}", caseData.getCcdCaseReference());

        if (caseData.getRespondent1DQWitnessesSmallClaim() != null) {
            log.debug("Setting respondent1DQWitnesses with small claim witnesses for caseId: {}", caseData.getCcdCaseReference());
            updatedData.respondent1DQ(
                    updatedData.build().getRespondent1DQ().toBuilder()
                            .respondent1DQWitnesses(caseData.getRespondent1DQWitnessesSmallClaim())
                            .build());
        }
    }
}
