package uk.gov.hmcts.reform.civil.notification.handlers.trialready.respondentonenotifyotherstrialready;

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
import uk.gov.hmcts.reform.civil.service.flowstate.IStateFlowEngine;

import java.util.HashSet;
import java.util.Set;

@Component
@Slf4j
public class RespondentOneNotifyOthersTrialReadyPartiesEmailGenerator extends TrialReadyPartiesEmailGenerator {

    public RespondentOneNotifyOthersTrialReadyPartiesEmailGenerator(TrialReadyAppSolOneEmailDTOGenerator appSolOneEmailDTOGenerator,
                                                                    TrialReadyClaimantEmailDTOGenerator claimantEmailDTOGenerator,
                                                                    TrialReadyRespSolOneEmailDTOGenerator respSolOneEmailDTOGenerator,
                                                                    TrialReadyRespSolTwoEmailDTOGenerator respSolTwoEmailDTOGenerator,
                                                                    TrialReadyDefendantEmailDTOGenerator defendantEmailDTOGenerator,
                                                                    TrialReadyDefendantTwoEmailDTOGenerator defendantTwoEmailDTOGenerator,
                                                                    IStateFlowEngine stateFlowEngine
    ) {
        super(
            appSolOneEmailDTOGenerator,
            claimantEmailDTOGenerator,
            respSolOneEmailDTOGenerator,
            respSolTwoEmailDTOGenerator,
            defendantEmailDTOGenerator,
            defendantTwoEmailDTOGenerator,
            stateFlowEngine
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

        EmailDTO respondentTwoEmailDTO = getRespondentTwo(caseData);
        if (respondentTwoEmailDTO != null) {
            partiesToEmail.add(respondentTwoEmailDTO);
        }

        return partiesToEmail;
    }
}
