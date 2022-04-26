package uk.gov.hmcts.reform.civil.handler.callback.user.spec;

import org.apache.commons.lang3.tuple.Pair;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.SuperClaimType;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.proceed.confirmation.AdmitNotProceedConfHeader;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.proceed.confirmation.AdmitProceedConfHeader;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.proceed.confirmation.DefendNotProceedConfHeader;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.proceed.confirmation.DefendProceedConfHeader;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.List;

public class RespondToResponseConfirmationHeaderGeneratorTest implements CaseDataToTextGeneratorTest
    .CaseDataToTextGeneratorIntentionConfig<RespondToResponseConfirmationHeaderGenerator> {

    @Override
    public Class<RespondToResponseConfirmationHeaderGenerator> getIntentionInterface() {
        return RespondToResponseConfirmationHeaderGenerator.class;
    }

    @Override
    public List<Pair<CaseData,
        Class<? extends RespondToResponseConfirmationHeaderGenerator>>>
        getCasesToExpectedImplementation() {
        return List.of(
            Pair.of(buildFullAdmitProceedCaseData(), AdmitProceedConfHeader.class),
            Pair.of(buildFullAdmitNotProceedCaseData(), AdmitNotProceedConfHeader.class),
            Pair.of(buildPartAdmitProceedCaseData(), AdmitProceedConfHeader.class),
            Pair.of(buildPartAdmitNotProceedCaseData(), AdmitNotProceedConfHeader.class),
            Pair.of(buildFullDefenceProceedCaseData(), DefendProceedConfHeader.class),
            Pair.of(buildFullDefenceNotProceedCaseData(), DefendNotProceedConfHeader.class)
        );
    }

    public static CaseData buildFullDefenceProceedCaseData() {
        return CaseData.builder()
            .superClaimType(SuperClaimType.SPEC_CLAIM)
            .legacyCaseReference("claimNumber")
            .applicant1ProceedWithClaim(YesOrNo.YES)
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
            .build();
    }

    public static CaseData buildFullDefenceNotProceedCaseData() {
        return CaseData.builder()
            .superClaimType(SuperClaimType.SPEC_CLAIM)
            .legacyCaseReference("claimNumber")
            .applicant1ProceedWithClaim(YesOrNo.NO)
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
            .build();
    }

    public static CaseData buildFullAdmitProceedCaseData() {
        return CaseData.builder()
            .superClaimType(SuperClaimType.SPEC_CLAIM)
            .legacyCaseReference("claimNumber")
            .applicant1ProceedWithClaim(YesOrNo.YES)
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
            .build();
    }

    public static CaseData buildFullAdmitNotProceedCaseData() {
        return CaseData.builder()
            .superClaimType(SuperClaimType.SPEC_CLAIM)
            .legacyCaseReference("claimNumber")
            .applicant1ProceedWithClaim(YesOrNo.NO)
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
            .build();
    }

    public static CaseData buildPartAdmitProceedCaseData() {
        return CaseData.builder()
            .superClaimType(SuperClaimType.SPEC_CLAIM)
            .legacyCaseReference("claimNumber")
            .applicant1ProceedWithClaim(YesOrNo.YES)
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
            .build();
    }

    public static CaseData buildPartAdmitNotProceedCaseData() {
        return CaseData.builder()
            .superClaimType(SuperClaimType.SPEC_CLAIM)
            .legacyCaseReference("claimNumber")
            .applicant1ProceedWithClaim(YesOrNo.NO)
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
            .build();
    }
}
