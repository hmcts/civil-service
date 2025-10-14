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

@Slf4j
@Service
@RequiredArgsConstructor
public class SdoGeneratorService {

    private final DocumentGeneratorService documentGeneratorService;
    private final DocumentManagementService documentManagementService;
    private final UserService userService;
    private final DocumentHearingLocationHelper locationHelper;
    private final FeatureToggleService featureToggleService;
    private final SdoHelper sdoHelper;

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

        if (sdoHelper.isSDOR2ScreenForDRHSmallClaim(caseData)) {
            docmosisTemplate = DocmosisTemplates.SDO_SMALL_DRH;
            templateData = getTemplateDataSmallDrh(caseData, judgeName, isJudge, authorisation);
        } else if (sdoHelper.isSmallClaimsTrack(caseData)) {
            docmosisTemplate = DocmosisTemplates.SDO_SMALL_R2;
            templateData = getTemplateDataSmall(caseData, judgeName, isJudge, authorisation);
        } else if (sdoHelper.isNihlFastTrack(caseData)) {
            docmosisTemplate = DocmosisTemplates.SDO_FAST_TRACK_NIHL;
            templateData = getTemplateDataFastNihl(caseData, judgeName, isJudge, authorisation);
        } else if (sdoHelper.isFastTrack(caseData)) {
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
        var sdoDocumentBuilder = SdoDocumentFormDisposal.builder()
            .writtenByJudge(isJudge)
            .currentDate(LocalDate.now())
            .judgeName(judgeName)
            .caseNumber(caseData.getLegacyCaseReference())
            .applicant1(caseData.getApplicant1())
            .hasApplicant2(
                sdoHelper.hasSharedVariable(caseData, APPLICANT_2)
            )
            .applicant2(caseData.getApplicant2())
            .respondent1(caseData.getRespondent1())
            .hasRespondent2(
                sdoHelper.hasSharedVariable(caseData, RESPONDENT_2)
            )
            .respondent2(caseData.getRespondent2())
            .drawDirectionsOrderRequired(caseData.getDrawDirectionsOrderRequired())
            .drawDirectionsOrder(caseData.getDrawDirectionsOrder())
            .disposalHearingJudgesRecital(caseData.getDisposalHearingJudgesRecital())
            .disposalHearingDisclosureOfDocuments(caseData.getDisposalHearingDisclosureOfDocuments())
            .disposalHearingWitnessOfFact(caseData.getDisposalHearingWitnessOfFact())
            .disposalHearingMedicalEvidence(caseData.getDisposalHearingMedicalEvidence())
            .disposalHearingQuestionsToExperts(caseData.getDisposalHearingQuestionsToExperts())
            .disposalHearingSchedulesOfLoss(caseData.getDisposalHearingSchedulesOfLoss())
            .disposalHearingFinalDisposalHearing(caseData.getDisposalHearingFinalDisposalHearing())
            .disposalHearingFinalDisposalHearingTime(
                sdoHelper.getDisposalHearingFinalDisposalHearingTimeLabel(caseData)
            )
            .disposalHearingMethod(caseData.getDisposalHearingMethod())
            .disposalHearingMethodInPerson(caseData.getDisposalHearingMethodInPerson())
            .disposalHearingMethodTelephoneHearing(
                sdoHelper.getDisposalHearingMethodTelephoneHearingLabel(caseData)
            )
            .disposalHearingMethodVideoConferenceHearing(
                sdoHelper.getDisposalHearingMethodVideoConferenceHearingLabel(caseData)
            )
            .disposalHearingBundle(caseData.getDisposalHearingBundle())
            .disposalHearingBundleTypeText(
                sdoHelper.getDisposalHearingBundleTypeText(caseData)
            )
            .hasNewDirections(
                sdoHelper.hasDisposalVariable(caseData, "disposalHearingAddNewDirections")
            )
            .disposalHearingAddNewDirections(caseData.getDisposalHearingAddNewDirections())
            .disposalHearingNotes(caseData.getDisposalHearingNotes())
            .disposalHearingDisclosureOfDocumentsToggle(
                sdoHelper.hasDisposalVariable(caseData, "disposalHearingDisclosureOfDocumentsToggle")
            )
            .disposalHearingWitnessOfFactToggle(
                sdoHelper.hasDisposalVariable(caseData, "disposalHearingWitnessOfFactToggle")
            )
            .disposalHearingMedicalEvidenceToggle(
                sdoHelper.hasDisposalVariable(caseData, "disposalHearingMedicalEvidenceToggle")
            )
            .disposalHearingQuestionsToExpertsToggle(
                sdoHelper.hasDisposalVariable(caseData, "disposalHearingQuestionsToExpertsToggle")
            )
            .disposalHearingSchedulesOfLossToggle(
                sdoHelper.hasDisposalVariable(caseData, "disposalHearingSchedulesOfLossToggle")
            )
            .disposalHearingFinalDisposalHearingToggle(
                sdoHelper.hasDisposalVariable(caseData, "disposalHearingFinalDisposalHearingToggle")
            )
            .disposalHearingMethodToggle(
                // SNI-5142
                true
            )
            .disposalHearingBundleToggle(
                sdoHelper.hasDisposalVariable(caseData, "disposalHearingBundleToggle")
            )
            .disposalHearingClaimSettlingToggle(
                sdoHelper.hasDisposalVariable(caseData, "disposalHearingClaimSettlingToggle")
            )
            .disposalHearingCostsToggle(
                sdoHelper.hasDisposalVariable(caseData, "disposalHearingCostsToggle")
            );

        sdoDocumentBuilder
            .disposalOrderWithoutHearing(caseData.getDisposalOrderWithoutHearing())
            .disposalHearingTime(caseData.getDisposalHearingHearingTime());
        Optional.ofNullable(caseData.getDisposalHearingHearingTime())
            .map(DisposalHearingHearingTime::getTime)
            .map(DisposalHearingFinalDisposalHearingTimeEstimate::getLabel)
            .ifPresent(sdoDocumentBuilder::disposalHearingTimeEstimate);

        Optional.ofNullable(caseData.getDisposalHearingHearingTime())
            .filter(hearingTime -> DisposalHearingFinalDisposalHearingTimeEstimate.OTHER.equals(hearingTime.getTime()))
                .ifPresent(hearingTime -> sdoDocumentBuilder
                    .disposalHearingTimeEstimate(
                        String.format("%s hours %s minutes",
                                      hearingTime.getOtherHours(),
                                      hearingTime.getOtherMinutes())
                    ));

        sdoDocumentBuilder.hearingLocation(
            locationHelper.getHearingLocation(
                Optional.ofNullable(caseData.getDisposalHearingMethodInPerson())
                    .map(DynamicList::getValue)
                    .map(DynamicListElement::getLabel)
                    .orElse(null),
                caseData,
                authorisation
            ))
            .caseManagementLocation(locationHelper.getHearingLocation(null, caseData, authorisation));

        sdoDocumentBuilder
            .hasDisposalWelshToggle(caseData.getSdoR2DisposalHearingUseOfWelshToggle() != null)
            .welshLanguageDescription(caseData.getSdoR2DisposalHearingUseOfWelshLanguage() != null
                                          ? caseData.getSdoR2DisposalHearingUseOfWelshLanguage().getDescription() : null);

        SdoDocumentFormDisposal sdoDocumentFormDisposal = sdoDocumentBuilder.build();
        log.info("Disposal SDO template data: {} for caseId {}", sdoDocumentFormDisposal, caseData.getCcdCaseReference());
        return sdoDocumentFormDisposal;
    }

