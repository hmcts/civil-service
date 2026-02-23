package uk.gov.hmcts.reform.civil.service.citizenui;

import org.elasticsearch.common.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.CCJPaymentDetails;
import uk.gov.hmcts.reform.civil.model.ClaimValue;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.RespondToClaimAdmitPartLRspec;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.ClaimantLiPResponse;
import uk.gov.hmcts.reform.civil.model.citizenui.DashboardClaimInfo;
import uk.gov.hmcts.reform.civil.model.citizenui.DashboardClaimStatusFactory;
import uk.gov.hmcts.reform.civil.model.citizenui.DashboardResponse;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentDetails;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentState;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentType;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.CoreCaseEventDataService;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.claimstore.ClaimStoreService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.model.citizenui.ChooseHowToProceed.REQUEST_A_CCJ;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
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
    private FeatureToggleService featureToggleService;

    @Mock
    private DashboardClaimStatusFactory dashboardClaimStatusFactory;

    @Mock
    private CoreCaseEventDataService eventDataService;

    @Inject
    private DashboardClaimInfoService dashboardClaimInfoService;

    private static final BigDecimal PART_ADMIT_PAY_IMMEDIATELY_AMOUNT = BigDecimal.valueOf(500);
    private static final LocalDateTime DATE_IN_2021 = LocalDateTime.of(2021, 2, 20, 0, 0);
    private static final LocalDateTime DATE_IN_2022 = LocalDateTime.of(2022, 2, 20, 0, 0);
    private static final LocalDateTime DATE_IN_2025 = LocalDateTime.of(2025, 2, 20, 0, 0);
    private static final List<DashboardClaimInfo> CLAIM_STORE_SERVICE_RESULTS =
            Collections.singletonList(new DashboardClaimInfo()
                    .setOcmc(true)
                    .setCreatedDate(DATE_IN_2025));
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
            new DashboardClaimInfo()
                .setOcmc(true)
                .setCreatedDate(DATE_IN_2021),
            new DashboardClaimInfo()
                .setOcmc(true)
                .setCreatedDate(DATE_IN_2022)
        );

    @BeforeEach
    void setUp() {

        dashboardClaimInfoService = new DashboardClaimInfoService(
            caseDetailsConverter,
            claimStoreService,
            coreCaseDataService,
            dashboardClaimStatusFactory,
            featureToggleService,
            eventDataService
        );

        when(eventDataService.getEventsForCase(ArgumentMatchers.nullable(String.class))).thenReturn(Collections.emptyList());
        given(claimStoreService.getClaimsForClaimant(any(), any())).willReturn(CLAIM_STORE_SERVICE_RESULTS);
        given(claimStoreService.getClaimsForDefendant(any(), any())).willReturn(CLAIM_STORE_SERVICE_RESULTS);

        List<CaseDetails> cases = List.of(CASE_DETAILS, CASE_DETAILS_2);
        SearchResult searchResult = SearchResult.builder().total(cases.size()).cases(cases).build();
        given(coreCaseDataService.getCCDClaimsForLipDefendant(any(), eq(0))).willReturn(searchResult);
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
                            .claimValue(new ClaimValue()
                                            .setStatementOfValueInPennies(new BigDecimal("100000")))
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
                            .claimValue(new ClaimValue()
                                            .setStatementOfValueInPennies(new BigDecimal("100000")))
                            .build());
    }

    @Test
    void shouldReturnClaimsForClaimantSuccessfullyWhenLipVLipEnabled() {
        given(featureToggleService.isLipVLipEnabled()).willReturn(true);

        List<CaseDetails> cases = List.of(CASE_DETAILS, CASE_DETAILS_2);
        SearchResult searchResult = SearchResult.builder().total(cases.size()).cases(cases).build();
        given(coreCaseDataService.getCCDClaimsForLipClaimant(any(), eq(0))).willReturn(searchResult);
        DashboardResponse claimsForClaimant = dashboardClaimInfoService.getDashboardClaimantResponse(
            "authorisation",
            "123",
            CURRENT_PAGE_NO
        );
        assertThat(claimsForClaimant.getClaims().size()).isEqualTo(3);
        assertThat(claimsForClaimant.getClaims().get(2)).isEqualTo(CLAIM_STORE_SERVICE_RESULTS.get(0));
    }

    @Test
    void shouldReturnOnlyOcmcClaimsForClaimantSuccessfullyWhenLipVLipDisabled() {
        given(featureToggleService.isLipVLipEnabled()).willReturn(false);

        DashboardResponse claimsForClaimant = dashboardClaimInfoService.getDashboardClaimantResponse(
            "authorisation",
            "123",
            CURRENT_PAGE_NO
        );
        assertThat(claimsForClaimant.getClaims().size()).isEqualTo(1);
        assertThat(claimsForClaimant.getClaims().get(0)).isEqualTo(CLAIM_STORE_SERVICE_RESULTS.get(0));
    }

    @Test
    void shouldReturnClaimsForDefendantSuccessfully() {

        DashboardResponse claimsForDefendant = dashboardClaimInfoService.getDashboardDefendantResponse(
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
                                                                            .claimValue(new ClaimValue()
                                                                                            .setStatementOfValueInPennies(
                                                                                                new BigDecimal("100000")))
                                                                            .respondent1ResponseDeadline(DATE_IN_2025)
                                                                            .build());
        DashboardResponse claimsForDefendant = dashboardClaimInfoService.getDashboardDefendantResponse(
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
                            .claimValue(new ClaimValue()
                                            .setStatementOfValueInPennies(
                                                new BigDecimal("100000")))
                            .respondToClaimAdmitPartLRspec(
                                new RespondToClaimAdmitPartLRspec()
                                    .setWhenWillThisAmountBePaid(DATE_IN_2025.toLocalDate()))
                            .build());
        DashboardResponse claimsForDefendant = dashboardClaimInfoService.getDashboardDefendantResponse(
            "authorisation",
            "123",
            CURRENT_PAGE_NO
        );
        assertThat(claimsForDefendant.getClaims().size()).isEqualTo(3);
        assertThat(claimsForDefendant.getClaims().get(0).getPaymentDate()).isEqualTo(DATE_IN_2025.toLocalDate());
    }

    @Test
    void shouldIncludeCcjCreationDateDateWhenItExists() {
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
                            .activeJudgment(new JudgmentDetails().setType(JudgmentType.DEFAULT_JUDGMENT)
                                            .setCreatedTimestamp(DATE_IN_2025))
                            .build());
        DashboardResponse claimsForDefendant = dashboardClaimInfoService.getDashboardDefendantResponse(
            "authorisation",
            "123",
            CURRENT_PAGE_NO
        );
        assertThat(claimsForDefendant.getClaims().size()).isEqualTo(3);
        assertThat(claimsForDefendant.getClaims().get(0).getCcjRequestedDate()).isEqualTo(DATE_IN_2025);
    }

    @Test
    void shouldIncludeCcjCreationDateDateForCcjJourneyWhenActiveJudgmentNotExist() {
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
                            .applicant1ResponseDate(DATE_IN_2025)
                            .caseDataLiP(new CaseDataLiP()
                                             .setApplicant1LiPResponse(new ClaimantLiPResponse()
                                                                        .setApplicant1ChoosesHowToProceed(REQUEST_A_CCJ)))
                            .ccjPaymentDetails(new CCJPaymentDetails()
                                                   .setCcjPaymentPaidSomeOption(YesOrNo.NO))
                            .build());
        DashboardResponse claimsForDefendant = dashboardClaimInfoService.getDashboardDefendantResponse(
            "authorisation",
            "123",
            CURRENT_PAGE_NO
        );
        assertThat(claimsForDefendant.getClaims().size()).isEqualTo(3);
        assertThat(claimsForDefendant.getClaims().get(0).getCcjRequestedDate()).isEqualTo(DATE_IN_2025);
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
                            .claimValue(new ClaimValue()
                                            .setStatementOfValueInPennies(
                                                new BigDecimal("100000")))
                            .respondToAdmittedClaimOwingAmountPounds(PART_ADMIT_PAY_IMMEDIATELY_AMOUNT)
                            .respondToClaimAdmitPartLRspec(
                                new RespondToClaimAdmitPartLRspec()
                                    .setWhenWillThisAmountBePaid(DATE_IN_2025.toLocalDate()))
                            .build());
        DashboardResponse claimsForDefendant = dashboardClaimInfoService.getDashboardDefendantResponse(
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
        given(coreCaseDataService.getCCDClaimsForLipDefendant(any(), eq(0))).willReturn(searchResult);

        DashboardResponse claimsForDefendant = dashboardClaimInfoService.getDashboardDefendantResponse(
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

        given(coreCaseDataService.getCCDClaimsForLipDefendant(any(), eq(0))).willReturn(searchResult);
        given(claimStoreService.getClaimsForDefendant(any(), any())).willReturn(List.of());

        DashboardResponse claimsForDefendant = dashboardClaimInfoService.getDashboardDefendantResponse(
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
        given(coreCaseDataService.getCCDClaimsForLipDefendant(any(), eq(0))).willReturn(searchResult);

        DashboardResponse claimsForDefendant = dashboardClaimInfoService.getDashboardDefendantResponse(
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
            LocalDateTime createdDate = LocalDateTime.of(2021, 3, (int) i, 0, 0);
            var caseDetail = CaseDetails.builder()
                .id(i)
                .createdDate(createdDate)
                .build();
            caseDetailsList.add(caseDetail);
            CaseData caseData = CaseData.builder().submittedDate(createdDate).build();
            given(caseDetailsConverter.toCaseData(caseDetail)).willReturn(caseData);

        }
        SearchResult searchResult = SearchResult.builder().total(caseDetailsList.size())
            .cases(caseDetailsList.subList(10, caseDetailsList.size())).build();
        given(claimStoreService.getClaimsForDefendant(any(), any())).willReturn(ORDERED_CASES);
        given(coreCaseDataService.getCCDClaimsForLipDefendant(any(), eq(10))).willReturn(searchResult);
        //when
        DashboardResponse claimsForDefendant = dashboardClaimInfoService.getDashboardDefendantResponse(
            "authorisation",
            "123",
            2
        );
        //assert
        assertThat(claimsForDefendant.getTotalPages()).isEqualTo(2);
        assertThat(claimsForDefendant.getClaims().size()).isEqualTo(7);
        assertThat(claimsForDefendant.getClaims().get(0).getCreatedDate()).isEqualTo("2021-03-05T00:00");
        assertThat(claimsForDefendant.getClaims().get(1).getCreatedDate()).isEqualTo("2021-03-04T00:00");
        assertThat(claimsForDefendant.getClaims().get(2).getCreatedDate()).isEqualTo("2021-03-03T00:00");
    }

    @Test
    void shouldTranslateSubmitDateToCreateDate() {
        LocalDateTime now = LocalDateTime.now();
        given(caseDetailsConverter.toCaseData(CASE_DETAILS))
            .willReturn(CaseData.builder()
                            .submittedDate(now)
                            .build());
        DashboardResponse claimsForDefendant = dashboardClaimInfoService.getDashboardDefendantResponse(
            "authorisation",
            "123",
            1
        );
        assertThat(claimsForDefendant.getClaims().size()).isEqualTo(3);
        assertThat(claimsForDefendant.getClaims().get(0).getCreatedDate()).isEqualTo(now);
    }

    @Test
    void shouldIncludeDefaultJudgementIssuedDate() {
        given(caseDetailsConverter.toCaseData(CASE_DETAILS))
            .willReturn(CaseData.builder().respondent1ResponseDeadline(LocalDateTime.now().minusDays(1)).activeJudgment(
                    new JudgmentDetails().setIssueDate(LocalDate.now()).setState(JudgmentState.ISSUED)
                        .setType(JudgmentType.DEFAULT_JUDGMENT))
                            .build());
        DashboardResponse claimsForDefendant = dashboardClaimInfoService.getDashboardDefendantResponse(
            "authorisation",
            "123",
            CURRENT_PAGE_NO
        );
        assertThat(claimsForDefendant.getClaims().size()).isEqualTo(3);
        assertThat(claimsForDefendant.getClaims().get(0).getDefaultJudgementIssuedDate()).isEqualTo(LocalDate.now());
    }

    @Test
    void shouldIncludeDefaultJudgementIssuedDate_WhenJOFlagIsOff() {
        given(caseDetailsConverter.toCaseData(CASE_DETAILS))
            .willReturn(CaseData.builder().respondent1ResponseDeadline(LocalDateTime.now().minusDays(1))
                            .defaultJudgmentDocuments(List.of(
                                Element.<CaseDocument>builder()
                                    .value(new CaseDocument().setDocumentType(DocumentType.DEFAULT_JUDGMENT)
                                               .setCreatedDatetime(LocalDateTime.now())).build()))
                            .build());
        DashboardResponse claimsForDefendant = dashboardClaimInfoService.getDashboardDefendantResponse(
            "authorisation",
            "123",
            CURRENT_PAGE_NO
        );
        assertThat(claimsForDefendant.getClaims().size()).isEqualTo(3);
        assertThat(claimsForDefendant.getClaims().get(0).getDefaultJudgementIssuedDate()).isEqualTo(LocalDate.now());
    }
}
