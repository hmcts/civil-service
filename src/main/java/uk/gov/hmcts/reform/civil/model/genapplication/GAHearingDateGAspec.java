package uk.gov.hmcts.reform.civil.model.genapplication;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class GAHearingDateGAspec {

    private YesOrNo hearingScheduledPreferenceYesNo;
    private LocalDate hearingScheduledDate;

    @JsonCreator
    GAHearingDateGAspec(@JsonProperty("hearingScheduledPreferenceYesNo") YesOrNo hearingScheduledPreferenceYesNo,
                         @JsonProperty("hearingScheduledDate") LocalDate hearingScheduledDate) {

        this.hearingScheduledPreferenceYesNo = hearingScheduledPreferenceYesNo;
        this.hearingScheduledDate = hearingScheduledDate;
    }
}
