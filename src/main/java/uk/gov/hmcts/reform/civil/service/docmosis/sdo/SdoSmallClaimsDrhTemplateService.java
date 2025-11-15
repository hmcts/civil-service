package uk.gov.hmcts.reform.civil.service.docmosis.sdo;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.docmosis.sdo.SdoDocumentFormSmallDrh;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentHearingLocationHelper;
import uk.gov.hmcts.reform.civil.service.sdo.SdoCaseClassificationService;
import uk.gov.hmcts.reform.civil.service.sdo.SdoR2SmallClaimsDirectionsService;
import uk.gov.hmcts.reform.civil.service.sdo.SdoSmallClaimsDirectionsService;
import uk.gov.hmcts.reform.civil.service.sdo.SdoSmallClaimsTemplateFieldService;

import java.time.LocalDate;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SdoSmallClaimsDrhTemplateService {

    private final DocumentHearingLocationHelper locationHelper;
    private final SdoCaseClassificationService caseClassificationService;
    private final SdoR2SmallClaimsDirectionsService r2SmallClaimsDirectionsService;
    private final SdoSmallClaimsDirectionsService smallClaimsDirectionsService;
    private final FeatureToggleService featureToggleService;
    private final SdoSmallClaimsTemplateFieldService smallClaimsTemplateFieldService;

    public SdoDocumentFormSmallDrh buildTemplate(
        CaseData caseData,
        String judgeName,
        boolean isJudge,
        String authorisation
    ) {
        boolean carmEnabled = featureToggleService.isCarmEnabledForCase(caseData);

        SdoDocumentFormSmallDrh.SdoDocumentFormSmallDrhBuilder builder = SdoDocumentFormSmallDrh.builder()
            .writtenByJudge(isJudge)
            .currentDate(LocalDate.now())
            .judgeName(judgeName)
            .caseNumber(caseData.getLegacyCaseReference())
            .applicant1(caseData.getApplicant1())
            .hasApplicant2(caseClassificationService.hasApplicant2(caseData))
            .applicant2(caseData.getApplicant2())
            .respondent1(caseData.getRespondent1())
            .hasRespondent2(caseClassificationService.hasRespondent2(caseData))
            .respondent2(caseData.getRespondent2())
            .hasPaymentProtectionInsurance(caseData.getSdoR2SmallClaimsPPIToggle() != null)
            .hasHearingToggle(caseData.getSdoR2SmallClaimsHearingToggle() != null)
            .hasWitnessStatement(caseData.getSdoR2SmallClaimsWitnessStatements() != null)
            .hasUploadDocToggle(caseData.getSdoR2SmallClaimsUploadDocToggle() != null)
            .hasDRHWelshLangToggle(caseData.getSdoR2DrhUseOfWelshIncludeInOrderToggle() != null)
            .hasSdoR2HearingTrialWindow(r2SmallClaimsDirectionsService.hasHearingTrialWindow(caseData))
            .hasNewDirections(caseData.getSdoR2SmallClaimsAddNewDirection() != null)
            .sdoR2SmallClaimsPhysicalTrialBundleTxt(r2SmallClaimsDirectionsService.getPhysicalTrialBundleText(caseData))
            .sdoR2SmallClaimsJudgesRecital(caseData.getSdoR2SmallClaimsJudgesRecital())
            .sdoR2SmallClaimsHearing(caseData.getSdoR2SmallClaimsHearing())
            .sdoR2SmallClaimsWitnessStatements(caseData.getSdoR2SmallClaimsWitnessStatements())
            .sdoR2SmallClaimsPPI(caseData.getSdoR2SmallClaimsPPI())
            .sdoR2SmallClaimsUploadDoc(caseData.getSdoR2SmallClaimsUploadDoc())
            .smallClaimsMethod(r2SmallClaimsDirectionsService.getHearingMethod(caseData))
            .hearingTime(r2SmallClaimsDirectionsService.getHearingTime(caseData))
            .sdoR2SmallClaimsImpNotes(caseData.getSdoR2SmallClaimsImpNotes())
            .sdoR2SmallClaimsAddNewDirection(caseData.getSdoR2SmallClaimsAddNewDirection())
            .welshLanguageDescription(Optional.ofNullable(caseData.getSdoR2DrhUseOfWelshLanguage())
                                              .map(value -> value.getDescription()).orElse(null))
            .carmEnabled(carmEnabled)
            .sdoR2SmallClaimMediationSectionInput(smallClaimsTemplateFieldService.getMediationTextDrh(caseData))
            .caseManagementLocation(locationHelper.getHearingLocation(null, caseData, authorisation))
            .sdoR2SmallClaimsMediationSectionToggle(
                smallClaimsTemplateFieldService.showMediationSectionDrh(caseData, carmEnabled)
            );

        if (caseData.getSdoR2SmallClaimsHearing() != null) {
            builder.hearingLocation(
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

        return builder.build();
    }
}
