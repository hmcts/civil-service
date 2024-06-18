package uk.gov.hmcts.reform.civil.utils;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.documents.CaseDocument;

import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.unwrapElements;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

class ElementUtilsTest {

    @Nested
    class WrapElements {
        Element<String> element1 = Element.<String>builder().value("First").build();
        Element<String> element2 = Element.<String>builder().value("Second").build();

        @Test
        void shouldWrapAllObjectsWithElement() {
            assertThat(wrapElements("First", "Second")).containsExactly(element1, element2);
        }

        @Test
        void shouldReturnEmptyElementListIfNoObjectsToWrap() {
            assertThat(wrapElements()).isEmpty();
        }

        @Test
        void shouldWrapNonNullObjectsWithElement() {
            assertThat(wrapElements("First", null)).containsExactly(element1);
        }
    }

    @Nested
    class UnwrapElements {
        Element<String> element1 = Element.<String>builder().id(randomUUID()).value("First").build();
        Element<String> element2 = Element.<String>builder().id(randomUUID()).value("Second").build();
        Element<String> elementWithoutValue = Element.<String>builder().id(randomUUID()).build();

        @Test
        void shouldUnwrapAllElements() {
            assertThat(unwrapElements(List.of(element1, element2))).containsExactly("First", "Second");
        }

        @Test
        void shouldExcludeElementsWithNullValues() {
            assertThat(unwrapElements(List.of(element1, elementWithoutValue))).containsExactly("First");
        }

        @Test
        void shouldReturnEmptyListIfListOfElementIsEmpty() {
            assertThat(unwrapElements(emptyList())).isEmpty();
        }

        @Test
        void shouldReturnEmptyListIfListOfElementIsNull() {
            assertThat(unwrapElements(null)).isEmpty();
        }
    }

    @Nested
    class BuildElement {

        @Test
        void shouldBuildElement_whenObjectIsProvided() {
            CaseDocument document = CaseDocument.builder().build();

            Element<CaseDocument> element = ElementUtils.element(document);

            assertThat(element.getId()).isNotNull();
            assertThat(element.getValue()).isEqualTo(document);
        }
    }
}
