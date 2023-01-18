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
import uk.gov.hmcts.reform.civil.model.citizenui.DashboardClaimInfo;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.claimstore.ClaimStoreService;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(SpringExtension.class)
public class DashboardClaimInfoServiceTest {

    private static final List<DashboardClaimInfo> CLAIM_STORE_SERVICE_RESULTS =
        Arrays.asList(DashboardClaimInfo.builder()
                          .ocmc(true)
                          .build());
    private static final String CLAIMANT_NAME = "Harry Porter";
    private static final String DEFENDANT_NAME = "James Bond";

    @Mock
    private ClaimStoreService claimStoreService;

    @Mock
    private CoreCaseDataService coreCaseDataService;

    @Mock
    private CaseDetailsConverter caseDetailsConverter;

    @InjectMocks
    private DashboardClaimInfoService dashboardClaimInfoService;

    @BeforeEach
    void setUp() {
        given(claimStoreService.getClaimsForClaimant(any(), any())).willReturn(CLAIM_STORE_SERVICE_RESULTS);
        given(claimStoreService.getClaimsForDefendant(any(), any())).willReturn(CLAIM_STORE_SERVICE_RESULTS);
        CaseDetails caseDetails = CaseDetails.builder().id(1L).build();
        List<CaseDetails> cases = List.of(caseDetails);
        SearchResult searchResult = SearchResult.builder().total(1).cases(cases).build();
        given(coreCaseDataService.searchCases(any(), any())).willReturn(searchResult);
        given(caseDetailsConverter.toCaseData(caseDetails))
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
        assertThat(claimsForDefendant.size()).isEqualTo(2);
        assertThat(claimsForDefendant.get(0)).isEqualTo(CLAIM_STORE_SERVICE_RESULTS.get(0));
        assertThat(claimsForDefendant.get(1).getDefendantName()).isEqualTo(DEFENDANT_NAME);
        assertThat(claimsForDefendant.get(1).getClaimantName()).isEqualTo(CLAIMANT_NAME);
    }
}
