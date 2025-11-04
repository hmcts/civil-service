package uk.gov.hmcts.reform.civil.service.flowstate;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.flowstate.legacy.LegacyFlowDelegate;

import java.util.function.Predicate;

public class FlowLipPredicate {

    private FlowLipPredicate() {

    }

    /**
     * @deprecated use {@link LegacyFlowDelegate#isLipCase}
     */
    @Deprecated
    public static final Predicate<CaseData> isLipCase = LegacyFlowDelegate.isLipCase;

    /**
     * @deprecated use {@link LegacyFlowDelegate#agreedToMediation}
     */
    @Deprecated
    public static final Predicate<CaseData> agreedToMediation = LegacyFlowDelegate.agreedToMediation;

    /**
     * @deprecated use {@link LegacyFlowDelegate#isTranslatedDocumentUploaded}
     */
    @Deprecated
    public static final Predicate<CaseData> isTranslatedDocumentUploaded = LegacyFlowDelegate.isTranslatedDocumentUploaded;

    /**
     * @deprecated use {@link LegacyFlowDelegate#ccjRequestJudgmentByAdmission}
     */
    @Deprecated
    public static final Predicate<CaseData> ccjRequestJudgmentByAdmission = LegacyFlowDelegate.ccjRequestJudgmentByAdmission;

    /**
     * @deprecated use {@link LegacyFlowDelegate#isRespondentSignSettlementAgreement}
     */
    @Deprecated
    public static final Predicate<CaseData> isRespondentSignSettlementAgreement = LegacyFlowDelegate.isRespondentSignSettlementAgreement;

    /**
     * @deprecated use {@link LegacyFlowDelegate#nocSubmittedForLiPDefendantBeforeOffline}
     */
    @Deprecated
    public static final Predicate<CaseData> nocSubmittedForLiPDefendantBeforeOffline = LegacyFlowDelegate.nocSubmittedForLiPDefendantBeforeOffline;

    /**
     * @deprecated use {@link LegacyFlowDelegate#nocSubmittedForLiPDefendant}
     */
    @Deprecated
    public static final Predicate<CaseData> nocSubmittedForLiPDefendant = LegacyFlowDelegate.nocSubmittedForLiPDefendant;

    /**
     * @deprecated use {@link LegacyFlowDelegate#isContainsLip}
     */
    @Deprecated
    public static final Predicate<CaseData> isContainsLip = LegacyFlowDelegate.isContainsLip; //TODO: No usage ?

}