    private SdoDocumentFormFast getTemplateDataFast(CaseData caseData, String judgeName, boolean isJudge, String authorisation) {
        var sdoDocumentFormBuilder = SdoDocumentFormFast.builder()
            .writtenByJudge(isJudge)
            .currentDate(LocalDate.now())
            .judgeName(judgeName)
            .caseNumber(caseData.getLegacyCaseReference())
            .applicant1(caseData.getApplicant1())
            .hasApplicant2(
                sdoHelper.hasSharedVariable(caseData, APPLICANT_2)
            )
            .applicant2(caseData.getApplicant2())
            .respondent1(caseData.getRespondent1())
            .hasRespondent2(
                sdoHelper.hasSharedVariable(caseData, RESPONDENT_2)
            )
            .respondent2(caseData.getRespondent2())
            .drawDirectionsOrderRequired(caseData.getDrawDirectionsOrderRequired())
            .drawDirectionsOrder(caseData.getDrawDirectionsOrder())
            .claimsTrack(caseData.getClaimsTrack())
            .fastClaims(caseData.getFastClaims())
            .hasBuildingDispute(
                sdoHelper.hasFastAdditionalDirections(caseData, "fastClaimBuildingDispute")
            )
            .hasClinicalNegligence(
                sdoHelper.hasFastAdditionalDirections(caseData, "fastClaimClinicalNegligence")
            )
            .hasSdoR2CreditHire(
                (sdoHelper.hasFastAdditionalDirections(caseData, "fastClaimCreditHire"))
            )
            .hasSdoR2CreditHireDetails(
                (nonNull(caseData.getSdoR2FastTrackCreditHire()) && (caseData.getSdoR2FastTrackCreditHire().getDetailsShowToggle() != null)
                    && caseData.getSdoR2FastTrackCreditHire().getDetailsShowToggle()
                    .equals(List.of(AddOrRemoveToggle.ADD)))
            )
            .hasEmployersLiability(
                sdoHelper.hasFastAdditionalDirections(caseData, "fastClaimEmployersLiability")
            )
            .hasHousingDisrepair(
                sdoHelper.hasFastAdditionalDirections(caseData, "fastClaimHousingDisrepair")
            )
            .hasPersonalInjury(
                sdoHelper.hasFastAdditionalDirections(caseData, "fastClaimPersonalInjury")
            )
            .hasRoadTrafficAccident(
                sdoHelper.hasFastAdditionalDirections(caseData, "fastClaimRoadTrafficAccident")
            )
            .fastTrackJudgesRecital(caseData.getFastTrackJudgesRecital())
            .fastTrackDisclosureOfDocuments(caseData.getFastTrackDisclosureOfDocuments())
            .fastTrackSchedulesOfLoss(caseData.getFastTrackSchedulesOfLoss())
            .fastTrackTrial(caseData.getFastTrackTrial())
            .fastTrackMethod(caseData.getFastTrackMethod())
            .fastTrackMethodInPerson(caseData.getFastTrackMethodInPerson())
            .fastTrackMethodTelephoneHearing(
                sdoHelper.getFastTrackMethodTelephoneHearingLabel(caseData)
            )
            .fastTrackMethodVideoConferenceHearing(
                sdoHelper.getFastTrackMethodVideoConferenceHearingLabel(caseData)
            )
            .fastTrackBuildingDispute(caseData.getFastTrackBuildingDispute())
            .fastTrackClinicalNegligence(caseData.getFastTrackClinicalNegligence())
            .fastTrackHousingDisrepair(caseData.getFastTrackHousingDisrepair())
            .fastTrackPersonalInjury(caseData.getFastTrackPersonalInjury())
            .fastTrackRoadTrafficAccident(caseData.getFastTrackRoadTrafficAccident())
            .hasNewDirections(
                sdoHelper.hasFastTrackVariable(caseData, "fastTrackAddNewDirections")
            )
            .fastTrackAddNewDirections(caseData.getFastTrackAddNewDirections())
            .fastTrackNotes(caseData.getFastTrackNotes())
            .fastTrackTrialDateToToggle(
                sdoHelper.hasFastTrackVariable(caseData, "fastTrackTrialDateToToggle"))
            .fastTrackAltDisputeResolutionToggle(
                sdoHelper.hasFastTrackVariable(caseData, "fastTrackAltDisputeResolutionToggle")
            )
            .fastTrackVariationOfDirectionsToggle(
                sdoHelper.hasFastTrackVariable(caseData, "fastTrackVariationOfDirectionsToggle")
            )
            .fastTrackSettlementToggle(
                sdoHelper.hasFastTrackVariable(caseData, "fastTrackSettlementToggle")
            )
            .fastTrackDisclosureOfDocumentsToggle(
                sdoHelper.hasFastTrackVariable(caseData, "fastTrackDisclosureOfDocumentsToggle")
            )
            .fastTrackWitnessOfFactToggle(
                sdoHelper.hasFastTrackVariable(caseData, "fastTrackWitnessOfFactToggle")
            )
            .fastTrackSchedulesOfLossToggle(
                sdoHelper.hasFastTrackVariable(caseData, "fastTrackSchedulesOfLossToggle")
            )
            .fastTrackCostsToggle(
                sdoHelper.hasFastTrackVariable(caseData, "fastTrackCostsToggle")
            )
            .fastTrackTrialToggle(
                sdoHelper.hasFastTrackVariable(caseData, "fastTrackTrialToggle")
            )
            // SNI-5142
            .fastTrackMethodToggle(true)
            .fastTrackAllocation(sdoHelper.getFastTrackAllocation(caseData))
            .showBundleInfo(sdoHelper.hasFastTrackVariable(caseData, "fastTrackTrialBundleToggle"));

        sdoDocumentFormBuilder
            .fastTrackOrderWithoutJudgement(caseData.getFastTrackOrderWithoutJudgement())
            .fastTrackHearingTime(caseData.getFastTrackHearingTime())
            .fastTrackHearingTimeEstimate(sdoHelper.getFastClaimsHearingTimeLabel(caseData));

        sdoDocumentFormBuilder
            .hearingLocation(locationHelper.getHearingLocation(
                Optional.ofNullable(caseData.getFastTrackMethodInPerson())
                    .map(DynamicList::getValue)
                    .map(DynamicListElement::getLabel)
                    .orElse(null),
                caseData,
                authorisation
            ))
            .caseManagementLocation(locationHelper.getHearingLocation(null, caseData, authorisation));

        sdoDocumentFormBuilder.fastTrackWelshLanguageToggle(
                sdoHelper.hasFastTrackVariable(caseData, "sdoR2FastTrackUseOfWelshToggle"))
            .welshLanguageDescription(caseData.getSdoR2FastTrackUseOfWelshLanguage() != null
                                          ? caseData.getSdoR2FastTrackUseOfWelshLanguage().getDescription() : null);
        sdoDocumentFormBuilder.sdoR2WitnessesOfFact(caseData.getSdoR2FastTrackWitnessOfFact())
            .sdoR2FastTrackCreditHire(caseData.getSdoR2FastTrackCreditHire());

        return sdoDocumentFormBuilder.build();
    }

