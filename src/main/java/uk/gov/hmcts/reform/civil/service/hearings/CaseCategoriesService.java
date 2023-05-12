package uk.gov.hmcts.reform.civil.service.hearings;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.config.PaymentsConfiguration;
import uk.gov.hmcts.reform.civil.crd.model.Category;
import uk.gov.hmcts.reform.civil.crd.model.CategorySearchResult;
import uk.gov.hmcts.reform.civil.enums.hearing.CategoryType;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.hearingvalues.CaseCategoryModel;
import uk.gov.hmcts.reform.civil.service.CategoryService;
import uk.gov.hmcts.reform.civil.utils.HmctsServiceIDUtils;

import java.util.Optional;

import static uk.gov.hmcts.reform.civil.utils.CollectorUtils.toSingleton;

@Service
@RequiredArgsConstructor
@Slf4j
public class CaseCategoriesService {

    private final CategoryService categoryService;
    private final PaymentsConfiguration paymentsConfiguration;

    private static final String CATEGORY_KEY = "%s-%s";
    private static final String CASE_SUB_TYPE = "caseSubType";
    private static final String CASE_TYPE = "caseType";

    public CaseCategoryModel getCaseCategoriesFor(CategoryType categoryType, CaseData caseData, String authToken) {
        String hmctsServiceID = HmctsServiceIDUtils.getHmctsServiceID(caseData, paymentsConfiguration);
        Optional<CategorySearchResult> caseTypeResult = categoryService.findCategoryByCategoryIdAndServiceId(
            authToken,
            categoryType.getStringValueForQuery(),
            hmctsServiceID
        );

        String allocatedTrack = caseData.getAllocatedTrack() != null
            ? caseData.getAllocatedTrack().toString()  //unspec
            : caseData.getResponseClaimTrack(); //spec

        String categoryKey = String.format(CATEGORY_KEY, hmctsServiceID, allocatedTrack);

        if (caseTypeResult.isPresent()) {
            CategorySearchResult categorySearchResult = caseTypeResult.get();
            Category categoryResult = categorySearchResult.getCategories().stream().filter(c -> c.getKey().equals(
                categoryKey)).collect(toSingleton());

            return CaseCategoryModel.builder()
                .categoryParent(categoryResult.getParentKey())
                .categoryType(getCategoryTypeFromResult(categoryResult))
                .categoryValue(categoryResult.getKey())
                .build();
        }
        return null;
    }

    private CategoryType getCategoryTypeFromResult(Category category) {
        if (CASE_TYPE.equals(category.getCategoryKey())) {
            return CategoryType.CASE_TYPE;
        } else if (CASE_SUB_TYPE.equals(category.getCategoryKey())) {
            return CategoryType.CASE_SUBTYPE;
        }
        return null;
    }
}
