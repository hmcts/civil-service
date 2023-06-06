package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.CaseNoteType;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static java.lang.String.format;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.EVIDENCE_UPLOAD_JUDGE;

@Service
@RequiredArgsConstructor
public class EvidenceUploadJudgeHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(EVIDENCE_UPLOAD_JUDGE);
    private final ObjectMapper objectMapper;
    public static final String EVIDENCE_UPLOAD_HEADER_TWO = "# Document uploaded \n # %s";
    public static final String EVIDENCE_UPLOAD_HEADER_ONE = "# Document uploaded and note added \n # %s";
    public static final String EVIDENCE_UPLOAD_BODY_ONE = "## You have uploaded: \n %s";

    @Override
    protected Map<String, Callback> callbacks() {
        return new ImmutableMap.Builder<String, Callback>()
            .put(callbackKey(ABOUT_TO_START), this::emptyCallbackResponse)
            .put(callbackKey(ABOUT_TO_SUBMIT), this::populateSubmittedDateTime)
            .put(callbackKey(SUBMITTED), this::buildConfirmation)
            .build();
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private AboutToStartOrSubmitCallbackResponse populateSubmittedDateTime(CallbackParams callbackParams) {
        var caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();

        if (caseData.getCaseNoteTypeNoteTA() != null) {
            caseDataBuilder.noteAdditionDateTime(LocalDateTime.now());
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }

    private SubmittedCallbackResponse buildConfirmation(CallbackParams callbackParams) {
        var caseData = callbackParams.getCaseData();

        return SubmittedCallbackResponse.builder()
            .confirmationHeader(getHeader(caseData))
            .confirmationBody(getBody(caseData))
            .build();
    }

    private String getHeader(CaseData caseData) {
        if (null != caseData.getCaseNoteType() && caseData.getCaseNoteType().equals(CaseNoteType.DOCUMENT_ONLY)) {
            return format(EVIDENCE_UPLOAD_HEADER_TWO, caseData.getLegacyCaseReference());
        }
        if (caseData.getCaseNoteType().equals(CaseNoteType.DOCUMENT_AND_NOTE)) {
            return format(EVIDENCE_UPLOAD_HEADER_ONE, caseData.getLegacyCaseReference());
        }
        return null;
    }

    private String getBody(CaseData caseData) {
        StringBuilder stringBuilder = new StringBuilder();
        if (null != caseData.getCaseNoteType() && caseData.getCaseNoteType().equals(CaseNoteType.DOCUMENT_ONLY)) {
            IntStream.range(0, caseData.getDocumentAndName()
                .size()).forEachOrdered(i -> stringBuilder.append("* ").append(
                caseData.getDocumentAndName().get(i).getValue().getDocument().getDocumentFileName()).append("\n"));

            return format(EVIDENCE_UPLOAD_BODY_ONE, stringBuilder);
        }
        if (caseData.getCaseNoteType().equals(CaseNoteType.DOCUMENT_AND_NOTE)) {
            IntStream.range(0, caseData.getDocumentAndNote()
                .size()).forEachOrdered(i -> stringBuilder.append("* ").append(
                caseData.getDocumentAndNote().get(i)
                    .getValue()
                    .getDocument()
                    .getDocumentFileName()).append("\n"));

            return format(EVIDENCE_UPLOAD_BODY_ONE, stringBuilder);
        }
        return null;
    }

}


