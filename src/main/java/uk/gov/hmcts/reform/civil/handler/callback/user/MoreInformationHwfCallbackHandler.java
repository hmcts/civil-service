package uk.gov.hmcts.reform.civil.handler.callback.user;

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
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.HelpWithFeesDetails;
import uk.gov.hmcts.reform.civil.model.citizenui.HelpWithFeesMoreInformation;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.MORE_INFORMATION_HWF;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_LIP_CLAIMANT_HWF_OUTCOME;

@Slf4j
@Service
@RequiredArgsConstructor
public class MoreInformationHwfCallbackHandler extends CallbackHandler {

    private static final String ERROR_MESSAGE_DOCUMENT_DATE_MUST_BE_AFTER_TODAY = "Documents date must be future date";

    private final ObjectMapper objectMapper;
    private final Map<String, Callback> callbackMap = Map.of(
        callbackKey(ABOUT_TO_START), this::emptyCallbackResponse,
        callbackKey(MID, "more-information-hwf"), this::validationMoreInformation,
        callbackKey(ABOUT_TO_SUBMIT), this::submitMoreInformationHwf,
        callbackKey(SUBMITTED), this::emptySubmittedCallbackResponse
    );

    @Override
    protected Map<String, Callback> callbacks() {
        return callbackMap;
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return Collections.singletonList(
           MORE_INFORMATION_HWF
        );
    }

    private CallbackResponse validationMoreInformation(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        List<String> errors = new ArrayList<>();
        HelpWithFeesMoreInformation moreInformationData =
            FeeType.HEARING == caseData.getHwfFeeType()
                ? caseData.getHelpWithFeesMoreInformationHearing()
                : caseData.getHelpWithFeesMoreInformationClaimIssue();
        LocalDate hwFMoreInfoDocumentDate = moreInformationData.getHwFMoreInfoDocumentDate();
        if (!hwFMoreInfoDocumentDate.isAfter(LocalDate.now())) {
            errors.add(ERROR_MESSAGE_DOCUMENT_DATE_MUST_BE_AFTER_TODAY);
        }
        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .build();
    }

    private CallbackResponse submitMoreInformationHwf(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder<?, ?> updatedData = caseData.toBuilder()
            .businessProcess(BusinessProcess.ready(NOTIFY_LIP_CLAIMANT_HWF_OUTCOME));

        if (caseData.isHWFTypeHearing()) {
            HelpWithFeesDetails hearingFeeDetails =
                Optional.ofNullable(caseData.getHearingHwfDetails()).orElse(new HelpWithFeesDetails());
            updatedData.hearingHwfDetails(hearingFeeDetails.toBuilder().hwfCaseEvent(MORE_INFORMATION_HWF).build());
        }
        if (caseData.isHWFTypeClaimIssued()) {
            HelpWithFeesDetails claimIssuedHwfDetails =
                Optional.ofNullable(caseData.getClaimIssuedHwfDetails()).orElse(new HelpWithFeesDetails());
            updatedData.claimIssuedHwfDetails(claimIssuedHwfDetails.toBuilder().hwfCaseEvent(MORE_INFORMATION_HWF).build());

        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedData.build().toMap(objectMapper))
            .build();
    }
}
