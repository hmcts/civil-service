package uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.businessprocess;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes;
import uk.gov.hmcts.reform.civil.ga.callback.GeneralApplicationCallbackHandler;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.service.GaForLipService;
import uk.gov.hmcts.reform.civil.ga.service.ParentCaseUpdateHelper;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.citizenui.FeePaymentOutcomeDetails;
import uk.gov.hmcts.reform.civil.model.citizenui.HelpWithFees;
import uk.gov.hmcts.reform.civil.model.genapplication.GAApplicationType;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.END_BUSINESS_PROCESS_GASPEC;

import static uk.gov.hmcts.reform.civil.enums.CaseState.APPLICATION_DISMISSED;
import static uk.gov.hmcts.reform.civil.enums.CaseState.APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION;
import static uk.gov.hmcts.reform.civil.enums.CaseState.AWAITING_ADDITIONAL_INFORMATION;
import static uk.gov.hmcts.reform.civil.enums.CaseState.AWAITING_APPLICATION_PAYMENT;
import static uk.gov.hmcts.reform.civil.enums.CaseState.AWAITING_DIRECTIONS_ORDER_DOCS;
import static uk.gov.hmcts.reform.civil.enums.CaseState.AWAITING_RESPONDENT_RESPONSE;
import static uk.gov.hmcts.reform.civil.enums.CaseState.AWAITING_WRITTEN_REPRESENTATIONS;
import static uk.gov.hmcts.reform.civil.enums.CaseState.LISTING_FOR_A_HEARING;
import static uk.gov.hmcts.reform.civil.enums.CaseState.ORDER_MADE;
import static uk.gov.hmcts.reform.civil.enums.CaseState.PENDING_APPLICATION_ISSUED;
import static uk.gov.hmcts.reform.civil.enums.CaseState.PROCEEDS_IN_HERITAGE;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.ga.enums.dq.GaFinalOrderSelection.ASSISTED_ORDER;
import static uk.gov.hmcts.reform.civil.ga.utils.JudicialDecisionNotificationUtil.isNotificationCriteriaSatisfied;
import static uk.gov.hmcts.reform.civil.ga.utils.RespondentsResponsesUtil.isRespondentsResponseSatisfied;

