package uk.gov.hmcts.reform.civil.handler.callback.user.dj.tasks.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.handler.callback.user.directionsorder.tasks.DirectionsOrderCallbackTask;
import uk.gov.hmcts.reform.civil.handler.callback.user.directionsorder.tasks.DirectionsOrderLifecycleStage;
import uk.gov.hmcts.reform.civil.handler.callback.user.directionsorder.tasks.DirectionsOrderTaskContext;
import uk.gov.hmcts.reform.civil.handler.callback.user.directionsorder.tasks.DirectionsOrderTaskResult;
import uk.gov.hmcts.reform.civil.handler.callback.user.directionsorder.tasks.DirectionsOrderTaskSupport;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.service.dj.DjDocumentService;
import uk.gov.hmcts.reform.civil.service.sdo.SdoLocationService;
import uk.gov.hmcts.reform.civil.utils.ElementUtils;

import java.util.List;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.STANDARD_DIRECTION_ORDER_DJ;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;

@Component
@RequiredArgsConstructor
@Slf4j
public class DjDocumentTask implements DirectionsOrderCallbackTask {

    private final DjDocumentService djDocumentService;
    private final SdoLocationService sdoLocationService;

    @Override
    public DirectionsOrderTaskResult execute(DirectionsOrderTaskContext context) {
        CaseData caseData = context.caseData();
        String authToken = context.callbackParams().getParams().get(BEARER_TOKEN).toString();
        log.info("Generating DJ order document for caseId {}", caseData.getCcdCaseReference());
        return djDocumentService.generateOrder(caseData, authToken)
            .map(document -> buildResult(caseData, document))
            .orElseGet(() -> DirectionsOrderTaskResult.empty(caseData));
    }

    @Override
    public boolean supports(DirectionsOrderLifecycleStage stage) {
        return DirectionsOrderLifecycleStage.DOCUMENT_GENERATION == stage;
    }

    @Override
    public boolean appliesTo(DirectionsOrderTaskContext context) {
        return DirectionsOrderTaskSupport.supportsEvent(context, STANDARD_DIRECTION_ORDER_DJ);
    }

    private DirectionsOrderTaskResult buildResult(CaseData caseData, CaseDocument document) {
        djDocumentService.assignCategory(document, "caseManagementOrders");
        log.info("Generated DJ order document for caseId {}", caseData.getCcdCaseReference());
        CaseData.CaseDataBuilder<?, ?> builder = caseData.toBuilder()
            .orderSDODocumentDJ(document.getDocumentLink())
            .orderSDODocumentDJCollection(List.of(toElement(document)))
            .disposalHearingMethodInPersonDJ(trimList(caseData.getDisposalHearingMethodInPersonDJ()))
            .trialHearingMethodInPersonDJ(trimList(caseData.getTrialHearingMethodInPersonDJ()));

        return new DirectionsOrderTaskResult(builder.build(), null, null);
    }

    private Element<CaseDocument> toElement(CaseDocument document) {
        return ElementUtils.element(document);
    }

    private DynamicList trimList(DynamicList list) {
        return list == null ? null : sdoLocationService.trimListItems(list);
    }
}
