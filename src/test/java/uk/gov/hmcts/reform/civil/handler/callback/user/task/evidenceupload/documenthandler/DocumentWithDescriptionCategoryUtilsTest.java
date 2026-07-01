package uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.documents.DocumentWithDescription;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.DocumentCategory.WITHOUT_PREJUDICE;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;

class DocumentWithDescriptionCategoryUtilsTest {

    @Test
    void applyCategoryIdShouldNoOpWhenListNull() {
        Document document = new Document().setDocumentFileName("evidence.pdf");
        assertThat(document.getCategoryID()).isNull();
        DocumentWithDescriptionCategoryUtils.applyCategoryId(null, WITHOUT_PREJUDICE);
        assertThat(document.getCategoryID()).isNull();
    }

    @Test
    void applyCategoryIdShouldNoOpWhenListEmpty() {
        Document document = new Document().setDocumentFileName("evidence.pdf");
        assertThat(document.getCategoryID()).isNull();
        DocumentWithDescriptionCategoryUtils.applyCategoryId(new ArrayList<>(), WITHOUT_PREJUDICE);
        assertThat(document.getCategoryID()).isNull();
    }

    @Test
    void applyCategoryIdShouldSetCategoryWhenDocumentPresent() {
        Document document = new Document().setDocumentFileName("evidence.pdf");
        DocumentWithDescription value = new DocumentWithDescription(
            document, "desc", LocalDateTime.of(2024, 6, 1, 12, 0), "user");
        List<Element<DocumentWithDescription>> list = List.of(element(value));

        DocumentWithDescriptionCategoryUtils.applyCategoryId(list, WITHOUT_PREJUDICE);

        assertThat(document.getCategoryID()).isEqualTo(WITHOUT_PREJUDICE.getCategoryId());
    }

    @Test
    void applyCategoryIdShouldSkipWhenValueOrDocumentNull() {
        Element<DocumentWithDescription> nullValue = new Element<>();
        nullValue.setValue(null);
        DocumentWithDescription noDoc = new DocumentWithDescription(null, "x", LocalDateTime.now(), "u");
        List<Element<DocumentWithDescription>> list = List.of(nullValue, element(noDoc));

        DocumentWithDescriptionCategoryUtils.applyCategoryId(list, WITHOUT_PREJUDICE);

        assertThat(noDoc.getDocument()).isNull();
    }
}
