package uk.gov.hmcts.reform.civil.model.docmosis.sdo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2SmallClaimsAddNewDirection;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2SmallClaimsHearing;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2SmallClaimsImpNotes;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2SmallClaimsJudgesRecital;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2SmallClaimsPPI;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2SmallClaimsUploadDoc;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2SmallClaimsWitnessStatements;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@SuppressWarnings({"java:S1104", "common-java:DuplicatedBlocks", "java:S6539"})
public class SdoDocumentFormSmallDrh implements MappableObject {

    private LocalDate currentDate;
    //Header
    private String judgeName;
    private String caseNumber;
    private Party applicant1;
    private Party respondent1;
    private boolean hasApplicant2;
    private Party applicant2;
    private boolean hasRespondent2;
    private Party respondent2;
    private boolean writtenByJudge;

    //Toggles
    private boolean hasPaymentProtectionInsurance;
    private boolean hasWitnessStatement;
    private boolean hasUploadDocToggle;
    private boolean hasHearingToggle;
    private boolean hasWitnessStatements;
    private boolean hasNewDirections;
    private boolean hasSdoR2HearingTrialWindow;
    private boolean hasDRHWelshLangToggle;
    private boolean sdoR2SmallClaimsMediationSectionToggle;
    private boolean carmEnabled;

    private List<Element<SdoR2SmallClaimsAddNewDirection>> sdoR2SmallClaimsAddNewDirection;
    private SdoR2SmallClaimsJudgesRecital sdoR2SmallClaimsJudgesRecital;
    private SdoR2SmallClaimsWitnessStatements sdoR2SmallClaimsWitnessStatements;
    private SdoR2SmallClaimsPPI sdoR2SmallClaimsPPI;
    private SdoR2SmallClaimsUploadDoc sdoR2SmallClaimsUploadDoc;
    private SdoR2SmallClaimsHearing sdoR2SmallClaimsHearing;
    private SdoR2SmallClaimsImpNotes sdoR2SmallClaimsImpNotes;
    private String hearingTime;
    private String smallClaimsMethod;
    private String sdoR2SmallClaimsPhysicalTrialBundleTxt;
    private LocationRefData hearingLocation;
    private LocationRefData caseManagementLocation;
    private String welshLanguageDescription;
    private String sdoR2SmallClaimMediationSectionInput;
}
