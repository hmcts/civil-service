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
import static uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY;

@Component
public class AdmitProceedConfHeader implements RespondToResponseConfirmationHeaderGenerator {

    private static final Set<RespondentResponseTypeSpec> ADMISSION = EnumSet.of(
        RespondentResponseTypeSpec.FULL_ADMISSION,
        RespondentResponseTypeSpec.PART_ADMISSION
    );

    @Override
    public Optional<String> generateTextFor(CaseData caseData) {
        String claimNumber = caseData.getLegacyCaseReference();
        if (isdefendatFullAdmitPayImmidietely(caseData)) {
            return Optional.ofNullable(format(
                "# The defendant said they'll pay you immediately.%n## Claim number: %s",
                claimNumber
            ));
        } else if (caseData.getApplicant1ProceedsWithClaimSpec() == null
            || YesOrNo.NO.equals(caseData.getApplicant1ProceedsWithClaimSpec())
            || !ADMISSION.contains(caseData.getRespondent1ClaimResponseTypeForSpec())) {
            return Optional.empty();
        }
        return Optional.of(format(
            "# You have submitted your intention to proceed%n## Claim number: %s",
            claimNumber
        ));
    }

    private boolean isdefendatFullAdmitPayImmidietely(CaseData caseData) {
        return caseData.getDefenceAdmitPartPaymentTimeRouteRequired() != null
            &&  caseData.getDefenceAdmitPartPaymentTimeRouteRequired() == IMMEDIATELY
            && (RespondentResponseTypeSpec.FULL_ADMISSION.equals(caseData.getRespondent1ClaimResponseTypeForSpec()));
    }
}
