package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.Optional;

/**
 * When an SDO is created it is notified to applicants and defendants.
 * This class sends the email to a represented second defendant.
 */
@Component
public class CreateSDORespondent2LRNotificationSender extends AbstractCreateSDORespondentLRNotificationSender {

    private static final String REFERENCE_TEMPLATE = "create-sdo-respondent-2-notification-%s";

    public CreateSDORespondent2LRNotificationSender(
        NotificationService notificationService,
        NotificationsProperties notificationsProperties,
        OrganisationService organisationService) {
        super(notificationService, notificationsProperties, organisationService);
    }

    @Override
    protected String getDocReference(CaseData caseData) {
        return String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference());
    }

    @Override
    protected String getRecipientEmail(CaseData caseData) {
        String r2 = caseData.getRespondentSolicitor2EmailAddress();
        if (StringUtils.isNotBlank(r2)) {
            return r2;
        } else {
            return caseData.getRespondentSolicitor1EmailAddress();
        }
    }

    @Override
    protected String getLROrganisationId(CaseData caseData) {
        return Optional.ofNullable(caseData.getRespondent2SameLegalRepresentative() == YesOrNo.NO
                                       ? caseData.getRespondent2OrganisationPolicy()
                                       : caseData.getRespondent1OrganisationPolicy())
            .map(OrganisationPolicy::getOrganisation).map(Organisation::getOrganisationID).orElse(null);

    }
}
