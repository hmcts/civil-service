package uk.gov.hmcts.reform.civil.utils;

import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class ElementUtils {

    private ElementUtils() {
        //NO-OP
    }

    @SafeVarargs
    public static <T> List<Element<T>> wrapElements(T... elements) {
        return Stream.of(elements)
            .filter(Objects::nonNull)
            .map(element -> new Element<T>().setValue(element))
            .collect(toList());
    }

    public static <T> List<Element<T>> wrapElements(List<T> elements) {
        return nullSafeCollection(elements).stream()
            .filter(Objects::nonNull)
            .map(element -> new Element<T>().setValue(element))
            .collect(toList());
    }

    public static <T> List<T> unwrapElements(List<Element<T>> elements) {
        return nullSafeCollection(elements)
            .stream()
            .map(Element::getValue)
            .filter(Objects::nonNull).toList();
    }

    public static <T> List<T> unwrapElementsNullSafe(List<Element<T>> elements) {
        return Optional.ofNullable(elements).map(ElementUtils::unwrapElements).orElse(Collections.emptyList());
    }

    public static <T> Element<T> element(T element) {
        return new Element<T>().setId(UUID.randomUUID()).setValue(element);
    }

    public static Element<CaseDocument> buildElemCaseDocument(Document document, String createdBy,
                                                        LocalDateTime createdAt, DocumentType type) {
        CaseDocument caseDocument = new CaseDocument()
            .setDocumentLink(document)
            .setDocumentName(document.getDocumentFileName())
            .setDocumentType(type)
            .setCreatedDatetime(createdAt)
            .setCreatedBy(createdBy);
        return ElementUtils.element(caseDocument);
    }

    private static <T> Collection<T> nullSafeCollection(Collection<T> collection) {
        return collection == null ? Collections.emptyList() : collection;
    }

}
