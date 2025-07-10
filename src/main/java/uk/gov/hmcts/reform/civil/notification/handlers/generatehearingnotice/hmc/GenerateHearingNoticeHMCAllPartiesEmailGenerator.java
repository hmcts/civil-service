package uk.gov.hmcts.reform.civil.notification.handlers.generatehearingnotice.hmc;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;

import java.util.List;

@Component
public class GenerateHearingNoticeHMCAllPartiesEmailGenerator extends AllPartiesEmailGenerator {

    public GenerateHearingNoticeHMCAllPartiesEmailGenerator(
            GenerateHearingNoticeHMCAppSolEmailDTOGenerator appSolGen,
            GenerateHearingNoticeHMCRespSolOneEmailDTOGenerator respSolOneGen,
            GenerateHearingNoticeHMCRespSolTwoEmailDTOGenerator respSolTwoGen,
            GenerateHearingNoticeHMCClaimantEmailDTOGenerator claimantEmailDTOGenerator,
            GenerateHearingNoticeHMCDefendantEmailDTOGenerator defendantEmailDTOGenerator
    ) {
        super(List.of(appSolGen, respSolOneGen, respSolTwoGen, claimantEmailDTOGenerator, defendantEmailDTOGenerator));
    }
}
