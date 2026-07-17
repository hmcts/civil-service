package uk.gov.hmcts.reform.civil.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SDOHearingNotes {

    @CCD(
            label = " ",
            hint = "This is only seen by the listing officer.",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    private String input;
}
