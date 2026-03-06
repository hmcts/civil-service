package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
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
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;
import uk.gov.hmcts.reform.civil.utils.ElementUtils;
import uk.gov.hmcts.reform.civil.utils.ServiceOfDateValidationMessageUtils;
import uk.gov.hmcts.reform.civil.validation.interfaces.ParticularsOfClaimValidator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

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
    private static final String PARTICULARS_OF_CLAIM = "particularsOfClaim";

    private final ExitSurveyContentService exitSurveyContentService;
    private final ObjectMapper objectMapper;
    private final Time time;
    private final DeadlinesCalculator deadlinesCalculator;
    private final ServiceOfDateValidationMessageUtils serviceOfDateValidationMessageUtils;
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
        LocalDateTime callbackReceivedAt = time.now();
        MultiPartyScenario multiPartyScenario = getMultiPartyScenario(caseData);

        saveCoSDetailsDoc(caseData, 1);
        saveCoSDetailsDoc(caseData, 2);

        // LiP involvement switches this event to date-based deadlines even in 1v2 two-LR scenarios.
        boolean useTwoRepAlignedDeadlineFlow = multiPartyScenario == ONE_V_TWO_TWO_LEGAL_REP
            && !isConfirmationForLip(caseData);

        if (useTwoRepAlignedDeadlineFlow) {
            setDeadlinesForFullyRepresentedFlow(caseData, callbackReceivedAt);
        } else {
            setDeadlinesFromServiceDateFlow(caseData, callbackReceivedAt);
        }
        // Assign category IDs to documents uploaded as part of 'notify claim details'.
        assignNotifyParticularOfClaimCategoryIds(caseData);

        return AboutToStartOrSubmitCallbackResponse.builder()
                .data(caseData.toMap(objectMapper))
                .build();
    }

    // Used when both respondents are represented and receive aligned deadlines from callback time.
    private void setDeadlinesForFullyRepresentedFlow(CaseData caseData, LocalDateTime callbackReceivedAt) {
        LocalDateTime plus14Days = deadlinesCalculator.plus14DaysDeadline(callbackReceivedAt);
        applyCommonNotificationFields(caseData, callbackReceivedAt, plus14Days, callbackReceivedAt.toLocalDate());

        caseData.setAddLegalRepDeadlineRes1(plus14Days);
        caseData.setAddLegalRepDeadlineRes2(plus14Days);
        caseData.setRespondent1ResponseDeadline(plus14Days);
        caseData.setRespondent2ResponseDeadline(plus14Days);
    }

    // Used when deadlines are based on the earliest deemed service date (including CoS/LiP flows).
    private void setDeadlinesFromServiceDateFlow(CaseData caseData, LocalDateTime callbackReceivedAt) {
        LocalDateTime effectiveServiceDateTime = getEarliestDateOfService(caseData);
        LocalDateTime plus14Days = deadlinesCalculator.plus14DaysDeadline(effectiveServiceDateTime);
        applyCommonNotificationFields(caseData, callbackReceivedAt, plus14Days, effectiveServiceDateTime.toLocalDate());

        boolean hasRespondent1 = Objects.nonNull(caseData.getRespondent1());
        boolean hasRespondent2 = Objects.nonNull(caseData.getRespondent2())
            && YES.equals(caseData.getAddRespondent2());

        if (hasRespondent1) {
            caseData.setRespondent1ResponseDeadline(plus14Days);
        }

        if (hasRespondent1 && NO.equals(caseData.getRespondent1Represented())) {
            caseData.setAddLegalRepDeadlineRes1(plus14Days);
        }

        if (hasRespondent2) {
            caseData.setRespondent2ResponseDeadline(plus14Days);
        }

        if (hasRespondent2 && NO.equals(caseData.getRespondent2Represented())) {
            caseData.setAddLegalRepDeadlineRes2(plus14Days);
        }

        updateLipStatementOfTruthLabels(caseData);
    }

    private void applyCommonNotificationFields(
        CaseData caseData,
        LocalDateTime callbackReceivedAt,
        LocalDateTime responseDeadline,
        LocalDate dismissedDeadlineDate
    ) {
        caseData.setBusinessProcess(BusinessProcess.ready(NOTIFY_DEFENDANT_OF_CLAIM_DETAILS));
        caseData.setClaimDetailsNotificationDate(callbackReceivedAt);
        caseData.setNextDeadline(responseDeadline.toLocalDate());
        caseData.setClaimDismissedDeadline(
            deadlinesCalculator.addMonthsToDateToNextWorkingDayAtMidnight(6, dismissedDeadlineDate)
        );
    }

    private void updateLipStatementOfTruthLabels(CaseData caseData) {
        if (!areAnyRespondentsLitigantInPerson(caseData)) {
            return;
        }
        if (Objects.nonNull(caseData.getCosNotifyClaimDetails1())) {
            caseData.setCosNotifyClaimDetails1(updateStatementOfTruthForLip(caseData.getCosNotifyClaimDetails1()));
        }
        if (Objects.nonNull(caseData.getCosNotifyClaimDetails2())) {
            caseData.setCosNotifyClaimDetails2(updateStatementOfTruthForLip(caseData.getCosNotifyClaimDetails2()));
        }
    }

    private LocalDateTime getEarliestDateOfService(CaseData caseData) {
        LocalDateTime currentDateTime = time.now();

        return Stream.of(caseData.getCosNotifyClaimDetails1(), caseData.getCosNotifyClaimDetails2())
            .filter(Objects::nonNull)
            .map(CertificateOfService::getCosDateDeemedServedForDefendant)
            .filter(Objects::nonNull)
            .map(date -> date.atTime(currentDateTime.toLocalTime()))
            .min(LocalDateTime::compareTo)
            .orElse(currentDateTime);
    }

    private void saveCoSDetailsDoc(CaseData caseData, int lipNumber) {
        CertificateOfService certificateOfService;
        if (lipNumber == 1) {
            certificateOfService = caseData.getCosNotifyClaimDetails1();
        } else {
            certificateOfService = caseData.getCosNotifyClaimDetails2();
        }
        if (Objects.nonNull(certificateOfService)) {
            certificateOfService.setCosDocSaved(YES);
            if (Objects.isNull(caseData.getServedDocumentFiles())) {
                caseData.setServedDocumentFiles(new ServedDocumentFiles());
            }
            if (Objects.isNull(caseData.getServedDocumentFiles().getOther())) {
                caseData.getServedDocumentFiles().setOther(new ArrayList<>());
            }
            List<Document> cosDocuments = ElementUtils
                    .unwrapElements(certificateOfService
                            .getCosEvidenceDocument());
            caseData.getServedDocumentFiles().getOther()
                    .addAll(cosDocuments.stream()
                            .map(document -> ElementUtils.element(new DocumentWithRegex(document)))
                            .toList());
        }
    }

    private SubmittedCallbackResponse buildConfirmationWithSolicitorOptions(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();

        if (caseData.getDefendantSolicitorNotifyClaimDetailsOptions() == null) {
            return buildConfirmation(callbackParams);
        }

        String formattedDeadline = formatLocalDateTime(caseData.getClaimDetailsNotificationDeadline(), DATE_TIME_AT);
        String confirmationBody = format(getConfirmationBody(caseData), formattedDeadline) + getSurveySuffix(caseData);
        return buildSubmittedConfirmation(caseData, confirmationBody);
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
        String confirmationBody = format(getConfirmationBody(caseData), formattedDeadline) + getSurveySuffix(caseData);
        return buildSubmittedConfirmation(caseData, confirmationBody);
    }

    private SubmittedCallbackResponse buildSubmittedConfirmation(CaseData caseData, String confirmationBody) {
        return SubmittedCallbackResponse.builder()
            .confirmationHeader(format(getConfirmationHeader(caseData), caseData.getLegacyCaseReference()))
            .confirmationBody(confirmationBody)
            .build();
    }

    private String getSurveySuffix(CaseData caseData) {
        return isConfirmationForLip(caseData) ? "" : exitSurveyContentService.applicantSurvey();
    }

    private CallbackResponse prepareDefendantSolicitorOptions(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();

        List<String> dynamicListOptions = new ArrayList<>();
        dynamicListOptions.add("Both");
        dynamicListOptions.add("Defendant One: " + caseData.getRespondent1().getPartyName());

        if (nonNull(caseData.getRespondent2())) {
            dynamicListOptions.add("Defendant Two: " + caseData.getRespondent2().getPartyName());
        }

        caseData.setDefendantSolicitorNotifyClaimDetailsOptions(DynamicList.fromList(dynamicListOptions));

        return AboutToStartOrSubmitCallbackResponse.builder()
                .data(caseData.toMap(objectMapper))
                .build();
    }

    private CallbackResponse validateNotificationOption(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();

        ArrayList<String> warnings = new ArrayList<>();
        if (!isNotificationDetailsToBothSolicitors(caseData)) {
            warnings.add(WARNING_ONLY_NOTIFY_ONE_DEFENDANT_SOLICITOR);
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
                .data(caseData.toMap(objectMapper))
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

        if (Objects.nonNull(caseData.getCosNotifyClaimDetails1())) {
            caseData.getCosNotifyClaimDetails1().setCosDocSaved(NO);
        }

        List<String> dateValidationErrorMessages = serviceOfDateValidationMessageUtils
            .getServiceOfDateValidationMessages(caseData.getCosNotifyClaimDetails1());

        ArrayList<String> errors = new ArrayList<>(dateValidationErrorMessages);

        if (Objects.nonNull(caseData.getCosNotifyClaimDetails1())
                && isMandatoryDocMissing(caseData.getCosNotifyClaimDetails1())) {
            errors.add(DOC_SERVED_MANDATORY);
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseData.toMap(objectMapper))
            .errors(errors)
            .build();
    }

    private CallbackResponse validateCoSDetailsDefendant2(final CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        if (Objects.nonNull(caseData.getCosNotifyClaimDetails2())) {
            caseData.getCosNotifyClaimDetails2().setCosDocSaved(NO);
        }
        List<String> dateValidationErrorMessages = serviceOfDateValidationMessageUtils.getServiceOfDateValidationMessages(caseData.getCosNotifyClaimDetails2());

        ArrayList<String> errors = new ArrayList<>(dateValidationErrorMessages);

        if (isBothDefendantLip(caseData) && !isBothDefendantWithSameDateOfService(caseData)) {
            errors.add(BOTH_CERTIFICATE_SERVED_SAME_DATE);
        }

        if (Objects.nonNull(caseData.getCosNotifyClaimDetails2())
            && isMandatoryDocMissing(caseData.getCosNotifyClaimDetails2())) {
            errors.add(DOC_SERVED_MANDATORY);
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseData.toMap(objectMapper))
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
        certificateOfService.setCosSenderStatementOfTruthLabel(cosUISenderStatementOfTruthLabel);
        return certificateOfService;
    }

    private boolean areAnyRespondentsLitigantInPerson(CaseData caseData) {
        return caseData.getRespondent1Represented() == NO
            || (YES.equals(caseData.getAddRespondent2()) && (caseData.getRespondent2Represented() == NO));
    }

    private void assignNotifyParticularOfClaimCategoryIds(CaseData caseData) {
        assignCategoryId.assignCategoryIdToCollection(
            caseData.getServedDocumentFiles().getParticularsOfClaimDocument(),
            Element::getValue,
            PARTICULARS_OF_CLAIM
        );
        assignCategoryId.assignCategoryIdToCollection(
            caseData.getServedDocumentFiles().getMedicalReport(),
            document -> document.getValue().getDocument(),
            PARTICULARS_OF_CLAIM
        );
        assignCategoryId.assignCategoryIdToCollection(
            caseData.getServedDocumentFiles().getScheduleOfLoss(),
            document -> document.getValue().getDocument(),
            PARTICULARS_OF_CLAIM
        );
        assignCategoryId.assignCategoryIdToCollection(
            caseData.getServedDocumentFiles().getCertificateOfSuitability(),
            document -> document.getValue().getDocument(),
            PARTICULARS_OF_CLAIM
        );
        assignCategoryId.assignCategoryIdToCollection(
            caseData.getServedDocumentFiles().getOther(),
            document -> document.getValue().getDocument(),
            PARTICULARS_OF_CLAIM
        );
    }

}
