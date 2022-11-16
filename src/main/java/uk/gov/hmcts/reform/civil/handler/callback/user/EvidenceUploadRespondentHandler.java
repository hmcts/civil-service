package uk.gov.hmcts.reform.civil.handler.callback.user;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

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
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.service.Time;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.EVIDENCE_UPLOAD_RESPONDENT;

@Service
@RequiredArgsConstructor
public class EvidenceUploadRespondentHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(EVIDENCE_UPLOAD_RESPONDENT);
    private final ObjectMapper objectMapper;
    private final Time time;

    @Override
    protected Map<String, Callback> callbacks() {
        return new ImmutableMap.Builder<String, Callback>()
            .put(callbackKey(ABOUT_TO_START), this::emptyCallbackResponse)
            .put(callbackKey(MID, "validateValuesRespondent"), this::validateValuesRespondent)
            .put(callbackKey(ABOUT_TO_SUBMIT), this::documentUploadTimeRespondent)
            .put(callbackKey(SUBMITTED), this::buildConfirmation)
            .build();
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse validateValuesRespondent(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        List<String> errors = new ArrayList<>();

        respondentCheckDateCorrectness(errors, caseData.getDocumentUploadWitness1Res(), date -> date.getValue()
                                 .getWitnessOption1UploadDate(),
                             "Invalid date: \"witness statement\" "
                                 + "date entered must not be in the future (1).");
        respondentCheckDateCorrectness(errors, caseData.getDocumentUploadWitness3Res(), date -> date.getValue()
                                 .getWitnessOption3UploadDate(),
                             "Invalid date: \"Notice of the intention to rely on hearsay evidence\" "
                                 + "date entered must not be in the future (2).");

        respondentCheckDateCorrectness(errors, caseData.getDocumentUploadExpert1Res(), date -> date.getValue()
                                 .getExpertOption1UploadDate(),
                             "Invalid date: \"Expert's report\""
                                 + " date entered must not be in the future (3).");
        respondentCheckDateCorrectness(errors, caseData.getDocumentUploadExpert2Res(), date -> date.getValue()
                                 .getExpertOption2UploadDate(),
                             "Invalid date: \"Joint statement of experts\" "
                                 + "date entered must not be in the future (4).");
        respondentCheckDateCorrectness(errors, caseData.getDocumentUploadExpert3Res(), date -> date.getValue()
                                 .getExpertOption3UploadDate(),
                             "Invalid date: \"Questions for other party's expert or joint experts\" "
                                 + "expert statement date entered must not be in the future (5).");
        respondentCheckDateCorrectness(errors, caseData.getDocumentUploadExpert4Res(), date -> date.getValue()
                                 .getExpertOption4UploadDate(),
                             "Invalid date: \"Answers to questions asked by the other party\" "
                                 + "date entered must not be in the future (6).");

        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .build();
    }

    <T> void respondentCheckDateCorrectness(List<String> errors, List<Element<T>> documentUploadRespondent,
                                  Function<Element<T>, LocalDate> dateExtractor, String errorMessage) {
        if (documentUploadRespondent == null) {
            return;
        }
        documentUploadRespondent.forEach(date -> {
            LocalDate dateToCheckRespondent = dateExtractor.apply(date);
            if (dateToCheckRespondent.isAfter(time.now().toLocalDate())) {
                errors.add(errorMessage);
            }
        });
    }

    private CallbackResponse documentUploadTimeRespondent(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();
        caseDataBuilder.caseDocumentUploadDateRes(time.now());
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

