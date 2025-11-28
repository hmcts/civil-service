package uk.gov.hmcts.reform.civil.service.dj;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.docmosis.dj.DefaultJudgmentOrderFormGenerator;
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DjDocumentServiceTest {

    private static final String AUTH = "auth";
    private static final String CATEGORY = "category";

    @Mock
    private DefaultJudgmentOrderFormGenerator generator;
    @Mock
    private AssignCategoryId assignCategoryId;
    @Mock
    private CaseDocument document;

    private DjDocumentService service;

    @BeforeEach
    void setUp() {
        service = new DjDocumentService(generator, assignCategoryId);
    }

    @Test
    void shouldGenerateCaseDocumentWhenDocmosisReturnsDocument() {
        CaseData caseData = CaseData.builder().legacyCaseReference("001").build();
        when(generator.generate(caseData, AUTH)).thenReturn(document);

        Optional<CaseDocument> result = service.generateOrder(caseData, AUTH);

        assertThat(result).contains(document);
    }

    @Test
    void shouldAssignCategoryToDocument() {
        service.assignCategory(document, CATEGORY);

        verify(assignCategoryId).assignCategoryIdToCaseDocument(document, CATEGORY);
    }
}
