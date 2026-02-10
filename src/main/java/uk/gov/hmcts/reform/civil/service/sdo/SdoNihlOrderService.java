package uk.gov.hmcts.reform.civil.service.sdo;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.sdo.FastTrackHearingTimeEstimate;
import uk.gov.hmcts.reform.civil.enums.sdo.PhysicalTrialBundleOptions;
import uk.gov.hmcts.reform.civil.enums.sdo.TrialOnRadioOptions;
import uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantFastTrack;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackJudgesRecital;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2AddendumReport;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2ApplicationToRelyOnFurther;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2ApplicationToRelyOnFurtherDetails;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2DisclosureOfDocuments;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2EvidenceAcousticEngineer;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2ExpertEvidence;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2FurtherAudiogram;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2PermissionToRelyOnExpert;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2QuestionsClaimantExpert;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2QuestionsToEntExpert;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2RestrictNoOfPagesDetails;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2RestrictNoOfWitnessDetails;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2RestrictPages;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2RestrictWitness;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2ScheduleOfLoss;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2Trial;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2TrialFirstOpenDateAfter;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2TrialWindow;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2UploadOfDocuments;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2WelshLanguageUsage;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2WitnessOfFact;

import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;

/**
 * Builds the NIHL-specific order sections so {@link SdoNihlFieldsService} can focus on track routing
 * and court-location wiring.
 */
@Service
@RequiredArgsConstructor
public class SdoNihlOrderService {

    private final SdoDeadlineService sdoDeadlineService;

    public void populateStandardDirections(CaseData caseData,
                                           DynamicList hearingMethodList,
                                           DynamicList trialCourtList,
                                           DynamicList alternativeCourtLocations) {

        caseData.setSdoFastTrackJudgesRecital(new FastTrackJudgesRecital()
                                                  .setInput(SdoR2UiConstantFastTrack.JUDGE_RECITAL));

        SdoR2DisclosureOfDocuments disclosureOfDocuments = new SdoR2DisclosureOfDocuments();
        disclosureOfDocuments.setStandardDisclosureTxt(SdoR2UiConstantFastTrack.STANDARD_DISCLOSURE);
        disclosureOfDocuments.setStandardDisclosureDate(sdoDeadlineService.calendarDaysFromNow(28));
        disclosureOfDocuments.setInspectionTxt(SdoR2UiConstantFastTrack.INSPECTION);
        disclosureOfDocuments.setInspectionDate(sdoDeadlineService.calendarDaysFromNow(42));
        disclosureOfDocuments.setRequestsWillBeCompiledLabel(SdoR2UiConstantFastTrack.REQUEST_COMPILED_WITH);
        caseData.setSdoR2DisclosureOfDocuments(disclosureOfDocuments);

        caseData.setSdoR2WitnessesOfFact(buildWitnessesOfFact());

        SdoR2ScheduleOfLoss scheduleOfLoss = new SdoR2ScheduleOfLoss();
        scheduleOfLoss.setSdoR2ScheduleOfLossClaimantText(SdoR2UiConstantFastTrack.SCHEDULE_OF_LOSS_CLAIMANT);
        scheduleOfLoss.setIsClaimForPecuniaryLoss(NO);
        scheduleOfLoss.setSdoR2ScheduleOfLossClaimantDate(sdoDeadlineService.calendarDaysFromNow(364));
        scheduleOfLoss.setSdoR2ScheduleOfLossDefendantText(SdoR2UiConstantFastTrack.SCHEDULE_OF_LOSS_DEFENDANT);
        scheduleOfLoss.setSdoR2ScheduleOfLossDefendantDate(sdoDeadlineService.calendarDaysFromNow(378));
        scheduleOfLoss.setSdoR2ScheduleOfLossPecuniaryLossTxt(SdoR2UiConstantFastTrack.PECUNIARY_LOSS);
        caseData.setSdoR2ScheduleOfLoss(scheduleOfLoss);

        caseData.setSdoR2Trial(buildTrial(hearingMethodList, trialCourtList, alternativeCourtLocations));

        caseData.setSdoR2ImportantNotesTxt(SdoR2UiConstantFastTrack.IMPORTANT_NOTES);
        caseData.setSdoR2ImportantNotesDate(sdoDeadlineService.calendarDaysFromNow(7));

        SdoR2ExpertEvidence expertEvidence = new SdoR2ExpertEvidence();
        expertEvidence.setSdoClaimantPermissionToRelyTxt(SdoR2UiConstantFastTrack.CLAIMANT_PERMISSION_TO_RELY);
        caseData.setSdoR2ExpertEvidence(expertEvidence);

        SdoR2AddendumReport addendumReport = new SdoR2AddendumReport();
        addendumReport.setSdoAddendumReportTxt(SdoR2UiConstantFastTrack.ADDENDUM_REPORT);
        addendumReport.setSdoAddendumReportDate(sdoDeadlineService.calendarDaysFromNow(56));
        caseData.setSdoR2AddendumReport(addendumReport);

        caseData.setSdoR2FurtherAudiogram(buildFurtherAudiogram());
        caseData.setSdoR2QuestionsClaimantExpert(buildQuestionsForClaimantExpert());
        caseData.setSdoR2PermissionToRelyOnExpert(buildPermissionToRelyOnExpert());
        caseData.setSdoR2EvidenceAcousticEngineer(buildEvidenceFromAcousticEngineer());
        caseData.setSdoR2QuestionsToEntExpert(buildQuestionsToEntExpert());
        SdoR2UploadOfDocuments uploadOfDocuments = new SdoR2UploadOfDocuments();
        uploadOfDocuments.setSdoUploadOfDocumentsTxt(SdoR2UiConstantFastTrack.UPLOAD_OF_DOCUMENTS);
        caseData.setSdoR2UploadOfDocuments(uploadOfDocuments);
        SdoR2WelshLanguageUsage welshLanguageUsage = new SdoR2WelshLanguageUsage();
        welshLanguageUsage.setDescription(SdoR2UiConstantFastTrack.WELSH_LANG_DESCRIPTION);
        caseData.setSdoR2NihlUseOfWelshLanguage(welshLanguageUsage);
    }

