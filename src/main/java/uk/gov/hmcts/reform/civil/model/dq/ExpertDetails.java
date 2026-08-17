package uk.gov.hmcts.reform.civil.model.dq;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class ExpertDetails {

    @CCD(label = " ", showCondition = "phoneNumber = \"DO NOT SHOW\"", searchable = false, retainHiddenValue = true)
    private String partyID;
    @CCD(label = "Expert name", searchable = false)
    private String expertName;
    @CCD(label = "First name", searchable = false, max = 40)
    private String firstName;
    @CCD(label = "Last name", searchable = false, max = 40)
    private String lastName;
    @CCD(label = "Phone number", searchable = false, typeOverride = FieldType.PhoneUK)
    private String phoneNumber;
    @CCD(label = "Email address", searchable = false, typeOverride = FieldType.Email)
    private String emailAddress;
    @CCD(label = "Why do you need this expert?", searchable = false, typeOverride = FieldType.TextArea)
    private String whyRequired;
    @CCD(label = "Field of expertise", searchable = false, typeOverride = FieldType.TextArea)
    private String fieldofExpertise;
    @CCD(label = "Estimated cost", searchable = false, typeOverride = FieldType.MoneyGBP)
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal estimatedCost;

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(label = "## Expert details \n", searchable = false, typeOverride = FieldType.Label)
  private String expertDetails;
  // ==== end synthesised definition-only fields ====
}
