package uk.gov.hmcts.reform.civil.model.docmosis.sdo;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2SmallClaimsAddNewDirection;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2SmallClaimsHearing;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2SmallClaimsImpNotes;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2SmallClaimsJudgesRecital;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2SmallClaimsPPI;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2SmallClaimsUploadDoc;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2SmallClaimsWitnessStatements;

import java.util.List;

@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SdoDocumentFormSmallDrh extends SdoDocumentFormBase implements MappableObject {

    //Toggles
    private boolean hasPaymentProtectionInsurance;
    private boolean hasWitnessStatement;
    private boolean hasUploadDocToggle;
    private boolean hasHearingToggle;
    private boolean hasWitnessStatements;
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
    private String sdoR2SmallClaimMediationSectionInput;
}
