package uk.gov.hmcts.reform.civil.service.sdo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2DisclosureOfDocuments;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2SmallClaimsPPI;
import uk.gov.hmcts.reform.civil.model.sdo.SmallClaimsWitnessStatement;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SdoValidationServiceTest {

    private static final String DATE_ERROR = "Date must be in the future";
    private static final String NUMBER_ERROR = "The number entered cannot be less than zero";

    @Mock
    private SdoCaseClassificationService classificationService;

    private SdoValidationService validationService;

    @BeforeEach
    void setUp() {
        validationService = new SdoValidationService(classificationService);
    }

    @Test
    void shouldReturnError_whenSmallClaimsWitnessCountsNegative() {
        CaseData caseData = CaseData.builder()
            .smallClaimsWitnessStatement(SmallClaimsWitnessStatement.builder()
                                              .input2("-1")
                                              .input3("1")
                                              .build())
            .build();

        List<String> errors = validationService.validate(caseData);

        assertThat(errors).contains(NUMBER_ERROR);
    }

    @Test
    void shouldReturnDateError_whenDrhSmallClaimDatesInPast() {
        when(classificationService.isDrhSmallClaim(any())).thenReturn(true);
        when(classificationService.isNihlFastTrack(any())).thenReturn(false);

        CaseData caseData = CaseData.builder()
            .sdoR2SmallClaimsPPI(SdoR2SmallClaimsPPI.builder()
                                     .ppiDate(LocalDate.now().minusDays(1))
                                     .build())
            .build();

        List<String> errors = validationService.validate(caseData);

        assertThat(errors).contains(DATE_ERROR);
    }

    @Test
    void shouldReturnDateError_whenNihlDisclosureDatesInPast() {
        when(classificationService.isDrhSmallClaim(any())).thenReturn(false);
        when(classificationService.isNihlFastTrack(any())).thenReturn(true);

        CaseData caseData = CaseData.builder()
            .sdoR2DisclosureOfDocuments(SdoR2DisclosureOfDocuments.builder()
                                            .standardDisclosureDate(LocalDate.now().minusDays(2))
                                            .inspectionDate(LocalDate.now().minusDays(1))
                                            .build())
            .build();

        List<String> errors = validationService.validate(caseData);

        assertThat(errors).contains(DATE_ERROR);
    }

    @Test
    void shouldReturnEmptyList_whenNoValidationIssues() {
        when(classificationService.isDrhSmallClaim(any())).thenReturn(false);
        when(classificationService.isNihlFastTrack(any())).thenReturn(false);

        CaseData caseData = CaseData.builder().build();

        List<String> errors = validationService.validate(caseData);

        assertThat(errors).isEmpty();
    }
}
