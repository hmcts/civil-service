package uk.gov.hmcts.reform.unspec.service.robotics.mapper;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.reform.unspec.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.unspec.model.CaseData;
import uk.gov.hmcts.reform.unspec.model.robotics.Event;
import uk.gov.hmcts.reform.unspec.model.robotics.EventDetails;
import uk.gov.hmcts.reform.unspec.model.robotics.EventHistory;
import uk.gov.hmcts.reform.unspec.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.unspec.service.flowstate.FlowState;
import uk.gov.hmcts.reform.unspec.service.flowstate.StateFlowEngine;

import java.util.List;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.time.format.DateTimeFormatter.ISO_DATE;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.unspec.enums.ResponseIntention.CONTEST_JURISDICTION;
import static uk.gov.hmcts.reform.unspec.enums.ResponseIntention.PART_DEFENCE;

@SpringBootTest(classes = {
    JacksonAutoConfiguration.class,
    CaseDetailsConverter.class,
    StateFlowEngine.class,
    EventHistoryMapper.class
})
class EventHistoryMapperTest {

    private static final Event EMPTY_EVENT = Event.builder().build();

    @Autowired
    EventHistoryMapper mapper;

    @Nested
    class UnrepresentedDefendant {

        @Test
        void shouldPrepareMiscellaneousEvent_whenClaimWithUnrepresentedDefendant() {
            CaseData caseData = CaseDataBuilder.builder().atStateProceedsOfflineUnrepresentedDefendant().build();
            Event expectedEvent = Event.builder()
                .eventSequence(1)
                .eventCode("999")
                .dateReceived(caseData.getSubmittedDate().toLocalDate().format(ISO_DATE))
                .eventDetailsText("RPA Reason: Unrepresented defendant.")
                .eventDetails(EventDetails.builder()
                                  .miscText("RPA Reason: Unrepresented defendant.")
                                  .build())
                .build();

            var eventHistory = mapper.buildEvents(caseData);

            assertThat(eventHistory).isNotNull();
            assertThat(eventHistory)
                .extracting("miscellaneous")
                .asList()
                .containsExactly(expectedEvent);
            assertEmptyEvents(
                eventHistory,
                "acknowledgementOfServiceReceived",
                "consentExtensionFilingDefence",
                "defenceFiled",
                "defenceAndCounterClaim",
                "receiptOfPartAdmission",
                "receiptOfAdmission",
                "replyToDefence",
                "directionsQuestionnaireFiled"
            );
        }
    }

    @Nested
    class UnregisteredDefendant {

        @Test
        void shouldPrepareMiscellaneousEvent_whenClaimWithUnregisteredDefendant() {
            CaseData caseData = CaseDataBuilder.builder().atStateProceedsOfflineUnregisteredDefendant().build();
            Event expectedEvent = Event.builder()
                .eventSequence(1)
                .eventCode("999")
                .dateReceived(caseData.getSubmittedDate().toLocalDate().format(ISO_DATE))
                .eventDetailsText("RPA Reason: Unregistered defendant solicitor firm.")
                .eventDetails(EventDetails.builder()
                                  .miscText("RPA Reason: Unregistered defendant solicitor firm.")
                                  .build())
                .build();

            var eventHistory = mapper.buildEvents(caseData);

            assertThat(eventHistory).isNotNull();
            assertThat(eventHistory)
                .extracting("miscellaneous")
                .asList()
                .containsExactly(expectedEvent);
            assertEmptyEvents(
                eventHistory,
                "acknowledgementOfServiceReceived",
                "consentExtensionFilingDefence",
                "defenceFiled",
                "defenceAndCounterClaim",
                "receiptOfPartAdmission",
                "receiptOfAdmission",
                "replyToDefence",
                "directionsQuestionnaireFiled"
            );
        }
    }

    @Nested
    class RespondentFullAdmission {

