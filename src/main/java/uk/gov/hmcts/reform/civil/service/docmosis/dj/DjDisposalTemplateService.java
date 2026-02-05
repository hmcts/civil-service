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

        DefaultJudgmentSDOOrderForm template = new DefaultJudgmentSDOOrderForm()
            .setWrittenByJudge(writtenByJudge)
            .setJudgeNameTitle(caseData.getDisposalHearingJudgesRecitalDJ().getJudgeNameTitle())
            .setCaseNumber(caseData.getLegacyCaseReference())
            .setDisposalHearingBundleDJ(caseData.getDisposalHearingBundleDJ())
            .setDisposalHearingBundleDJAddSection(nonNull(caseData.getDisposalHearingBundleDJ()))
            .setTypeBundleInfo(bundleFieldService.buildBundleInfo(caseData))
            .setDisposalHearingDisclosureOfDocumentsDJ(caseData.getDisposalHearingDisclosureOfDocumentsDJ())
            .setDisposalHearingDisclosureOfDocumentsDJAddSection(
                nonNull(caseData.getDisposalHearingDisclosureOfDocumentsDJ()))
            .setDisposalHearingWitnessOfFactDJ(caseData.getDisposalHearingWitnessOfFactDJ())
            .setDisposalHearingWitnessOfFactDJAddSection(nonNull(caseData.getDisposalHearingWitnessOfFactDJ()))
            .setDisposalHearingFinalDisposalHearingDJ(caseData.getDisposalHearingFinalDisposalHearingDJ())
            .setDisposalHearingMethodDJ(caseData.getDisposalHearingMethodDJ())
            .setDisposalHearingAttendance(disposalTemplateFieldService.getAttendanceLabel(caseData.getDisposalHearingMethodDJ()))
            .setDisposalHearingFinalDisposalHearingDJAddSection(nonNull(caseData.getDisposalHearingFinalDisposalHearingDJ()))
            .setDisposalHearingFinalDisposalHearingDJAddSection(nonNull(caseData.getDisposalHearingMethodDJ()))
            .setDisposalHearingFinalDisposalHearingDJAddSection(
                nonNull(disposalTemplateFieldService.getAttendanceLabel(caseData.getDisposalHearingMethodDJ())))
            .setCourtLocation(courtLocation)
            .setTelephoneOrganisedBy(hearingMethodFieldService.resolveTelephoneOrganisedBy(caseData))
            .setVideoConferenceOrganisedBy(hearingMethodFieldService.resolveVideoOrganisedBy(caseData))
            .setDisposalHearingTime(
                disposalTemplateFieldService.getHearingDuration(caseData)
            )
            .setDisposalHearingJudgesRecitalDJ(caseData.getDisposalHearingJudgesRecitalDJ())
            .setDisposalHearingMedicalEvidenceDJ(caseData.getDisposalHearingMedicalEvidenceDJ())
            .setDisposalHearingMedicalEvidenceDJAddSection(nonNull(caseData.getDisposalHearingMedicalEvidenceDJ()))
            .setDisposalHearingNotesDJ(caseData.getDisposalHearingNotesDJ())
            .setHasNewDirections(directionsToggleService.hasAdditionalDirections(caseData))
            .setDisposalHearingAddNewDirectionsDJ(caseData.getDisposalHearingAddNewDirectionsDJ())
            .setDisposalHearingQuestionsToExpertsDJ(caseData.getDisposalHearingQuestionsToExpertsDJ())
            .setDisposalHearingQuestionsToExpertsDJAddSection(nonNull(caseData.getDisposalHearingQuestionsToExpertsDJ()))
            .setDisposalHearingSchedulesOfLossDJ(caseData.getDisposalHearingSchedulesOfLossDJ())
            .setDisposalHearingSchedulesOfLossDJAddSection(nonNull(caseData.getDisposalHearingSchedulesOfLossDJ()))
            .setDisposalHearingClaimSettlingAddSection(
                directionsToggleService.isToggleEnabled(caseData.getDisposalHearingClaimSettlingDJToggle()))
            .setDisposalHearingCostsAddSection(
                directionsToggleService.isToggleEnabled(caseData.getDisposalHearingCostsDJToggle()))
            .setApplicant(partyFieldService.hasApplicantPartyName(caseData)
                           ? caseData.getApplicant1().getPartyName().toUpperCase() : null)
            .setRespondent(partyFieldService.resolveRespondent(caseData).toUpperCase())
            .setCaseManagementLocation(locationHelper.getHearingLocation(null, caseData, authorisation));

        template
            .setDisposalHearingOrderMadeWithoutHearingDJ(caseData.getDisposalHearingOrderMadeWithoutHearingDJ())
            .setDisposalHearingFinalDisposalHearingTimeDJ(caseData.getDisposalHearingFinalDisposalHearingTimeDJ())
            .setDisposalHearingTimeEstimateDJ(getDisposalHearingTimeEstimateDJ(caseData.getDisposalHearingFinalDisposalHearingTimeDJ()));

        template
            .setHearingLocation(locationHelper.getHearingLocation(courtLocation, caseData, authorisation))
            .setHasDisposalHearingWelshSectionDJ(
                directionsToggleService.isToggleEnabled(caseData.getSdoR2DisposalHearingUseOfWelshLangToggleDJ())
            )
            .setWelshLanguageDescriptionDJ(
                caseData.getSdoR2DisposalHearingWelshLanguageDJ() != null
                    ? caseData.getSdoR2DisposalHearingWelshLanguageDJ().getDescription()
                    : null
            );

        return template;
    }

}
