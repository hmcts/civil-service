package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.bankholidays.WorkingDayIndicator;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.finalorders.CostEnums;
import uk.gov.hmcts.reform.civil.enums.finalorders.HearingLengthFinalOrderList;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.HearingNotes;
import uk.gov.hmcts.reform.civil.model.caseprogression.FreeFormOrderValues;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.finalorders.AppealChoiceSecondDropdown;
import uk.gov.hmcts.reform.civil.model.finalorders.AppealGrantedRefused;
import uk.gov.hmcts.reform.civil.model.finalorders.AssistedOrderCostDetails;
import uk.gov.hmcts.reform.civil.model.finalorders.ClaimantAndDefendantHeard;
import uk.gov.hmcts.reform.civil.model.finalorders.DatesFinalOrders;
import uk.gov.hmcts.reform.civil.model.finalorders.FinalOrderAppeal;
import uk.gov.hmcts.reform.civil.model.finalorders.FinalOrderFurtherHearing;
import uk.gov.hmcts.reform.civil.model.finalorders.FinalOrderRepresentation;
import uk.gov.hmcts.reform.civil.model.finalorders.OrderAfterHearingDate;
import uk.gov.hmcts.reform.civil.model.finalorders.OrderAfterHearingDateType;
import uk.gov.hmcts.reform.civil.model.finalorders.OrderMade;
import uk.gov.hmcts.reform.civil.model.finalorders.OrderMadeOnDetails;
import uk.gov.hmcts.reform.civil.model.finalorders.OrderMadeOnDetailsOrderWithoutNotice;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.service.camunda.UpdateWaCourtLocationsService;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentHearingLocationHelper;
import uk.gov.hmcts.reform.civil.service.docmosis.caseprogression.JudgeFinalOrderGenerator;
import uk.gov.hmcts.reform.civil.service.docmosis.caseprogression.JudgeOrderDownloadGenerator;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static io.jsonwebtoken.lang.Collections.isEmpty;
import static java.lang.String.format;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GENERATE_DIRECTIONS_ORDER;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GENERATE_ORDER_NOTIFICATION;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument.toCaseDocument;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.JUDGE_FINAL_ORDER;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.UNSPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.CaseState.All_FINAL_ORDERS_ISSUED;
import static uk.gov.hmcts.reform.civil.enums.CaseState.CASE_PROGRESSION;
import static uk.gov.hmcts.reform.civil.enums.CaseState.JUDICIAL_REFERRAL;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_ONE_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.TWO_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.enums.caseprogression.FinalOrderDownloadTemplateOptions.BLANK_TEMPLATE_AFTER_HEARING;
import static uk.gov.hmcts.reform.civil.enums.caseprogression.FinalOrderDownloadTemplateOptions.BLANK_TEMPLATE_BEFORE_HEARING;
import static uk.gov.hmcts.reform.civil.enums.caseprogression.FinalOrderDownloadTemplateOptions.FIX_DATE_CCMC;
import static uk.gov.hmcts.reform.civil.enums.caseprogression.FinalOrderDownloadTemplateOptions.FIX_DATE_CMC;
import static uk.gov.hmcts.reform.civil.enums.caseprogression.FinalOrderSelection.ASSISTED_ORDER;
import static uk.gov.hmcts.reform.civil.enums.caseprogression.FinalOrderSelection.FREE_FORM_ORDER;
import static uk.gov.hmcts.reform.civil.enums.finalorders.CostEnums.CLAIMANT;
import static uk.gov.hmcts.reform.civil.enums.finalorders.CostEnums.STANDARD_BASIS;
import static uk.gov.hmcts.reform.civil.enums.finalorders.CostEnums.SUBJECT_DETAILED_ASSESSMENT;
import static uk.gov.hmcts.reform.civil.enums.finalorders.FinalOrderRepresentationList.CLAIMANT_AND_DEFENDANT;
import static uk.gov.hmcts.reform.civil.enums.finalorders.FinalOrderToggle.SHOW;
import static uk.gov.hmcts.reform.civil.model.common.DynamicList.fromList;
import static uk.gov.hmcts.reform.civil.model.finalorders.OrderAfterHearingDateType.DATE_RANGE;
import static uk.gov.hmcts.reform.civil.model.finalorders.OrderAfterHearingDateType.SINGLE_DATE;
import static uk.gov.hmcts.reform.civil.service.docmosis.caseprogression.JudgeOrderDownloadGenerator.BLANK_TEMPLATE_TO_BE_USED_AFTER_A_HEARING;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;

