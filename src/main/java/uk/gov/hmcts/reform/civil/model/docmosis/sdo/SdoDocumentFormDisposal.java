package uk.gov.hmcts.reform.civil.model.docmosis.sdo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.sdo.ClaimsTrack;
import uk.gov.hmcts.reform.civil.enums.sdo.OrderDetailsPagesSectionsToggle;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingBundle;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingDisclosureOfDocuments;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingFinalDisposalHearing;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingJudgementDeductionValue;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingJudgesRecital;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingMedicalEvidence;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingNotes;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingQuestionsToExperts;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingSchedulesOfLoss;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingWitnessOfFact;
import uk.gov.hmcts.reform.civil.model.sdo.JudgementSum;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class SdoDocumentFormDisposal implements MappableObject {

    @JsonProperty("courtseal")
    private final String courtSeal = "[userImage:courtseal.PNG]"; //NOSONAR

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    @JsonSerialize(using = LocalDateSerializer.class)
    private final LocalDate currentDate;

    private final String judgeName;

    private final String caseNumber;

    private final Party applicant1;
    private final Party applicant2;
    private final Party respondent1;
    private final Party respondent2;

    private final YesOrNo drawDirectionsOrderRequired;
    private final JudgementSum drawDirectionsOrder;
    private final ClaimsTrack claimsTrack;

    private DisposalHearingJudgesRecital disposalHearingJudgesRecital;
    private DisposalHearingJudgementDeductionValue disposalHearingJudgementDeductionValue;
    private DisposalHearingDisclosureOfDocuments disposalHearingDisclosureOfDocuments;
    private DisposalHearingWitnessOfFact disposalHearingWitnessOfFact;
    private DisposalHearingMedicalEvidence disposalHearingMedicalEvidence;
    private DisposalHearingQuestionsToExperts disposalHearingQuestionsToExperts;
    private DisposalHearingSchedulesOfLoss disposalHearingSchedulesOfLoss;
    private DisposalHearingFinalDisposalHearing disposalHearingFinalDisposalHearing;
    private final DynamicList disposalHearingMethodInPerson;
    private DisposalHearingBundle disposalHearingBundle;

    private DisposalHearingNotes disposalHearingNotes;

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
    private List<OrderDetailsPagesSectionsToggle> disposalHearingApplicationsOrderToggle;
}
