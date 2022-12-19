package uk.gov.hmcts.reform.civil.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class
NotificationService {

    private final NotificationClient notificationClient;

    public void sendMail(
        String targetEmail,
        String emailTemplate,
        Map<String, String> parameters,
        String reference
    ) {
        try {
            System.out.println(" inside SendMail method");
            targetEmail = "civilmoneyclaimsdemo@gmail.com";
            notificationClient.sendEmail(emailTemplate, targetEmail, parameters, reference);
            System.out.println(" target email been changed and email been sent out to  " + targetEmail );
        } catch (NotificationClientException e) {
            throw new NotificationException(e);
        }
    }

    public void sendLetter(String letterTemplate, Map<String, ?> personalisation, String reference) {
        try {
            notificationClient.sendLetter(letterTemplate, personalisation, reference);
        } catch (NotificationClientException e) {
            throw new NotificationException(e);
        }
    }
}
