package uk.gov.hmcts.reform.civil.handler.callback.user;

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
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.ATTACH_SCANNED_DOCS;

@Slf4j
@Service
@RequiredArgsConstructor
public class AttachScannedDocsCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(ATTACH_SCANNED_DOCS);

    private static final List<String> PAPER_RESPONSE_SCANNED_TYPES = Arrays.asList(
        "N9a",
        "N9b",
        "N11",
        "N225",
        "N180"
    );

    private static final List<String> PAPER_RESPONSE_DOC_TYPES = Arrays.asList(
        "PAPER_RESPONSE_FULL_ADMIT",
        "PAPER_RESPONSE_PART_ADMIT",
        "PAPER_RESPONSE_STATES_PAID",
        "PAPER_RESPONSE_MORE_TIME",
        "PAPER_RESPONSE_DISPUTES_ALL",
        "PAPER_RESPONSE_COUNTER_CLAIM"
    );

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::setDefendantResponseMethod
        );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse setDefendantResponseMethod(CallbackParams callbackParams) {
        Map<String, Object> data = new HashMap<>();
        if (callbackParams.getRequest() != null && callbackParams.getRequest().getCaseDetails() != null
            && callbackParams.getRequest().getCaseDetails().getData() != null) {
            data.putAll(callbackParams.getRequest().getCaseDetails().getData());
        }

        if (hasPaperResponse(data, callbackParams.getCaseData())) {
            log.info("Paper response detected for caseId: {}, setting defendant responseMethod to OFFLINE",
                     callbackParams.getCaseData() != null ? callbackParams.getCaseData().getCcdCaseReference() : null);

            // Update CMC style respondents collection if present
            if (data.containsKey("respondents") && data.get("respondents") instanceof List) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> respondents = (List<Map<String, Object>>) data.get("respondents");
                if (!respondents.isEmpty()) {
                    Map<String, Object> firstElement = new HashMap<>(respondents.get(0));
                    if (firstElement.get("value") instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> valueMap = new HashMap<>((Map<String, Object>) firstElement.get("value"));
                        valueMap.put("responseMethod", "OFFLINE");
                        firstElement.put("value", valueMap);
                        respondents.set(0, firstElement);
                        data.put("respondents", respondents);
                    }
                }
            }

            if (isCivilCaseType(callbackParams)) {
                data.put("respondent1ResponseMethod", "OFFLINE");
            }
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(data)
            .build();
    }

    private boolean hasPaperResponse(Map<String, Object> data, CaseData caseData) {
        // Check scannedDocuments in CaseData if present
        if (caseData != null && caseData.getScannedDocuments() != null) {
            boolean hasScannedPaper = caseData.getScannedDocuments().stream()
                .map(Element::getValue)
                .filter(doc -> doc != null)
                .anyMatch(doc -> (doc.getSubtype() != null && PAPER_RESPONSE_SCANNED_TYPES.stream().anyMatch(type -> type.equalsIgnoreCase(doc.getSubtype())))
                    || (doc.getFormSubtype() != null && PAPER_RESPONSE_SCANNED_TYPES.stream().anyMatch(type -> type.equalsIgnoreCase(doc.getFormSubtype()))));
            if (hasScannedPaper) {
                return true;
            }
        }

        // Check scannedDocuments in raw data map
        if (data.containsKey("scannedDocuments") && data.get("scannedDocuments") instanceof List) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> scannedDocs = (List<Map<String, Object>>) data.get("scannedDocuments");
            for (Map<String, Object> element : scannedDocs) {
                Object valueObj = element.get("value");
                if (valueObj instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> doc = (Map<String, Object>) valueObj;
                    String subtype = (String) doc.get("subtype");
                    String formSubtype = (String) doc.get("formSubtype");
                    if ((subtype != null && PAPER_RESPONSE_SCANNED_TYPES.stream().anyMatch(type -> type.equalsIgnoreCase(subtype)))
                        || (formSubtype != null && PAPER_RESPONSE_SCANNED_TYPES.stream().anyMatch(type -> type.equalsIgnoreCase(formSubtype)))) {
                        return true;
                    }
                }
            }
        }

        // Check staffUploadedDocuments in raw data map
        if (data.containsKey("staffUploadedDocuments") && data.get("staffUploadedDocuments") instanceof List) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> staffDocs = (List<Map<String, Object>>) data.get("staffUploadedDocuments");
            for (Map<String, Object> element : staffDocs) {
                Object valueObj = element.get("value");
                if (valueObj instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> doc = (Map<String, Object>) valueObj;
                    String docType = (String) doc.get("documentType");
                    if (docType != null && PAPER_RESPONSE_DOC_TYPES.contains(docType)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private boolean isCivilCaseType(CallbackParams callbackParams) {
        return callbackParams.getRequest() != null
            && callbackParams.getRequest().getCaseDetails() != null
            && Objects.equals("CIVIL", callbackParams.getRequest().getCaseDetails().getCaseTypeId());
    }
}
