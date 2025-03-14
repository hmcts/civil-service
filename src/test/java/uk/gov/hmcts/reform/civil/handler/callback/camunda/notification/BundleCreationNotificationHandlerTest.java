package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.BundleCreationNotifier;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_BUNDLE_CREATION;

@ExtendWith(MockitoExtension.class)
public class BundleCreationNotificationHandlerTest {

    @Mock
    private BundleCreationNotifier bundleCreationNotifier;
    @InjectMocks
    private BundleCreationNotificationHandler bundleCreationNotificationHandler;

    @Test
    void shouldNotifyParties_whenInvoked() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
        CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

        bundleCreationNotificationHandler.handle(params);
        verify(bundleCreationNotifier).notifyParties(caseData, NOTIFY_BUNDLE_CREATION.toString(), "BundleCreationNotifier");
    }
}
