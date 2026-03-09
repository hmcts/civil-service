package uk.gov.hmcts.reform.civil.notification.handlers.standarddirectionorderdj;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

import java.util.Map;

@Component
public class StandardDirectionOrderDJEmailDTOGeneratorBase {

    protected static final String LEGAL_ORG_NAME = "legalOrgName";
    protected static final String CLAIM_NUMBER = "claimReferenceNumber";

    private final NotificationsProperties notificationsProperties;

    public StandardDirectionOrderDJEmailDTOGeneratorBase(NotificationsProperties notificationsProperties) {
        this.notificationsProperties = notificationsProperties;
    }

    public String getEmailTemplateId(CaseData caseData) {
        return notificationsProperties.getStandardDirectionOrderDJTemplate();
    }

    public void addClaimReferenceNumber(Map<String, String> properties, CaseData caseData) {
        Long caseReference = caseData.getCcdCaseReference();
        if (caseReference != null) {
            properties.put(CLAIM_NUMBER, caseReference.toString());
        }
    }
}
