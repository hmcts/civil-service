package uk.gov.hmcts.reform.civil.model;

import lombok.Builder;
import lombok.Data;
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
import uk.gov.hmcts.reform.civil.model.defaultjudgment.*;
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
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackHousingDisrepair;
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

@SuperBuilder(toBuilder = true)
@Data
public class CaseDataCaseSdo implements MappableObject {

    //default judgement SDO fields for trial/fast track
    private TrialHearingJudgesRecital trialHearingJudgesRecitalDJ;
    private TrialHearingDisclosureOfDocuments trialHearingDisclosureOfDocumentsDJ;
    private TrialHearingWitnessOfFact trialHearingWitnessOfFactDJ;
    private TrialHearingSchedulesOfLoss trialHearingSchedulesOfLossDJ;
    private TrialHearingTrial trialHearingTrialDJ;
    private TrialHearingTimeDJ trialHearingTimeDJ;
    private TrialHearingNotes trialHearingNotesDJ;
    private TrialHearingHearingNotesDJ trialHearingHearingNotesDJ;
    private TrialOrderMadeWithoutHearingDJ trialOrderMadeWithoutHearingDJ;
    private TrialBuildingDispute trialBuildingDispute;
    private TrialClinicalNegligence trialClinicalNegligence;
    private TrialCreditHire trialCreditHire;
    private TrialPersonalInjury trialPersonalInjury;
    private TrialRoadTrafficAccident trialRoadTrafficAccident;
    private TrialEmployersLiability trialEmployersLiability;
    private TrialHousingDisrepair trialHousingDisrepair;
    private DisposalHearingMethodDJ trialHearingMethodDJ;
    private HearingMethodTelephoneHearingDJ trialHearingMethodTelephoneHearingDJ;
    private HearingMethodVideoConferenceDJ trialHearingMethodVideoConferenceHearingDJ;

    //default judgement SDO fields for disposal
    private DisposalHearingJudgesRecitalDJ disposalHearingJudgesRecitalDJ;
    private DisposalHearingDisclosureOfDocumentsDJ disposalHearingDisclosureOfDocumentsDJ;
    private DisposalHearingWitnessOfFactDJ disposalHearingWitnessOfFactDJ;
    private DisposalHearingMedicalEvidenceDJ disposalHearingMedicalEvidenceDJ;
    private DisposalHearingQuestionsToExpertsDJ disposalHearingQuestionsToExpertsDJ;
    private DisposalHearingSchedulesOfLossDJ disposalHearingSchedulesOfLossDJ;
    private DisposalHearingFinalDisposalHearingDJ disposalHearingFinalDisposalHearingDJ;
    private DisposalHearingFinalDisposalHearingTimeDJ disposalHearingFinalDisposalHearingTimeDJ;
    private DisposalHearingNotesDJ disposalHearingNotesDJ;
    private DisposalHearingHearingNotesDJ disposalHearingHearingNotesDJ;
    private DisposalHearingOrderMadeWithoutHearingDJ disposalHearingOrderMadeWithoutHearingDJ;
    private DisposalHearingMethodDJ disposalHearingMethodDJ;
    private DynamicList trialHearingMethodInPersonDJ;
    private DynamicList disposalHearingMethodInPersonDJ;
    private final DynamicList hearingMethodValuesDisposalHearingDJ;
    private final DynamicList hearingMethodValuesTrialHearingDJ;
    private List<Element<DisposalHearingAddNewDirectionsDJ>> disposalHearingAddNewDirectionsDJ;
    private List<Element<TrialHearingAddNewDirectionsDJ>> trialHearingAddNewDirectionsDJ;
    private HearingMethodTelephoneHearingDJ disposalHearingMethodTelephoneHearingDJ;
    private HearingMethodVideoConferenceDJ disposalHearingMethodVideoConferenceHearingDJ;
    @Builder.Default
    private final List<Element<CaseDocument>> orderSDODocumentDJCollection = new ArrayList<>();
    private String caseManagementOrderSelection;
    private Document orderSDODocumentDJ;

