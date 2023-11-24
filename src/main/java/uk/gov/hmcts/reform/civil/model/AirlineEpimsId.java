package uk.gov.hmcts.reform.civil.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@AllArgsConstructor
public class AirlineEpimsId {

    String airline;
    String epimsID;
}
