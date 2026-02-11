package uk.gov.hmcts.reform.civil.service.robotics;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
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
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CCJPaymentDetails;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.model.robotics.Event;
import uk.gov.hmcts.reform.civil.model.robotics.EventDetails;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;
import uk.gov.hmcts.reform.civil.model.robotics.RoboticsCaseDataSpec;
import uk.gov.hmcts.reform.civil.prd.client.OrganisationApi;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.sendgrid.EmailData;
import uk.gov.hmcts.reform.civil.sendgrid.SendGridClient;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;
import uk.gov.hmcts.reform.civil.service.flowstate.SimpleStateFlowEngine;
import uk.gov.hmcts.reform.civil.service.flowstate.TransitionsTestConfiguration;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;
import uk.gov.hmcts.reform.civil.service.robotics.mapper.AddressLinesMapper;
import uk.gov.hmcts.reform.civil.service.robotics.mapper.EventHistoryMapper;
import uk.gov.hmcts.reform.civil.service.robotics.mapper.EventHistorySequencer;
import uk.gov.hmcts.reform.civil.service.robotics.mapper.RoboticsAddressMapper;
import uk.gov.hmcts.reform.civil.service.robotics.mapper.RoboticsDataMapperForSpec;
import uk.gov.hmcts.reform.civil.service.robotics.mapper.RoboticsDataMapperForUnspec;
import uk.gov.hmcts.reform.civil.stateflow.simplegrammar.SimpleStateFlowBuilder;
import uk.gov.hmcts.reform.civil.utils.LocationRefDataUtil;

