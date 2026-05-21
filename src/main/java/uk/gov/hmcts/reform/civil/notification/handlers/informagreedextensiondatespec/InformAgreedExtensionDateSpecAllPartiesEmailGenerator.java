package uk.gov.hmcts.reform.civil.notification.handlers.informagreedextensiondatespec;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;

import java.util.List;

@Component
public class InformAgreedExtensionDateSpecAllPartiesEmailGenerator extends AllPartiesEmailGenerator {

    public InformAgreedExtensionDateSpecAllPartiesEmailGenerator(
        InformAgreedExtensionDateSpecAppSolOneEmailDTOGenerator applicantGenerator,
        InformAgreedExtensionDateSpecClaimantEmailDTOGenerator claimantGenerator,
        InformAgreedExtensionDateSpecRespSolOneEmailDTOGenerator respondentOneGenerator,
        InformAgreedExtensionDateSpecRespSolTwoEmailDTOGenerator respondentTwoGenerator
    ) {
        super(List.of(
            applicantGenerator,
            claimantGenerator,
            respondentOneGenerator,
            respondentTwoGenerator
        ));
    }
}
