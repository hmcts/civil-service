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
import uk.gov.hmcts.reform.civil.ga.service.HearingScheduledNotificationService;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_HEARING_NOTICE_DEFENDANT;

@Slf4j
@Service
/**
 * Superseded by {@code GaHearingScheduledNotifier} on the Notifier framework, and deliberately kept.
 *
 * <p>Camunda versions process definitions, and an instance keeps running the version it started
 * on. Both definitions that raised this event are migrated:
 * {@code GA_HEARING_SCHEDULED_PROCESS_ID} and, for the Welsh translated notice,
 * {@code UPLOAD_TRANSLATED_GA_HEARING_SCHEDULED_PROCESS_ID}. Instances of either already in
 * flight when the migrated definitions deploy continue on the previous version and still raise
 * {@code NOTIFY_HEARING_NOTICE_DEFENDANT}. Deleting this handler would leave those callbacks unhandled, so they would
 * fail into Camunda incidents and the cases would need unsticking by hand.</p>
 *
 * <p>New instances raise {@code NOTIFY_EVENT} and the {@code respondents} email is built by its
 * generator instead. Remove this and its civil-ccd-definition entry once no running instance
 * remains on a pre migration version of the definition, which can be confirmed from the engine
 * rather than assumed from elapsed time.</p>
 *
 * @deprecated retained only to drain in flight process instances. See DTSCCI-6726.
 */
@Deprecated(forRemoval = true)
@RequiredArgsConstructor
public class NotifyHearingNoticeDefendantHandler extends CallbackHandler implements GeneralApplicationCallbackHandler {

    private final ObjectMapper objectMapper;
    private final HearingScheduledNotificationService hearingScheduledNotificationService;
    private static final List<CaseEvent> EVENTS = List.of(
        NOTIFY_HEARING_NOTICE_DEFENDANT
    );

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::notifyHearingNoticeToDefendant
        );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse notifyHearingNoticeToDefendant(CallbackParams callbackParams) {

        GeneralApplicationCaseData caseData = callbackParams.getGeneralApplicationCaseData();
        log.info("Notify hearing notice to defendant for case: {}", caseData.getCcdCaseReference());
        caseData = hearingScheduledNotificationService.sendNotificationForDefendant(caseData);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseData.toMap(objectMapper))
            .build();
    }
}
