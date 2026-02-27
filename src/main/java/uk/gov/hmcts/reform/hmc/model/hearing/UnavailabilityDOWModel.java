package uk.gov.hmcts.reform.hmc.model.hearing;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UnavailabilityDOWModel {

    private DayOfWeek dow;
    private DOWUnavailabilityType dowUnavailabilityType;
}
