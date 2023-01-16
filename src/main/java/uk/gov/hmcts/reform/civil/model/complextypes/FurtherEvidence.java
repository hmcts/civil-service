package uk.gov.hmcts.reform.civil.model.complextypes;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.civil.enums.FurtherEvidenceDocumentType;
import uk.gov.hmcts.reform.civil.enums.RestrictToCafcassHmcts;
import uk.gov.hmcts.reform.civil.model.documents.Document;

import java.util.List;

@Data
@Builder
public class FurtherEvidence {

    private final FurtherEvidenceDocumentType typeOfDocumentFurtherEvidence;
    private final Document documentFurtherEvidence;
    private final List<RestrictToCafcassHmcts> restrictCheckboxFurtherEvidence;
}
