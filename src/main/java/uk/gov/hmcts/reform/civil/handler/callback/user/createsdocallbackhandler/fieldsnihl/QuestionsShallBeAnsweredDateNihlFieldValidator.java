package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.fieldsnihl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class QuestionsShallBeAnsweredDateNihlFieldValidator implements NihlFieldValidator {

    private final FieldsNihlUtils fieldsNihlUtils;

    @Override
    public void validate(CaseData caseData, List<String> errors) {
        if (caseData.getSdoR2QuestionsClaimantExpert() != null && caseData.getSdoR2QuestionsClaimantExpert().getSdoQuestionsShallBeAnsweredDate() != null) {
            log.debug("Validating Questions Shall Be Answered Date");
            fieldsNihlUtils.validateFutureDate(caseData.getSdoR2QuestionsClaimantExpert().getSdoQuestionsShallBeAnsweredDate()).ifPresent(error -> {
                log.warn("Questions Shall Be Answered Date validation failed: {}", error);
                errors.add(error);
            });
        }
    }
}
