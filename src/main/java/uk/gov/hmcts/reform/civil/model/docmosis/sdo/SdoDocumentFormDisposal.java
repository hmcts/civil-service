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
public class SdoDocumentFormDisposal implements MappableObject {

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

    private final DisposalHearingJudgesRecital disposalHearingJudgesRecital;
    private final DisposalHearingDisclosureOfDocuments disposalHearingDisclosureOfDocuments;
    private final DisposalHearingWitnessOfFact disposalHearingWitnessOfFact;
    private final DisposalHearingMedicalEvidence disposalHearingMedicalEvidence;
    private final DisposalHearingQuestionsToExperts disposalHearingQuestionsToExperts;
    private final DisposalHearingSchedulesOfLoss disposalHearingSchedulesOfLoss;
    private final DisposalHearingFinalDisposalHearing disposalHearingFinalDisposalHearing;
    private final String disposalHearingFinalDisposalHearingTime;
    private final DisposalOrderWithoutHearing disposalOrderWithoutHearing;
    private final DisposalHearingHearingTime disposalHearingTime;
    private final String disposalHearingTimeEstimate;

    private final DisposalHearingMethod disposalHearingMethod;
    private final DynamicList disposalHearingMethodInPerson;
    private final String disposalHearingMethodTelephoneHearing;
    private final String disposalHearingMethodVideoConferenceHearing;
    private final LocationRefData hearingLocation;
    private final LocationRefData caseManagementLocation;

    private final DisposalHearingBundle disposalHearingBundle;
    private final String disposalHearingBundleTypeText;

    private final boolean hasNewDirections;
    private final List<Element<DisposalHearingAddNewDirections>> disposalHearingAddNewDirections;

    private final DisposalHearingNotes disposalHearingNotes;

    private final boolean disposalHearingDisclosureOfDocumentsToggle;
    private final boolean disposalHearingWitnessOfFactToggle;
    private final boolean disposalHearingMedicalEvidenceToggle;
    private final boolean disposalHearingQuestionsToExpertsToggle;
    private final boolean disposalHearingSchedulesOfLossToggle;
    private final boolean disposalHearingFinalDisposalHearingToggle;
    private final boolean disposalHearingMethodToggle;
    private final boolean disposalHearingBundleToggle;
    private final boolean disposalHearingClaimSettlingToggle;
    private final boolean disposalHearingCostsToggle;
    private final boolean writtenByJudge;
}
