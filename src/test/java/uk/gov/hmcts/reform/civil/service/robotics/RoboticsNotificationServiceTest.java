package uk.gov.hmcts.reform.civil.service.robotics;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.civil.config.PrdAdminUserConfiguration;
import uk.gov.hmcts.reform.civil.config.properties.robotics.RoboticsEmailConfiguration;
import uk.gov.hmcts.reform.civil.enums.DJPaymentTypeSelection;
import uk.gov.hmcts.reform.civil.enums.ResponseIntention;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CCJPaymentDetails;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.model.robotics.Event;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;
import uk.gov.hmcts.reform.civil.model.robotics.RoboticsCaseDataSpec;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.sendgrid.EmailData;
import uk.gov.hmcts.reform.civil.sendgrid.SendGridClient;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;
import uk.gov.hmcts.reform.civil.service.flowstate.StateFlowEngine;
import uk.gov.hmcts.reform.civil.referencedata.LocationRefDataService;
import uk.gov.hmcts.reform.civil.service.robotics.mapper.AddressLinesMapper;
import uk.gov.hmcts.reform.civil.service.robotics.mapper.EventHistoryMapper;
import uk.gov.hmcts.reform.civil.service.robotics.mapper.EventHistorySequencer;
import uk.gov.hmcts.reform.civil.service.robotics.mapper.RoboticsAddressMapper;
import uk.gov.hmcts.reform.civil.service.robotics.mapper.RoboticsDataMapper;
import uk.gov.hmcts.reform.civil.service.robotics.mapper.RoboticsDataMapperForSpec;
import uk.gov.hmcts.reform.civil.prd.client.OrganisationApi;
import uk.gov.hmcts.reform.civil.utils.LocationRefDataUtil;

import java.time.LocalDateTime;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.isMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseType.FULL_DEFENCE;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@ExtendWith(SpringExtension.class)
@SpringBootTest(
    classes = {
        RoboticsEmailConfiguration.class,
        RoboticsNotificationService.class,
        JacksonAutoConfiguration.class,
        CaseDetailsConverter.class,
        StateFlowEngine.class,
        EventHistorySequencer.class,
        EventHistoryMapper.class,
        RoboticsDataMapper.class,
        RoboticsAddressMapper.class,
        AddressLinesMapper.class,
        OrganisationService.class
    },
    properties = {
        "sendgrid.api-key:some-key",
        "robotics.notification.sender:no-reply@exaple.com",
        "robotics.notification.recipient:multipartyrecipient@example.com",
        "robotics.notification.specRecipient:multipartyrecipient@example.com",
        "robotics.notification.multipartyrecipient:multipartyrecipient@example.com",
        "robotics.notitfication.lipJRecipient:lipJ@example.com"
    }
)
class RoboticsNotificationServiceTest {

    @Autowired
    RoboticsNotificationService service;
    @Autowired
    RoboticsEmailConfiguration emailConfiguration;
    @Autowired
    RoboticsDataMapper roboticsDataMapper;
    @MockBean
    FeatureToggleService featureToggleService;

    @Captor
    private ArgumentCaptor<EmailData> emailDataArgumentCaptor;
    private static final String BEARER_TOKEN = "Bearer Token";
    @MockBean
    SendGridClient sendGridClient;
    @MockBean
    OrganisationApi organisationApi;
    @MockBean
    AuthTokenGenerator authTokenGenerator;
    @MockBean
    UserService userService;
    @MockBean
    PrdAdminUserConfiguration userConfig;
    @MockBean
    RoboticsDataMapperForSpec roboticsDataMapperForSpec;
    @MockBean
    private Time time;
    @MockBean
    LocationRefDataService locationRefDataService;
    @MockBean
    LocationRefDataUtil locationRefDataUtil;

    LocalDateTime localDateTime;

    @BeforeEach
    void setup() {
        localDateTime = LocalDateTime.of(2020, 8, 1, 12, 0, 0);
        when(time.now()).thenReturn(localDateTime);
    }

