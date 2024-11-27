package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantFastTrack;
import uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantSmallClaim;
import uk.gov.hmcts.reform.civil.enums.sdo.FastTrackHearingTimeEstimate;
import uk.gov.hmcts.reform.civil.enums.sdo.HearingMethod;
import uk.gov.hmcts.reform.civil.enums.sdo.HearingOnRadioOptions;
import uk.gov.hmcts.reform.civil.enums.sdo.IncludeInOrderToggle;
import uk.gov.hmcts.reform.civil.enums.sdo.PhysicalTrialBundleOptions;
import uk.gov.hmcts.reform.civil.enums.sdo.SmallClaimsSdoR2PhysicalTrialBundleOptions;
import uk.gov.hmcts.reform.civil.enums.sdo.SmallClaimsSdoR2TimeEstimate;
import uk.gov.hmcts.reform.civil.enums.sdo.TrialOnRadioOptions;
import uk.gov.hmcts.reform.civil.helpers.LocationHelper;
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
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2FurtherAudiogram;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2PermissionToRelyOnExpert;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2QuestionsClaimantExpert;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2QuestionsToEntExpert;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2RestrictNoOfPagesDetails;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2RestrictNoOfWitnessDetails;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2RestrictPages;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2RestrictWitness;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2ScheduleOfLoss;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2SmallClaimsBundleOfDocs;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2SmallClaimsHearing;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2SmallClaimsHearingFirstOpenDateAfter;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2SmallClaimsHearingWindow;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2SmallClaimsImpNotes;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2SmallClaimsJudgesRecital;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2SmallClaimsMediation;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2SmallClaimsPPI;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2SmallClaimsRestrictPages;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2SmallClaimsRestrictWitness;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2SmallClaimsUploadDoc;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2SmallClaimsWitnessStatements;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2Trial;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2TrialFirstOpenDateAfter;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2TrialWindow;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2UploadOfDocuments;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2WelshLanguageUsage;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2WitnessOfFact;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.model.common.DynamicListElement.dynamicElementFromCode;

@Slf4j
@Component
@RequiredArgsConstructor
public class PrePopulateSdoR2AndNihlFields {

    private final FeatureToggleService featureToggleService;
    private final LocationHelper locationHelper;
    private final CreateSDOCallbackHandlerUtils createSDOCallbackHandlerUtils;
    private final List<IncludeInOrderToggle> includeInOrderToggle = List.of(IncludeInOrderToggle.INCLUDE);

    public void populateDRHFields(CallbackParams callbackParams,
                                  CaseData.CaseDataBuilder<?, ?> updatedData,
                                  Optional<RequestedCourt> preferredCourt,
                                  DynamicList hearingMethodList,
                                  List<LocationRefData> locationRefDataList) {
        DynamicList courtList = getCourtLocationForSdoR2(preferredCourt.orElse(null), locationRefDataList);
        courtList.setValue(courtList.getListItems().get(0));

        setHearingMethod(hearingMethodList);
        setSmallClaimsFields(updatedData, courtList, hearingMethodList, preferredCourt, locationRefDataList);

        CaseData caseData = callbackParams.getCaseData();
        if (featureToggleService.isCarmEnabledForCase(caseData)) {
            setCarmFields(updatedData);
        }
    }

    private void setHearingMethod(DynamicList hearingMethodList) {
        DynamicListElement hearingMethodElement = hearingMethodList.getListItems().stream()
                .filter(elem -> elem.getLabel().equals(HearingMethod.TELEPHONE.getLabel()))
                .findFirst()
                .orElse(null);
        hearingMethodList.setValue(hearingMethodElement);
    }

