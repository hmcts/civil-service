package uk.gov.hmcts.reform.civil.helpers.hearingsmappings;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.civil.enums.hearing.CategoryType;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocationCivil;
import uk.gov.hmcts.reform.civil.model.hearingvalues.CaseCategoryModel;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.hearings.CaseCategoriesService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

        assertThat(publicCaseName).isEqualTo("'John Rambo' v 'Sole Trader'");
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
    void shouldReturnFalse_whenCaseAdditionalSecurityFlagInvoked() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build();
        assertThat(ServiceHearingsCaseLevelMapper.getCaseAdditionalSecurityFlag(caseData))
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

    @Nested
    class GetPublicCaseName {

        Party applicant1;
        Party respondent1;

        @BeforeEach
        void setupParties() {
            applicant1 = Party.builder()
                .individualFirstName("Applicant")
                .individualLastName("One")
                .type(Party.Type.INDIVIDUAL).build();

            respondent1 = Party.builder()
                .individualFirstName("Respondent")
                .individualLastName("One")
                .type(Party.Type.INDIVIDUAL).build();
        }

        @Test
        void shouldReturnExpectedPublicCaseName_whenCaseNamePublicExists() {
            var expected = "'A Somebody' vs 'Somebody else'";
            var caseData = CaseData.builder()
                .caseNamePublic(expected)
                .applicant1(applicant1)
                .respondent1(respondent1)
                .build();

            var actual = ServiceHearingsCaseLevelMapper.getPublicCaseName(caseData);

            assertThat(actual).isEqualTo(expected);
        }

        @Test
        void shouldReturnExpectedPublicCaseName_whenCaseNamePublicDoesNotExist() {
            var expected = "'Applicant One' v 'Respondent One'";
            var caseData = CaseData.builder()
                .applicant1(applicant1)
                .respondent1(respondent1)
                .build();

            var actual = ServiceHearingsCaseLevelMapper.getPublicCaseName(caseData);

            assertThat(actual).isEqualTo(expected);
        }
    }

    @Nested
    class GetCaseSLAStartDate {
        @Test
        void shouldReturnExpectedSLAStartDateInStringFormat_whenClaimIssued() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build();
            var date = LocalDateTime.now();
            var expected = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

            var actual = ServiceHearingsCaseLevelMapper.getCaseSLAStartDate(caseData);

            assertThat(actual).isEqualTo(expected);
        }

        @Test
        void shouldReturnExpectedSLAStartDateInStringFormat_whenClaimSubmitted() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmitted().build();
            var date = LocalDateTime.now();
            var expected = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

            var actual = ServiceHearingsCaseLevelMapper.getCaseSLAStartDate(caseData);

            assertThat(actual).isEqualTo(expected);
        }
    }
}