import java.time.LocalDateTime;
import java.util.List;

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
        SimpleStateFlowEngine.class,
        SimpleStateFlowBuilder.class,
        TransitionsTestConfiguration.class,
        EventHistorySequencer.class,
        EventHistoryMapper.class,
        RoboticsDataMapperForUnspec.class,
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
    RoboticsDataMapperForUnspec roboticsDataMapper;
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
    LocationReferenceDataService locationRefDataService;
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
            caseData.setRespondent2Represented(YES);
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
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
        caseData.setRespondent1Represented(YES);
        caseData.setCaseAccessCategory(SPEC_CLAIM);
        if (caseData.getRespondent2OrgRegistered() != null
            && caseData.getRespondent2Represented() == null) {
            caseData.setRespondent2Represented(YES);
        }
        String lastEventText = "event text";
        RoboticsCaseDataSpec build = new RoboticsCaseDataSpec()
            .setEvents(new EventHistory()
                .setMiscellaneous(List.of(eventBuilder()
                    .eventDetailsText(lastEventText)
                    .dateReceived(LocalDateTime.now())
                    .build())))
            ;
        when(roboticsDataMapperForSpec.toRoboticsCaseData(caseData, BEARER_TOKEN)).thenReturn(build);

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
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
        caseData.setRespondent2(PartyBuilder.builder().individual().build());
        caseData.setAddRespondent2(YES);
        caseData.setRespondent2SameLegalRepresentative(NO);
        if (caseData.getRespondent2OrgRegistered() != null
            && caseData.getRespondent2Represented() == null) {
            caseData.setRespondent2Represented(YES);
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
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
        caseData.setCaseAccessCategory(SPEC_CLAIM);
        caseData.setRespondent2(PartyBuilder.builder().individual().build());
        caseData.setAddRespondent2(YES);
        caseData.setRespondent2SameLegalRepresentative(NO);

        String lastEventText = "event text";
        RoboticsCaseDataSpec roboticsCaseData = new RoboticsCaseDataSpec()
            .setEvents(new EventHistory().setMiscellaneous(List.of(
                eventBuilder().eventDetailsText(lastEventText)
                    .dateReceived(LocalDateTime.now())
                    .build()
            )))
            ;
        when(roboticsDataMapperForSpec.toRoboticsCaseData(caseData, BEARER_TOKEN)).thenReturn(roboticsCaseData);

        boolean multiPartyScenario = isMultiPartyScenario(caseData);

        // When
        service.notifyRobotics(caseData, multiPartyScenario, BEARER_TOKEN);

        verify(roboticsDataMapperForSpec).toRoboticsCaseData(caseData, BEARER_TOKEN);
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
            .respondent1DQ(new Respondent1DQ())
            .respondent2ClaimResponseIntentionType(ResponseIntention.FULL_DEFENCE)
            .build();
        if (caseData.getRespondent2OrgRegistered() != null
            && caseData.getRespondent2Represented() == null) {
            caseData.setRespondent2Represented(YES);
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
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
        caseData.setRespondent1Represented(NO);
        caseData.setCaseAccessCategory(SPEC_CLAIM);

        String lastEventText = "event text";
        RoboticsCaseDataSpec build = new RoboticsCaseDataSpec()
            .setEvents(new EventHistory()
                .setMiscellaneous(List.of(eventBuilder()
                    .eventDetailsText(lastEventText)
                    .dateReceived(LocalDateTime.now())
                    .build())))
            ;
        when(roboticsDataMapperForSpec.toRoboticsCaseData(caseData, BEARER_TOKEN)).thenReturn(build);

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
    void shouldNotifyJudgementLiP_whenLipDefendant() {
        //Given
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
        caseData.setRespondent1Represented(NO);
        caseData.setCaseAccessCategory(SPEC_CLAIM);
        String lastEventText = "event text";
        RoboticsCaseDataSpec build = new RoboticsCaseDataSpec()
            .setEvents(new EventHistory()
                .setMiscellaneous(List.of(eventBuilder()
                    .eventDetailsText(lastEventText)
                    .dateReceived(LocalDateTime.now())
                    .build())))
            ;
        when(roboticsDataMapperForSpec.toRoboticsCaseData(caseData, BEARER_TOKEN)).thenReturn(build);

        //When
        service.notifyJudgementLip(caseData, BEARER_TOKEN);

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
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
        caseData.setRespondent1Represented(NO);
        caseData.setCaseAccessCategory(SPEC_CLAIM);
        caseData.setPaymentTypeSelection(DJPaymentTypeSelection.SET_DATE);
        String lastEventText = "event text";
        RoboticsCaseDataSpec build = new RoboticsCaseDataSpec()
            .setEvents(new EventHistory()
                .setMiscellaneous(List.of(eventBuilder()
                    .eventDetailsText(lastEventText)
                    .dateReceived(LocalDateTime.now())
                    .build())))
            ;
        when(roboticsDataMapperForSpec.toRoboticsCaseData(caseData, BEARER_TOKEN)).thenReturn(build);

        //When
        service.notifyJudgementLip(caseData, BEARER_TOKEN);

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
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
        caseData.setRespondent1Represented(NO);
        caseData.setCaseAccessCategory(SPEC_CLAIM);
        caseData.setCcjPaymentDetails(CCJPaymentDetails.builder().ccjPaymentPaidSomeOption(YES).build());
        String lastEventText = "event text";
        RoboticsCaseDataSpec build = new RoboticsCaseDataSpec()
            .setEvents(new EventHistory()
                .setMiscellaneous(List.of(eventBuilder()
                    .eventDetailsText(lastEventText)
                    .dateReceived(LocalDateTime.now())
                    .build())))
            ;
        when(roboticsDataMapperForSpec.toRoboticsCaseData(caseData, BEARER_TOKEN)).thenReturn(build);

        //When
        service.notifyJudgementLip(caseData, BEARER_TOKEN);

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

    @Test
    void shouldNotifyJudgementLiP_whenLipvsLiPEnabled() {
        //Given
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
        caseData.setRespondent1Represented(NO);
        caseData.setApplicant1Represented(NO);
        caseData.setCaseAccessCategory(SPEC_CLAIM);
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
        String lastEventText = "event text";
        RoboticsCaseDataSpec build = new RoboticsCaseDataSpec()
            .setEvents(new EventHistory()
                .setMiscellaneous(List.of(eventBuilder()
                    .eventDetailsText(lastEventText)
                    .dateReceived(LocalDateTime.now())
                    .build())))
            ;
        when(roboticsDataMapperForSpec.toRoboticsCaseData(caseData, BEARER_TOKEN)).thenReturn(build);

        //When
        service.notifyJudgementLip(caseData, BEARER_TOKEN);

        //Then
        verify(sendGridClient).sendEmail(eq(emailConfiguration.getSender()), emailDataArgumentCaptor.capture());

        EmailData capturedEmailData = emailDataArgumentCaptor.getValue();
        String reference = caseData.getLegacyCaseReference();
        String message = format("Robotics case data JSON is attached for %s", reference);
        String subject = format("LiP v LiP Case Data for %s", reference);

        assertThat(capturedEmailData.getSubject()).isEqualTo(subject);
        assertThat(capturedEmailData.getMessage()).isEqualTo(message);
        assertThat(capturedEmailData.getTo()).isEqualTo(emailConfiguration.getLipJRecipient());
    }

    @ParameterizedTest
    @CsvSource({"DEFAULT_JUDGEMENT_SPEC", "DEFAULT_JUDGEMENT_NON_DIVERGENT_SPEC"})
    void shouldNotifyDefaultJudgementLiP_whenLipvsLiPEnabled(String camundaEvent) {
        //Given
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
        caseData.setRespondent1Represented(NO);
        caseData.setApplicant1Represented(NO);
        caseData.setCaseAccessCategory(SPEC_CLAIM);
        caseData.setPaymentTypeSelection(DJPaymentTypeSelection.SET_DATE);
        caseData.setBusinessProcess(BusinessProcess.builder()
            .camundaEvent(camundaEvent)
            .build());
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
        when(featureToggleService.isJudgmentOnlineLive()).thenReturn(true);
        when(featureToggleService.isJOLiveFeedActive()).thenReturn(true);
        String lastEventText = "event text";
        RoboticsCaseDataSpec build = new RoboticsCaseDataSpec()
            .setEvents(new EventHistory()
                .setMiscellaneous(List.of(eventBuilder()
                    .eventDetailsText(lastEventText)
                    .dateReceived(LocalDateTime.now())
                    .build())))
            ;
        when(roboticsDataMapperForSpec.toRoboticsCaseData(caseData, BEARER_TOKEN)).thenReturn(build);

        //When
        service.notifyJudgementLip(caseData, BEARER_TOKEN);

        //Then
        verify(sendGridClient).sendEmail(eq(emailConfiguration.getSender()), emailDataArgumentCaptor.capture());

        EmailData capturedEmailData = emailDataArgumentCaptor.getValue();
        String reference = caseData.getLegacyCaseReference();
        String message = format("Robotics case data JSON is attached for %s", reference);
        String subject = format("LiP v LiP Default Judgement Case Data for %s", reference);

        assertThat(capturedEmailData.getSubject()).isEqualTo(subject);
        assertThat(capturedEmailData.getMessage()).isEqualTo(message);
        assertThat(capturedEmailData.getTo()).isEqualTo(emailConfiguration.getLipJRecipient());
    }

    @Test
    void shouldNotifyJudgementByAdmissionLiP_whenLipvsLiPEnabled() {
        //Given
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
        caseData.setRespondent1Represented(NO);
        caseData.setApplicant1Represented(NO);
        caseData.setCaseAccessCategory(SPEC_CLAIM);
        caseData.setCcjPaymentDetails(CCJPaymentDetails.builder().ccjPaymentPaidSomeOption(YES).build());
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
        String lastEventText = "event text";
        RoboticsCaseDataSpec build = new RoboticsCaseDataSpec()
            .setEvents(new EventHistory()
                .setMiscellaneous(List.of(eventBuilder()
                    .eventDetailsText(lastEventText)
                    .dateReceived(LocalDateTime.now())
                    .build())))
            ;
        when(roboticsDataMapperForSpec.toRoboticsCaseData(caseData, BEARER_TOKEN)).thenReturn(build);

        //When
        service.notifyJudgementLip(caseData, BEARER_TOKEN);

        //Then
        verify(sendGridClient).sendEmail(eq(emailConfiguration.getSender()), emailDataArgumentCaptor.capture());

        EmailData capturedEmailData = emailDataArgumentCaptor.getValue();
        String reference = caseData.getLegacyCaseReference();
        String message = format("Robotics case data JSON is attached for %s", reference);
        String subject = format("LiP v LiP Judgement by Admission Case Data for %s", reference);

        assertThat(capturedEmailData.getSubject()).isEqualTo(subject);
        assertThat(capturedEmailData.getMessage()).isEqualTo(message);
        assertThat(capturedEmailData.getTo()).isEqualTo(emailConfiguration.getLipJRecipient());
    }

    @Test
    @SneakyThrows
    void shouldSendNotificationEmailWithLipVLrSubjectLine() {
        // Given
        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build();

        caseData.setApplicant1Represented(NO);
        caseData.setRespondent1Represented(YES);
        caseData.setCaseAccessCategory(SPEC_CLAIM);
        String lastEventText = "event text";
        RoboticsCaseDataSpec build = new RoboticsCaseDataSpec()
            .setEvents(new EventHistory()
                        .setMiscellaneous(List.of(eventBuilder()
                                           .eventDetailsText(lastEventText)
                                           .dateReceived(LocalDateTime.now())
                                           .build())))
            ;
        when(roboticsDataMapperForSpec.toRoboticsCaseData(caseData, BEARER_TOKEN)).thenReturn(build);

        // When
        service.notifyRobotics(caseData, false, BEARER_TOKEN);

        verify(sendGridClient).sendEmail(eq(emailConfiguration.getSender()), emailDataArgumentCaptor.capture());

        EmailData capturedEmailData = emailDataArgumentCaptor.getValue();
        String reference = caseData.getLegacyCaseReference();
        String fileName = format("CaseData_%s.json", reference);
        String message = format("Robotics case data JSON is attached for %s", reference);
        String subject = format("LIP v LR Case Data for %s", reference);

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
    void shouldSendNotificationEmailWithLipVLrDefaultJudgementSubjectLine() {
        // Given
        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build();

        caseData.setApplicant1Represented(NO);
        caseData.setRespondent1Represented(YES);
        caseData.setCaseAccessCategory(SPEC_CLAIM);
        caseData.setPaymentTypeSelection(DJPaymentTypeSelection.IMMEDIATELY);
        String lastEventText = "event text";
        RoboticsCaseDataSpec build = new RoboticsCaseDataSpec()
            .setEvents(new EventHistory()
                        .setMiscellaneous(List.of(eventBuilder()
                                           .eventDetailsText(lastEventText)
                                           .dateReceived(LocalDateTime.now())
                                           .build())))
            ;
        when(roboticsDataMapperForSpec.toRoboticsCaseData(caseData, BEARER_TOKEN)).thenReturn(build);

        // When
        service.notifyRobotics(caseData, false, BEARER_TOKEN);

        verify(sendGridClient).sendEmail(eq(emailConfiguration.getSender()), emailDataArgumentCaptor.capture());

        EmailData capturedEmailData = emailDataArgumentCaptor.getValue();
        String reference = caseData.getLegacyCaseReference();
        String fileName = format("CaseData_%s.json", reference);
        String message = format("Robotics case data JSON is attached for %s", reference);
        String subject = format("LIP v LR Default Judgment Case Data for %s", reference);

        // Then
        assertThat(capturedEmailData.getSubject()).isEqualTo(subject);
        assertThat(capturedEmailData.getMessage()).isEqualTo(message);
        assertThat(capturedEmailData.getTo()).isEqualTo(emailConfiguration.getRecipient());
        assertThat(capturedEmailData.getAttachments()).hasSize(1);
        assertThat(capturedEmailData.getAttachments())
            .extracting("filename", "contentType")
            .containsExactlyInAnyOrder(tuple(fileName, "application/json"));
    }

    private EventTestBuilder eventBuilder() {
        return new EventTestBuilder();
    }

    private static class EventTestBuilder {
        private final Event event = new Event();

        EventTestBuilder eventSequence(Integer sequence) {
            event.setEventSequence(sequence);
            return this;
        }

        EventTestBuilder eventCode(String code) {
            event.setEventCode(code);
            return this;
        }

        EventTestBuilder dateReceived(LocalDateTime dateReceived) {
            event.setDateReceived(dateReceived);
            return this;
        }

        EventTestBuilder litigiousPartyID(String id) {
            event.setLitigiousPartyID(id);
            return this;
        }

        EventTestBuilder eventDetails(EventDetails details) {
            event.setEventDetails(details);
            return this;
        }

        EventTestBuilder eventDetailsText(String text) {
            event.setEventDetailsText(text);
            return this;
        }

        Event build() {
            return event;
        }
    }
}
