package uk.gov.hmcts.reform.civil.utils;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static uk.gov.hmcts.reform.civil.utils.DynamicListUtils.getDynamicListValue;
import static uk.gov.hmcts.reform.civil.utils.DynamicListUtils.listFromDynamicList;
import static uk.gov.hmcts.reform.civil.utils.DynamicListUtils.trimToSelectedValue;

class DynamicListUtilsTest {

    @Nested
    class ListFromDynamicList {
        @Test
        void shouldCreateListFromDynamicList() {
            List<String> stringList = List.of("item 1", "item 2");

            DynamicList dynamicList = new DynamicList().setListItems(List.of(new DynamicListElement().setLabel("item 1"),
                                   new DynamicListElement().setLabel("item 2")));

            assertThat(listFromDynamicList(dynamicList)).isEqualTo(stringList);
        }

        @Test
        void shouldReturnNull_whenNullDynamicListItems() {
            DynamicList dynamicList = new DynamicList().setListItems(null);

            assertThat(listFromDynamicList(dynamicList)).isNull();
        }

        @Test
        void shouldReturnNull_whenNullDynamicList() {
            DynamicList dynamicList = new DynamicList();

            assertThat(listFromDynamicList(dynamicList)).isNull();
        }
    }

    @Nested
    class ValueFromDynamicList {
        void shouldReturnString_whenDynamicListHasValue() {
            String expected = "item 1";
            DynamicList dynamicList = new DynamicList().setListItems(List.of(
                    new DynamicListElement().setLabel("item 1"),
                    new DynamicListElement().setLabel("item 2"))).setValue(new DynamicListElement().setLabel("item 1"));

            assertThat(getDynamicListValue(dynamicList)).isEqualTo(expected);
        }
    }

    void shouldReturnNull_whenDynamicListHasNoValue() {
        DynamicList dynamicList = new DynamicList().setListItems(List.of(
                new DynamicListElement().setLabel("item 1"),
                new DynamicListElement().setLabel("item 2")));

        assertThat(getDynamicListValue(dynamicList)).isNull();
    }

    @Nested
    class TrimDynamicList {
        @Test
        void shouldTrimListItemsToSelectedValue() {
            DynamicListElement selected = new DynamicListElement().setCode("CODE").setLabel("Label");

            DynamicList dynamicList = new DynamicList().setListItems(List.of(
                    selected,
                    new DynamicListElement().setCode("OTHER").setLabel("Other")
                )).setValue(selected);

            DynamicList trimmed = trimToSelectedValue(dynamicList);

            assertThat(trimmed.getValue()).isEqualTo(selected);
            assertThat(trimmed.getListItems()).isNull();
        }

        @Test
        void shouldReturnNull_whenOriginalListNull() {
            assertThat(trimToSelectedValue(null)).isNull();
        }
    }
}
