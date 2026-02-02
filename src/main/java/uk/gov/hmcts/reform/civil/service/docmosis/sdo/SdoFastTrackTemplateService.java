package uk.gov.hmcts.reform.civil.service.docmosis.sdo;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.sdo.FastTrack;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.docmosis.sdo.SdoDocumentFormFast;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2WelshLanguageUsage;
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

@Service
@RequiredArgsConstructor
public class SdoFastTrackTemplateService {

    private final DocumentHearingLocationHelper locationHelper;
    private final SdoCaseClassificationService caseClassificationService;
    private final SdoFastTrackDirectionsService fastTrackDirectionsService;
    private final SdoFastTrackTemplateFieldService fastTrackTemplateFieldService;

    public SdoDocumentFormFast buildTemplate(CaseData caseData, String judgeName, boolean isJudge, String authorisation) {
        boolean showBundleInfo = hasVariable(caseData, FastTrackVariable.TRIAL_BUNDLE_TOGGLE);

        SdoDocumentFormFast template = new SdoDocumentFormFast()
            .setWrittenByJudge(isJudge)
            .setCurrentDate(LocalDate.now())
            .setJudgeName(judgeName)
            .setCaseNumber(caseData.getLegacyCaseReference())
            .setApplicant1(caseData.getApplicant1())
            .setHasApplicant2(caseClassificationService.hasApplicant2(caseData))
            .setApplicant2(caseData.getApplicant2())
            .setRespondent1(caseData.getRespondent1())
            .setHasRespondent2(caseClassificationService.hasRespondent2(caseData))
            .setRespondent2(caseData.getRespondent2())
            .setDrawDirectionsOrderRequired(caseData.getDrawDirectionsOrderRequired())
            .setDrawDirectionsOrder(caseData.getDrawDirectionsOrder())
            .setClaimsTrack(caseData.getClaimsTrack())
            .setFastClaims(caseData.getFastClaims())
            .setHasBuildingDispute(hasDirection(caseData, FastTrack.fastClaimBuildingDispute))
            .setHasClinicalNegligence(hasDirection(caseData, FastTrack.fastClaimClinicalNegligence))
            .setHasSdoR2CreditHire(hasDirection(caseData, FastTrack.fastClaimCreditHire))
            .setHasSdoR2CreditHireDetails(hasCreditHireDetails(caseData))
            .setHasEmployersLiability(hasDirection(caseData, FastTrack.fastClaimEmployersLiability))
            .setHasHousingDisrepair(hasDirection(caseData, FastTrack.fastClaimHousingDisrepair))
            .setHasPersonalInjury(hasDirection(caseData, FastTrack.fastClaimPersonalInjury))
            .setHasRoadTrafficAccident(hasDirection(caseData, FastTrack.fastClaimRoadTrafficAccident))
            .setFastTrackJudgesRecital(caseData.getFastTrackJudgesRecital())
            .setFastTrackDisclosureOfDocuments(caseData.getFastTrackDisclosureOfDocuments())
            .setFastTrackSchedulesOfLoss(caseData.getFastTrackSchedulesOfLoss())
            .setFastTrackTrial(caseData.getFastTrackTrial())
            .setFastTrackMethod(caseData.getFastTrackMethod())
            .setFastTrackMethodInPerson(caseData.getFastTrackMethodInPerson())
            .setFastTrackMethodTelephoneHearing(fastTrackTemplateFieldService.getMethodTelephoneHearingLabel(caseData))
            .setFastTrackMethodVideoConferenceHearing(
                fastTrackTemplateFieldService.getMethodVideoConferenceHearingLabel(caseData)
            )
            .setFastTrackBuildingDispute(caseData.getFastTrackBuildingDispute())
            .setFastTrackClinicalNegligence(caseData.getFastTrackClinicalNegligence())
            .setFastTrackHousingDisrepair(caseData.getFastTrackHousingDisrepair())
            .setFastTrackPersonalInjury(caseData.getFastTrackPersonalInjury())
            .setFastTrackRoadTrafficAccident(caseData.getFastTrackRoadTrafficAccident())
            .setHasNewDirections(hasVariable(caseData, FastTrackVariable.ADD_NEW_DIRECTIONS))
            .setFastTrackAddNewDirections(caseData.getFastTrackAddNewDirections())
            .setFastTrackNotes(caseData.getFastTrackNotes())
            .setFastTrackTrialDateToToggle(hasVariable(caseData, FastTrackVariable.TRIAL_DATE_TO_TOGGLE))
            .setFastTrackAltDisputeResolutionToggle(hasVariable(caseData, FastTrackVariable.ALT_DISPUTE_RESOLUTION))
            .setFastTrackVariationOfDirectionsToggle(hasVariable(caseData, FastTrackVariable.VARIATION_OF_DIRECTIONS))
            .setFastTrackSettlementToggle(hasVariable(caseData, FastTrackVariable.SETTLEMENT))
            .setFastTrackDisclosureOfDocumentsToggle(hasVariable(caseData, FastTrackVariable.DISCLOSURE_OF_DOCUMENTS))
            .setFastTrackWitnessOfFactToggle(hasVariable(caseData, FastTrackVariable.WITNESS_OF_FACT))
            .setFastTrackSchedulesOfLossToggle(hasVariable(caseData, FastTrackVariable.SCHEDULES_OF_LOSS))
            .setFastTrackCostsToggle(hasVariable(caseData, FastTrackVariable.COSTS))
            .setFastTrackTrialToggle(hasVariable(caseData, FastTrackVariable.TRIAL))
            .setFastTrackMethodToggle(true) // legacy toggle always true per CIV-5142
            .setFastTrackAllocation(fastTrackTemplateFieldService.getAllocationSummary(caseData))
            .setShowBundleInfo(showBundleInfo)
            .setFastTrackOrderWithoutJudgement(caseData.getFastTrackOrderWithoutJudgement())
            .setFastTrackHearingTime(caseData.getFastTrackHearingTime())
            .setFastTrackHearingTimeEstimate(fastTrackTemplateFieldService.getHearingTimeLabel(caseData))
            .setFastTrackWelshLanguageToggle(hasVariable(caseData, FastTrackVariable.WELSH_TOGGLE))
            .setWelshLanguageDescription(Optional.ofNullable(caseData.getSdoR2FastTrackUseOfWelshLanguage())
                                          .map(SdoR2WelshLanguageUsage::getDescription).orElse(null))
            .setSdoR2WitnessesOfFact(caseData.getSdoR2FastTrackWitnessOfFact())
            .setSdoR2FastTrackCreditHire(caseData.getSdoR2FastTrackCreditHire());

        template
            .setHearingLocation(locationHelper.getHearingLocation(
                Optional.ofNullable(caseData.getFastTrackMethodInPerson())
                    .map(DynamicList::getValue)
                    .map(DynamicListElement::getLabel)
                    .orElse(null),
                caseData,
                authorisation
            ))
            .setCaseManagementLocation(locationHelper.getHearingLocation(null, caseData, authorisation));

        return template;
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
