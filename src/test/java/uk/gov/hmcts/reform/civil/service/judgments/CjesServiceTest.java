package uk.gov.hmcts.reform.civil.service.judgments;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import uk.gov.hmcts.reform.civil.client.CjesApiClient;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.judgmentonline.cjes.JudgmentDetailsCJES;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentDetails;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentRTLStatus;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;

class CjesServiceTest {

    @Mock
    private CjesApiClient cjesApiClient;

    @Mock
    private CjesMapper cjesMapper;

    @Mock
    private FeatureToggleService featureToggleService;

    @InjectMocks
    private CjesService cjesService;

    @BeforeEach
    void setUp() {
        cjesApiClient = mock(CjesApiClient.class);
        cjesMapper = mock(CjesMapper.class);
        featureToggleService = mock(FeatureToggleService.class);

        cjesService = new CjesService(cjesApiClient, cjesMapper, featureToggleService);
    }

    @Test
    void sendJudgment_shouldUseActiveJudgment_whenIsActiveJudgementIsTrue() {
        JudgmentDetailsCJES judgmentDetailsCJES = new JudgmentDetailsCJES();
        judgmentDetailsCJES.setServiceId("123");

        when(cjesMapper.toJudgmentDetailsCJES(any(CaseData.class), any(Boolean.class)))
            .thenReturn(judgmentDetailsCJES);
        when(featureToggleService.isCjesServiceAvailable()).thenReturn(true);
        when(cjesApiClient.sendJudgmentDetailsCJES(any(JudgmentDetailsCJES.class))).thenReturn(null);

        LocalDateTime now = LocalDateTime.now();
        JudgmentDetails activeJudgment = new JudgmentDetails()
            .setJudgmentId(123)
            .setLastUpdateTimeStamp(now)
            .setCourtLocation("123456")
            .setTotalAmount("123.45")
            .setIssueDate(now.toLocalDate())
            .setRtlState(JudgmentRTLStatus.ISSUED.getRtlState())
            .setCancelDate(now.toLocalDate())
            .setDefendant1Name("Defendant 1")
            .setDefendant1Dob(LocalDate.of(1980, 1, 1));
        String caseId = String.valueOf(System.currentTimeMillis());
        String legacyCcdReference = "reference";

        CaseData caseData = CaseData.builder()
            .caseAccessCategory(SPEC_CLAIM)
            .ccdCaseReference(Long.valueOf(caseId))
            .legacyCaseReference(legacyCcdReference)
            .activeJudgment(activeJudgment)
            .build();

        cjesService.sendJudgment(caseData, true);

        verify(cjesMapper, times(1)).toJudgmentDetailsCJES(caseData, true);
        verify(cjesApiClient, times(1)).sendJudgmentDetailsCJES(any());
    }

    @Test
    void sendJudgment_shouldCallCjesApiClient_whenFeatureToggleIsOff() {
        CaseData caseData = mock(CaseData.class);
        JudgmentDetails judgmentDetails = mock(JudgmentDetails.class);
        when(caseData.getActiveJudgment()).thenReturn(judgmentDetails);
        JudgmentDetailsCJES judgmentDetailsCJES = mock(JudgmentDetailsCJES.class);
        when(cjesMapper.toJudgmentDetailsCJES(any(CaseData.class), any(Boolean.class)))
            .thenReturn(judgmentDetailsCJES);
        when(featureToggleService.isCjesServiceAvailable()).thenReturn(false);

        cjesService.sendJudgment(caseData, true);

        verify(cjesApiClient, never()).sendJudgmentDetailsCJES(any());
    }

    @Test
    void testSendJudgment_ExceptionHandling() {
        CaseData caseData = mock(CaseData.class);

        when(cjesMapper.toJudgmentDetailsCJES(caseData, true)).thenThrow(new RuntimeException());

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                                   () -> cjesService.sendJudgment(caseData, true));

        assertEquals(
            "Failed to send judgment to RTL",
            e.getMessage()
        );
    }
}
