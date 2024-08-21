package uk.gov.hmcts.reform.civil.handler.callback.user.task.createClaimSpecCallbackHanderTask;

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
        BigDecimal totalClaimAmount = calculateTotalClaimAmount(caseData.getClaimAmountBreakup());

        List<String> errors = validateClaimAmount(caseData, totalClaimAmount);
        if (!errors.isEmpty()) {
            return buildErrorResponse(errors);
        }

        CaseData.CaseDataBuilder caseDataBuilder = updateCaseData(caseData, totalClaimAmount);

        String claimAmountSummary = generateClaimAmountSummary(caseData.getClaimAmountBreakup(), totalClaimAmount);
        caseDataBuilder.claimAmountBreakupSummaryObject(claimAmountSummary);

        return buildSuccessResponse(caseDataBuilder);
    }

    private BigDecimal calculateTotalClaimAmount(List<ClaimAmountBreakup> claimAmountBreakups) {
        return claimAmountBreakups.stream()
            .map(breakup -> breakup.getValue().getClaimAmount())
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private List<String> validateClaimAmount(CaseData caseData, BigDecimal totalClaimAmount) {
        List<String> errors = new ArrayList<>();
        if (!featureToggleService.isMultiOrIntermediateTrackEnabled(caseData) &&
            MonetaryConversions.penniesToPounds(totalClaimAmount).doubleValue() > 25000) {
            errors.add("Total Claim Amount cannot exceed £ 25,000");
        }
        return errors;
    }

    private CaseData.CaseDataBuilder updateCaseData(CaseData caseData, BigDecimal totalClaimAmount) {
        return caseData.toBuilder()
            .totalClaimAmount(MonetaryConversions.penniesToPounds(totalClaimAmount));
    }

    private String generateClaimAmountSummary(List<ClaimAmountBreakup> claimAmountBreakups, BigDecimal totalClaimAmount) {
        StringBuilder summaryBuilder = new StringBuilder(" | Description | Amount | \n |---|---| \n ");
        for (ClaimAmountBreakup breakup : claimAmountBreakups) {
            summaryBuilder.append(" | ")
                .append(breakup.getValue().getClaimReason())
                .append(" | £ ")
                .append(MonetaryConversions.penniesToPounds(breakup.getValue().getClaimAmount()))
                .append(" |\n ");
        }
        summaryBuilder.append(" | **Total** | £ ")
            .append(MonetaryConversions.penniesToPounds(totalClaimAmount))
            .append(" | ");

        return summaryBuilder.toString();
    }

    private CallbackResponse buildErrorResponse(List<String> errors) {
        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .build();
    }

    private CallbackResponse buildSuccessResponse(CaseData.CaseDataBuilder caseDataBuilder) {
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }
}
