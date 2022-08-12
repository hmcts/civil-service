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
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackBuildingDispute;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackClinicalNegligence;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackCreditHire;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackDisclosureOfDocuments;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackHousingDisrepair;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackJudgementDeductionValue;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackJudgesRecital;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackNotes;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackPersonalInjury;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackRoadTrafficAccident;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackSchedulesOfLoss;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackTrial;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackWitnessOfFact;
import uk.gov.hmcts.reform.civil.model.sdo.JudgementSum;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class SdoDocumentFormFast implements MappableObject {

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

//    private final List<FastTrack> fastClaims;

    private FastTrackJudgesRecital fastTrackJudgesRecital;
    private FastTrackJudgementDeductionValue fastTrackJudgementDeductionValue;
    private FastTrackDisclosureOfDocuments fastTrackDisclosureOfDocuments;
    private FastTrackWitnessOfFact fastTrackWitnessOfFact;
    private FastTrackSchedulesOfLoss fastTrackSchedulesOfLoss;
    private FastTrackTrial fastTrackTrial;

    // private final FastTrackMethod fastTrackMethod;
    private final DynamicList fastTrackMethodInPerson;
    // private final FastTrackMethodTelephoneHearing fastTrackMethodTelephoneHearing;
    // private final FastTrackMethodVideoConferenceHearing fastTrackMethodVideoConferenceHearing;

    private FastTrackBuildingDispute fastTrackBuildingDispute;
    private FastTrackClinicalNegligence fastTrackClinicalNegligence;
    private FastTrackCreditHire fastTrackCreditHire;
    private FastTrackHousingDisrepair fastTrackHousingDisrepair;
    private FastTrackPersonalInjury fastTrackPersonalInjury;
    private FastTrackRoadTrafficAccident fastTrackRoadTrafficAccident;

    // private final List<Element<FastTrackAddNewDirections>> fastTrackAddNewDirections;

    private FastTrackNotes fastTrackNotes;

    private List<OrderDetailsPagesSectionsToggle> fastTrackAltDisputeResolutionToggle;
    private List<OrderDetailsPagesSectionsToggle> fastTrackVariationOfDirectionsToggle;
    private List<OrderDetailsPagesSectionsToggle> fastTrackSettlementToggle;
    private List<OrderDetailsPagesSectionsToggle> fastTrackDisclosureOfDocumentsToggle;
    private List<OrderDetailsPagesSectionsToggle> fastTrackWitnessOfFactToggle;
    private List<OrderDetailsPagesSectionsToggle> fastTrackSchedulesOfLossToggle;
    private List<OrderDetailsPagesSectionsToggle> fastTrackCostsToggle;
    private List<OrderDetailsPagesSectionsToggle> fastTrackTrialToggle;
    private List<OrderDetailsPagesSectionsToggle> fastTrackMethodToggle;
}
