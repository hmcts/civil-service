package uk.gov.hmcts.reform.civil.documentmanagement.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Accessors(chain = true)
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
public class Document {

    @JsonAlias("document_url")
    String documentUrl;
    @JsonAlias("document_binary_url")
    String documentBinaryUrl;
    @JsonAlias("document_filename")
    String documentFileName;
    @JsonAlias("document_hash")
    String documentHash;
    @JsonAlias("category_id")
    String categoryID;
    @JsonAlias("upload_timestamp")
    String uploadTimestamp;

    @JsonCreator
    public Document(@JsonProperty("document_url") String documentUrl,
                    @JsonProperty("document_binary_url") String documentBinaryUrl,
                    @JsonProperty("document_filename") String documentFileName,
                    @JsonProperty("document_hash") String documentHash,
                    @JsonProperty("category_id") String categoryID,
                    @JsonProperty("upload_timestamp") String uploadTimestamp) {
        this.documentUrl = documentUrl;
        this.documentBinaryUrl = documentBinaryUrl;
        this.documentFileName = documentFileName;
        this.documentHash = documentHash;
        this.categoryID = categoryID;
        this.uploadTimestamp = uploadTimestamp;
    }

    @JsonIgnore
    public static Document toDocument(Document document, DocumentType documentType) {
        return new Document()
            .setDocumentUrl(document.getDocumentUrl())
            .setDocumentBinaryUrl(document.getDocumentBinaryUrl())
            .setDocumentFileName(getDocumentName(documentType, document.getDocumentFileName()))
            .setDocumentHash(document.getDocumentHash())
            .setCategoryID(document.getCategoryID())
            .setUploadTimestamp(document.getUploadTimestamp());
    }

    public static String getDocumentName(DocumentType documentType, String documentFileName) {

        int lastDotIndex = documentFileName.lastIndexOf('.');
        String extension = documentFileName.substring(lastDotIndex + 1);

        switch (documentType) {
            case GENERAL_APPLICATION_DRAFT:
                return getFileName("Translated_draft_application_%s", extension);
            case GENERAL_ORDER:
                return getFileName("Translated_General_order_for_application_%s", extension);
            case DISMISSAL_ORDER:
                return getFileName("Translated_Dismissal_order_for_application_%s", extension);
            case DIRECTION_ORDER:
                return getFileName("Translated_Direction_order_for_application_%s", extension);
            case SEND_APP_TO_OTHER_PARTY:
                return "Translated Court document" + "." + extension;
            case HEARING_ORDER:
                return getFileName("Translated_Hearing_order_for_application_%s", extension);
            case HEARING_NOTICE:
                return getFileName("Translated_Application_Hearing_Notice_%s", extension);
            case REQUEST_FOR_INFORMATION:
                return getFileName("Translated_Request_for_information_for_application_%s", extension);
            case REQUEST_MORE_INFORMATION_APPLICANT_TRANSLATED, REQUEST_MORE_INFORMATION_RESPONDENT_TRANSLATED:
                return getFileName("Respond_for_information_for_application_%s", extension);
            case WRITTEN_REPRESENTATION_SEQUENTIAL:
                return getFileName("Translated_Order_Written_Representation_Sequential_for_application_%s", extension);
            case WRITTEN_REPRESENTATION_CONCURRENT:
                return getFileName("Translated_Order_Written_Representation_Concurrent_for_application_%s", extension);
            case WRITTEN_REPRESENTATION_APPLICANT_TRANSLATED, WRITTEN_REPRESENTATION_RESPONDENT_TRANSLATED:
                return getFileName("Respond_for_written_representation_for_application_%s", extension);
            default:
                return documentFileName;
        }
    }

    private static String getFileName(String documentName, String extension) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return String.format(documentName, LocalDateTime.now().format(formatter)) + "." + extension;
    }
}
