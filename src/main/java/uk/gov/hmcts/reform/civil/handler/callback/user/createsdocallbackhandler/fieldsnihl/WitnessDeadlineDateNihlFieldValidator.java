package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.fieldsnihl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class WitnessDeadlineDateNihlFieldValidator implements NihlFieldValidator {

    private final FieldsNihlUtils fieldsNihlUtils;

    @Override
    public void validate(CaseData caseData, List<String> errors) {
        if (caseData.getSdoR2WitnessesOfFact() != null && caseData.getSdoR2WitnessesOfFact().getSdoWitnessDeadlineDate() != null) {
            log.debug("Validating Witness Deadline Date");
            fieldsNihlUtils.validateFutureDate(caseData.getSdoR2WitnessesOfFact().getSdoWitnessDeadlineDate()).ifPresent(error -> {
                log.warn("Witness Deadline Date validation failed: {}", error);
                errors.add(error);
            });
        }
    }
}
