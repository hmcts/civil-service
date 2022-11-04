package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceExpert;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceWitness;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.EVIDENCE_UPLOAD;

@Service
@RequiredArgsConstructor
public class EvidenceUploadHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(EVIDENCE_UPLOAD);
    private final ObjectMapper objectMapper;

    @Override
    protected Map<String, Callback> callbacks() {
        return new ImmutableMap.Builder<String, Callback>()
            .put(callbackKey(ABOUT_TO_START), this::emptyCallbackResponse)
            .put(callbackKey(MID, "validateValues"), this::validateValues)
            .put(callbackKey(ABOUT_TO_SUBMIT), this::documentUploadTime)
            .put(callbackKey(SUBMITTED), this::buildConfirmation)
            .build();
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse validateValues(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        List<String> errors = new ArrayList<>();

        if (caseData.getDocumentUploadWitness1() != null) {
            List<Element<UploadEvidenceWitness>> dateList = caseData.getDocumentUploadWitness1();
            dateList.forEach(date -> {
                if (date.getValue().getWitnessOption1UploadDate().isAfter(LocalDate.now())) {
                    errors.add("Invalid date: date entered must not be in the future.");
                }
            });
        }

        if (caseData.getDocumentUploadWitness3() != null) {
            List<Element<UploadEvidenceWitness>> dateList = caseData.getDocumentUploadWitness3();
            dateList.forEach(date -> {
                if (date.getValue().getWitnessOption3UploadDate().isAfter(LocalDate.now())) {
                    errors.add("Invalid date: date entered must not be in the future.");
                }
            });
        }

        if (caseData.getDocumentUploadExpert1() != null) {
            List<Element<UploadEvidenceExpert>> dateList = caseData.getDocumentUploadExpert1();
            dateList.forEach(date -> {
                if (date.getValue().getExpertOption1UploadDate().isAfter(LocalDate.now())) {
                    errors.add("Invalid date: date entered must not be in the future.");
                }
            });
        }

        if (caseData.getDocumentUploadExpert2() != null) {
            List<Element<UploadEvidenceExpert>> dateList = caseData.getDocumentUploadExpert2();
            dateList.forEach(date -> {
                if (date.getValue().getExpertOption2UploadDate().isAfter(LocalDate.now())) {
                    errors.add("Invalid date: date entered must not be in the future.");
                }
            });
        }

        if (caseData.getDocumentUploadExpert3() != null) {
            List<Element<UploadEvidenceExpert>> dateList = caseData.getDocumentUploadExpert3();
            dateList.forEach(date -> {
                if (date.getValue().getExpertOption3UploadDate().isAfter(LocalDate.now())) {
                    errors.add("Invalid date: date entered must not be in the future.");
                }
            });
        }

        if (caseData.getDocumentUploadExpert4() != null) {
            List<Element<UploadEvidenceExpert>> dateList = caseData.getDocumentUploadExpert4();
            dateList.forEach(date -> {
                if (date.getValue().getExpertOption4UploadDate().isAfter(LocalDate.now())) {
                    errors.add("Invalid date: date entered must not be in the future.");
                }
            });
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .build();
    }

    private CallbackResponse documentUploadTime(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder caseDataBuilder = caseData.toBuilder();
        caseDataBuilder.caseDocumentUploadDate(LocalDateTime.now());
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }

    private SubmittedCallbackResponse buildConfirmation(CallbackParams callbackParams) {
        return SubmittedCallbackResponse.builder()
            .confirmationHeader("# Documents uploaded")
            .confirmationBody("You can continue uploading documents or return later. To upload more "
                                  + "documents, go to Next step and select \"Document Upload\".")
            .build();
    }

}

