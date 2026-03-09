package uk.gov.hmcts.reform.civil.ga.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dq.GAHearingDuration;
import uk.gov.hmcts.reform.civil.enums.dq.GAHearingSupportRequirements;
import uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes;
import uk.gov.hmcts.reform.civil.ga.callback.GeneralApplicationCallbackHandler;
import uk.gov.hmcts.reform.civil.ga.enums.dq.FinalOrderShowToggle;
import uk.gov.hmcts.reform.civil.ga.enums.dq.GAByCourtsInitiativeGAspec;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAJudgesHearingListGAspec;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAJudicialDecision;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAJudicialMakeAnOrder;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAJudicialRequestMoreInfo;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAJudicialWrittenRepresentations;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAOrderCourtOwnInitiativeGAspec;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAOrderWithoutNoticeGAspec;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GARespondentResponse;
import uk.gov.hmcts.reform.civil.ga.service.AssignCaseToRespondentSolHelper;
import uk.gov.hmcts.reform.civil.ga.service.GaForLipService;
import uk.gov.hmcts.reform.civil.ga.service.GeneralAppLocationRefDataService;
import uk.gov.hmcts.reform.civil.ga.service.JudicialDecisionHelper;
import uk.gov.hmcts.reform.civil.ga.service.JudicialDecisionWrittenRepService;
import uk.gov.hmcts.reform.civil.ga.service.JudicialTimeEstimateHelper;
import uk.gov.hmcts.reform.civil.ga.service.docmosis.DismissalOrderGenerator;
import uk.gov.hmcts.reform.civil.ga.service.docmosis.GeneralOrderGenerator;
import uk.gov.hmcts.reform.civil.ga.service.docmosis.HearingOrderGenerator;
import uk.gov.hmcts.reform.civil.ga.service.docmosis.RequestForInformationGenerator;
import uk.gov.hmcts.reform.civil.ga.service.docmosis.WrittenRepresentationConcurrentOrderGenerator;
import uk.gov.hmcts.reform.civil.ga.service.docmosis.WrittenRepresentationSequentialOrderGenerator;
import uk.gov.hmcts.reform.civil.ga.service.docmosis.directionorder.DirectionOrderGenerator;
import uk.gov.hmcts.reform.civil.ga.service.docmosis.finalorder.FreeFormOrderGenerator;
import uk.gov.hmcts.reform.civil.ga.utils.IdamUserUtils;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.caseprogression.FreeFormOrderValues;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.genapplication.GAHearingDetails;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.lang.String.format;
import static java.util.Optional.ofNullable;
import static org.apache.logging.log4j.util.Strings.concat;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.MAKE_DECISION;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes.EXTEND_TIME;
import static uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes.STAY_THE_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes.STRIKE_OUT;
import static uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes.UNLESS_ORDER;
import static uk.gov.hmcts.reform.civil.ga.enums.dq.GAByCourtsInitiativeGAspec.OPTION_1;
import static uk.gov.hmcts.reform.civil.ga.enums.dq.GAByCourtsInitiativeGAspec.OPTION_2;
import static uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeDecisionOption.MAKE_ORDER_FOR_WRITTEN_REPRESENTATIONS;
import static uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeDecisionOption.REQUEST_MORE_INFO;
import static uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeMakeAnOrderOption.APPROVE_OR_EDIT;
import static uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeMakeAnOrderOption.DISMISS_THE_APPLICATION;
import static uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeMakeAnOrderOption.GIVE_DIRECTIONS_WITHOUT_HEARING;
import static uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeRequestMoreInfoOption.REQUEST_MORE_INFORMATION;
import static uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeRequestMoreInfoOption.SEND_APP_TO_OTHER_PARTY;
import static uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeWrittenRepresentationsOptions.CONCURRENT_REPRESENTATIONS;
import static uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeWrittenRepresentationsOptions.SEQUENTIAL_REPRESENTATIONS;
import static uk.gov.hmcts.reform.civil.ga.utils.JudicialDecisionNotificationUtil.isGeneralAppConsentOrder;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;
import static uk.gov.hmcts.reform.civil.model.common.DynamicList.fromList;

@Slf4j
@Service
@RequiredArgsConstructor
public class JudicialDecisionHandler extends CallbackHandler implements GeneralApplicationCallbackHandler {

    public static final String RESPOND_TO_DIRECTIONS_DATE_IN_PAST = "The date, by which the response to direction"
        + " should be given, cannot be in past.";
    public static final String REQUESTED_MORE_INFO_BY_DATE_REQUIRED = "The date, by which the applicant must respond, "
        + "is required.";
    public static final String REQUESTED_MORE_INFO_BY_DATE_IN_PAST = "The date, by which the applicant must respond, "
        + "cannot be in past.";
    public static final String MAKE_DECISION_APPROVE_BY_DATE_IN_PAST = "The date entered cannot be in the past.";
    public static final String PREFERRED_LOCATION_REQUIRED = "Select your preferred hearing location.";
    public static final String PREFERRED_TYPE_IN_PERSON = "IN_PERSON";
    public static final String JUDICIAL_DECISION_LIST_FOR_HEARING = "LIST_FOR_A_HEARING";
    private static final List<CaseEvent> EVENTS = Collections.singletonList(MAKE_DECISION);
    private static final String VALIDATE_MAKE_DECISION_SCREEN = "validate-make-decision-screen";
    private static final String VALIDATE_MAKE_AN_ORDER = "validate-make-an-order";
    private static final int ONE_V_ONE = 0;
    private static final int ONE_V_TWO = 1;
    private static final String EMPTY_STRING = "";
    private static final String WITHOUT_NOTICE = " without notice ";
    private static final String DEFENDANT = "defendant";
    private static final String CLAIMANT = "claimant";
    private static final String JUDICIAL_MISSING_DATA = "Missing data during submission of judicial decision";
    private static final String NO_SUPPORT = "no support";
    private static final String VALIDATE_REQUEST_MORE_INFO_SCREEN = "validate-request-more-info-screen";
    private static final String VALIDATE_HEARING_ORDER_SCREEN = "validate-hearing-order-screen";
    private static final String POPULATE_HEARING_ORDER_DOC = "populate-hearing-order-doc";
    private static final String POPULATE_WRITTEN_REP_PREVIEW_DOC = "populate-written-rep-preview-doc";
    private static final String APPLICANT_ESTIMATES = "Applicant estimates %s";
    private static final String RESPONDENT_ESTIMATES = "Respondent estimates %s";
    private static final String RESPONDENT1_ESTIMATES = "Respondent 1 estimates %s";
    private static final String RESPONDENT2_ESTIMATES = "Respondent 2 estimates %s";
    private static final String ESTIMATES_NOT_PROVIDED = "Applicant and respondent have not provided estimates";
    private static final String JUDICIAL_TIME_EST_TEXT_BOTH = "Both applicant and respondent estimate it would take %s.";
    private static final String APPLICANT_REQUIRES = "Applicant requires ";
    private static final String JUDICIAL_APPLICANT_VULNERABILITY_TEXT = APPLICANT_REQUIRES + "support with regards to "
        + "vulnerability\n";
    private static final String APPLICANT_PREFERS = "Applicant prefers ";
    private static final String JUDICIAL_PREF_COURT_LOC_APPLICANT_TEXT = APPLICANT_PREFERS + "Location %s.";
    private static final String JUDICIAL_PREF_COURT_LOC_RESP1_TEXT = "Respondent 1 prefers Location %s.";
    private static final String JUDICIAL_PREF_COURT_LOC_RESP2_TEXT = "Respondent 2 prefers Location %s.";
    private static final String JUDICIAL_PREF_TYPE_TEXT_1 = APPLICANT_PREFERS
        + "%s. Respondent prefers %s.";
    private static final String JUDICIAL_PREF_TYPE_TEXT_2 = "Both applicant and respondent prefer %s.";
    private static final String JUDICIAL_PREF_TYPE_TEXT_3 = APPLICANT_PREFERS
        + "%s. Respondent 1 prefers %s. Respondent 2 prefers %s.";
    private static final String JUDICIAL_SUPPORT_REQ_TEXT_1 = APPLICANT_REQUIRES
        + "%s. Respondent requires %s.";
    private static final String JUDICIAL_SUPPORT_REQ_TEXT_2 = " Both applicant and respondent require %s.";
    private static final String JUDICIAL_SUPPORT_REQ_TEXT_3 = APPLICANT_REQUIRES
        + "%s. Respondent 1 requires %s. Respondent 2 requires %s.";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("d MMMM yyyy");
    private static final DateTimeFormatter DATE_FORMATTER_SUBMIT_CALLBACK = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final String VALIDATE_WRITTEN_REPRESENTATION_DATE = "ga-validate-written-representation-date";
    private static final String JUDICIAL_HEARING_TYPE = "The hearing will be %s";
    private static final String JUDICIAL_TIME_ESTIMATE = "Estimated length of hearing is %s";
    private static final String JUDICIAL_SEQUENTIAL_DATE =
        "The defendant should upload any written responses or evidence by 4pm on %s";
    private static final String JUDICIAL_SEQUENTIAL_APPLICANT_DATE =
        "The claimant should upload any written responses or evidence in reply by 4pm on %s";
    private static final String JUDICIAL_CONCURRENT_DATE =
        "The claimant and defendant should upload any written submissions and evidence by 4pm on %s";
    private static final String JUDICIAL_HEARING_REQ = "Hearing requirements %s";
    private static final String ORDER_COURT_OWN_INITIATIVE = "As this order was made on the court's own initiative, any "
        + "party affected by the order may apply to set aside, vary, or stay the order. "
        + "Any such application must be made by 4pm on \n\n";
    private static final String ORDER_WITHOUT_NOTICE = "If you were not notified of the application before this "
        + "order was made, you may apply to set aside, vary, or stay the order. "
        + "Any such application must be made by 4pm on \n\n";
    private static final String DISMISSAL_ORDER_TEXT = """
        This application is dismissed.

        """;
    private static final String JUDICIAL_RECITAL_TEXT = """
        The Judge considered the%sapplication of the %s dated %s

        %s""";
    private static final String JUDICIAL_RESPONDENT_VULNERABILITY_TEXT = """


        Respondent requires support with regards to vulnerability
        """;
    private static final String JUDICIAL_RESPONDENT1_VULNERABILITY_TEXT = """


        Respondent 1 requires support with regards to vulnerability
        """;
    private static final String JUDICIAL_RESPONDENT2_VULNERABILITY_TEXT = """


        Respondent 2 requires support with regards to vulnerability
        """;
    private static final String POPULATE_FINAL_ORDER_PREVIEW_DOC = "populate-final-order-preview-doc";
    private static final String ON_INITIATIVE_SELECTION_TEST = "As this order was made on the court's own initiative, "
        + "any party affected by the order may apply to set aside, vary, or stay the order."
        + " Any such application must be made by 4pm on";
    private static final String WITHOUT_NOTICE_SELECTION_TEXT = "If you were not notified of the application before "
        + "this order was made, you may apply to set aside, vary, or stay the order."
        + " Any such application must be made by 4pm on";
    private static final int PLUS_7DAYS = 7;
    private final GeneralAppLocationRefDataService locationRefDataService;
    private final JudicialDecisionHelper helper;
    private final AssignCaseToRespondentSolHelper assignCaseToResopondentSolHelper;
    private final JudicialTimeEstimateHelper timeEstimateHelper;
    private final JudicialDecisionWrittenRepService judicialDecisionWrittenRepService;
    private final ObjectMapper objectMapper;

