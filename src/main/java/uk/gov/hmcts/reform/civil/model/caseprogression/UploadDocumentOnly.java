package uk.gov.hmcts.reform.civil.model.caseprogression;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;

import java.time.LocalDateTime;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class UploadDocumentOnly {

    @CCD(
            label = "Upload a file",
            hint = "Each document must be less than 100MB. You can upload the following file types: Word, Excel, PowerPoint, PDF, RTF, TXT, CSV, JPG, JPEG, PNG, BMG, TIF, TIFF",
            regex = ".pdf,.txt,.doc,.dot,.docx,.rtf,.xls,.xlt,.xla,.xlsx,.xltx,.xlsb,.ppt,.pot,.pps,.ppa,.pptx,.potx,.ppsx,.jpg,.jpeg,.bmp,.tif,.tiff,.png",
            searchable = false
    )
    private Document documentUpload;
    @CCD(
            label = "Document Uploaded DateTime",
            showCondition = "documentUpload = \"DUMMY_VALUE_TO_HIDE_FIELD\"",
            searchable = false,
            retainHiddenValue = true
    )
    private LocalDateTime createdDatetime = LocalDateTime.now();
}
