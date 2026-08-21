package uk.gov.hmcts.reform.civil.model.citizenui;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.model.Address;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class AdditionalLipPartyDetails {

    @CCD(label = "Correspondence Address", searchable = false, typeOverride = FieldType.AddressUK)
    private Address correspondenceAddress;
    @CCD(label = "Contact Person", searchable = false)
    private String contactPerson;
}
