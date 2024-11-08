package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.fieldsnihl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class TrialWindowListFromNihlFieldValidator implements NihlFieldValidator {

    private final FieldsNihlUtils fieldsNihlUtils;

    @Override
    public void validate(CaseData caseData, List<String> errors) {
        if (caseData.getSdoR2Trial() != null
                && caseData.getSdoR2Trial().getSdoR2TrialWindow() != null
                && caseData.getSdoR2Trial().getSdoR2TrialWindow().getListFrom() != null) {
            log.debug("Validating Trial Window List From");
            fieldsNihlUtils.validateFutureDate(caseData.getSdoR2Trial().getSdoR2TrialWindow().getListFrom()).ifPresent(error -> {
                log.warn("Trial Window List From validation failed: {}", error);
                errors.add(error);
            });
        }
    }
}
