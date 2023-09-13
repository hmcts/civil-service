package uk.gov.hmcts.reform.civil.model.judgementonline;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class JudgementDetails {

    private JudgementStatus judgementStatus;
    private String rtlState;
    private LocalDate judgementRequestedDate;
    private LocalDate judgementIssuedDate;
    private LocalDate orderMadeDate;
    private LocalDateTime lastUpdatedDate;
    private LocalDate setAsideDate;
    private boolean isJointJudgement;
    private String amountOrdered;
    private String amountCostOrdered;
    private boolean isRegisteredWithRTL;
    private RePaymentPlanSelection rePaymentPlanSelection;
    private JudgementDetails judgementDetails;
}
