package uk.gov.hmcts.reform.civil.service.dj;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialHearingWitnessOfFact;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DjValidationServiceTest {

    private final DjValidationService validationService = new DjValidationService();

    @Test
    void shouldReturnErrorWhenWitnessNumbersAreNegative() {
        CaseData caseData = CaseData.builder()
            .trialHearingWitnessOfFactDJ(TrialHearingWitnessOfFact.builder()
                .input2("-1")
                .input3("0")
                .build())
            .build();

        List<String> errors = validationService.validate(caseData);

        assertThat(errors).containsExactly("The number entered cannot be less than zero");
    }

    @Test
    void shouldReturnEmptyListWhenWitnessNumbersAreValid() {
        CaseData caseData = CaseData.builder()
            .trialHearingWitnessOfFactDJ(TrialHearingWitnessOfFact.builder()
                .input2("2")
                .input3("3")
                .build())
            .build();

        List<String> errors = validationService.validate(caseData);

        assertThat(errors).isEmpty();
    }

    @Test
    void shouldReturnEmptyListWhenWitnessSectionMissing() {
        CaseData caseData = CaseData.builder().build();

        List<String> errors = validationService.validate(caseData);

        assertThat(errors).isEmpty();
    }
}

