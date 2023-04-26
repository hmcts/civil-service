package uk.gov.hmcts.reform.civil.service.docmosis.dj;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.enums.dj.CaseManagementOrderAdditional;
import uk.gov.hmcts.reform.civil.enums.dj.DisposalAndTrialHearingDJToggle;
import uk.gov.hmcts.reform.civil.enums.dj.DisposalHearingBundleType;
import uk.gov.hmcts.reform.civil.enums.dj.DisposalHearingFinalDisposalHearingTimeEstimate;
import uk.gov.hmcts.reform.civil.enums.dj.DisposalHearingMethodDJ;
import uk.gov.hmcts.reform.civil.enums.dj.HearingMethodTelephoneHearingDJ;
import uk.gov.hmcts.reform.civil.enums.dj.HearingMethodVideoConferenceDJ;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocationCivil;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.docmosis.dj.DefaultJudgmentSDOOrderForm;
import uk.gov.hmcts.reform.civil.referencedata.LocationRefDataService;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.docmosis.TemplateDataGenerator;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.enums.dj.CaseManagementOrderAdditional.OrderTypeTrialAdditionalDirectionsEmployersLiability;
import static uk.gov.hmcts.reform.civil.enums.dj.DisposalAndTrialHearingDJToggle.SHOW;
import static uk.gov.hmcts.reform.civil.enums.dj.DisposalHearingMethodDJ.disposalHearingMethodInPerson;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.DJ_SDO_DISPOSAL;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.DJ_SDO_TRIAL;
import static uk.gov.hmcts.reform.civil.utils.DocumentUtils.getDynamicListValueLabel;
import static uk.gov.hmcts.reform.civil.utils.DocumentUtils.getHearingTimeEstimateLabel;

@Service
@RequiredArgsConstructor
public class DefaultJudgmentOrderFormGenerator implements TemplateDataGenerator<DefaultJudgmentSDOOrderForm> {

    private final DocumentManagementService documentManagementService;
    private final DocumentGeneratorService documentGeneratorService;
    private final FeatureToggleService featureToggleService;
    private final LocationRefDataService locationRefDataService;
    private static final String BOTH_DEFENDANTS = "Both Defendants";
    public static final String DISPOSAL_HEARING = "DISPOSAL_HEARING";

    public CaseDocument generate(CaseData caseData, String authorisation) {
        DefaultJudgmentSDOOrderForm templateData = getDefaultJudgmentForms(caseData, authorisation);
        DocmosisTemplates docmosisTemplate = caseData.getCaseManagementOrderSelection().equals(DISPOSAL_HEARING)
            ? getDocmosisTemplate()
            : getDocmosisTemplateTrial();
        DocmosisDocument docmosisDocument =
            documentGeneratorService.generateDocmosisDocument(templateData, docmosisTemplate);
        return documentManagementService.uploadDocument(
            authorisation,
            new PDF(
                getFileName(caseData, docmosisTemplate),
                docmosisDocument.getBytes(),
                DocumentType.DEFAULT_JUDGMENT_SDO_ORDER
            )
        );
    }

    @Override
    public DefaultJudgmentSDOOrderForm getTemplateData(CaseData caseData) throws IOException {
        return null;
    }

    private String getFileName(CaseData caseData, DocmosisTemplates docmosisTemplate) {
        return String.format(docmosisTemplate.getDocumentTitle(), caseData.getLegacyCaseReference());
    }

    private DefaultJudgmentSDOOrderForm getDefaultJudgmentForms(CaseData caseData, String authorisation) {
        return caseData.getCaseManagementOrderSelection().equals(DISPOSAL_HEARING)
            ? getDefaultJudgmentFormHearing(caseData, authorisation)
            : getDefaultJudgmentFormTrial(caseData, authorisation);
    }

