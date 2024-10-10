package uk.gov.hmcts.reform.civil.service.docmosis.cosc;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.helpers.judgmentsonline.JudgmentsOnlineHelper;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.docmosis.cosc.CertificateOfDebtForm;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentDetails;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentState;
import uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.docmosis.TemplateDataGenerator;
import uk.gov.hmcts.reform.civil.utils.AddressUtils;

import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.CERTIFICATE_OF_DEBT_PAYMENT;

@Slf4j
@Service
@RequiredArgsConstructor
public class CertificateOfDebtGenerator implements TemplateDataGenerator<CertificateOfDebtForm> {

    public static final String MARKED_TO_SHOW_THAT_THE_DEBT_IS_SATISFIED = "Marked to show that the debt is satisfied.";
    public static final String REMOVED_AS_PAYMENT_HAS_BEEN_MADE_IN_FULL_WITHIN_ONE_MONTH_OF_JUDGMENT = "REMOVED (as payment has been made in full within one month of judgment).";
    private final DocumentManagementService documentManagementService;
    private final DocumentGeneratorService documentGeneratorService;

    public CaseDocument generateDoc(CaseData caseData, String authorisation) {
        CertificateOfDebtForm templateData = getCertificateOfDebtTemplateData(caseData);
        DocmosisTemplates docmosisTemplate = CERTIFICATE_OF_DEBT_PAYMENT;
        DocmosisDocument docmosisDocument =
            documentGeneratorService.generateDocmosisDocument(templateData, docmosisTemplate);

        return documentManagementService.uploadDocument(
            authorisation,
            new PDF(
                getFileName(caseData, docmosisTemplate),
                docmosisDocument.getBytes(),
                DocumentType.CERTIFICATE_OF_DEBT_PAYMENT
            )
        );
    }

    private String getFileName(CaseData caseData, DocmosisTemplates docmosisTemplate) {
        return String.format(docmosisTemplate.getDocumentTitle(), caseData.getLegacyCaseReference());
    }

    private CertificateOfDebtForm getCertificateOfDebtTemplateData(CaseData caseData) {
        var certificateOfDebtForm = CertificateOfDebtForm.builder()
            .claimNumber(caseData.getLegacyCaseReference())
            .defendantFullName(caseData.getRespondent1().getPartyName())
            .defendantAddress(AddressUtils.formatAddress(caseData.getRespondent1().getPrimaryAddress()))
            .judgmentOrderDate(caseData.getActiveJudgment().getIssueDate())
            .judgmentTotalAmount(caseData.getActiveJudgment().getTotalAmount())
            .defendantFullNameFromJudgment(caseData.getActiveJudgment().getDefendant1Name())
            .defendantAddressFromJudgment(JudgmentsOnlineHelper
                                              .formatAddress(caseData.getActiveJudgment().getDefendant1Address()))
            //.applicationIssuedDate(caseData.getGeneralApplications())
            .judgmentStatusText(getJudgmentText(caseData.getActiveJudgment()))
            .courtLocationName(caseData.getCourtLocation().toString());
        return certificateOfDebtForm.build();
    }

    private String getJudgmentText(JudgmentDetails activeJudgment) {

        if (JudgmentState.SATISFIED.equals(activeJudgment.getState())) {
            return MARKED_TO_SHOW_THAT_THE_DEBT_IS_SATISFIED;
        } else if (JudgmentState.CANCELLED.equals(activeJudgment.getState())) {
            return REMOVED_AS_PAYMENT_HAS_BEEN_MADE_IN_FULL_WITHIN_ONE_MONTH_OF_JUDGMENT;
        }
        return MARKED_TO_SHOW_THAT_THE_DEBT_IS_SATISFIED;
    }

}
