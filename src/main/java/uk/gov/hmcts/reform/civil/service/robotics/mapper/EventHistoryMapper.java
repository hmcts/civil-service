package uk.gov.hmcts.reform.civil.service.robotics.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.enums.ReasonForProceedingOnPaper;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseType;
import uk.gov.hmcts.reform.civil.launchdarkly.FeatureToggleService;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.ClaimProceedsInCaseman;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.dq.DQ;
import uk.gov.hmcts.reform.civil.model.dq.FileDirectionsQuestionnaire;
import uk.gov.hmcts.reform.civil.model.dq.RequestedCourt;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Respondent2DQ;
import uk.gov.hmcts.reform.civil.model.robotics.Event;
import uk.gov.hmcts.reform.civil.model.robotics.EventDetails;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;
import uk.gov.hmcts.reform.civil.service.flowstate.StateFlowEngine;
import uk.gov.hmcts.reform.civil.stateflow.model.State;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.lang.String.format;
import static java.time.format.DateTimeFormatter.ISO_DATE;
import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.left;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.enums.SuperClaimType.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.UnrepresentedOrUnregisteredScenario.UNREGISTERED;
import static uk.gov.hmcts.reform.civil.enums.UnrepresentedOrUnregisteredScenario.UNREPRESENTED;
import static uk.gov.hmcts.reform.civil.enums.UnrepresentedOrUnregisteredScenario.getDefendantNames;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.model.robotics.EventType.ACKNOWLEDGEMENT_OF_SERVICE_RECEIVED;
import static uk.gov.hmcts.reform.civil.model.robotics.EventType.CONSENT_EXTENSION_FILING_DEFENCE;
import static uk.gov.hmcts.reform.civil.model.robotics.EventType.DEFENCE_AND_COUNTER_CLAIM;
import static uk.gov.hmcts.reform.civil.model.robotics.EventType.DEFENCE_FILED;
import static uk.gov.hmcts.reform.civil.model.robotics.EventType.DIRECTIONS_QUESTIONNAIRE_FILED;
import static uk.gov.hmcts.reform.civil.model.robotics.EventType.MISCELLANEOUS;
import static uk.gov.hmcts.reform.civil.model.robotics.EventType.RECEIPT_OF_ADMISSION;
import static uk.gov.hmcts.reform.civil.model.robotics.EventType.RECEIPT_OF_PART_ADMISSION;
import static uk.gov.hmcts.reform.civil.model.robotics.EventType.REPLY_TO_DEFENCE;
import static uk.gov.hmcts.reform.civil.service.robotics.mapper.RoboticsDataMapper.APPLICANT_ID;
import static uk.gov.hmcts.reform.civil.service.robotics.mapper.RoboticsDataMapper.RESPONDENT2_ID;
import static uk.gov.hmcts.reform.civil.service.robotics.mapper.RoboticsDataMapper.RESPONDENT_ID;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.unwrapElements;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getResponseTypeForRespondent;

@Component
@RequiredArgsConstructor
public class EventHistoryMapper {

    private final StateFlowEngine stateFlowEngine;
    private final FeatureToggleService featureToggleService;
    private final EventHistorySequencer eventHistorySequencer;
    private final Time time;

    public EventHistory buildEvents(CaseData caseData) {
        List<State> states = null;
        EventHistory.EventHistoryBuilder builder = EventHistory.builder()
            .directionsQuestionnaireFiled(List.of(Event.builder().build()));
        states = stateFlowEngine.evaluate(caseData).getStateHistory();

        List<State> stateHistory = states;
        stateHistory
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
                    case FULL_ADMISSION:
                        buildRespondentFullAdmission(builder, caseData);
                        break;
                    case PART_ADMISSION:
                        buildRespondentPartAdmission(builder, caseData);
                        break;
                    case COUNTER_CLAIM:
                        buildRespondentCounterClaim(builder, caseData);
                        break;
                    case FULL_DEFENCE:
                        buildRespondentFullDefence(builder, caseData);
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
                        buildClaimDismissedPastDeadline(builder, caseData, stateHistory);
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
        return eventHistorySequencer.sortEvents(builder.build());
    }