    private LocationRefData getHearingLocation(String valueFromForm, CaseData caseData, String authorisation) {
        if (StringUtils.isNotBlank(valueFromForm)) {
            Optional<LocationRefData> fromForm = locationRefDataService.getLocationMatchingLabel(
                valueFromForm,
                authorisation
            );
            if (fromForm.isPresent()) {
                return fromForm.get();
            }
        }

        return Optional.ofNullable(caseData.getCaseManagementLocation())
            .map(CaseLocationCivil::getBaseLocation)
            .map(baseLocation -> locationRefDataService.getCourtLocationsByEpimmsId(
                authorisation,
                baseLocation
            )).flatMap(list -> list.stream()
                .filter(location -> StringUtils.equals(
                    location.getRegionId(),
                    caseData.getCaseManagementLocation().getRegion()
                ))
                .findFirst())
            .orElse(null);
    }

    private DefaultJudgmentSDOOrderForm getDefaultJudgmentFormHearing(CaseData caseData, String authorisation) {
        String courtLocation = getCourt(caseData);
        var djOrderFormBuilder = DefaultJudgmentSDOOrderForm.builder()
            .judgeNameTitle(caseData.getDisposalHearingJudgesRecitalDJ().getJudgeNameTitle())
            .caseNumber(caseData.getLegacyCaseReference())
            .disposalHearingBundleDJ(caseData.getDisposalHearingBundleDJ())
            .disposalHearingBundleDJAddSection(nonNull(caseData.getDisposalHearingBundleDJ()))
            .typeBundleInfo(nonNull(caseData.getDisposalHearingBundleDJ())
                                ? fillTypeBundleInfoTrial() : null)
            .disposalHearingDisclosureOfDocumentsDJ(caseData.getDisposalHearingDisclosureOfDocumentsDJ())
            .disposalHearingDisclosureOfDocumentsDJAddSection(nonNull(
                caseData.getDisposalHearingDisclosureOfDocumentsDJ()))
            .disposalHearingWitnessOfFactDJ(caseData.getDisposalHearingWitnessOfFactDJ())
            .disposalHearingWitnessOfFactDJAddSection(nonNull(caseData.getDisposalHearingWitnessOfFactDJ()))
            .disposalHearingFinalDisposalHearingDJ(caseData.getDisposalHearingFinalDisposalHearingDJ())
            .disposalHearingMethodDJ(caseData.getDisposalHearingMethodDJ())
            .disposalHearingAttendance(fillDisposalHearingMethod(caseData.getDisposalHearingMethodDJ()))
            .disposalHearingFinalDisposalHearingDJAddSection(nonNull(
                caseData.getDisposalHearingFinalDisposalHearingDJ()))
            .disposalHearingFinalDisposalHearingDJAddSection(nonNull(
                caseData.getDisposalHearingMethodDJ()))
            .disposalHearingFinalDisposalHearingDJAddSection(nonNull(fillDisposalHearingMethod(caseData
                                                                                                   .getDisposalHearingMethodDJ())))
            .courtLocation(courtLocation)
            .telephoneOrganisedBy(getHearingMethodTelephoneHearingLabel(caseData))
            .videoConferenceOrganisedBy(getHearingMethodVideoConferenceLabel(caseData))
            .disposalHearingTime(nonNull(caseData.getDisposalHearingFinalDisposalHearingDJ())
                                     ? fillDisposalHearingTime(
                caseData.getDisposalHearingFinalDisposalHearingDJ().getTime()) : null)
            .disposalHearingJudgesRecitalDJ(caseData.getDisposalHearingJudgesRecitalDJ())
            .disposalHearingMedicalEvidenceDJ(caseData.getDisposalHearingMedicalEvidenceDJ())
            .disposalHearingMedicalEvidenceDJAddSection(nonNull(caseData.getDisposalHearingMedicalEvidenceDJ()))
            .disposalHearingNotesDJ(caseData.getDisposalHearingNotesDJ())
            .hasNewDirections(addAdditionalDirection(caseData))
            .disposalHearingAddNewDirectionsDJ(caseData.getDisposalHearingAddNewDirectionsDJ())
            .disposalHearingQuestionsToExpertsDJ(caseData.getDisposalHearingQuestionsToExpertsDJ())
            .disposalHearingQuestionsToExpertsDJAddSection(nonNull(caseData.getDisposalHearingQuestionsToExpertsDJ()))
            .disposalHearingSchedulesOfLossDJ(caseData.getDisposalHearingSchedulesOfLossDJ())
            .disposalHearingSchedulesOfLossDJAddSection(nonNull(caseData.getDisposalHearingSchedulesOfLossDJ()))
            .disposalHearingClaimSettlingAddSection(getToggleValue(caseData.getDisposalHearingClaimSettlingDJToggle()))
            .disposalHearingCostsAddSection(getToggleValue(caseData.getDisposalHearingCostsDJToggle()))
            .applicant(checkApplicantPartyName(caseData)
                           ? caseData.getApplicant1().getPartyName().toUpperCase() : null)
            .respondent(checkDefendantRequested(caseData).toUpperCase());

        djOrderFormBuilder
            .disposalHearingOrderMadeWithoutHearingDJ(caseData.getDisposalHearingOrderMadeWithoutHearingDJ())
            .disposalHearingFinalDisposalHearingTimeDJ(caseData.getDisposalHearingFinalDisposalHearingTimeDJ())
            .disposalHearingTimeEstimateDJ(caseData.getDisposalHearingFinalDisposalHearingTimeDJ()
                                               .getTime().getLabel());

        djOrderFormBuilder.hearingLocation(getHearingLocation(
            courtLocation,
            caseData,
            authorisation
        ));

        return djOrderFormBuilder.build();
    }

