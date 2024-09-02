package uk.gov.hmcts.reform.civil.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.enums.DocCategory;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.dq.Applicant1DQ;
import uk.gov.hmcts.reform.civil.sampledata.DocumentBuilder;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class DQResponseDocumentUtilsTest {

    @InjectMocks
    private DQResponseDocumentUtils dqResponseDocumentUtils;

    @Mock
    private AssignCategoryId assignCategoryId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Nested
    class BuildClaimantResponseDocuments {

        @Test
        void shouldReturnEmptyListWhenDQIsNull() {

            CaseData caseData = CaseData.builder()
                .applicant1DQ(null)
                .applicant1ResponseDate(LocalDateTime.now())
                .build();

            List<Element<CaseDocument>> result = dqResponseDocumentUtils.buildClaimantResponseDocuments(caseData);

            assertThat(result).isEmpty();
            verifyNoInteractions(assignCategoryId);
        }

        @Test
        void shouldReturnEmptyListWhenDQHasNoDocuments() {
            Applicant1DQ dq = Applicant1DQ.builder().build();
            CaseData caseData = CaseData.builder()
                .applicant1DQ(dq)
                .applicant1ResponseDate(LocalDateTime.now())
                .build();

            List<Element<CaseDocument>> result = dqResponseDocumentUtils.buildClaimantResponseDocuments(caseData);

            assertThat(result).isEmpty();
            verifyNoInteractions(assignCategoryId);
        }

        @Test
        void shouldReturnListWithDocumentsWhenDQHasDraftDirections() {
            Document document = DocumentBuilder.builder().build();

            Applicant1DQ dq = Applicant1DQ.builder()
                .applicant1DQDraftDirections(document)
                .build();

            CaseData caseData = CaseData.builder()
                .applicant1DQ(dq)
                .applicant1ResponseDate(LocalDateTime.now())
                .build();

            List<Element<CaseDocument>> result = dqResponseDocumentUtils.buildClaimantResponseDocuments(caseData);

            assertThat(result).isNotEmpty();
            assertThat(result).hasSize(1);

            CaseDocument caseDocument = result.get(0).getValue();
            assertThat(caseDocument.getDocumentLink().getDocumentUrl()).isEqualTo(document.getDocumentUrl());
            assertThat(caseDocument.getDocumentType()).isEqualTo(DocumentType.CLAIMANT_DRAFT_DIRECTIONS);

            verify(assignCategoryId, times(1)).assignCategoryIdToCollection(
                any(), any(), eq(DocCategory.DQ_APP1.getValue())
            );
        }

        @Test
        void shouldNotAssignCategoryIdWhenNoDocumentsAdded() {
            Applicant1DQ dq = Applicant1DQ.builder().build();
            CaseData caseData = CaseData.builder()
                .applicant1DQ(dq)
                .applicant1ResponseDate(LocalDateTime.now())
                .build();

            List<Element<CaseDocument>> result = dqResponseDocumentUtils.buildClaimantResponseDocuments(caseData);

            assertThat(result).isEmpty();
            verifyNoInteractions(assignCategoryId);
        }
    }
}
