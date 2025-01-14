package uk.gov.hmcts.reform.civil.service.docmosis.sdo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentDownloadException;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.docmosis.sdo.SdoCoverLetter;
import uk.gov.hmcts.reform.civil.model.documents.DocumentMetaData;
import uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.documentmanagement.DocumentDownloadService;
import uk.gov.hmcts.reform.civil.stitch.service.CivilStitchService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.SDO_COVER_LETTER;

@Slf4j
@RequiredArgsConstructor
@Service
public class SdoCoverLetterAppendService {

    private final DocumentGeneratorService documentGeneratorService;
    private final CivilStitchService civilStitchService;
    private final DocumentManagementService documentManagementService;
    private final DocumentDownloadService documentDownloadService;

    private DocmosisDocument generate(Party recipient, String caseReference) {
        return documentGeneratorService.generateDocmosisDocument(
            getTemplateData(recipient, caseReference),
            SDO_COVER_LETTER
        );
    }

    public byte[] makeSdoDocumentMailable(CaseData caseData, String authorisation,
                                       Party recipient, DocumentType documentType, CaseDocument... caseDocuments) {
        Long caseId = caseData.getCcdCaseReference();
        log.info("Generating mailable document for caseId {}", caseId);

        DocmosisDocument coverLetter = generate(recipient, caseData.getLegacyCaseReference());
        CaseDocument coverLetterCaseDocument =  documentManagementService.uploadDocument(
            authorisation,
            new PDF(
                DocmosisTemplates.SDO_COVER_LETTER.getDocumentTitle(),
                coverLetter.getBytes(),
                DocumentType.SDO_COVER_LETTER
            )
        );

        List<DocumentMetaData> documentMetaDataList = appendSdoCoverToDocument(coverLetterCaseDocument, caseDocuments);
        String bundleFileName = Arrays.stream(caseDocuments).findFirst().get().getDocumentName().replace('/', '-').replace(' ', '-');

        log.info("Calling civil stitch service from cover letter for caseId {}", caseId);
        CaseDocument mailableDocument =
            civilStitchService.generateStitchedCaseDocument(documentMetaDataList,
                                                            bundleFileName,
                                                            caseId,
                                                            documentType,
                                                            authorisation);

        log.info("Civil stitch service cover letter response {} for caseId {}", mailableDocument, caseId);

        byte[] letterContent;

        try {
            String documentUrl = mailableDocument.getDocumentLink().getDocumentUrl();
            String documentId = documentUrl.substring(documentUrl.lastIndexOf("/") + 1);
            letterContent = documentDownloadService.downloadDocument(authorisation, documentId).file().getInputStream().readAllBytes();
        } catch (Exception e) {
            log.error("Failed getting letter content for document to mail for caseId {}", caseId, e);
            throw new DocumentDownloadException(mailableDocument.getDocumentLink().getDocumentFileName(), e);
        }
        return letterContent;
    }

    private List<DocumentMetaData> appendSdoCoverToDocument(CaseDocument coverLetter, CaseDocument... caseDocuments) {
        List<DocumentMetaData> documentMetaDataList = new ArrayList<>();

        documentMetaDataList.add(new DocumentMetaData(coverLetter.getDocumentLink(),
                                                      "Cover letter",
                                                      LocalDate.now().toString()));

        Arrays.stream(caseDocuments).forEach(caseDocument -> documentMetaDataList.add(new DocumentMetaData(
            caseDocument.getDocumentLink(),
            "Document to attach",
            LocalDate.now().toString()
        )));

        return documentMetaDataList;

    }

    private SdoCoverLetter getTemplateData(Party recipient, String caseReference) {
        return SdoCoverLetter
            .builder()
            .party(recipient)
            .claimReferenceNumber(caseReference)
            .build();
    }
}
