package uk.gov.hmcts.reform.civil.utils;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.model.CaseData;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.UNSPEC_CLAIM;

class CaseServiceUtilTest {

    @Test
    void shouldReturnAAA7WhenCaseAccessCategoryIsUnspecClaim() {
        CaseData caseData = mock(CaseData.class);
        when(caseData.getCaseAccessCategory()).thenReturn(CaseCategory.UNSPEC_CLAIM);

        String serviceId = CaseServiceUtil.getCaseServiceId(caseData);

        assertEquals("AAA7", serviceId);
    }

    @Test
    void shouldReturnAAA6WhenCaseAccessCategoryIsNotUnspecClaim() {
        CaseData caseData = mock(CaseData.class);
        when(caseData.getCaseAccessCategory()).thenReturn(SPEC_CLAIM); // Or any other category

        String serviceId = CaseServiceUtil.getCaseServiceId(caseData);

        assertEquals("AAA6", serviceId);
    }

    @Test
    void shouldReturnAAA6WhenCaseAccessCategoryIsNull() {
        CaseData caseData = mock(CaseData.class);
        when(caseData.getCaseAccessCategory()).thenReturn(null);

        String serviceId = CaseServiceUtil.getCaseServiceId(caseData);

        assertEquals("AAA6", serviceId);
    }

    @Test
    void shouldReturnAAA7WhenGeneralApplicationCaseAccessCategoryIsUnspecClaim() {
        GeneralApplicationCaseData generalApplicationCaseData = mock(GeneralApplicationCaseData.class);
        when(generalApplicationCaseData.getCaseAccessCategory()).thenReturn(CaseCategory.UNSPEC_CLAIM);

        String serviceId = CaseServiceUtil.getCaseServiceId(generalApplicationCaseData);

        assertEquals("AAA7", serviceId);
    }

    @Test
    void shouldReturnAAA7WhenGeneralApplicationParentCaseAccessCategoryIsUnspecClaim() {
        GeneralApplicationCaseData generalApplicationCaseData = mock(GeneralApplicationCaseData.class);
        when(generalApplicationCaseData.getGeneralAppSuperClaimType()).thenReturn(CaseCategory.UNSPEC_CLAIM.toString());

        String serviceId = CaseServiceUtil.getCaseServiceId(generalApplicationCaseData);

        assertEquals("AAA7", serviceId);
    }

    @Test
    void shouldReturnAAA6WhenGeneralApplicationCaseAccessCategoryIsNotUnspecClaim() {
        GeneralApplicationCaseData generalApplicationCaseData = mock(GeneralApplicationCaseData.class);
        when(generalApplicationCaseData.getCaseAccessCategory()).thenReturn(SPEC_CLAIM); // Or any other category

        String serviceId = CaseServiceUtil.getCaseServiceId(generalApplicationCaseData);

        assertEquals("AAA6", serviceId);
    }

    @Test
    void shouldReturnAAA6WhenGeneralApplicationParentCaseAccessCategoryIsNotUnspecClaim() {
        GeneralApplicationCaseData generalApplicationCaseData = mock(GeneralApplicationCaseData.class);
        when(generalApplicationCaseData.getGeneralAppSuperClaimType()).thenReturn(SPEC_CLAIM.toString()); // Or any other category

        String serviceId = CaseServiceUtil.getCaseServiceId(generalApplicationCaseData);

        assertEquals("AAA6", serviceId);
    }

    @Test
    void shouldReturnAAA6WhenGeneralApplicationCaseAccessCategoryIsNull() {
        GeneralApplicationCaseData generalApplicationCaseData = mock(GeneralApplicationCaseData.class);
        when(generalApplicationCaseData.getCaseAccessCategory()).thenReturn(null);

        String serviceId = CaseServiceUtil.getCaseServiceId(generalApplicationCaseData);

        assertEquals("AAA6", serviceId);
    }

    @Test
    void shouldReturnAAA6WhenCaseAccessCategoryIsSpec() {
        String serviceId = CaseServiceUtil.getCaseServiceId(SPEC_CLAIM);
        assertEquals("AAA6", serviceId);
    }

    @Test
    void shouldReturnAAA7WhenCaseAccessCategoryIsUnSpec() {
        String serviceId = CaseServiceUtil.getCaseServiceId(UNSPEC_CLAIM);
        assertEquals("AAA7", serviceId);
    }
}
