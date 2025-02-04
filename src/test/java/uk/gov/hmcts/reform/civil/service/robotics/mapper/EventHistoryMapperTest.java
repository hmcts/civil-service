package uk.gov.hmcts.reform.civil.service.robotics.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.civil.constants.SpecJourneyConstantLRSpec;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.DJPaymentTypeSelection;
import uk.gov.hmcts.reform.civil.enums.DebtPaymentOptions;
import uk.gov.hmcts.reform.civil.enums.MediationDecision;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.enums.PartyRole;
import uk.gov.hmcts.reform.civil.enums.PaymentFrequencyLRspec;
import uk.gov.hmcts.reform.civil.enums.RepaymentFrequencyDJ;
import uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.ResponseIntention;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CCJPaymentDetails;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.CaseNote;
import uk.gov.hmcts.reform.civil.model.DefendantPinToPostLRspec;
import uk.gov.hmcts.reform.civil.model.HearingSupportRequirementsDJ;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.PartyData;
import uk.gov.hmcts.reform.civil.model.RepaymentPlanLRspec;
import uk.gov.hmcts.reform.civil.model.RespondToClaim;
import uk.gov.hmcts.reform.civil.model.RespondToClaimAdmitPartLRspec;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.CertOfSC;
import uk.gov.hmcts.reform.civil.model.citizenui.ClaimantMediationLip;
import uk.gov.hmcts.reform.civil.model.citizenui.DebtPaymentEvidence;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.dq.Applicant1DQ;
import uk.gov.hmcts.reform.civil.model.dq.FileDirectionsQuestionnaire;
import uk.gov.hmcts.reform.civil.model.dq.RequestedCourt;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Respondent2DQ;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentDetails;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentSetAsideOrderType;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentSetAsideReason;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentState;
import uk.gov.hmcts.reform.civil.model.robotics.Event;
import uk.gov.hmcts.reform.civil.model.robotics.EventDetails;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;
import uk.gov.hmcts.reform.civil.model.sdo.ReasonNotSuitableSDO;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;
import uk.gov.hmcts.reform.civil.service.flowstate.SimpleStateFlowEngine;
import uk.gov.hmcts.reform.civil.service.flowstate.TransitionsTestConfiguration;
import uk.gov.hmcts.reform.civil.stateflow.simplegrammar.SimpleStateFlowBuilder;
import uk.gov.hmcts.reform.civil.utils.LocationRefDataUtil;
import uk.gov.hmcts.reform.civil.utils.PartyUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.math.BigDecimal.ZERO;
import static java.time.LocalDate.now;
import static java.time.format.DateTimeFormatter.ISO_DATE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.PartyRole.RESPONDENT_ONE;
import static uk.gov.hmcts.reform.civil.enums.PartyRole.RESPONDENT_TWO;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseType.COUNTER_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseType.FULL_ADMISSION;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseType.FULL_DEFENCE;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseType.PART_ADMISSION;
import static uk.gov.hmcts.reform.civil.enums.ResponseIntention.CONTEST_JURISDICTION;
import static uk.gov.hmcts.reform.civil.enums.ResponseIntention.PART_DEFENCE;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.enums.cosc.CoscRPAStatus.CANCELLED;
import static uk.gov.hmcts.reform.civil.enums.cosc.CoscRPAStatus.SATISFIED;
import static uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes.PROCEEDS_IN_HERITAGE;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_DETAILS_NOTIFIED_TIME_EXTENSION;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.FULL_DEFENCE_PROCEED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.NOTIFICATION_ACKNOWLEDGED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.NOTIFICATION_ACKNOWLEDGED_TIME_EXTENSION;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.TAKEN_OFFLINE_AFTER_SDO;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.TAKEN_OFFLINE_SDO_NOT_DRAWN;
import static uk.gov.hmcts.reform.civil.service.robotics.RoboticsNotificationService.findLatestEventTriggerReason;
import static uk.gov.hmcts.reform.civil.service.robotics.mapper.EventHistoryMapper.RPA_IN_MEDIATION;
import static uk.gov.hmcts.reform.civil.service.robotics.mapper.EventHistoryMapper.RPA_REASON_JUDGMENT_BY_ADMISSION;
import static uk.gov.hmcts.reform.civil.service.robotics.mapper.EventHistoryMapper.RECORD_JUDGMENT;
import static uk.gov.hmcts.reform.civil.service.robotics.mapper.EventHistoryMapper.RPA_RECORD_JUDGMENT_REASON;
import static uk.gov.hmcts.reform.civil.service.robotics.mapper.EventHistoryMapper.RPA_REASON_MANUAL_DETERMINATION;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

@SpringBootTest(classes = {
    JacksonAutoConfiguration.class,
    CaseDetailsConverter.class,
    SimpleStateFlowEngine.class,
    SimpleStateFlowBuilder.class,
    TransitionsTestConfiguration.class,
    EventHistorySequencer.class,
    EventHistorySequencer.class,
    EventHistoryMapper.class
})
class EventHistoryMapperTest {

    private static final Event EMPTY_EVENT = Event.builder().build();
    private static final String BEARER_TOKEN = "Bearer Token";

    @MockBean
    private FeatureToggleService featureToggleService;

    @MockBean
    LocationRefDataUtil locationRefDataUtil;

    @Autowired
    EventHistoryMapper mapper;

    @MockBean
    private Time time;

    LocalDateTime localDateTime;
    List<LocationRefData> courtLocations;

    @BeforeEach
    void setup() {
        localDateTime = LocalDateTime.of(2020, 8, 1, 12, 0, 0);
        when(time.now()).thenReturn(localDateTime);
        courtLocations = new ArrayList<>();
        courtLocations.add(LocationRefData.builder().siteName("SiteName").courtAddress("1").postcode("1")
                               .courtName("Court Name").region("Region").regionId("4").courtVenueId("000")
                               .courtTypeId("10").courtLocationCode("121")
                               .epimmsId("000000").build());
        when(locationRefDataUtil.getPreferredCourtData(any(), any(), eq(true))).thenReturn("121");
    }

    @Nested
    class UnregisteredDefendant {

        @Test
        void shouldPrepareMiscellaneousEvent_whenClaimWith1v1UnregisteredDefendant() {
            CaseData caseData = CaseDataBuilder.builder().atStateProceedsOffline1v1UnregisteredDefendant().build();
            if (caseData.getRespondent2OrgRegistered() != null
                && caseData.getRespondent2Represented() == null) {
                caseData = caseData.toBuilder()
                    .respondent2Represented(YES)
                    .build();
            }
            Event expectedEvent = Event.builder()
                .eventSequence(1)
                .eventCode("999")
                .dateReceived(caseData.getSubmittedDate())
                .eventDetailsText("RPA Reason: Unregistered defendant solicitor firm: Mr. Sole Trader")
                .eventDetails(EventDetails.builder()
                                  .miscText("RPA Reason: Unregistered defendant solicitor firm: Mr. Sole Trader")
                                  .build())
                .build();

            var eventHistory = mapper.buildEvents(caseData, BEARER_TOKEN);

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
        void shouldPrepareMiscellaneousEvent_whenClaimWith2UnregisteredDefendants() {
            CaseData caseData = CaseDataBuilder.builder().atStateProceedsOfflineUnregisteredDefendants().build();
            Event expectedEvent1 = Event.builder()
                .eventSequence(1)
                .eventCode("999")
                .dateReceived(caseData.getSubmittedDate())
                .eventDetailsText("RPA Reason: [1 of 2 - 2020-08-01] "
                                      + "Unregistered defendant solicitor firm: Mr. Sole Trader")
                .eventDetails(EventDetails.builder()
                                  .miscText("RPA Reason: [1 of 2 - 2020-08-01] "
                                                + "Unregistered defendant solicitor firm: Mr. Sole Trader")
                                  .build())
                .build();

            Event expectedEvent2 = Event.builder()
                .eventSequence(2)
                .eventCode("999")
                .dateReceived(caseData.getSubmittedDate())
                .eventDetailsText("RPA Reason: [2 of 2 - 2020-08-01] "
                                      + "Unregistered defendant solicitor firm: Mr. John Rambo")
                .eventDetails(EventDetails.builder()
                                  .miscText("RPA Reason: [2 of 2 - 2020-08-01] "
                                                + "Unregistered defendant solicitor firm: Mr. John Rambo")
                                  .build())
                .build();

            var eventHistory = mapper.buildEvents(caseData, BEARER_TOKEN);

            assertThat(eventHistory).isNotNull();
            assertThat(eventHistory)
                .extracting("miscellaneous")
                .asList()
                .containsExactly(expectedEvent1, expectedEvent2);
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
    class UnrepresentedAndUnregisteredDefendant {

        @Test
        void shouldPrepareMiscellaneousEvent_whenClaimWithUnrepresentedDefendant1UnregisteredDefendant2() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateProceedsOfflineUnrepresentedDefendant1UnregisteredDefendant2().build();
            if (caseData.getRespondent2OrgRegistered() != null
                && caseData.getRespondent2Represented() == null) {
                caseData = caseData.toBuilder()
                    .respondent2Represented(YES)
                    .build();
            }
            Event expectedEvent1 = Event.builder()
                .eventSequence(1)
                .eventCode("999")
                .dateReceived(caseData.getSubmittedDate())
                .eventDetailsText("RPA Reason: [1 of 2 - 2020-08-01] "
                                      + "Unrepresented defendant and unregistered defendant solicitor firm. "
                                      + "Unrepresented defendant: Mr. Sole Trader")
                .eventDetails(EventDetails.builder()
                                  .miscText("RPA Reason: [1 of 2 - 2020-08-01] "
                                                + "Unrepresented defendant and unregistered defendant solicitor firm. "
                                                + "Unrepresented defendant: Mr. Sole Trader")
                                  .build())
                .build();

            Event expectedEvent2 = Event.builder()
                .eventSequence(2)
                .eventCode("999")
                .dateReceived(caseData.getSubmittedDate())
                .eventDetailsText("RPA Reason: [2 of 2 - 2020-08-01] "
                                      + "Unrepresented defendant and unregistered defendant solicitor firm. "
                                      + "Unregistered defendant solicitor firm: Mr. John Rambo")
                .eventDetails(EventDetails.builder()
                                  .miscText("RPA Reason: [2 of 2 - 2020-08-01] "
                                                + "Unrepresented defendant and unregistered defendant solicitor firm. "
                                                + "Unregistered defendant solicitor firm: Mr. John Rambo")
                                  .build())
                .build();

            var eventHistory = mapper.buildEvents(caseData, BEARER_TOKEN);

            assertThat(eventHistory).isNotNull();
            assertThat(eventHistory)
                .extracting("miscellaneous")
                .asList()
                .containsExactly(expectedEvent1, expectedEvent2);
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
        void shouldPrepareMiscellaneousEvent_whenClaimWithUnregisteredDefendant1UnrepresentedDefendant2() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateProceedsOfflineUnregisteredDefendant1UnrepresentedDefendant2().build();
            if (caseData.getRespondent2OrgRegistered() != null
                && caseData.getRespondent2Represented() == null) {
                caseData = caseData.toBuilder()
                    .respondent2Represented(YES)
                    .build();
            }
            Event expectedEvent1 = Event.builder()
                .eventSequence(1)
                .eventCode("999")
                .dateReceived(caseData.getSubmittedDate())
                .eventDetailsText("RPA Reason: [1 of 2 - 2020-08-01] "
                                      + "Unrepresented defendant and unregistered defendant solicitor firm. "
                                      + "Unrepresented defendant: Mr. John Rambo")
                .eventDetails(EventDetails.builder()
                                  .miscText("RPA Reason: [1 of 2 - 2020-08-01] "
                                                + "Unrepresented defendant and unregistered defendant solicitor firm. "
                                                + "Unrepresented defendant: Mr. John Rambo")
                                  .build())
                .build();

            Event expectedEvent2 = Event.builder()
                .eventSequence(2)
                .eventCode("999")
                .dateReceived(caseData.getSubmittedDate())
                .eventDetailsText("RPA Reason: [2 of 2 - 2020-08-01] "
                                      + "Unrepresented defendant and unregistered defendant solicitor firm. "
                                      + "Unregistered defendant solicitor firm: Mr. Sole Trader")
                .eventDetails(EventDetails.builder()
                                  .miscText("RPA Reason: [2 of 2 - 2020-08-01] "
                                                + "Unrepresented defendant and unregistered defendant solicitor firm. "
                                                + "Unregistered defendant solicitor firm: Mr. Sole Trader")
                                  .build())
                .build();

            var eventHistory = mapper.buildEvents(caseData, BEARER_TOKEN);

            assertThat(eventHistory).isNotNull();
            assertThat(eventHistory)
                .extracting("miscellaneous")
                .asList()
                .containsExactly(expectedEvent1, expectedEvent2);
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
    class CreateClaimRpaContinuousFeed {

        @Test
        void shouldPrepareMiscellaneousEvent_whenClaimIssued() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build();
            if (caseData.getRespondent2OrgRegistered() != null
                && caseData.getRespondent2Represented() == null) {
                caseData = caseData.toBuilder()
                    .respondent2Represented(YES)
                    .build();
            }
            Event expectedEvent = Event.builder()
                .eventSequence(1)
                .eventCode("999")
                .dateReceived(caseData.getIssueDate().atStartOfDay())
                .eventDetailsText("Claim issued in CCD.")
                .eventDetails(EventDetails.builder()
                                  .miscText("Claim issued in CCD.")
                                  .build())
                .build();

            var eventHistory = mapper.buildEvents(caseData, BEARER_TOKEN);

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
    class NotifyClaimRpaHandedOffline {

        @Test
        void shouldPrepareMiscellaneousEvent_whenNotifyClaimRpaHandedOffline() {
            CaseData caseData = CaseDataBuilder.builder().atStateProceedsOfflineAfterClaimNotified().build();
            if (caseData.getRespondent2OrgRegistered() != null
                && caseData.getRespondent2Represented() == null) {
                caseData = caseData.toBuilder()
                    .respondent2Represented(YES)
                    .build();
            }
            List<Event> expectedMiscellaneousEvents = List.of(
                Event.builder()
                    .eventSequence(1)
                    .eventCode("999")
                    .dateReceived(caseData.getIssueDate().atStartOfDay())
                    .eventDetailsText("Claim issued in CCD.")
                    .eventDetails(EventDetails.builder()
                                      .miscText("Claim issued in CCD.")
                                      .build())
                    .build(),
                Event.builder()
                    .eventSequence(2)
                    .eventCode("999")
                    .dateReceived(caseData.getSubmittedDate())
                    .eventDetailsText("RPA Reason: Only one of the respondent is notified.")
                    .eventDetails(EventDetails.builder()
                                      .miscText("RPA Reason: Only one of the respondent is notified.")
                                      .build())
                    .build()
            );

            var eventHistory = mapper.buildEvents(caseData, BEARER_TOKEN);

            assertThat(eventHistory).isNotNull();
            assertThat(eventHistory)
                .extracting("miscellaneous")
                .asList()
                .containsExactly(expectedMiscellaneousEvents.get(0), expectedMiscellaneousEvents.get(1));
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
    class NotifyClaimDetailsRpaHandedOffline {

        @Test
        void shouldPrepareExpectedEvents_whenPastApplicantResponseDeadline() {
            CaseData caseData = CaseDataBuilder.builder().atStateProceedsOfflineAfterClaimDetailsNotified().build();
            if (caseData.getRespondent2OrgRegistered() != null
                && caseData.getRespondent2Represented() == null) {
                caseData = caseData.toBuilder()
                    .respondent2Represented(YES)
                    .build();
            }
            List<Event> expectedEvent = List.of(
                Event.builder()
                    .eventSequence(1)
                    .eventCode("999")
                    .dateReceived(caseData.getIssueDate().atStartOfDay())
                    .eventDetailsText("Claim issued in CCD.")
                    .eventDetails(EventDetails.builder()
                                      .miscText("Claim issued in CCD.")
                                      .build())
                    .build(),
                Event.builder()
                    .eventSequence(2)
                    .eventCode("999")
                    .dateReceived(caseData.getSubmittedDate())
                    .eventDetailsText("RPA Reason: Only one of the respondent is notified.")
                    .eventDetails(EventDetails.builder()
                                      .miscText("RPA Reason: Only one of the respondent is notified.")
                                      .build())
                    .build(),
                Event.builder()
                    .eventSequence(3)
                    .eventCode("999")
                    .dateReceived(caseData.getClaimNotificationDate())
                    .eventDetailsText("Claimant has notified defendant.")
                    .eventDetails(EventDetails.builder()
                                      .miscText("Claimant has notified defendant.")
                                      .build())
                    .build()
            );

            var eventHistory = mapper.buildEvents(caseData, BEARER_TOKEN);

            assertThat(eventHistory).isNotNull();
            assertThat(eventHistory)
                .extracting("miscellaneous")
                .asList()
                .containsExactly(expectedEvent.get(0), expectedEvent.get(1), expectedEvent.get(2));
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
    class NotifyClaimDetailsRpaContinuousFeed {

        @Test
        void shouldReturnLatestTriggerEvent() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
            if (caseData.getRespondent2OrgRegistered() != null
                && caseData.getRespondent2Represented() == null) {
                caseData = caseData.toBuilder()
                    .respondent2Represented(YES)
                    .build();
            }
            Event claimIssuedEvent = Event.builder()
                .eventSequence(1)
                .eventCode("999")
                .dateReceived(caseData.getIssueDate().atStartOfDay())
                .eventDetailsText("Claim issued in CCD.")
                .eventDetails(EventDetails.builder()
                                  .miscText("Claim issued in CCD.")
                                  .build())
                .build();
            Event claimNotifiedEvent = Event.builder()
                .eventSequence(2)
                .eventCode("999")
                .dateReceived(caseData.getClaimNotificationDate())
                .eventDetailsText("Claimant has notified defendant.")
                .eventDetails(EventDetails.builder()
                                  .miscText("Claimant has notified defendant.")
                                  .build())
                .build();

            Event claimDetailsNotifiedEvent = Event.builder()
                .eventSequence(3)
                .eventCode("999")
                .dateReceived(caseData.getClaimDetailsNotificationDate())
                .eventDetailsText("Claim details notified.")
                .eventDetails(EventDetails.builder()
                                  .miscText("Claim details notified.")
                                  .build())
                .build();

            var eventHistory = mapper.buildEvents(caseData, BEARER_TOKEN);

            assertThat(eventHistory).isNotNull();
            assertThat(eventHistory)
                .extracting("miscellaneous")
                .asList()
                .containsExactly(claimIssuedEvent, claimNotifiedEvent, claimDetailsNotifiedEvent);
            String triggerReason = findLatestEventTriggerReason(eventHistory);
            assertTrue(triggerReason.matches("Claim details notified."));
        }

        @Test
        void shouldPrepareMiscellaneousEvent_whenClaimIssued() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
            Event claimIssuedEvent = Event.builder()
                .eventSequence(1)
                .eventCode("999")
                .dateReceived(caseData.getIssueDate().atStartOfDay())
                .eventDetailsText("Claim issued in CCD.")
                .eventDetails(EventDetails.builder()
                                  .miscText("Claim issued in CCD.")
                                  .build())
                .build();
            Event claimNotifiedEvent = Event.builder()
                .eventSequence(2)
                .eventCode("999")
                .dateReceived(caseData.getClaimNotificationDate())
                .eventDetailsText("Claimant has notified defendant.")
                .eventDetails(EventDetails.builder()
                                  .miscText("Claimant has notified defendant.")
                                  .build())
                .build();

            Event claimDetailsNotifiedEvent = Event.builder()
                .eventSequence(3)
                .eventCode("999")
                .dateReceived(caseData.getClaimDetailsNotificationDate())
                .eventDetailsText("Claim details notified.")
                .eventDetails(EventDetails.builder()
                                  .miscText("Claim details notified.")
                                  .build())
                .build();

            var eventHistory = mapper.buildEvents(caseData, BEARER_TOKEN);

            assertThat(eventHistory).isNotNull();
            assertThat(eventHistory)
                .extracting("miscellaneous")
                .asList()
                .containsExactly(claimIssuedEvent, claimNotifiedEvent, claimDetailsNotifiedEvent);
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
    class AcknowledgementOfService {
        @Nested
        class OneVOne {
            @Test
            void shouldHaveCorrectEvents_whenRespondentAcknowledges() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateNotificationAcknowledged()
                    .build();
                if (caseData.getRespondent2OrgRegistered() != null
                    && caseData.getRespondent2Represented() == null) {
                    caseData = caseData.toBuilder()
                        .respondent2Represented(YES)
                        .build();
                }
                Event expectedAcknowledgementOfServiceReceived = Event.builder()
                    .eventSequence(4)
                    .eventCode("38")
                    .dateReceived(caseData.getRespondent1AcknowledgeNotificationDate())
                    .litigiousPartyID("002")
                    .eventDetails(EventDetails.builder()
                                      .responseIntention(caseData.getRespondent1ClaimResponseIntentionType().getLabel())
                                      .build())
                    .eventDetailsText(format(
                        "responseIntention: %s",
                        caseData.getRespondent1ClaimResponseIntentionType().getLabel()
                    ))
                    .build();

                var eventHistory = mapper.buildEvents(caseData, BEARER_TOKEN);

                assertThat(eventHistory).isNotNull();
                assertThat(eventHistory).extracting("acknowledgementOfServiceReceived").asList()
                    .containsExactly(expectedAcknowledgementOfServiceReceived);
            }
        }

        @Nested
        class OneVTwoOneLegalRep {
            @Test
            void shouldHaveCorrectEvents_whenBothRepAcknowledges() {
                String expectedMiscText1 = "[1 of 2 - 2020-08-01] "
                    + "Defendant: Mr. Sole Trader has acknowledged: Defend all of the claim";
                String expectedMiscText2 = "[2 of 2 - 2020-08-01] "
                    + "Defendant: Mr. John Rambo has acknowledged: Defend all of the claim";

                CaseData caseData = CaseDataBuilder.builder()
                    .atStateNotificationAcknowledged1v2SameSolicitor()
                    .multiPartyClaimOneDefendantSolicitor()
                    .build();
                if (caseData.getRespondent2OrgRegistered() != null
                    && caseData.getRespondent2Represented() == null) {
                    caseData = caseData.toBuilder()
                        .respondent2Represented(YES)
                        .build();
                }
                List<Event> expectedAcknowledgementOfServiceReceived = List.of(
                    Event.builder()
                        .eventSequence(4)
                        .eventCode("38")
                        .dateReceived(caseData.getRespondent1AcknowledgeNotificationDate())
                        .litigiousPartyID("002")
                        .eventDetails(EventDetails.builder()
                                          .responseIntention(
                                              caseData.getRespondent1ClaimResponseIntentionType().getLabel())
                                          .build())
                        .eventDetailsText(expectedMiscText1)
                        .build(),
                    Event.builder()
                        .eventSequence(5)
                        .eventCode("38")
                        .dateReceived(caseData.getRespondent2AcknowledgeNotificationDate())
                        .litigiousPartyID("003")
                        .eventDetails(EventDetails.builder()
                                          .responseIntention(
                                              caseData.getRespondent2ClaimResponseIntentionType().getLabel())
                                          .build())
                        .eventDetailsText(expectedMiscText2)
                        .build()
                );

                var eventHistory = mapper.buildEvents(caseData, BEARER_TOKEN);

                assertThat(eventHistory).isNotNull();
                assertThat(eventHistory)
                    .extracting("acknowledgementOfServiceReceived").asList()
                    .containsExactly(
                        expectedAcknowledgementOfServiceReceived.get(0),
                        expectedAcknowledgementOfServiceReceived.get(1)
                    );

            }
        }

        @Nested
        class OneVTwoTwoLegalRep {
            @Test
            void shouldHaveCorrectEvents_whenBothRepAcknowledges() {
                String expectedMiscText1 =
                    "Defendant: Mr. Sole Trader has acknowledged: Defend all of the claim";
                String expectedMiscText2 =
                    "Defendant: Mr. John Rambo has acknowledged: Defend all of the claim";

                CaseData caseData = CaseDataBuilder.builder()
                    .atStateNotificationAcknowledged1v2SameSolicitor()
                    .multiPartyClaimTwoDefendantSolicitors()
                    .build();
                if (caseData.getRespondent2OrgRegistered() != null
                    && caseData.getRespondent2Represented() == null) {
                    caseData = caseData.toBuilder()
                        .respondent2Represented(YES)
                        .build();
                }
                List<Event> expectedAcknowledgementOfServiceReceived = List.of(
                    Event.builder()
                        .eventSequence(4)
                        .eventCode("38")
                        .dateReceived(caseData.getRespondent1AcknowledgeNotificationDate())
                        .litigiousPartyID("002")
                        .eventDetails(EventDetails.builder()
                                          .responseIntention(
                                              caseData.getRespondent1ClaimResponseIntentionType().getLabel())
                                          .build())
                        .eventDetailsText(expectedMiscText1)
                        .build(),
                    Event.builder()
                        .eventSequence(5)
                        .eventCode("38")
                        .dateReceived(caseData.getRespondent2AcknowledgeNotificationDate())
                        .litigiousPartyID("003")
                        .eventDetails(EventDetails.builder()
                                          .responseIntention(
                                              caseData.getRespondent2ClaimResponseIntentionType().getLabel())
                                          .build())
                        .eventDetailsText(expectedMiscText2)
                        .build()
                );

                var eventHistory = mapper.buildEvents(caseData, BEARER_TOKEN);

                assertThat(eventHistory).isNotNull();
                assertThat(eventHistory)
                    .extracting("acknowledgementOfServiceReceived").asList()
                    .containsExactly(
                        expectedAcknowledgementOfServiceReceived.get(0),
                        expectedAcknowledgementOfServiceReceived.get(1)
                    );
            }

            @Test
            void shouldHaveCorrectEvents_whenOnlyRespondentOneRepAcknowledges() {
                String expectedMiscText1 =
                    "Defendant: Mr. Sole Trader has acknowledged: Defend all of the claim";

                CaseData caseData = CaseDataBuilder.builder()
                    .atStateNotificationAcknowledged1v2SameSolicitor()
                    .multiPartyClaimTwoDefendantSolicitors()
                    .respondent2AcknowledgeNotificationDate(null)
                    .build();
                if (caseData.getRespondent2OrgRegistered() != null
                    && caseData.getRespondent2Represented() == null) {
                    caseData = caseData.toBuilder()
                        .respondent2Represented(YES)
                        .build();
                }
                Event expectedAcknowledgementOfServiceReceivedEvent =
                    Event.builder()
                        .eventSequence(4)
                        .eventCode("38")
                        .dateReceived(caseData.getRespondent1AcknowledgeNotificationDate())
                        .litigiousPartyID("002")
                        .eventDetails(EventDetails.builder()
                                          .responseIntention(
                                              caseData.getRespondent1ClaimResponseIntentionType().getLabel())
                                          .build())
                        .eventDetailsText(expectedMiscText1)
                        .build();

                var eventHistory = mapper.buildEvents(caseData, BEARER_TOKEN);

                assertThat(eventHistory).isNotNull();
                assertThat(eventHistory)
                    .extracting("acknowledgementOfServiceReceived").asList()
                    .containsExactly(expectedAcknowledgementOfServiceReceivedEvent);
            }

            @Test
            void shouldHaveCorrectEvents_whenOnlyRespondentTwoRepAcknowledges() {
                String expectedMiscText1 =
                    "Defendant: Mr. John Rambo has acknowledged: Defend all of the claim";

                CaseData caseData = CaseDataBuilder.builder()
                    .atStateNotificationAcknowledged1v2SameSolicitor()
                    .multiPartyClaimTwoDefendantSolicitors()
                    .respondent1AcknowledgeNotificationDate(null)
                    .build();
                if (caseData.getRespondent2OrgRegistered() != null
                    && caseData.getRespondent2Represented() == null) {
                    caseData = caseData.toBuilder()
                        .respondent2Represented(YES)
                        .build();
                }
                Event expectedAcknowledgementOfServiceReceivedEvent =
                    Event.builder()
                        .eventSequence(4)
                        .eventCode("38")
                        .dateReceived(caseData.getRespondent2AcknowledgeNotificationDate())
                        .litigiousPartyID("003")
                        .eventDetails(EventDetails.builder()
                                          .responseIntention(
                                              caseData.getRespondent2ClaimResponseIntentionType().getLabel())
                                          .build())
                        .eventDetailsText(expectedMiscText1)
                        .build();

                var eventHistory = mapper.buildEvents(caseData, BEARER_TOKEN);

                assertThat(eventHistory).isNotNull();
                assertThat(eventHistory)
                    .extracting("acknowledgementOfServiceReceived").asList()
                    .containsExactly(expectedAcknowledgementOfServiceReceivedEvent);
            }
        }

        @Nested
        class TwoVOne {
            @Test
            void shouldHaveCorrectEvents_whenRespondentAcknowledges() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateNotificationAcknowledged()
                    .multiPartyClaimTwoApplicants()
                    .build();
                if (caseData.getRespondent2OrgRegistered() != null
                    && caseData.getRespondent2Represented() == null) {
                    caseData = caseData.toBuilder()
                        .respondent2Represented(YES)
                        .build();
                }
                Event expectedAcknowledgementOfServiceReceived = Event.builder()
                    .eventSequence(4)
                    .eventCode("38")
                    .dateReceived(caseData.getRespondent1AcknowledgeNotificationDate())
                    .litigiousPartyID("002")
                    .eventDetails(EventDetails.builder()
                                      .responseIntention(caseData.getRespondent1ClaimResponseIntentionType().getLabel())
                                      .build())
                    .eventDetailsText(format(
                        "responseIntention: %s",
                        caseData.getRespondent1ClaimResponseIntentionType().getLabel()
                    ))
                    .build();

                var eventHistory = mapper.buildEvents(caseData, BEARER_TOKEN);

                assertThat(eventHistory).isNotNull();
                assertThat(eventHistory).extracting("acknowledgementOfServiceReceived").asList()
                    .containsExactly(expectedAcknowledgementOfServiceReceived);
            }
        }
    }

    @Nested
    class NotifyTimeExtensionAcknowledged {

        Map<PartyRole, String> partyLitIdMap = Map.of(
            RESPONDENT_ONE, "002",
            RESPONDENT_TWO, "003"
        );

        @Nested
        class OneVOne {

            @Test
            void shouldPrepareExpectedEvents() {
                CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotifiedTimeExtension().build();
                if (caseData.getRespondent2OrgRegistered() != null
                    && caseData.getRespondent2Represented() == null) {
                    caseData = caseData.toBuilder()
                        .respondent2Represented(YES)
                        .build();
                }
                Event deadlineExtendedEvent = expectedDeadLineExtendedEvent(
                    PartyUtils.respondent1Data(caseData),
                    format("agreed extension date: %s", caseData.getRespondentSolicitor1AgreedDeadlineExtension()
                        .format(DateTimeFormatter.ofPattern("dd MM yyyy")))
                );

                var eventHistory = mapper.buildEvents(caseData, BEARER_TOKEN);

                assertThat(eventHistory).isNotNull();
                assertThat(eventHistory).extracting("consentExtensionFilingDefence").asList()
                    .containsExactly(deadlineExtendedEvent);
            }
        }

        @Nested
        class OneVTwo {

            @Test
            void shouldPrepareExpectedEvents_Respondent1RequestsTimeExtensionDifferentSolicitorScenario() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atState1v2DifferentSolicitorClaimDetailsRespondent1NotifiedTimeExtension().build();
                if (caseData.getRespondent2OrgRegistered() != null
                    && caseData.getRespondent2Represented() == null) {
                    caseData = caseData.toBuilder()
                        .respondent2Represented(YES)
                        .build();
                }
                Event deadlineExtendedEvent = expectedDeadLineExtendedEvent(
                    PartyUtils.respondent1Data(caseData),
                    format(
                        "Defendant: Mr. Sole Trader has agreed extension: %s",
                        caseData.getRespondentSolicitor1AgreedDeadlineExtension()
                            .format(DateTimeFormatter.ofPattern("dd MM yyyy"))
                    )
                );

                var eventHistory = mapper.buildEvents(caseData, BEARER_TOKEN);

                assertThat(eventHistory).isNotNull();
                assertThat(eventHistory).extracting("consentExtensionFilingDefence").asList()
                    .containsExactly(deadlineExtendedEvent);
            }

            @Test
            void shouldPrepareExpectedEvents_Respondent2RequestsTimeExtensionDifferentSolicitorScenario() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atState1v2DifferentSolicitorClaimDetailsRespondent2NotifiedTimeExtension().build();
                if (caseData.getRespondent2OrgRegistered() != null
                    && caseData.getRespondent2Represented() == null) {
                    caseData = caseData.toBuilder()
                        .respondent2Represented(YES)
                        .build();
                }
                Event deadlineExtendedEvent = expectedDeadLineExtendedEvent(
                    PartyUtils.respondent2Data(caseData),
                    format(
                        "Defendant: Mr. John Rambo has agreed extension: %s",
                        caseData.getRespondentSolicitor2AgreedDeadlineExtension()
                            .format(DateTimeFormatter.ofPattern("dd MM yyyy"))
                    )
                );

                var eventHistory = mapper.buildEvents(caseData, BEARER_TOKEN);

                assertThat(eventHistory).isNotNull();
                assertThat(eventHistory).extracting("consentExtensionFilingDefence").asList()
                    .containsExactly(deadlineExtendedEvent);
            }

            @Test
            void shouldPrepareExpectedEvents_SameSolicitorScenario() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atState1v2SameSolicitorClaimDetailsRespondentNotifiedTimeExtension().build();
                if (caseData.getRespondent2OrgRegistered() != null
                    && caseData.getRespondent2Represented() == null) {
                    caseData = caseData.toBuilder()
                        .respondent2Represented(YES)
                        .build();
                }
                Event deadlineExtendedEvent = expectedDeadLineExtendedEvent(
                    PartyUtils.respondent1Data(caseData),
                    format(
                        "Defendant(s) have agreed extension: %s",
                        caseData.getRespondentSolicitor1AgreedDeadlineExtension()
                            .format(DateTimeFormatter.ofPattern("dd MM yyyy"))
                    )
                );

                var eventHistory = mapper.buildEvents(caseData, BEARER_TOKEN);

                assertThat(eventHistory).isNotNull();
                assertThat(eventHistory).extracting("consentExtensionFilingDefence").asList()
                    .containsExactly(deadlineExtendedEvent);
            }
        }

        public Event expectedDeadLineExtendedEvent(PartyData partyData, String expectedMessage) {
            return Event.builder()
                .eventSequence(4)
                .eventCode("45")
                .dateReceived(partyData.getTimeExtensionDate())
                .litigiousPartyID(partyLitIdMap.get(partyData.getRole()))
                .eventDetails(EventDetails.builder()
                                  .agreedExtensionDate(partyData.getSolicitorAgreedDeadlineExtension()
                                                           .format(ISO_DATE))
                                  .build())
                .eventDetailsText(expectedMessage)
                .build();
        }
    }

    @Nested
    class RespondentFullAdmission {

        @Test
        void shouldPrepareExpectedEvents_whenClaimWithRespondentFullAdmissionWithOptionalEvents() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateNotificationAcknowledgedRespondent1TimeExtension()
                .atStateRespondentFullAdmissionAfterNotificationAcknowledged()
                .build();
            if (caseData.getRespondent2OrgRegistered() != null
                && caseData.getRespondent2Represented() == null) {
                caseData = caseData.toBuilder()
                    .respondent2Represented(YES)
                    .build();
            }
            Event expectedReceiptOfAdmission = Event.builder()
                .eventSequence(6)
                .eventCode("40")
                .dateReceived(caseData.getRespondent1ResponseDate())
                .litigiousPartyID("002")
                .build();
            List<Event> expectedMiscellaneousEvents = List.of(
                Event.builder()
                    .eventSequence(1)
                    .eventCode("999")
                    .dateReceived(caseData.getIssueDate().atStartOfDay())
                    .eventDetailsText("Claim issued in CCD.")
                    .eventDetails(EventDetails.builder()
                                      .miscText("Claim issued in CCD.")
                                      .build())
                    .build(),
                Event.builder()
                    .eventSequence(2)
                    .eventCode("999")
                    .dateReceived(caseData.getClaimNotificationDate())
                    .eventDetailsText("Claimant has notified defendant.")
                    .eventDetails(EventDetails.builder()
                                      .miscText("Claimant has notified defendant.")
                                      .build())
                    .build(),
                Event.builder()
                    .eventSequence(3)
                    .eventCode("999")
                    .dateReceived(caseData.getClaimDetailsNotificationDate())
                    .eventDetailsText("Claim details notified.")
                    .eventDetails(EventDetails.builder()
                                      .miscText("Claim details notified.")
                                      .build())
                    .build(),
                Event.builder()
                    .eventSequence(7)
                    .eventCode("999")
                    .dateReceived(caseData.getRespondent1ResponseDate())
                    .eventDetailsText("RPA Reason: Defendant fully admits.")
                    .eventDetails(EventDetails.builder()
                                      .miscText("RPA Reason: Defendant fully admits.")
                                      .build())
                    .build()
            );
            Event expectedAcknowledgementOfServiceReceived = Event.builder()
                .eventSequence(4)
                .eventCode("38")
                .dateReceived(caseData.getRespondent1AcknowledgeNotificationDate())
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
                .eventSequence(5)
                .eventCode("45")
                .dateReceived(caseData.getRespondent1TimeExtensionDate())
                .litigiousPartyID("002")
                .eventDetails(EventDetails.builder()
                                  .agreedExtensionDate(caseData.getRespondentSolicitor1AgreedDeadlineExtension()
                                                           .format(ISO_DATE))
                                  .build())
                .eventDetailsText(format("agreed extension date: %s", caseData
                    .getRespondentSolicitor1AgreedDeadlineExtension()
                    .format(DateTimeFormatter.ofPattern("dd MM yyyy"))))
                .build();

            var eventHistory = mapper.buildEvents(caseData, BEARER_TOKEN);

            assertThat(eventHistory).isNotNull();
            assertThat(eventHistory).extracting("receiptOfAdmission").asList()
                .containsExactly(expectedReceiptOfAdmission);
            assertThat(eventHistory).extracting("miscellaneous").asList()
                .containsExactly(expectedMiscellaneousEvents.get(0), expectedMiscellaneousEvents.get(1),
                                 expectedMiscellaneousEvents.get(2), expectedMiscellaneousEvents.get(3)
            );
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
                .atStateRespondentFullAdmissionAfterNotificationAcknowledged()
                .respondent1AcknowledgeNotificationDate(null)
                .build();
            if (caseData.getRespondent2OrgRegistered() != null
                && caseData.getRespondent2Represented() == null) {
                caseData = caseData.toBuilder()
                    .respondent2Represented(YES)
                    .build();
            }
            Event expectedReceiptOfAdmission = Event.builder()
                .eventSequence(4)
                .eventCode("40")
                .dateReceived(caseData.getRespondent1ResponseDate())
                .litigiousPartyID("002")
                .build();
            List<Event> expectedMiscellaneousEvents = List.of(

                Event.builder()
                    .eventSequence(1)
                    .eventCode("999")
                    .dateReceived(caseData.getIssueDate().atStartOfDay())
                    .eventDetailsText("Claim issued in CCD.")
                    .eventDetails(EventDetails.builder()
                                      .miscText("Claim issued in CCD.")
                                      .build())
                    .build(),
                Event.builder()
                    .eventSequence(2)
                    .eventCode("999")
                    .dateReceived(caseData.getClaimNotificationDate())
                    .eventDetailsText("Claimant has notified defendant.")
                    .eventDetails(EventDetails.builder()
                                      .miscText("Claimant has notified defendant.")
                                      .build())
                    .build(),
                Event.builder()
                    .eventSequence(3)
                    .eventCode("999")
                    .dateReceived(caseData.getClaimDetailsNotificationDate())
                    .eventDetailsText("Claim details notified.")
                    .eventDetails(EventDetails.builder()
                                      .miscText("Claim details notified.")
                                      .build())
                    .build(),
                Event.builder()
                    .eventSequence(5)
                    .eventCode("999")
                    .dateReceived(caseData.getRespondent1ResponseDate())
                    .eventDetailsText("RPA Reason: Defendant fully admits.")
                    .eventDetails(EventDetails.builder()
                                      .miscText("RPA Reason: Defendant fully admits.")
                                      .build())
                    .build()
            );

            var eventHistory = mapper.buildEvents(caseData, BEARER_TOKEN);

            assertThat(eventHistory).isNotNull();
            assertThat(eventHistory).extracting("receiptOfAdmission").asList()
                .containsExactly(expectedReceiptOfAdmission);
            assertThat(eventHistory).extracting("miscellaneous").asList()
                .containsExactly(expectedMiscellaneousEvents.get(0), expectedMiscellaneousEvents.get(1),
                                 expectedMiscellaneousEvents.get(2), expectedMiscellaneousEvents.get(3)
            );

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

        @Test
        void shouldPrepareExpectedEvents_whenClaimWith1v2DiffSolicitorBothRespondentFullAdmissionNoOptionalEvents() {
            CaseData caseData = CaseDataBuilder.builder()
                .multiPartyClaimTwoDefendantSolicitors()
                .atStateBothRespondentsSameResponse(FULL_ADMISSION)
                .build();
            if (caseData.getRespondent2OrgRegistered() != null
                && caseData.getRespondent2Represented() == null) {
                caseData = caseData.toBuilder()
                    .respondent2Represented(YES)
                    .build();
            }
            List<Event> expectedReceiptOfAdmission = List.of(
                Event.builder()
                    .eventSequence(4)
                    .eventCode("40")
                    .dateReceived(caseData.getRespondent1ResponseDate())
                    .litigiousPartyID("002")
                    .build(),
                Event.builder()
                    .eventSequence(6)
                    .eventCode("40")
                    .dateReceived(caseData.getRespondent2ResponseDate())
                    .litigiousPartyID("003")
                    .build()
            );
            String respondent1MiscText =
                mapper.prepareRespondentResponseText(caseData, caseData.getRespondent1(), true);
            String respondent2MiscText =
                mapper.prepareRespondentResponseText(caseData, caseData.getRespondent2(), false);

            List<Event> expectedMiscellaneousEvents = List.of(
                Event.builder()
                    .eventSequence(1)
                    .eventCode("999")
                    .dateReceived(caseData.getIssueDate().atStartOfDay())
                    .eventDetailsText("Claim issued in CCD.")
                    .eventDetails(EventDetails.builder()
                                      .miscText("Claim issued in CCD.")
                                      .build())
                    .build(),
                Event.builder()
                    .eventSequence(2)
                    .eventCode("999")
                    .dateReceived(caseData.getClaimNotificationDate())
                    .eventDetailsText("Claimant has notified defendant.")
                    .eventDetails(EventDetails.builder()
                                      .miscText("Claimant has notified defendant.")
                                      .build())
                    .build(),
                Event.builder()
                    .eventSequence(3)
                    .eventCode("999")
                    .dateReceived(caseData.getClaimDetailsNotificationDate())
                    .eventDetailsText("Claim details notified.")
                    .eventDetails(EventDetails.builder()
                                      .miscText("Claim details notified.")
                                      .build())
                    .build(),
                Event.builder()
                    .eventSequence(5)
                    .eventCode("999")
                    .dateReceived(caseData.getRespondent1ResponseDate())
                    .eventDetailsText(respondent1MiscText)
                    .eventDetails(EventDetails.builder()
                                      .miscText(respondent1MiscText)
                                      .build())
                    .build(),
                Event.builder()
                    .eventSequence(7)
                    .eventCode("999")
                    .dateReceived(caseData.getRespondent2ResponseDate())
                    .eventDetailsText(respondent2MiscText)
                    .eventDetails(EventDetails.builder()
                                      .miscText(respondent2MiscText)
                                      .build())
                    .build()
            );

            var eventHistory = mapper.buildEvents(caseData, BEARER_TOKEN);

            assertThat(eventHistory).isNotNull();
            assertThat(eventHistory).extracting("receiptOfAdmission").asList()
                .containsExactly(expectedReceiptOfAdmission.get(0), expectedReceiptOfAdmission.get(1));
            assertThat(eventHistory).extracting("miscellaneous").asList()
                .containsExactly(expectedMiscellaneousEvents.get(0),
                                 expectedMiscellaneousEvents.get(1), expectedMiscellaneousEvents.get(2),
                                 expectedMiscellaneousEvents.get(3), expectedMiscellaneousEvents.get(4)
            );

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

        @Test
        void shouldPrepareExpectedEvents_whenClaimWith1v2SameSolicitorBothRespondentFullAdmissionNoOptionalEvents() {
            CaseData caseData = CaseDataBuilder.builder()
                .multiPartyClaimOneDefendantSolicitor()
                .atStateBothRespondentsSameResponse1v2SameSolicitor(FULL_ADMISSION)
                .respondentResponseIsSame(YES)
                .build();
            if (caseData.getRespondent2OrgRegistered() != null
                && caseData.getRespondent2Represented() == null) {
                caseData = caseData.toBuilder()
                    .respondent2Represented(YES)
                    .build();
            }
            List<Event> expectedReceiptOfAdmission = List.of(
                Event.builder()
                    .eventSequence(4)
                    .eventCode("40")
                    .dateReceived(caseData.getRespondent1ResponseDate())
                    .litigiousPartyID("002")
                    .build(),
                Event.builder()
                    .eventSequence(5)
                    .eventCode("40")
                    .dateReceived(caseData.getRespondent2ResponseDate())
                    .litigiousPartyID("003")
                    .build()
            );
            String respondent1MiscText =
                mapper.prepareRespondentResponseText(caseData, caseData.getRespondent1(), true);
            String respondent2MiscText =
                mapper.prepareRespondentResponseText(caseData, caseData.getRespondent2(), false);
            List<Event> expectedMiscellaneousEvents = List.of(
                Event.builder()
                    .eventSequence(1)
                    .eventCode("999")
                    .dateReceived(caseData.getIssueDate().atStartOfDay())
                    .eventDetailsText("Claim issued in CCD.")
                    .eventDetails(EventDetails.builder()
                                      .miscText("Claim issued in CCD.")
                                      .build())
                    .build(),
                Event.builder()
                    .eventSequence(2)
                    .eventCode("999")
                    .dateReceived(caseData.getClaimNotificationDate())
                    .eventDetailsText("Claimant has notified defendant.")
                    .eventDetails(EventDetails.builder()
                                      .miscText("Claimant has notified defendant.")
                                      .build())
                    .build(),
                Event.builder()
                    .eventSequence(3)
                    .eventCode("999")
                    .dateReceived(caseData.getClaimDetailsNotificationDate())
                    .eventDetailsText("Claim details notified.")
                    .eventDetails(EventDetails.builder()
                                      .miscText("Claim details notified.")
                                      .build())
                    .build(),
                Event.builder()
                    .eventSequence(6)
                    .eventCode("999")
                    .dateReceived(caseData.getRespondent1ResponseDate())
                    .eventDetailsText(respondent1MiscText)
                    .eventDetails(EventDetails.builder()
                                      .miscText(respondent1MiscText)
                                      .build())
                    .build(),
                Event.builder()
                    .eventSequence(7)
                    .eventCode("999")
                    .dateReceived(caseData.getRespondent2ResponseDate())
                    .eventDetailsText(respondent2MiscText)
                    .eventDetails(EventDetails.builder()
                                      .miscText(respondent2MiscText)
                                      .build())
                    .build()
            );
            var eventHistory = mapper.buildEvents(caseData, BEARER_TOKEN);
            assertThat(eventHistory).isNotNull();
            assertThat(eventHistory).extracting("receiptOfAdmission").asList()
                .containsExactly(expectedReceiptOfAdmission.get(0), expectedReceiptOfAdmission.get(1));
            assertThat(eventHistory).extracting("miscellaneous").asList()
                .containsExactly(expectedMiscellaneousEvents.get(0),
                                 expectedMiscellaneousEvents.get(1), expectedMiscellaneousEvents.get(2),
                                 expectedMiscellaneousEvents.get(3), expectedMiscellaneousEvents.get(4)
            );

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
    class RespondentFullDefenceSpec {
        @Test
        void shouldPrepareExpectedEvents_whenClaimWithFullDefence1v1WithoutOptionalEventsSDO() {

            CaseData caseData = CaseDataBuilder.builder()
                .setClaimTypeToSpecClaim()
                .atStateTakenOfflineSDONotDrawn(MultiPartyScenario.ONE_V_ONE)
                .atState(TAKEN_OFFLINE_SDO_NOT_DRAWN)
                .atStateRespondent1v1FullDefenceSpec()
                .applicant1ProceedWithClaim(YES)
                .respondent1AcknowledgeNotificationDate(null)
                .build();
            if (caseData.getRespondent2OrgRegistered() != null
                && caseData.getRespondent2Represented() == null) {
                caseData = caseData.toBuilder()
                    .respondent2Represented(YES)
                    .build();
            }
            Event expectedDefenceFiled = Event.builder()
                .eventSequence(2)
                .eventCode("50")
                .dateReceived(caseData.getRespondent1ResponseDate())
                .litigiousPartyID("002")
                .build();
            List<Event> expectedDirectionsQuestionnaireFiled =
                List.of(
                    Event.builder()
                        .eventSequence(3)
                        .eventCode("197")
                        .dateReceived(caseData.getRespondent1ResponseDate())
                        .litigiousPartyID("002")
                        .eventDetailsText(mapper.prepareFullDefenceEventText(
                            caseData.getRespondent1DQ(),
                            caseData,
                            true,
                            caseData.getRespondent1()
                        ))
                        .eventDetails(EventDetails.builder()
                                          .stayClaim(mapper.isStayClaim(caseData.getRespondent1DQ()))
                                          .preferredCourtCode(mapper.getPreferredCourtCode(
                                              caseData.getRespondent1DQ()))
                                          .preferredCourtName("")
                                          .build())
                        .build(),
                    Event.builder()
                        .eventSequence(5)
                        .eventCode("197")
                        .dateReceived(caseData.getRespondent1ResponseDate())
                        .litigiousPartyID("002")
                        .eventDetailsText(mapper.prepareFullDefenceEventText(
                            caseData.getRespondent1DQ(),
                            caseData,
                            true,
                            caseData.getRespondent1()
                        ))
                        .eventDetails(EventDetails.builder()
                                          .stayClaim(mapper.isStayClaim(caseData.getRespondent1DQ()))
                                          .preferredCourtCode(
                                              mapper.getPreferredCourtCode(caseData.getRespondent1DQ()))
                                          .preferredCourtName("")
                                          .build())
                        .build()
                );
            List<Event> expectedMiscellaneousEvents = List.of();

            var eventHistory = mapper.buildEvents(caseData, BEARER_TOKEN);
            assertThat(eventHistory).isNotNull();
            assertThat(eventHistory).extracting("defenceFiled").asList()
                .containsExactly(expectedDefenceFiled);
            assertThat(eventHistory).extracting("directionsQuestionnaireFiled").asList()
                .contains(expectedDirectionsQuestionnaireFiled.get(0));
            assertEmptyEvents(
                eventHistory,
                "receiptOfAdmission",
                "receiptOfPartAdmission",
                "acknowledgementOfServiceReceived",
                "consentExtensionFilingDefence"
            );
        }

        @Test
        void shouldPrepareExpectedEvents_whenClaimWithFullDefence1v1AllPaid() {
            BigDecimal claimValue = BigDecimal.valueOf(1000);
            CaseData caseData = CaseDataBuilder.builder()
                .setClaimTypeToSpecClaim()
                .atStateApplicantRespondToDefenceAndProceed(MultiPartyScenario.ONE_V_ONE)
                .atState(FlowState.Main.FULL_DEFENCE)
                .atStateRespondent1v1FullDefenceSpec()
                .respondent1AcknowledgeNotificationDate(null)
                .totalClaimAmount(claimValue)
                .build().toBuilder()
                .respondToClaim(RespondToClaim.builder()
                                    .howMuchWasPaid(BigDecimal.valueOf(100000))
                                    .build())
                .build();
            Event expectedDefenceFiled = Event.builder()
                .eventSequence(2)
                .eventCode("49")
                .dateReceived(caseData.getRespondent1ResponseDate())
                .litigiousPartyID("002")
                .build();
            List<Event> expectedDirectionsQuestionnaireFiled =
                List.of(
                    Event.builder()
                        .eventSequence(3)
                        .eventCode("197")
                        .dateReceived(caseData.getRespondent1ResponseDate())
                        .litigiousPartyID("002")
                        .eventDetailsText(mapper.prepareFullDefenceEventText(
                            caseData.getRespondent1DQ(),
                            caseData,
                            true,
                            caseData.getRespondent1()
                        ))
                        .eventDetails(EventDetails.builder()
                                          .stayClaim(mapper.isStayClaim(caseData.getRespondent1DQ()))
                                          .preferredCourtCode(mapper.getPreferredCourtCode(
                                              caseData.getRespondent1DQ()))
                                          .preferredCourtName("")
                                          .build())
                        .build(),
                    Event.builder()
                        .eventSequence(5)
                        .eventCode("197")
                        .dateReceived(caseData.getRespondent1ResponseDate())
                        .litigiousPartyID("002")
                        .eventDetailsText(mapper.prepareFullDefenceEventText(
                            caseData.getRespondent1DQ(),
                            caseData,
                            true,
                            caseData.getRespondent1()
                        ))
                        .eventDetails(EventDetails.builder()
                                          .stayClaim(mapper.isStayClaim(caseData.getRespondent1DQ()))
                                          .preferredCourtCode(
                                              mapper.getPreferredCourtCode(caseData.getRespondent1DQ()))
                                          .preferredCourtName("")
                                          .build())
                        .build()
                );

            var eventHistory = mapper.buildEvents(caseData, BEARER_TOKEN);

            assertThat(eventHistory).isNotNull();
            assertThat(eventHistory).extracting("statesPaid").asList()
                .containsExactly(expectedDefenceFiled);
            assertThat(eventHistory).extracting("directionsQuestionnaireFiled").asList()
                .contains(expectedDirectionsQuestionnaireFiled.get(0));

            assertEmptyEvents(
                eventHistory,
                "receiptOfAdmission",
                "receiptOfPartAdmission",
                "acknowledgementOfServiceReceived",
                "consentExtensionFilingDefence"
            );
        }
    }

    @Nested
    class RespondentFullAdmissionSpec {
        @Test
        void shouldPrepareExpectedEvents_when1v1ClaimWithRespondentFullAdmissionToBoth() {
            CaseData caseData = CaseDataBuilder.builder()
                .setClaimTypeToSpecClaim()
                .atStateSpec1v1ClaimSubmitted()
                .atStateRespondent1v1FullAdmissionSpec()
                .build();
            if (caseData.getRespondent2OrgRegistered() != null
                && caseData.getRespondent2Represented() == null) {
                caseData = caseData.toBuilder()
                    .respondent2Represented(YES)
                    .build();
            }
            Event expectedReceiptOfAdmission = Event.builder()
                .eventSequence(2)
                .eventCode("40")
                .dateReceived(caseData.getRespondent1ResponseDate())
                .litigiousPartyID("002")
                .build();

            List<Event> expectedMiscellaneousEvents = List.of(
                Event.builder()
                    .eventSequence(1)
                    .eventCode("999")
                    .dateReceived(caseData.getIssueDate().atStartOfDay())
                    .eventDetailsText("Claim issued in CCD.")
                    .eventDetails(EventDetails.builder()
                                      .miscText("Claim issued in CCD.")
                                      .build())
                    .build()
            );

            var eventHistory = mapper.buildEvents(caseData, BEARER_TOKEN);

            assertThat(eventHistory).isNotNull();
            assertThat(eventHistory).extracting("receiptOfAdmission").asList()
                .containsExactly(expectedReceiptOfAdmission);
            assertThat(eventHistory).extracting("miscellaneous").asList()
                .containsExactly(expectedMiscellaneousEvents.get(0));

            assertEmptyEvents(
                eventHistory,
                "defenceFiled",
                "defenceAndCounterClaim",
                "receiptOfPartAdmission",
                "consentExtensionFilingDefence",
                "replyToDefence",
                "directionsQuestionnaireFiled"
            );
        }

        @Test
        void shouldPrepareExpectedEvents_when2v1ClaimWithRespondentFullAdmissionToBoth() {
            CaseData caseData = CaseDataBuilder.builder()
                .setClaimTypeToSpecClaim()
                .atStateSpec2v1ClaimSubmitted()
                .atStateRespondent2v1FullAdmission()
                .build();
            if (caseData.getRespondent2OrgRegistered() != null
                && caseData.getRespondent2Represented() == null) {
                caseData = caseData.toBuilder()
                    .respondent2Represented(YES)
                    .build();
            }
            Event expectedReceiptOfAdmission = Event.builder()
                .eventSequence(2)
                .eventCode("40")
                .dateReceived(caseData.getRespondent1ResponseDate())
                .litigiousPartyID("002")
                .build();

            List<Event> expectedMiscellaneousEvents = List.of(
                Event.builder()
                    .eventSequence(1)
                    .eventCode("999")
                    .dateReceived(caseData.getIssueDate().atStartOfDay())
                    .eventDetailsText("Claim issued in CCD.")
                    .eventDetails(EventDetails.builder()
                                      .miscText("Claim issued in CCD.")
                                      .build())
                    .build()
            );

            var eventHistory = mapper.buildEvents(caseData, BEARER_TOKEN);

            assertThat(eventHistory).isNotNull();
            assertThat(eventHistory).extracting("receiptOfAdmission").asList()
                .containsExactly(expectedReceiptOfAdmission);
            assertThat(eventHistory).extracting("miscellaneous").asList()
                .containsExactly(expectedMiscellaneousEvents.get(0));

            assertEmptyEvents(
                eventHistory,
                "defenceFiled",
                "defenceAndCounterClaim",
                "receiptOfPartAdmission",
                "consentExtensionFilingDefence",
                "replyToDefence",
                "directionsQuestionnaireFiled"
            );
        }

        @Test
        void shouldPrepareExpectedEvents_when1v2ClaimWithRespondentFullAdmissionToBoth() {
            CaseData caseData = CaseDataBuilder.builder()
                .setClaimTypeToSpecClaim()
                .atStateSpec1v2ClaimSubmitted()
                .atStateRespondent1v2FullAdmission()
                .build();
            if (caseData.getRespondent2OrgRegistered() != null
                && caseData.getRespondent2Represented() == null) {
                caseData = caseData.toBuilder()
                    .respondent2Represented(YES)
                    .build();
            }
            Event expectedReceiptOfAdmission = Event.builder()
                .eventSequence(2)
                .eventCode("40")
                .dateReceived(caseData.getRespondent1ResponseDate())
                .litigiousPartyID("002")
                .build();

            Event expectedMiscellaneousEvents = Event.builder()
                    .eventSequence(1)
                    .eventCode("999")
                    .dateReceived(caseData.getIssueDate().atStartOfDay())
                    .eventDetailsText("Claim issued in CCD.")
                    .eventDetails(EventDetails.builder()
                                      .miscText("Claim issued in CCD.")
                                      .build())
                    .build();

            var eventHistory = mapper.buildEvents(caseData, BEARER_TOKEN);

            assertThat(eventHistory).isNotNull();
            assertThat(eventHistory).extracting("receiptOfAdmission").asList()
                .containsExactly(expectedReceiptOfAdmission);
            assertThat(eventHistory).extracting("miscellaneous").asList()
                .containsExactly(expectedMiscellaneousEvents);

            assertEmptyEvents(
                eventHistory,
                "defenceFiled",
                "defenceAndCounterClaim",
                "receiptOfPartAdmission",
                "consentExtensionFilingDefence",
                "replyToDefence",
                "directionsQuestionnaireFiled"
            );
        }

    }

    @Nested
    class RespondentPartAdmissionSpec {

        @Test
        void shouldPrepareExpectedEvents_when2v1ClaimWithRespondentPartAdmissionToBoth() {
            CaseData caseData = CaseDataBuilder.builder()
                .setClaimTypeToSpecClaim()
                .atStateSpec2v1ClaimSubmitted()
                .atStateRespondent2v1PartAdmission()
                .respondent1DQ()
                .build();
            if (caseData.getRespondent2OrgRegistered() != null
                && caseData.getRespondent2Represented() == null) {
                caseData = caseData.toBuilder()
                    .respondent2Represented(YES)
                    .build();
            }
            Event expectedReceiptOfPartAdmission = Event.builder()
                .eventSequence(2)
                .eventCode("60")
                .dateReceived(caseData.getRespondent1ResponseDate())
                .litigiousPartyID("002")
                .build();

            Event expectedMiscellaneousEvents = Event.builder()
                    .eventSequence(1)
                    .eventCode("999")
                    .dateReceived(caseData.getIssueDate().atStartOfDay())
                    .eventDetailsText("Claim issued in CCD.")
                    .eventDetails(EventDetails.builder()
                                      .miscText("Claim issued in CCD.")
                                      .build())
                    .build();

            List<Event> expectedDirectionsQuestionnaireFiled = List.of(
                Event.builder()
                    .eventSequence(3)
                    .eventCode("197")
                    .dateReceived(caseData.getRespondent1ResponseDate())
                    .litigiousPartyID("002")
                    .eventDetailsText(mapper.prepareFullDefenceEventText(
                        caseData.getRespondent1DQ(),
                        caseData,
                        true,
                        caseData.getRespondent1()
                    ))
                    .eventDetails(EventDetails.builder()
                                      .stayClaim(mapper.isStayClaim(caseData.getRespondent1DQ()))
                                      .preferredCourtCode(mapper.getPreferredCourtCode(caseData.getRespondent1DQ()))
                                      .preferredCourtName("")
                                      .build())
                    .build());

            var eventHistory = mapper.buildEvents(caseData, BEARER_TOKEN);

            assertThat(eventHistory).isNotNull();
            assertThat(eventHistory).extracting("receiptOfPartAdmission").asList()
                .containsExactly(expectedReceiptOfPartAdmission);
            assertThat(eventHistory).extracting("miscellaneous").asList()
                .containsExactly(expectedMiscellaneousEvents);
            assertThat(eventHistory).extracting("directionsQuestionnaireFiled").asList()
                .contains(expectedDirectionsQuestionnaireFiled.get(0));

            assertEmptyEvents(
                eventHistory,
                "defenceFiled",
                "consentExtensionFilingDefence",
                "defenceAndCounterClaim",
                "receiptOfAdmission",
                "replyToDefence"
            );
        }
    }

    @Nested
    class RespondentCounterClaimSpec {

        @Test
        void shouldPrepareExpectedEvents_when2v1ClaimWithRespondentCounterClaimToBoth() {
            CaseData caseData = CaseDataBuilder.builder()
                .setClaimTypeToSpecClaim()
                .atStateSpec2v1ClaimSubmitted()
                .atStateRespondent2v1CounterClaim()
                .build();
            if (caseData.getRespondent2OrgRegistered() != null
                && caseData.getRespondent2Represented() == null) {
                caseData = caseData.toBuilder()
                    .respondent2Represented(YES)
                    .build();
            }

            List<Event> expectedMiscellaneousEvents = List.of(
                Event.builder()
                    .eventSequence(1)
                    .eventCode("999")
                    .dateReceived(caseData.getIssueDate().atStartOfDay())
                    .eventDetailsText("Claim issued in CCD.")
                    .eventDetails(EventDetails.builder()
                                      .miscText("Claim issued in CCD.")
                                      .build())
                    .build(),
                Event.builder()
                    .eventSequence(2)
                    .eventCode("999")
                    .dateReceived(caseData.getRespondent1ResponseDate())
                    .eventDetailsText("RPA Reason: Defendant rejects and counter claims.")
                    .eventDetails(EventDetails.builder()
                                      .miscText("RPA Reason: Defendant rejects and counter claims.")
                                      .build())
                    .build()
            );

            var eventHistory = mapper.buildEvents(caseData, BEARER_TOKEN);

            assertThat(eventHistory).isNotNull();
            assertThat(eventHistory).extracting("miscellaneous").asList()
                .containsExactly(expectedMiscellaneousEvents.get(0), expectedMiscellaneousEvents.get(1));

            assertEmptyEvents(
                eventHistory,
                "defenceFiled",
                "receiptOfAdmission",
                "receiptOfPartAdmission",
                "replyToDefence",
                "consentExtensionFilingDefence",
                "directionsQuestionnaireFiled"
            );
        }
    }

    @Nested
    class RespondentPartAdmission {

        final String partyID = "002";
        DynamicList locationValues = DynamicList.fromList(List.of("Value 1"));
        DynamicList preferredCourt = DynamicList.builder()
            .listItems(locationValues.getListItems())
            .value(locationValues.getListItems().get(0))
            .build();

        @Test
        void shouldPrepareExpectedEvents_whenClaimWithRespondentPartAdmissionWithOptionalEvents() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateRespondentPartAdmissionAfterNotificationAcknowledgement()
                .respondent1ClaimResponseIntentionType(PART_DEFENCE)
                .respondent1DQ()
                .build();
            if (caseData.getRespondent2OrgRegistered() != null
                && caseData.getRespondent2Represented() == null) {
                caseData = caseData.toBuilder()
                    .respondent2Represented(YES)
                    .build();
            }
            Event expectedReceiptOfPartAdmission = Event.builder()
                .eventSequence(5)
                .eventCode("60")
                .dateReceived(caseData.getRespondent1ResponseDate())
                .litigiousPartyID("002")
                .build();
            List<Event> expectedMiscellaneousEvents = List.of(
                Event.builder()
                    .eventSequence(1)
                    .eventCode("999")
                    .dateReceived(caseData.getIssueDate().atStartOfDay())
                    .eventDetailsText("Claim issued in CCD.")
                    .eventDetails(EventDetails.builder()
                                      .miscText("Claim issued in CCD.")
                                      .build())
                    .build(),
                Event.builder()
                    .eventSequence(2)
                    .eventCode("999")
                    .dateReceived(caseData.getClaimNotificationDate())
                    .eventDetailsText("Claimant has notified defendant.")
                    .eventDetails(EventDetails.builder()
                                      .miscText("Claimant has notified defendant.")
                                      .build())
                    .build(),
                Event.builder()
                    .eventSequence(3)
                    .eventCode("999")
                    .dateReceived(caseData.getClaimDetailsNotificationDate())
                    .eventDetailsText("Claim details notified.")
                    .eventDetails(EventDetails.builder()
                                      .miscText("Claim details notified.")
                                      .build())
                    .build(),
                Event.builder()
                    .eventSequence(7)
                    .eventCode("999")
                    .dateReceived(caseData.getRespondent1ResponseDate())
                    .eventDetailsText("RPA Reason: Defendant partial admission.")
                    .eventDetails(EventDetails.builder()
                                      .miscText("RPA Reason: Defendant partial admission.")
                                      .build())
                    .build()
            );
            Event expectedAcknowledgementOfServiceReceived = Event.builder()
                .eventSequence(4)
                .eventCode("38")
                .dateReceived(caseData.getRespondent1AcknowledgeNotificationDate())
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

            List<Event> expectedDirectionsQuestionnaireFiled = List.of(
                Event.builder()
                    .eventSequence(6)
                    .eventCode("197")
                    .dateReceived(caseData.getRespondent1ResponseDate())
                    .litigiousPartyID("002")
                    .eventDetailsText(mapper.prepareFullDefenceEventText(
                        caseData.getRespondent1DQ(),
                        caseData,
                        true,
                        caseData.getRespondent1()
                    ))
                    .eventDetails(EventDetails.builder()
                                      .stayClaim(mapper.isStayClaim(caseData.getRespondent1DQ()))
                                      .preferredCourtCode(mapper.getPreferredCourtCode(caseData.getRespondent1DQ()))
                                      .preferredCourtName("")
                                      .build())
                    .build());

            var eventHistory = mapper.buildEvents(caseData, BEARER_TOKEN);

            assertThat(eventHistory).isNotNull();
            assertThat(eventHistory).extracting("receiptOfPartAdmission").asList()
                .containsExactly(expectedReceiptOfPartAdmission);
            assertThat(eventHistory).extracting("miscellaneous").asList()
                .containsExactly(expectedMiscellaneousEvents.get(0), expectedMiscellaneousEvents.get(1),
                                 expectedMiscellaneousEvents.get(2), expectedMiscellaneousEvents.get(3)
            );
            assertThat(eventHistory).extracting("acknowledgementOfServiceReceived").asList()
                .containsExactly(expectedAcknowledgementOfServiceReceived);
            assertThat(eventHistory).extracting("directionsQuestionnaireFiled").asList()
                .contains(expectedDirectionsQuestionnaireFiled.get(0));

            assertEmptyEvents(
                eventHistory,
                "defenceFiled",
                "consentExtensionFilingDefence",
                "defenceAndCounterClaim",
                "receiptOfAdmission",
                "replyToDefence"
            );
        }

        @Test
        void shouldPrepareExpectedEvents_whenClaimWithRespondentPartAdmissionWithoutOptionalEvents() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateRespondentPartAdmissionAfterNotificationAcknowledgement()
                .respondent1AcknowledgeNotificationDate(null)
                .respondent1ClaimResponseIntentionType(PART_DEFENCE)
                .respondent1DQ(
                    Respondent1DQ.builder()
                        .respondToCourtLocation(
                            RequestedCourt.builder()
                                .responseCourtLocations(preferredCourt)
                                .reasonForHearingAtSpecificCourt("Reason")
                                .build()
                        )
                        .build()
                )
                .build();
            Event expectedReceiptOfPartAdmission = Event.builder()
                .eventSequence(4)
                .eventCode("60")
                .dateReceived(caseData.getRespondent1ResponseDate())
                .litigiousPartyID("002")
                .build();
            List<Event> expectedMiscellaneousEvents = List.of(
                Event.builder()
                    .eventSequence(1)
                    .eventCode("999")
                    .dateReceived(caseData.getIssueDate().atStartOfDay())
                    .eventDetailsText("Claim issued in CCD.")
                    .eventDetails(EventDetails.builder()
                                      .miscText("Claim issued in CCD.")
                                      .build())
                    .build(),
                Event.builder()
                    .eventSequence(2)
                    .eventCode("999")
                    .dateReceived(caseData.getClaimNotificationDate())
                    .eventDetailsText("Claimant has notified defendant.")
                    .eventDetails(EventDetails.builder()
                                      .miscText("Claimant has notified defendant.")
                                      .build())
                    .build(),
                Event.builder()
                    .eventSequence(3)
                    .eventCode("999")
                    .dateReceived(caseData.getClaimDetailsNotificationDate())
                    .eventDetailsText("Claim details notified.")
                    .eventDetails(EventDetails.builder()
                                      .miscText("Claim details notified.")
                                      .build())
                    .build(),
                Event.builder()
                    .eventSequence(6)
                    .eventCode("999")
                    .dateReceived(caseData.getRespondent1ResponseDate())
                    .eventDetailsText("RPA Reason: Defendant partial admission.")
                    .eventDetails(EventDetails.builder()
                                      .miscText("RPA Reason: Defendant partial admission.")
                                      .build())
                    .build()
            );
            List<Event> expectedDirectionsQuestionnaireFiled =
                List.of(Event.builder()
                            .eventSequence(5)
                            .eventCode("197")
                            .dateReceived(caseData.getRespondent1ResponseDate())
                            .litigiousPartyID(partyID)
                            .eventDetailsText(mapper.prepareFullDefenceEventText(
                                caseData.getRespondent1DQ(),
                                caseData,
                                true,
                                caseData.getRespondent1()
                            ))
                            .eventDetails(EventDetails.builder()
                                              .stayClaim(mapper.isStayClaim(caseData.getRespondent1DQ()))
                                              .preferredCourtCode(mapper.getPreferredCourtCode(
                                                  caseData.getRespondent1DQ()))
                                              .preferredCourtName("")
                                              .build())
                            .build());

            var eventHistory = mapper.buildEvents(caseData, BEARER_TOKEN);

            assertThat(eventHistory).isNotNull();
            assertThat(eventHistory).extracting("receiptOfPartAdmission").asList()
                .containsExactly(expectedReceiptOfPartAdmission);
            assertThat(eventHistory).extracting("miscellaneous").asList()
                .containsExactly(expectedMiscellaneousEvents.get(0), expectedMiscellaneousEvents.get(1),
                                 expectedMiscellaneousEvents.get(2), expectedMiscellaneousEvents.get(3)
            );
            assertThat(eventHistory).extracting("directionsQuestionnaireFiled").asList()
                .contains(expectedDirectionsQuestionnaireFiled.get(0));

            assertEmptyEvents(
                eventHistory,
                "defenceFiled",
                "defenceAndCounterClaim",
                "receiptOfAdmission",
                "replyToDefence",
                "consentExtensionFilingDefence",
                "acknowledgementOfServiceReceived"
            );
        }

        @Test
        void shouldPrepareExpectedEvents_whenClaimWith1v2DiffSolicitorBothRespondentPartAdmissionNoOptionalEvents() {
            CaseData caseData = CaseDataBuilder.builder()
                .multiPartyClaimTwoDefendantSolicitors()
                .atStateBothRespondentsSameResponse(PART_ADMISSION)
                .respondent1DQ()
                .respondent2Responds1v2DiffSol(PART_ADMISSION)
                .respondent2DQ()
                .build();

            if (caseData.getRespondent2OrgRegistered() != null
                && caseData.getRespondent2Represented() == null) {
                caseData = caseData.toBuilder()
                    .respondent2Represented(YES)
                    .build();
            }
            List<Event> expectedReceiptOfPartAdmission = List.of(
                Event.builder()
                    .eventSequence(4)
                    .eventCode("60")
                    .dateReceived(caseData.getRespondent1ResponseDate())
                    .litigiousPartyID("002")
                    .build(),
                Event.builder()
                    .eventSequence(7)
                    .eventCode("60")
                    .dateReceived(caseData.getRespondent2ResponseDate())
                    .litigiousPartyID("003")
                    .build()
            );
            String respondent1MiscText =
                mapper.prepareRespondentResponseText(caseData, caseData.getRespondent1(), true);
            String respondent2MiscText =
                mapper.prepareRespondentResponseText(caseData, caseData.getRespondent2(), false);

            List<Event> expectedMiscellaneousEvents = List.of(
                Event.builder()
                    .eventSequence(1)
                    .eventCode("999")
                    .dateReceived(caseData.getIssueDate().atStartOfDay())
                    .eventDetailsText("Claim issued in CCD.")
                    .eventDetails(EventDetails.builder()
                                      .miscText("Claim issued in CCD.")
                                      .build())
                    .build(),
                Event.builder()
                    .eventSequence(2)
                    .eventCode("999")
                    .dateReceived(caseData.getClaimNotificationDate())
                    .eventDetailsText("Claimant has notified defendant.")
                    .eventDetails(EventDetails.builder()
                                      .miscText("Claimant has notified defendant.")
                                      .build())
                    .build(),
                Event.builder()
                    .eventSequence(3)
                    .eventCode("999")
                    .dateReceived(caseData.getClaimDetailsNotificationDate())
                    .eventDetailsText("Claim details notified.")
                    .eventDetails(EventDetails.builder()
                                      .miscText("Claim details notified.")
                                      .build())
                    .build(),
                Event.builder()
                    .eventSequence(6)
                    .eventCode("999")
                    .dateReceived(caseData.getRespondent1ResponseDate())
                    .eventDetailsText(respondent1MiscText)
                    .eventDetails(EventDetails.builder()
                                      .miscText(respondent1MiscText)
                                      .build())
                    .build(),
                Event.builder()
                    .eventSequence(9)
                    .eventCode("999")
                    .dateReceived(caseData.getRespondent2ResponseDate())
                    .eventDetailsText(respondent2MiscText)
                    .eventDetails(EventDetails.builder()
                                      .miscText(respondent2MiscText)
                                      .build())
                    .build()
            );

            List<Event> expectedDirectionsQuestionnaireFiled = List.of(
                Event.builder()
                    .eventSequence(5)
                    .eventCode("197")
                    .dateReceived(caseData.getRespondent1ResponseDate())
                    .litigiousPartyID("002")
                    .eventDetailsText(mapper.prepareFullDefenceEventText(
                        caseData.getRespondent1DQ(),
                        caseData,
                        true,
                        caseData.getRespondent1()
                    ))
                    .eventDetails(EventDetails.builder()
                                      .stayClaim(mapper.isStayClaim(caseData.getRespondent1DQ()))
                                      .preferredCourtCode(mapper.getPreferredCourtCode(caseData.getRespondent1DQ()))
                                      .preferredCourtName("")
                                      .build())
                    .build(),
                Event.builder()
                    .eventSequence(8)
                    .eventCode("197")
                    .dateReceived(caseData.getRespondent2ResponseDate())
                    .litigiousPartyID("003")
                    .eventDetailsText(mapper.prepareFullDefenceEventText(
                        caseData.getRespondent2DQ(),
                        caseData,
                        false,
                        caseData.getRespondent2()
                    ))
                    .eventDetails(EventDetails.builder()
                                      .stayClaim(mapper.isStayClaim(caseData.getRespondent2DQ()))
                                      .preferredCourtCode(mapper.getPreferredCourtCode(caseData.getRespondent2DQ()))
                                      .preferredCourtName("")
                                      .build())
                    .build()
            );

            var eventHistory = mapper.buildEvents(caseData, BEARER_TOKEN);

            assertThat(eventHistory).isNotNull();
            assertThat(eventHistory).extracting("receiptOfPartAdmission").asList()
                .containsExactly(expectedReceiptOfPartAdmission.get(0), expectedReceiptOfPartAdmission.get(1));
            assertThat(eventHistory).extracting("miscellaneous").asList()
                .containsExactly(expectedMiscellaneousEvents.get(0),
                                 expectedMiscellaneousEvents.get(1), expectedMiscellaneousEvents.get(2),
                                 expectedMiscellaneousEvents.get(3), expectedMiscellaneousEvents.get(4)
            );
            assertThat(eventHistory).extracting("directionsQuestionnaireFiled").asList()
                .containsExactly(expectedDirectionsQuestionnaireFiled.get(0), expectedDirectionsQuestionnaireFiled.get(1)
            );

            assertEmptyEvents(
                eventHistory,
                "defenceFiled",
                "defenceAndCounterClaim",
                "receiptOfAdmission",
                "replyToDefence",
                "consentExtensionFilingDefence",
                "acknowledgementOfServiceReceived"
            );
        }

        @Test
        void shouldPrepareExpectedEvents_whenClaimWith1v2SameSolicitorBothRespondentPartAdmissionNoOptionalEvents() {
            CaseData caseData = CaseDataBuilder.builder()
                .multiPartyClaimOneDefendantSolicitor()
                .atStateBothRespondentsSameResponse1v2SameSolicitor(PART_ADMISSION)
                .respondentResponseIsSame(YES)
                .build();
            if (caseData.getRespondent2OrgRegistered() != null
                && caseData.getRespondent2Represented() == null) {
                caseData = caseData.toBuilder()
                    .respondent2Represented(YES)
                    .respondent1DQ(
                        Respondent1DQ.builder()
                            .respondToCourtLocation(
                                RequestedCourt.builder()
                                    .responseCourtLocations(preferredCourt)
                                    .reasonForHearingAtSpecificCourt("Reason")
                                    .build()
                            )
                            .build()
                    )
                    .build();
            }
            List<Event> expectedReceiptOfPartAdmission = List.of(
                Event.builder()
                    .eventSequence(4)
                    .eventCode("60")
                    .dateReceived(caseData.getRespondent1ResponseDate())
                    .litigiousPartyID("002")
                    .build(),
                Event.builder()
                    .eventSequence(5)
                    .eventCode("60")
                    .dateReceived(caseData.getRespondent2ResponseDate())
                    .litigiousPartyID("003")
                    .build()
            );
            String respondent1MiscText =
                mapper.prepareRespondentResponseText(caseData, caseData.getRespondent1(), true);
            String respondent2MiscText =
                mapper.prepareRespondentResponseText(caseData, caseData.getRespondent2(), false);

            List<Event> expectedMiscellaneousEvents = List.of(
                Event.builder()
                    .eventSequence(1)
                    .eventCode("999")
                    .dateReceived(caseData.getIssueDate().atStartOfDay())
                    .eventDetailsText("Claim issued in CCD.")
                    .eventDetails(EventDetails.builder()
                                      .miscText("Claim issued in CCD.")
                                      .build())
                    .build(),
                Event.builder()
                    .eventSequence(2)
                    .eventCode("999")
                    .dateReceived(caseData.getClaimNotificationDate())
                    .eventDetailsText("Claimant has notified defendant.")
                    .eventDetails(EventDetails.builder()
                                      .miscText("Claimant has notified defendant.")
                                      .build())
                    .build(),
                Event.builder()
                    .eventSequence(3)
                    .eventCode("999")
                    .dateReceived(caseData.getClaimDetailsNotificationDate())
                    .eventDetailsText("Claim details notified.")
                    .eventDetails(EventDetails.builder()
                                      .miscText("Claim details notified.")
                                      .build())
                    .build(),
                Event.builder()
                    .eventSequence(8)
                    .eventCode("999")
                    .dateReceived(caseData.getRespondent1ResponseDate())
                    .eventDetailsText(respondent1MiscText)
                    .eventDetails(EventDetails.builder()
                                      .miscText(respondent1MiscText)
                                      .build())
                    .build(),

                Event.builder()
                    .eventSequence(9)
                    .eventCode("999")
                    .dateReceived(caseData.getRespondent2ResponseDate())
                    .eventDetailsText(respondent2MiscText)
                    .eventDetails(EventDetails.builder()
                                      .miscText(respondent2MiscText)
                                      .build())
                    .build()
            );

            List<Event> expectedDirectionsQuestionnaireFiled = List.of(
                Event.builder()
                            .eventSequence(6)
                            .eventCode("197")
                            .dateReceived(caseData.getRespondent1ResponseDate())
                            .litigiousPartyID(partyID)
                            .eventDetailsText(mapper.prepareFullDefenceEventText(
                                caseData.getRespondent1DQ(),
                                caseData,
                                true,
                                caseData.getRespondent1()
                            ))
                            .eventDetails(EventDetails.builder()
                                              .stayClaim(mapper.isStayClaim(caseData.getRespondent1DQ()))
                                              .preferredCourtCode(mapper.getPreferredCourtCode(
                                                  caseData.getRespondent1DQ()))
                                              .preferredCourtName("")
                                              .build())
                            .build());

            var eventHistory = mapper.buildEvents(caseData, BEARER_TOKEN);

            assertThat(eventHistory).isNotNull();
            assertThat(eventHistory).extracting("receiptOfPartAdmission").asList()
                .containsExactly(expectedReceiptOfPartAdmission.get(0), expectedReceiptOfPartAdmission.get(1));
            assertThat(eventHistory).extracting("miscellaneous").asList()
                .containsExactly(expectedMiscellaneousEvents.get(0),
                                 expectedMiscellaneousEvents.get(1), expectedMiscellaneousEvents.get(2),
                                 expectedMiscellaneousEvents.get(3), expectedMiscellaneousEvents.get(4));
            assertThat(eventHistory).extracting("directionsQuestionnaireFiled").asList()
                .contains(expectedDirectionsQuestionnaireFiled.get(0));

            assertEmptyEvents(
                eventHistory,
                "defenceFiled",
                "defenceAndCounterClaim",
                "receiptOfAdmission",
                "replyToDefence",
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
                .atStateRespondentCounterClaim()
                .respondent1ClaimResponseIntentionType(CONTEST_JURISDICTION)
                .build();
            if (caseData.getRespondent2OrgRegistered() != null
                && caseData.getRespondent2Represented() == null) {
                caseData = caseData.toBuilder()
                    .respondent2Represented(YES)
                    .build();
            }
            List<Event> expectedMiscellaneousEvents = List.of(
                Event.builder()
                    .eventSequence(1)
                    .eventCode("999")
                    .dateReceived(caseData.getIssueDate().atStartOfDay())
                    .eventDetailsText("Claim issued in CCD.")
                    .eventDetails(EventDetails.builder()
                                      .miscText("Claim issued in CCD.")
                                      .build())
                    .build(),
                Event.builder()
                    .eventSequence(2)
                    .eventCode("999")
                    .dateReceived(caseData.getClaimNotificationDate())
                    .eventDetailsText("Claimant has notified defendant.")
                    .eventDetails(EventDetails.builder()
                                      .miscText("Claimant has notified defendant.")
                                      .build())
                    .build(),
                Event.builder()
                    .eventSequence(3)
                    .eventCode("999")
                    .dateReceived(caseData.getClaimDetailsNotificationDate())
                    .eventDetailsText("Claim details notified.")
                    .eventDetails(EventDetails.builder()
                                      .miscText("Claim details notified.")
                                      .build())
                    .build(),
                Event.builder()
                    .eventSequence(5)
                    .eventCode("999")
                    .dateReceived(caseData.getRespondent1ResponseDate())
                    .eventDetailsText("RPA Reason: Defendant rejects and counter claims.")
                    .eventDetails(EventDetails.builder()
                                      .miscText("RPA Reason: Defendant rejects and counter claims.")
                                      .build())
                    .build()
            );
            Event expectedAcknowledgementOfServiceReceived = Event.builder()
                .eventSequence(4)
                .eventCode("38")
                .dateReceived(caseData.getRespondent1AcknowledgeNotificationDate())
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

            var eventHistory = mapper.buildEvents(caseData, BEARER_TOKEN);

            assertThat(eventHistory).isNotNull();
            assertThat(eventHistory).extracting("miscellaneous").asList()
                .containsExactly(expectedMiscellaneousEvents.get(0), expectedMiscellaneousEvents.get(1),
                                 expectedMiscellaneousEvents.get(2), expectedMiscellaneousEvents.get(3)
            );
            assertThat(eventHistory).extracting("acknowledgementOfServiceReceived").asList()
                .containsExactly(expectedAcknowledgementOfServiceReceived);

            assertEmptyEvents(
                eventHistory,
                "defenceFiled",
                "receiptOfAdmission",
                "receiptOfPartAdmission",
                "replyToDefence",
                "consentExtensionFilingDefence"
            );
        }

        @Test
        void shouldPrepareExpectedEvents_whenClaimWithRespondentCounterClaimWithoutOptionalEvents() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateRespondentCounterClaim()
                .respondent1ClaimResponseIntentionType(CONTEST_JURISDICTION)
                .respondent1AcknowledgeNotificationDate(null)
                .build();
            if (caseData.getRespondent2OrgRegistered() != null
                && caseData.getRespondent2Represented() == null) {
                caseData = caseData.toBuilder()
                    .respondent2Represented(YES)
                    .build();
            }
            List<Event> expectedMiscellaneousEvents = List.of(
                Event.builder()
                    .eventSequence(1)
                    .eventCode("999")
                    .dateReceived(caseData.getIssueDate().atStartOfDay())
                    .eventDetailsText("Claim issued in CCD.")
                    .eventDetails(EventDetails.builder()
                                      .miscText("Claim issued in CCD.")
                                      .build())
                    .build(),
                Event.builder()
                    .eventSequence(2)
                    .eventCode("999")
                    .dateReceived(caseData.getClaimNotificationDate())
                    .eventDetailsText("Claimant has notified defendant.")
                    .eventDetails(EventDetails.builder()
                                      .miscText("Claimant has notified defendant.")
                                      .build())
                    .build(),
                Event.builder()
                    .eventSequence(3)
                    .eventCode("999")
                    .dateReceived(caseData.getClaimDetailsNotificationDate())
                    .eventDetailsText("Claim details notified.")
                    .eventDetails(EventDetails.builder()
                                      .miscText("Claim details notified.")
                                      .build())
                    .build(),
                Event.builder()
                    .eventSequence(4)
                    .eventCode("999")
                    .dateReceived(caseData.getRespondent1ResponseDate())
                    .eventDetailsText("RPA Reason: Defendant rejects and counter claims.")
                    .eventDetails(EventDetails.builder()
                                      .miscText("RPA Reason: Defendant rejects and counter claims.")
                                      .build())
                    .build()
            );

            var eventHistory = mapper.buildEvents(caseData, BEARER_TOKEN);

            assertThat(eventHistory).isNotNull();
            assertThat(eventHistory).extracting("miscellaneous").asList()
                .containsExactly(expectedMiscellaneousEvents.get(0), expectedMiscellaneousEvents.get(1),
                                 expectedMiscellaneousEvents.get(2), expectedMiscellaneousEvents.get(3)
            );

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

        @Test
        void shouldPrepareExpectedEvents_whenClaimWith1v2DiffSolicitorBothRespondentCounterClaimNoOptionalEvents() {
            CaseData caseData = CaseDataBuilder.builder()
                .multiPartyClaimTwoDefendantSolicitors()
                .atStateBothRespondentsSameResponse(COUNTER_CLAIM)
                .build();
            if (caseData.getRespondent2OrgRegistered() != null
                && caseData.getRespondent2Represented() == null) {
                caseData = caseData.toBuilder()
                    .respondent2Represented(YES)
                    .build();
            }
            List<Event> expectedDefenceAndCounterClaim = List.of(
                Event.builder()
                    .eventSequence(4)
                    .eventCode("52")
                    .dateReceived(caseData.getRespondent1ResponseDate())
                    .litigiousPartyID("002")
                    .build(),
                Event.builder()
                    .eventSequence(6)
                    .eventCode("52")
                    .dateReceived(caseData.getRespondent2ResponseDate())
                    .litigiousPartyID("003")
                    .build()
            );
            String respondent1MiscText =
                mapper.prepareRespondentResponseText(caseData, caseData.getRespondent1(), true);
            String respondent2MiscText =
                mapper.prepareRespondentResponseText(caseData, caseData.getRespondent2(), false);

            List<Event> expectedMiscellaneousEvents = List.of(
                Event.builder()
                    .eventSequence(1)
                    .eventCode("999")
                    .dateReceived(caseData.getIssueDate().atStartOfDay())
                    .eventDetailsText("Claim issued in CCD.")
                    .eventDetails(EventDetails.builder()
                                      .miscText("Claim issued in CCD.")
                                      .build())
                    .build(),
                Event.builder()
                    .eventSequence(2)
                    .eventCode("999")
                    .dateReceived(caseData.getClaimNotificationDate())
                    .eventDetailsText("Claimant has notified defendant.")
                    .eventDetails(EventDetails.builder()
                                      .miscText("Claimant has notified defendant.")
                                      .build())
                    .build(),
                Event.builder()
                    .eventSequence(3)
                    .eventCode("999")
                    .dateReceived(caseData.getClaimDetailsNotificationDate())
                    .eventDetailsText("Claim details notified.")
                    .eventDetails(EventDetails.builder()
                                      .miscText("Claim details notified.")
                                      .build())
                    .build(),
                Event.builder()
                    .eventSequence(4)
                    .eventCode("999")
                    .dateReceived(caseData.getRespondent1ResponseDate())
                    .eventDetailsText(respondent1MiscText)
                    .eventDetails(EventDetails.builder()
                                      .miscText(respondent1MiscText)
                                      .build())
                    .build(),
                Event.builder()
                    .eventSequence(5)
                    .eventCode("999")
                    .dateReceived(caseData.getRespondent2ResponseDate())
                    .eventDetailsText(respondent2MiscText)
                    .eventDetails(EventDetails.builder()
                                      .miscText(respondent2MiscText)
                                      .build())
                    .build()
            );

            var eventHistory = mapper.buildEvents(caseData, BEARER_TOKEN);

            assertThat(eventHistory).isNotNull();
            assertThat(eventHistory).extracting("miscellaneous").asList()
                .containsExactly(expectedMiscellaneousEvents.get(0),
                                 expectedMiscellaneousEvents.get(1), expectedMiscellaneousEvents.get(2),
                                 expectedMiscellaneousEvents.get(3), expectedMiscellaneousEvents.get(4)
            );

            assertEmptyEvents(
                eventHistory,
                "defenceFiled",
                "receiptOfPartAdmission",
                "receiptOfAdmission",
                "directionsQuestionnaireFiled",
                "consentExtensionFilingDefence",
                "acknowledgementOfServiceReceived"
            );
        }

        @Test
        void shouldPrepareExpectedEvents_whenClaimWith1v2SameSolicitorBothRespondentCounterClaimNoOptionalEvents() {
            CaseData caseData = CaseDataBuilder.builder()
                .multiPartyClaimOneDefendantSolicitor()
                .atStateBothRespondentsSameResponse1v2SameSolicitor(COUNTER_CLAIM)
                .respondentResponseIsSame(YES)
                .build();
            if (caseData.getRespondent2OrgRegistered() != null
                && caseData.getRespondent2Represented() == null) {
                caseData = caseData.toBuilder()
                    .respondent2Represented(YES)
                    .build();
            }

            List<Event> expectedDefenceAndCounterClaim = List.of(
                Event.builder()
                    .eventSequence(4)
                    .eventCode("52")
                    .dateReceived(caseData.getRespondent1ResponseDate())
                    .litigiousPartyID("002")
                    .build(),
                Event.builder()
                    .eventSequence(5)
                    .eventCode("52")
                    .dateReceived(caseData.getRespondent2ResponseDate())
                    .litigiousPartyID("003")
                    .build()
            );
            String respondent1MiscText =
                mapper.prepareRespondentResponseText(caseData, caseData.getRespondent1(), true);
            String respondent2MiscText =
                mapper.prepareRespondentResponseText(caseData, caseData.getRespondent2(), false);

            List<Event> expectedMiscellaneousEvents = List.of(
                Event.builder()
                    .eventSequence(1)
                    .eventCode("999")
                    .dateReceived(caseData.getIssueDate().atStartOfDay())
                    .eventDetailsText("Claim issued in CCD.")
                    .eventDetails(EventDetails.builder()
                                      .miscText("Claim issued in CCD.")
                                      .build())
                    .build(),
                Event.builder()
                    .eventSequence(2)
                    .eventCode("999")
                    .dateReceived(caseData.getClaimNotificationDate())
                    .eventDetailsText("Claimant has notified defendant.")
                    .eventDetails(EventDetails.builder()
                                      .miscText("Claimant has notified defendant.")
                                      .build())
                    .build(),
                Event.builder()
                    .eventSequence(3)
                    .eventCode("999")
                    .dateReceived(caseData.getClaimDetailsNotificationDate())
                    .eventDetailsText("Claim details notified.")
                    .eventDetails(EventDetails.builder()
                                      .miscText("Claim details notified.")
                                      .build())
                    .build(),
                Event.builder()
                    .eventSequence(4)
                    .eventCode("999")
                    .dateReceived(caseData.getRespondent1ResponseDate())
                    .eventDetailsText(respondent1MiscText)
                    .eventDetails(EventDetails.builder()
                                      .miscText(respondent1MiscText)
                                      .build())
                    .build(),
                Event.builder()
                    .eventSequence(5)
                    .eventCode("999")
                    .dateReceived(caseData.getRespondent2ResponseDate())
                    .eventDetailsText(respondent2MiscText)
                    .eventDetails(EventDetails.builder()
                                      .miscText(respondent2MiscText)
                                      .build())
                    .build()
            );

            var eventHistory = mapper.buildEvents(caseData, BEARER_TOKEN);

            assertThat(eventHistory).isNotNull();
            assertThat(eventHistory).extracting("miscellaneous").asList()
                .containsExactly(expectedMiscellaneousEvents.get(0),
                                 expectedMiscellaneousEvents.get(1), expectedMiscellaneousEvents.get(2),
                                 expectedMiscellaneousEvents.get(3), expectedMiscellaneousEvents.get(4)
            );

            assertEmptyEvents(
                eventHistory,
                "defenceFiled",
                "receiptOfPartAdmission",
                "receiptOfAdmission",
                "directionsQuestionnaireFiled",
                "consentExtensionFilingDefence",
                "acknowledgementOfServiceReceived"
            );
        }
    }

    @Nested
    class RespondentFullDefence {

        @Test
        void shouldPrepareExpectedEvents_whenClaimWithFullDefence1v1WithoutOptionalEvents() {
            CaseData caseData = CaseDataBuilder.builder()
                .atState(FlowState.Main.FULL_DEFENCE)
                .respondent1AcknowledgeNotificationDate(null)
                .build();
            if (caseData.getRespondent2OrgRegistered() != null
                && caseData.getRespondent2Represented() == null) {
                caseData = caseData.toBuilder()
                    .respondent2Represented(YES)
                    .build();
            }
            Event expectedDefenceFiled = Event.builder()
                .eventSequence(4)
                .eventCode("50")
                .dateReceived(caseData.getRespondent1ResponseDate())
                .litigiousPartyID("002")
                .build();
            Event expectedDirectionsQuestionnaireFiled = Event.builder()
                .eventSequence(5)
                .eventCode("197")
                .dateReceived(caseData.getRespondent1ResponseDate())
                .litigiousPartyID("002")
                .eventDetailsText(mapper.prepareFullDefenceEventText(
                    caseData.getRespondent1DQ(),
                    caseData,
                    true,
                    caseData.getRespondent1()
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
                    .dateReceived(caseData.getIssueDate().atStartOfDay())
                    .eventDetailsText("Claim issued in CCD.")
                    .eventDetails(EventDetails.builder()
                                      .miscText("Claim issued in CCD.")
                                      .build())
                    .build(),
                Event.builder()
                    .eventSequence(2)
                    .eventCode("999")
                    .dateReceived(caseData.getClaimNotificationDate())
                    .eventDetailsText("Claimant has notified defendant.")
                    .eventDetails(EventDetails.builder()
                                      .miscText("Claimant has notified defendant.")
                                      .build())
                    .build(),
                Event.builder()
                    .eventSequence(3)
                    .eventCode("999")
                    .dateReceived(caseData.getClaimDetailsNotificationDate())
                    .eventDetailsText("Claim details notified.")
                    .eventDetails(EventDetails.builder()
                                      .miscText("Claim details notified.")
                                      .build())
                    .build()
            );

            var eventHistory = mapper.buildEvents(caseData, BEARER_TOKEN);

            assertThat(eventHistory).isNotNull();
            assertThat(eventHistory).extracting("defenceFiled").asList()
                .containsExactly(expectedDefenceFiled);
            assertThat(eventHistory).extracting("directionsQuestionnaireFiled").asList()
                .containsExactly(expectedDirectionsQuestionnaireFiled);
            assertThat(eventHistory).extracting("miscellaneous").asList()
                .containsExactly(expectedMiscellaneousEvents.get(0),
                                 expectedMiscellaneousEvents.get(1), expectedMiscellaneousEvents.get(2)
            );

            assertEmptyEvents(
                eventHistory,
                "receiptOfAdmission",
                "receiptOfPartAdmission",
                "replyToDefence",
                "acknowledgementOfServiceReceived",
                "consentExtensionFilingDefence"
            );
        }

        @Test
        void shouldPrepareExpectedEvents_whenClaimWithFullDefence1v1WithoutOptionalEventsFullyPaid() {
            CaseData caseData = CaseDataBuilder.builder()
                .atState(FlowState.Main.FULL_DEFENCE)
                .respondent1AcknowledgeNotificationDate(null)
                .totalClaimAmount(BigDecimal.valueOf(1200))
                .build().toBuilder()
                .respondToClaim(RespondToClaim.builder()
                                    .howMuchWasPaid(BigDecimal.valueOf(120000))
                                    .build())
                .build();
            if (caseData.getRespondent2OrgRegistered() != null
                && caseData.getRespondent2Represented() == null) {
                caseData = caseData.toBuilder()
                    .respondent2Represented(YES)
                    .build();
            }
            Event expectedDefenceFiled = Event.builder()
                .eventSequence(4)
                .eventCode("49")
                .dateReceived(caseData.getRespondent1ResponseDate())
                .litigiousPartyID("002")
                .build();
            Event expectedDirectionsQuestionnaireFiled = Event.builder()
                .eventSequence(5)
                .eventCode("197")
                .dateReceived(caseData.getRespondent1ResponseDate())
                .litigiousPartyID("002")
                .eventDetailsText(mapper.prepareFullDefenceEventText(
                    caseData.getRespondent1DQ(),
                    caseData,
                    true,
                    caseData.getRespondent1()
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
                    .dateReceived(caseData.getIssueDate().atStartOfDay())
                    .eventDetailsText("Claim issued in CCD.")
                    .eventDetails(EventDetails.builder()
                                      .miscText("Claim issued in CCD.")
                                      .build())
                    .build(),
                Event.builder()
                    .eventSequence(2)
                    .eventCode("999")
                    .dateReceived(caseData.getClaimNotificationDate())
                    .eventDetailsText("Claimant has notified defendant.")
                    .eventDetails(EventDetails.builder()
                                      .miscText("Claimant has notified defendant.")
                                      .build())
                    .build(),
                Event.builder()
                    .eventSequence(3)
                    .eventCode("999")
                    .dateReceived(caseData.getClaimDetailsNotificationDate())
                    .eventDetailsText("Claim details notified.")
                    .eventDetails(EventDetails.builder()
                                      .miscText("Claim details notified.")
                                      .build())
                    .build()
            );

            var eventHistory = mapper.buildEvents(caseData, BEARER_TOKEN);

            assertThat(eventHistory).isNotNull();
            assertThat(eventHistory).extracting("statesPaid").asList()
                .containsExactly(expectedDefenceFiled);
            assertThat(eventHistory).extracting("directionsQuestionnaireFiled").asList()
                .containsExactly(expectedDirectionsQuestionnaireFiled);
            assertThat(eventHistory).extracting("miscellaneous").asList()
                .containsExactly(expectedMiscellaneousEvents.get(0),
                                 expectedMiscellaneousEvents.get(1), expectedMiscellaneousEvents.get(2)
            );

            assertEmptyEvents(
                eventHistory,
                "receiptOfAdmission",
                "receiptOfPartAdmission",
                "replyToDefence",
                "acknowledgementOfServiceReceived",
                "consentExtensionFilingDefence"
            );
        }

        @Test
        void shouldPrepareExpectedEvents_whenClaimWithFullDefence1v2SameSolicitorWithoutOptionalEvents() {
            CaseData caseData = CaseDataBuilder.builder()
                .multiPartyClaimOneDefendantSolicitor()
                .atState(FlowState.Main.FULL_DEFENCE)
                .respondent2Responds1v2SameSol(FULL_DEFENCE)
                .respondentResponseIsSame(YES)
                .respondent2DQ(Respondent2DQ.builder().build())
                .respondent2ClaimResponseIntentionType(ResponseIntention.FULL_DEFENCE)
                .build();
            if (caseData.getRespondent2OrgRegistered() != null
                && caseData.getRespondent2Represented() == null) {
                caseData = caseData.toBuilder()
                    .respondent2Represented(YES)
                    .build();
            }
            List<Event> expectedDefenceFiled = List.of(
                Event.builder()
                    .eventSequence(5)
                    .eventCode("50")
                    .dateReceived(caseData.getRespondent1ResponseDate())
                    .litigiousPartyID("002")
                    .build(),
                Event.builder()
                    .eventSequence(6)
                    .eventCode("50")
                    .dateReceived(caseData.getRespondent2ResponseDate())
                    .litigiousPartyID("003")
                    .build()
            );
            List<Event> expectedDirectionsQuestionnaireFiled = List.of(
                Event.builder()
                    .eventSequence(7)
                    .eventCode("197")
                    .dateReceived(caseData.getRespondent1ResponseDate())
                    .litigiousPartyID("002")
                    .eventDetailsText(mapper.prepareFullDefenceEventText(
                        caseData.getRespondent1DQ(),
                        caseData,
                        true,
                        caseData.getRespondent1()
                    ))
                    .eventDetails(EventDetails.builder()
                                      .stayClaim(mapper.isStayClaim(caseData.getRespondent1DQ()))
                                      .preferredCourtCode(mapper.getPreferredCourtCode(caseData.getRespondent1DQ()))
                                      .preferredCourtName("")
                                      .build())
                    .build(),
                Event.builder()
                    .eventSequence(8)
                    .eventCode("197")
                    .dateReceived(caseData.getRespondent2ResponseDate())
                    .litigiousPartyID("003")
                    .eventDetailsText(mapper.prepareFullDefenceEventText(
                        caseData.getRespondent1DQ(),
                        caseData,
                        true,
                        caseData.getRespondent2()
                    ))
                    .eventDetails(EventDetails.builder()
                                      .stayClaim(mapper.isStayClaim(caseData.getRespondent1DQ()))
                                      .preferredCourtCode(mapper.getPreferredCourtCode(caseData.getRespondent1DQ()))
                                      .preferredCourtName("")
                                      .build())
                    .build()
            );
            List<Event> expectedMiscellaneousEvents = List.of(
                Event.builder()
                    .eventSequence(1)
                    .eventCode("999")
                    .dateReceived(caseData.getIssueDate().atStartOfDay())
                    .eventDetailsText("Claim issued in CCD.")
                    .eventDetails(EventDetails.builder()
                                      .miscText("Claim issued in CCD.")
                                      .build())
                    .build(),
                Event.builder()
                    .eventSequence(2)
                    .eventCode("999")
                    .dateReceived(caseData.getClaimNotificationDate())
                    .eventDetailsText("Claimant has notified defendant.")
                    .eventDetails(EventDetails.builder()
                                      .miscText("Claimant has notified defendant.")
                                      .build())
                    .build(),
                Event.builder()
                    .eventSequence(3)
                    .eventCode("999")
                    .dateReceived(caseData.getClaimDetailsNotificationDate())
                    .eventDetailsText("Claim details notified.")
                    .eventDetails(EventDetails.builder()
                                      .miscText("Claim details notified.")
                                      .build())
                    .build()
            );

            var eventHistory = mapper.buildEvents(caseData, BEARER_TOKEN);

            assertThat(eventHistory).isNotNull();
            assertThat(eventHistory).extracting("defenceFiled").asList()
                .containsExactly(expectedDefenceFiled.get(0), expectedDefenceFiled.get(1));
            assertThat(eventHistory).extracting("directionsQuestionnaireFiled").asList()
                .containsExactly(
                    expectedDirectionsQuestionnaireFiled.get(0),
                    expectedDirectionsQuestionnaireFiled.get(1)
            );
            assertThat(eventHistory).extracting("miscellaneous").asList()
                .containsExactly(expectedMiscellaneousEvents.get(0), expectedMiscellaneousEvents.get(1),
                                 expectedMiscellaneousEvents.get(2)
            );

            assertEmptyEvents(
                eventHistory,
                "receiptOfAdmission",
                "receiptOfPartAdmission",
                "replyToDefence",
                "consentExtensionFilingDefence"
            );
        }

        @Test
        void shouldPrepareExpectedEvents_whenClaimWithFullDefence1v2DifferentSolicitorWithoutOptionalEvents() {
            CaseData caseData = CaseDataBuilder.builder()
                .multiPartyClaimTwoDefendantSolicitors()
                .atState(FlowState.Main.FULL_DEFENCE)
                .respondent1DQ()
                .respondent2Responds1v2DiffSol(FULL_DEFENCE)
                .respondent2DQ()
                .build();
            if (caseData.getRespondent2OrgRegistered() != null
                && caseData.getRespondent2Represented() == null) {
                caseData = caseData.toBuilder()
                    .respondent2Represented(YES)
                    .build();
            }
            List<Event> expectedDefenceFiled = List.of(
                Event.builder()
                    .eventSequence(5)
                    .eventCode("50")
                    .dateReceived(caseData.getRespondent1ResponseDate())
                    .litigiousPartyID("002")
                    .build(),
                Event.builder()
                    .eventSequence(7)
                    .eventCode("50")
                    .dateReceived(caseData.getRespondent2ResponseDate())
                    .litigiousPartyID("003")
                    .build()
            );
            List<Event> expectedDirectionsQuestionnaireFiled = List.of(
                Event.builder()
                    .eventSequence(6)
                    .eventCode("197")
                    .dateReceived(caseData.getRespondent1ResponseDate())
                    .litigiousPartyID("002")
                    .eventDetailsText(mapper.prepareFullDefenceEventText(
                        caseData.getRespondent1DQ(),
                        caseData,
                        true,
                        caseData.getRespondent1()
                    ))
                    .eventDetails(EventDetails.builder()
                                      .stayClaim(mapper.isStayClaim(caseData.getRespondent1DQ()))
                                      .preferredCourtCode(mapper.getPreferredCourtCode(caseData.getRespondent1DQ()))
                                      .preferredCourtName("")
                                      .build())
                    .build(),
                Event.builder()
                    .eventSequence(8)
                    .eventCode("197")
                    .dateReceived(caseData.getRespondent2ResponseDate())
                    .litigiousPartyID("003")
                    .eventDetailsText(mapper.prepareFullDefenceEventText(
                        caseData.getRespondent2DQ(),
                        caseData,
                        false,
                        caseData.getRespondent2()
                    ))
                    .eventDetails(EventDetails.builder()
                                      .stayClaim(mapper.isStayClaim(caseData.getRespondent2DQ()))
                                      .preferredCourtCode(mapper.getPreferredCourtCode(caseData.getRespondent2DQ()))
                                      .preferredCourtName("")
                                      .build())
                    .build()
            );
            List<Event> expectedMiscellaneousEvents = List.of(
                Event.builder()
                    .eventSequence(1)
                    .eventCode("999")
                    .dateReceived(caseData.getIssueDate().atStartOfDay())
                    .eventDetailsText("Claim issued in CCD.")
                    .eventDetails(EventDetails.builder()
                                      .miscText("Claim issued in CCD.")
                                      .build())
                    .build(),
                Event.builder()
                    .eventSequence(2)
                    .eventCode("999")
                    .dateReceived(caseData.getClaimNotificationDate())
                    .eventDetailsText("Claimant has notified defendant.")
                    .eventDetails(EventDetails.builder()
                                      .miscText("Claimant has notified defendant.")
                                      .build())
                    .build(),
                Event.builder()
                    .eventSequence(3)
                    .eventCode("999")
                    .dateReceived(caseData.getClaimDetailsNotificationDate())
                    .eventDetailsText("Claim details notified.")
                    .eventDetails(EventDetails.builder()
                                      .miscText("Claim details notified.")
                                      .build())
                    .build()
            );

            var eventHistory = mapper.buildEvents(caseData, BEARER_TOKEN);

            assertThat(eventHistory).extracting("miscellaneous").asList()
                .containsExactly(expectedMiscellaneousEvents.get(0),
                                 expectedMiscellaneousEvents.get(1), expectedMiscellaneousEvents.get(2)
            );
            assertThat(eventHistory).isNotNull();
            assertThat(eventHistory).extracting("defenceFiled").asList()
                .containsExactly(expectedDefenceFiled.get(0), expectedDefenceFiled.get(1));
            assertThat(eventHistory).extracting("directionsQuestionnaireFiled").asList()
                .containsExactly(
                    expectedDirectionsQuestionnaireFiled.get(0),
                    expectedDirectionsQuestionnaireFiled.get(1)
            );

            assertEmptyEvents(
                eventHistory,
                "receiptOfAdmission",
                "receiptOfPartAdmission",
                "replyToDefence",
                "consentExtensionFilingDefence"
            );
        }
    }

    @Nested
    class RespondentDivergentResponseSpec {

        @Test
        void shouldPrepareExpectedEvents_whenClaimWith1v2DiffSolicitorResp1PartAdmitsResp2FullDef() {
            CaseData caseData = CaseDataBuilder.builder()
                .setClaimTypeToSpecClaim()
                .atState(FlowState.Main.AWAITING_RESPONSES_FULL_DEFENCE_RECEIVED)
                .multiPartyClaimTwoDefendantSolicitors()
                .atStateRespondent1v2FullDefence_AdmitPart()
                .atState1v2DivergentResponseSpec(
                    RespondentResponseTypeSpec.FULL_DEFENCE,
                    RespondentResponseTypeSpec.PART_ADMISSION
                )
                .respondent2DQ()
                .respondent1DQ()
                .respondent1(PartyBuilder.builder().individual()
                                 .individualDateOfBirth(LocalDate.now().plusDays(1))
                                 .build())
                .build();
            if (caseData.getRespondent2Represented() == null && caseData.getRespondent2OrgRegistered() != null) {
                caseData = caseData.toBuilder()
                    .respondent2Represented(YesOrNo.YES)
                    .build();
            }
            if (caseData.getRespondent2OrgRegistered() != null
                && caseData.getRespondent2Represented() == null) {
                caseData = caseData.toBuilder()
                    .respondent2Represented(YES)
                    .build();
            }
            String respondent1MiscText =
                mapper.prepareRespondentResponseText(caseData, caseData.getRespondent1(), true);
            String respondent2MiscText =
                mapper.prepareRespondentResponseText(caseData, caseData.getRespondent2(), false);

            Event expectedDefenceFiled = Event.builder()
                .eventSequence(2)
                .eventCode("50")
                .dateReceived(caseData.getRespondent1ResponseDate())
                .litigiousPartyID("002")
                .build();
            List<Event> expectedMiscellaneousEvents = List.of(
                Event.builder()
                    .eventSequence(1)
                    .eventCode("999")
                    .dateReceived(caseData.getIssueDate().atStartOfDay())
                    .eventDetailsText("Claim issued in CCD.")
                    .eventDetails(EventDetails.builder()
                                      .miscText("Claim issued in CCD.")
                                      .build())
                    .build(),
                Event.builder()
                    .eventSequence(5)
                    .eventCode("999")
                    .dateReceived(caseData.getRespondent2ResponseDate())
                    .eventDetailsText(respondent2MiscText)
                    .eventDetails(EventDetails.builder()
                                      .miscText(respondent2MiscText)
                                      .build())
                    .build()
            );

            var eventHistory = mapper.buildEvents(caseData, BEARER_TOKEN);

            assertThat(eventHistory).isNotNull();
            assertThat(eventHistory).extracting("defenceFiled").asList()
                .containsExactly(expectedDefenceFiled);
            assertThat(eventHistory).extracting("miscellaneous").asList()
                .containsExactly(expectedMiscellaneousEvents.get(0), expectedMiscellaneousEvents.get(1));

            assertEmptyEvents(
                eventHistory,
                "defenceAndCounterClaim",
                "replyToDefence",
                "consentExtensionFilingDefence",
                "acknowledgementOfServiceReceived"
            );
        }

        @Test
        void shouldPrepareExpectedEvents_whenClaimWith1v2DiffSolicitorResp1FullyAdmitsResp2FullDef() {
            CaseData caseData = CaseDataBuilder.builder()
                .setClaimTypeToSpecClaim()
                .atState(FlowState.Main.AWAITING_RESPONSES_FULL_DEFENCE_RECEIVED)
                .multiPartyClaimTwoDefendantSolicitors()
                .atStateRespondent1v2FullDefence_AdmitFull()
                .atState1v2DivergentResponseSpec(
                    RespondentResponseTypeSpec.FULL_DEFENCE,
                    RespondentResponseTypeSpec.FULL_ADMISSION
                )
                .respondent2DQ()
                .respondent1DQ()
                .respondent1(PartyBuilder.builder().individual()
                                 .individualDateOfBirth(LocalDate.now().plusDays(1))
                                 .build())
                .build();
            if (caseData.getRespondent2Represented() == null && caseData.getRespondent2OrgRegistered() != null) {
                caseData = caseData.toBuilder()
                    .respondent2Represented(YesOrNo.YES)
                    .build();
            }
            String respondent1MiscText =
                mapper.prepareRespondentResponseText(caseData, caseData.getRespondent1(), true);
            String respondent2MiscText =
                mapper.prepareRespondentResponseText(caseData, caseData.getRespondent2(), false);

            Event expectedDefenceFiled = Event.builder()
                .eventSequence(2)
                .eventCode("50")
                .dateReceived(caseData.getRespondent1ResponseDate())
                .litigiousPartyID("002")
                .build();
            List<Event> expectedMiscellaneousEvents = List.of(
                Event.builder()
                    .eventSequence(1)
                    .eventCode("999")
                    .dateReceived(caseData.getIssueDate().atStartOfDay())
                    .eventDetailsText("Claim issued in CCD.")
                    .eventDetails(EventDetails.builder()
                                      .miscText("Claim issued in CCD.")
                                      .build())
                    .build(),
                Event.builder()
                    .eventSequence(5)
                    .eventCode("999")
                    .dateReceived(caseData.getRespondent2ResponseDate())
                    .eventDetailsText(respondent2MiscText)
                    .eventDetails(EventDetails.builder()
                                      .miscText(respondent2MiscText)
                                      .build())
                    .build()
            );

            var eventHistory = mapper.buildEvents(caseData, BEARER_TOKEN);

            assertThat(eventHistory).isNotNull();
            assertThat(eventHistory).extracting("defenceFiled").asList()
                .containsExactly(expectedDefenceFiled);
            assertThat(eventHistory).extracting("miscellaneous").asList()
                .containsExactly(expectedMiscellaneousEvents.get(0), expectedMiscellaneousEvents.get(1));

            assertEmptyEvents(
                eventHistory,
                "defenceAndCounterClaim",
                "replyToDefence",
                "consentExtensionFilingDefence",
                "acknowledgementOfServiceReceived"
            );
        }
    }

    @Nested
    class RespondentDivergentResponse {

        @Test
        void shouldPrepareExpectedEvents_whenClaimWith1v2DiffSolicitorResp1FullAdmitsResp2PartAdmitsNoOptionalEvents() {
            CaseData caseData = CaseDataBuilder.builder()
                .multiPartyClaimTwoDefendantSolicitors()
                .atState1v2DivergentResponse(FULL_ADMISSION, PART_ADMISSION)
                .build();
            if (caseData.getRespondent2OrgRegistered() != null
                && caseData.getRespondent2Represented() == null) {
                caseData = caseData.toBuilder()
                    .respondent2Represented(YES)
                    .build();
            }
            String respondent1MiscText =
                mapper.prepareRespondentResponseText(caseData, caseData.getRespondent1(), true);
            String respondent2MiscText =
                mapper.prepareRespondentResponseText(caseData, caseData.getRespondent2(), false);

            Event expectedReceiptOfAdmission = Event.builder()
                .eventSequence(4)
                .eventCode("40")
                .dateReceived(caseData.getRespondent1ResponseDate())
                .litigiousPartyID("002")
                .build();
            Event expectedReceiptOfPartAdmission = Event.builder()
                .eventSequence(6)
                .eventCode("60")
                .dateReceived(caseData.getRespondent2ResponseDate())
                .litigiousPartyID("003")
                .build();
            List<Event> expectedMiscellaneousEvents = List.of(
                Event.builder()
                    .eventSequence(1)
                    .eventCode("999")
                    .dateReceived(caseData.getIssueDate().atStartOfDay())
                    .eventDetailsText("Claim issued in CCD.")
                    .eventDetails(EventDetails.builder()
                                      .miscText("Claim issued in CCD.")
                                      .build())
                    .build(),
                Event.builder()
                    .eventSequence(2)
                    .eventCode("999")
                    .dateReceived(caseData.getClaimNotificationDate())
                    .eventDetailsText("Claimant has notified defendant.")
                    .eventDetails(EventDetails.builder()
                                      .miscText("Claimant has notified defendant.")
                                      .build())
                    .build(),
                Event.builder()
                    .eventSequence(3)
                    .eventCode("999")
                    .dateReceived(caseData.getClaimDetailsNotificationDate())
                    .eventDetailsText("Claim details notified.")
                    .eventDetails(EventDetails.builder()
                                      .miscText("Claim details notified.")
                                      .build())
                    .build(),
                Event.builder()
                    .eventSequence(5)
                    .eventCode("999")
                    .dateReceived(caseData.getRespondent1ResponseDate())
                    .eventDetailsText(respondent1MiscText)
                    .eventDetails(EventDetails.builder()
                                      .miscText(respondent1MiscText)
                                      .build())
                    .build(),
                Event.builder()
                    .eventSequence(7)
                    .eventCode("999")
                    .dateReceived(caseData.getRespondent2ResponseDate())
                    .eventDetailsText(respondent2MiscText)
                    .eventDetails(EventDetails.builder()
                                      .miscText(respondent2MiscText)
                                      .build())
                    .build()
            );

            var eventHistory = mapper.buildEvents(caseData, BEARER_TOKEN);

            assertThat(eventHistory).isNotNull();
            assertThat(eventHistory).extracting("receiptOfAdmission").asList()
                .containsExactly(expectedReceiptOfAdmission);
            assertThat(eventHistory).extracting("receiptOfPartAdmission").asList()
                .containsExactly(expectedReceiptOfPartAdmission);
            assertThat(eventHistory).extracting("miscellaneous").asList()
                .containsExactly(expectedMiscellaneousEvents.get(0),
                                 expectedMiscellaneousEvents.get(1), expectedMiscellaneousEvents.get(2),
                                 expectedMiscellaneousEvents.get(3), expectedMiscellaneousEvents.get(4)
            );

            assertEmptyEvents(
                eventHistory,
                "defenceFiled",
                "defenceAndCounterClaim",
                "replyToDefence",
                "directionsQuestionnaireFiled",
                "consentExtensionFilingDefence",
                "acknowledgementOfServiceReceived"
            );
        }

        @Test
        void shouldPrepareExpectedEvents_whenClaimWith1v2DiffSolicitorResp1FullyAdmitsResp2FullDefNoOptionalEvents() {
            CaseData caseData = CaseDataBuilder.builder()
                .multiPartyClaimTwoDefendantSolicitors()
                .atState1v2DivergentResponse(FULL_ADMISSION, FULL_DEFENCE)
                .respondent2DQ()
                .build();
            if (caseData.getRespondent2OrgRegistered() != null
                && caseData.getRespondent2Represented() == null) {
                caseData = caseData.toBuilder()
                    .respondent2Represented(YES)
                    .build();
            }
            String respondent1MiscText =
                mapper.prepareRespondentResponseText(caseData, caseData.getRespondent1(), true);
            String respondent2MiscText =
                mapper.prepareRespondentResponseText(caseData, caseData.getRespondent2(), false);

            Event expectedReceiptOfAdmission = Event.builder()
                .eventSequence(4)
                .eventCode("40")
                .dateReceived(caseData.getRespondent1ResponseDate())
                .litigiousPartyID("002")
                .build();
            Event expectedDefenceFiled = Event.builder()
                .eventSequence(6)
                .eventCode("50")
                .dateReceived(caseData.getRespondent2ResponseDate())
                .litigiousPartyID("003")
                .build();
            Event expectedDirectionsQuestionnaireFiled = Event.builder()
                .eventSequence(7)
                .eventCode("197")
                .dateReceived(caseData.getRespondent2ResponseDate())
                .litigiousPartyID("003")
                .eventDetailsText(mapper.prepareFullDefenceEventText(
                    caseData.getRespondent2DQ(),
                    caseData,
                    false,
                    caseData.getRespondent2()
                ))
                .eventDetails(EventDetails.builder()
                                  .stayClaim(mapper.isStayClaim(caseData.getRespondent2DQ()))
                                  .preferredCourtCode(mapper.getPreferredCourtCode(caseData.getRespondent2DQ()))
                                  .preferredCourtName("")
                                  .build())
                .build();
            List<Event> expectedMiscellaneousEvents = List.of(
                Event.builder()
                    .eventSequence(1)
                    .eventCode("999")
                    .dateReceived(caseData.getIssueDate().atStartOfDay())
                    .eventDetailsText("Claim issued in CCD.")
                    .eventDetails(EventDetails.builder()
                                      .miscText("Claim issued in CCD.")
                                      .build())
                    .build(),

                Event.builder()
                    .eventSequence(2)
                    .eventCode("999")
                    .dateReceived(caseData.getClaimNotificationDate())
                    .eventDetailsText("Claimant has notified defendant.")
                    .eventDetails(EventDetails.builder()
                                      .miscText("Claimant has notified defendant.")
                                      .build())
                    .build(),
                Event.builder()
                    .eventSequence(3)
                    .eventCode("999")
                    .dateReceived(caseData.getClaimDetailsNotificationDate())
                    .eventDetailsText("Claim details notified.")
                    .eventDetails(EventDetails.builder()
                                      .miscText("Claim details notified.")
                                      .build())
                    .build(),
                Event.builder()
                    .eventSequence(5)
                    .eventCode("999")
                    .dateReceived(caseData.getRespondent1ResponseDate())
                    .eventDetailsText(respondent1MiscText)
                    .eventDetails(EventDetails.builder()
                                      .miscText(respondent1MiscText)
                                      .build())
                    .build()
            );

            var eventHistory = mapper.buildEvents(caseData, BEARER_TOKEN);

            assertThat(eventHistory).isNotNull();
            assertThat(eventHistory).extracting("receiptOfAdmission").asList()
                .containsExactly(expectedReceiptOfAdmission);
            assertThat(eventHistory).extracting("defenceFiled").asList()
                .containsExactly(expectedDefenceFiled);
            assertThat(eventHistory).extracting("directionsQuestionnaireFiled").asList()
                .containsExactly(expectedDirectionsQuestionnaireFiled);
            assertThat(eventHistory).extracting("miscellaneous").asList()
                .containsExactly(expectedMiscellaneousEvents.get(0),
                                 expectedMiscellaneousEvents.get(1),
                                 expectedMiscellaneousEvents.get(2), expectedMiscellaneousEvents.get(3)
            );

            assertEmptyEvents(
                eventHistory,
                "defenceAndCounterClaim",
                "receiptOfPartAdmission",
                "replyToDefence",
                "consentExtensionFilingDefence",
                "acknowledgementOfServiceReceived"
            );
        }

        @Test
        void shouldPrepareExpectedEvents_whenClaimWith1v2SameSolResp1FullAdmissionResp2PartAdmissionNoOptionalEvents() {
            CaseData caseData = CaseDataBuilder.builder()
                .multiPartyClaimOneDefendantSolicitor()
                .atState1v2SameSolicitorDivergentResponse(FULL_ADMISSION, PART_ADMISSION)
                .respondentResponseIsSame(NO)
                .build();
            if (caseData.getRespondent2OrgRegistered() != null
                && caseData.getRespondent2Represented() == null) {
                caseData = caseData.toBuilder()
                    .respondent2Represented(YES)
                    .build();
            }
            String respondent1MiscText =
                mapper.prepareRespondentResponseText(caseData, caseData.getRespondent1(), true);
            String respondent2MiscText =
                mapper.prepareRespondentResponseText(caseData, caseData.getRespondent2(), false);

            Event expectedReceiptOfAdmission = Event.builder()
                .eventSequence(5)
                .eventCode("40")
                .dateReceived(caseData.getRespondent1ResponseDate())
                .litigiousPartyID("002")
                .build();
            Event expectedReceiptOfPartAdmission = Event.builder()
                .eventSequence(4)
                .eventCode("60")
                .dateReceived(caseData.getRespondent2ResponseDate())
                .litigiousPartyID("003")
                .build();
            List<Event> expectedMiscellaneousEvents = List.of(
                Event.builder()
                    .eventSequence(1)
                    .eventCode("999")
                    .dateReceived(caseData.getIssueDate().atStartOfDay())
                    .eventDetailsText("Claim issued in CCD.")
                    .eventDetails(EventDetails.builder()
                                      .miscText("Claim issued in CCD.")
                                      .build())
                    .build(),
                Event.builder()
                    .eventSequence(2)
                    .eventCode("999")
                    .dateReceived(caseData.getClaimNotificationDate())
                    .eventDetailsText("Claimant has notified defendant.")
                    .eventDetails(EventDetails.builder()
                                      .miscText("Claimant has notified defendant.")
                                      .build())
                    .build(),
                Event.builder()
                    .eventSequence(3)
                    .eventCode("999")
                    .dateReceived(caseData.getClaimDetailsNotificationDate())
                    .eventDetailsText("Claim details notified.")
                    .eventDetails(EventDetails.builder()
                                      .miscText("Claim details notified.")
                                      .build())
                    .build(),
                Event.builder()
                    .eventSequence(6)
                    .eventCode("999")
                    .dateReceived(caseData.getRespondent1ResponseDate())
                    .eventDetailsText(respondent1MiscText)
                    .eventDetails(EventDetails.builder()
                                      .miscText(respondent1MiscText)
                                      .build())
                    .build(),
                Event.builder()
                    .eventSequence(7)
                    .eventCode("999")
                    .dateReceived(caseData.getRespondent2ResponseDate())
                    .eventDetailsText(respondent2MiscText)
                    .eventDetails(EventDetails.builder()
                                      .miscText(respondent2MiscText)
                                      .build())
                    .build()
            );

            var eventHistory = mapper.buildEvents(caseData, BEARER_TOKEN);

            assertThat(eventHistory).isNotNull();
            assertThat(eventHistory).extracting("receiptOfAdmission").asList()
                .containsExactly(expectedReceiptOfAdmission);
            assertThat(eventHistory).extracting("receiptOfPartAdmission").asList()
                .containsExactly(expectedReceiptOfPartAdmission);
            assertThat(eventHistory).extracting("miscellaneous").asList()
                .containsExactly(expectedMiscellaneousEvents.get(0),
                                 expectedMiscellaneousEvents.get(1), expectedMiscellaneousEvents.get(2),
                                 expectedMiscellaneousEvents.get(3), expectedMiscellaneousEvents.get(4)
            );

            assertEmptyEvents(
                eventHistory,
                "defenceFiled",
                "defenceAndCounterClaim",
                "replyToDefence",
                "directionsQuestionnaireFiled",
                "consentExtensionFilingDefence",
                "acknowledgementOfServiceReceived"
            );
        }

        @Test
        void shouldPrepareExpectedEvents_whenClaimWith1v2SameSolResp1FullAdmissionResp2FullDefenceNoOptionalEvents() {
            CaseData caseData = CaseDataBuilder.builder()
                .multiPartyClaimOneDefendantSolicitor()
                .atState1v2SameSolicitorDivergentResponse(FULL_ADMISSION, FULL_DEFENCE)
                .respondentResponseIsSame(NO)
                .respondent2DQ()
                .build();
            if (caseData.getRespondent2OrgRegistered() != null
                && caseData.getRespondent2Represented() == null) {
                caseData = caseData.toBuilder()
                    .respondent2Represented(YES)
                    .build();
            }
            String respondent1MiscText =
                mapper.prepareRespondentResponseText(caseData, caseData.getRespondent1(), true);
            String respondent2MiscText =
                mapper.prepareRespondentResponseText(caseData, caseData.getRespondent2(), false);

            Event expectedReceiptOfAdmission = Event.builder()
                .eventSequence(5)
                .eventCode("40")
                .dateReceived(caseData.getRespondent1ResponseDate())
                .litigiousPartyID("002")
                .build();
            Event expectedDefenceFiled = Event.builder()
                .eventSequence(4)
                .eventCode("50")
                .dateReceived(caseData.getRespondent2ResponseDate())
                .litigiousPartyID("003")
                .build();
            Event expectedDirectionsQuestionnaireFiled = Event.builder()
                .eventSequence(6)
                .eventCode("197")
                .dateReceived(caseData.getRespondent2ResponseDate())
                .litigiousPartyID("003")
                .eventDetailsText(mapper.prepareFullDefenceEventText(
                    caseData.getRespondent2DQ(),
                    caseData,
                    false,
                    caseData.getRespondent2()
                ))
                .eventDetails(EventDetails.builder()
                                  .stayClaim(mapper.isStayClaim(caseData.getRespondent2DQ()))
                                  .preferredCourtCode(mapper.getPreferredCourtCode(caseData.getRespondent2DQ()))
                                  .preferredCourtName("")
                                  .build())
                .build();
            List<Event> expectedMiscellaneousEvents = List.of(
                Event.builder()
                    .eventSequence(1)
                    .eventCode("999")
                    .dateReceived(caseData.getIssueDate().atStartOfDay())
                    .eventDetailsText("Claim issued in CCD.")
                    .eventDetails(EventDetails.builder()
                                      .miscText("Claim issued in CCD.")
                                      .build())
                    .build(),
                Event.builder()
                    .eventSequence(2)
                    .eventCode("999")
                    .dateReceived(caseData.getClaimNotificationDate())
                    .eventDetailsText("Claimant has notified defendant.")
                    .eventDetails(EventDetails.builder()
                                      .miscText("Claimant has notified defendant.")
                                      .build())
                    .build(),

                Event.builder()
                    .eventSequence(3)
                    .eventCode("999")
                    .dateReceived(caseData.getClaimDetailsNotificationDate())
                    .eventDetailsText("Claim details notified.")
                    .eventDetails(EventDetails.builder()
                                      .miscText("Claim details notified.")
                                      .build())
                    .build(),
                Event.builder()
                    .eventSequence(7)
                    .eventCode("999")
                    .dateReceived(caseData.getRespondent1ResponseDate())
                    .eventDetailsText(respondent1MiscText)
                    .eventDetails(EventDetails.builder()
                                      .miscText(respondent1MiscText)
                                      .build())
                    .build()
            );

            var eventHistory = mapper.buildEvents(caseData, BEARER_TOKEN);

            assertThat(eventHistory).isNotNull();
            assertThat(eventHistory).extracting("receiptOfAdmission").asList()
                .containsExactly(expectedReceiptOfAdmission);
            assertThat(eventHistory).extracting("defenceFiled").asList()
                .containsExactly(expectedDefenceFiled);
            assertThat(eventHistory).extracting("directionsQuestionnaireFiled").asList()
                .containsExactly(expectedDirectionsQuestionnaireFiled);
            assertThat(eventHistory).extracting("miscellaneous").asList()
                .containsExactly(expectedMiscellaneousEvents.get(0),
                                 expectedMiscellaneousEvents.get(1),
                                 expectedMiscellaneousEvents.get(2), expectedMiscellaneousEvents.get(3)
            );

            assertEmptyEvents(
                eventHistory,
                "defenceAndCounterClaim",
                "receiptOfPartAdmission",
                "replyToDefence",
                "consentExtensionFilingDefence",
                "acknowledgementOfServiceReceived"
            );
        }

        @Test
        void shouldPrepareExpectedEvents_whenClaimWith1v2ssR1FullAdmissionR2FullDefenceNoOptionalEventsSpec() {
            CaseData caseData = CaseDataBuilder.builder()
                .setClaimTypeToSpecClaim()
                .multiPartyClaimOneDefendantSolicitor()
                .atState1v2SameSolicitorDivergentResponse(FULL_ADMISSION, FULL_DEFENCE)
                .respondentResponseIsSame(NO)
                .respondent2DQ()
                .build();
            if (caseData.getRespondent2OrgRegistered() != null
                && caseData.getRespondent2Represented() == null) {
                caseData = caseData.toBuilder()
                    .respondent2Represented(YES)
                    .build();
            }
            String respondent1MiscText =
                mapper.prepareRespondentResponseText(caseData, caseData.getRespondent1(), true);
            String respondent2MiscText =
                mapper.prepareRespondentResponseText(caseData, caseData.getRespondent2(), false);

            Event expectedReceiptOfAdmission = Event.builder()
                .eventSequence(3)
                .eventCode("40")
                .dateReceived(caseData.getRespondent1ResponseDate())
                .litigiousPartyID("002")
                .build();
            Event expectedDefenceFiled = Event.builder()
                .eventSequence(2)
                .eventCode("50")
                .dateReceived(caseData.getRespondent1ResponseDate())
                .litigiousPartyID("003")
                .build();
            Event expectedDirectionsQuestionnaireFiled = Event.builder()
                .eventSequence(4)
                .eventCode("197")
                .dateReceived(caseData.getRespondent1ResponseDate())
                .litigiousPartyID("003")
                .eventDetailsText(mapper.prepareFullDefenceEventText(
                    caseData.getRespondent2DQ(),
                    caseData,
                    false,
                    caseData.getRespondent2()
                ))
                .eventDetails(EventDetails.builder()
                                  .stayClaim(mapper.isStayClaim(caseData.getRespondent2DQ()))
                                  .preferredCourtCode(mapper.getPreferredCourtCode(caseData.getRespondent2DQ()))
                                  .preferredCourtName("")
                                  .build())
                .build();
            List<Event> expectedMiscellaneousEvents = List.of(
                Event.builder()
                    .eventSequence(1)
                    .eventCode("999")
                    .dateReceived(caseData.getIssueDate().atStartOfDay())
                    .eventDetailsText("Claim issued in CCD.")
                    .eventDetails(EventDetails.builder()
                                      .miscText("Claim issued in CCD.")
                                      .build())
                    .build(),
                Event.builder()
                    .eventSequence(5)
                    .eventCode("999")
                    .dateReceived(caseData.getRespondent1ResponseDate())
                    .eventDetailsText(respondent1MiscText)
                    .eventDetails(EventDetails.builder()
                                      .miscText(respondent1MiscText)
                                      .build())
                    .build()
            );

            var eventHistory = mapper.buildEvents(caseData, BEARER_TOKEN);

            assertThat(eventHistory).isNotNull();
            assertThat(eventHistory).extracting("receiptOfAdmission").asList()
                .containsExactly(expectedReceiptOfAdmission);
            assertThat(eventHistory).extracting("defenceFiled").asList()
                .containsExactly(expectedDefenceFiled);
            assertThat(eventHistory).extracting("directionsQuestionnaireFiled").asList()
                .containsExactly(expectedDirectionsQuestionnaireFiled);
            assertThat(eventHistory).extracting("miscellaneous").asList()
                .containsExactly(expectedMiscellaneousEvents.get(0), expectedMiscellaneousEvents.get(1));

            assertEmptyEvents(
                eventHistory,
                "defenceAndCounterClaim",
                "receiptOfPartAdmission",
                "replyToDefence",
                "consentExtensionFilingDefence",
                "acknowledgementOfServiceReceived"
            );
        }
    }

    @Nested
    class AwaitingResponses {
        //Only happens in 1v2 diff solicitor when only one defendant has replied

        @Nested
        class FullDefence {
            @Test
            void shouldPrepareExpectedEvents_whenDefendantResponseFullDefence() {
                CaseData caseData = CaseDataBuilder.builder()
                    .multiPartyClaimTwoDefendantSolicitors()
                    .atState(FlowState.Main.AWAITING_RESPONSES_FULL_DEFENCE_RECEIVED)
                    .build();
                if (caseData.getRespondent2OrgRegistered() != null
                    && caseData.getRespondent2Represented() == null) {
                    caseData = caseData.toBuilder()
                        .respondent2Represented(YES)
                        .build();
                }
                List<Event> expectedDefenceFiled =
                    List.of(Event.builder()
                                .eventSequence(4)
                                .eventCode("50")
                                .dateReceived(caseData.getRespondent1ResponseDate())
                                .litigiousPartyID("002")
                                .build());
                List<Event> expectedDirectionsQuestionnaireFiled =
                    List.of(Event.builder()
                                .eventSequence(5)
                                .eventCode("197")
                                .dateReceived(caseData.getRespondent1ResponseDate())
                                .litigiousPartyID("002")
                                .eventDetailsText(mapper.prepareFullDefenceEventText(
                                    caseData.getRespondent1DQ(),
                                    caseData,
                                    true,
                                    caseData.getRespondent1()
                                ))
                                .eventDetails(EventDetails.builder()
                                                  .stayClaim(mapper.isStayClaim(caseData.getRespondent1DQ()))
                                                  .preferredCourtCode(
                                                      mapper.getPreferredCourtCode(caseData.getRespondent1DQ()))
                                                  .preferredCourtName("").build()).build());

                var eventHistory = mapper.buildEvents(caseData, BEARER_TOKEN);

                assertThat(eventHistory).isNotNull();
                assertThat(eventHistory).extracting("defenceFiled").asList()
                    .containsExactly(expectedDefenceFiled.get(0));
                assertThat(eventHistory).extracting("directionsQuestionnaireFiled").asList().containsExactly(
                    expectedDirectionsQuestionnaireFiled.get(0));
                assertEmptyEvents(
                    eventHistory,
                    "receiptOfAdmission",
                    "receiptOfPartAdmission",
                    "acknowledgementOfServiceReceived",
                    "replyToDefence",
                    "consentExtensionFilingDefence"
                );
            }
        }

        @Nested
        class NotFullDefence {

            @Test
            void shouldPrepareExpectedEvents_whenDefendantResponseFullAdmits() {
                CaseData caseData = CaseDataBuilder.builder()
                    .multiPartyClaimTwoDefendantSolicitors()
                    .atState(FlowState.Main.AWAITING_RESPONSES_NOT_FULL_DEFENCE_OR_FULL_ADMIT_RECEIVED)
                    .build();
                if (caseData.getRespondent2OrgRegistered() != null
                    && caseData.getRespondent2Represented() == null) {
                    caseData = caseData.toBuilder()
                        .respondent2Represented(YES)
                        .build();
                }
                Event expectedReceiptOfAdmission = Event.builder()
                    .eventSequence(4)
                    .eventCode("40")
                    .dateReceived(caseData.getRespondent1ResponseDate())
                    .litigiousPartyID("002")
                    .build();
                List<Event> expectedMiscellaneousEvents = List.of(
                    Event.builder()
                        .eventSequence(1)
                        .eventCode("999")
                        .dateReceived(caseData.getIssueDate().atStartOfDay())
                        .eventDetailsText("Claim issued in CCD.")
                        .eventDetails(EventDetails.builder()
                                          .miscText("Claim issued in CCD.")
                                          .build())
                        .build(),
                    Event.builder()
                        .eventSequence(2)
                        .eventCode("999")
                        .dateReceived(caseData.getClaimNotificationDate())
                        .eventDetailsText("Claimant has notified defendant.")
                        .eventDetails(EventDetails.builder()
                                          .miscText("Claimant has notified defendant.")
                                          .build())
                        .build(),
                    Event.builder()
                        .eventSequence(3)
                        .eventCode("999")
                        .dateReceived(caseData.getClaimDetailsNotificationDate())
                        .eventDetailsText("Claim details notified.")
                        .eventDetails(EventDetails.builder()
                                          .miscText("Claim details notified.")
                                          .build())
                        .build(),
                    Event.builder()
                        .eventSequence(5)
                        .eventCode("999")
                        .dateReceived(caseData.getRespondent1ResponseDate())
                        .eventDetailsText("RPA Reason: Defendant: Mr. Sole Trader has responded: FULL_ADMISSION")
                        .eventDetails(EventDetails.builder()
                                          .miscText(
                                              "RPA Reason: Defendant: Mr. Sole Trader has responded: FULL_ADMISSION")
                                          .build())
                        .build()
                );

                var eventHistory = mapper.buildEvents(caseData, BEARER_TOKEN);

                assertThat(eventHistory).isNotNull();
                assertThat(eventHistory).extracting("receiptOfAdmission").asList().containsExactly(
                    expectedReceiptOfAdmission);
                assertThat(eventHistory).extracting("miscellaneous").asList()
                    .containsExactly(expectedMiscellaneousEvents.get(0), expectedMiscellaneousEvents.get(1),
                                     expectedMiscellaneousEvents.get(2), expectedMiscellaneousEvents.get(3)
                );
                assertEmptyEvents(
                    eventHistory,
                    "receiptOfPartAdmission",
                    "acknowledgementOfServiceReceived",
                    "replyToDefence",
                    "defenceFiled",
                    "directionsQuestionnaireFiled",
                    "consentExtensionFilingDefence"
                );
            }

            @Test
            void shouldPrepareExpectedEvents_whenDefendantResponsePartAdmits() {
                CaseData caseData = CaseDataBuilder.builder()
                    .multiPartyClaimTwoDefendantSolicitors()
                    .atStateAwaitingResponseNotFullDefenceReceived(PART_ADMISSION)
                    .build();
                if (caseData.getRespondent2OrgRegistered() != null
                    && caseData.getRespondent2Represented() == null) {
                    caseData = caseData.toBuilder()
                        .respondent2Represented(YES)
                        .build();
                }
                Event expectedReceiptOfPartAdmission = Event.builder()
                    .eventSequence(4)
                    .eventCode("60")
                    .dateReceived(caseData.getRespondent1ResponseDate())
                    .litigiousPartyID("002")
                    .build();
                List<Event> expectedMiscellaneousEvents = List.of(
                    Event.builder()
                        .eventSequence(1)
                        .eventCode("999")
                        .dateReceived(caseData.getIssueDate().atStartOfDay())
                        .eventDetailsText("Claim issued in CCD.")
                        .eventDetails(EventDetails.builder()
                                          .miscText("Claim issued in CCD.")
                                          .build())
                        .build(),
                    Event.builder()
                        .eventSequence(2)
                        .eventCode("999")
                        .dateReceived(caseData.getClaimNotificationDate())
                        .eventDetailsText("Claimant has notified defendant.")
                        .eventDetails(EventDetails.builder()
                                          .miscText("Claimant has notified defendant.")
                                          .build())
                        .build(),
                    Event.builder()
                        .eventSequence(3)
                        .eventCode("999")
                        .dateReceived(caseData.getClaimDetailsNotificationDate())
                        .eventDetailsText("Claim details notified.")
                        .eventDetails(EventDetails.builder()
                                          .miscText("Claim details notified.")
                                          .build())
                        .build(),
                    Event.builder()
                        .eventSequence(5)
                        .eventCode("999")
                        .dateReceived(caseData.getRespondent1ResponseDate())
                        .eventDetailsText("RPA Reason: Defendant: Mr. Sole Trader has responded: PART_ADMISSION")
                        .eventDetails(EventDetails.builder()
                                          .miscText(
                                              "RPA Reason: Defendant: Mr. Sole Trader has responded: PART_ADMISSION")
                                          .build())
                        .build()
                );

                var eventHistory = mapper.buildEvents(caseData, BEARER_TOKEN);

                assertThat(eventHistory).isNotNull();
                assertThat(eventHistory).extracting("receiptOfPartAdmission").asList()
                    .containsExactly(expectedReceiptOfPartAdmission);
                assertThat(eventHistory).extracting("miscellaneous").asList()
                    .containsExactly(expectedMiscellaneousEvents.get(0), expectedMiscellaneousEvents.get(1),
                                     expectedMiscellaneousEvents.get(2), expectedMiscellaneousEvents.get(3)
                );
                assertEmptyEvents(
                    eventHistory,
                    "receiptOfAdmission",
                    "acknowledgementOfServiceReceived",
                    "replyToDefence",
                    "defenceFiled",
                    "directionsQuestionnaireFiled",
                    "consentExtensionFilingDefence"
                );
            }

            @Test
            void shouldPrepareExpectedEvents_whenDefendantResponseCounterClaim() {
                CaseData caseData = CaseDataBuilder.builder()
                    .multiPartyClaimTwoDefendantSolicitors()
                    .atStateAwaitingResponseNotFullDefenceReceived(COUNTER_CLAIM)
                    .build();
                if (caseData.getRespondent2OrgRegistered() != null
                    && caseData.getRespondent2Represented() == null) {
                    caseData = caseData.toBuilder()
                        .respondent2Represented(YES)
                        .build();
                }

                var eventHistory = mapper.buildEvents(caseData, BEARER_TOKEN);
                assertThat(eventHistory).isNotNull();

                assertEmptyEvents(
                    eventHistory,
                    "receiptOfAdmission",
                    "acknowledgementOfServiceReceived",
                    "replyToDefence",
                    "defenceFiled",
                    "directionsQuestionnaireFiled",
                    "consentExtensionFilingDefence"
                );
            }
        }

    }

    @Nested
    class FullDefenceNotProceeds {

        @Nested
        class OneVOne {
            @Test
            void shouldPrepareExpectedEvents_whenClaimantDoesNotProceed() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atState(FlowState.Main.FULL_DEFENCE_NOT_PROCEED)
                    .build();
                if (caseData.getRespondent2OrgRegistered() != null
                    && caseData.getRespondent2Represented() == null) {
                    caseData = caseData.toBuilder()
                        .respondent2Represented(YES)
                        .build();
                }
                Event expectedDefenceFiled = Event.builder()
                    .eventSequence(5)
                    .eventCode("50")
                    .dateReceived(caseData.getRespondent1ResponseDate())
                    .litigiousPartyID("002")
                    .build();
                Event expectedDirectionsQuestionnaireFiled = Event.builder()
                    .eventSequence(6)
                    .eventCode("197")
                    .dateReceived(caseData.getRespondent1ResponseDate())
                    .litigiousPartyID("002")
                    .eventDetailsText(mapper.prepareFullDefenceEventText(
                        caseData.getRespondent1DQ(), caseData,
                        true, caseData.getRespondent1()
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
                        .dateReceived(caseData.getIssueDate().atStartOfDay())
                        .eventDetailsText("Claim issued in CCD.")
                        .eventDetails(EventDetails.builder()
                                          .miscText("Claim issued in CCD.")
                                          .build())
                        .build(),
                    Event.builder()
                        .eventSequence(2)
                        .eventCode("999")
                        .dateReceived(caseData.getClaimNotificationDate())
                        .eventDetailsText("Claimant has notified defendant.")
                        .eventDetails(EventDetails.builder()
                                          .miscText("Claimant has notified defendant.")
                                          .build())
                        .build(),
                    Event.builder()
                        .eventSequence(3)
                        .eventCode("999")
                        .dateReceived(caseData.getClaimDetailsNotificationDate())
                        .eventDetailsText("Claim details notified.")
                        .eventDetails(EventDetails.builder()
                                          .miscText("Claim details notified.")
                                          .build())
                        .build(),
                    Event.builder()
                        .eventSequence(7)
                        .eventCode("999")
                        .dateReceived(caseData.getApplicant1ResponseDate())
                        .eventDetailsText("RPA Reason: Claimant intends not to proceed.")
                        .eventDetails(EventDetails.builder()
                                          .miscText("RPA Reason: Claimant intends not to proceed.")
                                          .build())
                        .build()
                );

                var eventHistory = mapper.buildEvents(caseData, BEARER_TOKEN);

                assertThat(eventHistory).isNotNull();
                assertThat(eventHistory).extracting("defenceFiled").asList()
                    .containsExactly(expectedDefenceFiled);
                assertThat(eventHistory).extracting("directionsQuestionnaireFiled").asList()
                    .containsExactly(expectedDirectionsQuestionnaireFiled);
                assertThat(eventHistory).extracting("miscellaneous").asList()
                    .containsExactly(expectedMiscellaneousEvents.get(0), expectedMiscellaneousEvents.get(1),
                                     expectedMiscellaneousEvents.get(2), expectedMiscellaneousEvents.get(3)
                );

                assertEmptyEvents(
                    eventHistory,
                    "receiptOfAdmission",
                    "receiptOfPartAdmission",
                    "replyToDefence"
                );
            }
        }

        @Nested
        class OneVTwo {
            @Test
            void shouldPrepareExpectedEvents_whenClaimantDoesNotProceed() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atState(FlowState.Main.FULL_DEFENCE_NOT_PROCEED)
                    .atStateApplicantRespondToDefenceAndNotProceed_1v2()
                    .build();
                if (caseData.getRespondent2OrgRegistered() != null
                    && caseData.getRespondent2Represented() == null) {
                    caseData = caseData.toBuilder()
                        .respondent2Represented(YES)
                        .build();
                }
                Event expectedDefenceFiled = Event.builder()
                    .eventSequence(5)
                    .eventCode("50")
                    .dateReceived(caseData.getRespondent1ResponseDate())
                    .litigiousPartyID("002")
                    .build();
                Event expectedDirectionsQuestionnaireFiled = Event.builder()
                    .eventSequence(6)
                    .eventCode("197")
                    .dateReceived(caseData.getRespondent1ResponseDate())
                    .litigiousPartyID("002")
                    .eventDetailsText(mapper.prepareFullDefenceEventText(
                        caseData.getRespondent1DQ(), caseData,
                        true, caseData.getRespondent1()
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
                        .dateReceived(caseData.getIssueDate().atStartOfDay())
                        .eventDetailsText("Claim issued in CCD.")
                        .eventDetails(EventDetails.builder()
                                          .miscText("Claim issued in CCD.")
                                          .build())
                        .build(),
                    Event.builder()
                        .eventSequence(2)
                        .eventCode("999")
                        .dateReceived(caseData.getClaimNotificationDate())
                        .eventDetailsText("Claimant has notified defendant.")
                        .eventDetails(EventDetails.builder()
                                          .miscText("Claimant has notified defendant.")
                                          .build())
                        .build(),
                    Event.builder()
                        .eventSequence(3)
                        .eventCode("999")
                        .dateReceived(caseData.getClaimDetailsNotificationDate())
                        .eventDetailsText("Claim details notified.")
                        .eventDetails(EventDetails.builder()
                                          .miscText("Claim details notified.")
                                          .build())
                        .build(),
                    Event.builder()
                        .eventSequence(7)
                        .eventCode("999")
                        .dateReceived(caseData.getApplicant1ResponseDate())
                        .eventDetailsText("RPA Reason: Claimant intends not to proceed.")
                        .eventDetails(EventDetails.builder()
                                          .miscText("RPA Reason: Claimant intends not to proceed.")
                                          .build())
                        .build()
                );

                var eventHistory = mapper.buildEvents(caseData, BEARER_TOKEN);

                assertThat(eventHistory).isNotNull();
                assertThat(eventHistory).extracting("defenceFiled").asList()
                    .containsExactly(expectedDefenceFiled);
                assertThat(eventHistory).extracting("directionsQuestionnaireFiled").asList()
                    .containsExactly(expectedDirectionsQuestionnaireFiled);
                assertThat(eventHistory).extracting("miscellaneous").asList()
                    .containsExactly(expectedMiscellaneousEvents.get(0), expectedMiscellaneousEvents.get(1),
                                     expectedMiscellaneousEvents.get(2), expectedMiscellaneousEvents.get(3)
                );

                assertEmptyEvents(
                    eventHistory,
                    "receiptOfAdmission",
                    "receiptOfPartAdmission",
                    "replyToDefence"
                );
            }
        }

        @Nested
        class TwoVOne {
            @Test
            void shouldPrepareExpectedEvents_whenClaimantsDoNotProceed() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atState(FlowState.Main.FULL_DEFENCE_NOT_PROCEED)
                    .multiPartyClaimTwoApplicants()
                    .atStateApplicantRespondToDefenceAndNotProceed_2v1()
                    .build();
                if (caseData.getRespondent2OrgRegistered() != null
                    && caseData.getRespondent2Represented() == null) {
                    caseData = caseData.toBuilder()
                        .respondent2Represented(YES)
                        .build();
                }
                Event expectedDefenceFiled = Event.builder()
                    .eventSequence(5)
                    .eventCode("50")
                    .dateReceived(caseData.getRespondent1ResponseDate())
                    .litigiousPartyID("002")
                    .build();
                Event expectedDirectionsQuestionnaireFiled = Event.builder()
                    .eventSequence(6)
                    .eventCode("197")
                    .dateReceived(caseData.getRespondent1ResponseDate())
                    .litigiousPartyID("002")
                    .eventDetailsText(mapper.prepareFullDefenceEventText(
                        caseData.getRespondent1DQ(), caseData,
                        true, caseData.getRespondent1()
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
                        .dateReceived(caseData.getIssueDate().atStartOfDay())
                        .eventDetailsText("Claim issued in CCD.")
                        .eventDetails(EventDetails.builder()
                                          .miscText("Claim issued in CCD.")
                                          .build())
                        .build(),
                    Event.builder()
                        .eventSequence(2)
                        .eventCode("999")
                        .dateReceived(caseData.getClaimNotificationDate())
                        .eventDetailsText("Claimant has notified defendant.")
                        .eventDetails(EventDetails.builder()
                                          .miscText("Claimant has notified defendant.")
                                          .build())
                        .build(),
                    Event.builder()
                        .eventSequence(3)
                        .eventCode("999")
                        .dateReceived(caseData.getClaimDetailsNotificationDate())
                        .eventDetailsText("Claim details notified.")
                        .eventDetails(EventDetails.builder()
                                          .miscText("Claim details notified.")
                                          .build())
                        .build(),
                    Event.builder()
                        .eventSequence(7)
                        .eventCode("999")
                        .dateReceived(caseData.getApplicant1ResponseDate())
                        .eventDetailsText("RPA Reason: Claimants intend not to proceed.")
                        .eventDetails(EventDetails.builder()
                                          .miscText("RPA Reason: Claimants intend not to proceed.")
                                          .build())
                        .build()
                );

                var eventHistory = mapper.buildEvents(caseData, BEARER_TOKEN);

                assertThat(eventHistory).isNotNull();
                assertThat(eventHistory).extracting("defenceFiled").asList()
                    .containsExactly(expectedDefenceFiled);
                assertThat(eventHistory).extracting("directionsQuestionnaireFiled").asList()
                    .containsExactly(expectedDirectionsQuestionnaireFiled);
                assertThat(eventHistory).extracting("miscellaneous").asList()
                    .containsExactly(expectedMiscellaneousEvents.get(0), expectedMiscellaneousEvents.get(1),
                                     expectedMiscellaneousEvents.get(2), expectedMiscellaneousEvents.get(3)
                );

                assertEmptyEvents(
                    eventHistory,
                    "receiptOfAdmission",
                    "receiptOfPartAdmission",
                    "replyToDefence"
                );
            }
        }
    }

    @Nested
    class FullDefenceProceeds {

        @Nested
        class OneVOne {
            @Test
            void shouldPrepareExpectedEvents_whenClaimWithFullDefenceSDO() {

                String miscText = "RPA Reason: Case proceeds offline. "
                    + "Judge / Legal Advisor did not draw a Direction's Order: "
                    + "unforeseen complexities";

                CaseData caseData = CaseDataBuilder.builder()
                        .atState(TAKEN_OFFLINE_SDO_NOT_DRAWN)
                        .atStateTakenOfflineSDONotDrawn(MultiPartyScenario.ONE_V_ONE)
                        .respondentResponseIsSame(YES)
                        .respondent1DQ(Respondent1DQ.builder()
                                           .respondent1DQFileDirectionsQuestionnaire(FileDirectionsQuestionnaire.builder()
                                                                                         .oneMonthStayRequested(YES)
                                                                                         .build())
                                           .respondent1DQRequestedCourt(RequestedCourt.builder()
                                                                            .responseCourtCode("444")
                                                                            .build())
                                           .build())
                        .build();
                if (caseData.getRespondent2OrgRegistered() != null && caseData.getRespondent2Represented() == null) {
                    caseData = caseData.toBuilder()
                        .respondent2Represented(YES)
                        .build();
                }
                Event expectedDirectionsQuestionnaireRespondent = Event.builder()
                    .eventSequence(6)
                    .eventCode("197")
                    .dateReceived(caseData.getRespondent1ResponseDate())
                    .litigiousPartyID("002")
                    .eventDetailsText(mapper.prepareFullDefenceEventText(
                        caseData.getRespondent1DQ(), caseData, true, caseData.getRespondent1()))
                            .eventDetails(EventDetails.builder()
                                              .stayClaim(mapper.isStayClaim(caseData.getRespondent1DQ()))
                                              .preferredCourtCode(mapper.getPreferredCourtCode(caseData.getRespondent1DQ()))
                                              .preferredCourtName("")
                                              .build())
                        .build();
                Event expectedDirectionsQuestionnaireApplicant = Event.builder().eventSequence(7)
                        .eventCode("197")
                        .dateReceived(caseData.getApplicant1ResponseDate())
                        .litigiousPartyID("001")
                        .eventDetails(EventDetails.builder()
                                          .stayClaim(mapper.isStayClaim(caseData.getApplicant1DQ()))
                                          .preferredCourtCode(locationRefDataUtil.getPreferredCourtData(
                                                          caseData,
                                                          BEARER_TOKEN, true))
                                          .preferredCourtName("")
                                          .build())
                        .eventDetailsText(mapper.prepareEventDetailsText(
                            caseData.getApplicant1DQ(),
                                        locationRefDataUtil.getPreferredCourtData(
                                                caseData,
                                                BEARER_TOKEN, true)
                        ))
                        .build();
                List<Event> expectedMiscellaneousEvents = List.of(Event.builder().eventSequence(1).eventCode("999")
                            .dateReceived(caseData.getIssueDate().atStartOfDay())
                            .eventDetailsText("Claim issued in CCD.")
                            .eventDetails(EventDetails.builder()
                                              .miscText("Claim issued in CCD.")
                                              .build())
                            .build(), Event.builder()
                            .eventSequence(2)
                            .eventCode("999")
                            .dateReceived(LocalDate.now().plusDays(1).atStartOfDay()
                            )
                            .eventDetailsText("Claimant has notified defendant.")
                            .eventDetails(EventDetails.builder()
                                              .miscText("Claimant has notified defendant.")
                                              .build())
                            .build(),
                        Event.builder()
                            .eventSequence(3)
                            .eventCode("999")
                            .dateReceived(caseData.getClaimDetailsNotificationDate())
                            .eventDetailsText("Claim details notified.")
                            .eventDetails(EventDetails.builder()
                                              .miscText("Claim details notified.")
                                              .build())
                            .build(),
                        Event.builder()
                            .eventSequence(8)
                            .eventCode("999")
                            .dateReceived(caseData.getApplicant1ResponseDate())
                            .eventDetailsText("Claimant proceeds.")
                            .eventDetails(EventDetails.builder()
                                              .miscText("Claimant proceeds.")
                                              .build())
                            .build(),
                        Event.builder()
                            .eventSequence(9)
                            .eventCode("999")
                            .dateReceived(caseData.getUnsuitableSDODate())
                            .eventDetailsText(miscText)
                            .eventDetails(EventDetails.builder()
                                              .miscText(miscText)
                                              .build())
                            .build()
                    );

                var eventHistory = mapper.buildEvents(caseData, BEARER_TOKEN);
                assertThat(eventHistory).isNotNull();
                assertThat(eventHistory).extracting("directionsQuestionnaireFiled")
                    .asList().containsExactlyInAnyOrder(expectedDirectionsQuestionnaireRespondent,
                            expectedDirectionsQuestionnaireApplicant);
                assertThat(eventHistory).extracting("miscellaneous").asList()
                    .containsExactly(expectedMiscellaneousEvents.get(0),
                                     expectedMiscellaneousEvents.get(1),
                                     expectedMiscellaneousEvents.get(2),
                                     expectedMiscellaneousEvents.get(3), expectedMiscellaneousEvents.get(4));
                assertEmptyEvents(eventHistory, "receiptOfAdmission", "receiptOfPartAdmission");
            }

            @Test
            void shouldPrepareExpectedEvents_whenClaimWithFullDefenceTakenOfflineAfterSDO() {

                String miscText = "RPA Reason: Case Proceeds in Caseman.";

                CaseData caseData = CaseDataBuilder.builder()
                    .atState(TAKEN_OFFLINE_AFTER_SDO)
                    .atStateTakenOfflineAfterSDO(MultiPartyScenario.ONE_V_ONE)
                    .build();
                if (caseData.getRespondent2OrgRegistered() != null && caseData.getRespondent2Represented() == null) {
                    caseData = caseData.toBuilder().respondent2Represented(YES).build();
                }
                Event expectedDirectionsQuestionnaireRespondent = Event.builder()
                    .eventSequence(6)
                    .eventCode("197")
                    .dateReceived(caseData.getRespondent1ResponseDate())
                    .litigiousPartyID("002")
                    .eventDetailsText(mapper.prepareFullDefenceEventText(
                        caseData.getRespondent1DQ(), caseData, true, caseData.getRespondent1()))
                            .eventDetails(EventDetails.builder()
                                              .stayClaim(mapper.isStayClaim(caseData.getRespondent1DQ()))
                                              .preferredCourtCode(mapper.getPreferredCourtCode(caseData.getRespondent1DQ()))
                                              .preferredCourtName("")
                                              .build())
                        .build();
                Event expectedDirectionsQuestionnaireApplicant = Event.builder()
                    .eventSequence(7)
                    .eventCode("197")
                    .dateReceived(caseData.getApplicant1ResponseDate())
                    .litigiousPartyID("001")
                    .eventDetails(EventDetails.builder()
                                      .stayClaim(mapper.isStayClaim(caseData.getApplicant1DQ()))
                                      .preferredCourtCode(locationRefDataUtil.getPreferredCourtData(
                                          caseData,
                                          BEARER_TOKEN, true))
                                      .preferredCourtName("")
                                      .build())
                    .eventDetailsText(mapper.prepareEventDetailsText(
                        caseData.getApplicant1DQ(),
                        locationRefDataUtil.getPreferredCourtData(
                            caseData,
                            BEARER_TOKEN, true)
                        ))
                    .build();
                List<Event> expectedMiscellaneousEvents = List.of(
                    Event.builder()
                        .eventSequence(1)
                        .eventCode("999")
                        .dateReceived(caseData.getIssueDate().atStartOfDay())
                        .eventDetailsText("Claim issued in CCD.")
                        .eventDetails(EventDetails.builder()
                                          .miscText("Claim issued in CCD.")
                                          .build())
                        .build(),
                    Event.builder()
                        .eventSequence(2)
                        .eventCode("999")
                        .dateReceived(LocalDate.now().plusDays(1).atStartOfDay())
                        .eventDetailsText("Claimant has notified defendant.")
                        .eventDetails(EventDetails.builder()
                                              .miscText("Claimant has notified defendant.")
                                              .build())
                        .build(),
                    Event.builder()
                        .eventSequence(3)
                        .eventCode("999")
                        .dateReceived(caseData.getClaimDetailsNotificationDate())
                        .eventDetailsText("Claim details notified.")
                        .eventDetails(EventDetails.builder()
                                          .miscText("Claim details notified.")
                                          .build())
                        .build(),
                    Event.builder()
                        .eventSequence(8)
                        .eventCode("999")
                        .dateReceived(caseData.getApplicant1ResponseDate())
                        .eventDetailsText("Claimant proceeds.")
                        .eventDetails(EventDetails.builder()
                                          .miscText("Claimant proceeds.")
                                          .build())
                        .build(),
                        Event.builder()
                            .eventSequence(9)
                            .eventCode("999")
                            .dateReceived(caseData.getTakenOfflineDate())
                            .eventDetailsText(miscText)
                            .eventDetails(EventDetails.builder()
                                              .miscText(miscText)
                                              .build())
                            .build()
                    );
                var eventHistory = mapper.buildEvents(caseData, BEARER_TOKEN);
                assertThat(eventHistory).isNotNull();
                assertThat(eventHistory).extracting("directionsQuestionnaireFiled")
                    .asList().containsExactlyInAnyOrder(
                       expectedDirectionsQuestionnaireRespondent,
                       expectedDirectionsQuestionnaireApplicant);
                assertThat(eventHistory).extracting("miscellaneous").asList()
                    .containsExactly(expectedMiscellaneousEvents.get(0),
                                    expectedMiscellaneousEvents.get(1),
                                    expectedMiscellaneousEvents.get(2),
                                    expectedMiscellaneousEvents.get(3), expectedMiscellaneousEvents.get(4));
                assertEmptyEvents(eventHistory, "receiptOfAdmission", "receiptOfPartAdmission");
            }

            @Test
            void shouldPrepareExpectedEvents_whenClaimWithFullDefenceMediationSDO() {

                CaseData caseData = CaseDataBuilder.builder()
                        .atState(FULL_DEFENCE_PROCEED)
                        .atStateApplicantProceedAllMediation(MultiPartyScenario.ONE_V_ONE)
                        .build();
                if (caseData.getRespondent2OrgRegistered() != null && caseData.getRespondent2Represented() == null) {
                    caseData = caseData.toBuilder()
                       .respondent2Represented(YES)
                       .build();
                }
                Event expectedDirectionsQuestionnaireRespondent = Event.builder()
                    .eventSequence(3)
                    .eventCode("197")
                    .dateReceived(caseData.getRespondent1ResponseDate())
                    .litigiousPartyID("002")
                    .eventDetailsText(mapper.prepareFullDefenceEventText(
                       caseData.getRespondent1DQ(), caseData,
                       true, caseData.getRespondent1()))
                    .eventDetails(EventDetails.builder()
                                     .stayClaim(mapper.isStayClaim(caseData.getRespondent1DQ()))
                                     .preferredCourtCode("")
                                     .preferredCourtName("")
                                     .build())
                    .build();
                Event expectedDirectionsQuestionnaireApplicant = Event.builder()
                        .eventSequence(4)
                        .eventCode("197")
                        .dateReceived(caseData.getApplicant1ResponseDate())
                        .litigiousPartyID("001")
                        .eventDetails(EventDetails.builder()
                                          .stayClaim(mapper.isStayClaim(caseData.getApplicant1DQ()))
                                          .preferredCourtCode("")
                                          .preferredCourtName("")
                                          .build())
                        .eventDetailsText(mapper.prepareEventDetailsText(
                            caseData.getApplicant1DQ(),
                            ""
                        ))
                        .build();
                List<Event> expectedMiscellaneousEvents = List.of(
                        Event.builder()
                            .eventSequence(1)
                            .eventCode("999")
                            .dateReceived(caseData.getIssueDate().atStartOfDay())
                            .eventDetailsText("Claim issued in CCD.")
                            .eventDetails(EventDetails.builder()
                                              .miscText("Claim issued in CCD.")
                                              .build())
                            .build(),
                        Event.builder()
                            .eventSequence(5)
                            .eventCode("999")
                            .dateReceived(caseData.getApplicant1ResponseDate())
                            .eventDetailsText("RPA Reason: Claimant proceeds.")
                            .eventDetails(EventDetails.builder()
                                              .miscText("RPA Reason: Claimant proceeds.")
                                              .build())
                            .build()
                    );

                var eventHistory = mapper.buildEvents(caseData, BEARER_TOKEN);
                assertThat(eventHistory).isNotNull();
                assertThat(eventHistory).extracting("directionsQuestionnaireFiled")
                         .asList().containsExactlyInAnyOrder(
                            expectedDirectionsQuestionnaireRespondent,
                            expectedDirectionsQuestionnaireApplicant);
                assertThat(eventHistory).extracting("miscellaneous").asList()
                        .containsExactly(expectedMiscellaneousEvents.get(0),
                                         expectedMiscellaneousEvents.get(1));
            }
        }

        @Nested
        class OneVTwo {
            @Test
            void shouldPrepareExpectedEvents_whenClaimWithFullDefenceSDO() {

                String miscText = "RPA Reason: Case proceeds offline. "
                    + "Judge / Legal Advisor did not draw a Direction's Order: "
                    + "unforeseen complexities";

                CaseData caseData = CaseDataBuilder.builder()
                    .atState(TAKEN_OFFLINE_SDO_NOT_DRAWN)
                    .atStateTakenOfflineSDONotDrawn(MultiPartyScenario.ONE_V_TWO_ONE_LEGAL_REP)
                    .respondentResponseIsSame(YES)
                    .respondent1DQ(Respondent1DQ.builder()
                                       .respondent1DQFileDirectionsQuestionnaire(FileDirectionsQuestionnaire.builder()
                                                                                     .oneMonthStayRequested(YES)
                                                                                     .build())
                                       .respondent1DQRequestedCourt(RequestedCourt.builder()
                                                                        .responseCourtCode("444")
                                                                        .build())
                                       .build())
                    .build();
                if (caseData.getRespondent2OrgRegistered() != null
                    && caseData.getRespondent2Represented() == null) {
                    caseData = caseData.toBuilder()
                        .respondent2Represented(YES)
                        .build();
                }
                Event expectedDefence1 = Event.builder()
                    .eventSequence(5)
                    .eventCode("50")
                    .dateReceived(caseData.getRespondent1ResponseDate())
                    .litigiousPartyID("002")
                    .build();
                Event expectedDefence2 = Event.builder()
                    .eventSequence(7)
                    .eventCode("50")
                    .dateReceived(caseData.getRespondent2ResponseDate())
                    .litigiousPartyID("003")
                    .build();
                Event expectedRespondent1DQ = Event.builder()
                    .eventSequence(6)
                    .eventCode("197")
                    .dateReceived(caseData.getRespondent1ResponseDate())
                    .litigiousPartyID("002")
                    .eventDetailsText(mapper.prepareFullDefenceEventText(
                        caseData.getRespondent1DQ(), caseData,
                        true, caseData.getRespondent1()
                    ))
                    .eventDetails(EventDetails.builder()
                                      .stayClaim(mapper.isStayClaim(caseData.getRespondent1DQ()))
                                      .preferredCourtCode(mapper.getPreferredCourtCode(caseData.getRespondent1DQ()))
                                      .preferredCourtName("")
                                      .build())
                    .build();
                Event expectedRespondent2DQ = Event.builder()
                    .eventSequence(8)
                    .eventCode("197")
                    .dateReceived(caseData.getRespondent2ResponseDate())
                    .litigiousPartyID("003")
                    .eventDetailsText(mapper.prepareFullDefenceEventText(
                        caseData.getRespondent2DQ(), caseData,
                        true, caseData.getRespondent2()
                    ))
                    .eventDetails(EventDetails.builder()
                                      .stayClaim(mapper.isStayClaim(caseData.getRespondent2DQ()))
                                      .preferredCourtCode(mapper.getPreferredCourtCode(caseData.getRespondent2DQ()))
                                      .preferredCourtName("")
                                      .build())
                    .build();
                Event expectedApplicantDQ = Event.builder()
                    .eventSequence(9)
                    .eventCode("197")
                    .dateReceived(caseData.getApplicant1ResponseDate())
                    .litigiousPartyID("001")
                    .eventDetails(EventDetails.builder()
                                      .stayClaim(mapper.isStayClaim(caseData.getApplicant1DQ()))
                                      .preferredCourtCode(locationRefDataUtil.getPreferredCourtData(
                                          caseData,
                                          BEARER_TOKEN, true
                                      ))
                                      .preferredCourtName("")
                                      .build())
                    .eventDetailsText(mapper.prepareEventDetailsText(
                        caseData.getApplicant1DQ(),
                        locationRefDataUtil.getPreferredCourtData(
                            caseData,
                            BEARER_TOKEN, true
                        )
                    ))
                    .build();
                List<Event> expectedMiscEvents = List.of(
                    Event.builder()
                        .eventSequence(1)
                        .eventCode("999")
                        .dateReceived(caseData.getIssueDate().atStartOfDay())
                        .eventDetailsText("Claim issued in CCD.")
                        .eventDetails(EventDetails.builder()
                                          .miscText("Claim issued in CCD.")
                                          .build())
                        .build(),
                    Event.builder()
                        .eventSequence(2)
                        .eventCode("999")
                        .dateReceived(caseData.getClaimNotificationDate())
                        .eventDetailsText("Claimant has notified defendant.")
                        .eventDetails(EventDetails.builder()
                                          .miscText("Claimant has notified defendant.")
                                          .build())
                        .build(),
                    Event.builder()
                        .eventSequence(3)
                        .eventCode("999")
                        .dateReceived(caseData.getClaimDetailsNotificationDate())
                        .eventDetailsText("Claim details notified.")
                        .eventDetails(EventDetails.builder()
                                          .miscText("Claim details notified.")
                                          .build())
                        .build(),
                    Event.builder()
                        .eventSequence(10)
                        .eventCode("999")
                        .dateReceived(caseData.getApplicant1ResponseDate())
                        .eventDetailsText("Claimant proceeds.")
                        .eventDetails(EventDetails.builder()
                                          .miscText("Claimant proceeds.")
                                          .build())
                        .build(),
                    Event.builder()
                        .eventSequence(11)
                        .eventCode("999")
                        .dateReceived(caseData.getUnsuitableSDODate())
                        .eventDetailsText(miscText)
                        .eventDetails(EventDetails.builder()
                                          .miscText(miscText)
                                          .build())
                        .build()
                );
                var eventHistory = mapper.buildEvents(caseData, BEARER_TOKEN);
                assertThat(eventHistory).isNotNull();
                assertThat(eventHistory).extracting("defenceFiled").asList()
                    .containsExactly(expectedDefence1, expectedDefence2);
                assertThat(eventHistory).extracting("directionsQuestionnaireFiled")
                    .asList().containsExactlyInAnyOrder(
                        expectedRespondent1DQ,
                        expectedRespondent2DQ,
                        expectedApplicantDQ
                );
                assertThat(eventHistory).extracting("miscellaneous").asList()
                    .containsExactly(expectedMiscEvents.get(0),
                                     expectedMiscEvents.get(1),
                                     expectedMiscEvents.get(2),
                                     expectedMiscEvents.get(3), expectedMiscEvents.get(4)
                );

                assertEmptyEvents(
                    eventHistory,
                    "receiptOfAdmission",
                    "receiptOfPartAdmission",
                    "consentExtensionFilingDefence"
                );
            }

            @Test
            void shouldPrepareExpectedEvents_whenClaimWithFullDefenceMediationSDO_OneRep() {

                String expectedMiscText1 = "RPA Reason: [1 of 2 - 2020-08-01] "
                    + "Claimant has provided intention: proceed against defendant: Mr. Sole Trader";
                String expectedMiscText2 = "RPA Reason: [2 of 2 - 2020-08-01] "
                    + "Claimant has provided intention: proceed against defendant: Mr. John Rambo";

                CaseData caseData = CaseDataBuilder.builder()
                        .atState(FULL_DEFENCE_PROCEED)
                        .atStateApplicantProceedAllMediation(MultiPartyScenario.ONE_V_TWO_ONE_LEGAL_REP)
                        .respondentResponseIsSame(YES)
                        .build();
                if (caseData.getRespondent2OrgRegistered() != null
                        && caseData.getRespondent2Represented() == null) {
                    caseData = caseData.toBuilder()
                            .respondent2Represented(YES)
                            .respondent1DQ(Respondent1DQ.builder()
                                               .respondent1DQFileDirectionsQuestionnaire(FileDirectionsQuestionnaire.builder()
                                                                                             .oneMonthStayRequested(YES)
                                                                                             .build())
                                               .respondent1DQRequestedCourt(RequestedCourt.builder()
                                                                                .responseCourtCode("444")
                                                                                .build())
                                               .build())
                            .build();
                }
                Event expectedDefence1 = Event.builder()
                        .eventSequence(2)
                        .eventCode("50")
                        .dateReceived(caseData.getRespondent1ResponseDate())
                        .litigiousPartyID("002")
                        .build();
                Event expectedDefence2 = Event.builder()
                        .eventSequence(4)
                        .eventCode("50")
                        .dateReceived(caseData.getRespondent2ResponseDate())
                        .litigiousPartyID("003")
                        .build();
                Event expectedRespondent1DQ = Event.builder()
                        .eventSequence(3)
                        .eventCode("197")
                        .dateReceived(caseData.getRespondent1ResponseDate())
                        .litigiousPartyID("002")
                        .eventDetailsText(mapper.prepareFullDefenceEventText(
                            caseData.getRespondent1DQ(), caseData,
                            true, caseData.getRespondent1()))
                        .eventDetails(EventDetails.builder()
                                          .stayClaim(mapper.isStayClaim(caseData.getRespondent1DQ()))
                                          .preferredCourtCode(mapper.getPreferredCourtCode(caseData.getRespondent1DQ()))
                                          .preferredCourtName("")
                                          .build())
                        .build();
                Event expectedRespondent2DQ = Event.builder()
                        .eventSequence(5)
                        .eventCode("197")
                        .dateReceived(caseData.getRespondent2ResponseDate())
                        .litigiousPartyID("003")
                        .eventDetailsText(mapper.prepareFullDefenceEventText(
                            caseData.getRespondent2DQ(), caseData,
                            true, caseData.getRespondent2()))
                        .eventDetails(EventDetails.builder()
                                          .stayClaim(mapper.isStayClaim(caseData.getRespondent2DQ()))
                                          .preferredCourtCode(mapper.getPreferredCourtCode(caseData.getRespondent2DQ()))
                                          .preferredCourtName("")
                                          .build())
                        .build();
                Event expectedApplicantDQ = Event.builder()
                        .eventSequence(6)
                        .eventCode("197")
                        .dateReceived(caseData.getApplicant1ResponseDate())
                        .litigiousPartyID("001")
                        .eventDetails(EventDetails.builder()
                                          .stayClaim(mapper.isStayClaim(caseData.getApplicant1DQ()))
                                          .preferredCourtCode("")
                                          .preferredCourtName("")
                                          .build())
                        .eventDetailsText(mapper.prepareEventDetailsText(
                            caseData.getApplicant1DQ(),
                            ""
                        ))
                        .build();
                List<Event> expectedMiscEvents = List.of(
                        Event.builder()
                            .eventSequence(1)
                            .eventCode("999")
                            .dateReceived(caseData.getIssueDate().atStartOfDay())
                            .eventDetailsText("Claim issued in CCD.")
                            .eventDetails(EventDetails.builder()
                                              .miscText("Claim issued in CCD.")
                                              .build())
                            .build(),
                        Event.builder()
                            .eventSequence(7)
                            .eventCode("999")
                            .dateReceived(caseData.getApplicant1ResponseDate())
                            .eventDetailsText(expectedMiscText1)
                            .eventDetails(EventDetails.builder()
                                              .miscText(expectedMiscText1)
                                              .build())
                            .build(),
                        Event.builder()
                            .eventSequence(8)
                            .eventCode("999")
                            .dateReceived(caseData.getApplicant1ResponseDate())
                            .eventDetailsText(expectedMiscText2)
                            .eventDetails(EventDetails.builder()
                                              .miscText(expectedMiscText2)
                                              .build())
                            .build()

                    );
                var eventHistory = mapper.buildEvents(caseData, BEARER_TOKEN);
                assertThat(eventHistory).isNotNull();
                assertThat(eventHistory).extracting("defenceFiled").asList()
                        .containsExactly(expectedDefence1, expectedDefence2);
                assertThat(eventHistory).extracting("directionsQuestionnaireFiled")
                        .asList().containsExactlyInAnyOrder(expectedRespondent1DQ,
                                                            expectedRespondent2DQ,
                                                            expectedApplicantDQ);
                assertThat(eventHistory).extracting("miscellaneous").asList()
                        .containsExactly(expectedMiscEvents.get(0), expectedMiscEvents.get(1),
                                         expectedMiscEvents.get(2));

                assertEmptyEvents(
                        eventHistory,
                        "receiptOfAdmission",
                        "receiptOfPartAdmission",
                        "consentExtensionFilingDefence"
                );
            }

            @Test
            void shouldPrepareExpectedEvents_whenClaimWithFullDefenceMediationSDO_TwoRep() {

                String expectedMiscText1 = "RPA Reason: [1 of 2 - 2020-08-01] "
                    + "Claimant has provided intention: proceed against defendant: Mr. Sole Trader";
                String expectedMiscText2 = "RPA Reason: [2 of 2 - 2020-08-01] "
                    + "Claimant has provided intention: proceed against defendant: Mr. John Rambo";

                CaseData caseData = CaseDataBuilder.builder()
                        .atState(FULL_DEFENCE_PROCEED)
                        .atStateApplicantProceedAllMediation(MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP)
                        .respondentResponseIsSame(YES)
                        .build();
                if (caseData.getRespondent2OrgRegistered() != null && caseData.getRespondent2Represented() == null) {
                    caseData = caseData.toBuilder()
                            .respondent2Represented(YES)
                            .build();
                }
                Event expectedDefence1 = Event.builder()
                        .eventSequence(2)
                        .eventCode("50")
                        .dateReceived(caseData.getRespondent1ResponseDate())
                        .litigiousPartyID("002")
                        .build();
                Event expectedDefence2 = Event.builder()
                        .eventSequence(3)
                        .eventCode("50")
                        .dateReceived(caseData.getRespondent2ResponseDate())
                        .litigiousPartyID("003")
                        .build();
                Event expectedRespondent1DQ = Event.builder()
                        .eventSequence(4)
                        .eventCode("197")
                        .dateReceived(caseData.getRespondent1ResponseDate())
                        .litigiousPartyID("002")
                        .eventDetailsText(mapper.prepareFullDefenceEventText(
                            caseData.getRespondent1DQ(), caseData,
                            true, caseData.getRespondent1()))
                        .eventDetails(EventDetails.builder()
                                          .stayClaim(mapper.isStayClaim(caseData.getRespondent1DQ()))
                                          .preferredCourtCode(mapper.getPreferredCourtCode(caseData.getRespondent1DQ()))
                                          .preferredCourtName("")
                                          .build())
                        .build();
                Event expectedRespondent2DQ = Event.builder()
                        .eventSequence(5)
                        .eventCode("197")
                        .dateReceived(caseData.getRespondent2ResponseDate())
                        .litigiousPartyID("003")
                        .eventDetailsText(mapper.prepareFullDefenceEventText(
                            caseData.getRespondent2DQ(), caseData,
                            true, caseData.getRespondent2()))
                        .eventDetails(EventDetails.builder()
                                          .stayClaim(mapper.isStayClaim(caseData.getRespondent2DQ()))
                                          .preferredCourtCode(mapper.getPreferredCourtCode(caseData.getRespondent2DQ()))
                                          .preferredCourtName("")
                                          .build())
                        .build();
                Event expectedApplicantDQ = Event.builder()
                        .eventSequence(6)
                        .eventCode("197")
                        .dateReceived(caseData.getApplicant1ResponseDate())
                        .litigiousPartyID("001")
                        .eventDetails(EventDetails.builder()
                                          .stayClaim(mapper.isStayClaim(caseData.getApplicant1DQ()))
                                          .preferredCourtCode("")
                                          .preferredCourtName("")
                                          .build())
                        .eventDetailsText(mapper.prepareEventDetailsText(
                            caseData.getApplicant1DQ(),
                            ""
                        ))
                        .build();
                List<Event> expectedMiscEvents = List.of(
                        Event.builder()
                            .eventSequence(1)
                            .eventCode("999")
                            .dateReceived(caseData.getIssueDate().atStartOfDay())
                            .eventDetailsText("Claim issued in CCD.")
                            .eventDetails(EventDetails.builder()
                                              .miscText("Claim issued in CCD.")
                                              .build())
                            .build(),
                        Event.builder()
                            .eventSequence(7)
                            .eventCode("999")
                            .dateReceived(caseData.getApplicant1ResponseDate())
                            .eventDetailsText(expectedMiscText1)
                            .eventDetails(EventDetails.builder()
                                              .miscText(expectedMiscText1)
                                              .build())
                            .build(),
                        Event.builder()
                            .eventSequence(8)
                            .eventCode("999")
                            .dateReceived(caseData.getApplicant1ResponseDate())
                            .eventDetailsText(expectedMiscText2)
                            .eventDetails(EventDetails.builder()
                                              .miscText(expectedMiscText2)
                                              .build())
                            .build()

                    );
                var eventHistory = mapper.buildEvents(caseData, BEARER_TOKEN);
                assertThat(eventHistory).isNotNull();
                assertThat(eventHistory).extracting("defenceFiled").asList()
                        .containsExactly(expectedDefence1, expectedDefence2);
                assertThat(eventHistory).extracting("directionsQuestionnaireFiled")
                        .asList().containsExactlyInAnyOrder(expectedRespondent1DQ,
                                                            expectedRespondent2DQ,
                                                            expectedApplicantDQ);
                assertThat(eventHistory).extracting("miscellaneous").asList()
                        .containsExactly(expectedMiscEvents.get(0), expectedMiscEvents.get(1),
                                         expectedMiscEvents.get(2));

                assertEmptyEvents(
                        eventHistory,
                        "receiptOfAdmission",
                        "receiptOfPartAdmission",
                        "consentExtensionFilingDefence"
                );
            }

            @Test
            void shouldPrepareMiscellaneousEvents_whenClaimantProceedsWithOnlyFirstDefendantSDO() {

                String expectedMiscText1 = "RPA Reason: [1 of 2 - 2020-08-01] "
                    + "Claimant has provided intention: proceed against defendant: Mr. Sole Trader";
                String expectedMiscText2 = "RPA Reason: [2 of 2 - 2020-08-01] "
                    + "Claimant has provided intention: not proceed against defendant: Mr. John Rambo";

                CaseData caseData = CaseDataBuilder.builder()
                    .atState(FlowState.Main.FULL_DEFENCE_PROCEED, MultiPartyScenario.ONE_V_TWO_ONE_LEGAL_REP)
                    .atStateApplicantRespondToDefenceAndProceedVsDefendant1Only_1v2()
                    .build();
                if (caseData.getRespondent2OrgRegistered() != null
                    && caseData.getRespondent2Represented() == null) {
                    caseData = caseData.toBuilder()
                        .respondent2Represented(YES)
                        .build();
                }
                List<Event> expectedMiscEvents = List.of(
                    Event.builder()
                        .eventSequence(1)
                        .eventCode("999")
                        .dateReceived(caseData.getIssueDate().atStartOfDay())
                        .eventDetailsText("Claim issued in CCD.")
                        .eventDetails(EventDetails.builder()
                                          .miscText("Claim issued in CCD.")
                                          .build())
                        .build(),
                    Event.builder()
                        .eventSequence(2)
                        .eventCode("999")
                        .dateReceived(caseData.getClaimNotificationDate())
                        .eventDetailsText("Claimant has notified defendant.")
                        .eventDetails(EventDetails.builder()
                                          .miscText("Claimant has notified defendant.")
                                          .build())
                        .build(),
                    Event.builder()
                        .eventSequence(3)
                        .eventCode("999")
                        .dateReceived(caseData.getClaimDetailsNotificationDate())
                        .eventDetailsText("Claim details notified.")
                        .eventDetails(EventDetails.builder()
                                          .miscText("Claim details notified.")
                                          .build())
                        .build(),
                    Event.builder()
                        .eventSequence(8)
                        .eventCode("999")
                        .dateReceived(caseData.getApplicant1ResponseDate())
                        .eventDetailsText(expectedMiscText1)
                        .eventDetails(EventDetails.builder()
                                          .miscText(expectedMiscText1)
                                          .build())
                        .build(),
                    Event.builder()
                        .eventSequence(9)
                        .eventCode("999")
                        .dateReceived(caseData.getApplicant1ResponseDate())
                        .eventDetailsText(expectedMiscText2)
                        .eventDetails(EventDetails.builder()
                                          .miscText(expectedMiscText2)
                                          .build())
                        .build()
                );

                var eventHistory = mapper.buildEvents(caseData, BEARER_TOKEN);

                assertThat(eventHistory).extracting("miscellaneous").asList()
                    .containsExactly(expectedMiscEvents.get(0), expectedMiscEvents.get(1),
                                     expectedMiscEvents.get(2),
                                     expectedMiscEvents.get(3), expectedMiscEvents.get(4));
            }

            void shouldPrepareMiscellaneousEvents_whenClaimantProceedsWithOnlySecondDefendantSDO() {

                String expectedMiscText1 = "RPA Reason: [1 of 2 - 2020-08-01] "
                    + "Claimant has provided intention: not proceed against defendant: Mr. Sole Trader";
                String expectedMiscText2 = "RPA Reason: [2 of 2 - 2020-08-01] "
                    + "Claimant has provided intention: proceed against defendant: Mr. John Rambo";

                CaseData caseData = CaseDataBuilder.builder()
                    .atState(FlowState.Main.FULL_DEFENCE_PROCEED, MultiPartyScenario.ONE_V_TWO_ONE_LEGAL_REP)
                    .atStateApplicantRespondToDefenceAndProceedVsDefendant2Only_1v2()
                    .respondentResponseIsSame(YES)
                    .build();
                if (caseData.getRespondent2OrgRegistered() != null
                    && caseData.getRespondent2Represented() == null) {
                    caseData = caseData.toBuilder()
                        .respondent2Represented(YES)
                        .build();
                }
                List<Event> expectedMiscEvents = List.of(
                    Event.builder()
                        .eventSequence(3)
                        .eventCode("999")
                        .dateReceived(caseData.getClaimNotificationDate())
                        .eventDetailsText("Claimant has notified defendant.")
                        .eventDetails(EventDetails.builder()
                                          .miscText("Claimant has notified defendant.")
                                          .build())
                        .build(),
                    Event.builder()
                        .eventSequence(9)
                        .eventCode("999")
                        .dateReceived(caseData.getApplicant1ResponseDate())
                        .eventDetailsText(expectedMiscText1)
                        .eventDetails(EventDetails.builder()
                                          .miscText(expectedMiscText1)
                                          .build())
                        .build(),
                    Event.builder()
                        .eventSequence(10)
                        .eventCode("999")
                        .dateReceived(caseData.getApplicant1ResponseDate())
                        .eventDetailsText(expectedMiscText2)
                        .eventDetails(EventDetails.builder()
                                          .miscText(expectedMiscText2)
                                          .build())
                        .build()
                );

                var eventHistory = mapper.buildEvents(caseData, BEARER_TOKEN);
                assertThat(eventHistory).extracting("miscellaneous").asList()
                    .containsExactly(expectedMiscEvents.get(0), expectedMiscEvents.get(1),
                                     expectedMiscEvents.get(2)
                );
            }
        }

        @Nested
        class TwoVOne {

            @Test
            void shouldPrepareExpectedEvents_whenClaimantsProceedSDO() {

                String miscText = "RPA Reason: Case proceeds offline. "
                    + "Judge / Legal Advisor did not draw a Direction's Order: "
                    + "unforeseen complexities";
                CaseData caseData = CaseDataBuilder.builder()
                    .multiPartyClaimTwoApplicants()
                    .atState(TAKEN_OFFLINE_SDO_NOT_DRAWN)
                    .atStateTakenOfflineSDONotDrawn(MultiPartyScenario.TWO_V_ONE)
                    .build();

                Event expectedRespondentDQ = Event.builder()
                        .eventSequence(6)
                        .eventCode("197")
                        .dateReceived(caseData.getRespondent1ResponseDate())
                        .litigiousPartyID("002")
                        .eventDetailsText(mapper.prepareFullDefenceEventText(
                            caseData.getRespondent1DQ(), caseData,
                            true, caseData.getRespondent1()
                        ))
                        .eventDetails(EventDetails.builder()
                                          .stayClaim(mapper.isStayClaim(caseData.getRespondent1DQ()))
                                          .preferredCourtCode(mapper.getPreferredCourtCode(caseData.getRespondent1DQ()))
                                          .preferredCourtName("")
                                          .build())
                        .build();

                Event expectedApplicant1DQ = Event.builder()
                        .eventSequence(7)
                        .eventCode("197")
                        .dateReceived(caseData.getApplicant1ResponseDate())
                        .litigiousPartyID("001")
                        .eventDetails(EventDetails.builder()
                                          .stayClaim(mapper.isStayClaim(caseData.getApplicant1DQ()))
                                          .preferredCourtCode(locationRefDataUtil.getPreferredCourtData(
                                                  caseData,
                                                  BEARER_TOKEN, true))
                                          .preferredCourtName("")
                                          .build())
                        .eventDetailsText(mapper.prepareEventDetailsText(
                            caseData.getApplicant1DQ(),
                                locationRefDataUtil.getPreferredCourtData(
                                        caseData,
                                        BEARER_TOKEN, true)
                        ))
                        .build();
                Event expectedApplicant2DQ = Event.builder()
                        .eventSequence(8)
                        .eventCode("197")
                        .dateReceived(caseData.getApplicant2ResponseDate())
                        .litigiousPartyID("004")
                        .eventDetails(EventDetails.builder()
                                          .stayClaim(mapper.isStayClaim(caseData.getApplicant2DQ()))
                                          .preferredCourtCode(locationRefDataUtil.getPreferredCourtData(
                                                  caseData,
                                                  BEARER_TOKEN, true))
                                          .preferredCourtName("")
                                          .build())
                        .eventDetailsText(mapper.prepareEventDetailsText(
                            caseData.getApplicant2DQ(),
                                locationRefDataUtil.getPreferredCourtData(
                                        caseData,
                                        BEARER_TOKEN, true)
                        ))
                        .build();
                List<Event> expectedMiscEvents = List.of(
                        Event.builder()
                            .eventSequence(1)
                            .eventCode("999")
                            .dateReceived(caseData.getIssueDate().atStartOfDay())
                            .eventDetailsText("Claim issued in CCD.")
                            .eventDetails(EventDetails.builder()
                                              .miscText("Claim issued in CCD.")
                                              .build())
                            .build(),
                        Event.builder()
                            .eventSequence(2)
                            .eventCode("999")
                            .dateReceived(caseData.getClaimNotificationDate())
                            .eventDetailsText("Claimant has notified defendant.")
                            .eventDetails(EventDetails.builder()
                                              .miscText("Claimant has notified defendant.")
                                              .build())
                            .build(),
                        Event.builder()
                            .eventSequence(3)
                            .eventCode("999")
                            .dateReceived(caseData.getClaimDetailsNotificationDate())
                            .eventDetailsText("Claim details notified.")
                            .eventDetails(EventDetails.builder()
                                              .miscText("Claim details notified.")
                                              .build())
                            .build(),
                        Event.builder()
                            .eventSequence(9)
                            .eventCode("999")
                            .dateReceived(caseData.getApplicant1ResponseDate())
                            .eventDetailsText("Claimants proceed.")
                            .eventDetails(EventDetails.builder()
                                              .miscText("Claimants proceed.")
                                              .build())
                            .build(),
                        Event.builder()
                            .eventSequence(10)
                            .eventCode("999")
                            .dateReceived(caseData.getUnsuitableSDODate())
                            .eventDetailsText(miscText)
                            .eventDetails(EventDetails.builder()
                                              .miscText(miscText)
                                              .build())
                            .build()
                    );

                var eventHistory = mapper.buildEvents(caseData, BEARER_TOKEN);

                assertThat(eventHistory).isNotNull();
                assertThat(eventHistory).extracting("directionsQuestionnaireFiled")
                        .asList().containsExactlyInAnyOrder(expectedRespondentDQ, expectedApplicant1DQ,
                                                            expectedApplicant2DQ);
                assertThat(eventHistory).extracting("miscellaneous").asList()
                        .containsExactly(expectedMiscEvents.get(0),
                                         expectedMiscEvents.get(1),
                                         expectedMiscEvents.get(2),
                                         expectedMiscEvents.get(3),
                                         expectedMiscEvents.get(4));

                assertEmptyEvents(
                        eventHistory,
                        "receiptOfAdmission",
                        "receiptOfPartAdmission",
                        "consentExtensionFilingDefence"
                );
            }

            @Test
            void shouldPrepareExpectedEvents_whenClaimantsProceedMediationSDO() {

                String expectedMiscText1 = "RPA Reason: [1 of 2 - 2020-08-01] Claimant: "
                    + "Mr. John Rambo has provided intention: proceed";
                String expectedMiscText2 = "RPA Reason: [2 of 2 - 2020-08-01] Claimant: "
                    + "Mr. Jason Rambo has provided intention: proceed";
                CaseData caseData = CaseDataBuilder.builder()
                    .multiPartyClaimTwoApplicants()
                    .atState(FULL_DEFENCE_PROCEED)
                    .atStateApplicantProceedAllMediation(MultiPartyScenario.TWO_V_ONE)
                    .build();

                Event expectedRespondentDQ = Event.builder()
                        .eventSequence(3)
                        .eventCode("197")
                        .dateReceived(caseData.getRespondent1ResponseDate())
                        .litigiousPartyID("002")
                        .eventDetailsText(mapper.prepareFullDefenceEventText(
                            caseData.getRespondent1DQ(), caseData,
                            true, caseData.getRespondent1()
                        ))
                        .eventDetails(EventDetails.builder()
                                          .stayClaim(mapper.isStayClaim(caseData.getRespondent1DQ()))
                                          .preferredCourtCode(mapper.getPreferredCourtCode(caseData.getRespondent1DQ()))
                                          .preferredCourtName("")
                                          .build())
                        .build();

                Event expectedApplicant1DQ = Event.builder()
                        .eventSequence(4)
                        .eventCode("197")
                        .dateReceived(caseData.getApplicant1ResponseDate())
                        .litigiousPartyID("001")
                        .eventDetails(EventDetails.builder()
                                          .stayClaim(mapper.isStayClaim(caseData.getApplicant1DQ()))
                                          .preferredCourtCode("")
                                          .preferredCourtName("")
                                          .build())
                        .eventDetailsText(mapper.prepareEventDetailsText(
                            caseData.getApplicant1DQ(),
                            ""
                        ))
                        .build();
                Event expectedApplicant2DQ = Event.builder()
                        .eventSequence(5)
                        .eventCode("197")
                        .dateReceived(caseData.getApplicant2ResponseDate())
                        .litigiousPartyID("004")
                        .eventDetails(EventDetails.builder()
                                          .stayClaim(mapper.isStayClaim(caseData.getApplicant2DQ()))
                                          .preferredCourtCode("")
                                          .preferredCourtName("")
                                          .build())
                        .eventDetailsText(mapper.prepareEventDetailsText(
                            caseData.getApplicant2DQ(),
                            ""
                        ))
                        .build();
                List<Event> expectedMiscEvents = List.of(
                        Event.builder()
                            .eventSequence(1)
                            .eventCode("999")
                            .dateReceived(caseData.getIssueDate().atStartOfDay())
                            .eventDetailsText("Claim issued in CCD.")
                            .eventDetails(EventDetails.builder()
                                              .miscText("Claim issued in CCD.")
                                              .build())
                            .build(),
                        Event.builder()
                            .eventSequence(6)
                            .eventCode("999")
                            .dateReceived(caseData.getApplicant1ResponseDate())
                            .eventDetailsText(expectedMiscText1)
                            .eventDetails(EventDetails.builder()
                                              .miscText(expectedMiscText1)
                                              .build())
                            .build(),
                        Event.builder()
                            .eventSequence(7)
                            .eventCode("999")
                            .dateReceived(caseData.getApplicant2ResponseDate())
                            .eventDetailsText(expectedMiscText2)
                            .eventDetails(EventDetails.builder()
                                              .miscText(expectedMiscText2)
                                              .build())
                            .build()
                    );

                var eventHistory = mapper.buildEvents(caseData, BEARER_TOKEN);

                assertThat(eventHistory).isNotNull();
                assertThat(eventHistory).extracting("directionsQuestionnaireFiled")
                        .asList().containsExactlyInAnyOrder(expectedRespondentDQ, expectedApplicant1DQ,
                                                            expectedApplicant2DQ);
                assertThat(eventHistory).extracting("miscellaneous").asList()
                        .containsExactly(expectedMiscEvents.get(0), expectedMiscEvents.get(1),
                                         expectedMiscEvents.get(2));

                assertEmptyEvents(
                        eventHistory,
                        "receiptOfAdmission",
                        "receiptOfPartAdmission",
                        "consentExtensionFilingDefence"
                );
            }

            @Test
            void shouldPrepareExpectedEvents_whenOnlyFirstClaimantProceedsSDO() {

                String expectedMiscText1 = "RPA Reason: [1 of 2 - 2020-08-01] Claimant: "
                    + "Mr. John Rambo has provided intention: proceed";
                String expectedMiscText2 = "RPA Reason: [2 of 2 - 2020-08-01] Claimant: "
                    + "Mr. Jason Rambo has provided intention: not proceed";

                CaseData caseData = CaseDataBuilder.builder()
                    .multiPartyClaimTwoApplicants()
                    .atStateApplicant1RespondToDefenceAndProceed_2v1()
                    .build();
                Event expectedDefenceFiled = Event.builder()
                    .eventSequence(5)
                    .eventCode("50")
                    .dateReceived(caseData.getRespondent1ResponseDate())
                    .litigiousPartyID("002")
                    .build();
                Event expectedRespondentDQ = Event.builder()
                    .eventSequence(6)
                    .eventCode("197")
                    .dateReceived(caseData.getRespondent1ResponseDate())
                    .litigiousPartyID("002")
                    .eventDetailsText(mapper.prepareFullDefenceEventText(
                        caseData.getRespondent1DQ(), caseData,
                        true, caseData.getRespondent1()
                    ))
                    .eventDetails(EventDetails.builder()
                                      .stayClaim(mapper.isStayClaim(caseData.getRespondent1DQ()))
                                      .preferredCourtCode(mapper.getPreferredCourtCode(caseData.getRespondent1DQ()))
                                      .preferredCourtName("")
                                      .build())
                    .build();

                Event expectedApplicant1DQ = Event.builder()
                    .eventSequence(7)
                    .eventCode("197")
                    .dateReceived(caseData.getApplicant1ResponseDate())
                    .litigiousPartyID("001")
                    .eventDetails(EventDetails.builder()
                                      .stayClaim(mapper.isStayClaim(caseData.getApplicant1DQ()))
                                      .preferredCourtCode(locationRefDataUtil.getPreferredCourtData(
                                          caseData,
                                          BEARER_TOKEN, true
                                      ))
                                      .preferredCourtName("")
                                      .build())
                    .eventDetailsText(mapper.prepareEventDetailsText(
                        caseData.getApplicant1DQ(),
                        locationRefDataUtil.getPreferredCourtData(
                            caseData,
                            BEARER_TOKEN, true
                        )
                    ))
                    .build();

                List<Event> expectedMiscEvents = List.of(
                    Event.builder()
                        .eventSequence(1)
                        .eventCode("999")
                        .dateReceived(caseData.getIssueDate().atStartOfDay())
                        .eventDetailsText("Claim issued in CCD.")
                        .eventDetails(EventDetails.builder()
                                          .miscText("Claim issued in CCD.")
                                          .build())
                        .build(),
                    Event.builder()
                        .eventSequence(2)
                        .eventCode("999")
                        .dateReceived(caseData.getClaimNotificationDate())
                        .eventDetailsText("Claimant has notified defendant.")
                        .eventDetails(EventDetails.builder()
                                          .miscText("Claimant has notified defendant.")
                                          .build())
                        .build(),
                    Event.builder()
                        .eventSequence(3)
                        .eventCode("999")
                        .dateReceived(caseData.getClaimDetailsNotificationDate())
                        .eventDetailsText("Claim details notified.")
                        .eventDetails(EventDetails.builder()
                                          .miscText("Claim details notified.")
                                          .build())
                        .build(),
                    Event.builder()
                        .eventSequence(8)
                        .eventCode("999")
                        .dateReceived(caseData.getApplicant1ResponseDate())
                        .eventDetailsText(expectedMiscText1)
                        .eventDetails(EventDetails.builder()
                                          .miscText(expectedMiscText1)
                                          .build())
                        .build(),

                    Event.builder()
                        .eventSequence(9)
                        .eventCode("999")
                        .dateReceived(caseData.getApplicant2ResponseDate())
                        .eventDetailsText(expectedMiscText2)
                        .eventDetails(EventDetails.builder()
                                          .miscText(expectedMiscText2)
                                          .build())
                        .build()
                );

                var eventHistory = mapper.buildEvents(caseData, BEARER_TOKEN);

                assertThat(eventHistory).isNotNull();
                assertThat(eventHistory).extracting("defenceFiled").asList()
                    .containsExactly(expectedDefenceFiled);
                assertThat(eventHistory).extracting("directionsQuestionnaireFiled")
                    .asList().containsExactlyInAnyOrder(expectedRespondentDQ, expectedApplicant1DQ);

                assertThat(eventHistory).extracting("miscellaneous").asList()
                    .containsExactly(expectedMiscEvents.get(0), expectedMiscEvents.get(1),
                                     expectedMiscEvents.get(2), expectedMiscEvents.get(3),
                                     expectedMiscEvents.get(4)
                );

                assertEmptyEvents(
                    eventHistory,
                    "receiptOfAdmission",
                    "receiptOfPartAdmission",
                    "consentExtensionFilingDefence"
                );
            }

            @Test
            void shouldPrepareExpectedEvents_whenOnlySecondClaimantProceeds() {

                String expectedMiscText1 = "RPA Reason: [1 of 2 - 2020-08-01] "
                    + "Claimant: Mr. John Rambo has provided intention: not proceed";
                String expectedMiscText2 = "RPA Reason: [2 of 2 - 2020-08-01] "
                    + "Claimant: Mr. Jason Rambo has provided intention: proceed";

                CaseData caseData = CaseDataBuilder.builder()
                    .multiPartyClaimTwoApplicants()
                    .atStateApplicant2RespondToDefenceAndProceed_2v1()
                    .build();
                Event expectedDefenceFiled = Event.builder()
                    .eventSequence(3)
                    .eventCode("50")
                    .dateReceived(caseData.getRespondent1ResponseDate())
                    .litigiousPartyID("002")
                    .build();
                Event expectedRespondentDQ = Event.builder()
                    .eventSequence(4)
                    .eventCode("197")
                    .dateReceived(caseData.getRespondent1ResponseDate())
                    .litigiousPartyID("002")
                    .eventDetailsText(mapper.prepareFullDefenceEventText(
                        caseData.getRespondent1DQ(), caseData,
                        true, caseData.getRespondent1()
                    ))
                    .eventDetails(EventDetails.builder()
                                      .stayClaim(mapper.isStayClaim(caseData.getRespondent1DQ()))
                                      .preferredCourtCode(mapper.getPreferredCourtCode(caseData.getRespondent1DQ()))
                                      .preferredCourtName("")
                                      .build())
                    .build();
                Event expectedApplicant2DQ = Event.builder()
                    .eventSequence(6)
                    .eventCode("197")
                    .dateReceived(caseData.getApplicant2ResponseDate())
                    .litigiousPartyID("004")
                    .eventDetails(EventDetails.builder()
                                      .stayClaim(mapper.isStayClaim(caseData.getApplicant2DQ()))
                                      .preferredCourtCode(locationRefDataUtil.getPreferredCourtData(
                                          caseData,
                                          BEARER_TOKEN, true
                                      ))
                                      .preferredCourtName("")
                                      .build())
                    .eventDetailsText(mapper.prepareEventDetailsText(
                        caseData.getApplicant2DQ(),
                        locationRefDataUtil.getPreferredCourtData(
                            caseData,
                            BEARER_TOKEN, true
                        )
                    ))
                    .build();
                List<Event> expectedMiscEvents = List.of(
                    Event.builder()
                        .eventSequence(1)
                        .eventCode("999")
                        .dateReceived(caseData.getClaimNotificationDate())
                        .eventDetailsText("Claimant has notified defendant.")
                        .eventDetails(EventDetails.builder()
                                          .miscText("Claimant has notified defendant.")
                                          .build())
                        .build(),
                    Event.builder()
                        .eventSequence(7)
                        .eventCode("999")
                        .dateReceived(caseData.getApplicant1ResponseDate())
                        .eventDetailsText(expectedMiscText1)
                        .eventDetails(EventDetails.builder()
                                          .miscText(expectedMiscText1)
                                          .build())
                        .build(),
                    Event.builder()
                        .eventSequence(8)
                        .eventCode("999")
                        .dateReceived(caseData.getApplicant2ResponseDate())
                        .eventDetailsText(expectedMiscText2)
                        .eventDetails(EventDetails.builder()
                                          .miscText(expectedMiscText2)
                                          .build())
                        .build()
                );
            }
        }
    }

    @Nested
    class TakenOfflineByStaff {

        @Test
        void shouldPrepareExpectedEvents_whenClaimTakenOfflineAfterClaimIssued() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateTakenOfflineByStaff()
                .build();

            List<Event> expectedMiscellaneousEvents = List.of(
                Event.builder()
                    .eventSequence(1)
                    .eventCode("999")
                    .dateReceived(caseData.getIssueDate().atStartOfDay())
                    .eventDetailsText("Claim issued in CCD.")
                    .eventDetails(EventDetails.builder()
                                      .miscText("Claim issued in CCD.")
                                      .build())
                    .build(),
                Event.builder()
                    .eventSequence(2)
                    .eventCode("999")
                    .dateReceived(caseData.getTakenOfflineByStaffDate())
                    .eventDetailsText(mapper.prepareTakenOfflineEventDetails(caseData))
                    .eventDetails(EventDetails.builder()
                                      .miscText(mapper.prepareTakenOfflineEventDetails(caseData))
                                      .build())
                    .build()
            );

            var eventHistory = mapper.buildEvents(caseData, BEARER_TOKEN);

            assertThat(eventHistory).isNotNull();
            assertThat(eventHistory).extracting("miscellaneous").asList()
                .containsExactly(expectedMiscellaneousEvents.get(0), expectedMiscellaneousEvents.get(1));

            assertEmptyEvents(
                eventHistory,
                "defenceFiled",
                "defenceAndCounterClaim",
                "receiptOfPartAdmission",
                "replyToDefence",
                "directionsQuestionnaireFiled",
                "receiptOfAdmission",
                "acknowledgementOfServiceReceived",
                "consentExtensionFilingDefence"
            );
        }

        @Test
        void shouldPrepareExpectedEvents_whenClaimTakenOfflineAfterClaimIssuedSpec() {
            CaseData caseData = CaseDataBuilder.builder()
                .setClaimTypeToSpecClaim()
                .atStateTakenOfflineByStaffSpec()
                .setClaimNotificationDate()
                .build();

            List<Event> expectedMiscellaneousEvents = List.of(
                Event.builder()
                    .eventSequence(1)
                    .eventCode("999")
                    .dateReceived(caseData.getIssueDate().atStartOfDay())
                    .eventDetailsText("Claim issued in CCD.")
                    .eventDetails(EventDetails.builder()
                                      .miscText("Claim issued in CCD.")
                                      .build())
                    .build(),
                Event.builder()
                    .eventSequence(2)
                    .eventCode("999")
                    .dateReceived(caseData.getTakenOfflineByStaffDate())
                    .eventDetailsText(mapper.prepareTakenOfflineEventDetails(caseData))
                    .eventDetails(EventDetails.builder()
                                      .miscText(mapper.prepareTakenOfflineEventDetails(caseData))
                                      .build())
                    .build()
            );

            var eventHistory = mapper.buildEvents(caseData, BEARER_TOKEN);

            assertThat(eventHistory).isNotNull();
            assertThat(eventHistory).extracting("miscellaneous").asList()
                .containsExactly(expectedMiscellaneousEvents.get(0), expectedMiscellaneousEvents.get(1));

            assertEmptyEvents(
                eventHistory,
                "defenceFiled",
                "defenceAndCounterClaim",
                "receiptOfPartAdmission",
                "replyToDefence",
                "directionsQuestionnaireFiled",
                "receiptOfAdmission",
                "acknowledgementOfServiceReceived",
                "consentExtensionFilingDefence"
            );
        }

        @Test
        void shouldPrepareExpectedEvents_whenClaimTakenOfflineAfterClaimOrDetailsNotified() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateTakenOfflineByStaffAfterClaimNotified()
                .build();

            List<Event> expectedMiscellaneousEvents = List.of(
                Event.builder()
                    .eventSequence(1)
                    .eventCode("999")
                    .dateReceived(caseData.getIssueDate().atStartOfDay())
                    .eventDetailsText("Claim issued in CCD.")
                    .eventDetails(EventDetails.builder()
                                      .miscText("Claim issued in CCD.")
                                      .build())
                    .build(),
                Event.builder()
                    .eventSequence(2)
                    .eventCode("999")
                    .dateReceived(caseData.getClaimNotificationDate())
                    .eventDetailsText("Claimant has notified defendant.")
                    .eventDetails(EventDetails.builder()
                                      .miscText("Claimant has notified defendant.")
                                      .build())
                    .build(),
                Event.builder()
                    .eventSequence(3)
                    .eventCode("999")
                    .dateReceived(caseData.getTakenOfflineByStaffDate())
                    .eventDetailsText(mapper.prepareTakenOfflineEventDetails(caseData))
                    .eventDetails(EventDetails.builder()
                                      .miscText(mapper.prepareTakenOfflineEventDetails(caseData))
                                      .build())
                    .build()
            );

            var eventHistory = mapper.buildEvents(caseData, BEARER_TOKEN);

            assertThat(eventHistory).isNotNull();
            assertThat(eventHistory).extracting("miscellaneous").asList()
                .containsExactly(expectedMiscellaneousEvents.get(0), expectedMiscellaneousEvents.get(1),
                                 expectedMiscellaneousEvents.get(2)
            );

            assertEmptyEvents(
                eventHistory,
                "defenceFiled",
                "defenceAndCounterClaim",
                "receiptOfPartAdmission",
                "replyToDefence",
                "directionsQuestionnaireFiled",
                "receiptOfAdmission",
                "acknowledgementOfServiceReceived",
                "consentExtensionFilingDefence"
            );
        }

        @Test
        void shouldPrepareExpectedEvents_whenCaseTakenOfflineAfterClaimDetailsNotifiedExtension() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateTakenOfflineByStaffAfterClaimDetailsNotifiedExtension()
                .build();

            List<Event> expectedMiscellaneousEvents = List.of(
                Event.builder()
                    .eventSequence(1)
                    .eventCode("999")
                    .dateReceived(caseData.getIssueDate().atStartOfDay())
                    .eventDetailsText("Claim issued in CCD.")
                    .eventDetails(EventDetails.builder()
                                      .miscText("Claim issued in CCD.")
                                      .build())
                    .build(),
                Event.builder()
                    .eventSequence(2)
                    .eventCode("999")
                    .dateReceived(caseData.getClaimNotificationDate())
                    .eventDetailsText("Claimant has notified defendant.")
                    .eventDetails(EventDetails.builder()
                                      .miscText("Claimant has notified defendant.")
                                      .build())
                    .build(),
                Event.builder()
                    .eventSequence(3)
                    .eventCode("999")
                    .dateReceived(caseData.getClaimDetailsNotificationDate())
                    .eventDetailsText("Claim details notified.")
                    .eventDetails(EventDetails.builder()
                                      .miscText("Claim details notified.")
                                      .build())
                    .build(),
                Event.builder()
                    .eventSequence(5)
                    .eventCode("999")
                    .dateReceived(caseData.getTakenOfflineByStaffDate())
                    .eventDetailsText(mapper.prepareTakenOfflineEventDetails(caseData))
                    .eventDetails(EventDetails.builder()
                                      .miscText(mapper.prepareTakenOfflineEventDetails(caseData))
                                      .build())
                    .build()
            );
            Event expectedConsentExtensionFilingDefence = Event.builder()
                .eventSequence(4)
                .eventCode("45")
                .dateReceived(caseData.getRespondent1TimeExtensionDate())
                .litigiousPartyID("002")
                .eventDetails(EventDetails.builder()
                                  .agreedExtensionDate(caseData.getRespondentSolicitor1AgreedDeadlineExtension()
                                                           .format(ISO_DATE))
                                  .build())
                .eventDetailsText(format("agreed extension date: %s", caseData
                    .getRespondentSolicitor1AgreedDeadlineExtension()
                    .format(DateTimeFormatter.ofPattern("dd MM yyyy"))))
                .build();

            var eventHistory = mapper.buildEvents(caseData, BEARER_TOKEN);

            assertThat(eventHistory).isNotNull();
            assertThat(eventHistory).extracting("miscellaneous").asList()
                .containsExactly(expectedMiscellaneousEvents.get(0), expectedMiscellaneousEvents.get(1),
                                 expectedMiscellaneousEvents.get(2), expectedMiscellaneousEvents.get(3)
            );
            assertThat(eventHistory).extracting("consentExtensionFilingDefence").asList()
                .containsExactly(expectedConsentExtensionFilingDefence);
            assertEmptyEvents(
                eventHistory,
                "defenceFiled",
                "defenceAndCounterClaim",
                "receiptOfPartAdmission",
                "replyToDefence",
                "directionsQuestionnaireFiled",
                "receiptOfAdmission",
                "acknowledgementOfServiceReceived"
            );
        }

        @Test
        void shouldPrepareExpectedEvents_whenCaseTakenOfflineAfterNotificationAcknowledged() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateTakenOfflineByStaffAfterNotificationAcknowledged()
                .build();

            List<Event> expectedMiscellaneousEvents = List.of(
                Event.builder()
                    .eventSequence(1)
                    .eventCode("999")
                    .dateReceived(caseData.getIssueDate().atStartOfDay())
                    .eventDetailsText("Claim issued in CCD.")
                    .eventDetails(EventDetails.builder()
                                      .miscText("Claim issued in CCD.")
                                      .build())
                    .build(),
                Event.builder()
                    .eventSequence(2)
                    .eventCode("999")
                    .dateReceived(caseData.getClaimNotificationDate())
                    .eventDetailsText("Claimant has notified defendant.")
                    .eventDetails(EventDetails.builder()
                                      .miscText("Claimant has notified defendant.")
                                      .build())
                    .build(),
                Event.builder()
                    .eventSequence(3)
                    .eventCode("999")
                    .dateReceived(caseData.getClaimDetailsNotificationDate())
                    .eventDetailsText("Claim details notified.")
                    .eventDetails(EventDetails.builder()
                                      .miscText("Claim details notified.")
                                      .build())
                    .build(),
                Event.builder()
                    .eventSequence(5)
                    .eventCode("999")
                    .dateReceived(caseData.getTakenOfflineByStaffDate())
                    .eventDetailsText(mapper.prepareTakenOfflineEventDetails(caseData))
                    .eventDetails(EventDetails.builder()
                                      .miscText(mapper.prepareTakenOfflineEventDetails(caseData))
                                      .build())
                    .build()
            );
            Event expectedAcknowledgementOfServiceReceived = Event.builder()
                .eventSequence(4)
                .eventCode("38")
                .dateReceived(caseData.getRespondent1AcknowledgeNotificationDate())
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

            var eventHistory = mapper.buildEvents(caseData, BEARER_TOKEN);

            assertThat(eventHistory).isNotNull();
            assertThat(eventHistory).extracting("miscellaneous").asList()
                .containsExactly(expectedMiscellaneousEvents.get(0), expectedMiscellaneousEvents.get(1),
                                 expectedMiscellaneousEvents.get(2), expectedMiscellaneousEvents.get(3)
            );
            assertThat(eventHistory).extracting("acknowledgementOfServiceReceived").asList()
                .containsExactly(expectedAcknowledgementOfServiceReceived);
            assertEmptyEvents(
                eventHistory,
                "defenceFiled",
                "defenceAndCounterClaim",
                "receiptOfPartAdmission",
                "replyToDefence",
                "directionsQuestionnaireFiled",
                "receiptOfAdmission",
                "consentExtensionFilingDefence"
            );
        }

        @Test
        void shouldPrepareExpectedEvents_whenCaseTakenOfflineAfterNotificationAcknowledgeExtension() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateTakenOfflineByStaffAfterNotificationAcknowledgeExtension()
                .build();

            List<Event> expectedMiscellaneousEvents = List.of(
                Event.builder()
                    .eventSequence(1)
                    .eventCode("999")
                    .dateReceived(caseData.getIssueDate().atStartOfDay())
                    .eventDetailsText("Claim issued in CCD.")
                    .eventDetails(EventDetails.builder()
                                      .miscText("Claim issued in CCD.")
                                      .build())
                    .build(),
                Event.builder()
                    .eventSequence(2)
                    .eventCode("999")
                    .dateReceived(caseData.getClaimNotificationDate())
                    .eventDetailsText("Claimant has notified defendant.")
                    .eventDetails(EventDetails.builder()
                                      .miscText("Claimant has notified defendant.")
                                      .build())
                    .build(),
                Event.builder()
                    .eventSequence(3)
                    .eventCode("999")
                    .dateReceived(caseData.getClaimDetailsNotificationDate())
                    .eventDetailsText("Claim details notified.")
                    .eventDetails(EventDetails.builder()
                                      .miscText("Claim details notified.")
                                      .build())
                    .build(),
                Event.builder()
                    .eventSequence(6)
                    .eventCode("999")
                    .dateReceived(caseData.getTakenOfflineByStaffDate())
                    .eventDetailsText(mapper.prepareTakenOfflineEventDetails(caseData))
                    .eventDetails(EventDetails.builder()
                                      .miscText(mapper.prepareTakenOfflineEventDetails(caseData))
                                      .build())
                    .build()
            );
            Event expectedAcknowledgementOfServiceReceived = Event.builder()
                .eventSequence(4)
                .eventCode("38")
                .dateReceived(caseData.getRespondent1AcknowledgeNotificationDate())
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
                .eventSequence(5)
                .eventCode("45")
                .dateReceived(caseData.getRespondent1TimeExtensionDate())
                .litigiousPartyID("002")
                .eventDetails(EventDetails.builder()
                                  .agreedExtensionDate(caseData.getRespondentSolicitor1AgreedDeadlineExtension()
                                                           .format(ISO_DATE))
                                  .build())
                .eventDetailsText(format("agreed extension date: %s", caseData
                    .getRespondentSolicitor1AgreedDeadlineExtension()
                    .format(DateTimeFormatter.ofPattern("dd MM yyyy"))))
                .build();

            var eventHistory = mapper.buildEvents(caseData, BEARER_TOKEN);

            assertThat(eventHistory).isNotNull();
            assertThat(eventHistory).extracting("miscellaneous").asList()
                .containsExactly(expectedMiscellaneousEvents.get(0), expectedMiscellaneousEvents.get(1),
                                 expectedMiscellaneousEvents.get(2), expectedMiscellaneousEvents.get(3)
            );
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
                "directionsQuestionnaireFiled",
                "receiptOfAdmission"
            );
        }

        @Test
        void shouldPrepareExpectedEvents_whenCaseTakenOfflineAfterDefendantResponse() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateTakenOfflineByStaffAfterDefendantResponse()
                .build();
            Event expectedDefenceFiled = Event.builder()
                .eventSequence(5)
                .eventCode("50")
                .dateReceived(caseData.getRespondent1ResponseDate())
                .litigiousPartyID("002")
                .build();
            Event expectedDirectionsQuestionnaireRespondent = Event.builder()
                .eventSequence(6)
                .eventCode("197")
                .dateReceived(caseData.getRespondent1ResponseDate())
                .litigiousPartyID("002")
                .eventDetailsText(mapper.prepareFullDefenceEventText(
                    caseData.getRespondent1DQ(),
                    caseData, true, caseData.getRespondent1()
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
                    .dateReceived(caseData.getIssueDate().atStartOfDay())
                    .eventDetailsText("Claim issued in CCD.")
                    .eventDetails(EventDetails.builder()
                                      .miscText("Claim issued in CCD.")
                                      .build())
                    .build(),
                Event.builder()
                    .eventSequence(2)
                    .eventCode("999")
                    .dateReceived(caseData.getClaimNotificationDate())
                    .eventDetailsText("Claimant has notified defendant.")
                    .eventDetails(EventDetails.builder()
                                      .miscText("Claimant has notified defendant.")
                                      .build())
                    .build(),
                Event.builder()
                    .eventSequence(3)
                    .eventCode("999")
                    .dateReceived(caseData.getClaimDetailsNotificationDate())
                    .eventDetailsText("Claim details notified.")
                    .eventDetails(EventDetails.builder()
                                      .miscText("Claim details notified.")
                                      .build())
                    .build(),
                Event.builder()
                    .eventSequence(7)
                    .eventCode("999")
                    .dateReceived(caseData.getTakenOfflineByStaffDate())
                    .eventDetailsText(mapper.prepareTakenOfflineEventDetails(caseData))
                    .eventDetails(EventDetails.builder()
                                      .miscText(mapper.prepareTakenOfflineEventDetails(caseData))
                                      .build())
                    .build()
            );
            Event expectedAcknowledgementOfServiceReceived = Event.builder()
                .eventSequence(4)
                .eventCode("38")
                .dateReceived(caseData.getRespondent1AcknowledgeNotificationDate())
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

            var eventHistory = mapper.buildEvents(caseData, BEARER_TOKEN);

            assertThat(eventHistory).isNotNull();
            assertThat(eventHistory).extracting("defenceFiled").asList()
                .containsExactly(expectedDefenceFiled);
            assertThat(eventHistory).extracting("directionsQuestionnaireFiled")
                .asList().containsExactlyInAnyOrder(expectedDirectionsQuestionnaireRespondent);
            assertThat(eventHistory).extracting("miscellaneous").asList()
                .containsExactly(expectedMiscellaneousEvents.get(0), expectedMiscellaneousEvents.get(1),
                                 expectedMiscellaneousEvents.get(2), expectedMiscellaneousEvents.get(3)
            );
            assertThat(eventHistory).extracting("acknowledgementOfServiceReceived").asList()
                .containsExactly(expectedAcknowledgementOfServiceReceived);

            assertEmptyEvents(
                eventHistory,
                "receiptOfAdmission",
                "receiptOfPartAdmission",
                "replyToDefence",
                "consentExtensionFilingDefence"
            );
        }

        @Test
        void shouldPrepareGeneralApplicationEvents_whenGeneralApplicationApplicant1DecisionDefenseStruckOut() {
            final String eventDetailText = "APPLICATION TO Strike Out";
            CaseData caseData = CaseDataBuilder.builder()
                    .atStateTakenOfflineByStaff()
                    .getGeneralApplicationWithStrikeOut("001")
                    .getGeneralStrikeOutApplicationsDetailsWithCaseState(PROCEEDS_IN_HERITAGE.getDisplayedValue())
                    .build();
            List<Event> expectedMiscellaneousEvents = List.of(
                    Event.builder()
                            .eventSequence(1)
                            .eventCode("999")
                            .dateReceived(caseData.getIssueDate().atStartOfDay())
                            .eventDetailsText("Claim issued in CCD.")
                            .eventDetails(EventDetails.builder()
                                    .miscText("Claim issued in CCD.")
                                    .build())
                            .build(),
                    Event.builder()
                            .eventSequence(4)
                            .eventCode("999")
                            .dateReceived(caseData.getTakenOfflineByStaffDate())
                            .eventDetailsText(mapper.prepareTakenOfflineEventDetails(caseData))
                            .eventDetails(EventDetails.builder()
                                    .miscText(mapper.prepareTakenOfflineEventDetails(caseData))
                                    .build())
                            .build()
            );
            Event generalApplicationEvent = Event.builder()
                    .eventSequence(2)
                    .eventCode("136")
                    .litigiousPartyID("001")
                    .dateReceived(caseData.getGeneralApplications().get(0).getValue().getGeneralAppSubmittedDateGAspec())
                    .eventDetailsText(eventDetailText)
                    .eventDetails(EventDetails.builder()
                            .miscText(eventDetailText)
                            .build())
                    .build();
            Event defenceStruckOutJudgment = Event.builder()
                    .eventSequence(3)
                    .eventCode("57")
                    .litigiousPartyID("001")
                    .dateReceived(caseData.getGeneralApplications().get(0).getValue().getGeneralAppSubmittedDateGAspec())
                    .build();
            var eventHistory = mapper.buildEvents(caseData, BEARER_TOKEN);

            assertThat(eventHistory).isNotNull();
            assertThat(eventHistory)
                    .extracting("miscellaneous")
                    .asList()
                    .containsExactly(expectedMiscellaneousEvents.get(0), expectedMiscellaneousEvents.get(1));

            assertThat(eventHistory.getGeneralFormOfApplication()).isEqualTo(List.of(generalApplicationEvent));
            assertThat(eventHistory.getDefenceStruckOut()).isEqualTo(List.of(defenceStruckOutJudgment));
        }

        @Test
        void shouldPrepareGeneralApplicationEvents_whenGeneralApplicationApplicant2DecisionDefenseStruckOut() {
            String eventDetailText = "APPLICATION TO Strike Out";
            CaseData caseData = CaseDataBuilder.builder()
                    .atStateTakenOfflineByStaff()
                    .getGeneralApplicationWithStrikeOut("004")
                    .getGeneralStrikeOutApplicationsDetailsWithCaseState(PROCEEDS_IN_HERITAGE.getDisplayedValue())
                    .build();

            List<Event> expectedMiscellaneousEvents = List.of(
                    Event.builder()
                            .eventSequence(1)
                            .eventCode("999")
                            .dateReceived(caseData.getIssueDate().atStartOfDay())
                            .eventDetailsText("Claim issued in CCD.")
                            .eventDetails(EventDetails.builder()
                                    .miscText("Claim issued in CCD.")
                                    .build())
                            .build(),
                    Event.builder()
                            .eventSequence(4)
                            .eventCode("999")
                            .dateReceived(caseData.getTakenOfflineByStaffDate())
                            .eventDetailsText(mapper.prepareTakenOfflineEventDetails(caseData))
                            .eventDetails(EventDetails.builder()
                                    .miscText(mapper.prepareTakenOfflineEventDetails(caseData))
                                    .build())
                            .build()
            );
            Event generalApplicationEvent = Event.builder()
                    .eventSequence(2)
                    .eventCode("136")
                    .litigiousPartyID("004")
                    .dateReceived(caseData.getGeneralApplications().get(0).getValue().getGeneralAppSubmittedDateGAspec())
                    .eventDetailsText(eventDetailText)
                    .eventDetails(EventDetails.builder()
                            .miscText(eventDetailText)
                            .build())
                    .build();
            Event defenceStruckOutJudgment = Event.builder()
                    .eventSequence(3)
                    .eventCode("57")
                    .litigiousPartyID("004")
                    .dateReceived(caseData.getGeneralApplications().get(0).getValue().getGeneralAppSubmittedDateGAspec())
                    .build();
            var eventHistory = mapper.buildEvents(caseData, BEARER_TOKEN);

            assertThat(eventHistory).isNotNull();
            assertThat(eventHistory)
                    .extracting("miscellaneous")
                    .asList()
                    .containsExactly(expectedMiscellaneousEvents.get(0), expectedMiscellaneousEvents.get(1));

            assertThat(eventHistory.getGeneralFormOfApplication()).isEqualTo(List.of(generalApplicationEvent));
            assertThat(eventHistory.getDefenceStruckOut()).isEqualTo(List.of(defenceStruckOutJudgment));
        }

        @Test
        void shouldNotPrepareGeneralApplicationEvents_whenGeneralApplicationDecisionOrderMade() {

            CaseData caseData = CaseDataBuilder.builder()
                    .atStateTakenOfflineByStaff()
                    .getGeneralApplicationWithStrikeOut("001")
                    .getGeneralStrikeOutApplicationsDetailsWithCaseState("Order Made")
                    .build();

            List<Event> expectedMiscellaneousEvents = List.of(
                    Event.builder()
                            .eventSequence(1)
                            .eventCode("999")
                            .dateReceived(caseData.getIssueDate().atStartOfDay())
                            .eventDetailsText("Claim issued in CCD.")
                            .eventDetails(EventDetails.builder()
                                    .miscText("Claim issued in CCD.")
                                    .build())
                            .build(),
                    Event.builder()
                            .eventSequence(2)
                            .eventCode("999")
                            .dateReceived(caseData.getTakenOfflineByStaffDate())
                            .eventDetailsText(mapper.prepareTakenOfflineEventDetails(caseData))
                            .eventDetails(EventDetails.builder()
                                    .miscText(mapper.prepareTakenOfflineEventDetails(caseData))
                                    .build())
                            .build()
            );
            Event generalApplicationEvent = Event.builder().build();

            Event defenceStruckOutJudgment = Event.builder().build();

            var eventHistory = mapper.buildEvents(caseData, BEARER_TOKEN);

            assertThat(eventHistory).isNotNull();
            assertThat(eventHistory)
                    .extracting("miscellaneous")
                    .asList()
                    .containsExactly(expectedMiscellaneousEvents.get(0), expectedMiscellaneousEvents.get(1));

            assertThat(eventHistory.getGeneralFormOfApplication()).isEqualTo(List.of(generalApplicationEvent));
            assertThat(eventHistory.getDefenceStruckOut()).isEqualTo(List.of(defenceStruckOutJudgment));
        }
    }

    @Nested
    class ClaimDismissedPastClaimDismissedDeadline {

        @Test
        void shouldPrepareExpectedEvents_whenDeadlinePassedAfterStateClaimNotified() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDismissedPastClaimNotificationDeadline()
                .claimDismissedDeadline(LocalDateTime.now().minusDays(1))
                .build();

            String text = "RPA Reason: Claim dismissed. Claimant hasn't taken action since the claim was issued.";
            List<Event> expectedMiscellaneousEvents = List.of(
                Event.builder()
                    .eventSequence(1)
                    .eventCode("999")
                    .dateReceived(caseData.getIssueDate().atStartOfDay())
                    .eventDetailsText("Claim issued in CCD.")
                    .eventDetails(EventDetails.builder()
                                      .miscText("Claim issued in CCD.")
                                      .build())
                    .build(),
                Event.builder()
                    .eventSequence(2)
                    .eventCode("999")
                    .dateReceived(caseData.getClaimDismissedDate())
                    .eventDetailsText(text)
                    .eventDetails(EventDetails.builder()
                                      .miscText(text)
                                      .build())
                    .build()
            );

            var eventHistory = mapper.buildEvents(caseData, BEARER_TOKEN);

            assertThat(eventHistory).isNotNull();
            assertThat(eventHistory).extracting("miscellaneous").asList()
                .containsExactly(expectedMiscellaneousEvents.get(0), expectedMiscellaneousEvents.get(1));

            assertEmptyEvents(
                eventHistory,
                "defenceFiled",
                "defenceAndCounterClaim",
                "receiptOfPartAdmission",
                "replyToDefence",
                "directionsQuestionnaireFiled",
                "receiptOfAdmission",
                "acknowledgementOfServiceReceived",
                "consentExtensionFilingDefence"
            );
        }

        @Test
        void shouldPrepareExpectedEvents_whenDeadlinePassedAfterStateClaimDetailsNotified() {
            CaseData caseData = CaseDataBuilder.builder()
                .atDeadlinePassedAfterStateClaimDetailsNotified()
                .build();

            String detailsText = "RPA Reason: Claim dismissed. Claimant hasn't notified defendant of the "
                + "claim details within the allowed 2 weeks.";
            List<Event> expectedMiscellaneousEvents = List.of(
                Event.builder()
                    .eventSequence(1)
                    .eventCode("999")
                    .dateReceived(caseData.getIssueDate().atStartOfDay())
                    .eventDetailsText("Claim issued in CCD.")
                    .eventDetails(EventDetails.builder()
                                      .miscText("Claim issued in CCD.")
                                      .build())
                    .build(),
                Event.builder()
                    .eventSequence(2)
                    .eventCode("999")
                    .dateReceived(caseData.getClaimNotificationDate())
                    .eventDetailsText("Claimant has notified defendant.")
                    .eventDetails(EventDetails.builder()
                                      .miscText("Claimant has notified defendant.")
                                      .build())
                    .build(),
                Event.builder()
                    .eventSequence(3)
                    .eventCode("999")
                    .dateReceived(caseData.getClaimDismissedDate())
                    .eventDetailsText(detailsText)
                    .eventDetails(EventDetails.builder()
                                      .miscText(detailsText)
                                      .build())
                    .build()
            );

            var eventHistory = mapper.buildEvents(caseData, BEARER_TOKEN);

            assertThat(eventHistory).isNotNull();
            assertThat(eventHistory).extracting("miscellaneous").asList()
                .containsExactly(expectedMiscellaneousEvents.get(0), expectedMiscellaneousEvents.get(1),
                                 expectedMiscellaneousEvents.get(2)
            );

            assertEmptyEvents(
                eventHistory,
                "defenceFiled",
                "defenceAndCounterClaim",
                "receiptOfPartAdmission",
                "replyToDefence",
                "directionsQuestionnaireFiled",
                "receiptOfAdmission",
                "acknowledgementOfServiceReceived",
                "consentExtensionFilingDefence"
            );
        }

        @Test
        void shouldPrepareExpectedEvents_whenDeadlinePassedAfterStateClaimDetailsNotifiedExtension() {
            CaseData caseData = CaseDataBuilder.builder()
                .atDeadlinePassedAfterStateClaimDetailsNotifiedExtension()
                .build();
            if (caseData.getRespondent2OrgRegistered() != null
                && caseData.getRespondent2Represented() == null) {
                caseData = caseData.toBuilder()
                    .respondent2Represented(YES)
                    .build();
            }
            List<Event> expectedMiscellaneousEvents = List.of(
                Event.builder()
                    .eventSequence(1)
                    .eventCode("999")
                    .dateReceived(caseData.getIssueDate().atStartOfDay())
                    .eventDetailsText("Claim issued in CCD.")
                    .eventDetails(EventDetails.builder()
                                      .miscText("Claim issued in CCD.")
                                      .build())
                    .build(),
                Event.builder()
                    .eventSequence(2)
                    .eventCode("999")
                    .dateReceived(caseData.getClaimNotificationDate())
                    .eventDetailsText("Claimant has notified defendant.")
                    .eventDetails(EventDetails.builder()
                                      .miscText("Claimant has notified defendant.")
                                      .build())
                    .build(),
                Event.builder()
                    .eventSequence(3)
                    .eventCode("999")
                    .dateReceived(caseData.getClaimDetailsNotificationDate())
                    .eventDetailsText("Claim details notified.")
                    .eventDetails(EventDetails.builder()
                                      .miscText("Claim details notified.")
                                      .build())
                    .build(),
                Event.builder()
                    .eventSequence(5)
                    .eventCode("999")
                    .dateReceived(caseData.getClaimDismissedDate())
                    .eventDetailsText(mapper.prepareClaimDismissedDetails(CLAIM_DETAILS_NOTIFIED_TIME_EXTENSION))
                    .eventDetails(EventDetails.builder()
                                      .miscText(mapper.prepareClaimDismissedDetails(
                                          CLAIM_DETAILS_NOTIFIED_TIME_EXTENSION))
                                      .build())
                    .build()
            );

            Event expectedConsentExtensionFilingDefence = Event.builder()
                .eventSequence(4)
                .eventCode("45")
                .dateReceived(caseData.getRespondent1TimeExtensionDate())
                .litigiousPartyID("002")
                .eventDetails(EventDetails.builder()
                                  .agreedExtensionDate(caseData.getRespondentSolicitor1AgreedDeadlineExtension()
                                                           .format(ISO_DATE))
                                  .build())
                .eventDetailsText(format("agreed extension date: %s", caseData
                    .getRespondentSolicitor1AgreedDeadlineExtension()
                    .format(DateTimeFormatter.ofPattern("dd MM yyyy"))))
                .build();

            var eventHistory = mapper.buildEvents(caseData, BEARER_TOKEN);

            assertThat(eventHistory).isNotNull();
            assertThat(eventHistory).extracting("miscellaneous").asList()
                .containsExactly(expectedMiscellaneousEvents.get(0), expectedMiscellaneousEvents.get(1),
                                 expectedMiscellaneousEvents.get(2), expectedMiscellaneousEvents.get(3)
            );
            assertThat(eventHistory).extracting("consentExtensionFilingDefence").asList()
                .containsExactly(expectedConsentExtensionFilingDefence);

            assertEmptyEvents(
                eventHistory,
                "defenceFiled",
                "defenceAndCounterClaim",
                "receiptOfPartAdmission",
                "replyToDefence",
                "directionsQuestionnaireFiled",
                "receiptOfAdmission",
                "acknowledgementOfServiceReceived"
            );
        }

        @Test
        void shouldPrepareExpectedEvents_whenDeadlinePassedAfterStateNotificationAcknowledged() {
            CaseData caseData = CaseDataBuilder.builder()
                .atDeadlinePassedAfterStateNotificationAcknowledged()
                .reasonNotSuitableSDO(ReasonNotSuitableSDO.builder().build())
                .build();

            List<Event> expectedMiscellaneousEvents = List.of(
                Event.builder()
                    .eventSequence(1)
                    .eventCode("999")
                    .dateReceived(caseData.getIssueDate().atStartOfDay())
                    .eventDetailsText("Claim issued in CCD.")
                    .eventDetails(EventDetails.builder()
                                      .miscText("Claim issued in CCD.")
                                      .build())
                    .build(),
                Event.builder()
                    .eventSequence(2)
                    .eventCode("999")
                    .dateReceived(caseData.getClaimNotificationDate())
                    .eventDetailsText("Claimant has notified defendant.")
                    .eventDetails(EventDetails.builder()
                                      .miscText("Claimant has notified defendant.")
                                      .build())
                    .build(),
                Event.builder()
                    .eventSequence(3)
                    .eventCode("999")
                    .dateReceived(caseData.getClaimDetailsNotificationDate())
                    .eventDetailsText("Claim details notified.")
                    .eventDetails(EventDetails.builder()
                                      .miscText("Claim details notified.")
                                      .build())
                    .build(),
                Event.builder()
                    .eventSequence(5)
                    .eventCode("999")
                    .dateReceived(caseData.getClaimDismissedDate())
                    .eventDetailsText(mapper.prepareClaimDismissedDetails(NOTIFICATION_ACKNOWLEDGED))
                    .eventDetails(EventDetails.builder()
                                      .miscText(mapper.prepareClaimDismissedDetails(
                                          NOTIFICATION_ACKNOWLEDGED))
                                      .build())
                    .build()
            );

            Event expectedAcknowledgementOfServiceReceived = Event.builder()
                .eventSequence(4)
                .eventCode("38")
                .dateReceived(caseData.getRespondent1AcknowledgeNotificationDate())
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

            var eventHistory = mapper.buildEvents(caseData, BEARER_TOKEN);

            assertThat(eventHistory).isNotNull();
            assertThat(eventHistory).extracting("miscellaneous").asList()
                .containsExactly(expectedMiscellaneousEvents.get(0), expectedMiscellaneousEvents.get(1),
                                 expectedMiscellaneousEvents.get(2), expectedMiscellaneousEvents.get(3)
            );
            assertThat(eventHistory).extracting("acknowledgementOfServiceReceived").asList()
                .containsExactly(expectedAcknowledgementOfServiceReceived);

            assertEmptyEvents(
                eventHistory,
                "defenceFiled",
                "defenceAndCounterClaim",
                "receiptOfPartAdmission",
                "replyToDefence",
                "directionsQuestionnaireFiled",
                "receiptOfAdmission",
                "consentExtensionFilingDefence"
            );
        }

        @Test
        void shouldPrepareExpectedEvents_whenDeadlinePassedAfterStateNotificationAcknowledgedTimeExtension() {
            CaseData caseData = CaseDataBuilder.builder()
                .atDeadlinePassedAfterStateNotificationAcknowledgedTimeExtension()
                .build();

            List<Event> expectedMiscellaneousEvents = List.of(
                Event.builder()
                    .eventSequence(1)
                    .eventCode("999")
                    .dateReceived(caseData.getIssueDate().atStartOfDay())
                    .eventDetailsText("Claim issued in CCD.")
                    .eventDetails(EventDetails.builder()
                                      .miscText("Claim issued in CCD.")
                                      .build())
                    .build(),
                Event.builder()
                    .eventSequence(2)
                    .eventCode("999")
                    .dateReceived(caseData.getClaimNotificationDate())
                    .eventDetailsText("Claimant has notified defendant.")
                    .eventDetails(EventDetails.builder()
                                      .miscText("Claimant has notified defendant.")
                                      .build())
                    .build(),
                Event.builder()
                    .eventSequence(3)
                    .eventCode("999")
                    .dateReceived(caseData.getClaimDetailsNotificationDate())
                    .eventDetailsText("Claim details notified.")
                    .eventDetails(EventDetails.builder()
                                      .miscText("Claim details notified.")
                                      .build())
                    .build(),
                Event.builder()
                    .eventSequence(6)
                    .eventCode("999")
                    .dateReceived(caseData.getClaimDismissedDate())
                    .eventDetailsText(mapper.prepareClaimDismissedDetails(NOTIFICATION_ACKNOWLEDGED_TIME_EXTENSION))
                    .eventDetails(EventDetails.builder()
                                      .miscText(mapper.prepareClaimDismissedDetails(
                                          NOTIFICATION_ACKNOWLEDGED_TIME_EXTENSION))
                                      .build())
                    .build()
            );

            Event expectedAcknowledgementOfServiceReceived = Event.builder()
                .eventSequence(4)
                .eventCode("38")
                .dateReceived(caseData.getRespondent1AcknowledgeNotificationDate())
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
                .eventSequence(5)
                .eventCode("45")
                .dateReceived(caseData.getRespondent1TimeExtensionDate())
                .litigiousPartyID("002")
                .eventDetails(EventDetails.builder()
                                  .agreedExtensionDate(caseData.getRespondentSolicitor1AgreedDeadlineExtension()
                                                           .format(ISO_DATE))
                                  .build())
                .eventDetailsText(format("agreed extension date: %s", caseData
                    .getRespondentSolicitor1AgreedDeadlineExtension()
                    .format(DateTimeFormatter.ofPattern("dd MM yyyy"))))
                .build();

            var eventHistory = mapper.buildEvents(caseData, BEARER_TOKEN);

            assertThat(eventHistory).isNotNull();
            assertThat(eventHistory).extracting("miscellaneous").asList()
                .containsExactly(expectedMiscellaneousEvents.get(0), expectedMiscellaneousEvents.get(1),
                                 expectedMiscellaneousEvents.get(2), expectedMiscellaneousEvents.get(3)
            );
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
                "directionsQuestionnaireFiled",
                "receiptOfAdmission"
            );
        }
    }

    @Nested
    class PastApplicantResponseDeadline {

        @Test
        void shouldPrepareExpectedEvents_whenPastApplicantResponseDeadline() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateNotificationAcknowledgedRespondent1TimeExtension()
                .atState(FlowState.Main.TAKEN_OFFLINE_PAST_APPLICANT_RESPONSE_DEADLINE)
                .build();
            if (caseData.getRespondent2OrgRegistered() != null
                && caseData.getRespondent2Represented() == null) {
                caseData = caseData.toBuilder()
                    .respondent2Represented(YES)
                    .build();
            }
            Event expectedDefenceFiled = Event.builder()
                .eventSequence(4)
                .eventCode("50")
                .dateReceived(caseData.getRespondent1ResponseDate())
                .litigiousPartyID("002")
                .build();
            Event expectedDirectionsQuestionnaireFiled = Event.builder()
                .eventSequence(5)
                .eventCode("197")
                .dateReceived(caseData.getRespondent1ResponseDate())
                .litigiousPartyID("002")
                .eventDetailsText(mapper.prepareFullDefenceEventText(
                    caseData.getRespondent1DQ(),
                    caseData, true, caseData.getRespondent1()
                ))
                .eventDetails(EventDetails.builder()
                                  .stayClaim(mapper.isStayClaim(caseData.getRespondent1DQ()))
                                  .preferredCourtCode(mapper.getPreferredCourtCode(caseData.getRespondent1DQ()))
                                  .preferredCourtName("")
                                  .build())
                .build();
            String detailsText = "RPA Reason: Claim moved offline after no response from applicant past response deadline.";
            List<Event> expectedMiscellaneousEvents = List.of(
                Event.builder()
                    .eventSequence(1)
                    .eventCode("999")
                    .dateReceived(caseData.getClaimNotificationDate())
                    .eventDetailsText("Claimant has notified defendant.")
                    .eventDetails(EventDetails.builder()
                                      .miscText("Claimant has notified defendant.")
                                      .build())
                    .build(),
                Event.builder()
                    .eventSequence(6)
                    .eventCode("999")
                    .dateReceived(caseData.getTakenOfflineDate())
                    .eventDetailsText(detailsText)
                    .eventDetails(EventDetails
                                      .builder()
                                      .miscText(detailsText)
                                      .build())
                    .build()
            );
            Event expectedAcknowledgementOfServiceReceived = Event.builder()
                .eventSequence(2)
                .eventCode("38")
                .dateReceived(caseData.getRespondent1AcknowledgeNotificationDate())
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
                .dateReceived(caseData.getRespondent1TimeExtensionDate())
                .litigiousPartyID("002")
                .eventDetails(EventDetails.builder()
                                  .agreedExtensionDate(caseData.getRespondentSolicitor1AgreedDeadlineExtension()
                                                           .format(ISO_DATE))
                                  .build())
                .eventDetailsText(format("agreed extension date: %s", caseData
                    .getRespondentSolicitor1AgreedDeadlineExtension()
                    .format(DateTimeFormatter.ofPattern("dd MM yyyy"))))
                .build();

            var eventHistory = mapper.buildEvents(caseData, BEARER_TOKEN);

            // TODO Tobe done as part of RPA release
            /*
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
            */
        }
    }

    @Nested
    class NotifyClaimRpaContinuousFeed {

        @Test
        void shouldPrepareMiscellaneousEvent_whenClaimNotified() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimNotified_1v1().build();
            if (caseData.getRespondent2OrgRegistered() != null
                && caseData.getRespondent2Represented() == null) {
                caseData = caseData.toBuilder()
                    .respondent2Represented(YES)
                    .build();
            }
            Event claimIssuedEvent = Event.builder()
                .eventSequence(1)
                .eventCode("999")
                .dateReceived(caseData.getIssueDate().atStartOfDay())
                .eventDetailsText("Claim issued in CCD.")
                .eventDetails(EventDetails.builder()
                                  .miscText("Claim issued in CCD.")
                                  .build())
                .build();
            Event claimNotifiedEvent = Event.builder()
                .eventSequence(2)
                .eventCode("999")
                .dateReceived(caseData.getClaimNotificationDate())
                .eventDetailsText("Claimant has notified defendant.")
                .eventDetails(EventDetails.builder()
                                  .miscText("Claimant has notified defendant.")
                                  .build())
                .build();

            var eventHistory = mapper.buildEvents(caseData, BEARER_TOKEN);

            assertThat(eventHistory).isNotNull();
            assertThat(eventHistory)
                .extracting("miscellaneous")
                .asList()
                .containsExactly(claimIssuedEvent, claimNotifiedEvent);
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
    class RespondentLitigationFriendRpaContinuousFeed {

        @Test
        void shouldPrepareMiscellaneousEvent_whenRespondent1LitigationFriend() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimNotified_1v1()
                .addRespondent1LitigationFriend()
                .build();
            if (caseData.getRespondent2OrgRegistered() != null
                && caseData.getRespondent2Represented() == null) {
                caseData = caseData.toBuilder()
                    .respondent2Represented(YES)
                    .build();
            }
            Event claimIssuedEvent = Event.builder()
                .eventSequence(1)
                .eventCode("999")
                .dateReceived(caseData.getIssueDate().atStartOfDay())
                .eventDetailsText("Claim issued in CCD.")
                .eventDetails(EventDetails.builder()
                                  .miscText("Claim issued in CCD.")
                                  .build())
                .build();
            Event claimNotifiedEvent = Event.builder()
                .eventSequence(2)
                .eventCode("999")
                .dateReceived(caseData.getClaimNotificationDate())
                .eventDetailsText("Claimant has notified defendant.")
                .eventDetails(EventDetails.builder()
                                  .miscText("Claimant has notified defendant.")
                                  .build())
                .build();
            String miscText = "Litigation friend added for respondent: " + caseData.getRespondent1().getPartyName();
            Event respondent1LitigationFriendEvent = Event.builder()
                .eventSequence(3)
                .eventCode("999")
                .dateReceived(caseData.getRespondent1LitigationFriendDate())
                .eventDetailsText(miscText)
                .eventDetails(EventDetails.builder()
                                  .miscText(miscText)
                                  .build())
                .build();

            var eventHistory = mapper.buildEvents(caseData, BEARER_TOKEN);

            assertThat(eventHistory).isNotNull();
            assertThat(eventHistory)
                .extracting("miscellaneous")
                .asList()
                .containsExactly(claimIssuedEvent, claimNotifiedEvent, respondent1LitigationFriendEvent);
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
        void shouldPrepareMiscellaneousEvent_whenRespondent2LitigationFriend() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimNotified_1v2_andNotifyBothSolicitors()
                .addRespondent2LitigationFriend()
                .build();
            if (caseData.getRespondent2OrgRegistered() != null
                && caseData.getRespondent2Represented() == null) {
                caseData = caseData.toBuilder()
                    .respondent2Represented(YES)
                    .build();
            }
            Event claimIssuedEvent = Event.builder()
                .eventSequence(1)
                .eventCode("999")
                .dateReceived(caseData.getIssueDate().atStartOfDay())
                .eventDetailsText("Claim issued in CCD.")
                .eventDetails(EventDetails.builder()
                                  .miscText("Claim issued in CCD.")
                                  .build())
                .build();
            Event claimNotifiedEvent = Event.builder()
                .eventSequence(2)
                .eventCode("999")
                .dateReceived(caseData.getClaimNotificationDate())
                .eventDetailsText("Claimant has notified defendant.")
                .eventDetails(EventDetails.builder()
                                  .miscText("Claimant has notified defendant.")
                                  .build())
                .build();
            String miscText = "Litigation friend added for respondent: " + caseData.getRespondent2().getPartyName();
            Event respondent2LitigationFriendEvent = Event.builder()
                .eventSequence(3)
                .eventCode("999")
                .dateReceived(caseData.getRespondent2LitigationFriendDate())
                .eventDetailsText(miscText)
                .eventDetails(EventDetails.builder()
                                  .miscText(miscText)
                                  .build())
                .build();

            var eventHistory = mapper.buildEvents(caseData, BEARER_TOKEN);

            assertThat(eventHistory).isNotNull();
            assertThat(eventHistory)
                .extracting("miscellaneous")
                .asList()
                .containsExactly(claimIssuedEvent, claimNotifiedEvent, respondent2LitigationFriendEvent);
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
    class AddCaseNoteContinuousFeed {

        @Test
        void shouldPrepareMiscellaneousEvent_whenCaseNoteAdded() {
            var noteCreatedAt = LocalDateTime.now().plusDays(3);
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimNotified_1v1()
                .caseNotes(CaseNote.builder()
                               .createdOn(noteCreatedAt)
                               .createdBy("createdBy")
                               .note("my note")
                               .build())
                .build();
            if (caseData.getRespondent2OrgRegistered() != null
                && caseData.getRespondent2Represented() == null) {
                caseData = caseData.toBuilder()
                    .respondent2Represented(YES)
                    .build();
            }
            Event claimIssuedEvent = Event.builder()
                .eventSequence(1)
                .eventCode("999")
                .dateReceived(caseData.getIssueDate().atStartOfDay())
                .eventDetailsText("Claim issued in CCD.")
                .eventDetails(EventDetails.builder()
                                  .miscText("Claim issued in CCD.")
                                  .build())
                .build();
            Event claimNotifiedEvent = Event.builder()
                .eventSequence(2)
                .eventCode("999")
                .dateReceived(caseData.getClaimNotificationDate())
                .eventDetailsText("Claimant has notified defendant.")
                .eventDetails(EventDetails.builder()
                                  .miscText("Claimant has notified defendant.")
                                  .build())
                .build();
            Event caseNoteEvent = Event.builder()
                .eventSequence(3)
                .eventCode("999")
                .dateReceived(noteCreatedAt)
                .eventDetailsText("case note added: my note")
                .eventDetails(EventDetails.builder()
                                  .miscText("case note added: my note")
                                  .build())
                .build();

            var eventHistory = mapper.buildEvents(caseData, BEARER_TOKEN);

            assertThat(eventHistory).isNotNull();
            assertThat(eventHistory)
                .extracting("miscellaneous")
                .asList()
                .containsExactly(claimIssuedEvent, claimNotifiedEvent, caseNoteEvent);
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
        void shouldPrepareMiscellaneousEvent_whenCaseNoteAddedButCaseNoteSavedBecauseOfSystemGlitch() {
            var noteCreatedAt = LocalDateTime.now().plusDays(3);
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimNotified_1v1()
                .caseNotes(CaseNote.builder()
                               .createdOn(noteCreatedAt)
                               .createdBy("createdBy")
                               .build())
                .build();
            if (caseData.getRespondent2OrgRegistered() != null
                && caseData.getRespondent2Represented() == null) {
                caseData = caseData.toBuilder()
                    .respondent2Represented(YES)
                    .build();
            }
            Event claimIssuedEvent = Event.builder()
                .eventSequence(1)
                .eventCode("999")
                .dateReceived(caseData.getIssueDate().atStartOfDay())
                .eventDetailsText("Claim issued in CCD.")
                .eventDetails(EventDetails.builder()
                                  .miscText("Claim issued in CCD.")
                                  .build())
                .build();
            Event claimNotifiedEvent = Event.builder()
                .eventSequence(2)
                .eventCode("999")
                .dateReceived(caseData.getClaimNotificationDate())
                .eventDetailsText("Claimant has notified defendant.")
                .eventDetails(EventDetails.builder()
                                  .miscText("Claimant has notified defendant.")
                                  .build())
                .build();
            Event caseNoteEvent = Event.builder()
                .eventSequence(3)
                .eventCode("999")
                .dateReceived(noteCreatedAt)
                .eventDetailsText("case note added: ")
                .eventDetails(EventDetails.builder()
                                  .miscText("case note added: ")
                                  .build())
                .build();

            var eventHistory = mapper.buildEvents(caseData, BEARER_TOKEN);

            assertThat(eventHistory).isNotNull();
            assertThat(eventHistory)
                .extracting("miscellaneous")
                .asList()
                .containsExactly(claimIssuedEvent, claimNotifiedEvent, caseNoteEvent);
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
    class DemagesMultitrack {

        @Test   // Demages Multiclaim one v one
        void shouldPrepareMiscellaneousEvents_whenClaimantProceedsForMultiTrackClaim() {

            CaseData caseData = CaseDataBuilder.builder()
                .totalClaimAmount(BigDecimal.valueOf(25500.00))
                .atState(FlowState.Main.FULL_DEFENCE_PROCEED, MultiPartyScenario.ONE_V_ONE)
                .atStateApplicantRespondToDefenceAndProceed()
                .atStateClaimSubmittedMultiClaim()
                .build();

            List<Event> expectedMiscEvents = List.of(
                Event.builder()
                    .eventSequence(1)
                    .eventCode("999")
                    .dateReceived(caseData.getIssueDate().atStartOfDay())
                    .eventDetailsText("Claim issued in CCD.")
                    .eventDetails(EventDetails.builder()
                                      .miscText("Claim issued in CCD.")
                                      .build())
                    .build(),
                Event.builder()
                    .eventSequence(2)
                    .eventCode("999")
                    .dateReceived(caseData.getClaimNotificationDate())
                    .eventDetailsText("Claimant has notified defendant.")
                    .eventDetails(EventDetails.builder()
                                      .miscText("Claimant has notified defendant.")
                                      .build())
                    .build(),
                Event.builder()
                    .eventSequence(3)
                    .eventCode("999")
                    .dateReceived(caseData.getClaimDetailsNotificationDate())
                    .eventDetailsText("Claim details notified.")
                    .eventDetails(EventDetails.builder()
                                      .miscText("Claim details notified.")
                                      .build())
                    .build(),
                Event.builder()
                    .eventSequence(8)
                    .eventCode("999")
                    .dateReceived(caseData.getApplicant1ResponseDate())
                    .eventDetailsText("Claimant proceeds.")
                    .eventDetails(EventDetails.builder()
                                      .miscText("Claimant proceeds.")
                                      .build())
                    .build(),
                Event.builder()
                    .eventSequence(9)
                    .eventCode("999")
                    .dateReceived(caseData.getApplicant1ResponseDate())
                    .eventDetailsText("RPA Reason:Multitrack Unspec going offline.")
                    .eventDetails(EventDetails.builder()
                                      .miscText("RPA Reason:Multitrack Unspec going offline.")
                                      .build())
                    .build()

            );

            var eventHistory = mapper.buildEvents(caseData, BEARER_TOKEN);

            assertThat(eventHistory).extracting("miscellaneous").asList()
                .containsExactly(expectedMiscEvents.get(0),
                                 expectedMiscEvents.get(1),
                                 expectedMiscEvents.get(2),
                                 expectedMiscEvents.get(3), expectedMiscEvents.get(4));
        }

        @Test
        void shouldPrepareMiscellaneousEvents_whenClaimantProceedsWithOnlyFirstDefendantForMultiClaim() {

            CaseData caseData = CaseDataBuilder.builder()
                .atState(FlowState.Main.FULL_DEFENCE_PROCEED)
                .atStateApplicantRespondToDefenceAndProceedVsDefendant1Only_1v2()
                .atStateClaimSubmittedMultiClaim()
                .build();
            if (caseData.getRespondent2OrgRegistered() != null
                && caseData.getRespondent2Represented() == null) {
                caseData = caseData.toBuilder()
                    .respondent2Represented(YES)
                    .build();
            }
            List<Event> expectedMiscEvents = List.of(
                Event.builder()
                    .eventSequence(1)
                    .eventCode("999")
                    .dateReceived(caseData.getIssueDate().atStartOfDay())
                    .eventDetailsText("Claim issued in CCD.")
                    .eventDetails(EventDetails.builder()
                                      .miscText("Claim issued in CCD.")
                                      .build())
                    .build(),
                Event.builder()
                    .eventSequence(2)
                    .eventCode("999")
                    .dateReceived(caseData.getClaimNotificationDate())
                    .eventDetailsText("Claimant has notified defendant.")
                    .eventDetails(EventDetails.builder()
                                      .miscText("Claimant has notified defendant.")
                                      .build())
                    .build(),
                Event.builder()
                    .eventSequence(3)
                    .eventCode("999")
                    .dateReceived(caseData.getClaimDetailsNotificationDate())
                    .eventDetailsText("Claim details notified.")
                    .eventDetails(EventDetails.builder()
                                      .miscText("Claim details notified.")
                                      .build())
                    .build(),
                Event.builder()
                    .eventSequence(8)
                    .eventCode("999")
                    .dateReceived(caseData.getApplicant1ResponseDate())
                    .eventDetailsText("Claimant proceeds.")
                    .eventDetails(EventDetails.builder()
                                      .miscText("Claimant proceeds.")
                                      .build())
                    .build(),
                Event.builder()
                    .eventSequence(9)
                    .eventCode("999")
                    .dateReceived(caseData.getApplicant1ResponseDate())
                    .eventDetailsText("RPA Reason:Multitrack Unspec going offline.")
                    .eventDetails(EventDetails.builder()
                                      .miscText("RPA Reason:Multitrack Unspec going offline.")
                                      .build())
                    .build()
            );

            var eventHistory = mapper.buildEvents(caseData, BEARER_TOKEN);

            assertThat(eventHistory).extracting("miscellaneous").asList()
                .containsExactly(expectedMiscEvents.get(0), expectedMiscEvents.get(1),
                                 expectedMiscEvents.get(2),
                                 expectedMiscEvents.get(3), expectedMiscEvents.get(4));
        }

        @Test
        void shouldPrepareMiscellaneousEvents_whenClaimantProceedsWithOnlySecondDefendantForMultiTrack() {

            CaseData caseData = CaseDataBuilder.builder()
                .atState(FlowState.Main.FULL_DEFENCE_PROCEED)
                .atStateApplicantRespondToDefenceAndProceedVsDefendant2Only_1v2()
                .atStateClaimSubmittedMultiClaim()
                .build();
            List<Event> expectedMiscEvents = List.of(
                Event.builder()
                    .eventSequence(1)
                    .eventCode("999")
                    .dateReceived(caseData.getIssueDate().atStartOfDay())
                    .eventDetailsText("Claim issued in CCD.")
                    .eventDetails(EventDetails.builder()
                                      .miscText("Claim issued in CCD.")
                                      .build())
                    .build(),
                Event.builder()
                    .eventSequence(2)
                    .eventCode("999")
                    .dateReceived(caseData.getClaimNotificationDate())
                    .eventDetailsText("Claimant has notified defendant.")
                    .eventDetails(EventDetails.builder()
                                      .miscText("Claimant has notified defendant.")
                                      .build())
                    .build(),
                Event.builder()
                    .eventSequence(3)
                    .eventCode("999")
                    .dateReceived(caseData.getClaimDetailsNotificationDate())
                    .eventDetailsText("Claim details notified.")
                    .eventDetails(EventDetails.builder()
                                      .miscText("Claim details notified.")
                                      .build())
                    .build(),
                Event.builder()
                    .eventSequence(8)
                    .eventCode("999")
                    .dateReceived(caseData.getApplicant1ResponseDate())
                    .eventDetailsText("Claimant proceeds.")
                    .eventDetails(EventDetails.builder()
                                      .miscText("Claimant proceeds.")
                                      .build())
                    .build(),
                Event.builder()
                    .eventSequence(9)
                    .eventCode("999")
                    .dateReceived(caseData.getApplicant1ResponseDate())
                    .eventDetailsText("RPA Reason:Multitrack Unspec going offline.")
                    .eventDetails(EventDetails.builder()
                                      .miscText("RPA Reason:Multitrack Unspec going offline.")
                                      .build())
                    .build()
            );

            var eventHistory = mapper.buildEvents(caseData, BEARER_TOKEN);

            assertThat(eventHistory).extracting("miscellaneous").asList()
                .containsExactly(expectedMiscEvents.get(0), expectedMiscEvents.get(1),
                                 expectedMiscEvents.get(2),
                                 expectedMiscEvents.get(3), expectedMiscEvents.get(4));
        }

    }

    private void assertEmptyEvents(EventHistory eventHistory, String... eventNames) {
        Stream.of(eventNames).forEach(
            eventName -> assertThat(eventHistory).extracting(eventName).asList().containsOnly(EMPTY_EVENT));
    }

    @Test
    void specShouldPrepareMiscellaneousEvent_whenCaseNoteAdded() {
        LocalDateTime noteCreatedOn = LocalDateTime.now().plusDays(3);
        CaseData caseData = CaseDataBuilder.builder()
            .atStateClaimNotified_1v1()
            .caseNotes(CaseNote.builder()
                           .createdOn(noteCreatedOn)
                           .createdBy("createdBy")
                           .note("my note")
                           .build())
            .build().toBuilder()
            .caseAccessCategory(SPEC_CLAIM)
            .respondent1LitigationFriendCreatedDate(LocalDateTime.now())
            .build();

        Event claimIssuedEvent = Event.builder()
            .eventSequence(1)
            .eventCode("999")
            .dateReceived(caseData.getIssueDate().atStartOfDay())
            .eventDetailsText("Claim issued in CCD.")
            .eventDetails(EventDetails.builder()
                              .miscText("Claim issued in CCD.")
                              .build())
            .build();

        String miscText = "Litigation friend added for respondent: " + caseData.getRespondent1().getPartyName();
        Event respondent1LitigationFriendEvent = Event.builder()
            .eventSequence(2)
            .eventCode("999")
            .dateReceived(caseData.getRespondent1LitigationFriendCreatedDate())
            .eventDetailsText(miscText)
            .eventDetails(EventDetails.builder()
                              .miscText(miscText)
                              .build())
            .build();

        Event caseNoteEvent = Event.builder()
            .eventSequence(3)
            .eventCode("999")
            .dateReceived(noteCreatedOn)
            .eventDetailsText("case note added: my note")
            .eventDetails(EventDetails.builder()
                              .miscText("case note added: my note")
                              .build())
            .build();

        var eventHistory = mapper.buildEvents(caseData, BEARER_TOKEN);

        assertThat(eventHistory).isNotNull();
        assertThat(eventHistory)
            .extracting("miscellaneous")
            .asList()
            .containsExactly(claimIssuedEvent, respondent1LitigationFriendEvent, caseNoteEvent);
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
    void specShouldPrepareFriendEvent_whenRespondent2Friend() {
        LocalDateTime noteCreatedOn = LocalDateTime.now().plusDays(3);
        CaseData caseData = CaseDataBuilder.builder()
            .atStateClaimNotified_1v1()
            .caseNotes(CaseNote.builder()
                           .createdOn(noteCreatedOn)
                           .createdBy("createdBy")
                           .note("my note")
                           .build())
            .build().toBuilder()
            .caseAccessCategory(SPEC_CLAIM)
            .respondent2(Party.builder()
                             .type(Party.Type.COMPANY)
                             .companyName("Company Name")
                             .build())
            .respondent2LitigationFriendCreatedDate(LocalDateTime.now())
            .build();
        if (caseData.getRespondent2OrgRegistered() != null
            && caseData.getRespondent2Represented() == null) {
            caseData = caseData.toBuilder()
                .respondent2Represented(YES)
                .build();
        }

        Event claimIssuedEvent = Event.builder()
            .eventSequence(1)
            .eventCode("999")
            .dateReceived(caseData.getIssueDate().atStartOfDay())
            .eventDetailsText("Claim issued in CCD.")
            .eventDetails(EventDetails.builder()
                              .miscText("Claim issued in CCD.")
                              .build())
            .build();

        String miscText = "Litigation friend added for respondent: " + caseData.getRespondent2().getPartyName();
        Event respondent1LitigationFriendEvent = Event.builder()
            .eventSequence(2)
            .eventCode("999")
            .dateReceived(caseData.getRespondent2LitigationFriendCreatedDate())
            .eventDetailsText(miscText)
            .eventDetails(EventDetails.builder()
                              .miscText(miscText)
                              .build())
            .build();

        Event caseNoteEvent = Event.builder()
            .eventSequence(3)
            .eventCode("999")
            .dateReceived(noteCreatedOn)
            .eventDetailsText("case note added: my note")
            .eventDetails(EventDetails.builder()
                              .miscText("case note added: my note")
                              .build())
            .build();

        var eventHistory = mapper.buildEvents(caseData, BEARER_TOKEN);

        assertThat(eventHistory).isNotNull();
        assertThat(eventHistory)
            .extracting("miscellaneous")
            .asList()
            .containsExactly(claimIssuedEvent, respondent1LitigationFriendEvent, caseNoteEvent);
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

    @Nested
    class BreathingSpaceEvents {

        @Test
        void shouldPrepareExpectedEvents_whenCaseEntersBreathingSpace() {

            CaseData caseData = CaseDataBuilder.builder()
                .setClaimTypeToSpecClaim()
                .atStateApplicantRespondToDefenceAndProceed(MultiPartyScenario.ONE_V_ONE)
                .atState(FlowState.Main.CLAIM_ISSUED)
                .addEnterBreathingSpace()
                .build();

            LocalDateTime currentTime = LocalDateTime.now();

            var eventHistory = mapper.buildEvents(caseData, BEARER_TOKEN);

            assertThat(eventHistory).isNotNull();
            assertThat(eventHistory).extracting("breathingSpaceEntered").asList()
                .isNotNull();
        }

        @Test
        void shouldPrepareExpectedEvents_whenCaseLiftsBreathingSpace() {

            CaseData caseData = CaseDataBuilder.builder()
                .setClaimTypeToSpecClaim()
                .atStateApplicantRespondToDefenceAndProceed(MultiPartyScenario.ONE_V_ONE)
                .atState(FlowState.Main.CLAIM_ISSUED)
                .addEnterBreathingSpace()
                .addLiftBreathingSpace()
                .build();

            LocalDateTime currentTime = LocalDateTime.now();

            var eventHistory = mapper.buildEvents(caseData, BEARER_TOKEN);

            assertThat(eventHistory).isNotNull();
            assertThat(eventHistory).extracting("breathingSpaceLifted").asList()
                .isNotNull();
        }

        @Test
        void shouldPrepareExpectedEvents_whenCaseEntersMentalBreathingSpace() {

            CaseData caseData = CaseDataBuilder.builder()
                .setClaimTypeToSpecClaim()
                .atStateApplicantRespondToDefenceAndProceed(MultiPartyScenario.ONE_V_ONE)
                .atState(FlowState.Main.CLAIM_ISSUED)
                .addEnterMentalHealthBreathingSpace()
                .build();

            LocalDateTime currentTime = LocalDateTime.now();

            var eventHistory = mapper.buildEvents(caseData, BEARER_TOKEN);

            assertThat(eventHistory).isNotNull();
            assertThat(eventHistory).extracting("breathingSpaceMentalHealthEntered").asList()
                .isNotNull();
        }

        @Test
        void shouldPrepareExpectedEvents_whenCaseLiftsMentalHealthBreathingSpace() {

            CaseData caseData = CaseDataBuilder.builder()
                .setClaimTypeToSpecClaim()
                .atStateApplicantRespondToDefenceAndProceed(MultiPartyScenario.ONE_V_ONE)
                .atState(FlowState.Main.CLAIM_ISSUED)
                .addLiftMentalBreathingSpace()
                .build();

            LocalDateTime currentTime = LocalDateTime.now();

            var eventHistory = mapper.buildEvents(caseData, BEARER_TOKEN);

            assertThat(eventHistory).isNotNull();
            assertThat(eventHistory).extracting("breathingSpaceMentalHealthLifted").asList()
                .isNotNull();
        }

        @Test
        void shouldPrepareExpectedEvents_whenCaseEntersBreathingSpaceOptionalDataNull() {

            CaseData caseData = CaseDataBuilder.builder()
                .setClaimTypeToSpecClaim()
                .atStateApplicantRespondToDefenceAndProceed(MultiPartyScenario.ONE_V_ONE)
                .atState(FlowState.Main.CLAIM_ISSUED)
                .addEnterBreathingSpaceWithoutOptionalData()
                .build();

            LocalDateTime currentTime = LocalDateTime.now();

            var eventHistory = mapper.buildEvents(caseData, BEARER_TOKEN);

            assertThat(eventHistory).isNotNull();
            assertThat(eventHistory).extracting("breathingSpaceEntered").asList()
                .isNotNull();
        }

        @Test
        void shouldPrepareExpectedEvents_whenCaseLiftsBreathingSpaceWithoutOptionalData() {

            CaseData caseData = CaseDataBuilder.builder()
                .setClaimTypeToSpecClaim()
                .atStateApplicantRespondToDefenceAndProceed(MultiPartyScenario.ONE_V_ONE)
                .atState(FlowState.Main.CLAIM_ISSUED)
                .addEnterBreathingSpace()
                .addLiftBreathingSpaceWithoutOptionalData()
                .build();

            LocalDateTime currentTime = LocalDateTime.now();

            var eventHistory = mapper.buildEvents(caseData, BEARER_TOKEN);

            assertThat(eventHistory).isNotNull();
            assertThat(eventHistory).extracting("breathingSpaceLifted").asList()
                .isNotNull();
        }

        @Test
        void shouldPrepareExpectedEvents_whenCaseEntersMentalBreathingSpaceWithoutOptionalData() {

            CaseData caseData = CaseDataBuilder.builder()
                .setClaimTypeToSpecClaim()
                .atStateApplicantRespondToDefenceAndProceed(MultiPartyScenario.ONE_V_ONE)
                .atState(FlowState.Main.CLAIM_ISSUED)
                .addEnterMentalHealthBreathingSpaceNoOptionalData()
                .build();

            LocalDateTime currentTime = LocalDateTime.now();

            var eventHistory = mapper.buildEvents(caseData, BEARER_TOKEN);

            assertThat(eventHistory).isNotNull();
            assertThat(eventHistory).extracting("breathingSpaceMentalHealthEntered").asList()
                .isNotNull();
        }

        @Test
        void shouldPrepareExpectedEvents_whenCaseLiftsMentalHealthBreathingSpaceWithoutOptionalData() {

            CaseData caseData = CaseDataBuilder.builder()
                .setClaimTypeToSpecClaim()
                .atStateApplicantRespondToDefenceAndProceed(MultiPartyScenario.ONE_V_ONE)
                .atState(FlowState.Main.CLAIM_ISSUED)
                .addLiftMentalBreathingSpace()
                .build();

            LocalDateTime currentTime = LocalDateTime.now();

            var eventHistory = mapper.buildEvents(caseData, BEARER_TOKEN);

            assertThat(eventHistory).isNotNull();
            assertThat(eventHistory).extracting("breathingSpaceMentalHealthLifted").asList()
                .isNotNull();
        }

        @Test
        void shouldPrepareExpectedEvents_whenCaseEntersBreathingSpaceWithOnlyReferenceInfo() {

            CaseData caseData = CaseDataBuilder.builder()
                .setClaimTypeToSpecClaim()
                .atStateApplicantRespondToDefenceAndProceed(MultiPartyScenario.ONE_V_ONE)
                .atState(FlowState.Main.CLAIM_ISSUED)
                .addEnterBreathingSpaceWithOnlyReferenceInfo()
                .build();

            LocalDateTime currentTime = LocalDateTime.now();

            var eventHistory = mapper.buildEvents(caseData, BEARER_TOKEN);

            assertThat(eventHistory).isNotNull();
            assertThat(eventHistory).extracting("breathingSpaceEntered").asList()
                .isNotNull();
        }

    }

    @Nested
    class InformAgreedExtensionDateForSpecEvents {

        @Test
        void shouldPrepareExpectedEvents_whenCaseInformAgreedExtensionDate() {
            LocalDate extensionDateRespondent1 = now().plusDays(14);
            LocalDateTime datetime = LocalDateTime.now();

            CaseData caseData = CaseDataBuilder.builder()
                .setClaimTypeToSpecClaim()
                .atStateApplicantRespondToDefenceAndProceed(MultiPartyScenario.ONE_V_ONE)
                .atState(FlowState.Main.CLAIM_ISSUED)
                .respondentSolicitor1AgreedDeadlineExtension(extensionDateRespondent1)
                .respondent1TimeExtensionDate(datetime)
                .build();

            LocalDateTime currentTime = LocalDateTime.now();

            var eventHistory = mapper.buildEvents(caseData, BEARER_TOKEN);

            assertThat(eventHistory).isNotNull();
            assertThat(eventHistory).extracting("consentExtensionFilingDefence").asList()
                .extracting("eventCode").asString().contains("45");
        }
    }

    @Nested
    class InterlocutoryJudgment {

        @Test
        public void shouldgenerateRPAfeedfor_IJNoDivergent() {

            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .ccdState(CaseState.JUDICIAL_REFERRAL)
                .respondent2(PartyBuilder.builder().individual().build())
                .addRespondent2(YES)
                .respondent2SameLegalRepresentative(YES)
                .hearingSupportRequirementsDJ(HearingSupportRequirementsDJ.builder().build())
                .respondent1ResponseDeadline(LocalDateTime.now().minusDays(15))
                .defendantDetails(DynamicList.builder()
                                      .value(DynamicListElement.builder()
                                                 .label("Both")
                                                 .build())
                                      .build())
                .build();
            var eventHistory = mapper.buildEvents(caseData, BEARER_TOKEN);
            assertThat(eventHistory).extracting("interlocutoryJudgment").asList()
                .extracting("eventCode").asString().contains("[252, 252]");
        }

        @Test
        public void shouldgenerateRPAfeedfor_IJWithDivergent() {

            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .ccdState(CaseState.JUDICIAL_REFERRAL)
                .respondent2(PartyBuilder.builder().individual().build())
                .addRespondent2(YES)
                .respondent2SameLegalRepresentative(YES)
                .hearingSupportRequirementsDJ(HearingSupportRequirementsDJ.builder().build())
                .respondent1ResponseDeadline(LocalDateTime.now().minusDays(15))
                .defendantDetails(DynamicList.builder()
                                      .value(DynamicListElement.builder()
                                                 .label("Test User")
                                                 .build())
                                      .build())
                .build();
            var eventHistory = mapper.buildEvents(caseData, BEARER_TOKEN);
            assertThat(eventHistory).extracting("miscellaneous").asList()
                .extracting("eventCode").asString().contains("999");
        }

    }

    @Nested
    class DefaultJudgment {

        @Test
        public void shouldgenerateRPAfeedfor_DJNoDivergent() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateNotificationAcknowledged().build().toBuilder()
                .ccdState(CaseState.JUDICIAL_REFERRAL)
                .totalClaimAmount(new BigDecimal(1000))
                .repaymentSuggestion("100")
                .repaymentFrequency(RepaymentFrequencyDJ.ONCE_ONE_MONTH)
                .respondent2(PartyBuilder.builder().individual().build())
                .addRespondent2(YES)
                .paymentTypeSelection(DJPaymentTypeSelection.REPAYMENT_PLAN)
                .repaymentSummaryObject(
                    "The judgment will order dsfsdf ffdg to pay £1072.00, "
                        + "including the claim fee and interest,"
                        + " if applicable, as shown:\n### Claim amount \n"
                        + " £1000.00\n ### Fixed cost amount"
                        + " \n£102.00\n### Claim fee amount \n £70.00\n ## "
                        + "Subtotal \n £1172.00\n\n ### Amount"
                        + " already paid \n£100.00\n ## Total still owed \n £1072.00")
                .respondent2SameLegalRepresentative(YES)
                .hearingSupportRequirementsDJ(HearingSupportRequirementsDJ.builder().build())
                .respondent1ResponseDeadline(LocalDateTime.now().minusDays(15))
                .defendantDetailsSpec(DynamicList.builder()
                                          .value(DynamicListElement.builder()
                                                     .label("Both")
                                                     .build())
                                          .build())
                .build();
            var eventHistory = mapper.buildEvents(caseData, BEARER_TOKEN);
            assertThat(eventHistory).extracting("defaultJudgment").asList()
                .extracting("eventCode").asString().contains("[230, 230]");
        }

        @Test
        public void shouldgenerateRPAfeedfor_DJWithDivergent() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateNotificationAcknowledged().build().toBuilder()
                .ccdState(CaseState.JUDICIAL_REFERRAL)
                .totalClaimAmount(new BigDecimal(1000))
                .respondent2(PartyBuilder.builder().individual().build())
                .addRespondent2(YES)
                .paymentTypeSelection(DJPaymentTypeSelection.REPAYMENT_PLAN)
                .repaymentSummaryObject(
                    "The judgment will order dsfsdf ffdg to pay £1072.00, "
                        + "including the claim fee and interest,"
                        + " if applicable, as shown:\n### Claim amount \n"
                        + " £1000.00\n ### Fixed cost amount"
                        + " \n£102.00\n### Claim fee amount \n £70.00\n ## "
                        + "Subtotal \n £1172.00\n\n ### Amount"
                        + " already paid \n£100.00\n ## Total still owed \n £1072.00")
                .respondent2SameLegalRepresentative(YES)
                .hearingSupportRequirementsDJ(HearingSupportRequirementsDJ.builder().build())
                .respondent1ResponseDeadline(LocalDateTime.now().minusDays(15))
                .repaymentSuggestion("100")
                .repaymentFrequency(RepaymentFrequencyDJ.ONCE_ONE_MONTH)
                .defendantDetailsSpec(DynamicList.builder()
                                          .value(DynamicListElement.builder()
                                                     .label("Test User")
                                                     .build())
                                          .build())
                .build();
            var eventHistory = mapper.buildEvents(caseData, BEARER_TOKEN);
            assertThat(eventHistory).extracting("miscellaneous").asList()
                .extracting("eventCode").asString().contains("999");

        }

        @Test
        public void shouldgenerateRPAfeedfor_DJNoDivergent_case_online_999_event() {

            given(featureToggleService.isJOLiveFeedActive()).willReturn(true);
            CaseData caseData = CaseDataBuilder.builder()
                .atStateNotificationAcknowledged().build().toBuilder()
                .ccdState(CaseState.All_FINAL_ORDERS_ISSUED)
                .totalClaimAmount(new BigDecimal(1000))
                .repaymentSuggestion("100")
                .repaymentFrequency(RepaymentFrequencyDJ.ONCE_ONE_MONTH)
                .respondent2(PartyBuilder.builder().individual().build())
                .addRespondent2(YES)
                .paymentTypeSelection(DJPaymentTypeSelection.REPAYMENT_PLAN)
                .repaymentSummaryObject(
                    "The judgment will order dsfsdf ffdg to pay £1072.00, "
                        + "including the claim fee and interest,"
                        + " if applicable, as shown:\n### Claim amount \n"
                        + " £1000.00\n ### Fixed cost amount"
                        + " \n£102.00\n### Claim fee amount \n £70.00\n ## "
                        + "Subtotal \n £1172.00\n\n ### Amount"
                        + " already paid \n£100.00\n ## Total still owed \n £1072.00")
                .respondent2SameLegalRepresentative(YES)
                .hearingSupportRequirementsDJ(HearingSupportRequirementsDJ.builder().build())
                .respondent1ResponseDeadline(LocalDateTime.now().minusDays(15))
                .defendantDetailsSpec(DynamicList.builder()
                                          .value(DynamicListElement.builder()
                                                     .label("Both")
                                                     .build())
                                          .build())
                .build();
            var eventHistory = mapper.buildEvents(caseData, BEARER_TOKEN);
            assertThat(eventHistory).extracting("defaultJudgment").asList()
                .extracting("eventCode").asString().contains("[230, 230]");

            assertThat(eventHistory).extracting("miscellaneous").asList()
                .extracting("eventCode").asString().contains("999");
            assertThat(eventHistory).extracting("miscellaneous").asList()
                .extracting("eventDetailsText").asString().contains("Judgment recorded");
        }

        @Test
        public void shouldgenerateRPAfeedfor_DJNoDivergent_case_offline_999_event() {

            given(featureToggleService.isJOLiveFeedActive()).willReturn(true);
            CaseData caseData = CaseDataBuilder.builder()
                .atStateNotificationAcknowledged().build().toBuilder()
                .ccdState(CaseState.PROCEEDS_IN_HERITAGE_SYSTEM)
                .totalClaimAmount(new BigDecimal(1000))
                .repaymentSuggestion("100")
                .repaymentFrequency(RepaymentFrequencyDJ.ONCE_ONE_MONTH)
                .respondent2(PartyBuilder.builder().individual().build())
                .addRespondent2(YES)
                .paymentTypeSelection(DJPaymentTypeSelection.REPAYMENT_PLAN)
                .repaymentSummaryObject(
                    "The judgment will order dsfsdf ffdg to pay £1072.00, "
                        + "including the claim fee and interest,"
                        + " if applicable, as shown:\n### Claim amount \n"
                        + " £1000.00\n ### Fixed cost amount"
                        + " \n£102.00\n### Claim fee amount \n £70.00\n ## "
                        + "Subtotal \n £1172.00\n\n ### Amount"
                        + " already paid \n£100.00\n ## Total still owed \n £1072.00")
                .respondent2SameLegalRepresentative(YES)
                .hearingSupportRequirementsDJ(HearingSupportRequirementsDJ.builder().build())
                .respondent1ResponseDeadline(LocalDateTime.now().minusDays(15))
                .defendantDetailsSpec(DynamicList.builder()
                                          .value(DynamicListElement.builder()
                                                     .label("Both")
                                                     .build())
                                          .build())
                .build();
            var eventHistory = mapper.buildEvents(caseData, BEARER_TOKEN);
            assertThat(eventHistory).extracting("defaultJudgment").asList()
                .extracting("eventCode").asString().contains("[230, 230]");

            assertThat(eventHistory).extracting("miscellaneous").asList()
                .extracting("eventCode").asString().contains("999");
            assertThat(eventHistory).extracting("miscellaneous").asList()
                .extracting("eventDetailsText").asString().isNotEmpty();
        }

    }

    @Nested
    class SetAsideJudgment {

        @Test
        public void shouldGenerateRPAFeedfor_SetAside() {
            CaseData caseData = CaseDataBuilder.builder().buildJudmentOnlineCaseDataWithPaymentByInstalment();
            caseData.setJoSetAsideReason(JudgmentSetAsideReason.JUDGE_ORDER);
            caseData.setJoSetAsideOrderType(JudgmentSetAsideOrderType.ORDER_AFTER_APPLICATION);
            caseData.setJoSetAsideOrderDate(LocalDate.of(2022, 12, 12));
            caseData.setJoSetAsideApplicationDate(LocalDate.of(2022, 11, 11));
            caseData.setJoSetAsideCreatedDate(LocalDateTime.of(2022, 11, 11, 10, 10));
            caseData.setActiveJudgment(JudgmentDetails.builder().state(JudgmentState.SET_ASIDE).build());
            when(featureToggleService.isJOLiveFeedActive()).thenReturn(true);
            var eventHistory = mapper.buildEvents(caseData, BEARER_TOKEN);
            assertThat(eventHistory).extracting("setAsideJudgment").asList()
                .extracting("eventCode").asString().contains("[170]");
            assertThat(eventHistory).extracting("setAsideJudgment").asList()
                .extracting("eventDetails").asList()
                .extracting("applicant").contains("PARTY AGAINST");
            assertThat(eventHistory).extracting("setAsideJudgment").asList()
                .extracting("eventDetails").asList()
                .extracting("result").contains("GRANTED");
            assertThat(eventHistory).extracting("setAsideJudgment").asList()
                .extracting("eventDetails").asList()
                .extracting("resultDate").asString().contains("2022-12-12");
            assertThat(eventHistory).extracting("setAsideJudgment").asList()
                .extracting("eventDetails").asList()
                .extracting("applicationDate").asString().contains("2022-11-11");
        }

        @Test
        public void shouldGenerateRPAFeedfor_SetAsideDefence() {
            CaseData caseData = CaseDataBuilder.builder().buildJudmentOnlineCaseDataWithPaymentByInstalment();
            caseData.setJoSetAsideReason(JudgmentSetAsideReason.JUDGE_ORDER);
            caseData.setJoSetAsideOrderType(JudgmentSetAsideOrderType.ORDER_AFTER_DEFENCE);
            caseData.setJoSetAsideOrderDate(LocalDate.of(2022, 12, 12));
            caseData.setJoSetAsideDefenceReceivedDate(LocalDate.of(2022, 11, 11));
            caseData.setJoSetAsideCreatedDate(LocalDateTime.of(2022, 11, 11, 10, 10));
            caseData.setActiveJudgment(JudgmentDetails.builder().state(JudgmentState.SET_ASIDE).build());
            when(featureToggleService.isJOLiveFeedActive()).thenReturn(true);
            var eventHistory = mapper.buildEvents(caseData, BEARER_TOKEN);
            assertThat(eventHistory).extracting("setAsideJudgment").asList()
                .extracting("eventCode").asString().contains("[170]");
            assertThat(eventHistory).extracting("setAsideJudgment").asList()
                .extracting("eventDetails").asList()
                .extracting("applicant").contains("PARTY AGAINST");
            assertThat(eventHistory).extracting("setAsideJudgment").asList()
                .extracting("eventDetails").asList()
                .extracting("result").contains("GRANTED");
            assertThat(eventHistory).extracting("setAsideJudgment").asList()
                .extracting("eventDetails").asList()
                .extracting("resultDate").asString().contains("2022-12-12");
            assertThat(eventHistory).extracting("setAsideJudgment").asList()
                .extracting("eventDetails").asList()
                .extracting("applicationDate").asString().contains("2022-11-11");
        }

        @Test
        public void shouldGenerateRPAFeedfor_SetAside_Error() {
            CaseData caseData = CaseDataBuilder.builder().buildJudmentOnlineCaseDataWithPaymentByInstalment();
            caseData.setJoSetAsideReason(JudgmentSetAsideReason.JUDGMENT_ERROR);
            caseData.setJoSetAsideCreatedDate(LocalDateTime.of(2022, 11, 11, 10, 10));
            caseData.setActiveJudgment(JudgmentDetails.builder().state(JudgmentState.SET_ASIDE).build());
            when(featureToggleService.isJOLiveFeedActive()).thenReturn(true);
            var eventHistory = mapper.buildEvents(caseData, BEARER_TOKEN);
            assertThat(eventHistory).extracting("setAsideJudgment").asList()
                .extracting("eventCode").asString().contains("[170]");
            assertThat(eventHistory).extracting("setAsideJudgment").asList()
                .extracting("eventDetails").asList()
                .extracting("applicant").contains("PROPER OFFICER");
            assertThat(eventHistory).extracting("setAsideJudgment").asList()
                .extracting("eventDetails").asList()
                .extracting("result").contains("GRANTED");
            assertThat(eventHistory).extracting("setAsideJudgment").asList()
                .extracting("eventDetails").asList()
                .extracting("resultDate").asString().contains("null");
            assertThat(eventHistory).extracting("setAsideJudgment").asList()
                .extracting("eventDetails").asList()
                .extracting("applicationDate").asString().contains("null");
        }

        @Test
        public void shouldGenerateRPAfeedfor_SetAside_1v2() {
            CaseData caseData = CaseDataBuilder.builder().buildJudgmentOnlineCaseDataWithPaymentByDate_Multi_party();
            caseData.setJoSetAsideReason(JudgmentSetAsideReason.JUDGE_ORDER);
            caseData.setJoSetAsideOrderType(JudgmentSetAsideOrderType.ORDER_AFTER_APPLICATION);
            caseData.setJoSetAsideOrderDate(LocalDate.of(2022, 12, 12));
            caseData.setJoSetAsideApplicationDate(LocalDate.of(2022, 11, 11));
            caseData.setJoSetAsideCreatedDate(LocalDateTime.now());
            caseData.setActiveJudgment(JudgmentDetails.builder().state(JudgmentState.SET_ASIDE).build());
            when(featureToggleService.isJOLiveFeedActive()).thenReturn(true);
            var eventHistory = mapper.buildEvents(caseData, BEARER_TOKEN);
            assertThat(eventHistory).extracting("setAsideJudgment").asList()
                .extracting("eventCode").asString().contains("[170, 170]");

        }
    }

    @Nested
    class RejectPaymentPlan {

        final String partyID = "002";

        @Test
        public void shouldGenerateRPA_PartAdmitRejectPayment() {
            DynamicList locationValues = DynamicList.fromList(List.of("Value 1"));
            DynamicList preferredCourt = DynamicList.builder()
                .listItems(locationValues.getListItems())
                .value(locationValues.getListItems().get(0))
                .build();

            CaseData caseData = CaseDataBuilder.builder()
                .setClaimTypeToSpecClaim()
                .atStateSpec1v1ClaimSubmitted()
                .atStateRespondent1v1FullAdmissionSpec().build().toBuilder()
                .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
                .addRespondent2(NO)
                .applicant1AcceptPartAdmitPaymentPlanSpec(NO)
                .respondent1DQ(
                    Respondent1DQ.builder()
                        .respondToCourtLocation(
                            RequestedCourt.builder()
                                .responseCourtLocations(preferredCourt)
                                .reasonForHearingAtSpecificCourt("Reason")
                                .build()
                        )
                        .build()
                )
                .specDefenceAdmittedRequired(YES)
                .build();

            List<Event> expectedDirectionsQuestionnaireFiled =
                List.of(Event.builder()
                            .eventSequence(3)
                            .eventCode("197")
                            .dateReceived(caseData.getRespondent1ResponseDate())
                            .litigiousPartyID(partyID)
                            .eventDetailsText(mapper.prepareFullDefenceEventText(
                                caseData.getRespondent1DQ(),
                                caseData,
                                true,
                                caseData.getRespondent1()
                            ))
                            .eventDetails(EventDetails.builder()
                                              .stayClaim(mapper.isStayClaim(caseData.getRespondent1DQ()))
                                              .preferredCourtCode(mapper.getPreferredCourtCode(
                                                  caseData.getRespondent1DQ()))
                                              .preferredCourtName("")
                                              .build())
                            .build());

            var eventHistory = mapper.buildEvents(caseData, BEARER_TOKEN);
            assertThat(eventHistory).extracting("miscellaneous").asList()
                .extracting("eventCode").asString().contains("999");
            assertThat(eventHistory).extracting("statesPaid").asList()
                .extracting("eventCode").asString().contains("49");
            assertThat(eventHistory).extracting("miscellaneous").asList()
                .extracting("eventDetailsText").asString().contains(RPA_REASON_MANUAL_DETERMINATION);
            assertThat(eventHistory).extracting("directionsQuestionnaireFiled").asList()
                .contains(expectedDirectionsQuestionnaireFiled.get(0));
        }

        @Test
        public void shouldGenerateRPA_FullAdmitRejectPayment() {
            CaseData caseData = CaseDataBuilder.builder()
                .setClaimTypeToSpecClaim()
                .atStateSpec1v1ClaimSubmitted()
                .atStateRespondent1v1FullAdmissionSpec().build().toBuilder()
                .addRespondent2(NO)
                .applicant1AcceptFullAdmitPaymentPlanSpec(NO)
                .build();

            var eventHistory = mapper.buildEvents(caseData, BEARER_TOKEN);
            assertThat(eventHistory).extracting("miscellaneous").asList()
                .extracting("eventCode").asString().contains("999");
            assertThat(eventHistory).extracting("miscellaneous").asList()
                .extracting("eventDetailsText").asString().contains(RPA_REASON_MANUAL_DETERMINATION);
        }
    }

    @Nested
    class ClaimInMediation {

        final String partyID = "002";

        @Test
        public void shouldGenerateRPA_ForPartAdmit_WhenClaimIsInMediation() {
            DynamicList locationValues = DynamicList.fromList(List.of("Value 1"));
            DynamicList preferredCourt = DynamicList.builder()
                .listItems(locationValues.getListItems())
                .value(locationValues.getListItems().get(0))
                .build();

            CaseData caseData = CaseDataBuilder.builder()
                .setClaimTypeToSpecClaim()
                .atStateSpec1v1ClaimSubmitted()
                .atStateRespondent1v1FullAdmissionSpec().build().toBuilder()
                .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
                .responseClaimMediationSpecRequired(YES)
                .caseDataLiP(CaseDataLiP.builder().applicant1ClaimMediationSpecRequiredLip(
                    ClaimantMediationLip.builder().hasAgreedFreeMediation(MediationDecision.Yes).build()).build())
                .addRespondent2(NO)
                .applicant1DQ(
                    Applicant1DQ.builder()
                        .applicant1DQRequestedCourt(
                            RequestedCourt.builder()
                                .responseCourtLocations(preferredCourt)
                                .reasonForHearingAtSpecificCourt("test")
                                .build()
                        ).build()
                )
                .respondent1DQ(
                    Respondent1DQ.builder()
                        .respondToCourtLocation(
                            RequestedCourt.builder()
                                .responseCourtLocations(preferredCourt)
                                .reasonForHearingAtSpecificCourt("Reason")
                                .build()
                        )
                        .build()
                )
                .specDefenceAdmittedRequired(NO)
                .build();

            List<Event> expectedDirectionsQuestionnaireFiled =
                List.of(Event.builder()
                            .eventSequence(3)
                            .eventCode("197")
                            .dateReceived(caseData.getRespondent1ResponseDate())
                            .litigiousPartyID(partyID)
                            .eventDetailsText(mapper.prepareFullDefenceEventText(
                                caseData.getRespondent1DQ(),
                                caseData,
                                true,
                                caseData.getRespondent1()
                            ))
                            .eventDetails(EventDetails.builder()
                                              .stayClaim(mapper.isStayClaim(caseData.getRespondent1DQ()))
                                              .preferredCourtCode(mapper.getPreferredCourtCode(
                                                  caseData.getRespondent1DQ()))
                                              .preferredCourtName("")
                                              .build())
                            .build());
            var eventHistory = mapper.buildEvents(caseData, BEARER_TOKEN);
            assertThat(eventHistory).extracting("miscellaneous").asList()
                .extracting("eventCode").asString().contains("999");
            assertThat(eventHistory).extracting("miscellaneous").asList()
                .extracting("eventDetailsText").asString().contains(RPA_IN_MEDIATION);
            assertThat(eventHistory).extracting("directionsQuestionnaireFiled").asList()
                .contains(expectedDirectionsQuestionnaireFiled.get(0));
        }

        @Test
        public void shouldGenerateRPA_ForFullDefence_WhenClaimIsInMediation() {
            BigDecimal claimValue = BigDecimal.valueOf(1000);
            DynamicList locationValues = DynamicList.fromList(List.of("Value 1"));
            DynamicList preferredCourt = DynamicList.builder()
                .listItems(locationValues.getListItems())
                .value(locationValues.getListItems().get(0))
                .build();

            CaseData caseData = CaseDataBuilder.builder()
                .setClaimTypeToSpecClaim()
                .atStateSpec1v1ClaimSubmitted()
                .atStateRespondent1v1FullAdmissionSpec().build().toBuilder()
                .responseClaimMediationSpecRequired(YES)
                .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
                .respondent1AcknowledgeNotificationDate(null)
                .totalClaimAmount(claimValue)
                .caseDataLiP(CaseDataLiP.builder().applicant1ClaimMediationSpecRequiredLip(
                    ClaimantMediationLip.builder().hasAgreedFreeMediation(MediationDecision.Yes).build()).build())
                .applicant1DQ(
                    Applicant1DQ.builder()
                        .applicant1DQRequestedCourt(
                            RequestedCourt.builder()
                                .responseCourtLocations(preferredCourt)
                                .reasonForHearingAtSpecificCourt("test")
                                .build()
                        ).build()
                )
                .respondent1DQ(
                Respondent1DQ.builder()
                    .respondToCourtLocation(
                        RequestedCourt.builder()
                            .responseCourtLocations(preferredCourt)
                            .reasonForHearingAtSpecificCourt("Reason")
                            .build()
                    )
                    .build()
            )
                .build().toBuilder()
                .respondToClaim(RespondToClaim.builder()
                                    .howMuchWasPaid(BigDecimal.valueOf(100000))
                                    .build())
                .build();
            Event expectedDefenceFiled = Event.builder()
                .eventSequence(2)
                .eventCode("49")
                .dateReceived(caseData.getRespondent1ResponseDate())
                .litigiousPartyID(partyID)
                .build();
            List<Event> expectedDirectionsQuestionnaireFiled =
                List.of(Event.builder()
                            .eventSequence(3)
                            .eventCode("197")
                            .dateReceived(caseData.getRespondent1ResponseDate())
                            .litigiousPartyID(partyID)
                            .eventDetailsText(mapper.prepareFullDefenceEventText(
                                caseData.getRespondent1DQ(),
                                caseData,
                                true,
                                caseData.getRespondent1()
                            ))
                            .eventDetails(EventDetails.builder()
                                              .stayClaim(mapper.isStayClaim(caseData.getRespondent1DQ()))
                                              .preferredCourtCode(mapper.getPreferredCourtCode(
                                                  caseData.getRespondent1DQ()))
                                              .preferredCourtName("")
                                              .build())
                            .build()
                );

            var eventHistory = mapper.buildEvents(caseData, BEARER_TOKEN);

            assertThat(eventHistory).isNotNull();
            assertThat(eventHistory).extracting("statesPaid").asList()
                .containsExactly(expectedDefenceFiled);
            assertThat(eventHistory).extracting("directionsQuestionnaireFiled").asList()
                .contains(expectedDirectionsQuestionnaireFiled.get(0));

            assertThat(eventHistory).extracting("miscellaneous").asList()
                .extracting("eventCode").asString().contains("999");
            assertThat(eventHistory).extracting("miscellaneous").asList()
                .extracting("eventDetailsText").asString().contains(RPA_IN_MEDIATION);
        }

        @Test
        public void shouldGenerateRPA_ForFullDefence_WhenClaimIsInMediationNoLegalRep() {
            BigDecimal claimValue = BigDecimal.valueOf(1500);
            DynamicList locationValues = DynamicList.fromList(List.of("Value 1"));
            DynamicList preferredCourt = DynamicList.builder()
                .listItems(locationValues.getListItems())
                .value(locationValues.getListItems().get(0))
                .build();

            CaseData caseData = CaseDataBuilder.builder()
                .setClaimTypeToSpecClaim()
                .specClaim1v1LrVsLip()
                .atStateSpec1v1ClaimSubmitted()
                .respondent1(PartyBuilder.builder().company().build())
                .atStateRespondent1v1FullAdmissionSpec().build().toBuilder()
                .responseClaimMediationSpecRequired(YES)
                .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
                .defenceRouteRequired(SpecJourneyConstantLRSpec.HAS_PAID_THE_AMOUNT_CLAIMED)
                .respondent1AcknowledgeNotificationDate(null)
                .totalClaimAmount(claimValue)
                .respondent1Represented(null)
                .specRespondent1Represented(NO)
                .respondent1PinToPostLRspec(DefendantPinToPostLRspec.builder().build())
                .caseDataLiP(CaseDataLiP.builder().applicant1ClaimMediationSpecRequiredLip(
                    ClaimantMediationLip.builder().hasAgreedFreeMediation(MediationDecision.Yes).build()).build())
                .applicant1DQ(
                    Applicant1DQ.builder()
                        .applicant1DQRequestedCourt(
                            RequestedCourt.builder()
                                .responseCourtLocations(preferredCourt)
                                .reasonForHearingAtSpecificCourt("test")
                                .build()
                        ).build()
                )
                .respondent1DQ(
                    Respondent1DQ.builder()
                        .respondToCourtLocation(
                            RequestedCourt.builder()
                                .responseCourtLocations(preferredCourt)
                                .reasonForHearingAtSpecificCourt("Reason")
                                .build()
                        )
                        .build()
                )
                .build().toBuilder()
                .respondToClaim(RespondToClaim.builder()
                                    .howMuchWasPaid(BigDecimal.valueOf(100000))
                                    .build())
                .build();
            Event expectedDefenceFiled = Event.builder()
                .eventSequence(2)
                .eventCode("49")
                .dateReceived(caseData.getRespondent1ResponseDate())
                .litigiousPartyID(partyID)
                .build();
            List<Event> expectedDirectionsQuestionnaireFiled =
                List.of(Event.builder()
                            .eventSequence(3)
                            .eventCode("197")
                            .dateReceived(caseData.getRespondent1ResponseDate())
                            .litigiousPartyID(partyID)
                            .eventDetailsText(mapper.prepareFullDefenceEventText(
                                caseData.getRespondent1DQ(),
                                caseData,
                                true,
                                caseData.getRespondent1()
                            ))
                            .eventDetails(EventDetails.builder()
                                              .stayClaim(mapper.isStayClaim(caseData.getRespondent1DQ()))
                                              .preferredCourtCode(mapper.getPreferredCourtCode(
                                                  caseData.getRespondent1DQ()))
                                              .preferredCourtName("")
                                              .build())
                            .build()
                );

            var eventHistory = mapper.buildEvents(caseData, BEARER_TOKEN);
            assertThat(eventHistory).extracting("directionsQuestionnaireFiled").asList()
                .contains(expectedDirectionsQuestionnaireFiled.get(0));
            assertThat(eventHistory).extracting("statesPaid").asList()
                .containsExactly(expectedDefenceFiled);
            assertThat(eventHistory).extracting("miscellaneous").asList()
                .extracting("eventCode").asString().contains("999");
            assertThat(eventHistory).extracting("miscellaneous").asList()
                .extracting("eventDetailsText").asString().contains(RPA_IN_MEDIATION);
        }
    }

    @Nested
    class RequestJudgmentByAdmission {
        LocalDateTime now = LocalDate.now().atTime(12, 0, 0);

        @Test
        public void shouldGenerateRPA_ForFullAdmit_WhenClaimAgreedRepaymentPlan_BySetDate() {
            LocalDate whenWillPay = LocalDate.now().plusDays(5);

            CCJPaymentDetails ccjPaymentDetails = CCJPaymentDetails.builder()
                .ccjPaymentPaidSomeOption(NO)
                .ccjJudgmentAmountClaimAmount(BigDecimal.valueOf(1500))
                .ccjJudgmentFixedCostAmount(BigDecimal.valueOf(40))
                .ccjJudgmentAmountClaimFee(BigDecimal.valueOf(40))
                .ccjPaymentPaidSomeAmountInPounds(ZERO)
                .build();

            RespondToClaimAdmitPartLRspec paymentDetails = RespondToClaimAdmitPartLRspec.builder()
                .whenWillThisAmountBePaid(whenWillPay)
                .build();

            CaseData caseData = CaseDataBuilder.builder()
                .setClaimTypeToSpecClaim()
                .atStateSpec1v1ClaimSubmitted()
                .atStateRespondent1v1FullAdmissionSpec().build().toBuilder()
                .ccjPaymentDetails(ccjPaymentDetails)
                .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE)
                .respondToClaimAdmitPartLRspec(paymentDetails)
                .applicant1ResponseDate(now)
                .totalInterest(BigDecimal.ZERO)
                .joJudgementByAdmissionIssueDate(now)
                .build();

            var eventHistory = mapper.buildEvents(caseData, BEARER_TOKEN);
            assertThat(eventHistory).isNotNull();
            assertThat(eventHistory).extracting("judgmentByAdmission").isNotNull();
            assertThat(eventHistory).extracting("judgmentByAdmission").asList()
                .extracting("eventCode").asString().contains("240");
            assertThat(eventHistory).extracting("judgmentByAdmission").asList()
                .extracting("eventDetails").asList()
                .extracting("isJudgmentForthwith").contains(false);
            assertThat(eventHistory).extracting("judgmentByAdmission").asList()
                .extracting("eventDetails").asList()
                .extracting("paymentInFullDate").contains(whenWillPay.atStartOfDay());
            assertThat(eventHistory).extracting("judgmentByAdmission").asList()
                .extracting("eventDetails").asList()
                .extracting("installmentAmount").asString().contains("null");
            assertThat(eventHistory).extracting("miscellaneous").asList()
                .extracting("eventCode").asString().contains("999");
            assertThat(eventHistory).extracting("miscellaneous").asList()
                .extracting("eventDetailsText").asString().contains(RPA_REASON_JUDGMENT_BY_ADMISSION);
        }

        @Test
        public void shouldGenerateRPA_ForFullAdmit_WhenClaimAgreedRepaymentPlan() {
            LocalDate whenWillPay = LocalDate.now().plusDays(5);

            CCJPaymentDetails ccjPaymentDetails = CCJPaymentDetails.builder()
                .ccjPaymentPaidSomeOption(NO)
                .ccjJudgmentAmountClaimAmount(BigDecimal.valueOf(1500))
                .ccjJudgmentFixedCostAmount(BigDecimal.valueOf(40))
                .ccjJudgmentAmountClaimFee(BigDecimal.valueOf(40))
                .ccjPaymentPaidSomeAmountInPounds(ZERO)
                .build();

            RepaymentPlanLRspec respondent1RepaymentPlan = RepaymentPlanLRspec.builder()
                .firstRepaymentDate(whenWillPay)
                .paymentAmount(BigDecimal.valueOf(10000))
                .repaymentFrequency(PaymentFrequencyLRspec.ONCE_ONE_MONTH)
                .build();

            CaseData caseData = CaseDataBuilder.builder()
                .setClaimTypeToSpecClaim()
                .atStateSpec1v1ClaimSubmitted()
                .atStateRespondent1v1FullAdmissionSpec().build().toBuilder()
                .ccjPaymentDetails(ccjPaymentDetails)
                .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.SUGGESTION_OF_REPAYMENT_PLAN)
                .respondent1RepaymentPlan(respondent1RepaymentPlan)
                .totalInterest(BigDecimal.ZERO)
                .joJudgementByAdmissionIssueDate(now)
                .build();

            var eventHistory = mapper.buildEvents(caseData, BEARER_TOKEN);
            assertThat(eventHistory).isNotNull();
            assertThat(eventHistory).extracting("judgmentByAdmission").isNotNull();
            assertThat(eventHistory).extracting("judgmentByAdmission").asList()
                .extracting("eventCode").asString().contains("240");
            assertThat(eventHistory).extracting("judgmentByAdmission").asList()
                .extracting("eventDetails").asList()
                .extracting("isJudgmentForthwith").contains(false);
            assertThat(eventHistory).extracting("judgmentByAdmission").asList()
                .extracting("eventDetails").asList()
                .extracting("firstInstallmentDate").contains(whenWillPay);
            assertThat(eventHistory).extracting("judgmentByAdmission").asList()
                .extracting("eventDetails").asList()
                .extracting("installmentPeriod").contains("MTH");
            assertThat(eventHistory).extracting("judgmentByAdmission").asList()
                .extracting("eventDetails").asList()
                .extracting("installmentAmount").contains(BigDecimal.valueOf(100).setScale(2));
            assertThat(eventHistory).extracting("miscellaneous").asList()
                .extracting("eventCode").asString().contains("999");
            assertThat(eventHistory).extracting("miscellaneous").asList()
                .extracting("eventDetailsText").asString().contains(RPA_REASON_JUDGMENT_BY_ADMISSION);
        }

        @Test
        public void shouldGenerateRPA_ForFullAdmit_WhenClaimImmediately() {
            LocalDate whenWillPay = LocalDate.now().plusDays(5);

            CCJPaymentDetails ccjPaymentDetails = CCJPaymentDetails.builder()
                .ccjPaymentPaidSomeOption(NO)
                .ccjJudgmentAmountClaimAmount(BigDecimal.valueOf(1500))
                .ccjJudgmentFixedCostAmount(BigDecimal.valueOf(40))
                .ccjJudgmentAmountClaimFee(BigDecimal.valueOf(40))
                .ccjPaymentPaidSomeAmountInPounds(ZERO)
                .build();

            RespondToClaimAdmitPartLRspec paymentDetails = RespondToClaimAdmitPartLRspec.builder()
                .whenWillThisAmountBePaid(whenWillPay)
                .build();

            CaseData caseData = CaseDataBuilder.builder()
                .setClaimTypeToSpecClaim()
                .atStateSpec1v1ClaimSubmitted()
                .atStateRespondent1v1FullAdmissionSpec().build().toBuilder()
                .ccjPaymentDetails(ccjPaymentDetails)
                .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY)
                .respondToClaimAdmitPartLRspec(paymentDetails)
                .totalInterest(BigDecimal.ZERO)
                .applicant1ResponseDate(LocalDateTime.now())
                .joJudgementByAdmissionIssueDate(now)
                .build();

            var eventHistory = mapper.buildEvents(caseData, BEARER_TOKEN);
            assertThat(eventHistory).isNotNull();
            assertThat(eventHistory).extracting("judgmentByAdmission").isNotNull();
            assertThat(eventHistory).extracting("judgmentByAdmission").asList()
                .extracting("eventCode").asString().contains("240");
            assertThat(eventHistory).extracting("judgmentByAdmission").asList()
                .extracting("eventDetails").asList()
                .extracting("isJudgmentForthwith").contains(true);
            assertThat(eventHistory).extracting("miscellaneous").asList()
                .extracting("eventCode").asString().contains("999");
            assertThat(eventHistory).extracting("miscellaneous").asList()
                .extracting("eventDetailsText").asString().contains(RPA_REASON_JUDGMENT_BY_ADMISSION);
        }

        @Test
        public void shouldGenerateRPA_ForFullAdmit_WhenClaimImmediatelyForLip() {
            LocalDate whenWillPay = LocalDate.now().plusDays(5);

            CCJPaymentDetails ccjPaymentDetails = CCJPaymentDetails.builder()
                .ccjPaymentPaidSomeOption(NO)
                .ccjJudgmentAmountClaimAmount(BigDecimal.valueOf(1500))
                .ccjJudgmentFixedCostAmount(BigDecimal.valueOf(40))
                .ccjJudgmentAmountClaimFee(BigDecimal.valueOf(40))
                .ccjPaymentPaidSomeAmountInPounds(ZERO)
                .ccjJudgmentLipInterest(ZERO)
                .build();

            RespondToClaimAdmitPartLRspec paymentDetails = RespondToClaimAdmitPartLRspec.builder()
                .whenWillThisAmountBePaid(whenWillPay)
                .build();

            CaseData caseData = CaseDataBuilder.builder()
                .setClaimTypeToSpecClaim()
                .atStateSpec1v1ClaimSubmitted()
                .atStateRespondent1v1FullAdmissionSpec().build().toBuilder()
                .ccjPaymentDetails(ccjPaymentDetails)
                .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY)
                .respondToClaimAdmitPartLRspec(paymentDetails)
                .totalInterest(BigDecimal.ZERO)
                .applicant1ResponseDate(LocalDateTime.now())
                .respondent1Represented(YesOrNo.NO)
                .specRespondent1Represented(YesOrNo.NO)
                .applicant1Represented(YesOrNo.NO)
                .joJudgementByAdmissionIssueDate(now)
                .build();

            var eventHistory = mapper.buildEvents(caseData, BEARER_TOKEN);
            assertThat(eventHistory).isNotNull();
            assertThat(eventHistory).extracting("judgmentByAdmission").isNotNull();
            assertThat(eventHistory).extracting("judgmentByAdmission").asList()
                .extracting("eventCode").asString().contains("240");
            assertThat(eventHistory).extracting("judgmentByAdmission").asList()
                .extracting("eventDetails").asList()
                .extracting("isJudgmentForthwith").contains(true);
            assertThat(eventHistory).extracting("miscellaneous").asList()
                .extracting("eventCode").asString().contains("999");
            assertThat(eventHistory).extracting("miscellaneous").asList()
                .extracting("eventDetailsText").asString().contains(RPA_REASON_JUDGMENT_BY_ADMISSION);
        }

        @Test
        void shouldNotThrowNullPointerException_whenRepaymentPlanIsNull() {
            //Given
            CCJPaymentDetails ccjPaymentDetails = CCJPaymentDetails.builder()
                .ccjJudgmentAmountClaimAmount(BigDecimal.valueOf(1500))
                .ccjJudgmentAmountClaimFee(BigDecimal.valueOf(40))
                .ccjPaymentPaidSomeAmountInPounds(ZERO)
                .ccjJudgmentFixedCostAmount(ZERO)
                .ccjPaymentPaidSomeOption(NO)
                .ccjJudgmentFixedCostOption(NO)
                .build();
            CaseData caseData = CaseDataBuilder.builder()
                .setClaimTypeToSpecClaim()
                .atStateSpec1v1ClaimSubmitted()
                .atStateRespondent1v1FullAdmissionSpec().build().toBuilder()
                .ccjPaymentDetails(ccjPaymentDetails)
                .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.SUGGESTION_OF_REPAYMENT_PLAN)
                .totalInterest(BigDecimal.ZERO)
                .joJudgementByAdmissionIssueDate(now)
                .build();
            //When
            var eventHistory = mapper.buildEvents(caseData, BEARER_TOKEN);
            //Then
            assertThat(eventHistory).extracting("judgmentByAdmission").asList()
                .extracting("eventDetails").asList()
                .extracting("firstInstallmentDate").isNotNull();
            assertThat(eventHistory).extracting("judgmentByAdmission").asList()
                .extracting("eventDetails").asList()
                .extracting("installmentPeriod").isNotNull();
            assertThat(eventHistory).extracting("judgmentByAdmission").asList()
                .extracting("eventDetails").asList()
                .extracting("installmentAmount").isNotNull();

        }

        @Test
        void validateRespondent2IntentionType() {
            CaseData caseData = CaseDataBuilder.builder()
                .respondent2Represented(NO)
                .respondent2ClaimResponseIntentionType(ResponseIntention.FULL_DEFENCE)
                .build();

            assertEquals("Defend all of the claim", mapper.evaluateRespondent2IntentionType(caseData));

            CaseData caseData1 = CaseDataBuilder.builder()
                .respondent2Represented(NO)
                .respondent1Represented(YES)
                .respondent1ClaimResponseIntentionType(ResponseIntention.FULL_DEFENCE)
                .build();
            assertEquals("Defend all of the claim", mapper.evaluateRespondent2IntentionType(caseData1));

            CaseData caseData2 = CaseDataBuilder.builder()
                .respondent2Represented(YES)
                .respondent1Represented(YES)
                .respondent1ClaimResponseIntentionType(PART_DEFENCE)
                .build();
            assertEquals("Defend part of the claim", mapper.evaluateRespondent2IntentionType(caseData2));
        }

        @Test
        void shouldGenerateRPA_WhenClaimPayImmediately() {
            LocalDate whenWillPay = LocalDate.now().plusDays(5);
            CCJPaymentDetails ccjPaymentDetails = buildCcjPaymentDetails();
            RespondToClaimAdmitPartLRspec paymentDetails = RespondToClaimAdmitPartLRspec.builder()
                .whenWillThisAmountBePaid(whenWillPay)
                .build();
            CaseData caseData = CaseDataBuilder.builder()
                .setClaimTypeToSpecClaim()
                .atStateSpec1v1ClaimSubmitted()
                .atStateRespondent1v1FullAdmissionSpec().build().toBuilder()
                .ccjPaymentDetails(ccjPaymentDetails)
                .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY)
                .respondToClaimAdmitPartLRspec(paymentDetails)
                .totalInterest(BigDecimal.ZERO)
                .applicant1ResponseDate(LocalDateTime.now())
                .ccdState(CaseState.All_FINAL_ORDERS_ISSUED)
                .joJudgementByAdmissionIssueDate(now)
                .build();
            when(featureToggleService.isJOLiveFeedActive()).thenReturn(true);
            var eventHistory = mapper.buildEvents(caseData, BEARER_TOKEN);
            assertThat(eventHistory).isNotNull();
            assertThat(eventHistory).extracting("judgmentByAdmission").isNotNull();
            assertThat(eventHistory).extracting("judgmentByAdmission").asList()
                .extracting("eventCode").asString().contains("240");
            assertThat(eventHistory).extracting("judgmentByAdmission").asList()
                .extracting("eventDetails").asList()
                .extracting("isJudgmentForthwith").contains(true);
            assertThat(eventHistory).extracting("miscellaneous").asList()
                .extracting("eventCode").asString().contains("999");
            assertThat(eventHistory).extracting("miscellaneous").asList()
                .extracting("eventDetailsText").asString().contains(RPA_RECORD_JUDGMENT_REASON);
        }

        @Test
        void shouldGenerateRPA_WhenLrvLrClaimPayImmediately() {
            LocalDate whenWillPay = LocalDate.now().plusDays(5);
            CCJPaymentDetails ccjPaymentDetails = buildCcjPaymentDetails();
            RespondToClaimAdmitPartLRspec paymentDetails = RespondToClaimAdmitPartLRspec.builder()
                .whenWillThisAmountBePaid(whenWillPay)
                .build();
            CaseData caseData = CaseDataBuilder.builder()
                .setClaimTypeToSpecClaim()
                .atStateSpec1v1ClaimSubmitted()
                .atStateRespondent1v1FullAdmissionSpec().build().toBuilder()
                .ccjPaymentDetails(ccjPaymentDetails)
                .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY)
                .respondToClaimAdmitPartLRspec(paymentDetails)
                .totalInterest(BigDecimal.ZERO)
                .applicant1ResponseDate(LocalDateTime.now())
                .respondent1Represented(YesOrNo.YES)
                .specRespondent1Represented(YesOrNo.YES)
                .applicant1Represented(YesOrNo.YES)
                .ccdState(CaseState.PROCEEDS_IN_HERITAGE_SYSTEM)
                .joJudgementByAdmissionIssueDate(now)
                .build();
            when(featureToggleService.isJOLiveFeedActive()).thenReturn(true);
            var eventHistory = mapper.buildEvents(caseData, BEARER_TOKEN);
            assertThat(eventHistory).isNotNull();
            assertThat(eventHistory).extracting("judgmentByAdmission").isNotNull();
            assertThat(eventHistory).extracting("judgmentByAdmission").asList()
                .extracting("eventCode").asString().contains("240");
            assertThat(eventHistory).extracting("miscellaneous").asList()
                .extracting("eventCode").asString().contains("999");
            assertThat(eventHistory).extracting("miscellaneous").asList()
                .extracting("eventDetails").extracting("miscText").asString().contains(RECORD_JUDGMENT);
            assertThat(eventHistory).extracting("miscellaneous").asList()
                .extracting("eventDetailsText").asString().contains(RPA_RECORD_JUDGMENT_REASON);
        }

        @Test
        public void shouldGenerateRPA_ForFullAdmit_WhenLipClaimAgreedRepaymentPlan_BySetDate_JoLiveFeed() {
            LocalDate whenWillPay = LocalDate.now().plusDays(5);
            CCJPaymentDetails ccjPaymentDetails = buildCcjPaymentDetails();
            RespondToClaimAdmitPartLRspec paymentDetails = RespondToClaimAdmitPartLRspec.builder()
                .whenWillThisAmountBePaid(whenWillPay)
                .build();
            CaseData caseData = CaseDataBuilder.builder()
                .setClaimTypeToSpecClaim()
                .atStateSpec1v1ClaimSubmitted()
                .atStateRespondent1v1FullAdmissionSpec().build().toBuilder()
                .ccjPaymentDetails(ccjPaymentDetails)
                .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE)
                .respondToClaimAdmitPartLRspec(paymentDetails)
                .applicant1ResponseDate(LocalDateTime.now())
                .totalInterest(BigDecimal.ZERO)
                .joJudgementByAdmissionIssueDate(now)
                .build();
            given(featureToggleService.isJOLiveFeedActive()).willReturn(true);
            var eventHistory = mapper.buildEvents(caseData, BEARER_TOKEN);
            assertThat(eventHistory).isNotNull();
            assertThat(eventHistory).extracting("judgmentByAdmission").isNotNull();
            assertThat(eventHistory).extracting("judgmentByAdmission").asList()
                .extracting("eventCode").asString().contains("240");
            assertThat(eventHistory).extracting("miscellaneous").asList()
                .extracting("eventCode").asString().contains("999");
            assertThat(eventHistory).extracting("miscellaneous").asList()
                .extracting("eventDetails").extracting("miscText").asString().contains(RECORD_JUDGMENT);
            assertThat(eventHistory).extracting("miscellaneous").asList()
                .extracting("eventDetailsText").asString().contains(RPA_RECORD_JUDGMENT_REASON);
        }

        @Test
        public void shouldGenerateRPA_ForFullAdmit_WhenLipClaimAgreedRepaymentPlan_JoLiveFeed() {
            LocalDate whenWillPay = LocalDate.now().plusDays(5);
            CCJPaymentDetails ccjPaymentDetails = buildCcjPaymentDetails();
            RepaymentPlanLRspec respondent1RepaymentPlan = RepaymentPlanLRspec.builder()
                .firstRepaymentDate(whenWillPay)
                .paymentAmount(BigDecimal.valueOf(10000))
                .repaymentFrequency(PaymentFrequencyLRspec.ONCE_ONE_MONTH)
                .build();
            CaseData caseData = CaseDataBuilder.builder()
                .setClaimTypeToSpecClaim()
                .atStateSpec1v1ClaimSubmitted()
                .atStateRespondent1v1FullAdmissionSpec().build().toBuilder()
                .ccjPaymentDetails(ccjPaymentDetails)
                .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.SUGGESTION_OF_REPAYMENT_PLAN)
                .respondent1RepaymentPlan(respondent1RepaymentPlan)
                .totalInterest(BigDecimal.ZERO)
                .joJudgementByAdmissionIssueDate(now)
                .build();
            given(featureToggleService.isJOLiveFeedActive()).willReturn(true);
            var eventHistory = mapper.buildEvents(caseData, BEARER_TOKEN);
            assertThat(eventHistory).isNotNull();
            assertThat(eventHistory).extracting("judgmentByAdmission").isNotNull();
            assertThat(eventHistory).extracting("judgmentByAdmission").asList()
                .extracting("eventCode").asString().contains("240");
            assertThat(eventHistory).extracting("miscellaneous").asList()
                .extracting("eventCode").asString().contains("999");
            assertThat(eventHistory).extracting("miscellaneous").asList()
                .extracting("eventDetails").extracting("miscText").asString().contains(RECORD_JUDGMENT);
            assertThat(eventHistory).extracting("miscellaneous").asList()
                .extracting("eventDetailsText").asString().contains(RPA_RECORD_JUDGMENT_REASON);
        }

        private CCJPaymentDetails buildCcjPaymentDetails() {
            return CCJPaymentDetails.builder()
                .ccjPaymentPaidSomeOption(NO)
                .ccjJudgmentAmountClaimAmount(BigDecimal.valueOf(1500))
                .ccjJudgmentFixedCostAmount(BigDecimal.valueOf(40))
                .ccjJudgmentAmountClaimFee(BigDecimal.valueOf(40))
                .ccjPaymentPaidSomeAmountInPounds(ZERO)
                .build();
        }
    }

    @Nested
    class Cosc {
        LocalDate markPaidInFullDate = LocalDate.of(2024, 1, 1);
        LocalDate defendantFinalPaymentDate = LocalDate.of(2024, 1, 4);
        LocalDateTime markPaidInFullIssueDate = LocalDateTime.of(2024, 1, 2,  9, 0, 0);
        LocalDateTime schedulerDeadline = LocalDateTime.of(2024, 2, 2,  16, 0, 0);
        LocalDateTime joDefendantMarkedPaidInFullIssueDate = LocalDateTime.of(2024, 1, 3, 16, 0, 0);
        CaseDocument caseDocument = CaseDocument.builder()
            .documentType(DocumentType.CERTIFICATE_OF_DEBT_PAYMENT)
            .build();

        @BeforeEach
        void setup() {
            when(featureToggleService.isCoSCEnabled()).thenReturn(true);
            when(featureToggleService.isJOLiveFeedActive()).thenReturn(true);
        }

        @Nested
        class CancelledStatus {
            @Test
            public void shouldGenerateRPA_Cancelled_MarkPaidInFull_CoscApplied() {
                CertOfSC certOfSC = CertOfSC.builder()
                    .defendantFinalPaymentDate(LocalDate.now())
                    .debtPaymentEvidence(DebtPaymentEvidence.builder()
                                             .debtPaymentOption(DebtPaymentOptions.MADE_FULL_PAYMENT_TO_COURT).build()).build();

                CaseData caseData = CaseDataBuilder.builder()
                    .buildJudgmentOnlineCaseWithMarkJudgementPaidAfter31DaysForCosc().toBuilder()
                    .certOfSC(certOfSC)
                    .systemGeneratedCaseDocuments(wrapElements(caseDocument))
                    .joMarkedPaidInFullIssueDate(markPaidInFullIssueDate)
                    .joCoscRpaStatus(CANCELLED)
                    .joFullyPaymentMadeDate(markPaidInFullDate)
                    .build();

                Event expectedEvent = Event.builder()
                    .eventSequence(1)
                    .eventCode("600")
                    .dateReceived(markPaidInFullIssueDate)
                    .litigiousPartyID("001")
                    .eventDetailsText("")
                    .eventDetails(EventDetails.builder()
                                      .status(String.valueOf(CANCELLED))
                                      .datePaidInFull(markPaidInFullDate)
                                      .notificationReceiptDate(markPaidInFullIssueDate.toLocalDate())
                                      .build())
                    .build();

                EventHistory eventHistory = mapper.buildEvents(caseData, BEARER_TOKEN);

                assertThat(eventHistory).extracting("certificateOfSatisfactionOrCancellation").asList()
                    .extracting("eventCode").asString().contains("600");
                assertThat(eventHistory).extracting("certificateOfSatisfactionOrCancellation")
                    .asList().containsExactly(expectedEvent);
            }

            @Test
            public void shouldGenerateRPA_Cancelled_MarkPaidInFull_NoCoscApplied() {
                CaseData caseData = CaseDataBuilder.builder()
                    .buildJudgmentOnlineCaseWithMarkJudgementPaidAfter31DaysForCosc().toBuilder()
                    .joMarkedPaidInFullIssueDate(markPaidInFullIssueDate)
                    .joCoscRpaStatus(CANCELLED)
                    .joFullyPaymentMadeDate(markPaidInFullDate)
                    .build();

                Event expectedEvent = Event.builder()
                    .eventSequence(1)
                    .eventCode("600")
                    .dateReceived(markPaidInFullIssueDate)
                    .eventDetailsText("")
                    .litigiousPartyID("001")
                    .eventDetails(EventDetails.builder()
                                      .status(String.valueOf(CANCELLED))
                                      .datePaidInFull(markPaidInFullDate)
                                      .notificationReceiptDate(markPaidInFullIssueDate.toLocalDate())
                                      .build())
                    .build();

                EventHistory eventHistory = mapper.buildEvents(caseData, BEARER_TOKEN);

                assertThat(eventHistory).extracting("certificateOfSatisfactionOrCancellation").asList()
                    .extracting("eventCode").asString().contains("600");
                assertThat(eventHistory).extracting("certificateOfSatisfactionOrCancellation")
                    .asList().containsExactly(expectedEvent);
            }

            @Test
            public void shouldGenerateRPA_Cancelled_NotMarkPaidInFull_CoscApplied() {
                CertOfSC certOfSC = CertOfSC.builder()
                    .defendantFinalPaymentDate(defendantFinalPaymentDate)
                    .debtPaymentEvidence(DebtPaymentEvidence.builder()
                                             .debtPaymentOption(DebtPaymentOptions.MADE_FULL_PAYMENT_TO_COURT).build()).build();

                CaseData caseData = CaseDataBuilder.builder()
                    .buildJudgmentOnlineCaseWithMarkJudgementPaidAfter31DaysForCosc().toBuilder()
                    .certOfSC(certOfSC)
                    .joDefendantMarkedPaidInFullIssueDate(joDefendantMarkedPaidInFullIssueDate)
                    .systemGeneratedCaseDocuments(wrapElements(caseDocument))
                    .joMarkedPaidInFullIssueDate(null)
                    .joCoscRpaStatus(CANCELLED)
                    .joFullyPaymentMadeDate(null)
                    .build();

                Event expectedEvent = Event.builder()
                    .eventSequence(1)
                    .eventCode("600")
                    .litigiousPartyID("002")
                    .dateReceived(joDefendantMarkedPaidInFullIssueDate)
                    .eventDetailsText("")
                    .eventDetails(EventDetails.builder()
                                      .status(String.valueOf(CANCELLED))
                                      .datePaidInFull(defendantFinalPaymentDate)
                                      .notificationReceiptDate(joDefendantMarkedPaidInFullIssueDate.toLocalDate())
                                      .build())
                    .build();

                EventHistory eventHistory = mapper.buildEvents(caseData, BEARER_TOKEN);

                assertThat(eventHistory).extracting("certificateOfSatisfactionOrCancellation").asList()
                    .extracting("eventCode").asString().contains("600");
                assertThat(eventHistory).extracting("certificateOfSatisfactionOrCancellation")
                    .asList().containsExactly(expectedEvent);
            }
        }

        @Nested
        class SatisfiedStatus {
            @Test
            public void shouldGenerateRPA_MarkedPaidInFull_CoscApplied() {
                CertOfSC certOfSC = CertOfSC.builder()
                    .defendantFinalPaymentDate(LocalDate.now())
                    .debtPaymentEvidence(DebtPaymentEvidence.builder()
                                             .debtPaymentOption(DebtPaymentOptions.MADE_FULL_PAYMENT_TO_COURT).build()).build();

                CaseData caseData = CaseDataBuilder.builder()
                    .buildJudgmentOnlineCaseWithMarkJudgementPaidAfter31DaysForCosc().toBuilder()
                    .certOfSC(certOfSC)
                    .systemGeneratedCaseDocuments(wrapElements(caseDocument))
                    .joMarkedPaidInFullIssueDate(markPaidInFullIssueDate)
                    .joCoscRpaStatus(SATISFIED)
                    .joFullyPaymentMadeDate(markPaidInFullDate)
                    .build();

                Event expectedEvent = Event.builder()
                    .eventSequence(1)
                    .eventCode("600")
                    .dateReceived(markPaidInFullIssueDate)
                    .eventDetailsText("")
                    .litigiousPartyID("001")
                    .eventDetails(EventDetails.builder()
                                      .status(String.valueOf(SATISFIED))
                                      .datePaidInFull(markPaidInFullDate)
                                      .notificationReceiptDate(markPaidInFullIssueDate.toLocalDate())
                                      .build())
                    .build();

                EventHistory eventHistory = mapper.buildEvents(caseData, BEARER_TOKEN);

                assertThat(eventHistory).extracting("certificateOfSatisfactionOrCancellation").asList()
                    .extracting("eventCode").asString().contains("600");
                assertThat(eventHistory).extracting("certificateOfSatisfactionOrCancellation")
                    .asList().containsExactly(expectedEvent);
            }
        }
    }
}
