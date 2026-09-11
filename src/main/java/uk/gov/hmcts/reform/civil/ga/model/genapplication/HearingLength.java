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

    private String lengthListOtherDays;
    private String lengthListOtherHours;
    private String lengthListOtherMinutes;

    @JsonCreator
    HearingLength(@JsonProperty("lengthListOtherDays") String lengthListOtherDays,
                  @JsonProperty("lengthListOtherHours") String lengthListOtherHours,
                  @JsonProperty("lengthListOtherMinutes") String lengthListOtherMinutes
    ) {

        this.lengthListOtherDays = lengthListOtherDays;
        this.lengthListOtherHours = lengthListOtherHours;
        this.lengthListOtherMinutes = lengthListOtherMinutes;
    }

    public HearingLength setLengthListOtherDays(int lengthListOtherDays) {
        this.lengthListOtherDays = String.valueOf(lengthListOtherDays);
        return this;
    }

    public HearingLength setLengthListOtherDays(String lengthListOtherDays) {
        this.lengthListOtherDays = lengthListOtherDays;
        return this;
    }

    public HearingLength setLengthListOtherHours(int lengthListOtherHours) {
        this.lengthListOtherHours = String.valueOf(lengthListOtherHours);
        return this;
    }

    public HearingLength setLengthListOtherHours(String lengthListOtherHours) {
        this.lengthListOtherHours = lengthListOtherHours;
        return this;
    }

    public HearingLength setLengthListOtherMinutes(int lengthListOtherMinutes) {
        this.lengthListOtherMinutes = String.valueOf(lengthListOtherMinutes);
        return this;
    }

    public HearingLength setLengthListOtherMinutes(String lengthListOtherMinutes) {
        this.lengthListOtherMinutes = lengthListOtherMinutes;
        return this;
    }
}
