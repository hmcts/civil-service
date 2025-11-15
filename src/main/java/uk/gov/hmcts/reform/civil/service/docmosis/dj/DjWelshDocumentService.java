package uk.gov.hmcts.reform.civil.service.docmosis.dj;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GEN_DJ_FORM_NON_DIVERGENT_SPEC_CLAIMANT;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.N121_SPEC_CLAIMANT;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.N121_SPEC_CLAIMANT_WELSH;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.N121_SPEC_DEFENDANT;

@Service
@RequiredArgsConstructor
public class DjWelshDocumentService {

    private final DocumentGeneratorService documentGeneratorService;
    private final DocumentManagementService documentManagementService;
    private final CivilStitchService civilStitchService;

    private static final String ENGLISH_LABEL = "English Document";
    private static final String WELSH_LABEL = "Welsh Doc to attach";

    public CaseDocument attachWelshDocumentIfRequired(DefaultJudgmentForm defaultJudgmentForm,
                                                      CaseData caseData,
                                                      String authorisation,
                                                      String event,
                                                      DocumentType documentType,
                                                      CaseDocument englishDocument) {
        if (!shouldGenerateWelshDocument(caseData, event)) {
            return englishDocument;
        }

        CaseDocument welshDocument = createWelshDocument(defaultJudgmentForm, authorisation, caseData, event, documentType);
        List<DocumentMetaData> documentMetaDataList = appendWelshDocToDocument(englishDocument, welshDocument);

        return civilStitchService.generateStitchedCaseDocument(
            documentMetaDataList,
            welshDocument.getDocumentName(),
            caseData.getCcdCaseReference(),
            documentType,
            authorisation
        );
    }

    public DocmosisTemplates getDocmosisTemplate(String event, boolean isWelsh) {
        if (isClaimantEvent(event)) {
            return isWelsh ? N121_SPEC_CLAIMANT_WELSH : N121_SPEC_CLAIMANT;
        }
        return N121_SPEC_DEFENDANT;
    }

    boolean shouldGenerateWelshDocument(CaseData caseData, String event) {
        return isClaimantEvent(event) && caseData.isClaimantBilingual();
    }

    private boolean isClaimantEvent(String event) {
        return GEN_DJ_FORM_NON_DIVERGENT_SPEC_CLAIMANT.name().equals(event);
    }

    private CaseDocument createWelshDocument(DefaultJudgmentForm defaultJudgmentForm,
                                             String authorisation,
                                             CaseData caseData,
                                             String event,
                                             DocumentType documentType) {

        DocmosisTemplates docmosisTemplate = getDocmosisTemplate(event, true);
        DocmosisDocument docmosisDocument = documentGeneratorService.generateDocmosisDocument(defaultJudgmentForm,
            docmosisTemplate);
        return documentManagementService.uploadDocument(
            authorisation,
            new PDF(
                buildFileName(caseData, docmosisTemplate),
                docmosisDocument.getBytes(),
                documentType
            )
        );
    }

    private String buildFileName(CaseData caseData, DocmosisTemplates docmosisTemplate) {
        return String.format(docmosisTemplate.getDocumentTitle(), caseData.getLegacyCaseReference());
    }

    private List<DocumentMetaData> appendWelshDocToDocument(CaseDocument englishDoc, CaseDocument welshDocument) {
        List<DocumentMetaData> documentMetaDataList = new ArrayList<>();

        documentMetaDataList.add(new DocumentMetaData(
            englishDoc.getDocumentLink(),
            ENGLISH_LABEL,
            LocalDate.now().toString()
        ));

        documentMetaDataList.add(new DocumentMetaData(
            welshDocument.getDocumentLink(),
            WELSH_LABEL,
            LocalDate.now().toString()
        ));

        return documentMetaDataList;
    }
}