    private DefaultJudgmentSDOOrderForm getDefaultJudgmentFormTrial(CaseData caseData, String authorisation) {
        String trialHearingLocation = checkDisposalHearingMethod(caseData.getTrialHearingMethodDJ())
            ? getDynamicListValueLabel(caseData.getTrialHearingMethodInPersonDJ()) : null;
        var djTrialTemplateBuilder = DefaultJudgmentSDOOrderForm.builder()
            .judgeNameTitle(caseData.getTrialHearingJudgesRecitalDJ().getJudgeNameTitle())
            .caseNumber(caseData.getLegacyCaseReference())
            .trialBuildingDispute(caseData.getTrialBuildingDispute())
            .trialBuildingDisputeAddSection(nonNull(caseData.getTrialBuildingDispute()))
            .trialClinicalNegligence(caseData.getTrialClinicalNegligence())
            .trialClinicalNegligenceAddSection(nonNull(caseData.getTrialClinicalNegligence()))
            .trialCreditHire(caseData.getTrialCreditHire())
            .trialCreditHireAddSection(nonNull(caseData.getTrialCreditHire()))
            .trialHearingJudgesRecitalDJ(caseData.getTrialHearingJudgesRecitalDJ())
            .trialHearingTrialDJ(caseData.getTrialHearingTrialDJ())
            .typeBundleInfo(nonNull(caseData.getTrialHearingTrialDJ())
                                ? fillTypeBundleInfo(caseData.getTrialHearingTrialDJ().getType()) : null)
            .trialHearingTrialDJAddSection(
                getToggleValue(caseData.getTrialHearingTrialDJToggle()))
            .trialHearingNotesDJ(caseData.getTrialHearingNotesDJ())
            .hasNewDirections(addAdditionalDirection(caseData))
            .trialHearingAddNewDirectionsDJ(caseData.getTrialHearingAddNewDirectionsDJ())
            .trialHearingDisclosureOfDocumentsDJ(caseData.getTrialHearingDisclosureOfDocumentsDJ())
            .trialHearingDisclosureOfDocumentsDJAddSection(
                getToggleValue(caseData.getTrialHearingDisclosureOfDocumentsDJToggle()))
            .trialPersonalInjury(caseData.getTrialPersonalInjury())
            .trialPersonalInjuryAddSection(nonNull(caseData.getTrialPersonalInjury()))
            .trialHearingSchedulesOfLossDJ(caseData.getTrialHearingSchedulesOfLossDJ())
            .trialHearingSchedulesOfLossDJAddSection(
                getToggleValue(caseData.getTrialHearingSchedulesOfLossDJToggle()))
            .trialRoadTrafficAccident(caseData.getTrialRoadTrafficAccident())
            .trialRoadTrafficAccidentAddSection(nonNull(caseData.getTrialRoadTrafficAccident()))
            .trialHearingWitnessOfFactDJ(caseData.getTrialHearingWitnessOfFactDJ())
            .trialHearingWitnessOfFactDJAddSection(
                getToggleValue(caseData.getTrialHearingWitnessOfFactDJToggle()))
            .trialHearingDisputeAddSection(getToggleValue(caseData.getTrialHearingAlternativeDisputeDJToggle()))
            .trialHearingVariationsAddSection(getToggleValue(caseData.getTrialHearingVariationsDirectionsDJToggle()))
            .trialHearingSettlementAddSection(getToggleValue(caseData.getTrialHearingSettlementDJToggle()))
            .trialHearingCostsAddSection(getToggleValue(caseData.getTrialHearingCostsToggle()))
            .trialEmployerLiabilityAddSection(getLiabilityValue(caseData.getCaseManagementOrderAdditional()))
            .trialHearingMethodDJ(caseData.getTrialHearingMethodDJ())
            .telephoneOrganisedBy(getHearingMethodTelephoneHearingLabel(caseData))
            .videoConferenceOrganisedBy(getHearingMethodVideoConferenceLabel(caseData))
            .trialHousingDisrepair(caseData.getTrialHousingDisrepair())
            .trialHousingDisrepairAddSection(nonNull(caseData.getTrialHousingDisrepair()))
            .trialHearingMethodInPersonAddSection(checkDisposalHearingMethod(caseData.getTrialHearingMethodDJ()))
            .trialHearingLocation(trialHearingLocation)
            .applicant(checkApplicantPartyName(caseData)
                           ? caseData.getApplicant1().getPartyName().toUpperCase() : null)
            .respondent(checkDefendantRequested(caseData).toUpperCase());

        if (featureToggleService.isNoticeOfChangeEnabled()) {
            djTrialTemplateBuilder
                .trialHearingTimeDJ(caseData.getTrialHearingTimeDJ())
                .trialOrderMadeWithoutHearingDJ(caseData.getTrialOrderMadeWithoutHearingDJ())
                .trialHearingTimeEstimateDJ(getHearingTimeEstimateLabel(caseData.getTrialHearingTimeDJ()));
        }

        djTrialTemplateBuilder.hearingLocation(getHearingLocation(
            trialHearingLocation,
            caseData,
            authorisation
        ));

        return djTrialTemplateBuilder.build();

    }

