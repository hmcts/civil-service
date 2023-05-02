package uk.gov.hmcts.reform.hearings.hearingrequest.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;

@JsonIgnoreProperties(ignoreUnknown = true)
@Builder(toBuilder = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UnavailabilityDOWModel {

    private DayOfWeek dow;
    private DOWUnavailabilityType dowUnavailabilityType;
}
