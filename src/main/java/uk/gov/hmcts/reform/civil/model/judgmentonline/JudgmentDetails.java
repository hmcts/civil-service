package uk.gov.hmcts.reform.civil.model.judgmentonline;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.genapplication.GeneralApplicationsDetails;

import javax.annotation.Generated;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class JudgmentDetails {

    private Integer judgmentId;
    private List<Element<Party>> defendants;
    private LocalDateTime lastUpdateTimeStamp;
    private LocalDateTime createdTimestamp;
    private LocalDateTime cancelledTimeStamp;
    private JudgmentState state;
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
