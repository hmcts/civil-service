package uk.gov.hmcts.reform.civil.ga.handler.callback.user;

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
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.ga.callback.GeneralApplicationCallbackHandler;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.finalorder.AppealTypeChoiceList;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.finalorder.AppealTypeChoices;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.finalorder.AssistedOrderAppealDetails;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.finalorder.AssistedOrderCost;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.finalorder.AssistedOrderDateHeard;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.finalorder.AssistedOrderFurtherHearingDetails;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.finalorder.AssistedOrderHeardRepresentation;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.finalorder.AssistedOrderMadeDateHeardDetails;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.finalorder.ClaimantDefendantRepresentation;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.finalorder.DetailTextWithDate;
import uk.gov.hmcts.reform.civil.ga.service.GaForLipService;
import uk.gov.hmcts.reform.civil.ga.service.GeneralAppLocationRefDataService;
import uk.gov.hmcts.reform.civil.ga.service.docmosis.finalorder.AssistedOrderFormGenerator;
import uk.gov.hmcts.reform.civil.ga.service.docmosis.finalorder.FreeFormOrderGenerator;
import uk.gov.hmcts.reform.civil.ga.utils.IdamUserUtils;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.caseprogression.FreeFormOrderValues;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.lang.String.format;
import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GENERATE_DIRECTIONS_ORDER;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.ga.enums.dq.GaFinalOrderSelection.ASSISTED_ORDER;
import static uk.gov.hmcts.reform.civil.ga.enums.dq.GaFinalOrderSelection.FREE_FORM_ORDER;
import static uk.gov.hmcts.reform.civil.ga.enums.dq.HeardFromRepresentationTypes.CLAIMANT_AND_DEFENDANT;
import static uk.gov.hmcts.reform.civil.model.common.DynamicList.fromList;

@Slf4j
@Service
@RequiredArgsConstructor
public class JudicialFinalDecisionHandler extends CallbackHandler implements GeneralApplicationCallbackHandler {

