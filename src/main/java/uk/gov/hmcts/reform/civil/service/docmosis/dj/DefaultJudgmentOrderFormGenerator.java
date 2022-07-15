package uk.gov.hmcts.reform.civil.service.docmosis.dj;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.dj.CaseManagementOrderAdditional;
import uk.gov.hmcts.reform.civil.enums.dj.DisposalAndTrialHearingDJToggle;
import uk.gov.hmcts.reform.civil.enums.dj.DisposalHearingMethodDJ;
import uk.gov.hmcts.reform.civil.enums.sdo.DisposalHearingBundleType;
import uk.gov.hmcts.reform.civil.enums.sdo.DisposalHearingFinalDisposalHearingTimeEstimate;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialHearingTrial;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.docmosis.dj.DefaultJudgmentSDOOrderForm;
import uk.gov.hmcts.reform.civil.model.documents.CaseDocument;
import uk.gov.hmcts.reform.civil.model.documents.DocumentType;
import uk.gov.hmcts.reform.civil.model.documents.PDF;
import uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.docmosis.TemplateDataGenerator;
import uk.gov.hmcts.reform.civil.service.documentmanagement.DocumentManagementService;

import java.io.IOException;
import java.util.List;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.enums.dj.CaseManagementOrderAdditional.OrderTypeTrialAdditionalDirectionsEmployersLiability;
import static uk.gov.hmcts.reform.civil.enums.dj.DisposalAndTrialHearingDJToggle.SHOW;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.DJ_SDO_DISPOSAL;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.DJ_SDO_TRIAL;

@Service
@RequiredArgsConstructor
public class DefaultJudgmentOrderFormGenerator implements TemplateDataGenerator<DefaultJudgmentSDOOrderForm> {

    private final DocumentManagementService documentManagementService;
    private final DocumentGeneratorService documentGeneratorService;
    private static final String BOTH_DEFENDANTS = "Both Defendants";
    public static final String DISPOSAL_HEARING = "DISPOSAL_HEARING";

