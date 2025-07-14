package uk.gov.hmcts.reform.civil.notification.handlers.generatehearingnotice.nonhmc;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.RespSolTwoEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.utils.NotificationUtils;

import java.util.Map;

import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getLegalOrganizationNameForRespondent;

@Component
public class GenerateHearingNoticeRespSolTwoEmailDTOGenerator extends RespSolTwoEmailDTOGenerator {

    private static final String REFERENCE_TEMPLATE_HEARING_RESP_SOL_TWO = "notification-of-hearing-%s";
    private final NotificationsProperties notificationsProperties;

    public GenerateHearingNoticeRespSolTwoEmailDTOGenerator(NotificationsProperties notificationsProperties,
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
        return REFERENCE_TEMPLATE_HEARING_RESP_SOL_TWO;
    }

    @Override
    protected Map<String, String> addCustomProperties(Map<String, String> properties, CaseData caseData) {
        boolean isRespondent1 = false;
        properties.put(HEARING_DATE, NotificationUtils.getFormattedHearingDate(caseData.getHearingDate()));
        properties.put(HEARING_TIME, NotificationUtils.getFormattedHearingTime(caseData.getHearingTimeHourMinute()));
        properties.put(DEFENDANT_REFERENCE_NUMBER, caseData.getRespondentSolicitor2Reference() == null ? "" :
                caseData.getRespondentSolicitor2Reference());
        properties.put(CLAIM_LEGAL_ORG_NAME_SPEC, getLegalOrganizationNameForRespondent(caseData,
                                                                                        isRespondent1, organisationService));
        return properties;
    }
}
