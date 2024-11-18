package uk.gov.hmcts.reform.civil.stitch;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

class PdfMergerTest {

    @Test
    void should_return_a_merged_pdf_when_multiple_documents_are_sent() throws Exception {
        //given
        byte[] test1Pdf = loadResource("stitch-documents/test1.pdf");
        byte[] test2Pdf = loadResource("stitch-documents/test2.pdf");
        byte[] expectedMergedPdf = loadResource("stitch-documents/merged.pdf");

        //when
        byte[] actualMergedPdf = PdfMerger.mergeDocuments(asList(test1Pdf, test2Pdf), "test_service");

        // then
        try (
            InputStream actualPdfPage1 = getPdfPageContents(actualMergedPdf, 0);
            InputStream actualPdfPage2 = getPdfPageContents(actualMergedPdf, 1);

            InputStream expectedPdfPage1 = getPdfPageContents(expectedMergedPdf, 0);
            InputStream expectedPdfPage2 = getPdfPageContents(expectedMergedPdf, 1)
        ) {
            assertThat(actualPdfPage1).hasSameContentAs(expectedPdfPage1);
            assertThat(actualPdfPage2).hasSameContentAs(expectedPdfPage2);
        }
    }

    private byte[] loadResource(final String filePath) throws Exception {
        URL url = ResourceLoader.class.getClassLoader().getResource(filePath);

        if (url == null) {
            throw new IllegalArgumentException(String.format("Could not find resource in path %s", filePath));
        }

        return Files.readAllBytes(Paths.get(url.toURI()));
    }

    private InputStream getPdfPageContents(byte[] pdf, int pageNumber) throws IOException {
        try (PDDocument document = Loader.loadPDF(pdf)) {
            return document.getPage(pageNumber).getContents();
        }
    }
}
