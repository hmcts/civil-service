package uk.gov.hmcts.reform.civil.service.docmosis.dj;

import lombok.RequiredArgsConstructor;
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
import uk.gov.hmcts.reform.civil.enums.sdo.AddOrRemoveToggle;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.DisposalHearingBundleDJ;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.docmosis.dj.DefaultJudgmentSDOOrderForm;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentHearingLocationHelper;
import uk.gov.hmcts.reform.civil.service.docmosis.TemplateDataGenerator;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.enums.dj.CaseManagementOrderAdditional.OrderTypeTrialAdditionalDirectionsEmployersLiability;
import static uk.gov.hmcts.reform.civil.enums.dj.DisposalAndTrialHearingDJToggle.SHOW;
import static uk.gov.hmcts.reform.civil.enums.dj.DisposalHearingMethodDJ.disposalHearingMethodInPerson;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.DJ_SDO_R2_DISPOSAL;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.DJ_SDO_R2_TRIAL;
import static uk.gov.hmcts.reform.civil.utils.DocumentUtils.getDynamicListValueLabel;
import static uk.gov.hmcts.reform.civil.utils.DocumentUtils.getHearingTimeEstimateLabel;
import static uk.gov.hmcts.reform.civil.utils.DocumentUtils.getDisposalHearingTimeEstimateDJ;

@Service
@RequiredArgsConstructor
public class DefaultJudgmentOrderFormGenerator implements TemplateDataGenerator<DefaultJudgmentSDOOrderForm> {

    private final DocumentManagementService documentManagementService;
    private final DocumentGeneratorService documentGeneratorService;
    private final FeatureToggleService featureToggleService;
    private final DocumentHearingLocationHelper locationHelper;
    private final UserService userService;
    private static final String BOTH_DEFENDANTS = "Both Defendants";
    public static final String DISPOSAL_HEARING = "DISPOSAL_HEARING";

    public CaseDocument generate(CaseData caseData, String authorisation) {
        DefaultJudgmentSDOOrderForm templateData = getDefaultJudgmentForms(caseData, authorisation);
        DocmosisTemplates docmosisTemplate = caseData.getCaseManagementOrderSelection().equals(DISPOSAL_HEARING)
            ? getDocmosisTemplate()
            : getDocmosisTemplateTrial();
        DocmosisDocument docmosisDocument =
            documentGeneratorService.generateDocmosisDocument(templateData, docmosisTemplate);

        UserDetails userDetails = userService.getUserDetails(authorisation);
        String judgeName = userDetails.getFullName();

        return documentManagementService.uploadDocument(
            authorisation,
            new PDF(
                getFileName(judgeName),
                docmosisDocument.getBytes(),
                DocumentType.DEFAULT_JUDGMENT_SDO_ORDER
            )
        );
    }

    @Override
    public DefaultJudgmentSDOOrderForm getTemplateData(CaseData caseData) throws IOException {
        return null;
    }

    private String getFileName(String judgeName) {
        StringBuilder updatedFileName = new StringBuilder();
        updatedFileName.append(LocalDate.now()).append("_").append(judgeName).append(".pdf");
        return updatedFileName.toString();
    }

    private DefaultJudgmentSDOOrderForm getDefaultJudgmentForms(CaseData caseData, String authorisation) {
        return caseData.getCaseManagementOrderSelection().equals(DISPOSAL_HEARING)
            ? getDefaultJudgmentFormHearing(caseData, authorisation)
            : getDefaultJudgmentFormTrial(caseData, authorisation);
    }

