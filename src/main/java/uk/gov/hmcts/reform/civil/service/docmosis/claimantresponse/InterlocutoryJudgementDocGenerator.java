package uk.gov.hmcts.reform.civil.service.docmosis.claimantresponse;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.docmosis.InterlocutoryJudgementDoc;
import uk.gov.hmcts.reform.civil.model.docmosis.InterlocutoryJudgementDocMapper;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.docmosis.TemplateDataGenerator;

import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.INTERLOCUTORY_JUDGEMENT_DOCUMENT;

@Service
@Getter
@RequiredArgsConstructor
public class InterlocutoryJudgementDocGenerator implements TemplateDataGenerator<InterlocutoryJudgementDoc> {

    private final InterlocutoryJudgementDocMapper mapper;
    private final DocumentManagementService documentManagementService;
    private final DocumentGeneratorService documentGeneratorService;

    public CaseDocument generateInterlocutoryJudgementDoc(CaseData caseData, String authorisation) {
        DocmosisDocument docmosisDocument = null;
        try {
     docmosisDocument = documentGeneratorService.generateDocmosisDocument(
        getTemplateData(caseData),
        INTERLOCUTORY_JUDGEMENT_DOCUMENT
    );
}
catch (Exception e) {
    e.printStackTrace();
}
        return documentManagementService.uploadDocument(
            authorisation,
            new PDF(String.format(INTERLOCUTORY_JUDGEMENT_DOCUMENT.getDocumentTitle(), caseData.getLegacyCaseReference()),
                    docmosisDocument.getBytes(),
                    DocumentType.INTERLOCUTORY_JUDGEMENT
            )
        );
    }

    @Override
    public InterlocutoryJudgementDoc getTemplateData(CaseData caseData) {
        return mapper.toInterlocutoryJudgementDoc(caseData);
    }
}
