package uk.gov.hmcts.reform.civil.helpers.bundle.mappers;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory;
import uk.gov.hmcts.reform.civil.model.bundle.BundlingRequestDocument;
import uk.gov.hmcts.reform.civil.model.citizenui.ManageDocument;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.civil.helpers.bundle.mappers.MockManageDocument.getManageDocumentElement;
import static uk.gov.hmcts.reform.civil.model.citizenui.ManageDocumentType.OTHER;

class ManageDocMapperTest {

    private final ManageDocMapper mapper = new ManageDocMapper() { };

    @Test
    void shouldUseManageDocumentCreatedDateWhenUploadTimestampIsMissing() {
        Element<ManageDocument> manageDocument = getManageDocumentElement(
            OTHER,
            DocumentCategory.APPLICANT_ONE_EXPERT_REPORT
        );
        manageDocument.getValue().getDocumentLink().setUploadTimestamp(null);
        manageDocument.getValue().setCreatedDatetime(LocalDateTime.of(2024, Month.JUNE, 15, 10, 30));
        List<BundlingRequestDocument> documents = new ArrayList<>();

        mapper.addDocumentByCategoryId(
            manageDocument,
            documents,
            DocumentCategory.APPLICANT_ONE_EXPERT_REPORT
        );

        assertEquals(1, documents.size());
        assertEquals("Applicant1 Expert Report test_file 15/06/2024", documents.getFirst().getDocumentFileName());
    }
}
