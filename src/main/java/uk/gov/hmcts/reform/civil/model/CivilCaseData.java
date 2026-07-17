package uk.gov.hmcts.reform.civil.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dj.DisposalHearingMethodDJ;
import uk.gov.hmcts.reform.civil.enums.dj.HearingMethodTelephoneHearingDJ;
import uk.gov.hmcts.reform.civil.enums.dj.HearingMethodVideoConferenceDJ;
import uk.gov.hmcts.reform.civil.enums.sdo.ClaimsTrack;
import uk.gov.hmcts.reform.civil.enums.sdo.DisposalHearingMethod;
import uk.gov.hmcts.reform.civil.enums.sdo.DisposalHearingMethodTelephoneHearing;
import uk.gov.hmcts.reform.civil.enums.sdo.DisposalHearingMethodVideoConferenceHearing;
import uk.gov.hmcts.reform.civil.enums.sdo.FastTrack;
import uk.gov.hmcts.reform.civil.enums.sdo.FastTrackMethod;
import uk.gov.hmcts.reform.civil.enums.sdo.FastTrackMethodTelephoneHearing;
import uk.gov.hmcts.reform.civil.enums.sdo.FastTrackMethodVideoConferenceHearing;
import uk.gov.hmcts.reform.civil.enums.sdo.OrderDetailsPagesSectionsToggle;
import uk.gov.hmcts.reform.civil.enums.sdo.OrderType;
import uk.gov.hmcts.reform.civil.enums.sdo.SmallClaimsMethod;
import uk.gov.hmcts.reform.civil.enums.sdo.SmallClaimsMethodTelephoneHearing;
import uk.gov.hmcts.reform.civil.enums.sdo.SmallClaimsMethodVideoConferenceHearing;
import uk.gov.hmcts.reform.civil.enums.sdo.SmallTrack;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
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
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialEmployersLiability;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialHearingAddNewDirectionsDJ;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialHearingDisclosureOfDocuments;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialHearingJudgesRecital;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialHearingNotes;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialHearingSchedulesOfLoss;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialHearingTrial;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialHearingWitnessOfFact;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialHousingDisrepair;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialPersonalInjury;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialPPI;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialRoadTrafficAccident;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingAddNewDirections;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingBundle;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingDisclosureOfDocuments;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingFinalDisposalHearing;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingFinalDisposalHearingTimeDJ;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingHearingNotesDJ;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingHearingTime;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingJudgementDeductionValue;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingJudgesRecital;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingMedicalEvidence;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingNotes;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingOrderMadeWithoutHearingDJ;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingQuestionsToExperts;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingSchedulesOfLoss;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingStandardDisposalOrder;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingWitnessOfFact;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalOrderWithoutHearing;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackAddNewDirections;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackBuildingDispute;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackClinicalNegligence;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackCreditHire;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackDisclosureOfDocuments;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackHearingNotes;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackHearingTime;
import uk.gov.hmcts.reform.civil.model.sdo.HousingDisrepair;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackJudgementDeductionValue;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackJudgesRecital;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackNotes;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackOrderWithoutJudgement;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackPersonalInjury;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackRoadTrafficAccident;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackSchedulesOfLoss;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackTrial;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackWitnessOfFact;
import uk.gov.hmcts.reform.civil.model.sdo.JudgementSum;
import uk.gov.hmcts.reform.civil.model.sdo.ReasonNotSuitableSDO;
import uk.gov.hmcts.reform.civil.model.sdo.SmallClaimsAddNewDirections;
import uk.gov.hmcts.reform.civil.model.sdo.SmallClaimsCreditHire;
import uk.gov.hmcts.reform.civil.model.sdo.SmallClaimsDocuments;
import uk.gov.hmcts.reform.civil.model.sdo.SmallClaimsFlightDelay;
import uk.gov.hmcts.reform.civil.model.sdo.SmallClaimsHearing;
import uk.gov.hmcts.reform.civil.model.sdo.SmallClaimsJudgementDeductionValue;
import uk.gov.hmcts.reform.civil.model.sdo.SmallClaimsJudgesRecital;
import uk.gov.hmcts.reform.civil.model.sdo.SmallClaimsNotes;
import uk.gov.hmcts.reform.civil.model.sdo.SmallClaimsRoadTrafficAccident;
import uk.gov.hmcts.reform.civil.model.sdo.SmallClaimsWitnessStatement;
import uk.gov.hmcts.reform.civil.model.sdo.TrialHearingHearingNotesDJ;
import uk.gov.hmcts.reform.civil.model.sdo.TrialHearingTimeDJ;
import uk.gov.hmcts.reform.civil.model.sdo.TrialOrderMadeWithoutHearingDJ;

