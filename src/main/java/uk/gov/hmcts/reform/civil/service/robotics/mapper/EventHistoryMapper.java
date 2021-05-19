package uk.gov.hmcts.reform.civil.service.robotics.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.ReasonForProceedingOnPaper;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.ClaimProceedsInCaseman;
import uk.gov.hmcts.reform.civil.model.dq.DQ;
import uk.gov.hmcts.reform.civil.model.dq.FileDirectionsQuestionnaire;
import uk.gov.hmcts.reform.civil.model.dq.RequestedCourt;
import uk.gov.hmcts.reform.civil.model.robotics.Event;
import uk.gov.hmcts.reform.civil.model.robotics.EventDetails;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;
import uk.gov.hmcts.reform.civil.service.flowstate.StateFlowEngine;
import uk.gov.hmcts.reform.civil.stateflow.model.State;

import java.time.LocalDateTime;
import java.util.List;

import static java.lang.String.format;
import static java.time.format.DateTimeFormatter.ISO_DATE;
import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.service.robotics.mapper.RoboticsDataMapper.APPLICANT_ID;
import static uk.gov.hmcts.reform.civil.service.robotics.mapper.RoboticsDataMapper.RESPONDENT_ID;

@Component
@RequiredArgsConstructor
public class EventHistoryMapper {

    private final StateFlowEngine stateFlowEngine;

    public EventHistory buildEvents(CaseData caseData) {
        EventHistory.EventHistoryBuilder builder = EventHistory.builder()
            .directionsQuestionnaireFiled(List.of(Event.builder().build()));

        List<State> stateHistory = stateFlowEngine.evaluate(caseData)
            .getStateHistory();
        stateHistory
            .forEach(state -> {
                FlowState.Main flowState = (FlowState.Main) FlowState.fromFullName(state.getName());
                switch (flowState) {
                    case TAKEN_OFFLINE_UNREPRESENTED_DEFENDANT:
                        buildUnrepresentedDefendant(builder, caseData);
                        break;
                    case TAKEN_OFFLINE_UNREGISTERED_DEFENDANT:
                        buildUnregisteredDefendant(builder, caseData);
                        break;
                    case CLAIM_NOTIFIED:
                        buildClaimantHasNotifiedDefendant(builder, caseData);
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
                    case TAKEN_OFFLINE_PAST_APPLICANT_RESPONSE_DEADLINE:
                        buildClaimTakenOfflinePastApplicantResponse(builder, caseData);
                        break;
                    default:
                        break;
                }
            });

        return builder.build();
    }

    private void buildClaimTakenOfflinePastApplicantResponse(EventHistory.EventHistoryBuilder builder,
                                                             CaseData caseData) {
        String detailsText = "RPA Reason: Claim dismissed after no response from applicant past response deadline.";
        builder.miscellaneous(
            Event.builder()
                .eventSequence(prepareEventSequence(builder.build()))
                .eventCode("999")
                .dateReceived(caseData.getTakenOfflineDate().format(ISO_DATE))
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
                .eventCode("999")
                .dateReceived(caseData.getClaimDismissedDate().format(ISO_DATE))
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
                .eventCode("999")
                .dateReceived(caseData.getClaimDismissedDate().format(ISO_DATE))
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
                .eventCode("999")
                .dateReceived(caseData.getTakenOfflineByStaffDate().format(ISO_DATE))
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
        return format(
            "RPA Reason: Manually moved offline for reason %s on date %s.",
            prepareTakenOfflineByStaffReason(caseData.getClaimProceedsInCaseman()),
            caseData.getClaimProceedsInCaseman().getDate().format(ISO_DATE)
        );
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
                .eventSequence(1)
                .eventCode("999")
                .dateReceived(caseData.getClaimNotificationDate().format(ISO_DATE))
                .eventDetailsText("Claimant has notified defendant.")
                .eventDetails(EventDetails.builder()
                                  .miscText("Claimant has notified defendant.")
                                  .build())
                .build());
    }

    private void buildFullDefenceProceed(EventHistory.EventHistoryBuilder builder, CaseData caseData) {
        builder.replyToDefence(List.of(
            Event.builder()
                .eventSequence(prepareEventSequence(builder.build()))
                .eventCode("66")
                .dateReceived(caseData.getApplicant1ResponseDate().format(ISO_DATE))
                .litigiousPartyID(APPLICANT_ID)
                .build())
        ).directionsQuestionnaire(
            Event.builder()
                .eventSequence(prepareEventSequence(builder.build()))
                .eventCode("197")
                .dateReceived(caseData.getApplicant1ResponseDate().format(ISO_DATE))
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
                            .eventCode("999")
                            .dateReceived(caseData.getApplicant1ResponseDate().format(ISO_DATE))
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
                                  .eventCode("999")
                                  .dateReceived(caseData.getApplicant1ResponseDate().format(ISO_DATE))
                                  .eventDetailsText("RPA Reason: Claimant intends not to proceed.")
                                  .eventDetails(EventDetails.builder()
                                                    .miscText("RPA Reason: Claimant intends not to proceed.")
                                                    .build())
                                  .build());
    }

