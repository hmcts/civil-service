package uk.gov.hmcts.reform.civil.model.caseprogression;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class UploadEvidenceDocumentType {

    @CCD(ignore = true)
    private String witnessOptionName;
    @CCD(
            label = "Type of document",
            hint = "For example, contract, invoice, receipt, email, text message, photo, social media message",
            showCondition = "typeOfDocument != \"DO_NOT_SHOW_IN_UI\"",
            searchable = false,
            retainHiddenValue = true
    )
    private String typeOfDocument;
    @CCD(ignore = true)
    private String bundleName;
    @CCD(
            label = "Date document was issued or message was sent",
            hint = "For example, 12/09/2022",
            showCondition = "typeOfDocument != \"DO_NOT_SHOW_IN_UI\"",
            searchable = false,
            retainHiddenValue = true
    )
    private LocalDate documentIssuedDate;
    @CCD(
            label = "Upload a file",
            hint = "Each document must be less than 100MB. You can upload the following file types: Word, Excel, PowerPoint, PDF, RTF, TXT, CSV, JPG, JPEG, PNG, BMG, TIF, TIFF",
            showCondition = "typeOfDocument != \"DO_NOT_SHOW_IN_UI\"",
            regex = ".pdf,.txt,.doc,.dot,.docx,.rtf,.xls,.xlt,.xla,.xlsx,.xltx,.xlsb,.ppt,.pot,.pps,.ppa,.pptx,.potx,.ppsx,.jpg,.jpeg,.bmp,.tif,.tiff,.png",
            searchable = false,
            retainHiddenValue = true
    )
    private Document documentUpload;
    @CCD(
            label = "Document Uploaded DateTime",
            showCondition = "documentUpload = \"DUMMY_VALUE_TO_HIDE_FIELD\"",
            searchable = false,
            retainHiddenValue = true
    )
    private LocalDateTime createdDatetime = LocalDateTime.now(ZoneId.of("Europe/London"));
}

