package uk.gov.hmcts.reform.civil.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.enums.SuperClaimType;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

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

    /**
     * to ensure that there won't be any problem or NPE-risk when accessing super claim type
     * before it gets removed.
     */
    @Test
    public void superClaimType_replacedby_caseCategory() {
        CaseData data = new CaseDataBuilder().build();
        Assertions.assertEquals(CaseCategory.UNSPEC_CLAIM, data.getCaseAccessCategory());
        Assertions.assertEquals(SuperClaimType.UNSPEC_CLAIM, data.getSuperClaimType());

        CaseDataBuilder builder = new CaseDataBuilder();
        builder.setSuperClaimTypeToSpecClaim();
        data = builder.build();
        Assertions.assertEquals(CaseCategory.SPEC_CLAIM, data.getCaseAccessCategory());
        Assertions.assertEquals(SuperClaimType.SPEC_CLAIM, data.getSuperClaimType());

        data = CaseData.builder().build();
        Assertions.assertEquals(CaseCategory.UNSPEC_CLAIM, data.getCaseAccessCategory());
        Assertions.assertEquals(SuperClaimType.UNSPEC_CLAIM, data.getSuperClaimType());

        data = CaseData.builder().superClaimType(SuperClaimType.SPEC_CLAIM).build();
        Assertions.assertEquals(CaseCategory.SPEC_CLAIM, data.getCaseAccessCategory());
        Assertions.assertEquals(SuperClaimType.SPEC_CLAIM, data.getSuperClaimType());

        data = CaseData.builder().caseAccessCategory(CaseCategory.SPEC_CLAIM).build();
        Assertions.assertEquals(CaseCategory.SPEC_CLAIM, data.getCaseAccessCategory());
        Assertions.assertEquals(SuperClaimType.SPEC_CLAIM, data.getSuperClaimType());
    }
}
