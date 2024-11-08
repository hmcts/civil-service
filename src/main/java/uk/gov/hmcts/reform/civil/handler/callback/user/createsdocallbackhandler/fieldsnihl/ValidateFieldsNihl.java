package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.fieldsnihl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ValidateFieldsNihl {

    private final List<NihlFieldValidator> nihlFieldValidators;

    public List<String> validateFieldsNihl(CaseData caseData) {
        log.info("Validating NIHL fields");
        ArrayList<String> errors = new ArrayList<>();

        nihlFieldValidators.forEach(nihlFieldValidator -> nihlFieldValidator.validate(caseData, errors));
        log.info("Validation completed with {} errors", errors.size());
        return errors;
    }
}
