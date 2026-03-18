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

        DynamicList expected = new DynamicList().setListItems(List.of(
                new DynamicListElement().setCode("Item1_code").setLabel("Item1_label"),
                new DynamicListElement().setCode("Item2_code").setLabel("Item2_label"),
                new DynamicListElement().setCode("Item3_code").setLabel("Item3_label")
            )).setValue(new DynamicListElement().setCode("Item2_code").setLabel("Item2_label"));

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
