package uk.gov.hmcts.reform.civil.service.citizenui;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.ClaimValue;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.RespondToClaimAdmitPartLRspec;
import uk.gov.hmcts.reform.civil.model.citizenui.DashboardClaimInfo;
import uk.gov.hmcts.reform.civil.model.citizenui.DashboardClaimStatusFactory;
import uk.gov.hmcts.reform.civil.model.citizenui.DashboardDefendantResponse;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.claimstore.ClaimStoreService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(SpringExtension.class)
public class DashboardClaimInfoServiceTest {

    private static final String CLAIMANT_NAME = "Harry Porter";
    private static final String DEFENDANT_NAME = "James Bond";
    private static final BigDecimal PART_ADMIT_PAY_IMMEDIATELY_AMOUNT = BigDecimal.valueOf(500);
    private static final LocalDateTime DATE_IN_2021 = LocalDateTime.of(2021, 2, 20, 0, 0);
    private static final LocalDateTime DATE_IN_2022 = LocalDateTime.of(2022, 2, 20, 0, 0);
    private static final LocalDateTime DATE_IN_2025 = LocalDateTime.of(2025, 2, 20, 0, 0);
    private static final List<DashboardClaimInfo> CLAIM_STORE_SERVICE_RESULTS =
        Arrays.asList(DashboardClaimInfo.builder()
                          .ocmc(true)
                          .createdDate(DATE_IN_2025)
                          .build());
    private static final CaseDetails CASE_DETAILS = CaseDetails.builder()
        .id(1L)
        .createdDate(DATE_IN_2021)
        .build();
    private static final CaseDetails CASE_DETAILS_2 = CaseDetails.builder()
        .id(2L)
        .createdDate(DATE_IN_2022)
        .build();
    private static final int CURRENT_PAGE_NO = 1;
    private static final List<DashboardClaimInfo> ORDERED_CASES =
        Arrays.asList(
            DashboardClaimInfo.builder()
                .ocmc(true)
                .createdDate(DATE_IN_2021)
                .build(),
            DashboardClaimInfo.builder()
                .ocmc(true)
                .createdDate(DATE_IN_2022)
                .build()
        );
    @Mock
    private ClaimStoreService claimStoreService;
    @Mock
    private CoreCaseDataService coreCaseDataService;
    @Mock
    private CaseDetailsConverter caseDetailsConverter;
    @Mock
    private DashboardClaimStatusFactory dashboardClaimStatusFactory;
    @InjectMocks
    private DashboardClaimInfoService dashboardClaimInfoService;

    @BeforeEach
    void setUp() {
        given(claimStoreService.getClaimsForClaimant(any(), any())).willReturn(CLAIM_STORE_SERVICE_RESULTS);
        given(claimStoreService.getClaimsForDefendant(any(), any())).willReturn(CLAIM_STORE_SERVICE_RESULTS);

        List<CaseDetails> cases = List.of(CASE_DETAILS, CASE_DETAILS_2);
        SearchResult searchResult = SearchResult.builder().total(cases.size()).cases(cases).build();
        given(coreCaseDataService.getTotalCCDCasesCount(any())).willReturn(cases.size());
        given(coreCaseDataService.getCCDDataBasedOnIndex(any(), eq(0))).willReturn(searchResult);
        given(caseDetailsConverter.toCaseData(CASE_DETAILS))
            .willReturn(CaseData.builder()
                            .applicant1(Party.builder()
                                            .individualFirstName("Harry")
                                            .individualLastName("Porter")
                                            .type(Party.Type.INDIVIDUAL)
                                            .build())
                            .respondent1(Party.builder()
                                             .individualFirstName("James")
                                             .individualLastName("Bond")
                                             .type(Party.Type.INDIVIDUAL)
                                             .build())
                            .claimValue(ClaimValue
                                            .builder()
                                            .statementOfValueInPennies(new BigDecimal("100000"))
                                            .build())
                            .build());

        given(caseDetailsConverter.toCaseData(CASE_DETAILS_2))
            .willReturn(CaseData.builder()
                            .applicant1(Party.builder()
                                            .individualFirstName("Tom")
                                            .individualLastName("Cruise")
                                            .type(Party.Type.INDIVIDUAL)
                                            .build())
                            .respondent1(Party.builder()
                                             .individualFirstName("Jackie")
                                             .individualLastName("Chan")
                                             .type(Party.Type.INDIVIDUAL)
                                             .build())
                            .claimValue(ClaimValue
                                            .builder()
                                            .statementOfValueInPennies(new BigDecimal("100000"))
                                            .build())
                            .build());
    }

    @Test
    void shouldReturnClaimsForClaimantSuccessfully() {
        List<DashboardClaimInfo> claimsForClaimant = dashboardClaimInfoService.getClaimsForClaimant(
            "authorisation",
            "123"
        );
        assertThat(claimsForClaimant.size()).isEqualTo(1);
        assertThat(claimsForClaimant).isEqualTo(CLAIM_STORE_SERVICE_RESULTS);
    }

