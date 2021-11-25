package uk.gov.hmcts.reform.civil.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.civil.model.documents.Document;

@Data
@Builder
public class ResponseSpecDocument {

    private final Document file;

    @JsonCreator
    public ResponseSpecDocument(Document file) {
        this.file = file;
    }
}
