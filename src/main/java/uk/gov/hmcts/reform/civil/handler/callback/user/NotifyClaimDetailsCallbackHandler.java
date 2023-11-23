package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.CertificateOfService;
import uk.gov.hmcts.reform.civil.model.DocumentWithRegex;
import uk.gov.hmcts.reform.civil.model.ServedDocumentFiles;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.civil.service.ExitSurveyContentService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;
import uk.gov.hmcts.reform.civil.utils.ElementUtils;
import uk.gov.hmcts.reform.civil.validation.interfaces.ParticularsOfClaimValidator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_DEFENDANT_OF_CLAIM_DETAILS;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE_TIME_AT;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDateTime;

@Service
@RequiredArgsConstructor
public class NotifyClaimDetailsCallbackHandler extends CallbackHandler implements ParticularsOfClaimValidator {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(NOTIFY_DEFENDANT_OF_CLAIM_DETAILS);

    private static final String CONFIRMATION_HEADER = "# Defendant notified%n## Claim number: %s";
    private static final String CONFIRMATION_SUMMARY = "<br />The defendant legal representative's organisation has"
            + " been notified of the claim details.%n%n"
            + "They must respond by %s. Your account will be updated and you will be sent an email.";
    private static final String CONFIRMATION_COS_HEADER =
            "# Certificate of Service - notify claim details successful %n## %s";
    private static final String CONFIRMATION_COS_SUMMARY =
            "<br /><h2 class=\"govuk-heading-m\">What happens next</h2> "
                    + "%n%n The defendant(s) must respond to the claim by %s. "
                    + "If the defendant(s) appoint a legal representative who respond online "
                    + "your account will be updated.%n%n"
                    + "If the defendant(s) do not wish for the claim to remain online and respond offline, "
                    + "the claim will be moved and will continue offline.%n%n "
                    + "This is a new online process - " + "you don't need to file any further documents to the court.";

    public static final String NOTIFICATION_ONE_PARTY_SUMMARY = "<br />Notification of claim details sent to "
            + "1 Defendant legal representative only.%n%n"
            + "Your claim will proceed offline.";

    public static final String WARNING_ONLY_NOTIFY_ONE_DEFENDANT_SOLICITOR =
            "Your claim will progress offline if you only notify one Defendant of the claim details.";

    public static final String DOC_SERVED_DATE_IN_FUTURE =
            "Date you served the documents must be today or in the past";

    public static final String DOC_SERVED_DATE_OLDER_THAN_14DAYS =
        "Date of Service should not be more than 14 days old";

    public static final String DOC_SERVED_MANDATORY =
            "Supporting evidence is required";

    public static final String BOTH_CERTIFICATE_SERVED_SAME_DATE =
        "Date of Service for both certificate must be the same";

