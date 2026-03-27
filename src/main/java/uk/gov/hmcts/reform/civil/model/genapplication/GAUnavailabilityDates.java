package uk.gov.hmcts.reform.civil.model.genapplication;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.LocalDate;

@Setter
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class GAUnavailabilityDates {

    private LocalDate unavailableTrialDateFrom;
    private LocalDate unavailableTrialDateTo;

    @JsonCreator
    GAUnavailabilityDates(@JsonProperty("unavailableTrialDateFrom") LocalDate unavailableTrialDateFrom,
                          @JsonProperty("unavailableTrialDateTo") LocalDate unavailableTrialDateTo) {
        this.unavailableTrialDateFrom = unavailableTrialDateFrom;
        this.unavailableTrialDateTo = unavailableTrialDateTo;
    }
}
