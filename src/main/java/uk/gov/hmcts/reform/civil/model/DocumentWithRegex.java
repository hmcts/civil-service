package uk.gov.hmcts.reform.civil.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.civil.model.documents.Document;

@Data
@Builder
public class DocumentWithRegex {

    private final Document document;

    @JsonCreator
    public DocumentWithRegex(@JsonProperty("document") Document document) {
        this.document = document;
    }
}
