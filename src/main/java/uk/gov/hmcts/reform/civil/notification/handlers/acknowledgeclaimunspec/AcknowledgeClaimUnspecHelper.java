package uk.gov.hmcts.reform.civil.notification.handlers.acknowledgeclaimunspec;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.time.LocalDateTime;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_LEGAL_ORG_NAME_SPEC;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONDENT_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONSE_DEADLINE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONSE_INTENTION;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getLegalOrganizationNameForRespondent;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getResponseIntentionForEmail;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.isAcknowledgeUserRespondentTwo;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;

@Component
@AllArgsConstructor
public class AcknowledgeClaimUnspecHelper {

    private final OrganisationService organisationService;

    protected Map<String, String> addTemplateProperties(Map<String, String> properties, CaseData caseData) {
        boolean isRespondent1Acknowledged = isRespondentOneAcknowledged(caseData);
        Party respondent = getAcknowledgedRespondent(caseData, isRespondent1Acknowledged);
        LocalDateTime responseDeadline = getResponseDeadline(caseData, isRespondent1Acknowledged);

        properties.put(
            CLAIM_LEGAL_ORG_NAME_SPEC, getLegalOrganizationNameForRespondent(
                caseData,
                isRespondent1Acknowledged, organisationService
            )
        );
        properties.put(RESPONDENT_NAME, getPartyNameBasedOnType(respondent));
        properties.put(RESPONSE_DEADLINE, formatLocalDate(responseDeadline.toLocalDate(), DATE));
        properties.put(RESPONSE_INTENTION, getResponseIntentionForEmail(caseData));

        return properties;
    }

    private Party getAcknowledgedRespondent(CaseData caseData, boolean isRespondent1Acknowledged) {
        return isRespondent1Acknowledged ? caseData.getRespondent1()
            : caseData.getRespondent2();
    }

    private LocalDateTime getResponseDeadline(CaseData caseData, boolean isRespondent1Acknowledged) {
        return isRespondent1Acknowledged ? caseData.getRespondent1ResponseDeadline()
            : caseData.getRespondent2ResponseDeadline();
    }

    protected boolean isRespondentOneAcknowledged(CaseData caseData) {
        MultiPartyScenario multiPartyScenario = getMultiPartyScenario(caseData);
        return !(multiPartyScenario == ONE_V_TWO_TWO_LEGAL_REP && isAcknowledgeUserRespondentTwo(caseData));
    }
}
