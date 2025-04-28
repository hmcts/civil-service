package uk.gov.hmcts.reform.civil.notify;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
public class NotificationsSignatureConfiguration {

    private final String hmctsSignature;
    private final String phoneContact;
    private final String openingHours;
    private final String specContact;
    private final String unspecContact;
    private final String cnbcContact;

    public NotificationsSignatureConfiguration(@Value("${notifications.hmctsSignature}") String hmctsSignature,
                                               @Value("${notifications.phoneContact}") String phoneContact,
                                               @Value("${notifications.openingHours}") String openingHours,
                                               @Value("${notifications.specContact}") String specContact,
                                               @Value("${notifications.unspecContact}") String unspecContact,
                                               @Value("${notifications.cnbcContact}") String cnbcContact) {

        this.hmctsSignature = hmctsSignature;
        this.phoneContact = phoneContact;
        this.openingHours = openingHours;
        this.specContact = specContact;
        this.unspecContact = unspecContact;
        this.cnbcContact = cnbcContact;
    }

}
