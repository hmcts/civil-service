package uk.gov.hmcts.reform.civil.model.hearingvalues;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.enums.hearing.DOW;
import uk.gov.hmcts.reform.civil.enums.hearing.DOWUnavailabilityType;

@JsonIgnoreProperties(ignoreUnknown = true)
@Builder(toBuilder = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UnavailabilityDOWModel {
    private DOW DOW;
    private DOWUnavailabilityType DOWUnavailabilityType;
}
