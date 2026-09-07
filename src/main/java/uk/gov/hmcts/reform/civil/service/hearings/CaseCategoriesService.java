package uk.gov.hmcts.reform.civil.service.hearings;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.config.PaymentsConfiguration;
import uk.gov.hmcts.reform.civil.crd.model.Category;
import uk.gov.hmcts.reform.civil.crd.model.CategorySearchResult;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.enums.hearing.CategoryType;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.hearingvalues.CaseCategoryModel;
import uk.gov.hmcts.reform.civil.service.CategoryService;
import uk.gov.hmcts.reform.civil.utils.HmctsServiceIDUtils;

import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.UNSPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

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

        String categoryKey = String.format(CATEGORY_KEY, hmctsServiceID, allocatedTrack);
        log.info("Searching for category allocatedTrack={}, categoryKey={}", allocatedTrack, categoryKey);

        Optional<CategorySearchResult> caseTypeResult = categoryService.findCategoryByCategoryIdAndServiceId(
            authToken,
            categoryType.getStringValueForQuery(),
            hmctsServiceID
        );
        if (caseTypeResult.isPresent()) {
            CategorySearchResult categorySearchResult = caseTypeResult.get();
            log.info(
                "CategorySearchResult found with {} categories: {}",
                categorySearchResult.getCategories() == null ? null : categorySearchResult.getCategories().size(),
                categorySearchResult.getCategories()
            );

            List<Category> matchingCategories = Optional.ofNullable(categorySearchResult.getCategories())
                .orElseGet(List::of)
                .stream()
                .filter(c -> c.getKey().equals(categoryKey))
                .toList();
            if (matchingCategories.isEmpty()) {
                log.warn(
                    "Category not found caseNumber={}, allocatedTrack={}, categoryKey={}, categoryType={}",
                    caseData.getCcdCaseReference(), allocatedTrack, categoryKey, categoryType
                );
                return null;
            }

            if (matchingCategories.size() > 1) {
                throw new IllegalStateException(String.format(
                    "Expected one category for categoryKey %s but found %d",
                    categoryKey, matchingCategories.size()
                ));
            }

            Category categoryResult = matchingCategories.get(0);
            log.info("Category found: parentKey={}, key={}", categoryResult.getParentKey(), categoryResult.getKey());

            CaseCategoryModel caseCategoryModel = new CaseCategoryModel();
            caseCategoryModel.setCategoryParent(categoryResult.getParentKey());
            caseCategoryModel.setCategoryType(getCategoryTypeFromResult(categoryResult));
            caseCategoryModel.setCategoryValue(categoryResult.getKey());
            return caseCategoryModel;
        }

        log.info("No Search Result found for category allocatedTrack={}, categoryKey={}", allocatedTrack, categoryKey);
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
        if (YES.equals(caseData.getFinalOrderAllocateToTrack())
            && caseData.getFinalOrderTrackAllocation() != null) {
            return caseData.getFinalOrderTrackAllocation().name();
        }

        if (UNSPEC_CLAIM.equals(caseData.getCaseAccessCategory())) {
            return caseData.getAllocatedTrack() != null ? caseData.getAllocatedTrack().name() : null;
        } else if (SPEC_CLAIM.equals(caseData.getCaseAccessCategory())) {
            return caseData.getResponseClaimTrack() != null
                ? caseData.getResponseClaimTrack()
                : AllocatedTrack.SMALL_CLAIM.name();
        }

        return null;
    }
}
