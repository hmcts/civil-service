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

    private BundleUtils() {
        //NO-OP
    }

    public static String generateDocName(String fileName, String strParam, String strParam2, LocalDate date) {
        String formattedTitle;
        if (StringUtils.isBlank(strParam)) {
            formattedTitle = String.format(fileName, DateFormatHelper.formatLocalDate(date, DATE_FORMAT));
        } else if (StringUtils.isBlank(strParam2)) {
            formattedTitle =  String.format(fileName, strParam, DateFormatHelper.formatLocalDate(date, DATE_FORMAT));
        } else {
            formattedTitle =  String.format(fileName, strParam, strParam2, DateFormatHelper.formatLocalDate(date, DATE_FORMAT));
        }

        if (formattedTitle.length() > MAX_DOC_TITLE_LENGTH) {
            log.warn("Truncating generated doc name to 255 chars: {}", formattedTitle);
            formattedTitle = formattedTitle.substring(0, MAX_DOC_TITLE_LENGTH);
        }

        return formattedTitle;
    }

    public static BundlingRequestDocument buildBundlingRequestDoc(String docName, Document document, String docType) {
        return BundlingRequestDocument.builder()
            .documentFileName(docName)
            .documentType(docType)
            .documentLink(DocumentLink.builder()
                              .documentUrl(document.getDocumentUrl())
                              .documentBinaryUrl(document.getDocumentBinaryUrl())
                              .documentFilename(document.getDocumentFileName()).build())
            .build();
    }
}
