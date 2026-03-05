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
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes;
import uk.gov.hmcts.reform.civil.ga.callback.GeneralApplicationCallbackHandler;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.ga.service.GaForLipService;
import uk.gov.hmcts.reform.civil.ga.service.ParentCaseUpdateHelper;

import java.util.List;
import java.util.Map;
import java.util.Objects;

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
import static uk.gov.hmcts.reform.civil.ga.enums.dq.GaFinalOrderSelection.ASSISTED_ORDER;
import static uk.gov.hmcts.reform.civil.ga.utils.JudicialDecisionNotificationUtil.isNotificationCriteriaSatisfied;
import static uk.gov.hmcts.reform.civil.ga.utils.RespondentsResponsesUtil.isRespondentsResponseSatisfied;

@Slf4j
@Service
@RequiredArgsConstructor
public class EndGeneralAppBusinessProcessCallbackHandler extends CallbackHandler implements GeneralApplicationCallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(END_BUSINESS_PROCESS_GASPEC);

    private final CaseDetailsConverter caseDetailsConverter;
    private final GaForLipService gaForLipService;
    private final ParentCaseUpdateHelper parentCaseUpdateHelper;
    private static final String FREE_KEYWORD = "FREE";

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(callbackKey(ABOUT_TO_SUBMIT), this::endGeneralApplicationBusinessProcess);
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse endGeneralApplicationBusinessProcess(CallbackParams callbackParams) {
        Long caseId = callbackParams.getGeneralApplicationCaseData().getCcdCaseReference();
        log.info("End general application business process for caseId: {}", caseId);
        GeneralApplicationCaseData data = caseDetailsConverter.toGeneralApplicationCaseData(callbackParams.getRequest().getCaseDetails());

        if (!gaForLipService.isGaForLip(data)
            && (data.getCcdState().equals(AWAITING_APPLICATION_PAYMENT) || isFreeFeeCode(data))) {

            log.info("Updating Judge And Respondent Collection After Payment, not gaForLipService, ccdState: {}, caseId: {}",
                     AWAITING_APPLICATION_PAYMENT,
                     caseId);

            parentCaseUpdateHelper.updateJudgeAndRespondentCollectionAfterPayment(data);
        }

        /*
        * GA for LIP
        * When payment is done via Service Request for GA then,
        * Add GA into collections
        * */
        if (gaForLipService.isGaForLip(data)
            && (isLipPaymentViaServiceRequest(data) || isLipPaymentViaHelpWithFees(data)
            || isFreeFeeCode(data))) {

            log.info("Updating Judge And Respondent Collection After Payment, gaForLipService, ccdState: {}, caseId: {}",
                     data.getCcdState().name(),
                     caseId);

            parentCaseUpdateHelper.updateJudgeAndRespondentCollectionAfterPayment(data);
        }

        CaseState newState;
        if (data.getGeneralAppPBADetails().getPaymentDetails() == null) {
            newState = AWAITING_APPLICATION_PAYMENT;

            /*
             * GA for LIP
             * When Caseworker should have access to GA to perform HelpWithFee then,
             * Add GA into collections
             * */
            if (gaForLipService.isGaForLip(data) && Objects.nonNull(data.getGeneralAppHelpWithFees())
                && data.getGeneralAppHelpWithFees().getHelpWithFee().equals(YesOrNo.YES)) {

                log.info("Updating Master Collection For Hwf, gaForLipService, newState: {}, caseId: {}",
                         newState.name(),
                         caseId);

                parentCaseUpdateHelper.updateMasterCollectionForHwf(data);
            }

        } else if (Objects.nonNull(data.getFinalOrderSelection())) {
            if (data.getFinalOrderSelection().equals(ASSISTED_ORDER)
                && Objects.nonNull(data.getAssistedOrderFurtherHearingDetails())) {
                newState = LISTING_FOR_A_HEARING;
            } else {
                newState = ORDER_MADE;
            }
        } else if (data.getGeneralAppType().getTypes().contains(
            GeneralApplicationTypes.CONFIRM_CCJ_DEBT_PAID)) {
            newState = APPLICATION_DISMISSED;
        } else if (data.getParentClaimantIsApplicant().equals(YesOrNo.NO) && data.getGeneralAppType().getTypes().contains(
            GeneralApplicationTypes.VARY_PAYMENT_TERMS_OF_JUDGMENT) && isRespondentsResponseSatisfied(
            data,
            data.copy().build())) {
            newState = PROCEEDS_IN_HERITAGE;
        } else {
            newState = (isNotificationCriteriaSatisfied(data) && !isRespondentsResponseSatisfied(
                data,
                data.copy().build()
            ))
                ? AWAITING_RESPONDENT_RESPONSE
                : APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION;
        }

        if (!(data.getCcdState().equals(AWAITING_DIRECTIONS_ORDER_DOCS)
            || data.getCcdState().equals(AWAITING_ADDITIONAL_INFORMATION)
            || data.getCcdState().equals(AWAITING_WRITTEN_REPRESENTATIONS))) {

            log.info("Updating Parent With GA State, newState: {}, caseId: {}",
                     newState.name(),
                     caseId);

            parentCaseUpdateHelper.updateParentWithGAState(data, newState.getDisplayedValue());
        } else {
            newState = data.getCcdState();
        }
        return evaluateReady(callbackParams, newState);
    }

    private CallbackResponse evaluateReady(CallbackParams callbackParams,
                                           CaseState newState) {
        log.info("Evaluate ready, newState: {}, for caseId: {}",
                 newState.name(),
                 callbackParams.getGeneralApplicationCaseData().getCcdCaseReference());

        Map<String, Object> output = callbackParams.getRequest().getCaseDetails().getData();

        return AboutToStartOrSubmitCallbackResponse.builder()
            .state(newState.toString())
            .data(output)
            .build();
    }

    private boolean isFreeFeeCode(GeneralApplicationCaseData data) {
        return (data.getCcdState().equals(PENDING_APPLICATION_ISSUED)
            && Objects.nonNull(data.getGeneralAppPBADetails())
            && Objects.nonNull(data.getGeneralAppPBADetails().getFee())
            && (FREE_KEYWORD.equalsIgnoreCase(data.getGeneralAppPBADetails().getFee().getCode())));
    }

    private boolean isLipPaymentViaServiceRequest(GeneralApplicationCaseData data) {
        log.info("Is LIP payment via service request for caseId: {}", data.getCcdCaseReference());
        return data.getCcdState().equals(AWAITING_APPLICATION_PAYMENT)
            && (Objects.isNull(data.getGeneralAppHelpWithFees())
            || data.getGeneralAppHelpWithFees().getHelpWithFee() == YesOrNo.NO);
    }

    private boolean isLipPaymentViaHelpWithFees(GeneralApplicationCaseData data) {
        log.info("Is LIP payment via help with fees for caseId: {}", data.getCcdCaseReference());
        return data.getCcdState().equals(AWAITING_APPLICATION_PAYMENT)
            && !Objects.isNull(data.getGeneralAppHelpWithFees())
            && data.getGeneralAppHelpWithFees().getHelpWithFee() == YesOrNo.YES
            && !Objects.isNull(data.getFeePaymentOutcomeDetails())
            && (data.getFeePaymentOutcomeDetails().getHwfFullRemissionGrantedForGa() == YesOrNo.YES
                || (!Objects.isNull(data.getFeePaymentOutcomeDetails().getHwfOutstandingFeePaymentDoneForGa())
            && data.getFeePaymentOutcomeDetails().getHwfOutstandingFeePaymentDoneForGa().contains("Yes")));
    }
}
