package uk.gov.hmcts.reform.civil.notification.handlers.hearingfeeunpaid;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;

import java.util.List;

@Component
public class HearingFeeUnpaidAllPartiesEmailGenerator extends AllPartiesEmailGenerator {

    public HearingFeeUnpaidAllPartiesEmailGenerator(
            HearingFeeUnpaidClaimantEmailDTOGenerator claimantGenerator,
            HearingFeeUnpaidAppSolEmailDTOGenerator appSolGenerator,
            HearingFeeUnpaidDefendantEmailDTOGenerator defendantGenerator,
            HearingFeeUnpaidRespSolOneEmailDTOGenerator respSolOneGenerator,
            HearingFeeUnpaidRespSolTwoEmailDTOGenerator respSolTwoGenerator
    ) {
        super(List.of(claimantGenerator, appSolGenerator, defendantGenerator, respSolOneGenerator, respSolTwoGenerator));
    }
}
