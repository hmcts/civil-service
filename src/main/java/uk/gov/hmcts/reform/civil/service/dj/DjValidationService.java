package uk.gov.hmcts.reform.civil.service.dj;

import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class DjValidationService {

    private static final String ERROR_NUMBER_LESS_THAN_ZERO = "The number entered cannot be less than zero";

    public List<String> validate(CaseData caseData) {
        List<String> errors = new ArrayList<>();

        if (caseData.getTrialHearingWitnessOfFactDJ() != null) {
            String inputValue1 = caseData.getTrialHearingWitnessOfFactDJ().getInput2();
            String inputValue2 = caseData.getTrialHearingWitnessOfFactDJ().getInput3();
            validateNegativeWitness(inputValue1, inputValue2).ifPresent(errors::add);
        }

        log.info("DJ validation complete for caseId {}, total errors {}", caseData.getCcdCaseReference(), errors.size());
        return errors;
    }

    private java.util.Optional<String> validateNegativeWitness(String first, String second) {
        if (first != null && second != null) {
            try {
                int number1 = Integer.parseInt(first);
                int number2 = Integer.parseInt(second);
                if (number1 < 0 || number2 < 0) {
                    return java.util.Optional.of(ERROR_NUMBER_LESS_THAN_ZERO);
                }
            } catch (NumberFormatException ignored) {
                // CCD enforces numeric values; ignore format issues to avoid blocking submission.
            }
        }
        return java.util.Optional.empty();
    }
}