    private SdoR2WitnessOfFact buildWitnessesOfFact() {
        SdoR2RestrictNoOfWitnessDetails restrictWitnessDetails = new SdoR2RestrictNoOfWitnessDetails();
        restrictWitnessDetails.setNoOfWitnessClaimant(3);
        restrictWitnessDetails.setNoOfWitnessDefendant(3);
        restrictWitnessDetails.setPartyIsCountedAsWitnessTxt(SdoR2UiConstantFastTrack.RESTRICT_WITNESS_TEXT);

        SdoR2RestrictWitness restrictWitness = new SdoR2RestrictWitness();
        restrictWitness.setIsRestrictWitness(NO);
        restrictWitness.setRestrictNoOfWitnessDetails(restrictWitnessDetails);

        SdoR2RestrictNoOfPagesDetails restrictPagesDetails = new SdoR2RestrictNoOfPagesDetails();
        restrictPagesDetails.setWitnessShouldNotMoreThanTxt(SdoR2UiConstantFastTrack.RESTRICT_NUMBER_PAGES_TEXT1);
        restrictPagesDetails.setNoOfPages(12);
        restrictPagesDetails.setFontDetails(SdoR2UiConstantFastTrack.RESTRICT_NUMBER_PAGES_TEXT2);

        SdoR2RestrictPages restrictPages = new SdoR2RestrictPages();
        restrictPages.setIsRestrictPages(NO);
        restrictPages.setRestrictNoOfPagesDetails(restrictPagesDetails);

        SdoR2WitnessOfFact witnessOfFact = new SdoR2WitnessOfFact();
        witnessOfFact.setSdoStatementOfWitness(SdoR2UiConstantFastTrack.STATEMENT_WITNESS);
        witnessOfFact.setSdoR2RestrictWitness(restrictWitness);
        witnessOfFact.setSdoRestrictPages(restrictPages);
        witnessOfFact.setSdoWitnessDeadline(SdoR2UiConstantFastTrack.DEADLINE);
        witnessOfFact.setSdoWitnessDeadlineDate(sdoDeadlineService.calendarDaysFromNow(70));
        witnessOfFact.setSdoWitnessDeadlineText(SdoR2UiConstantFastTrack.DEADLINE_EVIDENCE);
        return witnessOfFact;
    }

