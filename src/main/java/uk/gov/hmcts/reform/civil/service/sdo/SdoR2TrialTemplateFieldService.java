package uk.gov.hmcts.reform.civil.service.sdo;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.sdo.FastTrackHearingTimeEstimate;
import uk.gov.hmcts.reform.civil.enums.sdo.HearingMethod;
import uk.gov.hmcts.reform.civil.enums.sdo.PhysicalTrialBundleOptions;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2ApplicationToRelyOnFurther;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2QuestionsClaimantExpert;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2Trial;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2WitnessOfFact;

import java.util.Optional;

@Service
public class SdoR2TrialTemplateFieldService {

    private static final String MINUTES = " minutes";

    public DynamicList getHearingLocation(CaseData caseData) {
        SdoR2Trial trial = caseData.getSdoR2Trial();
        if (trial == null) {
            return null;
        }

        if (trial.getHearingCourtLocationList() != null
            && trial.getHearingCourtLocationList().getValue() != null
            && !"OTHER_LOCATION".equals(trial.getHearingCourtLocationList().getValue().getCode())) {
            return trial.getHearingCourtLocationList();
        } else if (trial.getAltHearingCourtLocationList() != null
            && trial.getAltHearingCourtLocationList().getValue() != null) {
            return trial.getAltHearingCourtLocationList();
        }

        return null;
    }

    public String getPhysicalBundlePartyText(CaseData caseData) {
        SdoR2Trial trial = caseData.getSdoR2Trial();
        if (trial != null
            && PhysicalTrialBundleOptions.PARTY.equals(trial.getPhysicalBundleOptions())) {
            return trial.getPhysicalBundlePartyTxt();
        }
        return "";
    }

    public boolean hasRestrictWitness(CaseData caseData) {
        return Optional.ofNullable(caseData.getSdoR2WitnessesOfFact())
            .map(SdoR2WitnessOfFact::getSdoR2RestrictWitness)
            .map(restrict -> restrict.getIsRestrictWitness())
            .map(YesOrNo.YES::equals)
            .orElse(false);
    }

    public boolean hasRestrictPages(CaseData caseData) {
        return Optional.ofNullable(caseData.getSdoR2WitnessesOfFact())
            .map(SdoR2WitnessOfFact::getSdoRestrictPages)
            .map(restrict -> restrict.getIsRestrictPages())
            .map(YesOrNo.YES::equals)
            .orElse(false);
    }

    public boolean hasApplicationToRelyOnFurther(CaseData caseData) {
        return Optional.ofNullable(caseData.getSdoR2QuestionsClaimantExpert())
            .map(SdoR2QuestionsClaimantExpert::getSdoApplicationToRelyOnFurther)
            .map(SdoR2ApplicationToRelyOnFurther::getDoRequireApplicationToRely)
            .map(YesOrNo.YES::equals)
            .orElse(false);
    }

    public boolean hasClaimForPecuniaryLoss(CaseData caseData) {
        return Optional.ofNullable(caseData.getSdoR2ScheduleOfLoss())
            .map(loss -> loss.getIsClaimForPecuniaryLoss())
            .map(YesOrNo.YES::equals)
            .orElse(false);
    }

    public String getTrialHearingTimeAllocated(CaseData caseData) {
        SdoR2Trial trial = caseData.getSdoR2Trial();
        if (trial == null || trial.getLengthList() == null) {
            return "";
        }

        if (FastTrackHearingTimeEstimate.OTHER.equals(trial.getLengthList())) {
            return trial.getLengthListOther().getTrialLengthDays() + " days, "
                + trial.getLengthListOther().getTrialLengthHours() + " hours and "
                + trial.getLengthListOther().getTrialLengthMinutes() + MINUTES;
        }

        return trial.getLengthList().getLabel();
    }

    public String getTrialMethodOfHearing(CaseData caseData) {
        SdoR2Trial trial = caseData.getSdoR2Trial();
        if (trial == null || trial.getMethodOfHearing() == null
            || trial.getMethodOfHearing().getValue() == null) {
            return "";
        }

        String label = trial.getMethodOfHearing().getValue().getLabel();
        if (HearingMethod.TELEPHONE.getLabel().equals(label)) {
            return "by telephone";
        } else if (HearingMethod.VIDEO.getLabel().equals(label)) {
            return "by video conference";
        } else if (HearingMethod.IN_PERSON.getLabel().equals(label)) {
            return "in person";
        }

        return "";
    }
}
