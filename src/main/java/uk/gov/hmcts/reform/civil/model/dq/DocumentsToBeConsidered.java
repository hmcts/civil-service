package uk.gov.hmcts.reform.civil.model.dq;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;

@Data
@SuperBuilder(toBuilder = true)
@AllArgsConstructor
public class DocumentsToBeConsidered {

    private final YesOrNo hasDocumentsToBeConsidered;
    private final String details;
}
