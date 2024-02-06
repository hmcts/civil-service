package uk.gov.hmcts.reform.civil.service.docmosis.sdo;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.enums.sdo.DisposalHearingFinalDisposalHearingTimeEstimate;
import uk.gov.hmcts.reform.civil.helpers.sdo.SdoHelper;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.docmosis.sdo.SdoDocumentFormDisposal;
import uk.gov.hmcts.reform.civil.model.docmosis.sdo.SdoDocumentFormFast;
import uk.gov.hmcts.reform.civil.model.docmosis.sdo.SdoDocumentFormSmall;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingHearingTime;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentHearingLocationHelper;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.time.LocalDate;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.helpers.sdo.SdoHelper.getFastTrackAllocation;

@Service
@RequiredArgsConstructor
public class SdoGeneratorService {

    private final DocumentGeneratorService documentGeneratorService;
    private final DocumentManagementService documentManagementService;
    private final IdamClient idamClient;
    private final DocumentHearingLocationHelper locationHelper;
    private final FeatureToggleService featureToggleService;

    public CaseDocument generate(CaseData caseData, String authorisation) {
        MappableObject templateData;
        DocmosisTemplates docmosisTemplate;

        UserDetails userDetails = idamClient.getUserDetails(authorisation);
        String judgeName = userDetails.getFullName();

        boolean isJudge = false;

        if (userDetails.getRoles() != null) {
            isJudge = userDetails.getRoles().stream()
                .anyMatch(s -> s != null && s.toLowerCase().contains("judge"));
        }

        if (SdoHelper.isSmallClaimsTrack(caseData)) {
            docmosisTemplate = DocmosisTemplates.SDO_SMALL;
            templateData = getTemplateDataSmall(caseData, judgeName, isJudge, authorisation);
        } else if (SdoHelper.isFastTrack(caseData)) {
            docmosisTemplate = featureToggleService.isFastTrackUpliftsEnabled()
                ? DocmosisTemplates.SDO_FAST_FAST_TRACK_INT : DocmosisTemplates.SDO_FAST;
            templateData = getTemplateDataFast(caseData, judgeName, isJudge, authorisation);
        } else {
            docmosisTemplate = DocmosisTemplates.SDO_DISPOSAL;
            templateData = getTemplateDataDisposal(caseData, judgeName, isJudge, authorisation);
        }

        DocmosisDocument docmosisDocument = documentGeneratorService.generateDocmosisDocument(
            templateData,
            docmosisTemplate
        );

        return documentManagementService.uploadDocument(
            authorisation,
            new PDF(
                getFileName(docmosisTemplate, caseData),
                docmosisDocument.getBytes(),
                DocumentType.SDO_ORDER
            )
        );
    }

    private String getFileName(DocmosisTemplates docmosisTemplate, CaseData caseData) {
        return String.format(docmosisTemplate.getDocumentTitle(), caseData.getLegacyCaseReference());
    }

