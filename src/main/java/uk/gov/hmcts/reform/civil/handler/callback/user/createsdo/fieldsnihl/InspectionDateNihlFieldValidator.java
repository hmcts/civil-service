package uk.gov.hmcts.reform.civil.handler.callback.user.createsdo.fieldsnihl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class InspectionDateNihlFieldValidator implements NihlFieldValidator {

    private final FieldsNihlUtils fieldsNihlUtils;

    @Override
    public void validate(CaseData caseData, List<String> errors) {
        if (caseData.getSdoR2DisclosureOfDocuments() != null && caseData.getSdoR2DisclosureOfDocuments().getInspectionDate() != null) {
            log.debug("Validating Inspection Date for caseId: {}", caseData.getCcdCaseReference());
            fieldsNihlUtils.validateFutureDate(caseData.getSdoR2DisclosureOfDocuments().getInspectionDate()).ifPresent(error -> {
                log.warn("Inspection Date validation failed: {} for caseId: {}", error, caseData.getCcdCaseReference());
                errors.add(error);
            });
        }
    }
}
