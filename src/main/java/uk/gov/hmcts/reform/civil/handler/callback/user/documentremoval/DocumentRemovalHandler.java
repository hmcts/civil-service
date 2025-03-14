package uk.gov.hmcts.reform.civil.handler.callback.user.documentremoval;

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
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.documentremoval.DocumentToKeepCollection;
import uk.gov.hmcts.reform.civil.service.documentremoval.DocumentRemovalService;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.REMOVE_DOCUMENT;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentRemovalHandler extends CallbackHandler {

    private final ObjectMapper mapper;
    private final DocumentRemovalService documentRemovalService;

    private static final List<CaseEvent> EVENTS = Collections.singletonList(REMOVE_DOCUMENT);

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_START), this::retrieveDocuments,
            callbackKey(ABOUT_TO_SUBMIT), this::removeDocuments,
            callbackKey(SUBMITTED), this::emptySubmittedCallbackResponse
        );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse retrieveDocuments(CallbackParams params) {
        CaseData caseData = params.getCaseData();

        log.info("Invoking event document removal about to start callback for Case ID: {}",
            caseData.getCcdCaseReference());

        List<DocumentToKeepCollection> docsToRemoveCollection = documentRemovalService.getCaseDocumentsList(caseData);

        log.info("Retrieved {} case documents to remove from Case ID {}", docsToRemoveCollection.size(),
            caseData.getCcdCaseReference());

        CaseData.CaseDataBuilder<?, ?> updatedCaseData = caseData.toBuilder();
        updatedCaseData.documentToKeepCollection(docsToRemoveCollection);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedCaseData.build().toMap(mapper))
            .build();
    }

    private CallbackResponse removeDocuments(CallbackParams params) {
        CaseData caseData = params.getCaseData();
        String authToken = params.getParams().get(BEARER_TOKEN).toString();

        log.info("Invoking event document removal about to submit callback for Case ID: {}",
            caseData.getCcdCaseReference());

        CaseData updatedCaseData = documentRemovalService.removeDocuments(caseData, caseData.getCcdCaseReference(), authToken);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedCaseData.toMap(mapper))
            .build();
    }
}
