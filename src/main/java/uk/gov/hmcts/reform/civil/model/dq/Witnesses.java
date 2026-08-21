package uk.gov.hmcts.reform.civil.model.dq;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class Witnesses {

    @CCD(
            label = "Are there any witnesses who should attend the hearing?",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    private YesOrNo witnessesToAppear;
    @CCD(
            label = "Witness details",
            hint = "If the name is unknown at this time please add TBC to both the first name and last name lines. Then use the Manage Contact Information event to provide the name when known\n",
            showCondition = "witnessesToAppear = \"Yes\"",
            searchable = false
    )
    private List<Element<Witness>> details;

    public Witnesses copy() {
        return new Witnesses()
            .setWitnessesToAppear(witnessesToAppear)
            .setDetails(details);
    }
}
