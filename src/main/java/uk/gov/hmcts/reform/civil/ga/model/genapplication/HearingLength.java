package uk.gov.hmcts.reform.civil.ga.model.genapplication;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.Setter;

@Setter
@Data
@Builder(toBuilder = true)
public class HearingLength {

    private int lengthListOtherDays;
    private int lengthListOtherHours;
    private int lengthListOtherMinutes;

    @JsonCreator
    HearingLength(@JsonProperty("lengthListOtherDays") int lengthListOtherDays,
                  @JsonProperty("lengthListOtherHours") int lengthListOtherHours,
                  @JsonProperty("lengthListOtherMinutes") int lengthListOtherMinutes
    ) {

        this.lengthListOtherDays = lengthListOtherDays;
        this.lengthListOtherHours = lengthListOtherHours;
        this.lengthListOtherMinutes = lengthListOtherMinutes;
    }
}
