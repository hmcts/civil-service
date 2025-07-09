package uk.gov.hmcts.reform.civil.notification.handlers.hearingprocess;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.AppSolOneEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.Map;
import static uk.gov.hmcts.reform.civil.notification.handlers.hearingprocess.HearingProcessHelper.getAppSolReference;
import static uk.gov.hmcts.reform.civil.notification.handlers.hearingprocess.HearingProcessHelper.getHearingFeePropertiesIfNotPaid;
import static uk.gov.hmcts.reform.civil.notification.handlers.hearingprocess.HearingProcessHelper.isNoFeeDue;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getApplicantLegalOrganizationName;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getFormattedHearingDate;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getFormattedHearingTime;

@Component
public class HearingProcessAppSolEmailDTOGenerator extends AppSolOneEmailDTOGenerator {

    private static final String REFERENCE_APP_SOL_ONE_TEMPLATE = "notification-of-hearing-%s";

    private final NotificationsProperties notificationsProperties;

    protected HearingProcessAppSolEmailDTOGenerator(OrganisationService organisationService,
                                                    NotificationsProperties notificationsProperties) {
        super(organisationService);
        this.notificationsProperties = notificationsProperties;
    }

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        return isNoFeeDue(caseData)
            ? notificationsProperties.getHearingListedNoFeeClaimantLrTemplate()
            : notificationsProperties.getHearingListedFeeClaimantLrTemplate();
    }

    @Override
    protected String getReferenceTemplate() {
        return REFERENCE_APP_SOL_ONE_TEMPLATE;
    }

    @Override
    protected Map<String, String> addCustomProperties(Map<String, String> properties, CaseData caseData) {
        properties.put(HEARING_DATE, getFormattedHearingDate(caseData.getHearingDate()));
        properties.put(HEARING_TIME, getFormattedHearingTime(caseData.getHearingTimeHourMinute()));
        properties.put(CLAIM_LEGAL_ORG_NAME_SPEC, getApplicantLegalOrganizationName(caseData, organisationService));
        properties.put(CLAIMANT_REFERENCE_NUMBER, getAppSolReference(caseData));
        properties.putAll(getHearingFeePropertiesIfNotPaid(caseData));
        return properties;
    }
}
