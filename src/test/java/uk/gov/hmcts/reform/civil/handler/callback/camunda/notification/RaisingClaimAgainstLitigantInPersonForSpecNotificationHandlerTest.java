package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.Map;

import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_REFERENCES;

@ExtendWith(MockitoExtension.class)
public class RaisingClaimAgainstLitigantInPersonForSpecNotificationHandlerTest {

    @InjectMocks
    private RaisingClaimAgainstLitigantInPersonForSpecNotificationHandler handler;

    @Test
    public void addProperties() {
        CaseData caseData = CaseData.builder()
            .legacyCaseReference("reference")
            .build();

        Map<String, String> parameters = handler.addProperties(caseData);
        Assertions.assertTrue(parameters.containsKey(CLAIM_REFERENCE_NUMBER));
        Assertions.assertTrue(parameters.containsKey(PARTY_REFERENCES));
    }
}