    @Test
    @SneakyThrows
    void shouldSendNotificationEmail_whenCaseDataIsProvided() {
        // Given
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
        if (caseData.getRespondent2OrgRegistered() != null
            && caseData.getRespondent2Represented() == null) {
            caseData = caseData.toBuilder()
                .respondent2Represented(YES)
                .build();
        }

        // When
        service.notifyRobotics(caseData, false, BEARER_TOKEN);

        verify(sendGridClient).sendEmail(eq(emailConfiguration.getSender()), emailDataArgumentCaptor.capture());

        EmailData capturedEmailData = emailDataArgumentCaptor.getValue();
        String reference = caseData.getLegacyCaseReference();
        String fileName = format("CaseData_%s.json", reference);
        String message = format("Robotics case data JSON is attached for %s", reference);
        String subject = format("Robotics case data for %s", reference);

        // Then
        assertThat(capturedEmailData.getSubject()).isEqualTo(subject);
        assertThat(capturedEmailData.getMessage()).isEqualTo(message);
        assertThat(capturedEmailData.getTo()).isEqualTo(emailConfiguration.getRecipient());
        assertThat(capturedEmailData.getAttachments()).hasSize(1);
        assertThat(capturedEmailData.getAttachments())
            .extracting("filename", "contentType")
            .containsExactlyInAnyOrder(tuple(fileName, "application/json"));
    }

    @Test
    @SneakyThrows
    void shouldSendNotificationEmailLRSpec_whenCaseDataIsProvided() {
        // Given
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build()
            .toBuilder().respondent1Represented(YES).caseAccessCategory(SPEC_CLAIM).build();
        if (caseData.getRespondent2OrgRegistered() != null
            && caseData.getRespondent2Represented() == null) {
            caseData = caseData.toBuilder()
                .respondent2Represented(YES)
                .build();
        }
        String lastEventText = "event text";
        RoboticsCaseDataSpec build = RoboticsCaseDataSpec.builder()
            .events(EventHistory.builder()
                .miscellaneous(Event.builder()
                   .eventDetailsText(lastEventText)
                   .dateReceived(LocalDateTime.now())
                   .build())
                .build())
            .build();
        when(roboticsDataMapperForSpec.toRoboticsCaseData(caseData)).thenReturn(build);

        // When
        service.notifyRobotics(caseData, false, BEARER_TOKEN);

        verify(sendGridClient).sendEmail(eq(emailConfiguration.getSender()), emailDataArgumentCaptor.capture());

        EmailData capturedEmailData = emailDataArgumentCaptor.getValue();
        String reference = caseData.getLegacyCaseReference();
        String fileName = format("CaseData_%s.json", reference);
        String message = format("Robotics case data JSON is attached for %s", reference);
        String subject = format("LR v LR Case Data for %s", reference);

        // Then
        assertThat(capturedEmailData.getSubject()).isEqualTo(subject);
        assertThat(capturedEmailData.getMessage()).isEqualTo(message);
        assertThat(capturedEmailData.getTo()).isEqualTo(emailConfiguration.getRecipient());
        assertThat(capturedEmailData.getAttachments()).hasSize(1);
        assertThat(capturedEmailData.getAttachments())
            .extracting("filename", "contentType")
            .containsExactlyInAnyOrder(tuple(fileName, "application/json"));
    }

    @Test
    void shouldThrowNullPointerException_whenCaseDataIsNull() {

        assertThrows(NullPointerException.class, () ->
            service.notifyRobotics(null, true, BEARER_TOKEN));
    }

