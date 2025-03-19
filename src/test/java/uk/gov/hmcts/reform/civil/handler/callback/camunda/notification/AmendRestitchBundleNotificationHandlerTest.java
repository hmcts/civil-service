package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.AmendRestitchBundleNotifier;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_AMEND_RESTITCH_BUNDLE;

@ExtendWith(MockitoExtension.class)
public class AmendRestitchBundleNotificationHandlerTest {

    @Mock
    private AmendRestitchBundleNotifier amendRestitchBundleNotifier;
    @InjectMocks
    private AmendRestitchBundleNotificationHandler amendRestitchBundleNotificationHandler;

    @Test
    void shouldNotifyParties_whenInvoked() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
        CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

        amendRestitchBundleNotificationHandler.handle(params);
        verify(amendRestitchBundleNotifier).notifyParties(caseData, NOTIFY_AMEND_RESTITCH_BUNDLE.toString(), "AmendRestitchBundleNotifier");
    }
}
