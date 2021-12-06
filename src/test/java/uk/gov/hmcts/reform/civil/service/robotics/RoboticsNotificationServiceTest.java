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
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.launchdarkly.FeatureToggleService;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sendgrid.EmailData;
import uk.gov.hmcts.reform.civil.sendgrid.SendGridClient;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.flowstate.StateFlowEngine;
import uk.gov.hmcts.reform.civil.service.robotics.mapper.AddressLinesMapper;
import uk.gov.hmcts.reform.civil.service.robotics.mapper.EventHistoryMapper;
import uk.gov.hmcts.reform.civil.service.robotics.mapper.EventHistorySequencer;
import uk.gov.hmcts.reform.civil.service.robotics.mapper.RoboticsAddressMapper;
import uk.gov.hmcts.reform.civil.service.robotics.mapper.RoboticsDataMapper;
import uk.gov.hmcts.reform.civil.service.robotics.mapper.RoboticsDataMapperForSpec;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.prd.client.OrganisationApi;

import java.time.LocalDateTime;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
        "robotics.notification.recipient:recipient@example.com"
    }
)
class RoboticsNotificationServiceTest {

    @Autowired
    RoboticsNotificationService service;
    @Autowired
    RoboticsEmailConfiguration emailConfiguration;
    @MockBean
    FeatureToggleService featureToggleService;

    @Captor
    private ArgumentCaptor<EmailData> emailDataArgumentCaptor;

    @MockBean
    SendGridClient sendGridClient;
    @MockBean
    OrganisationApi organisationApi;
    @MockBean
    AuthTokenGenerator authTokenGenerator;
    @MockBean
    IdamClient idamClient;
    @MockBean
    PrdAdminUserConfiguration userConfig;
    @MockBean
    RoboticsDataMapperForSpec roboticsDataMapperForSpec;
    @MockBean
    private Time time;

    LocalDateTime localDateTime;

    @BeforeEach
    void setup() {
        localDateTime = LocalDateTime.of(2020, 8, 1, 12, 0, 0);
        when(time.now()).thenReturn(localDateTime);
    }

    @Test
    @SneakyThrows
    void shouldSendNotificationEmail_whenCaseDataIsProvided() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();

        service.notifyRobotics(caseData);

        verify(sendGridClient).sendEmail(eq(emailConfiguration.getSender()), emailDataArgumentCaptor.capture());

        EmailData capturedEmailData = emailDataArgumentCaptor.getValue();
        String reference = caseData.getLegacyCaseReference();
        String fileName = format("CaseData_%s.json", reference);
        String message = format("Robotics case data JSON is attached for %s", reference);
        String subject = format("Robotics case data for %s", reference);

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
            service.notifyRobotics(null));
    }
}
