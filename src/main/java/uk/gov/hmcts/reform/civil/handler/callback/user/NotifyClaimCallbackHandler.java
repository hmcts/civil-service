package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.launchdarkly.FeatureToggleService;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.CertificateOfService;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.civil.service.ExitSurveyContentService;
import uk.gov.hmcts.reform.civil.service.Time;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.lang.String.format;
import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_DEFENDANT_OF_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE_TIME_AT;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDateTime;

@Service
@RequiredArgsConstructor
public class NotifyClaimCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(NOTIFY_DEFENDANT_OF_CLAIM);

    public static final String CONFIRMATION_SUMMARY = "<br />The defendant legal representative's organisation has "
        + "been notified and granted access to this claim.%n%n"
        + "You must notify the defendant with the claim details by %s";
    public static final String CONFIRMATION_NOTIFICATION_ONE_PARTY_SUMMARY = "<br />Notification of claim sent to "
        + "1 Defendant legal representative only.%n%n"
        + "Your claim will proceed offline.";

    public static final String WARNING_ONLY_NOTIFY_ONE_DEFENDANT_SOLICITOR =
        "Your claim will progress offline if you only notify one Defendant of the claim details.";
    private static final String CONFIRMATION_COS_HEADER = "# Certificate of Service - notify claim successful %n## %s";

    private static final String CONFIRMATION_SUMMARY_COS = "<br /><h2 class=\"govuk-heading-m\">What happens next</h2>"
        + " %n%n You must serve the claim details and complete the certificate of service notify claim details next"
        + " step by 4:00pm on %s.%n%nThis is a new online process - you don't need to file any further documents"
        + " to the court";

    public static final String DOC_SERVED_DATE_IN_FUTURE =
        "Date you served the documents must be today or in the past";
    public static final String ERROR_PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT = "There is a problem"
        + "\n"
        + "This action cannot currently be performed because it has either already"
        + " been completed or another action must be completed first.";
    private final ExitSurveyContentService exitSurveyContentService;
    private final ObjectMapper objectMapper;
    private final DeadlinesCalculator deadlinesCalculator;
    private final Time time;
    private final FeatureToggleService featureToggleService;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_START), this::prepareDefendantSolicitorOptions,
            callbackKey(MID, "validateNotificationOption"), this::validateNotificationOption,
            callbackKey(MID, "validateCosNotifyClaimDef1"), this::validateCosDefendant1,
            callbackKey(MID, "validateCosNotifyClaimDef2"), this::validateCosDefendant2,
            callbackKey(ABOUT_TO_SUBMIT), this::submitClaim,
            callbackKey(SUBMITTED), this::buildConfirmationWithSolicitorOptions
        );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    //The field `defendantSolicitorNotifyClaimOptions` will only show when both defendants are represented
    private CallbackResponse prepareDefendantSolicitorOptions(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();

        if (!featureToggleService.isCertificateOfServiceEnabled() && areAnyRespondentsLitigantInPerson(caseData)) {
            return AboutToStartOrSubmitCallbackResponse.builder()
                .errors(List.of(ERROR_PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT))
                .build();
        }
        List<String> dynamicListOptions = new ArrayList<>();
        dynamicListOptions.add("Both");
        dynamicListOptions.add("Defendant One: " + caseData.getRespondent1().getPartyName());

        if (nonNull(caseData.getRespondent2())) {
            dynamicListOptions.add("Defendant Two: " + caseData.getRespondent2().getPartyName());
        }

        //build options for field (Default Value & List Options), add to case data
        CaseData.CaseDataBuilder caseDataBuilder = caseData.toBuilder();
        caseDataBuilder.defendantSolicitorNotifyClaimOptions(DynamicList.fromList(dynamicListOptions));

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }

    private CallbackResponse validateNotificationOption(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();

        ArrayList<String> warnings = new ArrayList<>();
        if (!ONE_V_ONE.equals(MultiPartyScenario.getMultiPartyScenario(caseData))
            && !notifyBothRespondentSolicitors(caseData)) {
            warnings.add(WARNING_ONLY_NOTIFY_ONE_DEFENDANT_SOLICITOR);
        }

        CaseData.CaseDataBuilder caseDataBuilder = caseData.toBuilder();
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .warnings(warnings)
            .build();
    }

    private CallbackResponse validateCosDefendant1(final CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();

        ArrayList<String> errors = new ArrayList<>();
        if (Objects.nonNull(caseData.getCosNotifyClaimDefendant1())
            && isCosDefendantNotifyDateFutureDate(caseData.getCosNotifyClaimDefendant1())) {

            errors.add(DOC_SERVED_DATE_IN_FUTURE);
        }

        CaseData.CaseDataBuilder caseDataBuilder = caseData.toBuilder();
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .errors(errors)
            .build();
    }

    private CallbackResponse validateCosDefendant2(final CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();

        ArrayList<String> errors = new ArrayList<>();
        if ((Objects.nonNull(caseData.getCosNotifyClaimDefendant2()))
            && isCosDefendantNotifyDateFutureDate(caseData.getCosNotifyClaimDefendant2())) {

            errors.add(DOC_SERVED_DATE_IN_FUTURE);
        }

        CaseData.CaseDataBuilder caseDataBuilder = caseData.toBuilder();
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .errors(errors)
            .build();
    }

    private CallbackResponse submitClaim(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        LocalDateTime claimNotificationDate = time.now();

        CaseData.CaseDataBuilder caseDataBuilder = caseData.toBuilder()
            .businessProcess(BusinessProcess.ready(NOTIFY_DEFENDANT_OF_CLAIM))
            .claimNotificationDate(claimNotificationDate);

        // set organisation policy after removing it in claim issue
        // workaround for hiding cases in CAA list before case notify
        setOrganisationPolicy(caseData, caseDataBuilder);
        LocalDateTime claimDetailsNotificationDeadline;
        if (featureToggleService.isCertificateOfServiceEnabled() && areAnyRespondentsLitigantInPerson(caseData)) {
            claimDetailsNotificationDeadline = getDeadline(getServiceDate(caseData));
        } else {
            claimDetailsNotificationDeadline = getDeadline(claimNotificationDate);
        }

        if (claimDetailsNotificationDeadline.isAfter(caseData.getClaimNotificationDeadline())
            || claimDetailsNotificationDeadline.isEqual(caseData.getClaimNotificationDeadline())) {
            claimDetailsNotificationDeadline = caseData.getClaimNotificationDeadline();
        }

        caseDataBuilder
            .claimDetailsNotificationDeadline(claimDetailsNotificationDeadline)
            .nextDeadline(claimDetailsNotificationDeadline.toLocalDate());

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }

    private void setOrganisationPolicy(CaseData caseData, CaseData.CaseDataBuilder caseDataBuilder) {
        if (caseData.getRespondent1OrganisationIDCopy() != null) {
            caseDataBuilder.respondent1OrganisationPolicy(
                caseData.getRespondent1OrganisationPolicy().toBuilder()
                    .organisation(Organisation.builder()
                                      .organisationID(caseData.getRespondent1OrganisationIDCopy())
                                      .build())
                    .build()
            );
        }

        if (caseData.getRespondent2OrganisationIDCopy() != null) {
            caseDataBuilder.respondent2OrganisationPolicy(
                caseData.getRespondent2OrganisationPolicy().toBuilder()
                    .organisation(Organisation.builder()
                                      .organisationID(caseData.getRespondent2OrganisationIDCopy())
                                      .build())
                    .build()
            );
        }
    }

    private LocalDateTime getDeadline(LocalDateTime claimNotificationDate) {
        return deadlinesCalculator.plus14DaysAt4pmDeadline(claimNotificationDate);
    }

    private SubmittedCallbackResponse buildConfirmation(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();

        String header = "";
        String body = "";

        if (featureToggleService.isCertificateOfServiceEnabled() && isConfirmationForLip(caseData)) {
            String formattedDeadline = formatLocalDate(caseData.getClaimDetailsNotificationDeadline().toLocalDate(),
                                                       DATE);
            header = String.format(CONFIRMATION_COS_HEADER, caseData.getLegacyCaseReference());
            body = format(CONFIRMATION_SUMMARY_COS, formattedDeadline) + exitSurveyContentService.applicantSurvey();

        } else {
            String formattedDeadline = formatLocalDateTime(caseData
                                                               .getClaimDetailsNotificationDeadline(), DATE_TIME_AT);
            header = String.format("# Notification of claim sent%n## Claim number: %s",
                                   caseData.getLegacyCaseReference());
            body = format(CONFIRMATION_SUMMARY, formattedDeadline) + exitSurveyContentService.applicantSurvey();
        }

        return SubmittedCallbackResponse.builder()
            .confirmationHeader(header)
            .confirmationBody(body)
            .build();
    }

    private SubmittedCallbackResponse buildConfirmationWithSolicitorOptions(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();

        if (caseData.getDefendantSolicitorNotifyClaimOptions() == null) {
            return buildConfirmation(callbackParams);
        }

        String formattedDeadline = formatLocalDateTime(caseData.getClaimDetailsNotificationDeadline(), DATE_TIME_AT);

        String confirmationText = (ONE_V_ONE.equals(MultiPartyScenario.getMultiPartyScenario(caseData))
            || notifyBothRespondentSolicitors(caseData))
            ? CONFIRMATION_SUMMARY
            : CONFIRMATION_NOTIFICATION_ONE_PARTY_SUMMARY;

        String body = format(confirmationText, formattedDeadline)
            + exitSurveyContentService.applicantSurvey();

        return SubmittedCallbackResponse.builder()
            .confirmationHeader(String.format(
                "# Notification of claim sent%n## Claim number: %s",
                caseData.getLegacyCaseReference()
            ))
            .confirmationBody(body)
            .build();
    }

    protected boolean notifyBothRespondentSolicitors(CaseData caseData) {
        return Objects.equals("Both", caseData.getDefendantSolicitorNotifyClaimOptions().getValue().getLabel());
    }

    private boolean isCosDefendantNotifyDateFutureDate(CertificateOfService cosNotifyClaimDefendant) {
        return LocalDate.now().isBefore(cosNotifyClaimDefendant.getCosDateOfServiceForDefendant());
    }

    private boolean isConfirmationForLip(CaseData caseData) {
        return (caseData.getDefendant1LIPAtClaimIssued() != null
            && caseData.getDefendant1LIPAtClaimIssued() == YesOrNo.YES)
            || (caseData.getDefendant2LIPAtClaimIssued() != null
            && caseData.getDefendant2LIPAtClaimIssued() == YesOrNo.YES);

    }

    private boolean areAnyRespondentsLitigantInPerson(CaseData caseData) {
        return caseData.getRespondent1Represented() == NO
            || (YES.equals(caseData.getAddRespondent2()) ? (caseData.getRespondent2Represented() == NO) : false);
    }

    private LocalDateTime getServiceDate(CaseData caseData) {
        LocalDateTime date = time.now();

        if (Objects.nonNull(caseData.getCosNotifyClaimDefendant1())
            && Objects.nonNull(caseData
                                   .getCosNotifyClaimDefendant1().getCosDateOfServiceForDefendant())) {
            LocalDateTime cosDate1 = caseData.getCosNotifyClaimDefendant1()
                .getCosDateOfServiceForDefendant().atStartOfDay();
            if (cosDate1.isBefore(date)) {
                date = cosDate1;
            }
        }

        if (Objects.nonNull(caseData.getCosNotifyClaimDefendant2())
            && Objects.nonNull(caseData
                                   .getCosNotifyClaimDefendant2().getCosDateOfServiceForDefendant())) {
            LocalDateTime cosDate2 = caseData.getCosNotifyClaimDefendant2()
                .getCosDateOfServiceForDefendant().atStartOfDay();
            if (cosDate2.isBefore(date)) {
                date = cosDate2;
            }
        }
        return date;
    }
}