    private SdoDocumentFormDisposal getTemplateDataDisposal(CaseData caseData, String judgeName, boolean isJudge, String authorisation) {
        var sdoDocumentBuilder = SdoDocumentFormDisposal.builder()
            .writtenByJudge(isJudge)
            .currentDate(LocalDate.now())
            .judgeName(judgeName)
            .caseNumber(caseData.getLegacyCaseReference())
            .applicant1(caseData.getApplicant1())
            .hasApplicant2(
                SdoHelper.hasSharedVariable(caseData, "applicant2")
            )
            .applicant2(caseData.getApplicant2())
            .respondent1(caseData.getRespondent1())
            .hasRespondent2(
                SdoHelper.hasSharedVariable(caseData, "respondent2")
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
                SdoHelper.getDisposalHearingFinalDisposalHearingTimeLabel(caseData)
            )
            .disposalHearingMethod(caseData.getDisposalHearingMethod())
            .disposalHearingMethodInPerson(caseData.getDisposalHearingMethodInPerson())
            .disposalHearingMethodTelephoneHearing(
                SdoHelper.getDisposalHearingMethodTelephoneHearingLabel(caseData)
            )
            .disposalHearingMethodVideoConferenceHearing(
                SdoHelper.getDisposalHearingMethodVideoConferenceHearingLabel(caseData)
            )
            .disposalHearingBundle(caseData.getDisposalHearingBundle())
            .disposalHearingBundleTypeText(
                SdoHelper.getDisposalHearingBundleTypeText(caseData)
            )
            .hasNewDirections(
                SdoHelper.hasDisposalVariable(caseData, "disposalHearingAddNewDirections")
            )
            .disposalHearingAddNewDirections(caseData.getDisposalHearingAddNewDirections())
            .disposalHearingNotes(caseData.getDisposalHearingNotes())
            .disposalHearingDisclosureOfDocumentsToggle(
                SdoHelper.hasDisposalVariable(caseData, "disposalHearingDisclosureOfDocumentsToggle")
            )
            .disposalHearingWitnessOfFactToggle(
                SdoHelper.hasDisposalVariable(caseData, "disposalHearingWitnessOfFactToggle")
            )
            .disposalHearingMedicalEvidenceToggle(
                SdoHelper.hasDisposalVariable(caseData, "disposalHearingMedicalEvidenceToggle")
            )
            .disposalHearingQuestionsToExpertsToggle(
                SdoHelper.hasDisposalVariable(caseData, "disposalHearingQuestionsToExpertsToggle")
            )
            .disposalHearingSchedulesOfLossToggle(
                SdoHelper.hasDisposalVariable(caseData, "disposalHearingSchedulesOfLossToggle")
            )
            .disposalHearingFinalDisposalHearingToggle(
                SdoHelper.hasDisposalVariable(caseData, "disposalHearingFinalDisposalHearingToggle")
            )
            .disposalHearingMethodToggle(
                SdoHelper.hasDisposalVariable(caseData, "disposalHearingMethodToggle")
            )
            .disposalHearingBundleToggle(
                SdoHelper.hasDisposalVariable(caseData, "disposalHearingBundleToggle")
            )
            .disposalHearingClaimSettlingToggle(
                SdoHelper.hasDisposalVariable(caseData, "disposalHearingClaimSettlingToggle")
            )
            .disposalHearingCostsToggle(
                SdoHelper.hasDisposalVariable(caseData, "disposalHearingCostsToggle")
            );

        sdoDocumentBuilder
            .disposalOrderWithoutHearing(caseData.getDisposalOrderWithoutHearing())
            .disposalHearingTime(caseData.getDisposalHearingHearingTime());
        Optional.ofNullable(caseData.getDisposalHearingHearingTime())
            .map(DisposalHearingHearingTime::getTime)
            .map(DisposalHearingFinalDisposalHearingTimeEstimate::getLabel)
            .ifPresent(sdoDocumentBuilder::disposalHearingTimeEstimate);

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

        return sdoDocumentBuilder.build();
    }

