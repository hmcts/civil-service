package uk.gov.hmcts.reform.civil.handler.callback.user.spec;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.proceed.confirmation.AcceptPartAdmitAndPaidConfText;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.proceed.confirmation.AdmitNotProceedConfText;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.proceed.confirmation.AdmitProceedConfText;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.proceed.confirmation.DefendNotProceedConfText;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.proceed.confirmation.DefendProceedConfText;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.proceed.confirmation.JudgmentByAdmissionConfText;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.proceed.confirmation.JudgmentSubmittedConfText;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.proceed.confirmation.PayImmediatelyConfText;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.proceed.confirmation.ProposePaymentPlanConfText;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.proceed.confirmation.RejectWithMediationConfText;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.proceed.confirmation.RejectWithoutMediationConfText;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.RespondToResponseConfirmationHeaderGeneratorTest.buildAcceptPartAdmitAndPaidCaseData;
import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.RespondToResponseConfirmationHeaderGeneratorTest.buildCaseClaimantWithOutMediationData;
import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.RespondToResponseConfirmationHeaderGeneratorTest.buildCaseDefendantWithOutMediationData;
import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.RespondToResponseConfirmationHeaderGeneratorTest.buildCaseWithMediation;
import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.RespondToResponseConfirmationHeaderGeneratorTest.buildCaseWithOutMediationFastTrackData;
import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.RespondToResponseConfirmationHeaderGeneratorTest.buildFullAdmitNotProceedCaseData;
import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.RespondToResponseConfirmationHeaderGeneratorTest.buildFullAdmitPayImmediatelyProceedCaseData;
import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.RespondToResponseConfirmationHeaderGeneratorTest.buildFullAdmitProceedCaseData;
import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.RespondToResponseConfirmationHeaderGeneratorTest.buildFullDefenceNotProceedCaseData;
import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.RespondToResponseConfirmationHeaderGeneratorTest.buildFullDefenceProceedCaseData;
import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.RespondToResponseConfirmationHeaderGeneratorTest.buildJudgmentSubmitProceedCaseData;
import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.RespondToResponseConfirmationHeaderGeneratorTest.buildJudgmentSubmitProceedCaseDataAllFoi;
import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.RespondToResponseConfirmationHeaderGeneratorTest.buildPartAdmitNotProceedCaseData;
import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.RespondToResponseConfirmationHeaderGeneratorTest.buildPartAdmitPayImmediatelyProceedCaseData;
import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.RespondToResponseConfirmationHeaderGeneratorTest.buildPartAdmitProceedCaseData;
import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.RespondToResponseConfirmationHeaderGeneratorTest.buildProposePaymentPlanCaseData;