    @Test
    void shouldReturnClaimsForDefendantSuccessfully() {

        DashboardDefendantResponse claimsForDefendant = dashboardClaimInfoService.getDashboardDefendantResponse(
            "authorisation",
            "123",
            CURRENT_PAGE_NO
        );
        assertThat(claimsForDefendant.getClaims().size()).isEqualTo(3);
        assertThat(claimsForDefendant.getClaims().get(2)).isEqualTo(CLAIM_STORE_SERVICE_RESULTS.get(0));
        assertThat(claimsForDefendant.getClaims().get(0).getDefendantName()).isEqualTo(DEFENDANT_NAME);
        assertThat(claimsForDefendant.getClaims().get(0).getClaimantName()).isEqualTo(CLAIMANT_NAME);
    }

    @Test
    void shouldIncludeResponseDeadlineIfItExists() {
        given(caseDetailsConverter.toCaseData(CASE_DETAILS)).willReturn(CaseData.builder()
                                                                            .applicant1(Party.builder()
                                                                                            .individualFirstName("Harry")
                                                                                            .individualLastName("Porter")
                                                                                            .type(Party.Type.INDIVIDUAL)
                                                                                            .build())
                                                                            .respondent1(Party.builder()
                                                                                             .individualFirstName(
                                                                                                 "James")
                                                                                             .individualLastName("Bond")
                                                                                             .type(Party.Type.INDIVIDUAL)
                                                                                             .build())
                                                                            .claimValue(ClaimValue
                                                                                            .builder()
                                                                                            .statementOfValueInPennies(
                                                                                                new BigDecimal("100000"))
                                                                                            .build())
                                                                            .respondent1ResponseDeadline(DATE_IN_2025)
                                                                            .build());
        DashboardDefendantResponse claimsForDefendant = dashboardClaimInfoService.getDashboardDefendantResponse(
            "authorisation",
            "123",
            CURRENT_PAGE_NO
        );
        assertThat(claimsForDefendant.getClaims().size()).isEqualTo(3);
        assertThat(claimsForDefendant.getClaims().get(0).getResponseDeadline()).isEqualTo(DATE_IN_2025.toLocalDate());
    }

    @Test
    void shouldIncludePaymentDateWhenItExists() {
        given(caseDetailsConverter.toCaseData(CASE_DETAILS))
            .willReturn(CaseData.builder()
                            .applicant1(Party.builder()
                                            .individualFirstName("Harry")
                                            .individualLastName("Porter")
                                            .type(Party.Type.INDIVIDUAL)
                                            .build())
                            .respondent1(Party.builder()
                                             .individualFirstName(
                                                 "James")
                                             .individualLastName("Bond")
                                             .type(Party.Type.INDIVIDUAL)
                                             .build())
                            .claimValue(ClaimValue
                                            .builder()
                                            .statementOfValueInPennies(
                                                new BigDecimal("100000"))
                                            .build())
                            .respondToClaimAdmitPartLRspec(
                                RespondToClaimAdmitPartLRspec
                                    .builder()
                                    .whenWillThisAmountBePaid(DATE_IN_2025.toLocalDate()).build())
                            .build());
        DashboardDefendantResponse claimsForDefendant = dashboardClaimInfoService.getDashboardDefendantResponse(
            "authorisation",
            "123",
            CURRENT_PAGE_NO
        );
        assertThat(claimsForDefendant.getClaims().size()).isEqualTo(3);
        assertThat(claimsForDefendant.getClaims().get(0).getPaymentDate()).isEqualTo(DATE_IN_2025.toLocalDate());
    }

    @Test
    void shouldGetThePartPaymentImmediateValue() {
        given(caseDetailsConverter.toCaseData(CASE_DETAILS))
            .willReturn(CaseData.builder()
                            .applicant1(Party.builder()
                                            .individualFirstName("Harry")
                                            .individualLastName("Porter")
                                            .type(Party.Type.INDIVIDUAL)
                                            .build())
                            .respondent1(Party.builder()
                                             .individualFirstName(
                                                 "James")
                                             .individualLastName("Bond")
                                             .type(Party.Type.INDIVIDUAL)
                                             .build())
                            .claimValue(ClaimValue
                                            .builder()
                                            .statementOfValueInPennies(
                                                new BigDecimal("100000"))
                                            .build())
                            .respondToAdmittedClaimOwingAmountPounds(PART_ADMIT_PAY_IMMEDIATELY_AMOUNT)
                            .respondToClaimAdmitPartLRspec(
                                RespondToClaimAdmitPartLRspec
                                    .builder()
                                    .whenWillThisAmountBePaid(DATE_IN_2025.toLocalDate()).build())
                            .build());
        DashboardDefendantResponse claimsForDefendant = dashboardClaimInfoService.getDashboardDefendantResponse(
            "authorisation",
            "123",
            CURRENT_PAGE_NO
        );
        assertThat(claimsForDefendant.getClaims().size()).isEqualTo(3);
        assertThat(claimsForDefendant.getClaims().get(0).getRespondToAdmittedClaimOwingAmountPounds()).isEqualTo(
            PART_ADMIT_PAY_IMMEDIATELY_AMOUNT);
    }