import java.util.ArrayList;
import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.reform.civil.ccd.access.CITIZENCLAIMANTPROFILECruPlus4RolesPrbgzjAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilSystemupdateCruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilAdminCruCaseworkerCivilSystemFieldReaderRAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilSolicitorJudgeProfileCruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilSystemupdateCrudAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilSystemFieldReaderRJudgeProfileCruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.JudgeProfileCruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilSystemupdateRAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilSolicitorCrudCaseworkerCivilSystemFieldReaderRAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CITIZENCLAIMANTPROFILECaseworkerCivilAdminCrudAccess;
import uk.gov.hmcts.reform.civil.ccd.access.JudgeProfileCrudAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CITIZENCLAIMANTPROFILECruCaseworkerCivilSolicitorCuAccess;
import uk.gov.hmcts.reform.civil.ccd.access.DefaultAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.GSProfileRAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilAdminCruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilSystemFieldReaderRPlus2RolesGiyvprAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorRAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CITIZENCLAIMANTPROFILECITIZENDEFENDANTPROFILERAccess;
import uk.gov.hmcts.reform.civil.ccd.access.JudgeProfileLegalAdviserCruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CITIZENDEFENDANTPROFILECruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilAdminCruCaseworkerCivilSolicitorRAccess;

@Accessors(chain = true)
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(callSuper = false)
@Data
public class CivilCaseData extends BaseCaseData implements MappableObject {