    private SdoR2Trial buildTrial(DynamicList hearingMethodList,
                                  DynamicList trialCourtList,
                                  DynamicList alternativeCourtLocations) {
        SdoR2TrialFirstOpenDateAfter firstOpen = new SdoR2TrialFirstOpenDateAfter();
        firstOpen.setListFrom(sdoDeadlineService.calendarDaysFromNow(434));

        SdoR2TrialWindow trialWindow = new SdoR2TrialWindow();
        trialWindow.setListFrom(sdoDeadlineService.calendarDaysFromNow(434));
        trialWindow.setDateTo(sdoDeadlineService.calendarDaysFromNow(455));

        SdoR2Trial trial = new SdoR2Trial();
        trial.setTrialOnOptions(TrialOnRadioOptions.OPEN_DATE);
        trial.setLengthList(FastTrackHearingTimeEstimate.FIVE_HOURS);
        trial.setMethodOfHearing(hearingMethodList);
        trial.setPhysicalBundleOptions(PhysicalTrialBundleOptions.PARTY);
        trial.setSdoR2TrialFirstOpenDateAfter(firstOpen);
        trial.setSdoR2TrialWindow(trialWindow);
        trial.setPhysicalBundlePartyTxt(SdoR2UiConstantFastTrack.PHYSICAL_TRIAL_BUNDLE);

        if (trialCourtList != null) {
            trial.setHearingCourtLocationList(trialCourtList);
        }
        if (alternativeCourtLocations != null) {
            trial.setAltHearingCourtLocationList(alternativeCourtLocations);
        }
        return trial;
    }

    private SdoR2FurtherAudiogram buildFurtherAudiogram() {
        SdoR2FurtherAudiogram furtherAudiogram = new SdoR2FurtherAudiogram();
        furtherAudiogram.setSdoClaimantShallUndergoTxt(SdoR2UiConstantFastTrack.CLAIMANT_SHALL_UNDERGO);
        furtherAudiogram.setSdoServiceReportTxt(SdoR2UiConstantFastTrack.SERVICE_REPORT);
        furtherAudiogram.setSdoClaimantShallUndergoDate(sdoDeadlineService.calendarDaysFromNow(42));
        furtherAudiogram.setSdoServiceReportDate(sdoDeadlineService.calendarDaysFromNow(98));
        return furtherAudiogram;
    }

    private SdoR2QuestionsClaimantExpert buildQuestionsForClaimantExpert() {
        SdoR2ApplicationToRelyOnFurtherDetails applicationDetails = new SdoR2ApplicationToRelyOnFurtherDetails();
        applicationDetails.setApplicationToRelyDetailsTxt(SdoR2UiConstantFastTrack.APPLICATION_TO_RELY_DETAILS);
        applicationDetails.setApplicationToRelyDetailsDate(sdoDeadlineService.calendarDaysFromNow(161));

        SdoR2ApplicationToRelyOnFurther applicationToRelyOnFurther = new SdoR2ApplicationToRelyOnFurther();
        applicationToRelyOnFurther.setDoRequireApplicationToRely(NO);
        applicationToRelyOnFurther.setApplicationToRelyOnFurtherDetails(applicationDetails);

        SdoR2QuestionsClaimantExpert questionsClaimantExpert = new SdoR2QuestionsClaimantExpert();
        questionsClaimantExpert.setSdoDefendantMayAskTxt(SdoR2UiConstantFastTrack.DEFENDANT_MAY_ASK);
        questionsClaimantExpert.setSdoDefendantMayAskDate(sdoDeadlineService.calendarDaysFromNow(126));
        questionsClaimantExpert.setSdoQuestionsShallBeAnsweredTxt(SdoR2UiConstantFastTrack.QUESTIONS_SHALL_BE_ANSWERED);
        questionsClaimantExpert.setSdoQuestionsShallBeAnsweredDate(sdoDeadlineService.calendarDaysFromNow(147));
        questionsClaimantExpert.setSdoUploadedToDigitalPortalTxt(SdoR2UiConstantFastTrack.UPLOADED_TO_DIGITAL_PORTAL);
        questionsClaimantExpert.setSdoApplicationToRelyOnFurther(applicationToRelyOnFurther);
        return questionsClaimantExpert;
    }

