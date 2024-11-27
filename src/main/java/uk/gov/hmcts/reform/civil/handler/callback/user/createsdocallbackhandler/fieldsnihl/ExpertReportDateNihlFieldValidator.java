package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.fieldsnihl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExpertReportDateNihlFieldValidator implements NihlFieldValidator {

    private final FieldsNihlUtils fieldsNihlUtils;

    @Override
    public void validate(CaseData caseData, List<String> errors) {
        if (caseData.getSdoR2EvidenceAcousticEngineer() != null
                && caseData.getSdoR2EvidenceAcousticEngineer().getSdoExpertReportDate() != null) {
            log.debug("Validating Expert Report Date");
            fieldsNihlUtils.validateFutureDate(caseData.getSdoR2EvidenceAcousticEngineer().getSdoExpertReportDate()).ifPresent(error -> {
                log.warn("Expert Report Date validation failed: {}", error);
                errors.add(error);
            });
        }
    }
}
