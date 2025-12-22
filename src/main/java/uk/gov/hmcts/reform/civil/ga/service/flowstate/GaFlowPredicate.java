package uk.gov.hmcts.reform.civil.ga.service.flowstate;

import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeRequestMoreInfoOption;
import uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;

import java.util.function.Predicate;

import static uk.gov.hmcts.reform.civil.enums.PaymentStatus.SUCCESS;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeDecisionOption.FREE_FORM_ORDER;
import static uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeDecisionOption.LIST_FOR_A_HEARING;
import static uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeDecisionOption.MAKE_AN_ORDER;
import static uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeDecisionOption.MAKE_ORDER_FOR_WRITTEN_REPRESENTATIONS;
import static uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeDecisionOption.REQUEST_MORE_INFO;
import static uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeMakeAnOrderOption.APPROVE_OR_EDIT;
import static uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeMakeAnOrderOption.DISMISS_THE_APPLICATION;
import static uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeMakeAnOrderOption.GIVE_DIRECTIONS_WITHOUT_HEARING;

public class GaFlowPredicate {

    private GaFlowPredicate() {
        //Utility class
    }

    public static final Predicate<GeneralApplicationCaseData> withOutNoticeApplication = caseData ->
        caseData.getGeneralAppInformOtherParty() != null
            && caseData.getGeneralAppRespondentAgreement().getHasAgreed() == YES
            || (caseData.getGeneralAppInformOtherParty() != null
            && caseData.getGeneralAppInformOtherParty().getIsWithNotice() == NO);

    public static final Predicate<GeneralApplicationCaseData> withNoticeApplication = caseData ->
        caseData.getGeneralAppInformOtherParty() != null
            && caseData.getGeneralAppRespondentAgreement().getHasAgreed() == NO
            || (caseData.getGeneralAppInformOtherParty() != null
            && caseData.getGeneralAppInformOtherParty().getIsWithNotice() == YES);

    public static final Predicate<GeneralApplicationCaseData> paymentSuccess = caseData ->
        caseData.getGeneralAppPBADetails() != null
            && caseData.getGeneralAppPBADetails().getPaymentDetails() != null
            && caseData.getGeneralAppPBADetails().getPaymentDetails().getStatus() == SUCCESS;

    public static final Predicate<GeneralApplicationCaseData> judgeMadeDecision = caseData ->
        caseData.getJudicialDecision() != null;

    public static final Predicate<GeneralApplicationCaseData> judgeMadeListingForHearing = caseData ->
        caseData.getJudicialDecision() != null
            && caseData.getJudicialDecision().getDecision().equals(LIST_FOR_A_HEARING)
            && caseData.getJudicialListForHearing() != null;

    public static final Predicate<GeneralApplicationCaseData> judgeRequestAdditionalInfo = caseData ->
        caseData.getJudicialDecision() != null
            && caseData.getJudicialDecision().getDecision().equals(REQUEST_MORE_INFO);

    public static final Predicate<GeneralApplicationCaseData> judgeMadeDirections = caseData ->
        caseData.getJudicialDecision() != null
            && caseData.getJudicialDecision().getDecision().equals(MAKE_AN_ORDER)
            && caseData.getJudicialDecisionMakeOrder().getMakeAnOrder().equals(GIVE_DIRECTIONS_WITHOUT_HEARING);

    public static final Predicate<GeneralApplicationCaseData> judgeMadeOrder = caseData ->
        caseData.getJudicialDecision() != null
            && (caseData.getJudicialDecision().getDecision().equals(MAKE_AN_ORDER)
            && caseData.getJudicialDecisionMakeOrder().getMakeAnOrder().equals(APPROVE_OR_EDIT))
            || (caseData.getJudicialDecision().getDecision().equals(FREE_FORM_ORDER));

    public static final Predicate<GeneralApplicationCaseData> judgeMadeWrittenRep = caseData ->
        caseData.getJudicialDecision() != null
            && caseData.getJudicialDecision().getDecision().equals(MAKE_ORDER_FOR_WRITTEN_REPRESENTATIONS);

    public static final Predicate<GeneralApplicationCaseData> judgeMadeDismissalOrder = caseData ->
        caseData.getJudicialDecision() != null
            && caseData.getJudicialDecision().getDecision().equals(MAKE_AN_ORDER)
            && caseData.getJudicialDecisionMakeOrder().getMakeAnOrder().equals(DISMISS_THE_APPLICATION);

    public static final Predicate<GeneralApplicationCaseData> isLipApplication = caseData -> caseData.getIsGaApplicantLip() == YES;
    public static final Predicate<GeneralApplicationCaseData> isLipRespondent = caseData -> caseData.getIsGaRespondentOneLip() == YES;

    public static final Predicate<GeneralApplicationCaseData> caseContainsLiPGa = caseData ->
        YesOrNo.YES.equals(caseData.getIsGaApplicantLip())
            || YesOrNo.YES.equals(caseData.getIsGaRespondentOneLip());

    public static final Predicate<GeneralApplicationCaseData> isVaryJudgementAppByResp = caseData -> caseData.getParentClaimantIsApplicant().equals(NO)
            && caseData.getGeneralAppType().getTypes().contains(GeneralApplicationTypes.VARY_PAYMENT_TERMS_OF_JUDGMENT);

    public static final Predicate<GeneralApplicationCaseData> isWelshApplicant =
        caseData -> (caseData.isApplicationBilingual());

    public static final Predicate<GeneralApplicationCaseData> judgeRequestForMoreInfo = caseData ->
        caseData.getJudicialDecision() != null
            && caseData.getJudicialDecision().getDecision().equals(REQUEST_MORE_INFO)
            && (caseData.getJudicialDecisionRequestMoreInfo() != null
            && caseData.getJudicialDecisionRequestMoreInfo().getRequestMoreInfoOption() != GAJudgeRequestMoreInfoOption.SEND_APP_TO_OTHER_PARTY);

    public static final Predicate<GeneralApplicationCaseData> isWelshJudgeDecision =
        caseData -> isWelshApplicant.test(caseData)
            && (judgeMadeWrittenRep.test(caseData) || judgeMadeDirections.test(caseData)
            || judgeRequestForMoreInfo.test(caseData) || judgeMadeOrder.test(caseData)
            || judgeMadeDismissalOrder.test(caseData) || judgeMadeListingForHearing.test(caseData));

    public static final Predicate<GeneralApplicationCaseData> isFreeFeeWelshApplication = caseData ->
        isWelshApplicant.test(caseData) && (caseData.getGeneralAppPBADetails() != null
            && (caseData.getGeneralAppPBADetails().getFee().getCode().equals("FREE")) && caseData.getGeneralAppType().getTypes()
            .contains(GeneralApplicationTypes.ADJOURN_HEARING));
}
