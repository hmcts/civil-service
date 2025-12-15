package uk.gov.hmcts.reform.civil.service.robotics.mapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.DJPaymentTypeSelection;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.enums.ReasonForProceedingOnPaper;
import uk.gov.hmcts.reform.civil.enums.RepaymentFrequencyDJ;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseType;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.helpers.judgmentsonline.JudgmentsOnlineHelper;
import uk.gov.hmcts.reform.civil.model.CCJPaymentDetails;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.ClaimProceedsInCaseman;
import uk.gov.hmcts.reform.civil.model.ClaimProceedsInCasemanLR;
import uk.gov.hmcts.reform.civil.model.ClaimantResponseDetails;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.PartyData;
import uk.gov.hmcts.reform.civil.model.PaymentBySetDate;
import uk.gov.hmcts.reform.civil.model.RepaymentPlanLRspec;
import uk.gov.hmcts.reform.civil.model.RespondToClaim;
import uk.gov.hmcts.reform.civil.model.RespondToClaimAdmitPartLRspec;
import uk.gov.hmcts.reform.civil.model.breathing.BreathingSpaceType;
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
import uk.gov.hmcts.reform.civil.model.robotics.EventType;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;
import uk.gov.hmcts.reform.civil.service.flowstate.IStateFlowEngine;
import uk.gov.hmcts.reform.civil.stateflow.model.State;
import uk.gov.hmcts.reform.civil.utils.LocationRefDataUtil;
import uk.gov.hmcts.reform.civil.utils.MonetaryConversions;
import uk.gov.hmcts.reform.civil.utils.PartyUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
import static uk.gov.hmcts.reform.civil.model.robotics.EventType.BREATHING_SPACE_ENTERED;
import static uk.gov.hmcts.reform.civil.model.robotics.EventType.BREATHING_SPACE_LIFTED;
import static uk.gov.hmcts.reform.civil.model.robotics.EventType.CERTIFICATE_OF_SATISFACTION_OR_CANCELLATION;
import static uk.gov.hmcts.reform.civil.model.robotics.EventType.CONSENT_EXTENSION_FILING_DEFENCE;
import static uk.gov.hmcts.reform.civil.model.robotics.EventType.DEFAULT_JUDGMENT_GRANTED;
import static uk.gov.hmcts.reform.civil.model.robotics.EventType.DEFENCE_FILED;
import static uk.gov.hmcts.reform.civil.model.robotics.EventType.DEFENCE_STRUCK_OUT;
import static uk.gov.hmcts.reform.civil.model.robotics.EventType.DIRECTIONS_QUESTIONNAIRE_FILED;
import static uk.gov.hmcts.reform.civil.model.robotics.EventType.GENERAL_FORM_OF_APPLICATION;
import static uk.gov.hmcts.reform.civil.model.robotics.EventType.INTERLOCUTORY_JUDGMENT_GRANTED;
import static uk.gov.hmcts.reform.civil.model.robotics.EventType.JUDGEMENT_BY_ADMISSION;
import static uk.gov.hmcts.reform.civil.model.robotics.EventType.MENTAL_HEALTH_BREATHING_SPACE_ENTERED;
import static uk.gov.hmcts.reform.civil.model.robotics.EventType.MENTAL_HEALTH_BREATHING_SPACE_LIFTED;
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
import static uk.gov.hmcts.reform.civil.utils.PredicateUtils.defendant1AckExists;
import static uk.gov.hmcts.reform.civil.utils.PredicateUtils.defendant1ExtensionExists;
import static uk.gov.hmcts.reform.civil.utils.PredicateUtils.defendant1ResponseExists;
import static uk.gov.hmcts.reform.civil.utils.PredicateUtils.defendant1v2SameSolicitorSameResponse;
import static uk.gov.hmcts.reform.civil.utils.PredicateUtils.defendant2AckExists;
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
    private final Time time;
    public static final String BS_REF = "Breathing space reference";
    public static final String BS_START_DT = "actual start date";
    public static final String BS_END_DATE = "actual end date";
    public static final String RPA_REASON_MANUAL_DETERMINATION = "RPA Reason: Manual Determination Required.";
    public static final String RPA_REASON_JUDGMENT_BY_ADMISSION = "RPA Reason: Judgment by Admission requested and claim moved offline.";
    public static final String RECORD_JUDGMENT = "Judgment recorded.";
    public static final String RPA_RECORD_JUDGMENT_REASON = "RPA Reason: Judgment recorded.";
    public static final String RPA_IN_MEDIATION = "IN MEDIATION";
    public static final String QUERIES_ON_CASE = "There has been a query on this case";
    static final String ENTER = "Enter";
    static final String LIFTED = "Lifted";
    static final Set<CaseState> OFFLINE_STATES = EnumSet.of(CASE_DISMISSED, PROCEEDS_IN_HERITAGE_SYSTEM, CASE_DISCONTINUED);

    private void addMiscellaneousEvent(EventHistory eventHistory, Event event) {
        List<Event> miscList = eventHistory.getMiscellaneous() != null 
            ? new ArrayList<>(eventHistory.getMiscellaneous()) 
            : new ArrayList<>();
        miscList.add(event);
        eventHistory.setMiscellaneous(miscList);
    }

    private void addJudgmentByAdmissionEvent(EventHistory eventHistory, Event event) {
        List<Event> jbaList = eventHistory.getJudgmentByAdmission() != null 
            ? new ArrayList<>(eventHistory.getJudgmentByAdmission()) 
            : new ArrayList<>();
        jbaList.add(event);
        eventHistory.setJudgmentByAdmission(jbaList);
    }

    public EventHistory buildEvents(CaseData caseData) {
        return buildEvents(caseData, null);
    }

    public EventHistory buildEvents(CaseData caseData, String authToken) {
        EventHistory eventHistory = new EventHistory()
            .setDirectionsQuestionnaireFiled(List.of(Event.builder().build()));

        stateFlowEngine.evaluate(caseData).getStateHistory()
            .forEach(state -> {
                FlowState.Main flowState = (FlowState.Main) FlowState.fromFullName(state.getName());
                switch (flowState) {
                    case TAKEN_OFFLINE_UNREPRESENTED_DEFENDANT:
                        buildUnrepresentedDefendant(eventHistory, caseData);
                        break;
                    case TAKEN_OFFLINE_UNREPRESENTED_UNREGISTERED_DEFENDANT:
                        buildUnregisteredAndUnrepresentedDefendant(eventHistory, caseData);
                        break;
                    case TAKEN_OFFLINE_UNREGISTERED_DEFENDANT:
                        buildUnregisteredDefendant(eventHistory, caseData);
                        break;
                    case CLAIM_ISSUED:
                        buildClaimIssued(eventHistory, caseData);
                        break;
                    case CLAIM_NOTIFIED:
                        buildClaimantHasNotifiedDefendant(eventHistory, caseData);
                        break;
                    case TAKEN_OFFLINE_AFTER_CLAIM_NOTIFIED:
                        buildTakenOfflineAfterClaimNotified(eventHistory, caseData);
                        break;
                    case CLAIM_DETAILS_NOTIFIED:
                        buildClaimDetailsNotified(eventHistory, caseData);
                        break;
                    case NOTIFICATION_ACKNOWLEDGED:
                        buildAcknowledgementOfServiceReceived(eventHistory, caseData);
                        break;
                    case NOTIFICATION_ACKNOWLEDGED_TIME_EXTENSION, CLAIM_DETAILS_NOTIFIED_TIME_EXTENSION:
                        buildConsentExtensionFilingDefence(eventHistory, caseData);
                        break;
                    case FULL_DEFENCE:
                        buildRespondentFullDefence(eventHistory, caseData);
                        break;
                    case FULL_ADMISSION:
                        buildRespondentFullAdmission(eventHistory, caseData);
                        break;
                    case PART_ADMISSION:
                        buildRespondentPartAdmission(eventHistory, caseData);
                        break;
                    case COUNTER_CLAIM:
                        buildRespondentCounterClaim(eventHistory, caseData);
                        break;
                    // AWAITING_RESPONSES states would only happen in 1v2 diff sol after 1 defendant responses.
                    // These states will not show in the history mapper after the second defendant response.
                    // It can share the same RPA builder as DIVERGENT_RESPOND state because it builds events according
                    // to defendant response
                    // DIVERGENT_RESPOND states would only happen in 1v2 diff sol after both defendant responds.
                    case AWAITING_RESPONSES_FULL_DEFENCE_RECEIVED, AWAITING_RESPONSES_FULL_ADMIT_RECEIVED, AWAITING_RESPONSES_NOT_FULL_DEFENCE_OR_FULL_ADMIT_RECEIVED:
                        buildRespondentDivergentResponse(eventHistory, caseData, false);
                        break;
                    case DIVERGENT_RESPOND_GENERATE_DQ_GO_OFFLINE, DIVERGENT_RESPOND_GO_OFFLINE:
                        buildRespondentDivergentResponse(eventHistory, caseData, true);
                        break;
                    case FULL_DEFENCE_NOT_PROCEED:
                        buildFullDefenceNotProceed(eventHistory, caseData);
                        break;
                    case FULL_DEFENCE_PROCEED:
                        buildFullDefenceProceed(eventHistory, caseData, authToken);
                        break;
                    case TAKEN_OFFLINE_BY_STAFF:
                        buildTakenOfflineByStaff(eventHistory, caseData);
                        buildGeneralFormApplicationEventsStrikeOutOrder(eventHistory, caseData);
                        break;
                    case CLAIM_DISMISSED_PAST_CLAIM_DISMISSED_DEADLINE:
                        buildClaimDismissedPastDeadline(eventHistory, caseData,
                                                        stateFlowEngine.evaluate(caseData).getStateHistory()
                        );
                        break;
                    case CLAIM_DISMISSED_PAST_CLAIM_NOTIFICATION_DEADLINE:
                        buildClaimDismissedPastNotificationsDeadline(
                            eventHistory,
                            caseData,
                            "RPA Reason: Claim dismissed. Claimant hasn't taken action since the "
                                + "claim was issued."
                        );
                        break;
                    case CLAIM_DISMISSED_PAST_CLAIM_DETAILS_NOTIFICATION_DEADLINE:
                        buildClaimDismissedPastNotificationsDeadline(
                            eventHistory,
                            caseData,
                            "RPA Reason: Claim dismissed. Claimant hasn't notified defendant of the "
                                + "claim details within the allowed 2 weeks."
                        );
                        break;
                    case TAKEN_OFFLINE_AFTER_CLAIM_DETAILS_NOTIFIED:
                        buildOfflineAfterClaimsDetailsNotified(
                            eventHistory,
                            caseData
                        );
                        break;
                    case TAKEN_OFFLINE_PAST_APPLICANT_RESPONSE_DEADLINE:
                        buildClaimTakenOfflinePastApplicantResponse(eventHistory, caseData);
                        break;
                    case TAKEN_OFFLINE_SDO_NOT_DRAWN:
                        buildSDONotDrawn(eventHistory, caseData);
                        break;
                    case TAKEN_OFFLINE_AFTER_SDO:
                        buildClaimTakenOfflineAfterSDO(eventHistory, caseData);
                        break;
                    case PART_ADMIT_REJECT_REPAYMENT, FULL_ADMIT_REJECT_REPAYMENT:
                        buildSpecAdmitRejectRepayment(eventHistory, caseData);
                        break;
                    case IN_MEDIATION:
                        buildClaimInMediation(eventHistory, caseData);
                        break;
                    case TAKEN_OFFLINE_SPEC_DEFENDANT_NOC, TAKEN_OFFLINE_SPEC_DEFENDANT_NOC_AFTER_JBA:
                        buildTakenOfflineDueToDefendantNoc(eventHistory, caseData);
                        break;
                    default:
                        break;
                }
            });

        buildRespondent1LitigationFriendEvent(eventHistory, caseData);
        buildRespondent2LitigationFriendEvent(eventHistory, caseData);
        buildCaseNotesEvents(eventHistory, caseData);
        if (null != caseData.getBreathing()) {
            if (null != caseData.getBreathing().getEnter() && null == caseData.getBreathing().getLift()) {
                if (BreathingSpaceType.STANDARD.equals(caseData.getBreathing().getEnter().getType())) {
                    buildBreathingSpaceEvent(eventHistory, caseData, BREATHING_SPACE_ENTERED, ENTER);
                } else if (BreathingSpaceType.MENTAL_HEALTH.equals(caseData.getBreathing().getEnter().getType())) {
                    buildBreathingSpaceEvent(eventHistory, caseData,
                                             MENTAL_HEALTH_BREATHING_SPACE_ENTERED, ENTER
                    );
                }
            } else if (null != caseData.getBreathing().getLift()) {
                if (BreathingSpaceType.STANDARD.equals(caseData.getBreathing().getEnter().getType())) {
                    buildBreathingSpaceEvent(eventHistory, caseData, BREATHING_SPACE_ENTERED, ENTER);
                    buildBreathingSpaceEvent(eventHistory, caseData, BREATHING_SPACE_LIFTED, LIFTED);
                } else if (BreathingSpaceType.MENTAL_HEALTH.equals(caseData.getBreathing().getEnter().getType())) {
                    buildBreathingSpaceEvent(eventHistory, caseData,
                                             MENTAL_HEALTH_BREATHING_SPACE_ENTERED, ENTER
                    );
                    buildBreathingSpaceEvent(eventHistory, caseData, MENTAL_HEALTH_BREATHING_SPACE_LIFTED, LIFTED);
                }
            }
        }

        buildInterlocutoryJudgment(eventHistory, caseData);
        buildMiscellaneousIJEvent(eventHistory, caseData);
        buildDefaultJudgment(eventHistory, caseData);
        buildMiscellaneousDJEvent(eventHistory, caseData);
        buildInformAgreedExtensionDateForSpec(eventHistory, caseData);
        buildClaimTakenOfflineAfterDJ(eventHistory, caseData);
        buildCcjEvent(eventHistory, caseData);
        buildSetAsideJudgment(eventHistory, caseData);
        buildCoscEvent(eventHistory, caseData);
        buildTakenOfflineAfterDefendantNoCDeadlinePassed(eventHistory, caseData);
        buildQueriesEvent(eventHistory, caseData);
        EventHistory sortedEventHistory = eventHistorySequencer.sortEvents(eventHistory);
        log.info("Event history: {}", sortedEventHistory);
        return sortedEventHistory;
    }

    private void buildTakenOfflineAfterDefendantNoCDeadlinePassed(EventHistory eventHistory,
                                                                  CaseData caseData) {
        boolean nocDeadlinePassed = false;
        LocalDateTime takenOfflineDate = caseData.getTakenOfflineDate();
        if (takenOfflineDate != null) {
            if (caseData.getAddLegalRepDeadlineRes1() != null
                && takenOfflineDate.isAfter(caseData.getAddLegalRepDeadlineRes1())
                && YesOrNo.NO.equals(caseData.getRespondent1Represented())) {
                nocDeadlinePassed = true;
            }
            if (caseData.getAddLegalRepDeadlineRes2() != null
                && takenOfflineDate.isAfter(caseData.getAddLegalRepDeadlineRes2())
                && YesOrNo.NO.equals(caseData.getRespondent2Represented())) {
                nocDeadlinePassed = true;
            }
            if (nocDeadlinePassed) {
                String miscText = "RPA Reason: Claim moved offline after defendant NoC deadline has passed";
                List<Event> currentMisc = eventHistory.getMiscellaneous() != null 
                    ? new ArrayList<>(eventHistory.getMiscellaneous()) 
                    : new ArrayList<>();
                currentMisc.add(
                    Event.builder()
                        .eventSequence(prepareEventSequence(eventHistory))
                        .eventCode(MISCELLANEOUS.getCode())
                        .dateReceived(takenOfflineDate)
                        .eventDetailsText(miscText)
                        .eventDetails(EventDetails.builder()
                                          .miscText(miscText)
                                          .build())
                        .build());
                eventHistory.setMiscellaneous(currentMisc);
            }
        }
    }

    private void buildInterlocutoryJudgment(EventHistory eventHistory, CaseData caseData) {
        List<Event> events = new ArrayList<>();
        boolean grantedFlag = caseData.getRespondent2() != null
            && caseData.getDefendantDetails() != null
            && !caseData.getDefendantDetails().getValue()
            .getLabel().startsWith("Both");
        if (!grantedFlag && null != caseData.getHearingSupportRequirementsDJ()) {
            events.add(prepareInterlocutoryJudgment(eventHistory, RESPONDENT_ID));

            if (null != caseData.getRespondent2()) {
                events.add(prepareInterlocutoryJudgment(eventHistory, RESPONDENT2_ID));
            }
            eventHistory.setInterlocutoryJudgment(events);
        }
    }

    private Event prepareInterlocutoryJudgment(EventHistory eventHistory,
                                               String litigiousPartyID) {
        return (Event.builder()
            .eventSequence(prepareEventSequence(eventHistory))
            .eventCode(INTERLOCUTORY_JUDGMENT_GRANTED.getCode())
            .dateReceived(LocalDateTime.now())
            .litigiousPartyID(litigiousPartyID)
            .eventDetailsText("")
            .eventDetails(EventDetails.builder().miscText("")
                              .build())
            .build());
    }

    private void buildDefaultJudgment(EventHistory eventHistory, CaseData caseData) {
        List<Event> events = new ArrayList<>();
        boolean grantedFlag = caseData.getRespondent2() != null
            && caseData.getDefendantDetailsSpec() != null
            && !caseData.getDefendantDetailsSpec().getValue()
            .getLabel().startsWith("Both");

        if (!grantedFlag && null != caseData.getDefendantDetailsSpec()) {
            events.add(prepareDefaultJudgment(eventHistory, caseData, RESPONDENT_ID));

            if (null != caseData.getRespondent2()) {
                events.add(prepareDefaultJudgment(eventHistory, caseData, RESPONDENT2_ID));
            }
            eventHistory.setDefaultJudgment(events);
        }
    }

    private boolean hasCourtDecisionInFavourOfClaimant(CaseData caseData) {
        ClaimantLiPResponse applicant1Response = Optional.ofNullable(caseData.getCaseDataLiP())
            .map(CaseDataLiP::getApplicant1LiPResponse).orElse(null);
        return applicant1Response != null && applicant1Response.hasCourtDecisionInFavourOfClaimant();
    }

    private Event prepareDefaultJudgment(EventHistory eventHistory, CaseData caseData,
                                         String litigiousPartyID) {

        // Monetary amounts
        BigDecimal totalInterest = caseData.getTotalInterest() != null ? caseData.getTotalInterest() : ZERO;
        BigDecimal amountClaimedWithInterest = caseData.getTotalClaimAmount().add(totalInterest);

        // Partial payment handling
        BigDecimal partialPaymentAmountInPennies = isNotEmpty(caseData.getPartialPaymentAmount())
            ? new BigDecimal(caseData.getPartialPaymentAmount())
            : null;
        BigDecimal partialPaymentAmountInPounds = isNotEmpty(partialPaymentAmountInPennies)
            ? MonetaryConversions.penniesToPounds(partialPaymentAmountInPennies)
            : null;

        // Common flags and dates
        boolean isImmediate = DJPaymentTypeSelection.IMMEDIATELY.equals(caseData.getPaymentTypeSelection());
        boolean isRepaymentPlan = DJPaymentTypeSelection.REPAYMENT_PLAN.equals(caseData.getPaymentTypeSelection());
        LocalDateTime dateOfDjCreated = getDateOfDjCreated(caseData);
        LocalDateTime paymentInFullDate = computePaymentInFullDate(caseData);

        // Costs and installments
        BigDecimal amountOfCosts = (caseData.isApplicantLipOneVOne() && featureToggleService.isLipVLipEnabled())
            ? ClaimFeeUtility.getCourtFee(caseData)
            : JudgmentsOnlineHelper.getFixedCostsOfJudgmentForDJ(caseData).add(
            JudgmentsOnlineHelper.getClaimFeeOfJudgmentForDJ(caseData));

        BigDecimal installmentAmount = isRepaymentPlan
            ? getInstallmentAmount(caseData.getRepaymentSuggestion()).setScale(2)
            : ZERO;

        BigDecimal amountPaidBeforeJudgment = (caseData.getPartialPayment() == YesOrNo.YES) ? partialPaymentAmountInPounds : ZERO;

        boolean isJointJudgment = caseData.getRespondent2() != null;

        return Event.builder()
            .eventSequence(prepareEventSequence(eventHistoryBuilder.build()))
            .eventCode(DEFAULT_JUDGMENT_GRANTED.getCode())
            .dateReceived(dateOfDjCreated)
            .litigiousPartyID(litigiousPartyID)
            .eventDetailsText("")
            .eventDetails(EventDetails.builder()
                              .miscText("")
                              .amountOfJudgment(amountClaimedWithInterest.setScale(2))
                              .amountOfCosts(amountOfCosts)
                              .amountPaidBeforeJudgment(amountPaidBeforeJudgment)
                              .isJudgmentForthwith(isImmediate)
                              .paymentInFullDate(paymentInFullDate)
                              .installmentAmount(installmentAmount)
                              .installmentPeriod(getInstallmentPeriod(caseData))
                              .firstInstallmentDate(caseData.getRepaymentDate())
                              .dateOfJudgment(dateOfDjCreated)
                              .jointJudgment(isJointJudgment)
                              .judgmentToBeRegistered(false)
                              .build())
            .build();
    }

    private LocalDateTime computePaymentInFullDate(CaseData caseData) {
        DJPaymentTypeSelection paymentTypeSelection = caseData.getPaymentTypeSelection();
        boolean claimantFavouredImmediate = hasCourtDecisionInFavourOfClaimant(caseData)
            && caseData.applicant1SuggestedPayImmediately();

        if (paymentTypeSelection == DJPaymentTypeSelection.IMMEDIATELY) {
            return claimantFavouredImmediate
                ? Optional.ofNullable(caseData.getApplicant1SuggestPayImmediatelyPaymentDateForDefendantSpec())
                .map(LocalDate::atStartOfDay)
                .orElse(null)
                : LocalDateTime.now();
        }

        if (paymentTypeSelection == DJPaymentTypeSelection.SET_DATE) {
            return claimantFavouredImmediate
                ? Optional.ofNullable(caseData.getApplicant1RequestedPaymentDateForDefendantSpec())
                .map(PaymentBySetDate::getPaymentSetDate)
                .map(LocalDate::atStartOfDay)
                .orElse(null)
                : caseData.getPaymentSetDate().atStartOfDay();
        }

        return null;
    }

    private LocalDateTime getDateOfDjCreated(CaseData caseData) {
        return featureToggleService.isJOLiveFeedActive() && Objects.nonNull(caseData.getJoDJCreatedDate())
            ? caseData.getJoDJCreatedDate()
            : LocalDateTime.now();
    }

    private BigDecimal getInstallmentAmount(String amount) {
        var regularRepaymentAmountPennies = new BigDecimal(amount);
        return MonetaryConversions.penniesToPounds(regularRepaymentAmountPennies);
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

    private void buildBreathingSpaceEvent(EventHistory eventHistory, CaseData caseData,
                                          EventType eventType, String bsStatus) {
        String eventDetails = null;
        if (caseData.getBreathing().getEnter().getReference() != null) {
            eventDetails = BS_REF + " "
                + caseData.getBreathing().getEnter().getReference() + ", ";
        }

        if (bsStatus.equals(ENTER)) {
            if (caseData.getBreathing().getEnter().getStart() != null) {
                if (eventDetails == null) {
                    eventDetails = StringUtils.capitalize(BS_START_DT) + " "
                        + caseData.getBreathing().getEnter().getStart();
                } else {
                    eventDetails = eventDetails + BS_START_DT + " "
                        + caseData.getBreathing().getEnter().getStart();
                }
            } else {
                if (eventDetails == null) {
                    eventDetails = StringUtils.capitalize(BS_START_DT) + " "
                        + LocalDateTime.now();
                } else {
                    eventDetails = eventDetails + BS_START_DT + " "
                        + LocalDateTime.now();
                }
            }
        } else if (bsStatus.equals(LIFTED) && caseData.getBreathing().getLift().getExpectedEnd() != null) {
            if (eventDetails == null) {
                eventDetails = StringUtils.capitalize(BS_END_DATE) + " "
                    + caseData.getBreathing().getLift().getExpectedEnd();
            } else {
                eventDetails = eventDetails + BS_END_DATE + " "
                    + caseData.getBreathing().getLift().getExpectedEnd();
            }
        }

        switch (eventType) {
            case BREATHING_SPACE_ENTERED:
                Event breathingSpaceEnteredEvent = Event.builder()
                    .eventSequence(prepareEventSequence(eventHistory))
                    .eventCode(eventType.getCode())
                    .dateReceived(caseData.getBreathing().getEnter().getStart() != null
                                      ? caseData.getBreathing().getEnter().getStart().atTime(LocalTime.now())
                                      : LocalDateTime.now())
                    .litigiousPartyID("001")
                    .eventDetailsText(eventDetails)
                    .eventDetails(EventDetails.builder().miscText(eventDetails)
                                      .build())
                    .build();
                List<Event> breathingSpaceEnteredList = eventHistory.getBreathingSpaceEntered() != null 
                    ? new ArrayList<>(eventHistory.getBreathingSpaceEntered()) 
                    : new ArrayList<>();
                breathingSpaceEnteredList.add(breathingSpaceEnteredEvent);
                eventHistory.setBreathingSpaceEntered(breathingSpaceEnteredList);
                break;
            case BREATHING_SPACE_LIFTED:
                Event breathingSpaceLiftedEvent = Event.builder()
                    .eventSequence(prepareEventSequence(eventHistory))
                    .eventCode(eventType.getCode())
                    .dateReceived(caseData.getBreathing().getLift().getExpectedEnd() != null
                                      ? caseData.getBreathing().getLift().getExpectedEnd().atTime(LocalTime.now())
                                      : LocalDateTime.now())
                    .eventDetailsText(eventDetails)
                    .litigiousPartyID("001")
                    .eventDetails(EventDetails.builder().miscText(eventDetails)
                                      .build())
                    .build();
                List<Event> breathingSpaceLiftedList = eventHistory.getBreathingSpaceLifted() != null 
                    ? new ArrayList<>(eventHistory.getBreathingSpaceLifted()) 
                    : new ArrayList<>();
                breathingSpaceLiftedList.add(breathingSpaceLiftedEvent);
                eventHistory.setBreathingSpaceLifted(breathingSpaceLiftedList);
                break;
            case MENTAL_HEALTH_BREATHING_SPACE_ENTERED:
                Event breathingSpaceMentalHealthEnteredEvent = Event.builder()
                    .eventSequence(prepareEventSequence(eventHistory))
                    .eventCode(eventType.getCode())
                    .dateReceived(caseData.getBreathing().getEnter().getStart() != null
                                      ? caseData.getBreathing().getEnter().getStart().atTime(LocalTime.now())
                                      : LocalDateTime.now())
                    .eventDetailsText(eventDetails)
                    .litigiousPartyID("001")
                    .eventDetails(EventDetails.builder().miscText(eventDetails)
                                      .build())
                    .build();
                List<Event> breathingSpaceMentalHealthEnteredList = eventHistory.getBreathingSpaceMentalHealthEntered() != null 
                    ? new ArrayList<>(eventHistory.getBreathingSpaceMentalHealthEntered()) 
                    : new ArrayList<>();
                breathingSpaceMentalHealthEnteredList.add(breathingSpaceMentalHealthEnteredEvent);
                eventHistory.setBreathingSpaceMentalHealthEntered(breathingSpaceMentalHealthEnteredList);
                break;
            case MENTAL_HEALTH_BREATHING_SPACE_LIFTED:
                Event breathingSpaceMentalHealthLiftedEvent = Event.builder()
                    .eventSequence(prepareEventSequence(eventHistory))
                    .eventCode(eventType.getCode())
                    .dateReceived(caseData.getBreathing().getLift().getExpectedEnd() != null
                                      ? caseData.getBreathing().getLift().getExpectedEnd().atTime(LocalTime.now())
                                      : LocalDateTime.now())
                    .eventDetailsText(eventDetails)
                    .litigiousPartyID("001")
                    .eventDetails(EventDetails.builder().miscText(eventDetails)
                                      .build())
                    .build();
                List<Event> breathingSpaceMentalHealthLiftedList = eventHistory.getBreathingSpaceMentalHealthLifted() != null 
                    ? new ArrayList<>(eventHistory.getBreathingSpaceMentalHealthLifted()) 
                    : new ArrayList<>();
                breathingSpaceMentalHealthLiftedList.add(breathingSpaceMentalHealthLiftedEvent);
                eventHistory.setBreathingSpaceMentalHealthLifted(breathingSpaceMentalHealthLiftedList);
                break;
            default:
                break;
        }
    }

    private void buildCoscEvent(EventHistory eventHistory, CaseData caseData) {
        boolean joMarkedPaidInFullDateExists = caseData.getJoMarkedPaidInFullIssueDate() != null;

        if (featureToggleService.isJOLiveFeedActive()
            && ((joMarkedPaidInFullDateExists && caseData.getJoDefendantMarkedPaidInFullIssueDate() == null)
                || caseData.hasCoscCert())
        ) {
            // date received when mark paid in full by claimant is issued or when the scheduler runs at the cosc deadline at 4pm
            LocalDateTime dateReceived = joMarkedPaidInFullDateExists
                ? caseData.getJoMarkedPaidInFullIssueDate() : caseData.getJoDefendantMarkedPaidInFullIssueDate();

            Event coscEvent = Event.builder()
                .eventSequence(prepareEventSequence(eventHistory))
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
                .eventDetailsText("");
            List<Event> coscList = eventHistory.getCertificateOfSatisfactionOrCancellation() != null 
                ? new ArrayList<>(eventHistory.getCertificateOfSatisfactionOrCancellation()) 
                : new ArrayList<>();
            coscList.add(coscEvent);
            eventHistory.setCertificateOfSatisfactionOrCancellation(coscList);
        }
    }

    private void buildCcjEvent(EventHistory eventHistory, CaseData caseData) {
        if (caseData.isCcjRequestJudgmentByAdmission()) {
            buildJudgmentByAdmissionEventDetails(eventHistory, caseData);

            String miscTextRequested = RPA_REASON_JUDGMENT_BY_ADMISSION;
            String detailsTextRequested = RPA_REASON_JUDGMENT_BY_ADMISSION;
            if (featureToggleService.isJOLiveFeedActive()) {
                miscTextRequested = RECORD_JUDGMENT;
                detailsTextRequested = RPA_RECORD_JUDGMENT_REASON;
            }

            addMiscellaneousEvent(eventHistory, Event.builder()
                .eventSequence(prepareEventSequence(eventHistory))
                .eventCode(MISCELLANEOUS.getCode())
                .dateReceived(getJbADate(caseData))
                .eventDetailsText(detailsTextRequested)
                .eventDetails(EventDetails.builder()
                                  .miscText(miscTextRequested)
                                  .build())
                .build());
        }
    }

    private void buildSetAsideJudgment(EventHistory eventHistory, CaseData caseData) {
        if (featureToggleService.isJOLiveFeedActive() && caseData.getJoSetAsideReason() != null) {
            List<Event> events = new ArrayList<>();
            events.add(buildSetAsideJudgmentEvent(eventHistory, caseData, RESPONDENT_ID));
            if (null != caseData.getRespondent2()) {
                events.add(buildSetAsideJudgmentEvent(eventHistory, caseData, RESPONDENT2_ID));
            }
            eventHistory.setAsideJudgment(events);
        }
    }

    private Event buildSetAsideJudgmentEvent(EventHistory eventHistory, CaseData caseData, String litigiousPartyID) {
        return Event.builder()
            .eventSequence(prepareEventSequence(eventHistory))
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

    private void buildJudgmentByAdmissionEventDetails(EventHistory eventHistory, CaseData caseData) {

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

        addJudgmentByAdmissionEvent(eventHistory, Event.builder()
            .eventSequence(prepareEventSequence(eventHistory))
            .eventCode(JUDGEMENT_BY_ADMISSION.getCode())
            .litigiousPartyID(featureToggleService.isJOLiveFeedActive() ? RESPONDENT_ID : APPLICANT_ID)
            .dateReceived(getJbADate(caseData))
            .eventDetails(judgmentByAdmissionEvent)
            .eventDetailsText("")
            .build());
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
        return Optional.ofNullable(caseData.getTotalInterest()).orElse(ZERO);
    }

    private void buildRespondentDivergentResponse(EventHistory eventHistory, CaseData caseData,
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

                buildRespondentResponseEventForSpec(eventHistory, caseData, respondent1SpecResponseType,
                                                    respondent1ResponseDate, RESPONDENT_ID
                );
            } else {
                buildRespondentResponseEvent(eventHistory, caseData, caseData.getRespondent1ClaimResponseType(),
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
                addMiscellaneousEvent(eventHistory, Event.builder()
                    .eventSequence(prepareEventSequence(eventHistory))
                    .eventCode(MISCELLANEOUS.getCode())
                    .dateReceived(respondent1ResponseDate)
                    .eventDetailsText(miscText)
                    .eventDetails(EventDetails.builder()
                                      .miscText(miscText)
                                      .build())
                    .build());
            }
        }

        if (defendant2DivergentResponseExists.test(caseData)) {
            if (SPEC_CLAIM.equals(caseData.getCaseAccessCategory())) {
                buildRespondentResponseEventForSpec(builder, caseData,
                                                    caseData.getRespondent2ClaimResponseTypeForSpec(),
                                                    respondent2ResponseDate, RESPONDENT2_ID
                );
            } else {
                buildRespondentResponseEvent(eventHistory, caseData, caseData.getRespondent2ClaimResponseType(),
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
                eventHistory.miscellaneous((Event.builder()
                    .eventSequence(prepareEventSequence(eventHistory))
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

    private void buildRespondentResponseEvent(EventHistory eventHistory,
                                              CaseData caseData,
                                              RespondentResponseType respondentResponseType,
                                              LocalDateTime respondentResponseDate,
                                              String respondentID) {
        switch (respondentResponseType) {
            case FULL_DEFENCE:
                buildDefenceFiled(eventHistory, caseData, respondentResponseDate, respondentID);
                break;
            case PART_ADMISSION:
                buildReceiptOfPartAdmission(eventHistory, respondentResponseDate, respondentID);
                break;
            case FULL_ADMISSION:
                buildReceiptOfAdmission(eventHistory, respondentResponseDate, respondentID);
                break;
            default:
                break;
        }
    }

    private void buildRespondentResponseEventForSpec(EventHistory eventHistory,
                                                     CaseData caseData,
                                                     RespondentResponseTypeSpec respondentResponseTypeSpec,
                                                     LocalDateTime respondentResponseDate,
                                                     String respondentID) {
        switch (respondentResponseTypeSpec) {
            case FULL_DEFENCE:
                buildDefenceFiled(eventHistory, caseData, respondentResponseDate, respondentID);
                break;
            case PART_ADMISSION:
                buildReceiptOfPartAdmission(eventHistory, respondentResponseDate, respondentID);
                break;
            case FULL_ADMISSION:
                buildReceiptOfAdmission(eventHistory, respondentResponseDate, respondentID);
                break;
            default:
                break;
        }
    }

    private void buildDefenceFiled(EventHistory eventHistory,
                                   CaseData caseData,
                                   LocalDateTime respondentResponseDate,
                                   String respondentID) {
        if (respondentID.equals(RESPONDENT_ID)) {
            RespondToClaim respondToClaim = caseData.getRespondToClaim();
            if (isAllPaid(caseData.getTotalClaimAmount(), respondToClaim)) {
                eventHistory.statesPaid(buildDefenceFiledEvent(
                    builder, respondentResponseDate, respondentID,
                    isAllPaid(caseData.getTotalClaimAmount(), respondToClaim)
                ));
            } else {
                eventHistory.defenceFiled(buildDefenceFiledEvent(
                    builder, respondentResponseDate, respondentID,
                    false
                ));
            }
            eventHistory.directionsQuestionnaire(buildDirectionsQuestionnaireFiledEvent(
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
                eventHistory.statesPaid(buildDefenceFiledEvent(
                    builder, respondentResponseDate, respondentID,
                    isAllPaid(caseData.getTotalClaimAmount(), respondToClaim)
                ));
            } else {
                eventHistory.defenceFiled(buildDefenceFiledEvent(
                    builder, respondentResponseDate, respondentID,
                    false
                ));
            }
            eventHistory.directionsQuestionnaire(buildDirectionsQuestionnaireFiledEvent(
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

    private void buildReceiptOfPartAdmission(EventHistory eventHistory,
                                             LocalDateTime respondentResponseDate,
                                             String respondentID) {
        Event receiptOfPartAdmissionEvent = Event.builder()
            .eventSequence(prepareEventSequence(eventHistory))
            .eventCode(RECEIPT_OF_PART_ADMISSION.getCode())
            .dateReceived(respondentResponseDate)
            .litigiousPartyID(respondentID)
            .build();
        List<Event> receiptOfPartAdmissionList = eventHistory.getReceiptOfPartAdmission() != null 
            ? new ArrayList<>(eventHistory.getReceiptOfPartAdmission()) 
            : new ArrayList<>();
        receiptOfPartAdmissionList.add(receiptOfPartAdmissionEvent);
        eventHistory.setReceiptOfPartAdmission(receiptOfPartAdmissionList);
    }

    private void buildReceiptOfAdmission(EventHistory eventHistory,
                                         LocalDateTime respondentResponseDate,
                                         String respondentID) {
        Event receiptOfAdmissionEvent = Event.builder()
            .eventSequence(prepareEventSequence(eventHistory))
            .eventCode(RECEIPT_OF_ADMISSION.getCode())
            .dateReceived(respondentResponseDate)
            .litigiousPartyID(respondentID)
            .build();
        List<Event> receiptOfAdmissionList = eventHistory.getReceiptOfAdmission() != null 
            ? new ArrayList<>(eventHistory.getReceiptOfAdmission()) 
            : new ArrayList<>();
        receiptOfAdmissionList.add(receiptOfAdmissionEvent);
        eventHistory.setReceiptOfAdmission(receiptOfAdmissionList);
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
                        defaultText = "RPA Reason: Defendant rejects and counter claims.";
                        break;
                    case FULL_ADMISSION:
                        defaultText = "RPA Reason: Defendant fully admits.";
                        break;
                    case PART_ADMISSION:
                        defaultText = "RPA Reason: Defendant partial admission.";
                        break;
                    default:
                        break;
                }
            } else {
                switch (caseData.getRespondent1ClaimResponseType()) {
                    case COUNTER_CLAIM:
                        defaultText = "RPA Reason: Defendant rejects and counter claims.";
                        break;
                    case FULL_ADMISSION:
                        defaultText = "RPA Reason: Defendant fully admits.";
                        break;
                    case PART_ADMISSION:
                        defaultText = "RPA Reason: Defendant partial admission.";
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
            defaultText = (format(
                "RPA Reason: %sDefendant: %s has responded: %s",
                paginatedMessage,
                respondent.getPartyName(),
                SPEC_CLAIM.equals(caseData.getCaseAccessCategory())
                    ? getResponseTypeForRespondentSpec(caseData, respondent)
                    : getResponseTypeForRespondent(caseData, respondent)
            ));
        }
        return defaultText;
    }

    private void buildCaseNotesEvents(EventHistory eventHistory, CaseData caseData) {
        if (isNotEmpty(caseData.getCaseNotes())) {
            buildMiscellaneousCaseNotesEvent(eventHistory, caseData);
        }

    }

    private void buildMiscellaneousCaseNotesEvent(EventHistory eventHistory, CaseData caseData) {
        List<Event> events = unwrapElements(caseData.getCaseNotes())
            .stream()
            .map(caseNote ->
                     Event.builder()
                         .eventSequence(prepareEventSequence(eventHistory))
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
        eventHistory.setMiscellaneous(events);
    }

    private void buildRespondent1LitigationFriendEvent(EventHistory eventHistory, CaseData caseData) {
        if (caseData.getRespondent1LitigationFriendCreatedDate() != null) {
            buildMiscellaneousRespondent1LitigationFriendEvent(eventHistory, caseData);
        }
    }

    private void buildMiscellaneousRespondent1LitigationFriendEvent(EventHistory eventHistory,
                                                                    CaseData caseData) {
        String miscText = "Litigation friend added for respondent: " + caseData.getRespondent1().getPartyName();
        eventHistory.miscellaneous(
            Event.builder()
                .eventSequence(prepareEventSequence(eventHistory))
                .eventCode(MISCELLANEOUS.getCode())
                .dateReceived(caseData.getRespondent1LitigationFriendCreatedDate())
                .eventDetailsText(miscText)
                .eventDetails(EventDetails.builder()
                                  .miscText(miscText)
                                  .build())
                .build());
    }

    private void buildRespondent2LitigationFriendEvent(EventHistory eventHistory, CaseData caseData) {
        if (caseData.getRespondent2LitigationFriendCreatedDate() != null) {
            buildMiscellaneousRespondent2LitigationFriendEvent(builder, caseData);
        }
    }

    private void buildMiscellaneousRespondent2LitigationFriendEvent(EventHistory eventHistory,
                                                                    CaseData caseData) {
        String miscText = "Litigation friend added for respondent: " + caseData.getRespondent2().getPartyName();
        eventHistory.miscellaneous(
            Event.builder()
                .eventSequence(prepareEventSequence(eventHistory))
                .eventCode(MISCELLANEOUS.getCode())
                .dateReceived(caseData.getRespondent2LitigationFriendCreatedDate())
                .eventDetailsText(miscText)
                .eventDetails(EventDetails.builder()
                                  .miscText(miscText)
                                  .build())
                .build());
    }

    private void buildClaimDetailsNotified(EventHistory eventHistory, CaseData caseData) {
        String miscText = "Claim details notified.";
        eventHistory.miscellaneous(
            Event.builder()
                .eventSequence(prepareEventSequence(eventHistory))
                .eventCode(MISCELLANEOUS.getCode())
                .dateReceived(caseData.getClaimDetailsNotificationDate())
                .eventDetailsText(miscText)
                .eventDetails(EventDetails.builder()
                                  .miscText(miscText)
                                  .build())
                .build());

    }

    private void buildClaimIssued(EventHistory eventHistory, CaseData caseData) {
        String miscText = "Claim issued in CCD.";
        eventHistory.miscellaneous(
            Event.builder()
                .eventSequence(prepareEventSequence(eventHistory))
                .eventCode(MISCELLANEOUS.getCode())
                .dateReceived(caseData.getIssueDate().atStartOfDay())
                .eventDetailsText(miscText)
                .eventDetails(EventDetails.builder()
                                  .miscText(miscText)
                                  .build())
                .build());
    }

    private void buildClaimTakenOfflinePastApplicantResponse(EventHistory eventHistory,
                                                             CaseData caseData) {
        String detailsText = "RPA Reason: Claim moved offline after no response from applicant past response deadline.";
        eventHistory.miscellaneous(
            Event.builder()
                .eventSequence(prepareEventSequence(eventHistory))
                .eventCode(MISCELLANEOUS.getCode())
                .dateReceived(caseData.getTakenOfflineDate())
                .eventDetailsText(detailsText)
                .eventDetails(EventDetails.builder()
                                  .miscText(detailsText)
                                  .build())
                .build());
    }

    private void buildClaimDismissedPastNotificationsDeadline(EventHistory eventHistory,
                                                              CaseData caseData, String miscText) {
        eventHistory.miscellaneous(
            Event.builder()
                .eventSequence(prepareEventSequence(eventHistory))
                .eventCode(MISCELLANEOUS.getCode())
                .dateReceived(caseData.getClaimDismissedDate())
                .eventDetailsText(miscText)
                .eventDetails(EventDetails.builder()
                                  .miscText(miscText)
                                  .build())
                .build());
    }

    private void buildClaimDismissedPastDeadline(EventHistory eventHistory,
                                                 CaseData caseData, List<State> stateHistory) {
        State previousState = getPreviousState(stateHistory);
        FlowState.Main flowState = (FlowState.Main) FlowState.fromFullName(previousState.getName());
        eventHistory.miscellaneous(
            Event.builder()
                .eventSequence(prepareEventSequence(eventHistory))
                .eventCode(MISCELLANEOUS.getCode())
                .dateReceived(caseData.getClaimDismissedDate())
                .eventDetailsText(prepareClaimDismissedDetails(flowState))
                .eventDetails(EventDetails.builder()
                                  .miscText(prepareClaimDismissedDetails(flowState))
                                  .build())
                .build());
    }

    public String prepareClaimDismissedDetails(FlowState.Main flowState) {
        switch (flowState) {
            case CLAIM_NOTIFIED, CLAIM_DETAILS_NOTIFIED:
                return "RPA Reason: Claim dismissed after no response from defendant after claimant sent notification.";
            case NOTIFICATION_ACKNOWLEDGED, NOTIFICATION_ACKNOWLEDGED_TIME_EXTENSION, CLAIM_DETAILS_NOTIFIED_TIME_EXTENSION, PAST_CLAIM_DISMISSED_DEADLINE_AWAITING_CAMUNDA:
                return "RPA Reason: Claim dismissed. No user action has been taken for 6 months.";
            default:
                throw new IllegalStateException("Unexpected flow state " + flowState.fullName());
        }
    }

    private State getPreviousState(List<State> stateHistory) {
        if (stateHistory.size() > 1) {
            return stateHistory.get(stateHistory.size() - 2);
        } else {
            throw new IllegalStateException("Flow state history should have at least two items: " + stateHistory);
        }
    }

    private void buildTakenOfflineByStaff(EventHistory eventHistory, CaseData caseData) {
        eventHistory.miscellaneous(
            Event.builder()
                .eventSequence(prepareEventSequence(eventHistory))
                .eventCode(MISCELLANEOUS.getCode())
                .dateReceived(caseData.getTakenOfflineByStaffDate())
                .eventDetailsText(prepareTakenOfflineEventDetails(caseData))
                .eventDetails(EventDetails.builder()
                                  .miscText(prepareTakenOfflineEventDetails(caseData))
                                  .build())
                .build());
    }

    private void buildQueriesEvent(EventHistory eventHistory, CaseData caseData) {
        if (!isCaseOffline(caseData)) {
            return;
        }
        if (!hasActiveQueries(caseData)) {
            return;
        }

        LocalDateTime dateReceived = Optional.ofNullable(caseData.getTakenOfflineDate())
            .orElseGet(time::now);

        eventHistory.miscellaneous(
            Event.builder()
                .eventSequence(prepareEventSequence(eventHistory))
                .eventCode(MISCELLANEOUS.getCode())
                .dateReceived(dateReceived)
                .eventDetailsText(QUERIES_ON_CASE)
                .eventDetails(EventDetails.builder()
                                  .miscText(QUERIES_ON_CASE)
                                  .build())
                .build());
    }

    private boolean hasActiveQueries(CaseData caseData) {
        if (featureToggleService.isPublicQueryManagementEnabled(caseData)) {
            return caseData.getQueries() != null;
        } else {
            return caseData.getQmApplicantSolicitorQueries() != null
                || caseData.getQmRespondentSolicitor1Queries() != null
                || caseData.getQmRespondentSolicitor2Queries() != null;
        }
    }

    private boolean isCaseOffline(CaseData caseData) {
        return OFFLINE_STATES.contains(caseData.getCcdState()) || caseData.getTakenOfflineDate() != null;
    }

    private void buildGeneralFormApplicationEventsStrikeOutOrder(EventHistory eventHistory,
                                                                 CaseData caseData) {
        if (caseData.getGeneralApplications() != null) {

            var generalApplications = caseData
                .getGeneralApplications()
                .stream()
                .filter(application -> {
                    if (application.getValue().getCaseLink() == null) {
                        return false;
                    }

                    return application.getValue().getGeneralAppType().getTypes().contains(STRIKE_OUT)
                        && getGeneralApplicationDetailsJudgeDecisionWithStruckOutDefence(
                        application.getValue().getCaseLink().getCaseReference(), caseData
                    ) != null;
                })
                .toList();

            if (!generalApplications.isEmpty()) {
                buildGeneralFormOfApplicationStrikeOut(builder, generalApplications);
                buildDefenceStruckOutJudgmentEvent(builder, generalApplications);
            }
        }

    }

    private void buildGeneralFormOfApplicationStrikeOut(EventHistory eventHistory,
                                                        List<Element<GeneralApplication>> generalApplicationsStrikeOut) {

        List<Event> generalApplicationsEvents = IntStream.range(0, generalApplicationsStrikeOut.size())
                .mapToObj(index -> {
                    String miscText = "APPLICATION TO Strike Out";
                    return Event.builder()
                            .eventSequence(prepareEventSequence(eventHistory))
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

        eventHistory.generalFormOfApplication(generalApplicationsEvents);
    }

    private void buildDefenceStruckOutJudgmentEvent(EventHistory eventHistory,
                                                    List<Element<GeneralApplication>> generalApplicationsStrikeOut) {

        List<Event> generalApplicationsEvents = IntStream.range(0, generalApplicationsStrikeOut.size())
                .mapToObj(index -> {
                    return Event.builder()
                            .eventSequence(prepareEventSequence(eventHistory))
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

        eventHistory.defenceStruckOut(generalApplicationsEvents);
    }

    private Element<GeneralApplicationsDetails> getGeneralApplicationDetailsJudgeDecisionWithStruckOutDefence(
            String caseLinkId, CaseData caseData) {

        List<Element<GeneralApplicationsDetails>> gaDetailsMasterCollection = caseData.getGaDetailsMasterCollection();
        if (gaDetailsMasterCollection == null) {
            return null;
        }
        return gaDetailsMasterCollection.stream()
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
        int currentSequence = 0;
        currentSequence = getCurrentSequence(history.getMiscellaneous(), currentSequence);
        currentSequence = getCurrentSequence(history.getAcknowledgementOfServiceReceived(), currentSequence);
        currentSequence = getCurrentSequence(history.getConsentExtensionFilingDefence(), currentSequence);
        currentSequence = getCurrentSequence(history.getDefenceFiled(), currentSequence);
        currentSequence = getCurrentSequence(history.getDefenceAndCounterClaim(), currentSequence);
        currentSequence = getCurrentSequence(history.getReceiptOfPartAdmission(), currentSequence);
        currentSequence = getCurrentSequence(history.getReceiptOfAdmission(), currentSequence);
        currentSequence = getCurrentSequence(history.getReplyToDefence(), currentSequence);
        currentSequence = getCurrentSequence(history.getBreathingSpaceEntered(), currentSequence);
        currentSequence = getCurrentSequence(history.getBreathingSpaceLifted(), currentSequence);
        currentSequence = getCurrentSequence(history.getBreathingSpaceMentalHealthEntered(), currentSequence);
        currentSequence = getCurrentSequence(history.getBreathingSpaceMentalHealthLifted(), currentSequence);
        currentSequence = getCurrentSequence(history.getStatesPaid(), currentSequence);
        currentSequence = getCurrentSequence(history.getDirectionsQuestionnaireFiled(), currentSequence);
        currentSequence = getCurrentSequence(history.getJudgmentByAdmission(), currentSequence);
        currentSequence = getCurrentSequence(history.getGeneralFormOfApplication(), currentSequence);
        currentSequence = getCurrentSequence(history.getDefenceStruckOut(), currentSequence);
        return currentSequence + 1;
    }

    private int getCurrentSequence(List<Event> events, int currentSequence) {
        for (Event event : events) {
            if (event.getEventSequence() != null && event.getEventSequence() > currentSequence) {
                currentSequence = event.getEventSequence();
            }
        }
        return currentSequence;
    }

    public String prepareTakenOfflineEventDetails(CaseData caseData) {
        if (UNSPEC_CLAIM.equals(caseData.getCaseAccessCategory())) {
            return left(format(
                "RPA Reason: Manually moved offline for reason %s on date %s.",
                prepareTakenOfflineByStaffReason(caseData.getClaimProceedsInCaseman()),
                caseData.getClaimProceedsInCaseman().getDate().format(ISO_DATE)
            ), 250); // Max chars allowed by Caseman
        } else {
            return left(format(
                "RPA Reason: Manually moved offline for reason %s on date %s.",
                prepareTakenOfflineByStaffReasonSpec(caseData.getClaimProceedsInCasemanLR()),
                caseData.getClaimProceedsInCasemanLR().getDate().format(ISO_DATE)
            ), 250); // Max chars allowed by Caseman
        }
    }

    private String prepareTakenOfflineByStaffReason(ClaimProceedsInCaseman claimProceedsInCaseman) {
        if (claimProceedsInCaseman.getReason() == ReasonForProceedingOnPaper.OTHER) {
            return claimProceedsInCaseman.getOther();
        }
        return claimProceedsInCaseman.getReason().name();
    }

    private String prepareTakenOfflineByStaffReasonSpec(ClaimProceedsInCasemanLR claimProceedsInCasemanLR) {
        if (claimProceedsInCasemanLR.getReason() == ReasonForProceedingOnPaper.OTHER) {
            return claimProceedsInCasemanLR.getOther();
        }
        return claimProceedsInCasemanLR.getReason().name();
    }

    private void buildClaimantHasNotifiedDefendant(EventHistory eventHistory, CaseData caseData) {
        eventHistory.miscellaneous(
            Event.builder()
                .eventSequence((prepareEventSequence(eventHistory)))
                .eventCode(MISCELLANEOUS.getCode())
                .dateReceived(caseData.getClaimNotificationDate())
                .eventDetailsText("Claimant has notified defendant.")
                .eventDetails(EventDetails.builder()
                                  .miscText("Claimant has notified defendant.")
                                  .build())
                .build());
    }

    static final String RPA_REASON_ONLY_ONE_OF_THE_RESPONDENT_IS_NOTIFIED = "RPA Reason: Only one of the respondent is notified.";

    private void buildTakenOfflineAfterClaimNotified(EventHistory eventHistory, CaseData caseData) {
        eventHistory.miscellaneous(
            List.of(
                Event.builder()
                    .eventSequence(prepareEventSequence(eventHistory))
                    .eventCode(MISCELLANEOUS.getCode())
                    .dateReceived(caseData.getSubmittedDate())
                    .eventDetailsText(RPA_REASON_ONLY_ONE_OF_THE_RESPONDENT_IS_NOTIFIED)
                    .eventDetails(EventDetails.builder()
                                      .miscText(RPA_REASON_ONLY_ONE_OF_THE_RESPONDENT_IS_NOTIFIED)
                                      .build())
                    .build()
            ));
    }

    private void buildTakenOfflineMultitrackUnspec(EventHistory eventHistory, CaseData caseData) {
        if (AllocatedTrack.MULTI_CLAIM.equals(caseData.getAllocatedTrack())) {
            String miscText = "RPA Reason:Multitrack Unspec going offline.";
            eventHistory.miscellaneous(
                Event.builder()
                    .eventSequence(prepareEventSequence(eventHistory))
                    .eventCode(MISCELLANEOUS.getCode())
                    .dateReceived(caseData.getApplicant1ResponseDate())
                    .eventDetailsText(miscText)
                    .eventDetails(EventDetails.builder()
                                      .miscText(miscText)
                                      .build())
                    .build());
        }
    }

    static final String CLAIMANT_PROCEEDS = "Claimant proceeds.";

    private void buildFullDefenceProceed(EventHistory eventHistory, CaseData caseData, String authToken) {
        List<ClaimantResponseDetails> applicantDetails = prepareApplicantsDetails(caseData);
        final List<String> miscEventText = prepMultipartyProceedMiscText(caseData);

        CaseCategory claimType = caseData.getCaseAccessCategory();
        if (SPEC_CLAIM.equals(claimType)) {
            List<Event> dqForProceedingApplicantsSpec = IntStream.range(0, applicantDetails.size())
                .mapToObj(index ->
                              Event.builder()
                                  .eventSequence(prepareEventSequence(eventHistory))
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
            eventHistory.directionsQuestionnaireFiled(dqForProceedingApplicantsSpec);
        } else {
            String preferredCourtCode = locationRefDataUtil.getPreferredCourtData(
                caseData,
                authToken, true
            );

            List<Event> dqForProceedingApplicants = IntStream.range(0, applicantDetails.size())
                .mapToObj(index ->
                              Event.builder()
                                  .eventSequence(prepareEventSequence(eventHistory))
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
            eventHistory.directionsQuestionnaireFiled(dqForProceedingApplicants);
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
                    eventHistory.miscellaneous(miscText);
                } else {
                    List<String> applicantProceedsText = new ArrayList<>();
                    applicantProceedsText.add(CLAIMANT_PROCEEDS);
                    List<Event> miscText = prepareMiscEventList(builder, caseData, applicantProceedsText);
                    eventHistory.miscellaneous(miscText);
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
                    eventHistory.miscellaneous(miscText);
                } else {
                    List<String> applicantProceedsText = new ArrayList<>();
                    applicantProceedsText.add(CLAIMANT_PROCEEDS);
                    List<Event> miscText = prepareMiscEventList(builder, caseData, applicantProceedsText);
                    eventHistory.miscellaneous(miscText);
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
                    eventHistory.miscellaneous(miscText);
                } else {
                    List<String> applicantProceedsText = new ArrayList<>();
                    applicantProceedsText.add(CLAIMANT_PROCEEDS);
                    List<Event> miscText = prepareMiscEventList(builder, caseData, applicantProceedsText);
                    eventHistory.miscellaneous(miscText);
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
                    eventHistory.miscellaneous(miscText);
                } else {
                    List<String> applicantProceedsText = new ArrayList<>();
                    applicantProceedsText.add("Claimants proceed.");
                    List<Event> miscText = prepareMiscEventList(builder, caseData, applicantProceedsText);
                    eventHistory.miscellaneous(miscText);
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
        String currentTime = time.now().toLocalDate().toString();

        switch (getMultiPartyScenario(caseData)) {
            case ONE_V_TWO_ONE_LEGAL_REP, ONE_V_TWO_TWO_LEGAL_REP: {
                eventDetailsText.add(String.format(
                    "RPA Reason: [1 of 2 - %s] Claimant has provided intention: %s against defendant: %s",
                    currentTime,
                    YES.equals(caseData.getApplicant1ProceedWithClaimAgainstRespondent1MultiParty1v2())
                        ? PROCEED
                        : NOT_PROCEED,
                    caseData.getRespondent1().getPartyName()
                ));
                eventDetailsText.add(String.format(
                    "RPA Reason: [2 of 2 - %s] Claimant has provided intention: %s against defendant: %s",
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
                eventDetailsText.add(String.format(
                    "RPA Reason: [1 of 2 - %s] Claimant: %s has provided intention: %s",
                    currentTime,
                    caseData.getApplicant1().getPartyName(),
                    YES.equals(app1Proceeds)
                        ? PROCEED
                        : NOT_PROCEED
                ));
                eventDetailsText.add(String.format(
                    "RPA Reason: [2 of 2 - %s] Claimant: %s has provided intention: %s",
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
                eventDetailsText.add("RPA Reason: Claimant proceeds.");
            }
        }
        return eventDetailsText;
    }

    public List<Event> prepareMiscEventList(EventHistory eventHistory, CaseData caseData,
                                            List<String> miscEventText, LocalDateTime... eventDate) {
        return IntStream.range(0, miscEventText.size())
            .mapToObj(index ->
                          Event.builder()
                              .eventSequence(prepareEventSequence(eventHistory))
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

    private void buildFullDefenceNotProceed(EventHistory eventHistory, CaseData caseData) {
        String miscText = getMultiPartyScenario(caseData).equals(TWO_V_ONE)
            ? "RPA Reason: Claimants intend not to proceed."
            : "RPA Reason: Claimant intends not to proceed.";

        eventHistory.miscellaneous(Event.builder()
                                  .eventSequence(prepareEventSequence(eventHistory))
                                  .eventCode(MISCELLANEOUS.getCode())
                                  .dateReceived(caseData.getApplicant1ResponseDate())
                                  .eventDetailsText(miscText)
                                  .eventDetails(EventDetails.builder()
                                                    .miscText(miscText)
                                                    .build())
                                  .build());
    }

    private void buildRespondentFullDefence(EventHistory eventHistory, CaseData caseData) {
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
        eventHistory.defenceFiled(defenceFiledEvents);
        eventHistory.statesPaid(statesPaidEvents);
        eventHistory.clearDirectionsQuestionnaireFiled().directionsQuestionnaireFiled(directionsQuestionnaireFiledEvents);
    }

    private Event buildDirectionsQuestionnaireFiledEvent(EventHistory eventHistory,
                                                         CaseData caseData,
                                                         LocalDateTime respondentResponseDate,
                                                         String litigiousPartyID,
                                                         DQ respondentDQ,
                                                         Party respondent,
                                                         boolean isRespondent1) {
        return Event.builder()
            .eventSequence(prepareEventSequence(eventHistory))
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

    private Event buildDefenceFiledEvent(EventHistory eventHistory,
                                         LocalDateTime respondentResponseDate,
                                         String litigiousPartyID,
                                         boolean statesPaid) {
        return Event.builder()
            .eventSequence(prepareEventSequence(eventHistory))
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
            time.now().toLocalDate().toString()
        );
    }

    private void buildUnrepresentedDefendant(EventHistory eventHistory, CaseData caseData) {
        List<String> unrepresentedDefendantsNames = getDefendantNames(UNREPRESENTED, caseData);

        List<Event> events = IntStream.range(0, unrepresentedDefendantsNames.size())
            .mapToObj(index -> {
                String paginatedMessage = unrepresentedDefendantsNames.size() > 1
                    ? format(
                    "[%d of %d - %s] ",
                    index + 1,
                    unrepresentedDefendantsNames.size(),
                    time.now().toLocalDate().toString()
                )
                    : "";
                String eventText = format(
                    "RPA Reason: %sUnrepresented defendant: %s",
                    paginatedMessage,
                    unrepresentedDefendantsNames.get(index)
                );

                return Event.builder()
                    .eventSequence(prepareEventSequence(eventHistory))
                    .eventCode(MISCELLANEOUS.getCode())
                    .dateReceived(caseData.getSubmittedDate())
                    .eventDetailsText(eventText)
                    .eventDetails(EventDetails.builder().miscText(eventText).build())
                    .build();
            })
            .toList();
        eventHistory.setMiscellaneous(events);
    }

    private void buildOfflineAfterClaimsDetailsNotified(EventHistory eventHistory, CaseData caseData) {
        eventHistory.miscellaneous(
            List.of(
                Event.builder()
                    .eventSequence(prepareEventSequence(eventHistory))
                    .eventCode(MISCELLANEOUS.getCode())
                    .dateReceived(caseData.getSubmittedDate())
                    .eventDetailsText(RPA_REASON_ONLY_ONE_OF_THE_RESPONDENT_IS_NOTIFIED)
                    .eventDetails(EventDetails.builder()
                                      .miscText(RPA_REASON_ONLY_ONE_OF_THE_RESPONDENT_IS_NOTIFIED)
                                      .build())
                    .build()
            ));
    }

    private void buildUnregisteredDefendant(EventHistory eventHistory, CaseData caseData) {
        List<String> unregisteredDefendantsNames;

        unregisteredDefendantsNames = getDefendantNames(UNREGISTERED, caseData);

        List<Event> events = IntStream.range(0, unregisteredDefendantsNames.size())
            .mapToObj(index -> {
                String paginatedMessage = unregisteredDefendantsNames.size() > 1
                    ? format(
                    "[%d of %d - %s] ",
                    index + 1,
                    unregisteredDefendantsNames.size(),
                    time.now().toLocalDate().toString()
                )
                    : "";
                String eventText = format(
                    "RPA Reason: %sUnregistered defendant solicitor firm: %s",
                    paginatedMessage,
                    unregisteredDefendantsNames.get(index)
                );

                return Event.builder()
                    .eventSequence(prepareEventSequence(eventHistory))
                    .eventCode(MISCELLANEOUS.getCode())
                    .dateReceived(caseData.getSubmittedDate())
                    .eventDetailsText(eventText)
                    .eventDetails(EventDetails.builder().miscText(eventText).build())
                    .build();
            })
            .toList();
        eventHistory.setMiscellaneous(events);
    }

    private void buildUnregisteredAndUnrepresentedDefendant(EventHistory eventHistory,
                                                            CaseData caseData) {
        String localDateTime = time.now().toLocalDate().toString();

        List<String> unregisteredDefendantsNames = getDefendantNames(UNREGISTERED, caseData);

        String unrepresentedEventText = format(
            "RPA Reason: [1 of 2 - %s] Unrepresented defendant and unregistered "
                + "defendant solicitor firm. Unrepresented defendant: %s",
            localDateTime,
            getDefendantNames(UNREPRESENTED, caseData).get(0)
        );
        String unregisteredEventText = format(
            "RPA Reason: [2 of 2 - %s] Unrepresented defendant and unregistered "
                + "defendant solicitor firm. Unregistered defendant solicitor "
                + "firm: %s",
            localDateTime,
            unregisteredDefendantsNames.get(0)
        );

        eventHistory.miscellaneous(
            List.of(
                Event.builder()
                    .eventSequence(prepareEventSequence(eventHistory))
                    .eventCode(MISCELLANEOUS.getCode())
                    .dateReceived(caseData.getSubmittedDate())
                    .eventDetailsText(unrepresentedEventText)
                    .eventDetails(EventDetails.builder().miscText(unrepresentedEventText).build())
                    .build(),
                Event.builder()
                    .eventSequence(prepareEventSequence(eventHistory))
                    .eventCode(MISCELLANEOUS.getCode())
                    .dateReceived(caseData.getSubmittedDate())
                    .eventDetailsText(unregisteredEventText)
                    .eventDetails(EventDetails.builder().miscText(unregisteredEventText).build())
                    .build()
            ));
    }

    private void buildAcknowledgementOfServiceReceived(EventHistory eventHistory, CaseData caseData) {
        switch (getMultiPartyScenario(caseData)) {
            case ONE_V_TWO_TWO_LEGAL_REP: {
                List<Event> events = new ArrayList<>();
                if (defendant1AckExists.test(caseData)) {
                    events.add(buildAcknowledgementOfServiceEvent(builder, caseData, true, format(
                        "Defendant: %s has acknowledged: %s",
                        caseData.getRespondent1().getPartyName(),
                        caseData.getRespondent1ClaimResponseIntentionType().getLabel()
                    )));
                }
                if (defendant2AckExists.test(caseData)) {
                    events.add(buildAcknowledgementOfServiceEvent(builder, caseData, false, format(
                        "Defendant: %s has acknowledged: %s",
                        caseData.getRespondent2().getPartyName(),
                        caseData.getRespondent2ClaimResponseIntentionType().getLabel()
                    )));
                }

                eventHistory.acknowledgementOfServiceReceived(events);
                break;
            }
            case ONE_V_TWO_ONE_LEGAL_REP: {
                String currentTime = time.now().toLocalDate().toString();

                builder
                    .acknowledgementOfServiceReceived(
                        List.of(
                            buildAcknowledgementOfServiceEvent(
                                builder, caseData, true, format(
                                    "[1 of 2 - %s] Defendant: %s has acknowledged: %s",
                                    currentTime,
                                    caseData.getRespondent1().getPartyName(),
                                    caseData.getRespondent1ClaimResponseIntentionType().getLabel()
                                )
                            ),
                            buildAcknowledgementOfServiceEvent(
                                builder, caseData, false, format(
                                    "[2 of 2 - %s] Defendant: %s has acknowledged: %s",
                                    currentTime,
                                    caseData.getRespondent2().getPartyName(),
                                    evaluateRespondent2IntentionType(caseData)
                                )
                            )
                        ));
                break;
            }
            default: {
                if (SPEC_CLAIM.equals(caseData.getCaseAccessCategory())) {
                    buildAcknowledgementOfServiceSpec(builder, caseData.getRespondent1AcknowledgeNotificationDate());
                    return;
                }

                LocalDateTime dateAcknowledge = caseData.getRespondent1AcknowledgeNotificationDate();
                if (dateAcknowledge == null) {
                    return;
                }

                builder
                    .acknowledgementOfServiceReceived(
                        List.of(
                            SPEC_CLAIM.equals(caseData.getCaseAccessCategory())
                                ?
                                Event.builder()
                                    .eventSequence(prepareEventSequence(eventHistory))
                                    .eventCode("38")
                                    .dateReceived(dateAcknowledge)
                                    .litigiousPartyID("002")
                                    .eventDetails(EventDetails.builder()
                                                      .acknowledgeService("Acknowledgement of Service")
                                                      .build())
                                    .eventDetailsText("Defendant LR Acknowledgement of Service ")
                                    .build()
                                :
                                buildAcknowledgementOfServiceEvent(
                                    builder, caseData, true,
                                    format(
                                        "responseIntention: %s",
                                        caseData.getRespondent1ClaimResponseIntentionType().getLabel()
                                    )
                                )));
            }
        }
    }

    public String evaluateRespondent2IntentionType(CaseData caseData) {
        if (caseData.getRespondent2ClaimResponseIntentionType() != null) {
            return caseData.getRespondent2ClaimResponseIntentionType().getLabel();
        }
        //represented by same solicitor
        return caseData.getRespondent1ClaimResponseIntentionType().getLabel();
    }

    private Event buildAcknowledgementOfServiceEvent(EventHistory eventHistory, CaseData caseData,
                                                     boolean isRespondent1, String eventDetailsText) {
        return Event.builder()
            .eventSequence(prepareEventSequence(eventHistory))
            .eventCode(ACKNOWLEDGEMENT_OF_SERVICE_RECEIVED.getCode())
            .dateReceived(isRespondent1
                              ? caseData.getRespondent1AcknowledgeNotificationDate()
                              : caseData.getRespondent2AcknowledgeNotificationDate())
            .litigiousPartyID(isRespondent1 ? "002" : "003")
            .eventDetails(EventDetails.builder()
                              .responseIntention(
                                  isRespondent1
                                      ? caseData.getRespondent1ClaimResponseIntentionType().getLabel()
                                      : evaluateRespondent2IntentionType(caseData))
                              .build())
            .eventDetailsText(eventDetailsText)
            .build();
    }

    private void buildAcknowledgementOfServiceSpec(EventHistory eventHistory,
                                                   LocalDateTime dateAcknowledge) {
        builder
            .acknowledgementOfServiceReceived(
                List.of(Event.builder()
                            .eventSequence(prepareEventSequence(eventHistory))
                            .eventCode("38")
                            .dateReceived(dateAcknowledge)
                            .litigiousPartyID("002")
                            .eventDetails(EventDetails.builder()
                                              .acknowledgeService("Acknowledgement of Service")
                                              .build())
                            .eventDetailsText("Defendant LR Acknowledgement of Service ")
                            .build()));
    }

    private void buildRespondentResponseText(EventHistory eventHistory, CaseData caseData, String miscText, LocalDateTime respondentResponseDate) {
        if (!SPEC_CLAIM.equals(caseData.getCaseAccessCategory())) {
            eventHistory.miscellaneous(Event.builder()
                                      .eventSequence(prepareEventSequence(eventHistory))
                                      .eventCode(MISCELLANEOUS.getCode())
                                      .dateReceived(respondentResponseDate)
                                      .eventDetailsText(miscText)
                                      .eventDetails(EventDetails.builder()
                                                        .miscText(miscText)
                                                        .build())
                                      .build());
        }

    }

    private void buildRespondentFullAdmission(EventHistory eventHistory, CaseData caseData) {
        String miscText;
        if (defendant1ResponseExists.test(caseData)) {
            miscText = prepareRespondentResponseText(caseData, caseData.getRespondent1(), true);
            eventHistory.receiptOfAdmission(Event.builder()
                                           .eventSequence(prepareEventSequence(eventHistory))
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
                eventHistory.receiptOfAdmission(Event.builder()
                                               .eventSequence(prepareEventSequence(eventHistory))
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
            eventHistory.receiptOfAdmission(Event.builder()
                                           .eventSequence(prepareEventSequence(eventHistory))
                                           .eventCode(RECEIPT_OF_ADMISSION.getCode())
                                           .dateReceived(caseData.getRespondent2ResponseDate())
                                           .litigiousPartyID(RESPONDENT2_ID)
                                           .build()
            );
            buildRespondentResponseText(builder, caseData, miscText, caseData.getRespondent2ResponseDate());
        }
    }

    private void buildRespondentPartAdmission(EventHistory eventHistory, CaseData caseData) {
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
                eventHistory.statesPaid(buildDefenceFiledEvent(
                    builder,
                    respondent1ResponseDate,
                    RESPONDENT_ID,
                    true
                ));
            } else {
                eventHistory.receiptOfPartAdmission(
                    Event.builder()
                        .eventSequence(prepareEventSequence(eventHistory))
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
                eventHistory.receiptOfPartAdmission(
                    Event.builder()
                        .eventSequence(prepareEventSequence(eventHistory))
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
            eventHistory.receiptOfPartAdmission(
                Event.builder()
                    .eventSequence(prepareEventSequence(eventHistory))
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
        eventHistory.clearDirectionsQuestionnaireFiled().directionsQuestionnaireFiled(directionsQuestionnaireFiledEvents);
    }

    private void buildRespondentCounterClaim(EventHistory eventHistory, CaseData caseData) {
        String miscText;
        if (defendant1ResponseExists.test(caseData)) {
            miscText = prepareRespondentResponseText(caseData, caseData.getRespondent1(), true);
            eventHistory.miscellaneous(Event.builder()
                                .eventSequence(prepareEventSequence(eventHistory))
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
                eventHistory.miscellaneous(Event.builder()
                                    .eventSequence(prepareEventSequence(eventHistory))
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
            eventHistory.miscellaneous(Event.builder()
                                .eventSequence(prepareEventSequence(eventHistory))
                                .eventCode(MISCELLANEOUS.getCode())
                                .dateReceived(caseData.getRespondent2ResponseDate())
                                .eventDetailsText(miscText)
                                .eventDetails(EventDetails.builder()
                                                  .miscText(miscText)
                                                  .build())
                                .build());
        }
    }

    private void buildConsentExtensionFilingDefence(EventHistory eventHistory, CaseData caseData) {
        List<Event> events = new ArrayList<>();
        MultiPartyScenario scenario = getMultiPartyScenario(caseData);

        if (defendant1ExtensionExists.test(caseData)) {
            events.add(buildConsentExtensionFilingDefenceEvent(
                PartyUtils.respondent1Data(caseData), scenario, prepareEventSequence(eventHistory)
            ));
        }

        if (defendant2ExtensionExists.test(caseData)) {
            events.add(buildConsentExtensionFilingDefenceEvent(
                PartyUtils.respondent2Data(caseData), scenario, prepareEventSequence(eventHistory)
            ));
        }

        eventHistory.consentExtensionFilingDefence(events);
    }

    private void buildInformAgreedExtensionDateForSpec(EventHistory eventHistory, CaseData caseData) {
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

    private void buildSDONotDrawn(EventHistory eventHistory,
                                  CaseData caseData) {
        String miscText = left(format(
            "RPA Reason: Case proceeds offline. "
                + "Judge / Legal Advisor did not draw a Direction's Order: %s",
            caseData.getReasonNotSuitableSDO().getInput()
        ), 250);

        LocalDateTime eventDate = caseData.getUnsuitableSDODate();
        eventHistory.miscellaneous(
            Event.builder()
                .eventSequence(prepareEventSequence(eventHistory))
                .eventCode(MISCELLANEOUS.getCode())
                .dateReceived(eventDate)
                .eventDetailsText(miscText)
                .eventDetails(EventDetails.builder()
                                  .miscText(miscText)
                                  .build())
                .build());
    }

    private void buildClaimTakenOfflineAfterSDO(EventHistory eventHistory,
                                                CaseData caseData) {
        String detailsText = "RPA Reason: Case Proceeds in Caseman.";
        eventHistory.miscellaneous(
            Event.builder()
                .eventSequence(prepareEventSequence(eventHistory))
                .eventCode(MISCELLANEOUS.getCode())
                .dateReceived(caseData.getTakenOfflineDate())
                .eventDetailsText(detailsText)
                .eventDetails(EventDetails.builder()
                                  .miscText(detailsText)
                                  .build())
                .build());
    }

    private void buildMiscellaneousIJEvent(EventHistory eventHistory, CaseData caseData) {
        Boolean grantedFlag = caseData.getRespondent2() != null
            && caseData.getDefendantDetails() != null
            && !caseData.getDefendantDetails().getValue()
            .getLabel().startsWith("Both");
        String miscTextRequested = "RPA Reason: Summary judgment requested and referred to judge.";
        String miscTextGranted = "RPA Reason: Summary judgment granted and referred to judge.";
        if (caseData.getDefendantDetails() != null) {
            eventHistory.miscellaneous(
                Event.builder()
                    .eventSequence(prepareEventSequence(eventHistory))
                    .eventCode(MISCELLANEOUS.getCode())
                    .dateReceived(LocalDateTime.now())
                    .eventDetailsText(grantedFlag ? miscTextRequested : miscTextGranted)
                    .eventDetails(EventDetails.builder()
                                      .miscText(grantedFlag ? miscTextRequested : miscTextGranted)
                                      .build())
                    .build());
        }

    }

    private void buildMiscellaneousDJEvent(EventHistory eventHistory, CaseData caseData) {
        Boolean grantedFlag = caseData.getRespondent2() != null
            && caseData.getDefendantDetailsSpec() != null
            && !caseData.getDefendantDetailsSpec().getValue()
            .getLabel().startsWith("Both");
        String miscTextRequested =  "RPA Reason: Default Judgment requested and claim moved offline.";
        String miscTextGranted = "RPA Reason: Default Judgment granted and claim moved offline.";

        if (featureToggleService.isJOLiveFeedActive()) {
            miscTextGranted = RECORD_JUDGMENT;
        }

        if (caseData.getDefendantDetailsSpec() != null) {
            eventHistory.miscellaneous(
                Event.builder()
                    .eventSequence(prepareEventSequence(eventHistory))
                    .eventCode(MISCELLANEOUS.getCode())
                    .dateReceived(getDateOfDjCreated(caseData))
                    .eventDetailsText(grantedFlag ? miscTextRequested : miscTextGranted)
                    .eventDetails(EventDetails.builder()
                                      .miscText(grantedFlag ? miscTextRequested : miscTextGranted)
                                      .build())
                    .build());
        }
    }

    private String getInstallmentPeriod(CaseData data) {
        if (data.getPaymentTypeSelection().equals(DJPaymentTypeSelection.REPAYMENT_PLAN)) {
            if (data.getRepaymentFrequency().equals(RepaymentFrequencyDJ.ONCE_ONE_WEEK)) {
                return "WK";
            } else if (data.getRepaymentFrequency().equals(RepaymentFrequencyDJ.ONCE_TWO_WEEKS)) {
                return "FOR";
            } else if (data.getRepaymentFrequency().equals(RepaymentFrequencyDJ.ONCE_ONE_MONTH)) {
                return "MTH";
            }

        } else if (data.getPaymentTypeSelection().equals(DJPaymentTypeSelection.IMMEDIATELY)) {
            return "FW";
        }

        return "FUL";
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

    private void buildClaimTakenOfflineAfterDJ(EventHistory eventHistory,
                                               CaseData caseData) {
        if (caseData.getTakenOfflineDate() != null && caseData.getOrderSDODocumentDJ() != null) {
            buildClaimTakenOfflineAfterSDO(builder, caseData);
        }

    }

    private void buildSpecAdmitRejectRepayment(EventHistory eventHistory,
                                               CaseData caseData) {

        if (caseData.hasApplicantRejectedRepaymentPlan()) {
            eventHistory.miscellaneous(
                Event.builder()
                    .eventSequence(prepareEventSequence(eventHistory))
                    .eventCode(MISCELLANEOUS.getCode())
                    .dateReceived(setApplicant1ResponseDate(caseData))
                    .eventDetailsText(RPA_REASON_MANUAL_DETERMINATION)
                    .eventDetails(EventDetails.builder()
                                      .miscText(RPA_REASON_MANUAL_DETERMINATION)
                                      .build())
                    .build());
        }
    }

    private void buildClaimInMediation(EventHistory eventHistory,
                                       CaseData caseData) {

        if (caseData.hasDefendantAgreedToFreeMediation() && caseData.hasClaimantAgreedToFreeMediation()) {

            buildClaimantDirectionQuestionnaireForSpec(builder, caseData);

            eventHistory.miscellaneous(
                Event.builder()
                    .eventSequence(prepareEventSequence(eventHistory))
                    .eventCode(MISCELLANEOUS.getCode())
                    .dateReceived(setApplicant1ResponseDate(caseData))
                    .eventDetailsText(RPA_IN_MEDIATION)
                    .eventDetails(EventDetails.builder()
                                      .miscText(RPA_IN_MEDIATION)
                                      .build())
                    .build());
        }
    }

    private void buildClaimantDirectionQuestionnaireForSpec(EventHistory eventHistory,
                                               CaseData caseData) {
        List<ClaimantResponseDetails> applicantDetails = prepareApplicantsDetails(caseData);

        CaseCategory claimType = caseData.getCaseAccessCategory();

        if (SPEC_CLAIM.equals(claimType)) {
            List<Event> dqForProceedingApplicantsSpec = IntStream.range(0, applicantDetails.size())
                .mapToObj(index ->
                              Event.builder()
                                  .eventSequence(prepareEventSequence(eventHistory))
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
            eventHistory.directionsQuestionnaireFiled(dqForProceedingApplicantsSpec);
        }
    }

    private void buildLrVLipFullDefenceEvent(EventHistory eventHistory, CaseData caseData,
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

    private void buildTakenOfflineDueToDefendantNoc(EventHistory eventHistory, CaseData caseData) {
        eventHistory.miscellaneous(
            Event.builder()
                .eventSequence(prepareEventSequence(eventHistory))
                .eventCode(MISCELLANEOUS.getCode())
                .dateReceived(caseData.getTakenOfflineDate())
                .eventDetailsText("RPA Reason : Notice of Change filed.")
                .eventDetails(EventDetails.builder()
                                  .miscText("RPA Reason : Notice of Change filed.")
                                  .build())
                .build());
    }

    private LocalDateTime setApplicant1ResponseDate(CaseData caseData) {
        LocalDateTime applicant1ResponseDate = caseData.getApplicant1ResponseDate();
        if (applicant1ResponseDate == null || applicant1ResponseDate.isBefore(LocalDateTime.now())) {
            applicant1ResponseDate = LocalDateTime.now();
        }
        return applicant1ResponseDate;
    }

    private void buildMiscellaneousForRespondentResponseLipVSLr(EventHistory eventHistory,
                                                                CaseData caseData) {
        if (caseData.isLipvLROneVOne()) {
            eventHistory.miscellaneous(
                Event.builder()
                    .eventSequence(prepareEventSequence(eventHistory))
                    .eventCode(MISCELLANEOUS.getCode())
                    .dateReceived(LocalDateTime.now())
                    .eventDetailsText("RPA Reason: LiP vs LR - full/part admission received.")
                    .eventDetails(EventDetails.builder()
                                      .miscText("RPA Reason: LiP vs LR - full/part admission received.")
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
