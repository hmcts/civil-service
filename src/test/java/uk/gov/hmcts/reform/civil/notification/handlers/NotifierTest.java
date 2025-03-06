package uk.gov.hmcts.reform.civil.notification.handlers;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationService;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotifierTest {

    private static final String MAIL = "test@test.com";
    private static final String TEMPLATE = "template";
    private static final String REFERENCE = "Reference";
    private static final HashMap<String, String> PARAMETERS = new HashMap<>();
    @Mock
    NotificationService notificationService;

    @Test
    void notifyParties() {

        EmailDTO emailDTO = EmailDTO.builder()
            .targetEmail(MAIL)
            .emailTemplate(TEMPLATE)
            .parameters(PARAMETERS)
            .reference(REFERENCE)
            .build();

        final HashSet<EmailDTO> parties = new HashSet<>();
        parties.add(emailDTO);

        Notifier notifier = new Notifier(notificationService, null, null, null) {
            @Override
            protected Set<EmailDTO> getPartiesToNotify(final CaseData caseData) {
                return parties;
            }

            @Override
            public Map<String, String> addProperties(final CaseData caseData) {
                return Map.of();
            }
        };

        final CaseData caseData = mock(CaseData.class);

        notifier.notifyParties(caseData);

        verify(notificationService).sendMail(eq(MAIL), eq(TEMPLATE),  eq(PARAMETERS), eq(REFERENCE));
    }
}
