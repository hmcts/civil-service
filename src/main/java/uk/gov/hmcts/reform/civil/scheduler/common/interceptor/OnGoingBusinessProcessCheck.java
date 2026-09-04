package uk.gov.hmcts.reform.civil.scheduler.common.interceptor;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.scheduler.casedismissed.CaseDismissedScheduler;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

@Component
@AllArgsConstructor
public class OnGoingBusinessProcessCheck<T> implements SchedulerInterceptor<T> {

    public static final AttributeKey<CaseData> CASE_DATA_KEY = AttributeKey.of("CaseData", CaseData.class);

    private final CoreCaseDataService coreCaseDataService;
    private final CaseDetailsConverter caseDetailsConverter;

    @Override
    public void accept(InterceptorContext<T> context, InterceptorChain<T> chain) {
        if (!(context.getItem() instanceof CaseDetails caseDetails)) {
            chain.next(context);
            return;
        }

        Long caseId = caseDetails.getId();
        CaseData caseData = context.getAttribute(CASE_DATA_KEY)
            .orElseGet(() -> {
                CaseDetails fullCaseDetails = coreCaseDataService.getCase(caseId);
                CaseData data = caseDetailsConverter.toCaseData(fullCaseDetails);
                context.setAttribute(CASE_DATA_KEY, data);
                return data;
            });

        if (caseData.hasNoOngoingBusinessProcess()) {
            chain.next(context);
            return;
        }

        throw new TaskAbortedException("Ongoing business process");
    }

    @Override
    public boolean supports(String schedulerName) {
        return schedulerName.equals(CaseDismissedScheduler.SCHEDULER_NAME);
    }

    @Override
    public int getOrder() {
        return 1;
    }
}
