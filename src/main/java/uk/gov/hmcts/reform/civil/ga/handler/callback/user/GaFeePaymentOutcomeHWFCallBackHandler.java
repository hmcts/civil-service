package uk.gov.hmcts.reform.civil.ga.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes;
import uk.gov.hmcts.reform.civil.ga.callback.GeneralApplicationCallbackHandler;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.HelpWithFeesDetails;
import uk.gov.hmcts.reform.civil.ga.service.GaPaymentRequestUpdateCallbackService;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.citizenui.FeePaymentOutcomeDetails;
import uk.gov.hmcts.reform.civil.ga.service.HwfNotificationService;
import uk.gov.hmcts.reform.civil.ga.utils.HwFFeeTypeUtil;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.INITIATE_COSC_APPLICATION_AFTER_PAYMENT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.INITIATE_GENERAL_APPLICATION_AFTER_PAYMENT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.UPDATE_GA_ADD_HWF;

@Slf4j
@Service
@RequiredArgsConstructor
public class GaFeePaymentOutcomeHWFCallBackHandler extends CallbackHandler implements GeneralApplicationCallbackHandler {

    public static final String WRONG_REMISSION_TYPE_SELECTED = "Incorrect remission type selected";
    public static final String CASE_STATE_INVALID = "Case is in invalid state";
    public static final String PROCESS_FEE_PAYMENT_FAILED = "Process fee payment failed";
    private static final List<CaseEvent> EVENTS = List.of(CaseEvent.FEE_PAYMENT_OUTCOME_GA);

    private final ObjectMapper objectMapper;
    private final GaPaymentRequestUpdateCallbackService paymentRequestUpdateCallbackService;
    private final HwfNotificationService hwfNotificationService;

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_START), this::setData,
            callbackKey(ABOUT_TO_SUBMIT), this::submitFeePaymentOutcome,
            callbackKey(MID, "remission-type"), this::validateSelectedRemissionType,
            callbackKey(SUBMITTED), this::emptySubmittedCallbackResponse
        );
    }

    private CallbackResponse setData(CallbackParams callbackParams) {
        GeneralApplicationCaseData caseData = callbackParams.getGeneralApplicationCaseData();
        GeneralApplicationCaseData caseDataBuilder = caseData.copy();
        FeePaymentOutcomeDetails feePaymentOutcomeDetails = new FeePaymentOutcomeDetails();
        feePaymentOutcomeDetails.setHwfNumberAvailable(YesOrNo.NO);
        if (Objects.nonNull(caseData.getGeneralAppHelpWithFees())
            && Objects.nonNull(caseData.getGeneralAppHelpWithFees().getHelpWithFeesReferenceNumber())) {
            feePaymentOutcomeDetails.setHwfNumberAvailable(YesOrNo.YES)
                .setHwfNumberForFeePaymentOutcome(caseData.getGeneralAppHelpWithFees().getHelpWithFeesReferenceNumber());
        }
        caseDataBuilder.feePaymentOutcomeDetails(feePaymentOutcomeDetails);
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }

    private CallbackResponse validateSelectedRemissionType(CallbackParams callbackParams) {
        GeneralApplicationCaseData caseData = callbackParams.getGeneralApplicationCaseData();
        var errors = new ArrayList<String>();

        if ((caseData.isHWFTypeApplication()
            && caseData.getFeePaymentOutcomeDetails().getHwfFullRemissionGrantedForGa() == YesOrNo.YES
            && Objects.nonNull(caseData.getGaHwfDetails())
            && Objects.nonNull(caseData.getGaHwfDetails().getOutstandingFee())
            && !Objects.equals(caseData.getGaHwfDetails().getOutstandingFee(), BigDecimal.ZERO))
            || (caseData.isHWFTypeAdditional()
            && caseData.getFeePaymentOutcomeDetails().getHwfFullRemissionGrantedForAdditionalFee() == YesOrNo.YES
            && Objects.nonNull(caseData.getAdditionalHwfDetails())
            && Objects.nonNull(caseData.getAdditionalHwfDetails().getOutstandingFee())
            && !Objects.equals(caseData.getAdditionalHwfDetails().getOutstandingFee(), BigDecimal.ZERO))) {
            errors.add(WRONG_REMISSION_TYPE_SELECTED);
        }
        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .build();
    }

    private CallbackResponse submitFeePaymentOutcome(CallbackParams callbackParams) {
        GeneralApplicationCaseData caseData = callbackParams.getGeneralApplicationCaseData();
        GeneralApplicationCaseData caseDataBuilder = caseData.copy();

        if (caseData.isHWFTypeApplication()) {
            LocalDate issueDate = LocalDate.now();
            caseDataBuilder.issueDate(issueDate).build();
        }
        caseData = caseDataBuilder.build();
        caseData = HwFFeeTypeUtil.updateHwfReferenceNumber(caseData);

        GeneralApplicationCaseData processedCaseData = paymentRequestUpdateCallbackService.processHwf(caseData);

        List<String> errors = new ArrayList<>();
        if (Objects.isNull(processedCaseData)) {
            errors.add(PROCESS_FEE_PAYMENT_FAILED);
        } else {
            hwfNotificationService.sendNotification(processedCaseData, CaseEvent.FEE_PAYMENT_OUTCOME_GA);

            if (processedCaseData.isHWFTypeApplication()) {

                CaseEvent caseEvent = INITIATE_GENERAL_APPLICATION_AFTER_PAYMENT;
                if (caseData.getGeneralAppType().getTypes().contains(
                    GeneralApplicationTypes.CONFIRM_CCJ_DEBT_PAID)) {
                    caseEvent = INITIATE_COSC_APPLICATION_AFTER_PAYMENT;
                }
                HelpWithFeesDetails updatedGaHwfDetails = caseData.getGaHwfDetails().copy();
                updatedGaHwfDetails
                    .setFee(caseData.getGeneralAppPBADetails().getFee())
                    .setHwfReferenceNumber(caseData.getGeneralAppHelpWithFees().getHelpWithFeesReferenceNumber())
                    .setOutstandingFee(BigDecimal.ZERO);
                caseData = processedCaseData.copy()
                    .gaHwfDetails(updatedGaHwfDetails)
                    .businessProcess(BusinessProcess.readyGa(caseEvent)).build();
            } else if (processedCaseData.isHWFTypeAdditional()) {
                HelpWithFeesDetails updatedAdditionalHwfDetails = caseData.getAdditionalHwfDetails().copy();
                updatedAdditionalHwfDetails
                    .setFee(caseData.getGeneralAppPBADetails().getFee())
                    .setHwfReferenceNumber(caseData.getGeneralAppHelpWithFees().getHelpWithFeesReferenceNumber())
                    .setOutstandingFee(BigDecimal.ZERO);
                caseData = processedCaseData.copy()
                    .additionalHwfDetails(updatedAdditionalHwfDetails)
                    .businessProcess(BusinessProcess.readyGa(UPDATE_GA_ADD_HWF))
                    .build();
                log.info("Start business process UPDATE_GA_ADD_HWF for caseId: {}", caseData.getCcdCaseReference());
            } else {
                errors.add(CASE_STATE_INVALID);
            }
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseData.toMap(objectMapper))
            .errors(errors)
            .build();
    }

}
