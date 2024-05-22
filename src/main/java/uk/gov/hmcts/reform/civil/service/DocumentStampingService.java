package uk.gov.hmcts.reform.civil.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.util.ZipSecureFile;
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.xmlbeans.XmlCursor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DownloadedDocumentResponse;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode.APPEND;
import static org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject.createFromByteArray;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentStampingService {

    private final DocumentManagementService documentManagementService;

    public CaseDocument stampDocument(String authToken, Document document, DocumentType documentType, String imageFileName) {
        DownloadedDocumentResponse caseDocumentWithMetadata = getDocument(document, authToken);
        byte[] updatedDocument = null;
        if (document.getDocumentFileName().toLowerCase().endsWith(".pdf")) {
            updatedDocument = stampPDFDocument(caseDocumentWithMetadata, imageFileName);
        } else if (document.getDocumentFileName().toLowerCase().endsWith(".doc") || document.getDocumentFileName().toLowerCase().endsWith(
            ".docx")) {
            updatedDocument = stampWordDocument(caseDocumentWithMetadata, imageFileName);
        } else {
            //Unsupported file extension wrap original document
            throw new IllegalArgumentException("Unsupported file type");
        }

        return documentManagementService.uploadDocument(authToken, new PDF(document.getDocumentFileName(), updatedDocument, documentType));
    }

    private byte[] stampWordDocument(DownloadedDocumentResponse docWithMetadata, String imageFileName) {
        try (InputStream docStream = docWithMetadata.file().getInputStream()) {
            ZipSecureFile.setMinInflateRatio(0);
            XWPFDocument wordDoc = new XWPFDocument(docStream);

            XmlCursor cursor = wordDoc.getParagraphs().isEmpty() ? wordDoc.getDocument().newCursor()
                : wordDoc.getParagraphs().get(0).getCTP().newCursor();
            XWPFParagraph image = wordDoc.insertNewParagraph(cursor);
            image.setAlignment(ParagraphAlignment.RIGHT);
            XWPFRun imageRun = image.createRun();
            imageRun.setTextPosition(20);

            try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(imageFileName)) {
                imageRun.addPicture(inputStream, XWPFDocument.PICTURE_TYPE_PNG, imageFileName,
                                    Units.toEMU(50), Units.toEMU(50)
                );
            } catch (InvalidFormatException ex) {
                throw new IllegalArgumentException(ex);
            }

            try(ByteArrayOutputStream output = new ByteArrayOutputStream()) {
                wordDoc.write(output);
                return output.toByteArray();
            }
        } catch (IOException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    private byte[] stampPDFDocument(
        DownloadedDocumentResponse docWithMetadata,
        String imageFileName) {
        try {
            byte[] documentBytes = docWithMetadata.file().getInputStream().readAllBytes();
            return stampPDFDocument(documentBytes, imageFileName);
        } catch (Exception ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    private byte[] stampPDFDocument(byte[] inputDocInBytes, String imageFileName) throws Exception {
        PDDocument doc = PDDocument.load(inputDocInBytes);
        doc.setAllSecurityToBeRemoved(true);
        PDPage page = doc.getPage(0);

        PDPageContentStream psdStream = new PDPageContentStream(doc, page, APPEND, true, true);
        PDImageXObject image = createFromByteArray(doc, imageAsBytes(imageFileName), null);

        int imageWith = 60;
        int imageHeight = 60;
        int topPadding = 15;
        int rightPadding = 15;

        psdStream.drawImage(
            image,
            page.getMediaBox().getWidth() - (imageWith + rightPadding),
            page.getMediaBox().getHeight() - (imageHeight + topPadding),
            imageWith,
            imageHeight
        );
        psdStream.close();
        ByteArrayOutputStream outputBytes = new ByteArrayOutputStream();
        doc.save(outputBytes);
        doc.close();

        return outputBytes.toByteArray();
    }

    private byte[] imageAsBytes(String fileName) throws IOException {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(fileName)) {
            return IOUtils.toByteArray(inputStream);
        }
    }

    private DownloadedDocumentResponse getDocument(Document document, String authToken) {
        String documentId = document.getDocumentUrl().substring(document.getDocumentUrl().lastIndexOf("/") + 1);
        return documentManagementService.downloadDocumentWithMetaData(
            authToken,
            String.format("documents/%s", documentId)
        );
    }
}
