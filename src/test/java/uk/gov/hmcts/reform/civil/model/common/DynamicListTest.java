package uk.gov.hmcts.reform.civil.model.common;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class DynamicListTest {

    private List<String> listItems;

    @BeforeEach
    public void setUp() {
        listItems = Arrays.asList("Item1", "Item2", "Item3");
    }

    @Test
    public void shouldPopulateDynamicListFromList_whenCodeFunctionIsProvided() {
        Function<String, String> toCode = item -> item + "_code";
        Function<String, String> toLabel = item -> item + "_label";
        String value = "Item2";

        DynamicList actual = DynamicList.fromList(listItems, toCode, toLabel, value, true);

        DynamicList expected = DynamicList.builder()
            .listItems(List.of(
                DynamicListElement.builder().code("Item1_code").label("Item1_label").build(),
                DynamicListElement.builder().code("Item2_code").label("Item2_label").build(),
                DynamicListElement.builder().code("Item3_code").label("Item3_label").build()
            ))
            .value(DynamicListElement.builder().code("Item2_code").label("Item2_label").build())
            .build();

        assertEquals(expected, actual);
    }

    @Test
    public void shouldPopulateDynamicListFromList_whenCodeFunctionIsNotProvided() {
        Function<String, String> toLabel = item -> item + "_label";
        String value = "Item2";

        DynamicList actual = DynamicList.fromList(listItems, null, toLabel, value, true);

        assertNotNull(actual.getListItems().get(0).getCode());
        assertEquals("Item1_label", actual.getListItems().get(0).getLabel());
        assertNotNull(actual.getListItems().get(1).getCode());
        assertEquals("Item2_label", actual.getListItems().get(1).getLabel());
        assertNotNull(actual.getListItems().get(2).getCode());
        assertEquals("Item3_label", actual.getListItems().get(2).getLabel());
        assertNotNull(actual.getValue().getCode());
        assertEquals("Item2_label", actual.getValue().getLabel());
    }
}
