package uk.gov.hmcts.reform.unspec.handler.callback;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.unspec.callback.Callback;
import uk.gov.hmcts.reform.unspec.callback.CallbackHandler;
import uk.gov.hmcts.reform.unspec.callback.CallbackParams;
import uk.gov.hmcts.reform.unspec.callback.CaseEvent;
import uk.gov.hmcts.reform.unspec.enums.ServedDocuments;
import uk.gov.hmcts.reform.unspec.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.unspec.model.CaseData;
import uk.gov.hmcts.reform.unspec.model.ServiceMethod;
import uk.gov.hmcts.reform.unspec.model.common.Element;
import uk.gov.hmcts.reform.unspec.model.documents.CaseDocument;
import uk.gov.hmcts.reform.unspec.model.documents.DocumentType;
import uk.gov.hmcts.reform.unspec.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.unspec.service.docmosis.cos.CertificateOfServiceGenerator;
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
import static uk.gov.hmcts.reform.unspec.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.unspec.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.unspec.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.unspec.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.unspec.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.unspec.callback.CaseEvent.CONFIRM_SERVICE;
import static uk.gov.hmcts.reform.unspec.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.unspec.helpers.DateFormatHelper.DATE_TIME_AT;
import static uk.gov.hmcts.reform.unspec.helpers.DateFormatHelper.formatLocalDate;
import static uk.gov.hmcts.reform.unspec.helpers.DateFormatHelper.formatLocalDateTime;
import static uk.gov.hmcts.reform.unspec.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.unspec.utils.ElementUtils.unwrapElements;
import static uk.gov.hmcts.reform.unspec.utils.ElementUtils.wrapElements;

@Service
@RequiredArgsConstructor
public class ConfirmServiceCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(CONFIRM_SERVICE);

    public static final String CONFIRMATION_SUMMARY = "<br /> Deemed date of service: %s."
        + "<br />The defendant must respond before %s."
        + "\n\n[Download certificate of service](%s) (PDF, %s KB)";

    private final Validator validator;
    private final CertificateOfServiceGenerator certificateOfServiceGenerator;
    private final CaseDetailsConverter caseDetailsConverter;
    private final DeadlinesCalculator deadlinesCalculator;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_START), this::prepopulateServedDocuments,
            callbackKey(MID, "served-documents"), this::checkServedDocumentsOtherHasWhiteSpace,
            callbackKey(MID, "service-date"), this::validateServiceDate,
            callbackKey(ABOUT_TO_SUBMIT), this::prepareCertificateOfService,
            callbackKey(SUBMITTED), this::buildConfirmation
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

    private CallbackResponse validateServiceDate(CallbackParams callbackParams) {
        Map<String, Object> data = callbackParams.getRequest().getCaseDetails().getData();
        CaseData caseData = caseDetailsConverter.toCaseData(callbackParams.getRequest().getCaseDetails());
        List<String> errors = validator.validate(caseData, ConfirmServiceDateGroup.class).stream()
            .map(ConstraintViolation::getMessage)
            .collect(Collectors.toList());

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(data)
            .errors(errors)
            .build();
    }

    private CallbackResponse prepareCertificateOfService(CallbackParams callbackParams) {
        CaseData caseData = caseDetailsConverter.toCaseData(callbackParams.getRequest().getCaseDetails());
        ServiceMethod serviceMethod = caseData.getServiceMethodToRespondentSolicitor1();
        LocalDateTime serviceDate;
        if (serviceMethod.requiresDateEntry()) {
            serviceDate = caseData.getServiceDateToRespondentSolicitor1().atStartOfDay();
        } else {
            serviceDate = caseData.getServiceDateTimeToRespondentSolicitor1();
        }
        Map<String, Object> data = callbackParams.getRequest().getCaseDetails().getData();

        LocalDate deemedDateOfService = deadlinesCalculator.calculateDeemedDateOfService(
            serviceDate, serviceMethod.getType());
        LocalDateTime responseDeadline = deadlinesCalculator.calculateDefendantResponseDeadline(deemedDateOfService);

        data.put("deemedServiceDateToRespondentSolicitor1", deemedDateOfService);
        data.put("respondentSolicitor1ResponseDeadline", responseDeadline);

        CaseData caseDateUpdated = caseData.toBuilder()
            .deemedServiceDateToRespondentSolicitor1(deemedDateOfService)
            .respondentSolicitor1ResponseDeadline(responseDeadline)
            .build();

        CaseDocument certificateOfService = certificateOfServiceGenerator.generate(
            caseDateUpdated,
            callbackParams.getParams().get(BEARER_TOKEN).toString()
        );
        List<Element<CaseDocument>> systemGeneratedCaseDocuments = caseData.getSystemGeneratedCaseDocuments();
        if (ObjectUtils.isEmpty(systemGeneratedCaseDocuments)) {
            data.put("systemGeneratedCaseDocuments", wrapElements(certificateOfService));
        } else {
            systemGeneratedCaseDocuments.add(element(certificateOfService));
            data.put("systemGeneratedCaseDocuments", systemGeneratedCaseDocuments);
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(data)
            .build();
    }

    private SubmittedCallbackResponse buildConfirmation(CallbackParams callbackParams) {
        CaseData caseData = caseDetailsConverter.toCaseData(callbackParams.getRequest().getCaseDetails());

        LocalDate deemedDateOfService = caseData.getDeemedServiceDateToRespondentSolicitor1();
        String formattedDeemedDateOfService = formatLocalDate(deemedDateOfService, DATE);
        String responseDeadlineDate = formatLocalDateTime(
            caseData.getRespondentSolicitor1ResponseDeadline(),
            DATE_TIME_AT
        );
        Long documentSize = unwrapElements(caseData.getSystemGeneratedCaseDocuments()).stream()
            .filter(c -> c.getDocumentType() == DocumentType.CERTIFICATE_OF_SERVICE)
            .findFirst()
            .map(CaseDocument::getDocumentSize)
            .orElse(0L);

        String body = format(
            CONFIRMATION_SUMMARY,
            formattedDeemedDateOfService,
            responseDeadlineDate,
            format("/cases/case-details/%s#CaseDocuments", caseData.getCcdCaseReference()),
            documentSize / 1024
        );

        return SubmittedCallbackResponse.builder()
            .confirmationHeader("# You've confirmed service")
            .confirmationBody(body)
            .build();
    }
}
