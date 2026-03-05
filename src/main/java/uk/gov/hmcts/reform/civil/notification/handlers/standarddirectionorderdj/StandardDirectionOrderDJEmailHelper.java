package uk.gov.hmcts.reform.civil.notification.handlers.standarddirectionorderdj;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

import java.util.Map;

/**
 * Helper class to reduce code duplication across Standard Direction Order DJ email generators.
 * Provides common functionality for template ID retrieval and property management.
 */
public class StandardDirectionOrderDJEmailHelper {

    private static final String CLAIM_NUMBER = "claimReferenceNumber";
    private final NotificationsProperties notificationsProperties;

    public StandardDirectionOrderDJEmailHelper(NotificationsProperties notificationsProperties) {
        this.notificationsProperties = notificationsProperties;
    }

    /**
     * Gets the email template ID for Standard Direction Order DJ notifications.
     *
     * @return the template ID
     */
    public String getTemplateId() {
        return notificationsProperties.getStandardDirectionOrderDJTemplate();
    }

    /**
     * Adds the claim reference number to the properties map.
     *
     * @param properties the properties map to populate
     * @param caseData the case data containing the claim reference
     */
    public void addClaimReferenceNumber(Map<String, String> properties, CaseData caseData) {
        Long caseReference = caseData.getCcdCaseReference();
        if (caseReference != null) {
            properties.put(CLAIM_NUMBER, caseReference.toString());
        }
    }
}