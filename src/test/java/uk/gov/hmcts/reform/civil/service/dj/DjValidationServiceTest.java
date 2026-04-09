package uk.gov.hmcts.reform.civil.service.dj;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialHearingWitnessOfFact;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DjValidationServiceTest {

    private final DjValidationService validationService = new DjValidationService();

    @Test
    void shouldReturnErrorWhenWitnessNumbersAreNegative() {
        TrialHearingWitnessOfFact witnessOfFact = new TrialHearingWitnessOfFact();
        witnessOfFact.setInput2("-1");
        witnessOfFact.setInput3("0");
        CaseData caseData = CaseDataBuilder.builder().build().toBuilder()
            .trialHearingWitnessOfFactDJ(witnessOfFact)
            .build();

        List<String> errors = validationService.validate(caseData);

        assertThat(errors).containsExactly("The number entered cannot be less than zero");
    }

    @Test
    void shouldReturnEmptyListWhenWitnessNumbersAreValid() {
        TrialHearingWitnessOfFact witnessOfFact = new TrialHearingWitnessOfFact();
        witnessOfFact.setInput2("2");
        witnessOfFact.setInput3("3");
        CaseData caseData = CaseDataBuilder.builder().build().toBuilder()
            .trialHearingWitnessOfFactDJ(witnessOfFact)
            .build();

        List<String> errors = validationService.validate(caseData);

        assertThat(errors).isEmpty();
    }

    @Test
    void shouldReturnEmptyListWhenWitnessSectionMissing() {
        CaseData caseData = CaseDataBuilder.builder().build();

        List<String> errors = validationService.validate(caseData);

        assertThat(errors).isEmpty();
    }
}
