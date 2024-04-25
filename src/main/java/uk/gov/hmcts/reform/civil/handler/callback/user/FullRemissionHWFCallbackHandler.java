package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.HelpWithFeesDetails;
import uk.gov.hmcts.reform.civil.service.citizenui.HelpWithFeesForTabService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.FULL_REMISSION_HWF;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_LIP_CLAIMANT_HWF_OUTCOME;

@Service
@RequiredArgsConstructor
public class FullRemissionHWFCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(FULL_REMISSION_HWF);
    private final ObjectMapper objectMapper;
    private final Map<String, Callback> callbackMap = Map.of(
        callbackKey(ABOUT_TO_START), this::emptyCallbackResponse,
        callbackKey(ABOUT_TO_SUBMIT),
        this::fullRemissionHWF,
        callbackKey(SUBMITTED), this::emptySubmittedCallbackResponse
    );
    private final HelpWithFeesForTabService helpWithFeesForTabService;

    @Override
    protected Map<String, Callback> callbacks() {
        return callbackMap;
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse fullRemissionHWF(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder<?, ?> updatedData = caseData.toBuilder()
            .businessProcess(BusinessProcess.ready(NOTIFY_LIP_CLAIMANT_HWF_OUTCOME));
        BigDecimal claimFeeAmount = caseData.getCalculatedClaimFeeInPence();
        BigDecimal hearingFeeAmount = caseData.getCalculatedHearingFeeInPence();

        if (caseData.isHWFTypeClaimIssued() && claimFeeAmount.compareTo(BigDecimal.ZERO) != 0) {
            Optional.ofNullable(caseData.getClaimIssuedHwfDetails())
                .ifPresentOrElse(
                    claimIssuedHwfDetails -> updatedData.claimIssuedHwfDetails(
                        claimIssuedHwfDetails.toBuilder().remissionAmount(claimFeeAmount)
                            .outstandingFeeInPounds(BigDecimal.ZERO)
                            .hwfCaseEvent(FULL_REMISSION_HWF)
                            .build()
                    ),
                    () -> updatedData.claimIssuedHwfDetails(
                        HelpWithFeesDetails.builder().remissionAmount(claimFeeAmount)
                            .outstandingFeeInPounds(BigDecimal.ZERO)
                            .hwfCaseEvent(FULL_REMISSION_HWF)
                            .build()
                    )
                );
        } else if (caseData.isHWFTypeHearing() && hearingFeeAmount.compareTo(BigDecimal.ZERO) != 0) {
            Optional.ofNullable(caseData.getHearingHwfDetails())
                .ifPresentOrElse(
                    hearingHwfDetails -> updatedData.hearingHwfDetails(
                        HelpWithFeesDetails.builder().remissionAmount(hearingFeeAmount)
                            .outstandingFeeInPounds(BigDecimal.ZERO)
                            .hwfCaseEvent(FULL_REMISSION_HWF)
                            .build()
                    ),
                    () -> updatedData.hearingHwfDetails(
                        HelpWithFeesDetails.builder().remissionAmount(hearingFeeAmount)
                            .hwfCaseEvent(FULL_REMISSION_HWF)
                            .build()
                    )
                );
        }
        helpWithFeesForTabService.setUpHelpWithFeeTab(updatedData);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedData.build().toMap(objectMapper))
            .build();
    }
}
