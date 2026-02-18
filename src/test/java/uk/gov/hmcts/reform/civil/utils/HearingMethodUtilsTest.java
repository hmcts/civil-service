package uk.gov.hmcts.reform.civil.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.crd.model.Category;
import uk.gov.hmcts.reform.civil.crd.model.CategorySearchResult;
import uk.gov.hmcts.reform.civil.service.CategoryService;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class HearingMethodUtilsTest {

    @Mock
    private CategoryService categoryService;

    @Test
    void shouldReturnListOfHearingMethods_WhenCategorySearchResultProvided() {
        String value = "In Person";
        Category category = hearingChannel("INTER", value);
        CategorySearchResult categorySearchResult = new CategorySearchResult();
        categorySearchResult.setCategories(List.of(category));

        DynamicList hearingMethodList = HearingMethodUtils.getHearingMethodList(categorySearchResult);
        assertEquals(value, hearingMethodList.getListItems().get(0).getLabel());
    }

    @Test
    void shouldReturnEmptyList_WhenCategorySearchResultNotProvided() {
        DynamicList hearingMethodList = HearingMethodUtils.getHearingMethodList(null);
        assertTrue(hearingMethodList.getListItems().isEmpty());
    }

    @Test
    void shouldReturnMapOfCodes_WhenCategorySearchResultNotNull() {
        Category inPerson = hearingChannel("INTER", "In Person");
        Category video = hearingChannel("VID", "Video");
        Category telephone = hearingChannel("TEL", "Telephone");
        CategorySearchResult categorySearchResult = new CategorySearchResult();
        categorySearchResult.setCategories(List.of(inPerson, video, telephone));
        when(categoryService.findCategoryByCategoryIdAndServiceId(anyString(), eq("HearingChannel"), anyString())).thenReturn(
            Optional.of(categorySearchResult));

        Map<String, String> actual = HearingMethodUtils.getHearingMethodCodes(categoryService, "", "");

        assertThat(actual).isEqualTo(Map.of("In Person", "INTER", "Video", "VID", "Telephone", "TEL"));
    }

    @Test
    void shouldReturnNull_WhenCategorySearchResultNull() {
        CategorySearchResult categorySearchResult = new CategorySearchResult();
        categorySearchResult.setCategories(emptyList());
        when(categoryService.findCategoryByCategoryIdAndServiceId(anyString(), eq("HearingChannel"), anyString())).thenReturn(
            Optional.of(categorySearchResult));

        Map<String, String> actual = HearingMethodUtils.getHearingMethodCodes(categoryService, "", "");
        assertThat(actual).isEqualTo(emptyMap());
    }

    private Category hearingChannel(String key, String valueEn) {
        return new Category()
            .setCategoryKey("HearingChannel")
            .setKey(key)
            .setValueEn(valueEn)
            .setActiveFlag("Y");
    }
}
