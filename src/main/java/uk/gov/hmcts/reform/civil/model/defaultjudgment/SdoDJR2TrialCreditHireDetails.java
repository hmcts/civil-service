package uk.gov.hmcts.reform.civil.model.defaultjudgment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import jakarta.validation.constraints.Future;
import java.time.LocalDate;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class SdoDJR2TrialCreditHireDetails {

    @CCD(label = " ", searchable = false, typeOverride = FieldType.TextArea)
    private String input2;
    @CCD(label = " ", searchable = false)
    @Future(message = "The date entered must be in the future")
    private LocalDate date1;
    @CCD(label = " ", searchable = false)
    private String input3;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.TextArea)
    private String input4;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.TextArea)
    private String input5;
    @CCD(label = " ", searchable = false)
    @Future(message = "The date entered must be in the future")
    private LocalDate date2;
}
