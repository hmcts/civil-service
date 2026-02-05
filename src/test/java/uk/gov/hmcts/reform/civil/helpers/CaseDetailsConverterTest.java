package uk.gov.hmcts.reform.civil.helpers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.model.BaseCaseData;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static uk.gov.hmcts.reform.civil.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.civil.CaseDefinitionConstants.GENERALAPPLICATION_CASE_TYPE;
import static uk.gov.hmcts.reform.civil.enums.CaseState.CASE_ISSUED;

class CaseDetailsConverterTest {

    public static final long CASE_REFERENCE = 200L;
    public static final String DETAILS_OF_CLAIM = "Details of Claim";
    private static final LocalDateTime DATE_IN_2025 = LocalDateTime.of(2025, 10, 23, 0, 0);
    private final CaseDetailsConverter caseDetailsConverter = new CaseDetailsConverter(
        new ObjectMapper().registerModule(new JavaTimeModule()));

    @Test
    public void shouldIdentifyGeneralApplicationCaseTypeAndReturnGeneralApplicationCaseData() {
        final CaseDetails caseDetails = CaseDetails.builder()
            .caseTypeId(GENERALAPPLICATION_CASE_TYPE)
            .id(CASE_REFERENCE)
            .state(CASE_ISSUED.name())
            .createdDate(DATE_IN_2025)
            .data(Map.of("detailsOfClaim", DETAILS_OF_CLAIM))
            .build();

        final BaseCaseData baseCaseData = caseDetailsConverter.toBaseCaseData(caseDetails);
        assertInstanceOf(GeneralApplicationCaseData.class, baseCaseData);
        final GeneralApplicationCaseData generalApplicationCaseData = (GeneralApplicationCaseData) baseCaseData;
        assertEquals(DETAILS_OF_CLAIM, generalApplicationCaseData.getDetailsOfClaim());
        assertEquals(CASE_REFERENCE, generalApplicationCaseData.getCcdCaseReference());
        assertEquals(CASE_ISSUED, generalApplicationCaseData.getCcdState());
        assertEquals(DATE_IN_2025, generalApplicationCaseData.getCreatedDate());
    }

    @Test
    public void shouldIdentifyCivilCaseTypeAndReturnCivilCaseData() {
        final CaseDetails caseDetails = CaseDetails.builder()
            .caseTypeId(CASE_TYPE)
            .id(CASE_REFERENCE)
            .state(CASE_ISSUED.name())
            .data(Map.of("detailsOfClaim", "Details of Claim"))
            .build();

        final BaseCaseData baseCaseData = caseDetailsConverter.toBaseCaseData(caseDetails);
        assertInstanceOf(CaseData.class, baseCaseData);
        final CaseData civilCaseData = (CaseData) baseCaseData;
        assertEquals("Details of Claim", civilCaseData.getDetailsOfClaim());
        assertEquals(CASE_REFERENCE, civilCaseData.getCcdCaseReference());
        assertEquals(CASE_ISSUED, civilCaseData.getCcdState());
    }
}
