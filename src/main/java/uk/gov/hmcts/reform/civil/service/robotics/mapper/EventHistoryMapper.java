package uk.gov.hmcts.reform.civil.service.robotics.mapper;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.enums.ReasonForProceedingOnPaper;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseType;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.launchdarkly.FeatureToggleService;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.ClaimProceedsInCaseman;
import uk.gov.hmcts.reform.civil.model.ClaimantResponseDetails;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.PartyData;
import uk.gov.hmcts.reform.civil.model.RespondToClaim;
import uk.gov.hmcts.reform.civil.model.breathing.BreathingSpaceType;
import uk.gov.hmcts.reform.civil.model.dq.DQ;
import uk.gov.hmcts.reform.civil.model.dq.FileDirectionsQuestionnaire;
import uk.gov.hmcts.reform.civil.model.dq.RequestedCourt;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Respondent2DQ;
import uk.gov.hmcts.reform.civil.model.robotics.Event;
import uk.gov.hmcts.reform.civil.model.robotics.EventDetails;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;
import uk.gov.hmcts.reform.civil.model.robotics.EventType;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;
import uk.gov.hmcts.reform.civil.service.flowstate.StateFlowEngine;
import uk.gov.hmcts.reform.civil.stateflow.model.State;
import uk.gov.hmcts.reform.civil.utils.MonetaryConversions;
import uk.gov.hmcts.reform.civil.utils.PartyUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.lang.String.format;
import static java.time.format.DateTimeFormatter.ISO_DATE;
import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.left;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_ONE_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.TWO_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.enums.PartyRole.RESPONDENT_ONE;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseType.FULL_DEFENCE;
import static uk.gov.hmcts.reform.civil.enums.SuperClaimType.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.UnrepresentedOrUnregisteredScenario.UNREGISTERED;
import static uk.gov.hmcts.reform.civil.enums.UnrepresentedOrUnregisteredScenario.UNREGISTERED_NOTICE_OF_CHANGE;
import static uk.gov.hmcts.reform.civil.enums.UnrepresentedOrUnregisteredScenario.UNREPRESENTED;
import static uk.gov.hmcts.reform.civil.enums.UnrepresentedOrUnregisteredScenario.getDefendantNames;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.model.robotics.EventType.ACKNOWLEDGEMENT_OF_SERVICE_RECEIVED;
import static uk.gov.hmcts.reform.civil.model.robotics.EventType.BREATHING_SPACE_ENTERED;
import static uk.gov.hmcts.reform.civil.model.robotics.EventType.BREATHING_SPACE_LIFTED;
import static uk.gov.hmcts.reform.civil.model.robotics.EventType.CONSENT_EXTENSION_FILING_DEFENCE;
import static uk.gov.hmcts.reform.civil.model.robotics.EventType.DEFENCE_AND_COUNTER_CLAIM;
import static uk.gov.hmcts.reform.civil.model.robotics.EventType.DEFENCE_FILED;
import static uk.gov.hmcts.reform.civil.model.robotics.EventType.DIRECTIONS_QUESTIONNAIRE_FILED;
import static uk.gov.hmcts.reform.civil.model.robotics.EventType.MENTAL_HEALTH_BREATHING_SPACE_ENTERED;
import static uk.gov.hmcts.reform.civil.model.robotics.EventType.MENTAL_HEALTH_BREATHING_SPACE_LIFTED;
import static uk.gov.hmcts.reform.civil.model.robotics.EventType.MISCELLANEOUS;
import static uk.gov.hmcts.reform.civil.model.robotics.EventType.RECEIPT_OF_ADMISSION;
import static uk.gov.hmcts.reform.civil.model.robotics.EventType.RECEIPT_OF_PART_ADMISSION;
import static uk.gov.hmcts.reform.civil.model.robotics.EventType.REPLY_TO_DEFENCE;
import static uk.gov.hmcts.reform.civil.model.robotics.EventType.STATES_PAID;
import static uk.gov.hmcts.reform.civil.service.robotics.mapper.RoboticsDataMapper.APPLICANT2_ID;
import static uk.gov.hmcts.reform.civil.service.robotics.mapper.RoboticsDataMapper.APPLICANT_ID;
import static uk.gov.hmcts.reform.civil.service.robotics.mapper.RoboticsDataMapper.RESPONDENT2_ID;
import static uk.gov.hmcts.reform.civil.service.robotics.mapper.RoboticsDataMapper.RESPONDENT_ID;
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
public class EventHistoryMapper {

    private final StateFlowEngine stateFlowEngine;
    private final FeatureToggleService featureToggleService;
    private final EventHistorySequencer eventHistorySequencer;
    private final Time time;
    public static final String BS_REF = "Breathing space reference";
    public static final String BS_START_DT = "actual start date";
    public static final String BS_END_DATE = "actual end date";