    private DocmosisTemplates getDocmosisTemplate() {
        return DJ_SDO_DISPOSAL;
    }

    private DocmosisTemplates getDocmosisTemplateTrial() {
        return DJ_SDO_TRIAL;
    }

    private String checkDefendantRequested(final CaseData caseData) {
        String defendantName = caseData.getDefendantDetails().getValue().getLabel();
        if (BOTH_DEFENDANTS.equals(defendantName)) {
            return caseData.getRespondent1().getPartyName()
                + ", "
                + caseData.getRespondent2().getPartyName();
        } else if (defendantName.equals(caseData.getRespondent1().getPartyName())) {
            return caseData.getRespondent1().getPartyName();
        } else {
            return caseData.getRespondent2().getPartyName();
        }
    }

    private String fillTypeBundleInfo(DisposalHearingBundleType type) {
        switch (type) {
            case DOCUMENTS:
                return "an indexed bundle of documents, with each page clearly numbered.";
            case ELECTRONIC:
                return "an electronic bundle of digital documents.";
            case SUMMARY:
                return "a case summary containing no more than 500 words.";
            default:
                return null;
        }
    }

    private String fillTypeBundleInfoTrial() {
        return "An indexed electronic bundle of documents for trial, with each page "
            + "clearly numbered including a case summary limited to 500 words";
    }

