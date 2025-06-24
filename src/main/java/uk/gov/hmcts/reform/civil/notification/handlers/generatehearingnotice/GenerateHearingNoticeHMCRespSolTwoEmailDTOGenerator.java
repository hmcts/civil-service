package uk.gov.hmcts.reform.civil.notification.handlers.generatehearingnotice;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.RespSolOneEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.service.hearingnotice.HearingNoticeCamundaService;
import uk.gov.hmcts.reform.civil.utils.NotificationUtils;

import java.time.LocalDateTime;
import java.util.Map;

@Component
public class GenerateHearingNoticeHMCRespSolTwoEmailDTOGenerator extends RespSolOneEmailDTOGenerator {

    private static final String REFERENCE_TEMPLATE_HEARING_HMC_RESP_SOL_TWO = "notification-of-hearing-%s";
    private final NotificationsProperties notificationsProperties;
    private final HearingNoticeCamundaService camundaService;

    public GenerateHearingNoticeHMCRespSolTwoEmailDTOGenerator(NotificationsProperties notificationsProperties,
                                                               HearingNoticeCamundaService camundaService,
                                                               OrganisationService organisationService) {
        super(organisationService);
        this.notificationsProperties = notificationsProperties;
        this.camundaService = camundaService;
    }

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        return notificationsProperties.getHearingListedNoFeeDefendantLrTemplateHMC();
    }

    @Override
    protected String getReferenceTemplate() {
        return REFERENCE_TEMPLATE_HEARING_HMC_RESP_SOL_TWO;
    }

    @Override
    protected Map<String, String> addCustomProperties(Map<String, String> properties, CaseData caseData) {
        LocalDateTime hearingStartDateTime = camundaService
                .getProcessVariables(caseData.getBusinessProcess().getProcessInstanceId()).getHearingStartDateTime();

        properties.put(HEARING_DATE, NotificationUtils.getFormattedHearingDate(caseData.getHearingDate()));
        properties.put(HEARING_TIME, NotificationUtils.getFormattedHearingTime(hearingStartDateTime.toLocalTime().toString()));
        properties.put(DEFENDANT_REFERENCE_NUMBER, caseData.getSolicitorReferences().getRespondentSolicitor1Reference());
        return properties;
    }
}
