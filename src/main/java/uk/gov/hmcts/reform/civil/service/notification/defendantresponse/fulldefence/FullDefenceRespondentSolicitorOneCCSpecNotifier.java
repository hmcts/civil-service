package uk.gov.hmcts.reform.civil.service.notification.defendantresponse.fulldefence;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.Optional;

import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP;

@Component
public class FullDefenceRespondentSolicitorOneCCSpecNotifier extends FullDefenceSolicitorCCSpecNotifier {

    //NOTIFY_RESPONDENT_SOLICITOR1_FOR_DEFENDANT_RESPONSE_CC
    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;

    public FullDefenceRespondentSolicitorOneCCSpecNotifier(NotificationsProperties notificationsProperties, NotificationService notificationService,
                                                           OrganisationService organisationService) {
        super(notificationsProperties, organisationService);
        this.notificationsProperties = notificationsProperties;
        this.notificationService = notificationService;
    }

    @Override
    protected String getRecipient(CaseData caseData) {
        if (caseData.getRespondent1DQ() != null
            && caseData.getRespondent1ClaimResponseTypeForSpec() != null) {
            var emailAddress = Optional.ofNullable(caseData.getRespondentSolicitor1EmailAddress());
            return emailAddress.orElse(null);
        } else {
            var emailAddress = Optional.ofNullable(caseData.getRespondentSolicitor2EmailAddress());
            return emailAddress.orElse(null);
        }
    }

    @Override
    protected void sendNotificationToSolicitor(CaseData caseData, String recipient) {
        String emailTemplate;
        if (MultiPartyScenario.getMultiPartyScenario(caseData).equals(ONE_V_TWO_TWO_LEGAL_REP)) {
            emailTemplate = notificationsProperties.getRespondentSolicitorDefendantResponseForSpec();
        } else {
            emailTemplate = getTemplateForSpecOtherThan1v2DS(caseData);
        }
        notificationService.sendMail(
            recipient,
            emailTemplate,
            addProperties(caseData),
            String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference())
        );
    }

}
