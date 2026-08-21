package uk.gov.hmcts.reform.civil.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "DocumentOrImageWithRegex", generate = true)
@Data
public class DocumentWithRegex {

    @CCD(
            label = "Document",
            regex = ".pdf,.txt,.doc,.dot,.docx,.rtf,.xls,.xlt,.xla,.xlsx,.xltx,.xlsb,.ppt,.pot,.pps,.ppa,.pptx,.potx,.ppsx,.jpg,.jpeg,.bmp,.tif,.tiff,.png",
            searchable = false
    )
    private Document document;

    @JsonCreator
    public DocumentWithRegex(@JsonProperty("document") Document document) {
        this.document = document;
    }
}
