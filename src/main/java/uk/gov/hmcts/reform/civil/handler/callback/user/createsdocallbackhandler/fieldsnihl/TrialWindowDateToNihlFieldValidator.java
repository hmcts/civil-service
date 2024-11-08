package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.fieldsnihl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class TrialWindowDateToNihlFieldValidator implements NihlFieldValidator {

    private final FieldsNihlUtils fieldsNihlUtils;

    @Override
    public void validate(CaseData caseData, List<String> errors) {
        if (caseData.getSdoR2Trial() != null
                && caseData.getSdoR2Trial().getSdoR2TrialWindow().getDateTo() != null) {
            log.debug("Validating Trial Window Date To");
            fieldsNihlUtils.validateFutureDate(caseData.getSdoR2Trial().getSdoR2TrialWindow().getDateTo()).ifPresent(error -> {
                log.warn("Trial Window Date To validation failed: {}", error);
                errors.add(error);
            });
        }
    }
}