    private SdoR2PermissionToRelyOnExpert buildPermissionToRelyOnExpert() {
        SdoR2PermissionToRelyOnExpert permissionToRelyOnExpert = new SdoR2PermissionToRelyOnExpert();
        permissionToRelyOnExpert.setSdoPermissionToRelyOnExpertTxt(SdoR2UiConstantFastTrack.PERMISSION_TO_RELY_ON_EXPERT);
        permissionToRelyOnExpert.setSdoPermissionToRelyOnExpertDate(sdoDeadlineService.calendarDaysFromNow(119));
        permissionToRelyOnExpert.setSdoJointMeetingOfExpertsTxt(SdoR2UiConstantFastTrack.JOINT_MEETING_OF_EXPERTS);
        permissionToRelyOnExpert.setSdoJointMeetingOfExpertsDate(sdoDeadlineService.calendarDaysFromNow(147));
        permissionToRelyOnExpert.setSdoUploadedToDigitalPortalTxt(SdoR2UiConstantFastTrack.UPLOADED_TO_DIGITAL_PORTAL_7_DAYS);
        return permissionToRelyOnExpert;
    }

    private SdoR2EvidenceAcousticEngineer buildEvidenceFromAcousticEngineer() {
        SdoR2EvidenceAcousticEngineer acousticEngineer = new SdoR2EvidenceAcousticEngineer();
        acousticEngineer.setSdoEvidenceAcousticEngineerTxt(SdoR2UiConstantFastTrack.EVIDENCE_ACOUSTIC_ENGINEER);
        acousticEngineer.setSdoInstructionOfTheExpertTxt(SdoR2UiConstantFastTrack.INSTRUCTION_OF_EXPERT);
        acousticEngineer.setSdoInstructionOfTheExpertDate(sdoDeadlineService.calendarDaysFromNow(42));
        acousticEngineer.setSdoInstructionOfTheExpertTxtArea(SdoR2UiConstantFastTrack.INSTRUCTION_OF_EXPERT_TA);
        acousticEngineer.setSdoExpertReportTxt(SdoR2UiConstantFastTrack.EXPERT_REPORT);
        acousticEngineer.setSdoExpertReportDate(sdoDeadlineService.calendarDaysFromNow(280));
        acousticEngineer.setSdoExpertReportDigitalPortalTxt(SdoR2UiConstantFastTrack.EXPERT_REPORT_DIGITAL_PORTAL);
        acousticEngineer.setSdoWrittenQuestionsTxt(SdoR2UiConstantFastTrack.WRITTEN_QUESTIONS);
        acousticEngineer.setSdoWrittenQuestionsDate(sdoDeadlineService.calendarDaysFromNow(294));
        acousticEngineer.setSdoWrittenQuestionsDigitalPortalTxt(SdoR2UiConstantFastTrack.WRITTEN_QUESTIONS_DIGITAL_PORTAL);
        acousticEngineer.setSdoRepliesTxt(SdoR2UiConstantFastTrack.REPLIES);
        acousticEngineer.setSdoRepliesDate(sdoDeadlineService.calendarDaysFromNow(315));
        acousticEngineer.setSdoRepliesDigitalPortalTxt(SdoR2UiConstantFastTrack.REPLIES_DIGITAL_PORTAL);
        acousticEngineer.setSdoServiceOfOrderTxt(SdoR2UiConstantFastTrack.SERVICE_OF_ORDER);
        return acousticEngineer;
    }

    private SdoR2QuestionsToEntExpert buildQuestionsToEntExpert() {
        SdoR2QuestionsToEntExpert questionsToEntExpert = new SdoR2QuestionsToEntExpert();
        questionsToEntExpert.setSdoWrittenQuestionsTxt(SdoR2UiConstantFastTrack.ENT_WRITTEN_QUESTIONS);
        questionsToEntExpert.setSdoWrittenQuestionsDate(sdoDeadlineService.calendarDaysFromNow(336));
        questionsToEntExpert.setSdoWrittenQuestionsDigPortalTxt(SdoR2UiConstantFastTrack.ENT_WRITTEN_QUESTIONS_DIG_PORTAL);
        questionsToEntExpert.setSdoQuestionsShallBeAnsweredTxt(SdoR2UiConstantFastTrack.ENT_QUESTIONS_SHALL_BE_ANSWERED);
        questionsToEntExpert.setSdoQuestionsShallBeAnsweredDate(sdoDeadlineService.calendarDaysFromNow(350));
        questionsToEntExpert.setSdoShallBeUploadedTxt(SdoR2UiConstantFastTrack.ENT_SHALL_BE_UPLOADED);
        return questionsToEntExpert;
    }
}
