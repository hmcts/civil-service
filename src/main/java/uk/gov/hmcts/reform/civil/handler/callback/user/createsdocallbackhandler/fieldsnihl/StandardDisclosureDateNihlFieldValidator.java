package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.fieldsnihl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class StandardDisclosureDateNihlFieldValidator implements NihlFieldValidator {

    private final FieldsNihlUtils fieldsNihlUtils;

    @Override
    public void validate(CaseData caseData, List<String> errors) {
        if (caseData.getSdoR2DisclosureOfDocuments() != null && caseData.getSdoR2DisclosureOfDocuments().getStandardDisclosureDate() != null) {
            log.debug("Validating Standard Disclosure Date for caseId: {}", caseData.getCcdCaseReference());
            fieldsNihlUtils.validateFutureDate(caseData.getSdoR2DisclosureOfDocuments().getStandardDisclosureDate()).ifPresent(error -> {
                log.warn("Standard Disclosure Date validation failed: {} for caseId: {}", error, caseData.getCcdCaseReference());
                errors.add(error);
            });
        }
    }

}
