package uk.gov.hmcts.reform.civil.model.docmosis.sdo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
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

@Getter
@Builder
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class SdoDocumentFormSmallDrh implements MappableObject {

    private final LocalDate currentDate;
    //Header
    private final String judgeName;
    private final String caseNumber;
    private final Party applicant1;
    private final Party respondent1;
    private final boolean hasApplicant2;
    private final Party applicant2;
    private final boolean hasRespondent2;
    private final Party respondent2;
    private final boolean writtenByJudge;

    //Toggles
    private final boolean hasPaymentProtectionInsurance;
    private final boolean hasWitnessStatement;
    private final boolean hasUploadDocToggle;
    private final boolean hasHearingToggle;
    private final boolean hasWitnessStatements;
    private final boolean hasNewDirections;
    private final boolean hasSdoR2HearingTrialWindow;
    private final boolean hasDRHWelshLangToggle;
    private final boolean sdoR2SmallClaimsMediationSectionToggle;
    private final boolean carmEnabled;

    private final List<Element<SdoR2SmallClaimsAddNewDirection>> sdoR2SmallClaimsAddNewDirection;
    private final SdoR2SmallClaimsJudgesRecital sdoR2SmallClaimsJudgesRecital;
    private final SdoR2SmallClaimsWitnessStatements sdoR2SmallClaimsWitnessStatements;
    private final SdoR2SmallClaimsPPI sdoR2SmallClaimsPPI;
    private final SdoR2SmallClaimsUploadDoc sdoR2SmallClaimsUploadDoc;
    private final SdoR2SmallClaimsHearing sdoR2SmallClaimsHearing;
    private final SdoR2SmallClaimsImpNotes sdoR2SmallClaimsImpNotes;
    private final String hearingTime;
    private final String smallClaimsMethod;
    private final String sdoR2SmallClaimsPhysicalTrialBundleTxt;
    private final LocationRefData hearingLocation;
    private final LocationRefData caseManagementLocation;
    private final String welshLanguageDescription;
    private final String sdoR2SmallClaimMediationSectionInput;
}
