package uk.gov.hmcts.reform.civil.model.docmosis.sdo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
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
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2FastTrackCreditHire;
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
@SuppressWarnings({"java:S1104", "java:S107"})
public class SdoDocumentFormFast implements MappableObject {

    private LocalDate currentDate;

    private String judgeName;

    private String caseNumber;

    private Party applicant1;
    private Party respondent1;
    private boolean hasApplicant2;
    private Party applicant2;
    private boolean hasRespondent2;
    private Party respondent2;
    private YesOrNo drawDirectionsOrderRequired;
    private JudgementSum drawDirectionsOrder;
    private ClaimsTrack claimsTrack;

    private List<FastTrack> fastClaims;

    private boolean hasBuildingDispute;
    private boolean hasClinicalNegligence;
    private boolean hasCreditHire;
    private boolean hasEmployersLiability;
    private boolean hasHousingDisrepair;
    private boolean hasPersonalInjury;
    private boolean hasRoadTrafficAccident;
    private boolean writtenByJudge;
    private boolean hasSdoR2CreditHire;
    private boolean hasSdoR2CreditHireDetails;

    private FastTrackJudgesRecital fastTrackJudgesRecital;
    private FastTrackDisclosureOfDocuments fastTrackDisclosureOfDocuments;
    private FastTrackWitnessOfFact fastTrackWitnessOfFact;
    private FastTrackSchedulesOfLoss fastTrackSchedulesOfLoss;
    private FastTrackTrial fastTrackTrial;
    private FastTrackHearingTime fastTrackHearingTime;
    private String fastTrackHearingTimeEstimate;
    private String fastTrackTrialBundleTypeText;

    private FastTrackMethod fastTrackMethod;
    private DynamicList fastTrackMethodInPerson;
    private String fastTrackMethodTelephoneHearing;
    private String fastTrackMethodVideoConferenceHearing;
    private LocationRefData hearingLocation;
    private LocationRefData caseManagementLocation;

    private FastTrackBuildingDispute fastTrackBuildingDispute;
    private FastTrackClinicalNegligence fastTrackClinicalNegligence;
    private FastTrackCreditHire fastTrackCreditHire;
    private FastTrackHousingDisrepair fastTrackHousingDisrepair;
    private FastTrackPersonalInjury fastTrackPersonalInjury;
    private FastTrackRoadTrafficAccident fastTrackRoadTrafficAccident;
    private SdoR2WitnessOfFact sdoR2WitnessesOfFact;
    private SdoR2FastTrackCreditHire sdoR2FastTrackCreditHire;

    private boolean hasNewDirections;
    private List<Element<FastTrackAddNewDirections>> fastTrackAddNewDirections;

    private FastTrackNotes fastTrackNotes;
    private FastTrackOrderWithoutJudgement fastTrackOrderWithoutJudgement;

    private boolean fastTrackAltDisputeResolutionToggle;
    private boolean fastTrackVariationOfDirectionsToggle;
    private boolean fastTrackSettlementToggle;
    private boolean fastTrackDisclosureOfDocumentsToggle;
    private boolean fastTrackWitnessOfFactToggle;
    private boolean fastTrackSchedulesOfLossToggle;
    private boolean fastTrackCostsToggle;
    private boolean fastTrackTrialDateToToggle;
    private boolean fastTrackTrialToggle;
    private boolean fastTrackMethodToggle;
    private boolean fastTrackWelshLanguageToggle;
    private String fastTrackAllocation;
    private String welshLanguageDescription;
    private boolean showBundleInfo;

    @SuppressWarnings("unused")
    public boolean getFastTrackMethodToggle() {
        // made mandatory in SNI-5142
        return true;
    }
}
