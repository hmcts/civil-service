package uk.gov.hmcts.reform.civil.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.enums.ObligationReason;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class StoredObligationData {

    private String createdBy;
    private LocalDateTime createdOn;
    private LocalDate obligationDate;
    private ObligationReason obligationReason;
    private String otherObligationReason;
    private String reasonText;
    private String obligationAction;
    private YesOrNo obligationWATaskRaised;
}