        @Test
        void shouldPrepareExpectedEvents_whenClaimWithRespondentFullAdmissionWithOptionalEvents() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateNotificationAcknowledgedTimeExtension()
                .atStateRespondentFullAdmission()
                .build();
            Event expectedReceiptOfAdmission = Event.builder()
                .eventSequence(4)
                .eventCode("40")
                .dateReceived(caseData.getRespondent1ResponseDate().format(ISO_DATE))
                .litigiousPartyID("002")
                .build();
            List<Event> expectedMiscellaneousEvents = List.of(
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
                    .eventSequence(5)
                    .eventCode("999")
                    .dateReceived(caseData.getRespondent1ResponseDate().format(ISO_DATE))
                    .eventDetailsText("RPA Reason: Defendant fully admits.")
                    .eventDetails(EventDetails.builder()
                                      .miscText("RPA Reason: Defendant fully admits.")
                                      .build())
                    .build()
            );
            Event expectedAcknowledgementOfServiceReceived = Event.builder()
                .eventSequence(2)
                .eventCode("38")
                .dateReceived(caseData.getRespondent1ResponseDate().format(ISO_DATE))
                .litigiousPartyID("002")
                .eventDetails(EventDetails.builder()
                                  .responseIntention(caseData.getRespondent1ClaimResponseIntentionType()
                                                         .getLabel())
                                  .build())
                .eventDetailsText(format(
                    "responseIntention: %s",
                    caseData.getRespondent1ClaimResponseIntentionType().getLabel()
                ))
                .build();

            Event expectedConsentExtensionFilingDefence = Event.builder()
                .eventSequence(3)
                .eventCode("45")
                .dateReceived(caseData.getRespondent1TimeExtensionDate().format(ISO_DATE))
                .litigiousPartyID("002")
                .eventDetails(EventDetails.builder()
                                  .agreedExtensionDate(caseData.getRespondentSolicitor1AgreedDeadlineExtension()
                                                           .format(ISO_DATE))
                                  .build())
                .eventDetailsText(format("agreedExtensionDate: %s", caseData
                    .getRespondentSolicitor1AgreedDeadlineExtension()
                    .format(ISO_DATE)))
                .build();

            var eventHistory = mapper.buildEvents(caseData);

            assertThat(eventHistory).isNotNull();
            assertThat(eventHistory).extracting("receiptOfAdmission").asList()
                .containsExactly(expectedReceiptOfAdmission);
            assertThat(eventHistory).extracting("miscellaneous").asList()
                .containsExactly(expectedMiscellaneousEvents.get(0), expectedMiscellaneousEvents.get(1));
            assertThat(eventHistory).extracting("acknowledgementOfServiceReceived").asList()
                .containsExactly(expectedAcknowledgementOfServiceReceived);
            assertThat(eventHistory).extracting("consentExtensionFilingDefence").asList()
                .containsExactly(expectedConsentExtensionFilingDefence);

            assertEmptyEvents(
                eventHistory,
                "defenceFiled",
                "defenceAndCounterClaim",
                "receiptOfPartAdmission",
                "replyToDefence",
                "directionsQuestionnaireFiled"
            );
        }

        @Test
        void shouldPrepareExpectedEvents_whenClaimWithRespondentFullAdmissionWithoutOptionalEvents() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateRespondentFullAdmission()
                .respondent1AcknowledgeNotificationDate(null)
                .build();
            Event expectedReceiptOfAdmission = Event.builder()
                .eventSequence(4)
                .eventCode("40")
                .dateReceived(caseData.getRespondent1ResponseDate().format(ISO_DATE))
                .litigiousPartyID("002")
                .build();
            List<Event> expectedMiscellaneousEvents = List.of(
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
                    .eventSequence(5)
                    .eventCode("999")
                    .dateReceived(caseData.getRespondent1ResponseDate().format(ISO_DATE))
                    .eventDetailsText("RPA Reason: Defendant fully admits.")
                    .eventDetails(EventDetails.builder()
                                      .miscText("RPA Reason: Defendant fully admits.")
                                      .build())
                    .build()
            );

            var eventHistory = mapper.buildEvents(caseData);

            assertThat(eventHistory).isNotNull();
            assertThat(eventHistory).extracting("receiptOfAdmission").asList()
                .containsExactly(expectedReceiptOfAdmission);
            assertThat(eventHistory).extracting("miscellaneous").asList()
                .containsExactly(expectedMiscellaneousEvents.get(0), expectedMiscellaneousEvents.get(1));

