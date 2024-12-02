package uk.gov.hmcts.reform.civil.service;

import org.apache.tika.Tika;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.exceptions.DocumentConversionException;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentConversionServiceTest {

    private static final byte[] CONVERTED_BINARY = "converted".getBytes();
    private static final String AUTH = "auth";
    private static final Long CASE_ID = 1L;
    private static final String PDF_MIME_TYPE = "application/pdf";
    private static final String WORD_MIME_TYPE = "application/msword";

    @InjectMocks
    private DocumentConversionService documentConversionService;
    @Mock
    private RestTemplate restTemplate;
    @Mock
    private Tika tika;
    @Mock
    private DocumentManagementService service;

    private Document documentToConvert;

    @BeforeEach
    public void setUp() {
        documentToConvert = Document.builder()
            .documentFileName("file.docx")
            .documentUrl("docUrl.com")
            .documentBinaryUrl("binaryUrl.com")
            .build();
    }

    @Test
    void testConvertDocumentToPdf_ConvertSuccessfullyWhenDocumentIsNotPdf() {
        when(service.downloadDocument(AUTH, documentToConvert.getDocumentUrl())).thenReturn("bytes".getBytes());
        when(tika.detect(documentToConvert.getDocumentFileName())).thenReturn(WORD_MIME_TYPE);
        when(restTemplate.postForObject(anyString(), any(HttpEntity.class), eq(byte[].class))).thenReturn(CONVERTED_BINARY);

        byte[] result = documentConversionService.convertDocumentToPdf(documentToConvert, CASE_ID, AUTH);

        assertNotNull(result, "The returned byte array should not be null.");
        assertArrayEquals(CONVERTED_BINARY, result);
        verify(tika, times(1)).detect(documentToConvert.getDocumentFileName());
        verify(service, times(1)).downloadDocument(AUTH, documentToConvert.getDocumentUrl());
        verify(restTemplate, times(1)).postForObject(anyString(), any(HttpEntity.class), eq(byte[].class));
    }

    @Test
    void convertDocumentToPdf_ShouldReturnDocumentIsAlreadyPdf() {
        byte[] bytes = "bytes".getBytes();
        when(service.downloadDocument(AUTH, documentToConvert.getDocumentUrl())).thenReturn(bytes);
        when(tika.detect(documentToConvert.getDocumentFileName())).thenReturn(PDF_MIME_TYPE);

        byte[] result = documentConversionService.convertDocumentToPdf(documentToConvert, CASE_ID, AUTH);

        assertNotNull(result, "The returned byte array should not be null.");
        assertArrayEquals(bytes, result);
        verify(tika, times(1)).detect(documentToConvert.getDocumentFileName());
        verifyNoInteractions(restTemplate);
    }

    @Test
    void convertDocumentToPdf_ThrowsExceptionOnClientError() {

        byte[] mockFileBytes = "dummy content".getBytes();
        when(tika.detect(documentToConvert.getDocumentFileName())).thenReturn(WORD_MIME_TYPE);
        when(service.downloadDocument(AUTH, documentToConvert.getDocumentUrl())).thenReturn(mockFileBytes);
        when(restTemplate.postForObject(anyString(), any(HttpEntity.class), eq(byte[].class)))
                .thenThrow(new HttpClientErrorException(org.springframework.http.HttpStatus.BAD_REQUEST));

        DocumentConversionException exception = assertThrows(DocumentConversionException.class, () ->
            documentConversionService.convertDocumentToPdf(documentToConvert, CASE_ID, AUTH));

        assertEquals("Error converting document to pdf for caseId 1", exception.getMessage());
        verify(tika, times(1)).detect(documentToConvert.getDocumentFileName());
        verify(service, times(1)).downloadDocument(AUTH, documentToConvert.getDocumentUrl());
        verify(restTemplate, times(1)).postForObject(anyString(), any(HttpEntity.class), eq(byte[].class));
    }
}
