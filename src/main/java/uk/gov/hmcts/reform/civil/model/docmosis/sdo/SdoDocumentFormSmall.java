package uk.gov.hmcts.reform.civil.model.docmosis.sdo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.sdo.ClaimsTrack;
import uk.gov.hmcts.reform.civil.enums.sdo.SmallClaimsMethod;
import uk.gov.hmcts.reform.civil.enums.sdo.SmallTrack;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;
import uk.gov.hmcts.reform.civil.model.sdo.JudgementSum;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2SmallClaimsWitnessStatements;
import uk.gov.hmcts.reform.civil.model.sdo.SmallClaimsAddNewDirections;
import uk.gov.hmcts.reform.civil.model.sdo.SmallClaimsCreditHire;
import uk.gov.hmcts.reform.civil.model.sdo.SmallClaimsDocuments;
import uk.gov.hmcts.reform.civil.model.sdo.SmallClaimsFlightDelay;
import uk.gov.hmcts.reform.civil.model.sdo.SmallClaimsHearing;
import uk.gov.hmcts.reform.civil.model.sdo.SmallClaimsJudgesRecital;
import uk.gov.hmcts.reform.civil.model.sdo.SmallClaimsNotes;
import uk.gov.hmcts.reform.civil.model.sdo.SmallClaimsRoadTrafficAccident;
import uk.gov.hmcts.reform.civil.model.sdo.SmallClaimsWitnessStatement;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@SuppressWarnings({"java:S1104", "java:S107", "common-java:DuplicatedBlocks", "java:S6539"})
public class SdoDocumentFormSmall implements MappableObject {

    private String judgeName;
    private String caseNumber;
    private LocalDate currentDate;
    private ClaimsTrack claimsTrack;
    private YesOrNo drawDirectionsOrderRequired;
    private JudgementSum drawDirectionsOrder;
    private Party applicant1;
    private Party applicant2;
    private boolean hasApplicant2;
    private Party respondent1;
    private Party respondent2;
    private boolean hasRespondent2;

    private List<SmallTrack> smallClaims;

    private boolean hasCreditHire;
    private boolean hasRoadTrafficAccident;
    private boolean writtenByJudge;

    private SmallClaimsJudgesRecital smallClaimsJudgesRecital;
    private SmallClaimsHearing smallClaimsHearing;
    private String smallClaimsHearingTime;
    private SmallClaimsMethod smallClaimsMethod;
    private DynamicList smallClaimsMethodInPerson;
    private String smallClaimsMethodTelephoneHearing;
    private String smallClaimsMethodVideoConferenceHearing;
    private SmallClaimsDocuments smallClaimsDocuments;
    private SmallClaimsFlightDelay smallClaimsFlightDelay;
    private SmallClaimsWitnessStatement smallClaimsWitnessStatement;
    private LocationRefData hearingLocation;
    private LocationRefData caseManagementLocation;
    private SmallClaimsCreditHire smallClaimsCreditHire;
    private SmallClaimsRoadTrafficAccident smallClaimsRoadTrafficAccident;
    private SdoR2SmallClaimsWitnessStatements sdoR2SmallClaimsWitnessStatements;

    private String welshLanguageDescription;
    private boolean hasNewDirections;
    private List<Element<SmallClaimsAddNewDirections>> smallClaimsAddNewDirections;

    private SmallClaimsNotes smallClaimsNotes;
    private boolean smallClaimsHearingToggle;
    /**
     * SNI-5142 made mandatory.
     */
    private boolean smallClaimsMethodToggle;
    private boolean smallClaimsDocumentsToggle;
    private boolean smallClaimsWitnessStatementToggle;
    private boolean smallClaimsNumberOfWitnessesToggle;
    private boolean smallClaimsFlightDelayToggle;
    private boolean smallClaimsMediationSectionToggle;
    private boolean carmEnabled;
    private String smallClaimMediationSectionInput;
    private boolean smallClaimsWelshLanguageToggle;
    private String caseAccessCategory;

    @SuppressWarnings("unused")
    public boolean getSmallClaimsMethodToggle() {
        // SNI-5142
        return true;
    }
}
