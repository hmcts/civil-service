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

        caseData.setSdoR2DisclosureOfDocuments(SdoR2DisclosureOfDocuments.builder()
                                                  .standardDisclosureTxt(SdoR2UiConstantFastTrack.STANDARD_DISCLOSURE)
                                                  .standardDisclosureDate(sdoDeadlineService.calendarDaysFromNow(28))
                                                  .inspectionTxt(SdoR2UiConstantFastTrack.INSPECTION)
                                                  .inspectionDate(sdoDeadlineService.calendarDaysFromNow(42))
                                                  .requestsWillBeCompiledLabel(SdoR2UiConstantFastTrack.REQUEST_COMPILED_WITH)
                                                  .build());

        caseData.setSdoR2WitnessesOfFact(buildWitnessesOfFact());

        caseData.setSdoR2ScheduleOfLoss(SdoR2ScheduleOfLoss.builder()
                                             .sdoR2ScheduleOfLossClaimantText(SdoR2UiConstantFastTrack.SCHEDULE_OF_LOSS_CLAIMANT)
                                             .isClaimForPecuniaryLoss(NO)
                                             .sdoR2ScheduleOfLossClaimantDate(sdoDeadlineService.calendarDaysFromNow(364))
                                             .sdoR2ScheduleOfLossDefendantText(SdoR2UiConstantFastTrack.SCHEDULE_OF_LOSS_DEFENDANT)
                                             .sdoR2ScheduleOfLossDefendantDate(sdoDeadlineService.calendarDaysFromNow(378))
                                             .sdoR2ScheduleOfLossPecuniaryLossTxt(SdoR2UiConstantFastTrack.PECUNIARY_LOSS)
                                             .build());

        caseData.setSdoR2Trial(buildTrial(hearingMethodList, trialCourtList, alternativeCourtLocations));

        caseData.setSdoR2ImportantNotesTxt(SdoR2UiConstantFastTrack.IMPORTANT_NOTES);
        caseData.setSdoR2ImportantNotesDate(sdoDeadlineService.calendarDaysFromNow(7));

        caseData.setSdoR2ExpertEvidence(SdoR2ExpertEvidence.builder()
                                               .sdoClaimantPermissionToRelyTxt(SdoR2UiConstantFastTrack.CLAIMANT_PERMISSION_TO_RELY).build());

        caseData.setSdoR2AddendumReport(SdoR2AddendumReport.builder()
                                               .sdoAddendumReportTxt(SdoR2UiConstantFastTrack.ADDENDUM_REPORT)
                                               .sdoAddendumReportDate(sdoDeadlineService.calendarDaysFromNow(56)).build());

        caseData.setSdoR2FurtherAudiogram(buildFurtherAudiogram());
        caseData.setSdoR2QuestionsClaimantExpert(buildQuestionsForClaimantExpert());
        caseData.setSdoR2PermissionToRelyOnExpert(buildPermissionToRelyOnExpert());
        caseData.setSdoR2EvidenceAcousticEngineer(buildEvidenceFromAcousticEngineer());
        caseData.setSdoR2QuestionsToEntExpert(buildQuestionsToEntExpert());
        caseData.setSdoR2UploadOfDocuments(SdoR2UploadOfDocuments.builder()
                                                  .sdoUploadOfDocumentsTxt(SdoR2UiConstantFastTrack.UPLOAD_OF_DOCUMENTS)
                                                  .build());
        caseData.setSdoR2NihlUseOfWelshLanguage(SdoR2WelshLanguageUsage.builder()
                                                  .description(SdoR2UiConstantFastTrack.WELSH_LANG_DESCRIPTION).build());
    }

    private SdoR2WitnessOfFact buildWitnessesOfFact() {
        return SdoR2WitnessOfFact.builder()
            .sdoStatementOfWitness(SdoR2UiConstantFastTrack.STATEMENT_WITNESS)
            .sdoR2RestrictWitness(SdoR2RestrictWitness.builder()
                                      .isRestrictWitness(NO)
                                      .restrictNoOfWitnessDetails(
                                          SdoR2RestrictNoOfWitnessDetails.builder()
                                              .noOfWitnessClaimant(3).noOfWitnessDefendant(3)
                                              .partyIsCountedAsWitnessTxt(SdoR2UiConstantFastTrack.RESTRICT_WITNESS_TEXT)
                                              .build())
                                      .build())
            .sdoRestrictPages(SdoR2RestrictPages.builder()
                                  .isRestrictPages(NO)
                                  .restrictNoOfPagesDetails(
                                      SdoR2RestrictNoOfPagesDetails.builder()
                                          .witnessShouldNotMoreThanTxt(SdoR2UiConstantFastTrack.RESTRICT_NUMBER_PAGES_TEXT1)
                                          .noOfPages(12)
                                          .fontDetails(SdoR2UiConstantFastTrack.RESTRICT_NUMBER_PAGES_TEXT2)
                                          .build()).build())
            .sdoWitnessDeadline(SdoR2UiConstantFastTrack.DEADLINE)
            .sdoWitnessDeadlineDate(sdoDeadlineService.calendarDaysFromNow(70))
            .sdoWitnessDeadlineText(SdoR2UiConstantFastTrack.DEADLINE_EVIDENCE)
            .build();
    }

    private SdoR2Trial buildTrial(DynamicList hearingMethodList,
                                  DynamicList trialCourtList,
                                  DynamicList alternativeCourtLocations) {
        SdoR2Trial.SdoR2TrialBuilder builder = SdoR2Trial.builder()
            .trialOnOptions(TrialOnRadioOptions.OPEN_DATE)
            .lengthList(FastTrackHearingTimeEstimate.FIVE_HOURS)
            .methodOfHearing(hearingMethodList)
            .physicalBundleOptions(PhysicalTrialBundleOptions.PARTY)
            .sdoR2TrialFirstOpenDateAfter(
                SdoR2TrialFirstOpenDateAfter.builder()
                    .listFrom(sdoDeadlineService.calendarDaysFromNow(434)).build())
            .sdoR2TrialWindow(SdoR2TrialWindow.builder()
                                  .listFrom(sdoDeadlineService.calendarDaysFromNow(434))
                                  .dateTo(sdoDeadlineService.calendarDaysFromNow(455))
                                  .build())
            .physicalBundlePartyTxt(SdoR2UiConstantFastTrack.PHYSICAL_TRIAL_BUNDLE);

        if (trialCourtList != null) {
            builder.hearingCourtLocationList(trialCourtList);
        }
        if (alternativeCourtLocations != null) {
            builder.altHearingCourtLocationList(alternativeCourtLocations);
        }
        return builder.build();
    }

    private SdoR2FurtherAudiogram buildFurtherAudiogram() {
        return SdoR2FurtherAudiogram.builder()
            .sdoClaimantShallUndergoTxt(SdoR2UiConstantFastTrack.CLAIMANT_SHALL_UNDERGO)
            .sdoServiceReportTxt(SdoR2UiConstantFastTrack.SERVICE_REPORT)
            .sdoClaimantShallUndergoDate(sdoDeadlineService.calendarDaysFromNow(42))
            .sdoServiceReportDate(sdoDeadlineService.calendarDaysFromNow(98))
            .build();
    }

    private SdoR2QuestionsClaimantExpert buildQuestionsForClaimantExpert() {
        return SdoR2QuestionsClaimantExpert.builder()
            .sdoDefendantMayAskTxt(SdoR2UiConstantFastTrack.DEFENDANT_MAY_ASK)
            .sdoDefendantMayAskDate(sdoDeadlineService.calendarDaysFromNow(126))
            .sdoQuestionsShallBeAnsweredTxt(SdoR2UiConstantFastTrack.QUESTIONS_SHALL_BE_ANSWERED)
            .sdoQuestionsShallBeAnsweredDate(sdoDeadlineService.calendarDaysFromNow(147))
            .sdoUploadedToDigitalPortalTxt(SdoR2UiConstantFastTrack.UPLOADED_TO_DIGITAL_PORTAL)
            .sdoApplicationToRelyOnFurther(
                SdoR2ApplicationToRelyOnFurther.builder()
                    .doRequireApplicationToRely(NO)
                    .applicationToRelyOnFurtherDetails(
                        SdoR2ApplicationToRelyOnFurtherDetails.builder()
                            .applicationToRelyDetailsTxt(SdoR2UiConstantFastTrack.APPLICATION_TO_RELY_DETAILS)
                            .applicationToRelyDetailsDate(sdoDeadlineService.calendarDaysFromNow(161)).build())
                    .build())
            .build();
    }

    private SdoR2PermissionToRelyOnExpert buildPermissionToRelyOnExpert() {
        return SdoR2PermissionToRelyOnExpert.builder()
            .sdoPermissionToRelyOnExpertTxt(SdoR2UiConstantFastTrack.PERMISSION_TO_RELY_ON_EXPERT)
            .sdoPermissionToRelyOnExpertDate(sdoDeadlineService.calendarDaysFromNow(119))
            .sdoJointMeetingOfExpertsTxt(SdoR2UiConstantFastTrack.JOINT_MEETING_OF_EXPERTS)
            .sdoJointMeetingOfExpertsDate(sdoDeadlineService.calendarDaysFromNow(147))
            .sdoUploadedToDigitalPortalTxt(SdoR2UiConstantFastTrack.UPLOADED_TO_DIGITAL_PORTAL_7_DAYS)
            .build();
    }

    private SdoR2EvidenceAcousticEngineer buildEvidenceFromAcousticEngineer() {
        return SdoR2EvidenceAcousticEngineer.builder()
            .sdoEvidenceAcousticEngineerTxt(SdoR2UiConstantFastTrack.EVIDENCE_ACOUSTIC_ENGINEER)
            .sdoInstructionOfTheExpertTxt(SdoR2UiConstantFastTrack.INSTRUCTION_OF_EXPERT)
            .sdoInstructionOfTheExpertDate(sdoDeadlineService.calendarDaysFromNow(42))
            .sdoInstructionOfTheExpertTxtArea(SdoR2UiConstantFastTrack.INSTRUCTION_OF_EXPERT_TA)
            .sdoExpertReportTxt(SdoR2UiConstantFastTrack.EXPERT_REPORT)
            .sdoExpertReportDate(sdoDeadlineService.calendarDaysFromNow(280))
            .sdoExpertReportDigitalPortalTxt(SdoR2UiConstantFastTrack.EXPERT_REPORT_DIGITAL_PORTAL)
            .sdoWrittenQuestionsTxt(SdoR2UiConstantFastTrack.WRITTEN_QUESTIONS)
            .sdoWrittenQuestionsDate(sdoDeadlineService.calendarDaysFromNow(294))
            .sdoWrittenQuestionsDigitalPortalTxt(SdoR2UiConstantFastTrack.WRITTEN_QUESTIONS_DIGITAL_PORTAL)
            .sdoRepliesTxt(SdoR2UiConstantFastTrack.REPLIES)
            .sdoRepliesDate(sdoDeadlineService.calendarDaysFromNow(315))
            .sdoRepliesDigitalPortalTxt(SdoR2UiConstantFastTrack.REPLIES_DIGITAL_PORTAL)
            .sdoServiceOfOrderTxt(SdoR2UiConstantFastTrack.SERVICE_OF_ORDER)
            .build();
    }

    private SdoR2QuestionsToEntExpert buildQuestionsToEntExpert() {
        return SdoR2QuestionsToEntExpert.builder()
            .sdoWrittenQuestionsTxt(SdoR2UiConstantFastTrack.ENT_WRITTEN_QUESTIONS)
            .sdoWrittenQuestionsDate(sdoDeadlineService.calendarDaysFromNow(336))
            .sdoWrittenQuestionsDigPortalTxt(SdoR2UiConstantFastTrack.ENT_WRITTEN_QUESTIONS_DIG_PORTAL)
            .sdoQuestionsShallBeAnsweredTxt(SdoR2UiConstantFastTrack.ENT_QUESTIONS_SHALL_BE_ANSWERED)
            .sdoQuestionsShallBeAnsweredDate(sdoDeadlineService.calendarDaysFromNow(350))
            .sdoShallBeUploadedTxt(SdoR2UiConstantFastTrack.ENT_SHALL_BE_UPLOADED)
            .build();
    }
}
