package uk.gov.hmcts.reform.unspec.handler.callback;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.unspec.callback.Callback;
import uk.gov.hmcts.reform.unspec.callback.CallbackHandler;
import uk.gov.hmcts.reform.unspec.callback.CallbackParams;
import uk.gov.hmcts.reform.unspec.callback.CaseEvent;
import uk.gov.hmcts.reform.unspec.enums.ServedDocuments;
import uk.gov.hmcts.reform.unspec.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.unspec.model.BusinessProcess;
import uk.gov.hmcts.reform.unspec.model.CaseData;
import uk.gov.hmcts.reform.unspec.model.ServiceMethod;
import uk.gov.hmcts.reform.unspec.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.unspec.validation.groups.ConfirmServiceDateGroup;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import static java.lang.String.format;
import static uk.gov.hmcts.reform.unspec.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.unspec.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.unspec.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.unspec.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.unspec.callback.CaseEvent.CONFIRM_SERVICE;
import static uk.gov.hmcts.reform.unspec.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.unspec.helpers.DateFormatHelper.DATE_TIME_AT;
import static uk.gov.hmcts.reform.unspec.helpers.DateFormatHelper.formatLocalDate;
import static uk.gov.hmcts.reform.unspec.helpers.DateFormatHelper.formatLocalDateTime;

@Service
@RequiredArgsConstructor
public class ConfirmServiceCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(CONFIRM_SERVICE);

    public static final String CONFIRMATION_SUMMARY = "<br /> Deemed date of service: %s."
        + "<br />The defendant must respond before %s."
        + "\n\n[Download certificate of service](%s)";

    private final Validator validator;
    private final DeadlinesCalculator deadlinesCalculator;
    private final CaseDetailsConverter caseDetailsConverter;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_START), this::prepopulateServedDocuments,
            callbackKey(MID, "served-documents"), this::checkServedDocumentsOtherHasWhiteSpace,
            callbackKey(MID, "service-date"), this::validateServiceDate,
            callbackKey(ABOUT_TO_SUBMIT), this::calculateServiceDates,
            callbackKey(SUBMITTED), this::buildConfirmation
        );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse prepopulateServedDocuments(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData().toBuilder()
            .servedDocuments(List.of(ServedDocuments.CLAIM_FORM))
            .build();

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetailsConverter.toMap(caseData))
            .build();
    }

    private CallbackResponse checkServedDocumentsOtherHasWhiteSpace(CallbackParams callbackParams) {
        List<String> errors = new ArrayList<>();
        String servedDocumentsOther = callbackParams.getCaseData().getServedDocumentsOther();

        if (servedDocumentsOther != null && servedDocumentsOther.isBlank()) {
            errors.add("CONTENT TBC: please enter a valid value for other documents");
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .build();
    }

    private CallbackResponse validateServiceDate(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        List<String> errors = validator.validate(caseData, ConfirmServiceDateGroup.class).stream()
            .map(ConstraintViolation::getMessage)
            .collect(Collectors.toList());

        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .build();
    }

    private CallbackResponse calculateServiceDates(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        ServiceMethod serviceMethod = caseData.getServiceMethodToRespondentSolicitor1();
        LocalDateTime serviceDate = caseData.getServiceDateTimeToRespondentSolicitor1();;
        if (serviceMethod.requiresDateEntry()) {
            serviceDate = caseData.getServiceDateToRespondentSolicitor1().atStartOfDay();
        }
        LocalDate deemedDateOfService = deadlinesCalculator.calculateDeemedDateOfService(
            serviceDate, serviceMethod.getType());
        LocalDateTime responseDeadline = deadlinesCalculator.calculateDefendantResponseDeadline(deemedDateOfService);

        CaseData caseDataUpdated = caseData.toBuilder()
            .deemedServiceDateToRespondentSolicitor1(deemedDateOfService)
            .respondentSolicitor1ResponseDeadline(responseDeadline)
            .businessProcess(BusinessProcess.ready(CONFIRM_SERVICE))
            .build();

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetailsConverter.toMap(caseDataUpdated))
            .build();
    }

    private SubmittedCallbackResponse buildConfirmation(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();

        LocalDate deemedDateOfService = caseData.getDeemedServiceDateToRespondentSolicitor1();
        String formattedDeemedDateOfService = formatLocalDate(deemedDateOfService, DATE);
        String responseDeadlineDate = formatLocalDateTime(
            caseData.getRespondentSolicitor1ResponseDeadline(),
            DATE_TIME_AT
        );

        String body = format(
            CONFIRMATION_SUMMARY,
            formattedDeemedDateOfService,
            responseDeadlineDate,
            format("/cases/case-details/%s#CaseDocuments", caseData.getCcdCaseReference())
        );

        return SubmittedCallbackResponse.builder()
            .confirmationHeader("# You've confirmed service")
            .confirmationBody(body)
            .build();
    }
}
