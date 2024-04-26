package uk.gov.hmcts.reform.civil.model.judgmentonline;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class JudgmentDetails {

    private Integer judgmentId;
    private DynamicList defendants;
    private LocalDateTime lastUpdateTimeStamp;
    private LocalDateTime createdTimestamp;
    private LocalDateTime cancelledTimeStamp;
    private JudgmentState state;
    private YesOrNo isRegisterWithRTL;
    private LocalDate requestDate;
    private LocalDate issueDate;
    private LocalDateTime setAsideDate;
    private LocalDateTime cancelDate;
    private LocalDateTime fullyPaymentMadeDate;
    private YesOrNo isJointJudgment;
    private BigDecimal orderedAmount;
    private BigDecimal costs;
    private BigDecimal totalAmount;
    private String courtLocation;
    private JudgmentInstalmentDetails instalmentDetails;
    private JudgmentPaymentPlan paymentPlan;
}