    private final GeneralOrderGenerator generalOrderGenerator;
    private final RequestForInformationGenerator requestForInformationGenerator;
    private final DirectionOrderGenerator directionOrderGenerator;
    private final DismissalOrderGenerator dismissalOrderGenerator;
    private final HearingOrderGenerator hearingOrderGenerator;
    private final WrittenRepresentationSequentialOrderGenerator writtenRepresentationSequentialOrderGenerator;
    private final WrittenRepresentationConcurrentOrderGenerator writtenRepresentationConcurrentOrderGenerator;
    private final FreeFormOrderGenerator gaFreeFormOrderGenerator;
    private final DeadlinesCalculator deadlinesCalculator;
    private final IdamClient idamClient;
    private final GaForLipService gaForLipService;
    private final CaseDetailsConverter caseDetailsConverter;
    private final CoreCaseDataService coreCaseDataService;

    public static String getAllPartyNames(GeneralApplicationCaseData caseData) {
        return format(
            "%s v %s%s",
            caseData.getClaimant1PartyName(),
            caseData.getDefendant1PartyName(),
            Objects.nonNull(caseData.getDefendant2PartyName())
                && (NO.equals(caseData.getRespondent2SameLegalRepresentative())
                || Objects.isNull(caseData.getRespondent2SameLegalRepresentative()))
                ? ", " + caseData.getDefendant2PartyName() : ""
        );
    }

    @Override
    protected Map<String, Callback> callbacks() {
        return new ImmutableMap.Builder<String, Callback>()
            .put(callbackKey(ABOUT_TO_START), this::checkInputForNextPage)
            .put(callbackKey(MID, VALIDATE_MAKE_AN_ORDER), this::gaValidateMakeAnOrder)
            .put(callbackKey(MID, VALIDATE_MAKE_DECISION_SCREEN), this::gaValidateMakeDecisionScreen)
            .put(callbackKey(MID, VALIDATE_REQUEST_MORE_INFO_SCREEN), this::gaValidateRequestMoreInfoScreen)
            .put(callbackKey(MID, VALIDATE_WRITTEN_REPRESENTATION_DATE), this::gaValidateWrittenRepresentationsDate)
            .put(callbackKey(MID, VALIDATE_HEARING_ORDER_SCREEN), this::gaValidateHearingOrder)
            .put(callbackKey(MID, POPULATE_HEARING_ORDER_DOC), this::gaPopulateHearingOrderDoc)
            .put(callbackKey(MID, POPULATE_WRITTEN_REP_PREVIEW_DOC), this::gaPopulateWrittenRepPreviewDoc)
            .put(callbackKey(MID, POPULATE_FINAL_ORDER_PREVIEW_DOC), this::gaPopulateFinalOrderPreviewDoc)
            .put(callbackKey(ABOUT_TO_SUBMIT), this::setJudgeBusinessProcess)
            .put(callbackKey(SUBMITTED), this::buildConfirmation).build();
    }

    private CallbackResponse checkInputForNextPage(CallbackParams callbackParams) {

        GeneralApplicationCaseData caseData = callbackParams.getGeneralApplicationCaseData();
        GeneralApplicationCaseData caseDataBuilder = caseData.copy();
        caseDataBuilder.judicialDecision(new GAJudicialDecision());
        UserInfo userDetails = idamClient.getUserInfo(callbackParams.getParams().get(BEARER_TOKEN).toString());
        caseDataBuilder.judgeTitle(IdamUserUtils.getIdamUserFullName(userDetails));

        if (caseData.getApplicationIsCloaked() == null && !gaForLipService.isGaForLip(caseData)) {
            caseDataBuilder.applicationIsCloaked(helper.isApplicationCreatedWithoutNoticeByApplicant(caseData));
        } else if (caseData.getApplicationIsCloaked() == null && gaForLipService.isGaForLip(caseData)) {
            caseDataBuilder.applicationIsCloaked(helper.isLipApplicationCreatedWithoutNoticeByApplicant(caseData));
        }

        caseDataBuilder.judicialDecisionMakeOrder(makeAnOrderBuilder(caseData, callbackParams));
        caseDataBuilder.judgeRecitalText(getJudgeRecitalPrepopulatedText(caseData)).build();

        caseDataBuilder
            .judicialDecisionRequestMoreInfo(buildRequestMoreInfo(caseData));

        caseDataBuilder.judicialGeneralHearingOrderRecital(getJudgeHearingRecitalPrepopulatedText(caseData))
            .build();

        YesOrNo isAppAndRespSameHearingPref = (caseData.getGeneralAppHearingDetails() != null
            && caseData.getRespondentsResponses() != null
            && caseData.getRespondentsResponses().size() == 1
            && caseData.getGeneralAppHearingDetails().getHearingPreferencesPreferredType().getDisplayedValue()
            .equals(caseData.getRespondentsResponses().stream().iterator().next().getValue().getGaHearingDetails()
                        .getHearingPreferencesPreferredType().getDisplayedValue()))
            ? YES : NO;

        GAJudgesHearingListGAspec gaJudgesHearingListGAspec;
        if (caseData.getJudicialListForHearing() != null) {
            gaJudgesHearingListGAspec = caseData.getJudicialListForHearing().copy();
        } else {
            gaJudgesHearingListGAspec = new GAJudgesHearingListGAspec();
        }

        YesOrNo isAppAndRespSameSupportReq = (caseData.getGeneralAppHearingDetails() != null
            && caseData.getRespondentsResponses() != null
            && caseData.getRespondentsResponses().size() == 1
            && caseData.getRespondentsResponses().get(0).getValue().getGaHearingDetails()
            .getSupportRequirement() != null
            && caseData.getGeneralAppHearingDetails().getSupportRequirement() != null
            && checkIfAppAndRespHaveSameSupportReq(caseData))
            ? YES : NO;

        String authToken = callbackParams.getParams().get(BEARER_TOKEN).toString();
        DynamicList dynamicLocationList = getLocationsFromList(locationRefDataService.getCourtLocations(authToken));

        boolean isAppAndRespSameCourtLocPref = helper.isApplicantAndRespondentLocationPrefSame(caseData);
        if (isAppAndRespSameCourtLocPref) {
            String applicationLocationLabel = caseData.getGeneralAppHearingDetails().getHearingPreferredLocation()
                .getValue().getLabel();
            Optional<DynamicListElement> first = dynamicLocationList.getListItems().stream()
                .filter(l -> l.getLabel().equals(applicationLocationLabel)).findFirst();
            first.ifPresent(dynamicLocationList::setValue);
        }

        YesOrNo isAppAndRespSameTimeEst = (caseData.getGeneralAppHearingDetails() != null
            && caseData.getRespondentsResponses() != null
            && caseData.getRespondentsResponses().size() == 1
            && caseData.getGeneralAppHearingDetails().getHearingDuration() != null
            && caseData.getGeneralAppHearingDetails().getHearingDuration()
            == caseData.getRespondentsResponses().get(0).getValue().getGaHearingDetails().getHearingDuration())
            ? YES : NO;

        caseDataBuilder.judicialListForHearing(gaJudgesHearingListGAspec
                                                   .setHearingPreferredLocation(dynamicLocationList)
                                                   .setHearingPreferencesPreferredTypeLabel1(
                                                       getJudgeHearingPrefType(caseData, isAppAndRespSameHearingPref))
                                                   .setJudgeHearingCourtLocationText1(
                                                       generateRespondentCourtLocationText(caseData))
                                                   .setJudgeHearingTimeEstimateText1(
                                                       getJudgeHearingTimeEst(caseData, isAppAndRespSameTimeEst))
                                                   .setJudgeHearingSupportReqText1(
                                                       getJudgeHearingSupportReq(caseData, isAppAndRespSameSupportReq))
                                                   .setJudicialVulnerabilityText(
                                                       getJudgeVulnerabilityText(caseData)));

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }

    private DynamicList getLocationsFromList(final List<LocationRefData> locations) {
        return fromList(locations.stream().map(location -> new StringBuilder().append(location.getSiteName())
                .append(" - ").append(location.getCourtAddress())
                .append(" - ").append(location.getPostcode()).toString())
                            .collect(Collectors.toList()));
    }

    public GAJudicialRequestMoreInfo buildRequestMoreInfo(GeneralApplicationCaseData caseData) {

        GAJudicialRequestMoreInfo gaJudicialRequestMoreInfo = new GAJudicialRequestMoreInfo();

        if (caseData.getGeneralAppRespondentAgreement().getHasAgreed().equals(NO)) {
            if (isAdditionalPaymentMade(caseData).equals(YES)) {
                log.info(
                    "General app respondent has not agreed and the additional payment has been made for caseId: {}",
                    caseData.getCcdCaseReference()
                );
                gaJudicialRequestMoreInfo.setIsWithNotice(YES);
            } else {
                log.info(
                    "General app respondent has not agreed and the additional payment has not been made for caseId: {}",
                    caseData.getCcdCaseReference()
                );
                gaJudicialRequestMoreInfo
                    .setIsWithNotice(caseData.getGeneralAppInformOtherParty().getIsWithNotice());
            }

        } else if (caseData.getGeneralAppRespondentAgreement().getHasAgreed().equals(YES)) {
            log.info("General app respondent has agreed for caseId: {}", caseData.getCcdCaseReference());
            if (isGeneralAppConsentOrder(caseData)) {
                gaJudicialRequestMoreInfo.setIsWithNotice(NO);
            } else {
                gaJudicialRequestMoreInfo.setIsWithNotice(YES);
            }
        }

        /*
         * Set isWithNotice to Yes if Judge uncloaks the application
         * */
        if (caseData.getApplicationIsUncloakedOnce() != null
            && caseData.getApplicationIsUncloakedOnce().equals(YES)) {
            gaJudicialRequestMoreInfo.setIsWithNotice(YES);
        }

        gaJudicialRequestMoreInfo
            .setJudgeRecitalText(format(
                JUDICIAL_RECITAL_TEXT,
                helper.isApplicationCreatedWithoutNoticeByApplicant(caseData) == YES
                    ? WITHOUT_NOTICE : " ",
                (caseData.getParentClaimantIsApplicant() == null
                    || YES.equals(caseData.getParentClaimantIsApplicant()))
                    ? CLAIMANT : DEFENDANT,
                DATE_FORMATTER.format(caseData.getCreatedDate()),
                (helper.isApplicationCreatedWithoutNoticeByApplicant(caseData)
                    == NO ? "" : judgeConsideredText(caseData))
            ));

        return gaJudicialRequestMoreInfo;
    }

    public String dismissalOrderText(GeneralApplicationCaseData caseData) {

        return caseData.getJudicialDecisionMakeOrder().getDismissalOrderText() == null
            ? DISMISSAL_ORDER_TEXT
            : caseData.getJudicialDecisionMakeOrder().getDismissalOrderText();

    }

