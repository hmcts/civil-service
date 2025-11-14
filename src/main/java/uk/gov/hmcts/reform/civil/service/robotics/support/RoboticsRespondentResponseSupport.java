package uk.gov.hmcts.reform.civil.service.robotics.support;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseType;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.dq.DQ;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;

import java.time.LocalDateTime;
import java.util.Optional;

import static java.lang.String.format;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_ONE_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.TWO_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsDirectionsQuestionnaireSupport.getPreferredCourtCode;
import static uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsDirectionsQuestionnaireSupport.isStayClaim;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getResponseTypeForRespondent;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getResponseTypeForRespondentSpec;

@Component
@RequiredArgsConstructor
public class RoboticsRespondentResponseSupport {

    private final RoboticsEventTextFormatter textFormatter;
    private final RoboticsTimelineHelper timelineHelper;

    public String prepareRespondentResponseText(CaseData caseData, Party respondent, boolean isRespondent1) {
        MultiPartyScenario scenario = getMultiPartyScenario(caseData);
        if (scenario.equals(ONE_V_ONE) || scenario.equals(TWO_V_ONE)) {
            return buildSingleOrTwoVOneResponseText(caseData);
        }

        String paginatedMessage = scenario.equals(ONE_V_TWO_ONE_LEGAL_REP)
            ? getPaginatedMessageFor1v2SameSolicitor(caseData, isRespondent1)
            : "";

        return textFormatter.formatRpa(
            "%sDefendant: %s has responded: %s",
            paginatedMessage,
            respondent.getPartyName(),
            SPEC_CLAIM.equals(caseData.getCaseAccessCategory())
                ? getResponseTypeForRespondentSpec(caseData, respondent)
                : getResponseTypeForRespondent(caseData, respondent)
        );
    }

    private String buildSingleOrTwoVOneResponseText(CaseData caseData) {
        if (SPEC_CLAIM.equals(caseData.getCaseAccessCategory())) {
            RespondentResponseTypeSpec responseType = resolveSpecResponseType(caseData);
            if (responseType != null) {
                return switch (responseType) {
                    case COUNTER_CLAIM -> textFormatter.defendantRejectsAndCounterClaims();
                    case FULL_ADMISSION -> textFormatter.defendantFullyAdmits();
                    case PART_ADMISSION -> textFormatter.defendantPartialAdmission();
                    case FULL_DEFENCE -> formatFullDefenceResponseText(caseData, true);
                    default -> "";
                };
            }
        }

        if (caseData.getRespondent1ClaimResponseType() == null) {
            return "";
        }

        return switch (caseData.getRespondent1ClaimResponseType()) {
            case COUNTER_CLAIM -> textFormatter.defendantRejectsAndCounterClaims();
            case FULL_ADMISSION -> textFormatter.defendantFullyAdmits();
            case PART_ADMISSION -> textFormatter.defendantPartialAdmission();
            case FULL_DEFENCE -> formatFullDefenceResponseText(caseData, false);
            default -> "";
        };
    }

    private String formatFullDefenceResponseText(CaseData caseData, boolean isSpec) {
        Party respondent = caseData.getRespondent1();
        String respondentName = respondent != null ? respondent.getPartyName() : "Defendant";
        String responseLabel = isSpec
            ? Optional.ofNullable(respondent)
                .map(value -> getResponseTypeForRespondentSpec(caseData, value))
                .map(Enum::name)
                .orElse(RespondentResponseTypeSpec.FULL_DEFENCE.name())
            : Optional.ofNullable(respondent)
                .map(value -> getResponseTypeForRespondent(caseData, value))
                .map(Enum::name)
                .orElse(RespondentResponseType.FULL_DEFENCE.name());

        return textFormatter.formatRpa(
            "Defendant: %s has responded: %s",
            respondentName,
            responseLabel
        );
    }

    private RespondentResponseTypeSpec resolveSpecResponseType(CaseData caseData) {
        MultiPartyScenario scenario = getMultiPartyScenario(caseData);
        if (!scenario.equals(TWO_V_ONE)) {
            return caseData.getRespondent1ClaimResponseTypeForSpec();
        }

        YesOrNo singleResponse = caseData.getDefendantSingleResponseToBothClaimants();
        if (YesOrNo.YES.equals(singleResponse)) {
            return caseData.getRespondent1ClaimResponseTypeForSpec();
        }
        return caseData.getClaimant1ClaimResponseTypeForSpec();
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

    public LocalDateTime resolveRespondent2ResponseDate(CaseData caseData) {
        if (caseData == null) {
            return null;
        }
        MultiPartyScenario scenario = getMultiPartyScenario(caseData);
        if (scenario.equals(ONE_V_TWO_ONE_LEGAL_REP)) {
            return caseData.getRespondent1ResponseDate();
        }
        return caseData.getRespondent2ResponseDate() != null
            ? caseData.getRespondent2ResponseDate()
            : caseData.getRespondent1ResponseDate();
    }

    public LocalDateTime resolveRespondent2ActualOrFallbackDate(CaseData caseData) {
        if (caseData == null) {
            return null;
        }
        return caseData.getRespondent2ResponseDate() != null
            ? caseData.getRespondent2ResponseDate()
            : caseData.getRespondent1ResponseDate();
    }

    public void addRespondentMiscEvent(EventHistory.EventHistoryBuilder builder,
                                       RoboticsSequenceGenerator sequenceGenerator,
                                       CaseData caseData,
                                       Party respondent,
                                       boolean isRespondent1,
                                       LocalDateTime dateReceived) {
        String message = prepareRespondentResponseText(caseData, respondent, isRespondent1);
        if (!StringUtils.hasText(message)) {
            return;
        }
        RoboticsEventSupport.addRespondentMiscEvent(builder, sequenceGenerator, message, dateReceived);
    }
}
