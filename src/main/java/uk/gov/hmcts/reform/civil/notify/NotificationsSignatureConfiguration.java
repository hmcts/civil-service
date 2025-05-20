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
    private final String hmctsSignatureWelsh;
    private final String phoneContactWelsh;
    private final String openingHoursWelsh;
    private final String specUnspecContact;
    private final String cnbcContact;

    public NotificationsSignatureConfiguration(@Value("${notifications.hmctsSignature}") String hmctsSignature,
                                               @Value("${notifications.phoneContact}") String phoneContact,
                                               @Value("${notifications.openingHours}") String openingHours,
                                               @Value("${notifications.hmctsSignatureWelsh}") String hmctsSignatureWelsh,
                                               @Value("${notifications.phoneContactWelsh}") String phoneContactWelsh,
                                               @Value("${notifications.openingHoursWelsh}") String openingHoursWelsh,
                                               @Value("${notifications.specUnspecContact}") String specUnspecContact,
                                               @Value("${notifications.cnbcContact}") String cnbcContact) {

        this.hmctsSignature = hmctsSignature;
        this.phoneContact = phoneContact;
        this.openingHours = openingHours;
        this.hmctsSignatureWelsh = hmctsSignatureWelsh;
        this.phoneContactWelsh = phoneContactWelsh;
        this.openingHoursWelsh = openingHoursWelsh;
        this.specUnspecContact = specUnspecContact;
        this.cnbcContact = cnbcContact;
    }
}
