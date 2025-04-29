package uk.gov.hmcts.reform.civil.notification.handlers.claimantresponsenotagreedrepayment;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.EmailDTO;
import uk.gov.hmcts.reform.civil.notification.handlers.PartiesEmailGenerator;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@AllArgsConstructor
@Component
public class ClaimantResponseNotAgreedRepaymentPartiesEmailGenerator implements PartiesEmailGenerator {

    private final ClaimantResponseNotAgreedRepaymentAppSolOneEmailDTOGenerator appSolOneEmailDTOGenerator;
    private final ClaimantResponseNotAgreedRepaymentClaimantEmailDTOGenerator claimantEmailDTOGenerator;
    private final ClaimantResponseNotAgreedRepaymentDefendantEmailDTOGenerator defendantEmailDTOGenerator;
    private final ClaimantResponseNotAgreedRepaymentRespSolOneEmailDTOGenerator respSolOneEmailDTOGenerator;

    @Override
    public Set<EmailDTO> getPartiesToNotify(CaseData caseData) {
        Set<EmailDTO> partiesToEmail = new HashSet<>();
        log.info("Generating email for case ID: {}", caseData.getCcdCaseReference());
        if (caseData.isApplicantLiP()) {
            partiesToEmail.add(claimantEmailDTOGenerator.buildEmailDTO(caseData));
        } else {
            partiesToEmail.add(appSolOneEmailDTOGenerator.buildEmailDTO(caseData));
        }

        if (caseData.isRespondent1NotRepresented()) {
            partiesToEmail.add(defendantEmailDTOGenerator.buildEmailDTO(caseData));
        } else {
            partiesToEmail.add(respSolOneEmailDTOGenerator.buildEmailDTO(caseData));
        }

        return partiesToEmail;
    }
}
