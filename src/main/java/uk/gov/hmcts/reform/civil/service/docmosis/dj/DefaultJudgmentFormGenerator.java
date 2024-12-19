package uk.gov.hmcts.reform.civil.service.docmosis.dj;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.enums.DJPaymentTypeSelection;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.docmosis.dj.DefaultJudgmentForm;
import uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GENERATE_DJ_FORM_SPEC;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.N121;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.N121_SPEC;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.N121_SPEC_NON_IMMEDIATE;

@Service
@Slf4j
@RequiredArgsConstructor
public class DefaultJudgmentFormGenerator {

    private final DocumentManagementService documentManagementService;
    private final DocumentGeneratorService documentGeneratorService;
    private final DefaultJudgmentFormBuilder defaultJudgmentFormBuilder;
    private final NonImmediatePaymentTypeDefaultJudgmentFormBuilder nonImmediatePaymentTypeDefaultJudgmentFormBuilder;

    public List<CaseDocument> generate(CaseData caseData, String authorisation, String event) {
        List<CaseDocument> caseDocuments = new ArrayList<>();
        DocmosisDocument docmosisDocument2;
        List<DefaultJudgmentForm> templateData = getDefaultJudgmentForms(caseData, event, getDefaultJudgmentBuilder(event, caseData));
        DocmosisTemplates docmosisTemplate = getDocmosisTemplate(event, caseData);
        log.info("Template for case {} for caseId {}", docmosisTemplate.getTemplate(), caseData.getCcdCaseReference());
        DocmosisDocument docmosisDocument1 =
            documentGeneratorService.generateDocmosisDocument(templateData.get(0), docmosisTemplate);
        caseDocuments.add(documentManagementService.uploadDocument(
            authorisation,
            new PDF(
                getFileName(caseData, docmosisTemplate),
                docmosisDocument1.getBytes(),
                DocumentType.DEFAULT_JUDGMENT
            )
        ));
        if (templateData.size() > 1) {
            docmosisDocument2 =
                documentGeneratorService.generateDocmosisDocument(templateData.get(1), docmosisTemplate);
            caseDocuments.add(documentManagementService.uploadDocument(
                authorisation,
                new PDF(
                    getFileName(caseData, docmosisTemplate),
                    docmosisDocument2.getBytes(),
                    DocumentType.DEFAULT_JUDGMENT
                )
            ));
        }
        return caseDocuments;
    }

    private String getFileName(CaseData caseData, DocmosisTemplates docmosisTemplate) {
        return String.format(docmosisTemplate.getDocumentTitle(), caseData.getLegacyCaseReference());
    }

    private List<DefaultJudgmentForm> getDefaultJudgmentForms(CaseData caseData, String event,
                                                              StandardDefaultJudgmentBuilder standardDefaultJudgmentBuilder) {
        List<DefaultJudgmentForm> defaultJudgmentForms = new ArrayList<>();

        defaultJudgmentForms.add(standardDefaultJudgmentBuilder.getDefaultJudgmentForm(caseData, caseData.getRespondent1(), event, true));
        if (caseData.getRespondent2() != null) {
            defaultJudgmentForms.add(standardDefaultJudgmentBuilder.getDefaultJudgmentForm(caseData, caseData.getRespondent2(), event, false));
        }
        return defaultJudgmentForms;

    }

    private DocmosisTemplates getDocmosisTemplate(String event, CaseData caseData) {
        if (event.equals(GENERATE_DJ_FORM_SPEC.name())) {
            if (!caseData.getPaymentTypeSelection().equals(DJPaymentTypeSelection.IMMEDIATELY)) {
                return N121_SPEC_NON_IMMEDIATE;
            }
            return N121_SPEC;
        } else {
            return N121;
        }
    }

    private StandardDefaultJudgmentBuilder getDefaultJudgmentBuilder(String event, CaseData caseData) {
        if (event.equals(GENERATE_DJ_FORM_SPEC.name())) {
            if (!caseData.getPaymentTypeSelection().equals(DJPaymentTypeSelection.IMMEDIATELY)) {
                return nonImmediatePaymentTypeDefaultJudgmentFormBuilder;
            }
        }
        return defaultJudgmentFormBuilder;
    }
}
