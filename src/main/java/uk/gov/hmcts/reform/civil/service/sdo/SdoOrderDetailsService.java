package uk.gov.hmcts.reform.civil.service.sdo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.callback.CallbackVersion;
import uk.gov.hmcts.reform.civil.handler.callback.user.directionsorder.tasks.DirectionsOrderTaskContext;
import uk.gov.hmcts.reform.civil.enums.sdo.DisposalHearingMethod;
import uk.gov.hmcts.reform.civil.enums.sdo.FastTrackMethod;
import uk.gov.hmcts.reform.civil.enums.sdo.HearingMethod;
import uk.gov.hmcts.reform.civil.enums.sdo.SmallClaimsMethod;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingJudgementDeductionValue;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackJudgementDeductionValue;
import uk.gov.hmcts.reform.civil.model.sdo.JudgementSum;
import uk.gov.hmcts.reform.civil.model.sdo.SmallClaimsJudgementDeductionValue;

import java.util.Arrays;
import java.util.function.Consumer;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.callback.CallbackVersion.V_1;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@Service
@RequiredArgsConstructor
@Slf4j
public class SdoOrderDetailsService {

    private final SdoCaseClassificationService caseClassificationService;

    public CaseData updateOrderDetails(DirectionsOrderTaskContext context) {
        CaseData caseData = context.caseData();

        updateDeductionValue(caseData);
        applyTrackFlags(caseData);
        mapHearingMethodFields(caseData, context.callbackParams().getVersion());

        return caseData;
    }

    private void applyTrackFlags(CaseData caseData) {
        caseData.setSetSmallClaimsFlag(NO);
        caseData.setSetFastTrackFlag(NO);
        caseData.setIsSdoR2NewScreen(NO);

        if (caseClassificationService.isSmallClaimsTrack(caseData)) {
            caseData.setSetSmallClaimsFlag(YES);
            if (caseClassificationService.isDrhSmallClaim(caseData)) {
                caseData.setIsSdoR2NewScreen(YES);
            }
        } else if (caseClassificationService.isFastTrack(caseData)) {
            caseData.setSetFastTrackFlag(YES);
            if (caseClassificationService.isNihlFastTrack(caseData)) {
                caseData.setIsSdoR2NewScreen(YES);
            }
        }
    }

    private void updateDeductionValue(CaseData caseData) {
        Optional.ofNullable(caseData.getDrawDirectionsOrder())
            .map(JudgementSum::getJudgementSum)
            .map(d -> d + "%")
            .ifPresent(deductionPercentage -> {
                DisposalHearingJudgementDeductionValue disposalValue =
                    DisposalHearingJudgementDeductionValue.builder()
                        .value(deductionPercentage)
                        .build();
                caseData.setDisposalHearingJudgementDeductionValue(disposalValue);

                FastTrackJudgementDeductionValue fastTrackValue =
                    FastTrackJudgementDeductionValue.builder()
                        .value(deductionPercentage)
                        .build();
                caseData.setFastTrackJudgementDeductionValue(fastTrackValue);

                SmallClaimsJudgementDeductionValue smallClaimsValue =
                    SmallClaimsJudgementDeductionValue.builder()
                        .value(deductionPercentage)
                        .build();
                caseData.setSmallClaimsJudgementDeductionValue(smallClaimsValue);
            });
    }

    private void mapHearingMethodFields(
        CaseData caseData,
        CallbackVersion version
    ) {
        if (!V_1.equals(version)) {
            return;
        }

        applyHearingMethod(caseData.getHearingMethodValuesDisposalHearing(), method -> {
            switch (method) {
                case IN_PERSON -> caseData.setDisposalHearingMethod(DisposalHearingMethod.disposalHearingMethodInPerson);
                case VIDEO -> caseData.setDisposalHearingMethod(DisposalHearingMethod.disposalHearingMethodVideoConferenceHearing);
                case TELEPHONE -> caseData.setDisposalHearingMethod(DisposalHearingMethod.disposalHearingMethodTelephoneHearing);
                default -> {
                    // No other values are expected;
                }
            }
        });

        applyHearingMethod(caseData.getHearingMethodValuesFastTrack(), method -> {
            switch (method) {
                case IN_PERSON -> caseData.setFastTrackMethod(FastTrackMethod.fastTrackMethodInPerson);
                case VIDEO -> caseData.setFastTrackMethod(FastTrackMethod.fastTrackMethodVideoConferenceHearing);
                case TELEPHONE -> caseData.setFastTrackMethod(FastTrackMethod.fastTrackMethodTelephoneHearing);
                default -> {
                    // No other values are expected;
                }
            }
        });

        applyHearingMethod(caseData.getHearingMethodValuesSmallClaims(), method -> {
            switch (method) {
                case IN_PERSON -> caseData.setSmallClaimsMethod(SmallClaimsMethod.smallClaimsMethodInPerson);
                case VIDEO -> caseData.setSmallClaimsMethod(SmallClaimsMethod.smallClaimsMethodVideoConferenceHearing);
                case TELEPHONE -> caseData.setSmallClaimsMethod(SmallClaimsMethod.smallClaimsMethodTelephoneHearing);
                default -> {
                    // No other values are expected;
                }
            }
        });
    }

    private void applyHearingMethod(
        DynamicList hearingMethodValues,
        Consumer<HearingMethod> consumer
    ) {
        if (hearingMethodValues == null || hearingMethodValues.getValue() == null) {
            return;
        }

        Arrays.stream(HearingMethod.values())
                .filter(value -> value.getLabel().equals(hearingMethodValues.getValue().getLabel()))
                .findFirst().ifPresent(consumer);

    }
}