    private final ExitSurveyContentService exitSurveyContentService;
    private final ObjectMapper objectMapper;
    private final Time time;
    private final DeadlinesCalculator deadlinesCalculator;
    private final FeatureToggleService featureToggleService;
    private final AssignCategoryId assignCategoryId;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
                callbackKey(ABOUT_TO_START), this::prepareDefendantSolicitorOptions,
                callbackKey(MID, "validateNotificationOption"), this::validateNotificationOption,
                callbackKey(MID, "particulars-of-claim"), this::validateParticularsOfClaim,
                callbackKey(MID, "validateCosNotifyClaimDetails1"), this::validateCoSDetailsDefendant1,
                callbackKey(MID, "validateCosNotifyClaimDetails2"), this::validateCoSDetailsDefendant2,
                callbackKey(ABOUT_TO_SUBMIT), this::submitClaim,
                callbackKey(SUBMITTED), this::buildConfirmationWithSolicitorOptions
        );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse submitClaim(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        LocalDateTime notificationDateTime = time.now();
        LocalDateTime currentDateTime = notificationDateTime;
        LocalDate notificationDate = notificationDateTime.toLocalDate();
        MultiPartyScenario multiPartyScenario = getMultiPartyScenario(caseData);

        if (featureToggleService.isCertificateOfServiceEnabled()) {
            caseData = saveCoSDetailsDoc(caseData, 1);
            caseData = saveCoSDetailsDoc(caseData, 2);

            if (multiPartyScenario == ONE_V_TWO_TWO_LEGAL_REP && isConfirmationForLip(caseData)) {
                multiPartyScenario = null;
            }
        }
        CaseData updatedCaseData;

        //Set R1 and R2 response deadlines, as both are represented
        if (multiPartyScenario == ONE_V_TWO_TWO_LEGAL_REP) {
            updatedCaseData = caseData.toBuilder()
                    .businessProcess(BusinessProcess.ready(NOTIFY_DEFENDANT_OF_CLAIM_DETAILS))
                    .claimDetailsNotificationDate(currentDateTime)
                    .respondent1ResponseDeadline(deadlinesCalculator.plus14DaysAt4pmDeadline(notificationDateTime))
                    .respondent2ResponseDeadline(deadlinesCalculator.plus14DaysAt4pmDeadline(notificationDateTime))
                    .nextDeadline(deadlinesCalculator.plus14DaysAt4pmDeadline(notificationDateTime).toLocalDate())
                    .claimDismissedDeadline(deadlinesCalculator.addMonthsToDateToNextWorkingDayAtMidnight(
                            6,
                            notificationDate
                    ))
                    .build();
        } else {
            //When CoS Enabled, this will return earlier date of CoS (also applicable for both LiP defendants)
            //these 2 fields are used for setting Deadlines
            notificationDateTime = getEarliestDateOfService(caseData);
            notificationDate = notificationDateTime.toLocalDate();

            CaseData.CaseDataBuilder builder = caseData.toBuilder()
                    .businessProcess(BusinessProcess.ready(NOTIFY_DEFENDANT_OF_CLAIM_DETAILS))
                    .claimDetailsNotificationDate(currentDateTime)
                    .nextDeadline(deadlinesCalculator.plus14DaysAt4pmDeadline(notificationDateTime).toLocalDate())
                    .claimDismissedDeadline(deadlinesCalculator.addMonthsToDateToNextWorkingDayAtMidnight(
                            6,
                            notificationDate
                    ));

            if (Objects.nonNull(caseData.getRespondent1())) {
                builder.respondent1ResponseDeadline(
                        deadlinesCalculator.plus14DaysAt4pmDeadline(notificationDateTime));
            }
            if (Objects.nonNull(caseData.getRespondent2())
                && YES.equals(caseData.getAddRespondent2())) {
                builder.respondent2ResponseDeadline(
                        deadlinesCalculator.plus14DaysAt4pmDeadline(notificationDateTime));
            }
            if (featureToggleService.isCertificateOfServiceEnabled() && areAnyRespondentsLitigantInPerson(caseData)) {
                if (Objects.nonNull(caseData.getCosNotifyClaimDetails1())) {
                    builder
                        .cosNotifyClaimDetails1(updateStatementOfTruthForLip(caseData.getCosNotifyClaimDetails1()))
                        .build();
                }
                if (Objects.nonNull(caseData.getCosNotifyClaimDetails2())) {
                    builder
                        .cosNotifyClaimDetails2(updateStatementOfTruthForLip(caseData.getCosNotifyClaimDetails2()))
                        .build();
                }
            }
            updatedCaseData = builder.build();
        }
        //assign category ids to documents uploaded as part of notify claim details
        assignNotifyParticularOfClaimCategoryIds(caseData);

        return AboutToStartOrSubmitCallbackResponse.builder()
                .data(updatedCaseData.toMap(objectMapper))
                .build();
    }

    private LocalDateTime getEarliestDateOfService(CaseData caseData) {
        LocalDateTime date = time.now();

        if (featureToggleService.isCertificateOfServiceEnabled()) {
            if (Objects.nonNull(caseData.getCosNotifyClaimDetails1())
                && Objects.nonNull(caseData.getCosNotifyClaimDetails1().getCosDateOfServiceForDefendant())) {
                LocalDateTime cosDate1 = caseData.getCosNotifyClaimDetails1()
                        .getCosDateOfServiceForDefendant().atTime(time.now().toLocalTime());
                if (cosDate1.isBefore(date)) {
                    date = cosDate1;
                }
            }
            if (Objects.nonNull(caseData.getCosNotifyClaimDetails2())
                && Objects.nonNull(caseData.getCosNotifyClaimDetails2().getCosDateOfServiceForDefendant())) {
                LocalDateTime cosDate2 = caseData.getCosNotifyClaimDetails2()
                        .getCosDateOfServiceForDefendant().atTime(time.now().toLocalTime());
                if (cosDate2.isBefore(date)) {
                    date = cosDate2;
                }
            }
        }
        return date;
    }

    private CaseData saveCoSDetailsDoc(CaseData caseData, int lipNumber) {
        CertificateOfService cosNotifyClaimDetails;
        if (lipNumber == 1) {
            cosNotifyClaimDetails = caseData.getCosNotifyClaimDetails1();
        } else {
            cosNotifyClaimDetails = caseData.getCosNotifyClaimDetails2();
        }
        if (Objects.nonNull(cosNotifyClaimDetails)) {
            cosNotifyClaimDetails.setCosDocSaved(YES);
            if (Objects.isNull(caseData.getServedDocumentFiles())) {
                caseData = caseData.toBuilder()
                        .servedDocumentFiles(ServedDocumentFiles.builder().build()).build();
            }
            if (Objects.isNull(caseData.getServedDocumentFiles().getOther())) {
                caseData.getServedDocumentFiles().setOther(new ArrayList<>());
            }
            List<Document> cosDoc = ElementUtils
                    .unwrapElements(cosNotifyClaimDetails
                            .getCosEvidenceDocument());
            caseData.getServedDocumentFiles().getOther()
                    .addAll(cosDoc.stream()
                            .map(document -> ElementUtils.element(new DocumentWithRegex(document)))
                            .collect(Collectors.toList()));
        }
        return caseData;
    }

    private SubmittedCallbackResponse buildConfirmationWithSolicitorOptions(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();

        if (caseData.getDefendantSolicitorNotifyClaimDetailsOptions() == null) {
            return buildConfirmation(callbackParams);
        }

        String formattedDeadline = formatLocalDateTime(caseData.getClaimDetailsNotificationDeadline(), DATE_TIME_AT);

        String confirmationText = getConfirmationBody(caseData);

        String body = format(confirmationText, formattedDeadline)
                + (isConfirmationForLip(caseData)
                ? "" : exitSurveyContentService.applicantSurvey());

        return SubmittedCallbackResponse.builder()
                .confirmationHeader(String.format(
                        getConfirmationHeader(caseData),
                        caseData.getLegacyCaseReference()
                ))
                .confirmationBody(body)
                .build();
    }

    private String getConfirmationBody(CaseData caseData) {
        String confirmationTextLR = isNotificationDetailsToBothSolicitors(caseData)
                || caseData.getDefendantSolicitorNotifyClaimDetailsOptions() == null
                ? CONFIRMATION_SUMMARY
                : NOTIFICATION_ONE_PARTY_SUMMARY;
        if (featureToggleService.isCertificateOfServiceEnabled()) {
            return isConfirmationForLip(caseData)
                    ? CONFIRMATION_COS_SUMMARY
                    : confirmationTextLR;
        } else {
            return confirmationTextLR;
        }
    }

    private String getConfirmationHeader(CaseData caseData) {
        if (featureToggleService.isCertificateOfServiceEnabled()) {
            return isConfirmationForLip(caseData)
                    ? CONFIRMATION_COS_HEADER
                    : CONFIRMATION_HEADER;
        } else {
            return CONFIRMATION_HEADER;
        }
    }

    private boolean isConfirmationForLip(CaseData caseData) {
        return (caseData.getDefendant1LIPAtClaimIssued() != null
                && caseData.getDefendant1LIPAtClaimIssued() == YES)
                || (caseData.getDefendant2LIPAtClaimIssued() != null
                && caseData.getDefendant2LIPAtClaimIssued() == YES);
    }

    private SubmittedCallbackResponse buildConfirmation(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        String formattedDeadline = formatLocalDateTime(caseData.getRespondent1ResponseDeadline(), DATE_TIME_AT);

        String body = format(getConfirmationBody(caseData), formattedDeadline)
                + (isConfirmationForLip(caseData)
                ? "" : exitSurveyContentService.applicantSurvey());

        return SubmittedCallbackResponse.builder()
                .confirmationHeader(String.format(
                        getConfirmationHeader(caseData),
                        caseData.getLegacyCaseReference()
                ))
                .confirmationBody(body)
                .build();
    }

    private CallbackResponse prepareDefendantSolicitorOptions(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();

        List<String> dynamicListOptions = new ArrayList<>();
        dynamicListOptions.add("Both");
        dynamicListOptions.add("Defendant One: " + caseData.getRespondent1().getPartyName());

        if (nonNull(caseData.getRespondent2())) {
            dynamicListOptions.add("Defendant Two: " + caseData.getRespondent2().getPartyName());
        }

        CaseData.CaseDataBuilder caseDataBuilder = caseData.toBuilder();
        caseDataBuilder.defendantSolicitorNotifyClaimDetailsOptions(DynamicList.fromList(dynamicListOptions));

        return AboutToStartOrSubmitCallbackResponse.builder()
                .data(caseDataBuilder.build().toMap(objectMapper))
                .build();
    }

    private CallbackResponse validateNotificationOption(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();

        ArrayList<String> warnings = new ArrayList<>();
        if (!isNotificationDetailsToBothSolicitors(caseData)) {
            warnings.add(WARNING_ONLY_NOTIFY_ONE_DEFENDANT_SOLICITOR);
        }

        CaseData.CaseDataBuilder caseDataBuilder = caseData.toBuilder();
        return AboutToStartOrSubmitCallbackResponse.builder()
                .data(caseDataBuilder.build().toMap(objectMapper))
                .warnings(warnings)
                .build();
    }

    protected boolean isNotificationDetailsToBothSolicitors(CaseData caseData) {
        return Optional.ofNullable(caseData.getDefendantSolicitorNotifyClaimDetailsOptions())
                .map(DynamicList::getValue)
                .map(DynamicListElement::getLabel)
                .orElse("")
                .equalsIgnoreCase("Both");
    }

    private CallbackResponse validateCoSDetailsDefendant1(final CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();

        ArrayList<String> errors = new ArrayList<>();
        CertificateOfService certificateOfService = caseData.getCosNotifyClaimDetails1();
        if (featureToggleService.isCertificateOfServiceEnabled()) {
            if (Objects.nonNull(caseData.getCosNotifyClaimDetails1())) {
                caseData.getCosNotifyClaimDetails1().setCosDocSaved(NO);
            }

            final String dateValidationErrorMessage = getServiceOfDateValidationMessage(
                caseData.getCosNotifyClaimDetails1());

            if (!dateValidationErrorMessage.isEmpty()) {
                errors.add(dateValidationErrorMessage);
            }
            if (Objects.nonNull(caseData.getCosNotifyClaimDetails1())
                    && isMandatoryDocMissing(caseData.getCosNotifyClaimDetails1())) {
                errors.add(DOC_SERVED_MANDATORY);
            }
        }
        CaseData.CaseDataBuilder caseDataBuilder = caseData.toBuilder();
        caseDataBuilder.cosNotifyClaimDetails1(certificateOfService.toBuilder()
                                                   .build());

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .errors(errors)
            .build();
    }

    private CallbackResponse validateCoSDetailsDefendant2(final CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CertificateOfService certificateOfServiceDef2 = caseData.getCosNotifyClaimDetails2();
        ArrayList<String> errors = new ArrayList<>();
        if (featureToggleService.isCertificateOfServiceEnabled()) {
            if (Objects.nonNull(caseData.getCosNotifyClaimDetails2())) {
                caseData.getCosNotifyClaimDetails2().setCosDocSaved(NO);
            }
            final String dateValidationErrorMessage = getServiceOfDateValidationMessage(
                caseData.getCosNotifyClaimDetails2());

            if (!dateValidationErrorMessage.isEmpty()) {
                errors.add(dateValidationErrorMessage);
            }

            if (isBothDefendantLip(caseData) && !isBothDefendantWithSameDateOfService(caseData)) {
                errors.add(BOTH_CERTIFICATE_SERVED_SAME_DATE);
            }

            if (Objects.nonNull(caseData.getCosNotifyClaimDetails2())
                && isMandatoryDocMissing(caseData.getCosNotifyClaimDetails2())) {
                errors.add(DOC_SERVED_MANDATORY);
            }
        }
        CaseData.CaseDataBuilder caseDataBuilder = caseData.toBuilder();
        caseDataBuilder.cosNotifyClaimDetails2(certificateOfServiceDef2.toBuilder()
                                                   .build());

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .errors(errors)
            .build();
    }

    private boolean isMandatoryDocMissing(CertificateOfService certificateOfService) {
        return Objects.isNull(certificateOfService.getCosEvidenceDocument());
    }

    private String getServiceOfDateValidationMessage(CertificateOfService certificateOfService) {
        final String errorMessage = "";
        if (Objects.nonNull(certificateOfService)) {
            if (isCosDefendantNotifyDateFutureDate(certificateOfService.getCosDateOfServiceForDefendant())) {
                return DOC_SERVED_DATE_IN_FUTURE;
            } else if (isCosDefendantNotifyDateOlderThan14Days(certificateOfService.getCosDateOfServiceForDefendant())) {
                return DOC_SERVED_DATE_OLDER_THAN_14DAYS;
            }
        }
        return errorMessage;
    }

    private boolean isCosDefendantNotifyDateFutureDate(LocalDate cosDateOfServiceForDefendant) {
        return time.now().toLocalDate().isBefore(cosDateOfServiceForDefendant);
    }

    private boolean isCosDefendantNotifyDateOlderThan14Days(LocalDate cosDateOfServiceForDefendant) {
        return time.now().isAfter(deadlinesCalculator.plus14DaysAt4pmDeadline(cosDateOfServiceForDefendant
                                                                                  .atTime(time.now().toLocalTime())));

    }

    private boolean isBothDefendantLip(CaseData caseData) {
        return (caseData.getDefendant1LIPAtClaimIssued() != null
            && caseData.getDefendant1LIPAtClaimIssued() == YES)
            && (caseData.getDefendant2LIPAtClaimIssued() != null
            && caseData.getDefendant2LIPAtClaimIssued() == YES);
    }

    private boolean isBothDefendantWithSameDateOfService(CaseData caseData) {
        if (Objects.nonNull(caseData.getCosNotifyClaimDetails1())
            && Objects.nonNull(caseData.getCosNotifyClaimDetails2())) {
            if (caseData.getCosNotifyClaimDetails1().getCosDateOfServiceForDefendant()
                .equals(caseData.getCosNotifyClaimDetails2().getCosDateOfServiceForDefendant())) {
                return true;
            }
        }
        return false;
    }

    private CertificateOfService updateStatementOfTruthForLip(CertificateOfService certificateOfService) {
        List<String> cosUISenderStatementOfTruthLabel = new ArrayList<>();
        cosUISenderStatementOfTruthLabel.add("CERTIFIED");
        return certificateOfService.toBuilder()
            .cosSenderStatementOfTruthLabel(cosUISenderStatementOfTruthLabel)
            .build();
    }

    private boolean areAnyRespondentsLitigantInPerson(CaseData caseData) {
        return caseData.getRespondent1Represented() == NO
            || (YES.equals(caseData.getAddRespondent2()) ? (caseData.getRespondent2Represented() == NO) : false);
    }

    private void assignNotifyParticularOfClaimCategoryIds(CaseData caseData) {
        assignCategoryId.assignCategoryIdToCollection(caseData.getServedDocumentFiles().getParticularsOfClaimDocument(),
                                                      Element::getValue, "particularsOfClaim");
        assignCategoryId.assignCategoryIdToCollection(caseData.getServedDocumentFiles().getMedicalReport(),
                                                      document -> document.getValue().getDocument(), "particularsOfClaim");
        assignCategoryId.assignCategoryIdToCollection(caseData.getServedDocumentFiles().getScheduleOfLoss(),
                                                      document -> document.getValue().getDocument(), "particularsOfClaim");
        assignCategoryId.assignCategoryIdToCollection(caseData.getServedDocumentFiles().getCertificateOfSuitability(),
                                                      document -> document.getValue().getDocument(), "particularsOfClaim");
        assignCategoryId.assignCategoryIdToCollection(caseData.getServedDocumentFiles().getOther(),
                                                      document -> document.getValue().getDocument(), "particularsOfClaim");
    }

}
