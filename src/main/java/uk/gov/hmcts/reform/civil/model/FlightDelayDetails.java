package uk.gov.hmcts.reform.civil.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocationCivil;

import java.time.LocalDate;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class FlightDelayDetails {

    @CCD(label = "Airline", searchable = false, typeOverride = FieldType.DynamicList)
    private DynamicList airlineList;
    @CCD(label = "Name of airline", searchable = false)
    private String nameOfAirline;
    @CCD(label = "Flight number", searchable = false)
    private String flightNumber;
    @CCD(label = "Date of flight", hint = "For example, 16 04 2021", searchable = false)
    private LocalDate scheduledDate;
    @CCD(label = " ", searchable = false)
    private CaseLocationCivil flightCourtLocation;
}
