package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.fieldsnihl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class WrittenQuestionsDateToEntExpertNihlFieldValidator implements NihlFieldValidator {

    private final FieldsNihlUtils fieldsNihlUtils;

    @Override
    public void validate(CaseData caseData, List<String> errors) {
        if (caseData.getSdoR2QuestionsToEntExpert() != null
                && caseData.getSdoR2QuestionsToEntExpert().getSdoWrittenQuestionsDate() != null) {
            log.debug("Validating Written Questions Date To ENT Expert");
            fieldsNihlUtils.validateFutureDate(caseData.getSdoR2QuestionsToEntExpert().getSdoWrittenQuestionsDate()).ifPresent(error -> {
                log.warn("Written Questions Date To ENT Expert validation failed: {}", error);
                errors.add(error);
            });
        }
    }
}
