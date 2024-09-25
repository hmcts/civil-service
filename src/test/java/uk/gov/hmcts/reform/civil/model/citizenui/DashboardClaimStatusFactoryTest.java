package uk.gov.hmcts.reform.civil.model.citizenui;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import uk.gov.hmcts.reform.ccd.client.model.CaseEventDetail;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.FeeType;
import uk.gov.hmcts.reform.civil.enums.PaymentStatus;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.Bundle;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.IdValue;
import uk.gov.hmcts.reform.civil.model.PaymentDetails;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DashboardClaimStatusFactoryTest {

    enum OrderType {
        OFFICER_ORDER,
        /**
         * directions order ending in Case_Progression.
         */
        DIRECTIONS_ORDER_CP,
        /**
         * Directions order ending in All_final_orders_issued.
         */
        DIRECTIONS_ORDER_ALL
    }

    /**
     * Generates arguments to interweave order creation in paths followed by cases.
     *
     * @param howManyPositions how many positions we are going to insert the order creation in
     * @return a list of arguments with all numbers in [0, howManyPositions] combined with both true and false
     */
    static Stream<Arguments> positionAndOrderTypeArguments(int howManyPositions) {
        List<Arguments> argumentsList = new ArrayList<>();
        for (int i = 0; i < howManyPositions + 1; i++) {
            for (OrderType ot : OrderType.values()) {
                argumentsList.add(Arguments.arguments(i, ot));
            }
        }
        return argumentsList.stream();
    }

    private final DashboardClaimStatusFactory claimStatusFactory = new DashboardClaimStatusFactory();
    private final FeatureToggleService toggleService = Mockito.mock(FeatureToggleService.class);

    @BeforeEach
    void prepare() {
        Mockito.when(toggleService.isCaseProgressionEnabled()).thenReturn(true);
    }

    @ParameterizedTest
    @MethodSource("hearingFeePaidArguments")
    void shouldReturnCorrectStatus_hearingFeePaid(int orderPosition, OrderType orderType) {
        List<CaseEventDetail> eventHistory = new ArrayList<>();
        CaseData caseData = fastClaim(eventHistory);
        caseData = passDays(caseData, eventHistory, 1);
        caseData = applyOrderIfPosition(1, orderPosition, orderType,
                                        caseData, eventHistory
        );

        caseData = passDays(caseData, eventHistory, 1);
        caseData = scheduleHearing(caseData, eventHistory);
        // 56 days to hearing
        caseData = applyOrderIfPosition(2, orderPosition, orderType,
                                        caseData, eventHistory
        );
        caseData = requestHwF(caseData, eventHistory);
        caseData = applyOrderIfPosition(3, orderPosition, orderType,
                                        caseData, eventHistory
        );
        caseData = invalidHwFReferenceNumber(caseData, eventHistory);
        caseData = applyOrderIfPosition(4, orderPosition, orderType,
                                        caseData, eventHistory
        );
        caseData = updatedHwFReferenceNumber(caseData, eventHistory);
        caseData = applyOrderIfPosition(5, orderPosition, orderType,
                                        caseData, eventHistory
        );
        caseData = moreInformationRequiredHwF(caseData, eventHistory);
        caseData = applyOrderIfPosition(6, orderPosition, orderType,
                                        caseData, eventHistory
        );
        caseData = hwfRejected(caseData, eventHistory);
        caseData = applyOrderIfPosition(7, orderPosition, orderType,
                                        caseData, eventHistory
        );
        caseData = payHearingFee(caseData, eventHistory);
        caseData = applyOrderIfPosition(8, orderPosition, orderType,
                                        caseData, eventHistory
        );
        // times passes until there's only 6 weeks to hearing
        caseData = passDays(caseData, eventHistory, 14);
        shouldRequireTrialArrangements(caseData, eventHistory);
        applyOrderIfPosition(9, orderPosition, orderType,
                             caseData, eventHistory
        );
    }

    static Stream<Arguments> hearingFeePaidArguments() {
        return positionAndOrderTypeArguments(9);
    }

    @ParameterizedTest
    @MethodSource("awaitingJudgmentArguments")
    void shouldReturnCorrectStatus_awaitingJudgment(int orderPosition, OrderType orderType) {
        List<CaseEventDetail> eventHistory = new ArrayList<>();
        CaseData caseData = fastClaim(eventHistory);
        caseData = applyOrderIfPosition(1, orderPosition, orderType,
                                        caseData, eventHistory
        );
        caseData = scheduleHearing(caseData, eventHistory);
        caseData = applyOrderIfPosition(2, orderPosition, orderType,
                                        caseData, eventHistory
        );
        // 56 days to hearing
        caseData = requestHwF(caseData, eventHistory);
        caseData = applyOrderIfPosition(3, orderPosition, orderType,
                                        caseData, eventHistory
        );
        caseData = hwfFull(caseData, eventHistory, DashboardClaimStatus.HEARING_FORM_GENERATED);
        caseData = applyOrderIfPosition(4, orderPosition, orderType,
                                        caseData, eventHistory
        );

        // wait until 6 weeks to hearing
        caseData = passDays(caseData, eventHistory, 14);
        caseData = applyOrderIfPosition(5, orderPosition, orderType,
                                        caseData, eventHistory
        );
        caseData = submitClaimantHearingArrangements(caseData, eventHistory);
        caseData = applyOrderIfPosition(6, orderPosition, orderType,
                                        caseData, eventHistory
        );
        caseData = submitDefendantHearingArrangements(caseData, eventHistory);
        caseData = applyOrderIfPosition(7, orderPosition, orderType,
                                        caseData, eventHistory
        );

        // wait until 3 weeks to hearing
        caseData = passDays(caseData, eventHistory, 21);
        caseData = createBundle(caseData, eventHistory);
        caseData = applyOrderIfPosition(8, orderPosition, orderType,
                                        caseData, eventHistory
        );
        awaitingJudgment(caseData, eventHistory);
    }

    static Stream<Arguments> awaitingJudgmentArguments() {
        return positionAndOrderTypeArguments(8);
    }

    @ParameterizedTest
    @MethodSource("feeNotPaidArguments")
    void shouldReturnCorrectStatus_feeNotPaid(int orderPosition, OrderType orderType) {
        List<CaseEventDetail> eventHistory = new ArrayList<>();
        CaseData caseData = smallClaim(eventHistory);
        caseData = applyOrderIfPosition(1, orderPosition, orderType,
                                        caseData, eventHistory
        );
        caseData = scheduleHearing(caseData, eventHistory);
        caseData = applyOrderIfPosition(2, orderPosition, orderType,
                                        caseData, eventHistory
        );
        caseData = requestHwF(caseData, eventHistory);
        caseData = applyOrderIfPosition(3, orderPosition, orderType,
                                        caseData, eventHistory
        );
        caseData = hwfPartial(caseData, eventHistory, DashboardClaimStatus.HEARING_FORM_GENERATED);
        caseData = applyOrderIfPosition(4, orderPosition, orderType,
                                        caseData, eventHistory
        );
        doNotPayHearingFee(caseData, eventHistory);
    }

    static Stream<Arguments> feeNotPaidArguments() {
        return positionAndOrderTypeArguments(4);
    }

    private CaseData applyOrderIfPosition(int position, int valueToApply, OrderType orderType,
                                          CaseData caseData,
                                          List<CaseEventDetail> eventHistory) {
        if (position == valueToApply) {
            if (orderType == OrderType.OFFICER_ORDER) {
                return courtOfficerOrder(caseData, LocalDateTime.now(), eventHistory);
            } else {
                return generateDirectionOrder(caseData, LocalDateTime.now(), eventHistory, orderType);
            }
        }
        return caseData;
    }

    private CaseData fastClaim(List<CaseEventDetail> eventHistory) {
        eventHistory.add(CaseEventDetail.builder()
                             .createdDate(LocalDateTime.now())
                             .id(CaseEvent.CREATE_SDO.name())
                             .build());
        CaseDocument sdoDocument = CaseDocument.builder()
            .documentType(DocumentType.SDO_ORDER)
            .createdDatetime(LocalDateTime.now())
            .build();
        CaseData caseData = CaseData.builder()
            .ccdState(CaseState.CASE_PROGRESSION)
            .responseClaimTrack(AllocatedTrack.FAST_CLAIM.name())
            .totalClaimAmount(BigDecimal.valueOf(1000))
            .systemGeneratedCaseDocuments(List.of(Element.<CaseDocument>builder()
                                                      .value(sdoDocument).build()))
            .build();
        checkStatus(caseData, eventHistory,
                    DashboardClaimStatus.SDO_ORDER_CREATED,
                    DashboardClaimStatus.SDO_ORDER_CREATED
        );
        return caseData;
    }

    /**
     * Used because sometimes tests fail because of run conditions, when the times are equal
     *
     * @param caseData                case data to test
     * @param eventHistory            events from case data
     * @param claimantExpectedStatus  status for claimant
     * @param defendantExpectedStatus status for defendant
     */
    private void checkStatus(CaseData caseData, List<CaseEventDetail> eventHistory,
                             DashboardClaimStatus claimantExpectedStatus,
                             DashboardClaimStatus defendantExpectedStatus) {
        Assertions.assertEquals(
            defendantExpectedStatus,
            claimStatusFactory.getDashboardClaimStatus(new CcdDashboardDefendantClaimMatcher(
                caseData,
                toggleService, eventHistory
            ))
        );
        Assertions.assertEquals(
            claimantExpectedStatus,
            claimStatusFactory.getDashboardClaimStatus(new CcdDashboardClaimantClaimMatcher(
                caseData,
                toggleService, eventHistory
            ))
        );
    }

    private CaseData scheduleHearing(CaseData previous, List<CaseEventDetail> eventHistory) {
        LocalDate hearingDate = LocalDate.now().plusDays(8 * 7);
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = previous.toBuilder()
            .hearingDate(hearingDate);

        eventHistory.add(CaseEventDetail.builder()
                             .createdDate(LocalDateTime.now())
                             .id(CaseEvent.HEARING_SCHEDULED.name())
                             .build());

        CaseDocument hearingForm = CaseDocument.builder()
            .createdDatetime(LocalDateTime.now())
            .documentType(DocumentType.HEARING_FORM)
            .build();
        List<Element<CaseDocument>> systemGenerated = new ArrayList<>(caseDataBuilder.build()
                                                                          .getSystemGeneratedCaseDocuments());
        systemGenerated.add(Element.<CaseDocument>builder().value(hearingForm).build());
        caseDataBuilder.systemGeneratedCaseDocuments(systemGenerated);
        CaseData caseData = caseDataBuilder.build();
        checkStatus(caseData, eventHistory,
                    DashboardClaimStatus.HEARING_FORM_GENERATED, DashboardClaimStatus.HEARING_FORM_GENERATED);

        return caseData;
    }

    private void shouldRequireTrialArrangements(CaseData caseData, List<CaseEventDetail> eventHistory) {
        checkStatus(caseData, eventHistory,
                    DashboardClaimStatus.TRIAL_ARRANGEMENTS_REQUIRED,
                    DashboardClaimStatus.TRIAL_ARRANGEMENTS_REQUIRED);
    }

    private CaseData createBundle(CaseData previous, List<CaseEventDetail> eventHistory) {
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = previous.toBuilder()
            .caseBundles(List.of(new IdValue<>(
                "bundle1",
                Bundle.builder()
                    .bundleHearingDate(Optional.of(previous.getHearingDate()))
                    .createdOn(Optional.of(LocalDateTime.now()))
                    .build()
            )));

        DashboardClaimStatus expectedStatus = DashboardClaimStatus.BUNDLE_CREATED;
        CaseData caseData = caseDataBuilder.build();
        checkStatus(caseData, eventHistory,
                    expectedStatus,
                    expectedStatus);

        return caseData;
    }

    private CaseData requestHwF(CaseData previous, List<CaseEventDetail> eventHistory) {
        eventHistory.add(CaseEventDetail.builder()
                             .id(CaseEvent.APPLY_HELP_WITH_HEARING_FEE.name())
                             .createdDate(LocalDateTime.now())
                             .build());
        CaseData caseData = previous.toBuilder()
            .hwfFeeType(FeeType.HEARING)
            .build();
        DashboardClaimStatus defendantStatus = claimStatusFactory.getDashboardClaimStatus(
            new CcdDashboardDefendantClaimMatcher(previous, toggleService, eventHistory)
        );
        checkStatus(caseData, eventHistory,
                    DashboardClaimStatus.HEARING_SUBMIT_HWF,
                    defendantStatus);
        return caseData;
    }

    private CaseData invalidHwFReferenceNumber(CaseData previous, List<CaseEventDetail> eventHistory) {
        eventHistory.add(CaseEventDetail.builder()
                             .id(CaseEvent.INVALID_HWF_REFERENCE.name())
                             .createdDate(LocalDateTime.now())
                             .build());
        DashboardClaimStatus defendantStatus = claimStatusFactory.getDashboardClaimStatus(
            new CcdDashboardDefendantClaimMatcher(previous, toggleService, eventHistory)
        );
        CaseData caseData = previous.toBuilder()
            .hwfFeeType(FeeType.HEARING)
            .hearingHwfDetails(HelpWithFeesDetails.builder()
                                   .hwfCaseEvent(CaseEvent.INVALID_HWF_REFERENCE)
                                   .build())
            .build();
        checkStatus(caseData, eventHistory,
                    DashboardClaimStatus.CLAIMANT_HWF_INVALID_REF_NUMBER,
                    defendantStatus);
        return caseData;
    }

    private CaseData updatedHwFReferenceNumber(CaseData previous, List<CaseEventDetail> eventHistory) {
        eventHistory.add(CaseEventDetail.builder()
                             .id(CaseEvent.UPDATE_HELP_WITH_FEE_NUMBER.name())
                             .createdDate(LocalDateTime.now())
                             .build());
        DashboardClaimStatus defendantStatus = claimStatusFactory.getDashboardClaimStatus(
            new CcdDashboardDefendantClaimMatcher(previous, toggleService, eventHistory)
        );
        CaseData caseData = previous.toBuilder()
            .hearingHwfDetails(HelpWithFeesDetails.builder()
                                   .hwfCaseEvent(CaseEvent.UPDATE_HELP_WITH_FEE_NUMBER)
                                   .build())
            .build();
        checkStatus(caseData, eventHistory,
                    DashboardClaimStatus.CLAIMANT_HWF_UPDATED_REF_NUMBER,
                    defendantStatus);
        return caseData;
    }

    private CaseData moreInformationRequiredHwF(CaseData previous, List<CaseEventDetail> eventHistory) {
        eventHistory.add(CaseEventDetail.builder()
                             .id(CaseEvent.MORE_INFORMATION_HWF.name())
                             .createdDate(LocalDateTime.now())
                             .build());
        DashboardClaimStatus defendantStatus = claimStatusFactory.getDashboardClaimStatus(
            new CcdDashboardDefendantClaimMatcher(previous, toggleService, eventHistory)
        );
        CaseData caseData = previous.toBuilder()
            .hearingHwfDetails(HelpWithFeesDetails.builder()
                                   .hwfCaseEvent(CaseEvent.MORE_INFORMATION_HWF)
                                   .build())
            .build();
        checkStatus(caseData, eventHistory,
                    DashboardClaimStatus.HWF_MORE_INFORMATION_NEEDED,
                    defendantStatus);
        return caseData;
    }

    private CaseData hwfRejected(CaseData previous, List<CaseEventDetail> eventHistory) {
        eventHistory.add(CaseEventDetail.builder()
                             .id(CaseEvent.NO_REMISSION_HWF.name())
                             .createdDate(LocalDateTime.now())
                             .build());
        DashboardClaimStatus defendantStatus = claimStatusFactory.getDashboardClaimStatus(
            new CcdDashboardDefendantClaimMatcher(previous, toggleService, eventHistory)
        );
        CaseData caseData = previous.toBuilder()
            .hearingHwfDetails(HelpWithFeesDetails.builder()
                                   .hwfCaseEvent(CaseEvent.NO_REMISSION_HWF)
                                   .build())
            .build();
        checkStatus(caseData, eventHistory,
                    DashboardClaimStatus.CLAIMANT_HWF_NO_REMISSION,
                    defendantStatus);
        return caseData;
    }

    private CaseData payHearingFee(CaseData previous, List<CaseEventDetail> eventHistory) {
        eventHistory.add(CaseEventDetail.builder()
                             .id(CaseEvent.CITIZEN_HEARING_FEE_PAYMENT.name())
                             .createdDate(LocalDateTime.now())
                             .build());
        DashboardClaimStatus defendantStatus = claimStatusFactory.getDashboardClaimStatus(
            new CcdDashboardDefendantClaimMatcher(previous, toggleService, eventHistory)
        );
        CaseData caseData = previous.toBuilder()
            .hearingFeePaymentDetails(PaymentDetails.builder()
                                          .status(PaymentStatus.SUCCESS)
                                          .build())
            .build();
        checkStatus(caseData, eventHistory,
                    DashboardClaimStatus.CLAIMANT_HWF_FEE_PAYMENT_OUTCOME,
                    defendantStatus);
        return caseData;
    }

    private CaseData smallClaim(List<CaseEventDetail> eventHistory) {
        CaseDocument sdoDocument = CaseDocument.builder()
            .documentType(DocumentType.SDO_ORDER)
            .createdDatetime(LocalDateTime.now())
            .build();
        CaseData caseData = CaseData.builder()
            .ccdState(CaseState.CASE_PROGRESSION)
            .responseClaimTrack(AllocatedTrack.SMALL_CLAIM.name())
            .totalClaimAmount(BigDecimal.valueOf(999))
            .systemGeneratedCaseDocuments(List.of(Element.<CaseDocument>builder()
                                                      .value(sdoDocument).build()))
            .build();
        checkStatus(caseData, eventHistory,
                    DashboardClaimStatus.SDO_ORDER_LEGAL_ADVISER_CREATED,
                    DashboardClaimStatus.SDO_ORDER_LEGAL_ADVISER_CREATED);
        return caseData;
    }

    private CaseData hwfPartial(CaseData previous, List<CaseEventDetail> eventHistory,
                                // ccd state changes so order made is not valid
                                DashboardClaimStatus defendantStatus) {
        eventHistory.add(CaseEventDetail.builder()
                             .id(CaseEvent.PARTIAL_REMISSION_HWF_GRANTED.name())
                             .createdDate(LocalDateTime.now())
                             .build());
        CaseData caseData = previous.toBuilder()
            .ccdState(CaseState.HEARING_READINESS)
            .hwfFeeType(FeeType.HEARING)
            .hearingHwfDetails(HelpWithFeesDetails.builder()
                                   .hwfCaseEvent(CaseEvent.PARTIAL_REMISSION_HWF_GRANTED)
                                   .build())
            .build();
        checkStatus(caseData, eventHistory,
                    DashboardClaimStatus.CLAIMANT_HWF_PARTIAL_REMISSION,
                    defendantStatus);
        return caseData;
    }

    private void doNotPayHearingFee(CaseData previous, List<CaseEventDetail> eventHistory) {
        CaseData caseData = previous.toBuilder()
            .caseDismissedHearingFeeDueDate(LocalDateTime.now())
            .build();
        checkStatus(caseData, eventHistory,
                    DashboardClaimStatus.HEARING_FEE_UNPAID,
                    DashboardClaimStatus.HEARING_FEE_UNPAID);
    }

    private CaseData hwfFull(CaseData previous, List<CaseEventDetail> eventHistory,
                             // status changes, so it can't be order made
                             DashboardClaimStatus defendantStatus) {
        eventHistory.add(CaseEventDetail.builder()
                             .id(CaseEvent.FULL_REMISSION_HWF.name())
                             .createdDate(LocalDateTime.now())
                             .build());
        CaseData caseData = previous.toBuilder()
            .ccdState(CaseState.HEARING_READINESS)
            .hwfFeeType(FeeType.HEARING)
            .hearingHwfDetails(HelpWithFeesDetails.builder()
                                   .hwfCaseEvent(CaseEvent.FULL_REMISSION_HWF)
                                   .build())
            .build();
        checkStatus(caseData, eventHistory,
                    DashboardClaimStatus.CLAIMANT_HWF_FEE_PAYMENT_OUTCOME,
                    defendantStatus);
        return caseData;
    }

    private CaseData submitClaimantHearingArrangements(CaseData previous, List<CaseEventDetail> eventHistory) {
        DashboardClaimStatus otherPartyStatus = claimStatusFactory.getDashboardClaimStatus(
            new CcdDashboardDefendantClaimMatcher(
                previous,
                toggleService, eventHistory
            ));
        eventHistory.add(CaseEventDetail.builder()
                             .createdDate(LocalDateTime.now())
                             .id(CaseEvent.TRIAL_READINESS.name())
                             .build());
        eventHistory.add(CaseEventDetail.builder()
                             .createdDate(LocalDateTime.now())
                             .id(CaseEvent.GENERATE_TRIAL_READY_FORM_APPLICANT.name())
                             .build());
        CaseData caseData = previous.toBuilder()
            .trialReadyApplicant(YesOrNo.YES)
            .build();
        checkStatus(caseData, eventHistory,
                    DashboardClaimStatus.TRIAL_ARRANGEMENTS_SUBMITTED,
                    otherPartyStatus);
        return caseData;
    }

    private CaseData submitDefendantHearingArrangements(CaseData previous, List<CaseEventDetail> eventHistory) {
        eventHistory.add(CaseEventDetail.builder()
                             .createdDate(LocalDateTime.now())
                             .id(CaseEvent.TRIAL_READINESS.name())
                             .build());
        eventHistory.add(CaseEventDetail.builder()
                             .createdDate(LocalDateTime.now())
                             .id(CaseEvent.GENERATE_TRIAL_READY_FORM_RESPONDENT1.name())
                             .build());
        CaseData caseData = previous.toBuilder()
            .trialReadyRespondent1(YesOrNo.YES)
            .build();
        DashboardClaimStatus otherPartyStatus = claimStatusFactory.getDashboardClaimStatus(
            new CcdDashboardClaimantClaimMatcher(
                previous,
                toggleService, eventHistory
            ));
        checkStatus(caseData, eventHistory,
                    otherPartyStatus,
                    DashboardClaimStatus.TRIAL_ARRANGEMENTS_SUBMITTED);
        return caseData;
    }

    private void awaitingJudgment(CaseData previous, List<CaseEventDetail> eventHistory) {
        CaseData caseData = previous.toBuilder()
            .ccdState(CaseState.DECISION_OUTCOME)
            .build();
        checkStatus(caseData, eventHistory,
                    DashboardClaimStatus.AWAITING_JUDGMENT,
                    DashboardClaimStatus.AWAITING_JUDGMENT);
    }

    /**
     * Checks that after creating an order, dashboard state is updated.
     *
     * @param previous current case data, as it is before creating the order
     * @param created  when this document was created
     * @return updated caseData
     */
    private CaseData generateDirectionOrder(CaseData previous, LocalDateTime created,
                                            List<CaseEventDetail> eventHistory,
                                            OrderType orderType) {
        eventHistory.add(CaseEventDetail.builder()
                             .createdDate(created)
                             .id(CaseEvent.GENERATE_DIRECTIONS_ORDER.name())
                             .build());

        List<Element<CaseDocument>> orderList = Optional.ofNullable(
                previous.getFinalOrderDocumentCollection())
            .map(ArrayList::new)
            .orElseGet(ArrayList::new);
        CaseDocument document = CaseDocument.builder()
            .createdDatetime(created)
            .build();
        orderList.add(Element.<CaseDocument>builder().value(document).build());
        CaseData caseData = previous.toBuilder()
            .finalOrderDocumentCollection(orderList)
            .ccdState(orderType == OrderType.DIRECTIONS_ORDER_ALL ? CaseState.All_FINAL_ORDERS_ISSUED
                          : CaseState.CASE_PROGRESSION)
            .build();
        checkStatus(caseData, eventHistory,
                    DashboardClaimStatus.ORDER_MADE,
                    DashboardClaimStatus.ORDER_MADE);
        return caseData;
    }

    /**
     * Checks that after creating an order, dashboard state is updated.
     *
     * @param previous current case data, as it is before creating the order
     * @param created  when this document was created
     * @return updated caseData
     */
    private CaseData courtOfficerOrder(CaseData previous, LocalDateTime created, List<CaseEventDetail> eventHistory) {
        eventHistory.add(CaseEventDetail.builder()
                             .createdDate(created)
                             .id(CaseEvent.COURT_OFFICER_ORDER.name())
                             .build());
        CaseDocument document = CaseDocument.builder()
            .createdDatetime(created)
            .build();
        CaseData caseData = previous.toBuilder()
            .ccdState(CaseState.CASE_PROGRESSION)
            .previewCourtOfficerOrder(document)
            .build();
        checkStatus(caseData, eventHistory,
                    DashboardClaimStatus.ORDER_MADE,
                    DashboardClaimStatus.ORDER_MADE);
        return caseData;
    }

    /**
     * In several tests we pretend that we have a caseData that has passed through some events and changes.
     * At each step we evaluate the DashboardClaimStatus that such caseData has, but sometimes the passing of time
     * is enough for that status to change. For instance, there are hearing events when the date is scheduled and
     * it is more than 6 weeks away, when it is less than 6 but more than 3 and when it's less than 3.
     * This method modifies caseData to pretend that "today" has shifted.
     * If I'm running the tests on 13th Sept, I may have a caseData with hearingDate 8th Nov, so I'm 8 weeks from
     * that day and my status should be "A". Now, to pretend that today I'm 6 weeks to hearing date, I have to move
     * hearing date 14 days sooner, so I call this method with deltaDays = 14.
     * Not only the hearing date has to change, but documents', events' and what not
     *
     * @param previous     previous case data
     * @param eventHistory events associated with previous case data
     * @param deltaDays    how many days have passed
     * @return modified case data
     */
    private CaseData passDays(CaseData previous, List<CaseEventDetail> eventHistory, int deltaDays) {
        CaseData.CaseDataBuilder<?, ?> builder = previous.toBuilder()
            .hearingDate(Optional.ofNullable(previous.getHearingDate())
                             .map(d -> d.minusDays(deltaDays))
                             .orElse(null));
        eventHistory.replaceAll(caseEventDetail -> caseEventDetail.toBuilder()
            .createdDate(caseEventDetail.getCreatedDate().minusDays(deltaDays))
            .build());
        builder.systemGeneratedCaseDocuments(
            previous.getSystemGeneratedCaseDocuments().stream()
                .map(e -> {
                    CaseDocument old = e.getValue();
                    return Element.<CaseDocument>builder()
                        .value(moveToThePast(old, deltaDays))
                        .build();
                }).toList());
        builder.previewCourtOfficerOrder(
            Optional.ofNullable(previous.getPreviewCourtOfficerOrder())
                .map(c -> c.toBuilder().createdDatetime(c.getCreatedDatetime().minusDays(deltaDays)).build())
                .orElse(null));
        builder.finalOrderDocumentCollection(
            previous.getFinalOrderDocumentCollection().stream()
                .map(e -> Element.<CaseDocument>builder()
                    .value(moveToThePast(e.getValue(), deltaDays))
                    .build()).toList()
        );
        return builder.build();
    }

    private static CaseDocument moveToThePast(CaseDocument c, int days) {
        return c.toBuilder().createdDatetime(c.getCreatedDatetime().minusDays(days)).build();
    }

    static Stream<Arguments> caseToExpectedStatus() {
        List<Arguments> argumentList = new ArrayList<>();
        FeatureToggleService toggleService = Mockito.mock(FeatureToggleService.class);
        addCaseStayedCases(argumentList, toggleService);
        addCaseDismissCases(argumentList, toggleService);

        return argumentList.stream();
    }

    @ParameterizedTest
    @MethodSource("caseToExpectedStatus")
    void shouldReturnCorrectStatus_whenInvoked(Claim claim, DashboardClaimStatus status) {
        assertEquals(status, claimStatusFactory.getDashboardClaimStatus(claim));
    }

    private static void addCaseStayedCases(List<Arguments> argumentList, FeatureToggleService toggleService) {
        CaseData caseData = CaseData.builder()
            .ccdState(CaseState.CASE_STAYED)
            .build();
        CcdDashboardDefendantClaimMatcher defendant = new CcdDashboardDefendantClaimMatcher(caseData, toggleService,
                                                                                            Collections.emptyList()
        );
        CcdDashboardClaimantClaimMatcher claimant = new CcdDashboardClaimantClaimMatcher(caseData, toggleService,
                                                                                         Collections.emptyList()
        );

        argumentList.add(Arguments.arguments(defendant, DashboardClaimStatus.CASE_STAYED));
        argumentList.add(Arguments.arguments(claimant, DashboardClaimStatus.CASE_STAYED));
    }

    private static void addCaseDismissCases(List<Arguments> argumentList, FeatureToggleService toggleService) {
        CaseData caseData = CaseData.builder()
            .ccdState(CaseState.CASE_DISMISSED)
            .build();
        CcdDashboardDefendantClaimMatcher defendant = new CcdDashboardDefendantClaimMatcher(
            caseData, toggleService, Collections.emptyList());
        CcdDashboardClaimantClaimMatcher claimant = new CcdDashboardClaimantClaimMatcher(caseData, toggleService,
                                                                                         Collections.emptyList()
        );

        argumentList.add(Arguments.arguments(defendant, DashboardClaimStatus.CASE_DISMISSED));
        argumentList.add(Arguments.arguments(claimant, DashboardClaimStatus.CASE_DISMISSED));
    }
}