    private void setSmallClaimsFields(CaseData.CaseDataBuilder<?, ?> updatedData,
                                      DynamicList courtList,
                                      DynamicList hearingMethodList,
                                      Optional<RequestedCourt> preferredCourt,
                                      List<LocationRefData> locationRefDataList) {
        updatedData.sdoR2SmallClaimsJudgesRecital(SdoR2SmallClaimsJudgesRecital.builder().input(
                SdoR2UiConstantSmallClaim.JUDGE_RECITAL).build());
        updatedData.sdoR2SmallClaimsPPI(SdoR2SmallClaimsPPI.builder().ppiDate(LocalDate.now().plusDays(21)).text(
                SdoR2UiConstantSmallClaim.PPI_DESCRIPTION).build());
        updatedData.sdoR2SmallClaimsUploadDoc(SdoR2SmallClaimsUploadDoc.builder().sdoUploadOfDocumentsTxt(
                SdoR2UiConstantSmallClaim.UPLOAD_DOC_DESCRIPTION).build());
        updatedData.sdoR2SmallClaimsWitnessStatements(SdoR2SmallClaimsWitnessStatements.builder()
                .sdoStatementOfWitness(SdoR2UiConstantSmallClaim.WITNESS_STATEMENT_TEXT)
                .isRestrictWitness(NO)
                .isRestrictPages(NO)
                .sdoR2SmallClaimsRestrictWitness(SdoR2SmallClaimsRestrictWitness.builder()
                        .partyIsCountedAsWitnessTxt(SdoR2UiConstantSmallClaim.RESTRICT_WITNESS_TEXT)
                        .build())
                .sdoR2SmallClaimsRestrictPages(SdoR2SmallClaimsRestrictPages.builder()
                        .fontDetails(SdoR2UiConstantSmallClaim.RESTRICT_NUMBER_PAGES_TEXT2)
                        .noOfPages(12)
                        .witnessShouldNotMoreThanTxt(SdoR2UiConstantSmallClaim.RESTRICT_NUMBER_PAGES_TEXT1)
                        .build())
                .text(SdoR2UiConstantSmallClaim.WITNESS_DESCRIPTION_TEXT).build());
        updatedData.sdoR2SmallClaimsHearing(SdoR2SmallClaimsHearing.builder()
                .trialOnOptions(HearingOnRadioOptions.OPEN_DATE)
                .methodOfHearing(hearingMethodList)
                .lengthList(SmallClaimsSdoR2TimeEstimate.THIRTY_MINUTES)
                .physicalBundleOptions(SmallClaimsSdoR2PhysicalTrialBundleOptions.PARTY)
                .sdoR2SmallClaimsHearingFirstOpenDateAfter(SdoR2SmallClaimsHearingFirstOpenDateAfter.builder()
                        .listFrom(LocalDate.now().plusDays(56)).build())
                .sdoR2SmallClaimsHearingWindow(SdoR2SmallClaimsHearingWindow.builder().dateTo(
                                LocalDate.now().plusDays(70))
                        .listFrom(LocalDate.now().plusDays(56)).build())
                .hearingCourtLocationList(courtList)
                .altHearingCourtLocationList(createSDOCallbackHandlerUtils.getLocationList(
                        preferredCourt.orElse(null),
                        true,
                        locationRefDataList
                ))
                .sdoR2SmallClaimsBundleOfDocs(SdoR2SmallClaimsBundleOfDocs.builder()
                        .physicalBundlePartyTxt(SdoR2UiConstantSmallClaim.BUNDLE_TEXT).build()).build());
        updatedData.sdoR2SmallClaimsImpNotes(SdoR2SmallClaimsImpNotes.builder()
                .text(SdoR2UiConstantSmallClaim.IMP_NOTES_TEXT)
                .date(LocalDate.now().plusDays(7)).build());
        updatedData.sdoR2SmallClaimsUploadDocToggle(includeInOrderToggle);
        updatedData.sdoR2SmallClaimsHearingToggle(includeInOrderToggle);
        updatedData.sdoR2SmallClaimsWitnessStatementsToggle(includeInOrderToggle);
        updatedData.sdoR2DrhUseOfWelshLanguage(SdoR2WelshLanguageUsage.builder().description(SdoR2UiConstantFastTrack.WELSH_LANG_DESCRIPTION).build());
    }

