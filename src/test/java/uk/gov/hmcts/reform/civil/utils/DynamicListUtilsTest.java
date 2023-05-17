package uk.gov.hmcts.reform.civil.utils;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static uk.gov.hmcts.reform.civil.utils.DynamicListUtils.getDynamicListValue;
import static uk.gov.hmcts.reform.civil.utils.DynamicListUtils.listFromDynamicList;

class DynamicListUtilsTest {

    @Nested
    class ListFromDynamicList {
        @Test
        void shouldCreateListFromDynamicList() {
            List<String> stringList = List.of("item 1", "item 2");

            DynamicList dynamicList = DynamicList.builder()
                .listItems(List.of(DynamicListElement.builder()
                                       .label("item 1")
                                       .build(),
                                   DynamicListElement.builder()
                                       .label("item 2")
                                       .build()))
                .build();

            assertThat(listFromDynamicList(dynamicList)).isEqualTo(stringList);
        }

        @Test
        void shouldReturnNull_whenNullDynamicListItems() {
            DynamicList dynamicList = DynamicList.builder()
                .listItems(null)
                .build();

            assertThat(listFromDynamicList(dynamicList)).isNull();
        }

        @Test
        void shouldReturnNull_whenNullDynamicList() {
            DynamicList dynamicList = DynamicList.builder().build();

            assertThat(listFromDynamicList(dynamicList)).isNull();
        }
    }

    @Nested
    class ValueFromDynamicList {
        void shouldReturnString_whenDynamicListHasValue() {
            String expected = "item 1";
            DynamicList dynamicList = DynamicList.builder()
                .listItems(List.of(
                    DynamicListElement.builder()
                        .label("item 1")
                        .build(),
                    DynamicListElement.builder()
                        .label("item 2")
                        .build()))
                .value(DynamicListElement.builder()
                           .label("item 1")
                           .build())
                .build();

            assertThat(getDynamicListValue(dynamicList)).isEqualTo(expected);
        }
    }

    void shouldReturnNull_whenDynamicListHasNoValue() {
        DynamicList dynamicList = DynamicList.builder()
            .listItems(List.of(
                DynamicListElement.builder()
                    .label("item 1")
                    .build(),
                DynamicListElement.builder()
                    .label("item 2")
                    .build()))
            .build();

        assertThat(getDynamicListValue(dynamicList)).isNull();
    }
}
