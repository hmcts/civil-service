package uk.gov.hmcts.reform.civil.service.robotics.mapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseType;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.RespondToClaim;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.ClaimantLiPResponse;
import uk.gov.hmcts.reform.civil.model.dq.DQ;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Respondent2DQ;
import uk.gov.hmcts.reform.civil.model.robotics.Event;
import uk.gov.hmcts.reform.civil.model.robotics.EventDetails;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;
import uk.gov.hmcts.reform.civil.service.flowstate.IStateFlowEngine;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsEventTextFormatter;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsSequenceGenerator;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsRespondentResponseSupport;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsTimelineHelper;
import uk.gov.hmcts.reform.civil.service.robotics.strategy.EventHistoryContributor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import uk.gov.hmcts.reform.civil.utils.MonetaryConversions;

import static java.lang.String.format;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.CaseState.CASE_DISCONTINUED;
import static uk.gov.hmcts.reform.civil.enums.CaseState.CASE_DISMISSED;
import static uk.gov.hmcts.reform.civil.enums.CaseState.PROCEEDS_IN_HERITAGE_SYSTEM;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_ONE_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.TWO_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseType.FULL_DEFENCE;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.model.robotics.EventType.DEFENCE_FILED;
import static uk.gov.hmcts.reform.civil.model.robotics.EventType.DIRECTIONS_QUESTIONNAIRE_FILED;
import static uk.gov.hmcts.reform.civil.model.robotics.EventType.MISCELLANEOUS;
import static uk.gov.hmcts.reform.civil.model.robotics.EventType.RECEIPT_OF_ADMISSION;
import static uk.gov.hmcts.reform.civil.model.robotics.EventType.RECEIPT_OF_PART_ADMISSION;
import static uk.gov.hmcts.reform.civil.model.robotics.EventType.STATES_PAID;
import static uk.gov.hmcts.reform.civil.service.robotics.utils.RoboticsDataUtil.RESPONDENT2_ID;
import static uk.gov.hmcts.reform.civil.service.robotics.utils.RoboticsDataUtil.RESPONDENT_ID;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getResponseTypeForRespondent;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getResponseTypeForRespondentSpec;
import static uk.gov.hmcts.reform.civil.utils.PredicateUtils.defendant1ResponseExists;
import static uk.gov.hmcts.reform.civil.utils.PredicateUtils.defendant1v2SameSolicitorSameResponse;
import static uk.gov.hmcts.reform.civil.utils.PredicateUtils.defendant2DivergentResponseExists;
import static uk.gov.hmcts.reform.civil.utils.PredicateUtils.defendant2ResponseExists;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsDirectionsQuestionnaireSupport;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventHistoryMapper {

    private final IStateFlowEngine stateFlowEngine;
    private final FeatureToggleService featureToggleService;
    private final EventHistorySequencer eventHistorySequencer;
    private final RoboticsTimelineHelper timelineHelper;
    private final RoboticsEventTextFormatter textFormatter;
    private final RoboticsSequenceGenerator sequenceGenerator;
    private final RoboticsRespondentResponseSupport respondentResponseSupport;
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
                        break;
                    case FULL_DEFENCE:
                        buildRespondentFullDefence(builder, caseData);
                        break;
                    case PART_ADMISSION:
                        buildRespondentPartAdmission(builder, caseData);
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
                        break;
                    case FULL_DEFENCE_PROCEED:
                        break;
                    case TAKEN_OFFLINE_BY_STAFF:
                        break;
                    case CLAIM_DISMISSED_PAST_CLAIM_DISMISSED_DEADLINE:
                        break;
                    case CLAIM_DISMISSED_PAST_CLAIM_NOTIFICATION_DEADLINE:
                    case CLAIM_DISMISSED_PAST_CLAIM_DETAILS_NOTIFICATION_DEADLINE:
                    case TAKEN_OFFLINE_AFTER_CLAIM_DETAILS_NOTIFIED:
                    case TAKEN_OFFLINE_PAST_APPLICANT_RESPONSE_DEADLINE:
                        break;
                    default:
                        break;
                }
            });

        eventHistoryContributors.stream()
            .filter(contributor -> contributor.supports(caseData))
            .forEach(contributor -> contributor.contribute(builder, caseData, authToken));
        EventHistory eventHistory = eventHistorySequencer.sortEvents(builder.build());
        log.info("Event history: {}", eventHistory);
        return eventHistory;
    }

    private boolean hasCourtDecisionInFavourOfClaimant(CaseData caseData) {
        ClaimantLiPResponse applicant1Response = Optional.ofNullable(caseData.getCaseDataLiP())
            .map(CaseDataLiP::getApplicant1LiPResponse).orElse(null);
        return applicant1Response != null && applicant1Response.hasCourtDecisionInFavourOfClaimant();
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
        return respondentResponseSupport.prepareRespondentResponseText(caseData, respondent, isRespondent1);
    }

    private int prepareEventSequence(EventHistory history) {
        return sequenceGenerator.nextSequence(history);
    }

    public String prepareEventDetailsText(DQ dq, String preferredCourtCode) {
        return RoboticsDirectionsQuestionnaireSupport.prepareEventDetailsText(dq, preferredCourtCode);
    }

    public boolean isStayClaim(DQ dq) {
        return RoboticsDirectionsQuestionnaireSupport.isStayClaim(dq);
    }

    public String getPreferredCourtCode(DQ dq) {
        return RoboticsDirectionsQuestionnaireSupport.getPreferredCourtCode(dq);
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
        return respondentResponseSupport.getPaginatedMessageFor1v2SameSolicitor(caseData, isRespondent1);
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

}
