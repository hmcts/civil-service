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
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
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
            HelpWithFeesDetails hearingFeeDetails = caseData.getHearingHwfDetails();
            caseData.setHearingHwfDetails(hearingFeeDetails.toBuilder().hwfCaseEvent(UPDATE_HELP_WITH_FEE_NUMBER).build());
        }
        if (caseData.isHWFTypeClaimIssued()) {
            HelpWithFeesDetails claimIssuedHwfDetails = caseData.getClaimIssuedHwfDetails();
            caseData.setClaimIssuedHwfDetails(claimIssuedHwfDetails.toBuilder().hwfCaseEvent(UPDATE_HELP_WITH_FEE_NUMBER).build());
        }
        return caseData;
    }

    private CaseData updateHwFReference(CaseData caseData) {
        if (caseData.isHWFTypeClaimIssued()) {
            ofNullable(caseData.getCaseDataLiP())
                    .map(CaseDataLiP::getHelpWithFees)
                    .ifPresent(hwf -> {
                        var caseDataLip = caseData.getCaseDataLiP();
                        caseData.setCaseDataLiP(caseDataLip.toBuilder().helpWithFees(
                            hwf.toBuilder().helpWithFeesReferenceNumber(
                                getHwFNewReferenceNumber(caseData.getClaimIssuedHwfDetails()))
                                .build()).build());
                    });
            if (caseData.getClaimIssuedHwfDetails() != null) {
                caseData.setClaimIssuedHwfDetails(caseData.getClaimIssuedHwfDetails().toBuilder().hwfReferenceNumber(null).build());
            }
            helpWithFeesForTabService.setUpHelpWithFeeTab(caseData);
            return caseData;
        }
        if (caseData.isHWFTypeHearing()) {
            caseData.setHearingHelpFeesReferenceNumber(getHwFNewReferenceNumber(caseData.getHearingHwfDetails()));
            if (caseData.getHearingHwfDetails() != null) {
                caseData.setHearingHwfDetails(caseData.getHearingHwfDetails().toBuilder().hwfReferenceNumber(null).build());
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
