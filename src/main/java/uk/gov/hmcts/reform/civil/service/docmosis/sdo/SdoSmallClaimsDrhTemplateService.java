package uk.gov.hmcts.reform.civil.service.docmosis.sdo;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.docmosis.sdo.SdoDocumentFormSmallDrh;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2WelshLanguageUsage;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentHearingLocationHelper;
import uk.gov.hmcts.reform.civil.service.sdo.SdoCaseClassificationService;
import uk.gov.hmcts.reform.civil.service.sdo.SdoR2SmallClaimsDirectionsService;
import uk.gov.hmcts.reform.civil.service.sdo.SdoSmallClaimsTemplateFieldService;

import java.time.LocalDate;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SdoSmallClaimsDrhTemplateService {

    private final DocumentHearingLocationHelper locationHelper;
    private final SdoCaseClassificationService caseClassificationService;
    private final SdoR2SmallClaimsDirectionsService r2SmallClaimsDirectionsService;
    private final FeatureToggleService featureToggleService;
    private final SdoSmallClaimsTemplateFieldService smallClaimsTemplateFieldService;

    public SdoDocumentFormSmallDrh buildTemplate(
        CaseData caseData,
        String judgeName,
        boolean isJudge,
        String authorisation
    ) {
        boolean carmEnabled = featureToggleService.isCarmEnabledForCase(caseData);

        SdoDocumentFormSmallDrh template = new SdoDocumentFormSmallDrh()
            .setWrittenByJudge(isJudge)
            .setCurrentDate(LocalDate.now())
            .setJudgeName(judgeName)
            .setCaseNumber(caseData.getLegacyCaseReference())
            .setApplicant1(caseData.getApplicant1())
            .setHasApplicant2(caseClassificationService.hasApplicant2(caseData))
            .setApplicant2(caseData.getApplicant2())
            .setRespondent1(caseData.getRespondent1())
            .setHasRespondent2(caseClassificationService.hasRespondent2(caseData))
            .setRespondent2(caseData.getRespondent2())
            .setHasPaymentProtectionInsurance(caseData.getSdoR2SmallClaimsPPIToggle() != null)
            .setHasHearingToggle(caseData.getSdoR2SmallClaimsHearingToggle() != null)
            .setHasWitnessStatement(caseData.getSdoR2SmallClaimsWitnessStatements() != null)
            .setHasUploadDocToggle(caseData.getSdoR2SmallClaimsUploadDocToggle() != null)
            .setHasDRHWelshLangToggle(caseData.getSdoR2DrhUseOfWelshIncludeInOrderToggle() != null)
            .setHasSdoR2HearingTrialWindow(r2SmallClaimsDirectionsService.hasHearingTrialWindow(caseData))
            .setHasNewDirections(caseData.getSdoR2SmallClaimsAddNewDirection() != null)
            .setSdoR2SmallClaimsPhysicalTrialBundleTxt(r2SmallClaimsDirectionsService.getPhysicalTrialBundleText(caseData))
            .setSdoR2SmallClaimsJudgesRecital(caseData.getSdoR2SmallClaimsJudgesRecital())
            .setSdoR2SmallClaimsHearing(caseData.getSdoR2SmallClaimsHearing())
            .setSdoR2SmallClaimsWitnessStatements(caseData.getSdoR2SmallClaimsWitnessStatements())
            .setSdoR2SmallClaimsPPI(caseData.getSdoR2SmallClaimsPPI())
            .setSdoR2SmallClaimsUploadDoc(caseData.getSdoR2SmallClaimsUploadDoc())
            .setSmallClaimsMethod(r2SmallClaimsDirectionsService.getHearingMethod(caseData))
            .setHearingTime(r2SmallClaimsDirectionsService.getHearingTime(caseData))
            .setSdoR2SmallClaimsImpNotes(caseData.getSdoR2SmallClaimsImpNotes())
            .setSdoR2SmallClaimsAddNewDirection(caseData.getSdoR2SmallClaimsAddNewDirection())
            .setWelshLanguageDescription(Optional.ofNullable(caseData.getSdoR2DrhUseOfWelshLanguage())
                                              .map(SdoR2WelshLanguageUsage::getDescription).orElse(null))
            .setCarmEnabled(carmEnabled)
            .setSdoR2SmallClaimMediationSectionInput(smallClaimsTemplateFieldService.getMediationTextDrh(caseData))
            .setCaseManagementLocation(locationHelper.getHearingLocation(null, caseData, authorisation))
            .setSdoR2SmallClaimsMediationSectionToggle(
                smallClaimsTemplateFieldService.showMediationSectionDrh(caseData, carmEnabled)
            );

        if (caseData.getSdoR2SmallClaimsHearing() != null) {
            template.setHearingLocation(
                locationHelper.getHearingLocation(
                    Optional.ofNullable(r2SmallClaimsDirectionsService.getHearingLocation(caseData))
                        .map(DynamicList::getValue)
                        .map(DynamicListElement::getLabel)
                        .orElse(null),
                    caseData,
                    authorisation
                )
            );
        }

        return template;
    }
}
