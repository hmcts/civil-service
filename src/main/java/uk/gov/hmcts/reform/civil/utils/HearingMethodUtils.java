package uk.gov.hmcts.reform.civil.utils;

import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.crd.model.Category;
import uk.gov.hmcts.reform.civil.crd.model.CategorySearchResult;
import uk.gov.hmcts.reform.civil.service.refdata.CategoryService;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HearingMethodUtils {

    private static final String ACTIVE_FLAG = "Y";
    private static final String HEARING_CHANNEL = "HearingChannel";

    private HearingMethodUtils() {
        //NO-OP
    }

    public static DynamicList getHearingMethodList(CategorySearchResult categorySearchResult) {
        DynamicList hearingMethodList;
        if (categorySearchResult != null) {
            List<Category> categories = categorySearchResult.getCategories().stream()
                .filter(category -> category.getActiveFlag().equals(ACTIVE_FLAG)).toList();
            hearingMethodList = DynamicList.fromList(categories, Category::getValueEn, null, false);
        } else {
            hearingMethodList = DynamicList.fromList(List.of());
        }
        return hearingMethodList;
    }

    public static Map<String, String> getHearingMethodCodes(CategoryService categoryService, String hmctsServiceId, String authToken) {
        Optional<CategorySearchResult> categorySearchResult = categoryService.findCategoryByCategoryIdAndServiceId(
            authToken, HEARING_CHANNEL, hmctsServiceId
        );
        log.info("categorySearchResult {}", categorySearchResult);
        final Map<String, String> hearingMethodCodes =
            categorySearchResult.map(searchResult -> searchResult.getCategories().stream()
                    .filter(category -> category.getActiveFlag().equals(ACTIVE_FLAG))
                    .collect(Collectors.toMap(Category::getValueEn, Category::getKey)))
                .orElse(null);
        
        log.info("hearingMethodCodes {}", hearingMethodCodes);
        return hearingMethodCodes;
    }

}
