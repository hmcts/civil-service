package uk.gov.hmcts.reform.civil.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.model.citizenui.DashboardClaimStatus;
import uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseType;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpecPaidStatus;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class CaseDataTest {

    @Test
    public void applicant1Proceed_when1v1() {
        CaseData caseData = CaseData.builder()
            .applicant1ProceedWithClaim(YesOrNo.YES)
            .build();
        Assertions.assertEquals(YesOrNo.YES, caseData.getApplicant1ProceedsWithClaimSpec());
    }

    @Test
    public void applicant1Proceed_when2v1() {
        CaseData caseData = CaseData.builder()
            .applicant1ProceedWithClaimSpec2v1(YesOrNo.YES)
            .build();
        Assertions.assertEquals(YesOrNo.YES, caseData.getApplicant1ProceedsWithClaimSpec());
    }
}
