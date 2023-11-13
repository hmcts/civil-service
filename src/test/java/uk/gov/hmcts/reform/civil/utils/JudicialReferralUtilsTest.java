package uk.gov.hmcts.reform.civil.utils;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.ClaimValue;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class JudicialReferralUtilsTest {

    @Test
    public void testShouldMoveToJudicialReferralForSpecClaim() {
        CaseData caseData = CaseData.builder()
            .caseAccessCategory(CaseCategory.SPEC_CLAIM)
            .applicant1ProceedWithClaim(YesOrNo.YES)
            .build();

        boolean judicialReferralUtils = JudicialReferralUtils.shouldMoveToJudicialReferral(caseData);
        assertTrue(judicialReferralUtils);
    }

    @Test
    public void testShouldNotMoveToJudicialReferralForSpecClaim() {
        CaseData caseData = CaseData.builder()
            .caseAccessCategory(CaseCategory.SPEC_CLAIM)
            .applicant1ProceedWithClaim(YesOrNo.NO)
            .build();

        boolean judicialReferralUtils = JudicialReferralUtils.shouldMoveToJudicialReferral(caseData);

        assertFalse(judicialReferralUtils);
    }

    @Test
    public void testShouldMoveToJudicialReferralForUnspecClaim() {
        CaseData caseData = CaseData.builder()
            .caseAccessCategory(CaseCategory.UNSPEC_CLAIM)
            .claimValue(ClaimValue.builder()
                            .statementOfValueInPennies(BigDecimal.valueOf(10000_00))
                            .build())
            .applicant1ProceedWithClaim(YesOrNo.YES)
            .build();

        boolean judicialReferralUtils = JudicialReferralUtils.shouldMoveToJudicialReferral(caseData);
        assertTrue(judicialReferralUtils);
    }

}