    private SdoDocumentFormFastNihl getTemplateDataFastNihl(CaseData caseData, String judgeName, boolean isJudge, String authorisation) {
        SdoDocumentFormFastNihl.SdoDocumentFormFastNihlBuilder sdoNihlDocumentFormBuilder = SdoDocumentFormFastNihl.builder()
            .writtenByJudge(isJudge)
            .currentDate(LocalDate.now())
            .judgeName(judgeName)
            .caseNumber(caseData.getLegacyCaseReference())
            .applicant1(caseData.getApplicant1())
            .hasApplicant2(
                sdoHelper.hasSharedVariable(caseData, APPLICANT_2)
            )
            .applicant2(caseData.getApplicant2())
            .respondent1(caseData.getRespondent1())
            .hasRespondent2(
                sdoHelper.hasSharedVariable(caseData, RESPONDENT_2)
            )
            .respondent2(caseData.getRespondent2())
            .drawDirectionsOrderRequired(caseData.getDrawDirectionsOrderRequired())
            .drawDirectionsOrder(caseData.getDrawDirectionsOrder())
            .claimsTrack(caseData.getClaimsTrack())
            .fastClaims(caseData.getFastClaims())
            .sdoFastTrackJudgesRecital(caseData.getSdoFastTrackJudgesRecital())
            .sdoR2DisclosureOfDocuments(caseData.getSdoR2DisclosureOfDocuments())
            .sdoR2WitnessesOfFact(caseData.getSdoR2WitnessesOfFact())
            .sdoR2ExpertEvidence(caseData.getSdoR2ExpertEvidence())
            .sdoR2AddendumReport(caseData.getSdoR2AddendumReport())
            .sdoR2FurtherAudiogram(caseData.getSdoR2FurtherAudiogram())
            .sdoR2QuestionsClaimantExpert(caseData.getSdoR2QuestionsClaimantExpert())
            .sdoR2PermissionToRelyOnExpert(caseData.getSdoR2PermissionToRelyOnExpert())
            .sdoR2EvidenceAcousticEngineer(caseData.getSdoR2EvidenceAcousticEngineer())
            .sdoR2QuestionsToEntExpert(caseData.getSdoR2QuestionsToEntExpert())
            .sdoR2ScheduleOfLoss(caseData.getSdoR2ScheduleOfLoss())
            .sdoR2UploadOfDocuments(caseData.getSdoR2UploadOfDocuments())
            .sdoR2AddNewDirection(caseData.getSdoR2AddNewDirection())
            .sdoR2Trial(caseData.getSdoR2Trial())
            .sdoR2ImportantNotesTxt(caseData.getSdoR2ImportantNotesTxt())
            .sdoR2ImportantNotesDate(caseData.getSdoR2ImportantNotesDate())
            .hasAltDisputeResolution(caseData.getSdoAltDisputeResolution().getIncludeInOrderToggle() != null)
            .hasVariationOfDirections(caseData.getSdoVariationOfDirections().getIncludeInOrderToggle() != null)
            .hasSettlement(caseData.getSdoR2Settlement()
                               .getIncludeInOrderToggle() != null)
            .hasDisclosureOfDocuments(caseData.getSdoR2DisclosureOfDocumentsToggle() != null)
            .hasWitnessOfFact(caseData.getSdoR2SeparatorWitnessesOfFactToggle() != null)
            .hasRestrictWitness(sdoHelper.isRestrictWitnessNihl(caseData))
            .hasRestrictPages(sdoHelper.isRestrictPagesNihl(caseData))
            .hasExpertEvidence(caseData.getSdoR2SeparatorExpertEvidenceToggle() != null)
            .hasAddendumReport(caseData.getSdoR2SeparatorAddendumReportToggle() != null)
            .hasFurtherAudiogram(caseData.getSdoR2SeparatorFurtherAudiogramToggle() != null)
            .hasQuestionsOfClaimantExpert(caseData.getSdoR2SeparatorQuestionsClaimantExpertToggle() != null)
            .isApplicationToRelyOnFurther(sdoHelper.isApplicationToRelyOnFurtherNihl(caseData))
            .hasPermissionFromENT(caseData.getSdoR2SeparatorPermissionToRelyOnExpertToggle() != null)
            .hasEvidenceFromAcousticEngineer(caseData.getSdoR2SeparatorEvidenceAcousticEngineerToggle() != null)
            .hasQuestionsToENTAfterReport(caseData.getSdoR2SeparatorQuestionsToEntExpertToggle() != null)
            .hasScheduleOfLoss(caseData.getSdoR2ScheduleOfLossToggle() != null)
            .hasClaimForPecuniaryLoss(sdoHelper.isClaimForPecuniaryLossNihl(caseData))
            .hasUploadDocuments(caseData.getSdoR2SeparatorUploadOfDocumentsToggle() != null)
            .hasSdoTrial(caseData.getSdoR2TrialToggle() != null)
            .hasNewDirections(caseData.getSdoR2AddNewDirection() != null)
            .sdoR2AddNewDirection(caseData.getSdoR2AddNewDirection())
            .hasSdoR2TrialWindow(caseData.getSdoR2TrialToggle() != null
                                     && TrialOnRadioOptions.TRIAL_WINDOW.equals(
                caseData.getSdoR2Trial().getTrialOnOptions()))
            .sdoTrialHearingTimeAllocated(sdoHelper.getSdoTrialHearingTimeAllocated(caseData))
            .sdoTrialMethodOfHearing(sdoHelper.getSdoTrialMethodOfHearing(caseData))
            .hasSdoR2TrialPhysicalBundleParty(caseData.getSdoR2Trial() != null
                                                  && PhysicalTrialBundleOptions.PARTY.equals(
                caseData.getSdoR2Trial().getPhysicalBundleOptions()))
            .physicalBundlePartyTxt(sdoHelper.getPhysicalTrialTextNihl(caseData))
            .hasNihlWelshLangToggle(caseData.getSdoR2NihlUseOfWelshIncludeInOrderToggle() != null)
            .welshLanguageDescription(caseData.getSdoR2NihlUseOfWelshLanguage() != null
                                          ? caseData.getSdoR2NihlUseOfWelshLanguage().getDescription() : null);

        if (caseData.getSdoR2Trial() != null) {
            sdoNihlDocumentFormBuilder
                .hearingLocation(locationHelper.getHearingLocation(
                    Optional.ofNullable(sdoHelper.getHearingLocationNihl(caseData))
                        .map(DynamicList::getValue)
                        .map(DynamicListElement::getLabel)
                        .orElse(null),
                    caseData,
                    authorisation
                ));
        }
        sdoNihlDocumentFormBuilder.caseManagementLocation(locationHelper.getHearingLocation(null, caseData, authorisation));

        return sdoNihlDocumentFormBuilder.build();
    }