    // CreateSdo fields
    private final JudgementSum drawDirectionsOrder;
    private DisposalHearingJudgesRecital disposalHearingJudgesRecital;
    private DisposalHearingJudgementDeductionValue disposalHearingJudgementDeductionValue;
    private DisposalHearingDisclosureOfDocuments disposalHearingDisclosureOfDocuments;
    private DisposalHearingWitnessOfFact disposalHearingWitnessOfFact;
    private DisposalHearingMedicalEvidence disposalHearingMedicalEvidence;
    private DisposalHearingQuestionsToExperts disposalHearingQuestionsToExperts;
    private DisposalHearingSchedulesOfLoss disposalHearingSchedulesOfLoss;
    private DisposalHearingStandardDisposalOrder disposalHearingStandardDisposalOrder;
    private DisposalHearingFinalDisposalHearing disposalHearingFinalDisposalHearing;
    private DisposalHearingHearingTime disposalHearingHearingTime;
    private DisposalHearingBundle disposalHearingBundle;
    private DisposalHearingBundleDJ disposalHearingBundleDJ;
    private DisposalHearingNotes disposalHearingNotes;
    private String disposalHearingHearingNotes;
    private DisposalOrderWithoutHearing disposalOrderWithoutHearing;
    private final DisposalHearingMethod disposalHearingMethod;
    private final DisposalHearingMethodTelephoneHearing disposalHearingMethodTelephoneHearing;
    private final DisposalHearingMethodVideoConferenceHearing disposalHearingMethodVideoConferenceHearing;
    private final List<Element<DisposalHearingAddNewDirections>> disposalHearingAddNewDirections;
    private final DynamicList disposalHearingMethodInPerson;
    private final DynamicList fastTrackMethodInPerson;
    private final DynamicList hearingMethodValuesFastTrack;
    private final DynamicList hearingMethodValuesDisposalHearing;
    private final DynamicList hearingMethodValuesSmallClaims;
    private final DynamicList smallClaimsMethodInPerson;
    private final DynamicList hearingMethod;
    private final YesOrNo drawDirectionsOrderRequired;
    private final YesOrNo drawDirectionsOrderSmallClaims;
    private final ClaimsTrack claimsTrack;
    private final OrderType orderType;
    private FastTrackBuildingDispute fastTrackBuildingDispute;
    private FastTrackClinicalNegligence fastTrackClinicalNegligence;
    private FastTrackCreditHire fastTrackCreditHire;
    private FastTrackHousingDisrepair fastTrackHousingDisrepair;
    private FastTrackPersonalInjury fastTrackPersonalInjury;
    private FastTrackRoadTrafficAccident fastTrackRoadTrafficAccident;
    private FastTrackJudgesRecital fastTrackJudgesRecital;
    private FastTrackJudgementDeductionValue fastTrackJudgementDeductionValue;
    private FastTrackDisclosureOfDocuments fastTrackDisclosureOfDocuments;
    private FastTrackWitnessOfFact fastTrackWitnessOfFact;
    private FastTrackSchedulesOfLoss fastTrackSchedulesOfLoss;
    private FastTrackTrial fastTrackTrial;
    private FastTrackHearingTime fastTrackHearingTime;
    private FastTrackNotes fastTrackNotes;
    private FastTrackHearingNotes fastTrackHearingNotes;
    private FastTrackOrderWithoutJudgement fastTrackOrderWithoutJudgement;
    private final List<FastTrack> fastClaims;
    private final List<FastTrack> trialAdditionalDirectionsForFastTrack;
    private final FastTrackMethod fastTrackMethod;
    private final FastTrackMethodTelephoneHearing fastTrackMethodTelephoneHearing;
    private final FastTrackMethodVideoConferenceHearing fastTrackMethodVideoConferenceHearing;
    private final List<Element<FastTrackAddNewDirections>> fastTrackAddNewDirections;
    private SmallClaimsCreditHire smallClaimsCreditHire;
    private SmallClaimsRoadTrafficAccident smallClaimsRoadTrafficAccident;
    private SmallClaimsDocuments smallClaimsDocuments;
    private SmallClaimsHearing smallClaimsHearing;
    private SmallClaimsJudgementDeductionValue smallClaimsJudgementDeductionValue;
    private SmallClaimsJudgesRecital smallClaimsJudgesRecital;
    private SmallClaimsNotes smallClaimsNotes;
    private SmallClaimsWitnessStatement smallClaimsWitnessStatement;
    private SmallClaimsFlightDelay smallClaimsFlightDelay;
    private SDOHearingNotes sdoHearingNotes;
    private ReasonNotSuitableSDO reasonNotSuitableSDO;
    private final List<SmallTrack> smallClaims;
    private final List<SmallTrack> drawDirectionsOrderSmallClaimsAdditionalDirections;
    private final SmallClaimsMethod smallClaimsMethod;
    private final SmallClaimsMethodTelephoneHearing smallClaimsMethodTelephoneHearing;
    private final SmallClaimsMethodVideoConferenceHearing smallClaimsMethodVideoConferenceHearing;
    private final List<Element<SmallClaimsAddNewDirections>> smallClaimsAddNewDirections;
    private List<OrderDetailsPagesSectionsToggle> fastTrackAltDisputeResolutionToggle;
    private List<OrderDetailsPagesSectionsToggle> fastTrackVariationOfDirectionsToggle;
    private List<OrderDetailsPagesSectionsToggle> fastTrackSettlementToggle;
    private List<OrderDetailsPagesSectionsToggle> fastTrackDisclosureOfDocumentsToggle;
    private List<OrderDetailsPagesSectionsToggle> fastTrackWitnessOfFactToggle;
    private List<OrderDetailsPagesSectionsToggle> fastTrackSchedulesOfLossToggle;
    private List<OrderDetailsPagesSectionsToggle> fastTrackCostsToggle;
    private List<OrderDetailsPagesSectionsToggle> fastTrackTrialToggle;
    private List<OrderDetailsPagesSectionsToggle> fastTrackMethodToggle;
    private List<OrderDetailsPagesSectionsToggle> disposalHearingDisclosureOfDocumentsToggle;
    private List<OrderDetailsPagesSectionsToggle> disposalHearingWitnessOfFactToggle;
    private List<OrderDetailsPagesSectionsToggle> disposalHearingMedicalEvidenceToggle;
    private List<OrderDetailsPagesSectionsToggle> disposalHearingQuestionsToExpertsToggle;
    private List<OrderDetailsPagesSectionsToggle> disposalHearingSchedulesOfLossToggle;
    private List<OrderDetailsPagesSectionsToggle> disposalHearingFinalDisposalHearingToggle;
    private List<OrderDetailsPagesSectionsToggle> disposalHearingMethodToggle;
    private List<OrderDetailsPagesSectionsToggle> disposalHearingBundleToggle;
    private List<OrderDetailsPagesSectionsToggle> disposalHearingClaimSettlingToggle;
    private List<OrderDetailsPagesSectionsToggle> disposalHearingCostsToggle;
    private List<OrderDetailsPagesSectionsToggle> smallClaimsHearingToggle;
    private CaseDocument sdoOrderDocument;
    private final YesOrNo eaCourtLocation;

    // sdo ui flags
    private final YesOrNo setSmallClaimsFlag;
    private final YesOrNo setFastTrackFlag;
    private final String eventDescriptionRTJ;
    private final String additionalInformationRTJ;

    //default judgement SDO R2 fields for trial
    private SdoDJR2TrialCreditHire sdoDJR2TrialCreditHire;
}
