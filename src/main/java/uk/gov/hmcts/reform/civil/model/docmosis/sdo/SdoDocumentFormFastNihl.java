package uk.gov.hmcts.reform.civil.model.docmosis.sdo;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
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
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
@SuppressWarnings("java:S1104") // Fields are managed by Lombok
public class SdoDocumentFormFastNihl implements MappableObject {

    /** Document metadata - current date. */
    private LocalDate currentDate;
    /** Document metadata - judge name. */
    private String judgeName;
    /** Document metadata - case number. */
    private String caseNumber;

    /** Primary parties. */
    private Party applicant1;
    private Party respondent1;

    /** Secondary parties and flags. */
    private boolean hasApplicant2;
    private Party applicant2;
    private boolean hasRespondent2;
    private Party respondent2;

    /** Directions order requirements. */
    private YesOrNo drawDirectionsOrderRequired;
    private JudgementSum drawDirectionsOrder;
    private ClaimsTrack claimsTrack;
    private List<FastTrack> fastClaims;
    private boolean writtenByJudge;
    private boolean hasAltDisputeResolution;
    private boolean hasVariationOfDirections;
    private boolean hasSettlement;
    private boolean hasDisclosureOfDocuments;
    private boolean hasWitnessOfFact;
    private boolean hasRestrictWitness;
    private boolean hasRestrictPages;
    private boolean hasExpertEvidence;
    private boolean hasAddendumReport;
    private boolean hasFurtherAudiogram;
    private boolean hasQuestionsOfClaimantExpert;
    private boolean hasPermissionFromENT;
    private boolean hasEvidenceFromAcousticEngineer;
    private boolean hasQuestionsToENTAfterReport;
    private boolean hasScheduleOfLoss;
    private boolean hasClaimForPecuniaryLoss;
    private boolean hasUploadDocuments;
    private boolean hasSdoTrial;
    private boolean hasNewDirections;
    private boolean hasSdoR2TrialWindow;
    private boolean hasSdoR2TrialPhysicalBundleParty;
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
    private boolean hasNihlWelshLangToggle;

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
    private FastTrackJudgesRecital sdoFastTrackJudgesRecital;
    private LocalDate sdoR2ImportantNotesDate;
    private SdoR2FastTrackAltDisputeResolution sdoAltDisputeResolution;
    private SdoR2VariationOfDirections sdoVariationOfDirections;
    private SdoR2Settlement sdoR2Settlement;
    private SdoR2DisclosureOfDocuments sdoR2DisclosureOfDocuments;
    private SdoR2ExpertEvidence sdoR2ExpertEvidence;
    private SdoR2WitnessOfFact sdoR2WitnessesOfFact;
    private LocationRefData hearingLocation;
    private LocationRefData caseManagementLocation;
    private String sdoTrialHearingTimeAllocated;
    private String sdoTrialMethodOfHearing;
    private String isApplicationToRelyOnFurther;
    private String sdoR2ImportantNotesTxt;
    private String physicalBundlePartyTxt;
    private String welshLanguageDescription;
}
