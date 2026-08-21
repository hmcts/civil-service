package uk.gov.hmcts.reform.civil.model.defaultjudgment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.enums.dj.DisposalHearingBundleType;

import jakarta.validation.constraints.Future;
import java.time.LocalDate;
import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.reform.civil.model.TrialHearingBundleType;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "TrialHearingTrialDJ", generate = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class TrialHearingTrial {

    @CCD(label = " ", searchable = false)
    private String input1;
    @CCD(label = "Date from", searchable = false)
    @Future(message = "The date entered must be in the future")
    private LocalDate date1;
    @CCD(label = "Date to", searchable = false)
    @Future(message = "The date entered must be in the future")
    private LocalDate date2;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.TextArea)
    private String input2;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.TextArea)
    private String input3;
    @CCD(
            label = "Bundle type",
            searchable = false,
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "TrialHearingBundleType",
            typeParameterClass = TrialHearingBundleType.class
    )
    private List<DisposalHearingBundleType> type;
}
