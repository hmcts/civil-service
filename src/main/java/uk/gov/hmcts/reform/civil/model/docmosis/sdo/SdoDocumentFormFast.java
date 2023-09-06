package uk.gov.hmcts.reform.civil.model.docmosis.sdo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.sdo.ClaimsTrack;
import uk.gov.hmcts.reform.civil.enums.sdo.FastTrack;
import uk.gov.hmcts.reform.civil.enums.sdo.FastTrackMethod;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackAddNewDirections;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackBuildingDispute;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackClinicalNegligence;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackCreditHire;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackDisclosureOfDocuments;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackHearingTime;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackHousingDisrepair;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackJudgesRecital;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackNotes;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackOrderWithoutJudgement;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackPersonalInjury;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackRoadTrafficAccident;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackSchedulesOfLoss;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackTrial;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackWitnessOfFact;
import uk.gov.hmcts.reform.civil.model.sdo.JudgementSum;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class SdoDocumentFormFast implements MappableObject {

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

    private final boolean hasBuildingDispute;
    private final boolean hasClinicalNegligence;
    private final boolean hasCreditHire;
    private final boolean hasEmployersLiability;
    private final boolean hasHousingDisrepair;
    private final boolean hasPersonalInjury;
    private final boolean hasRoadTrafficAccident;
    private final boolean writtenByJudge;

    private final FastTrackJudgesRecital fastTrackJudgesRecital;
    private final FastTrackDisclosureOfDocuments fastTrackDisclosureOfDocuments;
    private final FastTrackWitnessOfFact fastTrackWitnessOfFact;
    private final FastTrackSchedulesOfLoss fastTrackSchedulesOfLoss;
    private final FastTrackTrial fastTrackTrial;
    private final FastTrackHearingTime fastTrackHearingTime;
    private final String fastTrackHearingTimeEstimate;
    private final String fastTrackTrialBundleTypeText;

    private final FastTrackMethod fastTrackMethod;
    private final DynamicList fastTrackMethodInPerson;
    private final String fastTrackMethodTelephoneHearing;
    private final String fastTrackMethodVideoConferenceHearing;
    private final LocationRefData hearingLocation;
    private final LocationRefData caseManagementLocation;

    private final FastTrackBuildingDispute fastTrackBuildingDispute;
    private final FastTrackClinicalNegligence fastTrackClinicalNegligence;
    private final FastTrackCreditHire fastTrackCreditHire;
    private final FastTrackHousingDisrepair fastTrackHousingDisrepair;
    private final FastTrackPersonalInjury fastTrackPersonalInjury;
    private final FastTrackRoadTrafficAccident fastTrackRoadTrafficAccident;

    private final boolean hasNewDirections;
    private final List<Element<FastTrackAddNewDirections>> fastTrackAddNewDirections;

    private FastTrackNotes fastTrackNotes;
    private FastTrackOrderWithoutJudgement fastTrackOrderWithoutJudgement;

    private final boolean fastTrackAltDisputeResolutionToggle;
    private final boolean fastTrackVariationOfDirectionsToggle;
    private final boolean fastTrackSettlementToggle;
    private final boolean fastTrackDisclosureOfDocumentsToggle;
    private final boolean fastTrackWitnessOfFactToggle;
    private final boolean fastTrackSchedulesOfLossToggle;
    private final boolean fastTrackCostsToggle;
    private final boolean fastTrackTrialDateToToggle;
    private final boolean fastTrackTrialToggle;
    private final boolean fastTrackMethodToggle;

    private final String fastTrackAllocation;
}
