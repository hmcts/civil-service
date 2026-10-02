package uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler;

import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.documents.DocumentWithDescription;

import java.util.List;

public final class DocumentWithDescriptionCategoryUtils {

    private DocumentWithDescriptionCategoryUtils() {
    }

    public static void applyCategoryId(List<Element<DocumentWithDescription>> documents,
                                       DocumentCategory documentCategory) {
        if (documents == null || documents.isEmpty()) {
            return;
        }
        String categoryId = documentCategory.getCategoryId();
        documents.forEach(element -> {
            if (element.getValue() != null && element.getValue().getDocument() != null) {
                element.getValue().getDocument().setCategoryID(categoryId);
            }
        });
    }
}
