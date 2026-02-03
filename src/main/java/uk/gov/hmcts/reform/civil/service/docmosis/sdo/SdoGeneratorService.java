package uk.gov.hmcts.reform.civil.service.docmosis.sdo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.enums.sdo.AddOrRemoveToggle;
import uk.gov.hmcts.reform.civil.enums.sdo.DisposalHearingFinalDisposalHearingTimeEstimate;
import uk.gov.hmcts.reform.civil.enums.sdo.PhysicalTrialBundleOptions;
import uk.gov.hmcts.reform.civil.enums.sdo.TrialOnRadioOptions;
import uk.gov.hmcts.reform.civil.helpers.sdo.SdoHelper;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.docmosis.sdo.SdoDocumentFormDisposal;
import uk.gov.hmcts.reform.civil.model.docmosis.sdo.SdoDocumentFormFast;
import uk.gov.hmcts.reform.civil.model.docmosis.sdo.SdoDocumentFormFastNihl;
import uk.gov.hmcts.reform.civil.model.docmosis.sdo.SdoDocumentFormSmall;
import uk.gov.hmcts.reform.civil.model.docmosis.sdo.SdoDocumentFormSmallDrh;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingHearingTime;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentHearingLocationHelper;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.helpers.sdo.SdoHelper.getFastTrackAllocation;

@Slf4j
@Service
@RequiredArgsConstructor
public class SdoGeneratorService {

    private final DocumentGeneratorService documentGeneratorService;
    private final DocumentManagementService documentManagementService;
    private final UserService userService;
    private final DocumentHearingLocationHelper locationHelper;
    private final FeatureToggleService featureToggleService;

    public CaseDocument generate(CaseData caseData, String authorisation) {
        MappableObject templateData;
        DocmosisTemplates docmosisTemplate;

        UserDetails userDetails = userService.getUserDetails(authorisation);
        String judgeName = userDetails.getFullName();

        boolean isJudge = false;

        if (userDetails.getRoles() != null) {
            isJudge = userDetails.getRoles().stream()
                .anyMatch(s -> s != null && s.toLowerCase().contains("judge"));
        }

        if (SdoHelper.isSDOR2ScreenForDRHSmallClaim(caseData)) {
            docmosisTemplate = DocmosisTemplates.SDO_SMALL_DRH;
            templateData = getTemplateDataSmallDrh(caseData, judgeName, isJudge, authorisation);
        } else if (SdoHelper.isSmallClaimsTrack(caseData)) {
            docmosisTemplate = DocmosisTemplates.SDO_SMALL_R2;
            templateData = getTemplateDataSmall(caseData, judgeName, isJudge, authorisation);
        } else if (SdoHelper.isNihlFastTrack(caseData)) {
            docmosisTemplate = DocmosisTemplates.SDO_FAST_TRACK_NIHL;
            templateData = getTemplateDataFastNihl(caseData, judgeName, isJudge, authorisation);
        } else if (SdoHelper.isFastTrack(caseData)) {
            docmosisTemplate = DocmosisTemplates.SDO_FAST_FAST_TRACK_INT_R2;
            templateData = getTemplateDataFast(caseData, judgeName, isJudge, authorisation);
        } else {
            docmosisTemplate =  DocmosisTemplates.SDO_R2_DISPOSAL;
            templateData = getTemplateDataDisposal(caseData, judgeName, isJudge, authorisation);
        }
        log.info("SDO docmosisTemplate: {} for caseId {} legacyCaseReference{}", docmosisTemplate.getTemplate(), caseData.getCcdCaseReference(), caseData.getLegacyCaseReference());
        DocmosisDocument docmosisDocument = documentGeneratorService.generateDocmosisDocument(
            templateData,
            docmosisTemplate
        );

        return documentManagementService.uploadDocument(
            authorisation,
            new PDF(
                getFileName(judgeName),
                docmosisDocument.getBytes(),
                DocumentType.SDO_ORDER
            )
        );
    }

    private String getFileName(String judgeName) {
        StringBuilder updatedFileName = new StringBuilder();
        updatedFileName.append(LocalDate.now()).append("_").append(judgeName).append(".pdf");

        return updatedFileName.toString();
    }

    static final String APPLICANT_2 = "applicant2";
    static final String RESPONDENT_2 = "respondent2";

