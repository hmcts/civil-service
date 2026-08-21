package uk.gov.hmcts.reform.civil.model.defaultjudgment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import jakarta.validation.constraints.Future;
import java.time.LocalDate;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "TrialHearingSchedulesOfLossDJ", generate = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class TrialHearingSchedulesOfLoss {

    @CCD(label = "Claimant", searchable = false, typeOverride = FieldType.TextArea)
    private String input1;
    @CCD(label = " ", searchable = false)
    @Future(message = "The date entered must be in the future")
    private LocalDate date1;
    @CCD(label = "Defendant(s)", searchable = false, typeOverride = FieldType.TextArea)
    private String input2;
    @CCD(label = " ", searchable = false)
    @Future(message = "The date entered must be in the future")
    private LocalDate date2;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.TextArea)
    private String input3;
}