    private SdoDocumentFormFast getTemplateDataFast(CaseData caseData, String judgeName, boolean isJudge, String authorisation) {
        var sdoDocumentFormBuilder = SdoDocumentFormFast.builder()
            .writtenByJudge(isJudge)
            .currentDate(LocalDate.now())
            .judgeName(judgeName)
            .caseNumber(caseData.getLegacyCaseReference())
            .applicant1(caseData.getApplicant1())
            .hasApplicant2(
                SdoHelper.hasSharedVariable(caseData, "applicant2")
            )
            .applicant2(caseData.getApplicant2())
            .respondent1(caseData.getRespondent1())
            .hasRespondent2(
                SdoHelper.hasSharedVariable(caseData, "respondent2")
            )
            .respondent2(caseData.getRespondent2())
            .drawDirectionsOrderRequired(caseData.getDrawDirectionsOrderRequired())
            .drawDirectionsOrder(caseData.getDrawDirectionsOrder())
            .claimsTrack(caseData.getClaimsTrack())
            .fastClaims(caseData.getFastClaims())
            .hasBuildingDispute(
                SdoHelper.hasFastAdditionalDirections(caseData, "fastClaimBuildingDispute")
            )
            .hasClinicalNegligence(
                SdoHelper.hasFastAdditionalDirections(caseData, "fastClaimClinicalNegligence")
            )
            .hasCreditHire(
                SdoHelper.hasFastAdditionalDirections(caseData, "fastClaimCreditHire")
            )
            .hasEmployersLiability(
                SdoHelper.hasFastAdditionalDirections(caseData, "fastClaimEmployersLiability")
            )
            .hasHousingDisrepair(
                SdoHelper.hasFastAdditionalDirections(caseData, "fastClaimHousingDisrepair")
            )
            .hasPersonalInjury(
                SdoHelper.hasFastAdditionalDirections(caseData, "fastClaimPersonalInjury")
            )
            .hasRoadTrafficAccident(
                SdoHelper.hasFastAdditionalDirections(caseData, "fastClaimRoadTrafficAccident")
            )
            .fastTrackJudgesRecital(caseData.getFastTrackJudgesRecital())
            .fastTrackDisclosureOfDocuments(caseData.getFastTrackDisclosureOfDocuments())
            .fastTrackWitnessOfFact(caseData.getFastTrackWitnessOfFact())
            .fastTrackSchedulesOfLoss(caseData.getFastTrackSchedulesOfLoss())
            .fastTrackTrial(caseData.getFastTrackTrial())
            .fastTrackTrialBundleTypeText(
                SdoHelper.getFastTrackTrialBundleTypeText(caseData)
            )
            .fastTrackMethod(caseData.getFastTrackMethod())
            .fastTrackMethodInPerson(caseData.getFastTrackMethodInPerson())
            .fastTrackMethodTelephoneHearing(
                SdoHelper.getFastTrackMethodTelephoneHearingLabel(caseData)
            )
            .fastTrackMethodVideoConferenceHearing(
                SdoHelper.getFastTrackMethodVideoConferenceHearingLabel(caseData)
            )
            .fastTrackBuildingDispute(caseData.getFastTrackBuildingDispute())
            .fastTrackClinicalNegligence(caseData.getFastTrackClinicalNegligence())
            .fastTrackCreditHire(caseData.getFastTrackCreditHire())
            .fastTrackHousingDisrepair(caseData.getFastTrackHousingDisrepair())
            .fastTrackPersonalInjury(caseData.getFastTrackPersonalInjury())
            .fastTrackRoadTrafficAccident(caseData.getFastTrackRoadTrafficAccident())
            .hasNewDirections(
                SdoHelper.hasFastTrackVariable(caseData, "fastTrackAddNewDirections")
            )
            .fastTrackAddNewDirections(caseData.getFastTrackAddNewDirections())
            .fastTrackNotes(caseData.getFastTrackNotes())
            .fastTrackTrialDateToToggle(
                SdoHelper.hasFastTrackVariable(caseData, "fastTrackTrialDateToToggle"))
            .fastTrackAltDisputeResolutionToggle(
                SdoHelper.hasFastTrackVariable(caseData, "fastTrackAltDisputeResolutionToggle")
            )
            .fastTrackVariationOfDirectionsToggle(
                SdoHelper.hasFastTrackVariable(caseData, "fastTrackVariationOfDirectionsToggle")
            )
            .fastTrackSettlementToggle(
                SdoHelper.hasFastTrackVariable(caseData, "fastTrackSettlementToggle")
            )
            .fastTrackDisclosureOfDocumentsToggle(
                SdoHelper.hasFastTrackVariable(caseData, "fastTrackDisclosureOfDocumentsToggle")
            )
            .fastTrackWitnessOfFactToggle(
                SdoHelper.hasFastTrackVariable(caseData, "fastTrackWitnessOfFactToggle")
            )
            .fastTrackSchedulesOfLossToggle(
                SdoHelper.hasFastTrackVariable(caseData, "fastTrackSchedulesOfLossToggle")
            )
            .fastTrackCostsToggle(
                SdoHelper.hasFastTrackVariable(caseData, "fastTrackCostsToggle")
            )
            .fastTrackTrialToggle(
                SdoHelper.hasFastTrackVariable(caseData, "fastTrackTrialToggle")
            )
            .fastTrackMethodToggle(
                SdoHelper.hasFastTrackVariable(caseData, "fastTrackMethodToggle")
            )
            .fastTrackAllocation(getFastTrackAllocation(caseData, featureToggleService.isFastTrackUpliftsEnabled()));

        sdoDocumentFormBuilder
            .fastTrackOrderWithoutJudgement(caseData.getFastTrackOrderWithoutJudgement())
            .fastTrackHearingTime(caseData.getFastTrackHearingTime())
            .fastTrackHearingTimeEstimate(SdoHelper.getFastClaimsHearingTimeLabel(caseData));

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

        return sdoDocumentFormBuilder.build();
    }

