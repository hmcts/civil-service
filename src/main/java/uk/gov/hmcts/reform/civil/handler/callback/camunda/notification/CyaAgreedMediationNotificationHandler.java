package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamSource;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.config.properties.robotics.RoboticsEmailConfiguration;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sendgrid.EmailAttachment;
import uk.gov.hmcts.reform.civil.sendgrid.EmailData;
import uk.gov.hmcts.reform.civil.sendgrid.SendGridClient;
import uk.gov.hmcts.reform.civil.service.citizenui.MediationCSVService;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_CYA_ON_AGREED_MEDIATION;

@Service
@RequiredArgsConstructor
public class CyaAgreedMediationNotificationHandler extends CallbackHandler implements NotificationData {

    private static final List<CaseEvent> EVENTS = List.of(NOTIFY_CYA_ON_AGREED_MEDIATION);
    public static final String TASK_ID = "CyaAgreedMediationNotification";
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
        Optional<EmailData> emailData = prepareEmail(caseData);
        emailData.ifPresent(data -> sendGridClient.sendEmail(roboticsEmailConfiguration.getSender(), data));
        return AboutToStartOrSubmitCallbackResponse.builder()
            .build();
    }

    public Optional<EmailData> prepareEmail(CaseData data) {
        String content = mediationCSVService.generateCSVContent(data);
        InputStreamSource inputSource = new ByteArrayResource(content.getBytes(StandardCharsets.UTF_8));

        String toEmail = "smallclaimsmediation@justice.gov.uk";
        String subject = "OCMC Mediation Data";
        String filename = "ocmc_mediation_data.csv";

        return Optional.of(EmailData.builder()
                               .to(toEmail)
                               .subject(subject)
                               .attachments(List.of(new EmailAttachment(inputSource, "text/csv", filename)))
                               .build());
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        return null;
    }
}
