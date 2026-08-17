package uk.gov.hmcts.reform.civil.utils;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class MapperUtil {

    private static final String SCANNED_DOCUMENTS = "scannedDocuments";
    private static final String STAFF_UPLOADED_DOCUMENTS = "staffUploadedDocuments";
    private static final String VALUE = "value";
    private static final String SUBTYPE = "subtype";
    private static final String FORM_SUBTYPE = "formSubtype";
    private static final String DOCUMENT_TYPE = "documentType";

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

    private MapperUtil() {
        // Utility class, no instances
    }

    public static boolean hasPaperResponse(Map<String, Object> data, CaseData caseData) {
        return hasScannedPaperResponse(caseData)
            || hasScannedPaperResponse(data)
            || hasStaffUploadedPaperResponse(data);
    }

    private static boolean hasScannedPaperResponse(CaseData caseData) {
        return caseData != null
            && caseData.getScannedDocuments() != null
            && caseData.getScannedDocuments().stream()
            .map(Element::getValue)
            .filter(Objects::nonNull)
            .anyMatch(doc -> isPaperResponseScannedType(doc.getSubtype())
                || isPaperResponseScannedType(doc.getFormSubtype()));
    }

    private static boolean hasScannedPaperResponse(Map<String, Object> data) {
        return getCollection(data, SCANNED_DOCUMENTS).stream()
            .map(MapperUtil::getValueMap)
            .filter(Objects::nonNull)
            .anyMatch(doc -> isPaperResponseScannedType(doc.get(SUBTYPE))
                || isPaperResponseScannedType(doc.get(FORM_SUBTYPE)));
    }

    private static boolean hasStaffUploadedPaperResponse(Map<String, Object> data) {
        return getCollection(data, STAFF_UPLOADED_DOCUMENTS).stream()
            .map(MapperUtil::getValueMap)
            .filter(Objects::nonNull)
            .map(doc -> doc.get(DOCUMENT_TYPE))
            .filter(String.class::isInstance)
            .map(String.class::cast)
            .anyMatch(PAPER_RESPONSE_DOC_TYPES::contains);
    }

    private static boolean isPaperResponseScannedType(Object value) {
        return value instanceof String scannedType
            && PAPER_RESPONSE_SCANNED_TYPES.stream()
            .anyMatch(type -> type.equalsIgnoreCase(scannedType));
    }

    private static List<Map<String, Object>> getCollection(Map<String, Object> data, String key) {
        if (data == null || !(data.get(key) instanceof List<?> values)) {
            return List.of();
        }

        return values.stream()
            .filter(Map.class::isInstance)
            .map(element -> (Map<String, Object>) element)
            .toList();
    }

    private static Map<String, Object> getValueMap(Map<String, Object> element) {
        Object value = element.get(VALUE);
        if (value instanceof Map<?, ?> valueMap) {
            return (Map<String, Object>) valueMap;
        }
        return null;
    }

}