@Slf4j
@Service
@RequiredArgsConstructor
public class EndGeneralAppBusinessProcessCallbackHandler extends CallbackHandler implements GeneralApplicationCallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(END_BUSINESS_PROCESS_GASPEC);
    private static final String FREE_KEYWORD = "FREE";
    private static final Set<CaseState> NON_UPDATABLE_STATES = Set.of(
        AWAITING_DIRECTIONS_ORDER_DOCS,
        AWAITING_ADDITIONAL_INFORMATION,
        AWAITING_WRITTEN_REPRESENTATIONS
    );
    private final CaseDetailsConverter caseDetailsConverter;
    private final GaForLipService gaForLipService;
    private final ParentCaseUpdateHelper parentCaseUpdateHelper;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(callbackKey(ABOUT_TO_SUBMIT), this::endBusinessProcess);
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse endBusinessProcess(CallbackParams callbackParams) {
        var data = caseDetailsConverter.toGeneralApplicationCaseData(callbackParams.getRequest().getCaseDetails());
        var caseId = data.getCcdCaseReference();
        log.info("End general application business process for caseId: {}", caseId);

        if (shouldSyncJudgeAndRespondentAfterPayment(data)) {
            /*
            * GA for LIP
            * When payment is done via Service Request for GA then,
            * Add GA into collections
            * */
            log.info("Updating Judge And Respondent Collection After Payment, caseId: {}", caseId);
            parentCaseUpdateHelper.updateJudgeAndRespondentCollectionAfterPayment(data);
        }

        var newState = determineNewState(data, caseId);

        if (isStateUpdateAllowed(data.getCcdState())) {
            log.info("Updating Parent With GA State, newState: {}, caseId: {}", newState.name(), caseId);
            parentCaseUpdateHelper.updateParentWithGAState(data, newState.getDisplayedValue());
        } else {
            newState = data.getCcdState();
        }

        return prepareResponse(callbackParams, newState);
    }

    private boolean shouldSyncJudgeAndRespondentAfterPayment(GeneralApplicationCaseData data) {
        var isAwaitingPayment = AWAITING_APPLICATION_PAYMENT.equals(data.getCcdState());
        var isFreeFee = hasFreeFeeCode(data);

        if (!gaForLipService.isGaForLip(data)) {
            return isAwaitingPayment || isFreeFee;
        }

        return isFreeFee || isLipPaymentViaServiceRequest(data) || isLipPaymentViaHelpWithFees(data);
    }

    private void syncHwfCollectionIfRequired(GeneralApplicationCaseData data, Long caseId) {
        if (gaForLipService.isGaForLip(data) && isHelpWithFeesRequested(data)) {
            /*
             * GA for LIP
             * When Caseworker should have access to GA to perform HelpWithFee then,
             * Add GA into collections
             * */
            log.info("Updating master collection for HWF, caseId: {}", caseId);
            parentCaseUpdateHelper.updateMasterCollectionForHwf(data);
        }
    }

    private boolean isHelpWithFeesRequested(GeneralApplicationCaseData data) {
        return Optional.ofNullable(data.getGeneralAppHelpWithFees())
            .map(HelpWithFees::getHelpWithFee).filter(YES::equals).isPresent();
    }

    private boolean isStateUpdateAllowed(CaseState currentState) {
        return !NON_UPDATABLE_STATES.contains(currentState);
    }

    private CaseState determineNewState(GeneralApplicationCaseData data, Long caseId) {
        if (data.getGeneralAppPBADetails().getPaymentDetails() == null) {
            syncHwfCollectionIfRequired(data, caseId);
            return AWAITING_APPLICATION_PAYMENT;
        }

        return determineStateFromOrderOrType(data);
    }

    private CaseState determineStateFromOrderOrType(GeneralApplicationCaseData data) {
        if (data.getFinalOrderSelection() != null) {
            return determineOrderState(data);
        }

        var types = Optional.ofNullable(data.getGeneralAppType())
            .map(GAApplicationType::getTypes).orElse(List.of());

        if (types.contains(GeneralApplicationTypes.CONFIRM_CCJ_DEBT_PAID)) {
            return APPLICATION_DISMISSED;
        }

        var snapshot = data.copy();
        var respondentsSatisfied = isRespondentsResponseSatisfied(data, snapshot);

        if (isVaryPaymentTermsProceedsInHeritage(data, types, respondentsSatisfied)) {
            return PROCEEDS_IN_HERITAGE;
        }

        return determineStandardState(data, respondentsSatisfied);
    }

    private CaseState determineOrderState(GeneralApplicationCaseData data) {
        var needsListing = ASSISTED_ORDER.equals(data.getFinalOrderSelection())
            && data.getAssistedOrderFurtherHearingDetails() != null;
        return needsListing ? LISTING_FOR_A_HEARING : ORDER_MADE;
    }

    private boolean isVaryPaymentTermsProceedsInHeritage(GeneralApplicationCaseData data,
                                                         List<GeneralApplicationTypes> types,
                                                         boolean respondentsSatisfied) {
        return NO.equals(data.getParentClaimantIsApplicant())
            && types.contains(GeneralApplicationTypes.VARY_PAYMENT_TERMS_OF_JUDGMENT)
            && respondentsSatisfied;
    }

    private CaseState determineStandardState(GeneralApplicationCaseData data, boolean respondentsSatisfied) {
        var notifyCriteria = isNotificationCriteriaSatisfied(data);
        return (notifyCriteria && !respondentsSatisfied)
            ? AWAITING_RESPONDENT_RESPONSE
            : APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION;
    }

    private CallbackResponse prepareResponse(CallbackParams callbackParams, CaseState newState) {
        log.info(
            "Preparing response, newState: {}, for caseId: {}",
            newState.name(),
            callbackParams.getGeneralApplicationCaseData().getCcdCaseReference()
        );

        var output = callbackParams.getRequest().getCaseDetails().getData();
        return AboutToStartOrSubmitCallbackResponse.builder().state(newState.toString()).data(output).build();
    }

    private boolean hasFreeFeeCode(GeneralApplicationCaseData data) {
        return (PENDING_APPLICATION_ISSUED.equals(data.getCcdState())
            && Objects.nonNull(data.getGeneralAppPBADetails())
            && Objects.nonNull(data.getGeneralAppPBADetails().getFee())
            && (FREE_KEYWORD.equalsIgnoreCase(data.getGeneralAppPBADetails().getFee().getCode())));
    }

    private boolean isLipPaymentViaServiceRequest(GeneralApplicationCaseData data) {
        log.info("Is LIP payment via service request for caseId: {}", data.getCcdCaseReference());
        if (!AWAITING_APPLICATION_PAYMENT.equals(data.getCcdState())) {
            return false;
        }
        return Optional.ofNullable(data.getGeneralAppHelpWithFees())
            .map(HelpWithFees::getHelpWithFee)
            .map(hwf -> hwf == NO)
            .orElse(true);
    }

    private boolean isLipPaymentViaHelpWithFees(GeneralApplicationCaseData data) {
        log.info("Is LIP payment via help with fees for caseId: {}", data.getCcdCaseReference());

        if (!AWAITING_APPLICATION_PAYMENT.equals(data.getCcdState())) {
            return false;
        }

        return Optional.ofNullable(data.getGeneralAppHelpWithFees())
            .filter(hwf -> hwf.getHelpWithFee() == YES)
            .flatMap(hwf -> Optional.ofNullable(data.getFeePaymentOutcomeDetails()))
            .filter(this::isHwfRemissionOrPaymentDone)
            .isPresent();
    }

    private boolean isHwfRemissionOrPaymentDone(FeePaymentOutcomeDetails outcome) {
        return outcome.getHwfFullRemissionGrantedForGa() == YES
            || Optional.ofNullable(outcome.getHwfOutstandingFeePaymentDoneForGa())
            .filter(list -> list.contains("Yes"))
            .isPresent();
    }
}