    @Test
    @SneakyThrows
    void shouldSendNotificationEmailForMultiParty_whenCaseDataIsProvided() {
        // Given
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
            .respondent2(PartyBuilder.builder().individual().build())
            .addRespondent2(YES)
            .respondent2SameLegalRepresentative(NO)
            .build();
        if (caseData.getRespondent2OrgRegistered() != null
            && caseData.getRespondent2Represented() == null) {
            caseData = caseData.toBuilder()
                .respondent2Represented(YES)
                .build();
        }

        // When
        service.notifyRobotics(caseData, isMultiPartyScenario(caseData), BEARER_TOKEN);

        verify(sendGridClient).sendEmail(eq(emailConfiguration.getSender()), emailDataArgumentCaptor.capture());

        EmailData capturedEmailData = emailDataArgumentCaptor.getValue();
        String reference = caseData.getLegacyCaseReference();
        String fileName = format("CaseData_%s.json", reference);
        String message = format(
            "Multiparty claim data for %s - %s", reference, caseData.getCcdState()
        );
        String subject = format("Multiparty claim data for %s - %s - %s", reference, caseData.getCcdState(),
                                "Claim details notified.");

        // Then
        assertThat(capturedEmailData.getSubject()).isEqualTo(subject);
        assertThat(capturedEmailData.getMessage()).isEqualTo(message);
        assertThat(capturedEmailData.getAttachments()).hasSize(1);
        assertThat(capturedEmailData.getTo()).isEqualTo(emailConfiguration.getRecipient());
        assertThat(capturedEmailData.getAttachments())
            .extracting("filename", "contentType")
            .containsExactlyInAnyOrder(tuple(fileName, "application/json"));
    }

    @Test
    @SneakyThrows
    void shouldSendNotificationEmailForMultiPartySpec_whenCaseDataIsProvided() {
        // Given
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
            .caseAccessCategory(SPEC_CLAIM)
            .respondent2(PartyBuilder.builder().individual().build())
            .addRespondent2(YES)
            .respondent2SameLegalRepresentative(NO)
            .build();

        String lastEventText = "event text";
        RoboticsCaseDataSpec roboticsCaseData = RoboticsCaseDataSpec.builder()
            .events(EventHistory.builder().miscellaneous(
                Event.builder().eventDetailsText(lastEventText)
                    .dateReceived(LocalDateTime.now())
                    .build()
            ).build())
            .build();
        when(roboticsDataMapperForSpec.toRoboticsCaseData(caseData)).thenReturn(roboticsCaseData);

        boolean multiPartyScenario = isMultiPartyScenario(caseData);

        // When
        service.notifyRobotics(caseData, multiPartyScenario, BEARER_TOKEN);

        verify(roboticsDataMapperForSpec).toRoboticsCaseData(caseData);
        verify(sendGridClient).sendEmail(eq(emailConfiguration.getSender()), emailDataArgumentCaptor.capture());

        EmailData capturedEmailData = emailDataArgumentCaptor.getValue();
        String reference = caseData.getLegacyCaseReference();
        String message = format("Multiparty claim data for %s - %s", reference, caseData.getCcdState());
        String subject = format("Multiparty LR v LR Case Data for %s - %s - %s", reference, caseData.getCcdState(),
                                lastEventText);

        // Then
        assertThat(capturedEmailData.getSubject()).isEqualTo(subject);
        assertThat(capturedEmailData.getMessage()).isEqualTo(message);
        assertThat(capturedEmailData.getTo()).isEqualTo(emailConfiguration.getRecipient());
    }

