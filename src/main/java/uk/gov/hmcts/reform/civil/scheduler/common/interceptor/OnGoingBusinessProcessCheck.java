package uk.gov.hmcts.reform.civil.scheduler.common.interceptor;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.helpers.CaseTypeIdentifier;
import uk.gov.hmcts.reform.civil.model.BaseCaseData;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

@Component
public class OnGoingBusinessProcessCheck implements DefaultSchedulerInterceptor<CaseDetails> {

    private final CoreCaseDataService coreCaseDataService;
    private final CaseDetailsConverter caseDetailsConverter;
    private final CivilCaseDataHandler civilCaseDataHandler;
    private final GACaseDataHandler gaCaseDataHandler;

    public OnGoingBusinessProcessCheck(CoreCaseDataService coreCaseDataService,
                                       CaseDetailsConverter caseDetailsConverter) {
        this.coreCaseDataService = coreCaseDataService;
        this.caseDetailsConverter = caseDetailsConverter;
        this.civilCaseDataHandler = new CivilCaseDataHandler();
        this.gaCaseDataHandler = new GACaseDataHandler();
    }

    @Override
    public void accept(InterceptorContext<CaseDetails> context, InterceptorChain<CaseDetails> chain) {
        CaseDetails caseDetails = context.getItem();
        CaseDataHandler<?> handler = getHandler(caseDetails);
        process(context, chain, caseDetails, handler);
    }

    @Override
    public int getOrder() {
        return 1;
    }

    private <B extends BaseCaseData> void process(InterceptorContext<CaseDetails> context,
                                                  InterceptorChain<CaseDetails> chain,
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

        recordBusinessProcessProperties(context, handler.getBusinessProcess(caseData));

        throw new TaskAbortedException(AbortCategory.ONGOING_BUSINESS_PROCESS, "Ongoing business process");
    }

    private void recordBusinessProcessProperties(InterceptorContext<CaseDetails> context,
                                                 BusinessProcess businessProcess) {
        if (businessProcess == null) {
            return;
        }

        context.recordProperty("businessProcessStatus",
            businessProcess.getStatus() != null ? businessProcess.getStatus().name() : "UNKNOWN");
        context.recordProperty("camundaEvent", businessProcess.getCamundaEvent());
        context.recordProperty("activityId", businessProcess.getActivityId());
        context.recordProperty("processInstanceId", businessProcess.getProcessInstanceId());
        if (businessProcess.getReadyOn() != null) {
            context.recordProperty("businessProcessReadyOn", businessProcess.getReadyOn().toString());
        }
    }

    private CaseDataHandler<?> getHandler(CaseDetails caseDetails) {
        if (CaseTypeIdentifier.isGeneralApplication(caseDetails)) {
            return gaCaseDataHandler;
        }
        return civilCaseDataHandler;
    }

    private interface CaseDataHandler<B extends BaseCaseData> {

        AttributeKey<B> getKey();

        B toCaseData(CaseDetails caseDetails);

        boolean hasNoOngoingBusinessProcess(B caseData);

        BusinessProcess getBusinessProcess(B caseData);
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

        @Override
        public BusinessProcess getBusinessProcess(CaseData caseData) {
            return caseData.getBusinessProcess();
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

        @Override
        public BusinessProcess getBusinessProcess(GeneralApplicationCaseData caseData) {
            return caseData.getBusinessProcess();
        }
    }
}
