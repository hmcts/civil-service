package uk.gov.hmcts.reform.civil.notification.handlers.changeofrepresentation;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.EmailDTO;
import uk.gov.hmcts.reform.civil.notification.handlers.EmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notification.handlers.PartiesEmailGenerator;

import java.util.HashSet;
import java.util.Set;

@Component
@AllArgsConstructor
@Slf4j
public class AllNoCPartiesEmailGenerator implements PartiesEmailGenerator {

    //In case of LR v LR/LR
    private final NoCFormerSolicitorEmailDTOGenerator formerSolicitorEmailDTOGenerator;
    private final NoCOtherSolicitorOneEmailDTOGenerator otherSolicitorOneEmailDTOGenerator;
    private final NoCOtherSolicitorTwoEmailDTOGenerator otherSolicitorTwoEmailDTOGenerator;
    private final NoCHearingFeeUnpaidAppSolEmailDTOGenerator noCHearingFeeUnpaidAppSolEmailDTOGenerator;

    //In case of Lip v LR
    private final NoCClaimantLipEmailDTOGenerator nocClaimanLipEmailDTOGenerator;
    private final NoCLipVLRNewDefendantEmailDTOGenerator noCLipVLRNewDefendantEmailDTOGenerator;

    @Override
    public Set<EmailDTO> getPartiesToNotify(final CaseData caseData) {
        Set<EmailDTO> partiesToEmail = new HashSet<>();
        addIfPartyNeedsNotification(caseData, formerSolicitorEmailDTOGenerator, partiesToEmail);
        addIfPartyNeedsNotification(caseData, otherSolicitorOneEmailDTOGenerator, partiesToEmail);
        addIfPartyNeedsNotification(caseData, otherSolicitorTwoEmailDTOGenerator, partiesToEmail);
        addIfPartyNeedsNotification(caseData, noCHearingFeeUnpaidAppSolEmailDTOGenerator, partiesToEmail);
        addIfPartyNeedsNotification(caseData, nocClaimanLipEmailDTOGenerator, partiesToEmail);
        addIfPartyNeedsNotification(caseData, noCLipVLRNewDefendantEmailDTOGenerator, partiesToEmail);
        return partiesToEmail;
    }

    private void addIfPartyNeedsNotification(CaseData caseData,
                                             EmailDTOGenerator generator,
                                             Set<EmailDTO> partiesToEmail) {
        if ((generator != null) && generator.getShouldNotify(caseData)) {
            log.info("Generating email for party [{}] for case ID: {}",
                     generator.getClass().getSimpleName(), caseData.getCcdCaseReference());
            partiesToEmail.add(generator.buildEmailDTO(caseData));
        }
    }
}
