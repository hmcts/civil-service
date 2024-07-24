package uk.gov.hmcts.reform.civil.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;

@Builder(toBuilder = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContactDetailsUpdatedEvent {

    private String description;
    private String summary;
    private YesOrNo submittedByCaseworker;
}
