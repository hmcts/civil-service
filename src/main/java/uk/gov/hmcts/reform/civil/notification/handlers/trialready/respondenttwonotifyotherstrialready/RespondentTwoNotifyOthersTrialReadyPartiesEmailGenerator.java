package uk.gov.hmcts.reform.civil.notification.handlers.trialready.respondenttwonotifyotherstrialready;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.EmailDTO;
import uk.gov.hmcts.reform.civil.notification.handlers.trialready.TrialReadyAppSolOneEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notification.handlers.trialready.TrialReadyClaimantEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notification.handlers.trialready.TrialReadyDefendantEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notification.handlers.trialready.TrialReadyDefendantTwoEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notification.handlers.trialready.TrialReadyPartiesEmailGenerator;
import uk.gov.hmcts.reform.civil.notification.handlers.trialready.TrialReadyRespSolOneEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notification.handlers.trialready.TrialReadyRespSolTwoEmailDTOGenerator;

import java.util.HashSet;
import java.util.Set;

@Component
@Slf4j
public class RespondentTwoNotifyOthersTrialReadyPartiesEmailGenerator extends TrialReadyPartiesEmailGenerator {

    public RespondentTwoNotifyOthersTrialReadyPartiesEmailGenerator(TrialReadyAppSolOneEmailDTOGenerator appSolOneEmailDTOGenerator,
                                                                    TrialReadyClaimantEmailDTOGenerator claimantEmailDTOGenerator,
                                                                    TrialReadyRespSolOneEmailDTOGenerator respSolOneEmailDTOGenerator,
                                                                    TrialReadyRespSolTwoEmailDTOGenerator respSolTwoEmailDTOGenerator,
                                                                    TrialReadyDefendantEmailDTOGenerator defendantEmailDTOGenerator,
                                                                    TrialReadyDefendantTwoEmailDTOGenerator defendantTwoEmailDTOGenerator
    ) {
        super(
            appSolOneEmailDTOGenerator,
            claimantEmailDTOGenerator,
            respSolOneEmailDTOGenerator,
            respSolTwoEmailDTOGenerator,
            defendantEmailDTOGenerator,
            defendantTwoEmailDTOGenerator
        );
    }

    @Override
    public Set<EmailDTO> getPartiesToNotify(CaseData caseData) {
        Set<EmailDTO> partiesToEmail = new HashSet<>();
        log.info("Generating email for case ID: {}", caseData.getCcdCaseReference());

        EmailDTO applicantEmailDTO = getApplicant(caseData);
        if (applicantEmailDTO != null) {
            partiesToEmail.add(applicantEmailDTO);
        }

        EmailDTO respondentEmailDTO = getRespondentOne(caseData);
        if (respondentEmailDTO != null) {
            partiesToEmail.add(respondentEmailDTO);
        }

        return partiesToEmail;
    }
}
