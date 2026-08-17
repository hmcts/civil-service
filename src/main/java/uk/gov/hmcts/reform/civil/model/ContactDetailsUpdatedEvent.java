package uk.gov.hmcts.reform.civil.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "ContactDetailsChangedEvent", generate = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class ContactDetailsUpdatedEvent {

    @CCD(label = " ")
    private String description;
    @CCD(label = " ")
    private String summary;
    @CCD(label = " ", typeOverride = FieldType.YesOrNo)
    private YesOrNo submittedByCaseworker;

    public ContactDetailsUpdatedEvent copy() {
        return new ContactDetailsUpdatedEvent(description, summary, submittedByCaseworker);
    }
}
