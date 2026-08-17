package uk.gov.hmcts.reform.civil.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class CorrectEmail {

    @CCD(label = "Your current logged in email is", searchable = false, typeOverride = FieldType.Email)
    private String email;
    @CCD(
            label = "Would you like to use the same email address for notifications related to this claim?",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    private YesOrNo correct;

    public boolean isCorrect() {
        return correct == YesOrNo.YES;
    }
}