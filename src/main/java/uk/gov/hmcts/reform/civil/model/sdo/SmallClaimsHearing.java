package uk.gov.hmcts.reform.civil.model.sdo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.enums.sdo.SmallClaimsTimeEstimate;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor

public class SmallClaimsHearing {

    private String input1;
    private SmallClaimsTimeEstimate time;
    private BigDecimal otherHours;
    private BigDecimal otherMinutes;
    private String input2;
    private LocalDate dateFrom;
    private LocalDate dateTo;
}
