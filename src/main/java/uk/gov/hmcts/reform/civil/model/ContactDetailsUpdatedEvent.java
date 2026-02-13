package uk.gov.hmcts.reform.civil.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class ContactDetailsUpdatedEvent {

    private String description;
    private String summary;
    private YesOrNo submittedByCaseworker;

    public ContactDetailsUpdatedEvent copy() {
        return new ContactDetailsUpdatedEvent(description, summary, submittedByCaseworker);
    }
}
