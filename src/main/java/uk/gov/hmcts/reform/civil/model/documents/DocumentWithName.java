package uk.gov.hmcts.reform.civil.model.documents;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;

import java.time.LocalDateTime;
import java.time.ZoneId;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "DocumentAndName", generate = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class DocumentWithName {

    @CCD(
            label = "Document",
            regex = ".pdf,.txt,.doc,.dot,.docx,.rtf,.xls,.xlt,.xla,.xlsx,.xltx,.xlsb,.ppt,.pot,.pps,.ppa,.pptx,.potx,.ppsx,.jpg,.jpeg,.bmp,.tif,.tiff,.png",
            searchable = false
    )
    private Document document;
    @CCD(label = "Name of document", searchable = false)
    private String documentName;
    @CCD(label = "Date added", showCondition = "document = \"DO_NOT_SHOW\"", searchable = false)
    private LocalDateTime createdDateTime = LocalDateTime.now(ZoneId.of("Europe/London"));
    @CCD(label = "Created by", showCondition = "createdDateTime = \"DO_NOT_SHOW\"", searchable = false)
    private String createdBy;
}
