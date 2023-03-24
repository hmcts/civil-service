package uk.gov.hmcts.reform.civil.helpers.hearingsmappings;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.civil.enums.hearing.CategoryType;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocationCivil;
import uk.gov.hmcts.reform.civil.model.hearingvalues.CaseCategoryModel;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.hearings.CaseCategoriesService;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
public class ServiceHearingsCaseLevelMapperTest {

    @MockBean
    CaseCategoriesService caseCategoriesService;

    @Test
    void shouldReturnHmctsInternalCaseName_whenInvoked() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build();

        String hmctsInternalCaseName = ServiceHearingsCaseLevelMapper.getHmctsInternalCaseName(caseData);

        assertThat(hmctsInternalCaseName).isEqualTo("Mr. John Rambo v Mr. Sole Trader");
    }

    @Test
    void shouldReturnPublicCaseName_whenInvoked() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build();

        String publicCaseName = ServiceHearingsCaseLevelMapper.getPublicCaseName(caseData);

        assertThat(publicCaseName).isNull();
    }

    @Test
    void shouldReturnCaseDeepLink_whenInvoked() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build();

        Long caseId = 1237263L;

        String baseUrl = "localhost:3333";

        String expectedUrl = "localhost:3333/cases/case-details/1237263";

        String caseDeepLink = ServiceHearingsCaseLevelMapper.getCaseDeepLink(caseId, baseUrl);

        assertThat(caseDeepLink).isEqualTo(expectedUrl);
    }

    @Test
    void shouldReturnFalse_whenCaseRestrictedFlagInvoked() {
        assertThat(ServiceHearingsCaseLevelMapper.getCaseRestrictedFlag())
            .isEqualTo(false);
    }

    @Test
    void shouldReturnEmptyString_whenExternalCaseReferenceInvoked() {
        assertThat(ServiceHearingsCaseLevelMapper.getExternalCaseReference())
            .isEqualTo(null);
    }

    @Test
    void shouldReturnFalse_whenAutoListFlagInvoked() {
        assertThat(ServiceHearingsCaseLevelMapper.getAutoListFlag())
            .isEqualTo(false);
    }

    @Test
    void shouldReturnCaseManagementLocationCode_whenInvoked() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued()
            .caseManagementLocation(CaseLocationCivil.builder()
                                        .baseLocation("0123")
                                        .region("region")
                                        .build())
            .build();
        assertThat(ServiceHearingsCaseLevelMapper.getCaseManagementLocationCode(caseData))
            .isEqualTo("0123");
    }

    @Test
    void shouldReturnDateString_whenSLAStartDateInvoked() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build();
        assertThat(ServiceHearingsCaseLevelMapper.getCaseSLAStartDate(caseData))
            .isEqualTo("");
    }

    @Test
    void shouldReturnFalse_whenCaseAdditionalSecurityFlagInvoked() {
        assertThat(ServiceHearingsCaseLevelMapper.getCaseAdditionalSecurityFlag())
            .isEqualTo(false);
    }

    @Nested
    class CaseCategoriesMapping {
        @Test
        void shouldReturnListWithTypeAndSubtype_whenCaseCategoriesInvoked() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build();
            CaseCategoryModel caseType = CaseCategoryModel.builder()
                .categoryParent("")
                .categoryType(CategoryType.CASE_TYPE)
                .categoryValue("AAA7-SMALL_CLAIM")
                .build();
            CaseCategoryModel caseSubtype = CaseCategoryModel.builder()
                .categoryParent("AAA7-SMALL_CLAIM")
                .categoryType(CategoryType.CASE_SUBTYPE)
                .categoryValue("AAA7-SMALL_CLAIM")
                .build();

            List<CaseCategoryModel> expectedList = List.of(caseType, caseSubtype);

            when(caseCategoriesService.getCaseCategoriesFor(eq(CategoryType.CASE_TYPE), any(),  any())).thenReturn(
                caseType
            );
            when(caseCategoriesService.getCaseCategoriesFor(eq(CategoryType.CASE_SUBTYPE), any(),  any())).thenReturn(
                caseSubtype
            );
            List<CaseCategoryModel> actualList = ServiceHearingsCaseLevelMapper.getCaseCategories(
                caseData,
                caseCategoriesService,
                "auth"
            );
            assertThat(actualList)
                .isEqualTo(expectedList);
        }

        @Test
        void shouldReturnListWithType_whenCaseCategoriesInvoked() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build();
            CaseCategoryModel caseType = CaseCategoryModel.builder()
                .categoryParent("")
                .categoryType(CategoryType.CASE_TYPE)
                .categoryValue("AAA7-SMALL_CLAIM")
                .build();

            List<CaseCategoryModel> expectedList = List.of(caseType);

            when(caseCategoriesService.getCaseCategoriesFor(eq(CategoryType.CASE_TYPE), any(),  any())).thenReturn(
                caseType
            );
            when(caseCategoriesService.getCaseCategoriesFor(eq(CategoryType.CASE_SUBTYPE), any(),  any())).thenReturn(
                null
            );
            List<CaseCategoryModel> actualList = ServiceHearingsCaseLevelMapper.getCaseCategories(
                caseData,
                caseCategoriesService,
                "auth"
            );

            assertThat(actualList).isEqualTo(expectedList);
            assertThat(actualList.size()).isEqualTo(1);
            assertThat(actualList.contains(caseType)).isEqualTo(true);
        }

        @Test
        void shouldReturnListWithSubType_whenCaseCategoriesInvoked() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build();
            CaseCategoryModel caseSubtype = CaseCategoryModel.builder()
                .categoryParent("AAA7-SMALL_CLAIM")
                .categoryType(CategoryType.CASE_SUBTYPE)
                .categoryValue("AAA7-SMALL_CLAIM")
                .build();

            List<CaseCategoryModel> expectedList = List.of(caseSubtype);

            when(caseCategoriesService.getCaseCategoriesFor(eq(CategoryType.CASE_TYPE), any(),  any())).thenReturn(
                null
            );
            when(caseCategoriesService.getCaseCategoriesFor(eq(CategoryType.CASE_SUBTYPE), any(),  any())).thenReturn(
                caseSubtype
            );
            List<CaseCategoryModel> actualList = ServiceHearingsCaseLevelMapper.getCaseCategories(
                caseData,
                caseCategoriesService,
                "auth"
            );

            assertThat(actualList).isEqualTo(expectedList);
            assertThat(actualList.size()).isEqualTo(1);
            assertThat(actualList.contains(caseSubtype)).isEqualTo(true);
        }

        @Test
        void shouldReturnEmptyList_whenCaseCategoriesInvoked() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build();

            when(caseCategoriesService.getCaseCategoriesFor(eq(CategoryType.CASE_TYPE), any(),  any())).thenReturn(
                null
            );
            when(caseCategoriesService.getCaseCategoriesFor(eq(CategoryType.CASE_SUBTYPE), any(),  any())).thenReturn(
                null
            );
            List<CaseCategoryModel> actualList = ServiceHearingsCaseLevelMapper.getCaseCategories(
                caseData,
                caseCategoriesService,
                "auth"
            );
            assertThat(actualList)
                .isEqualTo(new ArrayList<>());
        }
    }
}
