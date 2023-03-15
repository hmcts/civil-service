package uk.gov.hmcts.reform.civil.utils;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.crd.model.Category;
import uk.gov.hmcts.reform.civil.crd.model.CategorySearchResult;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HearingMethodUtilsTest {

    @Test
    void shouldReturnListOfHearingMethods_WhenCategorySearchResultProvided() {
        String value = "In Person";
        Category category = Category.builder().categoryKey("HearingChannel").key("INTER").valueEn(value).activeFlag("Y").build();
        CategorySearchResult categorySearchResult = CategorySearchResult.builder().categories(List.of(category)).build();

        DynamicList hearingMethodList = HearingMethodUtils.getHearingMethodList(categorySearchResult);
        assertEquals(value, hearingMethodList.getListItems().get(0).getLabel());
    }

    @Test
    void shouldReturnEmptyList_WhenCategorySearchResultNotProvided() {
        DynamicList hearingMethodList = HearingMethodUtils.getHearingMethodList(null);
        assertTrue(hearingMethodList.getListItems().isEmpty());
    }
}
