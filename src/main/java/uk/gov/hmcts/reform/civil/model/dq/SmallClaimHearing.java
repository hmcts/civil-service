package uk.gov.hmcts.reform.civil.model.dq;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.UnavailableDate;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class SmallClaimHearing {

    @CCD(
            label = "Are there any days in the next 12 months when you, your client, an expert, or a witness, cannot attend a hearing?",
            hint = "Hearings\ntake place Monday to Friday.",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    private YesOrNo unavailableDatesRequired;
    @CCD(
            label = "Unavailable date",
            showCondition = "unavailableDatesRequired = \"Yes\"",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "SmallClaimUnavailableDate"
    )
    private List<Element<UnavailableDate>> smallClaimUnavailableDate;

    public SmallClaimHearing copy() {
        return new SmallClaimHearing()
            .setUnavailableDatesRequired(unavailableDatesRequired)
            .setSmallClaimUnavailableDate(smallClaimUnavailableDate);
    }

}
