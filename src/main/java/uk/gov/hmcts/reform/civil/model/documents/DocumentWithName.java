package uk.gov.hmcts.reform.civil.model.documents;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class DocumentWithName {

    private final Document document;
    private final String documentName;
}
