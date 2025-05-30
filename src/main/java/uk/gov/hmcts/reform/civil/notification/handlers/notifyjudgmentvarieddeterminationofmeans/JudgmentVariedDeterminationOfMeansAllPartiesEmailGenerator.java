package uk.gov.hmcts.reform.civil.notification.handlers.notifyjudgmentvarieddeterminationofmeans;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;

import java.util.List;

@Component
public class JudgmentVariedDeterminationOfMeansAllPartiesEmailGenerator extends AllPartiesEmailGenerator {

    public JudgmentVariedDeterminationOfMeansAllPartiesEmailGenerator(
            JudgmentVariedDeterminationOfMeansClaimantEmailDTOGenerator claimantGen,
            JudgmentVariedDeterminationOfMeansRespSolOneEmailDTOGenerator sol1Gen,
            JudgmentVariedDeterminationOfMeansRespSolTwoEmailDTOGenerator sol2Gen,
            JudgmentVariedDeterminationOfMeansLipDefendantEmailDTOGenerator lipGen
    ) {
        super(List.of(claimantGen, sol1Gen, sol2Gen, lipGen));
    }
}
