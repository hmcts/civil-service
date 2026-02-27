package uk.gov.hmcts.reform.civil.ga.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeDecisionOption;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.enums.CaseState.APPLICATION_ADD_PAYMENT;
import static uk.gov.hmcts.reform.civil.enums.CaseState.APPLICATION_DISMISSED;
import static uk.gov.hmcts.reform.civil.enums.CaseState.APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION;
import static uk.gov.hmcts.reform.civil.enums.CaseState.AWAITING_ADDITIONAL_INFORMATION;
import static uk.gov.hmcts.reform.civil.enums.CaseState.AWAITING_DIRECTIONS_ORDER_DOCS;
import static uk.gov.hmcts.reform.civil.enums.CaseState.AWAITING_RESPONDENT_RESPONSE;
import static uk.gov.hmcts.reform.civil.enums.CaseState.AWAITING_WRITTEN_REPRESENTATIONS;
import static uk.gov.hmcts.reform.civil.enums.CaseState.LISTING_FOR_A_HEARING;
import static uk.gov.hmcts.reform.civil.enums.CaseState.ORDER_MADE;
import static uk.gov.hmcts.reform.civil.enums.CaseState.PROCEEDS_IN_HERITAGE;
import static uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeDecisionOption.FREE_FORM_ORDER;
import static uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeDecisionOption.LIST_FOR_A_HEARING;
import static uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeDecisionOption.MAKE_AN_ORDER;
import static uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeDecisionOption.MAKE_ORDER_FOR_WRITTEN_REPRESENTATIONS;
import static uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeDecisionOption.REQUEST_MORE_INFO;
import static uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeMakeAnOrderOption.APPROVE_OR_EDIT;
import static uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeMakeAnOrderOption.DISMISS_THE_APPLICATION;
import static uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeMakeAnOrderOption.GIVE_DIRECTIONS_WITHOUT_HEARING;
import static uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes.STRIKE_OUT;
import static uk.gov.hmcts.reform.civil.ga.utils.RespondentsResponsesUtil.isRespondentsResponseSatisfied;

@Slf4j
@Service
@RequiredArgsConstructor
public class StateGeneratorService {

    private final JudicialDecisionHelper judicialDecisionHelper;

    public CaseState getCaseStateForEndJudgeBusinessProcess(GeneralApplicationCaseData data) {
        log.info("Starting getCaseStateForEndJudgeBusinessProcess for Case ID: {}", data.getCcdCaseReference());
        GAJudgeDecisionOption decision;
        if (data.getJudicialDecision() != null) {
            decision = data.getJudicialDecision().getDecision();
        } else {
            decision = null;
        }
        if (isCaseDismissed(data)) {
            return APPLICATION_DISMISSED;
        } else if (decision == MAKE_AN_ORDER && data.getJudicialDecisionMakeOrder()
            .getMakeAnOrder().equals(GIVE_DIRECTIONS_WITHOUT_HEARING)) {
            return AWAITING_DIRECTIONS_ORDER_DOCS;
        } else if (decision == REQUEST_MORE_INFO) {

            return getNewStateForRequestMoreInfo(data);

        } else if (decision == MAKE_ORDER_FOR_WRITTEN_REPRESENTATIONS) {
            return AWAITING_WRITTEN_REPRESENTATIONS;
        } else if (decision == LIST_FOR_A_HEARING) {
            return  LISTING_FOR_A_HEARING;
        } else if (decision == MAKE_AN_ORDER && data.getJudicialDecisionMakeOrder() != null
            && APPROVE_OR_EDIT.equals(data.getJudicialDecisionMakeOrder().getMakeAnOrder())) {
            if (YesOrNo.YES.equals(data.getParentClaimantIsApplicant())
                && data.getGeneralAppType().getTypes().contains(STRIKE_OUT)) {
                return PROCEEDS_IN_HERITAGE;
            } else {
                return ORDER_MADE;
            }
        } else if (data.getApproveConsentOrder() != null || decision == FREE_FORM_ORDER) {
            return ORDER_MADE;
        }
        return data.getCcdState();
    }

    private boolean isCaseDismissed(GeneralApplicationCaseData caseData) {
        boolean isJudicialDecisionNotNull = caseData.getJudicialDecisionMakeOrder() != null
            && caseData
                .getJudicialDecisionMakeOrder()
                .getMakeAnOrder() != null;

        boolean isJudicialDecisionMakeOrderIsDismissed = isJudicialDecisionNotNull
            && caseData
                .getJudicialDecisionMakeOrder()
                .getMakeAnOrder()
                .equals(DISMISS_THE_APPLICATION);

        return isJudicialDecisionNotNull && isJudicialDecisionMakeOrderIsDismissed;
    }

    private CaseState getNewStateForRequestMoreInfo(GeneralApplicationCaseData caseData) {
        log.info("Processing new state for 'Request More Info' for Case ID: {}", caseData.getCcdCaseReference());
        if (judicialDecisionHelper.isApplicationUncloakedWithAdditionalFee(caseData)) {
            if (caseData.getGeneralAppPBADetails().getAdditionalPaymentDetails() == null) {
                return APPLICATION_ADD_PAYMENT;
            } else if (hasPayment(caseData)
                && (isConsentOrderRespondentSatisfied(caseData) || isUrgentWithoutNotice(caseData))) {
                return APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION;
            } else {
                return AWAITING_RESPONDENT_RESPONSE;
            }
        }
        return AWAITING_ADDITIONAL_INFORMATION;
    }

    private static boolean hasPayment(GeneralApplicationCaseData caseData) {
        return caseData.getGeneralAppPBADetails().getAdditionalPaymentDetails() != null;
    }

    private static boolean isConsentOrderRespondentSatisfied(GeneralApplicationCaseData caseData) {
        return nonNull(caseData.getGeneralAppConsentOrder()) && isRespondentsResponseSatisfied(
            caseData,
            caseData.copy().build()
        );
    }

    private boolean isUrgentWithoutNotice(GeneralApplicationCaseData caseData) {
        return caseData.getGeneralAppInformOtherParty() != null
            && YesOrNo.NO.equals(caseData.getGeneralAppInformOtherParty().getIsWithNotice())
            && caseData.isUrgent();
    }
}
