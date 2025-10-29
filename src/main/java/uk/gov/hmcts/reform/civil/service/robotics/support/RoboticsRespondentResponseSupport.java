package uk.gov.hmcts.reform.civil.service.robotics.support;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.dq.DQ;

import java.time.LocalDateTime;

import static java.lang.String.format;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_ONE_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.TWO_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseType.COUNTER_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseType.FULL_ADMISSION;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseType.PART_ADMISSION;
import static uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsDirectionsQuestionnaireSupport.getPreferredCourtCode;
import static uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsDirectionsQuestionnaireSupport.isStayClaim;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getResponseTypeForRespondent;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getResponseTypeForRespondentSpec;

/**
 * Shared helpers for respondent-response narrative text to keep contributors and the mapper in sync.
 */
@Component
@RequiredArgsConstructor
public class RoboticsRespondentResponseSupport {

    private final RoboticsEventTextFormatter textFormatter;
    private final RoboticsTimelineHelper timelineHelper;

    public String prepareRespondentResponseText(CaseData caseData, Party respondent, boolean isRespondent1) {
        MultiPartyScenario scenario = getMultiPartyScenario(caseData);
        String defaultText = "";
        if (scenario.equals(ONE_V_ONE) || scenario.equals(TWO_V_ONE)) {
            if (SPEC_CLAIM.equals(caseData.getCaseAccessCategory())) {
                RespondentResponseTypeSpec responseTypeForSpec = scenario.equals(TWO_V_ONE)
                    ? (YesOrNo.YES.equals(caseData.getDefendantSingleResponseToBothClaimants())
                    ? caseData.getRespondent1ClaimResponseTypeForSpec()
                    : caseData.getClaimant1ClaimResponseTypeForSpec())
                    : caseData.getRespondent1ClaimResponseTypeForSpec();

                switch (responseTypeForSpec) {
                    case COUNTER_CLAIM:
                        defaultText = textFormatter.defendantRejectsAndCounterClaims();
                        break;
                    case FULL_ADMISSION:
                        defaultText = textFormatter.defendantFullyAdmits();
                        break;
                    case PART_ADMISSION:
                        defaultText = textFormatter.defendantPartialAdmission();
                        break;
                    default:
                        break;
                }
            } else if (caseData.getRespondent1ClaimResponseType() != null) {
                switch (caseData.getRespondent1ClaimResponseType()) {
                    case COUNTER_CLAIM:
                        defaultText = textFormatter.defendantRejectsAndCounterClaims();
                        break;
                    case FULL_ADMISSION:
                        defaultText = textFormatter.defendantFullyAdmits();
                        break;
                    case PART_ADMISSION:
                        defaultText = textFormatter.defendantPartialAdmission();
                        break;
                    default:
                        break;
                }
            }
        } else {
            String paginatedMessage = "";
            if (scenario.equals(ONE_V_TWO_ONE_LEGAL_REP)) {
                paginatedMessage = getPaginatedMessageFor1v2SameSolicitor(caseData, isRespondent1);
            }
            defaultText = textFormatter.formatRpa(
                "%sDefendant: %s has responded: %s",
                paginatedMessage,
                respondent.getPartyName(),
                SPEC_CLAIM.equals(caseData.getCaseAccessCategory())
                    ? getResponseTypeForRespondentSpec(caseData, respondent)
                    : getResponseTypeForRespondent(caseData, respondent)
            );
        }
        return defaultText;
    }

    public String prepareFullDefenceEventText(DQ dq, CaseData caseData, boolean isRespondent1, Party respondent) {
        MultiPartyScenario scenario = getMultiPartyScenario(caseData);
        String paginatedMessage = "";
        if (scenario.equals(ONE_V_TWO_ONE_LEGAL_REP)) {
            paginatedMessage = getPaginatedMessageFor1v2SameSolicitor(caseData, isRespondent1);
        }

        return format(
            "%sDefendant: %s has responded: %s; preferredCourtCode: %s; stayClaim: %s",
            paginatedMessage,
            respondent.getPartyName(),
            getResponseTypeForRespondent(caseData, respondent),
            getPreferredCourtCode(dq),
            isStayClaim(dq)
        );
    }

    public String getPaginatedMessageFor1v2SameSolicitor(CaseData caseData, boolean isRespondent1) {
        int index = 1;
        LocalDateTime respondent1ResponseDate = caseData.getRespondent1ResponseDate();
        LocalDateTime respondent2ResponseDate = caseData.getRespondent2ResponseDate();
        if (respondent1ResponseDate != null && respondent2ResponseDate != null) {
            index = isRespondent1 ? 1 : 2;
        }
        return format(
            "[%d of 2 - %s] ",
            index,
            timelineHelper.now().toLocalDate().toString()
        );
    }
}
