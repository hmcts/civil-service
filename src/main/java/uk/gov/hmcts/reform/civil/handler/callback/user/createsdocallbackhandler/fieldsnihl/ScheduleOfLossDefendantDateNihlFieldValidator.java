package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.fieldsnihl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduleOfLossDefendantDateNihlFieldValidator implements NihlFieldValidator {

    private final FieldsNihlUtils fieldsNihlUtils;

    @Override
    public void validate(CaseData caseData, List<String> errors) {
        if (caseData.getSdoR2ScheduleOfLoss() != null
                && caseData.getSdoR2ScheduleOfLoss().getSdoR2ScheduleOfLossDefendantDate() != null) {
            log.debug("Validating Schedule Of Loss Defendant Date");
            fieldsNihlUtils.validateFutureDate(caseData.getSdoR2ScheduleOfLoss().getSdoR2ScheduleOfLossDefendantDate()).ifPresent(error -> {
                log.warn("Schedule Of Loss Defendant Date validation failed: {}", error);
                errors.add(error);
            });
        }
    }
}