    private SdoDocumentFormDisposal getTemplateDataDisposal(CaseData caseData, String judgeName, boolean isJudge, String authorisation) {
        var sdoDocumentBuilder = new SdoDocumentFormDisposal()
            .setWrittenByJudge(isJudge)
            .setCurrentDate(LocalDate.now())
            .setJudgeName(judgeName)
            .setCaseNumber(caseData.getLegacyCaseReference())
            .setApplicant1(caseData.getApplicant1())
            .setHasApplicant2(
                SdoHelper.hasSharedVariable(caseData, APPLICANT_2)
            )
            .setApplicant2(caseData.getApplicant2())
            .setRespondent1(caseData.getRespondent1())
            .setHasRespondent2(
                SdoHelper.hasSharedVariable(caseData, RESPONDENT_2)
            )
            .setRespondent2(caseData.getRespondent2())
            .setDrawDirectionsOrderRequired(caseData.getDrawDirectionsOrderRequired())
            .setDrawDirectionsOrder(caseData.getDrawDirectionsOrder())
            .setDisposalHearingJudgesRecital(caseData.getDisposalHearingJudgesRecital())
            .setDisposalHearingDisclosureOfDocuments(caseData.getDisposalHearingDisclosureOfDocuments())
            .setDisposalHearingWitnessOfFact(caseData.getDisposalHearingWitnessOfFact())
            .setDisposalHearingMedicalEvidence(caseData.getDisposalHearingMedicalEvidence())
            .setDisposalHearingQuestionsToExperts(caseData.getDisposalHearingQuestionsToExperts())
            .setDisposalHearingSchedulesOfLoss(caseData.getDisposalHearingSchedulesOfLoss())
            .setDisposalHearingFinalDisposalHearing(caseData.getDisposalHearingFinalDisposalHearing())
            .setDisposalHearingFinalDisposalHearingTime(
                SdoHelper.getDisposalHearingFinalDisposalHearingTimeLabel(caseData)
            )
            .setDisposalHearingMethod(caseData.getDisposalHearingMethod())
            .setDisposalHearingMethodInPerson(caseData.getDisposalHearingMethodInPerson())
            .setDisposalHearingMethodTelephoneHearing(
                SdoHelper.getDisposalHearingMethodTelephoneHearingLabel(caseData)
            )
            .setDisposalHearingMethodVideoConferenceHearing(
                SdoHelper.getDisposalHearingMethodVideoConferenceHearingLabel(caseData)
            )
            .setDisposalHearingBundle(caseData.getDisposalHearingBundle())
            .setDisposalHearingBundleTypeText(
                SdoHelper.getDisposalHearingBundleTypeText(caseData)
            )
            .setHasNewDirections(
                SdoHelper.hasDisposalVariable(caseData, "disposalHearingAddNewDirections")
            )
            .setDisposalHearingAddNewDirections(caseData.getDisposalHearingAddNewDirections())
            .setDisposalHearingNotes(caseData.getDisposalHearingNotes())
            .setDisposalHearingDisclosureOfDocumentsToggle(
                SdoHelper.hasDisposalVariable(caseData, "disposalHearingDisclosureOfDocumentsToggle")
            )
            .setDisposalHearingWitnessOfFactToggle(
                SdoHelper.hasDisposalVariable(caseData, "disposalHearingWitnessOfFactToggle")
            )
            .setDisposalHearingMedicalEvidenceToggle(
                SdoHelper.hasDisposalVariable(caseData, "disposalHearingMedicalEvidenceToggle")
            )
            .setDisposalHearingQuestionsToExpertsToggle(
                SdoHelper.hasDisposalVariable(caseData, "disposalHearingQuestionsToExpertsToggle")
            )
            .setDisposalHearingSchedulesOfLossToggle(
                SdoHelper.hasDisposalVariable(caseData, "disposalHearingSchedulesOfLossToggle")
            )
            .setDisposalHearingFinalDisposalHearingToggle(
                SdoHelper.hasDisposalVariable(caseData, "disposalHearingFinalDisposalHearingToggle")
            )
            .setDisposalHearingMethodToggle(
                // SNI-5142
                true
            )
            .setDisposalHearingBundleToggle(
                SdoHelper.hasDisposalVariable(caseData, "disposalHearingBundleToggle")
            )
            .setDisposalHearingClaimSettlingToggle(
                SdoHelper.hasDisposalVariable(caseData, "disposalHearingClaimSettlingToggle")
            )
            .setDisposalHearingCostsToggle(
                SdoHelper.hasDisposalVariable(caseData, "disposalHearingCostsToggle")
            );

        sdoDocumentBuilder
            .setDisposalOrderWithoutHearing(caseData.getDisposalOrderWithoutHearing())
            .setDisposalHearingTime(caseData.getDisposalHearingHearingTime());
        Optional.ofNullable(caseData.getDisposalHearingHearingTime())
            .map(DisposalHearingHearingTime::getTime)
            .map(DisposalHearingFinalDisposalHearingTimeEstimate::getLabel)
            .ifPresent(sdoDocumentBuilder::setDisposalHearingTimeEstimate);

        Optional.ofNullable(caseData.getDisposalHearingHearingTime())
            .filter(hearingTime -> DisposalHearingFinalDisposalHearingTimeEstimate.OTHER.equals(hearingTime.getTime()))
                .ifPresent(hearingTime -> sdoDocumentBuilder
                    .setDisposalHearingTimeEstimate(
                        String.format("%s hours %s minutes",
                                      hearingTime.getOtherHours(),
                                      hearingTime.getOtherMinutes())
                    ));

        sdoDocumentBuilder.setHearingLocation(
            locationHelper.getHearingLocation(
                Optional.ofNullable(caseData.getDisposalHearingMethodInPerson())
                    .map(DynamicList::getValue)
                    .map(DynamicListElement::getLabel)
                    .orElse(null),
                caseData,
                authorisation
            ))
            .setCaseManagementLocation(locationHelper.getHearingLocation(null, caseData, authorisation));

        sdoDocumentBuilder
            .setHasDisposalWelshToggle(caseData.getSdoR2DisposalHearingUseOfWelshToggle() != null)
            .setWelshLanguageDescription(caseData.getSdoR2DisposalHearingUseOfWelshLanguage() != null
                                          ? caseData.getSdoR2DisposalHearingUseOfWelshLanguage().getDescription() : null);

        SdoDocumentFormDisposal sdoDocumentFormDisposal = sdoDocumentBuilder;
        log.info("Disposal SDO template data: {} for caseId {}", sdoDocumentFormDisposal, caseData.getCcdCaseReference());
        return sdoDocumentFormDisposal;
    }

