package uk.gov.hmcts.reform.civil.service.robotics.support;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
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
            resolveResponseLabel(caseData, respondent, isRespondent1),
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
            if (YesOrNo.YES.equals(caseData.getRespondentResponseIsSame())) {
                return caseData.getRespondent2ResponseDate() != null
                    ? caseData.getRespondent2ResponseDate()
                    : caseData.getRespondent1ResponseDate();
            }
            return caseData.getRespondent1ResponseDate();
        }
        return caseData.getRespondent2ResponseDate() != null
            ? caseData.getRespondent2ResponseDate()
            : caseData.getRespondent1ResponseDate();
    }

    private String resolveResponseLabel(CaseData caseData, Party respondent, boolean isRespondent1) {
        if (caseData == null || respondent == null) {
            return null;
        }
        RespondentResponseType responseType = getResponseTypeForRespondent(caseData, respondent);
        if (responseType != null) {
            return responseType.name();
        }
        if (!SPEC_CLAIM.equals(caseData.getCaseAccessCategory())) {
            return null;
        }
        RespondentResponseTypeSpec specResponse;
        if (isRespondent1) {
            specResponse = resolveSpecResponseType(caseData);
            if (specResponse == null) {
                specResponse = Optional.ofNullable(caseData.getClaimant1ClaimResponseTypeForSpec())
                    .orElse(caseData.getClaimant2ClaimResponseTypeForSpec());
            }
        } else {
            specResponse = caseData.getRespondent2ClaimResponseTypeForSpec();
        }
        return specResponse != null ? specResponse.name() : null;
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
        if (caseData == null || respondent == null) {
            return;
        }
        boolean isSpecClaim = SPEC_CLAIM.equals(caseData.getCaseAccessCategory());
        if (isSpecClaim && !hasCounterClaimResponse(caseData)) {
            return;
        }
        if (!isSpecClaim && AllocatedTrack.MULTI_CLAIM.equals(caseData.getAllocatedTrack())) {
            return;
        }
        if (!isSpecClaim && isUnspecFullDefence(caseData, isRespondent1)) {
            return;
        }
        if (shouldSkipRespondentMisc(caseData, isRespondent1)) {
            return;
        }
        String message = prepareRespondentResponseText(caseData, respondent, isRespondent1);
        if (!StringUtils.hasText(message)) {
            return;
        }
        RoboticsEventSupport.addRespondentMiscEvent(builder, sequenceGenerator, message, dateReceived);
    }

    private boolean shouldSkipRespondentMisc(CaseData caseData, boolean isRespondent1) {
        if (caseData == null) {
            return false;
        }
        if (hasCounterClaimResponse(caseData)) {
            return false;
        }
        if (SPEC_CLAIM.equals(caseData.getCaseAccessCategory())
            && !getMultiPartyScenario(caseData).equals(MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP)) {
            return true;
        }
        if (isTakenOffline(caseData)
            && !hasPartAdmissionResponse(caseData)
            && !hasFullAdmissionResponse(caseData)
            && !hasCounterClaimResponse(caseData)) {
            return true;
        }
        if (AllocatedTrack.MULTI_CLAIM.equals(caseData.getAllocatedTrack())) {
            return true;
        }
        if (getMultiPartyScenario(caseData).equals(ONE_V_TWO_ONE_LEGAL_REP)
            && YesOrNo.YES.equals(caseData.getRespondentResponseIsSame())
            && respondentsHaveFullDefenceResponse(caseData)) {
            return true;
        }
        return hasClaimantOptedOut(caseData, isRespondent1);
    }

    public boolean hasClaimantOptedOut(CaseData caseData, boolean isRespondent1) {
        if (caseData == null) {
            return false;
        }
        if (YesOrNo.NO.equals(caseData.getApplicant1ProceedWithClaim())) {
            return true;
        }
        if (YesOrNo.NO.equals(caseData.getApplicant1ProceedWithClaimMultiParty2v1())) {
            return true;
        }
        if (YesOrNo.NO.equals(caseData.getApplicant1ProceedWithClaimSpec2v1())) {
            return true;
        }
        YesOrNo proceedVsDef1 = caseData.getApplicant1ProceedWithClaimAgainstRespondent1MultiParty1v2();
        if (isRespondent1 && YesOrNo.NO.equals(proceedVsDef1)) {
            return true;
        }
        YesOrNo proceedVsDef2 = caseData.getApplicant1ProceedWithClaimAgainstRespondent2MultiParty1v2();
        return !isRespondent1 && YesOrNo.NO.equals(proceedVsDef2);
    }

    public void addSpecDivergentRespondentMiscEvent(EventHistory.EventHistoryBuilder builder,
                                                    RoboticsSequenceGenerator sequenceGenerator,
                                                    CaseData caseData,
                                                    Party respondent,
                                                    boolean isRespondent1,
                                                    LocalDateTime dateReceived) {
        if (caseData == null || respondent == null) {
            return;
        }
        String message = prepareRespondentResponseText(caseData, respondent, isRespondent1);
        if (!StringUtils.hasText(message)) {
            return;
        }
        RoboticsEventSupport.addRespondentMiscEvent(builder, sequenceGenerator, message, dateReceived);
    }

    private boolean hasCounterClaimResponse(CaseData caseData) {
        if (caseData == null) {
            return false;
        }
        if (SPEC_CLAIM.equals(caseData.getCaseAccessCategory())) {
            return RespondentResponseTypeSpec.COUNTER_CLAIM.equals(caseData.getRespondent1ClaimResponseTypeForSpec())
                || RespondentResponseTypeSpec.COUNTER_CLAIM.equals(caseData.getRespondent2ClaimResponseTypeForSpec())
                || RespondentResponseTypeSpec.COUNTER_CLAIM.equals(caseData.getClaimant1ClaimResponseTypeForSpec())
                || RespondentResponseTypeSpec.COUNTER_CLAIM.equals(caseData.getClaimant2ClaimResponseTypeForSpec());
        }
        return RespondentResponseType.COUNTER_CLAIM.equals(caseData.getRespondent1ClaimResponseType())
            || RespondentResponseType.COUNTER_CLAIM.equals(caseData.getRespondent2ClaimResponseType());
    }

    private boolean hasPartAdmissionResponse(CaseData caseData) {
        if (caseData == null) {
            return false;
        }
        if (SPEC_CLAIM.equals(caseData.getCaseAccessCategory())) {
            return RespondentResponseTypeSpec.PART_ADMISSION.equals(caseData.getRespondent1ClaimResponseTypeForSpec())
                || RespondentResponseTypeSpec.PART_ADMISSION.equals(caseData.getRespondent2ClaimResponseTypeForSpec())
                || RespondentResponseTypeSpec.PART_ADMISSION.equals(caseData.getClaimant1ClaimResponseTypeForSpec())
                || RespondentResponseTypeSpec.PART_ADMISSION.equals(caseData.getClaimant2ClaimResponseTypeForSpec());
        }
        return RespondentResponseType.PART_ADMISSION.equals(caseData.getRespondent1ClaimResponseType())
            || RespondentResponseType.PART_ADMISSION.equals(caseData.getRespondent2ClaimResponseType());
    }

    private boolean hasFullAdmissionResponse(CaseData caseData) {
        if (caseData == null) {
            return false;
        }
        if (SPEC_CLAIM.equals(caseData.getCaseAccessCategory())) {
            return RespondentResponseTypeSpec.FULL_ADMISSION.equals(caseData.getRespondent1ClaimResponseTypeForSpec())
                || RespondentResponseTypeSpec.FULL_ADMISSION.equals(caseData.getRespondent2ClaimResponseTypeForSpec())
                || RespondentResponseTypeSpec.FULL_ADMISSION.equals(caseData.getClaimant1ClaimResponseTypeForSpec())
                || RespondentResponseTypeSpec.FULL_ADMISSION.equals(caseData.getClaimant2ClaimResponseTypeForSpec());
        }
        return RespondentResponseType.FULL_ADMISSION.equals(caseData.getRespondent1ClaimResponseType())
            || RespondentResponseType.FULL_ADMISSION.equals(caseData.getRespondent2ClaimResponseType());
    }

    private boolean respondentsHaveFullDefenceResponse(CaseData caseData) {
        if (caseData == null) {
            return false;
        }
        if (SPEC_CLAIM.equals(caseData.getCaseAccessCategory())) {
            return RespondentResponseTypeSpec.FULL_DEFENCE.equals(caseData.getRespondent1ClaimResponseTypeForSpec())
                && (YesOrNo.YES.equals(caseData.getRespondentResponseIsSame())
                || RespondentResponseTypeSpec.FULL_DEFENCE.equals(caseData.getRespondent2ClaimResponseTypeForSpec()));
        }
        return RespondentResponseType.FULL_DEFENCE.equals(caseData.getRespondent1ClaimResponseType())
            && (YesOrNo.YES.equals(caseData.getRespondentResponseIsSame())
            || RespondentResponseType.FULL_DEFENCE.equals(caseData.getRespondent2ClaimResponseType()));
    }

    private boolean isUnspecFullDefence(CaseData caseData, boolean isRespondent1) {
        if (caseData == null) {
            return false;
        }
        RespondentResponseType responseType = isRespondent1
            ? caseData.getRespondent1ClaimResponseType()
            : caseData.getRespondent2ClaimResponseType();
        return RespondentResponseType.FULL_DEFENCE.equals(responseType);
    }

    private boolean isTakenOffline(CaseData caseData) {
        return caseData != null
            && (caseData.getTakenOfflineByStaffDate() != null || caseData.getTakenOfflineDate() != null);
    }

}