    @Test
    void shouldReturnCasesInProperOrder() {
        List<CaseDetails> cases = List.of();
        SearchResult searchResult = SearchResult.builder().total(0).cases(cases).build();
        given(claimStoreService.getClaimsForDefendant(any(), any())).willReturn(ORDERED_CASES);
        given(coreCaseDataService.getCCDDataBasedOnIndex(any(), eq(0))).willReturn(searchResult);
        given(coreCaseDataService.getTotalCCDCasesCount(any())).willReturn(searchResult.getTotal());

        DashboardDefendantResponse claimsForDefendant = dashboardClaimInfoService.getDashboardDefendantResponse(
            "authorisation",
            "123",
            CURRENT_PAGE_NO
        );

        assertThat(claimsForDefendant.getClaims().size()).isEqualTo(2);
        assertThat(claimsForDefendant.getClaims().get(0).getCreatedDate()).isEqualTo(DATE_IN_2022);
        assertThat(claimsForDefendant.getClaims().get(1).getCreatedDate()).isEqualTo(DATE_IN_2021);
        assertThat((claimsForDefendant.getTotalPages())).isEqualTo(1);
    }

    @Test
    void shouldReturnEmptyList() {
        List<CaseDetails> cases = List.of();
        SearchResult searchResult = SearchResult.builder().total(0).cases(cases).build();

        given(coreCaseDataService.getCCDDataBasedOnIndex(any(), eq(0))).willReturn(searchResult);
        given(claimStoreService.getClaimsForDefendant(any(), any())).willReturn(List.of());
        given(coreCaseDataService.getTotalCCDCasesCount(any())).willReturn(searchResult.getTotal());

        DashboardDefendantResponse claimsForDefendant = dashboardClaimInfoService.getDashboardDefendantResponse(
            "authorisation",
            "123",
            CURRENT_PAGE_NO
        );

        assertThat(claimsForDefendant.getClaims().size()).isEqualTo(0);
    }

    @Test
    void shouldReturnTotalNoPagesBasedOnTheCases() {
        List<CaseDetails> cases = List.of();
        SearchResult searchResult = SearchResult.builder().total(15).cases(cases).build();
        given(claimStoreService.getClaimsForDefendant(any(), any())).willReturn(ORDERED_CASES);
        given(coreCaseDataService.getTotalCCDCasesCount(any())).willReturn(searchResult.getTotal());

        DashboardDefendantResponse claimsForDefendant = dashboardClaimInfoService.getDashboardDefendantResponse(
            "authorisation",
            "123",
            CURRENT_PAGE_NO
        );

        assertThat(claimsForDefendant.getTotalPages()).isEqualTo(2);

    }

    @Test
    void shouldReturnCurrentPageClaimsForMoreThanTenClaims() {
        // Given
        List<CaseDetails> caseDetailsList = new ArrayList<CaseDetails>();
        for (long i = 15; i >= 1; i--) {
            var caseDetail = CaseDetails.builder()
                .id(i)
                .createdDate(LocalDateTime.of(2021, (int) 3, (int) i, 0, 0))
                .build();
            caseDetailsList.add(caseDetail);
            given(caseDetailsConverter.toCaseData(caseDetail)).willReturn(CaseData.builder()
                                                                              .submittedDate(LocalDateTime.of(
                                                                                  2021,
                                                                                  (int) 3,
                                                                                  (int) i,
                                                                                  0,
                                                                                  0
                                                                              )).build());

        }
        SearchResult searchResult = SearchResult.builder().total(caseDetailsList.size())
            .cases(caseDetailsList.subList(10, caseDetailsList.size())).build();
        given(claimStoreService.getClaimsForDefendant(any(), any())).willReturn(ORDERED_CASES);
        given(coreCaseDataService.getCCDDataBasedOnIndex(any(), eq(10))).willReturn(searchResult);
        given(coreCaseDataService.getTotalCCDCasesCount(any())).willReturn(searchResult.getTotal());
        //when
        DashboardDefendantResponse claimsForDefendant = dashboardClaimInfoService.getDashboardDefendantResponse(
            "authorisation",
            "123",
            2
        );
        //assert
        assertThat(claimsForDefendant.getTotalPages()).isEqualTo(2);
        assertThat(claimsForDefendant.getClaims().size()).isEqualTo(7);
        assertThat(claimsForDefendant.getClaims().get(0).getCreatedDate()).isEqualTo("2021-03-05T00:00");

    }
}