    private SdoDocumentFormFast getTemplateDataFast(CaseData caseData, String judgeName, boolean isJudge, String authorisation) {
        boolean hasPpi = SdoHelper.hasFastAdditionalDirections(caseData, "fastClaimPPI");
        var sdoDocumentFormFast = new SdoDocumentFormFast()
            .setWrittenByJudge(isJudge)
            .setCurrentDate(LocalDate.now())
            .setJudgeName(judgeName)
            .setCaseNumber(caseData.getLegacyCaseReference())
            .setApplicant1(caseData.getApplicant1())
            .setHasApplicant2(
                SdoHelper.hasSharedVariable(caseData, APPLICANT_2)
            )
            .setApplicant2(caseData.getApplicant2())
            .setRespondent1(caseData.getRespondent1())
            .setHasRespondent2(
                SdoHelper.hasSharedVariable(caseData, RESPONDENT_2)
            )
            .setRespondent2(caseData.getRespondent2())
            .setDrawDirectionsOrderRequired(caseData.getDrawDirectionsOrderRequired())
            .setDrawDirectionsOrder(caseData.getDrawDirectionsOrder())
            .setClaimsTrack(caseData.getClaimsTrack())
            .setFastClaims(caseData.getFastClaims())
            .setHasBuildingDispute(
                SdoHelper.hasFastAdditionalDirections(caseData, "fastClaimBuildingDispute")
            )
            .setHasClinicalNegligence(
                SdoHelper.hasFastAdditionalDirections(caseData, "fastClaimClinicalNegligence")
            )
            .setHasSdoR2CreditHire(
                (SdoHelper.hasFastAdditionalDirections(caseData, "fastClaimCreditHire"))
            )
            .setHasSdoR2CreditHireDetails(
                (nonNull(caseData.getSdoR2FastTrackCreditHire()) && (caseData.getSdoR2FastTrackCreditHire().getDetailsShowToggle() != null)
                    && caseData.getSdoR2FastTrackCreditHire().getDetailsShowToggle()
                    .equals(List.of(AddOrRemoveToggle.ADD)))
            )
            .setHasEmployersLiability(
                SdoHelper.hasFastAdditionalDirections(caseData, "fastClaimEmployersLiability")
            )
            .setHasHousingDisrepair(
                SdoHelper.hasFastAdditionalDirections(caseData, "fastClaimHousingDisrepair")
            )
            .setHasPersonalInjury(
                SdoHelper.hasFastAdditionalDirections(caseData, "fastClaimPersonalInjury")
            )
            .setHasRoadTrafficAccident(
                SdoHelper.hasFastAdditionalDirections(caseData, "fastClaimRoadTrafficAccident")
            )
            .setHasPaymentProtectionInsurance(hasPpi)
            .setFastTrackJudgesRecital(caseData.getFastTrackJudgesRecital())
            .setFastTrackDisclosureOfDocuments(caseData.getFastTrackDisclosureOfDocuments())
            .setFastTrackSchedulesOfLoss(caseData.getFastTrackSchedulesOfLoss())
            .setFastTrackTrial(caseData.getFastTrackTrial())
            .setFastTrackMethod(caseData.getFastTrackMethod())
            .setFastTrackMethodInPerson(caseData.getFastTrackMethodInPerson())
            .setFastTrackMethodTelephoneHearing(
                SdoHelper.getFastTrackMethodTelephoneHearingLabel(caseData)
            )
            .setFastTrackMethodVideoConferenceHearing(
                SdoHelper.getFastTrackMethodVideoConferenceHearingLabel(caseData)
            )
            .setFastTrackBuildingDispute(caseData.getFastTrackBuildingDispute())
            .setFastTrackClinicalNegligence(caseData.getFastTrackClinicalNegligence())
            .setFastTrackHousingDisrepair(caseData.getFastTrackHousingDisrepair())
            .setFastTrackPersonalInjury(caseData.getFastTrackPersonalInjury())
            .setFastTrackRoadTrafficAccident(caseData.getFastTrackRoadTrafficAccident())
            .setFastTrackPPI(hasPpi ? caseData.getFastTrackPPI() : null)
            .setHasNewDirections(
                SdoHelper.hasFastTrackVariable(caseData, "fastTrackAddNewDirections")
            )
            .setFastTrackAddNewDirections(caseData.getFastTrackAddNewDirections())
            .setFastTrackNotes(caseData.getFastTrackNotes())
            .setFastTrackTrialDateToToggle(
                SdoHelper.hasFastTrackVariable(caseData, "fastTrackTrialDateToToggle"))
            .setFastTrackAltDisputeResolutionToggle(
                SdoHelper.hasFastTrackVariable(caseData, "fastTrackAltDisputeResolutionToggle")
            )
            .setFastTrackVariationOfDirectionsToggle(
                SdoHelper.hasFastTrackVariable(caseData, "fastTrackVariationOfDirectionsToggle")
            )
            .setFastTrackSettlementToggle(
                SdoHelper.hasFastTrackVariable(caseData, "fastTrackSettlementToggle")
            )
            .setFastTrackDisclosureOfDocumentsToggle(
                SdoHelper.hasFastTrackVariable(caseData, "fastTrackDisclosureOfDocumentsToggle")
            )
            .setFastTrackWitnessOfFactToggle(
                SdoHelper.hasFastTrackVariable(caseData, "fastTrackWitnessOfFactToggle")
            )
            .setFastTrackSchedulesOfLossToggle(
                SdoHelper.hasFastTrackVariable(caseData, "fastTrackSchedulesOfLossToggle")
            )
            .setFastTrackCostsToggle(
                SdoHelper.hasFastTrackVariable(caseData, "fastTrackCostsToggle")
            )
            .setFastTrackTrialToggle(
                SdoHelper.hasFastTrackVariable(caseData, "fastTrackTrialToggle")
            )
            // SNI-5142
            .setFastTrackMethodToggle(true)
            .setFastTrackAllocation(getFastTrackAllocation(caseData))
            .setShowBundleInfo(SdoHelper.hasFastTrackVariable(caseData, "fastTrackTrialBundleToggle"));

        sdoDocumentFormFast
            .setFastTrackOrderWithoutJudgement(caseData.getFastTrackOrderWithoutJudgement())
            .setFastTrackHearingTime(caseData.getFastTrackHearingTime())
            .setFastTrackHearingTimeEstimate(SdoHelper.getFastClaimsHearingTimeLabel(caseData));

        sdoDocumentFormFast
            .setHearingLocation(locationHelper.getHearingLocation(
                Optional.ofNullable(caseData.getFastTrackMethodInPerson())
                    .map(DynamicList::getValue)
                    .map(DynamicListElement::getLabel)
                    .orElse(null),
                caseData,
                authorisation
            ))
            .setCaseManagementLocation(locationHelper.getHearingLocation(null, caseData, authorisation));

        sdoDocumentFormFast.setFastTrackWelshLanguageToggle(
                SdoHelper.hasFastTrackVariable(caseData, "sdoR2FastTrackUseOfWelshToggle"))
            .setWelshLanguageDescription(caseData.getSdoR2FastTrackUseOfWelshLanguage() != null
                                          ? caseData.getSdoR2FastTrackUseOfWelshLanguage().getDescription() : null);
        sdoDocumentFormFast.setShowPenalNotice(SdoHelper.hasFastTrackVariable(caseData, "fastTrackPenalNoticeToggle"))
            .setPenalNoticeText(caseData.getFastTrackPenalNotice());
        sdoDocumentFormFast.setSdoR2WitnessesOfFact(caseData.getSdoR2FastTrackWitnessOfFact())
            .setSdoR2FastTrackCreditHire(caseData.getSdoR2FastTrackCreditHire());

        return sdoDocumentFormFast;
    }

