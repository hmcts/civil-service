package uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim.evidenceupload.documenthandler.applicantonesolicitor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim.evidenceupload.documenthandler.BaseDocumentHandlerTest;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.applicantonesolcitor.ApplicantOneDisclosureDocumentHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceDocumentType;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim.evidenceupload.documenthandler.BaseDocumentHandlerTest.DomainConstants.ORIGINAL_FILE_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim.evidenceupload.documenthandler.BaseDocumentHandlerTest.DomainConstants.RESPONDENT;

class ApplicantOneDisclosureDocumentHandlerTest extends BaseDocumentHandlerTest {

    @InjectMocks
    private ApplicantOneDisclosureDocumentHandler handler;

    @BeforeEach
    void setup() {
        setUpDocumentForDisclosure();
    }

    @Test
    void shouldCopyDisclosureDocumentsToLegalRep2() {
        handler.copyLegalRep1ChangesToLegalRep2(caseData, caseDataBefore, builder);

        assertEquals(2, builder.build().getDocumentForDisclosureApp2().size());
    }

    @Test
    void shouldNotRenameDocuments() {
        CaseData caseData = CaseData.builder()
                .documentForDisclosureRes(List.of(
                        Element.<UploadEvidenceDocumentType>builder()
                                .value(UploadEvidenceDocumentType.builder()
                                        .documentIssuedDate(LocalDate.of(2022, 2, 10))
                                        .typeOfDocument("typeOfDocument")
                                        .documentUpload(document)
                                        .build())
                                .build()))
                .build();

        StringBuilder notificationBuilder = new StringBuilder();
        handler.handleDocuments(caseData, RESPONDENT, notificationBuilder);

        assertEquals(ORIGINAL_FILE_NAME, caseData.getDocumentForDisclosureRes().get(0).getValue().getDocumentUpload().getDocumentFileName());
    }
}
