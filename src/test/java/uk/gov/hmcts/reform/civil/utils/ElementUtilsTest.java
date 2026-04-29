package uk.gov.hmcts.reform.civil.utils;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;

import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.unwrapElements;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

class ElementUtilsTest {

    @Nested
    class WrapElements {
        Element<String> element1 = new Element<String>().setValue("First");
        Element<String> element2 = new Element<String>().setValue("Second");

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
        Element<String> element1 = new Element<String>().setId(randomUUID()).setValue("First");
        Element<String> element2 = new Element<String>().setId(randomUUID()).setValue("Second");
        Element<String> elementWithoutValue = new Element<String>().setId(randomUUID());

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
            CaseDocument document = new CaseDocument();

            Element<CaseDocument> element = ElementUtils.element(document);

            assertThat(element.getId()).isNotNull();
            assertThat(element.getValue()).isEqualTo(document);
        }
    }
}
