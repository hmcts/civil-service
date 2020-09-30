package uk.gov.hmcts.reform.unspec.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.unspec.model.documents.Document;

@Data
@Builder
public class ResponseDocument {

    private final Document file;

    @JsonCreator
    public ResponseDocument(Document file) {
        this.file = file;
    }
}
