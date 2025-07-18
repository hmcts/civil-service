package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.fieldsnihl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class TrialFirstOpenDateAfterNihlFieldValidator implements NihlFieldValidator {

    private final FieldsNihlUtils fieldsNihlUtils;

    @Override
    public void validate(CaseData caseData, List<String> errors) {
        if (caseData.getSdoR2Trial() != null
                && caseData.getSdoR2Trial().getSdoR2TrialFirstOpenDateAfter() != null
                && caseData.getSdoR2Trial().getSdoR2TrialFirstOpenDateAfter().getListFrom() != null) {
            log.debug("Validating Trial First Open Date After");
            fieldsNihlUtils.validateFutureDate(caseData.getSdoR2Trial().getSdoR2TrialFirstOpenDateAfter().getListFrom()).ifPresent(error -> {
                log.warn("Trial First Open Date After validation failed: {}", error);
                errors.add(error);
            });
        }
    }
}