    public static final String DATE_HEARD_VALIDATION = "The date entered cannot be in the future";
    public static final String DATE_RANGE_NOT_ALLOWED = "The date range in %s should not have a 'from date', that is after the 'date to'";
    public static final String PAST_DATE_NOT_ALLOWED = "The date in %s may not be before the established date";
    public static final String JUDGE_HEARD_FROM_EMPTY_LIST = "Judge Heard from: 'Claimant(s) and defendant(s)' section for %s, requires a selection to be made";
    private static final List<CaseEvent> EVENTS = Collections.singletonList(GENERATE_DIRECTIONS_ORDER);
    private static final String ON_INITIATIVE_SELECTION_TEST = "As this order was made on the court's own initiative, "
        + "any party affected by the order may apply to set aside, vary, or stay the order."
        + " Any such application must be made by 4pm on";
    private static final String WITHOUT_NOTICE_SELECTION_TEXT = "If you were not notified of the application before "
        + "this order was made, you may apply to set aside, vary, or stay the order."
        + " Any such application must be made by 4pm on";
    private static final String ORDER_ISSUED = "# Your order has been issued %n%n ## Case number %n%n # %s";
    private static final String ORDER_1_CLAI = "<br/><p>The order has been sent to: </p>"
        + "%n%n ## Claimant 1 %n%n %s";
    private static final String ORDER_1_DEF = "%n%n ## Defendant 1 %n%n %s";
    private static final String ORDER_2_DEF = "%n%n ## Defendant 2 %n%n %s";
    private static final String POPULATE_FINAL_ORDER_FORM_VALUES = "populate-finalOrder-form-values";
    private static final String POPULATE_FINAL_ORDER_PREVIEW_DOC = "populate-final-order-preview-doc";
    private static final int PLUS_7DAYS = 7;
    private final GeneralAppLocationRefDataService locationRefDataService;
    private final ObjectMapper objectMapper;
    private final FreeFormOrderGenerator gaFreeFormOrderGenerator;
    private final AssistedOrderFormGenerator assistedOrderFormGenerator;
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
            nonNull(caseData.getDefendant2PartyName())
                && (NO.equals(caseData.getRespondent2SameLegalRepresentative())
                || Objects.isNull(caseData.getRespondent2SameLegalRepresentative()))
                ? ", " + caseData.getDefendant2PartyName() : ""
        );
    }

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_START), this::setCaseName,
            callbackKey(MID, POPULATE_FINAL_ORDER_FORM_VALUES), this::populateFreeFormValues,
            callbackKey(MID, POPULATE_FINAL_ORDER_PREVIEW_DOC), this::gaPopulateFinalOrderPreviewDoc,
            callbackKey(ABOUT_TO_SUBMIT), this::setFinalDecisionBusinessProcess,
            callbackKey(SUBMITTED), this::buildConfirmation
        );
    }

    private CallbackResponse buildConfirmation(CallbackParams callbackParams) {
        var caseData = callbackParams.getGeneralApplicationCaseData();
        return SubmittedCallbackResponse.builder()
            .confirmationHeader(getHeader(caseData))
            .confirmationBody(getBody(caseData))
            .build();
    }

    private String getBody(final GeneralApplicationCaseData caseData) {
        if (nonNull(caseData.getDefendant2PartyName())
            && (NO.equals(caseData.getRespondent2SameLegalRepresentative())
            || Objects.isNull(caseData.getRespondent2SameLegalRepresentative()))) {
            return format(ORDER_1_CLAI, caseData.getClaimant1PartyName())
                + format(ORDER_1_DEF, caseData.getDefendant1PartyName())
                + format(ORDER_2_DEF, caseData.getDefendant2PartyName());

        } else {
            return format(ORDER_1_CLAI, caseData.getClaimant1PartyName())
                + format(ORDER_1_DEF, caseData.getDefendant1PartyName());
        }
    }

    private String getHeader(GeneralApplicationCaseData caseData) {
        return format(ORDER_ISSUED, getCaseNumberFormatted(caseData));
    }

    private CallbackResponse gaPopulateFinalOrderPreviewDoc(final CallbackParams callbackParams) {
        GeneralApplicationCaseData caseData = callbackParams.getGeneralApplicationCaseData();
        GeneralApplicationCaseData caseDataBuilder = caseData.copy();
        List<String> errors = validAssistedOrderForm(caseData);
        if (caseData.getFinalOrderSelection().equals(FREE_FORM_ORDER)) {
            CaseDocument freeform = gaFreeFormOrderGenerator.generate(
                caseData,
                callbackParams.getParams().get(BEARER_TOKEN).toString()
            );
            log.info("General app free form order has been generated for caseId: {}", caseData.getCcdCaseReference());
            caseDataBuilder.gaFinalOrderDocPreview(freeform.getDocumentLink());
        } else if (caseData.getFinalOrderSelection().equals(ASSISTED_ORDER) && errors.isEmpty()) {
            CaseDocument assistedOrder = assistedOrderFormGenerator.generate(
                caseData,
                callbackParams.getParams().get(BEARER_TOKEN).toString()
            );
            log.info("Assisted order has been generated for caseId: {}", caseData.getCcdCaseReference());
            caseDataBuilder.gaFinalOrderDocPreview(assistedOrder.getDocumentLink());
        }
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .errors(errors)
            .build();
    }

    private CallbackResponse setCaseName(CallbackParams callbackParams) {
        GeneralApplicationCaseData caseData = callbackParams.getGeneralApplicationCaseData();
        GeneralApplicationCaseData caseDataBuilder = caseData.copy()
            .caseNameHmctsInternal(getAllPartyNames(caseData));
        UserInfo userDetails = idamClient.getUserInfo(callbackParams.getParams().get(BEARER_TOKEN).toString());
        caseDataBuilder.judgeTitle(IdamUserUtils.getIdamUserFullName(userDetails));
        caseDataBuilder.bilingualHint(gaForLipService.anyWelshNotice(caseData) ? YES : NO);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }

    public CallbackResponse populateFreeFormValues(CallbackParams callbackParams) {
        GeneralApplicationCaseData caseData = callbackParams.getGeneralApplicationCaseData();
        GeneralApplicationCaseData caseDataBuilder = caseData.copy();

        caseDataBuilder.orderOnCourtInitiative(new FreeFormOrderValues()
                                                   .setOnInitiativeSelectionTextArea(ON_INITIATIVE_SELECTION_TEST)
                                                   .setOnInitiativeSelectionDate(deadlinesCalculator.getJudicialOrderDeadlineDate(
                                                       LocalDateTime.now(), PLUS_7DAYS)));
        caseDataBuilder.orderWithoutNotice(new FreeFormOrderValues()
                                               .setWithoutNoticeSelectionTextArea(WITHOUT_NOTICE_SELECTION_TEXT)
                                               .setWithoutNoticeSelectionDate(deadlinesCalculator.getJudicialOrderDeadlineDate(
                                                   LocalDateTime.now(), PLUS_7DAYS)));

        caseDataBuilder.orderMadeOnOwnInitiative(new DetailTextWithDate().setDetailText(ON_INITIATIVE_SELECTION_TEST)
                                                     .setDate(deadlinesCalculator.getJudicialOrderDeadlineDate(
                                                         LocalDateTime.now(), PLUS_7DAYS)));
        caseDataBuilder.orderMadeOnWithOutNotice(new DetailTextWithDate().setDetailText(WITHOUT_NOTICE_SELECTION_TEXT)
                                                     .setDate(deadlinesCalculator.getJudicialOrderDeadlineDate(
                                                         LocalDateTime.now(), PLUS_7DAYS)));

        caseDataBuilder.assistedOrderMadeDateHeardDetails(new AssistedOrderMadeDateHeardDetails()
                                                              .setSingleDateSelection(new AssistedOrderDateHeard().setSingleDate(
                                                                  LocalDate.now()))
                                                              ).build();
        caseDataBuilder.assistedOrderFurtherHearingDetails(new AssistedOrderFurtherHearingDetails()
                                                               .setDatesToAvoid(NO));
        caseDataBuilder
            .assistedOrderMakeAnOrderForCosts(
                new AssistedOrderCost()
                    .setAssistedOrderAssessmentThirdDropdownDate(
                        deadlinesCalculator
                            .getJudicialOrderDeadlineDate(LocalDateTime.now(), 14))
                    .setAssistedOrderCostsFirstDropdownDate(
                        deadlinesCalculator
                            .getJudicialOrderDeadlineDate(LocalDateTime.now(), 14))
                    .setMakeAnOrderForCostsYesOrNo(NO))
            .publicFundingCostsProtection(NO);

        LocalDate appealDeadline = deadlinesCalculator.getJudicialOrderDeadlineDate(LocalDateTime.now(), 21);
        AppealTypeChoiceList appealGrantedOptionA = new AppealTypeChoiceList()
            .setAppealGrantedRefusedDate(appealDeadline);
        AppealTypeChoiceList appealGrantedOptionB = new AppealTypeChoiceList()
            .setAppealGrantedRefusedDate(appealDeadline);
        AppealTypeChoices appealChoicesGranted = new AppealTypeChoices()
            .setAppealChoiceOptionA(appealGrantedOptionA)
            .setAppealChoiceOptionB(appealGrantedOptionB);

        AppealTypeChoiceList appealRefusedOptionA = new AppealTypeChoiceList()
            .setAppealGrantedRefusedDate(appealDeadline);
        AppealTypeChoiceList appealRefusedOptionB = new AppealTypeChoiceList()
            .setAppealGrantedRefusedDate(appealDeadline);
        AppealTypeChoices appealChoicesRefused = new AppealTypeChoices()
            .setAppealChoiceOptionA(appealRefusedOptionA)
            .setAppealChoiceOptionB(appealRefusedOptionB);

        caseDataBuilder.assistedOrderAppealDetails(new AssistedOrderAppealDetails()
                                                       .setAppealTypeChoicesForGranted(appealChoicesGranted)
                                                       .setAppealTypeChoicesForRefused(appealChoicesRefused));

        caseDataBuilder.assistedOrderRepresentation(new AssistedOrderHeardRepresentation()
                                                        .setClaimantDefendantRepresentation(new ClaimantDefendantRepresentation()
                                                                                             .setClaimantPartyName(caseData.getClaimant1PartyName())
                                                                                             .setDefendantPartyName(
                                                                                                 caseData.getDefendant1PartyName())));
        if (caseData.getIsMultiParty().equals(YES)) {
            caseDataBuilder.assistedOrderRepresentation(new AssistedOrderHeardRepresentation()
                                                            .setClaimantDefendantRepresentation(
                                                                new ClaimantDefendantRepresentation()
                                                                    .setClaimantPartyName(caseData.getClaimant1PartyName())
                                                                    .setDefendantPartyName(caseData.getDefendant1PartyName())
                                                                    .setDefendantTwoPartyName(caseData.getDefendant2PartyName())));
        }

        String authToken = callbackParams.getParams().get(BEARER_TOKEN).toString();
        DynamicList dynamicLocationList = getLocationsFromList(locationRefDataService.getCourtLocations(authToken));
        caseDataBuilder.assistedOrderFurtherHearingDetails(
            new AssistedOrderFurtherHearingDetails()
                .setHearingLocationList(populateHearingLocation(caseData))
                .setAlternativeHearingLocation(dynamicLocationList)
                .setDatesToAvoidDateDropdown(
                    new AssistedOrderDateHeard()
                        .setDatesToAvoidDates(deadlinesCalculator
                                                  .getJudicialOrderDeadlineDate(
                                                      LocalDateTime.now(), PLUS_7DAYS))
                ));

        caseDataBuilder.assistedOrderGiveReasonsYesNo(NO)
            .assistedOrderOrderedThatText(caseData.getGeneralAppDetailsOfOrder()).build();

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }

    private DynamicList populateHearingLocation(GeneralApplicationCaseData caseData) {
        return DynamicList.builder().listItems(List.of(
                DynamicListElement.builder()
                    .code("LOCATION_LIST")
                    .label(caseData.getLocationName())
                    .build(),
                DynamicListElement.builder()
                    .code("OTHER_LOCATION")
                    .label("Other location")
                    .build()
            ))
            .value(DynamicListElement.builder()
                       .code("LOCATION_LIST")
                       .label(caseData.getLocationName())
                       .build())
            .build();
    }

    private CallbackResponse setFinalDecisionBusinessProcess(CallbackParams callbackParams) {
        GeneralApplicationCaseData caseData = callbackParams.getGeneralApplicationCaseData();
        GeneralApplicationCaseData caseDataBuilder = caseData.copy();
        log.info("General app for LiP is enabled for caseId: {}", caseData.getCcdCaseReference());
        caseDataBuilder.bilingualHint(null);
        caseDataBuilder.businessProcess(BusinessProcess.readyGa(GENERATE_DIRECTIONS_ORDER)).build();

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }

    private List<String> validAssistedOrderForm(GeneralApplicationCaseData caseData) {
        List<String> errors = new ArrayList<>();
        if (caseData.getAssistedOrderMadeSelection() != null
            && caseData.getAssistedOrderMadeSelection().equals(YesOrNo.YES)
            && nonNull(caseData.getAssistedOrderMadeDateHeardDetails().getSingleDateSelection())
            && caseData.getAssistedOrderMadeDateHeardDetails().getSingleDateSelection().getSingleDate().isAfter(
            LocalDate.now())) {
            errors.add(DATE_HEARD_VALIDATION);
        } else if (caseData.getAssistedOrderMadeSelection() != null
            && caseData.getAssistedOrderMadeSelection().equals(YesOrNo.YES)
            && nonNull(caseData.getAssistedOrderMadeDateHeardDetails().getDateRangeSelection())
            && (caseData.getAssistedOrderMadeDateHeardDetails().getDateRangeSelection().getDateRangeFrom().isAfter(
            LocalDate.now())
            || caseData.getAssistedOrderMadeDateHeardDetails().getDateRangeSelection().getDateRangeTo().isAfter(
            LocalDate.now()))) {
            errors.add(DATE_HEARD_VALIDATION);
        } else if (caseData.getAssistedOrderMadeSelection() != null
            && caseData.getAssistedOrderMadeSelection().equals(YesOrNo.YES)
            && nonNull(caseData.getAssistedOrderMadeDateHeardDetails().getDateRangeSelection())
            && caseData.getAssistedOrderMadeDateHeardDetails().getDateRangeSelection().getDateRangeFrom()
            .isAfter(caseData.getAssistedOrderMadeDateHeardDetails().getDateRangeSelection().getDateRangeTo())) {
            errors.add(String.format(DATE_RANGE_NOT_ALLOWED, "Order Made"));
        }

        if (caseData.getAssistedOrderFurtherHearingDetails() != null
            && Objects.nonNull(caseData.getAssistedOrderFurtherHearingDetails().getDatesToAvoidDateDropdown())
            && Objects.nonNull(caseData.getAssistedOrderFurtherHearingDetails().getDatesToAvoidDateDropdown().getDatesToAvoidDates())
            && caseData.getAssistedOrderFurtherHearingDetails().getDatesToAvoidDateDropdown().getDatesToAvoidDates().isBefore(
            LocalDate.now())) {
            errors.add(String.format(PAST_DATE_NOT_ALLOWED, "Further Hearing"));
        }
        if (nonNull(caseData.getAssistedOrderMakeAnOrderForCosts())
            && ((nonNull(caseData.getAssistedOrderMakeAnOrderForCosts().getAssistedOrderCostsFirstDropdownDate())
            && caseData.getAssistedOrderMakeAnOrderForCosts().getAssistedOrderCostsFirstDropdownDate().isBefore(
            LocalDate.now()))
            || (nonNull(caseData.getAssistedOrderMakeAnOrderForCosts().getAssistedOrderAssessmentThirdDropdownDate())
            && caseData.getAssistedOrderMakeAnOrderForCosts().getAssistedOrderAssessmentThirdDropdownDate().isBefore(
            LocalDate.now())))) {
            errors.add(String.format(PAST_DATE_NOT_ALLOWED, "Make an order for detailed/summary costs"));
        }
        validateAssertedOrderDatesForAppeal(caseData, errors);
        validateAssertedOrderJudgeHeardForm(caseData, errors);
        return errors;
    }

    private void validateAssertedOrderJudgeHeardForm(GeneralApplicationCaseData caseData, List<String> errors) {
        if (nonNull(caseData.getAssistedOrderRepresentation())
            && caseData.getAssistedOrderRepresentation().getRepresentationType().equals(CLAIMANT_AND_DEFENDANT)) {
            if (caseData.getAssistedOrderRepresentation().getClaimantDefendantRepresentation().getClaimantRepresentation() == null) {
                errors.add(String.format(JUDGE_HEARD_FROM_EMPTY_LIST, "Claimant"));
            }
            if (caseData.getAssistedOrderRepresentation().getClaimantDefendantRepresentation().getDefendantRepresentation() == null) {
                errors.add(String.format(JUDGE_HEARD_FROM_EMPTY_LIST, "Defendant"));
            }
            if (caseData.getIsMultiParty().equals(YES)
                && (caseData.getAssistedOrderRepresentation().getClaimantDefendantRepresentation().getDefendantTwoRepresentation() == null)) {
                errors.add(String.format(JUDGE_HEARD_FROM_EMPTY_LIST, "Defendant"));
            }
        }
    }

    private void validateAssertedOrderDatesForAppeal(GeneralApplicationCaseData caseData, List<String> errors) {
        if (nonNull(caseData.getAssistedOrderAppealDetails())
            && ((nonNull(caseData.getAssistedOrderAppealDetails().getAppealTypeChoicesForGranted())
            && (nonNull(caseData.getAssistedOrderAppealDetails().getAppealTypeChoicesForGranted().getAppealChoiceOptionA()))
            && caseData.getAssistedOrderAppealDetails().getAppealTypeChoicesForGranted().getAppealChoiceOptionA().getAppealGrantedRefusedDate().isBefore(
            LocalDate.now()))
            || (nonNull(caseData.getAssistedOrderAppealDetails().getAppealTypeChoicesForGranted())
            && (nonNull(caseData.getAssistedOrderAppealDetails().getAppealTypeChoicesForGranted().getAppealChoiceOptionB()))
            && caseData.getAssistedOrderAppealDetails().getAppealTypeChoicesForGranted().getAppealChoiceOptionB().getAppealGrantedRefusedDate().isBefore(
            LocalDate.now())))) {
            errors.add(String.format(PAST_DATE_NOT_ALLOWED, "Appeal notice date"));
        }
        if (nonNull(caseData.getAssistedOrderAppealDetails())
            && ((nonNull(caseData.getAssistedOrderAppealDetails().getAppealTypeChoicesForRefused())
            && (nonNull(caseData.getAssistedOrderAppealDetails().getAppealTypeChoicesForRefused().getAppealChoiceOptionA()))
            && caseData.getAssistedOrderAppealDetails().getAppealTypeChoicesForRefused().getAppealChoiceOptionA().getAppealGrantedRefusedDate().isBefore(
            LocalDate.now()))
            || (nonNull(caseData.getAssistedOrderAppealDetails().getAppealTypeChoicesForRefused())
            && (nonNull(caseData.getAssistedOrderAppealDetails().getAppealTypeChoicesForRefused().getAppealChoiceOptionB()))
            && caseData.getAssistedOrderAppealDetails().getAppealTypeChoicesForRefused().getAppealChoiceOptionB().getAppealGrantedRefusedDate().isBefore(
            LocalDate.now())))) {
            errors.add(String.format(PAST_DATE_NOT_ALLOWED, "Appeal notice date"));
        }
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private String getCaseNumberFormatted(GeneralApplicationCaseData caseData) {
        String[] parts = caseData.getCcdCaseReference().toString().split("(?<=\\G.{4})");
        return String.join("-", parts);
    }

    private DynamicList getLocationsFromList(final List<LocationRefData> locations) {
        return fromList(locations.stream().map(location -> new StringBuilder().append(location.getSiteName())
                .append(" - ").append(location.getCourtAddress())
                .append(" - ").append(location.getPostcode()).toString())
                            .toList());
    }
}
