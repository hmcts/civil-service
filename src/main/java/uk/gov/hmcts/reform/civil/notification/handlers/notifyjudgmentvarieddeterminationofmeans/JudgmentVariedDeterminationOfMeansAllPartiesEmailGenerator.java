package uk.gov.hmcts.reform.civil.notification.handlers.notifyjudgmentvarieddeterminationofmeans;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;

import java.util.List;

@Component
public class JudgmentVariedDeterminationOfMeansAllPartiesEmailGenerator extends AllPartiesEmailGenerator {

    public JudgmentVariedDeterminationOfMeansAllPartiesEmailGenerator(
            JudgmentVariedDeterminationOfMeansClaimantEmailDTOGenerator claimantGen,
            JudgmentVariedDeterminationOfMeansAppSolOneEmailDTOGenerator appSol1Gen,
            JudgmentVariedDeterminationOfMeansRespSolOneEmailDTOGenerator respSol1Gen,
            JudgmentVariedDeterminationOfMeansRespSolTwoEmailDTOGenerator respSGen,
            JudgmentVariedDeterminationOfMeansDefendantEmailDTOGenerator defendantGen
    ) {
        super(List.of(claimantGen, appSol1Gen, respSol1Gen, respSGen, defendantGen));
    }
}
