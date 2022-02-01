package uk.gov.hmcts.reform.civil.model.genapplication;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.Setter;

import java.time.LocalDate;

@Setter
@Data
@Builder(toBuilder = true)
public class GAUnavailabilityDates {
    private final LocalDate unavailableTrialDateFrom;
    private final LocalDate unavailableTrialDateTo;

    @JsonCreator
    GAUnavailabilityDates(@JsonProperty("unavailableTrialDateFrom") LocalDate unavailableTrialDateFrom,
                          @JsonProperty("unavailableTrialDateTo") LocalDate unavailableTrialDateTo) {
        this.unavailableTrialDateFrom = unavailableTrialDateFrom;
        this.unavailableTrialDateTo = unavailableTrialDateTo;
    }
}
