package uk.gov.hmcts.reform.civil.notification.handlers.caseproceedsincaseman;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.EmailDTO;
import uk.gov.hmcts.reform.civil.notification.handlers.PartiesEmailGenerator;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.util.HashSet;
import java.util.Set;

import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.isOneVTwoTwoLegalRep;

@Slf4j
@AllArgsConstructor
@Component
public class CaseProceedsInCasemanPartiesEmailGenerator implements PartiesEmailGenerator {

    private final CaseProceedsInCasemanAppSolOneEmailDTOGenerator appSolOneEmailDTOGenerator;
    private final CaseProceedsInCasemanClaimantEmailDTOGenerator claimantEmailDTOGenerator;
    private final CaseProceedsInCasemanRespSolOneEmailDTOGenerator respSolOneEmailDTOGenerator;
    private final CaseProceedsInCasemanRespSolTwoEmailDTOGenerator respSolTwoEmailDTOGenerator;
    private final FeatureToggleService featureToggleService;

    @Override
    public Set<EmailDTO> getPartiesToNotify(CaseData caseData) {
        Set<EmailDTO> partiesToEmail = new HashSet<>();
        log.info("Generating email for case ID: {}", caseData.getCcdCaseReference());
        partiesToEmail.add(getApplicant(caseData));
        partiesToEmail.addAll(getRespondents(caseData));

        return partiesToEmail;
    }

    private Set<EmailDTO> getRespondents(CaseData caseData) {
        Set<EmailDTO> recipients = new HashSet<>();
        recipients.add(respSolOneEmailDTOGenerator.buildEmailDTO(caseData));

        if (isOneVTwoTwoLegalRep(caseData)) {
            recipients.add(respSolTwoEmailDTOGenerator.buildEmailDTO(caseData));
        }

        return recipients;
    }

    private EmailDTO getApplicant(CaseData caseData) {
        if (!caseData.isLipvLipOneVOne()) {
            return appSolOneEmailDTOGenerator.buildEmailDTO(caseData);
        } else if (!featureToggleService.isLipVLipEnabled()) {
            return claimantEmailDTOGenerator.buildEmailDTO(caseData);
        }

        return null;
    }
}
