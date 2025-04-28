package uk.gov.hmcts.reform.civil.notification.handlers.claimantresponseconfirmstoproceed;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.EmailDTO;
import uk.gov.hmcts.reform.civil.notification.handlers.PartiesEmailGenerator;
import uk.gov.hmcts.reform.civil.notification.handlers.claimantresponseconfirmstoproceed.multipartynottoproceed.MultiPartyNotToProceedAppSolOneEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notification.handlers.claimantresponseconfirmstoproceed.multipartynottoproceed.MultiPartyNotToProceedRespSolOneEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notification.handlers.claimantresponseconfirmstoproceed.multipartynottoproceed.MultiPartyNotToProceedRespSolTwoEmailDTOGenerator;

import java.util.HashSet;
import java.util.Set;

import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.isOneVTwoTwoLegalRep;

@Slf4j
@AllArgsConstructor
@Component
public class ClaimantResponseConfirmsToProceedPartiesEmailGenerator implements PartiesEmailGenerator {

    private final ClaimantResponseConfirmsToProceedAppSolOneEmailDTOGenerator appSolOneEmailDTOGenerator;
    private final ClaimantResponseConfirmsToProceedDefendantEmailDTOGenerator defendantEmailDTOGenerator;
    private final ClaimantResponseConfirmsToProceedRespSolOneEmailDTOGenerator respSolOneEmailDTOGenerator;
    private final ClaimantResponseConfirmsToProceedRespSolTwoEmailDTOGenerator respSolTwoEmailDTOGenerator;

    private final MultiPartyNotToProceedAppSolOneEmailDTOGenerator appSolOneEmailDTOGeneratorNTP;
    private final MultiPartyNotToProceedRespSolOneEmailDTOGenerator respSolOneEmailDTOGeneratorNTP;
    private final MultiPartyNotToProceedRespSolTwoEmailDTOGenerator respSolTwoEmailDTOGeneratorNTP;

    private final ClaimantResponseConfirmsToProceedEmailHelper claimantResponseConfirmsToProceedEmailHelper;

    @Override
    public Set<EmailDTO> getPartiesToNotify(CaseData caseData) {
        Set<EmailDTO> partiesToEmail = new HashSet<>();
        log.info("Generating email for case ID: {}", caseData.getCcdCaseReference());
        partiesToEmail.add(getApplicant(caseData));
        partiesToEmail.addAll(getRespondents(caseData));

        return partiesToEmail;
    }

    private EmailDTO getApplicant(CaseData caseData) {
        if (claimantResponseConfirmsToProceedEmailHelper.isMultiPartyNotProceed(caseData, false)) {
            return appSolOneEmailDTOGeneratorNTP.buildEmailDTO(caseData);
        }

        return appSolOneEmailDTOGenerator.buildEmailDTO(caseData);
    }

    private Set<EmailDTO> getRespondents(CaseData caseData) {
        Set<EmailDTO> recipients = new HashSet<>();
        if (claimantResponseConfirmsToProceedEmailHelper.isMultiPartyNotProceed(caseData, false)) {
            recipients.add(respSolOneEmailDTOGeneratorNTP.buildEmailDTO(caseData));
        } else if (isLRvLipToDefendant(caseData)) {
            recipients.add(defendantEmailDTOGenerator.buildEmailDTO(caseData));
        } else {
            recipients.add(respSolOneEmailDTOGenerator.buildEmailDTO(caseData));
        }

        if (isOneVTwoTwoLegalRep(caseData)) {
            if (claimantResponseConfirmsToProceedEmailHelper.isMultiPartyNotProceed(caseData, false)) {
                recipients.add(respSolTwoEmailDTOGeneratorNTP.buildEmailDTO(caseData));
            } else {
                recipients.add(respSolTwoEmailDTOGenerator.buildEmailDTO(caseData));
            }
        }

        return recipients;
    }

    private boolean isLRvLipToDefendant(CaseData caseData) {
        return caseData.getCaseAccessCategory().equals(SPEC_CLAIM)
            && (caseData.isLRvLipOneVOne()
            || caseData.isLipvLipOneVOne());
    }
}
