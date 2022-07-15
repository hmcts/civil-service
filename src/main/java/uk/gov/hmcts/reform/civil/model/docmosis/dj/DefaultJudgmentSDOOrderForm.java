package uk.gov.hmcts.reform.civil.model.docmosis.dj;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialBuildingDispute;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialClinicalNegligence;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialCreditHire;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialHearingDisclosureOfDocuments;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialHearingJudgesRecital;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialHearingNotes;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialHearingSchedulesOfLoss;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialHearingTrial;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialHearingWitnessOfFact;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialPersonalInjury;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialRoadTrafficAccident;
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
    private final DisposalHearingJudgesRecital disposalHearingJudgesRecitalDJ;
    private final DisposalHearingDisclosureOfDocuments disposalHearingDisclosureOfDocumentsDJ;
    private final DisposalHearingWitnessOfFact disposalHearingWitnessOfFactDJ;
    private final DisposalHearingMedicalEvidence disposalHearingMedicalEvidenceDJ;
    private final DisposalHearingQuestionsToExperts disposalHearingQuestionsToExpertsDJ;
    private final DisposalHearingSchedulesOfLoss disposalHearingSchedulesOfLossDJ;
    private final DisposalHearingStandardDisposalOrder disposalHearingStandardDisposalOrderDJ;
    private final DisposalHearingFinalDisposalHearing disposalHearingFinalDisposalHearingDJ;
    private final DisposalHearingBundle disposalHearingBundleDJ;
    private final DisposalHearingNotes disposalHearingNotesDJ;

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

    //additional data for hearings
    private final String disposalHearingDisclosureOfDocumentsDJAddSection;
    private final String disposalHearingWitnessOfFactDJAddSection;
    private final String disposalHearingMedicalEvidenceDJAddSection;
    private final String disposalHearingQuestionsToExpertsDJAddSection;
    private final String disposalHearingSchedulesOfLossDJAddSection;
    private final String disposalHearingFinalDisposalHearingDJAddSection;
    private final String disposalHearingBundleDJAddSection;
    private final String disposalHearingClaimSettlingAddSection;
    private final String disposalHearingCostsAddSection;

    private final String typeBundleInfo;
    private final String disposalHearingTime;
    private final String disposalHearingMethod;

    //additional data for trial
    private final String trialHearingDisputeAddSection;
    private final String trialHearingVariationsAddSection;
    private final String trialHearingSettlementAddSection;
    private final String trialHearingDisclosureOfDocumentsDJAddSection;
    private final String trialHearingWitnessOfFactDJAddSection;
    private final String trialHearingSchedulesOfLossDJAddSection;
    private final String trialHearingCostsAddSection;
    private final String trialHearingTrialDJAddSection;
    private final String trialBuildingDisputeAddSection;
    private final String trialClinicalNegligenceAddSection;
    private final String trialCreditHireAddSection;
    private final String trialEmployerLiabilityAddSection;
    private final String trialPersonalInjuryAddSection;
    private final String trialRoadTrafficAccidentAddSection;

    private final String trialDays;
    private final String trialHearingMethod;
}
