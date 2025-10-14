package uk.gov.hmcts.reform.civil.helpers.sdo;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.sdo.FastTrack;
import uk.gov.hmcts.reform.civil.enums.sdo.SmallTrack;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.helpers.sdo.strategy.DisposalHearingSdoStrategy;
import uk.gov.hmcts.reform.civil.helpers.sdo.strategy.FastTrackSdoStrategy;
import uk.gov.hmcts.reform.civil.helpers.sdo.strategy.NihlSdoStrategy;
import uk.gov.hmcts.reform.civil.helpers.sdo.strategy.SdoPartyStrategy;
import uk.gov.hmcts.reform.civil.helpers.sdo.strategy.SdoTrialStrategy;
import uk.gov.hmcts.reform.civil.helpers.sdo.strategy.SmallClaimsSdoStrategy;

@RequiredArgsConstructor
@Component
public class SdoHelper {

    private final SmallClaimsSdoStrategy smallClaimsStrategy;
    private final FastTrackSdoStrategy fastTrackStrategy;
    private final NihlSdoStrategy nihlStrategy;
    private final DisposalHearingSdoStrategy disposalStrategy;
    private final SdoTrialStrategy trialStrategy;
    private final SdoPartyStrategy partyStrategy;

    public boolean isSmallClaimsTrack(CaseData caseData) {
        return smallClaimsStrategy.isSmallClaimsTrack(caseData);
    }

    public boolean isFastTrack(CaseData caseData) {
        return fastTrackStrategy.isFastTrack(caseData);
    }

    public boolean isNihlFastTrack(CaseData caseData) {
        return nihlStrategy.isNihlFastTrack(caseData);
    }

    public DynamicList getHearingLocationNihl(CaseData caseData) {
        return nihlStrategy.getHearingLocationNihl(caseData);
    }

    public String getPhysicalTrialTextNihl(CaseData caseData) {
        return nihlStrategy.getPhysicalTrialTextNihl(caseData);
    }

    public boolean isRestrictWitnessNihl(CaseData caseData) {
        return nihlStrategy.isRestrictWitnessNihl(caseData);
    }

    public boolean isRestrictPagesNihl(CaseData caseData) {
        return nihlStrategy.isRestrictPagesNihl(caseData);
    }

    public String isApplicationToRelyOnFurtherNihl(CaseData caseData) {
        return nihlStrategy.isApplicationToRelyOnFurtherNihl(caseData);
    }

    public boolean isClaimForPecuniaryLossNihl(CaseData caseData) {
        return nihlStrategy.isClaimForPecuniaryLossNihl(caseData);
    }

    public boolean isSDOR2ScreenForDRHSmallClaim(CaseData caseData) {
        return smallClaimsStrategy.isSdoR2ScreenForDrhSmallClaim(caseData);
    }

    public DynamicList getHearingLocationDrh(CaseData caseData) {
        return smallClaimsStrategy.getHearingLocationDrh(caseData);
    }

    public boolean hasSdoR2HearingTrialWindow(CaseData caseData) {
        return smallClaimsStrategy.hasSdoR2HearingTrialWindow(caseData);
    }

    public String getSdoR2HearingTime(CaseData caseData) {
        return smallClaimsStrategy.getSdoR2HearingTime(caseData);
    }

    public String getSdoR2SmallClaimsHearingMethod(CaseData caseData) {
        return smallClaimsStrategy.getSdoR2SmallClaimsHearingMethod(caseData);
    }

    public String getSdoR2SmallClaimsPhysicalTrialBundleTxt(CaseData caseData) {
        return smallClaimsStrategy.getSdoR2SmallClaimsPhysicalTrialBundleText(caseData);
    }

    public boolean hasSharedVariable(CaseData caseData, String variableName) {
        return partyStrategy.hasSharedVariable(caseData, variableName);
    }

    public SmallTrack getSmallClaimsAdditionalDirectionEnum(String additionalDirection) {
        return smallClaimsStrategy.getSmallClaimsAdditionalDirectionEnum(additionalDirection);
    }

    public boolean hasSmallAdditionalDirections(CaseData caseData, String additionalDirection) {
        return smallClaimsStrategy.hasSmallAdditionalDirections(caseData, additionalDirection);
    }

