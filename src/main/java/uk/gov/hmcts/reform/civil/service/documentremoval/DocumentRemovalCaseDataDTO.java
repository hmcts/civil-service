package uk.gov.hmcts.reform.civil.service.documentremoval;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.civil.model.CaseData;

@Data
@Builder
public class DocumentRemovalCaseDataDTO {

    private final CaseData caseData;
    private final Boolean areSystemGeneratedDocumentsRemoved;

}