    @Test
    @SneakyThrows
    void shouldSendNotificationEmailForMultiPartyWithMiscellaneousMsg_whenCaseDataIsProvided() {
        // Given
        CaseData caseData = CaseDataBuilder.builder()
            .multiPartyClaimOneDefendantSolicitor()
            .atState(FlowState.Main.FULL_DEFENCE)
            .respondent2Responds1v2SameSol(FULL_DEFENCE)
            .respondentResponseIsSame(YES)
            .respondent1DQ(Respondent1DQ.builder().build())
            .respondent2ClaimResponseIntentionType(ResponseIntention.FULL_DEFENCE)
            .build();
        if (caseData.getRespondent2OrgRegistered() != null
            && caseData.getRespondent2Represented() == null) {
            caseData = caseData.toBuilder()
                .respondent2Represented(YES)
                .build();
        }

        // When
        service.notifyRobotics(caseData, isMultiPartyScenario(caseData), BEARER_TOKEN);

        verify(sendGridClient).sendEmail(eq(emailConfiguration.getSender()), emailDataArgumentCaptor.capture());

        EmailData capturedEmailData = emailDataArgumentCaptor.getValue();
        String reference = caseData.getLegacyCaseReference();
        String fileName = format("CaseData_%s.json", reference);
        String message = format(
            "Multiparty claim data for %s - %s", reference, caseData.getCcdState()
        );
        String subject = format("Multiparty claim data for %s - %s - %s", reference, caseData.getCcdState(),
                                "[1 of 2 - 2020-08-01] Defendant: Mr. John Rambo has responded: "
                                    + "FULL_DEFENCE; preferredCourtCode: ; stayClaim: false");

        // Then
        assertThat(capturedEmailData.getSubject()).isEqualTo(subject);
        assertThat(capturedEmailData.getMessage()).isEqualTo(message);
        assertThat(capturedEmailData.getAttachments()).hasSize(1);
        assertThat(capturedEmailData.getTo()).isEqualTo(emailConfiguration.getRecipient());
        assertThat(capturedEmailData.getAttachments())
            .extracting("filename", "contentType")
            .containsExactlyInAnyOrder(tuple(fileName, "application/json"));
    }

    @Test
    void shouldSendNotificationEmailForLRvsLiP_whenCaseDataIsProvided() {
        //Given
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build()
            .toBuilder().respondent1Represented(NO).caseAccessCategory(SPEC_CLAIM).build();

        String lastEventText = "event text";
        RoboticsCaseDataSpec build = RoboticsCaseDataSpec.builder()
            .events(EventHistory.builder()
                        .miscellaneous(Event.builder()
                                           .eventDetailsText(lastEventText)
                                           .dateReceived(LocalDateTime.now())
                                           .build())
                        .build())
            .build();
        when(roboticsDataMapperForSpec.toRoboticsCaseData(caseData)).thenReturn(build);

        // When
        service.notifyRobotics(caseData, false, BEARER_TOKEN);

        // Then
        verify(sendGridClient).sendEmail(eq(emailConfiguration.getSender()), emailDataArgumentCaptor.capture());

        EmailData capturedEmailData = emailDataArgumentCaptor.getValue();
        String reference = caseData.getLegacyCaseReference();
        String fileName = format("CaseData_%s.json", reference);
        String message = format("Robotics case data JSON is attached for %s", reference);
        String subject = format("LR v LiP Case Data for %s", reference);

        assertThat(capturedEmailData.getSubject()).isEqualTo(subject);
        assertThat(capturedEmailData.getMessage()).isEqualTo(message);
        assertThat(capturedEmailData.getTo()).isEqualTo(emailConfiguration.getRecipient());
        assertThat(capturedEmailData.getAttachments()).hasSize(1);
        assertThat(capturedEmailData.getAttachments())
            .extracting("filename", "contentType")
            .containsExactlyInAnyOrder(tuple(fileName, "application/json"));
    }

    @Test
    void shouldNotifyJudgementLiP_whenPinInPostEnabledAndLipDefendant() {
        //Given
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build()
            .toBuilder().respondent1Represented(NO).caseAccessCategory(SPEC_CLAIM).build();
        when(featureToggleService.isPinInPostEnabled()).thenReturn(true);
        String lastEventText = "event text";
        RoboticsCaseDataSpec build = RoboticsCaseDataSpec.builder()
            .events(EventHistory.builder()
                        .miscellaneous(Event.builder()
                                           .eventDetailsText(lastEventText)
                                           .dateReceived(LocalDateTime.now())
                                           .build())
                        .build())
            .build();
        when(roboticsDataMapperForSpec.toRoboticsCaseData(caseData)).thenReturn(build);

        //When
        service.notifyJudgementLip(caseData);

        //Then
        verify(sendGridClient).sendEmail(eq(emailConfiguration.getSender()), emailDataArgumentCaptor.capture());

        EmailData capturedEmailData = emailDataArgumentCaptor.getValue();
        String reference = caseData.getLegacyCaseReference();
        String message = format("Robotics case data JSON is attached for %s", reference);
        String subject = format("LR v LiP Case Data for %s", reference);

        assertThat(capturedEmailData.getSubject()).isEqualTo(subject);
        assertThat(capturedEmailData.getMessage()).isEqualTo(message);
        assertThat(capturedEmailData.getTo()).isEqualTo(emailConfiguration.getLipJRecipient());
    }

