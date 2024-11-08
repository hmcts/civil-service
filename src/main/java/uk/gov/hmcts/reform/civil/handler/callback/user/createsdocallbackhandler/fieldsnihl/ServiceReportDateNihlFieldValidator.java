package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.fieldsnihl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ServiceReportDateNihlFieldValidator implements NihlFieldValidator {

    private final FieldsNihlUtils fieldsNihlUtils;

    @Override
    public void validate(CaseData caseData, List<String> errors) {
        if (caseData.getSdoR2FurtherAudiogram() != null && caseData.getSdoR2FurtherAudiogram().getSdoServiceReportDate() != null) {
            log.debug("Validating Service Report Date");
            fieldsNihlUtils.validateFutureDate(caseData.getSdoR2FurtherAudiogram().getSdoServiceReportDate()).ifPresent(error -> {
                log.warn("Service Report Date validation failed: {}", error);
                errors.add(error);
            });
        }
    }
}
