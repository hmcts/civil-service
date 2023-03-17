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
        when(categoryService.findCategoryByCategoryIdAndServiceId(any(), any(), any()))
            .thenReturn(Optional.of(CategorySearchResult.builder()
                                        .categories(List.of(
                                            Category.builder()
                                                .categoryKey("caseType")
                                                .key("AAA7-FAST_CLAIM")
                                                .build()
                                        ))
                                        .build()));
        CaseCategoryModel actual = caseCategoriesService.getCaseCategoriesFor(
            CategoryType.CASE_TYPE,
            caseData,
            AUTH
        );

        CaseCategoryModel expected = CaseCategoryModel.builder()
            .categoryParent(null)
            .categoryType(CategoryType.CASE_TYPE)
            .categoryValue("AAA7-FAST_CLAIM")
            .build();

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void shouldReturnCaseSubType_whenCategorySearchResultPresent() {
        when(categoryService.findCategoryByCategoryIdAndServiceId(any(), any(), any()))
            .thenReturn(Optional.of(CategorySearchResult.builder()
                                        .categories(List.of(
                                            Category.builder()
                                                .categoryKey("caseSubType")
                                                .key("AAA7-FAST_CLAIM")
                                                .parentKey("AAA7-FAST_CLAIM")
                                                .build()
                                        ))
                                        .build()));
        CaseCategoryModel actual = caseCategoriesService.getCaseCategoriesFor(
            CategoryType.CASE_SUBTYPE,
            caseData,
            AUTH
        );

        CaseCategoryModel expected = CaseCategoryModel.builder()
            .categoryParent("AAA7-FAST_CLAIM")
            .categoryType(CategoryType.CASE_SUBTYPE)
            .categoryValue("AAA7-FAST_CLAIM")
            .build();

        assertThat(actual).isEqualTo(expected);
    }
}
