package uk.gov.hmcts.reform.civil.service.hearings;

import org.elasticsearch.core.List;
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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.UNSPEC_CLAIM;

@ExtendWith(SpringExtension.class)
public class CaseCategoriesServiceTest {

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
    }

    @Test
    void shouldReturnNull_whenCategorySearchResultNotPresent() {
        when(categoryService.findCategoryByCategoryIdAndServiceId(any(), any(), any())).thenReturn(Optional.empty());
        CaseCategoryModel actual = caseCategoriesService.getCaseCategoriesFor(
            CategoryType.CASE_TYPE,
            caseData,
            AUTH
        );

        assertThat(actual).isEqualTo(null);
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
