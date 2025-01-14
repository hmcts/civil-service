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
import uk.gov.hmcts.reform.civil.model.docmosis.common.Party;
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

    public byte[] makeSdoDocumentMailable(CaseData caseData, String authorisation,
                                       Party recipient, DocumentType documentType, CaseDocument... caseDocuments) {
        Long caseId = caseData.getCcdCaseReference();
        log.info("Generating mailable document for caseId {}", caseId);

        DocmosisDocument coverLetter = generateSDOCoverDoc(recipient, caseData.getLegacyCaseReference());
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

        log.info("Calling civil stitch service from sdo cover letter for caseId {}", caseId);
        CaseDocument mailableSdoDocument =
                getMailableStitchedDocument(documentMetaDataList, bundleFileName, caseId, documentType, authorisation);

        log.info("Civil stitch service sdo cover letter response {} for caseId {}", mailableSdoDocument, caseId);

        return getLetterContent(mailableSdoDocument, authorisation, caseId);
    }

    private List<DocumentMetaData> appendSdoCoverToDocument(CaseDocument coverLetter, CaseDocument... caseDocuments) {
        List<DocumentMetaData> documentMetaDataList = new ArrayList<>();

        documentMetaDataList.add(new DocumentMetaData(coverLetter.getDocumentLink(),
                                                      "Cover letter",
                                                      LocalDate.now().toString()));

        Arrays.stream(caseDocuments).forEach(caseDocument -> documentMetaDataList.add(new DocumentMetaData(
            caseDocument.getDocumentLink(),
            "SDO Document to attach",
            LocalDate.now().toString()
        )));

        return documentMetaDataList;

    }

    private DocmosisDocument generateSDOCoverDoc(Party recipient, String caseReference) {
        return documentGeneratorService.generateDocmosisDocument(getTemplateData(recipient, caseReference),
                SDO_COVER_LETTER);
    }

    private SdoCoverLetter getTemplateData(Party recipient, String caseReference) {
        return SdoCoverLetter
                .builder()
                .party(recipient)
                .claimReferenceNumber(caseReference)
                .build();
    }

    private CaseDocument getMailableStitchedDocument(List<DocumentMetaData> documentMetaDataList,
                                                     String bundleFileName,
                                                     Long caseId, DocumentType documentType,
                                                     String authorisation) {
        return civilStitchService.generateStitchedCaseDocument(documentMetaDataList,
                bundleFileName,
                caseId,
                documentType,
                authorisation);
    }

    private byte[] getLetterContent(CaseDocument mailableSdoDocument, String authorisation, Long caseId) {
        byte[] letterContent;
        try {
            String documentUrl = mailableSdoDocument.getDocumentLink().getDocumentUrl();
            String documentId = documentUrl.substring(documentUrl.lastIndexOf("/") + 1);
            letterContent = documentDownloadService.downloadDocument(authorisation, documentId).file().getInputStream().readAllBytes();
        } catch (Exception e) {
            log.error("Failed getting letter content for document to mail for caseId {}", caseId, e);
            throw new DocumentDownloadException(mailableSdoDocument.getDocumentLink().getDocumentFileName(), e);
        }
        return letterContent;
    }
}
