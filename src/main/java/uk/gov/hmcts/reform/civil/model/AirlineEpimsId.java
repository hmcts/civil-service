package uk.gov.hmcts.reform.civil.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Value;

@Value
@Builder
@AllArgsConstructor
@Data
@NoArgsConstructor(force = true)
public class AirlineEpimsId {

    String airline;
    String epimsID;
}
