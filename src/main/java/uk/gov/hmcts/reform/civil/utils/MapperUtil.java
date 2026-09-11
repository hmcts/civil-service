package uk.gov.hmcts.reform.civil.utils;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.scanneddocument.ScannedDocument;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

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

    private static final Predicate<ScannedDocument> filterStaffUploadedPaperResponseDoc = doc ->
        PAPER_RESPONSE_DOC_TYPES.stream().anyMatch(type -> type.equals(doc.getDocumentType().name()));

    private static final Predicate<ScannedDocument> filterCaseDocumentsPaperResponseDoc = doc ->
        PAPER_RESPONSE_SCANNED_TYPES.stream().anyMatch(type -> type.equalsIgnoreCase(doc.getSubtype()));

    public static boolean hasPaperResponse(CaseData caseData) {
        return StreamUtil.asStream(caseData.getScannedDocuments()).map(Element<ScannedDocument>::getValue)
            .anyMatch(filterStaffUploadedPaperResponseDoc)
            || StreamUtil.asStream(caseData.getScannedDocuments()).map(Element<ScannedDocument>::getValue)
            .anyMatch(filterCaseDocumentsPaperResponseDoc);
    }
}
