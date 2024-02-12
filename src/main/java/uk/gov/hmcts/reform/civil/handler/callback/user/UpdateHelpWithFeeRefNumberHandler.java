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
import uk.gov.hmcts.reform.civil.enums.FeeType;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.HelpWithFeesDetails;

import java.util.List;
import java.util.Map;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.UPDATE_HELP_WITH_FEE_NUMBER;

@Service
@RequiredArgsConstructor
public class UpdateHelpWithFeeRefNumberHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(UPDATE_HELP_WITH_FEE_NUMBER);
    private final Map<String, Callback> callbackMap = Map.of(callbackKey(ABOUT_TO_SUBMIT), this::updateHwFReferenceNumber,
                                                            callbackKey(SUBMITTED), this::emptySubmittedCallbackResponse);
    private final ObjectMapper objectMapper;

    @Override
    protected Map<String, Callback> callbacks() {
        return callbackMap;
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse updateHwFReferenceNumber(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = updateHwFReference(caseData);

        return AboutToStartOrSubmitCallbackResponse.builder()
                .data(caseDataBuilder.build().toMap(objectMapper))
                .build();
    }

    private CaseData.CaseDataBuilder<?, ?> updateHwFReference(CaseData caseData) {
        if (FeeType.CLAIMISSUED == caseData.getHwfFeeType()) {
            ofNullable(caseData.getCaseDataLiP())
                    .map(CaseDataLiP::getHelpWithFees)
                    .ifPresent(hwf -> hwf.setHelpWithFeesReferenceNumber(getHwFNewReferenceNumber(caseData)));
            clearHwFReferenceNumber(caseData);
            return caseData.toBuilder();
        }
        if (FeeType.HEARING == caseData.getHwfFeeType()) {
            CaseData.CaseDataBuilder<?, ?> updatedData = caseData.toBuilder();
            updatedData.hearingHelpFeesReferenceNumber(getHwFNewReferenceNumber(caseData));
            clearHwFReferenceNumber(caseData);
            return updatedData;
        }
        return caseData.toBuilder();
    }

    private String getHwFNewReferenceNumber(CaseData caseData) {
        return ofNullable(caseData.getHwFeesDetails())
                .map(HelpWithFeesDetails::getHwfReferenceNumber).orElse(null);
    }

    private void clearHwFReferenceNumber(CaseData caseData) {
        if (ofNullable(caseData.getHwFeesDetails())
                .map(HelpWithFeesDetails::getHwfReferenceNumber).isPresent()) {
            HelpWithFeesDetails hwFeesDetails = caseData.getHwFeesDetails();
            hwFeesDetails.setHwfReferenceNumber(null);
            caseData.toBuilder().hwFeesDetails(hwFeesDetails);
        }
    }
}
