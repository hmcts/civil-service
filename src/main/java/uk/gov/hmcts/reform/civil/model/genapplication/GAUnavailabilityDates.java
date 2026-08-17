package uk.gov.hmcts.reform.civil.model.genapplication;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.LocalDate;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Setter
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class GAUnavailabilityDates {

    @CCD(label = "Date from", searchable = false)
    private LocalDate unavailableTrialDateFrom;
    @CCD(label = "Date to", searchable = false)
    private LocalDate unavailableTrialDateTo;

    @JsonCreator
    GAUnavailabilityDates(@JsonProperty("unavailableTrialDateFrom") LocalDate unavailableTrialDateFrom,
                          @JsonProperty("unavailableTrialDateTo") LocalDate unavailableTrialDateTo) {
        this.unavailableTrialDateFrom = unavailableTrialDateFrom;
        this.unavailableTrialDateTo = unavailableTrialDateTo;
    }
}
