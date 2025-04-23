package uk.gov.hmcts.reform.civil.notification.handlers.courtofficerorder;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.EmailDTO;
import uk.gov.hmcts.reform.civil.notification.handlers.PartiesEmailGenerator;

import java.util.HashSet;
import java.util.Set;

import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.isOneVTwoTwoLegalRep;

@AllArgsConstructor
@Slf4j
public class CourtOfficerOrderAllPartiesEmailGenerator implements PartiesEmailGenerator {

    private final CourtOfficerOrderApplicantEmailDTOGenerator applicantEmailDTOGenerator;
    private final CourtOfficerOrderRespondentEmailDTOGenerator respondentEmailDTOGenerator;
    private final CourtOfficerOrderRespondent2EmailDTOGenerator respondent2EmailDTOGenerator;

    @Override
    public Set<EmailDTO> getPartiesToNotify(final CaseData caseData) {
        Set<EmailDTO> partiesToEmail = new HashSet<>();
        log.info("Generating email for case ID: {}", caseData.getCcdCaseReference());
        partiesToEmail.add(applicantEmailDTOGenerator.buildEmailDTO(caseData));
        partiesToEmail.add(respondentEmailDTOGenerator.buildEmailDTO(caseData));
        if (isOneVTwoTwoLegalRep(caseData)) {
            partiesToEmail.add(respondent2EmailDTOGenerator.buildEmailDTO(caseData));
        }
        return partiesToEmail;
    }
}
