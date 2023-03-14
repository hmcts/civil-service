package uk.gov.hmcts.reform.civil.service.docmosis.caseProgression;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.documents.CaseDocument;
import uk.gov.hmcts.reform.civil.model.documents.DocumentType;
import uk.gov.hmcts.reform.civil.model.documents.PDF;
import uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.docmosis.TemplateDataGenerator;
import uk.gov.hmcts.reform.civil.service.documentmanagement.DocumentManagementService;

import java.io.IOException;
import java.time.LocalDate;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.enums.caseprogression.FinalOrderSelection.FREE_FORM_ORDER;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.*;

@Service
@RequiredArgsConstructor
public class JudgeFinalOrderGenerator implements TemplateDataGenerator<JudgeFinalOrderForm> {


    private final DocumentManagementService documentManagementService;
    private final DocumentGeneratorService documentGeneratorService;

    public CaseDocument generate(CaseData caseData, String authorisation) {
        JudgeFinalOrderForm templateData = getFinalOrderType(caseData);
        DocmosisTemplates docmosisTemplate;
        if (caseData.getFinalOrderSelection().equals(FREE_FORM_ORDER))
            docmosisTemplate = FREE_FORM_ORDER_PDF;
        else docmosisTemplate = DJ_SDO_TRIAL;
        System.out.println("template is    " + docmosisTemplate);
        System.out.println("template data is  "+ templateData);
        DocmosisDocument docmosisDocument =
            documentGeneratorService.generateDocmosisDocument(templateData, docmosisTemplate);
        return documentManagementService.uploadDocument(
            authorisation,
            new PDF(
                getFileName(caseData, docmosisTemplate),
                docmosisDocument.getBytes(),
                DocumentType.JUDGE_FINAL_ORDER
            )
        );
    }

    @Override
    public JudgeFinalOrderForm getTemplateData(CaseData caseData) throws IOException {
        return null;
    }

    private String getFileName(CaseData caseData, DocmosisTemplates docmosisTemplate) {
        return String.format(docmosisTemplate.getDocumentTitle(), LocalDate.now());
    }

    private JudgeFinalOrderForm getFinalOrderType(CaseData caseData) {
        return caseData.getFinalOrderSelection().equals(FREE_FORM_ORDER) ? getFreeFormOrder(caseData) : getAssistedOrder(
            caseData);
    }

    private JudgeFinalOrderForm getFreeFormOrder(CaseData caseData) {
        var freeFormOrderBuilder = JudgeFinalOrderForm.builder()
            .caseNumber(caseData.getCcdCaseReference().toString())
            .caseName(caseData.getCaseNameHmctsInternal())
            .claimantReference(checkReference(caseData)
                                   ? caseData.getSolicitorReferences().getApplicantSolicitor1Reference() : null)
            .defendantReference(checkReference(caseData)
                                   ? caseData.getSolicitorReferences().getRespondentSolicitor1Reference() : null)
            .freeFormRecitalText(caseData.getFreeFormRecitalTextArea())
            .freeFormRecordedText(caseData.getFreeFormRecordedTextArea())
            .freeFormOrderedText(caseData.getFreeFormOrderedTextArea())
            .orderOnCourtsList(caseData.getOrderOnCourtsList())
            .onInitiativeSelectionText(nonNull(caseData.getOrderOnCourtInitiative())
                                           ? caseData.getOrderOnCourtInitiative().getOnInitiativeSelectionTextArea() : null)
            .onInitiativeSelectionDate(nonNull(caseData.getOrderOnCourtInitiative())
                                           ? caseData.getOrderOnCourtInitiative().getOnInitiativeSelectionDate() : null)
            .withoutNoticeSelectionText(nonNull(caseData.getOrderWithoutNotice())
                                            ? caseData.getOrderWithoutNotice().getWithoutNoticeSelectionTextArea() : null)
            .withoutNoticeSelectionDate(nonNull(caseData.getOrderWithoutNotice())
                                            ? caseData.getOrderWithoutNotice().getWithoutNoticeSelectionDate() : null);
        return freeFormOrderBuilder.build();
    }

    private JudgeFinalOrderForm getAssistedOrder(CaseData caseData) {
        return null;
    }

    private boolean checkReference(CaseData caseData) {
        return nonNull(caseData.getSolicitorReferences());
    }

}

