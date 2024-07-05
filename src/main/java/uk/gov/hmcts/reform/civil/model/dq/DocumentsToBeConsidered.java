package uk.gov.hmcts.reform.civil.model.dq;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;

@Data
@Builder
public class DocumentsToBeConsidered {

    private final YesOrNo hasDocumentsToBeConsidered;
    private final String details;
}