    private SdoDocumentFormSmall getTemplateDataSmall(CaseData caseData, String judgeName, boolean isJudge, String authorisation) {
        boolean carmEnabled = featureToggleService.isCarmEnabledForCase(caseData);
        SdoDocumentFormSmall.SdoDocumentFormSmallBuilder sdoDocumentFormBuilder = SdoDocumentFormSmall.builder()
            .writtenByJudge(isJudge)
            .currentDate(LocalDate.now())
            .judgeName(judgeName)
            .caseNumber(caseData.getLegacyCaseReference())
            .applicant1(caseData.getApplicant1())
            .hasApplicant2(
                sdoHelper.hasSharedVariable(caseData, APPLICANT_2)
            )
            .applicant2(caseData.getApplicant2())
            .respondent1(caseData.getRespondent1())
            .hasRespondent2(
                sdoHelper.hasSharedVariable(caseData, RESPONDENT_2)
            )
            .respondent2(caseData.getRespondent2())
            .drawDirectionsOrderRequired(caseData.getDrawDirectionsOrderRequired())
            .drawDirectionsOrder(caseData.getDrawDirectionsOrder())
            .claimsTrack(caseData.getClaimsTrack())
            .smallClaims(caseData.getSmallClaims())
            .hasCreditHire(
                sdoHelper.hasSmallAdditionalDirections(caseData, "smallClaimCreditHire")
            )
            .hasRoadTrafficAccident(
                sdoHelper.hasSmallAdditionalDirections(caseData, "smallClaimRoadTrafficAccident")
            )
            .smallClaimsJudgesRecital(caseData.getSmallClaimsJudgesRecital())
            .smallClaimsHearing(caseData.getSmallClaimsHearing())
            .smallClaimsHearingTime(
                sdoHelper.getSmallClaimsHearingTimeLabel(caseData)
            )
            // CIV-5514: smallClaimsMethodInPerson, smallClaimsMethodTelephoneHearing and
            // smallClaimsMethodVideoConferenceHearing can be removed after HNL is live
            .smallClaimsMethod(caseData.getSmallClaimsMethod())
            .smallClaimsMethodInPerson(caseData.getSmallClaimsMethodInPerson())
            .smallClaimsMethodTelephoneHearing(
                sdoHelper.getSmallClaimsMethodTelephoneHearingLabel(caseData)
            )
            .smallClaimsMethodVideoConferenceHearing(
                sdoHelper.getSmallClaimsMethodVideoConferenceHearingLabel(caseData)
            )
            .smallClaimsDocuments(caseData.getSmallClaimsDocuments())
            .smallClaimsCreditHire(caseData.getSmallClaimsCreditHire())
            .smallClaimsRoadTrafficAccident(caseData.getSmallClaimsRoadTrafficAccident())
            .hasNewDirections(
                sdoHelper.hasSmallClaimsVariable(caseData, "smallClaimsAddNewDirections")
            )
            .smallClaimsAddNewDirections(caseData.getSmallClaimsAddNewDirections())
            .smallClaimsNotes(caseData.getSmallClaimsNotes())
            .smallClaimsHearingToggle(
                sdoHelper.hasSmallClaimsVariable(caseData, "smallClaimsHearingToggle")
            )
            // SNI-5142
            .smallClaimsMethodToggle(true)
            .smallClaimMediationSectionInput(sdoHelper.getSmallClaimsMediationText(caseData))
            .smallClaimsDocumentsToggle(
                sdoHelper.hasSmallClaimsVariable(caseData, "smallClaimsDocumentsToggle")
            )
            .smallClaimsWitnessStatementToggle(
                sdoHelper.hasSmallClaimsVariable(caseData, "smallClaimsWitnessStatementToggle")
            )
            .smallClaimsNumberOfWitnessesToggle(
                sdoHelper.hasSmallClaimsVariable(caseData, "smallClaimsNumberOfWitnessesToggle")
            )
            .smallClaimsMediationSectionToggle(
                sdoHelper.showCarmMediationSection(caseData, carmEnabled)
            )
            .caseAccessCategory(caseData.getCaseAccessCategory().toString())
            .carmEnabled(carmEnabled);

        sdoDocumentFormBuilder.smallClaimsFlightDelayToggle(sdoHelper.hasSmallClaimsVariable(caseData, "smallClaimsFlightDelayToggle"))
            .smallClaimsFlightDelay(caseData.getSmallClaimsFlightDelay())
            .smallClaimsWelshLanguageToggle(sdoHelper.hasSmallClaimsVariable(caseData, "sdoR2SmallClaimsUseOfWelshToggle"))
            .welshLanguageDescription(caseData.getSdoR2SmallClaimsUseOfWelshLanguage() != null ? caseData.getSdoR2SmallClaimsUseOfWelshLanguage().getDescription() : null)
            .sdoR2SmallClaimsWitnessStatements(caseData.getSdoR2SmallClaimsWitnessStatementOther());

        sdoDocumentFormBuilder.hearingLocation(
                locationHelper.getHearingLocation(
                    Optional.ofNullable(caseData.getSmallClaimsMethodInPerson())
                        .map(DynamicList::getValue)
                        .map(DynamicListElement::getLabel)
                        .orElse(null),
                    caseData,
                    authorisation
                ))
            .caseManagementLocation(
                locationHelper.getHearingLocation(null, caseData, authorisation));

        return sdoDocumentFormBuilder
            .build();
    }

