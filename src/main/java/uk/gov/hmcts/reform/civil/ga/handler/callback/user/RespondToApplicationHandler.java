package uk.gov.hmcts.reform.civil.ga.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.ga.callback.GeneralApplicationCallbackHandler;
import uk.gov.hmcts.reform.civil.ga.enums.GADebtorPaymentPlanGAspec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.helpers.DateFormatHelper;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.ga.model.GARespondentRepresentative;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.model.genapplication.GAHearingDetails;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GARespondentDebtorOfferGAspec;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GARespondentResponse;
import uk.gov.hmcts.reform.civil.model.genapplication.GAUnavailabilityDates;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.ga.service.DocUploadDashboardNotificationService;
import uk.gov.hmcts.reform.civil.ga.service.GaForLipService;
import uk.gov.hmcts.reform.civil.ga.service.GeneralAppLocationRefDataService;
import uk.gov.hmcts.reform.civil.ga.utils.DocUploadUtils;
import uk.gov.hmcts.reform.civil.ga.utils.JudicialDecisionNotificationUtil;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.google.common.collect.Lists.newArrayList;
import static java.lang.String.format;
import static java.time.LocalDate.now;
import static java.util.Optional.ofNullable;
import static org.springframework.util.CollectionUtils.isEmpty;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.RESPOND_TO_APPLICATION;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.RESPOND_TO_APPLICATION_URGENT_LIP;
import static uk.gov.hmcts.reform.civil.ga.enums.GADebtorPaymentPlanGAspec.PAYFULL;
import static uk.gov.hmcts.reform.civil.ga.enums.GARespondentDebtorOfferOptionsGAspec.ACCEPT;
import static uk.gov.hmcts.reform.civil.ga.enums.GARespondentDebtorOfferOptionsGAspec.DECLINE;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.model.common.DynamicList.fromList;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;

@Slf4j
@Service
@RequiredArgsConstructor
public class RespondToApplicationHandler extends CallbackHandler implements GeneralApplicationCallbackHandler {

    private final ObjectMapper objectMapper;
    private final CaseDetailsConverter caseDetailsConverter;
    private final IdamClient idamClient;
    private final GeneralAppLocationRefDataService locationRefDataService;
    private final CoreCaseDataService coreCaseDataService;
    private final GaForLipService gaForLipService;
    private final FeatureToggleService featureToggleService;
    private final DocUploadDashboardNotificationService dashboardNotificationService;

    private static final String RESPONSE_MESSAGE = "# You have provided the requested information";
    private static final String JUDGES_REVIEW_MESSAGE =
        "<p> The application and your response will be reviewed by a Judge. </p>";
    private static final String CONFIRMATION_SUMMARY = "<br/><p> In relation to the following application(s): </p>"
        + "<ul> %s </ul>"
        + " %s ";
    public static final String TRIAL_DATE_FROM_REQUIRED = "Please enter the Date from if the trial has been fixed";
    public static final String INVALID_TRIAL_DATE_RANGE = "Trial Date From cannot be after Trial Date to. "
        + "Please enter valid range.";
    public static final String UNAVAILABLE_DATE_RANGE_MISSING = "Please provide at least one valid Date from if you "
        + "cannot attend hearing within next 3 months.";
    public static final String UNAVAILABLE_FROM_MUST_BE_PROVIDED = "If you selected option to be unavailable then "
        + "you must provide at least one valid Date from";
    public static final String INVALID_UNAVAILABILITY_RANGE = "Unavailability Date From cannot be after "
        + "Unavailability Date to. Please enter valid range.";
    public static final String INVALID_TRAIL_DATE_FROM_BEFORE_TODAY = "Trail date from must not be before today.";
    public static final String INVALID_UNAVAILABLE_DATE_FROM_BEFORE_TODAY = "Unavailability date from must not"
        + " be before today.";
    public static final String APPLICATION_RESPONSE_PRESENT = "The General Application has already "
        +  "received a response.";
    public static final String RESPONDENT_RESPONSE_EXISTS = "The application has already been responded to.";
    public static final String PAYMENT_DATE_CANNOT_BE_IN_PAST =
        "The date entered cannot be in the past.";

