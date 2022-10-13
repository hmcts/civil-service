package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.*;
import uk.gov.hmcts.reform.civil.config.properties.hearing.HearingNotificationEmailConfiguration;
import uk.gov.hmcts.reform.civil.config.properties.notification.NotificationsProperties;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.FeesService;
import uk.gov.hmcts.reform.civil.service.NotificationService;
import uk.gov.hmcts.reform.civil.utils.InterestCalculator;
import uk.gov.hmcts.reform.civil.utils.MonetaryConversions;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.*;
import static uk.gov.hmcts.reform.civil.callback.UserType.CAMUNDA;

@Service
@RequiredArgsConstructor
public class NotificationOfHearingHandler extends CallbackHandler implements NotificationData {

    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;
    private final ObjectMapper objectMapper;
    private final HearingNotificationEmailConfiguration hearingNotificationEmailConfiguration;
    private static final List<CaseEvent> EVENTS = List.of(CaseEvent.NOTIFY_CLAIMANT_HEARING, CaseEvent.NOTIFY_DEFENDANT_HEARING);
    private static final String REFERENCE_TEMPLATE_HEARING = "notification-of-hearing-%s";
    public static final String TASK_ID_CLAIMANT = "NotifyClaimantHearing";
    public static final String TASK_ID_DEFENDANT = "NotifyDefendantHearing";

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::notificationOfHearing //TODO name of camunda block caseEvent?
        );
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        CaseEvent caseEvent = CaseEvent.valueOf(callbackParams.getRequest().getEventId());
        switch (caseEvent) {
            case NOTIFY_CLAIMANT_HEARING:
                return TASK_ID_CLAIMANT;
            case NOTIFY_DEFENDANT_HEARING:
                return TASK_ID_DEFENDANT;
            default:
                throw new CallbackException(String.format("Callback handler received illegal event: %s", caseEvent));
        }
    }

    private CallbackResponse notificationOfHearing(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseEvent caseEvent = CaseEvent.valueOf(callbackParams.getRequest().getEventId());
        CaseData.CaseDataBuilder caseDataBuilder = caseData.toBuilder();
        String recipient;
        String emailTemplate;
        switch (caseEvent) {
            case NOTIFY_CLAIMANT_HEARING:
                recipient = caseData.getApplicantSolicitor1UserDetails().getEmail();
                if(Integer.valueOf(caseData.getHearingFee().toString()) > 0){
                    emailTemplate = notificationsProperties.getHearingListedFeeClaimantLrTemplate();
                }else{
                    emailTemplate = notificationsProperties.getHearingListedNoFeeClaimantLrTemplate();
                }
                break;
            case NOTIFY_DEFENDANT_HEARING:
                recipient = caseData.getRespondentSolicitor1EmailAddress();
                emailTemplate = notificationsProperties.getHearingListedNoFeeDefendantLrTemplate();
                break;
            default:
                throw new CallbackException(String.format("Callback handler received illegal event: %s", caseEvent));
        }

        notificationService.sendMail(recipient,
                                     emailTemplate,
                                     addProperties(caseData),
                                     String.format(REFERENCE_TEMPLATE_HEARING,
                                                   caseData.getLegacyCaseReference()));

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.toMap(objectMapper))
            .build();
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    @Override
    public Map<String, String> addProperties(final CaseData caseData) {
        return new HashMap<>(Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(), //TODO this is already in sendMail method
            HEARING_FEE, String.valueOf(caseData.getHearingFee()), //TODO check all these data
            HEARING_DATE, caseData.getHearingDate().toString(),
            HEARING_TIME, caseData.getHearingTimeHourMinute(),
            DEADLINE_DATE, caseData.getRespondent1ResponseDeadline().toString(),
            CLAIMANT_REFERENCE_NUMBER, caseData.getSolicitorReferences().toString(),
            DEFENDANT_REFERENCE_NUMBER, caseData.getRespondentSolicitor2Reference()
        ));
    }

}

