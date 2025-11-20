package uk.gov.hmcts.reform.civil.service.docmosis.dj;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.docmosis.dj.DefaultJudgmentSDOOrderForm;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentHearingLocationHelper;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.utils.DocumentUtils.getDisposalHearingTimeEstimateDJ;

@Service
@RequiredArgsConstructor
public class DjDisposalTemplateService {

    private final UserService userService;
    private final DocumentHearingLocationHelper locationHelper;
    private final DjAuthorisationFieldService authorisationFieldService;
    private final DjBundleFieldService bundleFieldService;
    private final DjDirectionsToggleService directionsToggleService;
    private final DjPartyFieldService partyFieldService;
    private final DjHearingMethodFieldService hearingMethodFieldService;
    private final DjDisposalTemplateFieldService disposalTemplateFieldService;

    public DefaultJudgmentSDOOrderForm buildTemplate(CaseData caseData, String authorisation) {
        UserDetails userDetails = userService.getUserDetails(authorisation);
        boolean writtenByJudge = authorisationFieldService.isJudge(userDetails);
        String courtLocation = disposalTemplateFieldService.getCourtLocation(caseData);

        DefaultJudgmentSDOOrderForm.DefaultJudgmentSDOOrderFormBuilder builder = DefaultJudgmentSDOOrderForm.builder()
            .writtenByJudge(writtenByJudge)
            .judgeNameTitle(caseData.getDisposalHearingJudgesRecitalDJ().getJudgeNameTitle())
            .caseNumber(caseData.getLegacyCaseReference())
            .disposalHearingBundleDJ(caseData.getDisposalHearingBundleDJ())
            .disposalHearingBundleDJAddSection(nonNull(caseData.getDisposalHearingBundleDJ()))
            .typeBundleInfo(bundleFieldService.buildBundleInfo(caseData))
            .disposalHearingDisclosureOfDocumentsDJ(caseData.getDisposalHearingDisclosureOfDocumentsDJ())
            .disposalHearingDisclosureOfDocumentsDJAddSection(
                nonNull(caseData.getDisposalHearingDisclosureOfDocumentsDJ()))
            .disposalHearingWitnessOfFactDJ(caseData.getDisposalHearingWitnessOfFactDJ())
            .disposalHearingWitnessOfFactDJAddSection(nonNull(caseData.getDisposalHearingWitnessOfFactDJ()))
            .disposalHearingFinalDisposalHearingDJ(caseData.getDisposalHearingFinalDisposalHearingDJ())
            .disposalHearingMethodDJ(caseData.getDisposalHearingMethodDJ())
            .disposalHearingAttendance(disposalTemplateFieldService.getAttendanceLabel(caseData.getDisposalHearingMethodDJ()))
            .disposalHearingFinalDisposalHearingDJAddSection(nonNull(caseData.getDisposalHearingFinalDisposalHearingDJ()))
            .disposalHearingFinalDisposalHearingDJAddSection(nonNull(caseData.getDisposalHearingMethodDJ()))
            .disposalHearingFinalDisposalHearingDJAddSection(
                nonNull(disposalTemplateFieldService.getAttendanceLabel(caseData.getDisposalHearingMethodDJ())))
            .courtLocation(courtLocation)
            .telephoneOrganisedBy(hearingMethodFieldService.resolveTelephoneOrganisedBy(caseData))
            .videoConferenceOrganisedBy(hearingMethodFieldService.resolveVideoOrganisedBy(caseData))
            .disposalHearingTime(
                disposalTemplateFieldService.getHearingDuration(caseData)
            )
            .disposalHearingJudgesRecitalDJ(caseData.getDisposalHearingJudgesRecitalDJ())
            .disposalHearingMedicalEvidenceDJ(caseData.getDisposalHearingMedicalEvidenceDJ())
            .disposalHearingMedicalEvidenceDJAddSection(nonNull(caseData.getDisposalHearingMedicalEvidenceDJ()))
            .disposalHearingNotesDJ(caseData.getDisposalHearingNotesDJ())
            .hasNewDirections(directionsToggleService.hasAdditionalDirections(caseData))
            .disposalHearingAddNewDirectionsDJ(caseData.getDisposalHearingAddNewDirectionsDJ())
            .disposalHearingQuestionsToExpertsDJ(caseData.getDisposalHearingQuestionsToExpertsDJ())
            .disposalHearingQuestionsToExpertsDJAddSection(nonNull(caseData.getDisposalHearingQuestionsToExpertsDJ()))
            .disposalHearingSchedulesOfLossDJ(caseData.getDisposalHearingSchedulesOfLossDJ())
            .disposalHearingSchedulesOfLossDJAddSection(nonNull(caseData.getDisposalHearingSchedulesOfLossDJ()))
            .disposalHearingClaimSettlingAddSection(
                directionsToggleService.isToggleEnabled(caseData.getDisposalHearingClaimSettlingDJToggle()))
            .disposalHearingCostsAddSection(
                directionsToggleService.isToggleEnabled(caseData.getDisposalHearingCostsDJToggle()))
            .applicant(partyFieldService.hasApplicantPartyName(caseData)
                           ? caseData.getApplicant1().getPartyName().toUpperCase() : null)
            .respondent(partyFieldService.resolveRespondent(caseData).toUpperCase())
            .caseManagementLocation(locationHelper.getHearingLocation(null, caseData, authorisation));

        builder.disposalHearingOrderMadeWithoutHearingDJ(caseData.getDisposalHearingOrderMadeWithoutHearingDJ())
            .disposalHearingFinalDisposalHearingTimeDJ(caseData.getDisposalHearingFinalDisposalHearingTimeDJ())
            .disposalHearingTimeEstimateDJ(getDisposalHearingTimeEstimateDJ(caseData.getDisposalHearingFinalDisposalHearingTimeDJ()));

        builder.hearingLocation(locationHelper.getHearingLocation(courtLocation, caseData, authorisation));
        builder.hasDisposalHearingWelshSectionDJ(
            directionsToggleService.isToggleEnabled(caseData.getSdoR2DisposalHearingUseOfWelshLangToggleDJ())
        );
        builder.welshLanguageDescriptionDJ(
            caseData.getSdoR2DisposalHearingWelshLanguageDJ() != null
                ? caseData.getSdoR2DisposalHearingWelshLanguageDJ().getDescription()
                : null
        );

        return builder.build();
    }

}
