package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.fieldsnihl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class RepliesDateNihlFieldValidator implements NihlFieldValidator {

    private final FieldsNihlUtils fieldsNihlUtils;

    @Override
    public void validate(CaseData caseData, List<String> errors) {
        if (caseData.getSdoR2EvidenceAcousticEngineer() != null
                && caseData.getSdoR2EvidenceAcousticEngineer().getSdoRepliesDate() != null) {
            log.debug("Validating Replies Date");
            fieldsNihlUtils.validateFutureDate(caseData.getSdoR2EvidenceAcousticEngineer().getSdoRepliesDate()).ifPresent(error -> {
                log.warn("Replies Date validation failed: {}", error);
                errors.add(error);
            });
        }
    }
}
