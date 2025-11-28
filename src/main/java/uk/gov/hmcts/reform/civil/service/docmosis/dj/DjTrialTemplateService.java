package uk.gov.hmcts.reform.civil.service.docmosis.dj;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.docmosis.dj.DefaultJudgmentSDOOrderForm;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentHearingLocationHelper;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.utils.DocumentUtils.getDynamicListValueLabel;
import static uk.gov.hmcts.reform.civil.utils.DocumentUtils.getHearingTimeEstimateLabel;

@Service
@RequiredArgsConstructor
public class DjTrialTemplateService {

    private final UserService userService;
    private final DocumentHearingLocationHelper locationHelper;
    private final DjAuthorisationFieldService authorisationFieldService;
    private final DjBundleFieldService bundleFieldService;
    private final DjDirectionsToggleService directionsToggleService;
    private final DjPartyFieldService partyFieldService;
    private final DjHearingMethodFieldService hearingMethodFieldService;
    private final DjTrialTemplateFieldService trialTemplateFieldService;

    public DefaultJudgmentSDOOrderForm buildTemplate(CaseData caseData, String authorisation) {
        UserDetails userDetails = userService.getUserDetails(authorisation);
        boolean writtenByJudge = authorisationFieldService.isJudge(userDetails);
        String trialHearingLocation = getDynamicListValueLabel(caseData.getTrialHearingMethodInPersonDJ());

        DefaultJudgmentSDOOrderForm.DefaultJudgmentSDOOrderFormBuilder builder = DefaultJudgmentSDOOrderForm.builder()
            .writtenByJudge(writtenByJudge)
            .judgeNameTitle(caseData.getTrialHearingJudgesRecitalDJ().getJudgeNameTitle())
            .caseNumber(caseData.getLegacyCaseReference())
            .trialBuildingDispute(caseData.getTrialBuildingDispute())
            .trialBuildingDisputeAddSection(nonNull(caseData.getTrialBuildingDispute()))
            .trialClinicalNegligence(caseData.getTrialClinicalNegligence())
            .trialClinicalNegligenceAddSection(nonNull(caseData.getTrialClinicalNegligence()))
            .trialCreditHire(caseData.getTrialCreditHire())
            .trialCreditHireAddSection(nonNull(caseData.getTrialCreditHire()))
            .trialHearingJudgesRecitalDJ(caseData.getTrialHearingJudgesRecitalDJ())
            .sdoDJR2TrialCreditHireAddSection(nonNull(caseData.getSdoDJR2TrialCreditHire()))
            .sdoDJR2TrialCreditHireDetailsAddSection(trialTemplateFieldService.showCreditHireDetails(caseData))
            .trialHearingTrialDJ(caseData.getTrialHearingTrialDJ())
            .typeBundleInfo(bundleFieldService.buildBundleInfo(caseData))
            .trialHearingTrialDJAddSection(directionsToggleService.isToggleEnabled(caseData.getTrialHearingTrialDJToggle()))
            .trialHearingNotesDJ(caseData.getTrialHearingNotesDJ())
            .hasNewDirections(directionsToggleService.hasAdditionalDirections(caseData))
            .trialHearingAddNewDirectionsDJ(caseData.getTrialHearingAddNewDirectionsDJ())
            .trialHearingDisclosureOfDocumentsDJ(caseData.getTrialHearingDisclosureOfDocumentsDJ())
            .trialHearingDisclosureOfDocumentsDJAddSection(
                directionsToggleService.isToggleEnabled(caseData.getTrialHearingDisclosureOfDocumentsDJToggle()))
            .trialPersonalInjury(caseData.getTrialPersonalInjury())
            .trialPersonalInjuryAddSection(nonNull(caseData.getTrialPersonalInjury()))
            .trialHearingSchedulesOfLossDJ(caseData.getTrialHearingSchedulesOfLossDJ())
            .trialHearingSchedulesOfLossDJAddSection(
                directionsToggleService.isToggleEnabled(caseData.getTrialHearingSchedulesOfLossDJToggle()))
            .trialRoadTrafficAccident(caseData.getTrialRoadTrafficAccident())
            .trialRoadTrafficAccidentAddSection(nonNull(caseData.getTrialRoadTrafficAccident()))
            .trialHearingWitnessOfFactDJ(caseData.getTrialHearingWitnessOfFactDJ())
            .trialHearingWitnessOfFactDJAddSection(
                directionsToggleService.isToggleEnabled(caseData.getTrialHearingWitnessOfFactDJToggle()))
            .trialHearingDisputeAddSection(
                directionsToggleService.isToggleEnabled(caseData.getTrialHearingAlternativeDisputeDJToggle()))
            .trialHearingVariationsAddSection(
                directionsToggleService.isToggleEnabled(caseData.getTrialHearingVariationsDirectionsDJToggle()))
            .trialHearingSettlementAddSection(
                directionsToggleService.isToggleEnabled(caseData.getTrialHearingSettlementDJToggle()))
            .trialHearingCostsAddSection(
                directionsToggleService.isToggleEnabled(caseData.getTrialHearingCostsToggle()))
            .trialEmployerLiabilityAddSection(
                directionsToggleService.hasEmployerLiability(caseData.getCaseManagementOrderAdditional()))
            .trialHearingMethodDJ(caseData.getTrialHearingMethodDJ())
            .telephoneOrganisedBy(hearingMethodFieldService.resolveTelephoneOrganisedBy(caseData))
            .videoConferenceOrganisedBy(hearingMethodFieldService.resolveVideoOrganisedBy(caseData))
            .trialHousingDisrepair(caseData.getTrialHousingDisrepair())
            .trialHousingDisrepairAddSection(nonNull(caseData.getTrialHousingDisrepair()))
            .trialHearingMethodInPersonAddSection(
                hearingMethodFieldService.isInPerson(caseData.getTrialHearingMethodDJ()))
            .trialHearingLocation(trialHearingLocation)
            .applicant(partyFieldService.hasApplicantPartyName(caseData)
                           ? caseData.getApplicant1().getPartyName().toUpperCase() : null)
            .respondent(partyFieldService.resolveRespondent(caseData).toUpperCase())
            .trialHearingTimeDJ(caseData.getTrialHearingTimeDJ())
            .disposalHearingDateToToggle(trialTemplateFieldService.hasDateToToggle(caseData))
            .trialOrderMadeWithoutHearingDJ(caseData.getTrialOrderMadeWithoutHearingDJ())
            .trialHearingTimeEstimateDJ(getHearingTimeEstimateLabel(caseData.getTrialHearingTimeDJ()))
            .caseManagementLocation(locationHelper.getHearingLocation(null, caseData, authorisation))
            .hearingLocation(locationHelper.getHearingLocation(trialHearingLocation, caseData, authorisation));

        builder.sdoDJR2TrialCreditHire(caseData.getSdoDJR2TrialCreditHire());
        builder.hasTrialHearingWelshSectionDJ(
            directionsToggleService.isToggleEnabled(caseData.getSdoR2TrialUseOfWelshLangToggleDJ())
        );
        builder.welshLanguageDescriptionDJ(
            caseData.getSdoR2TrialWelshLanguageDJ() != null
                ? caseData.getSdoR2TrialWelshLanguageDJ().getDescription()
                : null
        );

        return builder.build();
    }
}
