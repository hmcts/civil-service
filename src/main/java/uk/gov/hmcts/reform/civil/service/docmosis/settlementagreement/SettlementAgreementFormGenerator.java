package uk.gov.hmcts.reform.civil.service.docmosis.settlementagreement;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.docmosis.settlementagreement.SettlementAgreementForm;
import uk.gov.hmcts.reform.civil.model.docmosis.settlementagreement.SettlementAgreementFormMapper;
import uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.docmosis.TemplateDataGenerator;

import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.SETTLEMENT_AGREEMENT_PDF;

@Service
@Getter
@RequiredArgsConstructor
public class SettlementAgreementFormGenerator implements TemplateDataGenerator<SettlementAgreementForm> {

    private final SettlementAgreementFormMapper settlementAgreementFormMapper;
    private final DocumentManagementService documentManagementService;
    private final DocumentGeneratorService documentGeneratorService;

    public CaseDocument generate(CaseData caseData, String authorisation) {
        DocmosisDocument document = documentGeneratorService.generateDocmosisDocument(getTemplateData(caseData), SETTLEMENT_AGREEMENT_PDF);
        DocmosisTemplates template = getTemplate();
        PDF pdf = new PDF(getFileName(caseData, template), document.getBytes(), DocumentType.SETTLEMENT_AGREEMENT);
        return documentManagementService.uploadDocument(authorisation, pdf);
    }

    public SettlementAgreementForm getTemplateData(CaseData caseData) {
        return settlementAgreementFormMapper.buildFormData(caseData);
    }

    private String getFileName(CaseData caseData, DocmosisTemplates template) {
        return String.format(template.getDocumentTitle(), caseData.getLegacyCaseReference());
    }

    private DocmosisTemplates getTemplate() {
        return SETTLEMENT_AGREEMENT_PDF;
    }
}
