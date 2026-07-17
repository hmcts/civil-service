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
public class SolicitorOrganisationDetails {

    @CCD(
            label = "Email",
            regex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$|^$",
            max = 80,
            typeOverride = FieldType.Email
    )
    private String email;
    @CCD(label = "Organisation name")
    private String organisationName;
    @CCD(label = "Fax", max = 24)
    private String fax;
    @CCD(label = "DX", max = 35)
    private String dx;
    @CCD(label = "Phone number", max = 24)
    private String phoneNumber;
    @CCD(label = "Address")
    private Address address;
}
