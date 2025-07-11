package uk.gov.hmcts.reform.civil.notification.handlers.generatehearingnotice.nonhmc;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;

import java.util.List;

@Component
public class GenerateHearingNoticeAllPartiesEmailGenerator extends AllPartiesEmailGenerator {

    public GenerateHearingNoticeAllPartiesEmailGenerator(
            GenerateHearingNoticeClaimantEmailDTOGenerator lipGen,
            GenerateHearingNoticeAppSolOneEmailDTOGenerator appSolGen,
            GenerateHearingNoticeDefendantEmailDTOGenerator defendantGen,
            GenerateHearingNoticeRespSolOneEmailDTOGenerator respSolOneGen,
            GenerateHearingNoticeRespSolOneEmailDTOGenerator respSolTwoGen

    ) {
        super(List.of(lipGen, appSolGen, defendantGen, respSolOneGen, respSolTwoGen));
    }
}
