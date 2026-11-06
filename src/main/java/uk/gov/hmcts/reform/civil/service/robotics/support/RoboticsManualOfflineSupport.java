package uk.gov.hmcts.reform.civil.service.robotics.support;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class RoboticsManualOfflineSupport {

    private final RoboticsEventTextFormatter textFormatter;
    private static final String OFFLINE_REASON_REQUIRED = "offline reason must not be null";
    private static final String OFFLINE_DATE_REQUIRED = "offline date must not be null";

    public String prepareTakenOfflineEventDetails(CaseData caseData) {
        requireNonNull(caseData, "caseData must not be null");
        CaseCategory category = caseData.getCaseAccessCategory();
        if (CaseCategory.UNSPEC_CLAIM.equals(category)) {
            ClaimProceedsInCaseman unspecDetails = caseData.getClaimProceedsInCaseman();
            if (unspecDetails == null && caseData.getTakenOfflineByStaffDate() != null) {
                log.warn("Missing claimProceedsInCaseman for caseId {}; using taken offline by staff fallback with date {}",
                         caseData.getCcdCaseReference(), caseData.getTakenOfflineByStaffDate());
                return textFormatter.caseTakenOfflineByStaff();
            }
            requireNonNull(unspecDetails, "claimProceedsInCaseman must not be null");
            return buildDetails(resolveReason(unspecDetails.getReason(), unspecDetails.getOther()),
                                unspecDetails.getDate());
        }

        ClaimProceedsInCasemanLR specDetails = requireNonNull(caseData.getClaimProceedsInCasemanLR(),
            "claimProceedsInCasemanLR must not be null");
        return buildDetails(resolveReason(specDetails.getReason(), specDetails.getOther()), specDetails.getDate());
    }

    private String buildDetails(String reason, LocalDate date) {
        String formatted = textFormatter.formatRpa(
            "Manually moved offline for reason %s on date %s.",
            reason,
            date.format(ISO_DATE)
        );
        return left(formatted, 250);
    }

    private String resolveReason(ReasonForProceedingOnPaper reason, String other) {
        if (reason == ReasonForProceedingOnPaper.OTHER) {
            return other;
        }
        return reason.name();
    }
}
