package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.config.properties.robotics.RoboticsEmailConfiguration;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sendgrid.EmailData;
import uk.gov.hmcts.reform.civil.sendgrid.SendGridClient;
import uk.gov.hmcts.reform.civil.service.citizenui.MediationCSVService;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.SEND_CSV_FOR_AGREED_MEDIATION;

@Service
@RequiredArgsConstructor
public class SendingCsvWhenAgreedMediationSpecHandler extends CallbackHandler implements NotificationData {

    private static final List<CaseEvent> EVENTS = List.of(SEND_CSV_FOR_AGREED_MEDIATION);
    public static final String TASK_ID = "SendingCsvWhenAgreedMediationSpec";
    private final SendGridClient sendGridClient;
    private final RoboticsEmailConfiguration roboticsEmailConfiguration;
    private final MediationCSVService mediationCSVService;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::sendCVSMediation
        );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return TASK_ID;
    }

    private CallbackResponse sendCVSMediation(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        Optional<EmailData> emailData = mediationCSVService.prepareEmail(caseData);
        emailData.ifPresent(data -> sendGridClient.sendEmail(roboticsEmailConfiguration.getSender(), data));
        return AboutToStartOrSubmitCallbackResponse.builder()
            .build();
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        return null;
    }
}
