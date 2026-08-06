package uk.gov.hmcts.reform.civil.service.hearings;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.civil.config.PaymentsConfiguration;
import uk.gov.hmcts.reform.civil.crd.model.Category;
import uk.gov.hmcts.reform.civil.crd.model.CategorySearchResult;
import uk.gov.hmcts.reform.civil.enums.hearing.CategoryType;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.hearingvalues.CaseCategoryModel;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.CategoryService;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.AllocatedTrack.FAST_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.AllocatedTrack.MULTI_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.UNSPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@ExtendWith(SpringExtension.class)
class CaseCategoriesServiceTest {

    private static final String AUTH = "auth";

    @Mock
    private CategoryService categoryService;

    @Mock
    private PaymentsConfiguration paymentsConfiguration;

    @InjectMocks
    private CaseCategoriesService caseCategoriesService;

    private CaseData caseData;

    @BeforeEach
    void setUp() {
        caseData = CaseDataBuilder.builder().atStateClaimIssued()
            .caseAccessCategory(UNSPEC_CLAIM)
            .build();

        given(paymentsConfiguration.getSiteId()).willReturn("AAA7");
        given(paymentsConfiguration.getSpecSiteId()).willReturn("AAA6");
    }

    @Test
    void shouldReturnNull_whenCategorySearchResultNotPresent() {
        when(categoryService.findCategoryByCategoryIdAndServiceId(any(), any(), any())).thenReturn(Optional.empty());
        CaseCategoryModel actual = caseCategoriesService.getCaseCategoriesFor(
            CategoryType.CASE_TYPE,
            caseData,
            AUTH
        );

        assertThat(actual).isNull();
    }

