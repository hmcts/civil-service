package uk.gov.hmcts.reform.civil.utils;

import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.documents.CaseDocument;
import uk.gov.hmcts.reform.civil.model.documents.Document;
import uk.gov.hmcts.reform.civil.model.documents.DocumentType;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toUnmodifiableList;

public class ElementUtils {

    private ElementUtils() {
        //NO-OP
    }

    @SafeVarargs
    public static <T> List<Element<T>> wrapElements(T... elements) {
        return Stream.of(elements)
            .filter(Objects::nonNull)
            .map(element -> Element.<T>builder().value(element).build())
            .collect(toList());
    }

    public static <T> List<T> unwrapElements(List<Element<T>> elements) {
        return nullSafeCollection(elements)
            .stream()
            .map(Element::getValue)
            .filter(Objects::nonNull)
            .collect(toUnmodifiableList());
    }

    public static <T> Element<T> element(T element) {
        return Element.<T>builder()
            .id(UUID.randomUUID())
            .value(element)
            .build();
    }

    public static Element<CaseDocument> buildElemCaseDocument(Document document, String createdBy,
                                                        LocalDateTime createdAt, DocumentType type) {
        return ElementUtils.element(uk.gov.hmcts.reform.civil.model.documents.CaseDocument.builder()
                                        .documentLink(document)
                                        .documentName(document.getDocumentFileName())
                                        .documentType(type)
                                        .createdDatetime(createdAt)
                                        .createdBy(createdBy)
                                        .build()
        );
    }

    private static <T> Collection<T> nullSafeCollection(Collection<T> collection) {
        return collection == null ? Collections.emptyList() : collection;
    }

}
