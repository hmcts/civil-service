package uk.gov.hmcts.reform.civil.model.breathing;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDate;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "BreathingSpaceStartInfo", generate = true)
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class BreathingSpaceEnterInfo {

    @CCD(label = "What type is it?", searchable = false)
    private BreathingSpaceType type;

    @CCD(label = "Reference Number", searchable = false, max = 16)
    private String reference;

    @CCD(
            label = "When did it start?",
            hint = "This is the date Breathing Space started, not the date you received notification of it, for example, 15 01 2022",
            searchable = false
    )
    private LocalDate start;

    @CCD(
            label = "Expected end date",
            hint = "This is the date Breathing Space is due to finish, for example, 17 03 2022",
            searchable = false
    )
    private LocalDate expectedEnd;

    @CCD(label = "Event summary", hint = "A few words describing the purpose of the event", searchable = false)
    private String event;

    @CCD(label = "Event Description", searchable = false, typeOverride = FieldType.TextArea)
    private String eventDescription;
}
