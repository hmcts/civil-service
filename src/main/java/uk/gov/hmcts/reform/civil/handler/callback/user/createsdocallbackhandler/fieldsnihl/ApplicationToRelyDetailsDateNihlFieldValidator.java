package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.fieldsnihl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ApplicationToRelyDetailsDateNihlFieldValidator implements NihlFieldValidator {

    private final FieldsNihlUtils fieldsNihlUtils;

    @Override
    public void validate(CaseData caseData, List<String> errors) {
        if (caseData.getSdoR2QuestionsClaimantExpert() != null
                && caseData.getSdoR2QuestionsClaimantExpert().getSdoApplicationToRelyOnFurther() != null
                && caseData.getSdoR2QuestionsClaimantExpert().getSdoApplicationToRelyOnFurther().getApplicationToRelyOnFurtherDetails() != null
                && caseData.getSdoR2QuestionsClaimantExpert().getSdoApplicationToRelyOnFurther().getApplicationToRelyOnFurtherDetails().getApplicationToRelyDetailsDate() != null) {
            log.debug("Validating Application To Rely Details Date");
            fieldsNihlUtils.validateFutureDate(caseData.getSdoR2QuestionsClaimantExpert()
                            .getSdoApplicationToRelyOnFurther()
                            .getApplicationToRelyOnFurtherDetails()
                            .getApplicationToRelyDetailsDate())
                    .ifPresent(error -> {
                        log.warn("Application To Rely Details Date validation failed: {}", error);
                        errors.add(error);
                    });
        }
    }
}
