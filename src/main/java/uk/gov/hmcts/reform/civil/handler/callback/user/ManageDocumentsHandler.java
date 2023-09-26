package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.model.citizenui.ManageDocument;
import uk.gov.hmcts.reform.civil.model.citizenui.ManageDocumentType;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.MANAGE_DOCUMENTS;

@Service
@RequiredArgsConstructor
public class ManageDocumentsHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(MANAGE_DOCUMENTS);
    private final ObjectMapper objectMapper;
    private final Map<String, Callback> callbackMap = Map.of(
        callbackKey(ABOUT_TO_SUBMIT),
        this::uploadManageDocuments,
        callbackKey(SUBMITTED), this::emptySubmittedCallbackResponse
    );

    @Override
    protected Map<String, Callback> callbacks() {
        return callbackMap;
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse uploadManageDocuments(CallbackParams callbackParams) {

        List<Element<ManageDocument>> manageDocuments =
            Optional.ofNullable(callbackParams.getCaseData().getManageDocuments())
                .orElse(Collections.emptyList());
        boolean isMediationAgreementPresent = manageDocuments.stream()
            .map(Element::getValue)
            .filter(Objects::nonNull)
            .anyMatch(manageDocument ->
                          Objects.nonNull(manageDocument.getDocumentType())
                              && ManageDocumentType.MEDIATION_AGREEMENT.equals(manageDocument.getDocumentType())
            );
        if (isMediationAgreementPresent) {
            List<String> errors = Collections.singletonList("Mediation agreement is not allowed to upload");
            return AboutToStartOrSubmitCallbackResponse.builder()
                .errors(errors)
                .build();
        }
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(callbackParams.getCaseData().toMap(objectMapper))
            .build();
    }

}
