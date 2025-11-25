package uk.gov.hmcts.reform.civil.service.flowstate;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.flowstate.legacy.LegacyFlowDelegate;

import java.util.function.Predicate;

public class FlowLipPredicate {

    private FlowLipPredicate() {
        //Utility classes require a private constructor for checkstyle
    }

    public static final Predicate<CaseData> isLipCase = LegacyFlowDelegate.isLipCase;

    public static final Predicate<CaseData> agreedToMediation = LegacyFlowDelegate.agreedToMediation;

    public static final Predicate<CaseData> isTranslatedDocumentUploaded = LegacyFlowDelegate.isTranslatedDocumentUploaded;

    public static final Predicate<CaseData> ccjRequestJudgmentByAdmission = LegacyFlowDelegate.ccjRequestJudgmentByAdmission;

    public static final Predicate<CaseData> isRespondentSignSettlementAgreement = LegacyFlowDelegate.isRespondentSignSettlementAgreement;

    public static final Predicate<CaseData> nocSubmittedForLiPDefendantBeforeOffline = LegacyFlowDelegate.nocSubmittedForLiPDefendantBeforeOffline;

    public static final Predicate<CaseData> nocSubmittedForLiPDefendant = LegacyFlowDelegate.nocSubmittedForLiPDefendant;
}