    private void setCarmFields(CaseData.CaseDataBuilder<?, ?> updatedData) {
        updatedData.sdoR2SmallClaimsMediationSectionToggle(includeInOrderToggle);
        updatedData.sdoR2SmallClaimsMediationSectionStatement(SdoR2SmallClaimsMediation.builder()
                .input(SdoR2UiConstantSmallClaim.CARM_MEDIATION_TEXT)
                .build());
    }

    public void prePopulateNihlFields(CaseData.CaseDataBuilder<?, ?> updatedData, DynamicList hearingMethodList,
                                      Optional<RequestedCourt> preferredCourt, List<LocationRefData> locationRefDataList) {
        setHearingMethodInPerson(hearingMethodList);
        setFastTrackJudgesRecital(updatedData);
        setDisclosureOfDocuments(updatedData);
        setWitnessesOfFact(updatedData);
        setScheduleOfLoss(updatedData);
        setTrialDetails(updatedData, hearingMethodList, preferredCourt, locationRefDataList);
        setImportantNotes(updatedData);
        setExpertEvidence(updatedData);
        setAddendumReport(updatedData);
        setFurtherAudiogram(updatedData);
        setQuestionsClaimantExpert(updatedData);
        setPermissionToRelyOnExpert(updatedData);
        setEvidenceAcousticEngineer(updatedData);
        setQuestionsToEntExpert(updatedData);
        setUploadOfDocuments(updatedData);
        setWelshLanguageUsage(updatedData);
    }

    private void setHearingMethodInPerson(DynamicList hearingMethodList) {
        DynamicListElement hearingMethodInPerson = hearingMethodList.getListItems().stream()
                .filter(elem -> elem.getLabel().equals(HearingMethod.IN_PERSON.getLabel()))
                .findFirst()
                .orElse(null);
        hearingMethodList.setValue(hearingMethodInPerson);
    }

    private void setFastTrackJudgesRecital(CaseData.CaseDataBuilder<?, ?> updatedData) {
        updatedData.sdoFastTrackJudgesRecital(FastTrackJudgesRecital.builder()
                .input(SdoR2UiConstantFastTrack.JUDGE_RECITAL).build());
    }

    private void setDisclosureOfDocuments(CaseData.CaseDataBuilder<?, ?> updatedData) {
        updatedData.sdoR2DisclosureOfDocuments(SdoR2DisclosureOfDocuments.builder()
                .standardDisclosureTxt(SdoR2UiConstantFastTrack.STANDARD_DISCLOSURE)
                .standardDisclosureDate(LocalDate.now().plusDays(28))
                .inspectionTxt(SdoR2UiConstantFastTrack.INSPECTION)
                .inspectionDate(LocalDate.now().plusDays(42))
                .requestsWillBeCompiledLabel(SdoR2UiConstantFastTrack.REQUEST_COMPILED_WITH)
                .build());
    }

    private void setWitnessesOfFact(CaseData.CaseDataBuilder<?, ?> updatedData) {
        updatedData.sdoR2WitnessesOfFact(SdoR2WitnessOfFact.builder()
                .sdoStatementOfWitness(SdoR2UiConstantFastTrack.STATEMENT_WITNESS)
                .sdoR2RestrictWitness(SdoR2RestrictWitness.builder()
                        .isRestrictWitness(NO)
                        .restrictNoOfWitnessDetails(SdoR2RestrictNoOfWitnessDetails.builder()
                                .noOfWitnessClaimant(3)
                                .noOfWitnessDefendant(3)
                                .partyIsCountedAsWitnessTxt(SdoR2UiConstantFastTrack.RESTRICT_WITNESS_TEXT)
                                .build())
                        .build())
                .sdoRestrictPages(SdoR2RestrictPages.builder()
                        .isRestrictPages(NO)
                        .restrictNoOfPagesDetails(SdoR2RestrictNoOfPagesDetails.builder()
                                .witnessShouldNotMoreThanTxt(SdoR2UiConstantFastTrack.RESTRICT_NUMBER_PAGES_TEXT1)
                                .noOfPages(12)
                                .fontDetails(SdoR2UiConstantFastTrack.RESTRICT_NUMBER_PAGES_TEXT2)
                                .build())
                        .build())
                .sdoWitnessDeadline(SdoR2UiConstantFastTrack.DEADLINE)
                .sdoWitnessDeadlineDate(LocalDate.now().plusDays(70))
                .sdoWitnessDeadlineText(SdoR2UiConstantFastTrack.DEADLINE_EVIDENCE)
                .build());
    }

