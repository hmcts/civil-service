package uk.gov.hmcts.reform.civil.service.documentremoval;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.documentremoval.DocumentToKeep;

import java.util.List;

@Data
@Builder
public class DocumentRemovalCaseDataDTO {

    private final CaseData caseData;
    private List<DocumentToKeep> documentsMarkedForDelete;
}
