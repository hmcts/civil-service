package uk.gov.hmcts.reform.civil.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.utils.DateUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.nonNull;

@Service
@RequiredArgsConstructor
public class DashboardNotificationsParamsMapper {

    private final FeesService feesService;

    public Map<String, Object> mapCaseDataToParams(CaseData caseData) {
        // TODO Check for notification variables
        LocalDateTime currentDate = LocalDateTime.now();

        Map<String, Object> params = new HashMap<>();
        params.put("ccdCaseReference", caseData.getCcdCaseReference());
        params.put("defaultRespondTime", "4pm");
        params.put("defendantName", caseData.getRespondent1().getPartyName());

        // TODO: find the correct data
        params.put("date", caseData.getClaimDismissedDeadline());

        if (nonNull(caseData.getClaimDismissedDeadline())) {
            long daysDifference = ChronoUnit.DAYS.between(caseData.getClaimDismissedDeadline(), currentDate);
            params.put("daysLeftToRespond", daysDifference);
        }
        if (nonNull(caseData.getRespondent1ResponseDeadline())) {
            params.put("responseDeadline", DateUtils.formatDate(caseData.getRespondent1ResponseDeadline()));
        }

        Fee fee = feesService
            .getFeeDataByTotalClaimAmount(caseData.getTotalClaimAmount());
        params.put("claimFee", "£" + fee.toPounds().stripTrailingZeros().toPlainString());
        params.put("remissionAmount", caseData.getClaimIssueRemissionAmount());
        params.put("outStandingAmount", caseData.getOutstandingFeeInPounds());
        params.put("paymentDueDate", LocalDate.now());
        return params;
    }
}