    private void setScheduleOfLoss(CaseData.CaseDataBuilder<?, ?> updatedData) {
        updatedData.sdoR2ScheduleOfLoss(SdoR2ScheduleOfLoss.builder()
                .sdoR2ScheduleOfLossClaimantText(SdoR2UiConstantFastTrack.SCHEDULE_OF_LOSS_CLAIMANT)
                .isClaimForPecuniaryLoss(NO)
                .sdoR2ScheduleOfLossClaimantDate(LocalDate.now().plusDays(364))
                .sdoR2ScheduleOfLossDefendantText(SdoR2UiConstantFastTrack.SCHEDULE_OF_LOSS_DEFENDANT)
                .sdoR2ScheduleOfLossDefendantDate(LocalDate.now().plusDays(378))
                .sdoR2ScheduleOfLossPecuniaryLossTxt(SdoR2UiConstantFastTrack.PECUNIARY_LOSS)
                .build());
    }

    private void setTrialDetails(CaseData.CaseDataBuilder<?, ?> updatedData, DynamicList hearingMethodList,
                                 Optional<RequestedCourt> preferredCourt, List<LocationRefData> locationRefDataList) {
        updatedData.sdoR2Trial(SdoR2Trial.builder()
                .trialOnOptions(TrialOnRadioOptions.OPEN_DATE)
                .lengthList(FastTrackHearingTimeEstimate.FIVE_HOURS)
                .methodOfHearing(hearingMethodList)
                .physicalBundleOptions(PhysicalTrialBundleOptions.PARTY)
                .sdoR2TrialFirstOpenDateAfter(SdoR2TrialFirstOpenDateAfter.builder()
                        .listFrom(LocalDate.now().plusDays(434)).build())
                .sdoR2TrialWindow(SdoR2TrialWindow.builder()
                        .listFrom(LocalDate.now().plusDays(434))
                        .dateTo(LocalDate.now().plusDays(455))
                        .build())
                .hearingCourtLocationList(DynamicList.builder()
                        .listItems(getCourtLocationForSdoR2(preferredCourt.orElse(null), locationRefDataList).getListItems())
                        .value(getCourtLocationForSdoR2(preferredCourt.orElse(null), locationRefDataList).getListItems().get(0))
                        .build())
                .altHearingCourtLocationList(getAlternativeCourtLocationsForNihl(locationRefDataList))
                .physicalBundlePartyTxt(SdoR2UiConstantFastTrack.PHYSICAL_TRIAL_BUNDLE)
                .build());
    }

    private void setImportantNotes(CaseData.CaseDataBuilder<?, ?> updatedData) {
        updatedData.sdoR2ImportantNotesTxt(SdoR2UiConstantFastTrack.IMPORTANT_NOTES);
        updatedData.sdoR2ImportantNotesDate(LocalDate.now().plusDays(7));
    }

    private void setExpertEvidence(CaseData.CaseDataBuilder<?, ?> updatedData) {
        updatedData.sdoR2ExpertEvidence(SdoR2ExpertEvidence.builder()
                .sdoClaimantPermissionToRelyTxt(SdoR2UiConstantFastTrack.CLAIMANT_PERMISSION_TO_RELY)
                .build());
    }

    private void setAddendumReport(CaseData.CaseDataBuilder<?, ?> updatedData) {
        updatedData.sdoR2AddendumReport(SdoR2AddendumReport.builder()
                .sdoAddendumReportTxt(SdoR2UiConstantFastTrack.ADDENDUM_REPORT)
                .sdoAddendumReportDate(LocalDate.now().plusDays(56))
                .build());
    }

