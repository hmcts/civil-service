package uk.gov.hmcts.reform.civil.service.sdo;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.sdo.ClaimsTrack;
import uk.gov.hmcts.reform.civil.enums.sdo.FastTrack;
import uk.gov.hmcts.reform.civil.enums.sdo.OrderType;
import uk.gov.hmcts.reform.civil.enums.sdo.SmallTrack;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.List;

import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@Service
public class SdoCaseClassificationService {

    public boolean isSmallClaimsTrack(CaseData caseData) {
        YesOrNo drawDirectionsOrderRequired = caseData.getDrawDirectionsOrderRequired();
        ClaimsTrack claimsTrack = caseData.getClaimsTrack();
        YesOrNo drawDirectionsOrderSmallClaims = caseData.getDrawDirectionsOrderSmallClaims();

        boolean smallClaimsViaTrack = drawDirectionsOrderRequired == NO
            && claimsTrack == ClaimsTrack.smallClaimsTrack;
        boolean smallClaimsViaOrder = drawDirectionsOrderRequired == YES
            && drawDirectionsOrderSmallClaims == YES;

        return smallClaimsViaTrack || smallClaimsViaOrder;
    }

    public boolean isFastTrack(CaseData caseData) {
        YesOrNo drawDirectionsOrderRequired = caseData.getDrawDirectionsOrderRequired();
        YesOrNo drawDirectionsOrderSmallClaims = caseData.getDrawDirectionsOrderSmallClaims();
        ClaimsTrack claimsTrack = caseData.getClaimsTrack();
        OrderType orderType = caseData.getOrderType();

        boolean fastTrackViaTrack = drawDirectionsOrderRequired == NO
            && claimsTrack == ClaimsTrack.fastTrack;
        boolean fastTrackViaOrder = drawDirectionsOrderRequired == YES
            && drawDirectionsOrderSmallClaims == NO
            && orderType == OrderType.DECIDE_DAMAGES;

        return fastTrackViaTrack || fastTrackViaOrder;
    }

    public boolean isNihlFastTrack(CaseData caseData) {
        List<FastTrack> fastClaims = caseData.getFastClaims();
        List<FastTrack> trialDirections = caseData.getTrialAdditionalDirectionsForFastTrack();

        boolean selectedDuringDrawOrder = caseData.getDrawDirectionsOrderRequired() == YES
            && trialDirections != null
            && trialDirections.contains(FastTrack.fastClaimNoiseInducedHearingLoss);
        boolean selectedDuringCaseSetup = caseData.getDrawDirectionsOrderRequired() == NO
            && fastClaims != null
            && fastClaims.contains(FastTrack.fastClaimNoiseInducedHearingLoss);

        return selectedDuringDrawOrder || selectedDuringCaseSetup;
    }

    public boolean isDrhSmallClaim(CaseData caseData) {
        List<SmallTrack> smallClaimsSelections = caseData.getSmallClaims();
        List<SmallTrack> additionalDirections = caseData.getDrawDirectionsOrderSmallClaimsAdditionalDirections();

        boolean selectedDuringCaseSetup = caseData.getDrawDirectionsOrderRequired() == NO
            && smallClaimsSelections != null
            && smallClaimsSelections.contains(SmallTrack.smallClaimDisputeResolutionHearing);
        boolean selectedDuringDrawOrder = caseData.getDrawDirectionsOrderRequired() == YES
            && additionalDirections != null
            && additionalDirections.contains(SmallTrack.smallClaimDisputeResolutionHearing);

        return selectedDuringCaseSetup || selectedDuringDrawOrder;
    }

    public boolean hasApplicant2(CaseData caseData) {
        return caseData.getApplicant2() != null;
    }

    public boolean hasRespondent2(CaseData caseData) {
        return caseData.getRespondent2() != null;
    }
}
