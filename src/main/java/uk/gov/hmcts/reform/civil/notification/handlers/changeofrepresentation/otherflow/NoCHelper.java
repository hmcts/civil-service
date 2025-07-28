package uk.gov.hmcts.reform.civil.notification.handlers.changeofrepresentation.otherflow;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.callback.CallbackException;
import uk.gov.hmcts.reform.civil.enums.PaymentStatus;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.PaymentDetails;
import uk.gov.hmcts.reform.civil.notification.handlers.changeofrepresentation.common.NotificationHelper;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CASE_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CCD_REF;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIMANT_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_16_DIGIT_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.COURT_LOCATION;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.DEFENDANT_NAME_INTERIM;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.FORMER_SOL;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.HEARING_DATE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.HEARING_DUE_DATE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.HEARING_FEE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.HEARING_TIME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.ISSUE_DATE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.LEGAL_ORG_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.LEGAL_REP_NAME_WITH_SPACE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.NEW_SOL;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.OTHER_SOL_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.REFERENCE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getApplicantLegalOrganizationName;

@Component
@AllArgsConstructor
public class NoCHelper {

    protected static final String LIP = "LiP";
    protected static final String REFERENCE_TEMPLATE = "notice-of-change-%s";

    private final OrganisationService organisationService;

    public Map<String, String> getProperties(CaseData caseData, boolean isOtherSolicitor2) {
        return Map.of(
            CASE_NAME, NotificationHelper.getCaseName(caseData),
            ISSUE_DATE, formatLocalDate(caseData.getIssueDate(), DATE),
            CCD_REF, caseData.getCcdCaseReference().toString(),
            FORMER_SOL, getOrganisationName(caseData.getChangeOfRepresentation().getOrganisationToRemoveID()),
            NEW_SOL, getOrganisationName(caseData.getChangeOfRepresentation().getOrganisationToAddID()),
            OTHER_SOL_NAME, getOtherSolicitorOrgName(caseData, isOtherSolicitor2),
            LEGAL_REP_NAME_WITH_SPACE, getOrganisationName(caseData.getChangeOfRepresentation().getOrganisationToAddID()),
            REFERENCE, caseData.getCcdCaseReference().toString()
        );
    }

    public Map<String, String> getClaimantLipProperties(CaseData caseData) {
        return Map.of(
            CLAIMANT_NAME, caseData.getApplicant1().getPartyName(),
            CLAIM_16_DIGIT_NUMBER, caseData.getCcdCaseReference().toString(),
            DEFENDANT_NAME_INTERIM, caseData.getRespondent1().getPartyName(),
            CLAIM_NUMBER, caseData.getLegacyCaseReference()
        );
    }

    public Map<String, String> getHearingFeeEmailProperties(CaseData caseData) {
        return Map.of(
            LEGAL_ORG_NAME, getApplicantLegalOrganizationName(caseData, organisationService),
            HEARING_DATE, formatLocalDate(caseData.getHearingDate(), DATE),
            COURT_LOCATION, caseData.getHearingLocation().getValue().getLabel(),
            HEARING_TIME, caseData.getHearingTimeHourMinute(),
            HEARING_FEE, String.valueOf(caseData.getHearingFee().formData()),
            HEARING_DUE_DATE, formatLocalDate(caseData.getHearingDueDate(), DATE)
        );
    }

    public String getOrganisationName(String orgId) {
        return Optional.ofNullable(orgId)
            .map(id -> organisationService.findOrganisationById(id)
                .orElseThrow(() -> new CallbackException("Invalid organisation ID: " + id)).getName())
            .orElse(LIP);
    }

    private String getOtherSolicitorOrgName(CaseData caseData, boolean isOtherSolicitor2) {
        String orgId = isOtherSolicitor2
            ? NotificationHelper.getOtherSolicitor2Name(caseData)
            : NotificationHelper.getOtherSolicitor1Name(caseData);
        return getOrganisationName(orgId);
    }

    public boolean isHearingFeePaid(CaseData caseData) {
        PaymentDetails details = caseData.getHearingFeePaymentDetails();
        return details != null && PaymentStatus.SUCCESS.equals(details.getStatus());
    }
}
