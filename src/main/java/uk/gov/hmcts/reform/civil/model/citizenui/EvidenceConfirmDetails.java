package uk.gov.hmcts.reform.civil.model.citizenui;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "EvidenceConfirmYourDetails", generate = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class EvidenceConfirmDetails {

    @CCD(label = "First name", searchable = false)
    private String firstName;
    @CCD(label = "Last name", searchable = false)
    private String lastName;
    @CCD(label = "Email", searchable = false, typeOverride = FieldType.Email)
    private String email;
    @CCD(label = "Phone", searchable = false)
    private String phone;
    @CCD(label = "Job title", searchable = false)
    private String jobTitle;
}
