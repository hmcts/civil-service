package uk.gov.hmcts.reform.unspec.service.robotics.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.unspec.model.CaseData;
import uk.gov.hmcts.reform.unspec.model.dq.DQ;
import uk.gov.hmcts.reform.unspec.model.dq.RequestedCourt;
import uk.gov.hmcts.reform.unspec.model.robotics.Event;
import uk.gov.hmcts.reform.unspec.model.robotics.EventDetails;
import uk.gov.hmcts.reform.unspec.model.robotics.EventHistory;
import uk.gov.hmcts.reform.unspec.service.flowstate.FlowState;
import uk.gov.hmcts.reform.unspec.service.flowstate.StateFlowEngine;
import uk.gov.hmcts.reform.unspec.stateflow.model.State;

import java.time.LocalDateTime;
import java.util.List;

import static java.lang.String.format;
import static java.time.format.DateTimeFormatter.ISO_DATE;
import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.unspec.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.unspec.service.robotics.mapper.RoboticsDataMapper.APPLICANT_ID;
import static uk.gov.hmcts.reform.unspec.service.robotics.mapper.RoboticsDataMapper.RESPONDENT_ID;

@Component
@RequiredArgsConstructor
public class EventHistoryMapper {

    private final StateFlowEngine stateFlowEngine;

    public EventHistory buildEvents(CaseData caseData) {
        EventHistory.EventHistoryBuilder builder = EventHistory.builder();
        State state = stateFlowEngine.evaluate(caseData).getState();
        FlowState.Main mainFlowState = (FlowState.Main) FlowState.fromFullName(state.getName());
        switch (mainFlowState) {
            case TAKEN_OFFLINE_UNREPRESENTED_DEFENDANT:
                buildUnrepresentedDefendant(caseData, builder);
                break;
            case TAKEN_OFFLINE_UNREGISTERED_DEFENDANT:
                buildUnregisteredDefendant(caseData, builder);
                break;
            case FULL_ADMISSION:
                buildRespondentFullAdmission(caseData, builder);
                break;
            case PART_ADMISSION:
                buildRespondentPartAdmission(caseData, builder);
                break;
            case COUNTER_CLAIM:
                buildRespondentCounterClaim(caseData, builder);
                break;
            case FULL_DEFENCE_NOT_PROCEED:
                buildFullDefenceNotProceed(caseData, builder);
                break;
            case FULL_DEFENCE_PROCEED:
                buildFullDefenceProceed(caseData, builder);
                break;
            default:
                break;
        }
        return builder.build();
    }

    private void buildFullDefenceProceed(CaseData caseData, EventHistory.EventHistoryBuilder builder) {
        buildMiscellaneousEvents(builder, caseData, "Applicant proceeds.", 8);
        buildAcknowledgementOfServiceReceived(builder, caseData);
        buildConsentExtensionFilingDefence(builder, caseData);
        buildDefenceFiled(builder, caseData);
        builder.directionsQuestionnaireFiled(
            List.of(
                Event.builder()
                    .eventSequence(5)
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
                    .build(),
                Event.builder()
                    .eventSequence(7)
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
            )
        ).replyToDefence(List.of(
            Event.builder()
                .eventSequence(6)
                .eventCode("66")
                .dateReceived(caseData.getApplicant1ResponseDate().format(ISO_DATE))
                .litigiousPartyID(APPLICANT_ID)
                .build()
        ));
    }

    public String prepareEventDetailsText(DQ dq, String preferredCourtCode) {
        return format(
            "preferredCourtCode: %s; stayClaim: %s",
            preferredCourtCode,
            isStayClaim(dq)
        );
    }

    public boolean isStayClaim(DQ dq) {
        return dq.getFileDirectionQuestionnaire()
            .getOneMonthStayRequested() == YES;
    }

    public String getPreferredCourtCode(DQ dq) {
        return ofNullable(dq.getRequestedCourt())
            .map(RequestedCourt::getResponseCourtCode)
            .orElse("");
    }

    private void buildFullDefenceNotProceed(CaseData caseData, EventHistory.EventHistoryBuilder builder) {
        buildMiscellaneousEvents(builder, caseData, "Claimant intends not to proceed.", 6);
        buildAcknowledgementOfServiceReceived(builder, caseData);
        buildConsentExtensionFilingDefence(builder, caseData);
        buildDefenceFiled(builder, caseData);
        builder.directionsQuestionnaireFiled(
            List.of(
                Event.builder()
                    .eventSequence(5)
                    .eventCode("197")
                    .dateReceived(caseData.getRespondent1ResponseDate().format(ISO_DATE))
                    .litigiousPartyID(RESPONDENT_ID)
                    .eventDetails(EventDetails.builder()
                                      .stayClaim(isStayClaim(caseData.getRespondent1DQ()))
                                      .preferredCourtCode(getPreferredCourtCode(caseData.getRespondent1DQ()))
                                      .preferredCourtName("")
                                      .build())
                    .eventDetailsText(prepareEventDetailsText(
                        caseData.getRespondent1DQ(),
                        getPreferredCourtCode(caseData.getRespondent1DQ())
                    ))
                    .build()
            )
        );
    }

