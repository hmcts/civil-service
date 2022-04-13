package uk.gov.hmcts.reform.civil.handler.callback.user.spec.proceed.confirmation;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.RespondToResponseConfirmationHeaderGenerator;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

import static java.lang.String.format;

@Component
public class AdmitNotProceedConfHeader implements RespondToResponseConfirmationHeaderGenerator {

    private static final Set<RespondentResponseTypeSpec> ADMISSION = EnumSet.of(
        RespondentResponseTypeSpec.FULL_ADMISSION,
        RespondentResponseTypeSpec.PART_ADMISSION
    );

    @Override
    public Optional<String> generateTextFor(CaseData caseData) {
        if (YesOrNo.YES.equals(caseData.getApplicant1ProceedWithClaim())
            || !ADMISSION.contains(caseData.getRespondent1ClaimResponseTypeForSpec())) {
            return Optional.empty();
        }
        String claimNumber = caseData.getLegacyCaseReference();
        return Optional.of(format(
            "# You have chosen not to proceed%n## Claim number: %s",
            claimNumber
        ));
    }
}
