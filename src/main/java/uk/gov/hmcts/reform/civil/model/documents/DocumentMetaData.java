package uk.gov.hmcts.reform.civil.model.documents;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class DocumentMetaData {

    private Document document;
    private String description;
    private String dateUploaded;
    private String suppliedBy;

    public DocumentMetaData(
        Document document,
        String description,
        String dateUploaded) {
        this(document, description, dateUploaded, null);
    }
}