    public EventHistory buildEvents(CaseData caseData) {
        EventHistory.EventHistoryBuilder builder = EventHistory.builder()
            .directionsQuestionnaireFiled(List.of(Event.builder().build()));

        stateFlowEngine.evaluate(caseData).getStateHistory()
            .forEach(state -> {
                FlowState.Main flowState = (FlowState.Main) FlowState.fromFullName(state.getName());
                switch (flowState) {
                    case TAKEN_OFFLINE_UNREPRESENTED_DEFENDANT:
                        buildUnrepresentedDefendant(builder, caseData);
                        break;
                    case TAKEN_OFFLINE_UNREPRESENTED_UNREGISTERED_DEFENDANT:
                        buildUnregisteredAndUnrepresentedDefendant(builder, caseData);
                        break;
                    case TAKEN_OFFLINE_UNREGISTERED_DEFENDANT:
                        buildUnregisteredDefendant(builder, caseData);
                        break;
                    // Notice of change:
                    case PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT: {
                        // this would change in CIV-1620
                        if (featureToggleService.isNoticeOfChangeEnabled()) {
                            buildClaimIssued(builder, caseData);
                        }
                        break;
                    }
                    case CLAIM_ISSUED:
                        buildClaimIssued(builder, caseData);
                        break;
                    case CLAIM_NOTIFIED:
                        buildClaimantHasNotifiedDefendant(builder, caseData);
                        break;
                    case TAKEN_OFFLINE_AFTER_CLAIM_NOTIFIED:
                        buildTakenOfflineAfterClaimNotified(builder, caseData);
                        break;
                    case CLAIM_DETAILS_NOTIFIED:
                        buildClaimDetailsNotified(builder, caseData);
                        break;
                    case NOTIFICATION_ACKNOWLEDGED:
                        buildAcknowledgementOfServiceReceived(builder, caseData);
                        break;
                    case NOTIFICATION_ACKNOWLEDGED_TIME_EXTENSION:
                    case CLAIM_DETAILS_NOTIFIED_TIME_EXTENSION:
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
                    case AWAITING_RESPONSES_FULL_DEFENCE_RECEIVED:
                    case AWAITING_RESPONSES_NOT_FULL_DEFENCE_RECEIVED:
                        buildRespondentDivergentResponse(builder, caseData, false);
                        break;
                    case DIVERGENT_RESPOND_GENERATE_DQ_GO_OFFLINE:
                    case DIVERGENT_RESPOND_GO_OFFLINE:
                        buildRespondentDivergentResponse(builder, caseData, true);
                        break;
                    case FULL_DEFENCE_NOT_PROCEED:
                        buildFullDefenceNotProceed(builder, caseData);
                        break;
                    case FULL_DEFENCE_PROCEED:
                        buildFullDefenceProceed(builder, caseData);
                        break;
                    case TAKEN_OFFLINE_BY_STAFF:
                        buildTakenOfflineByStaff(builder, caseData);
                        break;
                    case CLAIM_DISMISSED_PAST_CLAIM_DISMISSED_DEADLINE:
                        buildClaimDismissedPastDeadline(builder, caseData,
                                                        stateFlowEngine.evaluate(caseData).getStateHistory()
                        );
                        break;
                    case CLAIM_DISMISSED_PAST_CLAIM_NOTIFICATION_DEADLINE:
                        buildClaimDismissedPastNotificationsDeadline(
                            builder,
                            caseData,
                            "RPA Reason: Claim dismissed. Claimant hasn't taken action since the "
                                + "claim was issued."
                        );
                        break;
                    case CLAIM_DISMISSED_PAST_CLAIM_DETAILS_NOTIFICATION_DEADLINE:
                        buildClaimDismissedPastNotificationsDeadline(
                            builder,
                            caseData,
                            "RPA Reason: Claim dismissed. Claimant hasn't notified defendant of the "
                                + "claim details within the allowed 2 weeks."
                        );
                        break;
                    case TAKEN_OFFLINE_AFTER_CLAIM_DETAILS_NOTIFIED:
                        buildOfflineAfterClaimsDetailsNotified(
                            builder,
                            caseData
                        );
                        break;
                    case TAKEN_OFFLINE_PAST_APPLICANT_RESPONSE_DEADLINE:
                        buildClaimTakenOfflinePastApplicantResponse(builder, caseData);
                        break;
                    default:
                        break;
                }
            });

        buildRespondent1LitigationFriendEvent(builder, caseData);
        buildRespondent2LitigationFriendEvent(builder, caseData);
        buildCaseNotesEvents(builder, caseData);
        if (null != caseData.getBreathing()) {
            if (null != caseData.getBreathing().getEnter() && null == caseData.getBreathing().getLift()) {
                if (BreathingSpaceType.STANDARD.equals(caseData.getBreathing().getEnter().getType())) {
                    buildBreathingSpaceEvent(builder, caseData, BREATHING_SPACE_ENTERED, "Enter");
                } else if (BreathingSpaceType.MENTAL_HEALTH.equals(caseData.getBreathing().getEnter().getType())) {
                    buildBreathingSpaceEvent(builder, caseData,
                                             MENTAL_HEALTH_BREATHING_SPACE_ENTERED, "Enter"
                    );
                }
            } else if (null != caseData.getBreathing().getLift()) {
                if (BreathingSpaceType.STANDARD.equals(caseData.getBreathing().getEnter().getType())) {
                    buildBreathingSpaceEvent(builder, caseData, BREATHING_SPACE_ENTERED, "Enter");
                    buildBreathingSpaceEvent(builder, caseData, BREATHING_SPACE_LIFTED, "Lifted");
                } else if (BreathingSpaceType.MENTAL_HEALTH.equals(caseData.getBreathing().getEnter().getType())) {
                    buildBreathingSpaceEvent(builder, caseData,
                                             MENTAL_HEALTH_BREATHING_SPACE_ENTERED, "Enter"
                    );
                    buildBreathingSpaceEvent(builder, caseData, MENTAL_HEALTH_BREATHING_SPACE_LIFTED, "Lifted");
                }
            }
        }
        buildInformAgreedExtensionDateForSpec(builder, caseData);

        return eventHistorySequencer.sortEvents(builder.build());
    }