    private void setFurtherAudiogram(CaseData.CaseDataBuilder<?, ?> updatedData) {
        updatedData.sdoR2FurtherAudiogram(SdoR2FurtherAudiogram.builder()
                .sdoClaimantShallUndergoTxt(SdoR2UiConstantFastTrack.CLAIMANT_SHALL_UNDERGO)
                .sdoServiceReportTxt(SdoR2UiConstantFastTrack.SERVICE_REPORT)
                .sdoClaimantShallUndergoDate(LocalDate.now().plusDays(42))
                .sdoServiceReportDate(LocalDate.now().plusDays(98))
                .build());
    }

    private void setQuestionsClaimantExpert(CaseData.CaseDataBuilder<?, ?> updatedData) {
        updatedData.sdoR2QuestionsClaimantExpert(SdoR2QuestionsClaimantExpert.builder()
                .sdoDefendantMayAskTxt(SdoR2UiConstantFastTrack.DEFENDANT_MAY_ASK)
                .sdoDefendantMayAskDate(LocalDate.now().plusDays(126))
                .sdoQuestionsShallBeAnsweredTxt(SdoR2UiConstantFastTrack.QUESTIONS_SHALL_BE_ANSWERED)
                .sdoQuestionsShallBeAnsweredDate(LocalDate.now().plusDays(147))
                .sdoUploadedToDigitalPortalTxt(SdoR2UiConstantFastTrack.UPLOADED_TO_DIGITAL_PORTAL)
                .sdoApplicationToRelyOnFurther(SdoR2ApplicationToRelyOnFurther.builder()
                        .doRequireApplicationToRely(NO)
                        .applicationToRelyOnFurtherDetails(SdoR2ApplicationToRelyOnFurtherDetails.builder()
                                .applicationToRelyDetailsTxt(SdoR2UiConstantFastTrack.APPLICATION_TO_RELY_DETAILS)
                                .applicationToRelyDetailsDate(LocalDate.now().plusDays(161))
                                .build())
                        .build())
                .build());
    }

    private void setPermissionToRelyOnExpert(CaseData.CaseDataBuilder<?, ?> updatedData) {
        updatedData.sdoR2PermissionToRelyOnExpert(SdoR2PermissionToRelyOnExpert.builder()
                .sdoPermissionToRelyOnExpertTxt(SdoR2UiConstantFastTrack.PERMISSION_TO_RELY_ON_EXPERT)
                .sdoPermissionToRelyOnExpertDate(LocalDate.now().plusDays(119))
                .sdoJointMeetingOfExpertsTxt(SdoR2UiConstantFastTrack.JOINT_MEETING_OF_EXPERTS)
                .sdoJointMeetingOfExpertsDate(LocalDate.now().plusDays(147))
                .sdoUploadedToDigitalPortalTxt(SdoR2UiConstantFastTrack.UPLOADED_TO_DIGITAL_PORTAL_7_DAYS)
                .build());
    }

