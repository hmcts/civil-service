package uk.gov.hmcts.reform.civil.service.sdo;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.DecisionOnRequestReconsiderationOptions;
import uk.gov.hmcts.reform.civil.enums.sdo.OrderDetailsPagesSectionsToggle;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackAllocation;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SdoSmallClaimsOrderDefaultsService {

    private final SdoSmallClaimsNarrativeService smallClaimsNarrativeService;
    private final SdoJourneyToggleService sdoJourneyToggleService;

    public void populateSmallClaimsOrderDetails(CaseData caseData,
                                                List<OrderDetailsPagesSectionsToggle> checkList) {
        smallClaimsNarrativeService.applyJudgesRecital(caseData);
        smallClaimsNarrativeService.applyDocumentDirections(caseData);
        smallClaimsNarrativeService.applyWitnessStatements(caseData);
        sdoJourneyToggleService.applySmallClaimsMediationStatement(caseData);
        smallClaimsNarrativeService.applyFlightDelaySection(caseData, checkList);
        smallClaimsNarrativeService.applyHearingSection(caseData);
        smallClaimsNarrativeService.applyNotesSection(caseData);
        smallClaimsNarrativeService.applyCreditHire(caseData);
        smallClaimsNarrativeService.applyRoadTrafficAccident(caseData);

        if (CaseState.CASE_PROGRESSION.equals(caseData.getCcdState())
            && DecisionOnRequestReconsiderationOptions.CREATE_SDO.equals(caseData.getDecisionOnRequestReconsiderationOptions())) {
            caseData.setDrawDirectionsOrderRequired(null);
            caseData.setDrawDirectionsOrderSmallClaims(null);
            caseData.setFastClaims(null);
            caseData.setSmallClaims(null);
            caseData.setClaimsTrack(null);
            caseData.setOrderType(null);
            caseData.setTrialAdditionalDirectionsForFastTrack(null);
            caseData.setDrawDirectionsOrderSmallClaimsAdditionalDirections(null);
            FastTrackAllocation allocation = new FastTrackAllocation();
            allocation.setAssignComplexityBand(null);
            caseData.setFastTrackAllocation(allocation);
            caseData.setDisposalHearingAddNewDirections(null);
            caseData.setSmallClaimsAddNewDirections(null);
            caseData.setFastTrackAddNewDirections(null);
            caseData.setSdoHearingNotes(null);
            caseData.setFastTrackHearingNotes(null);
            caseData.setDisposalHearingHearingNotes(null);
            caseData.setSdoR2SmallClaimsHearing(null);
            caseData.setSdoR2SmallClaimsUploadDoc(null);
            caseData.setSdoR2SmallClaimsPPI(null);
            caseData.setSdoR2SmallClaimsImpNotes(null);
            caseData.setSdoR2SmallClaimsWitnessStatements(null);
            caseData.setSdoR2SmallClaimsHearingToggle(null);
            caseData.setSdoR2SmallClaimsJudgesRecital(null);
            caseData.setSdoR2SmallClaimsWitnessStatementsToggle(null);
            caseData.setSdoR2SmallClaimsPPIToggle(null);
            caseData.setSdoR2SmallClaimsUploadDocToggle(null);
        }
    }
}
