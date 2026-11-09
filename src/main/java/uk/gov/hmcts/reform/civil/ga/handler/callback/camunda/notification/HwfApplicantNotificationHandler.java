package uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.ga.callback.GeneralApplicationCallbackHandler;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.service.HwfNotificationService;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_APPLICANT_LIP_HWF;

/**
 * Superseded by {@code GaHwfApplicantNotifier} on the Notifier framework, and deliberately kept.
 *
 * <p>Camunda versions process definitions, and an instance keeps running the version it started
 * on. When the migrated {@code notify_ga_applicant_hwf_outcome.bpmn} deploys, every
 * {@code GA_NOTIFY_HWF} instance already in flight continues on the previous version and still
 * reaches the old activity, which raises {@code NOTIFY_APPLICANT_LIP_HWF}. Deleting this handler
 * would leave those callbacks unhandled, so they would fail into Camunda incidents and the cases
 * would need unsticking by hand.
 *
 * <p>New instances use {@code NOTIFY_EVENT} and never reach this. Remove it once no running
 * instance remains on a pre migration version of the definition, which can be confirmed from the
 * engine rather than assumed from elapsed time.</p>
 *
 * <p>The removal takes this class and the {@code NOTIFY_APPLICANT_LIP_HWF} entry in
 * civil-ccd-definition, but not the {@link uk.gov.hmcts.reform.civil.callback.CaseEvent} value
 * itself. That name has a second, unrelated role: the five HWF user callbacks set it as the
 * business process event, and {@code GaEventEmitterService} correlates it as the Camunda start
 * message that launches {@code GA_NOTIFY_HWF}. Deleting the enum value or the
 * {@code bpmn:message} would stop the process starting at all.</p>
 *
 * @deprecated retained only to drain in flight process instances. See DTSCCI-6726.
 */
@Deprecated(forRemoval = true)
@Slf4j
@Service
@RequiredArgsConstructor
public class HwfApplicantNotificationHandler extends CallbackHandler implements GeneralApplicationCallbackHandler {

    private final ObjectMapper objectMapper;
    private final HwfNotificationService notificationService;
    private static final List<CaseEvent> EVENTS = List.of(
            NOTIFY_APPLICANT_LIP_HWF
    );

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::hwfApplicantNotification
        );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse hwfApplicantNotification(CallbackParams callbackParams) {
        GeneralApplicationCaseData caseData = callbackParams.getGeneralApplicationCaseData();
        log.info("HWF applicant notification for case: {}", caseData.getCcdCaseReference());
        notificationService.sendNotification(caseData);
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseData.toMap(objectMapper))
            .build();
    }
}
