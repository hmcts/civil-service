package uk.gov.hmcts.reform.civil.notification.handlers.hearingprocess;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;

import java.util.List;

@Component
public class HearingProcessAllPartiesEmailGenerator extends AllPartiesEmailGenerator {

    public HearingProcessAllPartiesEmailGenerator(
        HearingProcessAppSolEmailDTOGenerator appSolEmailGenerator,
        HearingProcessRespSolOneEmailDTOGenerator respOneEmailGenerator,
        HearingProcessRespSolTwoEmailDTOGenerator respTwoEmailGenerator,
        HearingProcessClaimantEmailDTOGenerator claimantGenerator,
        HearingProcessDefendantEmailDTOGenerator defendantGenerator) {

        super(List.of(
            appSolEmailGenerator,
            respOneEmailGenerator,
            respTwoEmailGenerator,
            claimantGenerator,
            defendantGenerator));
    }
}
