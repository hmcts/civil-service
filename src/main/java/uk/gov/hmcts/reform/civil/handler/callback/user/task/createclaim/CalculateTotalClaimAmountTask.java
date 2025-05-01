package uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.ClaimAmountBreakup;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.utils.MonetaryConversions;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class CalculateTotalClaimAmountTask {

    private final FeatureToggleService featureToggleService;
    private final ObjectMapper objectMapper;

    @Autowired
    public CalculateTotalClaimAmountTask(FeatureToggleService featureToggleService, ObjectMapper objectMapper) {
        this.featureToggleService = featureToggleService;
        this.objectMapper = objectMapper;
    }

    public CallbackResponse calculateTotalClaimAmount(CaseData caseData) {
        BigDecimal totalClaimAmount = new BigDecimal(0);

        List<ClaimAmountBreakup> claimAmountBreakups = caseData.getClaimAmountBreakup();

        String totalAmount = " | Description | Amount | \n |---|---| \n";
        StringBuilder stringBuilder = new StringBuilder();
        for (ClaimAmountBreakup claimAmountBreakup : claimAmountBreakups) {
            totalClaimAmount =
                totalClaimAmount.add(claimAmountBreakup.getValue().getClaimAmount());

            stringBuilder.append(" | ")
                .append(claimAmountBreakup.getValue().getClaimReason())
                .append(" | ")
                .append("£ ")
                .append(MonetaryConversions.penniesToPounds(claimAmountBreakup.getValue().getClaimAmount()))
                .append(" |\n");
        }
        totalAmount = totalAmount.concat(stringBuilder.toString());

        List<String> errors = new ArrayList<>();
        if (!featureToggleService.isMultiOrIntermediateTrackEnabled(caseData)
            && MonetaryConversions.penniesToPounds(totalClaimAmount).doubleValue() > 25000) {
            errors.add("Total Claim Amount cannot exceed £ 25,000");
            return AboutToStartOrSubmitCallbackResponse.builder()
                .errors(errors)
                .build();
        }

        CaseData.CaseDataBuilder caseDataBuilder = caseData.toBuilder();

        caseDataBuilder.totalClaimAmount(
            MonetaryConversions.penniesToPounds(totalClaimAmount));

        totalAmount = totalAmount.concat(" | **Total** | £ " + MonetaryConversions
            .penniesToPounds(totalClaimAmount) + " | ");

        caseDataBuilder.claimAmountBreakupSummaryObject(totalAmount);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }
}
