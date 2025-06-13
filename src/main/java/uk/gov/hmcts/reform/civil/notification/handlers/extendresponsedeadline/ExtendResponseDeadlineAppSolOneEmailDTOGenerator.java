package uk.gov.hmcts.reform.civil.notification.handlers.extendresponsedeadline;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.AppSolOneEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.Map;

import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

@Component
public class ExtendResponseDeadlineAppSolOneEmailDTOGenerator extends AppSolOneEmailDTOGenerator {

    private static final String REFERENCE_APP_SOL_ONE_TEMPLATE = "claimant-deadline-extension-notification-%s";

    private final NotificationsProperties notificationsProperties;

    protected ExtendResponseDeadlineAppSolOneEmailDTOGenerator(OrganisationService organisationService, NotificationsProperties notificationsProperties) {
        super(organisationService);
        this.notificationsProperties = notificationsProperties;
    }

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        return notificationsProperties.getClaimantDeadlineExtension();
    }

    @Override
    protected String getReferenceTemplate() {
        return REFERENCE_APP_SOL_ONE_TEMPLATE;
    }

    @Override
    protected Map<String, String> addCustomProperties(Map<String, String> properties, CaseData caseData) {
        properties.put(RESPONDENT_NAME, getPartyNameBasedOnType(caseData.getRespondent1()));
        properties.put(AGREED_EXTENSION_DATE, formatLocalDate(
                caseData.getRespondentSolicitor1AgreedDeadlineExtension(), DATE));
        return properties;
    }

}
