package uk.gov.hmcts.reform.civil.notification.handlers.claimantresponseagreedsettledpartadmit;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;

import java.util.List;

@Component
public class ClaimantResponseAgreedSettledPartAdmitPartiesEmailGenerator extends AllPartiesEmailGenerator {

    public ClaimantResponseAgreedSettledPartAdmitPartiesEmailGenerator(ClaimantResponseAgreedSettledPartAdmitDefendantEmailDTOGenerator defendantEmailDTOGenerator,
                                                                       ClaimantResponseAgreedSettledPartAdmitRespSolOneEmailDTOGenerator respSolOneEmailDTOGenerator) {
        super(List.of(respSolOneEmailDTOGenerator, defendantEmailDTOGenerator));
    }
}