    private DefaultJudgmentSDOOrderForm getDefaultJudgmentFormHearing(CaseData caseData, String authorisation) {
        UserDetails userDetails = userService.getUserDetails(authorisation);

        boolean isJudge = false;

        if (userDetails.getRoles() != null) {
            isJudge = userDetails.getRoles().stream()
                .anyMatch(s -> s != null && s.toLowerCase().contains("judge"));
        }
        String courtLocation = getCourt(caseData);
        var djOrderForm = new DefaultJudgmentSDOOrderForm()
            .setWrittenByJudge(isJudge)
            .setJudgeNameTitle(caseData.getDisposalHearingJudgesRecitalDJ().getJudgeNameTitle())
            .setCaseNumber(caseData.getLegacyCaseReference())
            .setDisposalHearingBundleDJ(caseData.getDisposalHearingBundleDJ())
            .setDisposalHearingBundleDJAddSection(nonNull(caseData.getDisposalHearingBundleDJ()))
            .setTypeBundleInfo(DefaultJudgmentOrderFormGenerator.fillTypeBundleInfo(caseData))
            .setDisposalHearingDisclosureOfDocumentsDJ(caseData.getDisposalHearingDisclosureOfDocumentsDJ())
            .setDisposalHearingDisclosureOfDocumentsDJAddSection(nonNull(
                caseData.getDisposalHearingDisclosureOfDocumentsDJ()))
            .setDisposalHearingWitnessOfFactDJ(caseData.getDisposalHearingWitnessOfFactDJ())
            .setDisposalHearingWitnessOfFactDJAddSection(nonNull(caseData.getDisposalHearingWitnessOfFactDJ()))
            .setDisposalHearingFinalDisposalHearingDJ(caseData.getDisposalHearingFinalDisposalHearingDJ())
            .setDisposalHearingMethodDJ(caseData.getDisposalHearingMethodDJ())
            .setDisposalHearingAttendance(fillDisposalHearingMethod(caseData.getDisposalHearingMethodDJ()))
            .setDisposalHearingFinalDisposalHearingDJAddSection(nonNull(
                caseData.getDisposalHearingFinalDisposalHearingDJ()))
            .setDisposalHearingFinalDisposalHearingDJAddSection(nonNull(
                caseData.getDisposalHearingMethodDJ()))
            .setDisposalHearingFinalDisposalHearingDJAddSection(nonNull(fillDisposalHearingMethod(caseData
                                                                                                   .getDisposalHearingMethodDJ())))
            .setCourtLocation(courtLocation)
            .setTelephoneOrganisedBy(getHearingMethodTelephoneHearingLabel(caseData))
            .setVideoConferenceOrganisedBy(getHearingMethodVideoConferenceLabel(caseData))
            .setDisposalHearingTime(nonNull(caseData.getDisposalHearingFinalDisposalHearingDJ())
                                     ? fillDisposalHearingTime(
                caseData.getDisposalHearingFinalDisposalHearingDJ().getTime()) : null)
            .setDisposalHearingJudgesRecitalDJ(caseData.getDisposalHearingJudgesRecitalDJ())
            .setDisposalHearingMedicalEvidenceDJ(caseData.getDisposalHearingMedicalEvidenceDJ())
            .setDisposalHearingMedicalEvidenceDJAddSection(nonNull(caseData.getDisposalHearingMedicalEvidenceDJ()))
            .setDisposalHearingNotesDJ(caseData.getDisposalHearingNotesDJ())
            .setHasNewDirections(addAdditionalDirection(caseData))
            .setDisposalHearingAddNewDirectionsDJ(caseData.getDisposalHearingAddNewDirectionsDJ())
            .setDisposalHearingQuestionsToExpertsDJ(caseData.getDisposalHearingQuestionsToExpertsDJ())
            .setDisposalHearingQuestionsToExpertsDJAddSection(nonNull(caseData.getDisposalHearingQuestionsToExpertsDJ()))
            .setDisposalHearingSchedulesOfLossDJ(caseData.getDisposalHearingSchedulesOfLossDJ())
            .setDisposalHearingSchedulesOfLossDJAddSection(nonNull(caseData.getDisposalHearingSchedulesOfLossDJ()))
            .setDisposalHearingClaimSettlingAddSection(getToggleValue(caseData.getDisposalHearingClaimSettlingDJToggle()))
            .setDisposalHearingCostsAddSection(getToggleValue(caseData.getDisposalHearingCostsDJToggle()))
            .setApplicant(checkApplicantPartyName(caseData)
                           ? caseData.getApplicant1().getPartyName().toUpperCase() : null)
            .setRespondent(checkDefendantRequested(caseData).toUpperCase())
            .setCaseManagementLocation(locationHelper.getHearingLocation(null, caseData, authorisation));

        djOrderForm
            .setDisposalHearingOrderMadeWithoutHearingDJ(caseData.getDisposalHearingOrderMadeWithoutHearingDJ())
            .setDisposalHearingFinalDisposalHearingTimeDJ(caseData.getDisposalHearingFinalDisposalHearingTimeDJ())
            .setDisposalHearingTimeEstimateDJ(getDisposalHearingTimeEstimateDJ(caseData.getDisposalHearingFinalDisposalHearingTimeDJ()));

        djOrderForm.setHearingLocation(locationHelper.getHearingLocation(courtLocation, caseData, authorisation));

        djOrderForm.setHasDisposalHearingWelshSectionDJ(getToggleValue(caseData.getSdoR2DisposalHearingUseOfWelshLangToggleDJ()));
        djOrderForm.setWelshLanguageDescriptionDJ(caseData.getSdoR2DisposalHearingWelshLanguageDJ() != null
                                                          ? caseData.getSdoR2DisposalHearingWelshLanguageDJ().getDescription() : null);

        return djOrderForm;
    }

