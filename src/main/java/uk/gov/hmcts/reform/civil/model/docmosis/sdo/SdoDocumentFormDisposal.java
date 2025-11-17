package uk.gov.hmcts.reform.civil.model.docmosis.sdo;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.reform.civil.enums.sdo.DisposalHearingMethod;
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

import java.util.List;

@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@SuppressWarnings({"java:S1104", "java:S107"})
public class SdoDocumentFormDisposal extends SdoDocumentFormBase implements MappableObject {

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

    private DisposalHearingBundle disposalHearingBundle;
    private String disposalHearingBundleTypeText;

    private List<Element<DisposalHearingAddNewDirections>> disposalHearingAddNewDirections;

    private DisposalHearingNotes disposalHearingNotes;

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
    private boolean hasDisposalWelshToggle;

    @SuppressWarnings("unused")
    public boolean getDisposalHearingMethodToggle() {
        // SNI-5142
        return true;
    }
}
