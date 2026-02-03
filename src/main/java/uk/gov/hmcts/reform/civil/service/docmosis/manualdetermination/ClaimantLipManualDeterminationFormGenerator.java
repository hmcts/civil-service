package uk.gov.hmcts.reform.civil.service.docmosis.manualdetermination;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.enums.PaymentFrequencyClaimantResponseLRspec;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.PaymentBySetDate;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.docmosis.manualdetermination.ClaimantLipManualDeterminationForm;
import uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.docmosis.TemplateDataGenerator;
import uk.gov.hmcts.reform.civil.utils.ClaimantResponseUtils;

import java.time.LocalDate;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.CLAIMANT_LIP_MANUAL_DETERMINATION_PDF;

@Service
@Getter
@RequiredArgsConstructor
public class ClaimantLipManualDeterminationFormGenerator implements TemplateDataGenerator<ClaimantLipManualDeterminationForm> {

    private final DocumentManagementService documentManagementService;
    private final DocumentGeneratorService documentGeneratorService;
    private final ClaimantResponseUtils claimantResponseUtils;

    public CaseDocument generate(CaseData caseData, String authorisation) {
        ClaimantLipManualDeterminationForm templateData = getTemplateData(caseData);
        DocmosisTemplates template = getDocmosisTemplate();
        DocmosisDocument docmosisDocument = documentGeneratorService.generateDocmosisDocument(templateData, template);
        PDF lipManualDeterminationPdf = new PDF(getFileName(template, caseData), docmosisDocument.getBytes(), DocumentType.LIP_MANUAL_DETERMINATION);
        return documentManagementService.uploadDocument(
                authorisation,
                lipManualDeterminationPdf);
    }

    private String getFileName(DocmosisTemplates template, CaseData caseData) {
        return String.format(template.getDocumentTitle(), caseData.getLegacyCaseReference());
    }

    @Override
    public ClaimantLipManualDeterminationForm getTemplateData(CaseData caseData) {
        return new ClaimantLipManualDeterminationForm()
        .setReferenceNumber(caseData.getLegacyCaseReference())
        .setClaimIssueDate(caseData.getIssueDate())
        .setClaimantResponseSubmitDate(caseData.getApplicant1ResponseDate())
        .setDefendantAdmittedAmount(caseData.getRespondToAdmittedClaimOwingAmountPounds())
        .setClaimantRequestRepaymentBy(claimantResponseUtils.getClaimantRepaymentType(caseData))
        .setClaimResponseType(caseData.getRespondent1ClaimResponseTypeForSpec())
        .setRegularPaymentAmount(caseData.getApplicant1SuggestInstalmentsPaymentAmountForDefendantSpec())
        .setRepaymentFrequency(getRepaymentFrequency(caseData.getApplicant1SuggestInstalmentsRepaymentFrequencyForDefendantSpec()))
        .setRepaymentType(caseData.getApplicant1RepaymentOptionForDefendantSpec())
        .setFirstRepaymentDate(caseData.getApplicant1SuggestInstalmentsFirstRepaymentDateForDefendantSpec())
        .setLastRepaymentDate(claimantResponseUtils.getClaimantFinalRepaymentDate(caseData))
        .setPaymentSetDateForDefendant(getRepaymentSetByDate(caseData.getApplicant1RequestedPaymentDateForDefendantSpec()));
    }

    private DocmosisTemplates getDocmosisTemplate() {
        return CLAIMANT_LIP_MANUAL_DETERMINATION_PDF;
    }

    private String getRepaymentFrequency(PaymentFrequencyClaimantResponseLRspec claimantSuggestedRepaymentFrequency) {
        return (nonNull(claimantSuggestedRepaymentFrequency)) ? claimantSuggestedRepaymentFrequency.getLabel() : null;
    }

    private LocalDate getRepaymentSetByDate(PaymentBySetDate paymentBySetDate) {
        return (nonNull(paymentBySetDate)) ? paymentBySetDate.getPaymentSetDate() : null;
    }
}
