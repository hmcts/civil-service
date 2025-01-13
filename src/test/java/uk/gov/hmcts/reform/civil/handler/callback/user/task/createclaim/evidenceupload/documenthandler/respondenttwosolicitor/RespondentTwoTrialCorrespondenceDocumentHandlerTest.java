package uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim.evidenceupload.documenthandler.respondenttwosolicitor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.respondenttwosolicitor.RespondentTwoTrialCorrespondenceDocumentHandler;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.retriever.UploadEvidenceDocumentRetriever;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceDocumentType;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RespondentTwoTrialCorrespondenceDocumentHandlerTest {

    @Mock
    private UploadEvidenceDocumentRetriever uploadDocumentRetriever;

    @InjectMocks
    private RespondentTwoTrialCorrespondenceDocumentHandler handler;

    @BeforeEach
    void setUp() {
        handler = new RespondentTwoTrialCorrespondenceDocumentHandler(uploadDocumentRetriever);
    }

    @Test
    void shouldHandleDocumentsWithRenaming() {
        Document document = Document.builder().documentFileName("OriginalName.pdf").build();
        when(uploadDocumentRetriever.getDocument(any())).thenReturn(document);

        LocalDateTime mockDateTime = LocalDateTime.of(2022, 2, 10, 10, 0);
        when(uploadDocumentRetriever.getDocumentDateTime(any())).thenReturn(mockDateTime);

        UploadEvidenceDocumentType uploadEvidenceDocumentType = UploadEvidenceDocumentType.builder()
                .documentUpload(document)
                .documentIssuedDate(LocalDate.from(mockDateTime))
                .typeOfDocument("Trial Correspondence")
                .build();

        CaseData caseData = CaseData.builder()
                .documentEvidenceForTrialRes2(List.of(
                        Element.<UploadEvidenceDocumentType>builder()
                                .value(uploadEvidenceDocumentType)
                                .build()))
                .build();

        handler.handleDocuments(caseData, "Respondent", new StringBuilder());

        assertEquals("Documentary Evidence Trial Correspondence 10-02-2022.pdf",
                caseData.getDocumentEvidenceForTrialRes2().get(0).getValue().getDocumentUpload().getDocumentFileName());
    }
}