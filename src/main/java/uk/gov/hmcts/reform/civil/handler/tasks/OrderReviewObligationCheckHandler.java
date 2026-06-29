package uk.gov.hmcts.reform.civil.handler.tasks;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.client.task.ExternalTask;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.event.OrderReviewObligationCheckEvent;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.ExternalTaskData;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.search.OrderReviewObligationSearchService;

import java.time.LocalDate;
import java.util.Set;
import uk.gov.hmcts.reform.civil.config.properties.EventProperties;
import uk.gov.hmcts.reform.civil.service.ExternalTaskCompletionService;

@Slf4j
@Component
public class OrderReviewObligationCheckHandler extends BaseExternalTaskHandler {

    private final OrderReviewObligationSearchService caseSearchService;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final CoreCaseDataService coreCaseDataService;
    private final CaseDetailsConverter caseDetailsConverter;

    public OrderReviewObligationCheckHandler(
        ExternalTaskCompletionService externalTaskCompletionService,
        EventProperties eventProperties,
        OrderReviewObligationSearchService caseSearchService,
        ApplicationEventPublisher applicationEventPublisher,
        CoreCaseDataService coreCaseDataService,
        CaseDetailsConverter caseDetailsConverter
    ) {
        super(externalTaskCompletionService, eventProperties);
        this.caseSearchService = caseSearchService;
        this.applicationEventPublisher = applicationEventPublisher;
        this.coreCaseDataService = coreCaseDataService;
        this.caseDetailsConverter = caseDetailsConverter;
    }

    @Override
    public ExternalTaskData handleTask(ExternalTask externalTask) {
        Set<CaseDetails> cases = caseSearchService.getCases();
        log.info("Job '{}' found {} case(s)", externalTask.getTopicName(), cases.size());

        cases.forEach(caseDetails -> {
            try {
                CaseData caseData = caseDetailsConverter.toCaseData(coreCaseDataService.getCase(caseDetails.getId()));
                LocalDate currentDate = LocalDate.now();

                caseData.getStoredObligationData().stream()
                    .map(Element::getValue)
                    .filter(data -> !data.getObligationDate().isAfter(currentDate) && YesOrNo.NO.equals(data.getObligationWATaskRaised()))
                    .forEach(data -> applicationEventPublisher.publishEvent(new OrderReviewObligationCheckEvent(
                        caseDetails.getId())));

            } catch (Exception e) {
                log.error("Updating case with id: '{}' failed", caseDetails.getId(), e);
            }
        });
        return new ExternalTaskData();
    }

}
