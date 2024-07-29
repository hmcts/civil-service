package uk.gov.hmcts.reform.civil.service.notification.defendantresponse.fulldefence;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.Optional;

import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP;

@Component
public class FullDefenceApplicantSolicitorOneCCSpecNotifier extends FullDefenceSolicitorCCSpecNotifier {

    //NOTIFY_APPLICANT_SOLICITOR1_FOR_DEFENDANT_RESPONSE_CC
    private final NotificationsProperties notificationsProperties;
    private final NotificationService notificationService;
    private final OrganisationService organisationService;

    @Autowired
    public FullDefenceApplicantSolicitorOneCCSpecNotifier(NotificationsProperties notificationsProperties, NotificationService notificationService,
                                                          OrganisationService organisationService) {
        super(notificationsProperties, organisationService);
        this.notificationsProperties = notificationsProperties;
        this.notificationService = notificationService;
        this.organisationService = organisationService;
    }

    @Override
    public String getRecipient(CaseData caseData) {
        return caseData.getRespondentSolicitor1EmailAddress();
    }

    @Override
    protected void sendNotificationToSolicitor(CaseData caseData, String recipient) {
        String emailTemplate;
        if (MultiPartyScenario.getMultiPartyScenario(caseData).equals(ONE_V_TWO_TWO_LEGAL_REP)) {
            emailTemplate = notificationsProperties.getRespondentSolicitorDefendantResponseForSpec();
        } else {
            emailTemplate = getTemplateForSpecOtherThan1v2DS(caseData);
        }
        if (caseData.getRespondent1ResponseDate() == null || !MultiPartyScenario.getMultiPartyScenario(caseData)
            .equals(ONE_V_TWO_TWO_LEGAL_REP)) {
            notificationService.sendMail(
                recipient,
                emailTemplate,
                addProperties(caseData),
                String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference())
            );
        }
    }

    //finding legal org name
    private String getLegalOrganisationName(CaseData caseData) {
        String organisationID;
        if (caseData.getRespondent1DQ() != null
            && caseData.getRespondent1ClaimResponseTypeForSpec() != null) {
            organisationID = caseData.getRespondent1OrganisationPolicy().getOrganisation().getOrganisationID();
        } else {
            organisationID = caseData.getRespondent2OrganisationPolicy().getOrganisation().getOrganisationID();
        }

        Optional<Organisation> organisation = organisationService.findOrganisationById(organisationID);
        return organisation.isPresent() ? organisation.get().getName() :
            caseData.getApplicantSolicitor1ClaimStatementOfTruth().getName();
    }
}
