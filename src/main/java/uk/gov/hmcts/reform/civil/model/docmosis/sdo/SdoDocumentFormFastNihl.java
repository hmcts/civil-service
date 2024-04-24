package uk.gov.hmcts.reform.civil.model.docmosis.sdo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.sdo.ClaimsTrack;
import uk.gov.hmcts.reform.civil.enums.sdo.FastTrack;
import uk.gov.hmcts.reform.civil.enums.sdo.IncludeInOrderToggle;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackJudgesRecital;
import uk.gov.hmcts.reform.civil.model.sdo.JudgementSum;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2AddNewDirection;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2AddendumReport;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2DisclosureOfDocuments;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2EvidenceAcousticEngineer;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2ExpertEvidence;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2FastTrackAltDisputeResolution;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2FurtherAudiogram;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2PermissionToRelyOnExpert;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2QuestionsClaimantExpert;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2QuestionsToEntExpert;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2ScheduleOfLoss;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2Settlement;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2Trial;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2UploadOfDocuments;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2VariationOfDirections;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2WitnessOfFact;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class SdoDocumentFormFastNihl implements MappableObject {

    private final LocalDate currentDate;
    private final String judgeName;
    private final String caseNumber;
    private final Party applicant1;
    private final Party respondent1;
    private final boolean hasApplicant2;
    private final Party applicant2;
    private final boolean hasRespondent2;
    private final Party respondent2;
    private final YesOrNo drawDirectionsOrderRequired;
    private final JudgementSum drawDirectionsOrder;
    private final ClaimsTrack claimsTrack;
    private final List<FastTrack> fastClaims;
    private final boolean writtenByJudge;
    private final boolean hasAltDisputeResolution;
    private final boolean hasVariationOfDirections;
    private final boolean hasSettlement;
    private final boolean hasDisclosureOfDocuments;
    private final boolean hasWitnessOfFact;
    private final boolean hasRestrictWitness;
    private final boolean hasRestrictPages;
    private final boolean hasExpertEvidence;
    private final boolean hasAddendumReport;
    private final boolean hasFurtherAudiogram;
    private final boolean hasQuestionsOfClaimantExpert;
    private final boolean hasPermissionFromENT;
    private final boolean hasEvidenceFromAcousticEngineer;
    private final boolean hasQuestionsToENTAfterReport;
    private final boolean hasScheduleOfLoss;
    private final boolean hasClaimForPecuniaryLoss;
    private final boolean hasUploadDocuments;
    private final boolean hasSdoTrial;
    private final boolean hasNewDirections;
    private final boolean hasSdoR2TrialWindow;
    private final boolean hasSdoR2TrialPhysicalBundleParty;
    private List<IncludeInOrderToggle> sdoR2DisclosureOfDocumentsToggle;
    private List<IncludeInOrderToggle> sdoR2SeparatorWitnessesOfFactToggle;
    private List<IncludeInOrderToggle> sdoR2ScheduleOfLossToggle;
    private List<IncludeInOrderToggle>  sdoR2TrialToggle;
    private List<IncludeInOrderToggle> sdoR2SeparatorExpertEvidenceToggle;
    private List<IncludeInOrderToggle> sdoR2SeparatorAddendumReportToggle;
    private List<IncludeInOrderToggle> sdoR2SeparatorFurtherAudiogramToggle;
    private List<IncludeInOrderToggle> sdoR2SeparatorQuestionsClaimantExpertToggle;
    private List<IncludeInOrderToggle> sdoR2SeparatorPermissionToRelyOnExpertToggle;
    private List<IncludeInOrderToggle> sdoR2SeparatorEvidenceAcousticEngineerToggle;
    private List<IncludeInOrderToggle> sdoR2SeparatorQuestionsToEntExpertToggle;
    private List<IncludeInOrderToggle> sdoR2SeparatorUploadOfDocumentsToggle;
    private final boolean hasNihlWelshLangToggle;

    private SdoR2QuestionsToEntExpert sdoR2QuestionsToEntExpert;
    private SdoR2EvidenceAcousticEngineer sdoR2EvidenceAcousticEngineer;
    private SdoR2ScheduleOfLoss sdoR2ScheduleOfLoss;
    private List<Element<SdoR2AddNewDirection>> sdoR2AddNewDirection;
    private SdoR2Trial sdoR2Trial;
    private SdoR2AddendumReport sdoR2AddendumReport;
    private SdoR2QuestionsClaimantExpert sdoR2QuestionsClaimantExpert;
    private SdoR2FurtherAudiogram sdoR2FurtherAudiogram;
    private SdoR2PermissionToRelyOnExpert sdoR2PermissionToRelyOnExpert;
    private SdoR2UploadOfDocuments sdoR2UploadOfDocuments;
    private final FastTrackJudgesRecital sdoFastTrackJudgesRecital;
    private LocalDate sdoR2ImportantNotesDate;
    private SdoR2FastTrackAltDisputeResolution sdoAltDisputeResolution;
    private SdoR2VariationOfDirections sdoVariationOfDirections;
    private SdoR2Settlement sdoR2Settlement;
    private SdoR2DisclosureOfDocuments sdoR2DisclosureOfDocuments;
    private SdoR2ExpertEvidence sdoR2ExpertEvidence;
    private SdoR2WitnessOfFact sdoR2WitnessesOfFact;
    private final LocationRefData hearingLocation;
    private final LocationRefData caseManagementLocation;
    private String sdoTrialHearingTimeAllocated;
    private String sdoTrialMethodOfHearing;
    private final String isApplicationToRelyOnFurther;
    private String sdoR2ImportantNotesTxt;
    private String physicalBundlePartyTxt;
    private final String welshLanguageDescription;
}