    private String fillDisposalHearingTime(DisposalHearingFinalDisposalHearingTimeEstimate type) {
        switch (type) {
            case FIFTEEN_MINUTES:
                return "15 minutes";
            case THIRTY_MINUTES:
                return "30 minutes";
            default:
                return null;
        }
    }

    private boolean getToggleValue(List<DisposalAndTrialHearingDJToggle> toggle) {
        return nonNull(toggle) && toggle.get(0).equals(SHOW);
    }

    private boolean addAdditionalDirection(CaseData caseData) {
        if (caseData.getDisposalHearingAddNewDirectionsDJ() != null
            || caseData.getTrialHearingAddNewDirectionsDJ() != null) {
            return true;
        }
        return false;
    }

    private String getCourt(CaseData caseData) {
        if (caseData.getDisposalHearingMethodInPersonDJ() != null) {
            return caseData.getDisposalHearingMethodInPersonDJ().getValue().getLabel();
        }
        return null;
    }

    public static String getHearingMethodTelephoneHearingLabel(CaseData caseData) {
        HearingMethodTelephoneHearingDJ disposalHearingMethodTelephoneHearingDJ =
            caseData.getDisposalHearingMethodTelephoneHearingDJ();
        HearingMethodTelephoneHearingDJ trialHearingMethodTelephoneHearingDJ =
            caseData.getTrialHearingMethodTelephoneHearingDJ();

        if (disposalHearingMethodTelephoneHearingDJ != null) {
            return disposalHearingMethodTelephoneHearingDJ.getLabel();
        }
        if (trialHearingMethodTelephoneHearingDJ != null) {
            return trialHearingMethodTelephoneHearingDJ.getLabel();
        }

        return null;
    }

    public static String getHearingMethodVideoConferenceLabel(CaseData caseData) {
        HearingMethodVideoConferenceDJ disposalHearingMethodVideoConferenceDJ =
            caseData.getDisposalHearingMethodVideoConferenceHearingDJ();

        HearingMethodVideoConferenceDJ trialHearingMethodVideoConferenceDJ =
            caseData.getTrialHearingMethodVideoConferenceHearingDJ();

        if (disposalHearingMethodVideoConferenceDJ != null) {
            return disposalHearingMethodVideoConferenceDJ.getLabel();
        }
        if (trialHearingMethodVideoConferenceDJ != null) {
            return trialHearingMethodVideoConferenceDJ.getLabel();
        }

        return null;
    }

    private String fillDisposalHearingMethod(DisposalHearingMethodDJ method) {
        if (method != null) {
            switch (method) {
                case disposalHearingMethodTelephoneHearing:
                    return "by telephone";
                case disposalHearingMethodInPerson:
                    return "in person";
                case disposalHearingMethodVideoConferenceHearing:
                    return "by video conference";
                default:
                    return null;
            }
        }
        return null;
    }

    private boolean checkDisposalHearingMethod(DisposalHearingMethodDJ method) {
        return method.equals(disposalHearingMethodInPerson);
    }

    private boolean getLiabilityValue(List<CaseManagementOrderAdditional> list) {
        return nonNull(list) && list.contains(OrderTypeTrialAdditionalDirectionsEmployersLiability);
    }

    private boolean checkApplicantPartyName(CaseData casedata) {
        return nonNull(casedata.getApplicant1()) && nonNull(casedata.getApplicant1().getPartyName());
    }
}