    public GAJudicialMakeAnOrder makeAnOrderBuilder(GeneralApplicationCaseData caseData,
                                                    CallbackParams callbackParams) {
        GAJudicialMakeAnOrder makeAnOrder;
        if (caseData.getJudicialDecisionMakeOrder() != null && callbackParams.getType() != ABOUT_TO_START) {
            makeAnOrder = caseData.getJudicialDecisionMakeOrder().copy();

            makeAnOrder.setOrderText(caseData.getJudicialDecisionMakeOrder().getOrderText())
                .setJudgeRecitalText(caseData.getJudicialDecisionMakeOrder().getJudgeRecitalText())
                .setDismissalOrderText(dismissalOrderText(caseData))
                .setDirectionsText(caseData.getJudicialDecisionMakeOrder().getDirectionsText())
                .setOrderWithoutNotice(caseData.getJudicialDecisionMakeOrder().getOrderWithoutNotice())
                .setOrderCourtOwnInitiative(caseData.getJudicialDecisionMakeOrder().getOrderCourtOwnInitiative());
        } else {
            makeAnOrder = new GAJudicialMakeAnOrder();

            makeAnOrder.setOrderText(caseData.getGeneralAppDetailsOfOrder())
                .setJudgeRecitalText(getJudgeRecitalPrepopulatedText(caseData))
                .setDismissalOrderText(DISMISSAL_ORDER_TEXT)
                .setIsOrderProcessedByStayScheduler(NO)
                .setIsOrderProcessedByUnlessScheduler(NO)
                .setOrderCourtOwnInitiative(ORDER_COURT_OWN_INITIATIVE)
                .setOrderWithoutNotice(ORDER_WITHOUT_NOTICE)
                .setOrderWithoutNoticeDate(deadlinesCalculator.getJudicialOrderDeadlineDate(
                    LocalDateTime.now(), PLUS_7DAYS))
                .setOrderCourtOwnInitiativeDate(deadlinesCalculator.getJudicialOrderDeadlineDate(
                    LocalDateTime.now(), PLUS_7DAYS))
                .setShowJudgeRecitalText(List.of(FinalOrderShowToggle.SHOW));
        }

        GAJudicialMakeAnOrder judicialDecisionMakeOrder = caseData.getJudicialDecisionMakeOrder();
        if (judicialDecisionMakeOrder != null) {
            return makeAnOrder
                .setDisplayjudgeApproveEditOptionDate(displayjudgeApproveEditOptionDate(
                    caseData,
                    judicialDecisionMakeOrder
                ))
                .setDisplayjudgeApproveEditOptionDateForUnlessOrder(
                    displayjudgeApproveEditOptionDateForUnlessOrder(caseData, judicialDecisionMakeOrder))
                .setDisplayjudgeApproveEditOptionDoc(
                    displayjudgeApproveEditOptionDoc(caseData, judicialDecisionMakeOrder));
        }

        return makeAnOrder
            .setDisplayjudgeApproveEditOptionDate(displayjudgeApproveEditOptionDate(
                caseData,
                judicialDecisionMakeOrder
            ))
            .setDisplayjudgeApproveEditOptionDateForUnlessOrder(
                displayjudgeApproveEditOptionDateForUnlessOrder(caseData, judicialDecisionMakeOrder))
            .setDisplayjudgeApproveEditOptionDoc(displayjudgeApproveEditOptionDoc(caseData, judicialDecisionMakeOrder));
    }

    public YesOrNo displayjudgeApproveEditOptionDate(GeneralApplicationCaseData caseData,
                                                     GAJudicialMakeAnOrder judicialDecisionMakeOrder) {

        if (judicialDecisionMakeOrder != null) {
            return checkApplicationTypeForDate(caseData) && APPROVE_OR_EDIT
                .equals(judicialDecisionMakeOrder.getMakeAnOrder()) ? YES : NO;
        }
        return checkApplicationTypeForDate(caseData) ? YES : NO;
    }

    public YesOrNo displayjudgeApproveEditOptionDateForUnlessOrder(GeneralApplicationCaseData caseData,
                                                                   GAJudicialMakeAnOrder judicialDecisionMakeOrder) {

        if (judicialDecisionMakeOrder != null) {
            return checkApplicationTypeForUnlessOrderDate(caseData)
                && APPROVE_OR_EDIT
                .equals(judicialDecisionMakeOrder.getMakeAnOrder()) ? YES : NO;
        }

        return checkApplicationTypeForUnlessOrderDate(caseData) ? YES : NO;
    }

    public YesOrNo displayjudgeApproveEditOptionDoc(GeneralApplicationCaseData caseData,
                                                    GAJudicialMakeAnOrder judicialDecisionMakeOrder) {
        if (judicialDecisionMakeOrder != null) {
            return checkApplicationTypeForDoc(caseData) && APPROVE_OR_EDIT
                .equals(judicialDecisionMakeOrder.getMakeAnOrder()) ? YES : NO;
        }
        return checkApplicationTypeForDoc(caseData) ? YES : NO;
    }

    /*Return True if General Application types are only Extend Time or/and Strike Out
    Else, Return False*/
    private boolean checkApplicationTypeForDoc(GeneralApplicationCaseData caseData) {

        if (caseData.getGeneralAppType() == null) {
            return false;
        }
        List<GeneralApplicationTypes> validGATypes = Arrays.asList(EXTEND_TIME, STRIKE_OUT);
        return caseData.getGeneralAppType().getTypes().stream().anyMatch(validGATypes::contains);

    }

    /*Return True if General Application types contains Stay the Claim
    Else, Return False*/
    private boolean checkApplicationTypeForDate(GeneralApplicationCaseData caseData) {

        if (caseData.getGeneralAppType() == null) {
            return false;
        }
        List<GeneralApplicationTypes> validGATypes = List.of(STAY_THE_CLAIM);
        return caseData.getGeneralAppType().getTypes().stream().anyMatch(validGATypes::contains);
    }

    /*Return True if General Application types contains Unless Order
    Else, Return False*/
    private boolean checkApplicationTypeForUnlessOrderDate(GeneralApplicationCaseData caseData) {

        if (caseData.getGeneralAppType() == null) {
            return false;
        }
        List<GeneralApplicationTypes> validGATypes = List.of(UNLESS_ORDER);
        return caseData.getGeneralAppType().getTypes().stream().anyMatch(validGATypes::contains);
    }

    private Boolean checkIfAppAndRespHaveSameSupportReq(GeneralApplicationCaseData caseData) {

        if (caseData.getRespondentsResponses().stream().iterator().next().getValue()
            .getGaHearingDetails().getSupportRequirement() != null) {

            ArrayList<GAHearingSupportRequirements> applicantSupportReq
                = caseData.getGeneralAppHearingDetails().getSupportRequirement().stream().sorted()
                .collect(Collectors.toCollection(ArrayList::new));

            ArrayList<GAHearingSupportRequirements> respondentSupportReq
                = caseData.getRespondentsResponses().stream().iterator().next().getValue()
                .getGaHearingDetails().getSupportRequirement().stream().sorted()
                .collect(Collectors.toCollection(ArrayList::new));

            return applicantSupportReq.equals(respondentSupportReq);
        }

        return false;
    }

    private String getJudgeRecitalPrepopulatedText(GeneralApplicationCaseData caseData) {
        return format(
            JUDICIAL_RECITAL_TEXT,
            helper.isApplicationCreatedWithoutNoticeByApplicant(caseData) == YES ? WITHOUT_NOTICE : " ",
            (caseData.getParentClaimantIsApplicant() == null
                || YES.equals(caseData.getParentClaimantIsApplicant()))
                ? CLAIMANT : DEFENDANT,
            DATE_FORMATTER.format(caseData.getCreatedDate()),
            (helper.isApplicationCreatedWithoutNoticeByApplicant(caseData)
                == NO ? "" : judgeConsideredText(caseData))
        );
    }

    private String getJudgeHearingRecitalPrepopulatedText(GeneralApplicationCaseData caseData) {
        return format(
            JUDICIAL_RECITAL_TEXT,
            helper.isApplicationCreatedWithoutNoticeByApplicant(caseData) == YES ? WITHOUT_NOTICE : " ",
            (caseData.getParentClaimantIsApplicant() == null
                || YES.equals(caseData.getParentClaimantIsApplicant()))
                ? CLAIMANT : DEFENDANT,
            DATE_FORMATTER.format(caseData.getCreatedDate()),
            (helper.isApplicationCreatedWithoutNoticeByApplicant(caseData)
                == NO ? "" : judgeConsideredText(caseData))
        );
    }

    private String judgeConsideredText(GeneralApplicationCaseData caseData) {
        return "And the Judge considered the information provided by the " + identifySolicitor(
            caseData);
    }

    private String identifySolicitor(GeneralApplicationCaseData caseData) {

        return (caseData.getParentClaimantIsApplicant() == null
            || YES.equals(caseData.getParentClaimantIsApplicant()))
            ? CLAIMANT : DEFENDANT;

    }

