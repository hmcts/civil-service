package uk.gov.hmcts.reform.civil.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Data;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;

@Data
public class ResponseSpecDocument {

    private Document file;

    @JsonCreator
    public ResponseSpecDocument(Document file) {
        this.file = file;
    }
}
