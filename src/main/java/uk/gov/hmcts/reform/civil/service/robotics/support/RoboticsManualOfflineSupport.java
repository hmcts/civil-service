package uk.gov.hmcts.reform.civil.service.robotics.support;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.enums.ReasonForProceedingOnPaper;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.ClaimProceedsInCaseman;
import uk.gov.hmcts.reform.civil.model.ClaimProceedsInCasemanLR;

import java.time.LocalDate;

import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.StringUtils.left;
import static java.time.format.DateTimeFormatter.ISO_DATE;

@Component
@RequiredArgsConstructor
public class RoboticsManualOfflineSupport {

    private final RoboticsEventTextFormatter textFormatter;

    public String prepareTakenOfflineEventDetails(CaseData caseData) {
        requireNonNull(caseData, "caseData must not be null");
        CaseCategory category = caseData.getCaseAccessCategory();
        if (CaseCategory.UNSPEC_CLAIM.equals(category)) {
            ClaimProceedsInCaseman unspecDetails = caseData.getClaimProceedsInCaseman();
            if (!hasRequiredDetails(unspecDetails == null ? null : unspecDetails.getReason(),
                unspecDetails == null ? null : unspecDetails.getDate())) {
                return textFormatter.caseTakenOfflineByStaff();
            }
            return buildDetails(resolveReason(unspecDetails.getReason(), unspecDetails.getOther()),
                                unspecDetails.getDate());
        }

        ClaimProceedsInCasemanLR specDetails = caseData.getClaimProceedsInCasemanLR();
        if (!hasRequiredDetails(specDetails == null ? null : specDetails.getReason(),
            specDetails == null ? null : specDetails.getDate())) {
            return textFormatter.caseTakenOfflineByStaff();
        }
        return buildDetails(resolveReason(specDetails.getReason(), specDetails.getOther()), specDetails.getDate());
    }

    private String buildDetails(String reason, LocalDate date) {
        String formatted = textFormatter.formatRpa(
            "Manually moved offline for reason %s on date %s.",
            reason,
            requireNonNull(date, "offline date must not be null").format(ISO_DATE)
        );
        return left(formatted, 250);
    }

    private String resolveReason(ReasonForProceedingOnPaper reason, String other) {
        if (reason == ReasonForProceedingOnPaper.OTHER) {
            return other;
        }
        return requireNonNull(reason, "offline reason must not be null").name();
    }

    private boolean hasRequiredDetails(ReasonForProceedingOnPaper reason, LocalDate date) {
        return reason != null && date != null;
    }
}
