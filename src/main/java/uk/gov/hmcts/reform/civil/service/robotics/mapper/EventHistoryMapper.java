package uk.gov.hmcts.reform.civil.service.robotics.mapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseType;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CCJPaymentDetails;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.ClaimantResponseDetails;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.PartyData;
import uk.gov.hmcts.reform.civil.model.PaymentBySetDate;
import uk.gov.hmcts.reform.civil.model.RepaymentPlanLRspec;
import uk.gov.hmcts.reform.civil.model.RespondToClaim;
import uk.gov.hmcts.reform.civil.model.RespondToClaimAdmitPartLRspec;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.ClaimantLiPResponse;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.dq.DQ;
import uk.gov.hmcts.reform.civil.model.dq.FileDirectionsQuestionnaire;
import uk.gov.hmcts.reform.civil.model.dq.RequestedCourt;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Respondent2DQ;
import uk.gov.hmcts.reform.civil.model.genapplication.GeneralApplication;
import uk.gov.hmcts.reform.civil.model.genapplication.GeneralApplicationsDetails;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentSetAsideOrderType;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentSetAsideReason;
import uk.gov.hmcts.reform.civil.model.judgmentonline.SetAsideApplicantTypeForRPA;
import uk.gov.hmcts.reform.civil.model.judgmentonline.SetAsideResultTypeForRPA;
import uk.gov.hmcts.reform.civil.model.robotics.Event;
import uk.gov.hmcts.reform.civil.model.robotics.EventDetails;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;
import uk.gov.hmcts.reform.civil.service.flowstate.IStateFlowEngine;
import uk.gov.hmcts.reform.civil.stateflow.model.State;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsEventTextFormatter;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsPartyLookup;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsSequenceGenerator;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsTimelineHelper;
import uk.gov.hmcts.reform.civil.service.robotics.strategy.EventHistoryContributor;
import uk.gov.hmcts.reform.civil.utils.LocationRefDataUtil;
import uk.gov.hmcts.reform.civil.utils.MonetaryConversions;
import uk.gov.hmcts.reform.civil.utils.PartyUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.IntStream;

