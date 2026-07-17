package uk.gov.hmcts.reform.civil.model.breathing;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDate;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class BreathingSpaceLiftInfo {

    @CCD(
            label = "When is the Breathing Space expected to end?",
            hint = "This is the date that you have been instructed it will finish",
            searchable = false
    )
    private LocalDate expectedEnd;

    @CCD(label = "Event summary", hint = "A few words describing the purpose of the event", searchable = false)
    private String event;

    @CCD(label = "Event Description", searchable = false, typeOverride = FieldType.TextArea)
    private String eventDescription;
}
