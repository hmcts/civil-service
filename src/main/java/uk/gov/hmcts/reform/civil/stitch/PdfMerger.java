package uk.gov.hmcts.reform.civil.stitch;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.io.RandomAccessRead;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.multipdf.PDFMergerUtility;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.apache.pdfbox.io.MemoryUsageSetting.setupMainMemoryOnly;

/**
 * Utility class to merge PDF documents.
 */
@Slf4j
public final class PdfMerger {

    private static final byte[] PDF_HEADER = "%PDF-".getBytes(StandardCharsets.US_ASCII);

    /**
     * Utility class constructor.
     */
    private PdfMerger() {
    }

    /**
     * Merges a list of PDF documents into a single PDF document.
     *
     * @param documents       The list of PDF documents
     * @param loggingContext  The logging context
     * @return The merged PDF document
     */
    public static byte[] mergeDocuments(List<byte[]> documents, String loggingContext) {
        validateDocuments(documents, loggingContext);

        if (documents.size() == 1) {
            return documents.get(0);
        }

        ByteArrayOutputStream docOutputStream = new ByteArrayOutputStream();

        List<RandomAccessRead> inputStreams = documents.stream()
            .map(RandomAccessReadBuffer::new)
            .collect(toList());

        PDFMergerUtility pdfMergerUtility = new PDFMergerUtility();
        pdfMergerUtility.addSources(inputStreams);
        pdfMergerUtility.setDestinationStream(docOutputStream);

        try {
            pdfMergerUtility.mergeDocuments(setupMainMemoryOnly().streamCache);
            return docOutputStream.toByteArray();
        } catch (IOException e) {
            log.error("Exception occurred while merging PDF files for caseId {}", loggingContext, e);
            throw new PdfMergeException("Exception occurred while merging PDF files." + loggingContext, e);
        }
    }

    private static void validateDocuments(List<byte[]> documents, String loggingContext) {
        if (documents == null || documents.isEmpty()) {
            throw new PdfMergeException("No PDF documents supplied for caseId " + loggingContext);
        }

        for (int index = 0; index < documents.size(); index++) {
            byte[] document = documents.get(index);
            if (!hasPdfHeader(document)) {
                int length = document == null ? 0 : document.length;
                throw new PdfMergeException(
                    "Document at index " + index + " is not a valid PDF for caseId " + loggingContext
                        + " (size: " + length + " bytes)"
                );
            }
        }
    }

    private static boolean hasPdfHeader(byte[] document) {
        if (document == null || document.length < PDF_HEADER.length) {
            return false;
        }

        for (int index = 0; index < PDF_HEADER.length; index++) {
            if (document[index] != PDF_HEADER[index]) {
                return false;
            }
        }
        return true;
    }
}
