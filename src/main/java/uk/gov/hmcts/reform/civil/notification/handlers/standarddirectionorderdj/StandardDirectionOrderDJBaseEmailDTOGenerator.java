package uk.gov.hmcts.reform.civil.notification.handlers.standarddirectionorderdj;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.Map;

public abstract class StandardDirectionOrderDJBaseEmailDTOGenerator {

    protected final NotificationsProperties notificationsProperties;
    protected final OrganisationService organisationService;
    protected static final String LEGAL_ORG_NAME = "legalOrgName";
    protected static final String CLAIM_NUMBER = "claimReferenceNumber";

    protected StandardDirectionOrderDJBaseEmailDTOGenerator(
        NotificationsProperties notificationsProperties,
        OrganisationService organisationService
    ) {
        this.notificationsProperties = notificationsProperties;
        this.organisationService = organisationService;
    }

    public String getEmailTemplateId() {
        return notificationsProperties.getStandardDirectionOrderDJTemplate();
    }

    /**
     * Adds standard properties to the notification template.
     * This method provides a base implementation that can be extended by subclasses
     * to add additional standard properties as needed.
     *
     * @param properties the properties map to populate
     * @param caseData the case data containing the information
     */
    protected void addStandardProperties(Map<String, String> properties, CaseData caseData) {
        validateNotificationParameters(properties, caseData);
        addClaimReferenceNumber(properties, caseData);
    }

    /**
     * Validates the notification parameters before processing.
     *
     * @param properties the properties map to validate
     * @param caseData the case data to validate
     * @throws IllegalArgumentException if any parameter is null
     */
    private void validateNotificationParameters(Map<String, String> properties, CaseData caseData) {
        if (properties == null) {
            throw new IllegalArgumentException("Properties map cannot be null");
        }
        if (caseData == null) {
            throw new IllegalArgumentException("Case data cannot be null");
        }
    }

    /**
     * Helper method to get the Standard Direction Order DJ email template ID.
     * Can be used by classes that don't extend this base class.
     *
     * @param notificationsProperties the notifications properties
     * @return the template ID
     */
    public static String getTemplateId(NotificationsProperties notificationsProperties) {
        return notificationsProperties.getStandardDirectionOrderDJTemplate();
    }

    /**
     * Helper method to add the claim reference number to properties.
     * Can be used by classes that don't extend this base class.
     *
     * @param properties the properties map to populate
     * @param caseData the case data containing the claim reference
     */
    public static void addClaimReferenceNumber(Map<String, String> properties, CaseData caseData) {
        Long caseReference = caseData.getCcdCaseReference();
        if (caseReference != null) {
            properties.put(CLAIM_NUMBER, caseReference.toString());
        }
    }

    /**
     * Helper method to populate all Standard Direction Order DJ properties including legal org name.
     *
     * @param properties the properties map to populate
     * @param caseData the case data
     * @param legalOrgName the legal organization name to add
     */
    public static void populateProperties(Map<String, String> properties, CaseData caseData, String legalOrgName) {
        properties.put(LEGAL_ORG_NAME, legalOrgName);
        addClaimReferenceNumber(properties, caseData);
    }
}
