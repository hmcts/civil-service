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
import uk.gov.hmcts.reform.civil.enums.FeeType;
import uk.gov.hmcts.reform.civil.ga.callback.GeneralApplicationCallbackHandler;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.HelpWithFeesDetails;
import uk.gov.hmcts.reform.civil.ga.utils.HwFFeeTypeUtil;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.FULL_REMISSION_HWF_GA;

@Slf4j
@Service
@RequiredArgsConstructor
public class GaFullRemissionHWFCallbackHandler extends CallbackHandler implements GeneralApplicationCallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(FULL_REMISSION_HWF_GA);

    private final ObjectMapper objectMapper;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_START), this::emptyCallbackResponse,
            callbackKey(ABOUT_TO_SUBMIT), this::fullRemissionHWF,
            callbackKey(SUBMITTED), this::emptySubmittedCallbackResponse
        );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse fullRemissionHWF(CallbackParams callbackParams) {
        GeneralApplicationCaseData caseData = callbackParams.getGeneralApplicationCaseData();
        GeneralApplicationCaseData.GeneralApplicationCaseDataBuilder<?, ?> updatedData = caseData.toBuilder();
        BigDecimal feeAmount = HwFFeeTypeUtil.getCalculatedFeeInPence(caseData);

        if (caseData.getHwfFeeType().equals(FeeType.APPLICATION)
                && feeAmount.compareTo(BigDecimal.ZERO) != 0) {
            log.info("HWF fee type is application for caseId: {}", caseData.getCcdCaseReference());
            Optional.ofNullable(caseData.getGaHwfDetails())
                .ifPresentOrElse(
                    gaHwfDetails -> updatedData.gaHwfDetails(
                        gaHwfDetails.toBuilder().remissionAmount(feeAmount)
                            .outstandingFee(BigDecimal.ZERO)
                            .hwfFeeType(FeeType.APPLICATION)
                            .hwfCaseEvent(FULL_REMISSION_HWF_GA)
                            .build()
                    ),
                    () -> updatedData.gaHwfDetails(
                        HelpWithFeesDetails.builder().remissionAmount(feeAmount)
                            .outstandingFee(BigDecimal.ZERO)
                            .hwfFeeType(FeeType.APPLICATION)
                            .hwfCaseEvent(FULL_REMISSION_HWF_GA)
                            .build()
                    )
                );
        } else if (caseData.getHwfFeeType().equals(FeeType.ADDITIONAL)
                && feeAmount.compareTo(BigDecimal.ZERO) != 0) {
            log.info("HWF fee type is additional for caseId: {}", caseData.getCcdCaseReference());
            Optional.ofNullable(caseData.getAdditionalHwfDetails())
                .ifPresentOrElse(
                    additionalHwfDetails -> updatedData.additionalHwfDetails(
                        additionalHwfDetails.toBuilder().remissionAmount(feeAmount)
                            .outstandingFee(BigDecimal.ZERO)
                            .hwfCaseEvent(FULL_REMISSION_HWF_GA)
                            .hwfFeeType(FeeType.ADDITIONAL)
                            .build()
                    ),
                    () -> updatedData.additionalHwfDetails(
                        HelpWithFeesDetails.builder().remissionAmount(feeAmount)
                            .outstandingFee(BigDecimal.ZERO)
                            .hwfCaseEvent(FULL_REMISSION_HWF_GA)
                            .hwfFeeType(FeeType.ADDITIONAL)
                            .build()
                    )
                );
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
                .data(updatedData.build().toMap(objectMapper))
                .build();
    }
}
