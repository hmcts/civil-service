package uk.gov.hmcts.reform.civil.service.docmosis.sdo;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.helpers.sdo.SdoHelper;
import uk.gov.hmcts.reform.civil.launchdarkly.FeatureToggleService;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.docmosis.sdo.SdoDocumentFormDisposal;
import uk.gov.hmcts.reform.civil.model.docmosis.sdo.SdoDocumentFormFast;
import uk.gov.hmcts.reform.civil.model.docmosis.sdo.SdoDocumentFormSmall;
import uk.gov.hmcts.reform.civil.model.documents.CaseDocument;
import uk.gov.hmcts.reform.civil.model.documents.DocumentType;
import uk.gov.hmcts.reform.civil.model.documents.PDF;
import uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class SdoGeneratorService {

    private final DocumentGeneratorService documentGeneratorService;
    private final DocumentManagementService documentManagementService;
    private final IdamClient idamClient;
    private final FeatureToggleService toggleService;

    public CaseDocument generate(CaseData caseData, String authorisation) {
        MappableObject templateData;
        DocmosisTemplates docmosisTemplate;

        UserDetails userDetails = idamClient.getUserDetails(authorisation);
        String judgeName = userDetails.getFullName();

        if (SdoHelper.isSmallClaimsTrack(caseData)) {
            docmosisTemplate = DocmosisTemplates.SDO_SMALL;

            if (toggleService.isHearingAndListingSDOEnabled()) {
                docmosisTemplate = DocmosisTemplates.SDO_SMALL_HNL;
            }
            templateData = getTemplateDataSmall(caseData, judgeName);
        } else if (SdoHelper.isFastTrack(caseData)) {
            docmosisTemplate = DocmosisTemplates.SDO_FAST;
            templateData = getTemplateDataFast(caseData, judgeName);
        } else {
            docmosisTemplate = DocmosisTemplates.SDO_DISPOSAL;
            templateData = getTemplateDataDisposal(caseData, judgeName);
        }

        DocmosisDocument docmosisDocument = documentGeneratorService.generateDocmosisDocument(
            templateData,
            docmosisTemplate
        );

        return documentManagementService.uploadDocument(
            authorisation,
            new PDF(getFileName(docmosisTemplate, caseData),
                    docmosisDocument.getBytes(),
                    DocumentType.SDO_ORDER
            )
        );
    }

    private String getFileName(DocmosisTemplates docmosisTemplate, CaseData caseData) {
        return String.format(docmosisTemplate.getDocumentTitle(), caseData.getLegacyCaseReference());
    }

    private SdoDocumentFormDisposal getTemplateDataDisposal(CaseData caseData, String judgeName) {
        return SdoDocumentFormDisposal.builder()
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
            )
            .build();
    }

    private SdoDocumentFormFast getTemplateDataFast(CaseData caseData, String judgeName) {
        return SdoDocumentFormFast.builder()
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
            .build();
    }

    private SdoDocumentFormSmall getTemplateDataSmall(CaseData caseData, String judgeName) {
        return SdoDocumentFormSmall.builder()
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
            .smallClaimsMethod(caseData.getSmallClaimsMethod())
            // CIV-5514: smallClaimsMethodInPerson, smallClaimsMethodTelephoneHearing and
            // smallClaimsMethodVideoConferenceHearing can be removed after HNL is live
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
            .build();
    }
}