    private void buildCaseNotesEvents(EventHistory.EventHistoryBuilder builder, CaseData caseData) {
        if (featureToggleService.isRpaContinuousFeedEnabled() && isNotEmpty(caseData.getCaseNotes())) {
            List<Event> events = unwrapElements(caseData.getCaseNotes())
                .stream()
                .map(caseNote ->
                         Event.builder()
                             .eventSequence(prepareEventSequence(builder.build()))
                             .eventCode("999")
                             .dateReceived(caseNote.getCreatedOn().atStartOfDay())
                             .eventDetailsText(left((format("case note added: %s", caseNote.getNote())), 250))
                             .eventDetails(EventDetails.builder()
                                               .miscText(left((format("case note added: %s", caseNote.getNote())), 250))
                                               .build())
                             .build())
                .collect(Collectors.toList());
            builder.miscellaneous(events);
        }
    }

    private void buildRespondent1LitigationFriendEvent(EventHistory.EventHistoryBuilder builder, CaseData caseData) {
        if (featureToggleService.isRpaContinuousFeedEnabled()
            && caseData.getRespondent1LitigationFriendCreatedDate() != null) {
            String miscText = "Litigation friend added for respondent.";
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
    }

    private void buildRespondent2LitigationFriendEvent(EventHistory.EventHistoryBuilder builder, CaseData caseData) {
        if (featureToggleService.isRpaContinuousFeedEnabled()
            && caseData.getRespondent2LitigationFriendCreatedDate() != null) {
            String miscText = "Litigation friend added for respondent.";
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

    private void buildClaimIssued(EventHistory.EventHistoryBuilder builder, CaseData caseData) {
        if (featureToggleService.isRpaContinuousFeedEnabled()) {
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
        builder.replyToDefence(List.of(
            Event.builder()
                .eventSequence(prepareEventSequence(builder.build()))
                .eventCode(REPLY_TO_DEFENCE.getCode())
                .dateReceived(caseData.getApplicant1ResponseDate())
                .litigiousPartyID(APPLICANT_ID)
                .build())
        ).directionsQuestionnaire(
            Event.builder()
                .eventSequence(prepareEventSequence(builder.build()))
                .eventCode(DIRECTIONS_QUESTIONNAIRE_FILED.getCode())
                .dateReceived(caseData.getApplicant1ResponseDate())
                .litigiousPartyID(APPLICANT_ID)
                .eventDetails(EventDetails.builder()
                                  .stayClaim(isStayClaim(caseData.getApplicant1DQ()))
                                  .preferredCourtCode(caseData.getCourtLocation().getApplicantPreferredCourt())
                                  .preferredCourtName("")
                                  .build())
                .eventDetailsText(prepareEventDetailsText(
                    caseData.getApplicant1DQ(),
                    caseData.getCourtLocation().getApplicantPreferredCourt()
                ))
                .build()
        ).miscellaneous(Event.builder()
                            .eventSequence(prepareEventSequence(builder.build()))
                            .eventCode(MISCELLANEOUS.getCode())
                            .dateReceived(caseData.getApplicant1ResponseDate())
                            .eventDetailsText("RPA Reason: Applicant proceeds.")
                            .eventDetails(EventDetails.builder()
                                              .miscText("RPA Reason: Applicant proceeds.")
                                              .build())
                            .build());
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
        builder.miscellaneous(Event.builder()
                                  .eventSequence(prepareEventSequence(builder.build()))
                                  .eventCode(MISCELLANEOUS.getCode())
                                  .dateReceived(caseData.getApplicant1ResponseDate())
                                  .eventDetailsText("RPA Reason: Claimant intends not to proceed.")
                                  .eventDetails(EventDetails.builder()
                                                    .miscText("RPA Reason: Claimant intends not to proceed.")
                                                    .build())
                                  .build());
    }

    private void buildRespondentFullDefence(EventHistory.EventHistoryBuilder builder, CaseData caseData) {
        List<Event> defenceFiledEvents = new ArrayList<>();
        List<Event> directionsQuestionnaireFiledEvents = new ArrayList<>();
        boolean isRespondent1;
        if (caseData.getRespondent1ResponseDate() != null) {
            isRespondent1 = true;
            Party respondent1 = caseData.getRespondent1();
            Respondent1DQ respondent1DQ = caseData.getRespondent1DQ();
            LocalDateTime respondent1ResponseDate = caseData.getRespondent1ResponseDate();

            defenceFiledEvents.add(
                buildDefenceFiledEvent(builder,
                                       respondent1ResponseDate,
                                       RESPONDENT_ID));
            directionsQuestionnaireFiledEvents.add(
                buildDirectionsQuestionnaireFiledEvent(builder, caseData,
                                                       respondent1ResponseDate,
                                                       RESPONDENT_ID,
                                                       respondent1DQ,
                                                       respondent1,
                                                       isRespondent1));
        }
        if (caseData.getRespondent2() != null && caseData.getRespondent2ResponseDate() != null) {
            isRespondent1 = false;
            Party respondent2 = caseData.getRespondent2();
            Respondent2DQ respondent2DQ = caseData.getRespondent2DQ();
            LocalDateTime respondent2ResponseDate = caseData.getRespondent2ResponseDate();

            defenceFiledEvents.add(
                buildDefenceFiledEvent(builder,
                                       respondent2ResponseDate,
                                       RESPONDENT2_ID));
            directionsQuestionnaireFiledEvents.add(
                buildDirectionsQuestionnaireFiledEvent(builder, caseData,
                                                       respondent2ResponseDate,
                                                       RESPONDENT2_ID,
                                                       respondent2DQ,
                                                       respondent2,
                                                       isRespondent1));
        }
        builder.defenceFiled(defenceFiledEvents);
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
                                         String litigiousPartyID) {
        return Event.builder()
            .eventSequence(prepareEventSequence(builder.build()))
            .eventCode(DEFENCE_FILED.getCode())
            .dateReceived(respondentResponseDate)
            .litigiousPartyID(litigiousPartyID)
            .build();
    }

    public String prepareFullDefenceEventText(DQ dq, CaseData caseData, boolean isRespondent1, Party respondent) {
        String defaultText;
        MultiPartyScenario scenario = getMultiPartyScenario(caseData);
        switch (scenario) {
            case ONE_V_TWO_ONE_LEGAL_REP:
                String paginatedMessage = getPaginatedMessageIndexFor1v2SameSolicitor(caseData, isRespondent1);
                defaultText = (format(
                    "RPA Reason:%s Defendant: %s has responded: %s; "
                        + "preferredCourtCode: %s; stayClaim: %s",
                    paginatedMessage,
                    respondent.getPartyName(),
                    getResponseTypeForRespondent(caseData, respondent),
                    getPreferredCourtCode(dq),
                    isStayClaim(dq)
                ));
                break;
            case ONE_V_TWO_TWO_LEGAL_REP:
                defaultText = format("RPA Reason: Defendant: %s has responded: %s; preferredCourtCode: %s; "
                                         + "stayClaim: %s",
                                     respondent.getPartyName(),
                                     getResponseTypeForRespondent(caseData, respondent),
                                     getPreferredCourtCode(dq),
                                     isStayClaim(dq)
                );
                break;
            default:
                defaultText = format(
                "RPA Reason: preferredCourtCode: %s; stayClaim: %s",
                getPreferredCourtCode(dq),
                isStayClaim(dq)
            );
        }
        return defaultText;
    }

    // Index 1 if respondent 1 responds on or before respondent 2's response date
    private String getPaginatedMessageIndexFor1v2SameSolicitor(CaseData caseData, boolean isRespondent1) {
        int index = 1;
        LocalDateTime respondent1ResponseDate = caseData.getRespondent1ResponseDate();
        LocalDateTime respondent2ResponseDate = caseData.getRespondent2ResponseDate();
        if (respondent1ResponseDate != null && respondent2ResponseDate != null) {
            if (respondent1ResponseDate.isBefore(respondent2ResponseDate)
                || respondent1ResponseDate.isEqual(respondent2ResponseDate)) {
                index = isRespondent1 ? 1 : 2;
            } else {
                index = isRespondent1 ? 2 : 1;
            }
        }
        return format(
            " [%d of 2 - %s] ",
            index,
            time.now().toLocalDate().toString()
        );
    }

    private void buildUnrepresentedDefendant(EventHistory.EventHistoryBuilder builder, CaseData caseData) {
        List<String> unrepresentedDefendantsNames = getDefendantNames(UNREPRESENTED, caseData);

        List<Event> events = IntStream.range(0, unrepresentedDefendantsNames.size())
            .mapToObj(index -> {
                String paginatedMessage = unrepresentedDefendantsNames.size() > 1
                    ? format("[%d of %d - %s] ",
                             index + 1,
                             unrepresentedDefendantsNames.size(),
                             time.now().toLocalDate().toString())
                    : "";
                String eventText = format("RPA Reason: %sUnrepresented defendant: %s",
                                          paginatedMessage,
                                          unrepresentedDefendantsNames.get(index));

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
        List<String> unregisteredDefendantsNames = getDefendantNames(UNREGISTERED, caseData);

        List<Event> events = IntStream.range(0, unregisteredDefendantsNames.size())
            .mapToObj(index -> {
                String paginatedMessage = unregisteredDefendantsNames.size() > 1
                    ? format("[%d of %d - %s] ",
                             index + 1,
                             unregisteredDefendantsNames.size(),
                             time.now().toLocalDate().toString())
                    : "";
                String eventText = format("RPA Reason: %sUnregistered defendant solicitor firm: %s",
                                          paginatedMessage,
                                          unregisteredDefendantsNames.get(index));

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

        String unrepresentedEventText = format("RPA Reason: [1 of 2 - %s] Unrepresented defendant and unregistered "
                                                   + "defendant solicitor firm. Unrepresented defendant: %s",
                                               localDateTime,
                                               getDefendantNames(UNREPRESENTED, caseData).get(0));
        String unregisteredEventText = format("RPA Reason: [2 of 2 - %s] Unrepresented defendant and unregistered "
                                                  + "defendant solicitor firm. Unregistered defendant solicitor "
                                                  + "firm: %s",
                                              localDateTime,
                                              getDefendantNames(UNREGISTERED, caseData).get(0));

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
        //TODO Defendant 2 will be handled under RPA ticket [CMC-1677]
        LocalDateTime dateAcknowledge = caseData.getRespondent1AcknowledgeNotificationDate();
        if (dateAcknowledge == null) {
            return;
        }
        builder
            .acknowledgementOfServiceReceived(
                List.of(
                    caseData.getSuperClaimType() != null && caseData.getSuperClaimType().equals(SPEC_CLAIM)
                        ?
                        Event.builder()
                            .eventSequence(prepareEventSequence(builder.build()))
                            .eventCode("38")
                            .dateReceived(dateAcknowledge)
                            .litigiousPartyID("002")
                            .eventDetails(EventDetails.builder()
                                              .acknowledgeService("Acknowledgement of Service")
                                              .build())
                            .eventDetailsText(format(
                                "Defendant LR Acknowledgement of Service "
                            ))
                            .build()
                        : Event.builder()
                        .eventSequence(prepareEventSequence(builder.build()))
                        .eventCode(ACKNOWLEDGEMENT_OF_SERVICE_RECEIVED.getCode())
                        .dateReceived(dateAcknowledge)
                        .litigiousPartyID("002")
                        .eventDetails(EventDetails.builder()
                                          .responseIntention(caseData.getRespondent1ClaimResponseIntentionType()
                                                                 .getLabel())
                                          .build())
                        .eventDetailsText(format(
                            "responseIntention: %s",
                            caseData.getRespondent1ClaimResponseIntentionType().getLabel()
                        ))
                        .build()
                ));
    }

    private void buildRespondentFullAdmission(EventHistory.EventHistoryBuilder builder, CaseData caseData) {
        builder.receiptOfAdmission(List.of(Event.builder()
                                               .eventSequence(prepareEventSequence(builder.build()))
                                               .eventCode(RECEIPT_OF_ADMISSION.getCode())
                                               .dateReceived(caseData.getRespondent1ResponseDate())
                                               .litigiousPartyID("002")
                                               .build())
        ).miscellaneous(Event.builder()
                            .eventSequence(prepareEventSequence(builder.build()))
                            .eventCode(MISCELLANEOUS.getCode())
                            .dateReceived(caseData.getRespondent1ResponseDate())
                            .eventDetailsText("RPA Reason: Defendant fully admits.")
                            .eventDetails(EventDetails.builder()
                                              .miscText("RPA Reason: Defendant fully admits.")
                                              .build())
                            .build());
    }

    private void buildRespondentPartAdmission(EventHistory.EventHistoryBuilder builder, CaseData caseData) {
        builder.receiptOfPartAdmission(
            List.of(
                Event.builder()
                    .eventSequence(prepareEventSequence(builder.build()))
                    .eventCode(RECEIPT_OF_PART_ADMISSION.getCode())
                    .dateReceived(caseData.getRespondent1ResponseDate())
                    .litigiousPartyID("002")
                    .build()
            )
        ).miscellaneous(Event.builder()
                            .eventSequence(prepareEventSequence(builder.build()))
                            .eventCode(MISCELLANEOUS.getCode())
                            .dateReceived(caseData.getRespondent1ResponseDate())
                            .eventDetailsText("RPA Reason: Defendant partial admission.")
                            .eventDetails(EventDetails.builder()
                                              .miscText("RPA Reason: Defendant partial admission.")
                                              .build())
                            .build());
    }

    private void buildRespondentCounterClaim(EventHistory.EventHistoryBuilder builder, CaseData caseData) {
        builder.defenceAndCounterClaim(
            List.of(
                Event.builder()
                    .eventSequence(prepareEventSequence(builder.build()))
                    .eventCode(DEFENCE_AND_COUNTER_CLAIM.getCode())
                    .dateReceived(caseData.getRespondent1ResponseDate())
                    .litigiousPartyID("002")
                    .build()
            )
        ).miscellaneous(Event.builder()
                            .eventSequence(prepareEventSequence(builder.build()))
                            .eventCode(MISCELLANEOUS.getCode())
                            .dateReceived(caseData.getRespondent1ResponseDate())
                            .eventDetailsText("RPA Reason: Defendant rejects and counter claims.")
                            .eventDetails(EventDetails.builder()
                                              .miscText("RPA Reason: Defendant rejects and counter claims.")
                                              .build())
                            .build());
    }

    private void buildConsentExtensionFilingDefence(EventHistory.EventHistoryBuilder builder, CaseData caseData) {
        LocalDateTime dateReceived = caseData.getRespondent1TimeExtensionDate();
        if (dateReceived == null) {
            return;
        }
        // date and time check to find the login
        LocalDate extensionDate = caseData.getRespondentSolicitor1AgreedDeadlineExtension();

        //finding extension date for the correct respondent in a 1v2 different solicitor scenario
        MultiPartyScenario multiPartyScenario = getMultiPartyScenario(caseData);
        if (multiPartyScenario == ONE_V_TWO_TWO_LEGAL_REP) {
            if ((caseData.getRespondent1TimeExtensionDate() == null)
                && (caseData.getRespondent2TimeExtensionDate() != null)) {
                extensionDate = caseData.getRespondentSolicitor2AgreedDeadlineExtension();
                dateReceived  = caseData.getRespondent2TimeExtensionDate();
            } else if ((caseData.getRespondent1TimeExtensionDate() != null)
                && (caseData.getRespondent2TimeExtensionDate() != null)) {
                if (caseData.getRespondent2TimeExtensionDate()
                    .isAfter(caseData.getRespondent1TimeExtensionDate())) {
                    extensionDate = caseData.getRespondentSolicitor2AgreedDeadlineExtension();
                    dateReceived  = caseData.getRespondent2TimeExtensionDate();
                } else {
                    extensionDate = caseData.getRespondentSolicitor1AgreedDeadlineExtension();
                    dateReceived  = caseData.getRespondent1TimeExtensionDate();
                }
            }
        }

        builder.consentExtensionFilingDefence(
            List.of(
                Event.builder()
                    .eventSequence(prepareEventSequence(builder.build()))
                    .eventCode(CONSENT_EXTENSION_FILING_DEFENCE.getCode())
                    .dateReceived(dateReceived)
                    .litigiousPartyID("002")
                    .eventDetailsText(
                        //format("agreed extension date: %s", extensionDate.format(ISO_DATE)))
                        format("agreed extension date: %s",
                               extensionDate.format(DateTimeFormatter.ofPattern("dd MM yyyy")))
                    )
                    .eventDetails(
                        EventDetails.builder()
                            .agreedExtensionDate(extensionDate.format(ISO_DATE))
                            .build()
                    )
                    .build()
            )
        );
    }
}
