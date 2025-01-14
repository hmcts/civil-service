package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CaseAssignmentApi;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.config.CrossAccessUserConfiguration;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.CertificateOfService;
import uk.gov.hmcts.reform.civil.model.DocumentWithRegex;
import uk.gov.hmcts.reform.civil.model.ServedDocumentFiles;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.civil.service.ExitSurveyContentService;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.RoleAssignmentsService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;
import uk.gov.hmcts.reform.civil.utils.ElementUtils;
import uk.gov.hmcts.reform.civil.utils.ServiceOfDateValidationMessageUtils;
import uk.gov.hmcts.reform.civil.validation.interfaces.ParticularsOfClaimValidator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static java.lang.String.format;
import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
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

@Slf4j
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

    public static final String DOC_SERVED_MANDATORY =
            "Supporting evidence is required";

    public static final String BOTH_CERTIFICATE_SERVED_SAME_DATE =
        "The date of service for defendant 1 and defendant 2 must be the same";

    public static final LocalTime END_OF_BUSINESS_DAY = LocalTime.of(16, 0, 0);

    private final ExitSurveyContentService exitSurveyContentService;
    private final ObjectMapper objectMapper;
    private final Time time;
    private final DeadlinesCalculator deadlinesCalculator;
    private final ServiceOfDateValidationMessageUtils serviceOfDateValidationMessageUtils;
    private final FeatureToggleService featureToggleService;
    private final AssignCategoryId assignCategoryId;
    private final CrossAccessUserConfiguration crossAccessUserConfiguration;
    private final UserService userService;
    private final CaseAssignmentApi caseAssignmentApi;
    private final AuthTokenGenerator authTokenGenerator;
    private final RoleAssignmentsService roleAssignmentsService;

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
        final LocalDateTime currentDateTime = notificationDateTime;
        MultiPartyScenario multiPartyScenario = getMultiPartyScenario(caseData);

        caseData = saveCoSDetailsDoc(caseData, 1);
        caseData = saveCoSDetailsDoc(caseData, 2);

        if (multiPartyScenario == ONE_V_TWO_TWO_LEGAL_REP && isConfirmationForLip(caseData)) {
            multiPartyScenario = null;
        }
        CaseData updatedCaseData;
        LocalDate notificationDate = notificationDateTime.toLocalDate();

        //Set R1 and R2 response deadlines, as both are represented
        if (multiPartyScenario == ONE_V_TWO_TWO_LEGAL_REP) {
            updatedCaseData = caseData.toBuilder()
                    .businessProcess(BusinessProcess.ready(NOTIFY_DEFENDANT_OF_CLAIM_DETAILS))
                    .claimDetailsNotificationDate(currentDateTime)
                    .addLegalRepDeadlineRes1(deadlinesCalculator.plus14DaysDeadline(notificationDateTime))
                    .addLegalRepDeadlineRes2(deadlinesCalculator.plus14DaysDeadline(notificationDateTime))
                    .respondent1ResponseDeadline(deadlinesCalculator.plus14DaysDeadline(notificationDateTime))
                    .respondent2ResponseDeadline(deadlinesCalculator.plus14DaysDeadline(notificationDateTime))
                    .nextDeadline(deadlinesCalculator.plus14DaysDeadline(notificationDateTime).toLocalDate())
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
                    .nextDeadline(deadlinesCalculator.plus14DaysDeadline(notificationDateTime).toLocalDate())
                    .claimDismissedDeadline(deadlinesCalculator.addMonthsToDateToNextWorkingDayAtMidnight(
                            6,
                            notificationDate
                    ));

            if (Objects.nonNull(caseData.getRespondent1())) {
                builder.respondent1ResponseDeadline(
                        deadlinesCalculator.plus14DaysDeadline(notificationDateTime));
            }

            if (Objects.nonNull(caseData.getRespondent1())
                && NO.equals(caseData.getRespondent1Represented())) {
                builder.addLegalRepDeadlineRes1(deadlinesCalculator.plus14DaysDeadline(notificationDateTime));
            }

            if (Objects.nonNull(caseData.getRespondent2())
                && YES.equals(caseData.getAddRespondent2())) {
                builder.respondent2ResponseDeadline(
                        deadlinesCalculator.plus14DaysDeadline(notificationDateTime));
            }

            if (Objects.nonNull(caseData.getRespondent2())
                   && YES.equals(caseData.getAddRespondent2())
                && NO.equals(caseData.getRespondent2Represented())) {
                builder.addLegalRepDeadlineRes2(deadlinesCalculator.plus14DaysDeadline(notificationDateTime));
            }

            if (areAnyRespondentsLitigantInPerson(caseData))  {
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
        LocalDateTime deemedDate1 = null;
        LocalDateTime deemedDate2 = null;

        if (Objects.nonNull(caseData.getCosNotifyClaimDetails1())
            && Objects.nonNull(caseData.getCosNotifyClaimDetails1().getCosDateDeemedServedForDefendant())) {
            deemedDate1 = caseData.getCosNotifyClaimDetails1()
                .getCosDateDeemedServedForDefendant().atTime(time.now().toLocalTime());
        }

        if (Objects.nonNull(caseData.getCosNotifyClaimDetails2())
            && Objects.nonNull(caseData.getCosNotifyClaimDetails2().getCosDateDeemedServedForDefendant())) {
            deemedDate2 = caseData.getCosNotifyClaimDetails2()
                .getCosDateDeemedServedForDefendant().atTime(time.now().toLocalTime());
        }

        if (deemedDate1 != null && deemedDate2 != null) {
            return deemedDate1.isBefore(deemedDate2) ? deemedDate1 : deemedDate2;
        } else if (deemedDate1 != null) {
            return deemedDate1;
        } else if (deemedDate2 != null) {
            return deemedDate2;
        } else {
            // If both deemedDate1 and deemedDate2 are null, use the current date and time
            return date;
        }
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
                            .toList());
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
        return isConfirmationForLip(caseData)
                ? CONFIRMATION_COS_SUMMARY
                : confirmationTextLR;
    }

    private String getConfirmationHeader(CaseData caseData) {
        return isConfirmationForLip(caseData)
                ? CONFIRMATION_COS_HEADER
                : CONFIRMATION_HEADER;
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
        log.info("GET ROLES");
        List<String> roleType = new ArrayList<>();
        List<String> roleName = new ArrayList<>();
        roleType.add("CASE");
        roleName.add("[APPLICANTSOLICITORONE]");
        CaseData caseData = callbackParams.getCaseData();
        String caseId = caseData.getCcdCaseReference().toString();
        var roleAssignmentResponse = roleAssignmentsService.queryRoleAssignmentsByCaseIdAndRole(caseId,
                                                                                                 roleType,
                                                                                                 roleName,
                                                                                                 callbackParams.getParams().get(BEARER_TOKEN).toString());
        log.info("GET ROLES case id roleAssignmentResponse:  {}", roleAssignmentResponse.getRoleAssignmentResponse());

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
        if (Objects.nonNull(caseData.getCosNotifyClaimDetails1())) {
            caseData.getCosNotifyClaimDetails1().setCosDocSaved(NO);
        }

        List<String> dateValidationErrorMessages = serviceOfDateValidationMessageUtils
            .getServiceOfDateValidationMessages(caseData.getCosNotifyClaimDetails1());
        errors.addAll(dateValidationErrorMessages);

        if (Objects.nonNull(caseData.getCosNotifyClaimDetails1())
                && isMandatoryDocMissing(caseData.getCosNotifyClaimDetails1())) {
            errors.add(DOC_SERVED_MANDATORY);
        }
        CertificateOfService certificateOfService = caseData.getCosNotifyClaimDetails1();
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
        ArrayList<String> errors = new ArrayList<>();
        if (Objects.nonNull(caseData.getCosNotifyClaimDetails2())) {
            caseData.getCosNotifyClaimDetails2().setCosDocSaved(NO);
        }
        List<String> dateValidationErrorMessages = serviceOfDateValidationMessageUtils.getServiceOfDateValidationMessages(caseData.getCosNotifyClaimDetails2());
        errors.addAll(dateValidationErrorMessages);

        if (isBothDefendantLip(caseData) && !isBothDefendantWithSameDateOfService(caseData)) {
            errors.add(BOTH_CERTIFICATE_SERVED_SAME_DATE);
        }

        if (Objects.nonNull(caseData.getCosNotifyClaimDetails2())
            && isMandatoryDocMissing(caseData.getCosNotifyClaimDetails2())) {
            errors.add(DOC_SERVED_MANDATORY);
        }
        CertificateOfService certificateOfServiceDef2 = caseData.getCosNotifyClaimDetails2();
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

    private boolean isBothDefendantLip(CaseData caseData) {
        return (caseData.getDefendant1LIPAtClaimIssued() != null
            && caseData.getDefendant1LIPAtClaimIssued() == YES)
            && (caseData.getDefendant2LIPAtClaimIssued() != null
            && caseData.getDefendant2LIPAtClaimIssued() == YES);
    }

    private boolean isBothDefendantWithSameDateOfService(CaseData caseData) {
        if (Objects.nonNull(caseData.getCosNotifyClaimDetails1())
            && Objects.nonNull(caseData.getCosNotifyClaimDetails2())) {
            return caseData.getCosNotifyClaimDetails1().getCosDateDeemedServedForDefendant()
                .equals(caseData.getCosNotifyClaimDetails2().getCosDateDeemedServedForDefendant());
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
            || (YES.equals(caseData.getAddRespondent2()) && (caseData.getRespondent2Represented() == NO));
    }

    static final String PARTICULARS_OF_CLAIM = "particularsOfClaim";

    private void assignNotifyParticularOfClaimCategoryIds(CaseData caseData) {
        assignCategoryId.assignCategoryIdToCollection(caseData.getServedDocumentFiles().getParticularsOfClaimDocument(),
                                                      Element::getValue, PARTICULARS_OF_CLAIM
        );
        assignCategoryId.assignCategoryIdToCollection(caseData.getServedDocumentFiles().getMedicalReport(),
                                                      document -> document.getValue().getDocument(),
                                                      PARTICULARS_OF_CLAIM
        );
        assignCategoryId.assignCategoryIdToCollection(caseData.getServedDocumentFiles().getScheduleOfLoss(),
                                                      document -> document.getValue().getDocument(),
                                                      PARTICULARS_OF_CLAIM
        );
        assignCategoryId.assignCategoryIdToCollection(caseData.getServedDocumentFiles().getCertificateOfSuitability(),
                                                      document -> document.getValue().getDocument(),
                                                      PARTICULARS_OF_CLAIM
        );
        assignCategoryId.assignCategoryIdToCollection(caseData.getServedDocumentFiles().getOther(),
                                                      document -> document.getValue().getDocument(),
                                                      PARTICULARS_OF_CLAIM
        );
    }

}