    private SdoDocumentFormFastNihl getTemplateDataFastNihl(CaseData caseData, String judgeName, boolean isJudge, String authorisation) {
        SdoDocumentFormFastNihl sdoNihlDocumentFormBuilder = new SdoDocumentFormFastNihl()
            .setWrittenByJudge(isJudge)
            .setCurrentDate(LocalDate.now())
            .setJudgeName(judgeName)
            .setCaseNumber(caseData.getLegacyCaseReference())
            .setApplicant1(caseData.getApplicant1())
            .setHasApplicant2(
                SdoHelper.hasSharedVariable(caseData, APPLICANT_2)
            )
            .setApplicant2(caseData.getApplicant2())
            .setRespondent1(caseData.getRespondent1())
            .setHasRespondent2(
                SdoHelper.hasSharedVariable(caseData, RESPONDENT_2)
            )
            .setRespondent2(caseData.getRespondent2())
            .setDrawDirectionsOrderRequired(caseData.getDrawDirectionsOrderRequired())
            .setDrawDirectionsOrder(caseData.getDrawDirectionsOrder())
            .setClaimsTrack(caseData.getClaimsTrack())
            .setFastClaims(caseData.getFastClaims())
            .setSdoFastTrackJudgesRecital(caseData.getSdoFastTrackJudgesRecital())
            .setSdoR2DisclosureOfDocuments(caseData.getSdoR2DisclosureOfDocuments())
            .setSdoR2WitnessesOfFact(caseData.getSdoR2WitnessesOfFact())
            .setSdoR2ExpertEvidence(caseData.getSdoR2ExpertEvidence())
            .setSdoR2AddendumReport(caseData.getSdoR2AddendumReport())
            .setSdoR2FurtherAudiogram(caseData.getSdoR2FurtherAudiogram())
            .setSdoR2QuestionsClaimantExpert(caseData.getSdoR2QuestionsClaimantExpert())
            .setSdoR2PermissionToRelyOnExpert(caseData.getSdoR2PermissionToRelyOnExpert())
            .setSdoR2EvidenceAcousticEngineer(caseData.getSdoR2EvidenceAcousticEngineer())
            .setSdoR2QuestionsToEntExpert(caseData.getSdoR2QuestionsToEntExpert())
            .setSdoR2ScheduleOfLoss(caseData.getSdoR2ScheduleOfLoss())
            .setSdoR2UploadOfDocuments(caseData.getSdoR2UploadOfDocuments())
            .setSdoR2AddNewDirection(caseData.getSdoR2AddNewDirection())
            .setSdoR2Trial(caseData.getSdoR2Trial())
            .setSdoR2ImportantNotesTxt(caseData.getSdoR2ImportantNotesTxt())
            .setSdoR2ImportantNotesDate(caseData.getSdoR2ImportantNotesDate())
            .setHasAltDisputeResolution(caseData.getSdoAltDisputeResolution().getIncludeInOrderToggle() != null)
            .setHasVariationOfDirections(caseData.getSdoVariationOfDirections().getIncludeInOrderToggle() != null)
            .setHasSettlement(caseData.getSdoR2Settlement()
                               .getIncludeInOrderToggle() != null)
            .setHasDisclosureOfDocuments(caseData.getSdoR2DisclosureOfDocumentsToggle() != null)
            .setHasWitnessOfFact(caseData.getSdoR2SeparatorWitnessesOfFactToggle() != null)
            .setHasRestrictWitness(SdoHelper.isRestrictWitnessNihl(caseData))
            .setHasRestrictPages(SdoHelper.isRestrictPagesNihl(caseData))
            .setHasExpertEvidence(caseData.getSdoR2SeparatorExpertEvidenceToggle() != null)
            .setHasAddendumReport(caseData.getSdoR2SeparatorAddendumReportToggle() != null)
            .setHasFurtherAudiogram(caseData.getSdoR2SeparatorFurtherAudiogramToggle() != null)
            .setHasQuestionsOfClaimantExpert(caseData.getSdoR2SeparatorQuestionsClaimantExpertToggle() != null)
            .setIsApplicationToRelyOnFurther(SdoHelper.isApplicationToRelyOnFurtherNihl(caseData))
            .setHasPermissionFromENT(caseData.getSdoR2SeparatorPermissionToRelyOnExpertToggle() != null)
            .setHasEvidenceFromAcousticEngineer(caseData.getSdoR2SeparatorEvidenceAcousticEngineerToggle() != null)
            .setHasQuestionsToENTAfterReport(caseData.getSdoR2SeparatorQuestionsToEntExpertToggle() != null)
            .setHasScheduleOfLoss(caseData.getSdoR2ScheduleOfLossToggle() != null)
            .setHasClaimForPecuniaryLoss(SdoHelper.isClaimForPecuniaryLossNihl(caseData))
            .setHasUploadDocuments(caseData.getSdoR2SeparatorUploadOfDocumentsToggle() != null)
            .setHasSdoTrial(caseData.getSdoR2TrialToggle() != null)
            .setHasNewDirections(caseData.getSdoR2AddNewDirection() != null)
            .setSdoR2AddNewDirection(caseData.getSdoR2AddNewDirection())
            .setHasSdoR2TrialWindow(caseData.getSdoR2TrialToggle() != null
                                     && TrialOnRadioOptions.TRIAL_WINDOW.equals(
                caseData.getSdoR2Trial().getTrialOnOptions()))
            .setSdoTrialHearingTimeAllocated(SdoHelper.getSdoTrialHearingTimeAllocated(caseData))
            .setSdoTrialMethodOfHearing(SdoHelper.getSdoTrialMethodOfHearing(caseData))
            .setHasSdoR2TrialPhysicalBundleParty(caseData.getSdoR2Trial() != null
                                                  && PhysicalTrialBundleOptions.PARTY.equals(
                caseData.getSdoR2Trial().getPhysicalBundleOptions()))
            .setPhysicalBundlePartyTxt(SdoHelper.getPhysicalTrialTextNihl(caseData))
            .setHasNihlWelshLangToggle(caseData.getSdoR2NihlUseOfWelshIncludeInOrderToggle() != null)
            .setWelshLanguageDescription(caseData.getSdoR2NihlUseOfWelshLanguage() != null
                                          ? caseData.getSdoR2NihlUseOfWelshLanguage().getDescription() : null);

        if (caseData.getSdoR2Trial() != null) {
            sdoNihlDocumentFormBuilder
                .setHearingLocation(locationHelper.getHearingLocation(
                    Optional.ofNullable(SdoHelper.getHearingLocationNihl(caseData))
                        .map(DynamicList::getValue)
                        .map(DynamicListElement::getLabel)
                        .orElse(null),
                    caseData,
                    authorisation
                ));
        }
        sdoNihlDocumentFormBuilder.setCaseManagementLocation(locationHelper.getHearingLocation(null, caseData, authorisation));

        return sdoNihlDocumentFormBuilder;
    }

