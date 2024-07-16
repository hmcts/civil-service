package uk.gov.hmcts.reform.civil.service.notification.defendantresponse.fulldefence;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.enums.AllocatedTrack.toStringValueForEmail;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.TWO_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.buildPartiesReferences;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

@Component
@RequiredArgsConstructor
public class FullDefenceRespondentSolicitorOneCCUnspecNotifier extends FullDefenceSolicitorUnspecNotifier {
    //NOTIFY_RESPONDENT_SOLICITOR1_FOR_DEFENDANT_RESPONSE_CC
    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;

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

    protected void sendNotificationToSolicitor(CaseData caseData, String recipient) {
        notificationService.sendMail(
            recipient,
            notificationsProperties.getClaimantSolicitorDefendantResponseFullDefence(),
            addProperties(caseData),
            String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference())
        );
    }

}
