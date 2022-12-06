package uk.gov.hmcts.reform.civil.utils;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CaseCategoryUtilsTest {

    @Test
    void shouldReturnTrue_whenSpecClaimAndNocNotEnabled() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimNotified()
            .setSuperClaimTypeToSpecClaim()
            .build();

        assertTrue(CaseCategoryUtils.isSpecCaseCategory(caseData, false));
    }

    @Test
    void shouldReturnFalse_whenUnspecClaimAndNocNotEnabled() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimNotified()
            .build();

        assertFalse(CaseCategoryUtils.isSpecCaseCategory(caseData, false));
    }

    @Test
    void shouldReturnTrue_whenSpecClaimAndNocEnabled() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimNotified()
            .setSuperClaimTypeToSpecClaim()
            .build();

        assertTrue(CaseCategoryUtils.isSpecCaseCategory(caseData, false));
    }

    @Test
    void shouldReturnFalse_whenUnspecClaimAndNocEnabled() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimNotified()
            .build();

        assertFalse(CaseCategoryUtils.isSpecCaseCategory(caseData, false));
    }
}
