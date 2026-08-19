package uk.gov.hmcts.reform.civil.service.sdo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.bankholidays.WorkingDayIndicator;
import uk.gov.hmcts.reform.civil.enums.DecisionOnRequestReconsiderationOptions;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.Time;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static java.util.Objects.isNull;

@Slf4j
@Service
@RequiredArgsConstructor
public class SdoReconsiderationDeadlineService {

    private final WorkingDayIndicator workingDayIndicator;
    private final SdoFeatureToggleService featureToggleService;
    private final Time time;

    public LocalDateTime calculateReconsiderationDeadline() {
        return calculateReconsiderationDeadline(time.now());
    }

    public LocalDateTime calculateReconsiderationDeadline(LocalDateTime fromDateTime) {
        LocalDate date = fromDateTime.toLocalDate();
        try {
            for (int i = 0; i < 7; i++) {
                if (workingDayIndicator.isPublicHoliday(date)) {
                    date = date.plusDays(2);
                } else {
                    date = date.plusDays(1);
                }
            }
        } catch (Exception e) {
            log.error("Error when retrieving public days");
            date = time.now().toLocalDate().plusDays(7);
        }

        return date.atTime(16, 0, 0);
    }

    public boolean isEligibleForReconsideration(CaseData caseData) {
        return (featureToggleService.isCaseProgressionEnabledAndLocationWhiteListed(
            caseData.getCaseManagementLocation().getBaseLocation())
            || featureToggleService.isWelshEnabledForMainCase())
            && caseData.isSmallClaim()
            && caseData.getTotalClaimAmount().compareTo(BigDecimal.valueOf(10000)) <= 0
            && (isNull(caseData.getDecisionOnRequestReconsiderationOptions())
            || !DecisionOnRequestReconsiderationOptions.CREATE_SDO.equals(
                caseData.getDecisionOnRequestReconsiderationOptions()));
    }
}
