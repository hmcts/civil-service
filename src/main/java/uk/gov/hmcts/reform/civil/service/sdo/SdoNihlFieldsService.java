package uk.gov.hmcts.reform.civil.service.sdo;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.sdo.FastTrackHearingTimeEstimate;
import uk.gov.hmcts.reform.civil.enums.sdo.HearingMethod;
import uk.gov.hmcts.reform.civil.enums.sdo.PhysicalTrialBundleOptions;
import uk.gov.hmcts.reform.civil.enums.sdo.TrialOnRadioOptions;
import uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantFastTrack;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.dq.RequestedCourt;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackJudgesRecital;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2AddendumReport;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2ApplicationToRelyOnFurther;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2ApplicationToRelyOnFurtherDetails;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2DisclosureOfDocuments;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2EvidenceAcousticEngineer;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2ExpertEvidence;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2FastTrackAltDisputeResolution;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2FastTrackCreditHire;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2FastTrackCreditHireDetails;
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
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;

import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;

@Service
@RequiredArgsConstructor
public class SdoNihlFieldsService {

    private final SdoLocationService sdoLocationService;
    private final SdoDeadlineService sdoDeadlineService;

    public void populateNihlFields(CaseData.CaseDataBuilder<?, ?> updatedData,
                                   DynamicList hearingMethodList,
                                   Optional<RequestedCourt> preferredCourt,
                                   List<LocationRefData> locationRefDataList) {


        DynamicListElement hearingMethodInPerson = hearingMethodList.getListItems().stream().filter(elem -> elem.getLabel()
            .equals(HearingMethod.IN_PERSON.getLabel())).findFirst().orElse(null);
        hearingMethodList.setValue(hearingMethodInPerson);
        updatedData.sdoFastTrackJudgesRecital(FastTrackJudgesRecital.builder()
                                                  .input(SdoR2UiConstantFastTrack.JUDGE_RECITAL).build());
        updatedData.sdoR2DisclosureOfDocuments(SdoR2DisclosureOfDocuments.builder()
                                                   .standardDisclosureTxt(SdoR2UiConstantFastTrack.STANDARD_DISCLOSURE)
                                                   .standardDisclosureDate(sdoDeadlineService.calendarDaysFromNow(28))
                                                   .inspectionTxt(SdoR2UiConstantFastTrack.INSPECTION)
                                                   .inspectionDate(sdoDeadlineService.calendarDaysFromNow(42))
                                                   .requestsWillBeCompiledLabel(SdoR2UiConstantFastTrack.REQUEST_COMPILED_WITH)
                                                   .build());
        updatedData.sdoR2WitnessesOfFact(SdoR2WitnessOfFact.builder()
                                             .sdoStatementOfWitness(SdoR2UiConstantFastTrack.STATEMENT_WITNESS)
                                             .sdoR2RestrictWitness(SdoR2RestrictWitness.builder()
                                                                       .isRestrictWitness(NO)
                                                                       .restrictNoOfWitnessDetails(
                                                                           SdoR2RestrictNoOfWitnessDetails
                                                                               .builder()
                                                                               .noOfWitnessClaimant(3).noOfWitnessDefendant(
                                                                                   3)
                                                                               .partyIsCountedAsWitnessTxt(
                                                                                   SdoR2UiConstantFastTrack.RESTRICT_WITNESS_TEXT)
                                                                               .build())
                                                                       .build())
                                             .sdoRestrictPages(SdoR2RestrictPages.builder()
                                                                   .isRestrictPages(NO)
                                                                   .restrictNoOfPagesDetails(
                                                                       SdoR2RestrictNoOfPagesDetails.builder()
                                                                           .witnessShouldNotMoreThanTxt(
                                                                               SdoR2UiConstantFastTrack.RESTRICT_NUMBER_PAGES_TEXT1)
                                                                           .noOfPages(12)
                                                                           .fontDetails(SdoR2UiConstantFastTrack.RESTRICT_NUMBER_PAGES_TEXT2)
                                                                           .build()).build())
                                             .sdoWitnessDeadline(SdoR2UiConstantFastTrack.DEADLINE)
                                             .sdoWitnessDeadlineDate(sdoDeadlineService.calendarDaysFromNow(70))
                                             .sdoWitnessDeadlineText(SdoR2UiConstantFastTrack.DEADLINE_EVIDENCE)
                                             .build());
        updatedData.sdoR2ScheduleOfLoss(SdoR2ScheduleOfLoss.builder().sdoR2ScheduleOfLossClaimantText(
                SdoR2UiConstantFastTrack.SCHEDULE_OF_LOSS_CLAIMANT)
                                            .isClaimForPecuniaryLoss(NO)
                                            .sdoR2ScheduleOfLossClaimantDate(sdoDeadlineService.calendarDaysFromNow(364))
                                            .sdoR2ScheduleOfLossDefendantText(SdoR2UiConstantFastTrack.SCHEDULE_OF_LOSS_DEFENDANT)
                                            .sdoR2ScheduleOfLossDefendantDate(sdoDeadlineService.calendarDaysFromNow(378))
                                            .sdoR2ScheduleOfLossPecuniaryLossTxt(SdoR2UiConstantFastTrack.PECUNIARY_LOSS)
                                            .build());
        DynamicList trialCourtList = sdoLocationService.buildCourtLocationForSdoR2(preferredCourt.orElse(null), locationRefDataList);
        if (trialCourtList != null && trialCourtList.getListItems() != null && !trialCourtList.getListItems().isEmpty()) {
            trialCourtList.setValue(trialCourtList.getListItems().get(0));
        }

        updatedData.sdoR2Trial(SdoR2Trial.builder()
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
                                   .hearingCourtLocationList(trialCourtList)

                                   .altHearingCourtLocationList(sdoLocationService.buildAlternativeCourtLocations(locationRefDataList))
                                   .physicalBundlePartyTxt(SdoR2UiConstantFastTrack.PHYSICAL_TRIAL_BUNDLE)
                                   .build());

        updatedData.sdoR2ImportantNotesTxt(SdoR2UiConstantFastTrack.IMPORTANT_NOTES);
        updatedData.sdoR2ImportantNotesDate(sdoDeadlineService.calendarDaysFromNow(7));

        updatedData.sdoR2ExpertEvidence(SdoR2ExpertEvidence.builder()
                                            .sdoClaimantPermissionToRelyTxt(SdoR2UiConstantFastTrack.CLAIMANT_PERMISSION_TO_RELY).build());
        updatedData.sdoR2AddendumReport(SdoR2AddendumReport.builder()
                                            .sdoAddendumReportTxt(SdoR2UiConstantFastTrack.ADDENDUM_REPORT)
                                            .sdoAddendumReportDate(sdoDeadlineService.calendarDaysFromNow(56)).build());
        updatedData.sdoR2FurtherAudiogram(SdoR2FurtherAudiogram.builder()
                                              .sdoClaimantShallUndergoTxt(SdoR2UiConstantFastTrack.CLAIMANT_SHALL_UNDERGO)
                                              .sdoServiceReportTxt(SdoR2UiConstantFastTrack.SERVICE_REPORT)
                                              .sdoClaimantShallUndergoDate(sdoDeadlineService.calendarDaysFromNow(42))
                                              .sdoServiceReportDate(sdoDeadlineService.calendarDaysFromNow(98)).build());
        updatedData.sdoR2QuestionsClaimantExpert(SdoR2QuestionsClaimantExpert.builder()
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
                                                                     .applicationToRelyDetailsTxt(
                                                                         SdoR2UiConstantFastTrack.APPLICATION_TO_RELY_DETAILS)
                                                                     .applicationToRelyDetailsDate(sdoDeadlineService.calendarDaysFromNow(
                                                                         161)).build()).build())
                                                     .build());
        updatedData.sdoR2PermissionToRelyOnExpert(SdoR2PermissionToRelyOnExpert.builder()
                                                      .sdoPermissionToRelyOnExpertTxt(SdoR2UiConstantFastTrack.PERMISSION_TO_RELY_ON_EXPERT)
                                                      .sdoPermissionToRelyOnExpertDate(sdoDeadlineService.calendarDaysFromNow(119))
                                                      .sdoJointMeetingOfExpertsTxt(SdoR2UiConstantFastTrack.JOINT_MEETING_OF_EXPERTS)
                                                      .sdoJointMeetingOfExpertsDate(sdoDeadlineService.calendarDaysFromNow(147))
                                                      .sdoUploadedToDigitalPortalTxt(SdoR2UiConstantFastTrack.UPLOADED_TO_DIGITAL_PORTAL_7_DAYS)
                                                      .build());
        updatedData.sdoR2EvidenceAcousticEngineer(SdoR2EvidenceAcousticEngineer.builder()
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
                                                      .build());
        updatedData.sdoR2QuestionsToEntExpert(SdoR2QuestionsToEntExpert.builder()
                                                  .sdoWrittenQuestionsTxt(SdoR2UiConstantFastTrack.ENT_WRITTEN_QUESTIONS)
                                                  .sdoWrittenQuestionsDate(sdoDeadlineService.calendarDaysFromNow(336))
                                                  .sdoWrittenQuestionsDigPortalTxt(SdoR2UiConstantFastTrack.ENT_WRITTEN_QUESTIONS_DIG_PORTAL)
                                                  .sdoQuestionsShallBeAnsweredTxt(SdoR2UiConstantFastTrack.ENT_QUESTIONS_SHALL_BE_ANSWERED)
                                                  .sdoQuestionsShallBeAnsweredDate(sdoDeadlineService.calendarDaysFromNow(350))
                                                  .sdoShallBeUploadedTxt(SdoR2UiConstantFastTrack.ENT_SHALL_BE_UPLOADED)
                                                  .build());
        updatedData.sdoR2UploadOfDocuments(SdoR2UploadOfDocuments.builder()
                                               .sdoUploadOfDocumentsTxt(SdoR2UiConstantFastTrack.UPLOAD_OF_DOCUMENTS)
                                               .build());
        updatedData.sdoR2NihlUseOfWelshLanguage(SdoR2WelshLanguageUsage.builder().description(SdoR2UiConstantFastTrack.WELSH_LANG_DESCRIPTION).build());

    
    }
}
