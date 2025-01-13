package uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim.evidenceupload.documenthandler.applicanttwosolicitor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.applicanttwosolicitor.ApplicantTwoExpertAnswersDocumentHandler;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.retriever.UploadEvidenceExpertRetriever;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceExpert;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ApplicantTwoExpertAnswersDocumentHandlerTest {

    @Mock
    private UploadEvidenceExpertRetriever uploadDocumentRetriever;

    @InjectMocks
    private ApplicantTwoExpertAnswersDocumentHandler handler;

    @BeforeEach
    void setUp() {
        handler = new ApplicantTwoExpertAnswersDocumentHandler(uploadDocumentRetriever);
    }

    @Test
    void shouldRenameDocumentsThroughHandleDocuments() {
        Document document = Document.builder().documentFileName("OriginalName.pdf").build();
        when(uploadDocumentRetriever.getDocument(any())).thenReturn(document);

        CaseData caseData = CaseData.builder()
                .documentAnswersApp2(List.of(
                        Element.<UploadEvidenceExpert>builder()
                                .value(UploadEvidenceExpert.builder()
                                        .expertOptionName("ExpertOptionNameTest")
                                        .expertOptionOtherParty("ExpertOptionOtherPartyTest")
                                        .expertDocumentAnswer("ExpertDocumentAnswerTest")
                                        .expertDocument(document)
                                        .build())
                                .build()))
                .build();

        LocalDateTime mockDateTime = LocalDateTime.of(2022, 2, 10, 10, 0);
        when(uploadDocumentRetriever.getDocumentDateTime(any())).thenReturn(mockDateTime);

        StringBuilder notificationBuilder = new StringBuilder();
        handler.handleDocuments(caseData, "Applicant", notificationBuilder);

        String actualFileName = caseData.getDocumentAnswersApp2().get(0).getValue().getExpertDocument().getDocumentFileName();
        assertEquals("ExpertOptionNameTest ExpertOptionOtherPartyTest ExpertDocumentAnswerTest.pdf", actualFileName);
    }
}