package uk.gov.hmcts.reform.civil.model.finalorders;

import lombok.AllArgsConstructor;
import lombok.experimental.Accessors;
import lombok.Data;
import lombok.NoArgsConstructor;

@Accessors(chain = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CaseHearingLengthElement {

    private String lengthListOtherDays;
    private String lengthListOtherHours;
    private String lengthListOtherMinutes;
}
