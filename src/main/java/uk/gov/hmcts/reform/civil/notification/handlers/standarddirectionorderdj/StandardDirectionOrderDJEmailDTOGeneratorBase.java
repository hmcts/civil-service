package uk.gov.hmcts.reform.civil.notification.handlers.standarddirectionorderdj;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

import java.util.Map;

/**
 * Helper component for Standard Direction Order DJ email generators.
 * Provides common functionality to reduce code duplication.
 */
@Component
public class StandardDirectionOrderDJEmailDTOGeneratorBase {

    private final NotificationsProperties notificationsProperties;

    public StandardDirectionOrderDJEmailDTOGeneratorBase(NotificationsProperties notificationsProperties) {
        this.notificationsProperties = notificationsProperties;
    }

    /**
     * Gets the email template ID for Standard Direction Order DJ notifications.
     *
     * @param caseData the case data
     * @return the template ID
     */
    public String getEmailTemplateId(CaseData caseData) {
        return StandardDirectionOrderDJBaseEmailDTOGenerator.getTemplateId(notificationsProperties);
    }

    /**
     * Adds the claim reference number to the properties map.
     *
     * @param properties the properties map to populate
     * @param caseData the case data
     */
    public void addClaimReferenceNumber(Map<String, String> properties, CaseData caseData) {
        StandardDirectionOrderDJBaseEmailDTOGenerator.addClaimReferenceNumber(properties, caseData);
    }
}
