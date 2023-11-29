package uk.gov.hmcts.reform.civil.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;

@Builder(toBuilder = true)
@Data
@AllArgsConstructor
public class ContactDetailsUpdatedEvent {

    private final String description;
    private final String summary;
    private final YesOrNo submittedByCaseworker;
}
