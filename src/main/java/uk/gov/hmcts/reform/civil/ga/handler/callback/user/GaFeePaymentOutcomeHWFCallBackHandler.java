package uk.gov.hmcts.reform.civil.ga.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.service.GaPaymentRequestUpdateCallbackService;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
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
public class GaFeePaymentOutcomeHWFCallBackHandler extends HWFCallbackHandlerBase {

    public static final String WRONG_REMISSION_TYPE_SELECTED = "Incorrect remission type selected";
    public static final String CASE_STATE_INVALID = "Case is in invalid state";
    public static final String PROCESS_FEE_PAYMENT_FAILED = "Process fee payment failed";
    private static final List<CaseEvent> EVENTS = List.of(CaseEvent.FEE_PAYMENT_OUTCOME_GA);

    public GaFeePaymentOutcomeHWFCallBackHandler(ObjectMapper objectMapper,
                                                 GaPaymentRequestUpdateCallbackService paymentRequestUpdateCallbackService,
                                                 HwfNotificationService hwfNotificationService, FeatureToggleService featureToggleService) {
        super(objectMapper, EVENTS, paymentRequestUpdateCallbackService, hwfNotificationService, featureToggleService);
    }

    @Override
    protected Map<String, Callback> callbacks() {
        return new ImmutableMap.Builder<String, Callback>()
            .put(callbackKey(ABOUT_TO_START), this::setData)
            .put(callbackKey(ABOUT_TO_SUBMIT), this::submitFeePaymentOutcome)
            .put(callbackKey(MID, "remission-type"), this::validateSelectedRemissionType)
            .put(callbackKey(SUBMITTED), this::emptySubmittedCallbackResponse)
            .build();
    }

    private CallbackResponse setData(CallbackParams callbackParams) {
        GeneralApplicationCaseData caseData = (GeneralApplicationCaseData) callbackParams.getBaseCaseData();
        GeneralApplicationCaseData.GeneralApplicationCaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();
        FeePaymentOutcomeDetails.FeePaymentOutcomeDetailsBuilder feeDetailBuilder = FeePaymentOutcomeDetails.builder();
        feeDetailBuilder.hwfNumberAvailable(YesOrNo.NO);
        if (Objects.nonNull(caseData.getGeneralAppHelpWithFees())
            && Objects.nonNull(caseData.getGeneralAppHelpWithFees().getHelpWithFeesReferenceNumber())) {
            feeDetailBuilder.hwfNumberAvailable(YesOrNo.YES)
                .hwfNumberForFeePaymentOutcome(caseData.getGeneralAppHelpWithFees().getHelpWithFeesReferenceNumber());
        }
        caseDataBuilder.feePaymentOutcomeDetails(feeDetailBuilder.build());
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }

    private CallbackResponse validateSelectedRemissionType(CallbackParams callbackParams) {
        GeneralApplicationCaseData caseData = (GeneralApplicationCaseData) callbackParams.getBaseCaseData();
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
        GeneralApplicationCaseData caseData = (GeneralApplicationCaseData) callbackParams.getBaseCaseData();
        GeneralApplicationCaseData.GeneralApplicationCaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();

        if (caseData.isHWFTypeApplication()) {
            LocalDate issueDate = LocalDate.now();
            caseDataBuilder.issueDate(issueDate).build();
        }
        caseData = caseDataBuilder.build();
        caseData = HwFFeeTypeUtil.updateHwfReferenceNumber(caseData);

        assert paymentRequestUpdateCallbackService != null;
        GeneralApplicationCaseData processedCaseData = paymentRequestUpdateCallbackService.processHwf(caseData);
        assert hwfNotificationService != null;
        hwfNotificationService.sendNotification(processedCaseData, CaseEvent.FEE_PAYMENT_OUTCOME_GA);
        List<String> errors = new ArrayList<>();
        if (Objects.isNull(processedCaseData)) {
            errors.add(PROCESS_FEE_PAYMENT_FAILED);
        } else {
            if (processedCaseData.isHWFTypeApplication()) {

                CaseEvent caseEvent = INITIATE_GENERAL_APPLICATION_AFTER_PAYMENT;
                if (caseData.getGeneralAppType().getTypes().contains(
                    GeneralApplicationTypes.CONFIRM_CCJ_DEBT_PAID)) {
                    caseEvent = INITIATE_COSC_APPLICATION_AFTER_PAYMENT;
                }
                caseData = processedCaseData.toBuilder()
                    .gaHwfDetails(caseData.getGaHwfDetails().toBuilder()
                                      .fee(caseData.getGeneralAppPBADetails().getFee())
                                      .hwfReferenceNumber(caseData
                                                              .getGeneralAppHelpWithFees()
                                                              .getHelpWithFeesReferenceNumber())
                                      .outstandingFee(BigDecimal.ZERO).build())
                    .businessProcess(BusinessProcess.ready(caseEvent)).build();
            } else if (processedCaseData.isHWFTypeAdditional()) {
                caseData = processedCaseData.toBuilder()
                    .additionalHwfDetails(caseData.getAdditionalHwfDetails().toBuilder()
                                              .fee(caseData.getGeneralAppPBADetails().getFee())
                                              .hwfReferenceNumber(caseData
                                                                      .getGeneralAppHelpWithFees()
                                                                      .getHelpWithFeesReferenceNumber())
                                              .outstandingFee(BigDecimal.ZERO).build())
                    .businessProcess(BusinessProcess.ready(UPDATE_GA_ADD_HWF))
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
