package uk.gov.hmcts.reform.civil.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.civil.model.documents.Document;

@Data
@Builder
public class ResponseDocument {

    private final Document file;

    @JsonCreator
    public ResponseDocument(Document file) {
        this.file = file;
    }
}