import static java.lang.String.format;
import static java.math.BigDecimal.ZERO;
import static java.time.format.DateTimeFormatter.ISO_DATE;
import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.left;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.UNSPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.CaseState.CASE_DISCONTINUED;
import static uk.gov.hmcts.reform.civil.enums.CaseState.CASE_DISMISSED;
import static uk.gov.hmcts.reform.civil.enums.CaseState.PROCEEDS_IN_HERITAGE_SYSTEM;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_ONE_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.TWO_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.enums.PartyRole.RESPONDENT_ONE;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseType.FULL_DEFENCE;
import static uk.gov.hmcts.reform.civil.enums.UnrepresentedOrUnregisteredScenario.UNREGISTERED;
import static uk.gov.hmcts.reform.civil.enums.UnrepresentedOrUnregisteredScenario.UNREPRESENTED;
import static uk.gov.hmcts.reform.civil.enums.UnrepresentedOrUnregisteredScenario.getDefendantNames;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes.PROCEEDS_IN_HERITAGE;
import static uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes.STRIKE_OUT;
import static uk.gov.hmcts.reform.civil.model.robotics.EventType.ACKNOWLEDGEMENT_OF_SERVICE_RECEIVED;
import static uk.gov.hmcts.reform.civil.model.robotics.EventType.CERTIFICATE_OF_SATISFACTION_OR_CANCELLATION;
import static uk.gov.hmcts.reform.civil.model.robotics.EventType.CONSENT_EXTENSION_FILING_DEFENCE;
import static uk.gov.hmcts.reform.civil.model.robotics.EventType.DEFAULT_JUDGMENT_GRANTED;
import static uk.gov.hmcts.reform.civil.model.robotics.EventType.DEFENCE_FILED;
import static uk.gov.hmcts.reform.civil.model.robotics.EventType.DEFENCE_STRUCK_OUT;
import static uk.gov.hmcts.reform.civil.model.robotics.EventType.DIRECTIONS_QUESTIONNAIRE_FILED;
import static uk.gov.hmcts.reform.civil.model.robotics.EventType.GENERAL_FORM_OF_APPLICATION;
import static uk.gov.hmcts.reform.civil.model.robotics.EventType.INTERLOCUTORY_JUDGMENT_GRANTED;
import static uk.gov.hmcts.reform.civil.model.robotics.EventType.JUDGEMENT_BY_ADMISSION;
import static uk.gov.hmcts.reform.civil.model.robotics.EventType.MISCELLANEOUS;
import static uk.gov.hmcts.reform.civil.model.robotics.EventType.RECEIPT_OF_ADMISSION;
import static uk.gov.hmcts.reform.civil.model.robotics.EventType.RECEIPT_OF_PART_ADMISSION;
import static uk.gov.hmcts.reform.civil.model.robotics.EventType.SET_ASIDE_JUDGMENT;
import static uk.gov.hmcts.reform.civil.model.robotics.EventType.STATES_PAID;
import static uk.gov.hmcts.reform.civil.service.robotics.utils.RoboticsDataUtil.APPLICANT2_ID;
import static uk.gov.hmcts.reform.civil.service.robotics.utils.RoboticsDataUtil.APPLICANT_ID;
import static uk.gov.hmcts.reform.civil.service.robotics.utils.RoboticsDataUtil.RESPONDENT2_ID;
import static uk.gov.hmcts.reform.civil.service.robotics.utils.RoboticsDataUtil.RESPONDENT_ID;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.unwrapElements;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getResponseTypeForRespondent;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getResponseTypeForRespondentSpec;
import static uk.gov.hmcts.reform.civil.utils.PredicateUtils.defendant1ExtensionExists;
import static uk.gov.hmcts.reform.civil.utils.PredicateUtils.defendant1ResponseExists;
import static uk.gov.hmcts.reform.civil.utils.PredicateUtils.defendant1v2SameSolicitorSameResponse;
import static uk.gov.hmcts.reform.civil.utils.PredicateUtils.defendant2DivergentResponseExists;
import static uk.gov.hmcts.reform.civil.utils.PredicateUtils.defendant2ExtensionExists;
import static uk.gov.hmcts.reform.civil.utils.PredicateUtils.defendant2ResponseExists;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventHistoryMapper {

    private final IStateFlowEngine stateFlowEngine;
    private final FeatureToggleService featureToggleService;
    private final EventHistorySequencer eventHistorySequencer;
    private final LocationRefDataUtil locationRefDataUtil;
    private final RoboticsTimelineHelper timelineHelper;
    private final RoboticsEventTextFormatter textFormatter;
    private final RoboticsPartyLookup partyLookup;
    private final RoboticsSequenceGenerator sequenceGenerator;
    private final List<EventHistoryContributor> eventHistoryContributors;
    public static final String BS_REF = "Breathing space reference";
    public static final String BS_START_DT = "actual start date";
    public static final String BS_END_DATE = "actual end date";
    public static final String RECORD_JUDGMENT = "Judgment recorded.";
    public static final String RPA_IN_MEDIATION = "IN MEDIATION";
    public static final String QUERIES_ON_CASE = "There has been a query on this case";
    static final String ENTER = "Enter";
    static final String LIFTED = "Lifted";
    static final Set<CaseState> OFFLINE_STATES = EnumSet.of(CASE_DISMISSED, PROCEEDS_IN_HERITAGE_SYSTEM, CASE_DISCONTINUED);

    public EventHistory buildEvents(CaseData caseData) {
        return buildEvents(caseData, null);
    }

    public EventHistory buildEvents(CaseData caseData, String authToken) {
        EventHistory.EventHistoryBuilder builder = EventHistory.builder()
            .directionsQuestionnaireFiled(List.of(Event.builder().build()));

        stateFlowEngine.evaluate(caseData).getStateHistory()
            .forEach(state -> {
                FlowState.Main flowState = (FlowState.Main) FlowState.fromFullName(state.getName());
                switch (flowState) {
                    case TAKEN_OFFLINE_UNREPRESENTED_DEFENDANT:
                    case TAKEN_OFFLINE_UNREPRESENTED_UNREGISTERED_DEFENDANT:
                    case TAKEN_OFFLINE_UNREGISTERED_DEFENDANT:
                        break;
                    case NOTIFICATION_ACKNOWLEDGED:
                        break;
                    case NOTIFICATION_ACKNOWLEDGED_TIME_EXTENSION, CLAIM_DETAILS_NOTIFIED_TIME_EXTENSION:
                        buildConsentExtensionFilingDefence(builder, caseData);
                        break;
                    case FULL_DEFENCE:
                        buildRespondentFullDefence(builder, caseData);
                        break;
                    case FULL_ADMISSION:
                        buildRespondentFullAdmission(builder, caseData);
                        break;
                    case PART_ADMISSION:
                        buildRespondentPartAdmission(builder, caseData);
                        break;
                    case COUNTER_CLAIM:
                        buildRespondentCounterClaim(builder, caseData);
                        break;
                    // AWAITING_RESPONSES states would only happen in 1v2 diff sol after 1 defendant responses.
                    // These states will not show in the history mapper after the second defendant response.
                    // It can share the same RPA builder as DIVERGENT_RESPOND state because it builds events according
                    // to defendant response
                    // DIVERGENT_RESPOND states would only happen in 1v2 diff sol after both defendant responds.
                    case AWAITING_RESPONSES_FULL_DEFENCE_RECEIVED, AWAITING_RESPONSES_FULL_ADMIT_RECEIVED, AWAITING_RESPONSES_NOT_FULL_DEFENCE_OR_FULL_ADMIT_RECEIVED:
                        buildRespondentDivergentResponse(builder, caseData, false);
                        break;
                    case DIVERGENT_RESPOND_GENERATE_DQ_GO_OFFLINE, DIVERGENT_RESPOND_GO_OFFLINE:
                        buildRespondentDivergentResponse(builder, caseData, true);
                        break;
                    case FULL_DEFENCE_NOT_PROCEED:
                        buildFullDefenceNotProceed(builder, caseData);
                        break;
                    case FULL_DEFENCE_PROCEED:
                        buildFullDefenceProceed(builder, caseData, authToken);
                        break;
                    case TAKEN_OFFLINE_BY_STAFF:
                        buildGeneralFormApplicationEventsStrikeOutOrder(builder, caseData);
                        break;
                    case CLAIM_DISMISSED_PAST_CLAIM_DISMISSED_DEADLINE:
                        break;
                    case CLAIM_DISMISSED_PAST_CLAIM_NOTIFICATION_DEADLINE:
                    case CLAIM_DISMISSED_PAST_CLAIM_DETAILS_NOTIFICATION_DEADLINE:
                    case TAKEN_OFFLINE_AFTER_CLAIM_DETAILS_NOTIFIED:
                    case TAKEN_OFFLINE_PAST_APPLICANT_RESPONSE_DEADLINE:
                        break;
                    case TAKEN_OFFLINE_SDO_NOT_DRAWN:
                        buildSDONotDrawn(builder, caseData);
                        break;
                    case PART_ADMIT_REJECT_REPAYMENT, FULL_ADMIT_REJECT_REPAYMENT:
                        buildSpecAdmitRejectRepayment(builder, caseData);
                        break;
                    case TAKEN_OFFLINE_SPEC_DEFENDANT_NOC, TAKEN_OFFLINE_SPEC_DEFENDANT_NOC_AFTER_JBA:
                        buildTakenOfflineDueToDefendantNoc(builder, caseData);
                        break;
                    default:
                        break;
                }
            });

        buildCaseNotesEvents(builder, caseData);
        eventHistoryContributors.stream()
            .filter(contributor -> contributor.supports(caseData))
            .forEach(contributor -> contributor.contribute(builder, caseData, authToken));
        buildInterlocutoryJudgment(builder, caseData);
        buildMiscellaneousIJEvent(builder, caseData);
        buildInformAgreedExtensionDateForSpec(builder, caseData);
        buildCcjEvent(builder, caseData);
        buildSetAsideJudgment(builder, caseData);
        buildCoscEvent(builder, caseData);
        EventHistory eventHistory = eventHistorySequencer.sortEvents(builder.build());
        log.info("Event history: {}", eventHistory);
        return eventHistory;
    }

    private void buildInterlocutoryJudgment(EventHistory.EventHistoryBuilder builder, CaseData caseData) {
        List<Event> events = new ArrayList<>();
        boolean grantedFlag = caseData.getRespondent2() != null
            && caseData.getDefendantDetails() != null
            && !caseData.getDefendantDetails().getValue()
            .getLabel().startsWith("Both");
        if (!grantedFlag && null != caseData.getHearingSupportRequirementsDJ()) {
            events.add(prepareInterlocutoryJudgment(builder, partyLookup.respondentId(0)));

            if (null != caseData.getRespondent2()) {
                events.add(prepareInterlocutoryJudgment(builder, partyLookup.respondentId(1)));
            }
            builder.interlocutoryJudgment(events);
        }
    }

    private Event prepareInterlocutoryJudgment(EventHistory.EventHistoryBuilder builder,
                                               String litigiousPartyID) {
        return (Event.builder()
            .eventSequence(prepareEventSequence(builder.build()))
            .eventCode(INTERLOCUTORY_JUDGMENT_GRANTED.getCode())
            .dateReceived(timelineHelper.now())
            .litigiousPartyID(litigiousPartyID)
            .eventDetailsText("")
            .eventDetails(EventDetails.builder().miscText("")
                              .build())
            .build());
    }

    private boolean hasCourtDecisionInFavourOfClaimant(CaseData caseData) {
        ClaimantLiPResponse applicant1Response = Optional.ofNullable(caseData.getCaseDataLiP())
            .map(CaseDataLiP::getApplicant1LiPResponse).orElse(null);
        return applicant1Response != null && applicant1Response.hasCourtDecisionInFavourOfClaimant();
    }
    @Nullable
    private BigDecimal getInstallmentAmount(CaseData caseData) {
        boolean payByInstallment = hasCourtDecisionInFavourOfClaimant(caseData) ? caseData.applicant1SuggestedPayByInstalments() : caseData.isPayByInstallment();
        Optional<RepaymentPlanLRspec> repaymentPlan = Optional.ofNullable(caseData.getRespondent1RepaymentPlan());
        BigDecimal repaymentAmount = hasCourtDecisionInFavourOfClaimant(caseData)
            ? caseData.getApplicant1SuggestInstalmentsPaymentAmountForDefendantSpec()
            : repaymentPlan.map(RepaymentPlanLRspec::getPaymentAmount).orElse(ZERO);
        return payByInstallment
            ? MonetaryConversions.penniesToPounds(
            Optional.ofNullable(repaymentAmount).map(amount -> amount.setScale(2)).orElse(ZERO))
            : null;
    }

    @Nullable
    private LocalDate getFirstInstallmentDate(CaseData caseData) {
        if (hasCourtDecisionInFavourOfClaimant(caseData)) {
            return caseData.applicant1SuggestedPayByInstalments() ? caseData.getApplicant1SuggestInstalmentsFirstRepaymentDateForDefendantSpec() : null;
        } else {
            return caseData.isPayByInstallment() ? ofNullable(caseData.getRespondent1RepaymentPlan()).map(RepaymentPlanLRspec::getFirstRepaymentDate).orElse(null) : null;
        }
    }

    private void buildCoscEvent(EventHistory.EventHistoryBuilder builder, CaseData caseData) {
        boolean joMarkedPaidInFullDateExists = caseData.getJoMarkedPaidInFullIssueDate() != null;

        if (featureToggleService.isJOLiveFeedActive()
            && ((joMarkedPaidInFullDateExists && caseData.getJoDefendantMarkedPaidInFullIssueDate() == null)
                || caseData.hasCoscCert())
        ) {
            // date received when mark paid in full by claimant is issued or when the scheduler runs at the cosc deadline at 4pm
            LocalDateTime dateReceived = joMarkedPaidInFullDateExists
                ? caseData.getJoMarkedPaidInFullIssueDate() : caseData.getJoDefendantMarkedPaidInFullIssueDate();

            builder.certificateOfSatisfactionOrCancellation((Event.builder()
                .eventSequence(prepareEventSequence(builder.build()))
                .litigiousPartyID(joMarkedPaidInFullDateExists ? APPLICANT_ID : RESPONDENT_ID)
                .eventCode(CERTIFICATE_OF_SATISFACTION_OR_CANCELLATION.getCode())
                .dateReceived(dateReceived)
                .eventDetails(EventDetails.builder()
                                  .status(caseData.getJoCoscRpaStatus().toString())
                                  .datePaidInFull(getCoscDate(caseData))
                                  .notificationReceiptDate(joMarkedPaidInFullDateExists
                                                               ? caseData.getJoMarkedPaidInFullIssueDate().toLocalDate()
                                                               : caseData.getJoDefendantMarkedPaidInFullIssueDate().toLocalDate())
                                  .build())
                .eventDetailsText("")
                .build()));
        }
    }

    private void buildCcjEvent(EventHistory.EventHistoryBuilder builder, CaseData caseData) {
        if (caseData.isCcjRequestJudgmentByAdmission()) {
            buildJudgmentByAdmissionEventDetails(builder, caseData);

            String miscTextRequested = textFormatter.judgmentByAdmissionOffline();
            String detailsTextRequested = textFormatter.judgmentByAdmissionOffline();
            if (featureToggleService.isJOLiveFeedActive()) {
                miscTextRequested = RECORD_JUDGMENT;
                detailsTextRequested = textFormatter.judgmentRecorded();
            }

            builder.miscellaneous((Event.builder()
                .eventSequence(prepareEventSequence(builder.build()))
                .eventCode(MISCELLANEOUS.getCode())
                .dateReceived(getJbADate(caseData))
                .eventDetailsText(detailsTextRequested)
                .eventDetails(EventDetails.builder()
                                  .miscText(miscTextRequested)
                                  .build())
                .build()));
        }
    }

    private void buildSetAsideJudgment(EventHistory.EventHistoryBuilder builder, CaseData caseData) {
        if (featureToggleService.isJOLiveFeedActive() && caseData.getJoSetAsideReason() != null) {
            List<Event> events = new ArrayList<>();
            events.add(buildSetAsideJudgmentEvent(builder, caseData, RESPONDENT_ID));
            if (null != caseData.getRespondent2()) {
                events.add(buildSetAsideJudgmentEvent(builder, caseData, RESPONDENT2_ID));
            }
            builder.setAsideJudgment(events);
        }
    }

    private Event buildSetAsideJudgmentEvent(EventHistory.EventHistoryBuilder builder, CaseData caseData, String litigiousPartyID) {
        return Event.builder()
            .eventSequence(prepareEventSequence(builder.build()))
            .litigiousPartyID(litigiousPartyID)
            .eventCode(SET_ASIDE_JUDGMENT.getCode())
            .dateReceived(caseData.getJoSetAsideCreatedDate())
            .eventDetails(getSetAsideEventDetails(caseData))
            .eventDetailsText("")
            .build();
    }

    private EventDetails getSetAsideEventDetails(CaseData caseData) {
        String applicant = null;
        LocalDate appDate = null;
        LocalDate resultDate = null;
        if (JudgmentSetAsideReason.JUDGE_ORDER.equals(caseData.getJoSetAsideReason())) {
            if (JudgmentSetAsideOrderType.ORDER_AFTER_APPLICATION.equals(caseData.getJoSetAsideOrderType())) {
                appDate = caseData.getJoSetAsideApplicationDate();
            } else if (JudgmentSetAsideOrderType.ORDER_AFTER_DEFENCE.equals(caseData.getJoSetAsideOrderType())) {
                appDate = caseData.getJoSetAsideDefenceReceivedDate();
            }
            applicant = SetAsideApplicantTypeForRPA.PARTY_AGAINST.getValue();
            resultDate = caseData.getJoSetAsideOrderDate();

        } else if (JudgmentSetAsideReason.JUDGMENT_ERROR.equals(caseData.getJoSetAsideReason())) {
            applicant = SetAsideApplicantTypeForRPA.PROPER_OFFICER.getValue();
        }
        return EventDetails.builder()
            .result(SetAsideResultTypeForRPA.GRANTED.name())
            .applicant(applicant)
            .applicationDate(appDate)
            .resultDate(resultDate)
            .build();
    }

    private void buildJudgmentByAdmissionEventDetails(EventHistory.EventHistoryBuilder builder, CaseData caseData) {

        Optional<CCJPaymentDetails> ccjPaymentDetails = ofNullable(caseData.getCcjPaymentDetails());
        BigDecimal amountOfCosts = (caseData.isApplicantLipOneVOne() && featureToggleService.isLipVLipEnabled())
            ? ClaimFeeUtility.getCourtFee(caseData) : ccjPaymentDetails.map(CCJPaymentDetails::getCcjJudgmentFixedCostAmount).orElse(BigDecimal.ZERO)
            .add(ccjPaymentDetails.map(CCJPaymentDetails::getCcjJudgmentAmountClaimFee)
                     .map(amount -> amount.setScale(2)).orElse(ZERO));

        EventDetails judgmentByAdmissionEvent = EventDetails.builder()
            .amountOfJudgment(getAmountOfJudgmentForAdmission(caseData))
            .amountOfCosts(amountOfCosts)
            .amountPaidBeforeJudgment(ccjPaymentDetails.map(CCJPaymentDetails::getCcjPaymentPaidSomeAmountInPounds).map(amountPaid -> amountPaid.setScale(2)).orElse(ZERO))
            .isJudgmentForthwith(hasCourtDecisionInFavourOfClaimant(caseData) ? caseData.applicant1SuggestedPayImmediately() : caseData.isPayImmediately())
            .paymentInFullDate(getPaymentInFullDate(caseData))
            .installmentAmount(getInstallmentAmount(caseData))
            .installmentPeriod(getJBAInstallmentPeriod(caseData))
            .firstInstallmentDate(getFirstInstallmentDate(caseData))
            .dateOfJudgment(getJbADate(caseData))
            .jointJudgment(false)
            .judgmentToBeRegistered(true)
            .miscText("")
            .build();

        log.info("judgmentByAdmissionEvent: {}", judgmentByAdmissionEvent);

        builder.judgmentByAdmission((Event.builder()
            .eventSequence(prepareEventSequence(builder.build()))
            .eventCode(JUDGEMENT_BY_ADMISSION.getCode())
            .litigiousPartyID(featureToggleService.isJOLiveFeedActive() ? RESPONDENT_ID : APPLICANT_ID)
            .dateReceived(getJbADate(caseData))
            .eventDetails(judgmentByAdmissionEvent)
            .eventDetailsText("")
            .build()));
    }

    @Nullable
    private LocalDateTime getPaymentInFullDate(CaseData caseData) {
        RespondToClaimAdmitPartLRspec respondToClaimAdmitPartLRspec = caseData.getRespondToClaimAdmitPartLRspec();
        if (hasCourtDecisionInFavourOfClaimant(caseData)) {
            if (caseData.applicant1SuggestedPayBySetDate()) {
                return Optional.ofNullable(caseData.getApplicant1RequestedPaymentDateForDefendantSpec())
                    .map(PaymentBySetDate::getPaymentSetDate).map(LocalDate::atStartOfDay).orElse(null);
            } else {
                return null;
            }
        }
        return caseData.isPayBySetDate()
            ? Optional.ofNullable(respondToClaimAdmitPartLRspec)
            .map(RespondToClaimAdmitPartLRspec::getWhenWillThisAmountBePaid)
            .map(LocalDate::atStartOfDay).orElse(null)
            : null;
    }

    @NotNull
    protected BigDecimal getAmountOfJudgmentForAdmission(CaseData caseData) {
        Optional<CCJPaymentDetails> ccjPaymentDetails = ofNullable(caseData.getCcjPaymentDetails());
        return ccjPaymentDetails.map(CCJPaymentDetails::getCcjJudgmentAmountClaimAmount).orElse(ZERO)
            .add(caseData.isLipvLipOneVOne() && !caseData.isPartAdmitClaimSpec()
                ? ccjPaymentDetails.map(CCJPaymentDetails::getCcjJudgmentLipInterest).orElse(ZERO) : totalInterestForLrClaim(caseData)).setScale(2);
    }

    private BigDecimal totalInterestForLrClaim(CaseData caseData) {
        return featureToggleService.isLrAdmissionBulkEnabled() ? ZERO : Optional.ofNullable(caseData.getTotalInterest()).orElse(
            ZERO);
    }

    private void buildRespondentDivergentResponse(EventHistory.EventHistoryBuilder builder, CaseData caseData,
                                                  boolean goingOffline) {
        LocalDateTime respondent1ResponseDate = caseData.getRespondent1ResponseDate();
        LocalDateTime respondent2ResponseDate;
        if (ONE_V_TWO_ONE_LEGAL_REP == MultiPartyScenario.getMultiPartyScenario(caseData)) {
            // even if response is not the same, the date is
            respondent2ResponseDate = caseData.getRespondent1ResponseDate();
        } else {
            respondent2ResponseDate = caseData.getRespondent2ResponseDate();
        }
        String miscText;
        if (defendant1ResponseExists.test(caseData)) {
            if (SPEC_CLAIM.equals(caseData.getCaseAccessCategory())) {
                RespondentResponseTypeSpec respondent1SpecResponseType =
                    MultiPartyScenario.TWO_V_ONE.equals(getMultiPartyScenario(caseData))
                        ? caseData.getClaimant1ClaimResponseTypeForSpec()
                        : caseData.getRespondent1ClaimResponseTypeForSpec();

                buildRespondentResponseEventForSpec(builder, caseData, respondent1SpecResponseType,
                                                    respondent1ResponseDate, RESPONDENT_ID
                );
            } else {
                buildRespondentResponseEvent(builder, caseData, caseData.getRespondent1ClaimResponseType(),
                                             respondent1ResponseDate, RESPONDENT_ID
                );
            }

            boolean addMiscEvent;
            if (SPEC_CLAIM.equals(caseData.getCaseAccessCategory())) {
                addMiscEvent = goingOffline && !RespondentResponseTypeSpec.FULL_DEFENCE
                    .equals(caseData.getRespondent1ClaimResponseTypeForSpec());
            } else {
                addMiscEvent = !FULL_DEFENCE.equals(caseData.getRespondent1ClaimResponseType());
            }
            if (addMiscEvent) {
                miscText = prepareRespondentResponseText(caseData, caseData.getRespondent1(), true);
                builder.miscellaneous((Event.builder()
                    .eventSequence(prepareEventSequence(builder.build()))
                    .eventCode(MISCELLANEOUS.getCode())
                    .dateReceived(respondent1ResponseDate)
                    .eventDetailsText(miscText)
                    .eventDetails(EventDetails.builder()
                                      .miscText(miscText)
                                      .build())
                    .build()));
            }
        }

        if (defendant2DivergentResponseExists.test(caseData)) {
            if (SPEC_CLAIM.equals(caseData.getCaseAccessCategory())) {
                buildRespondentResponseEventForSpec(builder, caseData,
                                                    caseData.getRespondent2ClaimResponseTypeForSpec(),
                                                    respondent2ResponseDate, RESPONDENT2_ID
                );
            } else {
                buildRespondentResponseEvent(builder, caseData, caseData.getRespondent2ClaimResponseType(),
                                             respondent2ResponseDate, RESPONDENT2_ID
                );
            }

            boolean addMiscEvent;
            if (SPEC_CLAIM.equals(caseData.getCaseAccessCategory())) {
                addMiscEvent = goingOffline && !RespondentResponseTypeSpec.FULL_DEFENCE
                    .equals(caseData.getRespondent2ClaimResponseTypeForSpec());
            } else {
                addMiscEvent = !FULL_DEFENCE.equals(caseData.getRespondent2ClaimResponseType());
            }
            if (addMiscEvent) {
                miscText = prepareRespondentResponseText(caseData, caseData.getRespondent2(), false);
                builder.miscellaneous((Event.builder()
                    .eventSequence(prepareEventSequence(builder.build()))
                    .eventCode(MISCELLANEOUS.getCode())
                    .dateReceived(respondent2ResponseDate)
                    .eventDetailsText(miscText)
                    .eventDetails(EventDetails.builder()
                                      .miscText(miscText)
                                      .build())
                    .build()));
            }
        }
    }

    private void buildRespondentResponseEvent(EventHistory.EventHistoryBuilder builder,
                                              CaseData caseData,
                                              RespondentResponseType respondentResponseType,
                                              LocalDateTime respondentResponseDate,
                                              String respondentID) {
        switch (respondentResponseType) {
            case FULL_DEFENCE:
                buildDefenceFiled(builder, caseData, respondentResponseDate, respondentID);
                break;
            case PART_ADMISSION:
                buildReceiptOfPartAdmission(builder, respondentResponseDate, respondentID);
                break;
            case FULL_ADMISSION:
                buildReceiptOfAdmission(builder, respondentResponseDate, respondentID);
                break;
            default:
                break;
        }
    }

    private void buildRespondentResponseEventForSpec(EventHistory.EventHistoryBuilder builder,
                                                     CaseData caseData,
                                                     RespondentResponseTypeSpec respondentResponseTypeSpec,
                                                     LocalDateTime respondentResponseDate,
                                                     String respondentID) {
        switch (respondentResponseTypeSpec) {
            case FULL_DEFENCE:
                buildDefenceFiled(builder, caseData, respondentResponseDate, respondentID);
                break;
            case PART_ADMISSION:
                buildReceiptOfPartAdmission(builder, respondentResponseDate, respondentID);
                break;
            case FULL_ADMISSION:
                buildReceiptOfAdmission(builder, respondentResponseDate, respondentID);
                break;
            default:
                break;
        }
    }

    private void buildDefenceFiled(EventHistory.EventHistoryBuilder builder,
                                   CaseData caseData,
                                   LocalDateTime respondentResponseDate,
                                   String respondentID) {
        if (respondentID.equals(RESPONDENT_ID)) {
            RespondToClaim respondToClaim = caseData.getRespondToClaim();
            if (isAllPaid(caseData.getTotalClaimAmount(), respondToClaim)) {
                builder.statesPaid(buildDefenceFiledEvent(
                    builder, respondentResponseDate, respondentID,
                    isAllPaid(caseData.getTotalClaimAmount(), respondToClaim)
                ));
            } else {
                builder.defenceFiled(buildDefenceFiledEvent(
                    builder, respondentResponseDate, respondentID,
                    false
                ));
            }
            builder.directionsQuestionnaire(buildDirectionsQuestionnaireFiledEvent(
                builder, caseData, respondentResponseDate, respondentID,
                caseData.getRespondent1DQ(), caseData.getRespondent1(), true
            ));
        } else {
            RespondToClaim respondToClaim;
            if (ONE_V_TWO_ONE_LEGAL_REP.equals(MultiPartyScenario.getMultiPartyScenario(caseData))
                && caseData.getSameSolicitorSameResponse() == YES) {
                respondToClaim = caseData.getRespondToClaim();
            } else {
                respondToClaim = caseData.getRespondToClaim2();
            }
            if (isAllPaid(caseData.getTotalClaimAmount(), respondToClaim)) {
                builder.statesPaid(buildDefenceFiledEvent(
                    builder, respondentResponseDate, respondentID,
                    isAllPaid(caseData.getTotalClaimAmount(), respondToClaim)
                ));
            } else {
                builder.defenceFiled(buildDefenceFiledEvent(
                    builder, respondentResponseDate, respondentID,
                    false
                ));
            }
            builder.directionsQuestionnaire(buildDirectionsQuestionnaireFiledEvent(
                builder, caseData, respondentResponseDate, respondentID,
                caseData.getRespondent2DQ(), caseData.getRespondent2(), false
            ));
        }
    }

    private boolean isAllPaid(BigDecimal totalClaimAmount, RespondToClaim claimResponse) {
        return totalClaimAmount != null
            && Optional.ofNullable(claimResponse).map(RespondToClaim::getHowMuchWasPaid)
            .map(paid -> MonetaryConversions.penniesToPounds(paid).compareTo(totalClaimAmount) >= 0).orElse(false);
    }

    private void buildReceiptOfPartAdmission(EventHistory.EventHistoryBuilder builder,
                                             LocalDateTime respondentResponseDate,
                                             String respondentID) {
        builder.receiptOfPartAdmission(
            Event.builder()
                .eventSequence(prepareEventSequence(builder.build()))
                .eventCode(RECEIPT_OF_PART_ADMISSION.getCode())
                .dateReceived(respondentResponseDate)
                .litigiousPartyID(respondentID)
                .build());
    }

    private void buildReceiptOfAdmission(EventHistory.EventHistoryBuilder builder,
                                         LocalDateTime respondentResponseDate,
                                         String respondentID) {
        builder.receiptOfAdmission(
            Event.builder()
                .eventSequence(prepareEventSequence(builder.build()))
                .eventCode(RECEIPT_OF_ADMISSION.getCode())
                .dateReceived(respondentResponseDate)
                .litigiousPartyID(respondentID)
                .build());
    }

    public String prepareRespondentResponseText(CaseData caseData, Party respondent, boolean isRespondent1) {
        MultiPartyScenario scenario = getMultiPartyScenario(caseData);
        String defaultText = "";
        if (scenario.equals(ONE_V_ONE) || scenario.equals(TWO_V_ONE)) {
            if (SPEC_CLAIM.equals(caseData.getCaseAccessCategory())) {
                var responseTypeForSpec = scenario.equals(TWO_V_ONE)
                    ? (YES.equals(caseData.getDefendantSingleResponseToBothClaimants())
                    ? caseData.getRespondent1ClaimResponseTypeForSpec()
                    : caseData.getClaimant1ClaimResponseTypeForSpec())
                    : caseData.getRespondent1ClaimResponseTypeForSpec();

                switch (responseTypeForSpec) {
                    case COUNTER_CLAIM:
                        defaultText = textFormatter.defendantRejectsAndCounterClaims();
                        break;
                    case FULL_ADMISSION:
                        defaultText = textFormatter.defendantFullyAdmits();
                        break;
                    case PART_ADMISSION:
                        defaultText = textFormatter.defendantPartialAdmission();
                        break;
                    default:
                        break;
                }
            } else {
                switch (caseData.getRespondent1ClaimResponseType()) {
                    case COUNTER_CLAIM:
                        defaultText = textFormatter.defendantRejectsAndCounterClaims();
                        break;
                    case FULL_ADMISSION:
                        defaultText = textFormatter.defendantFullyAdmits();
                        break;
                    case PART_ADMISSION:
                        defaultText = textFormatter.defendantPartialAdmission();
                        break;
                    default:
                        break;
                }
            }
        } else {
            String paginatedMessage = "";
            if (scenario.equals(ONE_V_TWO_ONE_LEGAL_REP)) {
                paginatedMessage = getPaginatedMessageFor1v2SameSolicitor(caseData, isRespondent1);
            }
            defaultText = textFormatter.formatRpa(
                "%sDefendant: %s has responded: %s",
                paginatedMessage,
                respondent.getPartyName(),
                SPEC_CLAIM.equals(caseData.getCaseAccessCategory())
                    ? getResponseTypeForRespondentSpec(caseData, respondent)
                    : getResponseTypeForRespondent(caseData, respondent)
            );
        }
        return defaultText;
    }

    private void buildCaseNotesEvents(EventHistory.EventHistoryBuilder builder, CaseData caseData) {
        if (isNotEmpty(caseData.getCaseNotes())) {
            buildMiscellaneousCaseNotesEvent(builder, caseData);
        }

    }

    private void buildMiscellaneousCaseNotesEvent(EventHistory.EventHistoryBuilder builder, CaseData caseData) {
        List<Event> events = unwrapElements(caseData.getCaseNotes())
            .stream()
            .map(caseNote ->
                     Event.builder()
                         .eventSequence(prepareEventSequence(builder.build()))
                         .eventCode(MISCELLANEOUS.getCode())
                         .dateReceived(caseNote.getCreatedOn())
                         .eventDetailsText(left((format(
                             "case note added: %s",
                             caseNote.getNote() != null
                                 ? caseNote.getNote().replaceAll("\\s+", " ") : ""
                         )), 250))
                         .eventDetails(EventDetails.builder()
                                           .miscText(left((format(
                                               "case note added: %s",
                                               caseNote.getNote() != null
                                                   ? caseNote.getNote().replaceAll("\\s+", " ") : ""
                                           )), 250))
                                           .build())
                         .build())
            .toList();
        builder.miscellaneous(events);
    }

    private void buildGeneralFormApplicationEventsStrikeOutOrder(EventHistory.EventHistoryBuilder builder,
                                                                 CaseData caseData) {
        if (caseData.getGeneralApplications() != null) {
            var generalApplications = caseData
                    .getGeneralApplications()
                    .stream()
                    .filter(application -> application.getValue().getGeneralAppType().getTypes().contains(STRIKE_OUT)
                            && getGeneralApplicationDetailsJudgeDecisionWithStruckOutDefence(
                            application.getValue()
                                    .getCaseLink()
                                    .getCaseReference(),
                            caseData
                    )
                            != null)
                    .toList();

            if (!generalApplications.isEmpty()) {
                buildGeneralFormOfApplicationStrikeOut(builder, generalApplications);
                buildDefenceStruckOutJudgmentEvent(builder, generalApplications);
            }
        }

    }

    private void buildGeneralFormOfApplicationStrikeOut(EventHistory.EventHistoryBuilder builder,
                                                        List<Element<GeneralApplication>> generalApplicationsStrikeOut) {

        List<Event> generalApplicationsEvents = IntStream.range(0, generalApplicationsStrikeOut.size())
                .mapToObj(index -> {
                    String miscText = "APPLICATION TO Strike Out";
                    return Event.builder()
                            .eventSequence(prepareEventSequence(builder.build()))
                            .eventCode(GENERAL_FORM_OF_APPLICATION.getCode())
                            .dateReceived(generalApplicationsStrikeOut
                                    .get(index)
                                    .getValue()
                                    .getGeneralAppSubmittedDateGAspec())
                            .litigiousPartyID(generalApplicationsStrikeOut
                                    .get(index)
                                    .getValue()
                                    .getLitigiousPartyID())
                            .eventDetailsText(miscText)
                            .eventDetails(EventDetails.builder()
                                    .miscText(miscText)
                                    .build())
                            .build();
                })
                .toList();

        builder.generalFormOfApplication(generalApplicationsEvents);
    }

    private void buildDefenceStruckOutJudgmentEvent(EventHistory.EventHistoryBuilder builder,
                                                    List<Element<GeneralApplication>> generalApplicationsStrikeOut) {

        List<Event> generalApplicationsEvents = IntStream.range(0, generalApplicationsStrikeOut.size())
                .mapToObj(index -> {
                    return Event.builder()
                            .eventSequence(prepareEventSequence(builder.build()))
                            .eventCode(DEFENCE_STRUCK_OUT.getCode())
                            .dateReceived(generalApplicationsStrikeOut
                                    .get(index)
                                    .getValue()
                                    .getGeneralAppSubmittedDateGAspec())
                            .litigiousPartyID(generalApplicationsStrikeOut
                                    .get(index)
                                    .getValue()
                                    .getLitigiousPartyID())
                            .build();
                })
                .toList();

        builder.defenceStruckOut(generalApplicationsEvents);
    }

    private Element<GeneralApplicationsDetails> getGeneralApplicationDetailsJudgeDecisionWithStruckOutDefence(
            String caseLinkId, CaseData caseData) {
        return caseData.getGaDetailsMasterCollection().stream()
                .filter(generalApplicationsDetailsElement ->
                        generalApplicationsDetailsElement
                                .getValue()
                                .getCaseLink()
                                .getCaseReference()
                                .equals(caseLinkId)
                                && generalApplicationsDetailsElement.getValue().getCaseState()
                                .equals(PROCEEDS_IN_HERITAGE.getDisplayedValue()))
                .findFirst()
                .orElse(null);
    }

    private int prepareEventSequence(EventHistory history) {
        return sequenceGenerator.nextSequence(history);
    }

    private void buildTakenOfflineMultitrackUnspec(EventHistory.EventHistoryBuilder builder, CaseData caseData) {
        if (AllocatedTrack.MULTI_CLAIM.equals(caseData.getAllocatedTrack())) {
            String miscText = textFormatter.multitrackUnspecOffline();
            builder.miscellaneous(
                Event.builder()
                    .eventSequence(prepareEventSequence(builder.build()))
                    .eventCode(MISCELLANEOUS.getCode())
                    .dateReceived(caseData.getApplicant1ResponseDate())
                    .eventDetailsText(miscText)
                    .eventDetails(EventDetails.builder()
                                      .miscText(miscText)
                                      .build())
                    .build());
        }
    }

    private void buildFullDefenceProceed(EventHistory.EventHistoryBuilder builder, CaseData caseData, String authToken) {
        List<ClaimantResponseDetails> applicantDetails = prepareApplicantsDetails(caseData);
        final List<String> miscEventText = prepMultipartyProceedMiscText(caseData);

        CaseCategory claimType = caseData.getCaseAccessCategory();
        if (SPEC_CLAIM.equals(claimType)) {
            List<Event> dqForProceedingApplicantsSpec = IntStream.range(0, applicantDetails.size())
                .mapToObj(index ->
                              Event.builder()
                                  .eventSequence(prepareEventSequence(builder.build()))
                                  .eventCode(DIRECTIONS_QUESTIONNAIRE_FILED.getCode())
                                  .dateReceived(applicantDetails.get(index).getResponseDate())
                                  .litigiousPartyID(applicantDetails.get(index).getLitigiousPartyID())
                                  .eventDetails(EventDetails.builder()
                                                    .stayClaim(isStayClaim(applicantDetails.get(index).getDq()))
                                                    .preferredCourtCode(
                                                        getPreferredCourtCode(caseData.getApplicant1DQ()))
                                                    .preferredCourtName("")
                                                    .build())
                                  .eventDetailsText(prepareEventDetailsText(
                                      applicantDetails.get(index).getDq(),
                                      getPreferredCourtCode(caseData.getApplicant1DQ())
                                  ))
                                  .build())
                .toList();
            builder.directionsQuestionnaireFiled(dqForProceedingApplicantsSpec);
        } else {
            String preferredCourtCode = locationRefDataUtil.getPreferredCourtData(
                caseData,
                authToken, true
            );

            List<Event> dqForProceedingApplicants = IntStream.range(0, applicantDetails.size())
                .mapToObj(index ->
                              Event.builder()
                                  .eventSequence(prepareEventSequence(builder.build()))
                                  .eventCode(DIRECTIONS_QUESTIONNAIRE_FILED.getCode())
                                  .dateReceived(applicantDetails.get(index).getResponseDate())
                                  .litigiousPartyID(applicantDetails.get(index).getLitigiousPartyID())
                                  .eventDetails(EventDetails.builder()
                                                    .stayClaim(isStayClaim(applicantDetails.get(index).getDq()))
                                                    .preferredCourtCode(preferredCourtCode)
                                                    .preferredCourtName("")
                                                    .build())
                                  .eventDetailsText(prepareEventDetailsText(
                                      applicantDetails.get(index).getDq(),
                                      preferredCourtCode
                                  ))
                                  .build())
                .toList();
            builder.directionsQuestionnaireFiled(dqForProceedingApplicants);
        }

        YesOrNo proceedRespondent1;
        YesOrNo proceedRespondent2;
        YesOrNo applicant1Proceeds;
        YesOrNo applicant2Proceeds;
        YesOrNo applicant1MediationRequired;

        if (caseData.getApplicant1ClaimMediationSpecRequired() == null
            || caseData.getApplicant1ClaimMediationSpecRequired()
            .getHasAgreedFreeMediation() == null
        ) {
            applicant1MediationRequired = NO;
        } else {
            applicant1MediationRequired = caseData.getApplicant1ClaimMediationSpecRequired()
                .getHasAgreedFreeMediation();
        }

        YesOrNo applicant2MediationRequired;

        if (caseData.getApplicantMPClaimMediationSpecRequired() == null
            || caseData.getApplicantMPClaimMediationSpecRequired()
            .getHasAgreedFreeMediation() == null
        ) {
            applicant2MediationRequired = NO;
        } else {
            applicant2MediationRequired = caseData.getApplicantMPClaimMediationSpecRequired()
                .getHasAgreedFreeMediation();
        }

        YesOrNo respondent1MediationRequired;
        YesOrNo respondent2MediationRequired;

        String track = caseData.getResponseClaimTrack();

        switch (getMultiPartyScenario(caseData)) {

            case ONE_V_ONE:

                respondent1MediationRequired = caseData.getResponseClaimMediationSpecRequired();

                if (claimType == SPEC_CLAIM
                    && AllocatedTrack.SMALL_CLAIM.name().equals(track)
                    && respondent1MediationRequired == YesOrNo.YES
                    && applicant1MediationRequired == YesOrNo.YES
                ) {
                    List<Event> miscText = prepareMiscEventList(builder, caseData, miscEventText);
                    builder.miscellaneous(miscText);
                } else {
                    List<String> applicantProceedsText = new ArrayList<>();
                    applicantProceedsText.add(textFormatter.claimantProceeds());
                    List<Event> miscText = prepareMiscEventList(builder, caseData, applicantProceedsText);
                    builder.miscellaneous(miscText);
                    buildTakenOfflineMultitrackUnspec(builder, caseData);
                }
                break;
            case ONE_V_TWO_ONE_LEGAL_REP:
                proceedRespondent1 =
                    caseData.getApplicant1ProceedWithClaimAgainstRespondent1MultiParty1v2();
                proceedRespondent2 =
                    caseData.getApplicant1ProceedWithClaimAgainstRespondent2MultiParty1v2();
                respondent1MediationRequired = caseData.getResponseClaimMediationSpecRequired();

                if (NO.equals(proceedRespondent1) || NO.equals(proceedRespondent2)
                    || (claimType == SPEC_CLAIM
                    && AllocatedTrack.SMALL_CLAIM.name().equals(track)
                    && respondent1MediationRequired == YesOrNo.YES
                    && applicant1MediationRequired == YesOrNo.YES
                    )
                ) {
                    List<Event> miscText = prepareMiscEventList(builder, caseData, miscEventText);
                    builder.miscellaneous(miscText);
                } else {
                    List<String> applicantProceedsText = new ArrayList<>();
                    applicantProceedsText.add(textFormatter.claimantProceeds());
                    List<Event> miscText = prepareMiscEventList(builder, caseData, applicantProceedsText);
                    builder.miscellaneous(miscText);
                    buildTakenOfflineMultitrackUnspec(builder, caseData);
                }
                break;
            case ONE_V_TWO_TWO_LEGAL_REP:

                proceedRespondent1 =
                    caseData.getApplicant1ProceedWithClaimAgainstRespondent1MultiParty1v2();
                proceedRespondent2 =
                    caseData.getApplicant1ProceedWithClaimAgainstRespondent2MultiParty1v2();
                respondent1MediationRequired = caseData.getResponseClaimMediationSpecRequired();
                respondent2MediationRequired = caseData.getResponseClaimMediationSpec2Required();

                if (NO.equals(proceedRespondent1) || NO.equals(proceedRespondent2)
                    || (claimType == SPEC_CLAIM
                    && AllocatedTrack.SMALL_CLAIM.name().equals(track)
                    && respondent1MediationRequired == YesOrNo.YES
                    && respondent2MediationRequired == YesOrNo.YES
                    && applicant1MediationRequired == YesOrNo.YES
                    )
                ) {
                    List<Event> miscText = prepareMiscEventList(builder, caseData, miscEventText);
                    builder.miscellaneous(miscText);
                } else {
                    List<String> applicantProceedsText = new ArrayList<>();
                    applicantProceedsText.add(textFormatter.claimantProceeds());
                    List<Event> miscText = prepareMiscEventList(builder, caseData, applicantProceedsText);
                    builder.miscellaneous(miscText);
                    buildTakenOfflineMultitrackUnspec(builder, caseData);
                }
                break;
            case TWO_V_ONE:

                applicant1Proceeds = caseData.getApplicant1ProceedWithClaimMultiParty2v1();
                applicant2Proceeds = caseData.getApplicant2ProceedWithClaimMultiParty2v1();
                respondent1MediationRequired = caseData.getResponseClaimMediationSpecRequired();

                if (NO.equals(applicant1Proceeds) || NO.equals(applicant2Proceeds)
                    || (claimType == SPEC_CLAIM
                    && AllocatedTrack.SMALL_CLAIM.name().equals(track)
                    && respondent1MediationRequired == YesOrNo.YES
                    && applicant1MediationRequired == YesOrNo.YES
                    && applicant2MediationRequired == YesOrNo.YES
                    )
                ) {
                    List<Event> miscText = prepareMiscEventList(builder, caseData, miscEventText);
                    builder.miscellaneous(miscText);
                } else {
                    List<String> applicantProceedsText = new ArrayList<>();
                    applicantProceedsText.add("Claimants proceed.");
                    List<Event> miscText = prepareMiscEventList(builder, caseData, applicantProceedsText);
                    builder.miscellaneous(miscText);
                    buildTakenOfflineMultitrackUnspec(builder, caseData);
                }
                break;
            default:
        }
    }

    private List<ClaimantResponseDetails> prepareApplicantsDetails(CaseData caseData) {
        List<ClaimantResponseDetails> applicantsDetails = new ArrayList<>();
        if (getMultiPartyScenario(caseData).equals(TWO_V_ONE)) {
            if (YES.equals(caseData.getApplicant1ProceedWithClaimMultiParty2v1())
                || YES.equals(caseData.getApplicant1ProceedWithClaimSpec2v1())) {
                applicantsDetails.add(ClaimantResponseDetails.builder()
                                          .dq(caseData.getApplicant1DQ())
                                          .litigiousPartyID(APPLICANT_ID)
                                          .responseDate(caseData.getApplicant1ResponseDate())
                                          .build());
            }
            if (YES.equals(caseData.getApplicant2ProceedWithClaimMultiParty2v1())
                || YES.equals(caseData.getApplicant1ProceedWithClaimSpec2v1())) {
                applicantsDetails.add(ClaimantResponseDetails.builder()
                                          .dq(caseData.getApplicant2DQ())
                                          .litigiousPartyID(APPLICANT2_ID)
                                          .responseDate(
                                              SPEC_CLAIM.equals(caseData.getCaseAccessCategory())
                                                  ? caseData.getApplicant1ResponseDate()
                                                  : caseData.getApplicant2ResponseDate())
                                          .build());
            }
        } else {
            applicantsDetails.add(ClaimantResponseDetails.builder()
                                      .dq(caseData.getApplicant1DQ())
                                      .litigiousPartyID(APPLICANT_ID)
                                      .responseDate(caseData.getApplicant1ResponseDate())
                                      .build());
        }
        return applicantsDetails;
    }

    public String prepareEventDetailsText(DQ dq, String preferredCourtCode) {
        return format(
            "preferredCourtCode: %s; stayClaim: %s",
            preferredCourtCode,
            isStayClaim(dq)
        );
    }

    static final String PROCEED = "proceed";
    static final String NOT_PROCEED = "not proceed";

    private List<String> prepMultipartyProceedMiscText(CaseData caseData) {
        List<String> eventDetailsText = new ArrayList<>();
        String currentTime = timelineHelper.now().toLocalDate().toString();

        switch (getMultiPartyScenario(caseData)) {
            case ONE_V_TWO_ONE_LEGAL_REP, ONE_V_TWO_TWO_LEGAL_REP: {
                eventDetailsText.add(textFormatter.formatRpa(
                    "[1 of 2 - %s] Claimant has provided intention: %s against defendant: %s",
                    currentTime,
                    YES.equals(caseData.getApplicant1ProceedWithClaimAgainstRespondent1MultiParty1v2())
                        ? PROCEED
                        : NOT_PROCEED,
                    caseData.getRespondent1().getPartyName()
                ));
                eventDetailsText.add(textFormatter.formatRpa(
                    "[2 of 2 - %s] Claimant has provided intention: %s against defendant: %s",
                    currentTime,
                    YES.equals(caseData.getApplicant1ProceedWithClaimAgainstRespondent2MultiParty1v2())
                        ? PROCEED
                        : NOT_PROCEED,
                    caseData.getRespondent2().getPartyName()
                ));
                break;
            }
            case TWO_V_ONE: {
                YesOrNo app1Proceeds;
                YesOrNo app2Proceeds;
                if (SPEC_CLAIM.equals(caseData.getCaseAccessCategory())) {
                    app1Proceeds = caseData.getApplicant1ProceedWithClaimSpec2v1();
                    app2Proceeds = app1Proceeds;
                } else {
                    app1Proceeds = caseData.getApplicant1ProceedWithClaimMultiParty2v1();
                    app2Proceeds = caseData.getApplicant2ProceedWithClaimMultiParty2v1();
                }
                eventDetailsText.add(textFormatter.formatRpa(
                    "[1 of 2 - %s] Claimant: %s has provided intention: %s",
                    currentTime,
                    caseData.getApplicant1().getPartyName(),
                    YES.equals(app1Proceeds)
                        ? PROCEED
                        : NOT_PROCEED
                ));
                eventDetailsText.add(textFormatter.formatRpa(
                    "[2 of 2 - %s] Claimant: %s has provided intention: %s",
                    currentTime,
                    caseData.getApplicant2().getPartyName(),
                    YES.equals(app2Proceeds)
                        ? PROCEED
                        : NOT_PROCEED
                ));
                break;
            }
            case ONE_V_ONE:
            default: {
                eventDetailsText.add(textFormatter.claimantProceeds());
            }
        }
        return eventDetailsText;
    }

    public List<Event> prepareMiscEventList(EventHistory.EventHistoryBuilder builder, CaseData caseData,
                                            List<String> miscEventText, LocalDateTime... eventDate) {
        return IntStream.range(0, miscEventText.size())
            .mapToObj(index ->
                          Event.builder()
                              .eventSequence(prepareEventSequence(builder.build()))
                              .eventCode(MISCELLANEOUS.getCode())
                              .dateReceived(eventDate.length > 0
                                                && eventDate[0] != null
                                                ? eventDate[0] : caseData.getApplicant1ResponseDate())
                              .eventDetailsText(miscEventText.get(index))
                              .eventDetails(EventDetails.builder()
                                                .miscText(miscEventText.get(index))
                                                .build())
                              .build())
            .toList();
    }

    public boolean isStayClaim(DQ dq) {
        return ofNullable(dq).map(DQ::getFileDirectionQuestionnaire)
            .map(FileDirectionsQuestionnaire::getOneMonthStayRequested)
            .orElse(NO) == YES;
    }

    public String getPreferredCourtCode(DQ dq) {
        return ofNullable(dq).map(DQ::getRequestedCourt)
            .map(RequestedCourt::getResponseCourtCode)
            .orElse("");
    }

    private void buildFullDefenceNotProceed(EventHistory.EventHistoryBuilder builder, CaseData caseData) {
        String miscText = getMultiPartyScenario(caseData).equals(TWO_V_ONE)
            ? textFormatter.withRpaPrefix("Claimants intend not to proceed.")
            : textFormatter.withRpaPrefix("Claimant intends not to proceed.");

        builder.miscellaneous(Event.builder()
                                  .eventSequence(prepareEventSequence(builder.build()))
                                  .eventCode(MISCELLANEOUS.getCode())
                                  .dateReceived(caseData.getApplicant1ResponseDate())
                                  .eventDetailsText(miscText)
                                  .eventDetails(EventDetails.builder()
                                                    .miscText(miscText)
                                                    .build())
                                  .build());
    }

    private void buildRespondentFullDefence(EventHistory.EventHistoryBuilder builder, CaseData caseData) {
        List<Event> defenceFiledEvents = new ArrayList<>();
        List<Event> statesPaidEvents = new ArrayList<>();
        List<Event> directionsQuestionnaireFiledEvents = new ArrayList<>();
        boolean isRespondent1;
        if (defendant1ResponseExists.test(caseData)) {
            isRespondent1 = true;
            Party respondent1 = caseData.getRespondent1();
            Respondent1DQ respondent1DQ = caseData.getRespondent1DQ();
            LocalDateTime respondent1ResponseDate = caseData.getRespondent1ResponseDate();

            if (caseData.isLRvLipOneVOne() || caseData.isLipvLipOneVOne()) {
                buildLrVLipFullDefenceEvent(builder, caseData, defenceFiledEvents, statesPaidEvents);
            } else {
                if (isAllPaid(caseData.getTotalClaimAmount(), caseData.getRespondToClaim())) {
                    statesPaidEvents.add(buildDefenceFiledEvent(
                        builder,
                        respondent1ResponseDate,
                        RESPONDENT_ID,
                        true
                    ));
                } else {
                    defenceFiledEvents.add(
                        buildDefenceFiledEvent(
                            builder,
                            respondent1ResponseDate,
                            RESPONDENT_ID,
                            false
                        ));
                }
            }
            directionsQuestionnaireFiledEvents.add(
                buildDirectionsQuestionnaireFiledEvent(builder, caseData,
                                                       respondent1ResponseDate,
                                                       RESPONDENT_ID,
                                                       respondent1DQ,
                                                       respondent1,
                                                       isRespondent1
                ));
            if (defendant1v2SameSolicitorSameResponse.test(caseData)) {
                Party respondent2 = caseData.getRespondent2();
                Respondent1DQ respondent2DQ = caseData.getRespondent1DQ();
                LocalDateTime respondent2ResponseDate = null != caseData.getRespondent2ResponseDate()
                    ? caseData.getRespondent2ResponseDate() : caseData.getRespondent1ResponseDate();

                if (isAllPaid(caseData.getTotalClaimAmount(), caseData.getRespondToClaim())) {
                    statesPaidEvents.add(buildDefenceFiledEvent(
                        builder,
                        respondent1ResponseDate,
                        RESPONDENT2_ID,
                        true
                    ));
                }
                defenceFiledEvents.add(
                    buildDefenceFiledEvent(
                        builder,
                        respondent2ResponseDate,
                        RESPONDENT2_ID,
                        false
                    ));
                directionsQuestionnaireFiledEvents.add(
                    buildDirectionsQuestionnaireFiledEvent(builder, caseData,
                                                           respondent2ResponseDate,
                                                           RESPONDENT2_ID,
                                                           respondent2DQ,
                                                           respondent2,
                                                           isRespondent1
                    ));
            }
        }
        if (defendant2ResponseExists.test(caseData)) {
            isRespondent1 = false;
            Party respondent2 = caseData.getRespondent2();
            Respondent2DQ respondent2DQ = caseData.getRespondent2DQ();
            LocalDateTime respondent2ResponseDate = caseData.getRespondent2ResponseDate();

            if (isAllPaid(caseData.getTotalClaimAmount(), caseData.getRespondToClaim2())) {
                statesPaidEvents.add(
                    buildDefenceFiledEvent(
                        builder,
                        respondent2ResponseDate,
                        RESPONDENT2_ID,
                        true
                    ));
            } else {
                defenceFiledEvents.add(
                    buildDefenceFiledEvent(
                        builder,
                        respondent2ResponseDate,
                        RESPONDENT2_ID,
                        false
                    ));
            }
            directionsQuestionnaireFiledEvents.add(
                buildDirectionsQuestionnaireFiledEvent(builder, caseData,
                                                       respondent2ResponseDate,
                                                       RESPONDENT2_ID,
                                                       respondent2DQ,
                                                       respondent2,
                                                       isRespondent1
                ));
        }
        builder.defenceFiled(defenceFiledEvents);
        builder.statesPaid(statesPaidEvents);
        builder.clearDirectionsQuestionnaireFiled().directionsQuestionnaireFiled(directionsQuestionnaireFiledEvents);
    }

    private Event buildDirectionsQuestionnaireFiledEvent(EventHistory.EventHistoryBuilder builder,
                                                         CaseData caseData,
                                                         LocalDateTime respondentResponseDate,
                                                         String litigiousPartyID,
                                                         DQ respondentDQ,
                                                         Party respondent,
                                                         boolean isRespondent1) {
        return Event.builder()
            .eventSequence(prepareEventSequence(builder.build()))
            .eventCode(DIRECTIONS_QUESTIONNAIRE_FILED.getCode())
            .dateReceived(respondentResponseDate)
            .litigiousPartyID(litigiousPartyID)
            .eventDetailsText(prepareFullDefenceEventText(
                respondentDQ,
                caseData,
                isRespondent1,
                respondent
            ))
            .eventDetails(EventDetails.builder()
                              .stayClaim(isStayClaim(respondentDQ))
                              .preferredCourtCode(getPreferredCourtCode(respondentDQ))
                              .preferredCourtName("")
                              .build())
            .build();
    }

    private Event buildDefenceFiledEvent(EventHistory.EventHistoryBuilder builder,
                                         LocalDateTime respondentResponseDate,
                                         String litigiousPartyID,
                                         boolean statesPaid) {
        return Event.builder()
            .eventSequence(prepareEventSequence(builder.build()))
            .eventCode(statesPaid ? STATES_PAID.getCode() : DEFENCE_FILED.getCode())
            .dateReceived(respondentResponseDate)
            .litigiousPartyID(litigiousPartyID)
            .build();
    }

    public String prepareFullDefenceEventText(DQ dq, CaseData caseData, boolean isRespondent1, Party respondent) {
        MultiPartyScenario scenario = getMultiPartyScenario(caseData);
        String paginatedMessage = "";
        if (scenario.equals(ONE_V_TWO_ONE_LEGAL_REP)) {
            paginatedMessage = getPaginatedMessageFor1v2SameSolicitor(caseData, isRespondent1);
        }
        return (format(
            "%sDefendant: %s has responded: %s; "
                + "preferredCourtCode: %s; stayClaim: %s",
            paginatedMessage,
            respondent.getPartyName(),
            getResponseTypeForRespondent(caseData, respondent),
            getPreferredCourtCode(dq),
            isStayClaim(dq)
        ));
    }

    private String getPaginatedMessageFor1v2SameSolicitor(CaseData caseData, boolean isRespondent1) {
        int index = 1;
        LocalDateTime respondent1ResponseDate = caseData.getRespondent1ResponseDate();
        LocalDateTime respondent2ResponseDate = caseData.getRespondent2ResponseDate();
        if (respondent1ResponseDate != null && respondent2ResponseDate != null) {
            index = isRespondent1 ? 1 : 2;
        }
        return format(
            "[%d of 2 - %s] ",
            index,
            timelineHelper.now().toLocalDate().toString()
        );
    }

    public String evaluateRespondent2IntentionType(CaseData caseData) {
        if (caseData.getRespondent2ClaimResponseIntentionType() != null) {
            return caseData.getRespondent2ClaimResponseIntentionType().getLabel();
        }
        return caseData.getRespondent1ClaimResponseIntentionType() != null
            ? caseData.getRespondent1ClaimResponseIntentionType().getLabel()
            : null;
    }

    private void buildRespondentResponseText(EventHistory.EventHistoryBuilder builder, CaseData caseData, String miscText, LocalDateTime respondentResponseDate) {
        if (!SPEC_CLAIM.equals(caseData.getCaseAccessCategory())) {
            builder.miscellaneous(Event.builder()
                                      .eventSequence(prepareEventSequence(builder.build()))
                                      .eventCode(MISCELLANEOUS.getCode())
                                      .dateReceived(respondentResponseDate)
                                      .eventDetailsText(miscText)
                                      .eventDetails(EventDetails.builder()
                                                        .miscText(miscText)
                                                        .build())
                                      .build());
        }

    }

    private void buildRespondentFullAdmission(EventHistory.EventHistoryBuilder builder, CaseData caseData) {
        String miscText;
        if (defendant1ResponseExists.test(caseData)) {
            miscText = prepareRespondentResponseText(caseData, caseData.getRespondent1(), true);
            builder.receiptOfAdmission(Event.builder()
                                           .eventSequence(prepareEventSequence(builder.build()))
                                           .eventCode(RECEIPT_OF_ADMISSION.getCode())
                                           .dateReceived(caseData.getRespondent1ResponseDate())
                                           .litigiousPartyID(RESPONDENT_ID)
                                           .build()
            );
            buildRespondentResponseText(builder, caseData, miscText, caseData.getRespondent1ResponseDate());
            buildMiscellaneousForRespondentResponseLipVSLr(builder, caseData);
            if (defendant1v2SameSolicitorSameResponse.test(caseData)) {
                LocalDateTime respondent2ResponseDate = null != caseData.getRespondent2ResponseDate()
                    ? caseData.getRespondent2ResponseDate() : caseData.getRespondent1ResponseDate();
                miscText = prepareRespondentResponseText(caseData, caseData.getRespondent2(), false);
                builder.receiptOfAdmission(Event.builder()
                                               .eventSequence(prepareEventSequence(builder.build()))
                                               .eventCode(RECEIPT_OF_ADMISSION.getCode())
                                               .dateReceived(respondent2ResponseDate)
                                               .litigiousPartyID(RESPONDENT2_ID)
                                               .build()
                );
                buildRespondentResponseText(builder, caseData, miscText, respondent2ResponseDate);
            }
        }
        if (defendant2ResponseExists.test(caseData)) {
            miscText = prepareRespondentResponseText(caseData, caseData.getRespondent2(), false);
            builder.receiptOfAdmission(Event.builder()
                                           .eventSequence(prepareEventSequence(builder.build()))
                                           .eventCode(RECEIPT_OF_ADMISSION.getCode())
                                           .dateReceived(caseData.getRespondent2ResponseDate())
                                           .litigiousPartyID(RESPONDENT2_ID)
                                           .build()
            );
            buildRespondentResponseText(builder, caseData, miscText, caseData.getRespondent2ResponseDate());
        }
    }

    private void buildRespondentPartAdmission(EventHistory.EventHistoryBuilder builder, CaseData caseData) {
        String miscText;
        List<Event> directionsQuestionnaireFiledEvents = new ArrayList<>();
        if (defendant1ResponseExists.test(caseData)) {
            final Party respondent1 = caseData.getRespondent1();
            miscText = prepareRespondentResponseText(caseData, caseData.getRespondent1(), true);
            LocalDateTime respondent1ResponseDate = caseData.getRespondent1ResponseDate();
            buildMiscellaneousForRespondentResponseLipVSLr(builder, caseData);
            Respondent1DQ respondent1DQ = caseData.getRespondent1DQ();
            if (SPEC_CLAIM.equals(caseData.getCaseAccessCategory())
                && Objects.nonNull(caseData.getSpecDefenceAdmittedRequired())
                && caseData.getSpecDefenceAdmittedRequired().equals(YES)) {
                builder.statesPaid(buildDefenceFiledEvent(
                    builder,
                    respondent1ResponseDate,
                    RESPONDENT_ID,
                    true
                ));
            } else {
                builder.receiptOfPartAdmission(
                    Event.builder()
                        .eventSequence(prepareEventSequence(builder.build()))
                        .eventCode(RECEIPT_OF_PART_ADMISSION.getCode())
                        .dateReceived(caseData.getRespondent1ResponseDate())
                        .litigiousPartyID(RESPONDENT_ID)
                        .build()
                );
            }

            buildRespondentResponseText(builder, caseData, miscText, respondent1ResponseDate);

            directionsQuestionnaireFiledEvents.add(
                buildDirectionsQuestionnaireFiledEvent(builder, caseData,
                                                       respondent1ResponseDate,
                                                       RESPONDENT_ID,
                                                       respondent1DQ,
                                                       respondent1,
                                                       true
                ));
            if (defendant1v2SameSolicitorSameResponse.test(caseData)) {
                final Party respondent2 = caseData.getRespondent2();
                final Respondent1DQ respondent2DQ = caseData.getRespondent1DQ();
                LocalDateTime respondent2ResponseDate = null != caseData.getRespondent2ResponseDate()
                    ? caseData.getRespondent2ResponseDate() : caseData.getRespondent1ResponseDate();
                miscText = prepareRespondentResponseText(caseData, caseData.getRespondent2(), false);
                builder.receiptOfPartAdmission(
                    Event.builder()
                        .eventSequence(prepareEventSequence(builder.build()))
                        .eventCode(RECEIPT_OF_PART_ADMISSION.getCode())
                        .dateReceived(respondent2ResponseDate)
                        .litigiousPartyID(RESPONDENT2_ID)
                        .build()
                );
                buildRespondentResponseText(builder, caseData, miscText, respondent2ResponseDate);
                directionsQuestionnaireFiledEvents.add(
                    buildDirectionsQuestionnaireFiledEvent(builder, caseData,
                                                           respondent2ResponseDate,
                                                           RESPONDENT2_ID,
                                                           respondent2DQ,
                                                           respondent2,
                                                           true
                    ));
            }
        }
        if (defendant2ResponseExists.test(caseData)) {
            miscText = prepareRespondentResponseText(caseData, caseData.getRespondent2(), false);
            Party respondent2 = caseData.getRespondent2();
            Respondent2DQ respondent2DQ = caseData.getRespondent2DQ();
            LocalDateTime respondent2ResponseDate = caseData.getRespondent2ResponseDate();
            builder.receiptOfPartAdmission(
                Event.builder()
                    .eventSequence(prepareEventSequence(builder.build()))
                    .eventCode(RECEIPT_OF_PART_ADMISSION.getCode())
                    .dateReceived(caseData.getRespondent2ResponseDate())
                    .litigiousPartyID(RESPONDENT2_ID)
                    .build()
            );

            buildRespondentResponseText(builder, caseData, miscText, respondent2ResponseDate);

            directionsQuestionnaireFiledEvents.add(
                buildDirectionsQuestionnaireFiledEvent(builder, caseData,
                                                       respondent2ResponseDate,
                                                       RESPONDENT2_ID,
                                                       respondent2DQ,
                                                       respondent2,
                                                       false
                ));
        }
        builder.clearDirectionsQuestionnaireFiled().directionsQuestionnaireFiled(directionsQuestionnaireFiledEvents);
    }

    private void buildRespondentCounterClaim(EventHistory.EventHistoryBuilder builder, CaseData caseData) {
        String miscText;
        if (defendant1ResponseExists.test(caseData)) {
            miscText = prepareRespondentResponseText(caseData, caseData.getRespondent1(), true);
            builder.miscellaneous(Event.builder()
                                .eventSequence(prepareEventSequence(builder.build()))
                                .eventCode(MISCELLANEOUS.getCode())
                                .dateReceived(caseData.getRespondent1ResponseDate())
                                .eventDetailsText(miscText)
                                .eventDetails(EventDetails.builder()
                                                  .miscText(miscText)
                                                  .build())
                                .build());
            if (defendant1v2SameSolicitorSameResponse.test(caseData)) {
                LocalDateTime respondent2ResponseDate = null != caseData.getRespondent2ResponseDate()
                    ? caseData.getRespondent2ResponseDate() : caseData.getRespondent1ResponseDate();
                miscText = prepareRespondentResponseText(caseData, caseData.getRespondent2(), false);
                builder.miscellaneous(Event.builder()
                                    .eventSequence(prepareEventSequence(builder.build()))
                                    .eventCode(MISCELLANEOUS.getCode())
                                    .dateReceived(respondent2ResponseDate)
                                    .eventDetailsText(miscText)
                                    .eventDetails(EventDetails.builder()
                                                      .miscText(miscText)
                                                      .build())
                                    .build());
            }
        }
        if (defendant2ResponseExists.test(caseData)) {
            miscText = prepareRespondentResponseText(caseData, caseData.getRespondent2(), false);
            builder.miscellaneous(Event.builder()
                                .eventSequence(prepareEventSequence(builder.build()))
                                .eventCode(MISCELLANEOUS.getCode())
                                .dateReceived(caseData.getRespondent2ResponseDate())
                                .eventDetailsText(miscText)
                                .eventDetails(EventDetails.builder()
                                                  .miscText(miscText)
                                                  .build())
                                .build());
        }
    }

    private void buildConsentExtensionFilingDefence(EventHistory.EventHistoryBuilder builder, CaseData caseData) {
        List<Event> events = new ArrayList<>();
        MultiPartyScenario scenario = getMultiPartyScenario(caseData);

        if (defendant1ExtensionExists.test(caseData)) {
            events.add(buildConsentExtensionFilingDefenceEvent(
                PartyUtils.respondent1Data(caseData), scenario, prepareEventSequence(builder.build())
            ));
        }

        if (defendant2ExtensionExists.test(caseData)) {
            events.add(buildConsentExtensionFilingDefenceEvent(
                PartyUtils.respondent2Data(caseData), scenario, prepareEventSequence(builder.build())
            ));
        }

        builder.consentExtensionFilingDefence(events);
    }

    private void buildInformAgreedExtensionDateForSpec(EventHistory.EventHistoryBuilder builder, CaseData caseData) {
        if (SPEC_CLAIM.equals(caseData.getCaseAccessCategory())
            && (caseData.getRespondentSolicitor1AgreedDeadlineExtension() != null
            || caseData.getRespondentSolicitor2AgreedDeadlineExtension() != null)) {
            buildConsentExtensionFilingDefence(builder, caseData);
        }
    }

    private Event buildConsentExtensionFilingDefenceEvent(
        PartyData party, MultiPartyScenario scenario, int eventNumber) {
        return Event.builder()
            .eventSequence(eventNumber)
            .eventCode(CONSENT_EXTENSION_FILING_DEFENCE.getCode())
            .dateReceived(party.getTimeExtensionDate())
            .litigiousPartyID(party.getRole().equals(RESPONDENT_ONE) ? RESPONDENT_ID : RESPONDENT2_ID)
            .eventDetailsText(getExtensionEventText(scenario, party))
            .eventDetails(EventDetails.builder()
                              .agreedExtensionDate(party.getSolicitorAgreedDeadlineExtension().format(ISO_DATE))
                              .build())
            .build();
    }

    private String getExtensionEventText(MultiPartyScenario scenario, PartyData party) {
        String extensionDate = party.getSolicitorAgreedDeadlineExtension()
            .format(DateTimeFormatter.ofPattern("dd MM yyyy"));
        switch (scenario) {
            case ONE_V_TWO_ONE_LEGAL_REP:
                return format("Defendant(s) have agreed extension: %s", extensionDate);
            case ONE_V_TWO_TWO_LEGAL_REP:
                return format("Defendant: %s has agreed extension: %s", party.getDetails().getPartyName(),
                              extensionDate
                );
            default:
                return format("agreed extension date: %s", extensionDate);
        }
    }

    private void buildSDONotDrawn(EventHistory.EventHistoryBuilder builder,
                                  CaseData caseData) {
        String miscText = left(textFormatter.caseProceedOffline(
            "Judge / Legal Advisor did not draw a Direction's Order: " + caseData.getReasonNotSuitableSDO().getInput()
        ), 250);

        LocalDateTime eventDate = caseData.getUnsuitableSDODate();
        builder.miscellaneous(
            Event.builder()
                .eventSequence(prepareEventSequence(builder.build()))
                .eventCode(MISCELLANEOUS.getCode())
                .dateReceived(eventDate)
                .eventDetailsText(miscText)
                .eventDetails(EventDetails.builder()
                                  .miscText(miscText)
                                  .build())
                .build());
    }

    private void buildMiscellaneousIJEvent(EventHistory.EventHistoryBuilder builder, CaseData caseData) {
        Boolean grantedFlag = caseData.getRespondent2() != null
            && caseData.getDefendantDetails() != null
            && !caseData.getDefendantDetails().getValue()
            .getLabel().startsWith("Both");
        String miscTextRequested = textFormatter.withRpaPrefix("Summary judgment requested and referred to judge.");
        String miscTextGranted = textFormatter.withRpaPrefix("Summary judgment granted and referred to judge.");
        if (caseData.getDefendantDetails() != null) {
            builder.miscellaneous(
                Event.builder()
                    .eventSequence(prepareEventSequence(builder.build()))
                    .eventCode(MISCELLANEOUS.getCode())
                    .dateReceived(timelineHelper.now())
                    .eventDetailsText(grantedFlag ? miscTextRequested : miscTextGranted)
                    .eventDetails(EventDetails.builder()
                                      .miscText(grantedFlag ? miscTextRequested : miscTextGranted)
                                      .build())
                    .build());
        }

    }

    private String getInstallmentPeriodForRequestJudgmentByAdmission(boolean payByInstallment, CaseData caseData) {
        if (payByInstallment) {
            if (hasCourtDecisionInFavourOfClaimant(caseData)) {
                return mapToRepaymentPlanFrequency(Optional.ofNullable(caseData.getApplicant1SuggestInstalmentsRepaymentFrequencyForDefendantSpec()).map(Enum::name).orElse(""));
            } else {
                return mapToRepaymentPlanFrequency(ofNullable(caseData.getRespondent1RepaymentPlan()).map(RepaymentPlanLRspec::getRepaymentFrequency).map(
                    Enum::name).orElse(""));
            }
        } else {
            return null;
        }
    }

    private String mapToRepaymentPlanFrequency(String frequency) {
        return switch (frequency) {
            case "ONCE_ONE_WEEK" -> "WK";
            case "ONCE_TWO_WEEKS" -> "FOR";
            case "ONCE_ONE_MONTH" -> "MTH";
            default -> null;
        };
    }

    private String getJBAInstallmentPeriod(CaseData caseData) {
        boolean joLiveFeedActive = featureToggleService.isJOLiveFeedActive();
        boolean payByInstallment = hasCourtDecisionInFavourOfClaimant(caseData) ? caseData.applicant1SuggestedPayByInstalments() : caseData.isPayByInstallment();
        if (payByInstallment) {
            return getInstallmentPeriodForRequestJudgmentByAdmission(payByInstallment, caseData);
        }
        boolean payBySetDate = hasCourtDecisionInFavourOfClaimant(caseData) ? caseData.applicant1SuggestedPayBySetDate() : caseData.isPayBySetDate();
        boolean payImmediately = hasCourtDecisionInFavourOfClaimant(caseData) ? caseData.applicant1SuggestedPayImmediately() : caseData.isPayImmediately();
        if (joLiveFeedActive && payBySetDate) {
            return "FUL";
        } else if (joLiveFeedActive && payImmediately) {
            return "FW";
        } else {
            return null;
        }
    }

    private void buildSpecAdmitRejectRepayment(EventHistory.EventHistoryBuilder builder,
                                               CaseData caseData) {

        if (caseData.hasApplicantRejectedRepaymentPlan()) {
            builder.miscellaneous(
                Event.builder()
                    .eventSequence(prepareEventSequence(builder.build()))
                    .eventCode(MISCELLANEOUS.getCode())
                    .dateReceived(setApplicant1ResponseDate(caseData))
                    .eventDetailsText(textFormatter.manualDeterminationRequired())
                    .eventDetails(EventDetails.builder()
                                      .miscText(textFormatter.manualDeterminationRequired())
                                      .build())
                    .build());
        }
    }

    private void buildLrVLipFullDefenceEvent(EventHistory.EventHistoryBuilder builder, CaseData caseData,
                                             List<Event> defenceFiledEvents, List<Event> statesPaidEvents) {
        LocalDateTime respondent1ResponseDate = caseData.getRespondent1ResponseDate();

        if (caseData.hasDefendantPaidTheAmountClaimed()) {
            statesPaidEvents.add(buildDefenceFiledEvent(
                builder,
                respondent1ResponseDate,
                RESPONDENT_ID,
                true
            ));
        } else {
            defenceFiledEvents.add(
                buildDefenceFiledEvent(
                    builder,
                    respondent1ResponseDate,
                    RESPONDENT_ID,
                    false
                ));
        }
    }

    private void buildTakenOfflineDueToDefendantNoc(EventHistory.EventHistoryBuilder builder, CaseData caseData) {
        builder.miscellaneous(
            Event.builder()
                .eventSequence(prepareEventSequence(builder.build()))
                .eventCode(MISCELLANEOUS.getCode())
                .dateReceived(caseData.getTakenOfflineDate())
                .eventDetailsText(textFormatter.noticeOfChangeFiled())
                .eventDetails(EventDetails.builder()
                                  .miscText(textFormatter.noticeOfChangeFiled())
                                  .build())
                .build());
    }

    private LocalDateTime setApplicant1ResponseDate(CaseData caseData) {
        LocalDateTime applicant1ResponseDate = caseData.getApplicant1ResponseDate();
        return timelineHelper.ensurePresentOrNow(applicant1ResponseDate);
    }

    private void buildMiscellaneousForRespondentResponseLipVSLr(EventHistory.EventHistoryBuilder builder,
                                                                CaseData caseData) {
        if (caseData.isLipvLROneVOne()) {
            builder.miscellaneous(
                Event.builder()
                    .eventSequence(prepareEventSequence(builder.build()))
                    .eventCode(MISCELLANEOUS.getCode())
                    .dateReceived(timelineHelper.now())
                .eventDetailsText(textFormatter.lipVsLrFullOrPartAdmissionReceived())
                    .eventDetails(EventDetails.builder()
                                      .miscText(textFormatter.lipVsLrFullOrPartAdmissionReceived())
                                      .build())
                    .build());
        }
    }

    private LocalDate getCoscDate(CaseData caseData) {
        if (caseData.getJoFullyPaymentMadeDate() != null) {
            return caseData.getJoFullyPaymentMadeDate();
        } else if (caseData.getCertOfSC() != null && caseData.getCertOfSC().getDefendantFinalPaymentDate() != null) {
            return caseData.getCertOfSC().getDefendantFinalPaymentDate();
        }
        throw new IllegalArgumentException("Payment date cannot be null");
    }

    private LocalDateTime getJbADate(CaseData caseData) {
        return featureToggleService.isJOLiveFeedActive()
            ? caseData.getJoJudgementByAdmissionIssueDate()
            : setApplicant1ResponseDate(caseData);
    }
}
