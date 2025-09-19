package uk.gov.hmcts.reform.civil.handler.callback.user.createsdo.fieldsnihl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class WitnessClaimantCountNihlFieldValidator implements NihlFieldValidator {

    private final FieldsNihlUtils fieldsNihlUtils;

    @Override
    public void validate(CaseData caseData, List<String> errors) {
        if (caseData.getSdoR2WitnessesOfFact() != null
                && caseData.getSdoR2WitnessesOfFact().getSdoR2RestrictWitness() != null
                && caseData.getSdoR2WitnessesOfFact().getSdoR2RestrictWitness().getRestrictNoOfWitnessDetails() != null
                && caseData.getSdoR2WitnessesOfFact().getSdoR2RestrictWitness().getRestrictNoOfWitnessDetails().getNoOfWitnessClaimant() != null) {
            log.debug("Validating Witness Claimant Count for caseId: {}", caseData.getCcdCaseReference());
            fieldsNihlUtils.validateGreaterOrEqualZero(caseData.getSdoR2WitnessesOfFact()
                            .getSdoR2RestrictWitness()
                            .getRestrictNoOfWitnessDetails()
                            .getNoOfWitnessClaimant())
                    .ifPresent(error -> {
                        log.warn("Witness Claimant Count validation failed: {} for caseId: {}", error, caseData.getCcdCaseReference());
                        errors.add(error);
                    });
        }
    }
}
