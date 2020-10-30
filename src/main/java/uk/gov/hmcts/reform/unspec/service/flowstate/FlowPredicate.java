package uk.gov.hmcts.reform.unspec.service.flowstate;

import uk.gov.hmcts.reform.unspec.model.CaseData;

import java.util.Objects;
import java.util.function.Predicate;

import static uk.gov.hmcts.reform.unspec.enums.CaseState.CLOSED;
import static uk.gov.hmcts.reform.unspec.enums.CaseState.STAYED;

public class FlowPredicate {

    public static final Predicate<CaseData> claimantIssueClaim = caseData ->
        caseData.getClaimIssuedDate() != null
            && caseData.getLegacyCaseReference() != null;

    public static final Predicate<CaseData> claimantConfirmService = caseData ->
        caseData.getDeemedServiceDateToRespondentSolicitor1() != null
            && Objects.isNull(caseData.getWithdrawClaim())
            && Objects.isNull(caseData.getDiscontinueClaim());

    public static final Predicate<CaseData> defendantAcknowledgeService = caseData ->
        caseData.getRespondent1ClaimResponseIntentionType() != null
            && caseData.getRespondent1ClaimResponseDocument() == null;

    public static final Predicate<CaseData> defendantRespondToClaim = caseData ->
        caseData.getRespondent1ClaimResponseDocument() != null;

    public static final Predicate<CaseData> defendantAskForAnExtension = caseData ->
        caseData.getRespondentSolicitor1claimResponseExtensionProposedDeadline() != null;

    public static final Predicate<CaseData> claimantRespondToRequestForExtension = caseData ->
        caseData.getRespondentSolicitor1claimResponseExtensionAccepted() != null;

    public static final Predicate<CaseData> claimantRespondToDefence = caseData ->
        caseData.getApplicant1ProceedWithClaim() != null
            && caseData.getApplicant1DefenceResponseDocument() != null;

    public static final Predicate<CaseData> schedulerStayClaim = caseData ->
        caseData.getCcdState() == STAYED;

    public static final Predicate<CaseData> claimWithdrawn = caseData ->
        caseData.getWithdrawClaim() != null
            && caseData.getCcdState() == CLOSED;

    public static final Predicate<CaseData> claimDiscontinued = caseData ->
        caseData.getDiscontinueClaim() != null
            && caseData.getCcdState() == CLOSED;

    private FlowPredicate() {
        //Utility class
    }

}
