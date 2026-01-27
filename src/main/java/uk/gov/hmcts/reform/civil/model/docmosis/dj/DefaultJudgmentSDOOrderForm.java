package uk.gov.hmcts.reform.civil.model.docmosis.dj;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.enums.dj.DisposalHearingMethodDJ;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.DisposalHearingAddNewDirectionsDJ;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.DisposalHearingBundleDJ;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.DisposalHearingDisclosureOfDocumentsDJ;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.DisposalHearingFinalDisposalHearingDJ;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.DisposalHearingJudgesRecitalDJ;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.DisposalHearingMedicalEvidenceDJ;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.DisposalHearingNotesDJ;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.DisposalHearingQuestionsToExpertsDJ;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.DisposalHearingSchedulesOfLossDJ;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.DisposalHearingWitnessOfFactDJ;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.SdoDJR2TrialCreditHire;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialBuildingDispute;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialClinicalNegligence;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialCreditHire;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialHearingAddNewDirectionsDJ;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialHearingDisclosureOfDocuments;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialHearingJudgesRecital;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialHearingNotes;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialHearingSchedulesOfLoss;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialHearingTrial;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialHearingWitnessOfFact;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialHousingDisrepair;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialPersonalInjury;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialRoadTrafficAccident;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingFinalDisposalHearingTimeDJ;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingOrderMadeWithoutHearingDJ;
import uk.gov.hmcts.reform.civil.model.sdo.TrialHearingTimeDJ;
import uk.gov.hmcts.reform.civil.model.sdo.TrialOrderMadeWithoutHearingDJ;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class DefaultJudgmentSDOOrderForm implements MappableObject {

    private  String caseNumber;
    private  String applicant;
    private  String respondent;
    private  String judgeNameTitle;

    //default judgement SDO fields for disposal
    private  DisposalHearingJudgesRecitalDJ disposalHearingJudgesRecitalDJ;
    private  DisposalHearingDisclosureOfDocumentsDJ disposalHearingDisclosureOfDocumentsDJ;
    private  DisposalHearingWitnessOfFactDJ disposalHearingWitnessOfFactDJ;
    private  DisposalHearingMedicalEvidenceDJ disposalHearingMedicalEvidenceDJ;
    private  DisposalHearingQuestionsToExpertsDJ disposalHearingQuestionsToExpertsDJ;
    private  DisposalHearingSchedulesOfLossDJ disposalHearingSchedulesOfLossDJ;
    private DisposalHearingFinalDisposalHearingDJ disposalHearingFinalDisposalHearingDJ;
    private  DisposalHearingBundleDJ disposalHearingBundleDJ;
    private  DisposalHearingNotesDJ disposalHearingNotesDJ;
    private  List<Element<DisposalHearingAddNewDirectionsDJ>> disposalHearingAddNewDirectionsDJ;
    private  boolean hasNewDirections;

    //default judgement SDO fields for trial
    private  TrialHearingJudgesRecital trialHearingJudgesRecitalDJ;
    private  TrialHearingDisclosureOfDocuments trialHearingDisclosureOfDocumentsDJ;
    private  TrialHearingWitnessOfFact trialHearingWitnessOfFactDJ;
    private  TrialHearingSchedulesOfLoss trialHearingSchedulesOfLossDJ;
    private  TrialHearingTrial trialHearingTrialDJ;
    private  TrialHearingNotes trialHearingNotesDJ;
    private  TrialBuildingDispute trialBuildingDispute;
    private  TrialClinicalNegligence trialClinicalNegligence;
    private  TrialCreditHire trialCreditHire;
    private  TrialPersonalInjury trialPersonalInjury;
    private  TrialRoadTrafficAccident trialRoadTrafficAccident;
    private  TrialHousingDisrepair trialHousingDisrepair;
    private  List<Element<TrialHearingAddNewDirectionsDJ>> trialHearingAddNewDirectionsDJ;
    private  LocationRefData hearingLocation;
    private  LocationRefData caseManagementLocation;
    private  SdoDJR2TrialCreditHire sdoDJR2TrialCreditHire;

    //additional data for hearings
    private  boolean disposalHearingDisclosureOfDocumentsDJAddSection;
    private  boolean disposalHearingWitnessOfFactDJAddSection;
    private  boolean disposalHearingMedicalEvidenceDJAddSection;
    private  boolean disposalHearingQuestionsToExpertsDJAddSection;
    private  boolean disposalHearingSchedulesOfLossDJAddSection;
    private  boolean disposalHearingFinalDisposalHearingDJAddSection;
    private  boolean disposalHearingBundleDJAddSection;
    private  boolean disposalHearingClaimSettlingAddSection;
    private  boolean disposalHearingCostsAddSection;

    private  String typeBundleInfo;
    private  String disposalHearingTime;
    private  DisposalHearingMethodDJ disposalHearingMethodDJ;
    private  String disposalHearingAttendance;
    private  String courtLocation;
    private  String telephoneOrganisedBy;
    private  String videoConferenceOrganisedBy;

    //additional data for trial
    private  boolean trialHearingDisputeAddSection;
    private  boolean trialHearingVariationsAddSection;
    private  boolean trialHearingSettlementAddSection;
    private  boolean trialHearingDisclosureOfDocumentsDJAddSection;
    private  boolean trialHearingWitnessOfFactDJAddSection;
    private  boolean trialHearingSchedulesOfLossDJAddSection;
    private  boolean trialHearingCostsAddSection;
    private  boolean trialHearingTrialDJAddSection;
    private  boolean trialBuildingDisputeAddSection;
    private  boolean trialClinicalNegligenceAddSection;
    private  boolean trialCreditHireAddSection;
    private  boolean trialEmployerLiabilityAddSection;
    private  boolean trialPersonalInjuryAddSection;
    private  boolean trialRoadTrafficAccidentAddSection;
    private  boolean trialHousingDisrepairAddSection;
    private  boolean trialHearingMethodInPersonAddSection;
    private  DisposalHearingMethodDJ trialHearingMethodDJ;
    private  String trialHearingLocation;
    private  boolean sdoDJR2TrialCreditHireAddSection;
    private  boolean sdoDJR2TrialCreditHireDetailsAddSection;

    //hnl fields
    private  DisposalHearingOrderMadeWithoutHearingDJ disposalHearingOrderMadeWithoutHearingDJ;
    private DisposalHearingFinalDisposalHearingTimeDJ disposalHearingFinalDisposalHearingTimeDJ;
    private String disposalHearingTimeEstimateDJ;

    //hnl fields for trial
    private TrialHearingTimeDJ trialHearingTimeDJ;
    private  boolean disposalHearingDateToToggle;
    private TrialOrderMadeWithoutHearingDJ trialOrderMadeWithoutHearingDJ;
    private String trialHearingTimeEstimateDJ;
    private  boolean writtenByJudge;

    //sdoR2 fields
    private  boolean hasDisposalHearingWelshSectionDJ;
    private  boolean hasTrialHearingWelshSectionDJ;
    private  String welshLanguageDescriptionDJ;

}
