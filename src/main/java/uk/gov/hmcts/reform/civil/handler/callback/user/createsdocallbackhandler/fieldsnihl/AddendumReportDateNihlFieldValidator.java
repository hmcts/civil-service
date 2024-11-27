package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.fieldsnihl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AddendumReportDateNihlFieldValidator implements NihlFieldValidator {

    private final FieldsNihlUtils fieldsNihlUtils;

    @Override
    public void validate(CaseData caseData, List<String> errors) {
        if (caseData.getSdoR2AddendumReport() != null && caseData.getSdoR2AddendumReport().getSdoAddendumReportDate() != null) {
            log.debug("Validating Addendum Report Date");
            fieldsNihlUtils.validateFutureDate(caseData.getSdoR2AddendumReport().getSdoAddendumReportDate()).ifPresent(error -> {
                log.warn("Addendum Report Date validation failed: {}", error);
                errors.add(error);
            });
        }
    }
}
