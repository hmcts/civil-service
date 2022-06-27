package uk.gov.hmcts.reform.civil.service.docmosis.dj;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.CaseData;
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
            .disposalHearingDisclosureOfDocumentsDJ(caseData.getDisposalHearingDisclosureOfDocumentsDJ())
            .disposalHearingFinalDisposalHearingDJ(caseData.getDisposalHearingFinalDisposalHearingDJ())
            .disposalHearingJudgesRecitalDJ(caseData.getDisposalHearingJudgesRecitalDJ())
            .disposalHearingMedicalEvidenceDJ(caseData.getDisposalHearingMedicalEvidenceDJ())
            .disposalHearingNotesDJ(caseData.getDisposalHearingNotesDJ())
            .disposalHearingQuestionsToExpertsDJ(caseData.getDisposalHearingQuestionsToExpertsDJ())
            .disposalHearingSchedulesOfLossDJ(caseData.getDisposalHearingSchedulesOfLossDJ())
            .disposalHearingWitnessOfFactDJ(caseData.getDisposalHearingWitnessOfFactDJ())
            .applicant(caseData.getApplicant1().getPartyName())
            .respondent(caseData.getRespondent1().getPartyName()).build();
    }

    private DefaultJudgmentSDOOrderForm getDefaultJudgmentFormTrial(CaseData caseData) {
        return DefaultJudgmentSDOOrderForm.builder()
            .caseNumber(caseData.getLegacyCaseReference())
            .trialBuildingDispute(caseData.getTrialBuildingDispute())
            .trialClinicalNegligence(caseData.getTrialClinicalNegligence())
            .trialCreditHire(caseData.getTrialCreditHire())
            .trialHearingJudgesRecitalDJ(caseData.getTrialHearingJudgesRecitalDJ())
            .trialHearingTrialDJ(caseData.getTrialHearingTrialDJ())
            .trialHearingNotesDJ(caseData.getTrialHearingNotesDJ())
            .trialHearingDisclosureOfDocumentsDJ(caseData.getTrialHearingDisclosureOfDocumentsDJ())
            .trialPersonalInjury(caseData.getTrialPersonalInjury())
            .trialHearingSchedulesOfLossDJ(caseData.getTrialHearingSchedulesOfLossDJ())
            .trialRoadTrafficAccident(caseData.getTrialRoadTrafficAccident())
            .trialHearingWitnessOfFactDJ(caseData.getTrialHearingWitnessOfFactDJ())
            .applicant(caseData.getApplicant1().getPartyName())
            .respondent(caseData.getRespondent1().getPartyName()).build();
    }

    private DocmosisTemplates getDocmosisTemplate() {
        return DJ_SDO_DISPOSAL;
    }

    private DocmosisTemplates getDocmosisTemplateTrial() {
        return DJ_SDO_TRIAL;
    }

    private Boolean checkIfBothDefendants(CaseData caseData) {
        return BOTH_DEFENDANTS.equals(caseData.getDefendantDetails().getValue().getLabel());
    }

    private String checkDefendantRequested(final CaseData caseData, String defendantName) {
        if (BOTH_DEFENDANTS.equals(caseData.getDefendantDetails().getValue().getLabel())) {
            return String.format(caseData.getRespondent1().getPartyName()
                                     + " \n "
                                     + caseData.getRespondent2().getPartyName());
        }
    }
}