    private CallbackResponse gaValidateMakeDecisionScreen(CallbackParams callbackParams) {
        GeneralApplicationCaseData caseData = callbackParams.getGeneralApplicationCaseData();
        GeneralApplicationCaseData caseDataBuilder = caseData.copy();

        GAJudicialMakeAnOrder judicialDecisionMakeOrder = caseData.getJudicialDecisionMakeOrder();
        List<String> errors = Collections.emptyList();
        if (judicialDecisionMakeOrder != null) {
            errors = validateUrgencyDates(judicialDecisionMakeOrder);
            errors.addAll(validateJudgeOrderRequestDates(judicialDecisionMakeOrder));
            errors.addAll(validateCourtsInitiativeDates(judicialDecisionMakeOrder));

            caseDataBuilder
                .judicialDecisionMakeOrder(makeAnOrderBuilder(caseData, callbackParams));

            CaseDocument judgeDecision;
            if (judicialDecisionMakeOrder.getOrderText() != null
                && judicialDecisionMakeOrder.getMakeAnOrder().equals(APPROVE_OR_EDIT)) {
                judgeDecision = generalOrderGenerator.generate(
                    caseDataBuilder.build(),
                    callbackParams.getParams().get(BEARER_TOKEN).toString()
                );
                log.info("General order is generated for caseId: {}", caseData.getCcdCaseReference());
                caseDataBuilder.judicialMakeOrderDocPreview(judgeDecision.getDocumentLink());
            } else if (judicialDecisionMakeOrder.getDirectionsText() != null
                && judicialDecisionMakeOrder.getMakeAnOrder().equals(GIVE_DIRECTIONS_WITHOUT_HEARING)) {
                judgeDecision = directionOrderGenerator.generate(
                    caseDataBuilder.build(),
                    callbackParams.getParams().get(BEARER_TOKEN).toString()
                );
                log.info("Direction order is generated for caseId: {}", caseData.getCcdCaseReference());
                caseDataBuilder.judicialMakeOrderDocPreview(judgeDecision.getDocumentLink());
            } else if (judicialDecisionMakeOrder.getMakeAnOrder().equals(DISMISS_THE_APPLICATION)) {
                judgeDecision = dismissalOrderGenerator.generate(
                    caseDataBuilder.build(),
                    callbackParams.getParams().get(BEARER_TOKEN).toString()
                );
                log.info("Dismissal order is generated for caseId: {}", caseData.getCcdCaseReference());
                caseDataBuilder.judicialMakeOrderDocPreview(judgeDecision.getDocumentLink());
            }
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }

    public List<String> validateUrgencyDates(GAJudicialMakeAnOrder judicialDecisionMakeOrder) {
        List<String> errors = new ArrayList<>();

        if (GIVE_DIRECTIONS_WITHOUT_HEARING.equals(judicialDecisionMakeOrder.getMakeAnOrder())
            && judicialDecisionMakeOrder.getDirectionsResponseByDate() != null) {
            LocalDate directionsResponseByDate = judicialDecisionMakeOrder.getDirectionsResponseByDate();
            if (LocalDate.now().isAfter(directionsResponseByDate)) {
                errors.add(RESPOND_TO_DIRECTIONS_DATE_IN_PAST);
            }
        }
        return errors;
    }

    private CallbackResponse gaValidateMakeAnOrder(CallbackParams callbackParams) {

        GeneralApplicationCaseData caseData = callbackParams.getGeneralApplicationCaseData();
        GeneralApplicationCaseData caseDataBuilder = caseData.copy();

        caseDataBuilder.judicialDecisionMakeOrder(makeAnOrderBuilder(caseData, callbackParams));

        caseDataBuilder
            .judicialDecisionRequestMoreInfo(buildRequestMoreInfo(caseData));

        ArrayList<String> errors = new ArrayList<>();

        if ((caseData.getApplicationIsUncloakedOnce() == null
            && helper.isLipApplicationCreatedWithoutNoticeByApplicant(caseData).equals(YES)
            && caseData.getJudicialDecision().getDecision().equals(MAKE_ORDER_FOR_WRITTEN_REPRESENTATIONS)
            && gaForLipService.isGaForLip(caseData))
            || (caseData.getApplicationIsUncloakedOnce() == null
            && helper.isApplicationCreatedWithoutNoticeByApplicant(caseData).equals(YES)
            && caseData.getJudicialDecision().getDecision().equals(MAKE_ORDER_FOR_WRITTEN_REPRESENTATIONS)
            && !gaForLipService.isGaForLip(caseData))
            || (caseData.getApplicationIsUncloakedOnce() != null
            && caseData.getJudicialDecision().getDecision().equals(MAKE_ORDER_FOR_WRITTEN_REPRESENTATIONS)
            && caseData.getApplicationIsUncloakedOnce().equals(NO))) {
            errors.add("The application needs to be uncloaked before requesting written representations");
        }

        /*
         * Set showRequestInfoPreview to NO as it caches and display Request More Information Document in Hearing screen
         *  */
        caseDataBuilder.showRequestInfoPreviewDoc(NO);

        caseDataBuilder.caseNameHmctsInternal(getAllPartyNames(caseData));
        caseDataBuilder.judicialDecisionRequestMoreInfo(
            new GAJudicialRequestMoreInfo().setJudgeRequestMoreInfoByDate(deadlinesCalculator
                .getJudicialOrderDeadlineDate(LocalDateTime.now(), PLUS_7DAYS))
        );

        caseDataBuilder.orderOnCourtInitiative(new FreeFormOrderValues()
                                                   .setOnInitiativeSelectionTextArea(ON_INITIATIVE_SELECTION_TEST)
                                                   .setOnInitiativeSelectionDate(deadlinesCalculator
                                                                                  .getJudicialOrderDeadlineDate(
                                                                                      LocalDateTime.now(), PLUS_7DAYS)));
        caseDataBuilder.orderWithoutNotice(new FreeFormOrderValues()
                                               .setWithoutNoticeSelectionTextArea(WITHOUT_NOTICE_SELECTION_TEXT)
                                               .setWithoutNoticeSelectionDate(deadlinesCalculator
                                                                               .getJudicialOrderDeadlineDate(
                                                                                   LocalDateTime.now(), PLUS_7DAYS)));
        caseDataBuilder.judicialDecisionMakeAnOrderForWrittenRepresentations(
            new GAJudicialWrittenRepresentations()
                .setWrittenConcurrentRepresentationsBy(deadlinesCalculator
                                                        .getJudicialOrderDeadlineDate(
                                                            LocalDateTime.now(),
                                                            14
                                                        ))
                .setWrittenSequentailRepresentationsBy(deadlinesCalculator
                                                        .getJudicialOrderDeadlineDate(
                                                            LocalDateTime.now(),
                                                            14
                                                        ))
                .setSequentialApplicantMustRespondWithin(deadlinesCalculator
                                                          .getJudicialOrderDeadlineDate(
                                                              LocalDateTime.now(),
                                                              21
                                                          )));

        caseDataBuilder.bilingualHint(setBilingualHint(caseData));

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .errors(errors)
            .build();
    }

    private YesOrNo setBilingualHint(GeneralApplicationCaseData caseData) {
        if (Objects.nonNull(caseData.getJudicialDecision())) {
            switch (caseData.getJudicialDecision().getDecision()) {
                case LIST_FOR_A_HEARING:
                case FREE_FORM_ORDER:
                case MAKE_ORDER_FOR_WRITTEN_REPRESENTATIONS:
                    return gaForLipService.anyWelshNotice(caseData) ? YES : NO;
                case MAKE_AN_ORDER:
                case REQUEST_MORE_INFO:
                    return gaForLipService.anyWelsh(caseData) ? YES : NO;
                default:
                    return NO;
            }
        }
        return NO;
    }

    private CallbackResponse gaValidateRequestMoreInfoScreen(CallbackParams callbackParams) {
        GeneralApplicationCaseData caseData = callbackParams.getGeneralApplicationCaseData();
        GeneralApplicationCaseData caseDataBuilder = caseData.copy();

        GAJudicialRequestMoreInfo judicialRequestMoreInfo = caseData.getJudicialDecisionRequestMoreInfo();

        GAJudicialRequestMoreInfo gaJudicialRequestMoreInfo = new GAJudicialRequestMoreInfo()
            .setRequestMoreInfoOption(judicialRequestMoreInfo.getRequestMoreInfoOption())
            .setJudgeRequestMoreInfoText(judicialRequestMoreInfo.getJudgeRequestMoreInfoText())
            .setJudgeRequestMoreInfoByDate(judicialRequestMoreInfo.getJudgeRequestMoreInfoByDate())
            .setDeadlineForMoreInfoSubmission(judicialRequestMoreInfo.getDeadlineForMoreInfoSubmission())
            .setIsWithNotice(judicialRequestMoreInfo.getIsWithNotice())
            .setJudgeRecitalText(judicialRequestMoreInfo.getJudgeRecitalText());

        if (judicialRequestMoreInfo.getIsWithNotice() == null && caseData.getApplicationIsUncloakedOnce() == null) {

            if (caseData.getGeneralAppRespondentAgreement().getHasAgreed().equals(NO)) {
                gaJudicialRequestMoreInfo
                    .setIsWithNotice(caseData.getGeneralAppInformOtherParty().getIsWithNotice());

            } else if (caseData.getGeneralAppRespondentAgreement().getHasAgreed().equals(YES)) {
                gaJudicialRequestMoreInfo.setIsWithNotice(YES);

            }
        }

        if ((judicialRequestMoreInfo.getIsWithNotice() != null
            && judicialRequestMoreInfo.getIsWithNotice().equals(YES))
            ||
            (caseData.getJudicialDecisionRequestMoreInfo() != null
                && caseData.getJudicialDecisionRequestMoreInfo().getRequestMoreInfoOption()
                .equals(REQUEST_MORE_INFORMATION))) {

            caseDataBuilder.showRequestInfoPreviewDoc(YES);

        } else {

            caseDataBuilder.showRequestInfoPreviewDoc(NO);

        }

        List<String> errors = validateDatesForRequestMoreInfoScreen(caseData, judicialRequestMoreInfo);

        caseDataBuilder
            .judicialDecisionRequestMoreInfo(gaJudicialRequestMoreInfo);

        CaseDocument judgeDecision;

        /*
         * Generate Request More Information preview Doc if it's without notice application and Request More Info
         * OR General Application is With notice
         * */

        if ((judicialRequestMoreInfo.getIsWithNotice() != null
            && judicialRequestMoreInfo.getIsWithNotice().equals(YES))
            ||
            (judicialRequestMoreInfo.getJudgeRequestMoreInfoByDate() != null
                && judicialRequestMoreInfo.getJudgeRequestMoreInfoText() != null
                && caseData.getJudicialDecisionRequestMoreInfo().getRequestMoreInfoOption()
                .equals(REQUEST_MORE_INFORMATION))) {

            judgeDecision = requestForInformationGenerator.generate(
                caseDataBuilder.build(),
                callbackParams.getParams().get(BEARER_TOKEN).toString()
            );
            log.info("Request for information is generated for caseId: {}", caseData.getCcdCaseReference());

            caseDataBuilder.judicialRequestMoreInfoDocPreview(judgeDecision.getDocumentLink());
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }

    public List<String> validateDatesForRequestMoreInfoScreen(GeneralApplicationCaseData caseData,
                                                              GAJudicialRequestMoreInfo judicialRequestMoreInfo) {
        List<String> errors = new ArrayList<>();
        if (REQUEST_MORE_INFO.equals(caseData.getJudicialDecision().getDecision())
            && REQUEST_MORE_INFORMATION.equals(judicialRequestMoreInfo.getRequestMoreInfoOption())
            || judicialRequestMoreInfo.getRequestMoreInfoOption() == null) {
            if (judicialRequestMoreInfo.getJudgeRequestMoreInfoByDate() == null) {
                errors.add(REQUESTED_MORE_INFO_BY_DATE_REQUIRED);
            } else {
                if (LocalDate.now().isAfter(judicialRequestMoreInfo.getJudgeRequestMoreInfoByDate())) {
                    errors.add(REQUESTED_MORE_INFO_BY_DATE_IN_PAST);
                }
            }
        }
        return errors;
    }

    private GeneralApplicationCaseData getSharedData(CallbackParams callbackParams) {
        GeneralApplicationCaseData caseData = callbackParams.getGeneralApplicationCaseData();
        // second idam call is workaround for null pointer when hiding field in getIdamEmail callback
        return caseData.copy();
    }

    private CallbackResponse gaPopulateFinalOrderPreviewDoc(final CallbackParams callbackParams) {
        GeneralApplicationCaseData caseData = callbackParams.getGeneralApplicationCaseData();
        GeneralApplicationCaseData caseDataBuilder = caseData.copy();
        CaseDocument freeform = gaFreeFormOrderGenerator
            .generate(
                caseData,
                callbackParams.getParams().get(BEARER_TOKEN).toString()
            );
        caseDataBuilder.gaFinalOrderDocPreview(freeform.getDocumentLink());

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }

    private CallbackResponse setJudgeBusinessProcess(CallbackParams callbackParams) {
        GeneralApplicationCaseData dataBuilder = getSharedData(callbackParams);
        GeneralApplicationCaseData caseData = callbackParams.getGeneralApplicationCaseData();

        if (caseData.getJudicialDecision().getDecision().name().equals(JUDICIAL_DECISION_LIST_FOR_HEARING)
            && caseData.getJudicialListForHearing().getHearingPreferredLocation() != null) {
            GAJudgesHearingListGAspec gaJudgesHearingListGAspec = caseData.getJudicialListForHearing().copy()
                .setHearingPreferredLocation(populateJudicialHearingLocation(caseData));
            GeneralApplicationCaseData updatedCaseData = caseData.copy().judicialListForHearing(
                    gaJudgesHearingListGAspec)
                .build();
            caseData = updatedCaseData;
            dataBuilder = updatedCaseData.copy();
        }
        String caseId = caseData.getCcdCaseReference().toString();
        dataBuilder.businessProcess(BusinessProcess.readyGa(MAKE_DECISION)).build();

        var isApplicationUncloaked = isApplicationContinuesCloakedAfterJudicialDecision(caseData);
        if (Objects.isNull(isApplicationUncloaked)
            && helper.isApplicationCreatedWithoutNoticeByApplicant(caseData).equals(NO)) {
            dataBuilder.applicationIsCloaked(NO);
        } else {
            dataBuilder.applicationIsCloaked(isApplicationUncloaked);
        }

        if (isApplicationUncloaked != null
            && isApplicationUncloaked.equals(NO)) {
            dataBuilder.applicationIsUncloakedOnce(YES);
            assignCaseToResopondentSolHelper.assignCaseToRespondentSolicitor(caseData, caseId);
        } else if (caseData.getIsGaRespondentOneLip() == YES) {
            /*
             * Assign case respondent solicitors if LiP respondent so they can access application and order doc
             * */
            assignCaseToResopondentSolHelper.assignCaseToRespondentSolicitor(caseData, caseId);
        }

        if (Objects.nonNull(caseData.getJudicialMakeOrderDocPreview())) {
            dataBuilder.judicialMakeOrderDocPreview(null);
        }

        if (Objects.nonNull(caseData.getJudicialListHearingDocPreview())) {
            dataBuilder.judicialListHearingDocPreview(null);
        }

        if (Objects.nonNull(caseData.getJudicialWrittenRepDocPreview())) {
            dataBuilder.judicialWrittenRepDocPreview(null);
        }

        if (Objects.nonNull(caseData.getJudicialRequestMoreInfoDocPreview())) {
            dataBuilder.judicialRequestMoreInfoDocPreview(null);
        }

        if (Objects.nonNull(caseData.getJudicialListForHearing())) {
            GAJudgesHearingListGAspec judicialListForHearing = caseData.getJudicialListForHearing().copy();

            dataBuilder.judicialListForHearing(judicialListForHearing.setJudgeHearingCourtLocationText1(null)
                                                   .setJudgeHearingTimeEstimateText1(null)
                                                   .setHearingPreferencesPreferredTypeLabel1(null)
                                                   .setJudgeHearingSupportReqText1(null));
        }

        dataBuilder.bilingualHint(null);
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(dataBuilder.build().toMap(objectMapper))
            .build();
    }

    private DynamicList populateJudicialHearingLocation(GeneralApplicationCaseData caseData) {
        DynamicList dynamicLocationList;
        String applicationLocationLabel = caseData.getJudicialListForHearing()
            .getHearingPreferredLocation().getValue().getLabel();
        dynamicLocationList = fromList(List.of(applicationLocationLabel));
        Optional<DynamicListElement> first = dynamicLocationList.getListItems().stream()
            .filter(l -> l.getLabel().equals(applicationLocationLabel)).findFirst();
        first.ifPresent(dynamicLocationList::setValue);
        return dynamicLocationList;
    }

    private SubmittedCallbackResponse buildConfirmation(CallbackParams callbackParams) {
        GeneralApplicationCaseData caseData = callbackParams.getGeneralApplicationCaseData();
        GAJudicialDecision judicialDecision = caseData.getJudicialDecision();
        if (judicialDecision == null || judicialDecision.getDecision() == null) {
            throw new IllegalArgumentException(JUDICIAL_MISSING_DATA);
        }
        String confirmationHeader = "# Your order has been made";
        String body = "<br/><br/>";
        if (REQUEST_MORE_INFO.equals(judicialDecision.getDecision())) {
            GAJudicialRequestMoreInfo requestMoreInfo = caseData.getJudicialDecisionRequestMoreInfo();
            if (requestMoreInfo != null) {
                if (REQUEST_MORE_INFORMATION.equals(requestMoreInfo.getRequestMoreInfoOption())
                    || requestMoreInfo.getRequestMoreInfoOption() == null) {
                    confirmationHeader = constructHeaderAndBody(requestMoreInfo, "header");
                    body = constructHeaderAndBody(requestMoreInfo, "body");
                } else if (SEND_APP_TO_OTHER_PARTY.equals(requestMoreInfo.getRequestMoreInfoOption())) {
                    confirmationHeader = "# You have requested a response";
                    body = "<br/><p>The parties will be notified.</p>";
                }
            } else {
                throw new IllegalArgumentException(JUDICIAL_MISSING_DATA);
            }
        }
        return SubmittedCallbackResponse.builder()
            .confirmationHeader(confirmationHeader)
            .confirmationBody(body)
            .build();
    }

    private String constructHeaderAndBody(GAJudicialRequestMoreInfo requestMoreInfo, String type) {

        if (requestMoreInfo.getJudgeRequestMoreInfoByDate() != null) {
            if (type.equals("header")) {
                return "# You have requested more information";
            }
            return "<br/><p>The applicant will be notified. They will need to provide a response by "
                + DATE_FORMATTER_SUBMIT_CALLBACK.format(requestMoreInfo.getJudgeRequestMoreInfoByDate())
                + "</p>";
        } else {
            throw new IllegalArgumentException(JUDICIAL_MISSING_DATA);
        }
    }

    private CallbackResponse gaValidateWrittenRepresentationsDate(CallbackParams callbackParams) {
        GeneralApplicationCaseData caseData = callbackParams.getGeneralApplicationCaseData();
        GAJudicialWrittenRepresentations judicialWrittenRepresentationsDate =
            caseData.getJudicialDecisionMakeAnOrderForWrittenRepresentations();

        List<String> errors;
        errors = caseData.getJudicialDecisionMakeAnOrderForWrittenRepresentations() != null
            ? judicialDecisionWrittenRepService.validateWrittenRepresentationsDates(judicialWrittenRepresentationsDate)
            : Collections.emptyList();

        GeneralApplicationCaseData caseDataBuilder = caseData.copy();
        if (caseData.getJudicialDecisionMakeAnOrderForWrittenRepresentations().getWrittenOption()
            .equals(SEQUENTIAL_REPRESENTATIONS)) {
            caseDataBuilder.judicialSequentialDateText(getJudicalSequentialDatePupulatedText(caseData)).build();
            caseDataBuilder.judicialApplicanSequentialDateText(
                getJudicalApplicantSequentialDatePupulatedText(caseData)).build();
        } else {
            caseDataBuilder.judicialConcurrentDateText(getJudicalConcurrentDatePupulatedText(caseData)).build();
        }

        caseDataBuilder.orderCourtOwnInitiativeForWrittenRep(new GAOrderCourtOwnInitiativeGAspec()
                                                                 .setOrderCourtOwnInitiative(ORDER_COURT_OWN_INITIATIVE)
                                                                 .setOrderCourtOwnInitiativeDate(deadlinesCalculator
                                                                                                     .getJudicialOrderDeadlineDate(
                                                                                                         LocalDateTime.now(),
                                                                                                         PLUS_7DAYS
                                                                                                     )))
            .orderWithoutNoticeForWrittenRep(new GAOrderWithoutNoticeGAspec()
                                                 .setOrderWithoutNoticeDate(deadlinesCalculator
                                                                                .getJudicialOrderDeadlineDate(
                                                                                    LocalDateTime.now(),
                                                                                    PLUS_7DAYS
                                                                                ))
                                                 .setOrderWithoutNotice(ORDER_WITHOUT_NOTICE))
            .build();

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .errors(errors)
            .build();
    }

    private CallbackResponse gaPopulateWrittenRepPreviewDoc(CallbackParams callbackParams) {

        GeneralApplicationCaseData caseData = callbackParams.getGeneralApplicationCaseData();
        GAJudicialWrittenRepresentations judicialWrittenRepresentationsDate =
            caseData.getJudicialDecisionMakeAnOrderForWrittenRepresentations();

        List<String> errors = ofNullable(validateCourtsInitiativeDatesForWrittenRep(caseData))
            .orElse(Collections.emptyList());

        GeneralApplicationCaseData caseDataBuilder = caseData.copy();

        CaseDocument judgeDecision;

        if (caseData.getJudicialDecisionMakeAnOrderForWrittenRepresentations() != null
            && caseData.getJudicialDecisionMakeAnOrderForWrittenRepresentations()
            .getWrittenOption().equals(SEQUENTIAL_REPRESENTATIONS)
            && caseData.getJudicialDecisionMakeAnOrderForWrittenRepresentations()
            .getWrittenSequentailRepresentationsBy() != null
            && judicialWrittenRepresentationsDate.getSequentialApplicantMustRespondWithin() != null) {

            judgeDecision = writtenRepresentationSequentialOrderGenerator.generate(
                caseDataBuilder.build(),
                callbackParams.getParams().get(BEARER_TOKEN).toString()
            );

            log.info(
                "Written representation sequential order is generated for caseId: {}",
                caseData.getCcdCaseReference()
            );
            caseDataBuilder.judicialWrittenRepDocPreview(judgeDecision.getDocumentLink());

        } else if (caseData.getJudicialDecisionMakeAnOrderForWrittenRepresentations() != null
            && caseData.getJudicialDecisionMakeAnOrderForWrittenRepresentations()
            .getWrittenOption().equals(CONCURRENT_REPRESENTATIONS)
            && caseData.getJudicialDecisionMakeAnOrderForWrittenRepresentations()
            .getWrittenConcurrentRepresentationsBy() != null) {

            judgeDecision = writtenRepresentationConcurrentOrderGenerator.generate(
                caseDataBuilder.build(),
                callbackParams.getParams().get(BEARER_TOKEN).toString()
            );

            log.info(
                "Written representation concurrent order is generated for caseId: {}",
                caseData.getCcdCaseReference()
            );
            caseDataBuilder.judicialWrittenRepDocPreview(judgeDecision.getDocumentLink());

        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .errors(errors)
            .build();
    }

    public List<String> validateCourtsInitiativeDatesForWrittenRep(GeneralApplicationCaseData caseData) {
        List<String> errors = new ArrayList<>();
        GAByCourtsInitiativeGAspec gaByCourtsInitiativeGAspec = caseData.getJudicialByCourtsInitiativeForWrittenRep();
        if (gaByCourtsInitiativeGAspec.equals(OPTION_1)
            && LocalDate.now()
            .isAfter(caseData.getOrderCourtOwnInitiativeForWrittenRep().getOrderCourtOwnInitiativeDate())) {
            errors.add(MAKE_DECISION_APPROVE_BY_DATE_IN_PAST);
        }

        if (gaByCourtsInitiativeGAspec.equals(OPTION_2)
            && LocalDate.now()
            .isAfter(caseData.getOrderWithoutNoticeForWrittenRep().getOrderWithoutNoticeDate())) {
            errors.add(MAKE_DECISION_APPROVE_BY_DATE_IN_PAST);
        }
        return errors;
    }

    private CallbackResponse gaValidateHearingOrder(CallbackParams callbackParams) {
        GeneralApplicationCaseData caseData = callbackParams.getGeneralApplicationCaseData();
        List<String> errors = new ArrayList<>();
        String preferredType = caseData.getJudicialListForHearing().getHearingPreferencesPreferredType().name();
        if (preferredType.equals(PREFERRED_TYPE_IN_PERSON)
            && (caseData.getJudicialListForHearing().getHearingPreferredLocation() == null)) {
            errors.add(PREFERRED_LOCATION_REQUIRED);
        }
        GeneralApplicationCaseData caseDataBuilder = caseData.copy();
        caseDataBuilder.judicialHearingGeneralOrderHearingText(getJudgeHearingPrePopulatedText(caseData))
            .judicialHearingGOHearingReqText(populateJudgeGOSupportRequirement(caseData))
            .judicialGeneralOrderHearingEstimationTimeText(getJudgeHearingTimeEstPrePopulatedText(caseData))
            .orderCourtOwnInitiativeListForHearing(new GAOrderCourtOwnInitiativeGAspec()
                                                       .setOrderCourtOwnInitiative(ORDER_COURT_OWN_INITIATIVE)
                                                       .setOrderCourtOwnInitiativeDate(deadlinesCalculator
                                                                                           .getJudicialOrderDeadlineDate(
                                                                                               LocalDateTime.now(),
                                                                                               PLUS_7DAYS
                                                                                           )))
            .orderWithoutNoticeListForHearing(new GAOrderWithoutNoticeGAspec()
                                                  .setOrderWithoutNoticeDate(deadlinesCalculator
                                                                                  .getJudicialOrderDeadlineDate(
                                                                                      LocalDateTime.now(),
                                                                                      PLUS_7DAYS
                                                                                  ))
                                                  .setOrderWithoutNotice(ORDER_WITHOUT_NOTICE))
            .build();

        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }

    private CallbackResponse gaPopulateHearingOrderDoc(CallbackParams callbackParams) {

        GeneralApplicationCaseData caseData = callbackParams.getGeneralApplicationCaseData();
        GeneralApplicationCaseData caseDataBuilder = caseData.copy();

        List<String> errors = ofNullable(validateCourtsInitiativeDatesForHearing(caseData))
            .orElse(Collections.emptyList());

        CaseDocument judgeDecision;
        if (caseData.getJudicialListForHearing() != null) {
            judgeDecision = hearingOrderGenerator.generate(
                caseDataBuilder.build(),
                callbackParams.getParams().get(BEARER_TOKEN).toString()
            );
            caseDataBuilder.judicialListHearingDocPreview(judgeDecision.getDocumentLink());
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .errors(errors)
            .build();
    }

    public List<String> validateCourtsInitiativeDatesForHearing(GeneralApplicationCaseData caseData) {
        List<String> errors = new ArrayList<>();
        GAByCourtsInitiativeGAspec gaByCourtsInitiativeGAspec = caseData.getJudicialByCourtsInitiativeListForHearing();
        if (gaByCourtsInitiativeGAspec.equals(OPTION_1)
            && LocalDate.now()
            .isAfter(caseData.getOrderCourtOwnInitiativeListForHearing().getOrderCourtOwnInitiativeDate())) {
            errors.add(MAKE_DECISION_APPROVE_BY_DATE_IN_PAST);
        }

        if (gaByCourtsInitiativeGAspec.equals(OPTION_2)
            && LocalDate.now()
            .isAfter(caseData.getOrderWithoutNoticeListForHearing().getOrderWithoutNoticeDate())) {
            errors.add(MAKE_DECISION_APPROVE_BY_DATE_IN_PAST);
        }
        return errors;
    }

    private String getJudgeHearingPrePopulatedText(GeneralApplicationCaseData caseData) {
        return format(
            JUDICIAL_HEARING_TYPE,
            caseData.getJudicialListForHearing().getHearingPreferencesPreferredType().getDisplayedValue()
                .concat(".")
        );
    }

    private String populateJudgeGOSupportRequirement(GeneralApplicationCaseData caseData) {

        StringJoiner supportReq = new StringJoiner(", ");

        if (caseData.getJudicialListForHearing().getJudicialSupportRequirement() != null) {
            caseData.getJudicialListForHearing().getJudicialSupportRequirement()
                .forEach(sr -> {
                    supportReq.add(sr.getDisplayedValue());
                });

            return format(
                JUDICIAL_HEARING_REQ, supportReq);
        }

        return "";
    }

    private String getJudgeHearingTimeEstPrePopulatedText(GeneralApplicationCaseData caseData) {
        return format(
            JUDICIAL_TIME_ESTIMATE, timeEstimateHelper.getEstimatedHearingLength(caseData));
    }

    private String getJudicalSequentialDatePupulatedText(GeneralApplicationCaseData caseData) {
        return format(
            JUDICIAL_SEQUENTIAL_DATE, formatLocalDate(
                caseData.getJudicialDecisionMakeAnOrderForWrittenRepresentations()
                    .getWrittenSequentailRepresentationsBy(), DATE
            )
        );
    }

    private String getJudicalApplicantSequentialDatePupulatedText(GeneralApplicationCaseData caseData) {
        return format(
            JUDICIAL_SEQUENTIAL_APPLICANT_DATE,
            formatLocalDate(
                caseData.getJudicialDecisionMakeAnOrderForWrittenRepresentations()
                    .getSequentialApplicantMustRespondWithin(), DATE
            )
        );
    }

    private String getJudicalConcurrentDatePupulatedText(GeneralApplicationCaseData caseData) {
        return format(
            JUDICIAL_CONCURRENT_DATE, formatLocalDate(
                caseData.getJudicialDecisionMakeAnOrderForWrittenRepresentations()
                    .getWrittenConcurrentRepresentationsBy(), DATE
            )
        );
    }

    public String getRespondentHearingPreference(GeneralApplicationCaseData caseData) {

        return caseData.getRespondentsResponses() == null
            ? StringUtils.EMPTY : caseData.getRespondentsResponses()
            .stream().iterator().next().getValue().getGaHearingDetails().getHearingPreferencesPreferredType()
            .getDisplayedValue();

    }

    private String getJudgeHearingPrefType(GeneralApplicationCaseData caseData, YesOrNo isAppAndRespSameHearingPref) {
        String respondent1HearingType = null;
        String respondent2HearingType = null;

        if (caseData.getRespondentsResponses() != null && caseData.getRespondentsResponses().size() == 1) {
            return isAppAndRespSameHearingPref == YES ? format(
                JUDICIAL_PREF_TYPE_TEXT_2, caseData
                    .getGeneralAppHearingDetails().getHearingPreferencesPreferredType().getDisplayedValue()
            )
                : format(
                JUDICIAL_PREF_TYPE_TEXT_1, caseData.getGeneralAppHearingDetails()
                    .getHearingPreferencesPreferredType().getDisplayedValue(), getRespondentHearingPreference(caseData)
            );
        }

        if (caseData.getRespondentsResponses() != null && caseData.getRespondentsResponses().size() == 2) {
            Optional<Element<GARespondentResponse>> responseElementOptional1 = response1(caseData);
            Optional<Element<GARespondentResponse>> responseElementOptional2 = response2(caseData);
            if (responseElementOptional1.isPresent()) {
                respondent1HearingType = responseElementOptional1.get().getValue()
                    .getGaHearingDetails().getHearingPreferencesPreferredType().getDisplayedValue();
            }
            if (responseElementOptional2.isPresent()) {
                respondent2HearingType = responseElementOptional2.get().getValue()
                    .getGaHearingDetails().getHearingPreferencesPreferredType().getDisplayedValue();
            }
            return format(
                JUDICIAL_PREF_TYPE_TEXT_3, caseData.getGeneralAppHearingDetails()
                    .getHearingPreferencesPreferredType().getDisplayedValue(),
                respondent1HearingType, respondent2HearingType
            );
        }

        if ((caseData.getGeneralAppUrgencyRequirement() != null
            && caseData.getGeneralAppUrgencyRequirement().getGeneralAppUrgency() == YesOrNo.YES)
            || caseData.getGeneralAppHearingDetails().getHearingPreferencesPreferredType() != null) {
            return APPLICANT_PREFERS.concat(caseData
                                                .getGeneralAppHearingDetails().getHearingPreferencesPreferredType()
                                                .getDisplayedValue());
        }

        return StringUtils.EMPTY;
    }

    private String getJudgeHearingTimeEst(GeneralApplicationCaseData caseData, YesOrNo isAppAndRespSameTimeEst) {
        GAHearingDuration applicantHearingDuration = caseData.getGeneralAppHearingDetails().getHearingDuration();
        if (caseData.getRespondentsResponses() != null && caseData.getRespondentsResponses().size() == 1) {
            GAHearingDuration respondentHearingDuration =
                caseData.getRespondentsResponses().get(0).getValue().getGaHearingDetails().getHearingDuration();
            if (isAppAndRespSameTimeEst == YES) {
                return format(JUDICIAL_TIME_EST_TEXT_BOTH, applicantHearingDuration.getDisplayedValue());
            }
            if (applicantHearingDuration == null && respondentHearingDuration == null) {
                return ESTIMATES_NOT_PROVIDED;
            }
            String hearingTimeEst = StringUtils.EMPTY;
            if (applicantHearingDuration != null) {
                hearingTimeEst += format(APPLICANT_ESTIMATES, applicantHearingDuration.getDisplayedValue());
            }
            if (respondentHearingDuration != null) {
                hearingTimeEst += ((hearingTimeEst.length() > 0 ? ". " : StringUtils.EMPTY)
                    + format(RESPONDENT_ESTIMATES, respondentHearingDuration.getDisplayedValue()));
            }
            return hearingTimeEst + (hearingTimeEst.contains(".") ? "." : StringUtils.EMPTY);
        }

        if (caseData.getRespondentsResponses() != null && caseData.getRespondentsResponses().size() == 2) {
            Optional<Element<GARespondentResponse>> responseElementOptional1 = response1(caseData);
            Optional<Element<GARespondentResponse>> responseElementOptional2 = response2(caseData);
            GAHearingDuration respondent1HearingDuration = responseElementOptional1.map(Element::getValue)
                .map(GARespondentResponse::getGaHearingDetails).map(GAHearingDetails::getHearingDuration).orElse(null);
            GAHearingDuration respondent2HearingDuration = responseElementOptional2.map(Element::getValue)
                .map(GARespondentResponse::getGaHearingDetails).map(GAHearingDetails::getHearingDuration).orElse(null);
            if (applicantHearingDuration == null && respondent1HearingDuration == null && respondent2HearingDuration == null) {
                return ESTIMATES_NOT_PROVIDED;
            }
            String hearingTimeEst = StringUtils.EMPTY;
            if (applicantHearingDuration != null) {
                hearingTimeEst += format(APPLICANT_ESTIMATES, applicantHearingDuration.getDisplayedValue());
            }
            if (respondent1HearingDuration != null) {
                hearingTimeEst += ((hearingTimeEst.length() > 0 ? ". " : StringUtils.EMPTY)
                    + format(RESPONDENT1_ESTIMATES, respondent1HearingDuration.getDisplayedValue()));
            }
            if (respondent2HearingDuration != null) {
                hearingTimeEst += ((hearingTimeEst.length() > 0 ? ". " : StringUtils.EMPTY)
                    + format(RESPONDENT2_ESTIMATES, respondent2HearingDuration.getDisplayedValue()));
            }
            return hearingTimeEst + (hearingTimeEst.contains(".") ? "." : StringUtils.EMPTY);
        }

        if ((caseData.getGeneralAppUrgencyRequirement() != null
            && caseData.getGeneralAppUrgencyRequirement().getGeneralAppUrgency() == YesOrNo.YES)
            || applicantHearingDuration != null) {
            return applicantHearingDuration != null
                ? format(APPLICANT_ESTIMATES, applicantHearingDuration.getDisplayedValue())
                : ESTIMATES_NOT_PROVIDED;
        }

        return StringUtils.EMPTY;
    }

    private String getJudgeVulnerabilityText(GeneralApplicationCaseData caseData) {
        YesOrNo applicantVulnerabilityResponse = caseData.getGeneralAppHearingDetails()
            .getVulnerabilityQuestionsYesOrNo();

        int responseCount = caseData.getRespondentsResponses() != null ? caseData.getRespondentsResponses().size() : 0;

        boolean hasRespondentVulnerabilityResponded =
            hasRespondentVulnerabilityResponded(responseCount, caseData);

        boolean hasRespondent1VulnerabilityResponded =
            hasRespondent1VulnerabilityResponded(responseCount, caseData);

        boolean hasRespondent2VulnerabilityResponded =
            hasRespondent2VulnerabilityResponded(responseCount, caseData);

        if (applicantVulnerabilityResponse == YES) {
            String response = getApplicantVulnerabilityResponseYes(
                caseData,
                hasRespondentVulnerabilityResponded,
                hasRespondent1VulnerabilityResponded,
                hasRespondent2VulnerabilityResponded
            );
            if (Objects.nonNull(response)) {
                return response;
            }
        }
        if (applicantVulnerabilityResponse == NO) {
            String response = getApplicantVulnerabilityResponseNo(
                caseData,
                //hasRespondentVulnerabilityResponded,
                hasRespondent1VulnerabilityResponded,
                hasRespondent2VulnerabilityResponded
            );
            if (Objects.nonNull(response)) {
                return response;
            }
        }
        return getDefaultVulnerabilityRespond(responseCount, caseData);
    }

    private String getApplicantVulnerabilityResponseYes(GeneralApplicationCaseData caseData,
                                                        boolean hasRespondentVulnerabilityResponded,
                                                        boolean hasRespondent1VulnerabilityResponded,
                                                        boolean hasRespondent2VulnerabilityResponded) {
        if (hasRespondentVulnerabilityResponded) {
            return JUDICIAL_APPLICANT_VULNERABILITY_TEXT
                .concat(caseData.getGeneralAppHearingDetails()
                            .getVulnerabilityQuestion()
                            .concat(JUDICIAL_RESPONDENT_VULNERABILITY_TEXT)
                            .concat(caseData.getRespondentsResponses().stream().iterator().next().getValue()
                                        .getGaHearingDetails().getVulnerabilityQuestion()));
        }
        if (hasRespondent1VulnerabilityResponded
            && hasRespondent2VulnerabilityResponded) {
            Optional<Element<GARespondentResponse>> responseElementOptional1 = response1(caseData);
            Optional<Element<GARespondentResponse>> responseElementOptional2 = response2(caseData);
            if (responseElementOptional1.isPresent() && responseElementOptional2.isPresent()) {
                return JUDICIAL_APPLICANT_VULNERABILITY_TEXT
                    .concat(caseData.getGeneralAppHearingDetails()
                                .getVulnerabilityQuestion()
                                .concat(JUDICIAL_RESPONDENT1_VULNERABILITY_TEXT)
                                .concat(responseElementOptional1.get().getValue()
                                            .getGaHearingDetails().getVulnerabilityQuestion())
                                .concat(JUDICIAL_RESPONDENT2_VULNERABILITY_TEXT)
                                .concat(responseElementOptional2.get().getValue()
                                            .getGaHearingDetails().getVulnerabilityQuestion()));
            }
        }
        if (hasRespondent1VulnerabilityResponded
            && !hasRespondent2VulnerabilityResponded) {
            Optional<Element<GARespondentResponse>> responseElementOptional1 = response1(caseData);
            if (responseElementOptional1.isPresent()) {
                return JUDICIAL_APPLICANT_VULNERABILITY_TEXT
                    .concat(caseData.getGeneralAppHearingDetails()
                                .getVulnerabilityQuestion()
                                .concat(JUDICIAL_RESPONDENT1_VULNERABILITY_TEXT)
                                .concat(responseElementOptional1.get().getValue()
                                            .getGaHearingDetails().getVulnerabilityQuestion()));
            }
        }
        if (!hasRespondent1VulnerabilityResponded
            && hasRespondent2VulnerabilityResponded) {
            Optional<Element<GARespondentResponse>> responseElementOptional2 = response2(caseData);
            if (responseElementOptional2.isPresent()) {
                return JUDICIAL_APPLICANT_VULNERABILITY_TEXT
                    .concat(caseData.getGeneralAppHearingDetails()
                                .getVulnerabilityQuestion()
                                .concat(JUDICIAL_RESPONDENT2_VULNERABILITY_TEXT)
                                .concat(responseElementOptional2.get().getValue()
                                            .getGaHearingDetails().getVulnerabilityQuestion()));
            }
        }
        return null;
    }

    private String getApplicantVulnerabilityResponseNo(GeneralApplicationCaseData caseData,
                                                       //boolean hasRespondentVulnerabilityResponded,
                                                       boolean hasRespondent1VulnerabilityResponded,
                                                       boolean hasRespondent2VulnerabilityResponded) {
        if (hasRespondent1VulnerabilityResponded
            && hasRespondent2VulnerabilityResponded) {
            Optional<Element<GARespondentResponse>> responseElementOptional1 = response1(caseData);
            Optional<Element<GARespondentResponse>> responseElementOptional2 = response2(caseData);
            if (responseElementOptional1.isPresent() && responseElementOptional2.isPresent()) {
                return JUDICIAL_RESPONDENT1_VULNERABILITY_TEXT
                    .concat(responseElementOptional1.get().getValue()
                                .getGaHearingDetails().getVulnerabilityQuestion())
                    .concat(JUDICIAL_RESPONDENT2_VULNERABILITY_TEXT)
                    .concat(responseElementOptional2.get().getValue()
                                .getGaHearingDetails().getVulnerabilityQuestion());
            }
        }
        if (!hasRespondent1VulnerabilityResponded
            && hasRespondent2VulnerabilityResponded) {
            Optional<Element<GARespondentResponse>> responseElementOptional2 = response2(caseData);
            if (responseElementOptional2.isPresent()) {
                return JUDICIAL_RESPONDENT2_VULNERABILITY_TEXT
                    .concat(responseElementOptional2.get().getValue()
                                .getGaHearingDetails().getVulnerabilityQuestion());
            }
        }
        if (hasRespondent1VulnerabilityResponded
            && !hasRespondent2VulnerabilityResponded) {
            Optional<Element<GARespondentResponse>> responseElementOptional1 = response1(caseData);
            if (responseElementOptional1.isPresent()) {
                return JUDICIAL_RESPONDENT1_VULNERABILITY_TEXT
                    .concat(responseElementOptional1.get().getValue()
                                .getGaHearingDetails().getVulnerabilityQuestion());
            }
        }
        return null;
    }

    private boolean hasRespondentVulnerabilityResponded(int responseCount, GeneralApplicationCaseData caseData) {
        return responseCount == 1
            ? caseData.getRespondentsResponses().get(ONE_V_ONE).getValue()
            .getGaHearingDetails().getVulnerabilityQuestionsYesOrNo() == YES ? TRUE : FALSE
            : FALSE;
    }

    private boolean hasRespondent1VulnerabilityResponded(int responseCount, GeneralApplicationCaseData caseData) {
        return responseCount == 2
            ? caseData.getRespondentsResponses().get(ONE_V_ONE).getValue()
            .getGaHearingDetails().getVulnerabilityQuestionsYesOrNo() == YES ? TRUE : FALSE
            : FALSE;
    }

    private boolean hasRespondent2VulnerabilityResponded(int responseCount, GeneralApplicationCaseData caseData) {
        return responseCount == 2
            ? caseData.getRespondentsResponses().get(ONE_V_TWO).getValue()
            .getGaHearingDetails().getVulnerabilityQuestionsYesOrNo() == YES ? TRUE : FALSE
            : FALSE;
    }

    private String getDefaultVulnerabilityRespond(int responseCount, GeneralApplicationCaseData caseData) {
        YesOrNo applicantVulnerabilityResponse = caseData.getGeneralAppHearingDetails()
            .getVulnerabilityQuestionsYesOrNo();
        boolean hasRespondentVulnerabilityResponded =
            hasRespondentVulnerabilityResponded(responseCount, caseData);
        if (applicantVulnerabilityResponse == YES) {
            return JUDICIAL_APPLICANT_VULNERABILITY_TEXT
                .concat(caseData.getGeneralAppHearingDetails()
                            .getVulnerabilityQuestion());
        } else {
            return hasRespondentVulnerabilityResponded
                ? ltrim(JUDICIAL_RESPONDENT_VULNERABILITY_TEXT).concat(caseData.getRespondentsResponses().stream()
                                                                           .iterator().next().getValue()
                                                                           .getGaHearingDetails()
                                                                           .getVulnerabilityQuestion())
                : "No support required with regards to vulnerability";
        }
    }

    private String ltrim(String str) {
        return str.replaceAll("^\\s+", EMPTY_STRING);
    }

    private String getJudgeHearingSupportReq(GeneralApplicationCaseData caseData, YesOrNo isAppAndRespSameSupportReq) {
        List<String> applicantSupportReq = Collections.emptyList();
        String resSupportReq = StringUtils.EMPTY;
        String appSupportReq = StringUtils.EMPTY;

        if (caseData.getGeneralAppHearingDetails().getSupportRequirement() != null) {
            applicantSupportReq = caseData.getGeneralAppHearingDetails().getSupportRequirement().stream()
                .map(GAHearingSupportRequirements::getDisplayedValue).toList();

            appSupportReq = String.join(", ", applicantSupportReq);
        }

        if (caseData.getRespondentsResponses() != null && caseData.getRespondentsResponses().size() == 1) {
            List<String> respondentSupportReq = Collections.emptyList();
            if (caseData.getRespondentsResponses() != null
                && caseData.getRespondentsResponses().size() == 1
                && caseData.getRespondentsResponses().get(ONE_V_ONE).getValue().getGaHearingDetails()
                .getSupportRequirement() != null) {
                respondentSupportReq
                    = caseData.getRespondentsResponses().stream().iterator().next().getValue()
                    .getGaHearingDetails().getSupportRequirement().stream()
                    .map(GAHearingSupportRequirements::getDisplayedValue)
                    .toList();

                resSupportReq = String.join(", ", respondentSupportReq);
            }

            return isAppAndRespSameSupportReq == YES ? format(JUDICIAL_SUPPORT_REQ_TEXT_2, appSupportReq)
                : format(
                JUDICIAL_SUPPORT_REQ_TEXT_1,
                constructApplicantSupportReqText(applicantSupportReq, appSupportReq),
                constructRespondentSupportReqText(respondentSupportReq, resSupportReq)
            );
        }

        if (caseData.getRespondentsResponses() != null && caseData.getRespondentsResponses().size() == 2) {
            return createJudicialSupportReqText3(caseData, appSupportReq);
        }

        if ((caseData.getGeneralAppUrgencyRequirement() != null
            && caseData.getGeneralAppUrgencyRequirement().getGeneralAppUrgency() == YesOrNo.YES)
            || caseData.getGeneralAppHearingDetails() != null) {
            return APPLICANT_REQUIRES.concat(applicantSupportReq.isEmpty() ? NO_SUPPORT : appSupportReq);
        }

        return StringUtils.EMPTY;
    }

    private String createJudicialSupportReqText3(GeneralApplicationCaseData caseData, String appSupportReq) {
        Optional<Element<GARespondentResponse>> response1 = response1(caseData);
        Optional<Element<GARespondentResponse>> response2 = response2(caseData);

        String respondentOne = retrieveSupportRequirementsFromResponse(response1);
        String respondentTwo = retrieveSupportRequirementsFromResponse(response2);
        return format(
            JUDICIAL_SUPPORT_REQ_TEXT_3,
            appSupportReq.isEmpty() ? NO_SUPPORT : appSupportReq,
            respondentOne.isEmpty() ? NO_SUPPORT : respondentOne,
            respondentTwo.isEmpty() ? NO_SUPPORT : respondentTwo
        );
    }

    public String constructApplicantSupportReqText(List<String> applicantSupportReq, String appSupportReq) {
        return format(applicantSupportReq.isEmpty() ? NO_SUPPORT : appSupportReq);
    }

    public String constructRespondentSupportReqText(List<String> respondentSupportReq, String resSupportReq) {
        return format(respondentSupportReq.isEmpty() ? NO_SUPPORT : resSupportReq);
    }

    private String retrieveSupportRequirementsFromResponse(Optional<Element<GARespondentResponse>> response) {
        if (response.isPresent()
            && response.get().getValue().getGaHearingDetails().getSupportRequirement() != null) {
            return response.get().getValue().getGaHearingDetails()
                .getSupportRequirement().stream().map(GAHearingSupportRequirements::getDisplayedValue)
                .collect(Collectors.joining(", "));
        }
        return StringUtils.EMPTY;
    }

    private String generateRespondentCourtLocationText(GeneralApplicationCaseData caseData) {

        if (caseData.getGeneralAppHearingDetails().getHearingPreferredLocation() == null
            && caseData.getRespondentsResponses() != null) {
            return generateRespondentCourtDirectionText(caseData);
        }

        if (caseData.getGeneralAppHearingDetails().getHearingPreferredLocation() != null
            && caseData.getRespondentsResponses() == null) {
            return format(
                JUDICIAL_PREF_COURT_LOC_APPLICANT_TEXT, caseData.getGeneralAppHearingDetails()
                    .getHearingPreferredLocation().getValue().getLabel()
            );
        }
        if (caseData.getGeneralAppHearingDetails().getHearingPreferredLocation() != null
            && caseData.getRespondentsResponses() != null) {

            return concat(
                concat(
                    format(
                        JUDICIAL_PREF_COURT_LOC_APPLICANT_TEXT, caseData.getGeneralAppHearingDetails()
                            .getHearingPreferredLocation().getValue().getLabel()
                    ), " "
                ),
                generateRespondentCourtDirectionText(caseData)
            ).trim();
        }

        return StringUtils.EMPTY;
    }

    public List<String> validateJudgeOrderRequestDates(GAJudicialMakeAnOrder judicialDecisionMakeOrder) {
        List<String> errors = new ArrayList<>();

        if (judicialDecisionMakeOrder.getMakeAnOrder() != null
            && APPROVE_OR_EDIT.equals(judicialDecisionMakeOrder.getMakeAnOrder())
            && judicialDecisionMakeOrder.getJudgeApproveEditOptionDate() != null) {
            LocalDate directionsResponseByDate = judicialDecisionMakeOrder.getJudgeApproveEditOptionDate();
            if (LocalDate.now().isAfter(directionsResponseByDate)) {
                errors.add(MAKE_DECISION_APPROVE_BY_DATE_IN_PAST);
            }
        }
        return errors;
    }

    public List<String> validateCourtsInitiativeDates(GAJudicialMakeAnOrder judicialDecisionMakeOrder) {
        List<String> errors = new ArrayList<>();

        if (judicialDecisionMakeOrder.getJudicialByCourtsInitiative().equals(OPTION_1)
            && LocalDate.now().isAfter(judicialDecisionMakeOrder.getOrderCourtOwnInitiativeDate())) {
            errors.add(MAKE_DECISION_APPROVE_BY_DATE_IN_PAST);
        }
        if (judicialDecisionMakeOrder.getJudicialByCourtsInitiative().equals(OPTION_2)) {
            {
                if (LocalDate.now().isAfter(judicialDecisionMakeOrder.getOrderWithoutNoticeDate())) {
                    errors.add(MAKE_DECISION_APPROVE_BY_DATE_IN_PAST);
                }
            }
        }
        return errors;
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private Optional<Element<GARespondentResponse>> response1(GeneralApplicationCaseData caseData) {
        List<Element<GARespondentResponse>> respondentResponse = caseData.getRespondentsResponses();
        String respondent1Id = caseData.getGeneralAppRespondentSolicitors().get(0).getValue().getId();
        Optional<Element<GARespondentResponse>> responseElementOptional1;
        responseElementOptional1 = respondentResponse.stream()
            .filter(res -> res.getValue() != null && res.getValue().getGaRespondentDetails()
                .equals(respondent1Id)).findAny();
        return responseElementOptional1;
    }

    private Optional<Element<GARespondentResponse>> response2(GeneralApplicationCaseData caseData) {
        List<Element<GARespondentResponse>> respondentResponse = caseData.getRespondentsResponses();
        String respondent2Id = caseData.getGeneralAppRespondentSolicitors().get(1).getValue().getId();
        Optional<Element<GARespondentResponse>> responseElementOptional2;
        responseElementOptional2 = respondentResponse.stream()
            .filter(res -> res.getValue() != null && res.getValue().getGaRespondentDetails()
                .equals(respondent2Id)).findAny();
        return responseElementOptional2;
    }

    private String generateRespondentCourtDirectionText(GeneralApplicationCaseData caseData) {
        Optional<Element<GARespondentResponse>> responseElementOptional1 = Optional.empty();
        Optional<Element<GARespondentResponse>> responseElementOptional2 = Optional.empty();

        if (caseData.getGeneralAppRespondentSolicitors() != null
            && caseData.getGeneralAppRespondentSolicitors().size() > 0) {
            log.info(
                "General app respondent has more than 0 solicitor(s) for caseId: {}",
                caseData.getCcdCaseReference()
            );
            responseElementOptional1 = response1(caseData);
        }
        if (caseData.getGeneralAppRespondentSolicitors() != null
            && caseData.getGeneralAppRespondentSolicitors().size() > 1) {
            log.info(
                "General app respondent has more than 1 solicitor(s) for caseId: {}",
                caseData.getCcdCaseReference()
            );
            responseElementOptional2 = response2(caseData);
        }
        YesOrNo hasRespondent1PreferredLocation = hasPreferredLocation(responseElementOptional1);
        YesOrNo hasRespondent2PreferredLocation = hasPreferredLocation(responseElementOptional2);

        if (responseElementOptional1.isPresent() && responseElementOptional2.isPresent()
            && hasRespondent1PreferredLocation == YES && hasRespondent2PreferredLocation == YES) {
            return concat(
                concat(
                    format(
                        JUDICIAL_PREF_COURT_LOC_RESP1_TEXT, responseElementOptional1.get()
                            .getValue().getGaHearingDetails().getHearingPreferredLocation()
                            .getValue().getLabel()
                    ), " "
                ),
                format(
                    JUDICIAL_PREF_COURT_LOC_RESP2_TEXT, responseElementOptional2.get().getValue()
                        .getGaHearingDetails().getHearingPreferredLocation().getValue().getLabel()
                )
            );
        }
        if (responseElementOptional1.isPresent() && hasRespondent1PreferredLocation == YES) {
            return format(
                JUDICIAL_PREF_COURT_LOC_RESP1_TEXT, responseElementOptional1.get().getValue()
                    .getGaHearingDetails().getHearingPreferredLocation().getValue().getLabel()
            );

        }
        if (responseElementOptional2.isPresent() && hasRespondent2PreferredLocation == YES) {
            return format(
                JUDICIAL_PREF_COURT_LOC_RESP2_TEXT, responseElementOptional2.get().getValue()
                    .getGaHearingDetails().getHearingPreferredLocation().getValue().getLabel()
            );
        }
        return StringUtils.EMPTY;
    }

    private YesOrNo hasPreferredLocation(Optional<Element<GARespondentResponse>> responseElementOptional) {
        if (responseElementOptional.isPresent() && responseElementOptional.get().getValue().getGaHearingDetails()
            != null && responseElementOptional.get().getValue().getGaHearingDetails().getHearingPreferredLocation()
            != null) {
            return YES;
        }
        return NO;
    }

    private YesOrNo isApplicationContinuesCloakedAfterJudicialDecision(GeneralApplicationCaseData caseData) {
        if (caseData.getMakeAppVisibleToRespondents() != null
            || isApplicationUncloakedForRequestMoreInformation(caseData).equals(YES)) {
            return NO;
        }
        return caseData.getApplicationIsCloaked();
    }

    private YesOrNo isApplicationUncloakedForRequestMoreInformation(GeneralApplicationCaseData caseData) {
        if (caseData.getJudicialDecisionRequestMoreInfo() != null
            && caseData.getJudicialDecisionRequestMoreInfo().getRequestMoreInfoOption() != null
            && caseData.getJudicialDecisionRequestMoreInfo()
            .getRequestMoreInfoOption().equals(SEND_APP_TO_OTHER_PARTY)) {
            return YES;
        }
        return NO;
    }

    private YesOrNo isAdditionalPaymentMade(GeneralApplicationCaseData caseData) {
        return caseData.getGeneralAppInformOtherParty().getIsWithNotice().equals(NO)
            && Objects.nonNull(caseData.getGeneralAppPBADetails())
            && Objects.nonNull(caseData.getGeneralAppPBADetails().getAdditionalPaymentDetails()) ? YES : NO;

    }
}
