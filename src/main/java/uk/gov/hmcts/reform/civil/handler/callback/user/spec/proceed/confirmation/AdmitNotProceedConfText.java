package uk.gov.hmcts.reform.civil.handler.callback.user.spec.proceed.confirmation;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.RespondToResponseConfirmationTextGenerator;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

@Component
public class AdmitNotProceedConfText implements RespondToResponseConfirmationTextGenerator {

    private static final Set<RespondentResponseTypeSpec> ADMISSION = EnumSet.of(
        RespondentResponseTypeSpec.FULL_ADMISSION,
        RespondentResponseTypeSpec.PART_ADMISSION
    );

    @Override
    public Optional<String> generateTextFor(CaseData caseData) {
        if ((caseData.getApplicant1ProceedsWithClaimSpec() == null)
            || YesOrNo.YES.equals(caseData.getApplicant1ProceedsWithClaimSpec())
            || !ADMISSION.contains(caseData.getRespondent1ClaimResponseTypeForSpec())) {
            return Optional.empty();
        }
        return Optional.of("<br>You've decided not to proceed with the claim.&nbsp;"
                               + "We'll contact the defendant to tell them your decision.");
    }
}
