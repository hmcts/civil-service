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
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.caseprogression.FreeFormOrderValues;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.finalorders.AppealChoiceSecondDropdown;
import uk.gov.hmcts.reform.civil.model.finalorders.AppealGrantedRefused;
import uk.gov.hmcts.reform.civil.model.finalorders.AssistedOrderCostDetails;
import uk.gov.hmcts.reform.civil.model.finalorders.ClaimantAndDefendantHeard;
import uk.gov.hmcts.reform.civil.model.finalorders.DatesFinalOrders;
import uk.gov.hmcts.reform.civil.model.finalorders.FinalOrderAppeal;
import uk.gov.hmcts.reform.civil.model.finalorders.FinalOrderFurtherHearing;
import uk.gov.hmcts.reform.civil.model.finalorders.FinalOrderRepresentation;
import uk.gov.hmcts.reform.civil.model.finalorders.OrderMade;
import uk.gov.hmcts.reform.civil.model.finalorders.OrderMadeOnDetails;
import uk.gov.hmcts.reform.civil.model.finalorders.OrderMadeOnDetailsOrderWithoutNotice;
import uk.gov.hmcts.reform.civil.referencedata.LocationRefDataService;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;

import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentHearingLocationHelper;
import uk.gov.hmcts.reform.civil.service.docmosis.caseprogression.JudgeFinalOrderGenerator;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static io.jsonwebtoken.lang.Collections.isEmpty;
import static java.lang.String.format;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GENERATE_DIRECTIONS_ORDER;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GENERATE_ORDER_NOTIFICATION;
import static uk.gov.hmcts.reform.civil.enums.CaseState.All_FINAL_ORDERS_ISSUED;
import static uk.gov.hmcts.reform.civil.enums.CaseState.CASE_PROGRESSION;
import static uk.gov.hmcts.reform.civil.enums.CaseState.JUDICIAL_REFERRAL;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_ONE_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.TWO_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.caseprogression.FinalOrderSelection.ASSISTED_ORDER;
import static uk.gov.hmcts.reform.civil.model.common.DynamicList.fromList;
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
    public static final String BODY_1v1 = "The order has been sent to: \n ### Claimant 1 \n %s \n ### Defendant 1 \n %s";
    public static final String BODY_2v1 = "The order has been sent to: \n ### Claimant 1 \n %s \n ### Claimant 2 \n %s"
        + "\n ### Defendant 1 \n %s";
    public static final String BODY_1v2 = "The order has been sent to: \n ### Claimant 1 \n %s \n ### Defendant 1 \n %s"
        + "\n ### Defendant 2 \n %s";
    public static final String NOT_ALLOWED_DATE = "The date in %s may not be later than the established date";
    public static final String NOT_ALLOWED_DATE_RANGE = "The date range in %s may not have a 'from date', that is after the 'date to'";
    public static final String NOT_ALLOWED_DATE_PAST = "The date in %s may not be before the established date";
    public String defendantTwoPartyName;
    public String claimantTwoPartyName;
    private final LocationRefDataService locationRefDataService;
    private final ObjectMapper objectMapper;
    private final JudgeFinalOrderGenerator judgeFinalOrderGenerator;
    private final DocumentHearingLocationHelper locationHelper;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_START), this::emptyCallbackResponse,
            callbackKey(MID, "populate-form-values"), this::populateFormValues,
            callbackKey(MID, "validate-and-generate-document"), this::validateFormAndGeneratePreviewDocument,
            callbackKey(ABOUT_TO_SUBMIT), this::addGeneratedDocumentToCollection,
            callbackKey(SUBMITTED), this::buildConfirmation
        );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse populateFormValues(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();
        if (ASSISTED_ORDER.equals(caseData.getFinalOrderSelection())) {
            String authToken = callbackParams.getParams().get(BEARER_TOKEN).toString();

            List<LocationRefData> locations = (locationRefDataService
                .getCourtLocationsForDefaultJudgments(authToken));
            caseDataBuilder = populateFields(caseDataBuilder, locations, caseData, authToken);
        } else {
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
            checkFieldDate(caseData, errors);
        }

        CaseDocument finalDocument = judgeFinalOrderGenerator.generate(
            caseData, callbackParams.getParams().get(BEARER_TOKEN).toString());
        caseDataBuilder.finalOrderDocument(finalDocument.getDocumentLink());

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .errors(errors)
            .build();
    }

    private CaseData.CaseDataBuilder<?, ?> populateFreeFormFields(CaseData.CaseDataBuilder<?, ?> builder) {
        return builder
            .orderOnCourtInitiative(FreeFormOrderValues.builder()
                                        .onInitiativeSelectionTextArea(ON_INITIATIVE_SELECTION_TEXT)
                                        .onInitiativeSelectionDate(LocalDate.now())
                                        .build())
            .orderWithoutNotice(FreeFormOrderValues.builder()
                                    .withoutNoticeSelectionTextArea(WITHOUT_NOTICE_SELECTION_TEXT)
                                    .withoutNoticeSelectionDate(LocalDate.now())
                                    .build());
    }

    private DynamicList getLocationsFromList(final List<LocationRefData> locations) {
        return fromList(locations.stream().map(location -> new StringBuilder().append(location.getSiteName())
                .append(" - ").append(location.getCourtAddress())
                .append(" - ").append(location.getPostcode()).toString())
                            .sorted()
                            .toList());
    }

    private DynamicList populateCurrentHearingLocation(CaseData caseData, String authorisation) {
        LocationRefData locationRefData;
        if (hasSDOBeenMade(caseData.getCcdState())) {
            locationRefData = locationHelper.getHearingLocation(null, caseData, authorisation);
        } else {
            locationRefData = locationRefDataService.getCcmccLocation(authorisation);
        }
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

    private boolean hasSDOBeenMade(CaseState state) {
        return !JUDICIAL_REFERRAL.equals(state);
    }

    private CaseData.CaseDataBuilder<?, ?> populateFields(
        CaseData.CaseDataBuilder<?, ?> builder, List<LocationRefData> locations, CaseData caseData, String authToken) {
        LocalDate advancedDate = LocalDate.now().plusDays(14);

        populateClaimant2Defendant2PartyNames(caseData);
        return builder.finalOrderDateHeardComplex(OrderMade.builder().singleDateSelection(DatesFinalOrders
                                                                               .builder().singleDate(LocalDate.now())
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
                                           .datesToAvoidDates(LocalDate.now().plusDays(7)).build()).build())
            .orderMadeOnDetailsOrderCourt(
                OrderMadeOnDetails.builder().ownInitiativeDate(
                    LocalDate.now()).ownInitiativeText(ON_INITIATIVE_SELECTION_TEXT).build())
            .orderMadeOnDetailsOrderWithoutNotice(
                OrderMadeOnDetailsOrderWithoutNotice.builder().withOutNoticeDate(
                    LocalDate.now()).withOutNoticeText(WITHOUT_NOTICE_SELECTION_TEXT).build())
            .assistedOrderMakeAnOrderForCosts(AssistedOrderCostDetails.builder()
                                                  .assistedOrderCostsFirstDropdownDate(advancedDate)
                                                  .assistedOrderAssessmentThirdDropdownDate(advancedDate)
                                                  .makeAnOrderForCostsYesOrNo(YesOrNo.NO)
                                                  .build())
            .publicFundingCostsProtection(YesOrNo.NO)
            .finalOrderAppealComplex(FinalOrderAppeal.builder()
                                         .appealGrantedRefusedDropdown(AppealGrantedRefused.builder()
                                                                           .appealChoiceSecondDropdownA(
                                                                               AppealChoiceSecondDropdown.builder()
                                                                                   .appealGrantedRefusedDate(LocalDate.now().plusDays(21))
                                                                                   .build())
                                                                           .appealChoiceSecondDropdownB(
                                                                               AppealChoiceSecondDropdown.builder()
                                                                                   .appealGrantedRefusedDate(LocalDate.now().plusDays(21))
                                                                                   .build())
                                                                           .build()).build());
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

    private void validateDate(LocalDate date, String dateDescription, String errorMessage, List<String> errors, Boolean pastDate) {
        if (pastDate) {
            if (nonNull(date) && date.isBefore(LocalDate.now())) {
                errors.add(String.format(errorMessage, dateDescription));
            }
        } else if (nonNull(date) && date.isAfter(LocalDate.now())) {
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

        if (nonNull(caseData.getFinalOrderDateHeardComplex())) {
            if (nonNull(caseData.getFinalOrderDateHeardComplex().getDateRangeSelection())
                && caseData.getFinalOrderDateHeardComplex().getDateRangeSelection().getDateRangeFrom()
                .isAfter(caseData.getFinalOrderDateHeardComplex().getDateRangeSelection().getDateRangeTo())) {
                errors.add(String.format(NOT_ALLOWED_DATE_RANGE, "Order made"));
            }
        }

        validateDate(Optional.ofNullable(caseData.getFinalOrderFurtherHearingComplex())
                         .map(FinalOrderFurtherHearing::getDatesToAvoidDateDropdown)
                         .map(DatesFinalOrders::getDatesToAvoidDates).orElse(null),
                     "Further hearing", NOT_ALLOWED_DATE_PAST, errors, true);

        validateDate(Optional.ofNullable(caseData.getAssistedOrderMakeAnOrderForCosts())
                         .map(AssistedOrderCostDetails::getAssistedOrderCostsFirstDropdownDate).orElse(null),
                     "Make an order for detailed/summary costs", NOT_ALLOWED_DATE_PAST, errors, true);

        validateDate(Optional.ofNullable(caseData.getAssistedOrderMakeAnOrderForCosts())
                         .map(AssistedOrderCostDetails::getAssistedOrderAssessmentThirdDropdownDate).orElse(null),
                     "Make an order for detailed/summary costs", NOT_ALLOWED_DATE_PAST, errors, true);

        validateDate(Optional.ofNullable(caseData.getFinalOrderAppealComplex())
                         .map(FinalOrderAppeal::getAppealGrantedRefusedDropdown)
                         .map(AppealGrantedRefused::getAppealChoiceSecondDropdownA)
                         .map(AppealChoiceSecondDropdown::getAppealGrantedRefusedDate).orElse(null),
                     "Appeal notice date", NOT_ALLOWED_DATE_PAST, errors, true);

        validateDate(Optional.ofNullable(caseData.getFinalOrderAppealComplex())
                         .map(FinalOrderAppeal::getAppealGrantedRefusedDropdown)
                         .map(AppealGrantedRefused::getAppealChoiceSecondDropdownB)
                         .map(AppealChoiceSecondDropdown::getAppealGrantedRefusedDate).orElse(null),
                     "Appeal notice date", NOT_ALLOWED_DATE_PAST, errors, true);

    }

    private CallbackResponse addGeneratedDocumentToCollection(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();

        CaseDocument finalDocument = judgeFinalOrderGenerator.generate(
            caseData, callbackParams.getParams().get(BEARER_TOKEN).toString());

        List<Element<CaseDocument>> finalCaseDocuments = new ArrayList<>();
        finalCaseDocuments.add(element(finalDocument));
        if (!isEmpty(caseData.getFinalOrderDocumentCollection())) {
            finalCaseDocuments.addAll(caseData.getFinalOrderDocumentCollection());
        }
        finalCaseDocuments.forEach(document -> document.getValue().getDocumentLink().setCategoryID("finalOrders"));
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();
        caseDataBuilder.finalOrderDocumentCollection(finalCaseDocuments);
        // Casefileview will show any document uploaded even without an categoryID under uncategorized section,
        // we only use freeFormOrderDocument as a preview and do not want it shown on case file view, so to prevent it
        // showing, we remove.
        caseDataBuilder.finalOrderDocument(null);
        caseDataBuilder.businessProcess(BusinessProcess.ready(GENERATE_ORDER_NOTIFICATION));

        CaseState state = All_FINAL_ORDERS_ISSUED;
        if (caseData.getFinalOrderFurtherHearingToggle() != null) {
            state = CASE_PROGRESSION;
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
            return format(BODY_1v2, caseData.getApplicant1().getPartyName(), caseData.getRespondent1().getPartyName(),
                          caseData.getRespondent2().getPartyName());
        }
        if ((caseData.getApplicant2() != null)) {
            return format(BODY_2v1, caseData.getApplicant1().getPartyName(), caseData.getApplicant2().getPartyName(),
                          caseData.getRespondent1().getPartyName());
        } else {
            return format(BODY_1v1, caseData.getApplicant1().getPartyName(), caseData.getRespondent1().getPartyName());
        }
    }
}
