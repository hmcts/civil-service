package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationService;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public abstract class NotificationHandler extends CallbackHandler implements NotificationData {
    protected abstract CallbackResponse notifyParties(CallbackParams callbackParams);

    protected abstract void notifyApplicants(final CaseData caseData);

    protected abstract void notifyRespondents(final CaseData caseData);
}
