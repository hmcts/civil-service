package uk.gov.hmcts.reform.civil.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ObligationWAFlag {

    private String obligationReason;
    private String obligationReasonDisplayValue;
    private String currentDate;
}
