package uk.gov.hmcts.reform.civil.service.sdo;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.callback.CallbackVersion;
import uk.gov.hmcts.reform.civil.handler.callback.user.sdo.tasks.SdoTaskContext;
import uk.gov.hmcts.reform.civil.enums.sdo.DisposalHearingMethod;
import uk.gov.hmcts.reform.civil.enums.sdo.FastTrackMethod;
import uk.gov.hmcts.reform.civil.enums.sdo.HearingMethod;
import uk.gov.hmcts.reform.civil.enums.sdo.SmallClaimsMethod;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingJudgementDeductionValue;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackJudgementDeductionValue;
import uk.gov.hmcts.reform.civil.model.sdo.JudgementSum;
import uk.gov.hmcts.reform.civil.model.sdo.SmallClaimsJudgementDeductionValue;

import java.util.Optional;

import static uk.gov.hmcts.reform.civil.callback.CallbackVersion.V_1;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@Service
@RequiredArgsConstructor
public class SdoOrderDetailsService {

    private final SdoCaseClassificationService caseClassificationService;

    public CaseData updateOrderDetails(SdoTaskContext context) {
        CaseData caseData = context.caseData();
        CaseData.CaseDataBuilder<?, ?> updatedData = caseData.toBuilder();

        updateDeductionValue(caseData, updatedData);
        applyTrackFlags(caseData, updatedData);
        mapHearingMethodFields(caseData, updatedData, context.callbackParams().getVersion());

        return updatedData.build();
    }

    private void applyTrackFlags(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedData) {
        updatedData.setSmallClaimsFlag(NO);
        updatedData.setFastTrackFlag(NO);
        updatedData.isSdoR2NewScreen(NO);

        if (caseClassificationService.isSmallClaimsTrack(caseData)) {
            updatedData.setSmallClaimsFlag(YES);
            if (caseClassificationService.isDrhSmallClaim(caseData)) {
                updatedData.isSdoR2NewScreen(YES);
            }
        } else if (caseClassificationService.isFastTrack(caseData)) {
            updatedData.setFastTrackFlag(YES);
            if (caseClassificationService.isNihlFastTrack(caseData)) {
                updatedData.isSdoR2NewScreen(YES);
            }
        }
    }

    private void updateDeductionValue(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedData) {
        Optional.ofNullable(caseData.getDrawDirectionsOrder())
            .map(JudgementSum::getJudgementSum)
            .map(d -> d + "%")
            .ifPresent(deductionPercentage -> {
                DisposalHearingJudgementDeductionValue disposalValue =
                    DisposalHearingJudgementDeductionValue.builder()
                        .value(deductionPercentage)
                        .build();
                updatedData.disposalHearingJudgementDeductionValue(disposalValue);

                FastTrackJudgementDeductionValue fastTrackValue =
                    FastTrackJudgementDeductionValue.builder()
                        .value(deductionPercentage)
                        .build();
                updatedData.fastTrackJudgementDeductionValue(fastTrackValue);

                SmallClaimsJudgementDeductionValue smallClaimsValue =
                    SmallClaimsJudgementDeductionValue.builder()
                        .value(deductionPercentage)
                        .build();
                updatedData.smallClaimsJudgementDeductionValue(smallClaimsValue);
            });
    }

    private void mapHearingMethodFields(
        CaseData caseData,
        CaseData.CaseDataBuilder<?, ?> updatedData,
        CallbackVersion version
    ) {
        if (!V_1.equals(version)) {
            return;
        }

        if (caseData.getHearingMethodValuesDisposalHearing() != null
            && caseData.getHearingMethodValuesDisposalHearing().getValue() != null) {
            String disposalHearingMethodLabel = caseData.getHearingMethodValuesDisposalHearing().getValue().getLabel();
            if (disposalHearingMethodLabel.equals(HearingMethod.IN_PERSON.getLabel())) {
                updatedData.disposalHearingMethod(DisposalHearingMethod.disposalHearingMethodInPerson);
            } else if (disposalHearingMethodLabel.equals(HearingMethod.VIDEO.getLabel())) {
                updatedData.disposalHearingMethod(DisposalHearingMethod.disposalHearingMethodVideoConferenceHearing);
            } else if (disposalHearingMethodLabel.equals(HearingMethod.TELEPHONE.getLabel())) {
                updatedData.disposalHearingMethod(DisposalHearingMethod.disposalHearingMethodTelephoneHearing);
            }
        } else if (caseData.getHearingMethodValuesFastTrack() != null
            && caseData.getHearingMethodValuesFastTrack().getValue() != null) {
            String fastTrackHearingMethodLabel = caseData.getHearingMethodValuesFastTrack().getValue().getLabel();
            if (fastTrackHearingMethodLabel.equals(HearingMethod.IN_PERSON.getLabel())) {
                updatedData.fastTrackMethod(FastTrackMethod.fastTrackMethodInPerson);
            } else if (fastTrackHearingMethodLabel.equals(HearingMethod.VIDEO.getLabel())) {
                updatedData.fastTrackMethod(FastTrackMethod.fastTrackMethodVideoConferenceHearing);
            } else if (fastTrackHearingMethodLabel.equals(HearingMethod.TELEPHONE.getLabel())) {
                updatedData.fastTrackMethod(FastTrackMethod.fastTrackMethodTelephoneHearing);
            }
        } else if (caseData.getHearingMethodValuesSmallClaims() != null
            && caseData.getHearingMethodValuesSmallClaims().getValue() != null) {
            String smallClaimsHearingMethodLabel = caseData.getHearingMethodValuesSmallClaims().getValue().getLabel();
            if (smallClaimsHearingMethodLabel.equals(HearingMethod.IN_PERSON.getLabel())) {
                updatedData.smallClaimsMethod(SmallClaimsMethod.smallClaimsMethodInPerson);
            } else if (smallClaimsHearingMethodLabel.equals(HearingMethod.VIDEO.getLabel())) {
                updatedData.smallClaimsMethod(SmallClaimsMethod.smallClaimsMethodVideoConferenceHearing);
            } else if (smallClaimsHearingMethodLabel.equals(HearingMethod.TELEPHONE.getLabel())) {
                updatedData.smallClaimsMethod(SmallClaimsMethod.smallClaimsMethodTelephoneHearing);
            }
        }
    }
}
