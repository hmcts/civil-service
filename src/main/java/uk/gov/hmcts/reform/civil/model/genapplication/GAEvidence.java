package uk.gov.hmcts.reform.civil.model.genapplication;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.Setter;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.documents.Document;

import java.util.List;

@Setter
@Data
@Builder(toBuilder = true)
public class GAEvidence {

    private List<Element<Document>> evidenceDocument;

    @JsonCreator
    GAEvidence(@JsonProperty("evidenceDocument") List<Element<Document>> evidenceDocument) {
        this.evidenceDocument = evidenceDocument;
    }

}
