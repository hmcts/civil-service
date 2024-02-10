package uk.gov.hmcts.reform.civil.handler.callback.user;

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
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;

@Slf4j
@Service
@RequiredArgsConstructor
public class MoreInformationHwfCallbackHandler extends CallbackHandler {

    private static final String ERROR_MESSAGE_DOCUMENT_DATE_MUST_BE_AFTER_TODAY = "Documents date must be future date";

    @Override
    protected Map<String, Callback> callbacks() {
        return new ImmutableMap.Builder<String, Callback>()
            .put(callbackKey(ABOUT_TO_START), this::emptyCallbackResponse)
            .put(callbackKey(MID, "more-information-hwf"), this::validationMoreInformation)
            .put(callbackKey(ABOUT_TO_SUBMIT), this::submitMoreInformationHwf)
            .put(callbackKey(SUBMITTED), this::emptySubmittedCallbackResponse)
            .build();
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return Collections.singletonList(
            CaseEvent.MORE_INFORMATION_HWF
        );
    }

    private CallbackResponse validationMoreInformation(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        List<String> errors = new ArrayList<>();
        LocalDate hwFMoreInfoDocumentDate = caseData.getHwFeesDetails().getHelpWithFeesMoreInformation().getHwFMoreInfoDocumentDate();
        if (!hwFMoreInfoDocumentDate.isAfter(LocalDate.now())) {
            errors.add(ERROR_MESSAGE_DOCUMENT_DATE_MUST_BE_AFTER_TODAY);
        }
        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .build();
    }

    private CallbackResponse submitMoreInformationHwf(CallbackParams callbackParams) {
        return AboutToStartOrSubmitCallbackResponse.builder()
            .build();
    }
}
