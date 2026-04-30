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
        if (isCaseDismissed(data)) {
            return APPLICATION_DISMISSED;
        }
        if (data.getApproveConsentOrder() != null) {
            return ORDER_MADE;
        }
        GAJudgeDecisionOption decision = getDecision(data);
        if (decision == null) {
            return data.getCcdState();
        }
        return switch (decision) {
            case REQUEST_MORE_INFO -> getNewStateForRequestMoreInfo(data);
            case MAKE_ORDER_FOR_WRITTEN_REPRESENTATIONS -> AWAITING_WRITTEN_REPRESENTATIONS;
            case LIST_FOR_A_HEARING -> LISTING_FOR_A_HEARING;
            case MAKE_AN_ORDER -> getMakeAnOrderState(data);
            case FREE_FORM_ORDER -> ORDER_MADE;
        };
    }

    private GAJudgeDecisionOption getDecision(GeneralApplicationCaseData data) {
        return data.getJudicialDecision() != null ? data.getJudicialDecision().getDecision() : null;
    }

    private boolean isDirectionsWithoutHearing(GeneralApplicationCaseData data) {
        return data.getJudicialDecisionMakeOrder() != null && GIVE_DIRECTIONS_WITHOUT_HEARING.equals(data.getJudicialDecisionMakeOrder().getMakeAnOrder());
    }

    private boolean isApproveOrEditOrder(GeneralApplicationCaseData data) {
        return data.getJudicialDecisionMakeOrder() != null && APPROVE_OR_EDIT.equals(data.getJudicialDecisionMakeOrder().getMakeAnOrder());
    }

    private CaseState getMakeAnOrderState(GeneralApplicationCaseData data) {
        if (isDirectionsWithoutHearing(data)) {
            return AWAITING_DIRECTIONS_ORDER_DOCS;
        }
        if (isApproveOrEditOrder(data)) {
            return shouldProceedInHeritage(data) ? PROCEEDS_IN_HERITAGE : ORDER_MADE;
        }
        return data.getCcdState();
    }

    private boolean shouldProceedInHeritage(GeneralApplicationCaseData data) {
        return YesOrNo.YES.equals(data.getParentClaimantIsApplicant())
            && data.getGeneralAppType().getTypes().contains(STRIKE_OUT);
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
