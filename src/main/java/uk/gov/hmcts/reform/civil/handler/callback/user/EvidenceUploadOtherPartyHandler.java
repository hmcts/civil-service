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
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.service.Time;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.EVIDENCE_UPLOAD_OTHER_PARTY;

@Service
@RequiredArgsConstructor
public class EvidenceUploadOtherPartyHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(EVIDENCE_UPLOAD_OTHER_PARTY);
    private final ObjectMapper objectMapper;

    private final Time time;

    @Override
    protected Map<String, Callback> callbacks() {
        return new ImmutableMap.Builder<String, Callback>()
            .put(callbackKey(ABOUT_TO_START), this::emptyCallbackResponse)
            .put(callbackKey(MID, "validateValuesOtherParty"), this::validateValuesOtherParty)
            .put(callbackKey(ABOUT_TO_SUBMIT), this::documentUploadTimeOtherParty)
            .put(callbackKey(SUBMITTED), this::buildConfirmation)
            .build();
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse validateValuesOtherParty(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        List<String> errors = new ArrayList<>();

        otherPartyCheckDateCorrectness(errors, caseData.getDocumentUploadWitness1Other(), date -> date.getValue()
                                 .getWitnessOption1UploadDate(),
                             "Invalid date: \"witness statement\" "
                                 + "date entered must not be in the future (1).");
        otherPartyCheckDateCorrectness(errors, caseData.getDocumentUploadWitness3Other(), date -> date.getValue()
                                 .getWitnessOption3UploadDate(),
                             "Invalid date: \"Notice of the intention to rely on hearsay evidence\" "
                                 + "date entered must not be in the future (2).");

        otherPartyCheckDateCorrectness(errors, caseData.getDocumentUploadExpert1Other(), date -> date.getValue()
                                 .getExpertOption1UploadDate(),
                             "Invalid date: \"Expert's report\""
                                 + " date entered must not be in the future (3).");
        otherPartyCheckDateCorrectness(errors, caseData.getDocumentUploadExpert2Other(), date -> date.getValue()
                                 .getExpertOption2UploadDate(),
                             "Invalid date: \"Joint statement of experts\" "
                                 + "date entered must not be in the future (4).");
        otherPartyCheckDateCorrectness(errors, caseData.getDocumentUploadExpert3Other(), date -> date.getValue()
                                 .getExpertOption3UploadDate(),
                             "Invalid date: \"Questions for other party's expert or joint experts\" "
                                 + "expert statement date entered must not be in the future (5).");
        otherPartyCheckDateCorrectness(errors, caseData.getDocumentUploadExpert4Other(), date -> date.getValue()
                                 .getExpertOption4UploadDate(),
                             "Invalid date: \"Answers to questions asked by the other party\" "
                                 + "date entered must not be in the future (6).");

        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .build();
    }

    <T> void otherPartyCheckDateCorrectness(List<String> errors, List<Element<T>> documentUploadOtherParty,
                                  Function<Element<T>, LocalDate> dateExtractor, String errorMessage) {
        if (documentUploadOtherParty == null) {
            return;
        }
        documentUploadOtherParty.forEach(date -> {
            LocalDate dateToCheckOther = dateExtractor.apply(date);
            if (dateToCheckOther.isAfter(time.now().toLocalDate())) {
                errors.add(errorMessage);
            }
        });
    }

    private CallbackResponse documentUploadTimeOtherParty(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();
        caseDataBuilder.caseDocumentUploadDateOther(time.now());
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

