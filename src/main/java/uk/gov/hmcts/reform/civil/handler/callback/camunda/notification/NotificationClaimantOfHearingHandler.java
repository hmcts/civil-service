package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackException;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.hearing.HearingNoticeList;
import uk.gov.hmcts.reform.civil.enums.hearing.ListingOrRelisting;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.notify.NotificationsSignatureConfiguration;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.service.hearingnotice.HearingNoticeCamundaService;
import uk.gov.hmcts.reform.civil.service.hearingnotice.HearingNoticeVariables;
import uk.gov.hmcts.reform.civil.service.hearings.HearingFeesService;
import uk.gov.hmcts.reform.civil.utils.NotificationUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_CLAIMANT_HEARING;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_CLAIMANT_HEARING_HMC;
import static uk.gov.hmcts.reform.civil.enums.PaymentStatus.SUCCESS;
import static uk.gov.hmcts.reform.civil.utils.HearingFeeUtils.calculateAndApplyFee;
import static uk.gov.hmcts.reform.civil.utils.HearingFeeUtils.calculateHearingDueDate;
import static uk.gov.hmcts.reform.civil.utils.HearingUtils.hearingFeeRequired;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.addAllFooterItems;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.buildPartiesReferencesEmailSubject;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getApplicantLegalOrganizationName;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.isEvent;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationClaimantOfHearingHandler extends CallbackHandler implements NotificationData {

    public static final String TASK_ID_CLAIMANT = "NotifyClaimantHearing";
    public static final String TASK_ID_CLAIMANT_HMC = "NotifyClaimantSolicitorHearing";

    private static final List<CaseEvent> EVENTS = List.of(NOTIFY_CLAIMANT_HEARING, NOTIFY_CLAIMANT_HEARING_HMC);
    private static final String REFERENCE_TEMPLATE_HEARING = "notification-of-hearing-%s";
    private static final String REFERENCE_TEMPLATE_HEARING_LIP = "notification-of-hearing-lip-%s";
    private static final String EVENT_NOT_FOUND_MESSAGE = "Callback handler received illegal event: %s";
    private static final String DATE_FORMAT = "dd-MM-yyyy";
    private static final String TIME_FORMAT = "hh:mma";

    private final NotificationService notificationService;
    private final HearingFeesService hearingFeesService;
    private final NotificationsProperties notificationsProperties;
    private final HearingNoticeCamundaService camundaService;
    private final OrganisationService organisationService;
    private final NotificationsSignatureConfiguration configuration;
    private final FeatureToggleService featureToggleService;

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::notifyClaimantHearing
        );
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        CaseEvent caseEvent = CaseEvent.valueOf(callbackParams.getRequest().getEventId());
        return switch (caseEvent) {
            case NOTIFY_CLAIMANT_HEARING -> TASK_ID_CLAIMANT;
            case NOTIFY_CLAIMANT_HEARING_HMC -> TASK_ID_CLAIMANT_HMC;
            default -> throw new CallbackException(String.format(EVENT_NOT_FOUND_MESSAGE, caseEvent));
        };
    }

    @Override
    public Map<String, String> addProperties(final CaseData caseData) {
        return Map.of();
    }

    private CallbackResponse notifyClaimantHearing(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        boolean isApplicantLip = isApplicantLip(caseData);
        boolean isHmcEvent = isEvent(callbackParams, NOTIFY_CLAIMANT_HEARING_HMC);

        if (isHmcEvent && !isApplicantLip) {
            Optional.ofNullable(caseData.getApplicantSolicitor1UserDetails())
                .map(IdamUserDetails::getEmail)
                .ifPresentOrElse(
                    recipient -> sendEmailHMC(caseData, recipient),
                    () -> logMissingRecipient(callbackParams)
                );
        } else {
            Optional.ofNullable(getRecipient(caseData, isApplicantLip))
                .ifPresentOrElse(
                    recipient -> sendEmail(
                        caseData,
                        recipient,
                        getReferenceTemplate(caseData, isApplicantLip, isHmcEvent),
                        isApplicantLip,
                        isHmcEvent),
                    () -> logMissingRecipient(callbackParams)
                );
        }

        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }

    private void logMissingRecipient(CallbackParams callbackParams) {
        log.warn("Skipping claimant hearing notification due to missing recipient email for case {}",
                 callbackParams.getRequest().getCaseDetails().getId());
    }

    private void sendEmail(CaseData caseData, String recipient, String reference, boolean isApplicantLip, boolean isHmcEvent) {
        notificationService.sendMail(
            recipient,
            getEmailTemplate(caseData, isApplicantLip),
            addPropertiesHearing(caseData, isHmcEvent, isApplicantLip),
            reference);
    }

    private void sendEmailHMC(CaseData caseData, String recipient) {
        HearingNoticeVariables camundaVars = camundaService.getProcessVariables(caseData.getBusinessProcess().getProcessInstanceId());

        boolean hearingFeeNotRequired = !hearingFeeRequired(camundaVars.getHearingType());
        boolean paymentSuccess = isPaymentSuccessful(caseData);

        String emailTemplate = (hearingFeeNotRequired || paymentSuccess)
            ? notificationsProperties.getHearingListedNoFeeClaimantLrTemplateHMC()
            : notificationsProperties.getHearingListedFeeClaimantLrTemplateHMC();

        String hearingId = String.format(REFERENCE_TEMPLATE_HEARING, camundaVars.getHearingId());
        notificationService.sendMail(recipient, emailTemplate, addPropertiesHMC(caseData), hearingId);
    }

    private Map<String, String> addPropertiesHearing(final CaseData caseData, boolean isHmcEvent, boolean isApplicantLip) {
        Map<String, String> map = new HashMap<>(Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
            HEARING_DATE, NotificationUtils.getFormattedHearingDate(caseData.getHearingDate()),
            HEARING_TIME, NotificationUtils.getFormattedHearingTime(getHearingTimeHM(caseData, isHmcEvent)),
            PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData),
            CASEMAN_REF, caseData.getLegacyCaseReference(),
            CLAIM_LEGAL_ORG_NAME_SPEC, isApplicantLip
                ? Optional.ofNullable(caseData.getApplicant1()).map(Party::getPartyName).orElse("")
                : getApplicantLegalOrganizationName(caseData, organisationService)
        ));

        if (!isApplicantLip) {
            String reference = (caseData.getSolicitorReferences() != null
                && caseData.getSolicitorReferences().getApplicantSolicitor1Reference() != null)
                ? caseData.getSolicitorReferences().getApplicantSolicitor1Reference() : "";

            if (!isPaymentSuccessful(caseData)) {
                map.put(HEARING_FEE, caseData.getHearingFee() != null && caseData.getHearingFee().formData() != null
                    ? String.valueOf(caseData.getHearingFee().formData()) : "£0.00");
                map.put(HEARING_DUE_DATE, caseData.getHearingDueDate() != null
                    ? NotificationUtils.getFormattedHearingDate(caseData.getHearingDueDate()) : "");
            }
            map.put(CLAIMANT_REFERENCE_NUMBER, reference);
        }
        addAllFooterItems(caseData, map, configuration, featureToggleService.isPublicQueryManagementEnabled(caseData));
        return map;
    }

    private Map<String, String> addPropertiesHMC(final CaseData caseData) {
        Fee fee = calculateAndApplyFee(hearingFeesService, caseData, caseData.getAssignedTrack());
        LocalDateTime hearingStartDateTime = camundaService
            .getProcessVariables(caseData.getBusinessProcess().getProcessInstanceId())
            .getHearingStartDateTime();

        LocalDate hearingDate = hearingStartDateTime.toLocalDate();
        LocalTime hearingTime = hearingStartDateTime.toLocalTime();

        Map<String, String> properties = new HashMap<>(Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
            HEARING_FEE, fee != null ? String.valueOf(fee.formData()) : "£0.00",
            HEARING_DATE, hearingDate.format(DateTimeFormatter.ofPattern(DATE_FORMAT)),
            HEARING_TIME, hearingTime.format(DateTimeFormatter.ofPattern(TIME_FORMAT))
                .replace("AM", "am")
                .replace("PM", "pm"),
            HEARING_DUE_DATE, calculateHearingDueDate(LocalDate.now(), hearingDate).format(DateTimeFormatter.ofPattern(DATE_FORMAT)),
            PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData),
            CLAIM_LEGAL_ORG_NAME_SPEC, getApplicantLegalOrganizationName(caseData, organisationService),
            CASEMAN_REF, caseData.getLegacyCaseReference()
        ));
        addAllFooterItems(caseData, properties, configuration, featureToggleService.isPublicQueryManagementEnabled(caseData));

        return properties;
    }

    private String getHearingTimeHM(CaseData caseData, boolean isHmcEvent) {
        return isHmcEvent
            ? camundaService.getProcessVariables(caseData.getBusinessProcess().getProcessInstanceId())
                .getHearingStartDateTime()
                .toLocalTime()
                .toString()
            : caseData.getHearingTimeHourMinute();
    }

    private boolean isApplicantLip(CaseData caseData) {
        return (YesOrNo.NO.equals(caseData.getApplicant1Represented()));
    }

    private String getRecipient(CaseData caseData, boolean isApplicantLip) {
        return isApplicantLip
            ? Optional.ofNullable(caseData.getApplicant1()).map(Party::getPartyEmail).orElse(null)
            : Optional.ofNullable(caseData.getApplicantSolicitor1UserDetails()).map(IdamUserDetails::getEmail).orElse(null);
    }

    private String getReferenceTemplate(CaseData caseData, boolean isApplicantLip, boolean isHmcEvent) {
        String hearingId = isHmcEvent
            ? camundaService.getProcessVariables(caseData.getBusinessProcess().getProcessInstanceId()).getHearingId()
            : caseData.getHearingReferenceNumber();

        return String.format(isApplicantLip ? REFERENCE_TEMPLATE_HEARING_LIP : REFERENCE_TEMPLATE_HEARING, hearingId);
    }

    private String getEmailTemplate(CaseData caseData, boolean isApplicantLip) {
        if (isApplicantLip) {
            return caseData.isClaimantBilingual()
                ? notificationsProperties.getHearingNotificationLipDefendantTemplateWelsh()
                : notificationsProperties.getHearingNotificationLipDefendantTemplate();
        }

        if (isPaymentSuccessful(caseData)
            || HearingNoticeList.OTHER.equals(caseData.getHearingNoticeList())
            || ListingOrRelisting.RELISTING.equals(caseData.getListingOrRelisting())) {
            return notificationsProperties.getHearingListedNoFeeClaimantLrTemplate();
        }
        return notificationsProperties.getHearingListedFeeClaimantLrTemplate();
    }

    private boolean isPaymentSuccessful(CaseData caseData) {
        return SUCCESS.equals(
            caseData.getHearingFeePaymentDetails() != null
                ? caseData.getHearingFeePaymentDetails().getStatus()
                : null);
    }

}
