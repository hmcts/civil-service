package uk.gov.hmcts.reform.civil.config.properties.mediation;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class MediationCSVEmailConfiguration {

    private final String sender;
    private  final String recipient;

    public MediationCSVEmailConfiguration(
        @Value("${mediation.emails.sender}") String sender,
        @Value("${mediation.emails.recipient}") String recipient) {
        this.sender = sender;
        this.recipient = recipient;
    }
}
