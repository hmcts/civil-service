package uk.gov.hmcts.reform.civil.model.finalorders;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDate;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(generate = false)
@Accessors(chain = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DatesFinalOrders {

    private LocalDate singleDate;
    private LocalDate dateRangeFrom;
    private LocalDate dateRangeTo;
    private LocalDate datesToAvoidDates;
    private String bespokeRangeTextArea;
}

