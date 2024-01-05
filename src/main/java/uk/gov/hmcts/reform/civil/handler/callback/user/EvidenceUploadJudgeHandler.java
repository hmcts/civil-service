package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.CaseNoteType;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.CaseNote;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.documents.DocumentAndNote;
import uk.gov.hmcts.reform.civil.model.documents.DocumentWithName;
import uk.gov.hmcts.reform.civil.service.CaseNoteService;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static java.lang.String.format;
import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.EVIDENCE_UPLOAD_JUDGE;

@Service
@RequiredArgsConstructor
@Slf4j
public class EvidenceUploadJudgeHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(EVIDENCE_UPLOAD_JUDGE);
    private final ObjectMapper objectMapper;

    public static final String EVIDENCE_UPLOAD_HEADER_THREE = "# Case note added \n # %s";
    public static final String EVIDENCE_UPLOAD_HEADER_TWO = "# Document uploaded \n # %s";
    public static final String EVIDENCE_UPLOAD_HEADER_ONE = "# Document uploaded and note added \n # %s";
    public static final String EVIDENCE_UPLOAD_BODY_ONE = "## You have uploaded: \n %s";
    private final CaseNoteService caseNoteService;
    private final IdamClient idamClient;

    @Override
    protected Map<String, Callback> callbacks() {
        return new ImmutableMap.Builder<String, Callback>()
            .put(callbackKey(ABOUT_TO_START), this::removeCaseNoteType)
            .put(callbackKey(ABOUT_TO_SUBMIT), this::populateSubmittedDateTime)
            .put(callbackKey(SUBMITTED), this::buildConfirmation)
            .build();
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private AboutToStartOrSubmitCallbackResponse removeCaseNoteType(CallbackParams callbackParams) {
        var caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();

        caseDataBuilder
            .caseNoteType(null)
            .documentAndNameToAdd(null)
            .documentAndNoteToAdd(null)
            .build();

        UserDetails userDetails = idamClient.getUserDetails(callbackParams.getParams().get(BEARER_TOKEN).toString());
        log.info("User roles: {}", userDetails.getRoles());

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }

    private AboutToStartOrSubmitCallbackResponse populateSubmittedDateTime(CallbackParams callbackParams) {
        var caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();
        String userAuth = callbackParams.getParams().get(BEARER_TOKEN).toString();

        if (caseData.getCaseNoteType().equals(CaseNoteType.NOTE_ONLY)) {
            CaseNote caseNoteTA = caseNoteService.buildCaseNote(
                userAuth,
                caseData.getCaseNoteTA()
            );

            List<Element<CaseNote>> caseNotesTa = caseNoteService.addNoteToListEnd(caseNoteTA, caseData.getCaseNotesTA());

            caseDataBuilder
                .caseNotesTA(caseNotesTa)
                .caseNoteTA(null)
                .build();
        }

        if (caseData.getCaseNoteType().equals(CaseNoteType.DOCUMENT_ONLY)) {
            List<Element<DocumentWithName>> documentAndNameToAdd = caseData.getDocumentAndNameToAdd();
            List<Element<DocumentWithName>> documentAndNameCurrent = new ArrayList<>();
            if (nonNull(caseData.getDocumentAndName())) {
                documentAndNameCurrent.addAll(caseData.getDocumentAndName());
            }
            documentAndNameToAdd.forEach(documentAndName -> {
                List<Element<DocumentWithName>> newJudgeCaseNoteDocumentAndName =  caseNoteService.buildJudgeCaseNoteDocumentAndName(documentAndName.getValue(), userAuth);
                documentAndNameCurrent.addAll(newJudgeCaseNoteDocumentAndName);
            });
            caseDataBuilder.documentAndName(documentAndNameCurrent);
        }

        if (caseData.getCaseNoteType().equals(CaseNoteType.DOCUMENT_AND_NOTE)) {
            List<Element<DocumentAndNote>> documentAndNoteToAdd = caseData.getDocumentAndNoteToAdd();
            List<Element<DocumentAndNote>> documentAndNoteCurrent = new ArrayList<>();
            if (nonNull(caseData.getDocumentAndNote())) {
                documentAndNoteCurrent.addAll(caseData.getDocumentAndNote());
            }
            documentAndNoteToAdd.forEach(documentAndNote -> {
                List<Element<DocumentAndNote>> newJudgeCaseNoteAndDocument =  caseNoteService.buildJudgeCaseNoteAndDocument(documentAndNote.getValue(), userAuth);
                documentAndNoteCurrent.addAll(newJudgeCaseNoteAndDocument);
            });
            caseDataBuilder.documentAndNote(documentAndNoteCurrent);
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

        switch (caseData.getCaseNoteType()) {
            case NOTE_ONLY:
                return format(EVIDENCE_UPLOAD_HEADER_THREE, caseData.getLegacyCaseReference());
            case DOCUMENT_ONLY:
                return format(EVIDENCE_UPLOAD_HEADER_TWO, caseData.getLegacyCaseReference());
            case DOCUMENT_AND_NOTE:
                return format(EVIDENCE_UPLOAD_HEADER_ONE, caseData.getLegacyCaseReference());
            default:
                return null;
        }
    }

    private String getBody(CaseData caseData) {
        StringBuilder stringBuilder = new StringBuilder();
        if (null != caseData.getCaseNoteType() && caseData.getCaseNoteType().equals(CaseNoteType.DOCUMENT_ONLY)) {
            IntStream.range(0, caseData.getDocumentAndNameToAdd()
                .size()).forEachOrdered(i -> stringBuilder.append("* ").append(
                caseData.getDocumentAndNameToAdd().get(i).getValue().getDocument().getDocumentFileName()).append("\n"));

            return format(EVIDENCE_UPLOAD_BODY_ONE, stringBuilder);
        }
        if (caseData.getCaseNoteType().equals(CaseNoteType.DOCUMENT_AND_NOTE)) {
            IntStream.range(0, caseData.getDocumentAndNoteToAdd()
                .size()).forEachOrdered(i -> stringBuilder.append("* ").append(
                caseData.getDocumentAndNoteToAdd().get(i)
                    .getValue()
                    .getDocument()
                    .getDocumentFileName()).append("\n"));

            return format(EVIDENCE_UPLOAD_BODY_ONE, stringBuilder);
        }
        return null;
    }

}


