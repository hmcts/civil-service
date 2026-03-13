package uk.gov.hmcts.reform.civil.service.docmosis.sdo;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.sdo.SmallTrack;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.docmosis.sdo.SdoDocumentFormSmall;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2WelshLanguageUsage;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentHearingLocationHelper;
import uk.gov.hmcts.reform.civil.service.sdo.SdoCaseClassificationService;
import uk.gov.hmcts.reform.civil.service.sdo.SdoSmallClaimsDirectionsService;
import uk.gov.hmcts.reform.civil.service.sdo.SdoSmallClaimsTemplateFieldService;
import uk.gov.hmcts.reform.civil.service.sdo.SmallClaimsVariable;

import java.time.LocalDate;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SdoSmallClaimsTemplateService {

    private final DocumentHearingLocationHelper locationHelper;
    private final SdoCaseClassificationService caseClassificationService;
    private final SdoSmallClaimsDirectionsService smallClaimsDirectionsService;
    private final FeatureToggleService featureToggleService;
    private final SdoSmallClaimsTemplateFieldService smallClaimsTemplateFieldService;

    public SdoDocumentFormSmall buildTemplate(
        CaseData caseData,
        String judgeName,
        boolean isJudge,
        String authorisation
    ) {
        boolean carmEnabled = featureToggleService.isCarmEnabledForCase(caseData);
        boolean hasPpi = hasAdditionalDirection(caseData, SmallTrack.smallClaimPPI);
        SdoDocumentFormSmall template = new SdoDocumentFormSmall()
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
            .setDrawDirectionsOrderRequired(caseData.getDrawDirectionsOrderRequired())
            .setDrawDirectionsOrder(caseData.getDrawDirectionsOrder())
            .setClaimsTrack(caseData.getClaimsTrack())
            .setSmallClaims(caseData.getSmallClaims())
            .setHasCreditHire(hasAdditionalDirection(caseData, SmallTrack.smallClaimCreditHire))
            .setHasHousingDisrepair(hasAdditionalDirection(caseData, SmallTrack.smallClaimHousingDisrepair))
            .setSmallClaimsHousingDisrepair(caseData.getSmallClaimsHousingDisrepair())
            .setHasRoadTrafficAccident(hasAdditionalDirection(caseData, SmallTrack.smallClaimRoadTrafficAccident))
            .setHasPaymentProtectionInsurance(hasPpi)
            .setSmallClaimsPPI(hasPpi ? caseData.getSmallClaimsPPI() : null)
            .setSmallClaimsJudgesRecital(caseData.getSmallClaimsJudgesRecital())
            .setSmallClaimsHearing(caseData.getSmallClaimsHearing())
            .setSmallClaimsHearingTime(smallClaimsTemplateFieldService.getHearingTimeLabel(caseData))
            .setSmallClaimsMethod(caseData.getSmallClaimsMethod())
            .setSmallClaimsMethodInPerson(caseData.getSmallClaimsMethodInPerson())
            .setSmallClaimsMethodTelephoneHearing(smallClaimsTemplateFieldService.getMethodTelephoneHearingLabel(caseData))
            .setSmallClaimsMethodVideoConferenceHearing(
                smallClaimsTemplateFieldService.getMethodVideoConferenceHearingLabel(caseData)
            )
            .setSmallClaimsDocuments(caseData.getSmallClaimsDocuments())
            .setSmallClaimsCreditHire(caseData.getSmallClaimsCreditHire())
            .setSmallClaimsRoadTrafficAccident(caseData.getSmallClaimsRoadTrafficAccident())
            .setHasNewDirections(hasVariable(caseData, SmallClaimsVariable.ADD_NEW_DIRECTIONS))
            .setSmallClaimsAddNewDirections(caseData.getSmallClaimsAddNewDirections())
            .setSmallClaimsNotes(caseData.getSmallClaimsNotes())
            .setSmallClaimsHearingToggle(hasVariable(caseData, SmallClaimsVariable.HEARING_TOGGLE))
            .setSmallClaimsMethodToggle(true)
            .setSmallClaimMediationSectionInput(smallClaimsTemplateFieldService.getMediationText(caseData))
            .setSmallClaimsDocumentsToggle(hasVariable(caseData, SmallClaimsVariable.DOCUMENTS_TOGGLE))
            .setSmallClaimsWitnessStatementToggle(hasVariable(caseData, SmallClaimsVariable.WITNESS_STATEMENT_TOGGLE))
            .setSmallClaimsNumberOfWitnessesToggle(hasVariable(caseData, SmallClaimsVariable.NUMBER_OF_WITNESSES_TOGGLE))
            .setSmallClaimsMediationSectionToggle(
                smallClaimsTemplateFieldService.showMediationSection(caseData, carmEnabled)
            )
            .setCaseAccessCategory(caseData.getCaseAccessCategory().toString())
            .setCarmEnabled(carmEnabled)
            .setSmallClaimsFlightDelayToggle(hasVariable(caseData, SmallClaimsVariable.FLIGHT_DELAY_TOGGLE))
            .setSmallClaimsFlightDelay(caseData.getSmallClaimsFlightDelay())
            .setSmallClaimsWelshLanguageToggle(hasVariable(caseData, SmallClaimsVariable.WELSH_TOGGLE))
            .setWelshLanguageDescription(
                Optional.ofNullable(caseData.getSdoR2SmallClaimsUseOfWelshLanguage())
                    .map(SdoR2WelshLanguageUsage::getDescription)
                    .orElse(null)
            )
            .setSdoR2SmallClaimsWitnessStatements(caseData.getSdoR2SmallClaimsWitnessStatementOther())
            .setShowPenalNotice(hasVariable(caseData, SmallClaimsVariable.PENAL_NOTICE_TOGGLE))
            .setPenalNoticeText(caseData.getSmallClaimsPenalNotice());

        template
            .setHearingLocation(
                locationHelper.getHearingLocation(
                    Optional.ofNullable(caseData.getSmallClaimsMethodInPerson())
                        .map(DynamicList::getValue)
                        .map(DynamicListElement::getLabel)
                        .orElse(null),
                    caseData,
                    authorisation
                ))
            .setCaseManagementLocation(locationHelper.getHearingLocation(null, caseData, authorisation));

        return template;
    }

    private boolean hasAdditionalDirection(CaseData caseData, SmallTrack track) {
        return smallClaimsDirectionsService.hasSmallAdditionalDirections(caseData, track);
    }

    private boolean hasVariable(CaseData caseData, SmallClaimsVariable variable) {
        return smallClaimsDirectionsService.hasSmallClaimsVariable(caseData, variable);
    }
}
