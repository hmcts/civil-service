package uk.gov.hmcts.reform.civil.handler.callback.user.spec;

import org.apache.commons.lang3.tuple.Pair;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.proceed.confirmation.AcceptPartAdmitAndPaidConfText;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.proceed.confirmation.AdmitNotProceedConfText;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.proceed.confirmation.AdmitProceedConfText;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.proceed.confirmation.DefendNotProceedConfText;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.proceed.confirmation.DefendProceedConfText;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.proceed.confirmation.JudgmentSubmittedConfText;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.proceed.confirmation.PayImmediatelyConfText;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.proceed.confirmation.ProposePaymentPlanConfText;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.proceed.confirmation.RejectWithMediationConfText;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.proceed.confirmation.RejectWithoutMediationConfText;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.List;

import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.RespondToResponseConfirmationHeaderGeneratorTest.buildCaseWithMediation;
import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.RespondToResponseConfirmationHeaderGeneratorTest.buildAcceptPartAdmitAndPaidCaseData;
import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.RespondToResponseConfirmationHeaderGeneratorTest.buildFullAdmitNotProceedCaseData;
import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.RespondToResponseConfirmationHeaderGeneratorTest.buildFullAdmitProceedCaseData;
import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.RespondToResponseConfirmationHeaderGeneratorTest.buildFullDefenceNotProceedCaseData;
import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.RespondToResponseConfirmationHeaderGeneratorTest.buildFullDefenceProceedCaseData;
import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.RespondToResponseConfirmationHeaderGeneratorTest.buildJudgmentSubmitProceedCaseData;
import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.RespondToResponseConfirmationHeaderGeneratorTest.buildPartAdmitNotProceedCaseData;
import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.RespondToResponseConfirmationHeaderGeneratorTest.buildPartAdmitProceedCaseData;
import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.RespondToResponseConfirmationHeaderGeneratorTest.buildFullAdmitPayImmediatelyProceedCaseData;
import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.RespondToResponseConfirmationHeaderGeneratorTest.buildProposePaymentPlanCaseData;
import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.RespondToResponseConfirmationHeaderGeneratorTest.buildCaseDefendantWithOutMediationData;
import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.RespondToResponseConfirmationHeaderGeneratorTest.buildCaseWithOutMediationFastTrackData;
import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.RespondToResponseConfirmationHeaderGeneratorTest.buildCaseClaimantWithOutMediationData;

public class RespondToResponseConfirmationTextGeneratorTest implements CaseDataToTextGeneratorTest
    .CaseDataToTextGeneratorIntentionConfig<RespondToResponseConfirmationTextGenerator> {

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
            Pair.of(buildCaseClaimantWithOutMediationData(), RejectWithoutMediationConfText.class)
        );
    }
}
