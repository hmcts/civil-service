package uk.gov.hmcts.reform.civil.model.citizenui;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.FeeType;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.Bundle;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.IdValue;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DashboardClaimStatusFactoryTest {

    /**
     * Generates arguments to interweave order creation in paths followed by cases.
     *
     * @param howManyPositions how many positions we are going to insert the order creation in
     * @return a list of arguments with all numbers in [0, howManyPositions] combined with both
     * true and false for court order
     */
    static Stream<Arguments> positionAndOrderTypeArguments(int howManyPositions) {
        List<Arguments> argumentsList = new ArrayList<>();
        for (int i = 0; i < howManyPositions + 1; i++) {
            argumentsList.add(Arguments.arguments(i, false));
            argumentsList.add(Arguments.arguments(i, true));
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
    void shouldReturnCorrectStatus_hearingFeePaid(int orderPosition, boolean isOfficerOrder) {
        CaseData caseData = fastClaim(LocalDateTime.now().minusDays(10 * 7));
        caseData = applyOrderIfPosition(1, orderPosition, isOfficerOrder,
                                        LocalDateTime.now().minusDays(6 * 7 + 2), caseData
        );
        caseData = scheduleHearingDays(caseData, 6 * 7 + 1);
        // TODO we need to know the date of HEARING_SCHEDULED event to compare with last order
//        caseData = applyOrderIfPosition(2, orderPosition, isOfficerOrder,
//                                        LocalDateTime.now().minusDays(6 * 7), caseData
//        );
        caseData = requestHwF(caseData);
        // TODO we need to know the date of HEARING_SCHEDULED event to compare with last order, because defendant still has it
//        caseData = applyOrderIfPosition(3, orderPosition, isOfficerOrder,
//                                        LocalDateTime.now().minusDays(6 * 7 -1), caseData
//        );
        caseData = invalidHwFReferenceNumber(caseData);
        // TODO we need to know the date of HEARING_SCHEDULED event to compare with last order, because defendant still has it
//        caseData = applyOrderIfPosition(4, orderPosition, isOfficerOrder,
//                                        LocalDateTime.now().minusDays(6 * 7 - 2), caseData
//        );
        caseData = updatedHwFReferenceNumber(caseData);
        // TODO we need to know the date of HEARING_SCHEDULED event to compare with last order, because defendant still has it
//        caseData = applyOrderIfPosition(5, orderPosition, isOfficerOrder,
//                                        LocalDateTime.now().minusDays(6 * 7 - 3), caseData
//        );
        caseData = moreInformationRequiredHwF(caseData);
        // TODO we need to know the date of HEARING_SCHEDULED event to compare with last order, because defendant still has it
//        caseData = applyOrderIfPosition(6, orderPosition, isOfficerOrder,
//                                        LocalDateTime.now().minusDays(6 * 7 - 4), caseData
//        );
        caseData = hwfRejected(caseData);
        // TODO we need to know the date of HEARING_SCHEDULED event to compare with last order, because defendant still has it
//        caseData = applyOrderIfPosition(7, orderPosition, isOfficerOrder,
//                                        LocalDateTime.now().minusDays(6 * 7 - 5), caseData
//        );
        caseData = payHearingFee(caseData);
        // TODO we need to know the date of CITIZEN_HEARING_FEE_PAYMENT event
//        applyOrderIfPosition(8, orderPosition, isOfficerOrder,
//                                        LocalDateTime.now().minusDays(6 * 7 - 6), caseData
//        );
    }

    static Stream<Arguments> hearingFeePaidArguments() {
        return positionAndOrderTypeArguments(8);
    }

    @ParameterizedTest
    @MethodSource("awaitingJudgmentArguments")
    void shouldReturnCorrectStatus_awaitingJudgment(int orderPosition, boolean isOfficerOrder) {
        CaseData caseData = fastClaim(LocalDateTime.now().minusDays(10 * 7));
        caseData = applyOrderIfPosition(1, orderPosition, isOfficerOrder,
                                        LocalDateTime.now().minusDays(10 * 7 - 1), caseData
        );
        caseData = scheduleHearingDays(caseData, 6 * 7 + 1);
        // TODO we need to know the date of HEARING_SCHEDULED event to compare with last order, because defendant still has it
//        caseData = applyOrderIfPosition(2, orderPosition, isOfficerOrder,
//                                        LocalDateTime.now().minusDays(6 * 7), caseData
//        );
        caseData = requestHwF(caseData);
        // TODO we need to know the date of HEARING_SCHEDULED event to compare with last order, because defendant still has it
//        caseData = applyOrderIfPosition(3, orderPosition, isOfficerOrder,
//                                        LocalDateTime.now().minusDays(6 * 7 - 1), caseData
//        );
        caseData = hwfFull(caseData);
        // TODO we need to know the date of HEARING_SCHEDULED event to compare with last order, because defendant still has it
//        caseData = applyOrderIfPosition(4, orderPosition, isOfficerOrder,
//                                        LocalDateTime.now().minusDays(6 * 7 - 2), caseData
//        );
        caseData = scheduleHearingDays(caseData, 6 * 7);
        caseData = applyOrderIfPosition(5, orderPosition, isOfficerOrder,
                                        caseData.getHearingDate()
                                            .atTime(LocalTime.now())
                                            .minusDays(6 * 7 - 1), caseData
        );
        caseData = submitClaimantHearingArrangements(caseData);
        // TODO can't because we don-t know when TRIAL_READINESS happened
//        caseData = applyOrderIfPosition(6, orderPosition, isOfficerOrder,
//                                        caseData.getHearingDate()
//                                            .atTime(LocalTime.now())
//                                            .minusDays(6 * 7 - 2), caseData
//        );
        caseData = submitDefendantHearingArrangements(caseData);
        // TODO can't because we don-t know when TRIAL_READINESS happened
//        caseData = applyOrderIfPosition(7, orderPosition, isOfficerOrder,
//                                        caseData.getHearingDate()
//                                            .atTime(LocalTime.now())
//                                            .minusDays(6 * 7 - 3), caseData
//        );
        caseData = scheduleHearingDays(caseData, 3 * 7);
        // TODO can't because we don-t know when TRIAL_READINESS happened
//        caseData = applyOrderIfPosition(8, orderPosition, isOfficerOrder,
//                                        caseData.getHearingDate()
//                                            .atTime(LocalTime.now().plusHours(1))
//                                            .minusDays(3 * 7 - 1), caseData
//        );
        awaitingJudgment(caseData);
    }

    static Stream<Arguments> awaitingJudgmentArguments() {
        return positionAndOrderTypeArguments(8);
    }

    @ParameterizedTest
    @MethodSource("feeNotPaidArguments")
    void shouldReturnCorrectStatus_feeNotPaid(int orderPosition, boolean isOfficerOrder) {
        CaseData caseData = smallClaim(LocalDateTime.now().minusDays(10 * 7));
        caseData = applyOrderIfPosition(1, orderPosition, isOfficerOrder,
                                        LocalDateTime.now().minusDays(10 * 7 - 1), caseData
        );
        caseData = scheduleHearingDays(caseData, 6 * 7 + 1);
        caseData = applyOrderIfPosition(2, orderPosition, isOfficerOrder,
                                        LocalDateTime.now().minusDays(6 * 7), caseData
        );
        caseData = requestHwF(caseData);
        caseData = applyOrderIfPosition(3, orderPosition, isOfficerOrder,
                                        LocalDateTime.now().minusDays(6 * 7 - 1), caseData
        );
        caseData = hwfPartial(caseData);
        caseData = applyOrderIfPosition(4, orderPosition, isOfficerOrder,
                                        LocalDateTime.now().minusDays(6 * 7 - 2), caseData
        );
        doNotPayHearingFee(caseData);
    }

    static Stream<Arguments> feeNotPaidArguments() {
        return positionAndOrderTypeArguments(4);
    }

    /**
     * If position == valueToApply, modifies caseData to include an order.
     *
     * @param position          one side for the active check
     * @param valueToApply      the other side for the active check
     * @param officerOrder      true to create an officer order, false to create a directions order
     * @param documentCreatedOn date for the document to be created on
     * @param caseData          the caseData
     * @return
     */
    private CaseData applyOrderIfPosition(int position, int valueToApply, boolean officerOrder,
                                          LocalDateTime documentCreatedOn, CaseData caseData) {
        if (position == valueToApply) {
            if (officerOrder) {
                return courtOfficerOrder(caseData, documentCreatedOn);
            } else {
                return generateDirectionOrder(caseData, documentCreatedOn);
            }
        }
        return caseData;
    }

    private CaseData fastClaim(LocalDateTime sdoTime) {
        CaseDocument sdoDocument = CaseDocument.builder()
            .documentType(DocumentType.SDO_ORDER)
            .createdDatetime(sdoTime)
            .build();
        CaseData caseData = CaseData.builder()
            .ccdState(CaseState.CASE_PROGRESSION)
            .responseClaimTrack(AllocatedTrack.FAST_CLAIM.name())
            .totalClaimAmount(BigDecimal.valueOf(1000))
            .systemGeneratedCaseDocuments(List.of(Element.<CaseDocument>builder()
                                                      .value(sdoDocument).build()))
            .build();
        Assertions.assertEquals(
            DashboardClaimStatus.SDO_ORDER_CREATED,
            claimStatusFactory.getDashboardClaimStatus(new CcdDashboardDefendantClaimMatcher(
                caseData,
                toggleService
            ))
        );
        Assertions.assertEquals(
            DashboardClaimStatus.SDO_ORDER_CREATED,
            claimStatusFactory.getDashboardClaimStatus(new CcdDashboardClaimantClaimMatcher(
                caseData,
                toggleService
            ))
        );
        return caseData;
    }

    private CaseData.CaseDataBuilder<?, ?> updateDocumentDates(CaseData previous, int deltaDays) {
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = previous.toBuilder();

        return caseDataBuilder;
    }

    private CaseData scheduleHearingDays(CaseData previous, int days) {
        LocalDate hearingDate = LocalDate.now().plusDays(days);
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder;
        if (previous.getHearingDate() != null) {
            // to pretend time has passed, we have to modify the dates of the relevant documents
            caseDataBuilder = previous.toBuilder()
                .caseBundles(
                    previous.getCaseBundles().stream()
                        .map(idValue ->
                                 new IdValue<>(
                                     idValue.getId(),
                                     idValue.getValue().toBuilder()
                                         .createdOn(Optional.of(
                                             hearingDate
                                                 .minusDays(3 * 7)
                                                 .atTime(LocalTime.now())))
                                         .bundleHearingDate(Optional.of(hearingDate))
                                         .build()
                                 )
                        ).collect(Collectors.toList()));
            int deltaDays = (int) (-Math.abs(ChronoUnit.DAYS.between(hearingDate, previous.getHearingDate())));
            List<Element<CaseDocument>> updatedDocuments = previous.getFinalOrderDocumentCollection().stream()
                .map(e -> Element.<CaseDocument>builder()
                    .id(e.getId())
                    .value(e.getValue().toBuilder()
                               .createdDatetime(e.getValue().getCreatedDatetime().plusDays(deltaDays))
                               .build())
                    .build())
                .collect(Collectors.toList());
            caseDataBuilder.finalOrderDocumentCollection(updatedDocuments);
            if (previous.getPreviewCourtOfficerOrder() != null) {
                caseDataBuilder.previewCourtOfficerOrder(
                    previous.getPreviewCourtOfficerOrder().toBuilder()
                        .createdDatetime(previous.getPreviewCourtOfficerOrder().getCreatedDatetime()
                                             .plusDays(deltaDays))
                        .build()
                );
            }
        } else {
            caseDataBuilder = previous.toBuilder();
        }
        caseDataBuilder.hearingDate(hearingDate);
        DashboardClaimStatus expectedStatus;
        if (days > 6 * 7) {
            expectedStatus = DashboardClaimStatus.TRIAL_OR_HEARING_SCHEDULED;
        } else if (days <= 3 * 7) {
            caseDataBuilder.caseBundles(List.of(new IdValue<>(
                "bundle1",
                Bundle.builder()
                    .bundleHearingDate(Optional.of(hearingDate))
                    .createdOn(Optional.of(hearingDate
                                               .minusDays(3 * 7)
                                               .atTime(LocalTime.now())))
                    .build()
            )));
            expectedStatus = DashboardClaimStatus.BUNDLE_CREATED;
        } else {
            expectedStatus = DashboardClaimStatus.TRIAL_ARRANGEMENTS_REQUIRED;
        }
        CaseData caseData = caseDataBuilder.build();
        Assertions.assertEquals(
            expectedStatus,
            claimStatusFactory.getDashboardClaimStatus(new CcdDashboardDefendantClaimMatcher(
                caseData,
                toggleService
            ))
        );
        Assertions.assertEquals(
            expectedStatus,
            claimStatusFactory.getDashboardClaimStatus(new CcdDashboardClaimantClaimMatcher(
                caseData,
                toggleService
            ))
        );

        return caseData;
    }

    private CaseData requestHwF(CaseData previous) {
        DashboardClaimStatus defendantStatus = claimStatusFactory.getDashboardClaimStatus(
            new CcdDashboardDefendantClaimMatcher(previous, toggleService)
        );
        CaseData caseData = previous.toBuilder()
            .hwfFeeType(FeeType.HEARING)
            .ccdState(CaseState.HEARING_READINESS)
            .build();
        Assertions.assertEquals(
            defendantStatus,
            claimStatusFactory.getDashboardClaimStatus(new CcdDashboardDefendantClaimMatcher(
                caseData,
                toggleService
            ))
        );
        Assertions.assertEquals(
            DashboardClaimStatus.HEARING_SUBMIT_HWF,
            claimStatusFactory.getDashboardClaimStatus(new CcdDashboardClaimantClaimMatcher(
                caseData,
                toggleService
            ))
        );
        return caseData;
    }

    private CaseData invalidHwFReferenceNumber(CaseData previous) {
        DashboardClaimStatus defendantStatus = claimStatusFactory.getDashboardClaimStatus(
            new CcdDashboardDefendantClaimMatcher(previous, toggleService)
        );
        CaseData caseData = previous.toBuilder()
            .ccdState(CaseState.HEARING_READINESS)
            .hwfFeeType(FeeType.HEARING)
            .hearingHwfDetails(HelpWithFeesDetails.builder()
                                   .hwfCaseEvent(CaseEvent.INVALID_HWF_REFERENCE)
                                   .build())
            .build();

        Assertions.assertEquals(
            defendantStatus,
            claimStatusFactory.getDashboardClaimStatus(new CcdDashboardDefendantClaimMatcher(
                caseData,
                toggleService
            ))
        );
        Assertions.assertEquals(
            DashboardClaimStatus.CLAIMANT_HWF_INVALID_REF_NUMBER,
            claimStatusFactory.getDashboardClaimStatus(new CcdDashboardClaimantClaimMatcher(
                caseData,
                toggleService
            ))
        );
        return caseData;
    }

    private CaseData updatedHwFReferenceNumber(CaseData previous) {
        DashboardClaimStatus defendantStatus = claimStatusFactory.getDashboardClaimStatus(
            new CcdDashboardDefendantClaimMatcher(previous, toggleService)
        );
        CaseData caseData = previous.toBuilder()
            .ccdState(CaseState.HEARING_READINESS)
            .hearingHwfDetails(HelpWithFeesDetails.builder()
                                   .hwfCaseEvent(CaseEvent.UPDATE_HELP_WITH_FEE_NUMBER)
                                   .build())
            .build();
        Assertions.assertEquals(
            defendantStatus,
            claimStatusFactory.getDashboardClaimStatus(new CcdDashboardDefendantClaimMatcher(
                caseData,
                toggleService
            ))
        );
        Assertions.assertEquals(
            DashboardClaimStatus.CLAIMANT_HWF_UPDATED_REF_NUMBER,
            claimStatusFactory.getDashboardClaimStatus(new CcdDashboardClaimantClaimMatcher(
                caseData,
                toggleService
            ))
        );
        return caseData;
    }

    private CaseData moreInformationRequiredHwF(CaseData previous) {
        DashboardClaimStatus defendantStatus = claimStatusFactory.getDashboardClaimStatus(
            new CcdDashboardDefendantClaimMatcher(previous, toggleService)
        );
        CaseData caseData = previous.toBuilder()
            .ccdState(CaseState.HEARING_READINESS)
            .hearingHwfDetails(HelpWithFeesDetails.builder()
                                   .hwfCaseEvent(CaseEvent.MORE_INFORMATION_HWF)
                                   .build())
            .build();
        Assertions.assertEquals(
            defendantStatus,
            claimStatusFactory.getDashboardClaimStatus(new CcdDashboardDefendantClaimMatcher(
                caseData,
                toggleService
            ))
        );
        Assertions.assertEquals(
            DashboardClaimStatus.HWF_MORE_INFORMATION_NEEDED,
            claimStatusFactory.getDashboardClaimStatus(new CcdDashboardClaimantClaimMatcher(
                caseData,
                toggleService
            ))
        );
        return caseData;
    }

    private CaseData hwfRejected(CaseData previous) {
        DashboardClaimStatus defendantStatus = claimStatusFactory.getDashboardClaimStatus(
            new CcdDashboardDefendantClaimMatcher(previous, toggleService)
        );
        CaseData caseData = previous.toBuilder()
            .ccdState(CaseState.HEARING_READINESS)
            .hearingHwfDetails(HelpWithFeesDetails.builder()
                                   .hwfCaseEvent(CaseEvent.NO_REMISSION_HWF)
                                   .build())
            .build();
        Assertions.assertEquals(
            defendantStatus,
            claimStatusFactory.getDashboardClaimStatus(new CcdDashboardDefendantClaimMatcher(
                caseData,
                toggleService
            ))
        );
        Assertions.assertEquals(
            DashboardClaimStatus.CLAIMANT_HWF_NO_REMISSION,
            claimStatusFactory.getDashboardClaimStatus(new CcdDashboardClaimantClaimMatcher(
                caseData,
                toggleService
            ))
        );
        return caseData;
    }

    private CaseData payHearingFee(CaseData previous) {
        DashboardClaimStatus defendantStatus = claimStatusFactory.getDashboardClaimStatus(
            new CcdDashboardDefendantClaimMatcher(previous, toggleService)
        );
        CaseData caseData = previous.toBuilder()
            .ccdState(CaseState.HEARING_READINESS)
            .hwfFeeType(FeeType.HEARING)
            .hearingHwfDetails(HelpWithFeesDetails.builder()
                                   .hwfCaseEvent(CaseEvent.FEE_PAYMENT_OUTCOME)
                                   .build())
            .build();
        Assertions.assertEquals(
            defendantStatus,
            claimStatusFactory.getDashboardClaimStatus(new CcdDashboardDefendantClaimMatcher(
                caseData,
                toggleService
            ))
        );
        Assertions.assertEquals(
            DashboardClaimStatus.CLAIMANT_HWF_FEE_PAYMENT_OUTCOME,
            claimStatusFactory.getDashboardClaimStatus(new CcdDashboardClaimantClaimMatcher(
                caseData,
                toggleService
            ))
        );
        return caseData;
    }

    private CaseData smallClaim(LocalDateTime sdoTime) {
        CaseDocument sdoDocument = CaseDocument.builder()
            .documentType(DocumentType.SDO_ORDER)
            .createdDatetime(sdoTime)
            .build();
        CaseData caseData = CaseData.builder()
            .ccdState(CaseState.CASE_PROGRESSION)
            .responseClaimTrack(AllocatedTrack.SMALL_CLAIM.name())
            .totalClaimAmount(BigDecimal.valueOf(999))
            .systemGeneratedCaseDocuments(List.of(Element.<CaseDocument>builder()
                                                      .value(sdoDocument).build()))
            .build();
        Assertions.assertEquals(
            DashboardClaimStatus.SDO_ORDER_LEGAL_ADVISER_CREATED,
            claimStatusFactory.getDashboardClaimStatus(new CcdDashboardDefendantClaimMatcher(
                caseData,
                toggleService
            ))
        );
        Assertions.assertEquals(
            DashboardClaimStatus.SDO_ORDER_LEGAL_ADVISER_CREATED,
            claimStatusFactory.getDashboardClaimStatus(new CcdDashboardClaimantClaimMatcher(
                caseData,
                toggleService
            ))
        );
        return caseData;
    }

    private CaseData hwfPartial(CaseData previous) {
        DashboardClaimStatus defendantStatus = claimStatusFactory.getDashboardClaimStatus(
            new CcdDashboardDefendantClaimMatcher(previous, toggleService)
        );
        CaseData caseData = previous.toBuilder()
            .ccdState(CaseState.HEARING_READINESS)
            .hwfFeeType(FeeType.HEARING)
            .hearingHwfDetails(HelpWithFeesDetails.builder()
                                   .hwfCaseEvent(CaseEvent.PARTIAL_REMISSION_HWF_GRANTED)
                                   .build())
            .build();
        Assertions.assertEquals(
            defendantStatus,
            claimStatusFactory.getDashboardClaimStatus(new CcdDashboardDefendantClaimMatcher(
                caseData,
                toggleService
            ))
        );
        Assertions.assertEquals(
            DashboardClaimStatus.CLAIMANT_HWF_PARTIAL_REMISSION,
            claimStatusFactory.getDashboardClaimStatus(new CcdDashboardClaimantClaimMatcher(
                caseData,
                toggleService
            ))
        );
        return caseData;
    }

    private CaseData doNotPayHearingFee(CaseData previous) {
        CaseData caseData = previous.toBuilder()
            .caseDismissedHearingFeeDueDate(LocalDateTime.now())
            .build();
        Assertions.assertEquals(
            DashboardClaimStatus.HEARING_FEE_UNPAID,
            claimStatusFactory.getDashboardClaimStatus(new CcdDashboardDefendantClaimMatcher(
                caseData,
                toggleService
            ))
        );
        Assertions.assertEquals(
            DashboardClaimStatus.HEARING_FEE_UNPAID,
            claimStatusFactory.getDashboardClaimStatus(new CcdDashboardClaimantClaimMatcher(
                caseData,
                toggleService
            ))
        );
        return caseData;
    }

    private CaseData hwfFull(CaseData previous) {
        DashboardClaimStatus defendantStatus = claimStatusFactory.getDashboardClaimStatus(
            new CcdDashboardDefendantClaimMatcher(previous, toggleService)
        );
        CaseData caseData = previous.toBuilder()
            .ccdState(CaseState.HEARING_READINESS)
            .hwfFeeType(FeeType.HEARING)
            .hearingHwfDetails(HelpWithFeesDetails.builder()
                                   .hwfCaseEvent(CaseEvent.FEE_PAYMENT_OUTCOME)
                                   .build())
            .build();
        Assertions.assertEquals(
            defendantStatus,
            claimStatusFactory.getDashboardClaimStatus(new CcdDashboardDefendantClaimMatcher(
                caseData,
                toggleService
            ))
        );
        Assertions.assertEquals(
            DashboardClaimStatus.CLAIMANT_HWF_FEE_PAYMENT_OUTCOME,
            claimStatusFactory.getDashboardClaimStatus(new CcdDashboardClaimantClaimMatcher(
                caseData,
                toggleService
            ))
        );
        return caseData;
    }

    private CaseData submitClaimantHearingArrangements(CaseData previous) {
        DashboardClaimStatus otherPartyStatus = claimStatusFactory.getDashboardClaimStatus(
            new CcdDashboardDefendantClaimMatcher(
                previous,
                toggleService
            ));
        CaseData caseData = previous.toBuilder()
            .trialReadyApplicant(YesOrNo.YES)
            .build();
        Assertions.assertEquals(
            otherPartyStatus,
            claimStatusFactory.getDashboardClaimStatus(new CcdDashboardDefendantClaimMatcher(
                caseData,
                toggleService
            ))
        );
        Assertions.assertEquals(
            DashboardClaimStatus.TRIAL_ARRANGEMENTS_SUBMITTED,
            claimStatusFactory.getDashboardClaimStatus(new CcdDashboardClaimantClaimMatcher(
                caseData,
                toggleService
            ))
        );
        return caseData;
    }

    private CaseData submitDefendantHearingArrangements(CaseData previous) {
        DashboardClaimStatus otherPartyStatus = claimStatusFactory.getDashboardClaimStatus(
            new CcdDashboardClaimantClaimMatcher(
                previous,
                toggleService
            ));
        CaseData caseData = previous.toBuilder()
            .trialReadyRespondent1(YesOrNo.YES)
            .build();
        Assertions.assertEquals(
            DashboardClaimStatus.TRIAL_ARRANGEMENTS_SUBMITTED,
            claimStatusFactory.getDashboardClaimStatus(new CcdDashboardDefendantClaimMatcher(
                caseData,
                toggleService
            ))
        );
        Assertions.assertEquals(
            otherPartyStatus,
            claimStatusFactory.getDashboardClaimStatus(new CcdDashboardClaimantClaimMatcher(
                caseData,
                toggleService
            ))
        );
        return caseData;
    }

    private CaseData awaitingJudgment(CaseData previous) {
        CaseData caseData = previous.toBuilder()
            .ccdState(CaseState.DECISION_OUTCOME)
            .build();
        Assertions.assertEquals(
            DashboardClaimStatus.AWAITING_JUDGMENT,
            claimStatusFactory.getDashboardClaimStatus(new CcdDashboardDefendantClaimMatcher(
                caseData,
                toggleService
            ))
        );
        Assertions.assertEquals(
            DashboardClaimStatus.AWAITING_JUDGMENT,
            claimStatusFactory.getDashboardClaimStatus(new CcdDashboardClaimantClaimMatcher(
                caseData,
                toggleService
            ))
        );
        return caseData;
    }

    /**
     * Checks that after creating an order, dashboard state is updated.
     *
     * @param previous current case data, as it is before creating the order
     * @param created  when this document was created
     * @return updated caseData
     */
    private CaseData generateDirectionOrder(CaseData previous, LocalDateTime created) {
        if (previous.getCcdState() != CaseState.CASE_PROGRESSION
            && previous.getCcdState() != CaseState.All_FINAL_ORDERS_ISSUED) {
            // GENERATE_DIRECTIONS_ORDER does not change state, and ORDER_MADE requires one of those states
            return previous;
        }
        List<Element<CaseDocument>> orderList = Optional.ofNullable(
            previous.getFinalOrderDocumentCollection()).orElseGet(ArrayList::new);
        int daysDelta = -orderList.stream().map(e -> e.getValue().getCreatedDatetime())
            .max(LocalDateTime::compareTo)
            .map(max -> Math.abs(ChronoUnit.DAYS.between(created, max)) + 2)
            .orElse(0L).intValue();
        for (int i = 0; i < orderList.size(); i++) {
            CaseDocument document = orderList.get(i).getValue();
            document = document.toBuilder()
                .createdDatetime(document.getCreatedDatetime().plusDays(daysDelta))
                .build();
            orderList.set(i, Element.<CaseDocument>builder()
                .value(document)
                .build());
        }
        CaseDocument document = CaseDocument.builder()
            .createdDatetime(created)
            .build();
        orderList.add(Element.<CaseDocument>builder().value(document).build());
        CaseData caseData = previous.toBuilder()
            .finalOrderDocumentCollection(orderList)
            .build();
        Assertions.assertEquals(
            DashboardClaimStatus.ORDER_MADE,
            claimStatusFactory.getDashboardClaimStatus(new CcdDashboardDefendantClaimMatcher(
                caseData,
                toggleService
            ))
        );
        Assertions.assertEquals(
            DashboardClaimStatus.ORDER_MADE,
            claimStatusFactory.getDashboardClaimStatus(new CcdDashboardClaimantClaimMatcher(
                caseData,
                toggleService
            ))
        );
        return caseData;
    }

    /**
     * Checks that after creating an order, dashboard state is updated.
     *
     * @param previous current case data, as it is before creating the order
     * @param created  when this document was created
     * @return updated caseData
     */
    private CaseData courtOfficerOrder(CaseData previous, LocalDateTime created) {
        CaseDocument document = CaseDocument.builder()
            .createdDatetime(created)
            .build();
        CaseData caseData = previous.toBuilder()
            .ccdState(CaseState.CASE_PROGRESSION)
            .previewCourtOfficerOrder(document)
            .build();
        Assertions.assertEquals(
            DashboardClaimStatus.ORDER_MADE,
            claimStatusFactory.getDashboardClaimStatus(new CcdDashboardDefendantClaimMatcher(
                caseData,
                toggleService
            ))
        );
        Assertions.assertEquals(
            DashboardClaimStatus.ORDER_MADE,
            claimStatusFactory.getDashboardClaimStatus(new CcdDashboardClaimantClaimMatcher(
                caseData,
                toggleService
            ))
        );
        return caseData;
    }


}