@Service
@RequiredArgsConstructor
public class GenerateDirectionOrderCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(GENERATE_DIRECTIONS_ORDER);
    private static final String ON_INITIATIVE_SELECTION_TEXT = "As this order was made on the court's own initiative "
        + "any party affected by the order may apply to set aside, vary or stay the order. Any such application must "
        + "be made by 4pm on";
    private static final String WITHOUT_NOTICE_SELECTION_TEXT = "If you were not notified of the application before "
        + "this order was made, you may apply to set aside, vary or stay the order. Any such application must be made "
        + "by 4pm on";
    public static final String HEADER = "## Your order has been issued \n ### Case number \n ### #%s";
    public static final String BODY_1_V_1 = """
    The order has been sent to:
    ### Claimant 1
    %s
    ### Defendant 1
    %s
        """;

    public static final String BODY_2_V_1 = """
    The order has been sent to:
    ### Claimant 1
    %s
    ### Claimant 2
    %s
    ### Defendant 1
    %s
        """;

    public static final String BODY_1_V_2 = """
    The order has been sent to:
    ### Claimant 1
    %s
    ### Defendant 1
    %s
    ### Defendant 2
    %s
        """;
    public static final String NOT_ALLOWED_DATE = "The date in %s may not be later than the established date";
    public static final String NOT_ALLOWED_DATE_RANGE = "The date range in %s may not have a 'from date', that is after the 'date to'";
    public static final String NOT_ALLOWED_DATE_PAST = "The date in %s may not be before the established date";
    public static final String JUDGE_HEARD_FROM_EMPTY = "Judge Heard from: 'Claimant(s) and defendant(s)' section for %s, requires a selection to be made";
    public static final String FURTHER_HEARING_OTHER_EMPTY = "Further hearing, Length of new hearing, Other is empty";
    public static final String FURTHER_HEARING_OTHER_ALT_LOCATION = "Further hearing alternative location required.";
    public static final String NOT_ALLOWED_FOR_CITIZEN = "This claim involves a LiP. To allocate to Small Claims or Fast Track you must use the"
        + " Standard Direction Order (SDO) otherwise use Not suitable for SDO.";
    public static final String NOT_ALLOWED_FOR_TRACK = "The Make an order event is not available for Small Claims and Fast Track cases until the track has"
        + " been allocated. You must use the Standard Direction Order (SDO) to proceed.";
    public static final String FUTURE_SINGLE_DATE_ERROR = "The date in Order made may not be later than the established date";
    public static final String FUTURE_DATE_FROM_ERROR = "The date in Order made 'Date from' may not be later than the established date";
    public static final String FUTURE_DATE_TO_ERROR = "The date in Order made 'Date to' may not be later than the established date";
    public static final String DATE_FROM_AFTER_DATE_TO_ERROR = "The date in Order made 'Date from' may not be later than the 'Date to'";
    private String defendantTwoPartyName;
    private String claimantTwoPartyName;
    public static final String APPEAL_NOTICE_DATE = "Appeal notice date";
    private final LocationReferenceDataService locationRefDataService;
    private final ObjectMapper objectMapper;
    private final JudgeFinalOrderGenerator judgeFinalOrderGenerator;
    private final JudgeOrderDownloadGenerator judgeOrderDownloadGenerator;
    private final DocumentHearingLocationHelper locationHelper;
    private String ext = "";
    private final UserService userService;
    private final WorkingDayIndicator workingDayIndicator;
    private final FeatureToggleService featureToggleService;
    private final Optional<UpdateWaCourtLocationsService> updateWaCourtLocationsService;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_START), this::nullPreviousSelections,
            callbackKey(MID, "assign-track-toggle"), this::assignTrackToggle,
            callbackKey(MID, "populate-form-values"), this::populateFormValues,
            callbackKey(MID, "create-download-template-document"), this::generateTemplate,
            callbackKey(MID, "validate-and-generate-document"), this::validateFormAndGeneratePreviewDocument,
            callbackKey(MID, "hearing-date-order"), this::addingHearingDateToTemplate,
            callbackKey(ABOUT_TO_SUBMIT), this::addGeneratedDocumentToCollection,
            callbackKey(SUBMITTED), this::buildConfirmation
        );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }
    // Final orders can be submitted multiple times, we want each one to be a "clean slate"
    // so we remove previously selected options from both Free form orders and assisted orders.
    // Exception is fields which we specifically prepopulate e.g. date fields, or specific text.

    private CallbackResponse nullPreviousSelections(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();
        if (isJudicialReferral(callbackParams)
            && caseData.isLipCase()) {
            return AboutToStartOrSubmitCallbackResponse.builder()
                .errors(List.of(NOT_ALLOWED_FOR_CITIZEN))
                .build();
        }

        if (isJudicialReferral(callbackParams) || isMultiOrIntTrack(caseData)) {
            caseDataBuilder.allowOrderTrackAllocation(YES).finalOrderTrackToggle(null);
        } else {
            caseDataBuilder.allowOrderTrackAllocation(NO);
            populateTrackToggle(caseData, caseDataBuilder);
        }

        if (featureToggleService.isWelshEnabledForMainCase()
            && (caseData.isClaimantBilingual() || caseData.isRespondentResponseBilingual())) {
            caseDataBuilder.bilingualHint(YesOrNo.YES);
        }

        caseDataBuilder.finalOrderFurtherHearingToggle(null);
        return nullPreviousSelections(caseDataBuilder);
    }

    private CallbackResponse nullPreviousSelections(CaseData.CaseDataBuilder<?, ?> caseDataBuilder) {
        caseDataBuilder.finalOrderSelection(null);
        // Free form orders
        caseDataBuilder
            .freeFormRecordedTextArea(null)
            .freeFormOrderedTextArea(null)
            .orderOnCourtsList(null)
            .freeFormHearingNotes(null);
        // Assisted orders
        caseDataBuilder
            .finalOrderMadeSelection(null).finalOrderDateHeardComplex(null)
            .finalOrderJudgePapers(null)
            .finalOrderJudgeHeardFrom(null)
            .finalOrderRepresentation(null)
            .finalOrderRecitals(null)
            .finalOrderRecitalsRecorded(null)
            .finalOrderOrderedThatText(null)
            .finalOrderFurtherHearingComplex(null)
            .assistedOrderCostList(null).assistedOrderCostsReserved(null).assistedOrderMakeAnOrderForCosts(null).assistedOrderCostsBespoke(null)
            .finalOrderAppealToggle(null).finalOrderAppealComplex(null)
            .orderMadeOnDetailsList(null).finalOrderGiveReasonsComplex(null);

        caseDataBuilder.finalOrderTrackAllocation(null)
            .finalOrderAllocateToTrack(null)
            .finalOrderIntermediateTrackComplexityBand(null)
            .finalOrderDownloadTemplateOptions(null)
            .orderAfterHearingDate(null)
            .showOrderAfterHearingDatePage(null);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }

    private CallbackResponse generateTemplate(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();

        if (!BLANK_TEMPLATE_TO_BE_USED_AFTER_A_HEARING.equals(caseData.getFinalOrderDownloadTemplateOptions().getValue().getLabel())) {
            CaseDocument documentDownload = judgeOrderDownloadGenerator.generate(caseData, callbackParams.getParams().get(BEARER_TOKEN).toString());
            caseDataBuilder.finalOrderDownloadTemplateDocument(documentDownload);
            caseDataBuilder.showOrderAfterHearingDatePage(NO);
        } else {
            caseDataBuilder.showOrderAfterHearingDatePage(YES);
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }

    private CallbackResponse addingHearingDateToTemplate(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();
        List<String> errors = validateOrderAfterHearingDates(caseData);

        if (!errors.isEmpty()) {
            return AboutToStartOrSubmitCallbackResponse.builder()
                .errors(errors)
                .build();
        }

        CaseDocument documentDownload = judgeOrderDownloadGenerator.generate(caseData, callbackParams.getParams().get(BEARER_TOKEN).toString());
        caseDataBuilder.finalOrderDownloadTemplateDocument(documentDownload);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }

    private CallbackResponse assignTrackToggle(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();
        if (NO.equals(caseData.getFinalOrderAllocateToTrack())
            && isJudicialReferral(callbackParams)
            && isSmallOrFastTrack(caseData)) {
            return AboutToStartOrSubmitCallbackResponse.builder()
                .errors(List.of(NOT_ALLOWED_FOR_TRACK))
                .build();
        }

        caseDataBuilder = populateDownloadTemplateOptions(caseDataBuilder);

        if (YES.equals(caseData.getFinalOrderAllocateToTrack())) {
            caseDataBuilder.finalOrderTrackToggle(caseData.getFinalOrderTrackAllocation().name());
        } else {
            populateTrackToggle(caseData, caseDataBuilder);
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }

    private CallbackResponse populateFormValues(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();
        if (ASSISTED_ORDER.equals(caseData.getFinalOrderSelection())) {
            String authToken = callbackParams.getParams().get(BEARER_TOKEN).toString();
            List<LocationRefData> locations = (locationRefDataService
                .getHearingCourtLocations(authToken));
            caseDataBuilder = populateFields(caseDataBuilder, locations, caseData, authToken);
        } else  {
            caseDataBuilder = populateFreeFormFields(caseDataBuilder);
        }
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }

    private CallbackResponse validateFormAndGeneratePreviewDocument(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();
        List<String> errors = new ArrayList<>();

        if (ASSISTED_ORDER.equals(caseData.getFinalOrderSelection())) {
            checkJudgeHeardFrom(caseData, errors);
            checkFieldDate(caseData, errors);
            checkFurtherHearingOther(caseData, errors);
            checkFurtherHearingOtherAlternateLocation(caseData, errors);
        }

        CaseDocument finalDocument = judgeFinalOrderGenerator.generate(
            caseData, callbackParams.getParams().get(BEARER_TOKEN).toString());
        caseDataBuilder.finalOrderDocument(finalDocument);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .errors(errors)
            .build();
    }

    private void checkFurtherHearingOther(final CaseData caseData, final List<String> errors) {
        if (caseData.getFinalOrderFurtherHearingToggle() != null
            && !caseData.getFinalOrderFurtherHearingToggle().isEmpty()
            && caseData.getFinalOrderFurtherHearingToggle().get(0).equals(SHOW)
            && caseData.getFinalOrderFurtherHearingComplex().getLengthList()
                .equals(HearingLengthFinalOrderList.OTHER)
            && Objects.isNull(caseData.getFinalOrderFurtherHearingComplex()
                .getLengthListOther())) {
            errors.add(FURTHER_HEARING_OTHER_EMPTY);
        }
    }

    private void checkFurtherHearingOtherAlternateLocation(final CaseData caseData, final List<String> errors) {
        if (caseData.getFinalOrderFurtherHearingToggle() != null
            && !caseData.getFinalOrderFurtherHearingToggle().isEmpty()
            && caseData.getFinalOrderFurtherHearingToggle().get(0).equals(SHOW)
            && caseData.getFinalOrderFurtherHearingComplex().getHearingLocationList() != null
            && caseData.getFinalOrderFurtherHearingComplex().getHearingLocationList().getValue().getCode().equals("OTHER_LOCATION")
            && caseData.getFinalOrderFurtherHearingComplex().getAlternativeHearingList() == null) {
            errors.add(FURTHER_HEARING_OTHER_ALT_LOCATION);
        }
    }

    private void checkJudgeHeardFrom(CaseData caseData, List<String> errors) {
        if (caseData.getFinalOrderRepresentation() != null
            && caseData.getFinalOrderRepresentation().getTypeRepresentationList().equals(CLAIMANT_AND_DEFENDANT)) {
            if (caseData.getFinalOrderRepresentation().getTypeRepresentationComplex().getTypeRepresentationClaimantList() == null) {
                errors.add(format(JUDGE_HEARD_FROM_EMPTY, "claimant"));
            }
            if (caseData.getFinalOrderRepresentation().getTypeRepresentationComplex().getTypeRepresentationDefendantList() == null) {
                errors.add(format(JUDGE_HEARD_FROM_EMPTY, "defendant"));
            }
            if (getMultiPartyScenario(caseData).equals(TWO_V_ONE)
                && caseData.getFinalOrderRepresentation().getTypeRepresentationComplex().getTypeRepresentationClaimantListTwo() == null) {
                errors.add(format(JUDGE_HEARD_FROM_EMPTY, "second claimant"));
            }
            if ((getMultiPartyScenario(caseData).equals(ONE_V_TWO_ONE_LEGAL_REP) || getMultiPartyScenario(caseData).equals(ONE_V_TWO_TWO_LEGAL_REP))
                && caseData.getFinalOrderRepresentation().getTypeRepresentationComplex().getTypeRepresentationDefendantTwoList() == null) {
                errors.add(format(JUDGE_HEARD_FROM_EMPTY, "second defendant"));
            }
        }
    }

    private CaseData.CaseDataBuilder<?, ?> populateFreeFormFields(CaseData.CaseDataBuilder<?, ?> builder) {
        return builder
            .orderOnCourtInitiative(FreeFormOrderValues.builder()
                                        .onInitiativeSelectionTextArea(ON_INITIATIVE_SELECTION_TEXT)
                                        .onInitiativeSelectionDate(workingDayIndicator
                                                                       .getNextWorkingDay(LocalDate.now().plusDays(7)))
                                        .build())
            .orderWithoutNotice(FreeFormOrderValues.builder()
                                    .withoutNoticeSelectionTextArea(WITHOUT_NOTICE_SELECTION_TEXT)
                                    .withoutNoticeSelectionDate(workingDayIndicator
                                                                    .getNextWorkingDay(LocalDate.now().plusDays(7)))
                                    .build());
    }

    private CaseData.CaseDataBuilder<?, ?> populateDownloadTemplateOptions(CaseData.CaseDataBuilder<?, ?> builder) {
        CaseData caseData = builder.build();
        List<String> options = new ArrayList<>();
        if (isTrack(AllocatedTrack.INTERMEDIATE_CLAIM, caseData)) {
            options.add(BLANK_TEMPLATE_AFTER_HEARING.getLabel());
            options.add(BLANK_TEMPLATE_BEFORE_HEARING.getLabel());
            options.add(FIX_DATE_CMC.getLabel());
        }
        if (isTrack(AllocatedTrack.MULTI_CLAIM, caseData)) {
            options.add(BLANK_TEMPLATE_AFTER_HEARING.getLabel());
            options.add(BLANK_TEMPLATE_BEFORE_HEARING.getLabel());
            options.add(FIX_DATE_CCMC.getLabel());
            options.add(FIX_DATE_CMC.getLabel());
        }
        return builder.finalOrderDownloadTemplateOptions(fromList(options));
    }

    private DynamicList getLocationsFromList(final List<LocationRefData> locations) {
        return fromList(locations.stream().map(location -> new StringBuilder().append(location.getSiteName())
                .append(" - ").append(location.getCourtAddress())
                .append(" - ").append(location.getPostcode()).toString())
                            .sorted()
                            .toList());
    }

    private DynamicList populateCurrentHearingLocation(CaseData caseData, String authorisation) {
        LocationRefData locationRefData = locationHelper.getHearingLocation(null, caseData, authorisation);

        return DynamicList.builder().listItems(List.of(DynamicListElement.builder()
                                   .code("LOCATION_LIST")
                                   .label(locationRefData.getSiteName())
                                   .build(),
                                                       DynamicListElement.builder()
                                   .code("OTHER_LOCATION")
                                   .label("Other location")
                                   .build()))
            .value(DynamicListElement.builder()
                       .code("LOCATION_LIST")
                       .label(locationRefData.getSiteName())
                       .build())
            .build();
    }

    private CaseData.CaseDataBuilder<?, ?> populateFields(
        CaseData.CaseDataBuilder<?, ?> builder, List<LocationRefData> locations, CaseData caseData, String authToken) {
        populateClaimant2Defendant2PartyNames(caseData);
        return builder.finalOrderDateHeardComplex(OrderMade.builder().singleDateSelection(DatesFinalOrders
                                                                               .builder().singleDate(workingDayIndicator
                                                                                                         .getNextWorkingDay(LocalDate.now()))
                                                                               .build()).build())
            .finalOrderRepresentation(FinalOrderRepresentation.builder()
                                          .typeRepresentationComplex(ClaimantAndDefendantHeard
                                                                         .builder()
                                                                         .typeRepresentationClaimantOneDynamic(caseData.getApplicant1().getPartyName())
                                                                         .typeRepresentationDefendantOneDynamic(caseData.getRespondent1().getPartyName())
                                                                         .typeRepresentationDefendantTwoDynamic(defendantTwoPartyName)
                                                                         .typeRepresentationClaimantTwoDynamic(claimantTwoPartyName)
                                                                         .build()).build())
            .finalOrderFurtherHearingComplex(
                FinalOrderFurtherHearing.builder()
                    .hearingLocationList(populateCurrentHearingLocation(caseData, authToken))
                    .alternativeHearingList(getLocationsFromList(locations))
                    .datesToAvoidDateDropdown(DatesFinalOrders.builder()
                                           .datesToAvoidDates(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusDays(7))).build()).build())
            .orderMadeOnDetailsOrderCourt(
                OrderMadeOnDetails.builder().ownInitiativeDate(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusDays(7)))
                    .ownInitiativeText(ON_INITIATIVE_SELECTION_TEXT).build())
            .orderMadeOnDetailsOrderWithoutNotice(
                OrderMadeOnDetailsOrderWithoutNotice.builder().withOutNoticeDate(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusDays(7)))
                    .withOutNoticeText(WITHOUT_NOTICE_SELECTION_TEXT).build())
            .assistedOrderMakeAnOrderForCosts(AssistedOrderCostDetails.builder()
                                                  .assistedOrderCostsFirstDropdownDate(workingDayIndicator
                                                                                           .getNextWorkingDay(LocalDate.now().plusDays(14)))
                                                  .assistedOrderAssessmentThirdDropdownDate(workingDayIndicator
                                                                                                .getNextWorkingDay(LocalDate.now().plusDays(14)))
                                                  .makeAnOrderForCostsYesOrNo(NO)
                                                  .makeAnOrderForCostsList(CLAIMANT)
                                                  .assistedOrderClaimantDefendantFirstDropdown(SUBJECT_DETAILED_ASSESSMENT)
                                                  .assistedOrderAssessmentSecondDropdownList1(STANDARD_BASIS)
                                                  .assistedOrderAssessmentSecondDropdownList2(CostEnums.NO)
                                                  .build())
            .publicFundingCostsProtection(NO)
            .finalOrderAppealComplex(FinalOrderAppeal.builder()
                                         .appealGrantedDropdown(AppealGrantedRefused.builder()
                                                                           .appealChoiceSecondDropdownA(
                                                                               AppealChoiceSecondDropdown.builder()
                                                                                   .appealGrantedRefusedDate(workingDayIndicator
                                                                                                             .getNextWorkingDay(LocalDate.now().plusDays(21)))
                                                                                   .build())
                                                                           .appealChoiceSecondDropdownB(
                                                                               AppealChoiceSecondDropdown.builder()
                                                                                   .appealGrantedRefusedDate(workingDayIndicator
                                                                                                            .getNextWorkingDay(LocalDate.now().plusDays(21)))
                                                                                   .build())
                                                                           .build())
                                         .appealRefusedDropdown(AppealGrantedRefused.builder()
                                                                    .appealChoiceSecondDropdownA(
                                                                        AppealChoiceSecondDropdown.builder()
                                                                            .appealGrantedRefusedDate(workingDayIndicator
                                                                                                          .getNextWorkingDay(LocalDate.now().plusDays(21)))
                                                                            .build())
                                                                    .appealChoiceSecondDropdownB(
                                                                        AppealChoiceSecondDropdown.builder()
                                                                            .appealGrantedRefusedDate(workingDayIndicator
                                                                                                          .getNextWorkingDay(LocalDate.now().plusDays(21)))
                                                                            .build())

                                                                    .build()).build())
            .finalOrderGiveReasonsYesNo(NO);
    }

    private void populateClaimant2Defendant2PartyNames(CaseData caseData) {
        claimantTwoPartyName = null;
        defendantTwoPartyName = null;
        MultiPartyScenario scenario = MultiPartyScenario.getMultiPartyScenario(caseData);
        if (scenario == ONE_V_TWO_ONE_LEGAL_REP || scenario == ONE_V_TWO_TWO_LEGAL_REP) {
            defendantTwoPartyName = caseData.getRespondent2().getPartyName();
        }
        if (scenario == TWO_V_ONE) {
            claimantTwoPartyName = caseData.getApplicant2().getPartyName();
        }
    }

    private void validateDate(LocalDate date, String dateDescription, String errorMessage, List<String> errors, boolean pastDate) {
        if ((nonNull(date) && pastDate && date.isBefore(LocalDate.now()))
            || (nonNull(date) && !pastDate && date.isAfter(LocalDate.now()))) {
            errors.add(String.format(errorMessage, dateDescription));
        }
    }

    private void checkFieldDate(CaseData caseData, List<String> errors) {
        // validate order made dates
        validateDate(Optional.ofNullable(caseData.getFinalOrderDateHeardComplex())
                         .map(OrderMade::getSingleDateSelection)
                         .map(DatesFinalOrders::getSingleDate).orElse(null),
                     "Order made", NOT_ALLOWED_DATE, errors, false);

        validateDate(Optional.ofNullable(caseData.getFinalOrderDateHeardComplex())
                         .map(OrderMade::getDateRangeSelection)
                         .map(DatesFinalOrders::getDateRangeFrom).orElse(null),
                     "Order made 'date from'", NOT_ALLOWED_DATE, errors, false);

        validateDate(Optional.ofNullable(caseData.getFinalOrderDateHeardComplex())
                         .map(OrderMade::getDateRangeSelection)
                         .map(DatesFinalOrders::getDateRangeTo).orElse(null),
                     "Order made 'date to'", NOT_ALLOWED_DATE, errors, false);

        if (nonNull(caseData.getFinalOrderDateHeardComplex())
            && nonNull(caseData.getFinalOrderDateHeardComplex().getDateRangeSelection())
            && caseData.getFinalOrderDateHeardComplex().getDateRangeSelection().getDateRangeFrom()
            .isAfter(caseData.getFinalOrderDateHeardComplex().getDateRangeSelection().getDateRangeTo())) {
            errors.add(String.format(NOT_ALLOWED_DATE_RANGE, "Order made"));
        }

        validateDate(Optional.ofNullable(caseData.getFinalOrderFurtherHearingComplex())
                         .map(FinalOrderFurtherHearing::getDatesToAvoidDateDropdown)
                         .map(DatesFinalOrders::getDatesToAvoidDates).orElse(null),
                     "Further hearing", NOT_ALLOWED_DATE_PAST, errors, true);

        if (nonNull(caseData.getFinalOrderFurtherHearingComplex())
            && nonNull(caseData.getFinalOrderFurtherHearingComplex().getDateToDate())
            && caseData.getFinalOrderFurtherHearingComplex().getDateToDate()
            .isBefore(caseData.getFinalOrderFurtherHearingComplex().getListFromDate())) {
            errors.add(String.format(NOT_ALLOWED_DATE_RANGE, "Further hearing"));
        }

        validateDate(Optional.ofNullable(caseData.getAssistedOrderMakeAnOrderForCosts())
                         .map(AssistedOrderCostDetails::getAssistedOrderCostsFirstDropdownDate).orElse(null),
                     "Make an order for detailed/summary costs", NOT_ALLOWED_DATE_PAST, errors, true);

        validateDate(Optional.ofNullable(caseData.getAssistedOrderMakeAnOrderForCosts())
                         .map(AssistedOrderCostDetails::getAssistedOrderAssessmentThirdDropdownDate).orElse(null),
                     "Make an order for detailed/summary costs", NOT_ALLOWED_DATE_PAST, errors, true);

        validateDate(Optional.ofNullable(caseData.getFinalOrderAppealComplex())
                         .map(FinalOrderAppeal::getAppealGrantedDropdown)
                         .map(AppealGrantedRefused::getAppealChoiceSecondDropdownA)
                         .map(AppealChoiceSecondDropdown::getAppealGrantedRefusedDate).orElse(null),
                     APPEAL_NOTICE_DATE, NOT_ALLOWED_DATE_PAST, errors, true);

        validateDate(Optional.ofNullable(caseData.getFinalOrderAppealComplex())
                         .map(FinalOrderAppeal::getAppealGrantedDropdown)
                         .map(AppealGrantedRefused::getAppealChoiceSecondDropdownB)
                         .map(AppealChoiceSecondDropdown::getAppealGrantedRefusedDate).orElse(null),
                     APPEAL_NOTICE_DATE, NOT_ALLOWED_DATE_PAST, errors, true);

        validateDate(Optional.ofNullable(caseData.getFinalOrderAppealComplex())
                         .map(FinalOrderAppeal::getAppealRefusedDropdown)
                         .map(AppealGrantedRefused::getAppealChoiceSecondDropdownA)
                         .map(AppealChoiceSecondDropdown::getAppealGrantedRefusedDate).orElse(null),
                     APPEAL_NOTICE_DATE, NOT_ALLOWED_DATE_PAST, errors, true);

        validateDate(Optional.ofNullable(caseData.getFinalOrderAppealComplex())
                         .map(FinalOrderAppeal::getAppealRefusedDropdown)
                         .map(AppealGrantedRefused::getAppealChoiceSecondDropdownB)
                         .map(AppealChoiceSecondDropdown::getAppealGrantedRefusedDate).orElse(null),
                     APPEAL_NOTICE_DATE, NOT_ALLOWED_DATE_PAST, errors, true);

    }

    private CallbackResponse addGeneratedDocumentToCollection(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        UserDetails userDetails = userService.getUserDetails(callbackParams.getParams().get(BEARER_TOKEN).toString());
        CaseDocument finalDocument = caseData.getFinalOrderDocument();

        if (caseData.getFinalOrderSelection() == null) {
            finalDocument = toCaseDocument(caseData.getUploadOrderDocumentFromTemplate(), JUDGE_FINAL_ORDER);
        }

        String judgeName = userDetails.getFullName();
        finalDocument.getDocumentLink().setCategoryID("caseManagementOrders");
        finalDocument.getDocumentLink().setDocumentFileName(getDocumentFilename(caseData, finalDocument, judgeName));
        if (caseData.getFinalOrderSelection() == null) {
            finalDocument.setDocumentName(getDocumentFilename(caseData, finalDocument, judgeName));
        }

        List<Element<CaseDocument>> finalCaseDocuments = new ArrayList<>();
        finalCaseDocuments.add(element(finalDocument));
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();
        if (featureToggleService.isWelshEnabledForMainCase()
                && (caseData.isClaimantBilingual() || caseData.isRespondentResponseBilingual())) {
            List<Element<CaseDocument>> preTranslationDocuments = caseData.getPreTranslationDocuments();
            preTranslationDocuments.addAll(finalCaseDocuments);
            caseDataBuilder.preTranslationDocuments(preTranslationDocuments);
            caseDataBuilder.bilingualHint(YesOrNo.YES);
            // Do not trigger business process when document is hidden
        } else {
            if (!isEmpty(caseData.getFinalOrderDocumentCollection())) {
                finalCaseDocuments.addAll(caseData.getFinalOrderDocumentCollection());
            }
            caseDataBuilder.finalOrderDocumentCollection(finalCaseDocuments);
        }
        caseDataBuilder.businessProcess(BusinessProcess.ready(GENERATE_ORDER_NOTIFICATION));

        // Casefileview will show any document uploaded even without an categoryID under uncategorized section,
        // we only use freeFormOrderDocument as a preview and do not want it shown on case file view, so to prevent it
        // showing, we remove.
        caseDataBuilder.finalOrderDocument(null);
        caseDataBuilder.uploadOrderDocumentFromTemplate(null);
        caseDataBuilder.finalOrderDownloadTemplateDocument(null);
        caseDataBuilder.allowOrderTrackAllocation(null);

        if (YES.equals(caseData.getFinalOrderAllocateToTrack())) {
            if (SPEC_CLAIM.equals(caseData.getCaseAccessCategory())) {
                caseDataBuilder.responseClaimTrack(caseData.getFinalOrderTrackAllocation().name());
            } else {
                caseDataBuilder.allocatedTrack(caseData.getFinalOrderTrackAllocation());
            }
        }

        if (nonNull(caseData.getFinalOrderSelection())) {
            // populate hearing notes in listing tab with hearing notes from either assisted or freeform order, if either exist.
            if (caseData.getFinalOrderSelection().equals(ASSISTED_ORDER)) {
                if (caseData.getFinalOrderFurtherHearingComplex() != null
                    && caseData.getFinalOrderFurtherHearingComplex().getHearingNotesText() != null) {
                    caseDataBuilder.hearingNotes(HearingNotes.builder()
                                                     .date(LocalDate.now())
                                                     .notes(caseData.getFinalOrderFurtherHearingComplex().getHearingNotesText())
                                                     .build());
                }
            } else if (nonNull(caseData.getFreeFormHearingNotes())) {
                caseDataBuilder.hearingNotes(HearingNotes.builder()
                                                 .date(LocalDate.now())
                                                 .notes(caseData.getFreeFormHearingNotes())
                                                 .build());
            }
        }

        if (featureToggleService.isMultiOrIntermediateTrackEnabled(caseData)) {
            updateWaCourtLocationsService.ifPresent(service -> service.updateCourtListingWALocations(
                callbackParams.getParams().get(CallbackParams.Params.BEARER_TOKEN).toString(),
                caseDataBuilder
            ));
        }
        nullPreviousSelections(caseDataBuilder);

        if (!JUDICIAL_REFERRAL.toString().equals(callbackParams.getRequest().getCaseDetails().getState())
            && ((ASSISTED_ORDER.equals(caseData.getFinalOrderSelection())
            && isNull(caseData.getFinalOrderFurtherHearingToggle()))
            || FREE_FORM_ORDER.equals(caseData.getFinalOrderSelection())
            || isMultiOrIntTrack(caseData))) {

            caseDataBuilder.finalOrderFurtherHearingToggle(null);

            return AboutToStartOrSubmitCallbackResponse.builder()
                .data(caseDataBuilder.build().toMap(objectMapper))
                .build();
        }
        CaseState state = All_FINAL_ORDERS_ISSUED;
        if ((ASSISTED_ORDER.equals(caseData.getFinalOrderSelection())
            && caseData.getFinalOrderFurtherHearingToggle() != null)
            || isJudicialReferral(callbackParams)) {
            state = CASE_PROGRESSION;
        }
        if (!ASSISTED_ORDER.equals(caseData.getFinalOrderSelection())) {
            caseDataBuilder.finalOrderFurtherHearingToggle(null);
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .state(state.name())
            .build();
    }

    private SubmittedCallbackResponse buildConfirmation(CallbackParams callbackParams) {
        var caseData = callbackParams.getCaseData();
        return SubmittedCallbackResponse.builder()
            .confirmationHeader(getHeader(caseData))
            .confirmationBody(getBody(caseData))
            .build();
    }

    private String getHeader(CaseData caseData) {
        return format(HEADER, caseData.getCcdCaseReference());
    }

    private String getBody(CaseData caseData) {
        if (caseData.getRespondent2() != null) {
            return format(BODY_1_V_2, caseData.getApplicant1().getPartyName(), caseData.getRespondent1().getPartyName(),
                          caseData.getRespondent2().getPartyName());
        }
        if ((caseData.getApplicant2() != null)) {
            return format(BODY_2_V_1, caseData.getApplicant1().getPartyName(), caseData.getApplicant2().getPartyName(),
                          caseData.getRespondent1().getPartyName());
        } else {
            return format(BODY_1_V_1, caseData.getApplicant1().getPartyName(), caseData.getRespondent1().getPartyName());
        }
    }

    private boolean isJudicialReferral(CallbackParams callbackParams) {
        return JUDICIAL_REFERRAL.toString().equals(callbackParams.getRequest().getCaseDetails().getState());
    }

    private void populateTrackToggle(CaseData caseData, CaseData.CaseDataBuilder<?, ?> caseDataBuilder) {
        if (caseData.getCaseAccessCategory().equals(SPEC_CLAIM)) {
            if (caseData.getResponseClaimTrack() != null) {
                caseDataBuilder.finalOrderTrackToggle(caseData.getResponseClaimTrack());
            } else {
                // track is null when DJ is completed, default to small/fast track journey
                // in this scenario
                caseDataBuilder.finalOrderTrackToggle(AllocatedTrack.SMALL_CLAIM.name());
            }

        }
        if (caseData.getCaseAccessCategory().equals(UNSPEC_CLAIM)) {
            caseDataBuilder.finalOrderTrackToggle(caseData.getAllocatedTrack().name());
        }
    }

    private boolean isSmallOrFastTrack(CaseData caseData) {
        if (SPEC_CLAIM.equals(caseData.getCaseAccessCategory())) {
            return AllocatedTrack.SMALL_CLAIM.name().equals(caseData.getResponseClaimTrack())
                || AllocatedTrack.FAST_CLAIM.name().equals(caseData.getResponseClaimTrack());
        } else {
            return AllocatedTrack.SMALL_CLAIM.equals(caseData.getAllocatedTrack())
                || AllocatedTrack.FAST_CLAIM.equals(caseData.getAllocatedTrack());
        }
    }

    private boolean isMultiOrIntTrack(CaseData caseData) {
        if (SPEC_CLAIM.equals(caseData.getCaseAccessCategory())) {
            return AllocatedTrack.INTERMEDIATE_CLAIM.name().equals(caseData.getResponseClaimTrack())
                || AllocatedTrack.MULTI_CLAIM.name().equals(caseData.getResponseClaimTrack());
        } else {
            return AllocatedTrack.INTERMEDIATE_CLAIM.equals(caseData.getAllocatedTrack())
                || AllocatedTrack.MULTI_CLAIM.equals(caseData.getAllocatedTrack());
        }
    }

    private boolean isTrack(AllocatedTrack track, CaseData caseData) {
        if (YES.equals(caseData.getFinalOrderAllocateToTrack())) {
            return track.equals(caseData.getFinalOrderTrackAllocation());
        } else {
            return (SPEC_CLAIM.equals(caseData.getCaseAccessCategory())
                && track.name().equals(caseData.getResponseClaimTrack()))
                || (UNSPEC_CLAIM.equals(caseData.getCaseAccessCategory())
                && track.equals(caseData.getAllocatedTrack()));
        }
    }

    private String getDocumentFilename(CaseData caseData, CaseDocument document, String judgeName) {
        StringBuilder updatedFileName = new StringBuilder();
        ext = FilenameUtils.getExtension(document.getDocumentLink().getDocumentFileName());
        updatedFileName
            .append(document.getCreatedDatetime().toLocalDate().toString());
        if (caseData.getFinalOrderSelection() == null) {
            if (BLANK_TEMPLATE_AFTER_HEARING.getLabel().equals(caseData.getFinalOrderDownloadTemplateOptions().getValue().getLabel())) {
                updatedFileName.append("_order");
            } else {
                updatedFileName.append("_directions order");
            }
        } else {
            updatedFileName
                .append("_").append(judgeName);
        }
        return updatedFileName.append(".").append(ext).toString();
    }

    private List<String> validateOrderAfterHearingDates(CaseData caseData) {
        List<String> errors = new ArrayList<>();
        LocalDate now = LocalDate.now();
        OrderAfterHearingDate orderAfterHearingDate = caseData.getOrderAfterHearingDate();
        OrderAfterHearingDateType dateType = orderAfterHearingDate.getDateType();

        if (dateType.equals(SINGLE_DATE) && orderAfterHearingDate.getDate().isAfter(now)) {
            errors.add(FUTURE_SINGLE_DATE_ERROR);
        }
        if (dateType.equals(DATE_RANGE)) {
            if (orderAfterHearingDate.getFromDate().isAfter(now)) {
                errors.add(FUTURE_DATE_FROM_ERROR);
            }
            if (orderAfterHearingDate.getToDate().isAfter(now)) {
                errors.add(FUTURE_DATE_TO_ERROR);
            }
            if (orderAfterHearingDate.getFromDate().isAfter(orderAfterHearingDate.getToDate())) {
                errors.add(DATE_FROM_AFTER_DATE_TO_ERROR);
            }
        }
        return errors;
    }
}