    @Test
    void shouldReturnCaseType_whenCategorySearchResultPresent() {
        CategorySearchResult categorySearchResult = new CategorySearchResult();
        categorySearchResult.setCategories(List.of(
            new Category()
                .setCategoryKey("caseType")
                .setKey("AAA7-FAST_CLAIM")
        ));
        when(categoryService.findCategoryByCategoryIdAndServiceId(any(), any(), any()))
            .thenReturn(Optional.of(categorySearchResult));
        CaseCategoryModel expected = new CaseCategoryModel();
        expected.setCategoryParent(null);
        expected.setCategoryType(CategoryType.CASE_TYPE);
        expected.setCategoryValue("AAA7-FAST_CLAIM");

        caseData = caseData.toBuilder().allocatedTrack(FAST_CLAIM).build();

        CaseCategoryModel actual = caseCategoriesService.getCaseCategoriesFor(
            CategoryType.CASE_TYPE,
            caseData,
            AUTH
        );

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void shouldReturnNull_whenCategorySearchResultDoesNotContainMatchingCategoryKey() {
        CategorySearchResult categorySearchResult = new CategorySearchResult();
        categorySearchResult.setCategories(List.of(
            new Category()
                .setCategoryKey("caseType")
                .setKey("AAA7-SMALL_CLAIM")
        ));
        when(categoryService.findCategoryByCategoryIdAndServiceId(any(), any(), any()))
            .thenReturn(Optional.of(categorySearchResult));

        caseData = caseData.toBuilder().allocatedTrack(FAST_CLAIM).build();

        CaseCategoryModel actual = caseCategoriesService.getCaseCategoriesFor(
            CategoryType.CASE_TYPE,
            caseData,
            AUTH
        );

        assertThat(actual).isNull();
    }

    @Test
    void shouldThrowDescriptiveException_whenMultipleMatchingCategoryKeysFound() {
        CategorySearchResult categorySearchResult = new CategorySearchResult();
        categorySearchResult.setCategories(List.of(
            new Category()
                .setCategoryKey("caseType")
                .setKey("AAA7-FAST_CLAIM"),
            new Category()
                .setCategoryKey("caseType")
                .setKey("AAA7-FAST_CLAIM")
        ));
        when(categoryService.findCategoryByCategoryIdAndServiceId(any(), any(), any()))
            .thenReturn(Optional.of(categorySearchResult));

        caseData = caseData.toBuilder().allocatedTrack(FAST_CLAIM).build();

        assertThatThrownBy(() -> caseCategoriesService.getCaseCategoriesFor(
            CategoryType.CASE_TYPE,
            caseData,
            AUTH
        ))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Expected one category for categoryKey AAA7-FAST_CLAIM but found 2");
    }

    @Test
    void shouldReturnCaseType_whenJudgeReallocatedTrack() {
        CategorySearchResult categorySearchResult = new CategorySearchResult();
        categorySearchResult.setCategories(List.of(
            new Category()
                .setCategoryKey("caseType")
                .setKey("AAA7-MULTI_CLAIM")
        ));
        when(categoryService.findCategoryByCategoryIdAndServiceId(any(), any(), any()))
            .thenReturn(Optional.of(categorySearchResult));
        CaseCategoryModel expected = new CaseCategoryModel();
        expected.setCategoryParent(null);
        expected.setCategoryType(CategoryType.CASE_TYPE);
        expected.setCategoryValue("AAA7-MULTI_CLAIM");

        caseData = caseData.toBuilder()
            .allocatedTrack(FAST_CLAIM)
            .finalOrderAllocateToTrack(YES)
            .finalOrderTrackAllocation(MULTI_CLAIM)
            .build();

        CaseCategoryModel actual = caseCategoriesService.getCaseCategoriesFor(
            CategoryType.CASE_TYPE,
            caseData,
            AUTH
        );

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void shouldReturnCaseType_whenSpecClaimWithResponseTrack() {
        CategorySearchResult categorySearchResult = new CategorySearchResult();
        categorySearchResult.setCategories(List.of(
            new Category()
                .setCategoryKey("caseType")
                .setKey("AAA6-FAST_CLAIM")
        ));
        when(categoryService.findCategoryByCategoryIdAndServiceId(any(), any(), any()))
            .thenReturn(Optional.of(categorySearchResult));
        CaseCategoryModel expected = new CaseCategoryModel();
        expected.setCategoryParent(null);
        expected.setCategoryType(CategoryType.CASE_TYPE);
        expected.setCategoryValue("AAA6-FAST_CLAIM");

        caseData = caseData.toBuilder()
            .caseAccessCategory(SPEC_CLAIM)
            .responseClaimTrack("FAST_CLAIM")
            .build();

        CaseCategoryModel actual = caseCategoriesService.getCaseCategoriesFor(
            CategoryType.CASE_TYPE,
            caseData,
            AUTH
        );

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void shouldReturnCaseType_whenSpecClaimWithNoResponseTrack() {
        CategorySearchResult categorySearchResult = new CategorySearchResult();
        categorySearchResult.setCategories(List.of(
            new Category()
                .setCategoryKey("caseType")
                .setKey("AAA6-SMALL_CLAIM")
        ));
        when(categoryService.findCategoryByCategoryIdAndServiceId(any(), any(), any()))
            .thenReturn(Optional.of(categorySearchResult));
        CaseCategoryModel expected = new CaseCategoryModel();
        expected.setCategoryParent(null);
        expected.setCategoryType(CategoryType.CASE_TYPE);
        expected.setCategoryValue("AAA6-SMALL_CLAIM");

        caseData = caseData.toBuilder()
            .caseAccessCategory(SPEC_CLAIM)
            .responseClaimTrack(null)
            .build();

        CaseCategoryModel actual = caseCategoriesService.getCaseCategoriesFor(
            CategoryType.CASE_TYPE,
            caseData,
            AUTH
        );

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void shouldReturnCaseSubType_whenCategorySearchResultPresent() {
        CategorySearchResult categorySearchResult = new CategorySearchResult();
        categorySearchResult.setCategories(List.of(
            new Category()
                .setCategoryKey("caseSubType")
                .setKey("AAA7-FAST_CLAIM")
                .setParentKey("AAA7-FAST_CLAIM")
        ));
        when(categoryService.findCategoryByCategoryIdAndServiceId(any(), any(), any()))
            .thenReturn(Optional.of(categorySearchResult));
        CaseCategoryModel expected = new CaseCategoryModel();
        expected.setCategoryParent("AAA7-FAST_CLAIM");
        expected.setCategoryType(CategoryType.CASE_SUBTYPE);
        expected.setCategoryValue("AAA7-FAST_CLAIM");

        CaseCategoryModel actual = caseCategoriesService.getCaseCategoriesFor(
            CategoryType.CASE_SUBTYPE,
            caseData,
            AUTH
        );

        assertThat(actual).isEqualTo(expected);
    }
}
