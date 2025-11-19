package uk.gov.hmcts.reform.civil.model.genapplication;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.Setter;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;

import java.time.LocalDate;

@Setter
@Data
@Builder(toBuilder = true)
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
