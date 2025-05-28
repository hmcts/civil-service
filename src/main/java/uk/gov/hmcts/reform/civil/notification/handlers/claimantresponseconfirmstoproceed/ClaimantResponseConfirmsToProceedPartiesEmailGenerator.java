package uk.gov.hmcts.reform.civil.notification.handlers.claimantresponseconfirmstoproceed;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.EmailDTO;
import uk.gov.hmcts.reform.civil.notification.handlers.PartiesEmailGenerator;
import uk.gov.hmcts.reform.civil.notification.handlers.claimantresponseconfirmstoproceed.multipartyclaimantoptsoutonerespandcasenotproceeded.MultiPartyNotToProceedAppSolOneEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notification.handlers.claimantresponseconfirmstoproceed.multipartyclaimantoptsoutonerespandcasenotproceeded.MultiPartyNotToProceedRespSolOneEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notification.handlers.claimantresponseconfirmstoproceed.multipartyclaimantoptsoutonerespandcasenotproceeded.MultiPartyNotToProceedRespSolTwoEmailDTOGenerator;

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
    public Set<EmailDTO> getPartiesToNotify(CaseData caseData, String taskId) {
        Set<EmailDTO> partiesToEmail = new HashSet<>();
        log.info("Generating email for case ID: {}", caseData.getCcdCaseReference());
        partiesToEmail.add(getApplicant(caseData, taskId));
        partiesToEmail.addAll(getRespondents(caseData, taskId));

        return partiesToEmail;
    }

    private EmailDTO getApplicant(CaseData caseData, String taskId) {
        if (claimantResponseConfirmsToProceedEmailHelper.isMultiPartyNotProceed(caseData, false)) {
            return appSolOneEmailDTOGeneratorNTP.buildEmailDTO(caseData, taskId);
        }

        return appSolOneEmailDTOGenerator.buildEmailDTO(caseData, taskId);
    }

    private Set<EmailDTO> getRespondents(CaseData caseData, String taskId) {
        Set<EmailDTO> recipients = new HashSet<>();
        if (claimantResponseConfirmsToProceedEmailHelper.isMultiPartyNotProceed(caseData, false)) {
            recipients.add(respSolOneEmailDTOGeneratorNTP.buildEmailDTO(caseData, taskId));
        } else if (isLRvLipToDefendant(caseData)) {
            recipients.add(defendantEmailDTOGenerator.buildEmailDTO(caseData, taskId));
        } else {
            recipients.add(respSolOneEmailDTOGenerator.buildEmailDTO(caseData, taskId));
        }

        if (isOneVTwoTwoLegalRep(caseData)) {
            if (claimantResponseConfirmsToProceedEmailHelper.isMultiPartyNotProceed(caseData, true)) {
                recipients.add(respSolTwoEmailDTOGeneratorNTP.buildEmailDTO(caseData, taskId));
            } else {
                recipients.add(respSolTwoEmailDTOGenerator.buildEmailDTO(caseData, taskId));
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
