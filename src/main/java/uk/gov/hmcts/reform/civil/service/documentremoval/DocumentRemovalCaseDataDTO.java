package uk.gov.hmcts.reform.civil.service.documentremoval;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.documentremoval.DocumentToKeep;

import java.util.List;

@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor(force = true)
public class DocumentRemovalCaseDataDTO {

    private CaseData caseData;
    private List<DocumentToKeep> documentsMarkedForDelete;
}