    private void buildBreathingSpaceEvent(EventHistory.EventHistoryBuilder builder, CaseData caseData,
                                          EventType eventType, String bsStatus) {
        String eventDetails = null;
        if (caseData.getBreathing().getEnter().getReference() != null) {
            eventDetails = BS_REF + " "
                + caseData.getBreathing().getEnter().getReference() + ", ";
        }

        if (bsStatus.equals("Enter")) {
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
        } else if (bsStatus.equals("Lifted")) {
            if (caseData.getBreathing().getLift().getExpectedEnd() != null) {
                if (eventDetails == null) {
                    eventDetails = StringUtils.capitalize(BS_END_DATE) + " "
                        + caseData.getBreathing().getLift().getExpectedEnd();
                } else {
                    eventDetails = eventDetails + BS_END_DATE + " "
                        + caseData.getBreathing().getLift().getExpectedEnd();
                }
            }
        }

        switch (eventType) {
            case BREATHING_SPACE_ENTERED:
                builder.breathingSpaceEntered((Event.builder()
                    .eventSequence(prepareEventSequence(builder.build()))
                    .eventCode(eventType.getCode())
                    .dateReceived(caseData.getBreathing().getEnter().getStart() != null
                                      ? caseData.getBreathing().getEnter().getStart().atTime(LocalTime.now())
                                      : LocalDateTime.now())
                    .litigiousPartyID("001")
                    .eventDetailsText(eventDetails)
                    .eventDetails(EventDetails.builder().miscText(eventDetails)
                                      .build())
                    .build()));
                break;
            case BREATHING_SPACE_LIFTED:
                builder.breathingSpaceLifted((Event.builder()
                    .eventSequence(prepareEventSequence(builder.build()))
                    .eventCode(eventType.getCode())
                    .dateReceived(caseData.getBreathing().getLift().getExpectedEnd() != null
                                      ? caseData.getBreathing().getLift().getExpectedEnd().atTime(LocalTime.now())
                                      : LocalDateTime.now())
                    .eventDetailsText(eventDetails)
                    .litigiousPartyID("001")
                    .eventDetails(EventDetails.builder().miscText(eventDetails)
                                      .build())
                    .build()));
                break;
            case MENTAL_HEALTH_BREATHING_SPACE_ENTERED:
                builder.breathingSpaceMentalHealthEntered((Event.builder()
                    .eventSequence(prepareEventSequence(builder.build()))
                    .eventCode(eventType.getCode())
                    .dateReceived(caseData.getBreathing().getEnter().getStart() != null
                                      ? caseData.getBreathing().getEnter().getStart().atTime(LocalTime.now())
                                      : LocalDateTime.now())
                    .eventDetailsText(eventDetails)
                    .litigiousPartyID("001")
                    .eventDetails(EventDetails.builder().miscText(eventDetails)
                                      .build())
                    .build()));
                break;
            case MENTAL_HEALTH_BREATHING_SPACE_LIFTED:
                builder.breathingSpaceMentalHealthLifted((Event.builder()
                    .eventSequence(prepareEventSequence(builder.build()))
                    .eventCode(eventType.getCode())
                    .dateReceived(caseData.getBreathing().getLift().getExpectedEnd() != null
                                      ? caseData.getBreathing().getLift().getExpectedEnd().atTime(LocalTime.now())
                                      : LocalDateTime.now())
                    .eventDetailsText(eventDetails)
                    .litigiousPartyID("001")
                    .eventDetails(EventDetails.builder().miscText(eventDetails)
                                      .build())
                    .build()));
                break;
            default:
                break;
        }
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
            if (SPEC_CLAIM.equals(caseData.getSuperClaimType())) {
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
            if (caseData.getSuperClaimType() == SPEC_CLAIM) {
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
            if (SPEC_CLAIM.equals(caseData.getSuperClaimType())) {
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
            if (caseData.getSuperClaimType() == SPEC_CLAIM) {
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
            case COUNTER_CLAIM:
                buildDefenceAndCounterClaim(builder, caseData, respondentResponseDate, respondentID);
                break;
            case PART_ADMISSION:
                buildReceiptOfPartAdmission(builder, caseData, respondentResponseDate, respondentID);
                break;
            case FULL_ADMISSION:
                buildReceiptOfAdmission(builder, caseData, respondentResponseDate, respondentID);
                break;
            /*case STATES_PAID:
                builder.statesPaid(
                    Event.builder()
                        .eventSequence(prepareEventSequence(builder.build()))
                        .eventCode(STATES_PAID.getCode())
                        .dateReceived(respondentResponseDate)
                        .litigiousPartyID(respondentID)
                        .build());
                break;
            case BREATHING_SPACE_ENTERED:
                builder.breathingSpaceEntered(
                    Event.builder()
                        .eventSequence(prepareEventSequence(builder.build()))
                        .eventCode(BREATHING_SPACE_ENTERED.getCode())
                        .dateReceived(respondentResponseDate)
                        .litigiousPartyID(respondentID)
                        .build());
                break;
            case BREATHING_SPACE_LIFTED:
                builder.breathingSpaceEntered(
                    Event.builder()
                        .eventSequence(prepareEventSequence(builder.build()))
                        .eventCode(BREATHING_SPACE_LIFTED.getCode())
                        .dateReceived(respondentResponseDate)
                        .litigiousPartyID(respondentID)
                        .build());
                break;
            case MENTAL_HEALTH_BREATHING_SPACE_ENTERED:
                builder.breathingSpaceMentalHealthEntered(
                    Event.builder()
                        .eventSequence(prepareEventSequence(builder.build()))
                        .eventCode(MENTAL_HEALTH_BREATHING_SPACE_ENTERED.getCode())
                        .dateReceived(respondentResponseDate)
                        .litigiousPartyID(respondentID)
                        .build());
                break;
            case MENTAL_HEALTH_BREATHING_SPACE_LIFTED:
                builder.breathingSpaceMentalHealthLifted(
                    Event.builder()
                        .eventSequence(prepareEventSequence(builder.build()))
                        .eventCode(MENTAL_HEALTH_BREATHING_SPACE_LIFTED.getCode())
                        .dateReceived(respondentResponseDate)
                        .litigiousPartyID(respondentID)
                        .build());
                break;*/
            /*case INTENTION_TO_PROCEED:
                builder.receiptOfAdmission(
                    Event.builder()
                        .eventSequence(prepareEventSequence(builder.build()))
                        .eventCode(INTENTION_TO_PROCEED.getCode())
                        .dateReceived(respondentResponseDate)
                        .litigiousPartyID(respondentID)
                        .build());
                break;
            case INTENTION_TO_PROCEED_STATES_PAID:
                builder.receiptOfAdmission(
                    Event.builder()
                        .eventSequence(prepareEventSequence(builder.build()))
                        .eventCode(INTENTION_TO_PROCEED_STATES_PAID.getCode())
                        .dateReceived(respondentResponseDate)
                        .litigiousPartyID(respondentID)
                        .build());
                break;*/
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
            case COUNTER_CLAIM:
                buildDefenceAndCounterClaim(builder, caseData, respondentResponseDate, respondentID);
                break;
            case PART_ADMISSION:
                buildReceiptOfPartAdmission(builder, caseData, respondentResponseDate, respondentID);
                break;
            case FULL_ADMISSION:
                buildReceiptOfAdmission(builder, caseData, respondentResponseDate, respondentID);
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

    private void buildDefenceAndCounterClaim(EventHistory.EventHistoryBuilder builder,
                                             CaseData caseData,
                                             LocalDateTime respondentResponseDate,
                                             String respondentID) {
        builder.defenceAndCounterClaim(
            Event.builder()
                .eventSequence(prepareEventSequence(builder.build()))
                .eventCode(DEFENCE_AND_COUNTER_CLAIM.getCode())
                .dateReceived(respondentResponseDate)
                .litigiousPartyID(respondentID)
                .build());
    }

    private void buildReceiptOfPartAdmission(EventHistory.EventHistoryBuilder builder,
                                             CaseData caseData,
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
                                         CaseData caseData,
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
            if (SPEC_CLAIM.equals(caseData.getSuperClaimType())) {
                switch (scenario.equals(TWO_V_ONE)
                    ? YES.equals(caseData.getDefendantSingleResponseToBothClaimants())
                    ? caseData.getRespondent1ClaimResponseTypeForSpec()
                    : caseData.getClaimant1ClaimResponseTypeForSpec()
                    : caseData.getRespondent1ClaimResponseTypeForSpec()) {
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
                SPEC_CLAIM.equals(caseData.getSuperClaimType())
                    ? getResponseTypeForRespondentSpec(caseData, respondent)
                    : getResponseTypeForRespondent(caseData, respondent)
            ));
        }
        return defaultText;
    }

    private void buildCaseNotesEvents(EventHistory.EventHistoryBuilder builder, CaseData caseData) {
        if (SPEC_CLAIM.equals(caseData.getSuperClaimType())) {
            if (featureToggleService.isSpecRpaContinuousFeedEnabled() && isNotEmpty(caseData.getCaseNotes())) {
                buildMiscellaneousCaseNotesEvent(builder, caseData);
            }
        } else {
            if (featureToggleService.isRpaContinuousFeedEnabled() && isNotEmpty(caseData.getCaseNotes())) {
                buildMiscellaneousCaseNotesEvent(builder, caseData);
            }
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
                         .eventDetailsText(left((format("case note added: %s", caseNote.getNote())), 250))
                         .eventDetails(EventDetails.builder()
                                           .miscText(left((format("case note added: %s", caseNote.getNote())), 250))
                                           .build())
                         .build())
            .collect(Collectors.toList());
        builder.miscellaneous(events);
    }

    private void buildRespondent1LitigationFriendEvent(EventHistory.EventHistoryBuilder builder, CaseData caseData) {
        if (SPEC_CLAIM.equals(caseData.getSuperClaimType())) {
            if (featureToggleService.isSpecRpaContinuousFeedEnabled()
                && caseData.getRespondent1LitigationFriendCreatedDate() != null) {
                buildMiscellaneousRespondent1LitigationFriendEvent(builder, caseData);
            }
        } else {
            if (featureToggleService.isRpaContinuousFeedEnabled()
                && caseData.getRespondent1LitigationFriendCreatedDate() != null) {
                buildMiscellaneousRespondent1LitigationFriendEvent(builder, caseData);
            }
        }
    }

    private void buildMiscellaneousRespondent1LitigationFriendEvent(EventHistory.EventHistoryBuilder builder,
                                                                    CaseData caseData) {
        String miscText = "Litigation friend added for respondent: " + caseData.getRespondent1().getPartyName();
        builder.miscellaneous(
            Event.builder()
                .eventSequence(prepareEventSequence(builder.build()))
                .eventCode(MISCELLANEOUS.getCode())
                .dateReceived(caseData.getRespondent1LitigationFriendCreatedDate())
                .eventDetailsText(miscText)
                .eventDetails(EventDetails.builder()
                                  .miscText(miscText)
                                  .build())
                .build());
    }

    private void buildRespondent2LitigationFriendEvent(EventHistory.EventHistoryBuilder builder, CaseData caseData) {
        if (SPEC_CLAIM.equals(caseData.getSuperClaimType())) {
            if (featureToggleService.isSpecRpaContinuousFeedEnabled()
                && caseData.getRespondent2LitigationFriendCreatedDate() != null) {
                buildMiscellaneousRespondent2LitigationFriendEvent(builder, caseData);
            }
        } else {
            if (featureToggleService.isRpaContinuousFeedEnabled()
                && caseData.getRespondent2LitigationFriendCreatedDate() != null) {
                buildMiscellaneousRespondent2LitigationFriendEvent(builder, caseData);
            }
        }
    }

    private void buildMiscellaneousRespondent2LitigationFriendEvent(EventHistory.EventHistoryBuilder builder,
                                                                    CaseData caseData) {
        String miscText = "Litigation friend added for respondent: " + caseData.getRespondent2().getPartyName();
        builder.miscellaneous(
            Event.builder()
                .eventSequence(prepareEventSequence(builder.build()))
                .eventCode(MISCELLANEOUS.getCode())
                .dateReceived(caseData.getRespondent2LitigationFriendCreatedDate())
                .eventDetailsText(miscText)
                .eventDetails(EventDetails.builder()
                                  .miscText(miscText)
                                  .build())
                .build());
    }

    private void buildClaimDetailsNotified(EventHistory.EventHistoryBuilder builder, CaseData caseData) {
        if (featureToggleService.isRpaContinuousFeedEnabled()) {
            String miscText = "Claim details notified.";
            builder.miscellaneous(
                Event.builder()
                    .eventSequence(prepareEventSequence(builder.build()))
                    .eventCode(MISCELLANEOUS.getCode())
                    .dateReceived(caseData.getClaimDetailsNotificationDate())
                    .eventDetailsText(miscText)
                    .eventDetails(EventDetails.builder()
                                      .miscText(miscText)
                                      .build())
                    .build());
        }
    }

    private boolean rpaEnabledForClaim(CaseData caseData) {
        if (SPEC_CLAIM.equals(caseData.getSuperClaimType())) {
            return featureToggleService.isSpecRpaContinuousFeedEnabled();
        } else {
            return featureToggleService.isRpaContinuousFeedEnabled();
        }
    }

    private void buildClaimIssued(EventHistory.EventHistoryBuilder builder, CaseData caseData) {
        if (rpaEnabledForClaim(caseData)) {
            String miscText = "Claim issued in CCD.";
            builder.miscellaneous(
                Event.builder()
                    .eventSequence(prepareEventSequence(builder.build()))
                    .eventCode(MISCELLANEOUS.getCode())
                    .dateReceived(caseData.getIssueDate().atStartOfDay())
                    .eventDetailsText(miscText)
                    .eventDetails(EventDetails.builder()
                                      .miscText(miscText)
                                      .build())
                    .build());
        }
    }

    private void buildClaimTakenOfflinePastApplicantResponse(EventHistory.EventHistoryBuilder builder,
                                                             CaseData caseData) {
        String detailsText = "RPA Reason: Claim dismissed after no response from applicant past response deadline.";
        builder.miscellaneous(
            Event.builder()
                .eventSequence(prepareEventSequence(builder.build()))
                .eventCode(MISCELLANEOUS.getCode())
                .dateReceived(caseData.getTakenOfflineDate())
                .eventDetailsText(detailsText)
                .eventDetails(EventDetails.builder()
                                  .miscText(detailsText)
                                  .build())
                .build());
    }

    private void buildClaimDismissedPastNotificationsDeadline(EventHistory.EventHistoryBuilder builder,
                                                              CaseData caseData, String miscText) {
        builder.miscellaneous(
            Event.builder()
                .eventSequence(prepareEventSequence(builder.build()))
                .eventCode(MISCELLANEOUS.getCode())
                .dateReceived(caseData.getClaimDismissedDate())
                .eventDetailsText(miscText)
                .eventDetails(EventDetails.builder()
                                  .miscText(miscText)
                                  .build())
                .build());
    }

    private void buildClaimDismissedPastDeadline(EventHistory.EventHistoryBuilder builder,
                                                 CaseData caseData, List<State> stateHistory) {
        State previousState = getPreviousState(stateHistory);
        FlowState.Main flowState = (FlowState.Main) FlowState.fromFullName(previousState.getName());
        builder.miscellaneous(
            Event.builder()
                .eventSequence(prepareEventSequence(builder.build()))
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
            case CLAIM_NOTIFIED:
            case CLAIM_DETAILS_NOTIFIED:
                return "RPA Reason: Claim dismissed after no response from defendant after claimant sent notification.";
            case NOTIFICATION_ACKNOWLEDGED:
            case NOTIFICATION_ACKNOWLEDGED_TIME_EXTENSION:
            case CLAIM_DETAILS_NOTIFIED_TIME_EXTENSION:
            case PAST_CLAIM_DISMISSED_DEADLINE_AWAITING_CAMUNDA:
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

    private void buildTakenOfflineByStaff(EventHistory.EventHistoryBuilder builder, CaseData caseData) {
        builder.miscellaneous(
            Event.builder()
                .eventSequence(prepareEventSequence(builder.build()))
                .eventCode(MISCELLANEOUS.getCode())
                .dateReceived(caseData.getTakenOfflineByStaffDate())
                .eventDetailsText(prepareTakenOfflineEventDetails(caseData))
                .eventDetails(EventDetails.builder()
                                  .miscText(prepareTakenOfflineEventDetails(caseData))
                                  .build())
                .build());
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
        return left(format(
            "RPA Reason: Manually moved offline for reason %s on date %s.",
            prepareTakenOfflineByStaffReason(caseData.getClaimProceedsInCaseman()),
            caseData.getClaimProceedsInCaseman().getDate().format(ISO_DATE)
        ), 250); // Max chars allowed by Caseman
    }

    private String prepareTakenOfflineByStaffReason(ClaimProceedsInCaseman claimProceedsInCaseman) {
        if (claimProceedsInCaseman.getReason() == ReasonForProceedingOnPaper.OTHER) {
            return claimProceedsInCaseman.getOther();
        }
        return claimProceedsInCaseman.getReason().name();
    }

    private void buildClaimantHasNotifiedDefendant(EventHistory.EventHistoryBuilder builder, CaseData caseData) {
        builder.miscellaneous(
            Event.builder()
                .eventSequence((prepareEventSequence(builder.build())))
                .eventCode(MISCELLANEOUS.getCode())
                .dateReceived(caseData.getClaimNotificationDate())
                .eventDetailsText("Claimant has notified defendant.")
                .eventDetails(EventDetails.builder()
                                  .miscText("Claimant has notified defendant.")
                                  .build())
                .build());
    }

    private void buildTakenOfflineAfterClaimNotified(EventHistory.EventHistoryBuilder builder, CaseData caseData) {
        builder.miscellaneous(
            List.of(
                Event.builder()
                    .eventSequence(prepareEventSequence(builder.build()))
                    .eventCode(MISCELLANEOUS.getCode())
                    .dateReceived(caseData.getSubmittedDate())
                    .eventDetailsText("RPA Reason: Only one of the respondent is notified.")
                    .eventDetails(EventDetails.builder()
                                      .miscText("RPA Reason: Only one of the respondent is notified.")
                                      .build())
                    .build()
            ));
    }

    private void buildFullDefenceProceed(EventHistory.EventHistoryBuilder builder, CaseData caseData) {
        List<ClaimantResponseDetails> applicantDetails = prepareApplicantsDetails(caseData);
        List<String> miscEventText = prepMultipartyProceedMiscText(caseData);

        List<Event> replyDefenceForProceedingApplicants = IntStream.range(0, applicantDetails.size())
            .mapToObj(index ->
                          Event.builder()
                              .eventSequence(prepareEventSequence(builder.build()))
                              .eventCode(REPLY_TO_DEFENCE.getCode())
                              .dateReceived(applicantDetails.get(index).getResponseDate())
                              .litigiousPartyID(applicantDetails.get(index).getLitigiousPartyID())
                              .build())
            .collect(Collectors.toList());
        builder.replyToDefence(replyDefenceForProceedingApplicants);

        if (SPEC_CLAIM.equals(caseData.getSuperClaimType())) {
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
                .collect(Collectors.toList());
            builder.directionsQuestionnaireFiled(dqForProceedingApplicantsSpec);
        } else {
            List<Event> dqForProceedingApplicants = IntStream.range(0, applicantDetails.size())
                .mapToObj(index ->
                              Event.builder()
                                  .eventSequence(prepareEventSequence(builder.build()))
                                  .eventCode(DIRECTIONS_QUESTIONNAIRE_FILED.getCode())
                                  .dateReceived(applicantDetails.get(index).getResponseDate())
                                  .litigiousPartyID(applicantDetails.get(index).getLitigiousPartyID())
                                  .eventDetails(EventDetails.builder()
                                                    .stayClaim(isStayClaim(applicantDetails.get(index).getDq()))
                                                    .preferredCourtCode(caseData.getCourtLocation()
                                                                            .getApplicantPreferredCourt())
                                                    .preferredCourtName("")
                                                    .build())
                                  .eventDetailsText(prepareEventDetailsText(
                                      applicantDetails.get(index).getDq(),
                                      caseData.getCourtLocation().getApplicantPreferredCourt()
                                  ))
                                  .build())
                .collect(Collectors.toList());
            builder.directionsQuestionnaireFiled(dqForProceedingApplicants);
        }

        List<Event> miscText = IntStream.range(0, miscEventText.size())
            .mapToObj(index ->
                          Event.builder()
                              .eventSequence(prepareEventSequence(builder.build()))
                              .eventCode(MISCELLANEOUS.getCode())
                              .dateReceived(caseData.getApplicant1ResponseDate())
                              .eventDetailsText(miscEventText.get(index))
                              .eventDetails(EventDetails.builder()
                                                .miscText(miscEventText.get(index))
                                                .build())
                              .build())
            .collect(Collectors.toList());
        builder.miscellaneous(miscText);
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
                                              SPEC_CLAIM.equals(caseData.getSuperClaimType())
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

    private List<String> prepMultipartyProceedMiscText(CaseData caseData) {
        List<String> eventDetailsText = new ArrayList<>();
        String currentTime = time.now().toLocalDate().toString();

        switch (getMultiPartyScenario(caseData)) {
            case ONE_V_TWO_ONE_LEGAL_REP:
            case ONE_V_TWO_TWO_LEGAL_REP: {
                eventDetailsText.add(String.format(
                    "RPA Reason: [1 of 2 - %s] Claimant has provided intention: %s against defendant: %s",
                    currentTime,
                    YES.equals(caseData.getApplicant1ProceedWithClaimAgainstRespondent1MultiParty1v2())
                        ? "proceed"
                        : "not proceed",
                    caseData.getRespondent1().getPartyName()
                ));
                eventDetailsText.add(String.format(
                    "RPA Reason: [2 of 2 - %s] Claimant has provided intention: %s against defendant: %s",
                    currentTime,
                    YES.equals(caseData.getApplicant1ProceedWithClaimAgainstRespondent2MultiParty1v2())
                        ? "proceed"
                        : "not proceed",
                    caseData.getRespondent2().getPartyName()
                ));
                break;
            }
            case TWO_V_ONE: {
                YesOrNo app1Proceeds;
                YesOrNo app2Proceeds;
                if (SPEC_CLAIM.equals(caseData.getSuperClaimType())) {
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
                        ? "proceed"
                        : "not proceed"
                ));
                eventDetailsText.add(String.format(
                    "RPA Reason: [2 of 2 - %s] Claimant: %s has provided intention: %s",
                    currentTime,
                    caseData.getApplicant2().getPartyName(),
                    YES.equals(app2Proceeds)
                        ? "proceed"
                        : "not proceed"
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

    public String prepareEventDetailsText(DQ dq, String preferredCourtCode) {
        return format(
            "preferredCourtCode: %s; stayClaim: %s",
            preferredCourtCode,
            isStayClaim(dq)
        );
    }

    public boolean isStayClaim(DQ dq) {
        return ofNullable(dq.getFileDirectionQuestionnaire())
            .map(FileDirectionsQuestionnaire::getOneMonthStayRequested)
            .orElse(NO) == YES;
    }

    public String getPreferredCourtCode(DQ dq) {
        return ofNullable(dq.getRequestedCourt())
            .map(RequestedCourt::getResponseCourtCode)
            .orElse("");
    }

    private void buildFullDefenceNotProceed(EventHistory.EventHistoryBuilder builder, CaseData caseData) {
        String miscText = getMultiPartyScenario(caseData).equals(TWO_V_ONE)
            ? "RPA Reason: Claimants intend not to proceed."
            : "RPA Reason: Claimant intends not to proceed.";

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
                Respondent2DQ respondent2DQ = caseData.getRespondent2DQ();
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
            time.now().toLocalDate().toString()
        );
    }

    private void buildUnrepresentedDefendant(EventHistory.EventHistoryBuilder builder, CaseData caseData) {
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
                    .eventSequence(prepareEventSequence(builder.build()))
                    .eventCode(MISCELLANEOUS.getCode())
                    .dateReceived(caseData.getSubmittedDate())
                    .eventDetailsText(eventText)
                    .eventDetails(EventDetails.builder().miscText(eventText).build())
                    .build();
            })
            .collect(Collectors.toList());
        builder.miscellaneous(events);
    }

    private void buildOfflineAfterClaimsDetailsNotified(EventHistory.EventHistoryBuilder builder, CaseData caseData) {
        builder.miscellaneous(
            List.of(
                Event.builder()
                    .eventSequence(prepareEventSequence(builder.build()))
                    .eventCode(MISCELLANEOUS.getCode())
                    .dateReceived(caseData.getSubmittedDate())
                    .eventDetailsText("RPA Reason: Only one of the respondent is notified.")
                    .eventDetails(EventDetails.builder()
                                      .miscText("RPA Reason: Only one of the respondent is notified.")
                                      .build())
                    .build()
            ));
    }

    private void buildUnregisteredDefendant(EventHistory.EventHistoryBuilder builder, CaseData caseData) {
        List<String> unregisteredDefendantsNames;

        // NOC: Revert after NOC is live
        if (featureToggleService.isNoticeOfChangeEnabled()) {
            unregisteredDefendantsNames = getDefendantNames(UNREGISTERED_NOTICE_OF_CHANGE, caseData);
        } else {
            unregisteredDefendantsNames = getDefendantNames(UNREGISTERED, caseData);
        }

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
                    .eventSequence(prepareEventSequence(builder.build()))
                    .eventCode(MISCELLANEOUS.getCode())
                    .dateReceived(caseData.getSubmittedDate())
                    .eventDetailsText(eventText)
                    .eventDetails(EventDetails.builder().miscText(eventText).build())
                    .build();
            })
            .collect(Collectors.toList());
        builder.miscellaneous(events);
    }

    private void buildUnregisteredAndUnrepresentedDefendant(EventHistory.EventHistoryBuilder builder,
                                                            CaseData caseData) {
        String localDateTime = time.now().toLocalDate().toString();

        // NOC: Revert after NOC is live
        List<String> unregisteredDefendantsNames;
        if (featureToggleService.isNoticeOfChangeEnabled()) {
            unregisteredDefendantsNames = getDefendantNames(UNREGISTERED_NOTICE_OF_CHANGE, caseData);
        } else {
            unregisteredDefendantsNames = getDefendantNames(UNREGISTERED, caseData);
        }

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

        builder.miscellaneous(
            List.of(
                Event.builder()
                    .eventSequence(prepareEventSequence(builder.build()))
                    .eventCode(MISCELLANEOUS.getCode())
                    .dateReceived(caseData.getSubmittedDate())
                    .eventDetailsText(unrepresentedEventText)
                    .eventDetails(EventDetails.builder().miscText(unrepresentedEventText).build())
                    .build(),
                Event.builder()
                    .eventSequence(prepareEventSequence(builder.build()))
                    .eventCode(MISCELLANEOUS.getCode())
                    .dateReceived(caseData.getSubmittedDate())
                    .eventDetailsText(unregisteredEventText)
                    .eventDetails(EventDetails.builder().miscText(unregisteredEventText).build())
                    .build()
            ));
    }

    private void buildAcknowledgementOfServiceReceived(EventHistory.EventHistoryBuilder builder, CaseData caseData) {
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

                builder.acknowledgementOfServiceReceived(events);
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
                                    caseData.getRespondent2ClaimResponseIntentionType().getLabel()
                                )
                            )
                        ));
                break;
            }
            default: {
                if (caseData.getSuperClaimType() != null && caseData.getSuperClaimType().equals(SPEC_CLAIM)) {
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
                            SPEC_CLAIM.equals(caseData.getSuperClaimType())
                                ?
                                Event.builder()
                                    .eventSequence(prepareEventSequence(builder.build()))
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

    private Event buildAcknowledgementOfServiceEvent(EventHistory.EventHistoryBuilder builder, CaseData caseData,
                                                     boolean isRespondent1, String eventDetailsText) {
        return Event.builder()
            .eventSequence(prepareEventSequence(builder.build()))
            .eventCode(ACKNOWLEDGEMENT_OF_SERVICE_RECEIVED.getCode())
            .dateReceived(isRespondent1
                              ? caseData.getRespondent1AcknowledgeNotificationDate()
                              : caseData.getRespondent2AcknowledgeNotificationDate())
            .litigiousPartyID(isRespondent1 ? "002" : "003")
            .eventDetails(EventDetails.builder()
                              .responseIntention(
                                  isRespondent1
                                      ? caseData.getRespondent1ClaimResponseIntentionType().getLabel()
                                      : caseData.getRespondent2ClaimResponseIntentionType().getLabel())
                              .build())
            .eventDetailsText(eventDetailsText)
            .build();
    }

    private void buildAcknowledgementOfServiceSpec(EventHistory.EventHistoryBuilder builder,
                                                   LocalDateTime dateAcknowledge) {
        builder
            .acknowledgementOfServiceReceived(
                List.of(Event.builder()
                            .eventSequence(prepareEventSequence(builder.build()))
                            .eventCode("38")
                            .dateReceived(dateAcknowledge)
                            .litigiousPartyID("002")
                            .eventDetails(EventDetails.builder()
                                              .acknowledgeService("Acknowledgement of Service")
                                              .build())
                            .eventDetailsText("Defendant LR Acknowledgement of Service ")
                            .build()));
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
            ).miscellaneous(Event.builder()
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
                builder.receiptOfAdmission(Event.builder()
                                               .eventSequence(prepareEventSequence(builder.build()))
                                               .eventCode(RECEIPT_OF_ADMISSION.getCode())
                                               .dateReceived(respondent2ResponseDate)
                                               .litigiousPartyID(RESPONDENT2_ID)
                                               .build()
                ).miscellaneous(Event.builder()
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
            builder.receiptOfAdmission(Event.builder()
                                           .eventSequence(prepareEventSequence(builder.build()))
                                           .eventCode(RECEIPT_OF_ADMISSION.getCode())
                                           .dateReceived(caseData.getRespondent2ResponseDate())
                                           .litigiousPartyID(RESPONDENT2_ID)
                                           .build()
            ).miscellaneous(Event.builder()
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

    private void buildRespondentPartAdmission(EventHistory.EventHistoryBuilder builder, CaseData caseData) {
        String miscText;
        if (defendant1ResponseExists.test(caseData)) {
            miscText = prepareRespondentResponseText(caseData, caseData.getRespondent1(), true);
            builder.receiptOfPartAdmission(
                Event.builder()
                    .eventSequence(prepareEventSequence(builder.build()))
                    .eventCode(RECEIPT_OF_PART_ADMISSION.getCode())
                    .dateReceived(caseData.getRespondent1ResponseDate())
                    .litigiousPartyID(RESPONDENT_ID)
                    .build()
            ).miscellaneous(Event.builder()
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
                builder.receiptOfPartAdmission(
                    Event.builder()
                        .eventSequence(prepareEventSequence(builder.build()))
                        .eventCode(RECEIPT_OF_PART_ADMISSION.getCode())
                        .dateReceived(respondent2ResponseDate)
                        .litigiousPartyID(RESPONDENT2_ID)
                        .build()
                ).miscellaneous(Event.builder()
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
            builder.receiptOfPartAdmission(
                Event.builder()
                    .eventSequence(prepareEventSequence(builder.build()))
                    .eventCode(RECEIPT_OF_PART_ADMISSION.getCode())
                    .dateReceived(caseData.getRespondent2ResponseDate())
                    .litigiousPartyID(RESPONDENT2_ID)
                    .build()
            ).miscellaneous(Event.builder()
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

    private void buildRespondentCounterClaim(EventHistory.EventHistoryBuilder builder, CaseData caseData) {
        String miscText;
        if (defendant1ResponseExists.test(caseData)) {
            miscText = prepareRespondentResponseText(caseData, caseData.getRespondent1(), true);
            builder.defenceAndCounterClaim(
                Event.builder()
                    .eventSequence(prepareEventSequence(builder.build()))
                    .eventCode(DEFENCE_AND_COUNTER_CLAIM.getCode())
                    .dateReceived(caseData.getRespondent1ResponseDate())
                    .litigiousPartyID(RESPONDENT_ID)
                    .build()
            ).miscellaneous(Event.builder()
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
                builder.defenceAndCounterClaim(
                    List.of(
                        Event.builder()
                            .eventSequence(prepareEventSequence(builder.build()))
                            .eventCode(DEFENCE_AND_COUNTER_CLAIM.getCode())
                            .dateReceived(respondent2ResponseDate)
                            .litigiousPartyID(RESPONDENT2_ID)
                            .build()
                    )
                ).miscellaneous(Event.builder()
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
            builder.defenceAndCounterClaim(
                List.of(
                    Event.builder()
                        .eventSequence(prepareEventSequence(builder.build()))
                        .eventCode(DEFENCE_AND_COUNTER_CLAIM.getCode())
                        .dateReceived(caseData.getRespondent2ResponseDate())
                        .litigiousPartyID(RESPONDENT2_ID)
                        .build()
                )
            ).miscellaneous(Event.builder()
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
        if (SPEC_CLAIM.equals(caseData.getSuperClaimType())
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
}
