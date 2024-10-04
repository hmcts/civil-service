package uk.gov.hmcts.reform.civil.service.judgments;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentAddress;
import uk.gov.hmcts.reform.civil.model.judgmentonline.cjes.JudgmentDefendantDetails;
import uk.gov.hmcts.reform.civil.model.judgmentonline.cjes.JudgmentDetailsCJES;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentDetails;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentRTLStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.UNSPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

public class CjesMapperTest {

    @InjectMocks
    private CjesMapper cjesMapper;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void toJudgmentDetailsCJES_shoudlMapCorrectly_whenValidDataProvided() {
        String caseId = String.valueOf(System.currentTimeMillis());
        String legacyCcdReference = "reference";
        LocalDateTime now = LocalDateTime.now();

        JudgmentDetails judgmentDetails = JudgmentDetails.builder()
            .judgmentId(123)
            .lastUpdateTimeStamp(now)
            .courtLocation("123456")
            .totalAmount("123.45")
            .issueDate(now.toLocalDate())
            .rtlState(JudgmentRTLStatus.ISSUED.getRtlState())
            .cancelDate(now.toLocalDate())
            .defendant1Name("Defendant 1")
            .defendant1Dob(LocalDate.of(1980, 1, 1))
            .defendant1Address(createMockAddress())
            .build();

        CaseData caseData = CaseData.builder()
            .caseAccessCategory(SPEC_CLAIM)
            .ccdCaseReference(Long.valueOf(caseId))
            .legacyCaseReference(legacyCcdReference)
            .activeJudgment(judgmentDetails)
            .build();

        JudgmentDetailsCJES result = cjesMapper.toJudgmentDetailsCJES(caseData, true);

        assertNotNull(result);
        assertEquals("AAA6", result.getServiceId());
        assertEquals("123", result.getJudgmentId());
        assertEquals(now, result.getJudgmentEventTimeStamp());

        assertEquals("123456", result.getCourtEPIMsId());
        assertEquals(caseId, result.getCcdCaseRef());
        assertEquals(legacyCcdReference, result.getCaseNumber());
        assertEquals(123.45, result.getJudgmentAdminOrderTotal());
        assertEquals(now.toLocalDate(), result.getJudgmentAdminOrderDate());
        assertEquals("judgmentRegistered", result.getRegistrationType());
        assertEquals(now.toLocalDate(), result.getCancellationDate());

        JudgmentDefendantDetails defendant1 = result.getDefendant1();
        assertNotNull(defendant1);
        assertEquals("Defendant 1", defendant1.getDefendantName());
        assertEquals(LocalDate.of(1980, 1, 1), defendant1.getDefendantDateOfBirth());

        JudgmentAddress defendant1Address = defendant1.getDefendantAddress();
        assertNotNull(defendant1Address);
        assertEquals("Line 1", defendant1Address.getDefendantAddressLine1());
        assertEquals("Line 2", defendant1Address.getDefendantAddressLine2());
        assertEquals("Line 3", defendant1Address.getDefendantAddressLine3());
        assertEquals("Town", defendant1Address.getDefendantAddressLine4());
        assertEquals("Country", defendant1Address.getDefendantAddressLine5());
        assertEquals("PostCode", defendant1Address.getDefendantPostCode());
    }

    @Test
    void toJudgmentDetailsCJES_shouldHandleNullJudgmentDetails() {
        String caseId = String.valueOf(System.currentTimeMillis());
        String ccdReference = "reference";
        LocalDateTime now = LocalDateTime.now();

        CaseData caseData = CaseData.builder()
            .caseAccessCategory(SPEC_CLAIM)
            .ccdCaseReference(Long.valueOf(caseId))
            .legacyCaseReference(ccdReference)
            .build();

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                     () -> cjesMapper.toJudgmentDetailsCJES(caseData, true));

