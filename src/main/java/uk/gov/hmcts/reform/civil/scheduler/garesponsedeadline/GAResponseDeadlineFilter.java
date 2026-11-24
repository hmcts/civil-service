package uk.gov.hmcts.reform.civil.scheduler.garesponsedeadline;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.service.Time;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class GAResponseDeadlineFilter {

    private final Time time;
    private final CaseDetailsConverter caseDetailsConverter;

    public boolean isDeadlineExpired(CaseDetails caseDetails) {
        try {
            LocalDateTime deadline = caseDetailsConverter
                .toGeneralApplicationCaseData(caseDetails)
                .getGeneralAppNotificationDeadlineDate();
            return deadline != null && time.now().isAfter(deadline);
        } catch (Exception e) {
            log.error("GAResponseDeadlineFilter failed for case {}: {}", caseDetails.getId(), e.getMessage());
            return false;
        }
    }
}