    private void buildRespondentFullDefence(EventHistory.EventHistoryBuilder builder, CaseData caseData) {
        builder
            .defenceFiled(
                List.of(
                    Event.builder()
                        .eventSequence(prepareEventSequence(builder.build()))
                        .eventCode("50")
                        .dateReceived(caseData.getRespondent1ResponseDate().format(ISO_DATE))
                        .litigiousPartyID(RESPONDENT_ID)
                        .build()
                ))
            .clearDirectionsQuestionnaireFiled()
            .directionsQuestionnaire(
                Event.builder()
                    .eventSequence(prepareEventSequence(builder.build()))
                    .eventCode("197")
                    .dateReceived(caseData.getRespondent1ResponseDate().format(ISO_DATE))
                    .litigiousPartyID(RESPONDENT_ID)
                    .eventDetailsText(prepareEventDetailsText(
                        caseData.getRespondent1DQ(),
                        getPreferredCourtCode(caseData.getRespondent1DQ())
                    ))
                    .eventDetails(EventDetails.builder()
                                      .stayClaim(isStayClaim(caseData.getRespondent1DQ()))
                                      .preferredCourtCode(getPreferredCourtCode(caseData.getRespondent1DQ()))
                                      .preferredCourtName("")
                                      .build())
                    .build()
            );
    }

    private void buildUnrepresentedDefendant(EventHistory.EventHistoryBuilder builder, CaseData caseData) {
        builder.miscellaneous(
            List.of(
                Event.builder()
                    .eventSequence(1)
                    .eventCode("999")
                    .dateReceived(caseData.getSubmittedDate().toLocalDate().format(ISO_DATE))
                    .eventDetailsText("RPA Reason: Unrepresented defendant.")
                    .eventDetails(EventDetails.builder()
                                      .miscText("RPA Reason: Unrepresented defendant.")
                                      .build())
                    .build()
            ));
    }

    private void buildUnregisteredDefendant(EventHistory.EventHistoryBuilder builder, CaseData caseData) {
        builder.miscellaneous(
            List.of(
                Event.builder()
                    .eventSequence(1)
                    .eventCode("999")
                    .dateReceived(caseData.getSubmittedDate().toLocalDate().format(ISO_DATE))
                    .eventDetailsText("RPA Reason: Unregistered defendant solicitor firm.")
                    .eventDetails(EventDetails.builder()
                                      .miscText("RPA Reason: Unregistered defendant solicitor firm.")
                                      .build())
                    .build()
            ));
    }

    private void buildAcknowledgementOfServiceReceived(EventHistory.EventHistoryBuilder builder, CaseData caseData) {
        LocalDateTime dateAcknowledge = caseData.getRespondent1AcknowledgeNotificationDate();
        if (dateAcknowledge == null) {
            return;
        }
        builder
            .acknowledgementOfServiceReceived(
                List.of(
                    Event.builder()
                        .eventSequence(2)
                        .eventCode("38")
                        .dateReceived(dateAcknowledge.format(ISO_DATE))
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
                                               .eventCode("40")
                                               .dateReceived(caseData.getRespondent1ResponseDate().format(ISO_DATE))
                                               .litigiousPartyID("002")
                                               .build())
        ).miscellaneous(Event.builder()
                            .eventSequence(prepareEventSequence(builder.build()))
                            .eventCode("999")
                            .dateReceived(caseData.getRespondent1ResponseDate().format(ISO_DATE))
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
                    .eventCode("60")
                    .dateReceived(caseData.getRespondent1ResponseDate().format(ISO_DATE))
                    .litigiousPartyID("002")
                    .build()
            )
        ).miscellaneous(Event.builder()
                            .eventSequence(prepareEventSequence(builder.build()))
                            .eventCode("999")
                            .dateReceived(caseData.getRespondent1ResponseDate().format(ISO_DATE))
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
                    .eventCode("52")
                    .dateReceived(caseData.getRespondent1ResponseDate().format(ISO_DATE))
                    .litigiousPartyID("002")
                    .build()
            )
        ).miscellaneous(Event.builder()
                            .eventSequence(prepareEventSequence(builder.build()))
                            .eventCode("999")
                            .dateReceived(caseData.getRespondent1ResponseDate().format(ISO_DATE))
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
        builder.consentExtensionFilingDefence(
            List.of(
                Event.builder()
                    .eventSequence(prepareEventSequence(builder.build()))
                    .eventCode("45")
                    .dateReceived(dateReceived.format(ISO_DATE))
                    .litigiousPartyID("002")
                    .eventDetailsText(format("agreedExtensionDate: %s", caseData
                        .getRespondentSolicitor1AgreedDeadlineExtension()
                        .format(ISO_DATE)))
                    .eventDetails(EventDetails.builder()
                                      .agreedExtensionDate(caseData.getRespondentSolicitor1AgreedDeadlineExtension()
                                                               .format(ISO_DATE))
                                      .build())
                    .build()
            )
        );
    }
}
