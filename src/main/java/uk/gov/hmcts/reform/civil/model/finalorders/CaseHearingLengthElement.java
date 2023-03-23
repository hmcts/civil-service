package uk.gov.hmcts.reform.civil.model.finalorders;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class CaseHearingLengthElement {
    private BigDecimal lengthListOtherDays;
    private BigDecimal lengthListOtherHours;
    private BigDecimal lengthListOtherMinutes;
}