        assertEquals(
            "Judgment details cannot be null",
            e.getMessage()
        );
    }

    @Test
    void toJudgmentDetailsCJES_shouldMapDefendant2Details_whenProvided() {
        String caseId = String.valueOf(System.currentTimeMillis());
        String ccdReference = "reference";
        LocalDateTime now = LocalDateTime.now();

        JudgmentDetails judgmentDetails = JudgmentDetails.builder()
            .judgmentId(123)
            .lastUpdateTimeStamp(now)
            .courtLocation("123456")
            .totalAmount("123.45")
            .issueDate(now.toLocalDate())
            .rtlState(JudgmentRTLStatus.ISSUED.getRtlState())
            .cancelDate(now.toLocalDate())
            .defendant1Name("Defendant 1")
            .defendant1Dob(LocalDate.of(1980, 1, 1))
            .defendant1Address(createMockAddress())
            .defendant2Name("Defendant 2")
            .defendant2Dob(LocalDate.of(1990, 2, 2))
            .defendant2Address(createMockAddress())
            .build();

        CaseData caseData = CaseData.builder()
            .caseAccessCategory(UNSPEC_CLAIM)
            .ccdCaseReference(Long.valueOf(caseId))
            .legacyCaseReference(ccdReference)
            .activeJudgment(judgmentDetails)
            .build();

        JudgmentDetailsCJES result = cjesMapper.toJudgmentDetailsCJES(caseData, true);

        assertNotNull(result);
        assertEquals("AAA7", result.getServiceId());
        assertNotNull(result.getDefendant2());

        JudgmentDefendantDetails defendant2 = result.getDefendant2();
        assertEquals("Defendant 2", defendant2.getDefendantName());
        assertEquals(LocalDate.of(1990, 2, 2), defendant2.getDefendantDateOfBirth());

        JudgmentAddress defendant2Address = defendant2.getDefendantAddress();
        assertNotNull(defendant2Address);
        assertEquals("Line 1", defendant2Address.getDefendantAddressLine1());
        assertEquals("Line 2", defendant2Address.getDefendantAddressLine2());
        assertEquals("Line 3", defendant2Address.getDefendantAddressLine3());
        assertEquals("Town", defendant2Address.getDefendantAddressLine4());
        assertEquals("Country", defendant2Address.getDefendantAddressLine5());
        assertEquals("PostCode", defendant2Address.getDefendantPostCode());
    }

    @Test
    void toJudgmentDetailsCJES_forHistoricJudgement() {
        String caseId = String.valueOf(System.currentTimeMillis());
        String ccdReference = "reference";
        LocalDateTime now = LocalDateTime.now();

        JudgmentDetails judgmentDetails = JudgmentDetails.builder()
            .judgmentId(123)
            .lastUpdateTimeStamp(now)
            .courtLocation("123456")
            .totalAmount("123.45")
            .issueDate(now.toLocalDate())
            .rtlState(JudgmentRTLStatus.ISSUED.getRtlState())
            .cancelDate(now.toLocalDate())
            .defendant1Name("Defendant 1")
            .defendant1Dob(LocalDate.of(1980, 1, 1))
            .defendant1Address(createMockAddress())
            .defendant2Name("Defendant 2")
            .defendant2Dob(LocalDate.of(1990, 2, 2))
            .defendant2Address(createMockAddress())
            .build();

        CaseData caseData = CaseData.builder()
            .caseAccessCategory(UNSPEC_CLAIM)
            .ccdCaseReference(Long.valueOf(caseId))
            .legacyCaseReference(ccdReference)
            .historicJudgment(wrapElements(judgmentDetails))
            .build();

        JudgmentDetailsCJES result = cjesMapper.toJudgmentDetailsCJES(caseData, false);

        assertNotNull(result);
        assertEquals("AAA7", result.getServiceId());
        assertNotNull(result.getDefendant2());

        JudgmentDefendantDetails defendant2 = result.getDefendant2();
        assertEquals("Defendant 2", defendant2.getDefendantName());
        assertEquals(LocalDate.of(1990, 2, 2), defendant2.getDefendantDateOfBirth());

        JudgmentAddress defendant2Address = defendant2.getDefendantAddress();
        assertNotNull(defendant2Address);
        assertEquals("Line 1", defendant2Address.getDefendantAddressLine1());
        assertEquals("Line 2", defendant2Address.getDefendantAddressLine2());
        assertEquals("Line 3", defendant2Address.getDefendantAddressLine3());
        assertEquals("Town", defendant2Address.getDefendantAddressLine4());
        assertEquals("Country", defendant2Address.getDefendantAddressLine5());
        assertEquals("PostCode", defendant2Address.getDefendantPostCode());
    }

    private JudgmentAddress createMockAddress() {
        return JudgmentAddress.builder()
            .defendantAddressLine1("Line 1")
            .defendantAddressLine2("Line 2")
            .defendantAddressLine3("Line 3")
            .defendantAddressLine4("Town")
            .defendantAddressLine5("Country")
            .defendantPostCode("PostCode")
            .build();
    }
}