    @Test
    void shouldNotifyDefaultJudgementLiP_whenLipDefendant() {
        //Given
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build()
            .toBuilder().respondent1Represented(NO).caseAccessCategory(SPEC_CLAIM).paymentTypeSelection(
                DJPaymentTypeSelection.SET_DATE).build();
        when(featureToggleService.isPinInPostEnabled()).thenReturn(true);
        String lastEventText = "event text";
        RoboticsCaseDataSpec build = RoboticsCaseDataSpec.builder()
            .events(EventHistory.builder()
                        .miscellaneous(Event.builder()
                                           .eventDetailsText(lastEventText)
                                           .dateReceived(LocalDateTime.now())
                                           .build())
                        .build())
            .build();
        when(roboticsDataMapperForSpec.toRoboticsCaseData(caseData)).thenReturn(build);

        //When
        service.notifyJudgementLip(caseData);

        //Then
        verify(sendGridClient).sendEmail(eq(emailConfiguration.getSender()), emailDataArgumentCaptor.capture());

        EmailData capturedEmailData = emailDataArgumentCaptor.getValue();
        String reference = caseData.getLegacyCaseReference();
        String message = format("Robotics case data JSON is attached for %s", reference);
        String subject = format("LR v LiP Default Judgement Case Data for %s", reference);

        assertThat(capturedEmailData.getSubject()).isEqualTo(subject);
        assertThat(capturedEmailData.getMessage()).isEqualTo(message);
        assertThat(capturedEmailData.getTo()).isEqualTo(emailConfiguration.getLipJRecipient());
    }

    @Test
    void shouldNotifyJudgementByAdmissionLiP_whenLipDefendant() {
        //Given
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build()
            .toBuilder().respondent1Represented(NO).caseAccessCategory(SPEC_CLAIM)
            .ccjPaymentDetails(CCJPaymentDetails.builder().ccjPaymentPaidSomeOption(YES).build()).build();
        when(featureToggleService.isPinInPostEnabled()).thenReturn(true);
        String lastEventText = "event text";
        RoboticsCaseDataSpec build = RoboticsCaseDataSpec.builder()
            .events(EventHistory.builder()
                        .miscellaneous(Event.builder()
                                           .eventDetailsText(lastEventText)
                                           .dateReceived(LocalDateTime.now())
                                           .build())
                        .build())
            .build();
        when(roboticsDataMapperForSpec.toRoboticsCaseData(caseData)).thenReturn(build);

        //When
        service.notifyJudgementLip(caseData);

        //Then
        verify(sendGridClient).sendEmail(eq(emailConfiguration.getSender()), emailDataArgumentCaptor.capture());

        EmailData capturedEmailData = emailDataArgumentCaptor.getValue();
        String reference = caseData.getLegacyCaseReference();
        String message = format("Robotics case data JSON is attached for %s", reference);
        String subject = format("LR v LiP Judgement by Admission Case Data for %s", reference);

        assertThat(capturedEmailData.getSubject()).isEqualTo(subject);
        assertThat(capturedEmailData.getMessage()).isEqualTo(message);
        assertThat(capturedEmailData.getTo()).isEqualTo(emailConfiguration.getLipJRecipient());
    }
}
