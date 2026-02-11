package uk.gov.hmcts.reform.civil.model.hearingvalues;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.enums.hearing.DOWUnavailabilityType;

import java.time.DayOfWeek;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UnavailabilityDOWModel {

    private DayOfWeek dow;
    private DOWUnavailabilityType dowUnavailabilityType;
}