            assertEmptyEvents(
                eventHistory,
                "defenceFiled",
                "defenceAndCounterClaim",
                "receiptOfPartAdmission",
                "replyToDefence",
                "directionsQuestionnaireFiled",
                "consentExtensionFilingDefence",
                "acknowledgementOfServiceReceived"
            );
        }
    }

    @Nested
    class RespondentPartAdmission {

        @Test
        void shouldPrepareExpectedEvents_whenClaimWithRespondentPartAdmissionWithOptionalEvents() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateNotificationAcknowledgedTimeExtension()
                .atStateRespondentPartAdmission()
                .respondent1ClaimResponseIntentionType(PART_DEFENCE)
                .build();
            Event expectedReceiptOfPartAdmission = Event.builder()
                .eventSequence(4)
                .eventCode("60")
                .dateReceived(caseData.getRespondent1ResponseDate().format(ISO_DATE))
                .litigiousPartyID("002")
                .build();
            List<Event> expectedMiscellaneousEvents = List.of(
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
                    .eventSequence(5)
                    .eventCode("999")
                    .dateReceived(caseData.getRespondent1ResponseDate().format(ISO_DATE))
                    .eventDetailsText("RPA Reason: Defendant partial admission.")
                    .eventDetails(EventDetails.builder()
                                      .miscText("RPA Reason: Defendant partial admission.")
                                      .build())
                    .build()
            );
            Event expectedAcknowledgementOfServiceReceived = Event.builder()
                .eventSequence(2)
                .eventCode("38")
                .dateReceived(caseData.getRespondent1ResponseDate().format(ISO_DATE))
                .litigiousPartyID("002")
                .eventDetails(EventDetails.builder()
                                  .responseIntention(caseData.getRespondent1ClaimResponseIntentionType()
                                                         .getLabel())
                                  .build())
                .eventDetailsText(format(
                    "responseIntention: %s",
                    caseData.getRespondent1ClaimResponseIntentionType().getLabel()
                ))
                .build();

            Event expectedConsentExtensionFilingDefence = Event.builder()
                .eventSequence(3)
                .eventCode("45")
                .dateReceived(caseData.getRespondent1TimeExtensionDate().format(ISO_DATE))
                .litigiousPartyID("002")
                .eventDetails(EventDetails.builder()
                                  .agreedExtensionDate(caseData.getRespondentSolicitor1AgreedDeadlineExtension()
                                                           .format(ISO_DATE))
                                  .build())
                .eventDetailsText(format("agreedExtensionDate: %s", caseData
                    .getRespondentSolicitor1AgreedDeadlineExtension()
                    .format(ISO_DATE)))
                .build();

            var eventHistory = mapper.buildEvents(caseData);

            assertThat(eventHistory).isNotNull();
            assertThat(eventHistory).extracting("receiptOfPartAdmission").asList()
                .containsExactly(expectedReceiptOfPartAdmission);
            assertThat(eventHistory).extracting("miscellaneous").asList()
                .containsExactly(expectedMiscellaneousEvents.get(0), expectedMiscellaneousEvents.get(1));
            assertThat(eventHistory).extracting("acknowledgementOfServiceReceived").asList()
                .containsExactly(expectedAcknowledgementOfServiceReceived);
            assertThat(eventHistory).extracting("consentExtensionFilingDefence").asList()
                .containsExactly(expectedConsentExtensionFilingDefence);

            assertEmptyEvents(
                eventHistory,
                "defenceFiled",
                "defenceAndCounterClaim",
                "receiptOfAdmission",
                "replyToDefence",
                "directionsQuestionnaireFiled"
            );
        }

        @Test
        void shouldPrepareExpectedEvents_whenClaimWithRespondentPartAdmissionWithoutOptionalEvents() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateRespondentPartAdmission()
                .respondent1AcknowledgeNotificationDate(null)
                .respondent1ClaimResponseIntentionType(PART_DEFENCE)
                .build();
            Event expectedReceiptOfPartAdmission = Event.builder()
                .eventSequence(4)
                .eventCode("60")
                .dateReceived(caseData.getRespondent1ResponseDate().format(ISO_DATE))
                .litigiousPartyID("002")
                .build();
            List<Event> expectedMiscellaneousEvents = List.of(
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
                    .eventSequence(5)
                    .eventCode("999")
                    .dateReceived(caseData.getRespondent1ResponseDate().format(ISO_DATE))
                    .eventDetailsText("RPA Reason: Defendant partial admission.")
                    .eventDetails(EventDetails.builder()
                                      .miscText("RPA Reason: Defendant partial admission.")
                                      .build())
                    .build()
            );

            var eventHistory = mapper.buildEvents(caseData);

            assertThat(eventHistory).isNotNull();
            assertThat(eventHistory).extracting("receiptOfPartAdmission").asList()
                .containsExactly(expectedReceiptOfPartAdmission);
            assertThat(eventHistory).extracting("miscellaneous").asList()
                .containsExactly(expectedMiscellaneousEvents.get(0), expectedMiscellaneousEvents.get(1));

            assertEmptyEvents(
                eventHistory,
                "defenceFiled",
                "defenceAndCounterClaim",
                "receiptOfAdmission",
                "replyToDefence",
                "directionsQuestionnaireFiled",
                "consentExtensionFilingDefence",
                "acknowledgementOfServiceReceived"
            );
        }
    }

    @Nested
    class RespondentCounterClaim {

        @Test
        void shouldPrepareExpectedEvents_whenClaimWithRespondentCounterClaimWithOptionalEvents() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateNotificationAcknowledgedTimeExtension()
                .atStateRespondentCounterClaim()
                .respondent1ClaimResponseIntentionType(CONTEST_JURISDICTION)
                .build();
            Event expectedDefenceAndCounterClaim = Event.builder()
                .eventSequence(4)
                .eventCode("52")
                .dateReceived(caseData.getRespondent1ResponseDate().format(ISO_DATE))
                .litigiousPartyID("002")
                .build();
            List<Event> expectedMiscellaneousEvents = List.of(
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
                    .eventSequence(5)
                    .eventCode("999")
                    .dateReceived(caseData.getRespondent1ResponseDate().format(ISO_DATE))
                    .eventDetailsText("RPA Reason: Defendant rejects and counter claims.")
                    .eventDetails(EventDetails.builder()
                                      .miscText("RPA Reason: Defendant rejects and counter claims.")
                                      .build())
                    .build()
            );
            Event expectedAcknowledgementOfServiceReceived = Event.builder()
                .eventSequence(2)
                .eventCode("38")
                .dateReceived(caseData.getRespondent1ResponseDate().format(ISO_DATE))
                .litigiousPartyID("002")
                .eventDetails(EventDetails.builder()
                                  .responseIntention(caseData.getRespondent1ClaimResponseIntentionType()
                                                         .getLabel())
                                  .build())
                .eventDetailsText(format(
                    "responseIntention: %s",
                    caseData.getRespondent1ClaimResponseIntentionType().getLabel()
                ))
                .build();
            Event expectedConsentExtensionFilingDefence = Event.builder()
                .eventSequence(3)
                .eventCode("45")
                .dateReceived(caseData.getRespondent1TimeExtensionDate().format(ISO_DATE))
                .litigiousPartyID("002")
                .eventDetails(EventDetails.builder()
                                  .agreedExtensionDate(caseData.getRespondentSolicitor1AgreedDeadlineExtension()
                                                           .format(ISO_DATE))
                                  .build())
                .eventDetailsText(format("agreedExtensionDate: %s", caseData
                    .getRespondentSolicitor1AgreedDeadlineExtension()
                    .format(ISO_DATE)))
                .build();

            var eventHistory = mapper.buildEvents(caseData);

            assertThat(eventHistory).isNotNull();
            assertThat(eventHistory).extracting("defenceAndCounterClaim").asList()
                .containsExactly(expectedDefenceAndCounterClaim);
            assertThat(eventHistory).extracting("miscellaneous").asList()
                .containsExactly(expectedMiscellaneousEvents.get(0), expectedMiscellaneousEvents.get(1));
            assertThat(eventHistory).extracting("acknowledgementOfServiceReceived").asList()
                .containsExactly(expectedAcknowledgementOfServiceReceived);
            assertThat(eventHistory).extracting("consentExtensionFilingDefence").asList()
                .containsExactly(expectedConsentExtensionFilingDefence);

            assertEmptyEvents(
                eventHistory,
                "defenceFiled",
                "receiptOfAdmission",
                "receiptOfPartAdmission",
                "replyToDefence",
                "directionsQuestionnaireFiled"
            );
        }

        @Test
        void shouldPrepareExpectedEvents_whenClaimWithRespondentCounterClaimWithoutOptionalEvents() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateRespondentCounterClaim()
                .respondent1ClaimResponseIntentionType(CONTEST_JURISDICTION)
                .respondent1AcknowledgeNotificationDate(null)
                .build();
            Event expectedDefenceAndCounterClaim = Event.builder()
                .eventSequence(4)
                .eventCode("52")
                .dateReceived(caseData.getRespondent1ResponseDate().format(ISO_DATE))
                .litigiousPartyID("002")
                .build();
            List<Event> expectedMiscellaneousEvents = List.of(
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
                    .eventSequence(5)
                    .eventCode("999")
                    .dateReceived(caseData.getRespondent1ResponseDate().format(ISO_DATE))
                    .eventDetailsText("RPA Reason: Defendant rejects and counter claims.")
                    .eventDetails(EventDetails.builder()
                                      .miscText("RPA Reason: Defendant rejects and counter claims.")
                                      .build())
                    .build()
            );

            var eventHistory = mapper.buildEvents(caseData);

            assertThat(eventHistory).isNotNull();
            assertThat(eventHistory).extracting("defenceAndCounterClaim").asList()
                .containsExactly(expectedDefenceAndCounterClaim);
            assertThat(eventHistory).extracting("miscellaneous").asList()
                .containsExactly(expectedMiscellaneousEvents.get(0), expectedMiscellaneousEvents.get(1));

            assertEmptyEvents(
                eventHistory,
                "defenceFiled",
                "receiptOfAdmission",
                "receiptOfPartAdmission",
                "replyToDefence",
                "directionsQuestionnaireFiled",
                "acknowledgementOfServiceReceived",
                "consentExtensionFilingDefence"
            );
        }
    }

    @Nested
    class FullDefenceNotProceeds {

        @Test
        void shouldPrepareExpectedEvents_whenClaimWithFullDefenceNotProceedsWithOptionalEvents() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateNotificationAcknowledgedTimeExtension()
                .atState(FlowState.Main.FULL_DEFENCE_NOT_PROCEED)
                .build();
            Event expectedDefenceFiled = Event.builder()
                .eventSequence(4)
                .eventCode("50")
                .dateReceived(caseData.getRespondent1ResponseDate().format(ISO_DATE))
                .litigiousPartyID("002")
                .build();
            Event expectedDirectionsQuestionnaireFiled = Event.builder()
                .eventSequence(5)
                .eventCode("197")
                .dateReceived(caseData.getRespondent1ResponseDate().format(ISO_DATE))
                .litigiousPartyID("002")
                .eventDetailsText(mapper.prepareEventDetailsText(
                    caseData.getRespondent1DQ(),
                    mapper.getPreferredCourtCode(caseData.getRespondent1DQ())
                ))
                .eventDetails(EventDetails.builder()
                                  .stayClaim(mapper.isStayClaim(caseData.getRespondent1DQ()))
                                  .preferredCourtCode(mapper.getPreferredCourtCode(caseData.getRespondent1DQ()))
                                  .preferredCourtName("")
                                  .build())
                .build();
            List<Event> expectedMiscellaneousEvents = List.of(
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
                    .eventSequence(6)
                    .eventCode("999")
                    .dateReceived(caseData.getRespondent1ResponseDate().format(ISO_DATE))
                    .eventDetailsText("RPA Reason: Claimant intends not to proceed.")
                    .eventDetails(EventDetails.builder()
                                      .miscText("RPA Reason: Claimant intends not to proceed.")
                                      .build())
                    .build()
            );
            Event expectedAcknowledgementOfServiceReceived = Event.builder()
                .eventSequence(2)
                .eventCode("38")
                .dateReceived(caseData.getRespondent1ResponseDate().format(ISO_DATE))
                .litigiousPartyID("002")
                .eventDetails(EventDetails.builder()
                                  .responseIntention(caseData.getRespondent1ClaimResponseIntentionType()
                                                         .getLabel())
                                  .build())
                .eventDetailsText(format(
                    "responseIntention: %s",
                    caseData.getRespondent1ClaimResponseIntentionType().getLabel()
                ))
                .build();
            Event expectedConsentExtensionFilingDefence = Event.builder()
                .eventSequence(3)
                .eventCode("45")
                .dateReceived(caseData.getRespondent1TimeExtensionDate().format(ISO_DATE))
                .litigiousPartyID("002")
                .eventDetails(EventDetails.builder()
                                  .agreedExtensionDate(caseData.getRespondentSolicitor1AgreedDeadlineExtension()
                                                           .format(ISO_DATE))
                                  .build())
                .eventDetailsText(format("agreedExtensionDate: %s", caseData
                    .getRespondentSolicitor1AgreedDeadlineExtension()
                    .format(ISO_DATE)))
                .build();

            var eventHistory = mapper.buildEvents(caseData);

            assertThat(eventHistory).isNotNull();
            assertThat(eventHistory).extracting("defenceFiled").asList()
                .containsExactly(expectedDefenceFiled);
            assertThat(eventHistory).extracting("directionsQuestionnaireFiled").asList()
                .containsExactly(expectedDirectionsQuestionnaireFiled);
            assertThat(eventHistory).extracting("miscellaneous").asList()
                .containsExactly(expectedMiscellaneousEvents.get(0), expectedMiscellaneousEvents.get(1));
            assertThat(eventHistory).extracting("acknowledgementOfServiceReceived").asList()
                .containsExactly(expectedAcknowledgementOfServiceReceived);
            assertThat(eventHistory).extracting("consentExtensionFilingDefence").asList()
                .containsExactly(expectedConsentExtensionFilingDefence);

            assertEmptyEvents(
                eventHistory,
                "receiptOfAdmission",
                "receiptOfPartAdmission",
                "replyToDefence"
            );
        }

        @Test
        void shouldPrepareExpectedEvents_whenClaimWithFullDefenceNotProceedsWithoutOptionalEvents() {
            CaseData caseData = CaseDataBuilder.builder()
                .atState(FlowState.Main.FULL_DEFENCE_NOT_PROCEED)
                .respondent1AcknowledgeNotificationDate(null)
                .build();
            Event expectedDefenceFiled = Event.builder()
                .eventSequence(4)
                .eventCode("50")
                .dateReceived(caseData.getRespondent1ResponseDate().format(ISO_DATE))
                .litigiousPartyID("002")
                .build();
            Event expectedDirectionsQuestionnaireFiled = Event.builder()
                .eventSequence(5)
                .eventCode("197")
                .dateReceived(caseData.getRespondent1ResponseDate().format(ISO_DATE))
                .litigiousPartyID("002")
                .eventDetailsText(mapper.prepareEventDetailsText(
                    caseData.getRespondent1DQ(),
                    mapper.getPreferredCourtCode(caseData.getRespondent1DQ())
                ))
                .eventDetails(EventDetails.builder()
                                  .stayClaim(mapper.isStayClaim(caseData.getRespondent1DQ()))
                                  .preferredCourtCode(mapper.getPreferredCourtCode(caseData.getRespondent1DQ()))
                                  .preferredCourtName("")
                                  .build())
                .build();
            List<Event> expectedMiscellaneousEvents = List.of(
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
                    .eventSequence(6)
                    .eventCode("999")
                    .dateReceived(caseData.getRespondent1ResponseDate().format(ISO_DATE))
                    .eventDetailsText("RPA Reason: Claimant intends not to proceed.")
                    .eventDetails(EventDetails.builder()
                                      .miscText("RPA Reason: Claimant intends not to proceed.")
                                      .build())
                    .build()
            );

            var eventHistory = mapper.buildEvents(caseData);

            assertThat(eventHistory).isNotNull();
            assertThat(eventHistory).extracting("defenceFiled").asList()
                .containsExactly(expectedDefenceFiled);
            assertThat(eventHistory).extracting("directionsQuestionnaireFiled").asList()
                .containsExactly(expectedDirectionsQuestionnaireFiled);
            assertThat(eventHistory).extracting("miscellaneous").asList()
                .containsExactly(expectedMiscellaneousEvents.get(0), expectedMiscellaneousEvents.get(1));

            assertEmptyEvents(
                eventHistory,
                "receiptOfAdmission",
                "receiptOfPartAdmission",
                "replyToDefence",
                "acknowledgementOfServiceReceived",
                "consentExtensionFilingDefence"
            );
        }
    }

    @Nested
    class FullDefenceProceeds {

        @Test
        void shouldPrepareExpectedEvents_whenClaimWithFullDefenceProceedsWithOptionalEvents() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateRespondentFullDefenceAfterAcknowledgementTimeExtension()
                .atState(FlowState.Main.FULL_DEFENCE_PROCEED)
                .build();
            Event expectedDefenceFiled = Event.builder()
                .eventSequence(4)
                .eventCode("50")
                .dateReceived(caseData.getRespondent1ResponseDate().format(ISO_DATE))
                .litigiousPartyID("002")
                .build();
            Event expectedDirectionsQuestionnaireRespondent = Event.builder()
                .eventSequence(5)
                .eventCode("197")
                .dateReceived(caseData.getRespondent1ResponseDate().format(ISO_DATE))
                .litigiousPartyID("002")
                .eventDetailsText(mapper.prepareEventDetailsText(
                    caseData.getRespondent1DQ(),
                    mapper.getPreferredCourtCode(caseData.getRespondent1DQ())
                ))
                .eventDetails(EventDetails.builder()
                                  .stayClaim(mapper.isStayClaim(caseData.getRespondent1DQ()))
                                  .preferredCourtCode(mapper.getPreferredCourtCode(caseData.getRespondent1DQ()))
                                  .preferredCourtName("")
                                  .build())
                .build();
            Event expectedReplyToDefence = Event.builder()
                .eventSequence(6)
                .eventCode("66")
                .dateReceived(caseData.getApplicant1ResponseDate().format(ISO_DATE))
                .litigiousPartyID("001")
                .build();
            Event expectedDirectionsQuestionnaireApplicant = Event.builder()
                .eventSequence(7)
                .eventCode("197")
                .dateReceived(caseData.getApplicant1ResponseDate().format(ISO_DATE))
                .litigiousPartyID("001")
                .eventDetails(EventDetails.builder()
                                  .stayClaim(mapper.isStayClaim(caseData.getApplicant1DQ()))
                                  .preferredCourtCode(caseData.getCourtLocation().getApplicantPreferredCourt())
                                  .preferredCourtName("")
                                  .build())
                .eventDetailsText(mapper.prepareEventDetailsText(
                    caseData.getApplicant1DQ(),
                    caseData.getCourtLocation().getApplicantPreferredCourt()
                ))
                .build();
            List<Event> expectedMiscellaneousEvents = List.of(
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
                    .eventSequence(8)
                    .eventCode("999")
                    .dateReceived(caseData.getRespondent1ResponseDate().format(ISO_DATE))
                    .eventDetailsText("RPA Reason: Applicant proceeds.")
                    .eventDetails(EventDetails.builder()
                                      .miscText("RPA Reason: Applicant proceeds.")
                                      .build())
                    .build()
            );
            Event expectedAcknowledgementOfServiceReceived = Event.builder()
                .eventSequence(2)
                .eventCode("38")
                .dateReceived(caseData.getRespondent1ResponseDate().format(ISO_DATE))
                .litigiousPartyID("002")
                .eventDetails(EventDetails.builder()
                                  .responseIntention(caseData.getRespondent1ClaimResponseIntentionType()
                                                         .getLabel())
                                  .build())
                .eventDetailsText(format(
                    "responseIntention: %s",
                    caseData.getRespondent1ClaimResponseIntentionType().getLabel()
                ))
                .build();
            Event expectedConsentExtensionFilingDefence = Event.builder()
                .eventSequence(3)
                .eventCode("45")
                .dateReceived(caseData.getRespondent1TimeExtensionDate().format(ISO_DATE))
                .litigiousPartyID("002")
                .eventDetails(EventDetails.builder()
                                  .agreedExtensionDate(caseData.getRespondentSolicitor1AgreedDeadlineExtension()
                                                           .format(ISO_DATE))
                                  .build())
                .eventDetailsText(format("agreedExtensionDate: %s", caseData
                    .getRespondentSolicitor1AgreedDeadlineExtension()
                    .format(ISO_DATE)))
                .build();

            var eventHistory = mapper.buildEvents(caseData);

            assertThat(eventHistory).isNotNull();
            assertThat(eventHistory).extracting("replyToDefence").asList()
                .containsExactly(expectedReplyToDefence);
            assertThat(eventHistory).extracting("defenceFiled").asList()
                .containsExactly(expectedDefenceFiled);
            assertThat(eventHistory).extracting("directionsQuestionnaireFiled")
                .asList().containsExactlyInAnyOrder(
                expectedDirectionsQuestionnaireRespondent,
                expectedDirectionsQuestionnaireApplicant
            );
            assertThat(eventHistory).extracting("miscellaneous").asList()
                .containsExactly(expectedMiscellaneousEvents.get(0), expectedMiscellaneousEvents.get(1));
            assertThat(eventHistory).extracting("acknowledgementOfServiceReceived").asList()
                .containsExactly(expectedAcknowledgementOfServiceReceived);
            assertThat(eventHistory).extracting("consentExtensionFilingDefence").asList()
                .containsExactly(expectedConsentExtensionFilingDefence);

            assertEmptyEvents(
                eventHistory,
                "receiptOfAdmission",
                "receiptOfPartAdmission"
            );
        }

        @Test
        void shouldPrepareExpectedEvents_whenClaimWithFullDefenceProceedsWithoutOptionalEvents() {
            CaseData caseData = CaseDataBuilder.builder()
                .atState(FlowState.Main.FULL_DEFENCE_PROCEED)
                .respondent1AcknowledgeNotificationDate(null)
                .build();
            Event expectedDefenceFiled = Event.builder()
                .eventSequence(4)
                .eventCode("50")
                .dateReceived(caseData.getRespondent1ResponseDate().format(ISO_DATE))
                .litigiousPartyID("002")
                .build();
            Event expectedDirectionsQuestionnaireRespondent = Event.builder()
                .eventSequence(5)
                .eventCode("197")
                .dateReceived(caseData.getRespondent1ResponseDate().format(ISO_DATE))
                .litigiousPartyID("002")
                .eventDetailsText(mapper.prepareEventDetailsText(
                    caseData.getRespondent1DQ(),
                    mapper.getPreferredCourtCode(caseData.getRespondent1DQ())
                ))
                .eventDetails(EventDetails.builder()
                                  .stayClaim(mapper.isStayClaim(caseData.getRespondent1DQ()))
                                  .preferredCourtCode(mapper.getPreferredCourtCode(caseData.getRespondent1DQ()))
                                  .preferredCourtName("")
                                  .build())
                .build();
            Event expectedReplyToDefence = Event.builder()
                .eventSequence(6)
                .eventCode("66")
                .dateReceived(caseData.getApplicant1ResponseDate().format(ISO_DATE))
                .litigiousPartyID("001")
                .build();
            Event expectedDirectionsQuestionnaireApplicant = Event.builder()
                .eventSequence(7)
                .eventCode("197")
                .dateReceived(caseData.getApplicant1ResponseDate().format(ISO_DATE))
                .litigiousPartyID("001")
                .eventDetails(EventDetails.builder()
                                  .stayClaim(mapper.isStayClaim(caseData.getApplicant1DQ()))
                                  .preferredCourtCode(caseData.getCourtLocation().getApplicantPreferredCourt())
                                  .preferredCourtName("")
                                  .build())
                .eventDetailsText(mapper.prepareEventDetailsText(
                    caseData.getApplicant1DQ(),
                    caseData.getCourtLocation().getApplicantPreferredCourt()
                ))
                .build();
            List<Event> expectedMiscellaneousEvents = List.of(
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
                    .eventSequence(8)
                    .eventCode("999")
                    .dateReceived(caseData.getRespondent1ResponseDate().format(ISO_DATE))
                    .eventDetailsText("RPA Reason: Applicant proceeds.")
                    .eventDetails(EventDetails.builder()
                                      .miscText("RPA Reason: Applicant proceeds.")
                                      .build())
                    .build()
            );

            var eventHistory = mapper.buildEvents(caseData);

            assertThat(eventHistory).isNotNull();
            assertThat(eventHistory).extracting("replyToDefence").asList()
                .containsExactly(expectedReplyToDefence);
            assertThat(eventHistory).extracting("defenceFiled").asList()
                .containsExactly(expectedDefenceFiled);
            assertThat(eventHistory).extracting("directionsQuestionnaireFiled")
                .asList().containsExactlyInAnyOrder(
                expectedDirectionsQuestionnaireRespondent,
                expectedDirectionsQuestionnaireApplicant
            );
            assertThat(eventHistory).extracting("miscellaneous").asList()
                .containsExactly(expectedMiscellaneousEvents.get(0), expectedMiscellaneousEvents.get(1));

            assertEmptyEvents(
                eventHistory,
                "receiptOfAdmission",
                "receiptOfPartAdmission",
                "acknowledgementOfServiceReceived",
                "consentExtensionFilingDefence"
            );
        }
    }

    @ParameterizedTest
    @EnumSource(value = FlowState.Main.class, mode = EnumSource.Mode.EXCLUDE, names = {
        "FULL_ADMISSION",
        "PART_ADMISSION",
        "COUNTER_CLAIM",
        "TAKEN_OFFLINE_UNREPRESENTED_DEFENDANT",
        "TAKEN_OFFLINE_UNREGISTERED_DEFENDANT",
        "FULL_DEFENCE_NOT_PROCEED",
        "FULL_DEFENCE_PROCEED"
    })
    void shouldBuildEmptyEventHistory_whenNoMappingsDefinedForStateFlow(FlowState.Main flowStateMain) {
        CaseData caseData = CaseDataBuilder.builder().atState(flowStateMain).build();

        var eventHistory = mapper.buildEvents(caseData);

        assertThat(eventHistory).isNotNull();
        assertEmptyEvents(
            eventHistory,
            "miscellaneous",
            "acknowledgementOfServiceReceived",
            "consentExtensionFilingDefence",
            "defenceFiled",
            "defenceAndCounterClaim",
            "receiptOfPartAdmission",
            "receiptOfAdmission",
            "replyToDefence",
            "directionsQuestionnaireFiled"
        );
    }

    private void assertEmptyEvents(EventHistory eventHistory, String... eventNames) {
        Stream.of(eventNames).forEach(
            eventName -> assertThat(eventHistory).extracting(eventName).asList().containsOnly(EMPTY_EVENT));
    }
}
