package uk.gov.hmcts.reform.unspec.service.robotics.mapper;

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

import static java.time.format.DateTimeFormatter.ISO_DATE;
import static org.assertj.core.api.Assertions.assertThat;

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

    @Test
    void shouldPrepareMiscellaneousEvent_whenClaimWithUnrepresentedDefendant() {
        CaseData caseData = CaseDataBuilder.builder().atStateProceedsOfflineUnrepresentedDefendant().build();
        Event expectedEvent = Event.builder()
            .eventSequence(1)
            .eventCode("999")
            .dateReceived(caseData.getSubmittedDate().toLocalDate().format(ISO_DATE))
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

    @Test
    void shouldPrepareExpectedEvents_whenClaimWithRespondentFullAdmission() {
        CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullAdmission().build();
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
                .eventDetails(EventDetails.builder()
                                  .miscText("Claimant has notified defendant")
                                  .build())
                .build(),
            Event.builder()
                .eventSequence(5)
                .eventCode("999")
                .dateReceived(caseData.getRespondent1ResponseDate().format(ISO_DATE))
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
                              .responseIntention("contest jurisdiction")
                              .build())
            .build();
        Event expectedConsentExtensionFilingDefence = Event.builder()
            .eventSequence(3)
            .eventCode("45")
            .dateReceived(caseData.getRespondent1ResponseDate().format(ISO_DATE))
            .litigiousPartyID("002")
            .eventDetails(EventDetails.builder()
                              .agreedExtensionDate("")
                              .build())
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
    void shouldPrepareExpectedEvents_whenClaimWithRespondentPartAdmission() {
        CaseData caseData = CaseDataBuilder.builder().atStateRespondentPartAdmission().build();
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
                .eventDetails(EventDetails.builder()
                                  .miscText("Claimant has notified defendant")
                                  .build())
                .build(),
            Event.builder()
                .eventSequence(5)
                .eventCode("999")
                .dateReceived(caseData.getRespondent1ResponseDate().format(ISO_DATE))
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
                              .responseIntention("contest jurisdiction")
                              .build())
            .build();
        Event expectedConsentExtensionFilingDefence = Event.builder()
            .eventSequence(3)
            .eventCode("45")
            .dateReceived(caseData.getRespondent1ResponseDate().format(ISO_DATE))
            .litigiousPartyID("002")
            .eventDetails(EventDetails.builder()
                              .agreedExtensionDate("")
                              .build())
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
    void shouldPrepareExpectedEvents_whenClaimWithRespondentCounterClaim() {
        CaseData caseData = CaseDataBuilder.builder().atStateRespondentCounterClaim().build();
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
                .eventDetails(EventDetails.builder()
                                  .miscText("Claimant has notified defendant")
                                  .build())
                .build(),
            Event.builder()
                .eventSequence(5)
                .eventCode("999")
                .dateReceived(caseData.getRespondent1ResponseDate().format(ISO_DATE))
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
                              .responseIntention("contest jurisdiction")
                              .build())
            .build();
        Event expectedConsentExtensionFilingDefence = Event.builder()
            .eventSequence(3)
            .eventCode("45")
            .dateReceived(caseData.getRespondent1ResponseDate().format(ISO_DATE))
            .litigiousPartyID("002")
            .eventDetails(EventDetails.builder()
                              .agreedExtensionDate("")
                              .build())
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

    @ParameterizedTest
    @EnumSource(value = FlowState.Main.class, mode = EnumSource.Mode.EXCLUDE, names = {
        "RESPONDENT_FULL_ADMISSION",
        "RESPONDENT_PART_ADMISSION",
        "RESPONDENT_COUNTER_CLAIM",
        "PROCEEDS_OFFLINE_UNREPRESENTED_DEFENDANT"
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
