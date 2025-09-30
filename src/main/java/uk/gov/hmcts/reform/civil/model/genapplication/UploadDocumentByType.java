package uk.gov.hmcts.reform.civil.model.genapplication;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.Setter;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;

@Setter
@Data
@Builder(toBuilder = true)
public class UploadDocumentByType {

    private final String documentType;
    private final Document additionalDocument;

    @JsonCreator
    UploadDocumentByType(@JsonProperty("typeOfDocument") String documentType,
                         @JsonProperty("documentUpload") Document additionalDocument) {
        this.documentType = documentType;
        this.additionalDocument = additionalDocument;
    }

}
