package uk.gov.hmcts.reform.civil.service.docmosis.sdo;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.sdo.SmallTrack;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.docmosis.sdo.SdoDocumentFormSmall;
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
        SdoDocumentFormSmall.SdoDocumentFormSmallBuilder builder = SdoDocumentFormSmall.builder()
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
            .drawDirectionsOrderRequired(caseData.getDrawDirectionsOrderRequired())
            .drawDirectionsOrder(caseData.getDrawDirectionsOrder())
            .claimsTrack(caseData.getClaimsTrack())
            .smallClaims(caseData.getSmallClaims())
            .hasCreditHire(hasAdditionalDirection(caseData, SmallTrack.smallClaimCreditHire))
            .hasRoadTrafficAccident(hasAdditionalDirection(caseData, SmallTrack.smallClaimRoadTrafficAccident))
            .smallClaimsJudgesRecital(caseData.getSmallClaimsJudgesRecital())
            .smallClaimsHearing(caseData.getSmallClaimsHearing())
            .smallClaimsHearingTime(smallClaimsTemplateFieldService.getHearingTimeLabel(caseData))
            .smallClaimsMethod(caseData.getSmallClaimsMethod())
            .smallClaimsMethodInPerson(caseData.getSmallClaimsMethodInPerson())
            .smallClaimsMethodTelephoneHearing(smallClaimsTemplateFieldService.getMethodTelephoneHearingLabel(caseData))
            .smallClaimsMethodVideoConferenceHearing(
                smallClaimsTemplateFieldService.getMethodVideoConferenceHearingLabel(caseData)
            )
            .smallClaimsDocuments(caseData.getSmallClaimsDocuments())
            .smallClaimsCreditHire(caseData.getSmallClaimsCreditHire())
            .smallClaimsRoadTrafficAccident(caseData.getSmallClaimsRoadTrafficAccident())
            .hasNewDirections(hasVariable(caseData, SmallClaimsVariable.ADD_NEW_DIRECTIONS))
            .smallClaimsAddNewDirections(caseData.getSmallClaimsAddNewDirections())
            .smallClaimsNotes(caseData.getSmallClaimsNotes())
            .smallClaimsHearingToggle(hasVariable(caseData, SmallClaimsVariable.HEARING_TOGGLE))
            .smallClaimsMethodToggle(true)
            .smallClaimMediationSectionInput(smallClaimsTemplateFieldService.getMediationText(caseData))
            .smallClaimsDocumentsToggle(hasVariable(caseData, SmallClaimsVariable.DOCUMENTS_TOGGLE))
            .smallClaimsWitnessStatementToggle(hasVariable(caseData, SmallClaimsVariable.WITNESS_STATEMENT_TOGGLE))
            .smallClaimsNumberOfWitnessesToggle(hasVariable(caseData, SmallClaimsVariable.NUMBER_OF_WITNESSES_TOGGLE))
            .smallClaimsMediationSectionToggle(
                smallClaimsTemplateFieldService.showMediationSection(caseData, carmEnabled)
            )
            .caseAccessCategory(caseData.getCaseAccessCategory().toString())
            .carmEnabled(carmEnabled)
            .smallClaimsFlightDelayToggle(hasVariable(caseData, SmallClaimsVariable.FLIGHT_DELAY_TOGGLE))
            .smallClaimsFlightDelay(caseData.getSmallClaimsFlightDelay())
            .smallClaimsWelshLanguageToggle(hasVariable(caseData, SmallClaimsVariable.WELSH_TOGGLE))
            .welshLanguageDescription(
                Optional.ofNullable(caseData.getSdoR2SmallClaimsUseOfWelshLanguage())
                    .map(value -> value.getDescription())
                    .orElse(null)
            )
            .sdoR2SmallClaimsWitnessStatements(caseData.getSdoR2SmallClaimsWitnessStatementOther());

        builder.hearingLocation(
                locationHelper.getHearingLocation(
                    Optional.ofNullable(caseData.getSmallClaimsMethodInPerson())
                        .map(DynamicList::getValue)
                        .map(DynamicListElement::getLabel)
                        .orElse(null),
                    caseData,
                    authorisation
                ))
            .caseManagementLocation(locationHelper.getHearingLocation(null, caseData, authorisation));

        return builder.build();
    }

    private boolean hasAdditionalDirection(CaseData caseData, SmallTrack track) {
        return smallClaimsDirectionsService.hasSmallAdditionalDirections(caseData, track);
    }

    private boolean hasVariable(CaseData caseData, SmallClaimsVariable variable) {
        return smallClaimsDirectionsService.hasSmallClaimsVariable(caseData, variable);
    }
}
