package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamSource;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.config.properties.mediation.MediationCSVEmailConfiguration;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sendgrid.EmailAttachment;
import uk.gov.hmcts.reform.civil.sendgrid.EmailData;
import uk.gov.hmcts.reform.civil.sendgrid.SendGridClient;
import uk.gov.hmcts.reform.civil.service.mediation.MediationCSVService;
import uk.gov.hmcts.reform.civil.service.mediation.MediationCsvServiceFactory;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_CYA_ON_AGREED_MEDIATION;

@Service
@RequiredArgsConstructor
@Slf4j
public class CyaAgreedMediationNotificationHandler extends CallbackHandler implements NotificationData {

    private static final List<CaseEvent> EVENTS = List.of(NOTIFY_CYA_ON_AGREED_MEDIATION);
    public static final String TASK_ID = "CyaAgreedMediationNotification";
    private final SendGridClient sendGridClient;
    private final MediationCSVEmailConfiguration mediationCSVEmailConfiguration;
    private final MediationCsvServiceFactory mediationCsvServiceFactory;

    private final String subject = "OCMC Mediation Data";
    private final String filename = "ocmc_mediation_data.csv";

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

        log.info("from " + mediationCSVEmailConfiguration.getSender());
        emailData.ifPresent(data -> sendGridClient.sendEmail(mediationCSVEmailConfiguration.getSender(), data));

        return AboutToStartOrSubmitCallbackResponse.builder()
            .build();
    }

    private Optional<EmailData> prepareEmail(CaseData data) {
        MediationCSVService mediationCSVService = mediationCsvServiceFactory.getMediationCSVService(data);
        String content = mediationCSVService.generateCSVContent(data);
        InputStreamSource inputSource = new ByteArrayResource(content.getBytes(StandardCharsets.UTF_8));

        return Optional.of(EmailData.builder()
                               .to(mediationCSVEmailConfiguration.getRecipient())
                               .subject(subject)
                               .attachments(List.of(new EmailAttachment(inputSource, "text/csv", filename)))
                               .build());
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        return null;
    }
}
