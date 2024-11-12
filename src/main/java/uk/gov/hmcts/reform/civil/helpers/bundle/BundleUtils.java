package uk.gov.hmcts.reform.civil.helpers.bundle;

import org.apache.commons.lang.StringUtils;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.helpers.DateFormatHelper;
import uk.gov.hmcts.reform.civil.model.bundle.BundlingRequestDocument;
import uk.gov.hmcts.reform.civil.model.bundle.DocumentLink;

import java.time.LocalDate;

public class BundleUtils {

    private static final String DATE_FORMAT = "dd/MM/yyyy";

    private BundleUtils() {
        //NO-OP
    }

    public static String generateDocName(String fileName, String strParam, String strParam2, LocalDate date) {
        if (StringUtils.isBlank(strParam)) {
            return String.format(fileName, DateFormatHelper.formatLocalDate(date, DATE_FORMAT));
        } else if (StringUtils.isBlank(strParam2)) {
            return String.format(fileName, strParam, DateFormatHelper.formatLocalDate(date, DATE_FORMAT));
        } else {
            return String.format(fileName, strParam, strParam2, DateFormatHelper.formatLocalDate(date, DATE_FORMAT));
        }
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
