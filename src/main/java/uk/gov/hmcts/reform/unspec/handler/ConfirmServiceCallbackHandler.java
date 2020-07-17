package uk.gov.hmcts.reform.unspec.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.unspec.callback.Callback;
import uk.gov.hmcts.reform.unspec.callback.CallbackHandler;
import uk.gov.hmcts.reform.unspec.callback.CallbackParams;
import uk.gov.hmcts.reform.unspec.callback.CallbackType;
import uk.gov.hmcts.reform.unspec.callback.CaseEvent;
import uk.gov.hmcts.reform.unspec.enums.ServedDocuments;
import uk.gov.hmcts.reform.unspec.enums.ServiceMethod;
import uk.gov.hmcts.reform.unspec.model.CaseData;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static uk.gov.hmcts.reform.unspec.callback.CaseEvent.CONFIRM_SERVICE;
import static uk.gov.hmcts.reform.unspec.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.unspec.helpers.DateFormatHelper.DATE_TIME_AT;
import static uk.gov.hmcts.reform.unspec.helpers.DateFormatHelper.formatLocalDate;
import static uk.gov.hmcts.reform.unspec.helpers.DateFormatHelper.formatLocalDateTime;

@Service
@RequiredArgsConstructor
public class ConfirmServiceCallbackHandler extends CallbackHandler {
    private static final List<CaseEvent> EVENTS = Collections.singletonList(CONFIRM_SERVICE);

    private final ObjectMapper mapper;

    @Override
    protected Map<CallbackType, Callback> callbacks() {
        return Map.of(
            CallbackType.ABOUT_TO_START, this::prepopulateServedDocuments,
            CallbackType.MID, this::checkServedDocumentsOtherHasWhiteSpace,
            CallbackType.ABOUT_TO_SUBMIT, this::addResponseDatesToCase,
            CallbackType.SUBMITTED, this::buildConfirmation
        );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse prepopulateServedDocuments(CallbackParams callbackParams) {
        List<ServedDocuments> servedDocuments = List.of(ServedDocuments.CLAIM_FORM);

        Map<String, Object> data = callbackParams.getRequest().getCaseDetails().getData();
        data.put("servedDocuments", servedDocuments);

        return AboutToStartOrSubmitCallbackResponse.builder()
                   .data(data)
                   .build();
    }

    private CallbackResponse checkServedDocumentsOtherHasWhiteSpace(CallbackParams callbackParams) {
        Map<String, Object> data = callbackParams.getRequest().getCaseDetails().getData();
        List<String> errors = new ArrayList<>();
        var servedDocumentsOther = data.get("servedDocumentsOther");

        if (servedDocumentsOther != null && servedDocumentsOther.toString().isBlank()) {
            errors.add("CONTENT TBC: please enter a valid value for other documents");
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
                   .data(data)
                   .errors(errors)
                   .build();
    }

    private CallbackResponse addResponseDatesToCase(CallbackParams callbackParams) {
        Map<String, Object> data = callbackParams.getRequest().getCaseDetails().getData();
        CaseData caseData = mapper.convertValue(data, CaseData.class);

        ServiceMethod serviceMethod = caseData.getServiceMethod();

        // TODO: this field will be different (date / date time) in CCD depending on service method.
        LocalDate serviceDate = caseData.getServiceDate();

        LocalDate deemedDateOfService = serviceMethod.getDeemedDateOfService(serviceDate);
        LocalDateTime responseDeadline = addFourteenDays(deemedDateOfService);

        data.put("deemedDateOfService", deemedDateOfService);
        data.put("responseDeadline", responseDeadline);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(data)
            .build();
    }

    private SubmittedCallbackResponse buildConfirmation(CallbackParams callbackParams) {
        Map<String, Object> data = callbackParams.getRequest().getCaseDetails().getData();
        CaseData caseData = mapper.convertValue(data, CaseData.class);

        LocalDate deemedDateOfService = caseData.getDeemedDateOfService();
        String formattedDeemedDateOfService = formatLocalDate(deemedDateOfService, DATE);
        String responseDeadlineDate = formatLocalDateTime(addFourteenDays(deemedDateOfService), DATE_TIME_AT);
        String certificateOfServiceLink = "http://www.google.com";

        String body = format("<br /> Deemed date of service: %s."
                                 + "<br />The defendant must respond before %s."
                                 + "\n\n[Download certificate of service](%s) (PDF, 266 KB)",
                             formattedDeemedDateOfService, responseDeadlineDate, certificateOfServiceLink
        );

        return SubmittedCallbackResponse.builder()
            .confirmationHeader("# You've confirmed service")
            .confirmationBody(body)
            .build();
    }

    private LocalDateTime addFourteenDays(LocalDate deemedDateOfService) {
        return deemedDateOfService.plusDays(14).atTime(16, 0);
    }
}
