package uk.gov.hmcts.reform.civil.model.docmosis.sdo;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.reform.civil.enums.sdo.ClaimsTrack;
import uk.gov.hmcts.reform.civil.enums.sdo.SmallClaimsMethod;
import uk.gov.hmcts.reform.civil.enums.sdo.SmallTrack;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;
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

import java.util.List;

@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@SuppressWarnings({"java:S1104", "java:S107"})
public class SdoDocumentFormSmall extends SdoDocumentFormBase implements MappableObject {

    private ClaimsTrack claimsTrack;

    private List<SmallTrack> smallClaims;

    private boolean hasCreditHire;
    private boolean hasRoadTrafficAccident;

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
    private SmallClaimsCreditHire smallClaimsCreditHire;
    private SmallClaimsRoadTrafficAccident smallClaimsRoadTrafficAccident;
    private SdoR2SmallClaimsWitnessStatements sdoR2SmallClaimsWitnessStatements;

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