    public String getDisposalHearingTimeLabel(CaseData caseData) {
        return disposalStrategy.getDisposalHearingTimeLabel(caseData);
    }

    public String getSmallClaimsHearingTimeLabel(CaseData caseData) {
        return smallClaimsStrategy.getSmallClaimsHearingTimeLabel(caseData);
    }

    public String getFastClaimsHearingTimeLabel(CaseData caseData) {
        return fastTrackStrategy.getFastClaimsHearingTimeLabel(caseData);
    }

    public String getSmallClaimsMethodTelephoneHearingLabel(CaseData caseData) {
        return smallClaimsStrategy.getSmallClaimsMethodTelephoneHearingLabel(caseData);
    }

    public String getSmallClaimsMethodVideoConferenceHearingLabel(CaseData caseData) {
        return smallClaimsStrategy.getSmallClaimsMethodVideoConferenceHearingLabel(caseData);
    }

    public boolean showCarmMediationSection(CaseData caseData, boolean carmEnabled) {
        return smallClaimsStrategy.showCarmMediationSection(caseData, carmEnabled);
    }

    public String getSmallClaimsMediationText(CaseData caseData) {
        return smallClaimsStrategy.getSmallClaimsMediationText(caseData);
    }

    public boolean showCarmMediationSectionDRH(CaseData caseData, boolean carmEnabled) {
        return smallClaimsStrategy.showCarmMediationSectionDrh(caseData, carmEnabled);
    }

    public String getSmallClaimsMediationTextDRH(CaseData caseData) {
        return smallClaimsStrategy.getSmallClaimsMediationTextDrh(caseData);
    }

    public boolean hasSmallClaimsVariable(CaseData caseData, String variableName) {
        return smallClaimsStrategy.hasSmallClaimsVariable(caseData, variableName);
    }

    public FastTrack getFastTrackAdditionalDirectionEnum(String additionalDirection) {
        return fastTrackStrategy.getFastTrackAdditionalDirectionEnum(additionalDirection);
    }

    public boolean hasFastAdditionalDirections(CaseData caseData, String additionalDirection) {
        return fastTrackStrategy.hasFastAdditionalDirections(caseData, additionalDirection);
    }

    public boolean hasFastTrackVariable(CaseData caseData, String variableName) {
        return fastTrackStrategy.hasFastTrackVariable(caseData, variableName);
    }

    public String getFastTrackMethodTelephoneHearingLabel(CaseData caseData) {
        return fastTrackStrategy.getFastTrackMethodTelephoneHearingLabel(caseData);
    }

    public String getFastTrackMethodVideoConferenceHearingLabel(CaseData caseData) {
        return fastTrackStrategy.getFastTrackMethodVideoConferenceHearingLabel(caseData);
    }

    public String getFastTrackTrialBundleTypeText(CaseData caseData) {
        return fastTrackStrategy.getFastTrackTrialBundleTypeText(caseData);
    }

    public String getFastTrackAllocation(CaseData caseData) {
        return fastTrackStrategy.getFastTrackAllocation(caseData);
    }

    public String getDisposalHearingFinalDisposalHearingTimeLabel(CaseData caseData) {
        return disposalStrategy.getDisposalHearingFinalDisposalHearingTimeLabel(caseData);
    }

    public String getDisposalHearingMethodTelephoneHearingLabel(CaseData caseData) {
        return disposalStrategy.getDisposalHearingMethodTelephoneHearingLabel(caseData);
    }

    public String getDisposalHearingMethodVideoConferenceHearingLabel(CaseData caseData) {
        return disposalStrategy.getDisposalHearingMethodVideoConferenceHearingLabel(caseData);
    }

    public String getDisposalHearingBundleTypeText(CaseData caseData) {
        return disposalStrategy.getDisposalHearingBundleTypeText(caseData);
    }

    public boolean hasDisposalVariable(CaseData caseData, String variableName) {
        return disposalStrategy.hasDisposalVariable(caseData, variableName);
    }

    public String getSdoTrialHearingTimeAllocated(CaseData caseData) {
        return trialStrategy.getSdoTrialHearingTimeAllocated(caseData);
    }

    public String getSdoTrialMethodOfHearing(CaseData caseData) {
        return trialStrategy.getSdoTrialMethodOfHearing(caseData);
    }
}
