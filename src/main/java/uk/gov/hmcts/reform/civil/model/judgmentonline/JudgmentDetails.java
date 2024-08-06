package uk.gov.hmcts.reform.civil.model.judgmentonline;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.Address;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class JudgmentDetails {

    private Integer judgmentId;
    private String defendant1Name;
    private String defendant2Name;
    private Address defendant1Address;
    private Address defendant2Address;
    private LocalDate defendant1Dob;
    private LocalDate defendant2Dob;
    private LocalDateTime lastUpdateTimeStamp;
    private LocalDateTime createdTimestamp;
    private LocalDateTime cancelledTimeStamp;
    private JudgmentState state;
    private String rtlState;
    private YesOrNo isRegisterWithRTL;
    private LocalDate requestDate;
    private LocalDate issueDate;
    private LocalDate setAsideDate;
    private LocalDate cancelDate;
    private LocalDate fullyPaymentMadeDate;
    private YesOrNo isJointJudgment;
    private String orderedAmount;
    private String costs;
    private String totalAmount;
    private String courtLocation;
    private JudgmentInstalmentDetails instalmentDetails;
    private JudgmentPaymentPlan paymentPlan;
    private JudgmentType type;
}
