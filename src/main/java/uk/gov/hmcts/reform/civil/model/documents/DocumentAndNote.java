package uk.gov.hmcts.reform.civil.model.documents;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class DocumentAndNote {

    private Document document;

}
