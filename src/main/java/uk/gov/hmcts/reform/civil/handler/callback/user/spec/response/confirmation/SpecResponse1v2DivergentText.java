package uk.gov.hmcts.reform.civil.handler.callback.user.spec.response.confirmation;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.RespondToClaimConfirmationTextSpecGenerator;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.Optional;

import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_ONE_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;

@Component
public class SpecResponse1v2DivergentText implements RespondToClaimConfirmationTextSpecGenerator {

    @Override
    public Optional<String> generateTextFor(CaseData caseData) {
        if (!isDivergentResponse1v2SameSolicitor(caseData)) {
            return Optional.empty();
        }
        String body = "<br>The defendants have chosen different responses and the claim cannot continue online."
            + "<br>Use form N9A to admit, or form N9B to counterclaim. "
            + "Do not create a new claim to counterclaim."
            + String.format(
                "%n%n<a href=\"%s\" target=\"_blank\">Download form N9A (opens in a new tab)</a>",
                "https://www.gov.uk/respond-money-claim"
            )
            + String.format(
                "<br><a href=\"%s\" target=\"_blank\">Download form N9B (opens in a new tab)</a>",
                "https://www.gov.uk/respond-money-claim"
            )
            + "<br><br>Post the completed form to:"
            + "<br><br>County Court Business Centre<br>St. Katherine's House"
            + "<br>21-27 St.Katherine Street<br>Northampton<br>NN1 2LH";
        return Optional.of(body);
    }

    private boolean isDivergentResponse1v2SameSolicitor(CaseData caseData) {
        if (NO.equals(caseData.getRespondentResponseIsSame())
            && ONE_V_TWO_ONE_LEGAL_REP.equals(getMultiPartyScenario(caseData))) {
            RespondentResponseTypeSpec respondent1Response = caseData.getRespondent1ClaimResponseTypeForSpec();
            RespondentResponseTypeSpec respondent2Response = caseData.getRespondent2ClaimResponseTypeForSpec();
            if ((respondent1Response.equals(RespondentResponseTypeSpec.FULL_DEFENCE)
                && (respondent2Response.equals(RespondentResponseTypeSpec.FULL_ADMISSION)
                || respondent2Response.equals(RespondentResponseTypeSpec.PART_ADMISSION)
                || respondent2Response.equals(RespondentResponseTypeSpec.COUNTER_CLAIM)))
                ||
                (respondent1Response.equals(RespondentResponseTypeSpec.FULL_ADMISSION)
                    && (respondent2Response.equals(RespondentResponseTypeSpec.FULL_DEFENCE)
                    || respondent2Response.equals(RespondentResponseTypeSpec.PART_ADMISSION)
                    || respondent2Response.equals(RespondentResponseTypeSpec.COUNTER_CLAIM)))
                ||
                (respondent1Response.equals(RespondentResponseTypeSpec.COUNTER_CLAIM)
                    && (respondent2Response.equals(RespondentResponseTypeSpec.FULL_DEFENCE)
                    || respondent2Response.equals(RespondentResponseTypeSpec.PART_ADMISSION)
                    || respondent2Response.equals(RespondentResponseTypeSpec.FULL_ADMISSION)))
                ||
                (respondent1Response.equals(RespondentResponseTypeSpec.PART_ADMISSION)
                    && (respondent2Response.equals(RespondentResponseTypeSpec.FULL_DEFENCE)
                    || respondent2Response.equals(RespondentResponseTypeSpec.COUNTER_CLAIM))
                    || respondent2Response.equals(RespondentResponseTypeSpec.FULL_ADMISSION))) {
                return true;
            }
        }
        return false;
    }
}
