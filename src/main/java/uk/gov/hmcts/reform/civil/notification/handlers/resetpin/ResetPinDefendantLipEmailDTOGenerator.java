package uk.gov.hmcts.reform.civil.notification.handlers.resetpin;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.config.PinInPostConfiguration;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.DefendantPinToPostLRspec;
import uk.gov.hmcts.reform.civil.notification.handlers.EmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

@Component
@AllArgsConstructor
@Slf4j
public class ResetPinDefendantLipEmailDTOGenerator extends EmailDTOGenerator {

    private static final String REFERENCE_TEMPLATE = "claim-reset-pin-notification-%s";
    private final PinInPostConfiguration pipInPostConfiguration;
    private final NotificationsProperties notificationsProperties;

    @Override
    public Boolean getShouldNotify(CaseData caseData) {
        return caseData.isRespondent1LiP() && caseData.getRespondent1PartyEmail() != null;
    }

    @Override
    public Map<String, String> addCustomProperties(Map<String, String> properties, CaseData caseData) {
        String lipAccessCode = Optional.ofNullable(caseData.getRespondent1PinToPostLRspec())
            .map(DefendantPinToPostLRspec::getAccessCode)
            .orElse("");

        if (StringUtils.isEmpty(lipAccessCode)) {
            log.info("Missing respondent access code for case id: {}", caseData.getCcdCaseReference());
        }

        properties.putAll(Map.of(
            RESPONDENT_NAME, getPartyNameBasedOnType(caseData.getRespondent1()),
            CLAIMANT_NAME, getPartyNameBasedOnType(caseData.getApplicant1()),
            ISSUED_ON, formatLocalDate(caseData.getIssueDate(), DATE),
            RESPOND_URL, pipInPostConfiguration.getRespondToClaimUrl(),
            CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
            CLAIM_16_DIGIT_NUMBER, caseData.getCcdCaseReference().toString(),
            PIN, lipAccessCode,
            RESPONSE_DEADLINE, formatLocalDate(caseData.getRespondent1ResponseDeadline()
                .toLocalDate(), DATE),
            FRONTEND_URL, pipInPostConfiguration.getCuiFrontEndUrl()
        ));
        return properties;
    }

    @Override
    protected String getEmailAddress(CaseData caseData) {
        return caseData.getRespondent1().getPartyEmail();
    }

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        return notificationsProperties.getRespondentDefendantResponseForSpec();
    }

    @Override
    protected String getReferenceTemplate() {
        return REFERENCE_TEMPLATE;
    }
}
