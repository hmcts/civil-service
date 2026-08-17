package uk.gov.hmcts.reform.civil.utils;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class MapperUtil {
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

}
