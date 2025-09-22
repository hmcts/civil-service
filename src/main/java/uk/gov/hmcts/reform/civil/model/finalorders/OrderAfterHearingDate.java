package uk.gov.hmcts.reform.civil.model.finalorders;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class OrderAfterHearingDate {

    private OrderAfterHearingDateType dateType;
    private LocalDate date;
    private LocalDate fromDate;
    private LocalDate toDate;
    private String bespokeDates;
}
