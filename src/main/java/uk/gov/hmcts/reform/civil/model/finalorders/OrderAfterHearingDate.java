package uk.gov.hmcts.reform.civil.model.finalorders;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDate;

@Accessors(chain = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderAfterHearingDate {

    private OrderAfterHearingDateType dateType;
    private LocalDate date;
    private LocalDate fromDate;
    private LocalDate toDate;
    private String bespokeDates;
}