    public static final String PREFERRED_TYPE_IN_PERSON = "IN_PERSON";
    private static final List<CaseEvent> EVENTS =
        List.of(RESPOND_TO_APPLICATION, RESPOND_TO_APPLICATION_URGENT_LIP);

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_START), this::applicationValidation,
            callbackKey(MID, "validate-debtor-offer"), this::validateDebtorOffer,
            callbackKey(MID, "hearing-screen-response"), this::hearingScreenResponse,
            callbackKey(ABOUT_TO_SUBMIT), this::submitClaim,
            callbackKey(SUBMITTED), this::buildResponseConfirmation
        );
    }

    private AboutToStartOrSubmitCallbackResponse applicationValidation(CallbackParams callbackParams) {
        GeneralApplicationCaseData caseData = callbackParams.getGeneralApplicationCaseData();
        GeneralApplicationCaseData caseDataBuilder = caseData.copy();
        String authToken = callbackParams.getParams().get(BEARER_TOKEN).toString();

        if (caseData.getGeneralAppType().getTypes().contains(GeneralApplicationTypes.VARY_PAYMENT_TERMS_OF_JUDGMENT)
            && caseData.getParentClaimantIsApplicant().equals(NO)) {
            caseDataBuilder.generalAppVaryJudgementType(YesOrNo.YES);
            log.info("General app vary judgement type for caseId: {}", caseData.getCcdCaseReference());
        } else {
            caseDataBuilder.generalAppVaryJudgementType(NO);
            log.info("General app does not vary judgement type for caseId: {}", caseData.getCcdCaseReference());
        }

        caseDataBuilder
            .hearingDetailsResp(
                GAHearingDetails
                    .builder()
                    .hearingPreferredLocation(getLocationsFromList(locationRefDataService
                                                                       .getCourtLocations(authToken)))
                    .build());

        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(applicationExistsValidation(callbackParams))
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }

    private CallbackResponse validateDebtorOffer(CallbackParams callbackParams) {
        GeneralApplicationCaseData caseData = callbackParams.getGeneralApplicationCaseData();
        ArrayList<String> errors = new ArrayList<>();
        if (ofNullable(caseData.getGaRespondentDebtorOffer()).isPresent()
            && caseData.getGaRespondentDebtorOffer().getRespondentDebtorOffer().equals(DECLINE)
            && caseData.getGaRespondentDebtorOffer().getPaymentPlan().equals(PAYFULL)
            && !now().isBefore(caseData.getGaRespondentDebtorOffer().getPaymentSetDate())) {
            errors.add(PAYMENT_DATE_CANNOT_BE_IN_PAST);
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .build();
    }

    private DynamicList getLocationsFromList(final List<LocationRefData> locations) {
        return fromList(locations.stream().map(location -> new StringBuilder().append(location.getSiteName())
                .append(" - ").append(location.getCourtAddress())
                .append(" - ").append(location.getPostcode()).toString()).toList());
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private SubmittedCallbackResponse buildResponseConfirmation(CallbackParams callbackParams) {
        GeneralApplicationCaseData caseData = callbackParams.getGeneralApplicationCaseData();
        String authToken = callbackParams.getParams().get(BEARER_TOKEN).toString();
        // Generate Dashboard Notification for Lip Party
        if (gaForLipService.isGaForLip(caseData) && !(featureToggleService.isGaForWelshEnabled() && caseData.isApplicationBilingual())) {

            if (caseData.getParentClaimantIsApplicant().equals(YesOrNo.NO) && caseData.getGeneralAppType().getTypes().contains(
                GeneralApplicationTypes.VARY_PAYMENT_TERMS_OF_JUDGMENT)) {
                if (gaForLipService.isLipApp(caseData)) {
                    dashboardNotificationService.createOfflineResponseDashboardNotification(caseData, "APPLICANT", authToken);
                }
                if (gaForLipService.isLipResp(caseData)) {
                    dashboardNotificationService.createOfflineResponseDashboardNotification(caseData, "RESPONDENT", authToken);
                }
            } else {
                dashboardNotificationService.createResponseDashboardNotification(caseData, "APPLICANT", authToken);
                dashboardNotificationService.createResponseDashboardNotification(caseData, "RESPONDENT", authToken);
            }
        }

        return SubmittedCallbackResponse.builder()
            .confirmationHeader(RESPONSE_MESSAGE)
            .confirmationBody(buildConfirmationSummary(caseData))
            .build();
    }

    public List<String> applicationExistsValidation(CallbackParams callbackParams) {
        GeneralApplicationCaseData caseData = callbackParams.getGeneralApplicationCaseData();
        UserInfo userInfo = idamClient.getUserInfo(callbackParams.getParams().get(BEARER_TOKEN).toString());

        List<Element<GARespondentResponse>> respondentResponse = caseData.getRespondentsResponses();
        boolean isNotAllowedForLip =
            gaForLipService.isLipResp(caseData) && JudicialDecisionNotificationUtil.isUrgent(caseData);
        List<String> errors = new ArrayList<>();
        if (caseData.getCcdState() == CaseState
            .APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION && !isNotAllowedForLip
        ) {
            errors.add(APPLICATION_RESPONSE_PRESENT);
        }
        if (respondentResponse != null) {
            Optional<Element<GARespondentResponse>> respondentResponseElement = respondentResponse.stream().findAny();
            if (respondentResponseElement.isPresent()) {
                String respondentResponseId = respondentResponseElement.get().getValue().getGaRespondentDetails();
                if (respondentResponseId.equals(userInfo.getUid())) {
                    errors.add(RESPONDENT_RESPONSE_EXISTS);
                }
            }
        }
        return errors;
    }

    private CallbackResponse hearingScreenResponse(CallbackParams callbackParams) {
        GeneralApplicationCaseData caseData = callbackParams.getGeneralApplicationCaseData();
        List<String> errors = null;
        if (callbackParams.getRequest().getEventId().equals("RESPOND_TO_APPLICATION")) {
            GAHearingDetails hearingDetails = caseData.getHearingDetailsResp();
            errors = validateResponseHearingScreen(hearingDetails);
        }
        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .build();
    }

    private List<String> validateResponseHearingScreen(GAHearingDetails hearingDetails) {
        List<String> errors = new ArrayList<>();
        validateDateRanges(errors,
                           hearingDetails.getTrialRequiredYesOrNo(),
                           hearingDetails.getTrialDateFrom(),
                           hearingDetails.getTrialDateTo(),
                           hearingDetails.getUnavailableTrialRequiredYesOrNo(),
                           hearingDetails.getGeneralAppUnavailableDates()
        );
        return errors;
    }

    private void validateDateRanges(List<String> errors,
                                    YesOrNo isTrialScheduled,
                                    LocalDate trialDateFrom,
                                    LocalDate trialDateTo,
                                    YesOrNo isUnavailable,
                                    List<Element<GAUnavailabilityDates>> datesUnavailableList) {

        if (YES.equals(isTrialScheduled)) {
            checkTrialScheduled(errors,
                    trialDateFrom,
                    trialDateTo);
        }

        if (YES.equals(isUnavailable)) {
            checkUnavailable(errors, datesUnavailableList);
        }
    }

    private void checkTrialScheduled(List<String> errors,
                                     LocalDate trialDateFrom,
                                     LocalDate trialDateTo) {
        if (trialDateFrom != null) {
            if (trialDateTo != null && trialDateTo.isBefore(trialDateFrom)) {
                errors.add(INVALID_TRIAL_DATE_RANGE);
            } else if (trialDateFrom.isBefore(LocalDate.now())) {
                errors.add(INVALID_TRAIL_DATE_FROM_BEFORE_TODAY);
            }
        } else {
            errors.add(TRIAL_DATE_FROM_REQUIRED);
        }
    }

    private void checkUnavailable(List<String> errors,
                                  List<Element<GAUnavailabilityDates>> datesUnavailableList) {
        if (isEmpty(datesUnavailableList)) {
            errors.add(UNAVAILABLE_DATE_RANGE_MISSING);
        } else {
            for (Element<GAUnavailabilityDates> dateRange : datesUnavailableList) {
                LocalDate dateFrom = dateRange.getValue().getUnavailableTrialDateFrom();
                LocalDate dateTo = dateRange.getValue().getUnavailableTrialDateTo();
                if (dateFrom == null) {
                    errors.add(UNAVAILABLE_FROM_MUST_BE_PROVIDED);
                } else if (dateTo != null && dateTo.isBefore(dateFrom)) {
                    errors.add(INVALID_UNAVAILABILITY_RANGE);
                } else if (dateFrom.isBefore(LocalDate.now())) {
                    errors.add(INVALID_UNAVAILABLE_DATE_FROM_BEFORE_TODAY);
                }
            }
        }
    }

    private String buildConfirmationSummary(GeneralApplicationCaseData caseData) {
        var genAppTypes = caseData.getGeneralAppType().getTypes();
        String appType = genAppTypes.stream().map(type -> "<li>" + type.getDisplayedValue() + "</li>")
            .collect(Collectors.joining());
        return format(
            CONFIRMATION_SUMMARY,
            appType,
            JUDGES_REVIEW_MESSAGE
        );
    }

    private CallbackResponse submitClaim(CallbackParams callbackParams) {

        GeneralApplicationCaseData caseData = caseDetailsConverter.toGeneralApplicationCaseData(callbackParams.getRequest().getCaseDetails());
        GeneralApplicationCaseData caseDataBuilder = caseData.copy();

        UserInfo userInfo = idamClient.getUserInfo(callbackParams.getParams().get(BEARER_TOKEN).toString());
        String userId = userInfo.getUid();
        List<Element<GARespondentResponse>> respondentsResponses =
            addResponse(buildResponse(caseData, userInfo), caseData.getRespondentsResponses());

        caseDataBuilder.respondentsResponses(respondentsResponses);
        caseDataBuilder.hearingDetailsResp(null); // Empty HearingDetails Respondent details as its added in the field RespondetsResponses collection
        caseDataBuilder.generalAppRespondent1Representative(new GARespondentRepresentative());
        caseDataBuilder.gaRespondentConsent(null);
        caseDataBuilder.generalAppRespondReason(null);
        caseDataBuilder.generalAppRespondConsentReason(null);
        String role = DocUploadUtils.getUserRole(caseData, userId);
        addResponseDoc(caseDataBuilder, caseData, role);
        caseDataBuilder.generalAppRespondDocument(null);
        caseDataBuilder.generalAppRespondConsentDocument(null);
        caseDataBuilder.generalAppRespondDebtorDocument(null);
        caseDataBuilder.businessProcess(BusinessProcess.readyGa(RESPOND_TO_APPLICATION)).build();
        GeneralApplicationCaseData updatedCaseData = caseDataBuilder.build();

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedCaseData.toMap(objectMapper))
            .build();
    }

    private void addResponseDoc(GeneralApplicationCaseData caseDataBuilder, GeneralApplicationCaseData caseData, String role) {
        List<Element<Document>> documents = caseData.getGeneralAppRespondDocument();
        if (Objects.isNull(documents)) {
            documents = caseData.getGeneralAppRespondConsentDocument();
        }
        if (Objects.isNull(documents)) {
            documents = caseData.getGeneralAppRespondDebtorDocument();
        }
        DocUploadUtils.addDocumentToAddl(caseData, caseDataBuilder,
                documents, role, CaseEvent.RESPOND_TO_APPLICATION, false);
    }

    private GAHearingDetails populateHearingDetailsResp(GeneralApplicationCaseData caseData, UserInfo userInfo) {
        GAHearingDetails gaHearingDetailsResp;
        String preferredType = caseData.getHearingDetailsResp().getHearingPreferencesPreferredType().name();
        if ((preferredType.equals(PREFERRED_TYPE_IN_PERSON) || caseData.getIsGaRespondentOneLip() == YES)
            && Objects.nonNull(caseData.getHearingDetailsResp().getHearingPreferredLocation())
            && Objects.nonNull(caseData.getHearingDetailsResp().getHearingPreferredLocation().getValue())) {
            String applicationLocationLabel = caseData.getHearingDetailsResp()
                .getHearingPreferredLocation().getValue()
                .getLabel();
            DynamicList dynamicLocationList = fromList(List.of(applicationLocationLabel));
            Optional<DynamicListElement> first = dynamicLocationList.getListItems().stream()
                .filter(l -> l.getLabel().equals(applicationLocationLabel)).findFirst();
            first.ifPresent(dynamicLocationList::setValue);
            gaHearingDetailsResp = caseData.getHearingDetailsResp().toBuilder()
                .respondentResponsePartyName(getRespondentResponsePartyName(caseData, userInfo))
                .hearingPreferredLocation(dynamicLocationList).build();

        } else {
            gaHearingDetailsResp = caseData.getHearingDetailsResp().toBuilder()
                .respondentResponsePartyName(getRespondentResponsePartyName(caseData, userInfo))
                .hearingPreferredLocation(DynamicList.builder().build()).build();
        }
        return gaHearingDetailsResp;
    }

    private String getRespondentResponsePartyName(GeneralApplicationCaseData gaCaseData, UserInfo userInfo) {

        GeneralApplicationCaseData civilCaseData = caseDetailsConverter
            .toGeneralApplicationCaseData(coreCaseDataService
                            .getCase(Long.parseLong(gaCaseData.getGeneralAppParentCaseLink().getCaseReference())));

        return checkIfEmailIdMatch(userInfo, civilCaseData, gaCaseData);
    }

    private String checkIfEmailIdMatch(UserInfo userInfo,
                                       GeneralApplicationCaseData civilCaseData, GeneralApplicationCaseData gaCaseData) {

        // civil claim claimant
        if (!gaCaseData.getParentClaimantIsApplicant().equals(YES)
            && (Objects.nonNull(civilCaseData.getApplicantSolicitor1UserDetails())
            && userInfo.getSub().equals(civilCaseData.getApplicantSolicitor1UserDetails().getEmail())
            || gaForLipService.isLipResp(gaCaseData))) {

            log.info("Return Civil Claim Defendant two party Name if GA Solicitor Email ID "
                         + "as same as Civil Claim Claimant Solicitor Two Email");

            return gaCaseData.getClaimant1PartyName() + " - Claimant";
        }

        // Civil Claim Defendant 1
        if (userInfo.getSub().equals(civilCaseData.getRespondentSolicitor1EmailAddress())) {

            log.info("Return Civil Claim Defendant One party Name if GA Solicitor Email ID "
                         + "as same as Civil Claim Respondent Solicitor Two Email");

            return gaCaseData.getDefendant1PartyName() + " - Defendant";
        }

        // civil claim defendant 2
        if (YES.equals(gaCaseData.getIsMultiParty())
            && NO.equals(civilCaseData.getRespondent2SameLegalRepresentative())
            && userInfo.getSub().equals(civilCaseData.getRespondentSolicitor2EmailAddress())) {

            log.info("Return Civil Claim Defendant two party Name if GA Solicitor Email ID "
                         + "as same as Civil Claim Respondent Solicitor Two Email");

            return gaCaseData.getDefendant2PartyName() + " - Defendant";
        }

        return StringUtils.EMPTY;
    }

    private List<Element<GARespondentResponse>> addResponse(GARespondentResponse gaRespondentResponseBuilder,
                                                            List<Element<GARespondentResponse>> respondentsResponses) {

        List<Element<GARespondentResponse>> newApplication = ofNullable(respondentsResponses).orElse(newArrayList());
        newApplication.add(element(gaRespondentResponseBuilder));

        return newApplication;
    }

    private GARespondentResponse buildResponse(GeneralApplicationCaseData caseData, UserInfo userInfo) {

        YesOrNo generalOther = NO;
        if (Objects.nonNull(caseData.getGeneralAppConsentOrder())) {
            generalOther = caseData.getGaRespondentConsent();
        } else if (caseData.getGeneralAppType().getTypes().contains(GeneralApplicationTypes.VARY_PAYMENT_TERMS_OF_JUDGMENT)
            && caseData.getParentClaimantIsApplicant().equals(NO)
            && ofNullable(caseData.getGaRespondentDebtorOffer()).isPresent()
            && caseData.getGaRespondentDebtorOffer().getRespondentDebtorOffer().equals(ACCEPT)) {
            generalOther = YES;
        }

        GARespondentResponse gaRespondentResponse = new GARespondentResponse();
        String reason = caseData.getGeneralAppRespondReason();
        if (Objects.isNull(reason)) {
            reason = caseData.getGeneralAppRespondConsentReason();
            if (Objects.isNull(reason)) {
                reason = getDebtorReason(caseData.getGaRespondentDebtorOffer());
            }
        }
        gaRespondentResponse
            .setGeneralAppRespondent1Representative(caseData.getGeneralAppRespondent1Representative() == null
                                                     ? generalOther
                                                     : caseData.getGeneralAppRespondent1Representative()
                .getGeneralAppRespondent1Representative())
            .setGaHearingDetails(populateHearingDetailsResp(caseData, userInfo))
            .setGaRespondentResponseReason(reason)
            .setGaRespondentDetails(userInfo.getUid());

        return gaRespondentResponse;
    }

    private String getDebtorReason(GARespondentDebtorOfferGAspec gaRespondentDebtorOffer) {
        if (Objects.nonNull(gaRespondentDebtorOffer)
                && gaRespondentDebtorOffer.getRespondentDebtorOffer().equals(DECLINE)) {
            StringBuilder sb = new StringBuilder();
            sb.append("Proposed payment plan is ")
                    .append(gaRespondentDebtorOffer.getPaymentPlan().getDisplayedValue())
                    .append(". ");
            if (gaRespondentDebtorOffer.getPaymentPlan().equals(GADebtorPaymentPlanGAspec.INSTALMENT)) {
                BigDecimal pounds = gaRespondentDebtorOffer.getMonthlyInstalment()
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                sb.append("Proposed instalments per month is ")
                        .append(pounds)
                        .append(" pounds. ");
            } else {
                sb.append("Proposed set date is ")
                        .append(DateFormatHelper
                                .formatLocalDate(gaRespondentDebtorOffer.getPaymentSetDate(), "d MMMM yyyy"))
                        .append(". ");
            }
            sb.append("Objections to the debtor's proposals is ")
                    .append(gaRespondentDebtorOffer.getDebtorObjections());
            return sb.toString();
        }
        return null;
    }
}
