package uk.gov.hmcts.reform.civil.helpers.bundle;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.helpers.DateFormatHelper;
import uk.gov.hmcts.reform.civil.model.bundle.BundlingRequestDocument;
import uk.gov.hmcts.reform.civil.model.bundle.DocumentLink;

import java.time.LocalDate;

@Slf4j
public class BundleUtils {

    private static final String DATE_FORMAT = "dd/MM/yyyy";

    public static final int MAX_DOC_TITLE_LENGTH = 255;
    private static final int PRESERVED_DOC_TITLE_SUFFIX_LENGTH = 40;
    private static final String TRUNCATION_MARKER = "...";

    private BundleUtils() {
        //NO-OP
    }

    public static String generateDocName(String fileName, String strParam, String strParam2, LocalDate date) {
        log.info(
            "Generating doc name fileName: {} strParam: {} strParam2: {} date: {}",
            fileName,
            strParam,
            strParam2,
            date
        );
        String formatLocalDate = DateFormatHelper.formatLocalDate(date, DATE_FORMAT);
        String formattedTitle;
        if (StringUtils.isBlank(strParam)) {
            formattedTitle = String.format(fileName, formatLocalDate);
        } else if (StringUtils.isBlank(strParam2)) {
            formattedTitle = String.format(fileName, strParam, formatLocalDate);
        } else {
            formattedTitle = String.format(fileName, strParam, strParam2, formatLocalDate);
        }

        return truncateDocName(formattedTitle);
    }

    public static BundlingRequestDocument buildBundlingRequestDoc(String docName, Document document, String docType) {
        return new BundlingRequestDocument()
            .setDocumentFileName(truncateDocName(docName))
            .setDocumentType(docType)
            .setDocumentLink(new DocumentLink()
                                 .setDocumentUrl(document.getDocumentUrl())
                                 .setDocumentBinaryUrl(document.getDocumentBinaryUrl())
                                 .setDocumentFilename(document.getDocumentFileName()));
    }

    private static String truncateDocName(String docName) {
        if (docName == null || docName.length() <= MAX_DOC_TITLE_LENGTH) {
            return docName;
        }

        log.info("Truncating bundle document name to {} chars: {}", MAX_DOC_TITLE_LENGTH, docName);
        int prefixLength = MAX_DOC_TITLE_LENGTH
            - TRUNCATION_MARKER.length()
            - PRESERVED_DOC_TITLE_SUFFIX_LENGTH;
        String truncatedName = docName.substring(0, prefixLength)
            + TRUNCATION_MARKER
            + docName.substring(docName.length() - PRESERVED_DOC_TITLE_SUFFIX_LENGTH);
        log.info("Truncated bundle document name to {}", truncatedName);
        return truncatedName;
    }
}
