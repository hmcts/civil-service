package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.PaymentStatus;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_APPLICANT_SOLICITOR_FOR_HEARING_FEE_AFTER_NOC;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;

@Service
@RequiredArgsConstructor
public class HearingFeeDueAfterNocNotificationHandler extends CallbackHandler
    implements NotificationData {

    private static final List<CaseEvent> EVENTS = List.of(
        NOTIFY_APPLICANT_SOLICITOR_FOR_HEARING_FEE_AFTER_NOC);
    public static final String TASK_ID = "HearingFeeDueNotifyApplicantSolicitorAfterNoc";
    private static final String REFERENCE_TEMPLATE = "NOC-hearing-fee-unpaid-applicant-notification-%s";

    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;
    private final OrganisationService organisationService;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(callbackKey(ABOUT_TO_SUBMIT), this::notifyApplicantSolicitorForHearingFeeUnpaidNoc
        );
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return TASK_ID;
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse notifyApplicantSolicitorForHearingFeeUnpaidNoc(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        if (!hearingAlreadyPaidCheck(caseData)) {
            notificationService.sendMail(
                caseData.getApplicantSolicitor1UserDetails().getEmail(),
                notificationsProperties.getHearingFeeUnpaidNoc(),
                addProperties(caseData),
                String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference())
            );
        }
        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        return Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
            LEGAL_ORG_NAME, getLegalOrganizationName(caseData.getApplicant1OrganisationPolicy()
                                                         .getOrganisation()
                                                         .getOrganisationID(), caseData),
            HEARING_DATE, formatLocalDate(caseData.getHearingDate(), DATE),
            COURT_LOCATION, caseData.getHearingLocation().getValue().getLabel(),
            HEARING_TIME, caseData.getHearingTimeHourMinute(),
            HEARING_FEE, String.valueOf(caseData.getHearingFee().formData()),
            HEARING_DUE_DATE, formatLocalDate(caseData.getHearingDueDate(), DATE)
        );
    }

    public String getLegalOrganizationName(String id, CaseData caseData) {
        Optional<Organisation> organisation = organisationService.findOrganisationById(id);
        if (organisation.isPresent()) {
            return organisation.get().getName();
        }
        return caseData.getApplicantSolicitor1ClaimStatementOfTruth().getName();
    }

    private boolean hearingAlreadyPaidCheck(CaseData caseData) {
        return nonNull(caseData.getHearingFeePaymentDetails())
            && caseData.getHearingFeePaymentDetails().getStatus().equals(PaymentStatus.SUCCESS);
    }
}
