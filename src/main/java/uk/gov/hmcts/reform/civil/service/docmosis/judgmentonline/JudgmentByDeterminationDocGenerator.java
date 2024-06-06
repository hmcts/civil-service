package uk.gov.hmcts.reform.civil.service.docmosis.judgmentonline;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.docmosis.judgmentonline.JudgmentByDeterminationDocForm;
import uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.*;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.*;

@Slf4j
@RequiredArgsConstructor
@Service
public class JudgmentByDeterminationDocGenerator {

    private final String applicant1 = "applicant1";
    private final String applicant2 = "applicant2";
    private final String respondent1 = "respondent1";
    private final String respondent2 = "respondent2";
    private final DocumentManagementService documentManagementService;
    private final DocumentGeneratorService documentGeneratorService;
    public List<CaseDocument> generateDocs(CaseData caseData, String authorisation, String event) {
        List<JudgmentByDeterminationDocForm> defaultJudgmentForms = new ArrayList<>();
        if (event.equals(GEN_DJ_FORM_NON_DIVERGENT_SPEC_CLAIMANT.name())) {
            defaultJudgmentForms.add(getDefaultJudgmentFormNonDivergent(caseData, caseData.getApplicant1(), event, applicant1));
            if (caseData.getApplicant2() != null) {
                defaultJudgmentForms.add(getDefaultJudgmentFormNonDivergent(caseData, caseData.getApplicant2(), event, applicant2));
            }
        } else {
            defaultJudgmentForms.add(getDefaultJudgmentFormNonDivergent(caseData, caseData.getRespondent1(), event,
                                                                        respondent1));
            if (caseData.getRespondent2() != null) {
                defaultJudgmentForms.add(getDefaultJudgmentFormNonDivergent(caseData, caseData.getRespondent2(),
                                                                            event, respondent2));
            }
        }
        return generateDocmosisDocsForNonDivergent(defaultJudgmentForms, authorisation, caseData, event);
    }

    private List<CaseDocument> generateDocmosisDocsForNonDivergent(List<JudgmentByDeterminationDocForm> defaultJudgmentForms,
                                                                   String authorisation, CaseData caseData, String event) {
        List<CaseDocument> caseDocuments = new ArrayList<>();
        for (int i = 0; i < defaultJudgmentForms.size(); i++) {
            JudgmentByDeterminationDocForm defaultJudgmentForm = defaultJudgmentForms.get(i);
            DocumentType documentType = getDocumentTypeBasedOnEvent(i, event);
            DocmosisTemplates docmosisTemplate = getDocmosisTemplate(event);
            DocmosisDocument docmosisDocument = documentGeneratorService.generateDocmosisDocument(defaultJudgmentForm,
                                                                                                  docmosisTemplate);
            caseDocuments.add(documentManagementService.uploadDocument(
                authorisation,
                new PDF(
                    getFileName(caseData, docmosisTemplate),
                    docmosisDocument.getBytes(),
                    documentType
                )
            ));
        }
        return caseDocuments;
    }

    private String getFileName(CaseData caseData, DocmosisTemplates docmosisTemplate) {
        return String.format(docmosisTemplate.getDocumentTitle(), caseData.getLegacyCaseReference());
    }

    private DocmosisTemplates getDocmosisTemplate(String event) {
        if (event.equals(GENERATE_DJ_FORM_SPEC.name())) {
            return N121_SPEC;
        } else if (event.equals(GEN_DJ_FORM_NON_DIVERGENT_SPEC_CLAIMANT.name())) {
            return N121_SPEC_CLAIMANT;
        } else if (event.equals(GEN_DJ_FORM_NON_DIVERGENT_SPEC_DEFENDANT.name())) {
            return N121_SPEC_DEFENDANT;
        } else {
            return N121;
        }
    }

    private DocumentType getDocumentTypeBasedOnEvent(int i, String event) {
        if (event.equals(GEN_DJ_FORM_NON_DIVERGENT_SPEC_CLAIMANT.name())) {
            if (i == 0) {
                return DocumentType.DEFAULT_JUDGMENT_CLAIMANT1;
            } else {
                return DocumentType.DEFAULT_JUDGMENT_CLAIMANT2;
            }
        } else {
            if (i == 0) {
                return DocumentType.DEFAULT_JUDGMENT_DEFENDANT1;
            } else {
                return DocumentType.DEFAULT_JUDGMENT_DEFENDANT2;
            }
        }
    }

    private JudgmentByDeterminationDocForm getDefaultJudgmentFormNonDivergent(CaseData caseData,
                                                                   uk.gov.hmcts.reform.civil.model.Party party,
                                                                   String event, String partyType) {
        JudgmentByDeterminationDocForm.JudgmentByDeterminationDocFormBuilder builder = JudgmentByDeterminationDocForm.builder();
        return builder.build();
    }
}
