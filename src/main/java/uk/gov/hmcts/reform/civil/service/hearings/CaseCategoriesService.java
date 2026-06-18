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
        String allocatedTrack = getClaimTrack(caseData);
        if (allocatedTrack == null) {
            log.info(
                "No claim track found for category search caseNumber={}, hmctsServiceID={}, categoryType={}",
                caseData.getCcdCaseReference(), hmctsServiceID, categoryType
            );
            return null;
        }

        Optional<CategorySearchResult> caseTypeResult = categoryService.findCategoryByCategoryIdAndServiceId(
            authToken,
            categoryType.getStringValueForQuery(),
            hmctsServiceID
        );

        String categoryKey = String.format(CATEGORY_KEY, hmctsServiceID, allocatedTrack);
        log.info(
            "Searching for category caseNumber={}, hmctsServiceID={}, allocatedTrack={}, categoryKey={}, categoryType={}",
            caseData.getCcdCaseReference(), hmctsServiceID, allocatedTrack, categoryKey, categoryType
        );

        if (caseTypeResult.isPresent()) {
            CategorySearchResult categorySearchResult = caseTypeResult.get();
            log.info(
                "CategorySearchResult found with {} categories",
                categorySearchResult.getCategories() == null ? null : categorySearchResult.getCategories().size()
            );

            Category categoryResult = categorySearchResult.getCategories().stream().filter(c -> c.getKey().equals(
                categoryKey)).collect(toSingleton());
            log.info("Category found: parentKey={}, key={}", categoryResult.getParentKey(), categoryResult.getKey());

            CaseCategoryModel caseCategoryModel = new CaseCategoryModel();
            caseCategoryModel.setCategoryParent(categoryResult.getParentKey());
            caseCategoryModel.setCategoryType(getCategoryTypeFromResult(categoryResult));
            caseCategoryModel.setCategoryValue(categoryResult.getKey());
            return caseCategoryModel;
        }

        log.info(
            "No CategorySearchResult found caseNumber={}, categoryType={}, categoryId={}, hmctsServiceID={}",
            caseData.getCcdCaseReference(), categoryType, categoryType.getStringValueForQuery(), hmctsServiceID
        );
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

    private String getClaimTrack(CaseData caseData) {
        return caseData.getAllocatedTrack() != null
            ? caseData.getAllocatedTrack().toString()
            : caseData.getResponseClaimTrack();
    }
}
