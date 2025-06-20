package uk.gov.hmcts.reform.civil.notification.handlers.hearingprocess;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.RespSolTwoEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.Map;

import static uk.gov.hmcts.reform.civil.notification.handlers.hearingprocess.HearingProcessHelper.getRespSolTwoReference;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getFormattedHearingDate;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getFormattedHearingTime;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getRespondentLegalOrganizationName;

public class HearingProcessRespSolTwoEmailDTOGenerator extends RespSolTwoEmailDTOGenerator {

    protected static final String REFERENCE_RESP_SOL_TWO_TEMPLATE = "notification-of-hearing-%s";

    private final NotificationsProperties notificationsProperties;

    protected HearingProcessRespSolTwoEmailDTOGenerator(NotificationsProperties notificationsProperties,
                                                        OrganisationService organisationService) {
        super(organisationService);
        this.notificationsProperties = notificationsProperties;
    }

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        return notificationsProperties.getHearingListedNoFeeDefendantLrTemplate();
    }

    @Override
    protected String getReferenceTemplate() {
        return REFERENCE_RESP_SOL_TWO_TEMPLATE;
    }

    @Override
    protected Map<String, String> addCustomProperties(Map<String, String> properties, CaseData caseData) {
        properties.put(HEARING_DATE, getFormattedHearingDate(caseData.getHearingDate()));
        properties.put(HEARING_TIME, getFormattedHearingTime(caseData.getHearingTimeHourMinute()));
        properties.put(CLAIM_LEGAL_ORG_NAME_SPEC, getRespondentLegalOrganizationName(
            caseData.getRespondent2OrganisationPolicy(), organisationService));
        properties.put(DEFENDANT_REFERENCE_NUMBER, getRespSolTwoReference(caseData));
        return properties;
    }
}
