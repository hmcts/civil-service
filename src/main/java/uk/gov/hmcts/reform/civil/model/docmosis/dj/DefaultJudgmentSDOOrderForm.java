package uk.gov.hmcts.reform.civil.model.docmosis.dj;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.*;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingBundle;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingDisclosureOfDocuments;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingFinalDisposalHearing;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingJudgesRecital;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingMedicalEvidence;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingNotes;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingQuestionsToExperts;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingSchedulesOfLoss;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingStandardDisposalOrder;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingWitnessOfFact;

@Getter
@Builder
@AllArgsConstructor
@EqualsAndHashCode
public class DefaultJudgmentSDOOrderForm implements MappableObject {

    private final String caseNumber;
    private final String applicant;
    private final String respondent;

    //default judgement SDO fields for disposal
    private final DisposalHearingJudgesRecitalDJ disposalHearingJudgesRecitalDJ;
    private final DisposalHearingDisclosureOfDocumentsDJ disposalHearingDisclosureOfDocumentsDJ;
    private final DisposalHearingWitnessOfFactDJ disposalHearingWitnessOfFactDJ;
    private final DisposalHearingMedicalEvidenceDJ disposalHearingMedicalEvidenceDJ;
    private final DisposalHearingQuestionsToExpertsDJ disposalHearingQuestionsToExpertsDJ;
    private final DisposalHearingSchedulesOfLossDJ disposalHearingSchedulesOfLossDJ;
    private final DisposalHearingStandardDisposalOrderDJ disposalHearingStandardDisposalOrderDJ;
    private final DisposalHearingFinalDisposalHearingDJ disposalHearingFinalDisposalHearingDJ;
    private final DisposalHearingBundleDJ disposalHearingBundleDJ;
    private final DisposalHearingNotesDJ disposalHearingNotesDJ;

    //default judgement SDO fields for trial
    private final TrialHearingJudgesRecital trialHearingJudgesRecitalDJ;
    private final TrialHearingDisclosureOfDocuments trialHearingDisclosureOfDocumentsDJ;
    private final TrialHearingWitnessOfFact trialHearingWitnessOfFactDJ;
    private final TrialHearingSchedulesOfLoss trialHearingSchedulesOfLossDJ;
    private final TrialHearingTrial trialHearingTrialDJ;
    private final TrialHearingNotes trialHearingNotesDJ;
    private final TrialBuildingDispute trialBuildingDispute;
    private final TrialClinicalNegligence trialClinicalNegligence;
    private final TrialCreditHire trialCreditHire;
    private final TrialPersonalInjury trialPersonalInjury;
    private final TrialRoadTrafficAccident trialRoadTrafficAccident;
    private final TrialHousingDisrepair trialHousingDisrepair;

    //additional data for hearings
    private final boolean disposalHearingDisclosureOfDocumentsDJAddSection;
    private final boolean disposalHearingWitnessOfFactDJAddSection;
    private final boolean disposalHearingMedicalEvidenceDJAddSection;
    private final boolean disposalHearingQuestionsToExpertsDJAddSection;
    private final boolean disposalHearingSchedulesOfLossDJAddSection;
    private final boolean disposalHearingFinalDisposalHearingDJAddSection;
    private final boolean disposalHearingBundleDJAddSection;
    private final boolean disposalHearingClaimSettlingAddSection;
    private final boolean disposalHearingCostsAddSection;

    private final String typeBundleInfo;
    private final String disposalHearingTime;
    private final String disposalHearingMethod;

    //additional data for trial
    private final boolean trialHearingDisputeAddSection;
    private final boolean trialHearingVariationsAddSection;
    private final boolean trialHearingSettlementAddSection;
    private final boolean trialHearingDisclosureOfDocumentsDJAddSection;
    private final boolean trialHearingWitnessOfFactDJAddSection;
    private final boolean trialHearingSchedulesOfLossDJAddSection;
    private final boolean trialHearingCostsAddSection;
    private final boolean trialHearingTrialDJAddSection;
    private final boolean trialBuildingDisputeAddSection;
    private final boolean trialClinicalNegligenceAddSection;
    private final boolean trialCreditHireAddSection;
    private final boolean trialEmployerLiabilityAddSection;
    private final boolean trialPersonalInjuryAddSection;
    private final boolean trialRoadTrafficAccidentAddSection;
    private final boolean trialHousingDisrepairAddSection;
    private final boolean trialHearingMethodInPersonAddSection;

    private final String trialDays;
    private final String trialHearingMethod;
    private final String trialHearingLocation;
}
