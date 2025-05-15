package uk.gov.hmcts.reform.civil.notification.handlers.mediation.carmEnabled;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.DefendantEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.TWO_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.isTwoVOne;

@Component
@AllArgsConstructor
public class CarmDefendantEmailDTOGenerator extends DefendantEmailDTOGenerator {

    private static final String REFERENCE_TEMPLATE = "notification-mediation-successful-defendant-LIP-%s";

    private final NotificationsProperties notificationsProperties;

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        return  caseData.isRespondentResponseBilingual()
            ? notificationsProperties.getNotifyLipSuccessfulMediationWelsh()
            : notificationsProperties.getNotifyLipSuccessfulMediation();
    }

    @Override
    protected String getReferenceTemplate() {
        return REFERENCE_TEMPLATE;
    }
}
