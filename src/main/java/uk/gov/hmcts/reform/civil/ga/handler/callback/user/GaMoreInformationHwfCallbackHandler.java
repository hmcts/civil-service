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
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.citizenui.HelpWithFeesMoreInformation;
import uk.gov.hmcts.reform.civil.ga.utils.HwFFeeTypeUtil;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.MORE_INFORMATION_HWF_GA;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_APPLICANT_LIP_HWF;

@Slf4j
@Service
@RequiredArgsConstructor
public class GaMoreInformationHwfCallbackHandler extends CallbackHandler implements GeneralApplicationCallbackHandler {

    private static final String ERROR_MESSAGE_DOCUMENT_DATE_MUST_BE_AFTER_TODAY = "Documents date must be future date";
    public static final List<CaseEvent> EVENTS = List.of(MORE_INFORMATION_HWF_GA);

    private final ObjectMapper objectMapper;

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_START), this::emptyCallbackResponse,
            callbackKey(MID, "more-information-hwf"), this::validationMoreInformation,
            callbackKey(ABOUT_TO_SUBMIT), this::submitMoreInformationHwf,
            callbackKey(SUBMITTED), this::emptySubmittedCallbackResponse
        );
    }

    private CallbackResponse validationMoreInformation(CallbackParams callbackParams) {
        GeneralApplicationCaseData caseData = callbackParams.getGeneralApplicationCaseData();
        List<String> errors = new ArrayList<>();
        HelpWithFeesMoreInformation moreInformationData =
            FeeType.ADDITIONAL == caseData.getHwfFeeType()
                ? caseData.getHelpWithFeesMoreInformationAdditional()
                : caseData.getHelpWithFeesMoreInformationGa();
        LocalDate hwFMoreInfoDocumentDate = moreInformationData.getHwFMoreInfoDocumentDate();
        if (!hwFMoreInfoDocumentDate.isAfter(LocalDate.now())) {
            errors.add(ERROR_MESSAGE_DOCUMENT_DATE_MUST_BE_AFTER_TODAY);
        }
        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .build();
    }

    private CallbackResponse submitMoreInformationHwf(CallbackParams callbackParams) {
        GeneralApplicationCaseData caseData = callbackParams.getGeneralApplicationCaseData();
        GeneralApplicationCaseData updatedData = caseData.copy()
                .businessProcess(BusinessProcess.readyGa(NOTIFY_APPLICANT_LIP_HWF));

        HwFFeeTypeUtil.updateEventInHwfDetails(caseData, updatedData, MORE_INFORMATION_HWF_GA);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedData.build().toMap(objectMapper))
            .build();
    }
}
