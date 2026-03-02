package uk.gov.hmcts.reform.civil.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Data;
import lombok.NoArgsConstructor;

import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;

@Data
@NoArgsConstructor
public class ResponseDocument {

    private Document file;

    @JsonCreator
    public ResponseDocument(Document file) {
        this.file = file;
    }
}
