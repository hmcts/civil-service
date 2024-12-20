package uk.gov.hmcts.reform.civil.service.docmosis.pip;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.config.PinInPostConfiguration;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentDownloadException;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.docmosis.pip.PiPLetter;
import uk.gov.hmcts.reform.civil.model.documents.DocumentMetaData;
import uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.docmosis.TemplateDataGenerator;
import uk.gov.hmcts.reform.civil.service.documentmanagement.DocumentDownloadService;
import uk.gov.hmcts.reform.civil.stitch.service.CivilStitchService;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.PIN_IN_THE_POST_LETTER;

@Slf4j
@RequiredArgsConstructor
@Service
public class PiPLetterGenerator implements TemplateDataGenerator<PiPLetter> {

    private static final String PIP_LETTER_NAME = DocmosisTemplates.PIN_IN_THE_POST_LETTER.getDocumentTitle();
    private final DocumentGeneratorService documentGeneratorService;
    private final PinInPostConfiguration pipInPostConfiguration;
    private final CivilStitchService civilStitchService;
    private final DocumentManagementService documentManagementService;
    private final DocumentDownloadService documentDownloadService;

    private DocmosisDocument generate(CaseData caseData) {
        return documentGeneratorService.generateDocmosisDocument(
            getTemplateData(caseData),
            PIN_IN_THE_POST_LETTER
        );
    }

    public byte[] downloadLetter(CaseData caseData, String authorisation) {
        Long caseId = caseData.getCcdCaseReference();
        log.info("Generating PiPLetter document for caseId {}", caseId);
        DocmosisDocument pipLetter = generate(caseData);
        CaseDocument pipLetterCaseDocument =  documentManagementService.uploadDocument(
            authorisation,
            new PDF(
                PIP_LETTER_NAME,
                pipLetter.getBytes(),
                DocumentType.ACKNOWLEDGEMENT_OF_CLAIM
            )
        );

        List<DocumentMetaData> documentMetaDataList = fetchDocumentsFromCaseData(caseData, pipLetterCaseDocument);
        log.info("Calling civil stitch service from pip letter generation for caseId {}", caseId);
        CaseDocument stitchedDocument =
            civilStitchService.generateStitchedCaseDocument(documentMetaDataList,
                                                            PIP_LETTER_NAME,
                                                            caseId,
                                                            DocumentType.PIP_LETTER,
                                                            authorisation);

        log.info("Civil stitch service pip letter generation response {} for caseId {}", stitchedDocument, caseId);

        byte[] letterContent;
        try {
            String documentUrl = stitchedDocument.getDocumentLink().getDocumentUrl();
            String documentId = documentUrl.substring(documentUrl.lastIndexOf("/") + 1);
            letterContent = documentDownloadService.downloadDocument(authorisation, documentId).file().getInputStream().readAllBytes();
        } catch (IOException e) {
            log.error("Failed getting letter content for Pip Stitched Letter for caseId {}", caseId, e);
            throw new DocumentDownloadException(stitchedDocument.getDocumentLink().getDocumentFileName(), e);
        }
        return letterContent;
    }

    @Override
    public PiPLetter getTemplateData(CaseData caseData) {
        return PiPLetter
            .builder()
            .pin(caseData.getRespondent1PinToPostLRspec().getAccessCode())
            .ccdCaseNumber(String.valueOf(caseData.getCcdCaseReference()))
            .claimReferenceNumber(caseData.getLegacyCaseReference())
            .claimantName(caseData.getApplicant1().getPartyName())
            .defendant(caseData.getRespondent1())
            .responseDeadline(caseData.getRespondent1ResponseDeadline().toLocalDate())
            .totalAmountOfClaim(caseData.getTotalClaimAmount())
            .respondToClaimUrl(pipInPostConfiguration.getRespondToClaimUrl())
            .issueDate(LocalDate.now())
            .build();
    }

    private List<DocumentMetaData> fetchDocumentsFromCaseData(CaseData caseData, CaseDocument caseDocument) {
        List<DocumentMetaData> documentMetaDataList = new ArrayList<>();

        documentMetaDataList.add(new DocumentMetaData(caseDocument.getDocumentLink(),
                                                      "PiP Letter",
                                                      LocalDate.now().toString()));

        Optional<Element<CaseDocument>> optionalSealedDocument = caseData.getSystemGeneratedCaseDocuments().stream()
            .filter(systemGeneratedCaseDocument -> systemGeneratedCaseDocument.getValue()
                .getDocumentType().equals(DocumentType.SEALED_CLAIM)).findAny();

        optionalSealedDocument.ifPresent(caseDocumentElement -> documentMetaDataList.add(new DocumentMetaData(
            caseDocumentElement.getValue().getDocumentLink(),
            "Sealed Claim form",
            LocalDate.now().toString()
        )));

        return documentMetaDataList;
    }
}
