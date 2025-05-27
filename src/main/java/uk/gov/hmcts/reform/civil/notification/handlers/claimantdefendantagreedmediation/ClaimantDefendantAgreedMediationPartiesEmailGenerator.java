package uk.gov.hmcts.reform.civil.notification.handlers.claimantdefendantagreedmediation;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.AllLegalRepsEmailGenerator;
import uk.gov.hmcts.reform.civil.notification.handlers.EmailDTO;

import java.util.HashSet;
import java.util.Set;

import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.isOneVTwoTwoLegalRep;

@Component
public class ClaimantDefendantAgreedMediationPartiesEmailGenerator extends AllLegalRepsEmailGenerator {

    private final ClaimantDefendantAgreedMediationDefendantEmailDTOGenerator claimantDefendantAgreedMediationDefendantEmailDTOGenerator;

    public ClaimantDefendantAgreedMediationPartiesEmailGenerator(
        ClaimantDefendantAgreedMediationAppSolOneEmailDTOGenerator appSolOneEmailGenerator,
        ClaimantDefendantAgreedMediationRespSolOneEmailDTOGenerator respSolOneEmailGenerator,
        ClaimantDefendantAgreedMediationRespSolTwoEmailDTOGenerator respSolTwoEmailGenerator,
        ClaimantDefendantAgreedMediationDefendantEmailDTOGenerator claimantDefendantAgreedMediationDefendantEmailDTOGenerator
    ) {
        super(appSolOneEmailGenerator, respSolOneEmailGenerator, respSolTwoEmailGenerator);
        this.claimantDefendantAgreedMediationDefendantEmailDTOGenerator = claimantDefendantAgreedMediationDefendantEmailDTOGenerator;
    }

    @Override
    protected Set<EmailDTO> getRespondents(CaseData caseData, String taskId) {
        Set<EmailDTO> recipients = new HashSet<>();
        if (caseData.isRespondent1NotRepresented()) {
            recipients.add(claimantDefendantAgreedMediationDefendantEmailDTOGenerator.buildEmailDTO(caseData, taskId));
        } else {
            recipients.add(respSolOneEmailGenerator.buildEmailDTO(caseData, taskId));
            if (isOneVTwoTwoLegalRep(caseData)) {
                recipients.add(respSolTwoEmailGenerator.buildEmailDTO(caseData, taskId));
            }
        }

        return recipients;
    }
}
