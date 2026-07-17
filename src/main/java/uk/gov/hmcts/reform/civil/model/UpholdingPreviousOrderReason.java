package uk.gov.hmcts.reform.civil.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class UpholdingPreviousOrderReason {

    @CCD(
            label = "Reason for upholding previous order",
            hint = "State the reason why previous order is upheld (i.e. previous order doesn't need amending, all "
                    + "information on the claim has been taken into account and directions order drawn are all in "
                    + "order)\n",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    private String reasonForReconsiderationTxtYes;
}
