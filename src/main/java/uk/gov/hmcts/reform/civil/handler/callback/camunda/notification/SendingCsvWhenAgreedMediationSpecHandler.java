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

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.SENDING_CSV_WHEN_AGREED_MEDIATION_SPEC;

@Service
@RequiredArgsConstructor
public class SendingCsvWhenAgreedMediationSpecHandler extends CallbackHandler implements NotificationData {

    private static final List<CaseEvent> EVENTS = List.of(SENDING_CSV_WHEN_AGREED_MEDIATION_SPEC);
    public static final String TASK_ID = "SendingCsvWhenAgreedMediationSpec";
    private final SendGridClient sendGridClient;
    private final RoboticsEmailConfiguration roboticsEmailConfiguration;

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

    private Optional<EmailData> prepareEmail(CaseData data) {
//        String toEmail = "smallclaimsmediation@justice.gov.uk";
        String toEmail = "civilmoneyclaimsdemo@gmail.com";
        String subject = "OCMC Mediation Data";
        String filename = "ocmc_mediation_data.csv";
        String content = generateAttachmentContent(data);
        InputStreamSource inputSource = new ByteArrayResource(content.getBytes(StandardCharsets.UTF_8));


        return Optional.of(EmailData.builder()
                               .to(toEmail)
                               .subject(subject)
                               .attachments(List.of(new EmailAttachment(inputSource, "text/csv", filename)))
                               .build());
    }

    private String generateAttachmentContent(CaseData data) {
        StringBuilder content = new StringBuilder();
        content.append(5);
        content.append(data.getLegacyCaseReference());
        content.append(1);
        content.append(data.getTotalClaimAmount());
        content.append(data.getApplicant1().getType());
        content.append(data.getApplicant1().getCompanyName());
        content.append(data.getApplicant1().getPartyName());
        content.append(data.getApplicant1().getPartyPhone());
        content.append(4);
        content.append(5);
        content.append(data.getApplicant1().getPartyEmail());
        content.append(data.getTotalClaimAmount().compareTo(new BigDecimal(10000)) < 0 ? "Yes" : "No");

        return content.toString();
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        return null;
    }
}
