package uk.gov.hmcts.reform.civil.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.enums.ObligationReason;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;

import java.time.LocalDate;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class ObligationData {

    private LocalDate obligationDate;
    private ObligationReason obligationReason;
    private String otherObligationReason;
    private String obligationAction;
    private YesOrNo obligationWATaskRaised;
}
