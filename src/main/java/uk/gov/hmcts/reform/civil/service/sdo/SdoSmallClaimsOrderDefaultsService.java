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
                                                CaseData.CaseDataBuilder<?, ?> updatedData,
                                                List<OrderDetailsPagesSectionsToggle> checkList) {
        smallClaimsNarrativeService.applyJudgesRecital(updatedData);
        smallClaimsNarrativeService.applyDocumentDirections(updatedData);
        smallClaimsNarrativeService.applyWitnessStatements(updatedData);
        sdoJourneyToggleService.applySmallClaimsMediationStatement(caseData, updatedData);
        smallClaimsNarrativeService.applyFlightDelaySection(updatedData, checkList);
        smallClaimsNarrativeService.applyHearingSection(updatedData);
        smallClaimsNarrativeService.applyNotesSection(updatedData);
        smallClaimsNarrativeService.applyCreditHire(updatedData);
        smallClaimsNarrativeService.applyRoadTrafficAccident(updatedData);

        if (CaseState.CASE_PROGRESSION.equals(caseData.getCcdState())
            && DecisionOnRequestReconsiderationOptions.CREATE_SDO.equals(caseData.getDecisionOnRequestReconsiderationOptions())) {
            updatedData.drawDirectionsOrderRequired(null);
            updatedData.drawDirectionsOrderSmallClaims(null);
            updatedData.fastClaims(null);
            updatedData.smallClaims(null);
            updatedData.claimsTrack(null);
            updatedData.orderType(null);
            updatedData.trialAdditionalDirectionsForFastTrack(null);
            updatedData.drawDirectionsOrderSmallClaimsAdditionalDirections(null);
            updatedData.fastTrackAllocation(FastTrackAllocation.builder().assignComplexityBand(null).build());
            updatedData.disposalHearingAddNewDirections(null);
            updatedData.smallClaimsAddNewDirections(null);
            updatedData.fastTrackAddNewDirections(null);
            updatedData.sdoHearingNotes(null);
            updatedData.fastTrackHearingNotes(null);
            updatedData.disposalHearingHearingNotes(null);
            updatedData.sdoR2SmallClaimsHearing(null);
            updatedData.sdoR2SmallClaimsUploadDoc(null);
            updatedData.sdoR2SmallClaimsPPI(null);
            updatedData.sdoR2SmallClaimsImpNotes(null);
            updatedData.sdoR2SmallClaimsWitnessStatements(null);
            updatedData.sdoR2SmallClaimsHearingToggle(null);
            updatedData.sdoR2SmallClaimsJudgesRecital(null);
            updatedData.sdoR2SmallClaimsWitnessStatementsToggle(null);
            updatedData.sdoR2SmallClaimsPPIToggle(null);
            updatedData.sdoR2SmallClaimsUploadDocToggle(null);
        }
    }
}
