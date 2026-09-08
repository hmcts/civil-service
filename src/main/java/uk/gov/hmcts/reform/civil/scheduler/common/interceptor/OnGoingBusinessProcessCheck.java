package uk.gov.hmcts.reform.civil.scheduler.common.interceptor;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.helpers.CaseTypeIdentifier;
import uk.gov.hmcts.reform.civil.model.BaseCaseData;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

@Component
@AllArgsConstructor
public class OnGoingBusinessProcessCheck<T> implements SchedulerInterceptor<T> {

    private final CoreCaseDataService coreCaseDataService;
    private final CaseDetailsConverter caseDetailsConverter;

    @Override
    public void accept(InterceptorContext<T> context, InterceptorChain<T> chain) {
        if (!(context.getItem() instanceof CaseDetails caseDetails)) {
            chain.next(context);
            return;
        }

        CaseDataHandler<?> handler = getHandler(caseDetails);
        process(context, chain, caseDetails, handler);
    }

    @Override
    public int getOrder() {
        return 1;
    }

    private <B extends BaseCaseData> void process(InterceptorContext<T> context,
                                                  InterceptorChain<T> chain,
                                                  CaseDetails caseDetails,
                                                  CaseDataHandler<B> handler) {
        B caseData = context.getAttribute(handler.getKey())
            .orElseGet(() -> {
                CaseDetails fullCaseDetails = coreCaseDataService.getCase(caseDetails.getId());
                B data = handler.toCaseData(fullCaseDetails);
                context.setAttribute(handler.getKey(), data);
                return data;
            });

        if (handler.hasNoOngoingBusinessProcess(caseData)) {
            chain.next(context);
            return;
        }

        throw new TaskAbortedException("Ongoing business process");
    }

    private CaseDataHandler<?> getHandler(CaseDetails caseDetails) {
        if (CaseTypeIdentifier.isGeneralApplication(caseDetails)) {
            return new GACaseDataHandler();
        }
        return new CivilCaseDataHandler();
    }

    private interface CaseDataHandler<B extends BaseCaseData> {

        AttributeKey<B> getKey();

        B toCaseData(CaseDetails caseDetails);

        boolean hasNoOngoingBusinessProcess(B caseData);
    }

    private class CivilCaseDataHandler implements CaseDataHandler<CaseData> {

        @Override
        public AttributeKey<CaseData> getKey() {
            return CaseInterceptorAttributes.CIVIL_CASE_DATA;
        }

        @Override
        public CaseData toCaseData(CaseDetails caseDetails) {
            return caseDetailsConverter.toCaseData(caseDetails);
        }

        @Override
        public boolean hasNoOngoingBusinessProcess(CaseData caseData) {
            return caseData.hasNoOngoingBusinessProcess();
        }
    }

    private class GACaseDataHandler implements CaseDataHandler<GeneralApplicationCaseData> {

        @Override
        public AttributeKey<GeneralApplicationCaseData> getKey() {
            return CaseInterceptorAttributes.GA_CASE_DATA;
        }

        @Override
        public GeneralApplicationCaseData toCaseData(CaseDetails caseDetails) {
            return caseDetailsConverter.toGeneralApplicationCaseData(caseDetails);
        }

        @Override
        public boolean hasNoOngoingBusinessProcess(GeneralApplicationCaseData caseData) {
            return caseData.hasNoOngoingBusinessProcess();
        }
    }
}
