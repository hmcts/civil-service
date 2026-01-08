package uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.ClaimAmountBreakup;
import uk.gov.hmcts.reform.civil.model.ClaimAmountBreakupDetails;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
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
        ClaimAmountBreakup claimAmountBreakup1 = new ClaimAmountBreakup();
        ClaimAmountBreakupDetails claimAmountBreakupDetails = new ClaimAmountBreakupDetails();
        claimAmountBreakupDetails.setClaimAmount(new BigDecimal(1000));
        claimAmountBreakupDetails.setClaimReason("Claim 1");
        claimAmountBreakup1.setValue(claimAmountBreakupDetails);
        List<ClaimAmountBreakup> claimAmountBreakup = new ArrayList<>();
        claimAmountBreakup.add(claimAmountBreakup1);
        ClaimAmountBreakup claimAmountBreakup2 = new ClaimAmountBreakup();
        ClaimAmountBreakupDetails claimAmountBreakupDetails1 = new ClaimAmountBreakupDetails();
        claimAmountBreakupDetails1.setClaimAmount(new BigDecimal(2000));
        claimAmountBreakupDetails1.setClaimReason("Claim 2");
        claimAmountBreakup2.setValue(claimAmountBreakupDetails1);

        claimAmountBreakup.add(claimAmountBreakup2);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setClaimAmountBreakup(claimAmountBreakup);

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
