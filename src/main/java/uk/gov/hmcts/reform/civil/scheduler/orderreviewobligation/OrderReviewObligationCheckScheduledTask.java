package uk.gov.hmcts.reform.civil.scheduler.orderreviewobligation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.event.OrderReviewObligationCheckEvent;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.scheduler.common.DefaultBackPressureConfiguration;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTask;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTaskBackPressureConfiguration;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import java.time.LocalDate;
import java.time.ZoneId;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderReviewObligationCheckScheduledTask implements ScheduledTask<CaseDetails, Long> {

    private static final ZoneId LOCAL_ZONE = ZoneId.of("Europe/London");

    private final ApplicationEventPublisher applicationEventPublisher;
    private final CoreCaseDataService coreCaseDataService;
    private final CaseDetailsConverter caseDetailsConverter;
    private final DefaultBackPressureConfiguration defaultBackPressureConfiguration;

    @Override
    public Long getItemId(CaseDetails caseDetails) {
        return caseDetails.getId();
    }

    @Override
    public void accept(CaseDetails caseDetails) {
        Long caseId = caseDetails.getId();
        log.info("OrderReviewObligationCheckScheduledTask::accept case {}", caseId);
        CaseData caseData = caseDetailsConverter.toCaseData(coreCaseDataService.getCase(caseId));
        LocalDate currentDate = LocalDate.now(LOCAL_ZONE);

        caseData.getStoredObligationData().stream()
            .map(Element::getValue)
            .filter(data -> !data.getObligationDate().isAfter(currentDate)
                && YesOrNo.NO.equals(data.getObligationWATaskRaised()))
            .forEach(data -> applicationEventPublisher.publishEvent(new OrderReviewObligationCheckEvent(caseId)));
    }

    @Override
    public ScheduledTaskBackPressureConfiguration backPressureConfiguration() {
        return defaultBackPressureConfiguration.getDefaultBackPressure();
    }
}
