package uk.gov.hmcts.reform.civil.utils;

import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.crd.model.Category;
import uk.gov.hmcts.reform.crd.model.CategorySearchResult;

import java.util.List;
import java.util.stream.Collectors;

public class HearingMethodUtils {

    private static final String ACTIVE_FLAG = "Y";

    private HearingMethodUtils() {
        //NO-OP
    }

    public static DynamicList getHearingMethodList(CategorySearchResult categorySearchResult) {
        DynamicList hearingMethodList;
        if (categorySearchResult != null) {
            List<Category> categories = categorySearchResult.getCategories().stream()
                .filter(category -> category.getActiveFlag().equals(ACTIVE_FLAG)).collect(Collectors.toList());
            hearingMethodList = DynamicList.fromList(categories, Category::getValueEn, null, false);
        } else {
            hearingMethodList = DynamicList.fromList(List.of());
        }
        return hearingMethodList;
    }

}
