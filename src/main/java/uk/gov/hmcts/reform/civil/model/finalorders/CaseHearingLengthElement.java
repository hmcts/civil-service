package uk.gov.hmcts.reform.civil.model.finalorders;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class CaseHearingLengthElement {

    private String lengthListOtherDays;
    private String lengthListOtherHours;
    private String lengthListOtherMinutes;
}
