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

        applyHearingMethod(caseData.getHearingMethodValuesDisposalHearing(), method -> {
            switch (method) {
                case IN_PERSON -> updatedData.disposalHearingMethod(DisposalHearingMethod.disposalHearingMethodInPerson);
                case VIDEO -> updatedData.disposalHearingMethod(DisposalHearingMethod.disposalHearingMethodVideoConferenceHearing);
                case TELEPHONE -> updatedData.disposalHearingMethod(DisposalHearingMethod.disposalHearingMethodTelephoneHearing);
                default -> {
                    // No other values are expected;
                }
            }
        });

        applyHearingMethod(caseData.getHearingMethodValuesFastTrack(), method -> {
            switch (method) {
                case IN_PERSON -> updatedData.fastTrackMethod(FastTrackMethod.fastTrackMethodInPerson);
                case VIDEO -> updatedData.fastTrackMethod(FastTrackMethod.fastTrackMethodVideoConferenceHearing);
                case TELEPHONE -> updatedData.fastTrackMethod(FastTrackMethod.fastTrackMethodTelephoneHearing);
                default -> {
                    // No other values are expected;
                }
            }
        });

        applyHearingMethod(caseData.getHearingMethodValuesSmallClaims(), method -> {
            switch (method) {
                case IN_PERSON -> updatedData.smallClaimsMethod(SmallClaimsMethod.smallClaimsMethodInPerson);
                case VIDEO -> updatedData.smallClaimsMethod(SmallClaimsMethod.smallClaimsMethodVideoConferenceHearing);
                case TELEPHONE -> updatedData.smallClaimsMethod(SmallClaimsMethod.smallClaimsMethodTelephoneHearing);
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
