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

@ComplexType(name = "TrialHearingWitnessOfFactDJ", generate = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class TrialHearingWitnessOfFact {

    @CCD(label = " ", searchable = false, typeOverride = FieldType.TextArea)
    private String input1;
    @CCD(
            label = " ",
            hint = "Number of witnesses (claimant), e.g. 4",
            searchable = false,
            typeOverride = FieldType.Number
    )
    private String input2;
    @CCD(
            label = " ",
            hint = "Number of witnesses (defendant), e.g. 4",
            searchable = false,
            typeOverride = FieldType.Number
    )
    private String input3;
    @CCD(label = " ", searchable = false)
    private String input4;
    @CCD(label = " ", searchable = false)
    private String input5;
    @CCD(label = " ", hint = "Number of pages, e.g. 4", searchable = false, typeOverride = FieldType.Number)
    private String input6;
    @CCD(label = " ", searchable = false)
    private String input7;
    @CCD(label = " ", searchable = false)
    private String input8;
    @CCD(label = " ", searchable = false)
    @Future(message = "The date entered must be in the future")
    private LocalDate date1;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.TextArea)
    private String input9;
}
