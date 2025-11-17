package uk.gov.hmcts.reform.civil.model.docmosis.sdo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.sdo.DisposalHearingMethod;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingAddNewDirections;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingBundle;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingDisclosureOfDocuments;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingFinalDisposalHearing;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingJudgesRecital;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingMedicalEvidence;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingNotes;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingQuestionsToExperts;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingSchedulesOfLoss;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingHearingTime;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingWitnessOfFact;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalOrderWithoutHearing;
import uk.gov.hmcts.reform.civil.model.sdo.JudgementSum;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@EqualsAndHashCode
@ToString
@SuppressWarnings({"java:S1104", "java:S107", "java:S1192", "java:CPD"})
public class SdoDocumentFormDisposal implements MappableObject {

    private LocalDate currentDate;
    private String judgeName;

    @EqualsAndHashCode.Include
    private String caseNumber;

    private Party applicant1;

    private Party respondent1;
    private boolean hasApplicant2;

    private Party applicant2;
    private boolean hasRespondent2;

    private Party respondent2;
    private YesOrNo drawDirectionsOrderRequired;

    private JudgementSum drawDirectionsOrder;

    private DisposalHearingJudgesRecital disposalHearingJudgesRecital;
    private DisposalHearingDisclosureOfDocuments disposalHearingDisclosureOfDocuments;
    private DisposalHearingWitnessOfFact disposalHearingWitnessOfFact;
    private DisposalHearingMedicalEvidence disposalHearingMedicalEvidence;
    private DisposalHearingQuestionsToExperts disposalHearingQuestionsToExperts;
    private DisposalHearingSchedulesOfLoss disposalHearingSchedulesOfLoss;
    private DisposalHearingFinalDisposalHearing disposalHearingFinalDisposalHearing;
    private String disposalHearingFinalDisposalHearingTime;
    private DisposalOrderWithoutHearing disposalOrderWithoutHearing;
    private DisposalHearingHearingTime disposalHearingTime;
    private String disposalHearingTimeEstimate;

    private DisposalHearingMethod disposalHearingMethod;
    private DynamicList disposalHearingMethodInPerson;
    private String disposalHearingMethodTelephoneHearing;
    private String disposalHearingMethodVideoConferenceHearing;
    private LocationRefData hearingLocation;
    private LocationRefData caseManagementLocation;

    private DisposalHearingBundle disposalHearingBundle;
    private String disposalHearingBundleTypeText;

    private boolean hasNewDirections;
    private List<Element<DisposalHearingAddNewDirections>> disposalHearingAddNewDirections;

    private DisposalHearingNotes disposalHearingNotes;
    private String welshLanguageDescription;

    private boolean disposalHearingDisclosureOfDocumentsToggle;
    private boolean disposalHearingWitnessOfFactToggle;
    private boolean disposalHearingMedicalEvidenceToggle;
    private boolean disposalHearingQuestionsToExpertsToggle;
    private boolean disposalHearingSchedulesOfLossToggle;
    private boolean disposalHearingFinalDisposalHearingToggle;
    private boolean disposalHearingMethodToggle;
    private boolean disposalHearingBundleToggle;
    private boolean disposalHearingClaimSettlingToggle;
    private boolean disposalHearingCostsToggle;
    private boolean writtenByJudge;
    private boolean hasDisposalWelshToggle;

    @SuppressWarnings("unused")
    public boolean getDisposalHearingMethodToggle() {
        // SNI-5142
        return true;
    }
}
