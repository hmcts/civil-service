package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
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
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.citizen.HWFFeePaymentOutcomeService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CITIZEN_HEARING_FEE_PAYMENT;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeePaymentOutcomeHWFCallBackHandler extends CallbackHandler {

    private final ObjectMapper objectMapper;
    private final HWFFeePaymentOutcomeService hwfFeePaymentOutcomeService;
    public static final String WRONG_REMISSION_TYPE_SELECTED = "Incorrect remission type selected";

    @Override
    protected Map<String, Callback> callbacks() {
        return new ImmutableMap.Builder<String, Callback>()
            .put(callbackKey(ABOUT_TO_START), this::emptyCallbackResponse)
            .put(callbackKey(ABOUT_TO_SUBMIT), this::submitFeePaymentOutcome)
            .put(callbackKey(MID, "remission-type"), this::validateSelectedRemissionType)
            .put(callbackKey(SUBMITTED), this::emptySubmittedCallbackResponse)
            .build();
    }

    private CallbackResponse validateSelectedRemissionType(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        var errors = new ArrayList<String>();

        if ((caseData.isHWFTypeClaimIssued()
            && caseData.getFeePaymentOutcomeDetails().getHwfFullRemissionGrantedForClaimIssue() == YesOrNo.YES
            && Objects.nonNull(caseData.getClaimIssuedHwfDetails())
            && Objects.nonNull(caseData.getClaimIssuedHwfDetails().getOutstandingFeeInPounds())
            && !Objects.equals(caseData.getClaimIssuedHwfDetails().getOutstandingFeeInPounds(), BigDecimal.ZERO))
            || (caseData.isHWFTypeHearing()
            && caseData.getFeePaymentOutcomeDetails().getHwfFullRemissionGrantedForHearingFee() == YesOrNo.YES
            && Objects.nonNull(caseData.getHearingHwfDetails())
            && Objects.nonNull(caseData.getHearingHwfDetails().getOutstandingFeeInPounds())
            && !Objects.equals(caseData.getHearingHwfDetails().getOutstandingFeeInPounds(), BigDecimal.ZERO))) {
            errors.add(WRONG_REMISSION_TYPE_SELECTED);
        }
        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .build();
    }

    private CallbackResponse submitFeePaymentOutcome(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();

        if (caseData.isHWFTypeClaimIssued()) {
            LocalDate issueDate = LocalDate.now();
            caseDataBuilder.issueDate(issueDate).build();
            caseDataBuilder.businessProcess(BusinessProcess.ready(CaseEvent.CREATE_CLAIM_SPEC_AFTER_PAYMENT));
        } else if (caseData.isHWFTypeHearing()) {
            caseDataBuilder.businessProcess(BusinessProcess.ready(CITIZEN_HEARING_FEE_PAYMENT));
        }
        caseData = caseDataBuilder.build();
        caseData = hwfFeePaymentOutcomeService.updateHwfReferenceNumber(caseData);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseData.toMap(objectMapper))
            .build();
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return Collections.singletonList(CaseEvent.FEE_PAYMENT_OUTCOME);
    }
}
