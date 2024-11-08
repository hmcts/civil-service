package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.fieldsnihl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class WitnessDefendantCountNihlFieldValidator implements NihlFieldValidator {

    private final FieldsNihlUtils fieldsNihlUtils;

    @Override
    public void validate(CaseData caseData, List<String> errors) {
        if (caseData.getSdoR2WitnessesOfFact() != null
                && caseData.getSdoR2WitnessesOfFact().getSdoR2RestrictWitness() != null
                && caseData.getSdoR2WitnessesOfFact().getSdoR2RestrictWitness().getRestrictNoOfWitnessDetails() != null
                && caseData.getSdoR2WitnessesOfFact().getSdoR2RestrictWitness().getRestrictNoOfWitnessDetails().getNoOfWitnessDefendant() != null) {
            log.debug("Validating Witness Defendant Count");
            fieldsNihlUtils.validateGreaterOrEqualZero(caseData.getSdoR2WitnessesOfFact()
                            .getSdoR2RestrictWitness()
                            .getRestrictNoOfWitnessDetails()
                            .getNoOfWitnessDefendant())
                    .ifPresent(error -> {
                        log.warn("Witness Defendant Count validation failed: {}", error);
                        errors.add(error);
                    });
        }
    }
}