public class RespondToResponseConfirmationTextGeneratorTest implements CaseDataToTextGeneratorTest
        .CaseDataToTextGeneratorIntentionConfig<RespondToResponseConfirmationTextGenerator> {

    @Test
    void shouldGenerateAdmitProceedConfirmationText() {
        CaseData caseData = buildFullAdmitProceedCaseData();

        assertThat(new AdmitProceedConfText().generateTextFor(caseData, null)).contains(
                "<br>You've chosen to proceed with the claim.&nbsp;"
                        + "This means that your claim cannot continue online."
                        + "<br>We'll review the case and contact you about what to do next"
        );
    }

    @Test
    void shouldGenerateRejectWithoutMediationConfirmationText() {
        CaseData caseData = buildCaseDefendantWithOutMediationData();

        assertThat(new RejectWithoutMediationConfText().generateTextFor(caseData, null)).hasValueSatisfying(text ->
                assertThat(text)
                        .contains("<h2 class=\"govuk-heading-m\">What happens next</h2>")
                        .contains("We'll review the case and contact you about what to do next")
                        .contains(String.format("/cases/case-details/%s#Claim documents", caseData.getCcdCaseReference()))
        );
    }

    @Test
    void shouldGenerateJudgmentByAdmissionConfirmationText() {
        CaseData caseData = buildJudgmentSubmitProceedCaseDataAllFoi();

        assertThat(new JudgmentByAdmissionConfText().generateTextFor(caseData, null)).hasValueSatisfying(text ->
                assertThat(text)
                        .contains("Download county court judgment")
                        .contains("The defendant will be served the county court judgment")
                        .contains(String.format("/cases/case-details/%s#Claim documents", caseData.getCcdCaseReference()))
        );
    }

    @Test
    void shouldReturnEmptyForAdmitProceedConfirmationTextWhenClaimantAgreedToFreeMediation() {
        CaseData caseData = RespondToResponseConfirmationHeaderGeneratorTest.buildFullAdmitProceedCaseDataWithMediation();

        assertThat(new AdmitProceedConfText().generateTextFor(caseData, null)).isEmpty();
    }

    @Test
    void shouldGenerateJudgmentByAdmissionConfirmationTextForInstallmentPlan() {
        CaseData caseData = RespondToResponseConfirmationHeaderGeneratorTest.buildJudgmentSubmitProceedCaseDataInstallment();

        assertThat(new JudgmentByAdmissionConfText().generateTextFor(caseData, null)).hasValueSatisfying(text ->
                assertThat(text)
                        .contains("Download county court judgment")
                        .contains("The defendant will be served the county court judgment")
        );
    }

    @Test
    void shouldReturnEmptyForJudgmentByAdmissionConfirmationTextWhenPaymentRouteIsNotEligible() {
        CaseData caseData = buildJudgmentSubmitProceedCaseDataAllFoi();
        caseData.setDefenceAdmitPartPaymentTimeRouteRequired(
                uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY
        );

        assertThat(new JudgmentByAdmissionConfText().generateTextFor(caseData, null)).isEmpty();
    }

    @Override
    public Class<RespondToResponseConfirmationTextGenerator> getIntentionInterface() {
        return RespondToResponseConfirmationTextGenerator.class;
    }

    @Override
    public List<Pair<CaseData,
            Class<? extends RespondToResponseConfirmationTextGenerator>>>
    getCasesToExpectedImplementation() {
        return List.of(
                Pair.of(buildFullAdmitPayImmediatelyProceedCaseData(), PayImmediatelyConfText.class),
                Pair.of(buildPartAdmitPayImmediatelyProceedCaseData(), PayImmediatelyConfText.class),
                Pair.of(buildFullAdmitProceedCaseData(), AdmitProceedConfText.class),
                Pair.of(buildFullAdmitNotProceedCaseData(), AdmitNotProceedConfText.class),
                Pair.of(buildPartAdmitProceedCaseData(), AdmitProceedConfText.class),
                Pair.of(buildPartAdmitNotProceedCaseData(), AdmitNotProceedConfText.class),
                Pair.of(buildFullDefenceProceedCaseData(), DefendProceedConfText.class),
                Pair.of(buildFullDefenceNotProceedCaseData(), DefendNotProceedConfText.class),
                Pair.of(buildJudgmentSubmitProceedCaseData(), JudgmentSubmittedConfText.class),
                Pair.of(buildProposePaymentPlanCaseData(), ProposePaymentPlanConfText.class),
                Pair.of(buildCaseWithMediation(), RejectWithMediationConfText.class),
                Pair.of(buildAcceptPartAdmitAndPaidCaseData(), AcceptPartAdmitAndPaidConfText.class),
                Pair.of(buildCaseDefendantWithOutMediationData(), RejectWithoutMediationConfText.class),
                Pair.of(buildCaseWithOutMediationFastTrackData(), RejectWithoutMediationConfText.class),
                Pair.of(buildCaseClaimantWithOutMediationData(), RejectWithoutMediationConfText.class),
                Pair.of(buildJudgmentSubmitProceedCaseDataAllFoi(), JudgmentByAdmissionConfText.class)
        );
    }
}
