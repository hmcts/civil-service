package uk.gov.hmcts.reform.civil.service.docmosis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentDownloadException;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.docmosis.CoverLetter;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.documents.DocumentMetaData;
import uk.gov.hmcts.reform.civil.service.documentmanagement.DocumentDownloadService;
import uk.gov.hmcts.reform.civil.service.stitching.CivilDocumentStitchingService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.COVER_LETTER;

@Slf4j
@RequiredArgsConstructor
@Service
public class CoverLetterAppendService {

    private final DocumentGeneratorService documentGeneratorService;
    private final CivilDocumentStitchingService civilDocumentStitchingService;
    private final DocumentManagementService documentManagementService;
    private final DocumentDownloadService documentDownloadService;

    private DocmosisDocument generate(Party recipient) {
        return documentGeneratorService.generateDocmosisDocument(
            getTemplateData(recipient),
            COVER_LETTER
        );
    }

    public byte[] makeDocumentMailable(CaseData caseData, String authorisation, Party recipient, CaseDocument... caseDocuments) {
        DocmosisDocument coverLetter = generate(recipient);
        CaseDocument coverLetterCaseDocument =  documentManagementService.uploadDocument(
            authorisation,
            new PDF(
                DocmosisTemplates.COVER_LETTER.getDocumentTitle(),
                coverLetter.getBytes(),
                DocumentType.COVER_LETTER
            )
        );

        List<DocumentMetaData> documentMetaDataList = appendCoverToDocument(coverLetterCaseDocument, caseDocuments);
        log.info("Bundle the following documents" + documentMetaDataList.get(0).toString()
                     + " and " + documentMetaDataList.get(1).toString());
        log.info("Name" + Arrays.stream(caseDocuments).findFirst().get().getDocumentName().replace('/', '-'));
        log.info("Authorisation:" + authorisation);
        String bundleFileName = Arrays.stream(caseDocuments).findFirst().get().getDocumentName().replace('/', '-');

        CaseDocument mailableDocument = civilDocumentStitchingService.bundle(
            documentMetaDataList,
            authorisation,
            bundleFileName,
            bundleFileName,
            caseData
        );

        String documentUrl = mailableDocument.getDocumentLink().getDocumentUrl();
        String documentId = documentUrl.substring(documentUrl.lastIndexOf("/") + 1);
        byte[] letterContent;

        try {
            letterContent = documentDownloadService.downloadDocument(authorisation, documentId).file().getInputStream().readAllBytes();
        } catch (Exception e) {
            log.error("Failed getting letter content for document to mail ");
            throw new DocumentDownloadException(mailableDocument.getDocumentLink().getDocumentFileName(), e);
        }
        return letterContent;
    }

    private List<DocumentMetaData> appendCoverToDocument(CaseDocument coverLetter, CaseDocument... caseDocuments) {
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

    private CoverLetter getTemplateData(Party recipient) {
        return CoverLetter
            .builder()
            .party(recipient)
            .build();
    }
}