    //default judgement SDO fields for trial/fast track
    @CCD(
            label = "Judge's recital",
            searchable = false,
            access = {CITIZENCLAIMANTPROFILECruPlus4RolesPrbgzjAccess.class}
    )
    private TrialHearingJudgesRecital trialHearingJudgesRecitalDJ;
    @CCD(
            label = "Disclosure of documents",
            searchable = false,
            access = {CITIZENCLAIMANTPROFILECruPlus4RolesPrbgzjAccess.class, CaseworkerCivilSystemupdateCruAccess.class}
    )
    private TrialHearingDisclosureOfDocuments trialHearingDisclosureOfDocumentsDJ;
    @CCD(
            label = "Witnesses of fact",
            searchable = false,
            access = {CITIZENCLAIMANTPROFILECruPlus4RolesPrbgzjAccess.class}
    )
    private TrialHearingWitnessOfFact trialHearingWitnessOfFactDJ;
    @CCD(
            label = "Schedule of loss",
            searchable = false,
            access = {CITIZENCLAIMANTPROFILECruPlus4RolesPrbgzjAccess.class}
    )
    private TrialHearingSchedulesOfLoss trialHearingSchedulesOfLossDJ;
    @CCD(label = "Trial", searchable = false, access = {CITIZENCLAIMANTPROFILECruPlus4RolesPrbgzjAccess.class})
    private TrialHearingTrial trialHearingTrialDJ;
    @CCD(label = "Hearing time", searchable = false, access = {CITIZENCLAIMANTPROFILECruPlus4RolesPrbgzjAccess.class})
    private TrialHearingTimeDJ trialHearingTimeDJ;
    @CCD(label = "Important", searchable = false, access = {CITIZENCLAIMANTPROFILECruPlus4RolesPrbgzjAccess.class})
    private TrialHearingNotes trialHearingNotesDJ;
    @CCD(label = "Hearing notes", searchable = false, access = {CITIZENCLAIMANTPROFILECruPlus4RolesPrbgzjAccess.class})
    private TrialHearingHearingNotesDJ trialHearingHearingNotesDJ;
    @CCD(
            label = "Important notes",
            searchable = false,
            access = {CITIZENCLAIMANTPROFILECruPlus4RolesPrbgzjAccess.class}
    )
    private TrialOrderMadeWithoutHearingDJ trialOrderMadeWithoutHearingDJ;
    @CCD(
            label = "Building dispute",
            searchable = false,
            access = {CITIZENCLAIMANTPROFILECruPlus4RolesPrbgzjAccess.class}
    )
    private TrialBuildingDispute trialBuildingDispute;
    @CCD(
            label = "Clinical negligence",
            searchable = false,
            access = {CITIZENCLAIMANTPROFILECruPlus4RolesPrbgzjAccess.class}
    )
    private TrialClinicalNegligence trialClinicalNegligence;
    @CCD(
            label = "Credit hire",
            searchable = false,
            access = {CaseworkerCivilAdminCruCaseworkerCivilSystemFieldReaderRAccess.class, CaseworkerCivilSolicitorJudgeProfileCruAccess.class}
    )
    private TrialCreditHire trialCreditHire;
    @CCD(
            label = "Expert evidence",
            searchable = false,
            access = {CITIZENCLAIMANTPROFILECruPlus4RolesPrbgzjAccess.class}
    )
    private TrialPersonalInjury trialPersonalInjury;
    @CCD(
            label = "Road traffic accident",
            searchable = false,
            access = {CITIZENCLAIMANTPROFILECruPlus4RolesPrbgzjAccess.class}
    )
    private TrialRoadTrafficAccident trialRoadTrafficAccident;
    @CCD(
            label = "Employer's liability",
            searchable = false,
            access = {CITIZENCLAIMANTPROFILECruPlus4RolesPrbgzjAccess.class}
    )
    private TrialEmployersLiability trialEmployersLiability;
    @CCD(
            label = "Housing Disrepair",
            searchable = false,
            access = {CITIZENCLAIMANTPROFILECruPlus4RolesPrbgzjAccess.class, CaseworkerCivilSystemupdateCrudAccess.class}
    )
    private TrialHousingDisrepair trialHousingDisrepair;
    @CCD(
            label = "Payment Protection Insurance (PPI)",
            searchable = false,
            access = {CITIZENCLAIMANTPROFILECruPlus4RolesPrbgzjAccess.class}
    )
    private TrialPPI trialPPI;
    @CCD(label = " ", searchable = false, access = {CITIZENCLAIMANTPROFILECruPlus4RolesPrbgzjAccess.class})
    private DisposalHearingMethodDJ trialHearingMethodDJ;
    @CCD(
            label = "Who will arrange the call? ",
            searchable = false,
            access = {CITIZENCLAIMANTPROFILECruPlus4RolesPrbgzjAccess.class}
    )
    private HearingMethodTelephoneHearingDJ trialHearingMethodTelephoneHearingDJ;
    @CCD(
            label = "Who will arrange the video conference? ",
            searchable = false,
            access = {CITIZENCLAIMANTPROFILECruPlus4RolesPrbgzjAccess.class}
    )
    private HearingMethodVideoConferenceDJ trialHearingMethodVideoConferenceHearingDJ;

