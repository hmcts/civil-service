package uk.gov.hmcts.reform.civil.service.flowstate.legacy.predicate;

import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.Objects;
import java.util.function.Predicate;

import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseType.FULL_ADMISSION;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseType.FULL_DEFENCE;

/**
 * Cohesive predicates related to responses progress and milestones during the flow state.
 * Logic copied as-is from FlowPredicate to avoid functional changes.
 */
public final class ResponsesProgressPredicates {

    private ResponsesProgressPredicates() {
        // Utility class
    }

    public static final Predicate<CaseData> notificationAcknowledged = ResponsesProgressPredicates::getPredicateForNotificationAcknowledged;

    private static boolean getPredicateForNotificationAcknowledged(CaseData caseData) {
        MultiPartyScenario scenario = getMultiPartyScenario(caseData);
        boolean respondent1Acknowledged = caseData.getRespondent1AcknowledgeNotificationDate() != null;
        boolean respondent2Acknowledged = caseData.getRespondent2AcknowledgeNotificationDate() != null;

        return switch (scenario) {
            case ONE_V_TWO_TWO_LEGAL_REP, ONE_V_TWO_ONE_LEGAL_REP -> respondent1Acknowledged || respondent2Acknowledged;
            default -> respondent1Acknowledged;
        };
    }

    public static final Predicate<CaseData> respondentTimeExtension = ResponsesProgressPredicates::getPredicateForTimeExtension;

    private static boolean getPredicateForTimeExtension(CaseData caseData) {
        MultiPartyScenario scenario = getMultiPartyScenario(caseData);
        boolean respondent1TimeExtension = caseData.getRespondent1TimeExtensionDate() != null;
        boolean respondent2TimeExtension = caseData.getRespondent2TimeExtensionDate() != null;

        return scenario == ONE_V_TWO_TWO_LEGAL_REP
            ? respondent1TimeExtension || respondent2TimeExtension
            : respondent1TimeExtension;
    }

    public static final Predicate<CaseData> allResponsesReceived = ResponsesProgressPredicates::getPredicateForResponses;

    private static boolean getPredicateForResponses(CaseData caseData) {
        MultiPartyScenario scenario = Objects.requireNonNull(getMultiPartyScenario(caseData));
        boolean respondent1ResponseReceived = caseData.getRespondent1ResponseDate() != null;

        if (scenario == MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP) {
            return respondent1ResponseReceived && caseData.getRespondent2ResponseDate() != null;
        }
        return respondent1ResponseReceived;
    }

    public static final Predicate<CaseData> awaitingResponsesFullDefenceReceived = ResponsesProgressPredicates::getPredicateForAwaitingResponsesFullDefenceReceived;

    private static boolean getPredicateForAwaitingResponsesFullDefenceReceived(CaseData caseData) {
        MultiPartyScenario scenario = Objects.requireNonNull(getMultiPartyScenario(caseData));

        if (scenario == MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP) {
            boolean respondent1FullDefence = caseData.getRespondent1ClaimResponseType() != null
                && caseData.getRespondent2ClaimResponseType() == null
                && FULL_DEFENCE.equals(caseData.getRespondent1ClaimResponseType());
            boolean respondent2FullDefence = caseData.getRespondent1ClaimResponseType() == null
                && caseData.getRespondent2ClaimResponseType() != null
                && FULL_DEFENCE.equals(caseData.getRespondent2ClaimResponseType());
            return respondent1FullDefence || respondent2FullDefence;
        }

        return false;
    }

    public static final Predicate<CaseData> awaitingResponsesFullAdmitReceived = ResponsesProgressPredicates::getPredicateForAwaitingResponsesFullAdmitReceived;

    private static boolean getPredicateForAwaitingResponsesFullAdmitReceived(CaseData caseData) {
        MultiPartyScenario scenario = Objects.requireNonNull(getMultiPartyScenario(caseData));

        if (scenario == MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP) {
            boolean respondent1FullDefence = caseData.getRespondent1ClaimResponseType() != null
                && caseData.getRespondent2ClaimResponseType() == null
                && FULL_ADMISSION.equals(caseData.getRespondent1ClaimResponseType());
            boolean respondent2FullDefence = caseData.getRespondent1ClaimResponseType() == null
                && caseData.getRespondent2ClaimResponseType() != null
                && FULL_ADMISSION.equals(caseData.getRespondent2ClaimResponseType());
            return respondent1FullDefence || respondent2FullDefence;
        }

        return false;
    }

    public static final Predicate<CaseData> awaitingResponsesNonFullDefenceOrFullAdmitReceived
        = ResponsesProgressPredicates::getPredicateForAwaitingResponsesNonFullDefenceOrFullAdmitReceived;

    private static boolean getPredicateForAwaitingResponsesNonFullDefenceOrFullAdmitReceived(CaseData caseData) {
        MultiPartyScenario scenario = Objects.requireNonNull(getMultiPartyScenario(caseData));

        if (scenario == MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP) {
            boolean respondent1NonFullDefenceOrFullAdmit = caseData.getRespondent1ClaimResponseType() != null
                && caseData.getRespondent2ClaimResponseType() == null
                && !FULL_DEFENCE.equals(caseData.getRespondent1ClaimResponseType())
                && !FULL_ADMISSION.equals(caseData.getRespondent1ClaimResponseType());
            boolean respondent2NonFullDefenceOrFullAdmit = caseData.getRespondent1ClaimResponseType() == null
                && caseData.getRespondent2ClaimResponseType() != null
                && !FULL_DEFENCE.equals(caseData.getRespondent2ClaimResponseType())
                && !FULL_ADMISSION.equals(caseData.getRespondent2ClaimResponseType());
            return respondent1NonFullDefenceOrFullAdmit || respondent2NonFullDefenceOrFullAdmit;
        }

        return false;
    }
}
