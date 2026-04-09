package uk.gov.hmcts.reform.civil.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;

@Data
public class DocumentWithRegex {

    private Document document;

    @JsonCreator
    public DocumentWithRegex(@JsonProperty("document") Document document) {
        this.document = document;
    }
}
