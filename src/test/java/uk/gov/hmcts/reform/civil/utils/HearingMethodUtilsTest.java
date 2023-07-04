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

    @Test
    void shouldReturnMapOfCodes_WhenCategorySearchResultNotNull() {
        Category inPerson = Category.builder().categoryKey("HearingChannel").key("INTER").valueEn("In Person").activeFlag("Y").build();
        Category video = Category.builder().categoryKey("HearingChannel").key("VID").valueEn("Video").activeFlag("Y").build();
        Category telephone = Category.builder().categoryKey("HearingChannel").key("TEL").valueEn("Telephone").activeFlag("Y").build();
        CategorySearchResult categorySearchResult = CategorySearchResult.builder().categories(List.of(inPerson, video, telephone)).build();
        when(categoryService.findCategoryByCategoryIdAndServiceId(anyString(), eq("HearingChannel"), anyString())).thenReturn(
            Optional.of(categorySearchResult));

        Map<String, String> actual = HearingMethodUtils.getHearingMethodCodes(categoryService, "", "");

        assertThat(actual).isEqualTo(Map.of("In Person", "INTER", "Video", "VID", "Telephone", "TEL"));
    }

    @Test
    void shouldReturnNull_WhenCategorySearchResultNull() {
        when(categoryService.findCategoryByCategoryIdAndServiceId(anyString(), eq("HearingChannel"), anyString())).thenReturn(
            Optional.of(CategorySearchResult.builder().categories(emptyList()).build()));

        Map<String, String> actual = HearingMethodUtils.getHearingMethodCodes(categoryService, "", "");
        assertThat(actual).isEqualTo(emptyMap());
    }
}
