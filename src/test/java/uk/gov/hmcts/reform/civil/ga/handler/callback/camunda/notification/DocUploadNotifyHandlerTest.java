package uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.notification;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.ga.handler.GeneralApplicationBaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.service.DocUploadNotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationException;
import uk.gov.hmcts.reform.civil.sampledata.GeneralApplicationCaseDataBuilder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GA_EVIDENCE_UPLOAD_CHECK;

@ExtendWith(MockitoExtension.class)
public class DocUploadNotifyHandlerTest extends GeneralApplicationBaseCallbackHandlerTest {

    @InjectMocks
    private DocUploadNotifyHandler handler;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @Mock
    DocUploadNotificationService notificationService;

    Logger logger = (Logger) LoggerFactory.getLogger(DocUploadNotifyHandler.class);
    ListAppender<ILoggingEvent> listAppender = new ListAppender<>();

    private CallbackParams params;

    @Test
    public void shouldReturnCorrectEvent() {
        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().withNoticeCaseData();
        params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        assertThat(handler.handledEvents()).contains(GA_EVIDENCE_UPLOAD_CHECK);
    }

    @Test
    public void shouldNotifyAll1v2WithNotice() {
        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().withNoticeCaseData();
        params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        handler.handle(params);
        verify(notificationService, times(1)).notifyApplicantEvidenceUpload(
                caseData
        );
        verify(notificationService, times(1)).notifyRespondentEvidenceUpload(
                caseData
        );
    }

    @Test
    public void shouldNotifyOnlyApp1v2WithoutNotice() {
        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder()
                .judicialOrderMadeWithUncloakApplication(YesOrNo.YES).build();
        params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        handler.handle(params);
        verify(notificationService, times(1)).notifyApplicantEvidenceUpload(
                caseData
        );
        verify(notificationService, times(0)).notifyRespondentEvidenceUpload(
                caseData
        );
    }

    @Test
    public void shouldLogError() {
        listAppender.start();
        logger.addAppender(listAppender);
        doThrow(buildNotificationException()).when(notificationService)
                .notifyApplicantEvidenceUpload(any());
        doThrow(buildNotificationException()).when(notificationService)
                .notifyRespondentEvidenceUpload(any());

        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder()
                .withNoticeCaseData();
        params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        handler.handle(params);
        List<ILoggingEvent> logsList = listAppender.list;
        assertThat(logsList.getFirst().getMessage()).contains("Failed to send email notification");
        assertEquals(Level.WARN, logsList.getFirst().getLevel());
        assertEquals(2, logsList.size());
        listAppender.stop();
    }

    private NotificationException buildNotificationException() {
        return new NotificationException(new Exception("Notification Exception"));
    }
}
