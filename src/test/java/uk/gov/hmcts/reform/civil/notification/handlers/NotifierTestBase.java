package uk.gov.hmcts.reform.civil.notification.handlers;

import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;

public abstract class NotifierTestBase {

    @Mock
    protected NotificationService notificationService;

    @Mock
    protected CaseTaskTrackingService caseTaskTrackingService;

    @Mock
    protected AllLegalRepsEmailGenerator emailGenerator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }
}