    private void buildDefenceFiled(EventHistory.EventHistoryBuilder builder, CaseData caseData) {
        builder.defenceFiled(
            List.of(
                Event.builder()
                    .eventSequence(4)
                    .eventCode("50")
                    .dateReceived(caseData.getRespondent1ResponseDate().format(ISO_DATE))
                    .litigiousPartyID(RESPONDENT_ID)
                    .build()
            )
        );
    }

    private void buildUnrepresentedDefendant(CaseData caseData, EventHistory.EventHistoryBuilder builder) {
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

    private void buildUnregisteredDefendant(CaseData caseData, EventHistory.EventHistoryBuilder builder) {
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

    private void buildRespondentFullAdmission(CaseData caseData, EventHistory.EventHistoryBuilder builder) {
        builder.receiptOfAdmission(
            List.of(
                Event.builder()
                    .eventSequence(4)
                    .eventCode("40")
                    .dateReceived(caseData.getRespondent1ResponseDate().format(ISO_DATE))
                    .litigiousPartyID("002")
                    .build()
            )
        );
        buildCommonDefendantResponseEvents(builder, caseData, "Defendant fully admits.");
    }

    private void buildRespondentPartAdmission(CaseData caseData, EventHistory.EventHistoryBuilder builder) {
        builder.receiptOfPartAdmission(
            List.of(
                Event.builder()
                    .eventSequence(4)
                    .eventCode("60")
                    .dateReceived(caseData.getRespondent1ResponseDate().format(ISO_DATE))
                    .litigiousPartyID("002")
                    .build()
            )
        );
        buildCommonDefendantResponseEvents(builder, caseData, "Defendant partial admission.");
    }

    private void buildRespondentCounterClaim(CaseData caseData, EventHistory.EventHistoryBuilder builder) {
        builder.defenceAndCounterClaim(
            List.of(
                Event.builder()
                    .eventSequence(4)
                    .eventCode("52")
                    .dateReceived(caseData.getRespondent1ResponseDate().format(ISO_DATE))
                    .litigiousPartyID("002")
                    .build()
            )
        );
        buildCommonDefendantResponseEvents(builder, caseData, "Defendant rejects and counter claims.");
    }

    private void buildCommonDefendantResponseEvents(
        EventHistory.EventHistoryBuilder builder,
        CaseData caseData,
        String rpaReason
    ) {
        buildMiscellaneousEvents(builder, caseData, rpaReason, 5);
        buildAcknowledgementOfServiceReceived(builder, caseData);
        buildConsentExtensionFilingDefence(builder, caseData);
    }

    private void buildConsentExtensionFilingDefence(EventHistory.EventHistoryBuilder builder, CaseData caseData) {
        LocalDateTime dateReceived = caseData.getRespondent1TimeExtensionDate();
        if (dateReceived == null) {
            return;
        }
        builder.consentExtensionFilingDefence(
            List.of(
                Event.builder()
                    .eventSequence(3)
                    .eventCode("45")
                    .dateReceived(dateReceived.format(ISO_DATE))
                    .litigiousPartyID("002")
                    .eventDetails(EventDetails.builder()
                                      .agreedExtensionDate(caseData.getRespondentSolicitor1AgreedDeadlineExtension()
                                                               .format(ISO_DATE))
                                      .build())
                    .eventDetailsText(format("agreedExtensionDate: %s", caseData
                        .getRespondentSolicitor1AgreedDeadlineExtension()
                        .format(ISO_DATE)))
                    .build()
            )
        );
    }

    private void buildMiscellaneousEvents(
        EventHistory.EventHistoryBuilder builder,
        CaseData caseData,
        String rpaReason,
        Integer eventSequence
    ) {
        builder.miscellaneous(
            List.of(
                Event.builder()
                    .eventSequence(1)
                    .eventCode("999")
                    .dateReceived(caseData.getRespondent1ResponseDate().format(ISO_DATE))
                    .eventDetailsText("Claimant has notified defendant.")
                    .eventDetails(EventDetails.builder()
                                      .miscText("Claimant has notified defendant.")
                                      .build())
                    .build(),
                Event.builder()
                    .eventSequence(eventSequence)
                    .eventCode("999")
                    .dateReceived(caseData.getRespondent1ResponseDate().format(ISO_DATE))
                    .eventDetailsText("RPA Reason: " + rpaReason)
                    .eventDetails(EventDetails.builder()
                                      .miscText("RPA Reason: " + rpaReason)
                                      .build())
                    .build()
            )
        );
    }
}
