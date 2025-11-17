package uk.gov.hmcts.reform.civil.model.docmosis.sdo;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.reform.civil.enums.sdo.ClaimsTrack;
import uk.gov.hmcts.reform.civil.enums.sdo.FastTrack;
import uk.gov.hmcts.reform.civil.enums.sdo.IncludeInOrderToggle;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackJudgesRecital;
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

import java.time.LocalDate;
import java.util.List;

/**
 * Document form for Fast Track NIHL (Noise Induced Hearing Loss) claims.
 * This class represents the data structure for SDO documents in NIHL cases.
 */
@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@SuppressWarnings("java:S1104")
public class SdoDocumentFormFastNihl extends SdoDocumentFormBase implements MappableObject {

    private ClaimsTrack claimsTrack;
    private List<FastTrack> fastClaims;
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
    private String sdoTrialHearingTimeAllocated;
    private String sdoTrialMethodOfHearing;
    private String isApplicationToRelyOnFurther;
    private String sdoR2ImportantNotesTxt;
    private String physicalBundlePartyTxt;
}