    private SdoDocumentFormSmall getTemplateDataSmall(CaseData caseData, String judgeName, boolean isJudge, String authorisation) {
        boolean carmEnabled = featureToggleService.isCarmEnabledForCase(caseData);
        boolean hasPpi = SdoHelper.hasSmallAdditionalDirections(caseData, "smallClaimPPI");
        SdoDocumentFormSmall sdoDocumentFormBuilder = new SdoDocumentFormSmall()
            .setWrittenByJudge(isJudge)
            .setCurrentDate(LocalDate.now())
            .setJudgeName(judgeName)
            .setCaseNumber(caseData.getLegacyCaseReference())
            .setApplicant1(caseData.getApplicant1())
            .setHasApplicant2(
                SdoHelper.hasSharedVariable(caseData, APPLICANT_2)
            )
            .setApplicant2(caseData.getApplicant2())
            .setRespondent1(caseData.getRespondent1())
            .setHasRespondent2(
                SdoHelper.hasSharedVariable(caseData, RESPONDENT_2)
            )
            .setRespondent2(caseData.getRespondent2())
            .setDrawDirectionsOrderRequired(caseData.getDrawDirectionsOrderRequired())
            .setDrawDirectionsOrder(caseData.getDrawDirectionsOrder())
            .setClaimsTrack(caseData.getClaimsTrack())
            .setSmallClaims(caseData.getSmallClaims())
            .setHasCreditHire(
                SdoHelper.hasSmallAdditionalDirections(caseData, "smallClaimCreditHire")
            )
            .setHasRoadTrafficAccident(
                SdoHelper.hasSmallAdditionalDirections(caseData, "smallClaimRoadTrafficAccident")
            )
            .setHasPaymentProtectionInsurance(hasPpi)
            .setSmallClaimsJudgesRecital(caseData.getSmallClaimsJudgesRecital())
            .setSmallClaimsHearing(caseData.getSmallClaimsHearing())
            .setSmallClaimsHearingTime(
                SdoHelper.getSmallClaimsHearingTimeLabel(caseData)
            )
            // CIV-5514: smallClaimsMethodInPerson, smallClaimsMethodTelephoneHearing and
            // smallClaimsMethodVideoConferenceHearing can be removed after HNL is live
            .setSmallClaimsMethod(caseData.getSmallClaimsMethod())
            .setSmallClaimsMethodInPerson(caseData.getSmallClaimsMethodInPerson())
            .setSmallClaimsMethodTelephoneHearing(
                SdoHelper.getSmallClaimsMethodTelephoneHearingLabel(caseData)
            )
            .setSmallClaimsMethodVideoConferenceHearing(
                SdoHelper.getSmallClaimsMethodVideoConferenceHearingLabel(caseData)
            )
            .setSmallClaimsDocuments(caseData.getSmallClaimsDocuments())
            .setSmallClaimsCreditHire(caseData.getSmallClaimsCreditHire())
            .setSmallClaimsRoadTrafficAccident(caseData.getSmallClaimsRoadTrafficAccident())
            .setSmallClaimsPPI(hasPpi ? caseData.getSmallClaimsPPI() : null)
            .setHasNewDirections(
                SdoHelper.hasSmallClaimsVariable(caseData, "smallClaimsAddNewDirections")
            )
            .setSmallClaimsAddNewDirections(caseData.getSmallClaimsAddNewDirections())
            .setSmallClaimsNotes(caseData.getSmallClaimsNotes())
            .setSmallClaimsHearingToggle(
                SdoHelper.hasSmallClaimsVariable(caseData, "smallClaimsHearingToggle")
            )
            // SNI-5142
            .setSmallClaimsMethodToggle(true)
            .setSmallClaimMediationSectionInput(SdoHelper.getSmallClaimsMediationText(caseData))
            .setSmallClaimsDocumentsToggle(
                SdoHelper.hasSmallClaimsVariable(caseData, "smallClaimsDocumentsToggle")
            )
            .setSmallClaimsWitnessStatementToggle(
                SdoHelper.hasSmallClaimsVariable(caseData, "smallClaimsWitnessStatementToggle")
            )
            .setSmallClaimsNumberOfWitnessesToggle(
                SdoHelper.hasSmallClaimsVariable(caseData, "smallClaimsNumberOfWitnessesToggle")
            )
            .setSmallClaimsMediationSectionToggle(
                SdoHelper.showCarmMediationSection(caseData, carmEnabled)
            )
            .setCaseAccessCategory(caseData.getCaseAccessCategory().toString())
            .setCarmEnabled(carmEnabled);

        sdoDocumentFormBuilder.setSmallClaimsFlightDelayToggle(SdoHelper.hasSmallClaimsVariable(caseData, "smallClaimsFlightDelayToggle"))
            .setSmallClaimsFlightDelay(caseData.getSmallClaimsFlightDelay())
            .setSmallClaimsWelshLanguageToggle(SdoHelper.hasSmallClaimsVariable(caseData, "sdoR2SmallClaimsUseOfWelshToggle"))
            .setWelshLanguageDescription(caseData.getSdoR2SmallClaimsUseOfWelshLanguage() != null ? caseData.getSdoR2SmallClaimsUseOfWelshLanguage().getDescription() : null)
            .setSdoR2SmallClaimsWitnessStatements(caseData.getSdoR2SmallClaimsWitnessStatementOther())
            .setShowPenalNotice(SdoHelper.hasSmallClaimsVariable(caseData, "smallClaimsPenalNoticeToggle"))
            .setPenalNoticeText(caseData.getSmallClaimsPenalNotice());

        sdoDocumentFormBuilder.setHearingLocation(
                locationHelper.getHearingLocation(
                    Optional.ofNullable(caseData.getSmallClaimsMethodInPerson())
                        .map(DynamicList::getValue)
                        .map(DynamicListElement::getLabel)
                        .orElse(null),
                    caseData,
                    authorisation
                ))
            .setCaseManagementLocation(
                locationHelper.getHearingLocation(null, caseData, authorisation));

        return sdoDocumentFormBuilder;
    }

