package uk.gov.hmcts.reform.civil.model.dq;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class DocumentsToBeConsidered {

    private final YesOrNo hasDocumentsToBeConsidered;
    private final String details;
}