    private SdoDocumentFormSmallDrh getTemplateDataSmallDrh(CaseData caseData, String judgeName, boolean isJudge, String authorisation) { //TODO Change to suit SDO R2 DRH
        boolean carmEnabled = featureToggleService.isCarmEnabledForCase(caseData);
        SdoDocumentFormSmallDrh.SdoDocumentFormSmallDrhBuilder sdoDocumentFormBuilderDrh = SdoDocumentFormSmallDrh.builder()
            .writtenByJudge(isJudge)
            .currentDate(LocalDate.now())
            .judgeName(judgeName)
            .caseNumber(caseData.getLegacyCaseReference())
            .applicant1(caseData.getApplicant1())
            .hasApplicant2(
                sdoHelper.hasSharedVariable(caseData, APPLICANT_2)
            )
            .applicant2(caseData.getApplicant2())
            .respondent1(caseData.getRespondent1())
            .hasRespondent2(
                sdoHelper.hasSharedVariable(caseData, RESPONDENT_2)
            )
            .respondent2(caseData.getRespondent2())
            .hasPaymentProtectionInsurance(caseData.getSdoR2SmallClaimsPPIToggle() != null)
            .hasHearingToggle(caseData.getSdoR2SmallClaimsHearingToggle() != null)
            .hasWitnessStatement(caseData.getSdoR2SmallClaimsWitnessStatements() != null)
            .hasUploadDocToggle(caseData.getSdoR2SmallClaimsUploadDocToggle() != null)
            .hasDRHWelshLangToggle(caseData.getSdoR2DrhUseOfWelshIncludeInOrderToggle() != null)
            .hasSdoR2HearingTrialWindow(sdoHelper.hasSdoR2HearingTrialWindow(caseData))
            .hasNewDirections(caseData.getSdoR2SmallClaimsAddNewDirection() != null)
            .sdoR2SmallClaimsPhysicalTrialBundleTxt(sdoHelper.getSdoR2SmallClaimsPhysicalTrialBundleTxt(caseData))
            .sdoR2SmallClaimsJudgesRecital(caseData.getSdoR2SmallClaimsJudgesRecital())
            .sdoR2SmallClaimsHearing(caseData.getSdoR2SmallClaimsHearing())
            .sdoR2SmallClaimsWitnessStatements(caseData.getSdoR2SmallClaimsWitnessStatements())
            .sdoR2SmallClaimsPPI(caseData.getSdoR2SmallClaimsPPI())
            .sdoR2SmallClaimsUploadDoc(caseData.getSdoR2SmallClaimsUploadDoc())
            .smallClaimsMethod(sdoHelper.getSdoR2SmallClaimsHearingMethod(caseData))
            .hearingTime(sdoHelper.getSdoR2HearingTime(caseData))
            .sdoR2SmallClaimsImpNotes(caseData.getSdoR2SmallClaimsImpNotes())
            .sdoR2SmallClaimsAddNewDirection(caseData.getSdoR2SmallClaimsAddNewDirection())
            .welshLanguageDescription(caseData.getSdoR2DrhUseOfWelshLanguage() != null
                                          ? caseData.getSdoR2DrhUseOfWelshLanguage().getDescription() : null)
            .carmEnabled(carmEnabled)
            .sdoR2SmallClaimMediationSectionInput(sdoHelper.getSmallClaimsMediationTextDRH(caseData))
            .caseManagementLocation(
                locationHelper.getHearingLocation(null, caseData, authorisation))
            .sdoR2SmallClaimsMediationSectionToggle(
                sdoHelper.showCarmMediationSectionDRH(caseData, carmEnabled)
            );

        if (caseData.getSdoR2SmallClaimsHearing() != null) {
            sdoDocumentFormBuilderDrh.hearingLocation(
                locationHelper.getHearingLocation(
                    Optional.ofNullable(sdoHelper.getHearingLocationDrh(caseData))
                        .map(DynamicList::getValue)
                        .map(DynamicListElement::getLabel)
                        .orElse(null),
                    caseData,
                    authorisation
                ));

        }

        return sdoDocumentFormBuilderDrh
            .build();
    }

}
