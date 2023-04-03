package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.caseprogression.FreeFormOrderValues;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.finalorders.AssistedOrderCostDetails;
import uk.gov.hmcts.reform.civil.model.finalorders.FinalOrderFurtherHearing;
import uk.gov.hmcts.reform.civil.model.finalorders.OrderMade;
import uk.gov.hmcts.reform.civil.model.finalorders.OrderMadeOnDetails;
import uk.gov.hmcts.reform.civil.model.finalorders.OrderMadeOnDetailsOrderWithoutNotice;
import uk.gov.hmcts.reform.civil.referencedata.LocationRefDataService;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GENERATE_DIRECTIONS_ORDER;
import static uk.gov.hmcts.reform.civil.model.common.DynamicList.fromList;

@Service
@RequiredArgsConstructor
public class GenerateDirectionOrderCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(GENERATE_DIRECTIONS_ORDER);
    private static final String ON_INITIATIVE_SELECTION_TEST = "As this order was made on the court's own initiative "
        + "any party affected by the order may apply to set aside, vary or stay the order. Any such application must "
        + "be made by 4pm on";
    private static final String WITHOUT_NOTICE_SELECTION_TEXT = "If you were not notified of the application before "
        + "this order was made, you may apply to set aside, vary or stay the order. Any such application must be made "
        + "by 4pm on";
    public static final String COURT_OWN_INITIATIVE = "As this order was made on the court's own initiative any party" +
        " affected by the order may apply to set aside, vary or stay the order." +
        " Any such application must be made by 4pm on";
    public static final String ORDER_WITHOUT_NOTICE = "If you were not notified of the application before this order" +
        " was made, you may apply to set aside, vary or stay the order. Any such application must be made by 4pm on";
    private final LocationRefDataService locationRefDataService;
    private final ObjectMapper objectMapper;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_START), this::emptyCallbackResponse,
            callbackKey(MID, "populate-freeForm-values"), this::populateFreeFormValues,
            callbackKey(MID, "order"), this::populateOrderFields,
            callbackKey(ABOUT_TO_SUBMIT), this::emptyCallbackResponse,
            callbackKey(SUBMITTED), this::emptySubmittedCallbackResponse
        );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse populateOrderFields(CallbackParams callbackParams) {
        var caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();
        String authToken = callbackParams.getParams().get(BEARER_TOKEN).toString();

        List<LocationRefData> locations = (locationRefDataService
            .getCourtLocationsForDefaultJudgments(authToken));
        caseDataBuilder = populateFields(caseDataBuilder, locations);

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

    private CaseData.CaseDataBuilder<?, ?> populateFields(
        CaseData.CaseDataBuilder<?, ?> builder, List<LocationRefData> locations) {
        LocalDate advancedDate = LocalDate.now().plusDays(14);
        return builder.finalOrderDateHeardComplex(OrderMade.builder().date(LocalDate.now()).build())
            .assistedOrderCostsDefendantPaySub(
                AssistedOrderCostDetails.builder().defendantCostStandardDate(advancedDate).build())
            .assistedOrderCostsClaimantPaySub(
                AssistedOrderCostDetails.builder().claimantCostStandardDate(advancedDate).build())
            .assistedOrderCostsDefendantSum(
                AssistedOrderCostDetails.builder().defendantCostSummarilyDate(advancedDate).build())
            .assistedOrderCostsClaimantSum(
                AssistedOrderCostDetails.builder().claimantCostSummarilyDate(advancedDate).build())
            .finalOrderFurtherHearingComplex(
                FinalOrderFurtherHearing.builder().alternativeHearingList(getLocationsFromList(locations)).build())
            .orderMadeOnDetailsOrderCourt(
                OrderMadeOnDetails.builder().ownInitiativeDate(
                    LocalDate.now()).ownInitiativeText(COURT_OWN_INITIATIVE).build())
            .orderMadeOnDetailsOrderWithoutNotice(
                OrderMadeOnDetailsOrderWithoutNotice.builder().withOutNoticeDate(
                    LocalDate.now()).withOutNoticeText(ORDER_WITHOUT_NOTICE).build());
    }


    public CallbackResponse populateFreeFormValues(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder caseDataBuilder = caseData.toBuilder();

        caseDataBuilder.orderOnCourtInitiative(FreeFormOrderValues.builder()
                                                   .onInitiativeSelectionTextArea(ON_INITIATIVE_SELECTION_TEST)
                                                   .onInitiativeSelectionDate(LocalDate.now())
                                                   .build());
        caseDataBuilder.orderWithoutNotice(FreeFormOrderValues.builder()
                                                   .withoutNoticeSelectionTextArea(WITHOUT_NOTICE_SELECTION_TEXT)
                                                   .withoutNoticeSelectionDate(LocalDate.now())
                                                   .build());

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }

}
