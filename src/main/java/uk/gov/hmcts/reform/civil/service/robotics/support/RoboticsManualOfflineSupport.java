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
            ClaimProceedsInCaseman unspecDetails = requireNonNull(caseData.getClaimProceedsInCaseman(),
                "claimProceedsInCaseman must not be null");
            requireNonNull(unspecDetails.getReason(), "offline reason must not be null");
            requireNonNull(unspecDetails.getDate(), "offline date must not be null");
            return buildDetails(resolveReason(unspecDetails.getReason(), unspecDetails.getOther()),
                                unspecDetails.getDate());
        }

        ClaimProceedsInCasemanLR specDetails = requireNonNull(caseData.getClaimProceedsInCasemanLR(),
            "claimProceedsInCasemanLR must not be null");
        requireNonNull(specDetails.getReason(), "offline reason must not be null");
        requireNonNull(specDetails.getDate(), "offline date must not be null");
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
            return requireNonNull(other, "offline reason must not be null");
        }
        return requireNonNull(reason, "offline reason must not be null").name();
    }
}
