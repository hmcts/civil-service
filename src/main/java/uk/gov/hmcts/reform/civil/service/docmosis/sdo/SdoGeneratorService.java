package uk.gov.hmcts.reform.civil.service.docmosis.sdo;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.docmosis.sdo.SdoDocumentForm;
import uk.gov.hmcts.reform.civil.model.docmosis.sdo.SdoDocumentFormDisposal;
import uk.gov.hmcts.reform.civil.model.docmosis.sdo.SdoDocumentFormFast;
import uk.gov.hmcts.reform.civil.model.docmosis.sdo.SdoDocumentFormSmall;
import uk.gov.hmcts.reform.civil.model.documents.CaseDocument;
import uk.gov.hmcts.reform.civil.model.documents.DocumentType;
import uk.gov.hmcts.reform.civil.model.documents.PDF;
import uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.documentmanagement.DocumentManagementService;

@Service
@RequiredArgsConstructor
public class SdoGeneratorService {

    private final DocumentGeneratorService documentGeneratorService;
    private final DocumentManagementService documentManagementService;

    public CaseDocument generate(CaseData caseData, String authorisation) {
        SdoDocumentForm templateData;
        DocmosisTemplates docmosisTemplates;

        if ("DISPOSAL_HEARING".equals(caseData.getHearingSelection())) {
            docmosisTemplates = DocmosisTemplates.DISPOSAL_DIRECTIONS;
            templateData = getTemplateDataDisposal(caseData);
        } else if (caseData.getAllocatedTrack() == AllocatedTrack.SMALL_CLAIM) {
            docmosisTemplates = DocmosisTemplates.SDO_SMALL;
            templateData = getTemplateDataSmall(caseData);
        } else {
            docmosisTemplates = DocmosisTemplates.SDO_FAST;
            templateData = getTemplateDataFast(caseData);
        }

        DocmosisDocument docmosisDocument = documentGeneratorService.generateDocmosisDocument(
            templateData,
            docmosisTemplates
        );

        return documentManagementService.uploadDocument(
            authorisation,
            new PDF(getFileName(docmosisTemplates, caseData),
                    docmosisDocument.getBytes(),
                    DocumentType.SDO_ORDER
            )
        );
    }

    private String getFileName(DocmosisTemplates docmosisTemplate, CaseData caseData) {
        return String.format(docmosisTemplate.getDocumentTitle(), caseData.getLegacyCaseReference());
    }

    private SdoDocumentForm getTemplateDataDisposal(CaseData caseData) {
        return new SdoDocumentFormDisposal();
    }

    private SdoDocumentForm getTemplateDataFast(CaseData caseData) {
        return new SdoDocumentFormFast();
    }

    private SdoDocumentForm getTemplateDataSmall(CaseData caseData) {
        return new SdoDocumentFormSmall();
    }
}
