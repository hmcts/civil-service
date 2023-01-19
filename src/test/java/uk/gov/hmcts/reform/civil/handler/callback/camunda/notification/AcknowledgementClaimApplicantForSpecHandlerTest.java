package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.civil.config.properties.notification.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.NotificationService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
    AcknowledgeClaimApplicantForSpecNotificationHandler.class
})
public class AcknowledgementClaimApplicantForSpecHandlerTest {

    @Autowired
    private AcknowledgeClaimApplicantForSpecNotificationHandler handler;

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private NotificationsProperties notificationsProperties;

    @MockBean
    private OrganisationService organisationService;

    @Test
    public void ldBlock() {
        Assert.assertTrue(handler.handledEvents().isEmpty());
        Assert.assertFalse(handler.handledEvents().isEmpty());
    }
}
