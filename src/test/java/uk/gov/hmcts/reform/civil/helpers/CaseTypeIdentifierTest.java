package uk.gov.hmcts.reform.civil.helpers;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.civil.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.civil.CaseDefinitionConstants.GENERALAPPLICATION_CASE_TYPE;

class CaseTypeIdentifierTest {

    @Test
    void shouldReturnTrueWhenCaseTypeIsGeneralApplication() {
        final CaseDetails caseDetails = CaseDetails.builder().caseTypeId(GENERALAPPLICATION_CASE_TYPE).build();
        assertTrue(CaseTypeIdentifier.isGeneralApplication(caseDetails));
    }

    @Test
    void shouldReturnFalseWhenCaseTypeIsNotGeneralApplication() {
        final CaseDetails caseDetails = CaseDetails.builder().caseTypeId(CASE_TYPE).build();
        assertFalse(CaseTypeIdentifier.isGeneralApplication(caseDetails));
    }

    @Test
    void shouldReturnTrueWhenCaseTypeIsCivil() {
        final CaseDetails caseDetails = CaseDetails.builder().caseTypeId(CASE_TYPE).build();
        assertTrue(CaseTypeIdentifier.isCivil(caseDetails));
    }

    @Test
    void shouldReturnFalseWhenCaseTypeIsNotCivil() {
        final CaseDetails caseDetails = CaseDetails.builder().caseTypeId(GENERALAPPLICATION_CASE_TYPE).build();
        assertFalse(CaseTypeIdentifier.isCivil(caseDetails));
    }

    @Test
    void shouldReturnFalseWhenCaseDetailsIsNull() {
        assertFalse(CaseTypeIdentifier.isGeneralApplication(null));
        assertFalse(CaseTypeIdentifier.isCivil(null));
    }

    @Test
    void shouldReturnFalseWhenCaseTypeIsNull() {
        final CaseDetails caseDetails = CaseDetails.builder().caseTypeId(null).build();
        assertFalse(CaseTypeIdentifier.isGeneralApplication(caseDetails));
        assertFalse(CaseTypeIdentifier.isCivil(caseDetails));
    }
}
