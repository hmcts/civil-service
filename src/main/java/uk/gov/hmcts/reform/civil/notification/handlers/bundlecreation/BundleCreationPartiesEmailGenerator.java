package uk.gov.hmcts.reform.civil.notification.handlers.bundlecreation;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.EmailDTO;
import uk.gov.hmcts.reform.civil.notification.handlers.PartiesEmailGenerator;

import java.util.HashSet;
import java.util.Set;

import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.isOneVTwoTwoLegalRep;

@Slf4j
@AllArgsConstructor
@Component
public class BundleCreationPartiesEmailGenerator implements PartiesEmailGenerator {

    private final BundleCreationAppSolOneEmailDTOGenerator appSolOneEmailDTOGenerator;
    private final BundleCreationRespSolOneEmailDTOGenerator respSolOneEmailDTOGenerator;
    private final BundleCreationRespSolTwoEmailDTOGenerator respSolTwoEmailDTOGenerator;
    private final BundleCreationClaimantEmailDTOGenerator claimantEmailDTOGenerator;
    private final BundleCreationDefendantEmailDTOGenerator defendantEmailDTOGenerator;

    @Override
    public Set<EmailDTO> getPartiesToNotify(CaseData caseData) {
        Set<EmailDTO> partiesToEmail = new HashSet<>();
        log.info("Generating email for case ID: {}", caseData.getCcdCaseReference());
        partiesToEmail.add(getApplicant(caseData));
        partiesToEmail.addAll(getRespondents(caseData));

        return partiesToEmail;
    }

    private EmailDTO getApplicant(CaseData caseData) {
        if (caseData.isApplicantLiP()) {
            return claimantEmailDTOGenerator.buildEmailDTO(caseData);
        }

        return appSolOneEmailDTOGenerator.buildEmailDTO(caseData);
    }

    private Set<EmailDTO> getRespondents(CaseData caseData) {
        Set<EmailDTO> recipients = new HashSet<>();

        if (caseData.isRespondent1NotRepresented()) {
            recipients.add(defendantEmailDTOGenerator.buildEmailDTO(caseData));
        } else {
            recipients.add(respSolOneEmailDTOGenerator.buildEmailDTO(caseData));
        }

        if (isOneVTwoTwoLegalRep(caseData)) {
            recipients.add(respSolTwoEmailDTOGenerator.buildEmailDTO(caseData));
        }

        return recipients;
    }
}