    private SdoDocumentFormSmallDrh getTemplateDataSmallDrh(CaseData caseData, String judgeName, boolean isJudge, String authorisation) { //TODO Change to suit SDO R2 DRH
        boolean carmEnabled = featureToggleService.isCarmEnabledForCase(caseData);
        SdoDocumentFormSmallDrh sdoDocumentFormBuilderDrh = new SdoDocumentFormSmallDrh()
            .setWrittenByJudge(isJudge)
            .setCurrentDate(LocalDate.now())
            .setJudgeName(judgeName)
            .setCaseNumber(caseData.getLegacyCaseReference())
            .setApplicant1(caseData.getApplicant1())
            .setHasApplicant2(
                SdoHelper.hasSharedVariable(caseData, APPLICANT_2)
            )
            .setApplicant2(caseData.getApplicant2())
            .setRespondent1(caseData.getRespondent1())
            .setHasRespondent2(
                SdoHelper.hasSharedVariable(caseData, RESPONDENT_2)
            )
            .setRespondent2(caseData.getRespondent2())
            .setHasPaymentProtectionInsurance(caseData.getSdoR2SmallClaimsPPIToggle() != null)
            .setHasHearingToggle(caseData.getSdoR2SmallClaimsHearingToggle() != null)
            .setHasWitnessStatement(caseData.getSdoR2SmallClaimsWitnessStatements() != null)
            .setHasUploadDocToggle(caseData.getSdoR2SmallClaimsUploadDocToggle() != null)
            .setHasDRHWelshLangToggle(caseData.getSdoR2DrhUseOfWelshIncludeInOrderToggle() != null)
            .setHasSdoR2HearingTrialWindow(SdoHelper.hasSdoR2HearingTrialWindow(caseData))
            .setHasNewDirections(caseData.getSdoR2SmallClaimsAddNewDirection() != null)
            .setSdoR2SmallClaimsPhysicalTrialBundleTxt(SdoHelper.getSdoR2SmallClaimsPhysicalTrialBundleTxt(caseData))
            .setSdoR2SmallClaimsJudgesRecital(caseData.getSdoR2SmallClaimsJudgesRecital())
            .setSdoR2SmallClaimsHearing(caseData.getSdoR2SmallClaimsHearing())
            .setSdoR2SmallClaimsWitnessStatements(caseData.getSdoR2SmallClaimsWitnessStatements())
            .setSdoR2SmallClaimsPPI(caseData.getSdoR2SmallClaimsPPI())
            .setSdoR2SmallClaimsUploadDoc(caseData.getSdoR2SmallClaimsUploadDoc())
            .setSmallClaimsMethod(SdoHelper.getSdoR2SmallClaimsHearingMethod(caseData))
            .setHearingTime(SdoHelper.getSdoR2HearingTime(caseData))
            .setSdoR2SmallClaimsImpNotes(caseData.getSdoR2SmallClaimsImpNotes())
            .setSdoR2SmallClaimsAddNewDirection(caseData.getSdoR2SmallClaimsAddNewDirection())
            .setWelshLanguageDescription(caseData.getSdoR2DrhUseOfWelshLanguage() != null
                                          ? caseData.getSdoR2DrhUseOfWelshLanguage().getDescription() : null)
            .setCarmEnabled(carmEnabled)
            .setSdoR2SmallClaimMediationSectionInput(SdoHelper.getSmallClaimsMediationTextDRH(caseData))
            .setCaseManagementLocation(
                locationHelper.getHearingLocation(null, caseData, authorisation))
            .setSdoR2SmallClaimsMediationSectionToggle(
                SdoHelper.showCarmMediationSectionDRH(caseData, carmEnabled)
            );

        if (caseData.getSdoR2SmallClaimsHearing() != null) {
            sdoDocumentFormBuilderDrh.setHearingLocation(
                locationHelper.getHearingLocation(
                    Optional.ofNullable(SdoHelper.getHearingLocationDrh(caseData))
                        .map(DynamicList::getValue)
                        .map(DynamicListElement::getLabel)
                        .orElse(null),
                    caseData,
                    authorisation
                ));

        }

        return sdoDocumentFormBuilderDrh;
    }

}