    private SdoDocumentFormSmall getTemplateDataSmall(CaseData caseData, String judgeName, boolean isJudge, String authorisation) {
        SdoDocumentFormSmall.SdoDocumentFormSmallBuilder sdoDocumentFormBuilder = SdoDocumentFormSmall.builder()
            .writtenByJudge(isJudge)
            .currentDate(LocalDate.now())
            .judgeName(judgeName)
            .caseNumber(caseData.getLegacyCaseReference())
            .applicant1(caseData.getApplicant1())
            .hasApplicant2(
                SdoHelper.hasSharedVariable(caseData, "applicant2")
            )
            .applicant2(caseData.getApplicant2())
            .respondent1(caseData.getRespondent1())
            .hasRespondent2(
                SdoHelper.hasSharedVariable(caseData, "respondent2")
            )
            .respondent2(caseData.getRespondent2())
            .drawDirectionsOrderRequired(caseData.getDrawDirectionsOrderRequired())
            .drawDirectionsOrder(caseData.getDrawDirectionsOrder())
            .claimsTrack(caseData.getClaimsTrack())
            .smallClaims(caseData.getSmallClaims())
            .hasCreditHire(
                SdoHelper.hasSmallAdditionalDirections(caseData, "smallClaimCreditHire")
            )
            .hasRoadTrafficAccident(
                SdoHelper.hasSmallAdditionalDirections(caseData, "smallClaimRoadTrafficAccident")
            )
            .smallClaimsJudgesRecital(caseData.getSmallClaimsJudgesRecital())
            .smallClaimsHearing(caseData.getSmallClaimsHearing())
            .smallClaimsHearingTime(
                SdoHelper.getSmallClaimsHearingTimeLabel(caseData)
            )
            // CIV-5514: smallClaimsMethodInPerson, smallClaimsMethodTelephoneHearing and
            // smallClaimsMethodVideoConferenceHearing can be removed after HNL is live
            .smallClaimsMethod(caseData.getSmallClaimsMethod())
            .smallClaimsMethodInPerson(caseData.getSmallClaimsMethodInPerson())
            .smallClaimsMethodTelephoneHearing(
                SdoHelper.getSmallClaimsMethodTelephoneHearingLabel(caseData)
            )
            .smallClaimsMethodVideoConferenceHearing(
                SdoHelper.getSmallClaimsMethodVideoConferenceHearingLabel(caseData)
            )
            .smallClaimsDocuments(caseData.getSmallClaimsDocuments())
            .smallClaimsWitnessStatement(caseData.getSmallClaimsWitnessStatement())
            .smallClaimsCreditHire(caseData.getSmallClaimsCreditHire())
            .smallClaimsRoadTrafficAccident(caseData.getSmallClaimsRoadTrafficAccident())
            .hasNewDirections(
                SdoHelper.hasSmallClaimsVariable(caseData, "smallClaimsAddNewDirections")
            )
            .smallClaimsAddNewDirections(caseData.getSmallClaimsAddNewDirections())
            .smallClaimsNotes(caseData.getSmallClaimsNotes())
            .smallClaimsHearingToggle(
                SdoHelper.hasSmallClaimsVariable(caseData, "smallClaimsHearingToggle")
            )
            .smallClaimsMethodToggle(
                SdoHelper.hasSmallClaimsVariable(caseData, "smallClaimsMethodToggle")
            )
            .smallClaimsDocumentsToggle(
                SdoHelper.hasSmallClaimsVariable(caseData, "smallClaimsDocumentsToggle")
            )
            .smallClaimsWitnessStatementToggle(
                SdoHelper.hasSmallClaimsVariable(caseData, "smallClaimsWitnessStatementToggle")
            )
            .smallClaimsNumberOfWitnessesToggle(
                SdoHelper.hasSmallClaimsVariable(caseData, "smallClaimsNumberOfWitnessesToggle")
            );

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

}
