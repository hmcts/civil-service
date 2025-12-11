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
import uk.gov.hmcts.reform.civil.model.citizenui.HelpWithFees;
import uk.gov.hmcts.reform.civil.model.citizenui.HelpWithFeesDetails;
import uk.gov.hmcts.reform.civil.service.citizenui.HelpWithFeesForTabService;

import java.util.List;
import java.util.Map;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_LIP_CLAIMANT_HWF_OUTCOME;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.UPDATE_HELP_WITH_FEE_NUMBER;

@Service
@RequiredArgsConstructor
public class UpdateHelpWithFeeRefNumberHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(UPDATE_HELP_WITH_FEE_NUMBER);
    private final Map<String, Callback> callbackMap = Map.of(callbackKey(ABOUT_TO_START), this::emptyCallbackResponse,
                                                            callbackKey(ABOUT_TO_SUBMIT), this::updateHwFReferenceNumber,
                                                            callbackKey(SUBMITTED), this::emptySubmittedCallbackResponse);
    private final ObjectMapper objectMapper;
    private final HelpWithFeesForTabService helpWithFeesForTabService;

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
        CaseData updatedCaseData = setUpBusinessProcess(caseData);
        updateHwFReference(updatedCaseData);
        return AboutToStartOrSubmitCallbackResponse.builder()
                .data(updatedCaseData.toMap(objectMapper))
                .build();
    }

    private CaseData setUpBusinessProcess(CaseData caseData) {
        caseData.setBusinessProcess(BusinessProcess.ready(NOTIFY_LIP_CLAIMANT_HWF_OUTCOME));

        if (caseData.isHWFTypeHearing()) {
            HelpWithFeesDetails hearingFeeDetails = ofNullable(caseData.getHearingHwfDetails())
                .orElseGet(HelpWithFeesDetails::new);
            hearingFeeDetails.setHwfCaseEvent(UPDATE_HELP_WITH_FEE_NUMBER);
            caseData.setHearingHwfDetails(hearingFeeDetails);
        }
        if (caseData.isHWFTypeClaimIssued()) {
            HelpWithFeesDetails claimIssuedHwfDetails = ofNullable(caseData.getClaimIssuedHwfDetails())
                .orElseGet(HelpWithFeesDetails::new);
            claimIssuedHwfDetails.setHwfCaseEvent(UPDATE_HELP_WITH_FEE_NUMBER);
            caseData.setClaimIssuedHwfDetails(claimIssuedHwfDetails);
        }
        return caseData;
    }

    private CaseData updateHwFReference(CaseData caseData) {
        if (caseData.isHWFTypeClaimIssued()) {
            ofNullable(caseData.getCaseDataLiP())
                .ifPresent(caseDataLip -> {
                    HelpWithFees helpWithFees = ofNullable(caseDataLip.getHelpWithFees())
                        .orElseGet(HelpWithFees::new);
                    helpWithFees.setHelpWithFeesReferenceNumber(
                        getHwFNewReferenceNumber(caseData.getClaimIssuedHwfDetails()));
                    caseDataLip.setHelpWithFees(helpWithFees);
                    caseData.setCaseDataLiP(caseDataLip);
                });
            if (caseData.getClaimIssuedHwfDetails() != null) {
                caseData.getClaimIssuedHwfDetails().setHwfReferenceNumber(null);
            }
            helpWithFeesForTabService.setUpHelpWithFeeTab(caseData);
            return caseData;
        }
        if (caseData.isHWFTypeHearing()) {
            caseData.setHearingHelpFeesReferenceNumber(getHwFNewReferenceNumber(caseData.getHearingHwfDetails()));
            if (caseData.getHearingHwfDetails() != null) {
                caseData.getHearingHwfDetails().setHwfReferenceNumber(null);
            }
            helpWithFeesForTabService.setUpHelpWithFeeTab(caseData);
            return caseData;
        }
        return caseData;
    }

    private String getHwFNewReferenceNumber(HelpWithFeesDetails  helpWithFeesDetails) {
        return ofNullable(helpWithFeesDetails)
                .map(HelpWithFeesDetails::getHwfReferenceNumber).orElse(null);
    }
}
