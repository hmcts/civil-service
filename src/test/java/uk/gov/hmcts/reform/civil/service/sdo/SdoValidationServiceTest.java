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
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

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
        CaseData caseData = CaseDataBuilder.builder().build();
        SmallClaimsWitnessStatement witnessStatement = new SmallClaimsWitnessStatement();
        witnessStatement.setInput2("-1");
        witnessStatement.setInput3("1");
        caseData.setSmallClaimsWitnessStatement(witnessStatement);

        List<String> errors = validationService.validate(caseData);

        assertThat(errors).contains(NUMBER_ERROR);
    }

    @Test
    void shouldReturnDateError_whenDrhSmallClaimDatesInPast() {
        when(classificationService.isDrhSmallClaim(any())).thenReturn(true);
        when(classificationService.isNihlFastTrack(any())).thenReturn(false);

        CaseData caseData = CaseDataBuilder.builder().build();
        SdoR2SmallClaimsPPI ppi = new SdoR2SmallClaimsPPI();
        ppi.setPpiDate(LocalDate.now().minusDays(1));
        caseData.setSdoR2SmallClaimsPPI(ppi);

        List<String> errors = validationService.validate(caseData);

        assertThat(errors).contains(DATE_ERROR);
    }

    @Test
    void shouldReturnDateError_whenNihlDisclosureDatesInPast() {
        when(classificationService.isDrhSmallClaim(any())).thenReturn(false);
        when(classificationService.isNihlFastTrack(any())).thenReturn(true);

        CaseData caseData = CaseDataBuilder.builder().build();
        SdoR2DisclosureOfDocuments disclosure = new SdoR2DisclosureOfDocuments();
        disclosure.setStandardDisclosureDate(LocalDate.now().minusDays(2));
        disclosure.setInspectionDate(LocalDate.now().minusDays(1));
        caseData.setSdoR2DisclosureOfDocuments(disclosure);

        List<String> errors = validationService.validate(caseData);

        assertThat(errors).contains(DATE_ERROR);
    }

    @Test
    void shouldReturnEmptyList_whenNoValidationIssues() {
        when(classificationService.isDrhSmallClaim(any())).thenReturn(false);
        when(classificationService.isNihlFastTrack(any())).thenReturn(false);

        CaseData caseData = CaseDataBuilder.builder().build();

        List<String> errors = validationService.validate(caseData);

        assertThat(errors).isEmpty();
    }
}
