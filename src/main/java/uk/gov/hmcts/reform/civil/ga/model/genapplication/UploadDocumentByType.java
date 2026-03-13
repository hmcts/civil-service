package uk.gov.hmcts.reform.civil.ga.model.genapplication;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;

@Setter
@Data
@Accessors(chain = true)
@NoArgsConstructor
public class UploadDocumentByType {

    private String documentType;
    private Document additionalDocument;

    @JsonCreator
    UploadDocumentByType(@JsonProperty("typeOfDocument") String documentType,
                         @JsonProperty("documentUpload") Document additionalDocument) {
        this.documentType = documentType;
        this.additionalDocument = additionalDocument;
    }

}