    //default judgement SDO fields for disposal
    @CCD(label = " ", searchable = false, access = {CITIZENCLAIMANTPROFILECruPlus4RolesPrbgzjAccess.class})
    private DisposalHearingJudgesRecitalDJ disposalHearingJudgesRecitalDJ;
    @CCD(label = " ", searchable = false, access = {CITIZENCLAIMANTPROFILECruPlus4RolesPrbgzjAccess.class})
    private DisposalHearingDisclosureOfDocumentsDJ disposalHearingDisclosureOfDocumentsDJ;
    @CCD(
            label = "Witnesses of Fact",
            searchable = false,
            access = {CITIZENCLAIMANTPROFILECruPlus4RolesPrbgzjAccess.class}
    )
    private DisposalHearingWitnessOfFactDJ disposalHearingWitnessOfFactDJ;
    @CCD(
            label = "Expert evidence",
            searchable = false,
            access = {CITIZENCLAIMANTPROFILECruPlus4RolesPrbgzjAccess.class}
    )
    private DisposalHearingMedicalEvidenceDJ disposalHearingMedicalEvidenceDJ;
    @CCD(
            label = "Questions to experts",
            searchable = false,
            access = {CITIZENCLAIMANTPROFILECruPlus4RolesPrbgzjAccess.class}
    )
    private DisposalHearingQuestionsToExpertsDJ disposalHearingQuestionsToExpertsDJ;
    @CCD(
            label = "Schedules or counter-schedules of loss",
            searchable = false,
            access = {CITIZENCLAIMANTPROFILECruPlus4RolesPrbgzjAccess.class}
    )
    private DisposalHearingSchedulesOfLossDJ disposalHearingSchedulesOfLossDJ;
    @CCD(
            label = "Final disposal hearing",
            searchable = false,
            access = {CITIZENCLAIMANTPROFILECruPlus4RolesPrbgzjAccess.class}
    )
    private DisposalHearingFinalDisposalHearingDJ disposalHearingFinalDisposalHearingDJ;
    @CCD(
            label = "Hearing time",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruAccess.class}
    )
    private DisposalHearingFinalDisposalHearingTimeDJ disposalHearingFinalDisposalHearingTimeDJ;
    @CCD(label = "Notes", searchable = false, access = {CITIZENCLAIMANTPROFILECruPlus4RolesPrbgzjAccess.class})
    private DisposalHearingNotesDJ disposalHearingNotesDJ;
    @CCD(
            label = "Hearing notes",
            searchable = false,
            access = {CaseworkerCivilAdminCruCaseworkerCivilSystemFieldReaderRAccess.class, JudgeProfileCruAccess.class}
    )
    private DisposalHearingHearingNotesDJ disposalHearingHearingNotesDJ;
    @CCD(
            label = "Important notes",
            searchable = false,
            access = {CaseworkerCivilAdminCruCaseworkerCivilSystemFieldReaderRAccess.class, JudgeProfileCruAccess.class}
    )
    private DisposalHearingOrderMadeWithoutHearingDJ disposalHearingOrderMadeWithoutHearingDJ;
    @CCD(label = " ", searchable = false, access = {CITIZENCLAIMANTPROFILECruPlus4RolesPrbgzjAccess.class})
    private DisposalHearingMethodDJ disposalHearingMethodDJ;
    @CCD(
            label = "Hearing location",
            searchable = false,
            typeOverride = FieldType.DynamicList,
            access = {CITIZENCLAIMANTPROFILECruPlus4RolesPrbgzjAccess.class}
    )
    private DynamicList trialHearingMethodInPersonDJ;
    @CCD(
            label = "Hearing location ",
            searchable = false,
            typeOverride = FieldType.DynamicList,
            access = {CITIZENCLAIMANTPROFILECruPlus4RolesPrbgzjAccess.class}
    )
    private DynamicList disposalHearingMethodInPersonDJ;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.DynamicRadioList,
            access = {CITIZENCLAIMANTPROFILECruPlus4RolesPrbgzjAccess.class, CaseworkerCivilSystemupdateRAccess.class}
    )
    private  DynamicList hearingMethodValuesDisposalHearingDJ;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.DynamicRadioList,
            access = {CITIZENCLAIMANTPROFILECruPlus4RolesPrbgzjAccess.class, CaseworkerCivilSystemupdateRAccess.class}
    )
    private  DynamicList hearingMethodValuesTrialHearingDJ;
    @CCD(
            label = "Add a new direction",
            searchable = false,
            access = {CaseworkerCivilSolicitorCrudCaseworkerCivilSystemFieldReaderRAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilAdminCrudAccess.class, JudgeProfileCrudAccess.class}
    )
    private List<Element<DisposalHearingAddNewDirectionsDJ>> disposalHearingAddNewDirectionsDJ;
    @CCD(
            label = "Add a new direction",
            searchable = false,
            access = {CaseworkerCivilSolicitorCrudCaseworkerCivilSystemFieldReaderRAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilAdminCrudAccess.class, JudgeProfileCrudAccess.class}
    )
    private List<Element<TrialHearingAddNewDirectionsDJ>> trialHearingAddNewDirectionsDJ;
    @CCD(
            label = "Who will arrange the call? ",
            searchable = false,
            access = {CITIZENCLAIMANTPROFILECruPlus4RolesPrbgzjAccess.class}
    )
    private HearingMethodTelephoneHearingDJ disposalHearingMethodTelephoneHearingDJ;
    @CCD(
            label = "Who will arrange the video conference? ",
            searchable = false,
            access = {CITIZENCLAIMANTPROFILECruPlus4RolesPrbgzjAccess.class}
    )
    private HearingMethodVideoConferenceDJ disposalHearingMethodVideoConferenceHearingDJ;
    @CCD(
            label = "System generated Case Documents",
            searchable = false,
            access = {CaseworkerCivilAdminCruCaseworkerCivilSystemFieldReaderRAccess.class, CITIZENCLAIMANTPROFILECruCaseworkerCivilSolicitorCuAccess.class, JudgeProfileCruAccess.class}
    )
    @Builder.Default
    private  List<Element<CaseDocument>> orderSDODocumentDJCollection = new ArrayList<>();
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "HearingSelection",
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, CaseworkerCivilSystemupdateRAccess.class, JudgeProfileCruAccess.class}
    )
    private String caseManagementOrderSelection;
    @CCD(
            label = "View directions order",
            searchable = false,
            access = {CITIZENCLAIMANTPROFILECruPlus4RolesPrbgzjAccess.class, GSProfileRAccess.class, CaseworkerCivilSystemupdateCruAccess.class}
    )
    private Document orderSDODocumentDJ;

    // CreateSdo fields
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
    )
    private  JudgementSum drawDirectionsOrder;
    @CCD(
            label = "Judge's recital",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
    )
    private DisposalHearingJudgesRecital disposalHearingJudgesRecital;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
    )
    private DisposalHearingJudgementDeductionValue disposalHearingJudgementDeductionValue;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
    )
    private DisposalHearingDisclosureOfDocuments disposalHearingDisclosureOfDocuments;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
    )
    private DisposalHearingWitnessOfFact disposalHearingWitnessOfFact;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
    )
    private DisposalHearingMedicalEvidence disposalHearingMedicalEvidence;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
    )
    private DisposalHearingQuestionsToExperts disposalHearingQuestionsToExperts;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
    )
    private DisposalHearingSchedulesOfLoss disposalHearingSchedulesOfLoss;
    @CCD(ignore = true)
    private DisposalHearingStandardDisposalOrder disposalHearingStandardDisposalOrder;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
    )
    private DisposalHearingFinalDisposalHearing disposalHearingFinalDisposalHearing;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, CaseworkerCivilAdminCruAccess.class}
    )
    private DisposalHearingHearingTime disposalHearingHearingTime;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
    )
    private DisposalHearingBundle disposalHearingBundle;
    @CCD(
            label = " ",
            searchable = false,
            access = {CITIZENCLAIMANTPROFILECruPlus4RolesPrbgzjAccess.class, CaseworkerCivilSystemupdateCruAccess.class}
    )
    private DisposalHearingBundleDJ disposalHearingBundleDJ;
    @CCD(
            label = "Notes",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
    )
    private DisposalHearingNotes disposalHearingNotes;
    @CCD(
            label = "This is only seen by the listing officer",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, CaseworkerCivilAdminCruAccess.class}
    )
    private String disposalHearingHearingNotes;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, CaseworkerCivilAdminCruAccess.class}
    )
    private DisposalOrderWithoutHearing disposalOrderWithoutHearing;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
    )
    private  DisposalHearingMethod disposalHearingMethod;
    @CCD(
            label = "Who will arrange the telephone hearing? ",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
    )
    private  DisposalHearingMethodTelephoneHearing disposalHearingMethodTelephoneHearing;
    @CCD(
            label = "Who will arrange the video conference? ",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
    )
    private  DisposalHearingMethodVideoConferenceHearing disposalHearingMethodVideoConferenceHearing;
    @CCD(
            label = "Add a new direction",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRPlus2RolesGiyvprAccess.class}
    )
    private  List<Element<DisposalHearingAddNewDirections>> disposalHearingAddNewDirections;
    @CCD(
            label = "Select hearing location ",
            searchable = false,
            typeOverride = FieldType.DynamicList,
            typeParameterOverride = "DisposalHearingMethodInPerson",
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
    )
    private  DynamicList disposalHearingMethodInPerson;
    @CCD(
            label = "This hearing will take place at:",
            searchable = false,
            typeOverride = FieldType.DynamicList,
            typeParameterOverride = "FastTrackMethodInPerson",
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class, CaseworkerCivilSystemupdateCruAccess.class}
    )
    private  DynamicList fastTrackMethodInPerson;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.DynamicRadioList,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class, CaseworkerCivilSystemupdateRAccess.class}
    )
    private  DynamicList hearingMethodValuesFastTrack;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.DynamicRadioList,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class, CaseworkerCivilSystemupdateRAccess.class}
    )
    private  DynamicList hearingMethodValuesDisposalHearing;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.DynamicRadioList,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class, CaseworkerCivilSystemupdateRAccess.class}
    )
    private  DynamicList hearingMethodValuesSmallClaims;
    @CCD(
            label = "This hearing will take place at:",
            searchable = false,
            typeOverride = FieldType.DynamicList,
            typeParameterOverride = "SmallClaimsMethodInPerson",
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
    )
    private  DynamicList smallClaimsMethodInPerson;
    @CCD(ignore = true)
    private  DynamicList hearingMethod;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
    )
    private  YesOrNo drawDirectionsOrderRequired;
    @CCD(
            label = "Do you want to allocate this claim to the small claims track?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
    )
    private  YesOrNo drawDirectionsOrderSmallClaims;
    @CCD(
            label = "What track are you allocating the claim to?",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorRAccess.class, CaseworkerCivilSystemupdateCruAccess.class}
    )
    private  ClaimsTrack claimsTrack;
    @CCD(
            label = "What order would you like to make?",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class, CaseworkerCivilSystemupdateRAccess.class}
    )
    private  OrderType orderType;
    @CCD(
            label = "Building dispute",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
    )
    private FastTrackBuildingDispute fastTrackBuildingDispute;
    @CCD(
            label = "Clinical negligence",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
    )
    private FastTrackClinicalNegligence fastTrackClinicalNegligence;
    @CCD(
            label = "Credit hire",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
    )
    private FastTrackCreditHire fastTrackCreditHire;
    @CCD(
            label = "Housing Disrepair",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class, CaseworkerCivilSystemupdateCrudAccess.class}
    )
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private HousingDisrepair fastTrackHousingDisrepair;
    @CCD(
            label = "Expert Evidence",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
    )
    private FastTrackPersonalInjury fastTrackPersonalInjury;
    @CCD(
            label = "Road traffic accident",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
    )
    private FastTrackRoadTrafficAccident fastTrackRoadTrafficAccident;
    @CCD(
            label = "Judge's recital",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
    )
    private FastTrackJudgesRecital fastTrackJudgesRecital;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
    )
    private FastTrackJudgementDeductionValue fastTrackJudgementDeductionValue;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class, CaseworkerCivilSystemupdateCruAccess.class}
    )
    private FastTrackDisclosureOfDocuments fastTrackDisclosureOfDocuments;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
    )
    private FastTrackWitnessOfFact fastTrackWitnessOfFact;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
    )
    private FastTrackSchedulesOfLoss fastTrackSchedulesOfLoss;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
    )
    private FastTrackTrial fastTrackTrial;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class, CITIZENCLAIMANTPROFILECITIZENDEFENDANTPROFILERAccess.class}
    )
    private FastTrackHearingTime fastTrackHearingTime;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
    )
    private FastTrackNotes fastTrackNotes;
    @CCD(
            label = "Hearing notes",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
    )
    private FastTrackHearingNotes fastTrackHearingNotes;
    @CCD(
            label = "Important notes",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
    )
    private FastTrackOrderWithoutJudgement fastTrackOrderWithoutJudgement;
    @CCD(
            label = "Select additional directions for Fast Track, if any ",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
    )
    private  List<FastTrack> fastClaims;
    @CCD(
            label = "Select additional directions for Fast Track, if any ",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
    )
    private  List<FastTrack> trialAdditionalDirectionsForFastTrack;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
    )
    private  FastTrackMethod fastTrackMethod;
    @CCD(
            label = "Who will arrange the telephone hearing? ",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
    )
    private  FastTrackMethodTelephoneHearing fastTrackMethodTelephoneHearing;
    @CCD(
            label = "Who will arrange the video conference? ",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
    )
    private  FastTrackMethodVideoConferenceHearing fastTrackMethodVideoConferenceHearing;
    @CCD(
            label = "Add a new direction",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRPlus2RolesGiyvprAccess.class}
    )
    private  List<Element<FastTrackAddNewDirections>> fastTrackAddNewDirections;
    @CCD(
            label = "Credit hire",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class, CaseworkerCivilSystemupdateCruAccess.class}
    )
    private SmallClaimsCreditHire smallClaimsCreditHire;
    @CCD(
            label = "Road traffic accident ",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
    )
    private SmallClaimsRoadTrafficAccident smallClaimsRoadTrafficAccident;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class, CaseworkerCivilSystemupdateCruAccess.class}
    )
    private SmallClaimsDocuments smallClaimsDocuments;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class, CITIZENCLAIMANTPROFILECITIZENDEFENDANTPROFILERAccess.class}
    )
    private SmallClaimsHearing smallClaimsHearing;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
    )
    private SmallClaimsJudgementDeductionValue smallClaimsJudgementDeductionValue;
    @CCD(
            label = "Judge's recital",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
    )
    private SmallClaimsJudgesRecital smallClaimsJudgesRecital;
    @CCD(
            label = "Important notes",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
    )
    private SmallClaimsNotes smallClaimsNotes;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
    )
    private SmallClaimsWitnessStatement smallClaimsWitnessStatement;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
    )
    private SmallClaimsFlightDelay smallClaimsFlightDelay;
    @CCD(
            label = "Housing Disrepair",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
    )
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private HousingDisrepair smallClaimsHousingDisrepair;
    @CCD(
            label = "Hearing notes",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
    )
    private SDOHearingNotes sdoHearingNotes;
    @CCD(
            label = "Other reason",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
    )
    private ReasonNotSuitableSDO reasonNotSuitableSDO;
    @CCD(
            label = "Select additional directions for Small Claims Track, if any ",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
    )
    private  List<SmallTrack> smallClaims;
    @CCD(
            label = "Select additional directions for Small Claims Track, if any ",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
    )
    private  List<SmallTrack> drawDirectionsOrderSmallClaimsAdditionalDirections;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
    )
    private  SmallClaimsMethod smallClaimsMethod;
    @CCD(
            label = "Who will arrange the telephone hearing? ",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
    )
    private  SmallClaimsMethodTelephoneHearing smallClaimsMethodTelephoneHearing;
    @CCD(
            label = "Who will arrange the video conference? ",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
    )
    private  SmallClaimsMethodVideoConferenceHearing smallClaimsMethodVideoConferenceHearing;
    @CCD(
            label = "Add a new direction",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRPlus2RolesGiyvprAccess.class}
    )
    private  List<Element<SmallClaimsAddNewDirections>> smallClaimsAddNewDirections;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
    )
    private List<OrderDetailsPagesSectionsToggle> fastTrackAltDisputeResolutionToggle;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
    )
    private List<OrderDetailsPagesSectionsToggle> fastTrackVariationOfDirectionsToggle;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
    )
    private List<OrderDetailsPagesSectionsToggle> fastTrackSettlementToggle;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
    )
    private List<OrderDetailsPagesSectionsToggle> fastTrackDisclosureOfDocumentsToggle;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
    )
    private List<OrderDetailsPagesSectionsToggle> fastTrackWitnessOfFactToggle;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
    )
    private List<OrderDetailsPagesSectionsToggle> fastTrackSchedulesOfLossToggle;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
    )
    private List<OrderDetailsPagesSectionsToggle> fastTrackCostsToggle;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
    )
    private List<OrderDetailsPagesSectionsToggle> fastTrackTrialToggle;
    @CCD(label = " ", searchable = false, access = {JudgeProfileLegalAdviserCruAccess.class})
    private List<OrderDetailsPagesSectionsToggle> fastTrackTrialBundleToggle;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
    )
    private List<OrderDetailsPagesSectionsToggle> fastTrackMethodToggle;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
    )
    private List<OrderDetailsPagesSectionsToggle> disposalHearingDisclosureOfDocumentsToggle;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
    )
    private List<OrderDetailsPagesSectionsToggle> disposalHearingWitnessOfFactToggle;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
    )
    private List<OrderDetailsPagesSectionsToggle> disposalHearingMedicalEvidenceToggle;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
    )
    private List<OrderDetailsPagesSectionsToggle> disposalHearingQuestionsToExpertsToggle;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
    )
    private List<OrderDetailsPagesSectionsToggle> disposalHearingSchedulesOfLossToggle;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
    )
    private List<OrderDetailsPagesSectionsToggle> disposalHearingFinalDisposalHearingToggle;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
    )
    private List<OrderDetailsPagesSectionsToggle> disposalHearingMethodToggle;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
    )
    private List<OrderDetailsPagesSectionsToggle> disposalHearingBundleToggle;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
    )
    private List<OrderDetailsPagesSectionsToggle> disposalHearingClaimSettlingToggle;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
    )
    private List<OrderDetailsPagesSectionsToggle> disposalHearingCostsToggle;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
    )
    private List<OrderDetailsPagesSectionsToggle> smallClaimsHearingToggle;
    @CCD(
            label = "View directions order",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, CITIZENDEFENDANTPROFILECruAccess.class, CaseworkerCivilAdminCruAccess.class}
    )
    private CaseDocument sdoOrderDocument;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class, CaseworkerCivilAdminCruCaseworkerCivilSolicitorRAccess.class, CaseworkerCivilSystemupdateCrudAccess.class}
    )
    private  YesOrNo eaCourtLocation;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class, CaseworkerCivilAdminCruCaseworkerCivilSolicitorRAccess.class, CaseworkerCivilSystemupdateRAccess.class}
    )
    private  YesOrNo hmcEaCourtLocation;

    // sdo ui flags
    @CCD(
            label = "small claims flag",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
    )
    private  YesOrNo setSmallClaimsFlag;
    @CCD(
            label = "fast track flag",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
    )
    private  YesOrNo setFastTrackFlag;
    private  String eventDescriptionRTJ;
    private  String additionalInformationRTJ;

    //default judgement SDO R2 fields for trial
    @CCD(label = "Credit hire", searchable = false, access = {CITIZENCLAIMANTPROFILECruPlus4RolesPrbgzjAccess.class})
    private SdoDJR2TrialCreditHire sdoDJR2TrialCreditHire;
}
