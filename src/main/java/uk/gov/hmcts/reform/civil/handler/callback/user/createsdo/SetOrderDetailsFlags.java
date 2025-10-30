package uk.gov.hmcts.reform.civil.handler.callback.user.createsdo;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.sdo.OrderType;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.CaseTask;
import uk.gov.hmcts.reform.civil.helpers.sdo.SdoHelper;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingJudgementDeductionValue;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackJudgementDeductionValue;
import uk.gov.hmcts.reform.civil.model.sdo.JudgementSum;
import uk.gov.hmcts.reform.civil.model.sdo.SmallClaimsJudgementDeductionValue;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.constants.CreateSDOText.ERROR_MINTI_DISPOSAL_NOT_ALLOWED;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@Component
@RequiredArgsConstructor
@Slf4j
public class SetOrderDetailsFlags implements CaseTask {

    private final ObjectMapper objectMapper;
    private final FeatureToggleService featureToggleService;

    public CallbackResponse execute(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        log.info("Executing SetOrderDetailsFlags for caseId: {}", caseData.getCcdCaseReference());
        CaseData.CaseDataBuilder<?, ?> updatedData = caseData.toBuilder();

        if (isMultiOrIntermediateTrackClaim(caseData)
                && OrderType.DISPOSAL.equals(caseData.getOrderType())
                && CaseState.JUDICIAL_REFERRAL.equals(caseData.getCcdState())) {
            return AboutToStartOrSubmitCallbackResponse.builder()
                    .errors(List.of(ERROR_MINTI_DISPOSAL_NOT_ALLOWED))
                    .build();
        }

        log.debug("Updating deduction values for caseId: {}", caseData.getCcdCaseReference());
        updateDeductionValue(caseData, updatedData);

        log.debug("Resetting flags for caseId: {}", caseData.getCcdCaseReference());
        resetFlags(updatedData);

        log.debug("Setting isSdoR2NewScreen to NO for caseId: {}", caseData.getCcdCaseReference());
        updatedData.isSdoR2NewScreen(NO).build();

        log.debug("Updating flags based on track for caseId: {}", caseData.getCcdCaseReference());
        updateFlagsBasedOnTrack(caseData, updatedData);

        return AboutToStartOrSubmitCallbackResponse.builder()
                .data(updatedData.build().toMap(objectMapper))
                .build();
    }

    private void resetFlags(CaseData.CaseDataBuilder<?, ?> updatedData) {
        log.debug("Resetting smallClaimsFlag and fastTrackFlag to NO");
        updatedData.setSmallClaimsFlag(NO).build();
        updatedData.setFastTrackFlag(NO).build();
    }

    private void updateFlagsBasedOnTrack(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedData) {
        if (SdoHelper.isSmallClaimsTrack(caseData)) {
            log.debug("Case is on Small Claims track for caseId: {}", caseData.getCcdCaseReference());
            updatedData.setSmallClaimsFlag(YES).build();
            if (SdoHelper.isSDOR2ScreenForDRHSmallClaim(caseData)) {
                log.debug("Enabling SdoR2 new screen for DRH Small Claim for caseId: {}", caseData.getCcdCaseReference());
                updatedData.isSdoR2NewScreen(YES).build();
            }
        } else if (SdoHelper.isFastTrack(caseData)) {
            log.debug("Case is on Fast Track for caseId: {}", caseData.getCcdCaseReference());
            updatedData.setFastTrackFlag(YES).build();
            if (SdoHelper.isNihlFastTrack(caseData)) {
                log.debug("Enabling SdoR2 new screen for NIHL Fast Track for caseId: {}", caseData.getCcdCaseReference());
                updatedData.isSdoR2NewScreen(YES).build();
            }
        }
    }

    private void updateDeductionValue(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedData) {
        Optional.ofNullable(caseData.getDrawDirectionsOrder())
                .map(JudgementSum::getJudgementSum)
                .map(d -> d + "%")
                .ifPresent(deductionPercentage -> {
                    log.debug("Updating DisposalHearingJudgementDeductionValue with value: {} for caseId: {}", deductionPercentage, caseData.getCcdCaseReference());
                    DisposalHearingJudgementDeductionValue tempDisposalHearingJudgementDeductionValue =
                            DisposalHearingJudgementDeductionValue.builder()
                                    .value(deductionPercentage)
                                    .build();
                    updatedData.disposalHearingJudgementDeductionValue(tempDisposalHearingJudgementDeductionValue);

                    log.debug("Updating FastTrackJudgementDeductionValue with value: {} for caseId: {}", deductionPercentage, caseData.getCcdCaseReference());
                    FastTrackJudgementDeductionValue tempFastTrackJudgementDeductionValue =
                            FastTrackJudgementDeductionValue.builder()
                                    .value(deductionPercentage)
                                    .build();
                    updatedData.fastTrackJudgementDeductionValue(tempFastTrackJudgementDeductionValue).build();

                    log.debug("Updating SmallClaimsJudgementDeductionValue with value: {} for caseId: {}", deductionPercentage, caseData.getCcdCaseReference());
                    SmallClaimsJudgementDeductionValue tempSmallClaimsJudgementDeductionValue =
                            SmallClaimsJudgementDeductionValue.builder()
                                    .value(deductionPercentage)
                                    .build();
                    updatedData.smallClaimsJudgementDeductionValue(tempSmallClaimsJudgementDeductionValue).build();
                });
    }

    private boolean isMultiOrIntermediateTrackClaim(CaseData caseData) {
        return featureToggleService.isMultiOrIntermediateTrackEnabled(caseData)
                && (AllocatedTrack.INTERMEDIATE_CLAIM.equals(caseData.getAllocatedTrack())
                || AllocatedTrack.INTERMEDIATE_CLAIM.name().equals(caseData.getResponseClaimTrack())
                || AllocatedTrack.MULTI_CLAIM.equals(caseData.getAllocatedTrack())
                || AllocatedTrack.MULTI_CLAIM.name().equals(caseData.getResponseClaimTrack()));
    }
}
