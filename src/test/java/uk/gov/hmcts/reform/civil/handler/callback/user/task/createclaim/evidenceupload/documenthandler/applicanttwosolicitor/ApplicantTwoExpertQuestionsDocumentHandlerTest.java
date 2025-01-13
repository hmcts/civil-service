package uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim.evidenceupload.documenthandler.applicanttwosolicitor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.applicanttwosolicitor.ApplicantTwoExpertQuestionsDocumentHandler;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.retriever.UploadEvidenceExpertRetriever;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceExpert;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ApplicantTwoExpertQuestionsDocumentHandlerTest {

    @Mock
    private UploadEvidenceExpertRetriever uploadDocumentRetriever;

    @InjectMocks
    private ApplicantTwoExpertQuestionsDocumentHandler handler;

    @BeforeEach
    void setUp() {
        handler = new ApplicantTwoExpertQuestionsDocumentHandler(uploadDocumentRetriever);
    }

    @Test
    void shouldRenameDocumentsThroughHandleDocuments() {
        Document document = Document.builder().documentFileName("OriginalName.pdf").build();
        when(uploadDocumentRetriever.getDocument(any())).thenReturn(document);

        CaseData caseData = CaseData.builder()
                .documentQuestionsApp2(List.of(
                        Element.<UploadEvidenceExpert>builder()
                                .value(UploadEvidenceExpert.builder()
                                        .expertOptionUploadDate(LocalDate.of(2022, 2, 10))
                                        .expertOptionName("test")
                                        .expertDocumentQuestion("Document Question")
                                        .expertOptionOtherParty("Other Party")
                                        .expertDocument(document)
                                        .build())
                                .build()))
                .build();

        LocalDateTime mockDateTime = LocalDateTime.of(2022, 2, 10, 10, 0);
        when(uploadDocumentRetriever.getDocumentDateTime(any())).thenReturn(mockDateTime);

        handler.handleDocuments(caseData, "Applicant", new StringBuilder());

        assertEquals("test Other Party Document Question.pdf", caseData.getDocumentQuestionsApp2().get(0).getValue().getExpertDocument().getDocumentFileName());
    }
}