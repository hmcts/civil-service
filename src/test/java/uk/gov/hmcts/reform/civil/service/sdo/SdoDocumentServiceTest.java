package uk.gov.hmcts.reform.civil.service.sdo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.docmosis.sdo.SdoGeneratorService;
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SdoDocumentServiceTest {

    private static final String AUTH_TOKEN = "auth-token";

    @Mock
    private SdoGeneratorService sdoGeneratorService;

    @Mock
    private AssignCategoryId assignCategoryId;

    private SdoDocumentService sdoDocumentService;

    @BeforeEach
    void setUp() {
        sdoDocumentService = new SdoDocumentService(sdoGeneratorService, assignCategoryId);
    }

    @Test
    void shouldReturnDocument_whenGeneratorProducesOutput() {
        CaseData caseData = CaseData.builder().build();
        CaseDocument expectedDocument = CaseDocument.builder().build();

        when(sdoGeneratorService.generate(caseData, AUTH_TOKEN)).thenReturn(expectedDocument);

        Optional<CaseDocument> generated = sdoDocumentService.generateSdoDocument(caseData, AUTH_TOKEN);

        assertThat(generated).contains(expectedDocument);
        verify(sdoGeneratorService).generate(caseData, AUTH_TOKEN);
    }

    @Test
    void shouldReturnEmpty_whenGeneratorReturnsNull() {
        CaseData caseData = CaseData.builder().build();
        when(sdoGeneratorService.generate(caseData, AUTH_TOKEN)).thenReturn(null);

        Optional<CaseDocument> generated = sdoDocumentService.generateSdoDocument(caseData, AUTH_TOKEN);

        assertThat(generated).isEmpty();
    }

    @Test
    void shouldAssignCategoryToDocument() {
        CaseDocument document = CaseDocument.builder().build();
        String category = "caseManagementOrders";

        sdoDocumentService.assignCategory(document, category);

        verify(assignCategoryId).assignCategoryIdToCaseDocument(document, category);
    }
}