    private void setEvidenceAcousticEngineer(CaseData.CaseDataBuilder<?, ?> updatedData) {
        updatedData.sdoR2EvidenceAcousticEngineer(SdoR2EvidenceAcousticEngineer.builder()
                .sdoEvidenceAcousticEngineerTxt(SdoR2UiConstantFastTrack.EVIDENCE_ACOUSTIC_ENGINEER)
                .sdoInstructionOfTheExpertTxt(SdoR2UiConstantFastTrack.INSTRUCTION_OF_EXPERT)
                .sdoInstructionOfTheExpertDate(LocalDate.now().plusDays(42))
                .sdoInstructionOfTheExpertTxtArea(SdoR2UiConstantFastTrack.INSTRUCTION_OF_EXPERT_TA)
                .sdoExpertReportTxt(SdoR2UiConstantFastTrack.EXPERT_REPORT)
                .sdoExpertReportDate(LocalDate.now().plusDays(280))
                .sdoExpertReportDigitalPortalTxt(SdoR2UiConstantFastTrack.EXPERT_REPORT_DIGITAL_PORTAL)
                .sdoWrittenQuestionsTxt(SdoR2UiConstantFastTrack.WRITTEN_QUESTIONS)
                .sdoWrittenQuestionsDate(LocalDate.now().plusDays(294))
                .sdoWrittenQuestionsDigitalPortalTxt(SdoR2UiConstantFastTrack.WRITTEN_QUESTIONS_DIGITAL_PORTAL)
                .sdoRepliesTxt(SdoR2UiConstantFastTrack.REPLIES)
                .sdoRepliesDate(LocalDate.now().plusDays(315))
                .sdoRepliesDigitalPortalTxt(SdoR2UiConstantFastTrack.REPLIES_DIGITAL_PORTAL)
                .sdoServiceOfOrderTxt(SdoR2UiConstantFastTrack.SERVICE_OF_ORDER)
                .build());
    }

    private void setQuestionsToEntExpert(CaseData.CaseDataBuilder<?, ?> updatedData) {
        updatedData.sdoR2QuestionsToEntExpert(SdoR2QuestionsToEntExpert.builder()
                .sdoWrittenQuestionsTxt(SdoR2UiConstantFastTrack.ENT_WRITTEN_QUESTIONS)
                .sdoWrittenQuestionsDate(LocalDate.now().plusDays(336))
                .sdoWrittenQuestionsDigPortalTxt(SdoR2UiConstantFastTrack.ENT_WRITTEN_QUESTIONS_DIG_PORTAL)
                .sdoQuestionsShallBeAnsweredTxt(SdoR2UiConstantFastTrack.ENT_QUESTIONS_SHALL_BE_ANSWERED)
                .sdoQuestionsShallBeAnsweredDate(LocalDate.now().plusDays(350))
                .sdoShallBeUploadedTxt(SdoR2UiConstantFastTrack.ENT_SHALL_BE_UPLOADED)
                .build());
    }

    private void setUploadOfDocuments(CaseData.CaseDataBuilder<?, ?> updatedData) {
        updatedData.sdoR2UploadOfDocuments(SdoR2UploadOfDocuments.builder()
                .sdoUploadOfDocumentsTxt(SdoR2UiConstantFastTrack.UPLOAD_OF_DOCUMENTS)
                .build());
    }

    private void setWelshLanguageUsage(CaseData.CaseDataBuilder<?, ?> updatedData) {
        updatedData.sdoR2NihlUseOfWelshLanguage(SdoR2WelshLanguageUsage.builder()
                .description(SdoR2UiConstantFastTrack.WELSH_LANG_DESCRIPTION)
                .build());
    }

    private DynamicList getCourtLocationForSdoR2(RequestedCourt preferredCourt,
                                                 List<LocationRefData> locations) {
        Optional<LocationRefData> matchingLocation = Optional.ofNullable(preferredCourt)
                .flatMap(requestedCourt -> locationHelper.getMatching(locations, preferredCourt));

        List<DynamicListElement> dynamicListOptions = new ArrayList<>();
        matchingLocation.ifPresent(locationRefData -> dynamicListOptions.add(dynamicElementFromCode(
                locationRefData.getEpimmsId(),
                LocationReferenceDataService.getDisplayEntry(locationRefData)
        )));
        dynamicListOptions.add(dynamicElementFromCode("OTHER_LOCATION", "Other location"));
        return DynamicList.fromDynamicListElementList(dynamicListOptions);
    }

    private DynamicList getAlternativeCourtLocationsForNihl(List<LocationRefData> locations) {

        List<DynamicListElement> dynamicListOptions = new ArrayList<>();

        locations.forEach(loc -> dynamicListOptions.add(
                dynamicElementFromCode(loc.getEpimmsId(), LocationReferenceDataService.getDisplayEntry(loc))));
        return DynamicList.fromDynamicListElementList(dynamicListOptions);
    }
}
