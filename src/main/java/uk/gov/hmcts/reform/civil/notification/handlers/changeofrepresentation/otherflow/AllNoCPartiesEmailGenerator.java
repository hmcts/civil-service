package uk.gov.hmcts.reform.civil.notification.handlers.changeofrepresentation.otherflow;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;

import java.util.List;

@Component
public class AllNoCPartiesEmailGenerator extends AllPartiesEmailGenerator {

    public AllNoCPartiesEmailGenerator(
        //In case of LR v LR/LR
        NoCFormerSolicitorEmailDTOGenerator formerSolicitorEmailDTOGenerator,
        NoCOtherSolicitorOneEmailDTOGenerator otherSolicitorOneEmailDTOGenerator,
        NoCOtherSolicitorTwoEmailDTOGenerator otherSolicitorTwoEmailDTOGenerator,
        NoCHearingFeeUnpaidAppSolEmailDTOGenerator noCHearingFeeUnpaidAppSolEmailDTOGenerator,

        //In case of Lip v LR
        NoCClaimantLipEmailDTOGenerator nocClaimanLipEmailDTOGenerator,
        NoCLipVLRNewDefendantEmailDTOGenerator noCLipVLRNewDefendantEmailDTOGenerator
    ) {
        super(List.of(formerSolicitorEmailDTOGenerator, otherSolicitorOneEmailDTOGenerator, otherSolicitorTwoEmailDTOGenerator,
                      noCHearingFeeUnpaidAppSolEmailDTOGenerator, nocClaimanLipEmailDTOGenerator, noCLipVLRNewDefendantEmailDTOGenerator));
    }
}
