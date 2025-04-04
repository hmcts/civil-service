package uk.gov.hmcts.reform.civil.service.docmosis.dj.nondivergent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.docmosis.dj.DefaultJudgmentForm;
import uk.gov.hmcts.reform.civil.model.documents.DocumentMetaData;
import uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.stitch.service.CivilStitchService;
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GEN_DJ_FORM_NON_DIVERGENT_SPEC_CLAIMANT;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.N121_SPEC_CLAIMANT;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.N121_SPEC_CLAIMANT_WELSH;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.N121_SPEC_DEFENDANT;

@Component
@Slf4j
@RequiredArgsConstructor
public class NonDivergentSpecDefaultJudgmentFormGenerator {

    private final DocumentManagementService documentManagementService;
    private final DocumentGeneratorService documentGeneratorService;
    private final AssignCategoryId assignCategoryId;
    private final NonDivergentSpecDefaultJudgementFormBuilder nonDivergentSpecDefaultJudgementFormBuilder;
    private final CivilStitchService civilStitchService;
    private static final String APPLICANT_1 = "applicant1";
    private static final String APPLICANT_2 = "applicant2";
    private static final String RESPONDENT_1 = "respondent1";
    private static final String RESPONDENT_2 = "respondent2";

    private String getFileName(CaseData caseData, DocmosisTemplates docmosisTemplate) {
        return String.format(docmosisTemplate.getDocumentTitle(), caseData.getLegacyCaseReference());
    }

    private DocmosisTemplates getDocmosisTemplate(String event, boolean isWelsh) {
        if (event.equals(GEN_DJ_FORM_NON_DIVERGENT_SPEC_CLAIMANT.name())) {
            return isWelsh ? N121_SPEC_CLAIMANT_WELSH : N121_SPEC_CLAIMANT;
        } else {
            return N121_SPEC_DEFENDANT;
        }
    }

    public List<CaseDocument> generateNonDivergentDocs(CaseData caseData, String authorisation, String event) {
        List<DefaultJudgmentForm> defaultJudgmentForms = new ArrayList<>();
        if (event.equals(GEN_DJ_FORM_NON_DIVERGENT_SPEC_CLAIMANT.name())) {
            defaultJudgmentForms.add(nonDivergentSpecDefaultJudgementFormBuilder.getDefaultJudgmentForm(caseData, APPLICANT_1));
            if (caseData.getApplicant2() != null) {
                defaultJudgmentForms.add(nonDivergentSpecDefaultJudgementFormBuilder.getDefaultJudgmentForm(caseData, APPLICANT_2));
            }
        } else {
            defaultJudgmentForms.add(nonDivergentSpecDefaultJudgementFormBuilder.getDefaultJudgmentForm(caseData,
                RESPONDENT_1
            ));
            if (caseData.getRespondent2() != null) {
                defaultJudgmentForms.add(nonDivergentSpecDefaultJudgementFormBuilder.getDefaultJudgmentForm(caseData, RESPONDENT_2));
            }
        }
        return generateDocmosisDocsForNonDivergent(defaultJudgmentForms, authorisation, caseData, event);
    }

    private List<CaseDocument> generateDocmosisDocsForNonDivergent(List<DefaultJudgmentForm> defaultJudgmentForms,
                                                                   String authorisation, CaseData caseData, String event) {
        List<CaseDocument> caseDocuments = new ArrayList<>();
        for (int i = 0; i < defaultJudgmentForms.size(); i++) {
            DefaultJudgmentForm defaultJudgmentForm = defaultJudgmentForms.get(i);
            DocumentType documentType = getDocumentTypeBasedOnEvent(i, event);
            DocmosisTemplates docmosisTemplate = getDocmosisTemplate(event, false);
            DocmosisDocument docmosisDocument = documentGeneratorService.generateDocmosisDocument(defaultJudgmentForm,
                docmosisTemplate);
            CaseDocument engDocument = documentManagementService.uploadDocument(
                authorisation,
                new PDF(
                    getFileName(caseData, docmosisTemplate),
                    docmosisDocument.getBytes(),
                    documentType
                )
            );
            CaseDocument uploadedDocument = engDocument;
            if (event.equals(GEN_DJ_FORM_NON_DIVERGENT_SPEC_CLAIMANT.name()) && caseData.isClaimantBilingual()) {
                uploadedDocument = null;
                CaseDocument welshCaseDoc = createWelshDocument(defaultJudgmentForm, authorisation, caseData, event, documentType);
                List<DocumentMetaData> documentMetaDataList = appendWelshDocToDocument(engDocument, welshCaseDoc);
                Long caseId = caseData.getCcdCaseReference();
                uploadedDocument = civilStitchService.generateStitchedCaseDocument(
                    documentMetaDataList,
                    welshCaseDoc.getDocumentName(),
                    caseId,
                    documentType,
                    authorisation
                );
            }
            assignCategoryId.assignCategoryIdToCaseDocument(uploadedDocument, "judgments");
            caseDocuments.add(uploadedDocument);
        }
        return caseDocuments;
    }

    private CaseDocument createWelshDocument(DefaultJudgmentForm defaultJudgmentForm,
                                 String authorisation, CaseData caseData, String event, DocumentType documentType) {

        DocmosisTemplates docmosisTemplate = getDocmosisTemplate(event, true);
        DocmosisDocument docmosisDocument = documentGeneratorService.generateDocmosisDocument(defaultJudgmentForm,
                                                                                              docmosisTemplate);
        return documentManagementService.uploadDocument(
            authorisation,
            new PDF(
                getFileName(caseData, docmosisTemplate),
                docmosisDocument.getBytes(),
                documentType
            )
        );
    }

    private List<DocumentMetaData> appendWelshDocToDocument(CaseDocument englishDoc, CaseDocument welshDocument) {
        List<DocumentMetaData> documentMetaDataList = new ArrayList<>();

        documentMetaDataList.add(new DocumentMetaData(
            englishDoc.getDocumentLink(),
            "English Document",
            LocalDate.now().toString()
        ));

        documentMetaDataList.add(new DocumentMetaData(
            welshDocument.getDocumentLink(),
            "Welsh Doc to attach",
            LocalDate.now().toString()
        ));

        return documentMetaDataList;
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
}
