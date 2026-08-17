package uk.gov.hmcts.reform.civil.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDate;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "hearingDatesParam", generate = true)
@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class HearingDates {

    @CCD(label = "Date from", searchable = false)
    private LocalDate hearingUnavailableFrom;
    @CCD(label = "Date to", searchable = false)
    private LocalDate hearingUnavailableUntil;
}