    private DefaultJudgmentSDOOrderForm getDefaultJudgmentFormTrial(CaseData caseData, String authorisation) {
        String trialHearingLocation = getDynamicListValueLabel(caseData.getTrialHearingMethodInPersonDJ());
        UserDetails userDetails = userService.getUserDetails(authorisation);

        boolean isJudge = false;

        if (userDetails.getRoles() != null) {
            isJudge = userDetails.getRoles().stream()
                .anyMatch(s -> s != null && s.toLowerCase().contains("judge"));
        }
        var djTrialTemplate = new DefaultJudgmentSDOOrderForm()
            .setWrittenByJudge(isJudge)
            .setJudgeNameTitle(caseData.getTrialHearingJudgesRecitalDJ().getJudgeNameTitle())
            .setCaseNumber(caseData.getLegacyCaseReference())
            .setTrialBuildingDispute(caseData.getTrialBuildingDispute())
            .setTrialBuildingDisputeAddSection(nonNull(caseData.getTrialBuildingDispute()))
            .setTrialClinicalNegligence(caseData.getTrialClinicalNegligence())
            .setTrialClinicalNegligenceAddSection(nonNull(caseData.getTrialClinicalNegligence()))
            .setTrialCreditHire(caseData.getTrialCreditHire())
            .setTrialCreditHireAddSection(nonNull(caseData.getTrialCreditHire()))
            .setTrialHearingJudgesRecitalDJ(caseData.getTrialHearingJudgesRecitalDJ())
            .setSdoDJR2TrialCreditHireAddSection(nonNull(caseData.getSdoDJR2TrialCreditHire()))
            .setSdoDJR2TrialCreditHireDetailsAddSection(
                (nonNull(caseData.getSdoDJR2TrialCreditHire())
                    && nonNull(caseData.getSdoDJR2TrialCreditHire().getDetailsShowToggle())
                    && caseData.getSdoDJR2TrialCreditHire().getDetailsShowToggle()
                    .equals(List.of(AddOrRemoveToggle.ADD))))
            .setTrialHearingTrialDJ(caseData.getTrialHearingTrialDJ())
            .setTypeBundleInfo(DefaultJudgmentOrderFormGenerator.fillTypeBundleInfo(caseData))
            .setTrialHearingTrialDJAddSection(
                getToggleValue(caseData.getTrialHearingTrialDJToggle()))
            .setTrialHearingNotesDJ(caseData.getTrialHearingNotesDJ())
            .setHasNewDirections(addAdditionalDirection(caseData))
            .setTrialHearingAddNewDirectionsDJ(caseData.getTrialHearingAddNewDirectionsDJ())
            .setTrialHearingDisclosureOfDocumentsDJ(caseData.getTrialHearingDisclosureOfDocumentsDJ())
            .setTrialHearingDisclosureOfDocumentsDJAddSection(
                getToggleValue(caseData.getTrialHearingDisclosureOfDocumentsDJToggle()))
            .setTrialPersonalInjury(caseData.getTrialPersonalInjury())
            .setTrialPersonalInjuryAddSection(nonNull(caseData.getTrialPersonalInjury()))
            .setTrialHearingSchedulesOfLossDJ(caseData.getTrialHearingSchedulesOfLossDJ())
            .setTrialHearingSchedulesOfLossDJAddSection(
                getToggleValue(caseData.getTrialHearingSchedulesOfLossDJToggle()))
            .setTrialRoadTrafficAccident(caseData.getTrialRoadTrafficAccident())
            .setTrialRoadTrafficAccidentAddSection(nonNull(caseData.getTrialRoadTrafficAccident()))
            .setTrialHearingWitnessOfFactDJ(caseData.getTrialHearingWitnessOfFactDJ())
            .setTrialHearingWitnessOfFactDJAddSection(
                getToggleValue(caseData.getTrialHearingWitnessOfFactDJToggle()))
            .setTrialHearingDisputeAddSection(getToggleValue(caseData.getTrialHearingAlternativeDisputeDJToggle()))
            .setTrialHearingVariationsAddSection(getToggleValue(caseData.getTrialHearingVariationsDirectionsDJToggle()))
            .setTrialHearingSettlementAddSection(getToggleValue(caseData.getTrialHearingSettlementDJToggle()))
            .setTrialHearingCostsAddSection(getToggleValue(caseData.getTrialHearingCostsToggle()))
            .setTrialEmployerLiabilityAddSection(getLiabilityValue(caseData.getCaseManagementOrderAdditional()))
            .setTrialHearingMethodDJ(caseData.getTrialHearingMethodDJ())
            .setTelephoneOrganisedBy(getHearingMethodTelephoneHearingLabel(caseData))
            .setVideoConferenceOrganisedBy(getHearingMethodVideoConferenceLabel(caseData))
            .setTrialHousingDisrepair(caseData.getTrialHousingDisrepair())
            .setTrialHousingDisrepairAddSection(nonNull(caseData.getTrialHousingDisrepair()))
            .setTrialHearingMethodInPersonAddSection(checkDisposalHearingMethod(caseData.getTrialHearingMethodDJ()))
            .setTrialHearingLocation(trialHearingLocation)
            .setApplicant(checkApplicantPartyName(caseData)
                           ? caseData.getApplicant1().getPartyName().toUpperCase() : null)
            .setRespondent(checkDefendantRequested(caseData).toUpperCase())
            .setTrialHearingTimeDJ(caseData.getTrialHearingTimeDJ())
            .setDisposalHearingDateToToggle(caseData.getTrialHearingTimeDJ() != null
                                             && caseData.getTrialHearingTimeDJ().getDateToToggle() != null)
            .setTrialOrderMadeWithoutHearingDJ(caseData.getTrialOrderMadeWithoutHearingDJ())
            .setTrialHearingTimeEstimateDJ(getHearingTimeEstimateLabel(caseData.getTrialHearingTimeDJ()))
            .setCaseManagementLocation(locationHelper.getHearingLocation(null, caseData, authorisation))
            .setHearingLocation(locationHelper.getHearingLocation(
                                trialHearingLocation,
                                caseData,
                                authorisation
                            ));

        djTrialTemplate.setSdoDJR2TrialCreditHire(caseData.getSdoDJR2TrialCreditHire());
        djTrialTemplate.setHasTrialHearingWelshSectionDJ(getToggleValue(caseData.getSdoR2TrialUseOfWelshLangToggleDJ()));
        djTrialTemplate.setWelshLanguageDescriptionDJ(caseData.getSdoR2TrialWelshLanguageDJ() != null
                                                              ? caseData.getSdoR2TrialWelshLanguageDJ().getDescription() : null);

        return djTrialTemplate;

    }

