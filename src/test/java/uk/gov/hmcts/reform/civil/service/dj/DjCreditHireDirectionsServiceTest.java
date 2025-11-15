package uk.gov.hmcts.reform.civil.service.dj;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.sdo.AddOrRemoveToggle;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.SdoDJR2TrialCreditHire;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.CREDIT_HIRE_BASIC_RATE_EVIDENCE_WITH_LIABILITY_DJ;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.CREDIT_HIRE_CLAIMANT_EVIDENCE_DJ;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.CREDIT_HIRE_DEFENDANT_UPLOAD_DJ;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.CREDIT_HIRE_DISCLOSURE_DJ;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.CREDIT_HIRE_NON_COMPLIANCE_DJ;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.CREDIT_HIRE_PARTIES_LIAISE;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.CREDIT_HIRE_STATEMENT_DEADLINE_DJ;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.CREDIT_HIRE_STATEMENT_PROMPT_DJ;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.CREDIT_HIRE_WITNESS_LIMIT_DJ;

@ExtendWith(MockitoExtension.class)
class DjCreditHireDirectionsServiceTest {

    @Mock
    private DjDeadlineService deadlineService;

    private DjCreditHireDirectionsService service;

    @BeforeEach
    void setUp() {
        service = new DjCreditHireDirectionsService(deadlineService);
        when(deadlineService.nextWorkingDayInWeeks(anyInt()))
            .thenAnswer(invocation -> LocalDate.of(2025, 2, 1)
                .plusWeeks(invocation.getArgument(0, Integer.class)));
    }

    @Test
    void shouldBuildCreditHireDirectionsWithCalculatedDeadlines() {
        SdoDJR2TrialCreditHire result = service.buildCreditHireDirections();

        assertThat(result.getDetailsShowToggle()).isEqualTo(List.of(AddOrRemoveToggle.ADD));
        assertThat(result.getDate3()).isEqualTo(LocalDate.of(2025, 2, 1).plusWeeks(12));
        assertThat(result.getDate4()).isEqualTo(LocalDate.of(2025, 2, 1).plusWeeks(14));
        assertThat(result.getSdoDJR2TrialCreditHireDetails().getDate1())
            .isEqualTo(LocalDate.of(2025, 2, 1).plusWeeks(8));
        assertThat(result.getInput1()).isEqualTo(CREDIT_HIRE_DISCLOSURE_DJ);
        assertThat(result.getInput6())
            .isEqualTo(CREDIT_HIRE_BASIC_RATE_EVIDENCE_WITH_LIABILITY_DJ + " " + CREDIT_HIRE_DEFENDANT_UPLOAD_DJ);
        assertThat(result.getInput7()).isEqualTo(CREDIT_HIRE_CLAIMANT_EVIDENCE_DJ);
        assertThat(result.getInput8()).isEqualTo(CREDIT_HIRE_WITNESS_LIMIT_DJ);
        assertThat(result.getSdoDJR2TrialCreditHireDetails().getInput2())
            .isEqualTo(CREDIT_HIRE_STATEMENT_PROMPT_DJ);
        assertThat(result.getSdoDJR2TrialCreditHireDetails().getInput3())
            .isEqualTo(CREDIT_HIRE_STATEMENT_DEADLINE_DJ);
        assertThat(result.getSdoDJR2TrialCreditHireDetails().getInput4())
            .isEqualTo(CREDIT_HIRE_NON_COMPLIANCE_DJ);
        assertThat(result.getSdoDJR2TrialCreditHireDetails().getInput5())
            .isEqualTo(CREDIT_HIRE_PARTIES_LIAISE);
    }
}
