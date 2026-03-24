package uk.gov.hmcts.reform.civil.notification.handlers.setasidejudgementrequest;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;

import java.util.List;

@Component
public class SetAsideJudgementRequestAllPartiesEmailGenerator extends AllPartiesEmailGenerator {

    public SetAsideJudgementRequestAllPartiesEmailGenerator(
        SetAsideJudgementRequestAppSolOneEmailDTOGenerator applicantGenerator,
        SetAsideJudgementRequestClaimantEmailDTOGenerator claimantGenerator,
        SetAsideJudgementRequestDefendantEmailDTOGenerator defendantGenerator,
        SetAsideJudgementRequestRespSolOneEmailDTOGenerator respSolOneGenerator,
        SetAsideJudgementRequestRespSolTwoEmailDTOGenerator respSolTwoGenerator
    ) {
        super(List.of(
            applicantGenerator,
            claimantGenerator,
            defendantGenerator,
            respSolOneGenerator,
            respSolTwoGenerator
        ));
    }
}
