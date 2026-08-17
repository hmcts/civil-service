package uk.gov.hmcts.reform.civil.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RespondentSolicitorDetails {

    @CCD(label = " ", searchable = false)
    private String orgName;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.AddressUK)
    private Address address;
}
