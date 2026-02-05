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

        DefaultJudgmentSDOOrderForm template = new DefaultJudgmentSDOOrderForm()
            .setWrittenByJudge(writtenByJudge)
            .setJudgeNameTitle(caseData.getTrialHearingJudgesRecitalDJ().getJudgeNameTitle())
            .setCaseNumber(caseData.getLegacyCaseReference())
            .setTrialBuildingDispute(caseData.getTrialBuildingDispute())
            .setTrialBuildingDisputeAddSection(nonNull(caseData.getTrialBuildingDispute()))
            .setTrialClinicalNegligence(caseData.getTrialClinicalNegligence())
            .setTrialClinicalNegligenceAddSection(nonNull(caseData.getTrialClinicalNegligence()))
            .setTrialCreditHire(caseData.getTrialCreditHire())
            .setTrialCreditHireAddSection(nonNull(caseData.getTrialCreditHire()))
            .setTrialHearingJudgesRecitalDJ(caseData.getTrialHearingJudgesRecitalDJ())
            .setSdoDJR2TrialCreditHireAddSection(nonNull(caseData.getSdoDJR2TrialCreditHire()))
            .setSdoDJR2TrialCreditHireDetailsAddSection(trialTemplateFieldService.showCreditHireDetails(caseData))
            .setTrialHearingTrialDJ(caseData.getTrialHearingTrialDJ())
            .setTypeBundleInfo(bundleFieldService.buildBundleInfo(caseData))
            .setTrialHearingTrialDJAddSection(directionsToggleService.isToggleEnabled(caseData.getTrialHearingTrialDJToggle()))
            .setTrialHearingNotesDJ(caseData.getTrialHearingNotesDJ())
            .setHasNewDirections(directionsToggleService.hasAdditionalDirections(caseData))
            .setTrialHearingAddNewDirectionsDJ(caseData.getTrialHearingAddNewDirectionsDJ())
            .setTrialHearingDisclosureOfDocumentsDJ(caseData.getTrialHearingDisclosureOfDocumentsDJ())
            .setTrialHearingDisclosureOfDocumentsDJAddSection(
                directionsToggleService.isToggleEnabled(caseData.getTrialHearingDisclosureOfDocumentsDJToggle()))
            .setTrialPersonalInjury(caseData.getTrialPersonalInjury())
            .setTrialPersonalInjuryAddSection(nonNull(caseData.getTrialPersonalInjury()))
            .setTrialHearingSchedulesOfLossDJ(caseData.getTrialHearingSchedulesOfLossDJ())
            .setTrialHearingSchedulesOfLossDJAddSection(
                directionsToggleService.isToggleEnabled(caseData.getTrialHearingSchedulesOfLossDJToggle()))
            .setTrialRoadTrafficAccident(caseData.getTrialRoadTrafficAccident())
            .setTrialRoadTrafficAccidentAddSection(nonNull(caseData.getTrialRoadTrafficAccident()))
            .setTrialHearingWitnessOfFactDJ(caseData.getTrialHearingWitnessOfFactDJ())
            .setTrialHearingWitnessOfFactDJAddSection(
                directionsToggleService.isToggleEnabled(caseData.getTrialHearingWitnessOfFactDJToggle()))
            .setTrialHearingDisputeAddSection(
                directionsToggleService.isToggleEnabled(caseData.getTrialHearingAlternativeDisputeDJToggle()))
            .setTrialHearingVariationsAddSection(
                directionsToggleService.isToggleEnabled(caseData.getTrialHearingVariationsDirectionsDJToggle()))
            .setTrialHearingSettlementAddSection(
                directionsToggleService.isToggleEnabled(caseData.getTrialHearingSettlementDJToggle()))
            .setTrialHearingCostsAddSection(
                directionsToggleService.isToggleEnabled(caseData.getTrialHearingCostsToggle()))
            .setTrialEmployerLiabilityAddSection(
                directionsToggleService.hasEmployerLiability(caseData.getCaseManagementOrderAdditional()))
            .setTrialHearingMethodDJ(caseData.getTrialHearingMethodDJ())
            .setTelephoneOrganisedBy(hearingMethodFieldService.resolveTelephoneOrganisedBy(caseData))
            .setVideoConferenceOrganisedBy(hearingMethodFieldService.resolveVideoOrganisedBy(caseData))
            .setTrialHousingDisrepair(caseData.getTrialHousingDisrepair())
            .setTrialHousingDisrepairAddSection(nonNull(caseData.getTrialHousingDisrepair()))
            .setTrialHearingMethodInPersonAddSection(
                hearingMethodFieldService.isInPerson(caseData.getTrialHearingMethodDJ()))
            .setTrialHearingLocation(trialHearingLocation)
            .setApplicant(partyFieldService.hasApplicantPartyName(caseData)
                           ? caseData.getApplicant1().getPartyName().toUpperCase() : null)
            .setRespondent(partyFieldService.resolveRespondent(caseData).toUpperCase())
            .setTrialHearingTimeDJ(caseData.getTrialHearingTimeDJ())
            .setDisposalHearingDateToToggle(trialTemplateFieldService.hasDateToToggle(caseData))
            .setTrialOrderMadeWithoutHearingDJ(caseData.getTrialOrderMadeWithoutHearingDJ())
            .setTrialHearingTimeEstimateDJ(getHearingTimeEstimateLabel(caseData.getTrialHearingTimeDJ()))
            .setCaseManagementLocation(locationHelper.getHearingLocation(null, caseData, authorisation))
            .setHearingLocation(locationHelper.getHearingLocation(trialHearingLocation, caseData, authorisation));

        template
            .setSdoDJR2TrialCreditHire(caseData.getSdoDJR2TrialCreditHire())
            .setHasTrialHearingWelshSectionDJ(
                directionsToggleService.isToggleEnabled(caseData.getSdoR2TrialUseOfWelshLangToggleDJ())
            )
            .setWelshLanguageDescriptionDJ(
                caseData.getSdoR2TrialWelshLanguageDJ() != null
                    ? caseData.getSdoR2TrialWelshLanguageDJ().getDescription()
                    : null
            );

        return template;
    }
}
