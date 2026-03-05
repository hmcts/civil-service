package uk.gov.hmcts.reform.civil.ga.model.genapplication;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Data
@Accessors(chain = true)
@NoArgsConstructor
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
