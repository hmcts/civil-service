package uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim.CalculateTotalClaimAmountTask;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.ClaimAmountBreakup;
import uk.gov.hmcts.reform.civil.model.ClaimAmountBreakupDetails;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CalculateTotalClaimAmountTaskTest {

    @InjectMocks
    private CalculateTotalClaimAmountTask calculateTotalClaimAmountTask;

    @Mock
    private FeatureToggleService featureToggleService;

    @BeforeEach
    public void setUp() {
        calculateTotalClaimAmountTask = new CalculateTotalClaimAmountTask(featureToggleService, new ObjectMapper());
    }

    @Test
    void shouldCalculateTotalClaimAmountSuccessfully() {
        List<ClaimAmountBreakup> claimAmountBreakup = new ArrayList<>();
        claimAmountBreakup.add(ClaimAmountBreakup.builder()
                                   .value(ClaimAmountBreakupDetails.builder()
                                              .claimAmount(new BigDecimal(1000)).claimReason("Claim 1").build()).build());

        claimAmountBreakup.add(ClaimAmountBreakup.builder()
                                   .value(ClaimAmountBreakupDetails.builder()
                                              .claimAmount(new BigDecimal(2000)).claimReason("Claim 2").build()).build());

        CaseData caseData = CaseData.builder()
            .claimAmountBreakup(claimAmountBreakup)
            .build();

        when(featureToggleService.isMultiOrIntermediateTrackEnabled(caseData)).thenReturn(false);

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse)
            calculateTotalClaimAmountTask.calculateTotalClaimAmount(caseData);

        String actualBreakupSummary = (String) response.getData().get("claimAmountBreakupSummaryObject");

        String expectedBreakupSummary =
            " | Description | Amount | \n" +
            " |---|---| \n" +
            " | Claim 1 | £ 10.00 |\n" +
            " | Claim 2 | £ 20.00 |\n" +
            " | **Total** | £ 30.00 | ";

        System.out.println("expected \n" + expectedBreakupSummary);
        System.out.println("actual \n" + actualBreakupSummary);

        assertThat(response.getData()).isNotNull();
        assertThat(actualBreakupSummary).isEqualTo(expectedBreakupSummary);
    }
}
