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
import uk.gov.hmcts.reform.civil.enums.caseprogression.FinalOrderSelection;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.caseprogression.FreeFormOrderValues;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.finalorders.AppealGrantedRefused;
import uk.gov.hmcts.reform.civil.model.finalorders.AssistedOrderCostDetails;
import uk.gov.hmcts.reform.civil.model.finalorders.FinalOrderAppeal;
import uk.gov.hmcts.reform.civil.model.finalorders.FinalOrderFurtherHearing;
import uk.gov.hmcts.reform.civil.model.finalorders.OrderMade;
import uk.gov.hmcts.reform.civil.model.finalorders.OrderMadeOnDetails;
import uk.gov.hmcts.reform.civil.model.finalorders.OrderMadeOnDetailsOrderWithoutNotice;
import uk.gov.hmcts.reform.civil.referencedata.LocationRefDataService;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;

import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.service.docmosis.caseprogression.JudgeFinalOrderGenerator;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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
    private final LocationRefDataService locationRefDataService;
    private final ObjectMapper objectMapper;
    private final JudgeFinalOrderGenerator judgeFinalOrderGenerator;

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
            caseDataBuilder = populateFields(caseDataBuilder, locations);
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
                    LocalDate.now()).ownInitiativeText(ON_INITIATIVE_SELECTION_TEXT).build())
            .orderMadeOnDetailsOrderWithoutNotice(
                OrderMadeOnDetailsOrderWithoutNotice.builder().withOutNoticeDate(
                    LocalDate.now()).withOutNoticeText(WITHOUT_NOTICE_SELECTION_TEXT).build())
            .finalOrderAppealComplex(FinalOrderAppeal.builder()
                                         .appealGranted(
                AppealGrantedRefused.builder().appealDate(LocalDate.now().plusDays(21)).build())
                                         .appealRefused(
                AppealGrantedRefused.builder().refusedText("[name] court")
                    .appealDate(LocalDate.now().plusDays(21)).build()).build());
    }

    private void checkFieldDate(CaseData caseData, List<String> errors) {
        if (nonNull(caseData.getFinalOrderDateHeardComplex())
            && caseData.getFinalOrderDateHeardComplex().getDate().isAfter(LocalDate.now())) {
            errors.add(String.format(NOT_ALLOWED_DATE, "Order Made"));
        }
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
        caseDataBuilder.freeFormOrderDocument(null);
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
