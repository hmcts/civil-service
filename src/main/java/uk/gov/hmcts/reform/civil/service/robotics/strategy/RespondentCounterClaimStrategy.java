package uk.gov.hmcts.reform.civil.service.robotics.strategy;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseType;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsRespondentResponseSupport;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsSequenceGenerator;

import java.time.LocalDateTime;

import static uk.gov.hmcts.reform.civil.utils.PredicateUtils.defendant1ResponseExists;
import static uk.gov.hmcts.reform.civil.utils.PredicateUtils.defendant1v2SameSolicitorSameResponse;
import static uk.gov.hmcts.reform.civil.utils.PredicateUtils.defendant2ResponseExists;

@Component
@RequiredArgsConstructor
public class RespondentCounterClaimStrategy implements EventHistoryStrategy {

    private final RoboticsSequenceGenerator sequenceGenerator;
    private final RoboticsRespondentResponseSupport respondentResponseSupport;

    @Override
    public boolean supports(CaseData caseData) {
        return hasCounterClaimResponse(caseData)
                && (defendant1ResponseExists.test(caseData) || defendant2ResponseExists.test(caseData));
    }

    @Override
    public void contribute(EventHistory.EventHistoryBuilder builder, CaseData caseData, String authToken) {
        if (!supports(caseData)) {
            return;
        }

        if (defendant1ResponseExists.test(caseData)) {
            respondentResponseSupport.addRespondentMiscEvent(
                builder,
                sequenceGenerator,
                caseData,
                caseData.getRespondent1(),
                true,
                caseData.getRespondent1ResponseDate()
            );

            if (defendant1v2SameSolicitorSameResponse.test(caseData)) {
                LocalDateTime respondent2Date = respondentResponseSupport.resolveRespondent2ActualOrFallbackDate(caseData);
                respondentResponseSupport.addRespondentMiscEvent(
                    builder,
                    sequenceGenerator,
                    caseData,
                    caseData.getRespondent2(),
                    false,
                    respondent2Date
                );
                return;
            }
        }

        if (defendant2ResponseExists.test(caseData)) {
            respondentResponseSupport.addRespondentMiscEvent(
                builder,
                sequenceGenerator,
                caseData,
                caseData.getRespondent2(),
                false,
                caseData.getRespondent2ResponseDate()
            );
        }
    }

    private boolean hasCounterClaimResponse(CaseData caseData) {
        if (caseData == null) {
            return false;
        }
        if (CaseCategory.SPEC_CLAIM.equals(caseData.getCaseAccessCategory())) {
            return RespondentResponseTypeSpec.COUNTER_CLAIM.equals(caseData.getRespondent1ClaimResponseTypeForSpec())
                || RespondentResponseTypeSpec.COUNTER_CLAIM.equals(caseData.getRespondent2ClaimResponseTypeForSpec())
                || RespondentResponseTypeSpec.COUNTER_CLAIM.equals(caseData.getClaimant1ClaimResponseTypeForSpec())
                || RespondentResponseTypeSpec.COUNTER_CLAIM.equals(caseData.getClaimant2ClaimResponseTypeForSpec());
        }
        return RespondentResponseType.COUNTER_CLAIM.equals(caseData.getRespondent1ClaimResponseType())
            || RespondentResponseType.COUNTER_CLAIM.equals(caseData.getRespondent2ClaimResponseType());
    }
}