    public CaseDocument generate(CaseData caseData, String authorisation) {
        DefaultJudgmentSDOOrderForm templateData = getDefaultJudgmentForms(caseData);
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

    private DefaultJudgmentSDOOrderForm getDefaultJudgmentForms(CaseData caseData) {
        return caseData.getCaseManagementOrderSelection().equals(DISPOSAL_HEARING)
            ? getDefaultJudgmentFormHearing(caseData)
            : getDefaultJudgmentFormTrial(caseData);
    }

    private DefaultJudgmentSDOOrderForm getDefaultJudgmentFormHearing(CaseData caseData) {
        return DefaultJudgmentSDOOrderForm.builder()
            .caseNumber(caseData.getLegacyCaseReference())
            .disposalHearingBundleDJ(caseData.getDisposalHearingBundleDJ())
            .disposalHearingBundleDJAddSection(nonNull(caseData.getDisposalHearingBundleDJ())
                                                   ? "true" : "false" )
            .typeBundleInfo(nonNull(caseData.getDisposalHearingBundleDJ())
                                ? fillTypeBundleInfo(caseData.getDisposalHearingBundleDJ().getType()) : null )
            .disposalHearingDisclosureOfDocumentsDJ(caseData.getDisposalHearingDisclosureOfDocumentsDJ())
            .disposalHearingDisclosureOfDocumentsDJAddSection(nonNull(
                caseData.getDisposalHearingDisclosureOfDocumentsDJ()) ? "true" : "false" )
            .disposalHearingWitnessOfFactDJ(caseData.getDisposalHearingWitnessOfFactDJ())
            .disposalHearingWitnessOfFactDJAddSection(nonNull(caseData.getDisposalHearingWitnessOfFactDJ())
                                     ? "true" : "false" )
            .disposalHearingFinalDisposalHearingDJ(caseData.getDisposalHearingFinalDisposalHearingDJ())
            .disposalHearingFinalDisposalHearingDJAddSection(nonNull(
                caseData.getDisposalHearingFinalDisposalHearingDJ()) ? "true" : "false" )
            .disposalHearingTime(nonNull(caseData.getDisposalHearingFinalDisposalHearingDJ())
                                     ? fillDisposalHearingTime(
                                         caseData.getDisposalHearingFinalDisposalHearingDJ().getTime()) : "false" )
            .disposalHearingJudgesRecitalDJ(caseData.getDisposalHearingJudgesRecitalDJ())
            .disposalHearingMedicalEvidenceDJ(caseData.getDisposalHearingMedicalEvidenceDJ())
            .disposalHearingMedicalEvidenceDJAddSection(nonNull(caseData.getDisposalHearingMedicalEvidenceDJ())
                                                            ? "true" : "false" )
            .disposalHearingNotesDJ(caseData.getDisposalHearingNotesDJ())
            .disposalHearingQuestionsToExpertsDJ(caseData.getDisposalHearingQuestionsToExpertsDJ())
            .disposalHearingQuestionsToExpertsDJAddSection(nonNull(caseData.getDisposalHearingQuestionsToExpertsDJ())
                                                               ? "true" : "false" )
            .disposalHearingSchedulesOfLossDJ(caseData.getDisposalHearingSchedulesOfLossDJ())
            .disposalHearingSchedulesOfLossDJAddSection(nonNull(caseData.getDisposalHearingSchedulesOfLossDJ())
                                                            ? "true" : "false" )
            .disposalHearingClaimSettlingAddSection(getToggleValue(caseData.getDisposalHearingClaimSettlingDJToggle()))
            .disposalHearingCostsAddSection(getToggleValue(caseData.getDisposalHearingCostsDJToggle()))
            .disposalHearingMethod(fillDisposalHearingMethod(caseData.getDisposalHearingMethodDJ()))
            .applicant(caseData.getApplicant1().getPartyName())
            .respondent(checkDefendantRequested(caseData)).build();
    }

    private DefaultJudgmentSDOOrderForm getDefaultJudgmentFormTrial(CaseData caseData) {
        return DefaultJudgmentSDOOrderForm.builder()
            .caseNumber(caseData.getLegacyCaseReference())
            .trialBuildingDispute(caseData.getTrialBuildingDispute())
            .trialBuildingDisputeAddSection(nonNull(caseData.getTrialBuildingDispute())
                                                ? "true" : "false" )
            .trialClinicalNegligence(caseData.getTrialClinicalNegligence())
            .trialClinicalNegligenceAddSection(nonNull(caseData.getTrialClinicalNegligence())
                                                   ? "true" : "false" )
            .trialCreditHire(caseData.getTrialCreditHire())
            .trialCreditHireAddSection(nonNull(caseData.getTrialCreditHire())
                                           ? "true" : "false" )
            .trialHearingJudgesRecitalDJ(caseData.getTrialHearingJudgesRecitalDJ())
            .trialHearingTrialDJ(caseData.getTrialHearingTrialDJ())
            .trialHearingTrialDJAddSection(
                getToggleValue(caseData.getTrialHearingTrialDJToggle()))
            .trialDays(getTrialDays(caseData.getTrialHearingTrialDJ()))
            .trialHearingNotesDJ(caseData.getTrialHearingNotesDJ())
            .trialHearingDisclosureOfDocumentsDJ(caseData.getTrialHearingDisclosureOfDocumentsDJ())
            .trialHearingDisclosureOfDocumentsDJAddSection(
                getToggleValue(caseData.getTrialHearingDisclosureOfDocumentsDJToggle()))
            .trialPersonalInjury(caseData.getTrialPersonalInjury())
            .trialPersonalInjuryAddSection(nonNull(caseData.getTrialPersonalInjury())
                                               ? "true" : "false" )
            .trialHearingSchedulesOfLossDJ(caseData.getTrialHearingSchedulesOfLossDJ())
            .trialHearingSchedulesOfLossDJAddSection(
                getToggleValue(caseData.getTrialHearingSchedulesOfLossDJToggle()))
            .trialRoadTrafficAccident(caseData.getTrialRoadTrafficAccident())
            .trialRoadTrafficAccidentAddSection(nonNull(caseData.getTrialRoadTrafficAccident())
                                                    ? "true" : "false" )
            .trialHearingWitnessOfFactDJ(caseData.getTrialHearingWitnessOfFactDJ())
            .trialHearingWitnessOfFactDJAddSection(
                getToggleValue(caseData.getTrialHearingWitnessOfFactDJToggle()))
            .trialHearingDisputeAddSection(getToggleValue(caseData.getTrialHearingAlternativeDisputeDJToggle()))
            .trialHearingVariationsAddSection(getToggleValue(caseData.getTrialHearingVariationsDirectionsDJToggle()))
            .trialHearingSettlementAddSection(getToggleValue(caseData.getTrialHearingSettlementDJToggle()))
            .trialHearingCostsAddSection(getToggleValue(caseData.getTrialHearingCostsToggle()))
            .trialEmployerLiabilityAddSection(getLiabilityValue(caseData.getCaseManagementOrderAdditional()))
            .trialHearingMethod(fillDisposalHearingMethod(caseData.getTrialHearingMethodDJ()))
            .applicant(caseData.getApplicant1().getPartyName())
            .respondent(checkDefendantRequested(caseData)).build();
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
            case DOCUMENTS :
                return "an indexed bundle of documents, with each page clearly numbered";
            case ELECTRONIC:
                return "an electronic bundle of digital documents ";
            default:
                return null;
        }
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

    private String getToggleValue(List<DisposalAndTrialHearingDJToggle> toggle) {
        if (nonNull(toggle) && toggle.get(0).equals(SHOW)) {
            return "true";
        }
        return "false";
    }

    private String fillDisposalHearingMethod(DisposalHearingMethodDJ method) {
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

    private String getTrialDays(TrialHearingTrial trial) {
        if(nonNull(trial)) {
            long daysBetween = DAYS.between(trial.getDate1(), trial.getDate2());
            return String.valueOf(daysBetween);
        }
        return null;
    }

    private String getLiabilityValue(List<CaseManagementOrderAdditional> list) {
        if (nonNull(list) && list.contains(OrderTypeTrialAdditionalDirectionsEmployersLiability)) {
            return "true";
        }
        return "false";
    }

}