    private DocmosisTemplates getDocmosisTemplate() {
        return DJ_SDO_R2_DISPOSAL;
    }

    private DocmosisTemplates getDocmosisTemplateTrial() {
        return  DJ_SDO_R2_TRIAL;
    }

    private String checkDefendantRequested(final CaseData caseData) {
        if (caseData.isRespondent1NotRepresented()) {
            return caseData.getRespondent1().getPartyName();
        }
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

    static String fillTypeBundleInfo(CaseData caseData) {
        DisposalHearingBundleDJ disposalHearingBundle = caseData.getDisposalHearingBundleDJ();

        if (disposalHearingBundle != null) {
            List<DisposalHearingBundleType> types = disposalHearingBundle.getType();
            StringBuilder stringBuilder = new StringBuilder();

            if (disposalHearingBundle.getType().size() == 3) {
                stringBuilder.append(uk.gov.hmcts.reform.civil.enums.sdo.DisposalHearingBundleType.DOCUMENTS.getLabel());
                stringBuilder.append(" / " + uk.gov.hmcts.reform.civil.enums.sdo.DisposalHearingBundleType.ELECTRONIC.getLabel());
                stringBuilder.append(" / " + DisposalHearingBundleType.SUMMARY.getLabel());
            } else if (disposalHearingBundle.getType().size() == 2) {
                stringBuilder.append(types.get(0).getLabel());
                stringBuilder.append(" / " + types.get(1).getLabel());
            } else {
                stringBuilder.append(types.get(0).getLabel());
            }

            return stringBuilder.toString();
        }

        return "";
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
        return caseData.getDisposalHearingAddNewDirectionsDJ() != null
            || caseData.getTrialHearingAddNewDirectionsDJ() != null;
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

