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
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.claimstore.ClaimStoreService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(SpringExtension.class)
public class DashboardClaimInfoServiceTest {

    private static final String CLAIMANT_NAME = "Harry Porter";
    private static final String DEFENDANT_NAME = "James Bond";

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

<<<<<<< HEAD
    private static final OffsetDateTime DATE_IN_2021 = LocalDateTime.of(2021, 2, 20, 0, 0).atOffset(ZoneOffset.UTC);
    private static final OffsetDateTime DATE_IN_2022 = LocalDateTime.of(2022, 2, 20, 0, 0).atOffset(ZoneOffset.UTC);
    private static final OffsetDateTime DATE_IN_2025 = LocalDateTime.of(2025, 2, 20, 0, 0).atOffset(ZoneOffset.UTC);
=======
    private static final LocalDateTime DATE_IN_2021 = LocalDateTime.of(2021, 2, 20, 0, 0);
    private static final LocalDateTime DATE_IN_2022 = LocalDateTime.of(2022, 2, 20, 0, 0);
    private static final LocalDateTime DATE_IN_2025 = LocalDateTime.of(2025, 2, 20, 0, 0);
>>>>>>> 1ae087e26dad3ed71927db9164a64cb16af0f6af
    private static final List<DashboardClaimInfo> CLAIM_STORE_SERVICE_RESULTS =
        Arrays.asList(DashboardClaimInfo.builder()
                          .ocmc(true)
                          .createdDate(DATE_IN_2025)
                          .build());
    private static final CaseDetails CASE_DETAILS = CaseDetails.builder()
        .id(1L)
<<<<<<< HEAD
        .createdDate(DATE_IN_2021.toLocalDateTime())
        .build();
    private static final CaseDetails CASE_DETAILS_2 = CaseDetails.builder()
        .id(2L)
        .createdDate(DATE_IN_2022.toLocalDateTime())
=======
        .createdDate(DATE_IN_2021)
        .build();
    private static final CaseDetails CASE_DETAILS_2 = CaseDetails.builder()
        .id(2L)
        .createdDate(DATE_IN_2022)
>>>>>>> 1ae087e26dad3ed71927db9164a64cb16af0f6af
        .build();

    private static final List<DashboardClaimInfo> ORDERED_CASES =
        Arrays.asList(DashboardClaimInfo.builder()
                          .ocmc(true)
                          .createdDate(DATE_IN_2021)
                          .build(),
                      DashboardClaimInfo.builder()
                          .ocmc(true)
                          .createdDate(DATE_IN_2022)
                          .build()
        );

    @BeforeEach
    void setUp() {
        given(claimStoreService.getClaimsForClaimant(any(), any())).willReturn(CLAIM_STORE_SERVICE_RESULTS);
        given(claimStoreService.getClaimsForDefendant(any(), any())).willReturn(CLAIM_STORE_SERVICE_RESULTS);

        List<CaseDetails> cases = List.of(CASE_DETAILS, CASE_DETAILS_2);
        SearchResult searchResult = SearchResult.builder().total(1).cases(cases).build();
        given(coreCaseDataService.searchCases(any(), any())).willReturn(searchResult);
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
        List<DashboardClaimInfo> claimsForDefendant = dashboardClaimInfoService.getClaimsForDefendant(
            "authorisation",
            "123"
        );
        assertThat(claimsForDefendant.size()).isEqualTo(3);
        assertThat(claimsForDefendant.get(0)).isEqualTo(CLAIM_STORE_SERVICE_RESULTS.get(0));
        assertThat(claimsForDefendant.get(2).getDefendantName()).isEqualTo(DEFENDANT_NAME);
        assertThat(claimsForDefendant.get(2).getClaimantName()).isEqualTo(CLAIMANT_NAME);
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
<<<<<<< HEAD
                                                                            .respondent1ResponseDeadline(DATE_IN_2025.toLocalDateTime())
=======
                                                                            .respondent1ResponseDeadline(DATE_IN_2025)
>>>>>>> 1ae087e26dad3ed71927db9164a64cb16af0f6af
                                                                            .build());
        List<DashboardClaimInfo> claimsForDefendant = dashboardClaimInfoService.getClaimsForDefendant(
            "authorisation",
            "123"
        );
        assertThat(claimsForDefendant.size()).isEqualTo(3);
        assertThat(claimsForDefendant.get(2).getResponseDeadline()).isEqualTo(DATE_IN_2025.toLocalDate());
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
        List<DashboardClaimInfo> claimsForDefendant = dashboardClaimInfoService.getClaimsForDefendant(
            "authorisation",
            "123"
        );
        assertThat(claimsForDefendant.size()).isEqualTo(3);
        assertThat(claimsForDefendant.get(2).getPaymentDate()).isEqualTo(DATE_IN_2025.toLocalDate());
    }

    @Test
    void shouldReturnCasesInProperOrder() {
        List<CaseDetails> cases = List.of();
        SearchResult searchResult = SearchResult.builder().total(0).cases(cases).build();
        given(claimStoreService.getClaimsForDefendant(any(), any())).willReturn(ORDERED_CASES);
        given(coreCaseDataService.searchCases(any(), any())).willReturn(searchResult);

        List<DashboardClaimInfo> claimsForDefendant = dashboardClaimInfoService.getClaimsForDefendant(
            "authorisation",
            "123"
        );

        assertThat(claimsForDefendant.size()).isEqualTo(2);
        assertThat(claimsForDefendant.get(0).getCreatedDate()).isEqualTo(DATE_IN_2022);
        assertThat(claimsForDefendant.get(1).getCreatedDate()).isEqualTo(DATE_IN_2021);
    }

    @Test
    void shouldReturnEmptyList() {
        List<CaseDetails> cases = List.of();
        SearchResult searchResult = SearchResult.builder().total(0).cases(cases).build();

        given(coreCaseDataService.searchCases(any(), any())).willReturn(searchResult);
        given(claimStoreService.getClaimsForDefendant(any(), any())).willReturn(List.of());

        List<DashboardClaimInfo> claimsForDefendant = dashboardClaimInfoService.getClaimsForDefendant(
            "authorisation",
            "123"
        );

        assertThat(claimsForDefendant.size()).isEqualTo(0);
    }
}
