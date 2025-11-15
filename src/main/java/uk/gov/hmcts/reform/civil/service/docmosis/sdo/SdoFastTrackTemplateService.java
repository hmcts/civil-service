package uk.gov.hmcts.reform.civil.service.docmosis.sdo;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.sdo.FastTrack;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.docmosis.sdo.SdoDocumentFormFast;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentHearingLocationHelper;
import uk.gov.hmcts.reform.civil.service.sdo.FastTrackVariable;
import uk.gov.hmcts.reform.civil.service.sdo.SdoCaseClassificationService;
import uk.gov.hmcts.reform.civil.service.sdo.SdoFastTrackDirectionsService;
import uk.gov.hmcts.reform.civil.service.sdo.SdoFastTrackTemplateFieldService;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.enums.sdo.AddOrRemoveToggle.ADD;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.FAST_TRACK_DIGITAL_PORTAL_BUNDLE_WARNING;

@Service
@RequiredArgsConstructor
public class SdoFastTrackTemplateService {

    private final DocumentHearingLocationHelper locationHelper;
    private final SdoCaseClassificationService caseClassificationService;
    private final SdoFastTrackDirectionsService fastTrackDirectionsService;
    private final SdoFastTrackTemplateFieldService fastTrackTemplateFieldService;

    public SdoDocumentFormFast buildTemplate(CaseData caseData, String judgeName, boolean isJudge, String authorisation) {
        boolean showBundleInfo = hasVariable(caseData, FastTrackVariable.TRIAL_BUNDLE_TOGGLE);

        SdoDocumentFormFast.SdoDocumentFormFastBuilder builder = SdoDocumentFormFast.builder()
            .writtenByJudge(isJudge)
            .currentDate(LocalDate.now())
            .judgeName(judgeName)
            .caseNumber(caseData.getLegacyCaseReference())
            .applicant1(caseData.getApplicant1())
            .hasApplicant2(caseClassificationService.hasApplicant2(caseData))
            .applicant2(caseData.getApplicant2())
            .respondent1(caseData.getRespondent1())
            .hasRespondent2(caseClassificationService.hasRespondent2(caseData))
            .respondent2(caseData.getRespondent2())
            .drawDirectionsOrderRequired(caseData.getDrawDirectionsOrderRequired())
            .drawDirectionsOrder(caseData.getDrawDirectionsOrder())
            .claimsTrack(caseData.getClaimsTrack())
            .fastClaims(caseData.getFastClaims())
            .hasBuildingDispute(hasDirection(caseData, FastTrack.fastClaimBuildingDispute))
            .hasClinicalNegligence(hasDirection(caseData, FastTrack.fastClaimClinicalNegligence))
            .hasSdoR2CreditHire(hasDirection(caseData, FastTrack.fastClaimCreditHire))
            .hasSdoR2CreditHireDetails(hasCreditHireDetails(caseData))
            .hasEmployersLiability(hasDirection(caseData, FastTrack.fastClaimEmployersLiability))
            .hasHousingDisrepair(hasDirection(caseData, FastTrack.fastClaimHousingDisrepair))
            .hasPersonalInjury(hasDirection(caseData, FastTrack.fastClaimPersonalInjury))
            .hasRoadTrafficAccident(hasDirection(caseData, FastTrack.fastClaimRoadTrafficAccident))
            .fastTrackJudgesRecital(caseData.getFastTrackJudgesRecital())
            .fastTrackDisclosureOfDocuments(caseData.getFastTrackDisclosureOfDocuments())
            .fastTrackSchedulesOfLoss(caseData.getFastTrackSchedulesOfLoss())
            .fastTrackTrial(caseData.getFastTrackTrial())
            .fastTrackTrialBundleTypeText(fastTrackTemplateFieldService.getTrialBundleTypeText(caseData))
            .fastTrackDigitalPortalBundleWarning(
                showBundleInfo ? FAST_TRACK_DIGITAL_PORTAL_BUNDLE_WARNING : null
            )
            .fastTrackMethod(caseData.getFastTrackMethod())
            .fastTrackMethodInPerson(caseData.getFastTrackMethodInPerson())
            .fastTrackMethodTelephoneHearing(fastTrackTemplateFieldService.getMethodTelephoneHearingLabel(caseData))
            .fastTrackMethodVideoConferenceHearing(
                fastTrackTemplateFieldService.getMethodVideoConferenceHearingLabel(caseData)
            )
            .fastTrackBuildingDispute(caseData.getFastTrackBuildingDispute())
            .fastTrackClinicalNegligence(caseData.getFastTrackClinicalNegligence())
            .fastTrackHousingDisrepair(caseData.getFastTrackHousingDisrepair())
            .fastTrackPersonalInjury(caseData.getFastTrackPersonalInjury())
            .fastTrackRoadTrafficAccident(caseData.getFastTrackRoadTrafficAccident())
            .hasNewDirections(hasVariable(caseData, FastTrackVariable.ADD_NEW_DIRECTIONS))
            .fastTrackAddNewDirections(caseData.getFastTrackAddNewDirections())
            .fastTrackNotes(caseData.getFastTrackNotes())
            .fastTrackTrialDateToToggle(hasVariable(caseData, FastTrackVariable.TRIAL_DATE_TO_TOGGLE))
            .fastTrackAltDisputeResolutionToggle(hasVariable(caseData, FastTrackVariable.ALT_DISPUTE_RESOLUTION))
            .fastTrackVariationOfDirectionsToggle(hasVariable(caseData, FastTrackVariable.VARIATION_OF_DIRECTIONS))
            .fastTrackSettlementToggle(hasVariable(caseData, FastTrackVariable.SETTLEMENT))
            .fastTrackDisclosureOfDocumentsToggle(hasVariable(caseData, FastTrackVariable.DISCLOSURE_OF_DOCUMENTS))
            .fastTrackWitnessOfFactToggle(hasVariable(caseData, FastTrackVariable.WITNESS_OF_FACT))
            .fastTrackSchedulesOfLossToggle(hasVariable(caseData, FastTrackVariable.SCHEDULES_OF_LOSS))
            .fastTrackCostsToggle(hasVariable(caseData, FastTrackVariable.COSTS))
            .fastTrackTrialToggle(hasVariable(caseData, FastTrackVariable.TRIAL))
            .fastTrackMethodToggle(true) // legacy toggle always true per CIV-5142
            .fastTrackAllocation(fastTrackTemplateFieldService.getAllocationSummary(caseData))
            .showBundleInfo(showBundleInfo)
            .fastTrackOrderWithoutJudgement(caseData.getFastTrackOrderWithoutJudgement())
            .fastTrackHearingTime(caseData.getFastTrackHearingTime())
            .fastTrackHearingTimeEstimate(fastTrackTemplateFieldService.getHearingTimeLabel(caseData))
            .fastTrackWelshLanguageToggle(hasVariable(caseData, FastTrackVariable.WELSH_TOGGLE))
            .welshLanguageDescription(Optional.ofNullable(caseData.getSdoR2FastTrackUseOfWelshLanguage())
                                          .map(value -> value.getDescription()).orElse(null))
            .sdoR2WitnessesOfFact(caseData.getSdoR2FastTrackWitnessOfFact())
            .sdoR2FastTrackCreditHire(caseData.getSdoR2FastTrackCreditHire());

        builder.hearingLocation(locationHelper.getHearingLocation(
                Optional.ofNullable(caseData.getFastTrackMethodInPerson())
                    .map(DynamicList::getValue)
                    .map(DynamicListElement::getLabel)
                    .orElse(null),
                caseData,
                authorisation
            ))
            .caseManagementLocation(locationHelper.getHearingLocation(null, caseData, authorisation));

        return builder.build();
    }

    private boolean hasDirection(CaseData caseData, FastTrack direction) {
        return fastTrackDirectionsService.hasFastAdditionalDirections(caseData, direction);
    }

    private boolean hasVariable(CaseData caseData, FastTrackVariable variable) {
        return fastTrackDirectionsService.hasFastTrackVariable(caseData, variable);
    }

    private boolean hasCreditHireDetails(CaseData caseData) {
        return nonNull(caseData.getSdoR2FastTrackCreditHire())
            && caseData.getSdoR2FastTrackCreditHire().getDetailsShowToggle() != null
            && caseData.getSdoR2FastTrackCreditHire().getDetailsShowToggle().equals(List.of(ADD));
    }
}
