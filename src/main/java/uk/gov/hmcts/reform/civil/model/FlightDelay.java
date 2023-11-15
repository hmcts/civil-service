package uk.gov.hmcts.reform.civil.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocationCivil;

import java.time.LocalDate;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class FlightDelay {

    private DynamicList flightDetailsAirlineList;
    private String flightDetailsNameOfAirline;
    private String flightDetailsFlightNumber;
    private LocalDate flightDetailsScheduledDate;
    private CaseLocationCivil flightCourtLocation;
}
